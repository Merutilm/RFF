package kr.merutilm.rff.locater;

import kr.merutilm.rff.precision.LWBigComplex;

public interface Locator {

    LWBigComplex center();

    double logZoom();
}
