package kr.merutilm.fractal.formula;


import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.parallel.RenderState;
import kr.merutilm.fractal.settings.CalculationSettings;
import kr.merutilm.fractal.struct.DoubleExponent;

public abstract class MandelbrotPerturbator extends Perturbator {

    protected final boolean strictFPGBn;

    /**
     * Creates The Mandelbrot Scene.
     * @param state Render State of this scene
     * @param currentID It can be valid when currentID and stateID are same
     * @param calc The calculation settings of scene.
     * @param strictFPGBn Use arbitrary-precision operation to get more accurate and strict center when calculating FPGBn. 
     */
    protected MandelbrotPerturbator(RenderState state, int currentID, CalculationSettings calc, boolean strictFPGBn) {
        super(state, currentID, new Mandelbrot(), calc);
        this.strictFPGBn = strictFPGBn;
    }

    protected static final double FPG_EPSILON = Math.pow(10, -3);

    @Override
    public abstract MandelbrotPerturbator reuse(RenderState state, int currentID, CalculationSettings calc, DoubleExponent dcMax, int precision) throws IllegalRenderStateException;
    
    @Override
    public abstract MandelbrotReference getReference();
    
    public DoubleExponent getDcMaxByDoubleExponent() {
        if (this instanceof LightMandelbrotPerturbator s) {
            return DoubleExponent.valueOf(s.dcMax());
        } else if (this instanceof DeepMandelbrotPerturbator s) {
            return s.dcMax();
        } else
            throw new IllegalArgumentException();

    }
}
