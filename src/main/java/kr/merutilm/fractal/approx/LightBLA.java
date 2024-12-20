package kr.merutilm.fractal.approx;

import kr.merutilm.base.util.AdvancedMath;

/**
 * <h1>Bi-variate Linear Approximation</h1>
 * Bilinear approximation (a.k.a. BLA) can skips many of iterations when zn is Small and Zn is large.
 * On specific case, it is significantly fast than Series Approximation.
 */
public interface LightBLA extends BLA{


    double radius();
    double anr();
    double ani();
    double bnr();
    double bni();
    default boolean isValid(double dzr, double dzi) {
        double r = radius();
        return AdvancedMath.hypotApproximate(dzr, dzi) < r;
    }



}
