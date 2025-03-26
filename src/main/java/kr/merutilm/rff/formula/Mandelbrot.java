package kr.merutilm.rff.formula;

import kr.merutilm.rff.precision.LWBigComplex;

public class Mandelbrot implements Formula {
    @Override
    public LWBigComplex apply(LWBigComplex current, LWBigComplex original, int precision) {
        return current.square(precision).add(original, precision);
    }
}
