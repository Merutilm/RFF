package kr.merutilm.fractal.formula;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.parallel.RenderState;
import kr.merutilm.fractal.settings.CalculationSettings;
import kr.merutilm.fractal.struct.DoubleExponent;
import kr.merutilm.fractal.struct.LWBigComplex;


public abstract class Perturbator {
    public static final double INTERIOR_DETECTION_ERROR = 1.0E-6;
    private static final int PRECISION_ADDITION = 18;
    private static final double LN2 = Math.log(2);
    protected final RenderState state;
    protected final Formula formula;
    protected final int currentID;
    protected final double bailout;
    protected int currentReferenceIteration;
    protected final long maxIteration;
    protected final LWBigComplex center;
    protected final double logZoom;
    protected final CalculationSettings calc;

    protected Perturbator(RenderState state, int currentID, Formula formula, CalculationSettings calc) {
        this.state = state;
        this.currentID = currentID;
        this.formula = formula;
        this.calc = calc;
        this.bailout = calc.bailout();
        this.center = calc.center();
        this.logZoom = calc.logZoom();
        this.maxIteration = calc.maxIteration();

    }

    public static int precision(double logZoom) {
        return -(int) logZoom - PRECISION_ADDITION;
    }

    public double getLogZoom() {
        return logZoom;
    }

    public CalculationSettings getCalc() {
        return calc;
    }

    public LWBigComplex getCenter() {
        return center;
    }

    public double getDoubleValueIteration(long iteration, double prevIterDistance, double currIterDistance) {
        // prevIterDistance = p
        // currIterDistance = c
        // bailout = b
        //
        // a = b - p (p < b)
        // b = c - b (c > b)
        // 0 dec 1 decimal value
        // a : b ratio
        // ratio = a / (a + b) = (b - p) / (c - p)

        if (prevIterDistance == currIterDistance) {
            return iteration;
        }
        double ratio = (bailout - prevIterDistance) / (currIterDistance - prevIterDistance);
        
        switch (calc.decimalIterationSettings()) {
            case NONE -> {
                ratio = 0;
            }
            case LINEAR -> {
                //noop
            }
            case SQUARE_ROOT -> {
                ratio = Math.sqrt(ratio);
            }
            case LOG -> {
                ratio = Math.log(ratio + 1) / LN2;
            }
            case LOG_LOG -> {
                ratio = Math.log(Math.log(ratio + 1) / LN2 + 1) / LN2;
            }
            default -> throw new IllegalArgumentException();
        }
        
        return iteration + ratio;
    }

    public synchronized int getCurrentReferenceIteration() {
        return currentReferenceIteration;
    }

    public RenderState getState() {
        return state;
    }

    public int getCurrentID() {
        return currentID;
    }

    public abstract DoubleExponent getDcMaxByDoubleExponent();

    public abstract Reference getReference();

    public abstract Perturbator reuse(RenderState state, int currentID, CalculationSettings calc, DoubleExponent dcMax, int precision) throws IllegalRenderStateException;

    public abstract double iterate(DoubleExponent dcr, DoubleExponent dci);


}
