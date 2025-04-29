package kr.merutilm.rff.preset.shader.bloom;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.BloomSettings;

public class BloomItself implements ShaderPreset.Bloom {
    @Override
    public String getName() {
        return "Itself";
    }

    @Override
    public BloomSettings bloomSettings() {
        return new BloomSettings(0, 0, 0, 1);
    }
}
