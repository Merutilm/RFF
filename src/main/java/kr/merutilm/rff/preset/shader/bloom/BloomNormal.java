package kr.merutilm.rff.preset.shader.bloom;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.BloomSettings;

public class BloomNormal implements ShaderPreset.Bloom {
    @Override
    public String getName() {
        return "Normal";
    }

    @Override
    public BloomSettings bloomSettings() {
        return new BloomSettings(0, 0.1, 0, 1);
    }
}
