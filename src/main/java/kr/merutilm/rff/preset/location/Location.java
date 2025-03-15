package kr.merutilm.rff.preset.location;

import kr.merutilm.rff.preset.Preset;
import kr.merutilm.rff.struct.LWBigComplex;

public interface Location extends Preset{
    String real();
    String imag();
    double logZoom();
    long maxIteration();

    default LWBigComplex createCenter(){
        return LWBigComplex.valueOf(real(), imag());
    }
}
