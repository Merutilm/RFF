package kr.merutilm.rff.preset.calc;

import kr.merutilm.rff.preset.Preset;
import kr.merutilm.rff.settings.MPASettings;
import kr.merutilm.rff.settings.ReferenceCompressionSettings;

public interface Calculation extends Preset{
    
    MPASettings r3aSettings();
    ReferenceCompressionSettings referenceCompressionSettings();
}
