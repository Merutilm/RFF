package kr.merutilm.rff.formula;

import kr.merutilm.rff.struct.LWBigComplex;

@FunctionalInterface
public interface Formula {

    LWBigComplex apply(LWBigComplex current, LWBigComplex original, int precision);
}
