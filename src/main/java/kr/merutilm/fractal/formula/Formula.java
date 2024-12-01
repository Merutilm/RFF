package kr.merutilm.fractal.formula;

import kr.merutilm.fractal.struct.LWBigComplex;

@FunctionalInterface
public interface Formula {

    LWBigComplex apply(LWBigComplex current, LWBigComplex original, int precision);
}
