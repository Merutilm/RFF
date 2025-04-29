package kr.merutilm.rff.preset.shader.slope;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.SlopeSettings;

public class SlopeNano implements ShaderPreset.Slope {
    @Override
    public String getName() {
        return "Nano Slope";
    }

    @Override
    public SlopeSettings slopeSettings() {
        return new SlopeSettings(0.003, 0.5, 1, 60, 135);
    }
}
