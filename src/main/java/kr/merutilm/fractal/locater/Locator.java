package kr.merutilm.fractal.locater;

import kr.merutilm.fractal.struct.LWBigComplex;

public interface Locator {

    LWBigComplex center();

    double logZoom();
}
