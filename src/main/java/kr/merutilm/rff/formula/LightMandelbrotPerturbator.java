package kr.merutilm.rff.formula;

// import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.LongConsumer;

import kr.merutilm.rff.approx.LightPA;
import kr.merutilm.rff.approx.LightMPATable;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.precision.LWBigComplex;

public class LightMandelbrotPerturbator extends MandelbrotPerturbator {
    private final LightMandelbrotReference reference;
    private final LightMPATable table;
    private final double dcMax;
    private final double offR;
    private final double offI;

    public LightMandelbrotPerturbator(ParallelRenderState state, int currentID, CalculationSettings calc, double dcMax, int precision, long period, LongConsumer actionPerRefCalcIteration, BiConsumer<Long, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException{
        this(state, currentID, calc, dcMax, precision, period, actionPerRefCalcIteration, actionPerCreatingTableIteration,false);
    }
    public LightMandelbrotPerturbator(ParallelRenderState state, int currentID, CalculationSettings calc, double dcMax, int precision, long period, LongConsumer actionPerRefCalcIteration, BiConsumer<Long, Double> actionPerCreatingTableIteration, boolean arbitraryPrecisionFPGBn) throws IllegalParallelRenderStateException{
        this(state, currentID, calc, dcMax, precision, period, actionPerRefCalcIteration, actionPerCreatingTableIteration, arbitraryPrecisionFPGBn, null, null, 0, 0);
    }

    public LightMandelbrotPerturbator(ParallelRenderState state, int currentID, CalculationSettings calc, double dcMax, int precision, long period, LongConsumer actionPerRefCalcIteration, BiConsumer<Long, Double> actionPerCreatingTableIteration, boolean arbitraryPrecisionFPGBn,
                                      LightMandelbrotReference reusedReference, LightMPATable reusedTable, double offR, double offI) throws IllegalParallelRenderStateException{
        super(state, currentID, calc, arbitraryPrecisionFPGBn);
        this.dcMax = dcMax;
        this.offR = offR;
        this.offI = offI;
        this.reference = reusedTable == null ? LightMandelbrotReference.generate(state, currentID, calc, precision, period, dcMax, strictFPGBn, actionPerRefCalcIteration) : reusedReference;
        this.table = reusedTable == null ? reference.generateMPA(state, currentID, calc.MPASettings(), dcMax, actionPerCreatingTableIteration) : reusedTable;
    }
    // AtomicInteger a = new AtomicInteger();

    // it returns the double value of iteration
    // Performs the corresponding action on all pixels
    @Override
    public double iterate(DoubleExponent dcr, DoubleExponent dci) throws IllegalParallelRenderStateException {
        double dcr1 = dcr.doubleValue() + offR;
        double dci1 = dci.doubleValue() + offI;
        // int i = a.incrementAndGet();

        long iteration = 0;
        long refIteration = 0;
        int absIteration = 0;
        long maxRefIteration = reference.longestPeriod();

//        double minRad = Double.MAX_VALUE;
        double dzr = 0; // delta z
        double dzi = 0;
        double zr; // z
        double zi;

        double cd = 0;
        double pd = cd;
        boolean isAbs = calc.absoluteIterationMode();
        // if (i == 1) {
        //     System.out.println("[SKIP VALUES]");
        // }
        // int startRef = 0;
        // int skipCount = 0;
        // boolean isFirst = true;
        // if(dcr1 * dcr1 + dci1 * dci1 < 4e-181){
        //     System.out.println(i);
        // }
        // if (dcr1 * dcr1 + dci1 * dci1 < 4e-181) {
        //     System.out.println(1);
        // }

        while (iteration < maxIteration) {
            
            if(table != null){
                LightPA pa = table.lookup(refIteration, dzr, dzi);

                if (pa != null){
                    double dzr1 = pa.anr() * dzr - pa.ani() * dzi + pa.bnr() * dcr1 - pa.bni() * dci1;
                    double dzi1 = pa.anr() * dzi + pa.ani() * dzr + pa.bnr() * dci1 + pa.bni() * dcr1;

                    dzr = dzr1;
                    dzi = dzi1;

                    iteration += pa.skip();
                    refIteration += pa.skip();
                    if (iteration >= maxIteration) {
                        return isAbs ? absIteration : maxIteration;
                    }

                    // if (dcr1 * dcr1 + dci1 * dci1 < 4e-181) { //Tracking refIteration Skips
                    //     if (isFirst) {
                    //         startRef = refIteration;
                    //     }

                    //     if (!isFirst && startRef + skipCount != refIteration) {
                    //         System.out.println("S " + startRef + ", K " + skipCount + ", E " + (startRef + skipCount));
                    //         skipCount = 0;
                    //         isFirst = true;
                    //     } else {
                    //         skipCount += r3a.skip();
                    //         isFirst = false;
                    //     }

                    // }

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
                absIteration++;
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

        if(isAbs){
            return absIteration;
        }

        if (iteration >= maxIteration) {
            return maxIteration;
        }

        pd = Math.sqrt(pd);
        cd = Math.sqrt(cd);

        return getDoubleValueIteration(iteration, pd, cd);

    }

    @Override
    public LightMandelbrotPerturbator reuse(ParallelRenderState state, int currentID, CalculationSettings calc, DoubleExponent dcMax, int precision) throws IllegalParallelRenderStateException{
        LWBigComplex centerOffset = calc.center().subtract(reference.refCenter(), precision);

        double offR = centerOffset.re().doubleValue();
        double offI = centerOffset.im().doubleValue();

        return new LightMandelbrotPerturbator(state, currentID, calc, dcMax.doubleValue(), precision, reference.longestPeriod(), _ -> {}, (_, _) -> {}, strictFPGBn, reference, table, offR, offI);
    }

    @Override
    public LightMandelbrotReference getReference() {
        return reference;
    }

    @Override
    public LightMPATable getMPATable() {
        return table;
    }

    public double dcMax() {
        return dcMax;
    }

}
