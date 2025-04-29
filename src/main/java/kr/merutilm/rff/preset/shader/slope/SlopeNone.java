package kr.merutilm.rff.preset.shader.slope;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.SlopeSettings;

public class SlopeNone implements ShaderPreset.Slope {
    @Override
    public String getName() {
        return "None";
    }

    @Override
    public SlopeSettings slopeSettings() {
        return new SlopeSettings(0, 0.5, 1, 60, 135);
    }
}
