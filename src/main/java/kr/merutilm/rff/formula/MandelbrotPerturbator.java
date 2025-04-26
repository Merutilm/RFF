package kr.merutilm.rff.formula;

import kr.merutilm.rff.approx.MPATable;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.struct.DoubleExponent;

public abstract class MandelbrotPerturbator extends Perturbator {

    protected final boolean strictFPGBn;

    /**
     * Creates The Mandelbrot Scene.
     * @param state Render State of this scene
     * @param currentID It can be valid when currentID and stateID are same
     * @param calc The calculation settings of scene.
     * @param strictFPGBn Use arbitrary-precision operation to get more accurate and strict center when calculating FPGBn. 
     */
    protected MandelbrotPerturbator(ParallelRenderState state, int currentID, CalculationSettings calc, boolean strictFPGBn) {
        super(state, currentID, new Mandelbrot(), calc);
        this.strictFPGBn = strictFPGBn;
    }
    
    @Override
    public abstract MandelbrotPerturbator reuse(ParallelRenderState state, int currentID, CalculationSettings calc, DoubleExponent dcMax, int precision) throws IllegalParallelRenderStateException;
    
    public abstract MPATable getMPATable();

    @Override
    public abstract MandelbrotReference getReference();
    
    public DoubleExponent getDcMaxByDoubleExponent() {
        return switch (this) {
            case LightMandelbrotPerturbator s -> DoubleExponent.valueOf(s.dcMax());
            case DeepMandelbrotPerturbator s -> s.dcMax();
            default -> throw new IllegalArgumentException();
        };

    }
}
