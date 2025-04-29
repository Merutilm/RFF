package kr.merutilm.rff.preset.shader.slope;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.SlopeSettings;

public class SlopeReversed implements ShaderPreset.Slope {
    @Override
    public String getName() {
        return "Reversed";
    }

    @Override
    public SlopeSettings slopeSettings() {
        return new SlopeSettings(-300, 0.5, 1, 60, 135);
    }
}
