package kr.merutilm.rff.approx;

import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.util.AdvancedMath;
import kr.merutilm.rff.util.DoubleExponentMath;

public record DeepR3A(DoubleExponent anr, DoubleExponent ani, DoubleExponent bnr, DoubleExponent bni, int start, int skip, DoubleExponent radius) implements R3A{
    public static DeepR3A create(int start) {
        return new DeepR3A(DoubleExponent.ONE, DoubleExponent.ZERO, DoubleExponent.ZERO, DoubleExponent.ZERO, start, 0, DoubleExponent.POSITIVE_INFINITY);
    }

    public DeepR3A step(DoubleExponent[] rr, DoubleExponent[] ri, double epsilon, DoubleExponent dcMax) {

        int iter = start + skip; //n+k
        DoubleExponent z2r = rr[iter].doubled();
        DoubleExponent z2i = ri[iter].doubled();
        DoubleExponent anrStep = anr.multiply(z2r).subtract(ani.multiply(z2i));
        DoubleExponent aniStep = anr.multiply(z2i).add(ani.multiply(z2r));
        DoubleExponent bnrStep = bnr.multiply(z2r).subtract(bni.multiply(z2i)).add(DoubleExponent.ONE);
        DoubleExponent bniStep = bnr.multiply(z2i).add(bni.multiply(z2r));

        DoubleExponent z2l = DoubleExponentMath.hypotApproximate(z2r, z2i);
        DoubleExponent anlOriginal = DoubleExponentMath.hypotApproximate(anr, ani);
        DoubleExponent bnlOriginal = DoubleExponentMath.hypotApproximate(bnr, bni);

        DoubleExponent radius = DoubleExponentMath.min(this.radius, DoubleExponent.valueOf(epsilon).multiply(z2l).subtract(bnlOriginal.multiply(dcMax)).divide(anlOriginal));

        return new DeepR3A(anrStep, aniStep, bnrStep, bniStep, start, skip + 1, radius);
    }

    public boolean isValid(DoubleExponent dzr, DoubleExponent dzi){
        return DoubleExponentMath.hypotApproximate(dzr, dzi).isSmallerThan(radius);
    }
}
