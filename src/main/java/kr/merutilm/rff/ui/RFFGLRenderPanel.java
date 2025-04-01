package kr.merutilm.rff.ui;


import kr.merutilm.rff.formula.DeepMandelbrotPerturbator;
import kr.merutilm.rff.formula.LightMandelbrotPerturbator;
import kr.merutilm.rff.formula.MandelbrotPerturbator;
import kr.merutilm.rff.formula.Perturbator;
import kr.merutilm.rff.io.RFFMap;
import kr.merutilm.rff.locater.MandelbrotLocator;
import kr.merutilm.rff.opengl.*;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelDoubleArrayDispatcher;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.settings.ImageSettings;
import kr.merutilm.rff.settings.Settings;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.struct.DoubleMatrix;
import kr.merutilm.rff.util.DoubleExponentMath;
import kr.merutilm.rff.util.TextFormatter;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL11.*;

final class RFFGLRenderPanel extends AWTGLCanvas {

    private final transient ParallelRenderState state = new ParallelRenderState();

    private final transient RFF master;
    private static final int FPS = 30;

    private transient GLMultiPassRenderer renderer;
    private transient BufferedImage currentImage;
    private transient GLRendererIteration rendererIteration;
    private transient GLRendererStripe rendererStripe;
    private transient GLRendererSlope rendererSlope;
    private transient GLRendererColorFilter rendererColorFilter;
    private transient GLRendererFog rendererFog;
    private transient GLRendererBloom rendererBloom;

    private transient RFFMap currentMap;
    private transient Perturbator currentPerturbator;
    private boolean recomputeRequested = false;
    private boolean resizeRequested = false;
    private boolean colorRequested = false;

    private int period = 1;
    private boolean isRenderPreparing = false;


    public RFFGLRenderPanel(RFF master, GLData data) {
        super(data);
        this.master = master;
        setBackground(Color.BLACK);
        addListeners();

    }


    public void renderLoop(){
        Runnable renderLoop = new Runnable() {
            @Override
            public void run() {
                if (!isValid()) {
                    SwingUtilities.invokeLater(this);
                    return;
                }

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(RFFGLRenderPanel.this::render);
                    }
                }, 0, (int) (1000.0 / FPS));

            }
        };
        SwingUtilities.invokeLater(renderLoop);
    }


    @Override
    public void initGL() {

        createCapabilities();

        renderer = new GLMultiPassRenderer();

        rendererIteration = new GLRendererIteration();
        rendererStripe = new GLRendererStripe();
        rendererSlope = new GLRendererSlope();
        rendererColorFilter = new GLRendererColorFilter();
        rendererFog = new GLRendererFog();
        rendererBloom = new GLRendererBloom();
        GLRendererInterpolation interpolation = new GLRendererInterpolation();


        renderer.addRenderer(rendererIteration);
        renderer.addRenderer(rendererStripe);
        renderer.addRenderer(rendererSlope);
        renderer.addRenderer(rendererColorFilter);
        renderer.addRenderer(rendererFog);
        renderer.addRenderer(rendererBloom);
        renderer.addRenderer(interpolation);

        requestRecompute();
        requestResize();

    }


    @Override
    public void paintGL() {


        if(!isRenderPreparing){
            glClear(GL_COLOR_BUFFER_BIT);
            renderer.update();
        }

        if(resizeRequested){
            resizeRequested = false;
            renderer.reloadSize(getFramebufferWidth(), getFramebufferHeight());
        }

        if(colorRequested){
            colorRequested = false;
            applySettings();
        }

        if(recomputeRequested){
            recomputeRequested = false;
            applySettings();
            recompute();
        }

        swapBuffers();

    }

    private void applySettings(){
        if (master.getSettings().calculationSettings().autoIteration()) {
            master.setSettings(e -> e.setCalculationSettings(e1 -> e1.setMaxIteration(Math.max(Perturbator.MINIMUM_ITERATION, period * Perturbator.AUTOMATIC_ITERATION_MULTIPLIER))));
        }
        Settings settings = master.getSettings();
        rendererIteration.setColorSettings(settings.shaderSettings().colorSettings());
        rendererStripe.setStripeSettings(settings.shaderSettings().stripeSettings());
        rendererStripe.setAnimationSettings(settings.videoSettings().animationSettings());
        rendererSlope.setSlopeSettings(settings.shaderSettings().slopeSettings());
        rendererColorFilter.setColorFilterSettings(settings.shaderSettings().colorFilterSettings());
        rendererFog.setFogSettings(settings.shaderSettings().fogSettings());
        rendererBloom.setBloomSettings(settings.shaderSettings().bloomSettings());
    }

    private void addListeners() {

        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {

                super.mouseWheelMoved(e);
                int r = e.getWheelRotation();
                Settings settings = master.getSettings();

                if (r == 1) {
                    DoubleExponent[] offset = offsetConversion(settings,
                            getMouseX(settings, e),
                            getMouseY(settings, e)
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
                    DoubleExponent[] offset = offsetConversion(settings,
                            getMouseX(settings, e),
                            getMouseY(settings, e)
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

                requestRecompute();
            }
        });

        AtomicInteger pmx = new AtomicInteger();
        AtomicInteger pmy = new AtomicInteger();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                Settings settings = master.getSettings();
                if (SwingUtilities.isLeftMouseButton(e)) {
                    pmx.set(getMouseX(settings, e));
                    pmy.set(getMouseY(settings, e));
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
                Settings settings = master.getSettings();
                long it = (long) currentMap.iterations().pipette(getMouseX(settings, e), getMouseY(settings, e));
                RFFStatusPanel panel = master.getWindow().getStatusPanel();
                panel.setIterationText(it);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                Settings settings = master.getSettings();
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int dx = pmx.get() - getMouseX(settings, e);
                    int dy = pmy.get() - getMouseY(settings, e);
                    double m = master.getSettings().imageSettings().resolutionMultiplier();
                    double logZoom = master.getSettings().calculationSettings().logZoom();
                    master.setSettings(e1 -> e1.setCalculationSettings(
                                    e2 -> e2.addCenter(
                                            DoubleExponent.valueOf(dx / m).divide(getDivisor(settings)),
                                            DoubleExponent.valueOf(dy / m).divide(getDivisor(settings)), Perturbator.precision(logZoom))

                            )
                    );

                    pmx.set(getMouseX(settings, e));
                    pmy.set(getMouseY(settings, e));

                    requestRecompute();
                }
            }
        });

    }

    private int getImgWidth(Settings settings) {
        ImageSettings img = settings.imageSettings();
        return (int) (getFramebufferWidth() * img.resolutionMultiplier());
    }

    private int getImgHeight(Settings settings) {
        ImageSettings img = settings.imageSettings();
        return (int) (getFramebufferHeight() * img.resolutionMultiplier());
    }

    private int getMouseX(Settings settings, MouseEvent e) {
        ImageSettings img = settings.imageSettings();
        return RFFPanel.toRealLength((int) (e.getX() * img.resolutionMultiplier()));
    }

    private int getMouseY(Settings settings, MouseEvent e) {
        ImageSettings img = settings.imageSettings();
        return getImgHeight(settings) - RFFPanel.toRealLength((int) (e.getY() * img.resolutionMultiplier()));
    }


    private static DoubleExponent getDivisor(Settings settings) {
        double logZoom = settings.calculationSettings().logZoom();
        return DoubleExponentMath.pow10(logZoom);
    }

    private DoubleExponent[] offsetConversion(Settings settings, double px, double py) {
        ImageSettings img = settings.imageSettings();
        double resolutionMultiplier = img.resolutionMultiplier();
        return new DoubleExponent[]{
                DoubleExponent.valueOf(px - getImgWidth(settings) / 2.0).divide(getDivisor(settings)).divide(resolutionMultiplier),
                DoubleExponent.valueOf(py - getImgHeight(settings) / 2.0).divide(getDivisor(settings)).divide(resolutionMultiplier)
        };
    }

    private void recompute() {
        Settings settings = master.getSettings();
        rendererIteration.reloadIterationBuffer(getImgWidth(settings), getImgHeight(settings), settings.calculationSettings().maxIteration());
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

    public void requestResize(){
        resizeRequested = true;
    }

    public void requestRecompute(){
        isRenderPreparing = true;
        recomputeRequested = true;
    }

    public void requestColor(){
        colorRequested = true;
    }


    //this method can only be thread-safe when called via recompute()
    public void compute(int currentID) throws IllegalParallelRenderStateException {
        compute0(master.getSettings(), currentID);
    }

    private void compute0(Settings settings, int currentID) throws IllegalParallelRenderStateException {

        int w = getImgWidth(settings);
        int h = getImgHeight(settings);

        state.tryBreak(currentID);

        CalculationSettings calc = settings.calculationSettings();
        DoubleMatrix iterations = new DoubleMatrix(w, h);
        int precision = Perturbator.precision(calc.logZoom());
        double logZoom = calc.logZoom();

        state.tryBreak(currentID);

        RFFStatusPanel panel = master.getWindow().getStatusPanel();
        panel.initTime();
        panel.setZoomText(logZoom);

        ParallelDoubleArrayDispatcher generator = new ParallelDoubleArrayDispatcher(state, currentID, iterations);
        DoubleExponent[] offset = offsetConversion(settings, 0, 0);
        DoubleExponent dcMax = DoubleExponentMath.hypot(offset[0], offset[1]);

        int refreshInterval = ActionsExplore.periodPanelRefreshInterval(settings.calculationSettings().logZoom());
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


        state.tryBreak(currentID);

        switch (calc.reuseReference()) {
            case CURRENT_REFERENCE -> {
                currentPerturbator = currentPerturbator.reuse(state, currentID, calc, currentPerturbator.getDcMaxByDoubleExponent(), precision);
            }
            case CENTERED_REFERENCE -> {
                int period = currentPerturbator.getReference().longestPeriod();
                MandelbrotLocator center = MandelbrotLocator.locateMinibrot(state, currentID, (MandelbrotPerturbator) currentPerturbator,
                        ActionsExplore.getActionWhileFindingMinibrotCenter(panel, logZoom, period),
                        ActionsExplore.getActionWhileCreatingTable(panel, logZoom),
                        ActionsExplore.getActionWhileFindingMinibrotZoom(panel)
                );
                currentPerturbator = null; //try to call gc

                CalculationSettings refCalc = calc.edit().setCenter(center.center()).setLogZoom(center.logZoom()).build();
                int refPrecision = Perturbator.precision(center.logZoom());

                if (refCalc.logZoom() > DoubleExponent.EXP_DEADLINE) {
                    currentPerturbator = new DeepMandelbrotPerturbator(state, currentID, refCalc, center.dcMax(), refPrecision, period, actionPerRefCalcIteration, actionPerCreatingTableIteration)
                            .reuse(state, currentID, calc, dcMax, precision);
                } else {
                    currentPerturbator = new LightMandelbrotPerturbator(state, currentID, refCalc, center.dcMax().doubleValue(), refPrecision, period, actionPerRefCalcIteration, actionPerCreatingTableIteration)
                            .reuse(state, currentID, calc, dcMax, precision);
                }

            }
            case DISABLED -> {
                currentPerturbator = null; //try to call gc
                if (logZoom > DoubleExponent.EXP_DEADLINE) {
                    currentPerturbator = new DeepMandelbrotPerturbator(state, currentID, calc, dcMax, precision, -1, actionPerRefCalcIteration, actionPerCreatingTableIteration);
                } else {
                    currentPerturbator = new LightMandelbrotPerturbator(state, currentID, calc, dcMax.doubleValue(), precision, -1, actionPerRefCalcIteration, actionPerCreatingTableIteration);
                }
            }
        }

        period = currentPerturbator.getReference().longestPeriod();
        int length = currentPerturbator.getReference().length();
        currentMap = new RFFMap(calc.logZoom(), period, calc.maxIteration(), iterations);

        panel.setPeriodText(period, length - 1, currentPerturbator.getR3ATable().length());
        panel.setProcess("Preparing...");

        state.tryBreak(currentID);

        generator.createRenderer((x, y, xr, _, _, _, _, _, _) -> {
            DoubleExponent[] dc = offsetConversion(settings, x, y);
            double iteration = currentPerturbator.iterate(dc[0], dc[1]);
            rendererIteration.setIteration(x, y, xr, iteration);

            if(x == 0 && y == 1){
                isRenderPreparing = false;
            }
            return iteration;
        });

        process(panel, generator);

    }



    /**
     * Processes the renderer. <p>
     * The method invoked by compute0() ensures thread-safe, so it is also thread-safe.
     *
     * @param generator the iterator
     * @throws IllegalParallelRenderStateException If the render state changed
     */
    private void process(RFFStatusPanel panel, ParallelDoubleArrayDispatcher generator) throws IllegalParallelRenderStateException {


        generator.process(p -> {
            boolean processing = p < 1;
            if (processing) {
                panel.setProcess("Calculating... " + TextFormatter.processText(p));
            } else {
                panel.setProcess("Done");
            }
            panel.refreshTime();
        }, 100);
    }

    public ParallelRenderState getState() {
        return state;
    }

    public void setCurrentMap(RFFMap currentMap) {
        this.currentMap = currentMap;
    }

    public Perturbator getCurrentPerturbator() {
        return currentPerturbator;
    }

    public RFFMap getCurrentMap() {
        return currentMap;
    }

    public BufferedImage getCurrentImage() {
        return currentImage;
    }
}
