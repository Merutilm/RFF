package kr.merutilm.rff.preset.shader.bloom;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.BloomSettings;

public class BloomStrong implements ShaderPreset.Bloom {
    @Override
    public String getName() {
        return "Strong";
    }

    @Override
    public BloomSettings bloomSettings() {
        return new BloomSettings(0, 0.1, 0, 1.5);
    }
}
