package kr.merutilm.rff.formula;

import kr.merutilm.rff.approx.R3ATable;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.precision.LWBigComplex;


public abstract class Perturbator {
    private static final int PRECISION_ADDITION = 18;
    public static final long MINIMUM_ITERATION = 300;
    public static final long AUTOMATIC_ITERATION_MULTIPLIER = 50;
    private static final double LN2 = Math.log(2);
    protected final ParallelRenderState state;
    protected final Formula formula;
    protected final int currentID;
    protected final double bailout;
    protected final long maxIteration;
    protected final LWBigComplex center;
    protected final double logZoom;
    protected final CalculationSettings calc;

    protected Perturbator(ParallelRenderState state, int currentID, Formula formula, CalculationSettings calc) {
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
        
        switch (calc.decimalizeIterationMethod()) {
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


    public abstract DoubleExponent getDcMaxByDoubleExponent();

    public abstract Reference getReference();
    
    public abstract R3ATable<?> getR3ATable();

    public abstract Perturbator reuse(ParallelRenderState state, int currentID, CalculationSettings calc, DoubleExponent dcMax, int precision) throws IllegalParallelRenderStateException;

    public abstract double iterate(DoubleExponent dcr, DoubleExponent dci) throws IllegalParallelRenderStateException;


}
