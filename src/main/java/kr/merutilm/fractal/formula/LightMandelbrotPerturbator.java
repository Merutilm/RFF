package kr.merutilm.fractal.formula;

import java.util.function.IntConsumer;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.parallel.RenderState;
import kr.merutilm.fractal.approx.LightBLA;
import kr.merutilm.fractal.approx.LightBLATable;
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
 // AtomicInteger a = new AtomicInteger();

    // it returns the double value of iteration
    // Performs the corresponding action on all pixels
    @Override
    public double iterate(DoubleExponent dcr, DoubleExponent dci) {
        double dcr1 = dcr.doubleValue() + offR;
        double dci1 = dci.doubleValue() + offI;
        double[] rr = reference.refReal();
        double[] ri = reference.refImag();
 // int i = a.incrementAndGet();

        long iteration = 0;
        int refIteration = 0;
        int maxRefIteration = reference.period();

        double dzr = 0; // delta z
        double dzi = 0;

        double zr; // z
        double zi;

        double cd = 0;
        double pd = cd;
//        if (i == 1) {
//            System.out.println("[SKIP VALUES]");
//        }
//        int startRef = 0;
//        int skipCount = 0;
//        boolean isFirst = true;
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
                    
    //                if (i == 1) { //Tracking refIteration Skips
    //                    if (isFirst) {
    //                        startRef = refIteration;
    //                    }
    //
    //                    if (!isFirst && startRef + skipCount != refIteration) {
    //                        System.out.println("S " + startRef + ", K " + skipCount + ", E " + (startRef + skipCount));
    //                        skipCount = 0;
    //                        isFirst = true;
    //                    } else {
    //                        skipCount += bla.skip();
    //                        isFirst = false;
    //                    }
    //
    //                }
    
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
                refIteration = 0;
                dzr = zr;
                dzi = zi;
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
