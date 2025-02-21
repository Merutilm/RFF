package kr.merutilm.rff.formula;

import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

import kr.merutilm.rff.shader.IllegalRenderStateException;
import kr.merutilm.rff.shader.RenderState;
import kr.merutilm.rff.approx.LightR3A;
import kr.merutilm.rff.approx.LightR3ATable;
import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.struct.LWBigComplex;

public class LightMandelbrotPerturbator extends MandelbrotPerturbator {
    private final LightMandelbrotReference reference;
    private final LightR3ATable table;
    private final double dcMax;
    private final double offR;
    private final double offI;

    public LightMandelbrotPerturbator(RenderState state, int currentID, CalculationSettings calc, double dcMax, int precision, int period, IntConsumer actionPerRefCalcIteration, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalRenderStateException{
        this(state, currentID, calc, dcMax, precision, period, actionPerRefCalcIteration, actionPerCreatingTableIteration,false);
    }
    public LightMandelbrotPerturbator(RenderState state, int currentID, CalculationSettings calc, double dcMax, int precision, int period, IntConsumer actionPerRefCalcIteration, BiConsumer<Integer, Double> actionPerCreatingTableIteration, boolean arbitraryPrecisionFPGBn) throws IllegalRenderStateException{
        this(state, currentID, calc, dcMax, precision, period, actionPerRefCalcIteration, actionPerCreatingTableIteration, arbitraryPrecisionFPGBn, null, null, 0, 0);
    }

    public LightMandelbrotPerturbator(RenderState state, int currentID, CalculationSettings calc, double dcMax, int precision, int period, IntConsumer actionPerRefCalcIteration, BiConsumer<Integer, Double> actionPerCreatingTableIteration, boolean arbitraryPrecisionFPGBn,
                                      LightMandelbrotReference reusedReference, LightR3ATable reusedTable, double offR, double offI) throws IllegalRenderStateException{
        super(state, currentID, calc, arbitraryPrecisionFPGBn);
        this.dcMax = dcMax;
        this.offR = offR;
        this.offI = offI;
        this.reference = reusedTable == null ? LightMandelbrotReference.generate(state, currentID, calc, precision, period, dcMax, strictFPGBn, actionPerRefCalcIteration) : reusedReference;
        this.table = reusedTable == null ? reference.generateR3A(state, currentID, calc.r3aSettings(), dcMax, actionPerCreatingTableIteration) : reusedTable;
    }
 // AtomicInteger a = new AtomicInteger();

    // it returns the double value of iteration
    // Performs the corresponding action on all pixels
    @Override
    public double iterate(DoubleExponent dcr, DoubleExponent dci) throws IllegalRenderStateException {
        double dcr1 = dcr.doubleValue() + offR;
        double dci1 = dci.doubleValue() + offI;
 // int i = a.incrementAndGet();

        long iteration = 0;
        int refIteration = 0;
        int maxRefIteration = reference.longestPeriod();

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
                LightR3A r3a = table.lookup(refIteration, dzr, dzi);
                
                if (r3a != null){
                    double dzr1 = r3a.anr() * dzr - r3a.ani() * dzi + r3a.bnr() * dcr1 - r3a.bni() * dci1;
                    double dzi1 = r3a.anr() * dzi + r3a.ani() * dzr + r3a.bnr() * dci1 + r3a.bni() * dcr1;
    
                    dzr = dzr1;
                    dzi = dzi1;
    
                    iteration += r3a.skip();
                    refIteration += r3a.skip();
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
    //                        skipCount += r3a.skip();
    //                        isFirst = false;
    //                    }
    //
    //                }
    
                    continue;
                }
            }
            
            if(refIteration != maxRefIteration){
                double zr1 = reference.real(refIteration) * 2 + dzr;
                double zi1 = reference.imag(refIteration) * 2 + dzi;

                double zr2 = zr1 * dzr - zi1 * dzi + dcr1;
                double zi2 = zr1 * dzi + zi1 * dzr + dci1;

                dzr = zr2;
                dzi = zi2;


                refIteration++;
                iteration++;

            }

            zr = reference.real(refIteration) + dzr;
            zi = reference.imag(refIteration) + dzi;


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

            state.tryBreak(currentID);

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

        return new LightMandelbrotPerturbator(state, currentID, calc, dcMax.doubleValue(), precision, reference.longestPeriod(), _ -> {}, (_, _) -> {}, strictFPGBn, reference, table, offR, offI);
    }

    @Override
    public LightMandelbrotReference getReference() {
        return reference;
    }

    public double dcMax() {
        return dcMax;
    }

}
