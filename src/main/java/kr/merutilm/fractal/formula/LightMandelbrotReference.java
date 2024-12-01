package kr.merutilm.fractal.formula;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntConsumer;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.parallel.RenderState;
import kr.merutilm.base.util.AdvancedMath;
import kr.merutilm.base.util.ArrayFunction;
import kr.merutilm.fractal.approx.LightBLATable;
import kr.merutilm.fractal.settings.BLASettings;
import kr.merutilm.fractal.struct.LWBigComplex;

public record LightMandelbrotReference(Formula formula, LWBigComplex refCenter, double[] refReal, double[] refImag, int period, LWBigComplex lastReference, LWBigComplex fpgBn) implements MandelbrotReference{


    public static LightMandelbrotReference generate(RenderState state, int renderID, LWBigComplex center, int precision, long maxIteration, double bailout, int initialPeriod, double dcMax, boolean strictFPGBn, IntConsumer actionPerRefCalcIteration) throws IllegalRenderStateException{
        state.tryBreak(renderID);
        Formula formula = new Mandelbrot();

        double[] rr = new double[initialPeriod == -1 ? 1 : initialPeriod];
        double[] ri = new double[initialPeriod == -1 ? 1 : initialPeriod];
        rr[0] = 0;
        ri[0] = 0;

        LWBigComplex z = LWBigComplex.zero(precision);
        
        LWBigComplex lastRef = z;
        LWBigComplex fpgBn = LWBigComplex.zero(precision);

        double fpgBnr = 0;
        double fpgBni = 0;


        int iteration = 0;
        double pzr;
        double pzi;
        double zr = 0;
        double zi = 0;
        int period = 1;

        while (zr * zr + zi * zi < bailout * bailout && iteration < maxIteration) {

            state.tryBreak(renderID);

            lastRef = z;
            pzr = zr;
            pzi = zi;
            z = formula.apply(z, center, precision);

            zr = z.re().doubleValue();
            zi = z.im().doubleValue();

            double prz2 = pzr * pzr + pzi * pzi;


            // use Fast-Period-Guessing
            double limit = limit(prz2, dcMax);
            double fpgBnrTemp = fpgBnr * pzr * 2 - fpgBni * pzi * 2 + 1;
            double fpgBniTemp = fpgBnr * pzi * 2 + fpgBni * pzr * 2;
            double r = AdvancedMath.hypotApproximate(fpgBnrTemp, fpgBniTemp);

            actionPerRefCalcIteration.accept(iteration);

            if ((iteration >= 1 && r > limit) || iteration == maxIteration - 1 || initialPeriod == iteration) {
                period = iteration;
                break;
            }

            if(strictFPGBn){
                fpgBn = fpgBn.multiply(lastRef, precision).doubled().add(LWBigComplex.valueOf(1,0, precision), precision);
            }
            fpgBnr = fpgBnrTemp;
            fpgBni = fpgBniTemp;


            iteration++;
            
            if(iteration == rr.length){
                rr = ArrayFunction.exp2xArr(rr);
                ri = ArrayFunction.exp2xArr(ri);
            }

            rr[iteration] = zr;
            ri[iteration] = zi;
        }
        if(!strictFPGBn){
            fpgBn = LWBigComplex.valueOf(fpgBnr, fpgBni, precision);
        }

        rr = Arrays.copyOfRange(rr, 0, period + 1);
        ri = Arrays.copyOfRange(ri, 0, period + 1);

        return new LightMandelbrotReference(formula, center, rr, ri, period, lastRef, fpgBn);
    }

    private static double limit(double prz2, double dcMax){
        return prz2 * 32 * FPG_MAX_EPSILON / dcMax;
    }
    public LightBLATable generateBLA(RenderState state, int renderID, BLASettings blaSettings, double dcMax) throws IllegalRenderStateException{
        return new LightBLATable(state, renderID, blaSettings, refReal, refImag, dcMax);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(Arrays.hashCode(refReal), Arrays.hashCode(refImag), period, lastReference, fpgBn);
    }
    @Override
    public final boolean equals(Object o) {
        return o instanceof LightMandelbrotReference r && 
        Arrays.equals(refReal, r.refReal) && 
        Arrays.equals(refImag, r.refImag) &&
        period == r.period &&
        Objects.equals(lastReference, r.lastReference) &&
        Objects.equals(fpgBn, r.fpgBn);
    }

    @Override
    public final String toString() {
        return getClass().toString() + "[ " + 
        STR_FORMULA + formula +
        STR_CENTER + refCenter +
        STR_REFERENCE_REAL + Arrays.toString(refReal) + 
        STR_REFERENCE_IMAG + Arrays.toString(refImag) +
        STR_PERIOD + period +
        STR_LAST_REF + lastReference +
        STR_FPG_BN + fpgBn + "\n]";
    }
    
 
    
}
