package kr.merutilm.rff.preset.shader.slope;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.SlopeSettings;

public class SlopeNoReflection implements ShaderPreset.Slope {
    @Override
    public String getName() {
        return "No Reflection";
    }

    @Override
    public SlopeSettings slopeSettings() {
        return new SlopeSettings(300, 0, 0.8, 60, 135);
    }
}
