package kr.merutilm.rff.formula;

import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

import javax.annotation.Nonnull;

import kr.merutilm.rff.approx.DeepPA;
import kr.merutilm.rff.approx.DeepMPATable;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.precision.LWBigComplex;
import kr.merutilm.rff.util.DoubleExponentMath;

public class DeepMandelbrotPerturbator extends MandelbrotPerturbator {

    private final DeepMandelbrotReference reference;
    private final DeepMPATable table;
    private final DoubleExponent dcMax;
    private final DoubleExponent offR;
    private final DoubleExponent offI;

    public DeepMandelbrotPerturbator(ParallelRenderState state, int currentID, CalculationSettings calc, DoubleExponent dcMax, int precision, int period, IntConsumer actionPerRefCalcIteration, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException {
        this(state, currentID, calc, dcMax, precision, period, actionPerRefCalcIteration, actionPerCreatingTableIteration, false);
    }

    public DeepMandelbrotPerturbator(ParallelRenderState state, int currentID, CalculationSettings calc, DoubleExponent dcMax, int precision, int period, IntConsumer actionPerRefCalcIteration, BiConsumer<Integer, Double> actionPerCreatingTableIteration, boolean arbitraryPrecisionFPGBn) throws IllegalParallelRenderStateException {
        this(state, currentID, calc, dcMax, precision, period, actionPerRefCalcIteration, actionPerCreatingTableIteration, arbitraryPrecisionFPGBn, null, null, DoubleExponent.ZERO, DoubleExponent.ZERO);
    }

    public DeepMandelbrotPerturbator(ParallelRenderState state, int currentID, CalculationSettings calc, DoubleExponent dcMax, int precision, int period, IntConsumer actionPerRefCalcIteration, BiConsumer<Integer, Double> actionPerCreatingTableIteration, boolean arbitraryPrecisionFPGBn, DeepMandelbrotReference reusedReference, DeepMPATable reusedTable, @Nonnull DoubleExponent offR, @Nonnull DoubleExponent offI) throws IllegalParallelRenderStateException {
        super(state, currentID, calc, arbitraryPrecisionFPGBn);
        this.dcMax = dcMax;
        this.offR = offR;
        this.offI = offI;
        this.reference = reusedReference == null ? DeepMandelbrotReference.generate(state, currentID, calc, precision, period, dcMax, strictFPGBn, actionPerRefCalcIteration) : reusedReference;
        this.table = reusedTable == null ? reference.generateMPA(state, currentID, calc.MPASettings(), dcMax, actionPerCreatingTableIteration) : reusedTable;
    }

    // it returns the double value of iteration
    // Performs the corresponding action on all pixels
    @Override
    public double iterate(DoubleExponent dcr, DoubleExponent dci) throws IllegalParallelRenderStateException {

        dcr = dcr.add(offR);
        dci = dci.add(offI);

        long iteration = 0;
        int refIteration = 0;
        int absIteration = 0;
        int maxRefIteration = reference.longestPeriod();

        DoubleExponent dzr = DoubleExponent.ZERO; // delta z
        DoubleExponent dzi = DoubleExponent.ZERO;

        DoubleExponent zr; // z
        DoubleExponent zi;

        DoubleExponent cd = DoubleExponent.ZERO;
        DoubleExponent pd = DoubleExponent.ZERO;

        while (iteration < maxIteration) {

            if (table != null) {
                DeepPA r3a = table.lookup(refIteration, dzr, dzi);
                if (r3a != null) {
                    DoubleExponent dzr1 = r3a.anr().multiply(dzr).subtract(r3a.ani().multiply(dzi)).add(r3a.bnr().multiply(dcr)).subtract(r3a.bni().multiply(dci));
                    DoubleExponent dzi1 = r3a.anr().multiply(dzi).add(r3a.ani().multiply(dzr)).add(r3a.bnr().multiply(dci)).add(r3a.bni().multiply(dcr));

                    dzr = dzr1;
                    dzi = dzi1;

                    iteration += r3a.skip();
                    refIteration += r3a.skip();
                    if (iteration >= maxIteration) {
                        return maxIteration;
                    }
                    continue;
                }
            }
            if (refIteration != maxRefIteration) {
                DoubleExponent zr1 = reference.real(refIteration).doubled().add(dzr);
                DoubleExponent zi1 = reference.imag(refIteration).doubled().add(dzi);

                DoubleExponent zr2 = zr1.multiply(dzr).subtract(zi1.multiply(dzi)).add(dcr);
                DoubleExponent zi2 = zr1.multiply(dzi).add(zi1.multiply(dzr)).add(dci);

                dzr = zr2;
                dzi = zi2;

                refIteration++;
                iteration++;
                absIteration++;
            }

            zr = reference.real(refIteration).add(dzr);
            zi = reference.imag(refIteration).add(dzi);

            pd = cd;
            cd = DoubleExponentMath.hypot(zr, zi);

            if (refIteration == maxRefIteration || cd.isSmallerThan(DoubleExponentMath.hypot(dzr, dzi))) {
                refIteration = 0;
                dzr = zr;
                dzi = zi;
            }


            if (cd.isLargerThan(bailout)) {
                break;
            }

            state.tryBreak(currentID);

        }

        if (calc.absoluteIterationMode()) {
            return absIteration;
        }

        if (iteration >= maxIteration) {
            return maxIteration;
        }


        return getDoubleValueIteration(iteration, pd.doubleValue(), cd.doubleValue());

    }

    @Override
    public DeepMandelbrotPerturbator reuse(ParallelRenderState state, int currentID, CalculationSettings calc, DoubleExponent dcMax, int precision) throws IllegalParallelRenderStateException {
        LWBigComplex centerOffset = calc.center().subtract(reference.refCenter(), precision);
        DoubleExponent offR = DoubleExponent.valueOf(centerOffset.re());
        DoubleExponent offI = DoubleExponent.valueOf(centerOffset.im());
        return new DeepMandelbrotPerturbator(state, currentID, calc, dcMax, precision, reference.longestPeriod(), _ -> {
        }, (_, _) -> {
        }, strictFPGBn, reference, table, offR, offI);
    }

    @Override
    public DeepMandelbrotReference getReference() {
        return reference;
    }

    @Override
    public DeepMPATable getMPATable() {
        return table;
    }

    public DoubleExponent dcMax() {
        return dcMax;
    }

}
