package kr.merutilm.rff.formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

import kr.merutilm.rff.shader.IllegalRenderStateException;
import kr.merutilm.rff.shader.RenderState;
import kr.merutilm.rff.approx.DeepR3ATable;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.struct.LWBigComplex;
import kr.merutilm.rff.util.ArrayFunction;
import kr.merutilm.rff.util.DoubleExponentMath;

public record DeepMandelbrotReference(Formula formula, LWBigComplex refCenter, DoubleExponent[] refReal,
                                      DoubleExponent[] refImag, int[] period, LWBigComplex lastReference,
                                      LWBigComplex fpgBn) implements MandelbrotReference {


    public static DeepMandelbrotReference generate(RenderState state, int renderID, LWBigComplex center, int precision, long maxIteration, double bailout, int initialPeriod, DoubleExponent dcMax, boolean strictFPGBn, IntConsumer actionPerRefCalcIteration) throws IllegalRenderStateException {
        state.tryBreak(renderID);
        Formula formula = new Mandelbrot();

        List<DoubleExponent> rr = new ArrayList<>(initialPeriod == -1 ? 1 : initialPeriod);
        List<DoubleExponent> ri = new ArrayList<>(initialPeriod == -1 ? 1 : initialPeriod);
        rr.add(DoubleExponent.ZERO);
        ri.add(DoubleExponent.ZERO);


        LWBigComplex z = LWBigComplex.zero(precision);
        LWBigComplex lastRef = z;
        LWBigComplex fpgBn = LWBigComplex.zero(precision);

        DoubleExponent fpgBnr = DoubleExponent.ZERO;
        DoubleExponent fpgBni = DoubleExponent.ZERO;

        int iteration = 0;
        DoubleExponent pzr;
        DoubleExponent pzi;
        DoubleExponent zr = DoubleExponent.ZERO;
        DoubleExponent zi = DoubleExponent.ZERO;
        int period = 1;

        int[] periodArray = new int[1];
        int periodArrayLength = 0;
        DoubleExponent minZRadius = DoubleExponent.POSITIVE_INFINITY;

        while (DoubleExponentMath.hypot2(zr, zi).isSmallerThan(bailout * bailout) && iteration < maxIteration) {

            state.tryBreak(renderID);

            lastRef = z;
            pzr = zr;
            pzi = zi;
            z = formula.apply(z, center, precision);

            zr = DoubleExponent.valueOf(z.re());
            zi = DoubleExponent.valueOf(z.im());

            DoubleExponent prevZRadius2 = DoubleExponentMath.hypot2(pzr, pzi);

            // use Fast-Period-Guessing, and create RRA Table
            DoubleExponent fpgLimit = prevZRadius2.divide(dcMax);

            DoubleExponent fpgBnrTemp = fpgBnr.multiply(pzr.doubled()).subtract(fpgBni.multiply(pzi.doubled())).add(DoubleExponent.ONE);
            DoubleExponent fpgBniTemp = fpgBnr.multiply(pzi.doubled()).add(fpgBni.multiply(pzr.doubled()));
            DoubleExponent fpgRadius = DoubleExponentMath.hypotApproximate(fpgBnrTemp, fpgBniTemp);

            actionPerRefCalcIteration.accept(iteration);

            if (minZRadius.isLargerThan(prevZRadius2) && prevZRadius2.isLargerThan(DoubleExponent.ZERO)) {
                minZRadius = prevZRadius2;
                if (periodArrayLength == periodArray.length) {
                    periodArray = ArrayFunction.exp2xArr(periodArray);
                }

                periodArray[periodArrayLength] = iteration;
                periodArrayLength++;
            }

            if ((iteration >= 1 && fpgRadius.isLargerThan(fpgLimit)) || iteration == maxIteration - 1 || initialPeriod == iteration) {
                period = iteration;

                if (periodArrayLength == periodArray.length) {
                    periodArray = ArrayFunction.exp2xArr(periodArray);
                }
                periodArray[periodArrayLength] = iteration;
                periodArrayLength++;
                break;
            }


            if (strictFPGBn) {
                fpgBn = fpgBn.multiply(lastRef, precision).doubled().add(LWBigComplex.valueOf(1, 0, precision), precision);
            }

            fpgBnr = fpgBnrTemp;
            fpgBni = fpgBniTemp;


            iteration++;
            rr.add(zr);
            ri.add(zi);
        }

//        boolean useSwirlGuessing = false; // TODO : create parameters
//        if(useSwirlGuessing){
//            int swirlPeriod = 1;
//            double swirlDzrTemp = 0;
//            double swirlDziTemp = 0;
//            double swirlDzr = 0;
//            double swirlDzi = 0;
//            double squaredSwirlDzThreshold = dcMax * 100;
//            double swirlDetectionThreshold = 2;
//
//            z = LWBigComplex.zero(precision);
//            iteration = 0;
//            double prevSwirlDz2 = 0;
//
//            while (zr * zr + zi * zi < bailout * bailout && iteration < maxIteration) {
//
//                state.tryBreak(renderID);
//
//                pzr = zr;
//                pzi = zi;
//                z = formula.apply(z, center, precision);
//
//                zr = z.re().doubleValue();
//                zi = z.im().doubleValue();
//
//
//                if(iteration % swirlPeriod == 0){
//                    swirlDzr = pzr - swirlDzrTemp;
//                    swirlDzi = pzi - swirlDziTemp;
//                    swirlDzrTemp = pzr;
//                    swirlDziTemp = pzi;
//                    double swirlDz2 = swirlDzr * swirlDzr + swirlDzi * swirlDzi;
//
//                    if(0 < prevSwirlDz2 && prevSwirlDz2 <= swirlDz2 && swirlDz2 / prevSwirlDz2 < swirlDetectionThreshold && swirlDz2 < squaredSwirlDzThreshold){
//                        break;
//                    }
//                    prevSwirlDz2 = swirlDz2;
//                }
//
//                actionPerRefCalcIteration.accept(iteration);
//                iteration++;
//            }
//
//            System.out.println(iteration - 1);
//        }


        if (!strictFPGBn) {
            fpgBn = LWBigComplex.valueOf(fpgBnr, fpgBni, precision);
        }

        DoubleExponent[] rra = rr.toArray(DoubleExponent[]::new);
        DoubleExponent[] ria = ri.toArray(DoubleExponent[]::new);
        periodArray = periodArrayLength == 0 ? new int[]{period} : Arrays.copyOfRange(periodArray, 0, periodArrayLength);

        return new DeepMandelbrotReference(formula, center, rra, ria, periodArray, lastRef, fpgBn);

    }


    public DeepR3ATable generateR3A(RenderState state, int renderID, R3ASettings blaSettings, DoubleExponent dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalRenderStateException {
        return new DeepR3ATable(state, renderID, blaSettings, refReal, refImag, period, dcMax, actionPerCreatingTableIteration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(refReal), Arrays.hashCode(refImag), Arrays.hashCode(period), lastReference, fpgBn);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DeepMandelbrotReference r &&
               Arrays.equals(refReal, r.refReal) &&
               Arrays.equals(refImag, r.refImag) &&
               Arrays.equals(period, r.period) &&
               Objects.equals(lastReference, r.lastReference) &&
               Objects.equals(fpgBn, r.fpgBn);
    }

    @Override
    public String toString() {
        return getClass() + "[ " +
               STR_FORMULA + formula +
               STR_CENTER + refCenter +
               STR_REFERENCE_REAL + Arrays.toString(refReal) +
               STR_REFERENCE_IMAG + Arrays.toString(refImag) +
               STR_PERIOD + Arrays.toString(period) +
               STR_LAST_REF + lastReference +
               STR_FPG_BN + fpgBn + "\n]";
    }
}