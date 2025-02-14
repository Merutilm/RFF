package kr.merutilm.fractal.approx;

import kr.merutilm.base.util.AdvancedMath;

public record LightR3A(double anr, double ani, double bnr, double bni, int start, int skip, double radius) {

    public static LightR3A create(int start) {
        return new LightR3A(1, 0, 0, 0, start, 0, 0);
    }

    public LightR3A step(double[] rr, double[] ri, double epsilon, double dcMax) {

        int iter = start + skip; //n+k
        double z2r = 2 * rr[iter]; 
        double z2i = 2 * ri[iter];
        double z2l = AdvancedMath.hypotApproximate(z2r, z2i); // |Z_n|
        double anlOriginal = AdvancedMath.hypotApproximate(anr, ani);
        double bnlOriginal = AdvancedMath.hypotApproximate(bnr, bni);
        double anrStep = anr * z2r - ani * z2i;
        double aniStep = anr * z2i + ani * z2r;
        double bnrStep = bnr * z2r - bni * z2i + 1;
        double bniStep = bnr * z2i + bni * z2r;
        double radius = (epsilon * z2l - bnlOriginal * dcMax) / anlOriginal;

        return new LightR3A(anrStep, aniStep, bnrStep, bniStep, start, skip + 1, radius);
    }
}
