package kr.merutilm.fractal.ui;

import javax.annotation.Nullable;
import javax.swing.*;

import org.jcodec.api.awt.AWTSequenceEncoder;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.io.BitMap;
import kr.merutilm.base.io.BitMapImage;
import kr.merutilm.base.parallel.DoubleArrayDispatcher;
import kr.merutilm.base.parallel.RenderState;
import kr.merutilm.base.parallel.ShaderDispatcher;
import kr.merutilm.base.struct.DoubleMatrix;
import kr.merutilm.base.struct.HexColor;
import kr.merutilm.base.util.ConsoleUtils;
import kr.merutilm.base.util.TaskManager;
import kr.merutilm.customswing.CSPanel;
import kr.merutilm.fractal.RFFUtils;
import kr.merutilm.fractal.formula.DeepMandelbrotPerturbator;
import kr.merutilm.fractal.formula.LightMandelbrotPerturbator;
import kr.merutilm.fractal.formula.MandelbrotPerturbator;
import kr.merutilm.fractal.formula.Perturbator;
import kr.merutilm.fractal.locater.Locator;
import kr.merutilm.fractal.locater.MandelbrotLocator;
import kr.merutilm.fractal.settings.CalculationSettings;
import kr.merutilm.fractal.settings.ColorSettings;
import kr.merutilm.fractal.settings.ImageSettings;
import kr.merutilm.fractal.settings.Settings;
import kr.merutilm.fractal.shader.Bloom;
import kr.merutilm.fractal.shader.ColorFilter;
import kr.merutilm.fractal.shader.Fog;
import kr.merutilm.fractal.shader.Slope;
import kr.merutilm.fractal.struct.DoubleExponent;
import kr.merutilm.fractal.struct.LWBigComplex;
import kr.merutilm.fractal.theme.BasicTheme;
import kr.merutilm.fractal.util.DoubleExponentMath;
import kr.merutilm.fractal.util.LabelTextUtils;

import static kr.merutilm.fractal.theme.BasicTheme.INIT_ITERATION;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

final class RenderPanel extends CSPanel {
    private final transient RenderState state = new RenderState();

    private transient BufferedImage currentImage;
    private final transient RFF master;
    private transient DoubleMatrix iterations;
    private transient Thread currentThread;
    private transient Perturbator currentPerturbator;
    private static final double EXP_DEADLINE = 290;
    private static final int NOT_RENDERED = -1;
    private static final double COMPRESSION_CRITERIA = 1;
    private static final String FINISHING_TEXT = "Finishing... ";
    private static final String VIDEO_EXPORT_DIR = "Videos";
    private static final double LOG2 = Math.log10(2);
    

    private int lastPeriod = 1;

    public RenderPanel(RFF master, RenderWindow window) {
        super(window);
        this.master = master;
        setBackground(Color.BLACK);
        addListeners(window);
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

    private void exportZoomMap(){

        File file = new File(RFFUtils.getOriginalResource(), VIDEO_EXPORT_DIR);
        try{
            if(file.exists()){
                for (File f : file.listFiles()) {
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
                        saveMap();
                        master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit().zoomOut(LOG2).build()).build());
                    }
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            });
           

        }catch(IOException e){
            ConsoleUtils.logError(e);
        }
       
    }

    private void exportZoomingVideo(){

        double logZoomPerSecond = 0.5;
        double frameZoomingPerSecond = logZoomPerSecond / LOG2;
        int fps = 5;
        double frameInterval = frameZoomingPerSecond / fps;
        int currentID = state.getId();
        File file = new File(RFFUtils.getOriginalResource(), VIDEO_EXPORT_DIR);
        try{

            AWTSequenceEncoder encoder = AWTSequenceEncoder.createSequenceEncoder(new File(file, "video.avi"), fps);
            DoubleMatrix frame;
            double currentFrameNumber = 1;
        
            while(true) {    
                frame = getFrame(currentID, currentFrameNumber);
                if(frame == null){
                    break;
                }
                reloadColor(frame, currentID, false);

                encoder.encodeImage(currentImage);
                
                currentFrameNumber += frameInterval;
            }
        
            encoder.finish();
        }catch(IOException e){
            ConsoleUtils.logError(e);
        }catch(IllegalRenderStateException e){
            //noop
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
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
        window.addKeyListener(this::exportZoomMap, KeyEvent.VK_E, true, true);
        window.addKeyListener(this::exportZoomingVideo, KeyEvent.VK_V, true, true);
        window.addKeyListener(() -> saveImage("Fractals"), KeyEvent.VK_ENTER, true);
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

    private static byte[] iterationToByteArray(double iteration){
            
        long v = Double.doubleToLongBits(iteration);
        byte[] arr = new byte[8];

        arr[0] = (byte)((v >>> 56) & 0xff);
        arr[1] = (byte)((v >>> 48) & 0xff);
        arr[2] = (byte)((v >>> 40) & 0xff);
        arr[3] = (byte)((v >>> 32) & 0xff);
        arr[4] = (byte)((v >>> 24) & 0xff);
        arr[5] = (byte)((v >>> 16) & 0xff);
        arr[6] = (byte)((v >>> 8) & 0xff);
        arr[7] = (byte)(v & 0xff);
        
        return arr;
    }

    private static double byteArrayToIteration(byte[] arr){
            
        long a1 = (((long) arr[0]) & 0xff) << 56;
        long a2 = (((long) arr[1]) & 0xff) << 48;
        long a3 = (((long) arr[2]) & 0xff) << 40;
        long a4 = (((long) arr[3]) & 0xff) << 32;
        long a5 = (((long) arr[4]) & 0xff) << 24;
        long a6 = (((long) arr[5]) & 0xff) << 16;
        long a7 = (((long) arr[6]) & 0xff) << 8;
        long a8 = arr[7] & 0xff;
        long result = a1 | a2 | a3 | a4 | a5 | a6 | a7 | a8;
        return Double.longBitsToDouble(result);
    }

    private DoubleMatrix getIteratonMapFromFile(int frameNumber){
        File file = RFFUtils.mkdir(VIDEO_EXPORT_DIR);
        File dest = new File(file, RFFUtils.numberToDefaultFileName(frameNumber) + "." + RFF.EXTENSION_MAP);
        if(!dest.exists()){
            return null;
        }
        try(FileInputStream stream = new FileInputStream(dest)) {
            byte[] data = stream.readAllBytes();
            int width = ((data[0] << 8) | (data[1] & 0xff));
            int len = (data.length - 2) / 8;
            int height = len / width;
            double[] iterations = new double[len];
            for (int i = 2; i < data.length; i+=8) {
                iterations[i / 8] = byteArrayToIteration(Arrays.copyOfRange(data, i, i+8));
            }
            return new DoubleMatrix(width, height, iterations);
        }catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    private DoubleMatrix getFrame(int currentID, double frame) throws IllegalRenderStateException, InterruptedException{
        int f1 = (int) frame; // it is smaller
        int f2 = f1 + 1; 
        //frame size : f1 = 1x, f2 = 2x
        double r = 1 - frame + f1;
        DoubleMatrix m1 = getIteratonMapFromFile(f1);
        DoubleMatrix m2 = getIteratonMapFromFile(f2);

        if(m1 == null || m2 == null){
            return null;
        }

        double ssr = Math.pow(2, r - 1);
        double lsr = ssr * 2;

        DoubleMatrix result = new DoubleMatrix(m1.getWidth(), m1.getHeight());
        DoubleArrayDispatcher dispatcher = new DoubleArrayDispatcher(state, currentID, result);
        dispatcher.createRenderer((x, y, xRes, yRes, rx, ry, i, col, t) -> {
            double dx = xRes * (rx - 0.5);
            double dy = yRes * (ry - 0.5);
            double x1 = xRes / 2.0 + dx / ssr;
            double y1 = yRes / 2.0 + dy / ssr;
            double x2 = xRes / 2.0 + dx / lsr;
            double y2 = yRes / 2.0 + dy / lsr;
            if(x1 < 0 || x1 > xRes || y1 < 0 || y1 > yRes){
                return m2.pipetteAdvanced(x2, y2);
            }
            return m1.pipetteAdvanced(x1, y1);
        });
        dispatcher.dispatch();
        return result;
    }

    private void saveMap(){
        File file = RFFUtils.mkdir(VIDEO_EXPORT_DIR);
        File dest = RFFUtils.generateNewFile(file, RFF.EXTENSION_MAP);
        try(FileOutputStream stream = new FileOutputStream(dest)) {
            double[] canvas = iterations.getCanvas();
            int width = iterations.getWidth();

            stream.write((byte)((width >>> 8) & 0x000000ffL));
            stream.write((byte)((width) & 0x000000ffL));
            byte[] arr = new byte[canvas.length * 8];
            for (int i = 0; i < canvas.length; i++) {
                System.arraycopy(iterationToByteArray(canvas[i]), 0, arr, i * 8, 8);
            }

            stream.write(arr);
            
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    private void saveImage(String dir) {
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


    private HexColor getColorByIteration(double iteration) {

        if (iteration >= master.getSettings().calculationSettings().maxIteration()) {
            return HexColor.BLACK;
        }
        if (iteration == NOT_RENDERED) {
            return null;
        }
        ColorSettings col = master.getSettings().imageSettings().colorSettings();
        double r = iteration % 1;

        double value = switch (col.colorSmoothing()) {
            case NONE -> (long) iteration;
            case REVERSED -> (long) iteration + 1 - r;
            default -> (long) iteration + r;
        };


        return col.getColor(value);
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
        Arrays.fill(iterations.getCanvas(), NOT_RENDERED);

        if (master.getSettings().calculationSettings().autoIteration()) {
            master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit().setMaxIteration(Math.max(INIT_ITERATION, lastPeriod * 50L)).build()).build());
        }

        Settings settings = master.getSettings();

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

            

            generator.createRenderer((x, y, xRes, yRes, rx, ry, i, col, t) -> {
                DoubleExponent[] dc = offsetConversion(x, y);
                return currentPerturbator.iterate(dc[0], dc[1]);
            });

            int period = currentPerturbator.getReference().period();
            panel.setPeriodText(period);
            setPeriodDirectly(period);

            panel.setProcess("Preparing...");
            generator.process(p -> {

                boolean processing = p < 1;

                if (getCompressDivisor() > 1 || processing) {
                    reloadColor(iterations, currentID, true);
                }

                if (processing) {
                    panel.setProcess("Calculating... " + LabelTextUtils.processText(p));
                } else {
                    reloadColor(iterations, currentID, false);
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

    public void reloadColor() {
        TaskManager.runTask(() -> {
            try {
                CalcSettingsPanel panel = master.getFractalStatus().getFractalCalc();
                reloadColor(iterations, state.getId(), false);
                panel.setProcess("Done");
            } catch (IllegalRenderStateException | InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

    }


    private int getCompressDivisor() {
        ImageSettings img = master.getSettings().imageSettings();
        return Math.max(1, (int) ((int) img.resolutionMultiplier() / COMPRESSION_CRITERIA));
    }

    public synchronized void reloadColor(DoubleMatrix iterations, int currentID, boolean compressed) throws IllegalRenderStateException, InterruptedException {


        int compressDivisor = getCompressDivisor();
        int currentDivisor = compressed ? compressDivisor : 1;

        BitMap bitMap = new BitMap(iterations.getWidth() / currentDivisor, iterations.getHeight() / currentDivisor);


        basicShaders(bitMap, iterations, currentID, compressed);
        postProcessing(bitMap, iterations, currentID, compressed);

        currentImage = bitMap.getImage();
        repaint();

    }


    private synchronized void basicShaders(BitMap bitMap, DoubleMatrix iterations, int currentID, boolean compressed) throws IllegalRenderStateException, InterruptedException {
        CalcSettingsPanel panel = master.getFractalStatus().getFractalCalc();
        ImageSettings img = master.getSettings().imageSettings();
        ShaderDispatcher pp1 = new ShaderDispatcher(state, currentID, bitMap);
        int fitResolutionMultiplier = iterations.getWidth() / bitMap.getWidth();

        pp1.createRenderer((x, y, xRes, yRes, rx, ry, i, c, t) -> getColorByIteration(iterations.pipette(x * fitResolutionMultiplier, y * fitResolutionMultiplier)));
        pp1.createRenderer(new Slope(iterations, img.slopeSettings(), img.resolutionMultiplier(), fitResolutionMultiplier));
        pp1.createRenderer(new ColorFilter(img.colorFilterSettings()));


        if (compressed) {
            pp1.dispatch();
        } else {
            pp1.process(a -> panel.setProcess(FINISHING_TEXT + LabelTextUtils.processText(a)
                    + LabelTextUtils.frac(1, 3, LabelTextUtils.Parentheses.SQUARE)), 400);
        }
    }

    private synchronized void postProcessing(BitMap bitMap, DoubleMatrix iterations, int currentID, boolean compressed) throws IllegalRenderStateException, InterruptedException {

        CalcSettingsPanel panel = master.getFractalStatus().getFractalCalc();
        ImageSettings img = master.getSettings().imageSettings();
        int compressDivisor = getCompressDivisor();

        BitMap compressedBitMap = compressDivisor > 1 ? new BitMap(iterations.getWidth() / compressDivisor, iterations.getHeight() / compressDivisor) : bitMap;
        if (compressDivisor > 1) {
            basicShaders(compressedBitMap, iterations, currentID, true);
        }

        ShaderDispatcher pp2 = new ShaderDispatcher(state, currentID, bitMap);
        pp2.createRenderer(new Fog(bitMap, compressedBitMap, img.fogSettings()));


        if (compressed) {
            pp2.dispatch();
        } else {
            pp2.process(a -> panel.setProcess(FINISHING_TEXT + LabelTextUtils.processText(a)
                    + LabelTextUtils.frac(2, 3, LabelTextUtils.Parentheses.SQUARE)), 400);
        }

        ShaderDispatcher pp3 = new ShaderDispatcher(state, currentID, bitMap);

        pp3.createRenderer(new Bloom(bitMap, compressedBitMap, img.bloomSettings()));
        pp3.createRenderer((x, y, xRes, yRes, rx, ry, i, c, t) -> {

            HexColor a1 = pp3.texture2D(x, y - 1);
            HexColor a2 = pp3.texture2D(x, y + 1);
            HexColor a3 = pp3.texture2D(x + 1, y);
            HexColor a4 = pp3.texture2D(x - 1, y);
            return HexColor.average(a1, a2, a3, a4);
        });

        if (compressed) {
            pp3.dispatch();
        } else {
            pp3.process(a -> panel.setProcess(FINISHING_TEXT + LabelTextUtils.processText(a)
                    + LabelTextUtils.frac(3, 3, LabelTextUtils.Parentheses.SQUARE)), 400);
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        BitMapImage.highGraphics(g2);
        g2.drawImage(currentImage, 0, 0, getWidth(), getHeight(), null);
    }

}
