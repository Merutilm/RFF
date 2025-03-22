package kr.merutilm.rff.ui;

import javax.swing.*;

import kr.merutilm.rff.io.BitMapImage;
import kr.merutilm.rff.struct.DoubleMatrix;
import kr.merutilm.rff.formula.DeepMandelbrotPerturbator;
import kr.merutilm.rff.formula.LightMandelbrotPerturbator;
import kr.merutilm.rff.formula.MandelbrotPerturbator;
import kr.merutilm.rff.formula.Perturbator;
import kr.merutilm.rff.io.RFFMap;
import kr.merutilm.rff.locater.MandelbrotLocator;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelDoubleArrayDispatcher;
import kr.merutilm.rff.parallel.ParallelRenderProcessVisualizer;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.settings.ImageSettings;
import kr.merutilm.rff.settings.Settings;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.util.DoubleExponentMath;
import kr.merutilm.rff.util.TextFormatter;


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

final class RFFRenderPanel extends JPanel {
    private final transient ParallelRenderState state = new ParallelRenderState();

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
                    double logZoom = master.getSettings().calculationSettings().logZoom();
                    master.setSettings(e1 -> e1.setCalculationSettings(
                                    e2 -> e2.addCenter(
                                            offset[0].multiply(1 - mzo), offset[1].multiply(1 - mzo), Perturbator.precision(logZoom)
                                    ).zoomOut()
                            )
                    );
                }
                if (r == -1) {
                    DoubleExponent[] offset = offsetConversion(
                            getMouseX(e),
                            getMouseY(e)
                    );
                    double mzi = 1.0 / Math.pow(10, CalculationSettings.ZOOM_VALUE);
                    double logZoom = master.getSettings().calculationSettings().logZoom();
                    master.setSettings(e1 -> e1.setCalculationSettings(
                                    e2 -> e2.addCenter(
                                            offset[0].multiply(1 - mzi), offset[1].multiply(1 - mzi), Perturbator.precision(logZoom)
                                    ).zoomIn()
                            )
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
                if (currentMap == null) {
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
                    double logZoom = master.getSettings().calculationSettings().logZoom();
                    master.setSettings(e1 -> e1.setCalculationSettings(
                                    e2 -> e2.addCenter(
                                                    DoubleExponent.valueOf(dx / m).divide(getDivisor()),
                                                    DoubleExponent.valueOf(-dy / m).divide(getDivisor()), Perturbator.precision(logZoom))
                                            
                            )
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
                try {
                    compute(id);
                } catch (IllegalParallelRenderStateException e) {
                    RFFLoggers.logCancelledMessage("Recompute", id);
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }


    //this method can only be thread-safe when called via recompute()
    public void compute(int currentID) throws IllegalParallelRenderStateException {

        int w = getImgWidth();
        int h = getImgHeight();

        if (master.getSettings().calculationSettings().autoIteration()) {
            master.setSettings(e -> e.setCalculationSettings(e1 -> e1.setMaxIteration(Math.max(master.getSettings().calculationSettings().maxIteration(), period * 50L))));
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

        ParallelDoubleArrayDispatcher generator = new ParallelDoubleArrayDispatcher(state, currentID, iterations);
        DoubleExponent[] offset = offsetConversion(0, 0);
        DoubleExponent dcMax = DoubleExponentMath.hypot(offset[0], offset[1]);

        int refreshInterval = ActionsExplore.periodPanelRefreshInterval(master);
        IntConsumer actionPerRefCalcIteration = p -> {
            if (p % refreshInterval == 0) {
                panel.setProcess("Period " + p);
            }
        };
        BiConsumer<Integer, Double> actionPerCreatingTableIteration = (p, i) -> {
            if (p % refreshInterval == 0) {
                panel.setProcess("Creating Table... " + TextFormatter.processText(i));
            }
        };


        switch (calc.reuseReference()) {
            case CURRENT_REFERENCE -> {
                currentPerturbator = currentPerturbator.reuse(state, currentID, calc, currentPerturbator.getDcMaxByDoubleExponent(), precision);
            }
            case CENTERED_REFERENCE -> {
                int period = currentPerturbator.getReference().longestPeriod();
                MandelbrotLocator center = MandelbrotLocator.locateMinibrot(state, currentID, (MandelbrotPerturbator) currentPerturbator,
                        ActionsExplore.getActionWhileFindingMinibrotCenter(master, period),
                        ActionsExplore.getActionWhileCreatingTable(master),
                        ActionsExplore.getActionWhileFindingMinibrotZoom(master)
                );
                currentPerturbator = null; //try to call gc

                CalculationSettings refCalc = calc.edit().setCenter(center.center()).setLogZoom(center.logZoom()).build();
                int refPrecision = Perturbator.precision(center.logZoom());

                if (refCalc.logZoom() > EXP_DEADLINE) {
                    currentPerturbator = new DeepMandelbrotPerturbator(state, currentID, refCalc, center.dcMax(), refPrecision, period, actionPerRefCalcIteration, actionPerCreatingTableIteration)
                            .reuse(state, currentID, calc, dcMax, precision);
                } else {
                    currentPerturbator = new LightMandelbrotPerturbator(state, currentID, refCalc, center.dcMax().doubleValue(), refPrecision, period, actionPerRefCalcIteration, actionPerCreatingTableIteration)
                            .reuse(state, currentID, calc, dcMax, precision);
                }

            }
            case DISABLED -> {
                currentPerturbator = null; //try to call gc
                if (logZoom > EXP_DEADLINE) {
                    currentPerturbator = new DeepMandelbrotPerturbator(state, currentID, calc, dcMax, precision, -1, actionPerRefCalcIteration, actionPerCreatingTableIteration);
                } else {
                    currentPerturbator = new LightMandelbrotPerturbator(state, currentID, calc, dcMax.doubleValue(), precision, -1, actionPerRefCalcIteration, actionPerCreatingTableIteration);
                }
            }
        }


        generator.createRenderer((x, y, _, _, _, _, _, _, _) -> {
            DoubleExponent[] dc = offsetConversion(x, y);
            //time mode
            //long t = System.nanoTime();
            //double iteration = currentPerturbator.iterate(dc[0], dc[1]);
            //return (System.nanoTime() - t) / 10.0 + iteration / 1.0E20;
            return currentPerturbator.iterate(dc[0], dc[1]);
        });

        period = currentPerturbator.getReference().longestPeriod();
        int length = currentPerturbator.getReference().length();
        currentMap = new RFFMap(calc.logZoom(), period, calc.maxIteration(), iterations);

        panel.setPeriodText(period, length - 1, currentPerturbator.getR3ATable().length());
        panel.setProcess("Preparing...");

        generator.process(p -> {
            boolean processing = p < 1;

            if (RFFShaderProcessor.getImageCompressDivisor(settings.imageSettings()) > 1 || processing) {
                refreshColorUnsafe(currentID, true);
            }

            if (processing) {
                panel.setProcess("Calculating... " + TextFormatter.processText(p));
            } else {
                refreshColorUnsafe(currentID, false);
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

    public ParallelRenderState getState() {
        return state;
    }


    private ParallelRenderProcessVisualizer gvf(int fracA) {
        RFFStatusPanel panel = master.getWindow().getStatusPanel();
        return a -> panel.setProcess(FINISHING_TEXT + TextFormatter.processText(a)
                                     + TextFormatter.frac(fracA, 2, TextFormatter.Parentheses.SQUARE));
    }

    public void refreshColor(){
        try {
            state.createThread(id -> {
                try {
                    RFFStatusPanel panel = master.getWindow().getStatusPanel();
                    refreshColorUnsafe(id, false);
                    panel.setProcess("Done");
                } catch (IllegalParallelRenderStateException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void refreshColorUnsafe(){
        try {
            RFFStatusPanel panel = master.getWindow().getStatusPanel();
            refreshColorUnsafe(state.currentID(), false);
            panel.setProcess("Done");
        }catch(IllegalParallelRenderStateException | InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }
    /**
     * It is not thread-safe. Invoke via safe method.
     */
    private void refreshColorUnsafe(int currentID, boolean compressed) throws IllegalParallelRenderStateException, InterruptedException {
        ParallelRenderProcessVisualizer[] pv = new ParallelRenderProcessVisualizer[]{
                gvf(1),
                gvf(2)
        };

        currentImage = RFFShaderProcessor.createImage(state, currentID, currentMap, master.getSettings(), compressed, pv);

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
