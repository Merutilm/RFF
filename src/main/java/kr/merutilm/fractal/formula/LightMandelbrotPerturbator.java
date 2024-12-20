package kr.merutilm.fractal.formula;

import java.util.function.IntConsumer;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.parallel.RenderState;
import kr.merutilm.base.util.AdvancedMath;
import kr.merutilm.fractal.approx.LightBLA;
import kr.merutilm.fractal.approx.LightBLATable;
import kr.merutilm.fractal.approx.LightRRA;
import kr.merutilm.fractal.settings.CalculationSettings;
import kr.merutilm.fractal.struct.DoubleExponent;
import kr.merutilm.fractal.struct.LWBigComplex;

public class LightMandelbrotPerturbator extends MandelbrotPerturbator {
    private final LightMandelbrotReference reference;
    private final LightBLATable table;
    private final double dcMax;
    private final double offR;
    private final double offI;

    public LightMandelbrotPerturbator(RenderState state, int currentID, CalculationSettings calc, double dcMax, int precision, int period, IntConsumer actionPerRefCalcIteration) throws IllegalRenderStateException{
        this(state, currentID, calc, dcMax, precision, period, actionPerRefCalcIteration, false);
    }
    public LightMandelbrotPerturbator(RenderState state, int currentID, CalculationSettings calc, double dcMax, int precision, int period, IntConsumer actionPerRefCalcIteration, boolean arbitaryPrecisionFPGBn) throws IllegalRenderStateException{
        this(state, currentID, calc, dcMax, precision, period, actionPerRefCalcIteration, arbitaryPrecisionFPGBn, null, null, 0, 0);
    }

    public LightMandelbrotPerturbator(RenderState state, int currentID, CalculationSettings calc, double dcMax, int precision, int period, IntConsumer actionPerRefCalcIteration, boolean arbitaryPrecisionFPGBn,
            LightMandelbrotReference reusedReference, LightBLATable reusedTable, double offR, double offI) throws IllegalRenderStateException{
        super(state, currentID, calc, arbitaryPrecisionFPGBn);
        this.dcMax = dcMax;
        this.offR = offR;
        this.offI = offI;
        this.reference = reusedTable == null ? LightMandelbrotReference.generate(state, currentID, calc.center(), precision, calc.maxIteration(), bailout, period, dcMax, strictFPGBn, actionPerRefCalcIteration) : reusedReference;
        this.table = reusedTable == null ? reference.generateBLA(state, currentID, calc.blaSettings(), dcMax) : reusedTable;
    }

    // it returns the double value of iteration
    // Performs the corresponding action on all pixels
    @Override
    public double iterate(DoubleExponent dcr, DoubleExponent dci) {
        double dcr1 = dcr.doubleValue() + offR;
        double dci1 = dci.doubleValue() + offI;
        double[] rr = reference.refReal();
        double[] ri = reference.refImag();


        long iteration = 0;
        int refIteration = 0;
        int maxRefIteration = reference.period();

        double dzr = 0; // delta z
        double dzi = 0;

        double zr; // z
        double zi;

        double cd = 0;
        double pd = cd;

        while (iteration < maxIteration) {

            if(table != null){
                LightBLA bla = table.lookup(refIteration, dzr, dzi);
                
                if (bla != null){
                    double dzr1 = bla.anr() * dzr - bla.ani() * dzi + bla.bnr() * dcr1 - bla.bni() * dci1;
                    double dzi1 = bla.anr() * dzi + bla.ani() * dzr + bla.bnr() * dci1 + bla.bni() * dcr1;
    
                    dzr = dzr1;
                    dzi = dzi1;
    
                    iteration += bla.skip();
                    refIteration += bla.skip();
                    if (iteration >= maxIteration) {
                        return maxIteration;
                    }
                    continue;
                }
            }
            
            
            double zr1 = rr[refIteration] * 2 + dzr;
            double zi1 = ri[refIteration] * 2 + dzi;

            double zr2 = zr1 * dzr - zi1 * dzi + dcr1;
            double zi2 = zr1 * dzi + zi1 * dzr + dci1;

            dzr = zr2;
            dzi = zi2;


            refIteration++;


            zr = rr[refIteration] + dzr;
            zi = ri[refIteration] + dzi;


            pd = cd;
            cd = zr * zr + zi * zi;

            if (refIteration == maxRefIteration || cd < dzr * dzr + dzi * dzi) {
                
                int index = Math.max(0, -Math.getExponent(AdvancedMath.hypotApproximate(dzr, dzi)) / MandelbrotReference.RRA_UNIT_POWER);
                LightRRA[] table = reference.rraTable();
                LightRRA rra = table.length == 0 ? null : table[Math.min(index, table.length - 1)];
                
                if(rra == null || rra.skip() <= 1){
                    refIteration = 0;
                    dzr = zr;
                    dzi = zi;
                }else{

                    iteration += rra.skip();
                    refIteration = rra.skip();
                    double zrp = zr * zr - zi * zi;
                    double zip = 2 * zr * zi;

                    dzr = rra.qnr() * zrp - rra.qni() * zip + rra.bnr() * dcr1 - rra.bni() * dci1;
                    dzi = rra.qnr() * zip + rra.qni() * zrp + rra.bnr() * dci1 + rra.bni() * dcr1;    
                }
            }

            if (cd > bailout * bailout) {
                break;
            }

            iteration++;


        }

        if (iteration >= maxIteration) {
            return maxIteration;
        }


        pd = Math.sqrt(pd);
        cd = Math.sqrt(cd);
    

        return getDoubleValueIteration(iteration, pd, cd);

    }

    @Override
    public LightMandelbrotPerturbator reuse(RenderState state, int currentID, CalculationSettings calc, DoubleExponent dcMax, int precision) throws IllegalRenderStateException{
        LWBigComplex centerOffset = calc.center().subtract(reference.refCenter(), precision);

        double offR = centerOffset.re().doubleValue();
        double offI = centerOffset.im().doubleValue();

        return new LightMandelbrotPerturbator(state, currentID, calc, dcMax.doubleValue(), precision, reference.period(), p -> {}, strictFPGBn, reference, table, offR, offI);
    }

    @Override
    public LightMandelbrotReference getReference() {
        return reference;
    }

    public double dcMax() {
        return dcMax;
    }

}
