package kr.merutilm.fractal.formula;

import java.util.function.IntConsumer;

import javax.annotation.Nonnull;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.parallel.RenderState;
import kr.merutilm.fractal.approx.DeepBLA;
import kr.merutilm.fractal.approx.DeepBLATable;
import kr.merutilm.fractal.settings.CalculationSettings;
import kr.merutilm.fractal.struct.DoubleExponent;
import kr.merutilm.fractal.struct.LWBigComplex;
import kr.merutilm.fractal.util.DoubleExponentMath;

public class DeepMandelbrotPerturbator extends MandelbrotPerturbator {

    private final DeepMandelbrotReference reference;
    private final DeepBLATable table;
    private final DoubleExponent dcMax;
    private final DoubleExponent offR;
    private final DoubleExponent offI;

    public DeepMandelbrotPerturbator(RenderState state, int currentID, CalculationSettings calc, DoubleExponent dcMax, int precision, int period, IntConsumer actionPerRefCalcIteration) throws IllegalRenderStateException{
        this(state, currentID, calc, dcMax, precision, period, actionPerRefCalcIteration, false);
    }
    public DeepMandelbrotPerturbator(RenderState state, int currentID, CalculationSettings calc, DoubleExponent dcMax, int precision, int period, IntConsumer actionPerRefCalcIteration, boolean arbitaryPrecisionFPGBn) throws IllegalRenderStateException {
        this(state, currentID, calc, dcMax, precision, period, actionPerRefCalcIteration, arbitaryPrecisionFPGBn, null, null, DoubleExponent.ZERO, DoubleExponent.ZERO);
    }

    public DeepMandelbrotPerturbator(RenderState state, int currentID, CalculationSettings calc, DoubleExponent dcMax, int precision, int period, IntConsumer actionPerRefCalcIteration, boolean arbitaryPrecisionFPGBn, DeepMandelbrotReference reusedReference, DeepBLATable reusedTable, @Nonnull DoubleExponent offR,@Nonnull DoubleExponent offI) throws IllegalRenderStateException{
        super(state, currentID, calc, arbitaryPrecisionFPGBn);
        this.dcMax = dcMax;
        this.offR = offR;
        this.offI = offI;
        this.reference = reusedReference == null ? DeepMandelbrotReference.generate(state, currentID, calc.center(), precision, calc.maxIteration(), bailout, period, dcMax, strictFPGBn, actionPerRefCalcIteration) : reusedReference;
        this.table = reusedTable == null ? reference.generateBLA(state, currentID, calc.blaSettings(), dcMax) : reusedTable;
    }

    // it returns the double value of iteration
    // Performs the corresponding action on all pixels
    @Override
    public double iterate(DoubleExponent dcr, DoubleExponent dci) {
        
        dcr = dcr.add(offR);
        dci = dci.add(offI);
        DoubleExponent[] rr = reference.refReal();
        DoubleExponent[] ri = reference.refImag();
        
        long iteration = 0;
        int refIteration = 0;
        int maxRefIteration = reference.period();

        DoubleExponent dzr = DoubleExponent.ZERO; // delta z
        DoubleExponent dzi = DoubleExponent.ZERO;

        DoubleExponent zr; // z
        DoubleExponent zi;

        DoubleExponent cd = DoubleExponent.ZERO;
        DoubleExponent pd = cd;

        while (iteration < maxIteration) {

            if(table != null){
                DeepBLA bla = table.lookup(refIteration, dzr, dzi);
                if (bla != null) {
                    DoubleExponent dzr1 = bla.anr().multiply(dzr).subtract(bla.ani().multiply(dzi)).add(bla.bnr().multiply(dcr)).subtract(bla.bni().multiply(dci));
                    DoubleExponent dzi1 = bla.anr().multiply(dzi).add(bla.ani().multiply(dzr)).add(bla.bnr().multiply(dci)).add(bla.bni().multiply(dcr));
    
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
            

            DoubleExponent zr1 = rr[refIteration].doubled().add(dzr);
            DoubleExponent zi1 = ri[refIteration].doubled().add(dzi);

            DoubleExponent zr2 = zr1.multiply(dzr).subtract(zi1.multiply(dzi)).add(dcr);
            DoubleExponent zi2 = zr1.multiply(dzi).add(zi1.multiply(dzr)).add(dci);

            dzr = zr2;
            dzi = zi2;

            refIteration++;

            zr = rr[refIteration].add(dzr);
            zi = ri[refIteration].add(dzi);

            pd = cd;
            cd = DoubleExponentMath.hypotApproximate(zr, zi);

            if (refIteration == maxRefIteration || cd.isSmallerThan(DoubleExponentMath.hypotApproximate(dzr, dzi))) {
                refIteration = 0;
                dzr = zr;
                dzi = zi;
            }


            if (cd.isLargerThan(bailout)) {
                break;
            }

            iteration++;

        }

        if (iteration >= maxIteration) {
            return maxIteration;
        }



        return getDoubleValueIteration(iteration, pd.doubleValue(), cd.doubleValue());

    }

    @Override
    public DeepMandelbrotPerturbator reuse(RenderState state, int currentID, CalculationSettings calc, DoubleExponent dcMax, int precision) throws IllegalRenderStateException{
        LWBigComplex centerOffset = calc.center().subtract(reference.refCenter(), precision);
        DoubleExponent offR = DoubleExponent.valueOf(centerOffset.re());
        DoubleExponent offI = DoubleExponent.valueOf(centerOffset.im());
        return new DeepMandelbrotPerturbator(state, currentID, calc, dcMax, precision, reference.period(), p -> {}, strictFPGBn, reference, table, offR, offI);
    }

    @Override
    public DeepMandelbrotReference getReference() {
        return reference;
    }

    public DoubleExponent dcMax(){
        return dcMax;
    }

}
