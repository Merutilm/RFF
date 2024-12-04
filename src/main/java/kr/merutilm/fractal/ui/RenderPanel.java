package kr.merutilm.fractal.ui;

import javax.annotation.Nullable;
import javax.swing.*;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.io.BitMapImage;
import kr.merutilm.base.parallel.DoubleArrayDispatcher;
import kr.merutilm.base.parallel.ProcessVisualizer;
import kr.merutilm.base.parallel.RenderState;
import kr.merutilm.base.struct.DoubleMatrix;
import kr.merutilm.base.util.ConsoleUtils;
import kr.merutilm.base.util.TaskManager;
import kr.merutilm.customswing.CSMultiDialog;
import kr.merutilm.customswing.CSPanel;
import kr.merutilm.fractal.RFFUtils;
import kr.merutilm.fractal.formula.DeepMandelbrotPerturbator;
import kr.merutilm.fractal.formula.LightMandelbrotPerturbator;
import kr.merutilm.fractal.formula.MandelbrotPerturbator;
import kr.merutilm.fractal.formula.Perturbator;
import kr.merutilm.fractal.io.IOManager;
import kr.merutilm.fractal.io.RFFMap;
import kr.merutilm.fractal.locater.Locator;
import kr.merutilm.fractal.locater.MandelbrotLocator;
import kr.merutilm.fractal.settings.CalculationSettings;
import kr.merutilm.fractal.settings.ImageSettings;
import kr.merutilm.fractal.settings.Settings;
import kr.merutilm.fractal.settings.VideoSettings;
import kr.merutilm.fractal.struct.DoubleExponent;
import kr.merutilm.fractal.struct.LWBigComplex;
import kr.merutilm.fractal.theme.BasicTheme;
import kr.merutilm.fractal.util.DoubleExponentMath;
import kr.merutilm.fractal.util.LabelTextUtils;
import kr.merutilm.fractal.util.MathUtilities;

import static kr.merutilm.fractal.RFFUtils.selectFolder;
import static kr.merutilm.fractal.theme.BasicTheme.INIT_ITERATION;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

final class RenderPanel extends CSPanel {
    private final transient RenderState state = new RenderState();

    private transient BufferedImage currentImage;
    private final transient RFF master;
    private transient DoubleMatrix iterations;
    private transient RFFMap currentMap = null;
    private transient Thread currentThread;
    private transient Perturbator currentPerturbator;
    private static final double EXP_DEADLINE = 290;
    private static final String FINISHING_TEXT = "Finishing... ";
    private static final String IMAGE_EXPORT_DIR = "Fractals";
    

    private int lastPeriod = 1;

    public RenderPanel(RFF master, RenderWindow window) {
        super(window);
        this.master = master;
        setBackground(Color.BLACK);
        addListeners(window);
    }


    private int getImgWidth() {
        ImageSettings img = master.getSettings().imageSettings();
        return (int) (getWidth() * img.resolutionMultiplier());
    }

    private int getImgHeight() {
        ImageSettings img = master.getSettings().imageSettings();
        return (int) (getHeight() * img.resolutionMultiplier());
    }

    private int getMouseX(MouseEvent e) {
        ImageSettings img = master.getSettings().imageSettings();
        return (int) (e.getX() * img.resolutionMultiplier());
    }

    private int getMouseY(MouseEvent e) {
        ImageSettings img = master.getSettings().imageSettings();
        return (int) (e.getY() * img.resolutionMultiplier());
    }



    private void addListeners(RenderWindow window) {
        window.addKeyListener(this::resetToInit, KeyEvent.VK_R);
        window.addKeyListener(this::locateMinibrot, KeyEvent.VK_M);
        window.addKeyListener(this::findCenter, KeyEvent.VK_C);
        window.addKeyListener(this::cancel, KeyEvent.VK_ESCAPE);
        window.addKeyListener(this::openMap, KeyEvent.VK_M, true, true);
        window.addKeyListener(this::createVideoData, KeyEvent.VK_E, true, true);
        window.addKeyListener(() -> saveCurrentMapToImage(IMAGE_EXPORT_DIR), KeyEvent.VK_ENTER, true);
        window.addKeyListener(() -> {
            VideoSettings.Builder vsb = new VideoSettings.Builder();
            VideoSettings first = vsb.build();
            CSMultiDialog dialog = new CSMultiDialog(window, "Video Settings", 200, 150, () -> {
                File selected = RFFUtils.selectFolder("Select Sample Folder");
                if(selected == null){
                    return;
                }
                File toSave = RFFUtils.saveFile("Export", "mp4", "video");
                if(toSave == null){
                    return;
                }
                IOManager.exportZoomingVideo(master.getSettings(), vsb.build(), selected, toSave);
            });
            dialog.getInput().createTextInput("FPS", null, first.fps(), Double::parseDouble, vsb::setFps);
            dialog.getInput().createTextInput("Zoom Speed", null, first.logZoomPerSecond(), Double::parseDouble, vsb::setLogZoomPerSecond);
            dialog.getInput().createTextInput("Multi Sampling", null, first.multiSampling(), Double::parseDouble, vsb::setMultiSampling);
            dialog.setup();

        }, KeyEvent.VK_V, true, true);
        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                int r = e.getWheelRotation();

                final double mzi = 1.0 / Math.pow(10, CalculationSettings.ZOOM_VALUE);
                final double mzo = 1.0 / Math.pow(10, -CalculationSettings.ZOOM_VALUE);


                if (r == 1) {
                    DoubleExponent[] offset = offsetConversion(
                            getMouseX(e),
                            getMouseY(e)
                    );
                        
                    master.setSettings(e1 -> e1.edit().setCalculationSettings(      
                                    e2 -> e2.edit().addCenter(
                                            offset[0].multiply(1 - mzo), offset[1].multiply(1 - mzo), Perturbator.precision(e2.logZoom())
                                    ).zoomOut().build()
                            ).build()
                    );
                }
                if (r == -1) {
                    DoubleExponent[] offset = offsetConversion(
                            getMouseX(e),
                            getMouseY(e)
                    );
                    master.setSettings(e1 -> e1.edit().setCalculationSettings(
                                    e2 -> e2.edit().addCenter(
                                            offset[0].multiply(1 - mzi), offset[1].multiply(1 - mzi), Perturbator.precision(e2.logZoom())
                                    ).zoomIn().build()
                            ).build()
                    );

                }
                recompute();
            }
        });

        AtomicInteger pmx = new AtomicInteger();
        AtomicInteger pmy = new AtomicInteger();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (SwingUtilities.isLeftMouseButton(e)) {
                    pmx.set(getMouseX(e));
                    pmy.set(getMouseY(e));
                }

            }

        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                if(iterations == null){
                    return;
                }
                long it = (long) iterations.pipette(getMouseX(e), getMouseY(e));
                CalcSettingsPanel panel = master.getFractalStatus().getFractalCalc();
                panel.setIterationText(it);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int dx = pmx.get() - getMouseX(e);
                    int dy = pmy.get() - getMouseY(e);
                    double m = master.getSettings().imageSettings().resolutionMultiplier();
                    master.setSettings(e1 -> e1.edit().setCalculationSettings(
                                    e2 -> e2.edit().addCenter(
                                                    DoubleExponent.valueOf(dx / m).divide(getDivisor()),
                                                    DoubleExponent.valueOf(-dy / m).divide(getDivisor()), Perturbator.precision(e2.logZoom()))
                                            .build()
                            ).build()
                    );

                    pmx.set(getMouseX(e));
                    pmy.set(getMouseY(e));
                    recompute();
                }
            }
        });

    }
    
    private void cancel() {
        state.createBreakpoint();
    }

    private void resetToInit() {
        master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit()
                .setCenter(BasicTheme.INIT_C)
                .setLogZoom(BasicTheme.INIT_LOG_ZOOM)
                .build()).build());
        recompute();
    }

    private void findCenter() {
        
        if (currentPerturbator == null) {
            return;
        }
        LWBigComplex c = MandelbrotLocator.findCenter((MandelbrotPerturbator) currentPerturbator);

        if (c != null) {
            master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit()
                    .setCenter(c)
                    .build()).build());
            recompute();
        }else{
            checkNullLocator(null);
        }
    }

    private void createVideoData(){

        File dir = selectFolder("Folder to Export Samples");
        if(dir == null){
            return;
        }   
        try{
            if(dir.exists()){
                for (File f : dir.listFiles()) {
                    Files.delete(f.toPath());
                }
            }

            TaskManager.runTask(() -> {

                try{
                    int id = state.getId();
                    while(master.getSettings().calculationSettings().logZoom() > 1 && id == state.getId()){
                        id++;
                        recompute();
                        currentThread.join();
                        IOManager.exportMap(dir, master.getSettings().calculationSettings().maxIteration(), iterations);
                        master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit().zoomOut(MathUtilities.LOG2).build()).build());
                    }

                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            });

           

        }catch(IOException e){
            ConsoleUtils.logError(e);
        }
       
    }


    private BiConsumer<Integer, Integer> getActionWhileFindingMinibrotCenter(int period){
        CalcSettingsPanel panel = master.getFractalStatus().getFractalCalc();
        int interval = periodPanelRefreshInterval();
        return (p, i) -> {
                            
            if (p % interval == 0) {
                panel.setProcess("Locating Center... "
                        + LabelTextUtils
                                .processText((double) p / period)
                        + " [" + i + "]");
            }
        };
    }

    private DoubleConsumer getActionWhileFindingMinibrotZoom(){
        CalcSettingsPanel panel = master.getFractalStatus().getFractalCalc();
        return d -> TaskManager.runTask(() -> panel.setProcess("Finding Zoom... 10^-" + String.format("%.2f", d)));
    }


    private synchronized void locateMinibrot() {
        if (currentPerturbator == null) {
            return;
        }
        TaskManager.runTask(() -> {
            
            AtomicInteger id = new AtomicInteger();
            int period = currentPerturbator.getReference().period();
            MandelbrotLocator locator = MandelbrotLocator.locateMinibrot(state, state.getId(), (MandelbrotPerturbator) currentPerturbator,
                    getActionWhileFindingMinibrotCenter(period),
                    getActionWhileFindingMinibrotZoom()
                    );

            id.getAndIncrement();
            if (checkNullLocator(locator)) {
                master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit()
                        .setCenter(locator.center())
                        .setLogZoom(locator.logZoom())
                        .build()).build());
                recompute();
            }
        });

    }

    private boolean checkNullLocator(@Nullable Locator locator) {
        if (locator == null) {
            JOptionPane.showMessageDialog(null, "Cannot find center. Zoom in a little and try again.", "Locate Minibrot", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void openMap(){
        File file = RFFUtils.selectFile("Open Map", RFFUtils.EXTENSION_MAP, "RFF Map");
        if(file == null){
            return;
        }
        RFFMap map = IOManager.readMap(file);
        currentMap = map;
        try{
            reloadAndPaint(state.getId(), false);
        }catch(IllegalRenderStateException e){
            //noop
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    private void saveCurrentMapToImage(String dir) {
        File file = RFFUtils.mkdir(dir);
        File dest = RFFUtils.generateNewFile(file, "png");
        try {
            new BitMapImage(currentImage).export(dest);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    private DoubleExponent getDivisor() {
        double logZoom = master.getSettings().calculationSettings().logZoom();
        return DoubleExponentMath.pow10(logZoom);
    }


    private DoubleExponent[] offsetConversion(double px, double py) {
        ImageSettings img = master.getSettings().imageSettings();
        double resolutionMultiplier = img.resolutionMultiplier();
        return new DoubleExponent[]{
                DoubleExponent.valueOf(px / resolutionMultiplier - getWidth() / 2.0).divide(getDivisor()),
                DoubleExponent.valueOf(getHeight() / 2.0 - py / resolutionMultiplier).divide(getDivisor())
        };
    }



    public synchronized void recompute() {

        if (currentThread != null) {
            try {
                state.createBreakpoint();
                currentThread.interrupt();
                currentThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        int id = state.getId();
        currentThread = TaskManager.runTask(() -> compute(id));
    }


    //this method can be thread-safe only when called via recompute()
    private void compute(int currentID) {
        
        int w = getImgWidth();
        int h = getImgHeight();
        iterations = new DoubleMatrix(w, h);
        currentMap = null;
        Settings settings = master.getSettings();
        ShaderProcessor.fillInit(iterations);

        if (master.getSettings().calculationSettings().autoIteration()) {
            master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit().setMaxIteration(Math.max(INIT_ITERATION, lastPeriod * 50L)).build()).build());
        }

        
        CalculationSettings calc = settings.calculationSettings();
        int precision = Perturbator.precision(calc.logZoom());
        double logZoom = calc.logZoom();

        try {
         
            CalcSettingsPanel panel = master.getFractalStatus().getFractalCalc();
            panel.refreshMaxIterationText();
            panel.initTime();
            panel.setZoomText(logZoom);

            DoubleArrayDispatcher generator = new DoubleArrayDispatcher(state, currentID, iterations);
            DoubleExponent[] offset = offsetConversion(0, 0);
            DoubleExponent dcMax = DoubleExponentMath.hypot(offset[0], offset[1]);

            Thread t1 = TaskManager.runTask(() -> {
                try {
                    while (currentPerturbator != null && state.getId() == currentID) {
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            int refreshInterval = periodPanelRefreshInterval();
            IntConsumer actionPerRefCalcIteration = p -> {
                if(p % refreshInterval == 0){
                    panel.setProcess("Period " + p);
                }
            };

            switch (calc.reuseReference()) {
                case CURRENT_REFERENCE -> {
                    currentPerturbator = currentPerturbator.reuse(state, currentID, calc, currentPerturbator.getDcMaxByDoubleExponent(), precision);
                }
                case CENTERED_REFERENCE ->  {
                    int period = currentPerturbator.getReference().period();
                    MandelbrotLocator center = MandelbrotLocator.locateMinibrot(state, currentID, (MandelbrotPerturbator)currentPerturbator, getActionWhileFindingMinibrotCenter(period), getActionWhileFindingMinibrotZoom());
                    if(center != null){
                        
                        CalculationSettings refCalc = calc.edit().setCenter(center.center()).setLogZoom(center.logZoom()).build();
                        int refPrecision = Perturbator.precision(center.logZoom());
                        if (refCalc.logZoom() > EXP_DEADLINE) {
                            currentPerturbator = new DeepMandelbrotPerturbator(state, currentID, refCalc, center.dcMax(), refPrecision, period, actionPerRefCalcIteration)
                                    .reuse(state, currentID, calc, dcMax, precision);
                        }else{
                            currentPerturbator = new LightMandelbrotPerturbator(state, currentID, refCalc, center.dcMax().doubleValue(), refPrecision, period, actionPerRefCalcIteration)
                                    .reuse(state, currentID, calc, dcMax, precision);
                        }
                    }
                    
                }
                case DISABLED -> {
                    if (logZoom > EXP_DEADLINE) {
                        currentPerturbator = new DeepMandelbrotPerturbator(state, currentID, calc, dcMax, precision, -1, actionPerRefCalcIteration);
                    } else {
                        currentPerturbator = new LightMandelbrotPerturbator(state, currentID, calc, dcMax.doubleValue(), precision, -1, actionPerRefCalcIteration);
                    }
                }
            }


            t1.interrupt();
            t1.join();

            

            generator.createRenderer((x, y, xRes, yRes, rx, ry, i, v, t) -> {
                DoubleExponent[] dc = offsetConversion(x, y);
                return currentPerturbator.iterate(dc[0], dc[1]);
            });

            int period = currentPerturbator.getReference().period();
            panel.setPeriodText(period);
            setPeriodDirectly(period);

            panel.setProcess("Preparing...");
            generator.process(p -> {

                boolean processing = p < 1;

                if (ShaderProcessor.getCompressDivisor(settings.imageSettings()) > 1 || processing) {
                    reloadAndPaint(currentID, true);
                }

                if (processing) {
                    panel.setProcess("Calculating... " + LabelTextUtils.processText(p));
                } else {
                    reloadAndPaint(currentID, false);
                    panel.setProcess("Done");
                }
                panel.refreshTime();
            }, 500);

        } catch (IllegalRenderStateException ignored) {
            //noop
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


    }


    public void setPeriodDirectly(int period) {
        this.lastPeriod = period;
    }

    private int periodPanelRefreshInterval(){
        return (int) (1000000 / master.getSettings().calculationSettings().logZoom());
    }

    public void reloadAndPaintCurrentIterations() {
        TaskManager.runTask(() -> {
            try {
                CalcSettingsPanel panel = master.getFractalStatus().getFractalCalc();
                reloadAndPaint(state.getId(), false);
                panel.setProcess("Done");
            } catch (IllegalRenderStateException | InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

    }

    private ProcessVisualizer gvf(int fracA, int fracB){
        CalcSettingsPanel panel = master.getFractalStatus().getFractalCalc();
        return a -> panel.setProcess(FINISHING_TEXT + LabelTextUtils.processText(a)
        + LabelTextUtils.frac(fracA, fracB, LabelTextUtils.Parentheses.SQUARE));
    }

    
    public synchronized void reloadAndPaint(int currentID, boolean compressed) throws IllegalRenderStateException, InterruptedException {
        ProcessVisualizer[] pv = new ProcessVisualizer[]{
            gvf(1, 3), 
            gvf(2, 3),
            gvf(3, 3)
        };
        if(currentMap == null){
            currentImage = ShaderProcessor.createImageWithVisualizer(state, currentID, iterations, master.getSettings(), compressed, pv);
        }else{
            currentImage = ShaderProcessor.createImageWithVisualizer(state, currentID, currentMap.iterations(), IOManager.modifyToMapSettings(currentMap, master.getSettings()), false, pv);
        }
            
        repaint();

    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        BitMapImage.highGraphics(g2);
        g2.drawImage(currentImage, 0, 0, getWidth(), getHeight(), null);
    }

}
