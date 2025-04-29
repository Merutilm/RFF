package kr.merutilm.rff.ui;


import kr.merutilm.rff.formula.DeepMandelbrotPerturbator;
import kr.merutilm.rff.formula.LightMandelbrotPerturbator;
import kr.merutilm.rff.formula.MandelbrotPerturbator;
import kr.merutilm.rff.formula.Perturbator;
import kr.merutilm.rff.io.BitMap;
import kr.merutilm.rff.io.RFFMap;
import kr.merutilm.rff.locater.MandelbrotLocator;
import kr.merutilm.rff.opengl.*;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelDoubleArrayDispatcher;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.settings.RenderSettings;
import kr.merutilm.rff.settings.Settings;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.struct.DoubleMatrix;
import kr.merutilm.rff.util.DoubleExponentMath;
import kr.merutilm.rff.util.TextFormatter;
import org.lwjgl.BufferUtils;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.LongConsumer;

import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;

final class RFFRenderPanel extends RFFGLPanel {

    private final transient RFF master;
    private transient RFFMap currentMap;
    private final transient ParallelRenderState state = new ParallelRenderState();
    private transient GLMultiPassRenderer renderer;
    private transient GLRendererIterationPalette rendererIteration;
    private transient GLRendererStripe rendererStripe;
    private transient GLRendererSlope rendererSlope;
    private transient GLRendererColor rendererColorFilter;
    private transient GLRendererFog rendererFog;
    private transient GLRendererBloom rendererBloom;
    private transient GLRendererAntialiasing rendererAntialiasing;

    private static final int FPS = 30;

    private transient MandelbrotPerturbator currentPerturbator;

    private volatile boolean recomputeRequested = false;
    private volatile boolean resizeRequested = false;
    private volatile boolean colorRequested = false;
    private volatile boolean openMapRequested = false;
    private volatile boolean canBeDisplayed = false;
    private volatile boolean createImageRequested = false;

    private volatile boolean isRendering = false;
    private volatile boolean isImageCreating = false;

    private long period = 1;


    public RFFRenderPanel(RFF master) {
        super();
        this.master = master;
        addListeners();

    }


    public void renderLoop(){
        Runnable renderLoop = () -> new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                render();
            }
        }, 0, (int) (1000.0 / FPS));
        renderLoop.run();
    }


    @Override
    public void initGL() {
        createCapabilities();

        renderer = new GLMultiPassRenderer();

        rendererIteration = new GLRendererIterationPalette();
        rendererStripe = new GLRendererStripe();
        rendererSlope = new GLRendererSlope();
        rendererColorFilter = new GLRendererColor();
        rendererFog = new GLRendererFog();
        rendererBloom = new GLRendererBloom();
        rendererAntialiasing = new GLRendererAntialiasing();


        renderer.addRenderer(rendererIteration);
        renderer.addRenderer(rendererStripe);
        renderer.addRenderer(rendererSlope);
        renderer.addRenderer(rendererColorFilter);
        renderer.addRenderer(rendererFog);
        renderer.addRenderer(rendererBloom);
        renderer.addRenderer(rendererAntialiasing);
        requestResize();
        requestColor();
        requestRecompute();

    }

    @Override
    public void applyColor(Settings settings){
        rendererIteration.setPaletteSettings(settings.shaderSettings().paletteSettings());
        rendererStripe.setStripeSettings(settings.shaderSettings().stripeSettings());
        rendererSlope.setSlopeSettings(settings.shaderSettings().slopeSettings());
        rendererColorFilter.setColorFilterSettings(settings.shaderSettings().colorSettings());
        rendererFog.setFogSettings(settings.shaderSettings().fogSettings());
        rendererBloom.setBloomSettings(settings.shaderSettings().bloomSettings());
        rendererAntialiasing.setUse(settings.renderSettings().antialiasing());
    }


    @Override
    public void paintGL() {
        renderer.setTime(GLTimeRenderer.getTime());

        if(canBeDisplayed){
            glClear(GL_COLOR_BUFFER_BIT);
            renderer.update();
            swapBuffers();
        }
        
        if(openMapRequested){
            openMapRequested = false;
            applyCurrentMap();
        }
        
        if(resizeRequested){
            resizeRequested = false;
            applyResize();
        }
        
        if(colorRequested){
            colorRequested = false;
            applyColor(master.getSettings());
        }
        
        
        if(recomputeRequested){
            isRendering = true;
            recomputeRequested = false;
            applyComputationalSettings();
            recompute();
        }

        if(createImageRequested){
            isImageCreating = true;
            createImageRequested = false;
            applyCreateImage();
        }

    }


    private void applyComputationalSettings(){
        if (master.getSettings().calculationSettings().autoIteration()) {
            master.setSettings(e -> e.setCalculationSettings(e1 -> e1.setMaxIteration(Math.max(Perturbator.MINIMUM_ITERATION, period * Perturbator.AUTOMATIC_ITERATION_MULTIPLIER))));
        }
        Settings settings = master.getSettings();
        rendererIteration.reloadIterationBuffer(getImgWidth(settings), getImgHeight(settings), settings.calculationSettings().maxIteration());
    }

    @Override
    public int getFramebufferWidth() {
        return (int)Math.ceil(super.getFramebufferWidth() / 10.0) * 10;
    }

    @Override
    public int getFramebufferHeight() {
        return (int)Math.ceil(super.getFramebufferHeight() / 10.0) * 10;
    }

    private void applyResize(){
        renderer.reloadSize(getFramebufferWidth(), getFramebufferHeight());
    }



    private void applyCreateImage(){
        int w = getFramebufferWidth();
        int h = getFramebufferHeight();
        ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * 4);
        glReadPixels(0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        currentImage = BitMap.getImage(w, h, buffer);
        isImageCreating = false;
        synchronized (this){
            notifyAll();
        }
    }
    @Override
    public void applyCurrentMap(){
        DoubleMatrix iterations = currentMap.iterations();
        rendererIteration.reloadIterationBuffer(iterations.getWidth(), iterations.getHeight(), currentMap.maxIteration());
        rendererIteration.setAllIterations(iterations.getCanvas());
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
                    double m = master.getSettings().renderSettings().resolutionMultiplier();
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
        RenderSettings img = settings.renderSettings();
        return (int) (getFramebufferWidth() * img.resolutionMultiplier());
    }

    private int getImgHeight(Settings settings) {
        RenderSettings img = settings.renderSettings();
        return (int) (getFramebufferHeight() * img.resolutionMultiplier());
    }

    private int getMouseX(Settings settings, MouseEvent e) {
        RenderSettings img = settings.renderSettings();
        return RFFPanel.toRealLength((int) (e.getX() * img.resolutionMultiplier()));
    }

    private int getMouseY(Settings settings, MouseEvent e) {
        RenderSettings img = settings.renderSettings();
        return getImgHeight(settings) - RFFPanel.toRealLength((int) (e.getY() * img.resolutionMultiplier()));
    }


    private static DoubleExponent getDivisor(Settings settings) {
        double logZoom = settings.calculationSettings().logZoom();
        return DoubleExponentMath.pow10(logZoom);
    }

    private DoubleExponent[] offsetConversion(Settings settings, double px, double py) {
        RenderSettings img = settings.renderSettings();
        double resolutionMultiplier = img.resolutionMultiplier();
        return new DoubleExponent[]{
                DoubleExponent.valueOf(px - getImgWidth(settings) / 2.0).divide(getDivisor(settings)).divide(resolutionMultiplier),
                DoubleExponent.valueOf(py - getImgHeight(settings) / 2.0).divide(getDivisor(settings)).divide(resolutionMultiplier)
        };
    }

    private void recompute() {
        try {
            state.createThread(id -> {
                try {
                    compute(id);
                } catch (IllegalParallelRenderStateException e) {
                    RFFLoggers.logCancelledMessage("Recompute", id);
                } finally {
                    isRendering = false;
                    synchronized (this){
                        notifyAll();
                    }
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    public void requestOpenMap(RFFMap currentMap) {
        this.currentMap = currentMap;
        openMapRequested = true;
    }

    public void requestResize(){
        resizeRequested = true;
    }

    public void requestRecompute(){
        canBeDisplayed = false;
        recomputeRequested = true;
    }

    public void requestColor(){
        colorRequested = true;
    }

    public void requestCreateImage(){
        createImageRequested = true;
    }

    public synchronized void waitUntilComputeFinished() throws InterruptedException{
        while(recomputeRequested || isRendering){
            wait();
        }
    }

    public synchronized void waitUntilCreateImage() throws InterruptedException{
        while(createImageRequested || isImageCreating){
            wait();
        }
    }

    private void compute(int currentID) throws IllegalParallelRenderStateException {
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
        LongConsumer actionPerRefCalcIteration = p -> {
            if (p % refreshInterval == 0) {
                panel.setProcess("Period " + RFFStatusPanel.THOUSAND_FORMATTER.format(p));
            }
        };
        BiConsumer<Long, Double> actionPerCreatingTableIteration = (p, i) -> {
            if (p % refreshInterval == 0) {
                panel.setProcess("Creating Table... " + TextFormatter.processText(i));
            }
        };


        state.tryBreak(currentID);

        switch (calc.reuseReference()) {
            case CURRENT_REFERENCE ->
                currentPerturbator = currentPerturbator.reuse(state, currentID, calc, currentPerturbator.getDcMaxByDoubleExponent(), precision);

            case CENTERED_REFERENCE -> {
                long period = currentPerturbator.getReference().longestPeriod();
                MandelbrotLocator center = MandelbrotLocator.locateMinibrot(state, currentID, currentPerturbator,
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

        panel.setPeriodText(period, length - 1, currentPerturbator.getMPATable().length());
        panel.setProcess("Preparing...");

        state.tryBreak(currentID);

        generator.createRenderer((x, y, xr, _, _, _, _, _, _) -> {
            DoubleExponent[] dc = offsetConversion(settings, x, y);
            double iteration = currentPerturbator.iterate(dc[0], dc[1]);
            rendererIteration.setIteration(x, y, xr, iteration);

            if(x == xr - 1){
                canBeDisplayed = true;
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
