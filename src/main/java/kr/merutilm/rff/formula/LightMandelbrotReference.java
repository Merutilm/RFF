package kr.merutilm.rff.formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

import kr.merutilm.rff.shader.IllegalRenderStateException;
import kr.merutilm.rff.shader.RenderState;
import kr.merutilm.rff.util.AdvancedMath;
import kr.merutilm.rff.util.ArrayFunction;
import kr.merutilm.rff.approx.LightR3ATable;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.struct.LWBigComplex;

public record LightMandelbrotReference(Formula formula, LWBigComplex refCenter, double[] refReal, double[] refImag,
                                       int[] period, ReferenceCompressor[] compressors, int[] indexToIterationIncrement, LWBigComplex lastReference,
                                       LWBigComplex fpgBn) implements MandelbrotReference {

    public static LightMandelbrotReference generate(RenderState state, int renderID, LWBigComplex center, int precision, long maxIteration, double bailout, int initialPeriod, double dcMax, boolean strictFPGBn, IntConsumer actionPerRefCalcIteration) throws IllegalRenderStateException {
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

        int[] periodArray = new int[1];
        int periodArrayLength = 0;
        double minZRadius = Double.MAX_VALUE;
        int reuseIndex = 0;

        List<ReferenceCompressor> referenceCompressors = new ArrayList<>();
        int[] indexToIterationIncrement = new int[1];
        int iterationIncrement = 0;

        while (zr * zr + zi * zi < bailout * bailout && iteration < maxIteration) {

            state.tryBreak(renderID);

            lastRef = z;
            pzr = zr;
            pzi = zi;
            z = formula.apply(z, center, precision);
            period = iteration;

            zr = z.re().doubleValue();
            zi = z.im().doubleValue();

            if (iteration >= 1 && zr == rr[reuseIndex + 1] && zi == ri[reuseIndex + 1]) {
                reuseIndex++;
            } else if (reuseIndex != 0) {
                ReferenceCompressor compressor = new ReferenceCompressor(1, reuseIndex, iteration - reuseIndex + 1, iteration);
                System.out.println(compressor.startIteration() + "->" + compressor.endIteration() + "=" + 1 + "->" + compressor.length());

                iterationIncrement += compressor.length(); //TODO : get the increment of iteration
                referenceCompressors.add(compressor);

                //TODO : If it is enough to large, set all reference in the range to 0 and save the index
                reuseIndex = 0;
            }

            double prevZRadius2 = pzr * pzr + pzi * pzi;

            // use Fast-Period-Guessing, and create RRA Table
            double fpgLimit = prevZRadius2 / dcMax;

            double fpgBnrTemp = fpgBnr * pzr * 2 - fpgBni * pzi * 2 + 1;
            double fpgBniTemp = fpgBnr * pzi * 2 + fpgBni * pzr * 2;
            double fpgRadius = AdvancedMath.hypotApproximate(fpgBnrTemp, fpgBniTemp);

            actionPerRefCalcIteration.accept(iteration);

            if (minZRadius > prevZRadius2 && prevZRadius2 > 0) {
                minZRadius = prevZRadius2;
                if (periodArrayLength == periodArray.length) {
                    periodArray = ArrayFunction.exp2xArr(periodArray);
                }

                periodArray[periodArrayLength] = iteration;
                periodArrayLength++;
            }

            if ((iteration >= 1 && fpgRadius > fpgLimit) || iteration == maxIteration - 1 || initialPeriod == iteration) {

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

            if (iteration == rr.length) {
                rr = ArrayFunction.exp2xArr(rr);
                ri = ArrayFunction.exp2xArr(ri);
            }

            rr[iteration] = zr;
            ri[iteration] = zi;
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

        rr = Arrays.copyOfRange(rr, 0, period + 1);
        ri = Arrays.copyOfRange(ri, 0, period + 1);
        periodArray = periodArrayLength == 0 ? new int[]{period} : Arrays.copyOfRange(periodArray, 0, periodArrayLength);

        return new LightMandelbrotReference(formula, center, rr, ri, periodArray, referenceCompressors.toArray(ReferenceCompressor[]::new), indexToIterationIncrement, lastRef, fpgBn);
    }

    public double real(int iteration){
        return refReal[iterationToIndex(iteration)];
    }

    public double imag(int iteration){
        return refImag[iterationToIndex(iteration)];
    }

    private int iterationToIndex(int iteration){
//        for (ReferenceCompressor compressor : compressors){
//            if(compressor.startIteration() <= iteration && iteration <= compressor.endIteration()) {
//                iteration -= compressor.startIteration() - compressor.startReferenceIndex();
//            }
//        }

        //TODO : apply compressors
        return iteration;
    }

    public LightR3ATable generateR3A(RenderState state, int renderID, R3ASettings r3aSettings, double dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalRenderStateException {
        return new LightR3ATable(state, renderID, this, r3aSettings, dcMax, actionPerCreatingTableIteration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(refReal), Arrays.hashCode(refImag), Arrays.hashCode(period), lastReference, fpgBn);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LightMandelbrotReference (Formula f, LWBigComplex c, double[] rr, double[] ri,
                                                      int[] p, ReferenceCompressor[] comp, int[] iti, LWBigComplex l,
                                                      LWBigComplex bn) &&
               Objects.equals(formula, f) &&
               Objects.equals(refCenter, c) &&
               Arrays.equals(refReal, rr) &&
               Arrays.equals(refImag, ri) &&
               Arrays.equals(period, p) &&
               Arrays.equals(compressors, comp) &&
               Arrays.equals(indexToIterationIncrement, iti) &&
               Objects.equals(lastReference, l) &&
               Objects.equals(fpgBn, bn);
    }

    @Override
    public String toString() {
        return getClass() + "[ " +
               STR_FORMULA + formula +
               STR_CENTER + refCenter +
               STR_REFERENCE_REAL + Arrays.toString(refReal) +
               STR_REFERENCE_IMAG + Arrays.toString(refImag) +
               STR_PERIOD + Arrays.toString(period) +
               STR_COMPRESSORS + Arrays.toString(compressors) +
               STR_LAST_REF + lastReference +
               STR_FPG_BN + fpgBn + "\n]";
    }


}
