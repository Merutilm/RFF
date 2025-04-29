package kr.merutilm.rff.preset.shader.slope;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.SlopeSettings;

public class SlopeMicro implements ShaderPreset.Slope {
    @Override
    public String getName() {
        return "Micro Slope";
    }

    @Override
    public SlopeSettings slopeSettings() {
        return new SlopeSettings(3, 0.5, 1, 60, 135);
    }
}
