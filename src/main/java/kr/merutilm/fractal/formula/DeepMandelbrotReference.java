package kr.merutilm.fractal.formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.parallel.RenderState;
import kr.merutilm.fractal.approx.DeepBLATable;
import kr.merutilm.fractal.settings.R3ASettings;
import kr.merutilm.fractal.struct.DoubleExponent;
import kr.merutilm.fractal.struct.LWBigComplex;
import kr.merutilm.fractal.util.DoubleExponentMath;

public record DeepMandelbrotReference(Formula formula, LWBigComplex refCenter, DoubleExponent[] refReal, DoubleExponent[] refImag, int period, LWBigComplex lastReference, LWBigComplex fpgBn) implements MandelbrotReference{


    public static DeepMandelbrotReference generate(RenderState state, int renderID, LWBigComplex center, int precision, long maxIteration, double bailout, int initialPeriod, DoubleExponent dcMax, boolean strictFPGBn, IntConsumer actionPerIteration) throws IllegalRenderStateException{
        state.tryBreak(renderID);
        Formula formula = new Mandelbrot();

        List<DoubleExponent> rr = new ArrayList<>(initialPeriod == -1 ? 1 : initialPeriod);
        List<DoubleExponent> ri = new ArrayList<>(initialPeriod == -1 ? 1 : initialPeriod);
        
        rr.add(DoubleExponent.ZERO);
        ri.add(DoubleExponent.ZERO);
        LWBigComplex z = LWBigComplex.zero(precision);

        LWBigComplex fpgBn = LWBigComplex.zero(precision);
        LWBigComplex lastRef = z;

        DoubleExponent fpgBnr = DoubleExponent.ZERO;
        DoubleExponent fpgBni = DoubleExponent.ZERO;

        int iteration = 0;
        DoubleExponent pzr;
        DoubleExponent pzi;
        DoubleExponent zr = DoubleExponent.ZERO;
        DoubleExponent zi = DoubleExponent.ZERO;
        int period = 1;

        while (DoubleExponentMath.hypot2(zr, zi).isSmallerThan(bailout * bailout) && iteration < maxIteration) {
            
            state.tryBreak(renderID);

            lastRef = z;
            pzr = zr;
            pzi = zi;
            z = formula.apply(z, center, precision);

            zr = DoubleExponent.valueOf(z.re());
            zi = DoubleExponent.valueOf(z.im());
            
            DoubleExponent prz2 = DoubleExponentMath.hypot2(pzr, pzi);
            
            //use Fast-Period-Guessing 
            DoubleExponent limit = prz2.divide(dcMax);
            DoubleExponent fpgBnrTemp = fpgBnr.multiply(pzr.doubled()).subtract(fpgBni.multiply(pzi.doubled())).add(DoubleExponent.ONE);
            DoubleExponent fpgBniTemp = fpgBnr.multiply(pzi.doubled()).add(fpgBni.multiply(pzr.doubled()));
            DoubleExponent r = DoubleExponentMath.hypotApproximate(fpgBnrTemp, fpgBniTemp);
            
            actionPerIteration.accept(iteration);

            if ((iteration >= 1 && r.isLargerThan(limit)) || iteration == maxIteration - 1 || initialPeriod == iteration) {
                period = iteration;
                break;
            }

            if(strictFPGBn){
                fpgBn = fpgBn.multiply(lastRef, precision).doubled().add(LWBigComplex.valueOf(1,0, precision), precision);
            }
            fpgBnr = fpgBnrTemp;
            fpgBni = fpgBniTemp;

            iteration++;
            rr.add(zr);
            ri.add(zi);
        }

        if(!strictFPGBn){
            fpgBn = LWBigComplex.valueOf(fpgBnr, fpgBni, precision);
        }

        DoubleExponent[] rr1 = rr.toArray(DoubleExponent[]::new);
        DoubleExponent[] ri1 = ri.toArray(DoubleExponent[]::new);

        return new DeepMandelbrotReference(formula, center, rr1, ri1, period, lastRef, fpgBn);
    }


    public DeepBLATable generateBLA(RenderState state, int renderID, R3ASettings blaSettings, DoubleExponent dcMax) throws IllegalRenderStateException{
        return new DeepBLATable(state, renderID, blaSettings, refReal, refImag, period, dcMax);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(Arrays.hashCode(refReal), Arrays.hashCode(refImag), period, lastReference, fpgBn);
    }
    @Override
    public final boolean equals(Object o) {
        return o instanceof DeepMandelbrotReference r && 
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