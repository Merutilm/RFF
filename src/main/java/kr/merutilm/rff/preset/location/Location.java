package kr.merutilm.rff.preset.location;

import kr.merutilm.rff.preset.Preset;

public interface Location extends Preset{
    String real();
    String imag();
    double logZoom();
    long maxIteration();
}
