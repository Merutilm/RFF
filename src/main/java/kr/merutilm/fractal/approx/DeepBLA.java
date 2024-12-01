package kr.merutilm.fractal.approx;

import kr.merutilm.fractal.struct.DoubleExponent;
import kr.merutilm.fractal.util.DoubleExponentMath;

public interface DeepBLA extends BLA{
    DoubleExponent radius();
    DoubleExponent anr();
    DoubleExponent ani();
    DoubleExponent bnr();
    DoubleExponent bni();
    default boolean isValid(DoubleExponent dzr,  DoubleExponent dzi) {
        DoubleExponent r = radius();
        return DoubleExponentMath.hypotApproximate(dzr, dzi).isSmallerThan(r);
    }
}
