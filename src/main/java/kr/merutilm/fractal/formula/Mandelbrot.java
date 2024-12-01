package kr.merutilm.fractal.formula;

import kr.merutilm.fractal.struct.LWBigComplex;

public class Mandelbrot implements Formula {
    @Override
    public LWBigComplex apply(LWBigComplex current, LWBigComplex original, int precision) {
        return current.square(precision).add(original, precision);
    }
}
