package kr.merutilm.rff.ui;

import javax.swing.*;

import kr.merutilm.rff.shader.IllegalRenderStateException;
import kr.merutilm.rff.io.BitMapImage;
import kr.merutilm.rff.shader.DoubleArrayDispatcher;
import kr.merutilm.rff.shader.ProcessVisualizer;
import kr.merutilm.rff.shader.RenderState;
import kr.merutilm.rff.struct.DoubleMatrix;
import kr.merutilm.rff.formula.DeepMandelbrotPerturbator;
import kr.merutilm.rff.formula.LightMandelbrotPerturbator;
import kr.merutilm.rff.formula.MandelbrotPerturbator;
import kr.merutilm.rff.formula.Perturbator;
import kr.merutilm.rff.io.RFFMap;
import kr.merutilm.rff.locater.MandelbrotLocator;
import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.settings.ImageSettings;
import kr.merutilm.rff.settings.Settings;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.util.DoubleExponentMath;
import kr.merutilm.rff.util.TextFormatter;

import static kr.merutilm.rff.theme.BasicTheme.INIT_ITERATION;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

final class RFFRenderPanel extends JPanel {
    private final transient RenderState state = new RenderState();

    private transient BufferedImage currentImage;
    private transient RFFMap currentMap;
    private transient Perturbator currentPerturbator;
    private final transient RFF master;
    private static final double EXP_DEADLINE = 290;
    private static final String FINISHING_TEXT = "Finishing... ";

    private int period = 1;

    public RFFRenderPanel(RFF master) {
        this.master = master;
        setBackground(Color.BLACK);
        addListeners();
    }

    private void addListeners() {

        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                int r = e.getWheelRotation();

                if (r == 1) {
                    DoubleExponent[] offset = offsetConversion(
                            getMouseX(e),
                            getMouseY(e)
                    );
                    double mzo = 1.0 / Math.pow(10, -CalculationSettings.ZOOM_VALUE);
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
                    double mzi = 1.0 / Math.pow(10, CalculationSettings.ZOOM_VALUE);
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
                if(currentMap == null){
                    return;
                }
                long it = (long) currentMap.iterations().pipette(getMouseX(e), getMouseY(e));
                RFFStatusPanel panel = master.getWindow().getStatusPanel();
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




    private DoubleExponent getDivisor() {
        double logZoom = master.getSettings().calculationSettings().logZoom();
        return DoubleExponentMath.pow10(logZoom);
    }


    private DoubleExponent[] offsetConversion(double px, double py) {
        ImageSettings img = master.getSettings().imageSettings();
        double resolutionMultiplier = img.resolutionMultiplier();
        return new DoubleExponent[]{
                DoubleExponent.valueOf(px - getImgWidth() / 2.0).divide(getDivisor()).divide(resolutionMultiplier),
                DoubleExponent.valueOf(getImgHeight() / 2.0 - py).divide(getDivisor()).divide(resolutionMultiplier)
        };
    }



    public synchronized void recompute() {

        try {
            state.createThread(id -> {
                try{
                    compute(id);
                } catch (IllegalRenderStateException e) {
                    RFFLoggers.logCancelledMessage("Recompute", id);
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    
    //this method can be thread-safe only when called via recompute()
    public void compute(int currentID) throws IllegalRenderStateException{

        int w = getImgWidth();
        int h = getImgHeight();
        
        if (master.getSettings().calculationSettings().autoIteration()) {
            master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit().setMaxIteration(Math.max(INIT_ITERATION, period * 50L)).build()).build());
        }
        
        Settings settings = master.getSettings();
        CalculationSettings calc = settings.calculationSettings();
        
        DoubleMatrix iterations = new DoubleMatrix(w, h);
        RFFShaderProcessor.fillInit(iterations);

        int precision = Perturbator.precision(calc.logZoom());
        double logZoom = calc.logZoom();

       
        RFFStatusPanel panel = master.getWindow().getStatusPanel();
        panel.initTime();
        panel.setZoomText(logZoom);

        DoubleArrayDispatcher generator = new DoubleArrayDispatcher(state, currentID, iterations);
        DoubleExponent[] offset = offsetConversion(0, 0);
        DoubleExponent dcMax = DoubleExponentMath.hypot(offset[0], offset[1]);

        int refreshInterval = ActionsExplore.periodPanelRefreshInterval(master);
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
                int period = currentPerturbator.getReference().longestPeriod();
                MandelbrotLocator center = MandelbrotLocator.locateMinibrot(state, currentID, (MandelbrotPerturbator)currentPerturbator,
                    ActionsExplore.getActionWhileFindingMinibrotCenter(master, period), 
                    ActionsExplore.getActionWhileFindingMinibrotZoom(master)
                );
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


        generator.createRenderer((x, y, _, _, _, _, _, _, _) -> {
            DoubleExponent[] dc = offsetConversion(x, y);
            return currentPerturbator.iterate(dc[0], dc[1]);
        });

        period = currentPerturbator.getReference().longestPeriod();
        currentMap = new RFFMap(calc.logZoom(), period, calc.maxIteration(), iterations);
        
        panel.setPeriodText(period);
        panel.setProcess("Preparing...");

        generator.process(p -> {
            boolean processing = p < 1;

            if (RFFShaderProcessor.getCompressDivisor(settings.imageSettings()) > 1 || processing) {
                reloadAndPaint(currentID, true);
            }

            if (processing) {
                panel.setProcess("Calculating... " + TextFormatter.processText(p));
            } else {
                reloadAndPaint(currentID, false);
                panel.setProcess("Done");
            }
            panel.refreshTime();
        }, 500);
         

            
        


    }

    public Perturbator getCurrentPerturbator() {
        return currentPerturbator;
    }

    public BufferedImage getCurrentImage() {
        return currentImage;
    }

    public RFFMap getCurrentMap() {
        return currentMap;
    }
    
    public RenderState getState() {
        return state;
    }


    private ProcessVisualizer gvf(int fracA){
        RFFStatusPanel panel = master.getWindow().getStatusPanel();
        return a -> panel.setProcess(FINISHING_TEXT + TextFormatter.processText(a)
        + TextFormatter.frac(fracA, 3, TextFormatter.Parentheses.SQUARE));
    }

    /**
     * It is not thread-safe. Invoke via safe method.
     */
    public void reloadAndPaint(int currentID, boolean compressed) throws IllegalRenderStateException, InterruptedException {
        ProcessVisualizer[] pv = new ProcessVisualizer[]{
            gvf(1),
            gvf(2),
            gvf(3)
        };

        currentImage = RFFShaderProcessor.createImageWithVisualizer(state, currentID, currentMap, master.getSettings(), compressed, pv);
        
        repaint();

    }

    public void setCurrentMap(RFFMap currentMap) {
        this.currentMap = currentMap;
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        BitMapImage.highGraphics(g2);
        g2.drawImage(currentImage, 0, 0, getWidth(), getHeight(), null);
    }

}
