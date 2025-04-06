package kr.merutilm.rff.formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.util.AdvancedMath;
import kr.merutilm.rff.util.ArrayFunction;
import kr.merutilm.rff.approx.LightR3ATable;
import kr.merutilm.rff.functions.ArrayCompressionTool;
import kr.merutilm.rff.functions.ArrayCompressor;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.settings.ReferenceCompressionSettings;
import kr.merutilm.rff.precision.LWBigComplex;

public record LightMandelbrotReference(Formula formula, LWBigComplex refCenter, double[] refReal, double[] refImag,
                                       ArrayCompressor referenceCompressor, boolean hasCompressor,
                                       int[] period,
                                       LWBigComplex lastReference,
                                       LWBigComplex fpgBn) implements MandelbrotReference {

    public static LightMandelbrotReference generate(ParallelRenderState state, int renderID, CalculationSettings calc, int precision, int initialPeriod, double dcMax, boolean strictFPGBn, IntConsumer actionPerRefCalcIteration) throws IllegalParallelRenderStateException {
        state.tryBreak(renderID);
        Formula formula = new Mandelbrot();

        double[] rr = new double[1];
        double[] ri = new double[1];
        rr[0] = 0;
        ri[0] = 0;

        LWBigComplex center = calc.center();
        LWBigComplex z = LWBigComplex.zero(precision);
        LWBigComplex fpgBn = LWBigComplex.zero(precision);

        double fpgBnr = 0;
        double fpgBni = 0;

        int iteration = 0;
        double zr = 0;
        double zi = 0;
        int period = 1;

        int[] periodArray = new int[1];
        int periodArrayLength = 0;
        double minZRadius = Double.MAX_VALUE;
        int reuseIndex = 0;

        List<ArrayCompressionTool> tools = new ArrayList<>();
        // List<LightR3A> mergeR3A = new ArrayList<>();
        int compressed = 0;
        double bailout = calc.bailout();
        long maxIteration = calc.maxIteration();
        ReferenceCompressionSettings refCompressionSettings = calc.referenceCompressionSettings();
        int compressCriteria = refCompressionSettings.compressCriteria();
        int compressionThresholdPower = refCompressionSettings.compressionThresholdPower();
        double compressionThreshold = compressionThresholdPower <= 0 ? 0 : Math.pow(10, -compressionThresholdPower);
//        double mergeAnr = 1;
//        double mergeAni = 0;
//        double mergeBnr = 0;
//        double mergeBni = 0;


        while (zr * zr + zi * zi < bailout * bailout && iteration < maxIteration) {

            state.tryBreak(renderID);

            // use Fast-Period-Guessing, and create R3A Table
            double radius2 = zr * zr + zi * zi;
            double fpgLimit = radius2 / dcMax;

            double fpgBnrTemp = fpgBnr * zr * 2 - fpgBni * zi * 2 + 1;
            double fpgBniTemp = fpgBnr * zi * 2 + fpgBni * zr * 2;
            double fpgRadius = AdvancedMath.hypotApproximate(fpgBnrTemp, fpgBniTemp);


            if (minZRadius > radius2 && radius2 > 0) {
                minZRadius = radius2;
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
                fpgBn = fpgBn.multiply(z, precision).doubled().add(LWBigComplex.valueOf(1, 0, precision), precision);
            }

            fpgBnr = fpgBnrTemp;
            fpgBni = fpgBniTemp;

            //Let's do arbitrary-precision operation!!
            actionPerRefCalcIteration.accept(iteration);
            z = formula.apply(z, center, precision);
            zr = z.re().doubleValue();
            zi = z.im().doubleValue();

            if(compressCriteria >= 0 && iteration >= 1) {
                int refIndex = new ArrayCompressor(tools).compress(reuseIndex + 1);

                if (AdvancedMath.abs(zr / rr[refIndex] - 1) <= compressionThreshold  &&
                    AdvancedMath.abs(zi / ri[refIndex] - 1) <= compressionThreshold
                ) {
//                    double mergeAnrTemp = mergeAnr * zr * 2 - mergeAni * zi * 2;
//                    double mergeAniTemp = mergeAnr * zi * 2 + mergeAni * zr * 2;
//                    double mergeBnrTemp = mergeBnr * zr * 2 - mergeBni * zi * 2 + 1;
//                    double mergeBniTemp = mergeBnr * zi * 2 + mergeBni * zr * 2;
//                    mergeAnr = mergeAnrTemp;
//                    mergeAni = mergeAniTemp;
//                    mergeBnr = mergeBnrTemp;
//                    mergeBni = mergeBniTemp;
                    reuseIndex++;
                } else {
                    if (reuseIndex != 0) {
                        if (reuseIndex > compressCriteria) { // reference compression criteria

                            ArrayCompressionTool compressor = new ArrayCompressionTool(1, iteration - reuseIndex + 1, iteration);
                            compressed += compressor.range(); //get the increment of iteration
                            tools.add(compressor);
//                            mergeR3A.add(new LightR3A(mergeAnr, mergeAni, mergeBnr, mergeBni, compressor.range() - 1, Double.MAX_VALUE));
                            // Since its usage is merging only duplicate R3As,
                            // the radius is always smaller than the R3A to be merged.
                            // When LightR3A.merge() is invoked, the radius is computed via the "Math.min" operator,
                            // so the radius is not required.
                        }
                        //If it is enough to large, set all reference in the range to 0 and save the index

                        reuseIndex = 0;
                    }
//                    mergeAnr = 1;
//                    mergeAni = 0;
//                    mergeBnr = 0;
//                    mergeBni = 0;

                }
            }
            
            period = ++iteration;

            if(compressCriteria < 0 || reuseIndex <= compressCriteria){
                int index = iteration - compressed;

                if (index == rr.length) {
                    rr = ArrayFunction.exp2xArr(rr);
                    ri = ArrayFunction.exp2xArr(ri);
                }
    
                rr[index] = zr;
                ri[index] = zi;
            }
            
        }

//        boolean useSwirlGuessing = false; 
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
//            long swirlIteration = 0;
//            double prevSwirlDz2 = 0;
//
//            while (zr * zr + zi * zi < bailout * bailout && swirlIteration < maxIteration) {
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
//                if(swirlIteration % swirlPeriod == 0){
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
//                swirlIteration++;
//            }
//
//            System.out.println(swirlIteration - 1);
//        }


        if (!strictFPGBn) {
            fpgBn = LWBigComplex.valueOf(fpgBnr, fpgBni, precision);
        }

        rr = Arrays.copyOfRange(rr, 0, period - compressed + 1);
        ri = Arrays.copyOfRange(ri, 0, period - compressed + 1);
        periodArray = periodArrayLength == 0 ? new int[]{period} : Arrays.copyOfRange(periodArray, 0, periodArrayLength);

        return new LightMandelbrotReference(formula, center, rr, ri, new ArrayCompressor(tools), !tools.isEmpty(), periodArray, z, fpgBn);
    }

    public double real(int iteration) {
        return hasCompressor ? refReal[referenceCompressor.compress(iteration)] : refReal[iteration];
    }

    public double imag(int iteration) {
        return hasCompressor ? refImag[referenceCompressor.compress(iteration)] : refImag[iteration];
    }

    public LightR3ATable generateR3A(ParallelRenderState state, int renderID, R3ASettings r3aSettings, double dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException {
        return new LightR3ATable(state, renderID, this, r3aSettings, dcMax, actionPerCreatingTableIteration);
    }

    @Override
    public int length() {
        return refReal.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(refReal), Arrays.hashCode(refImag), Arrays.hashCode(period), lastReference, fpgBn);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LightMandelbrotReference(
                Formula f, LWBigComplex c, double[] rr, double[] ri, ArrayCompressor rc, boolean hc,
                int[] p, LWBigComplex l,
                LWBigComplex bn
        ) &&
               Objects.equals(formula, f) &&
               Objects.equals(refCenter, c) &&
               Arrays.equals(refReal, rr) &&
               Arrays.equals(refImag, ri) &&
               Objects.equals(referenceCompressor, rc) &&
               Objects.equals(hasCompressor, hc) &&
               Arrays.equals(period, p) &&
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
               STR_LAST_REF + lastReference +
               STR_FPG_BN + fpgBn + "\n]";
    }


}
