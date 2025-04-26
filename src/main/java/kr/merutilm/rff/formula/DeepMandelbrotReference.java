package kr.merutilm.rff.formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.approx.DeepMPATable;
import kr.merutilm.rff.functions.ArrayCompressor;
import kr.merutilm.rff.functions.ArrayCompressionTool;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.MPASettings;
import kr.merutilm.rff.settings.ReferenceCompressionSettings;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.precision.LWBigComplex;
import kr.merutilm.rff.util.ArrayFunction;
import kr.merutilm.rff.util.DoubleExponentMath;

public record DeepMandelbrotReference(Formula formula, LWBigComplex refCenter, DoubleExponent[] refReal,
                                      DoubleExponent[] refImag, ArrayCompressor referenceCompressor, int[] period, LWBigComplex lastReference,
                                      LWBigComplex fpgBn) implements MandelbrotReference {


    public static DeepMandelbrotReference generate(ParallelRenderState state, int renderID, CalculationSettings calc, int precision, int initialPeriod, DoubleExponent dcMax, boolean strictFPGBn, IntConsumer actionPerRefCalcIteration) throws IllegalParallelRenderStateException {
        state.tryBreak(renderID);
        Formula formula = new Mandelbrot();

        DoubleExponent[] rr = new DoubleExponent[1];
        DoubleExponent[] ri = new DoubleExponent[1];
        rr[0] = DoubleExponent.ZERO;
        ri[0] = DoubleExponent.ZERO;

        LWBigComplex center = calc.center();
        LWBigComplex z = LWBigComplex.zero(precision);
        LWBigComplex fpgBn = LWBigComplex.zero(precision);

        DoubleExponent fpgBnr = DoubleExponent.ZERO;
        DoubleExponent fpgBni = DoubleExponent.ZERO;

        int iteration = 0;
        DoubleExponent zr = DoubleExponent.ZERO;
        DoubleExponent zi = DoubleExponent.ZERO;
        int period = 1;

        int[] periodArray = new int[1];
        int periodArrayLength = 0;
        DoubleExponent minZRadius = DoubleExponent.POSITIVE_INFINITY;
        int reuseIndex = 0;

        List<ArrayCompressionTool> tools = new ArrayList<>();
        int compressed = 0;
        double bailout = calc.bailout();
        long maxIteration = calc.maxIteration();
        ReferenceCompressionSettings refCompressionSettings = calc.referenceCompressionSettings();
        int compressCriteria = refCompressionSettings.compressCriteria();
        int compressionThresholdPower = refCompressionSettings.compressionThresholdPower();
        double compressionThreshold = compressionThresholdPower <= 0 ? 0 : Math.pow(10, -compressionThresholdPower);



        while (DoubleExponentMath.hypot2(zr, zi).isSmallerThan(bailout * bailout) && iteration < maxIteration) {

            state.tryBreak(renderID);

            DoubleExponent radius2 = DoubleExponentMath.hypot2(zr, zi);
            DoubleExponent fpgLimit = radius2.divide(dcMax);

            DoubleExponent fpgBnrTemp = fpgBnr.multiply(zr.doubled()).subtract(fpgBni.multiply(zi.doubled())).add(DoubleExponent.ONE);
            DoubleExponent fpgBniTemp = fpgBnr.multiply(zi.doubled()).add(fpgBni.multiply(zr.doubled()));
            DoubleExponent fpgRadius = DoubleExponentMath.hypotApproximate(fpgBnrTemp, fpgBniTemp);



            if (minZRadius.isLargerThan(radius2) && radius2.isLargerThan(DoubleExponent.ZERO)) {
                minZRadius = radius2;
                if (periodArrayLength == periodArray.length) {
                    periodArray = ArrayFunction.exp2xArr(periodArray);
                }

                periodArray[periodArrayLength] = iteration;
                periodArrayLength++;
            }

            if ((iteration >= 1 && fpgRadius.isLargerThan(fpgLimit)) || iteration == maxIteration - 1 || initialPeriod == iteration) {

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

            actionPerRefCalcIteration.accept(iteration);
            z = formula.apply(z, center, precision);
            zr = DoubleExponent.valueOf(z.re());
            zi = DoubleExponent.valueOf(z.im());

            if(compressCriteria >= 0 && iteration >= 1) {
                int refIndex = new ArrayCompressor(tools).compress(reuseIndex + 1);

                if (!zr.divide(rr[refIndex]).subtract(DoubleExponent.ONE).abs().isLargerThan(compressionThreshold) &&
                    !zi.divide(ri[refIndex]).subtract(DoubleExponent.ONE).abs().isLargerThan(compressionThreshold)
                ) {
                    reuseIndex++;
                } else if (reuseIndex != 0) {
                    if (reuseIndex > compressCriteria) { 

                        ArrayCompressionTool compressor = new ArrayCompressionTool(1, iteration - reuseIndex + 1, iteration);
                        compressed += compressor.range(); //get the increment of iteration
                        tools.add(compressor);
                    }
                    reuseIndex = 0;
                }
            }
            
            period = ++iteration;

            if(compressCriteria < 0 || reuseIndex <= compressCriteria){
                int index = iteration - compressed;

                if (index == rr.length) {
                    rr = ArrayFunction.exp2xArr(rr, DoubleExponent[]::new);
                    ri = ArrayFunction.exp2xArr(ri, DoubleExponent[]::new);
                }
    
                rr[index] = zr;
                ri[index] = zi;
            }
        }


        if (!strictFPGBn) {
            fpgBn = LWBigComplex.valueOf(fpgBnr, fpgBni, precision);
        }

        rr = Arrays.copyOfRange(rr, 0, period - compressed + 1);
        ri = Arrays.copyOfRange(ri, 0, period - compressed + 1);
        periodArray = periodArrayLength == 0 ? new int[]{period} : Arrays.copyOfRange(periodArray, 0, periodArrayLength);
       
        return new DeepMandelbrotReference(formula, center, rr, ri, new ArrayCompressor(tools), periodArray, z, fpgBn);

    }

    public DoubleExponent real(int iteration) {
        return refReal[referenceCompressor.compress(iteration)];
    }

    public DoubleExponent imag(int iteration) {
        return refImag[referenceCompressor.compress(iteration)];
    }

    public DeepMPATable generateMPA(ParallelRenderState state, int renderID, MPASettings MPASettings, DoubleExponent dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException {
        return new DeepMPATable(state, renderID, this, MPASettings, dcMax, actionPerCreatingTableIteration);
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
        return o instanceof DeepMandelbrotReference(
                Formula f, LWBigComplex c, DoubleExponent[] rr, DoubleExponent[] ri,
                ArrayCompressor rc, int[] p, LWBigComplex l,
                LWBigComplex bn
        ) &&
               Objects.equals(formula, f) &&
               Objects.equals(refCenter, c) &&
               Arrays.equals(refReal, rr) &&
               Arrays.equals(refImag, ri) &&
               Objects.equals(referenceCompressor, rc) &&
               Arrays.equals(period, p) &&
               Objects.equals(lastReference, l) &&
               Objects.equals(fpgBn, bn);
    }

    @Override
    public String toString() {
        return getClass() + "[ " +
               STR_FORMULA + formula +
               STR_CENTER + refCenter +
               STR_REFERENCE_REAL + Objects.toString(refReal) +
               STR_REFERENCE_IMAG + Objects.toString(refImag) +
               STR_PERIOD + Arrays.toString(period) +
               STR_LAST_REF + lastReference +
               STR_FPG_BN + fpgBn + "\n]";
    }
}