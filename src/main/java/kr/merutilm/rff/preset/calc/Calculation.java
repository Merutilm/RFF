package kr.merutilm.rff.preset.calc;

import kr.merutilm.rff.preset.Preset;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.settings.ReferenceCompressionSettings;

public interface Calculation extends Preset{
    
    R3ASettings r3aSettings();
    ReferenceCompressionSettings referenceCompressionSettings();
}
