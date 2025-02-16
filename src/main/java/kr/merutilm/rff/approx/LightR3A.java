package kr.merutilm.rff.approx;

import kr.merutilm.rff.util.AdvancedMath;

public record LightR3A(double anr, double ani, double bnr, double bni, int start, int skip, double radius) implements R3A{

    public static LightR3A create(int start) {
        return new LightR3A(1, 0, 0, 0, start, 0, Double.MAX_VALUE);
    }

    public LightR3A step(double[] rr, double[] ri, double epsilon, double dcMax) {

        int iter = start + skip; //n+k
        double z2r = 2 * rr[iter];
        double z2i = 2 * ri[iter];
        double anrStep = anr * z2r - ani * z2i;
        double aniStep = anr * z2i + ani * z2r;
        double bnrStep = bnr * z2r - bni * z2i + 1;
        double bniStep = bnr * z2i + bni * z2r;

        double z2l = AdvancedMath.hypotApproximate(z2r, z2i);
        double anlOriginal = AdvancedMath.hypotApproximate(anr, ani);
        double bnlOriginal = AdvancedMath.hypotApproximate(bnr, bni);

        double radius = Math.min(this.radius, (epsilon * z2l - bnlOriginal * dcMax) / anlOriginal);

        return new LightR3A(anrStep, aniStep, bnrStep, bniStep, start, skip + 1, radius);
    }

    public boolean isValid(double dzr, double dzi){
        return AdvancedMath.hypotApproximate(dzr, dzi) < radius;
    }
}
