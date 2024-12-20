package kr.merutilm.fractal.formula;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntConsumer;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.parallel.RenderState;
import kr.merutilm.base.util.AdvancedMath;
import kr.merutilm.base.util.ArrayFunction;
import kr.merutilm.fractal.approx.LightBLATable;
import kr.merutilm.fractal.approx.LightRRA;
import kr.merutilm.fractal.settings.BLASettings;
import kr.merutilm.fractal.struct.LWBigComplex;

public record LightMandelbrotReference(Formula formula, LWBigComplex refCenter,double[] refReal, double[] refImag, int period, LWBigComplex lastReference, LWBigComplex fpgBn, LightRRA[] rraTable) implements MandelbrotReference{

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

        double rraQnr = 1;
        double rraQni = 0;
        double fpgBnr = 0;
        double fpgBni = 0;

        int iteration = 0;
        double pzr;
        double pzi;
        double zr = 0;
        double zi = 0;
        int period = 1;

        int rraLength = Math.max(0, -Math.getExponent(dcMax) / RRA_UNIT_POWER);
        
        int rraIndex = 0;
        double epsilon = 1.0E-3;

        LightRRA[] table = new LightRRA[rraLength];
        double checkDzLength = 1;
        
        while (zr * zr + zi * zi < bailout * bailout && iteration < maxIteration) {

            state.tryBreak(renderID);

            lastRef = z;
            pzr = zr;
            pzi = zi;
            z = formula.apply(z, center, precision);

            zr = z.re().doubleValue();
            zi = z.im().doubleValue();

            double prz2 = pzr * pzr + pzi * pzi;

            // use Fast-Period-Guessing, and create RRA Table
            double fpgLimit = prz2 / dcMax;

            double rraQnrTemp = iteration == 0 ? 1 : rraQnr * pzr * 2 - rraQni * pzi * 2;
            double rraQniTemp = iteration == 0 ? 0 : rraQnr * pzi * 2 + rraQni * pzr * 2;
            double rraRadius = AdvancedMath.hypotApproximate(rraQnrTemp, rraQniTemp);

            double fpgBnrTemp = fpgBnr * pzr * 2 - fpgBni * pzi * 2 + 1;
            double fpgBniTemp = fpgBnr * pzi * 2 + fpgBni * pzr * 2;
            double fpgRadius = AdvancedMath.hypotApproximate(fpgBnrTemp, fpgBniTemp);


            
            actionPerRefCalcIteration.accept(iteration);

            while(iteration >= 1 && rraIndex < rraLength && rraRadius * checkDzLength + fpgRadius * dcMax > epsilon *  2 * AdvancedMath.hypotApproximate(zr, zi)){
                table[rraIndex] = new LightRRA(rraQnr, rraQni, fpgBnr, fpgBni, iteration);
                rraIndex++;
                checkDzLength /= (2 << RRA_UNIT_POWER);
            }

            if ((iteration >= 1 && fpgRadius > fpgLimit) || iteration == maxIteration - 1 || initialPeriod == iteration) {
                period = iteration;
                break;
            }

            if(strictFPGBn){
                fpgBn = fpgBn.multiply(lastRef, precision).doubled().add(LWBigComplex.valueOf(1,0, precision), precision);
            }

            rraQnr = rraQnrTemp;
            rraQni = rraQniTemp;
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

        for (int i = 0; i < table.length; i++) {
            if(i >= 1 && table[i] == null){
                table[i] = table[i - 1];
            }
        }
        System.out.println(Arrays.stream(table).map(e -> e == null ? null : e.skip()).toList());
        if(!strictFPGBn){
            fpgBn = LWBigComplex.valueOf(fpgBnr, fpgBni, precision);
        }

        rr = Arrays.copyOfRange(rr, 0, period + 1);
        ri = Arrays.copyOfRange(ri, 0, period + 1);

        return new LightMandelbrotReference(formula, center, rr, ri, period, lastRef, fpgBn, table);
    }

    public LightBLATable generateBLA(RenderState state, int renderID, BLASettings blaSettings, double dcMax) throws IllegalRenderStateException{
        return new LightBLATable(state, renderID, blaSettings, refReal, refImag, period, dcMax);
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
