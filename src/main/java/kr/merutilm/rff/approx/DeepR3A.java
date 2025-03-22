package kr.merutilm.rff.approx;

import kr.merutilm.rff.formula.DeepMandelbrotReference;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.util.DoubleExponentMath;

public record DeepR3A(DoubleExponent anr, DoubleExponent ani, DoubleExponent bnr, DoubleExponent bni, int skip, DoubleExponent radius) implements R3A{
    public static final class Builder{
        private DoubleExponent anr; 
        private DoubleExponent ani; 
        private DoubleExponent bnr;
        private DoubleExponent bni;
        private int skip; 
        private DoubleExponent radius;

        private final int start;
        private final DeepMandelbrotReference reference;
        private final double epsilon;
        private final DoubleExponent dcMax;

        private Builder(DeepMandelbrotReference reference, double epsilon, DoubleExponent dcMax, DoubleExponent anr, DoubleExponent ani, DoubleExponent bnr, DoubleExponent bni, int start, int skip, DoubleExponent radius){
            this.anr = anr;
            this.ani = ani;
            this.bnr = bnr;
            this.bni = bni;
            this.skip = skip;
            this.radius = radius;

            this.start = start;
            this.reference = reference;
            this.epsilon = epsilon;
            this.dcMax = dcMax;
        }

        public static Builder create(DeepMandelbrotReference reference, double epsilon, DoubleExponent dcMax, int start) {
            return new Builder(reference, epsilon, dcMax, DoubleExponent.ONE, DoubleExponent.ZERO, DoubleExponent.ZERO, DoubleExponent.ZERO, start, 0, DoubleExponent.POSITIVE_INFINITY);
        }

        public int start(){
            return start;
        }

        // public Builder merge(LightR3A r3a){
        //     double anrMerge = r3a.anr * anr - r3a.ani * ani;
        //     double aniMerge = r3a.anr * ani + r3a.ani * anr;
        //     double bnrMerge = r3a.anr * bnr - r3a.ani * bni + r3a.bnr;
        //     double bniMerge = r3a.anr * bni + r3a.ani * bnr + r3a.bni;

        //     radius = Math.min(radius, r3a.radius);
        //     anr = anrMerge;
        //     ani = aniMerge;
        //     bnr = bnrMerge;
        //     bni = bniMerge;
        //     return this;
        // }

        public Builder step() {

            int iter = start + skip++;
            int index = reference.refReal().compress(iter);
            DoubleExponent z2r = reference.refReal().getArray()[index].doubled();
            DoubleExponent z2i = reference.refImag().getArray()[index].doubled();
            DoubleExponent anrStep = anr.multiply(z2r).subtract(ani.multiply(z2i));
            DoubleExponent aniStep = anr.multiply(z2i).add(ani.multiply(z2r));
            DoubleExponent bnrStep = bnr.multiply(z2r).subtract(bni.multiply(z2i)).add(DoubleExponent.ONE);
            DoubleExponent bniStep = bnr.multiply(z2i).add(bni.multiply(z2r));

            DoubleExponent z2l = DoubleExponentMath.hypotApproximate(z2r, z2i);
            DoubleExponent anlOriginal = DoubleExponentMath.hypotApproximate(anr, ani);
            DoubleExponent bnlOriginal = DoubleExponentMath.hypotApproximate(bnr, bni);

            radius = DoubleExponentMath.min(this.radius, DoubleExponent.valueOf(epsilon).multiply(z2l).subtract(bnlOriginal.multiply(dcMax)).divide(anlOriginal));
            anr = anrStep;
            ani = aniStep;
            bnr = bnrStep;
            bni = bniStep;
            return this;
        }

        public DeepR3A build(){
            return new DeepR3A(anr, ani, bnr, bni, skip, radius);
        }
    }
    public boolean isValid(DoubleExponent dzRadius){
        return dzRadius.isSmallerThan(radius);
    }
}
