package kr.merutilm.rff.preset.shader.bloom;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.BloomSettings;

public class BloomHighlight implements ShaderPreset.Bloom {
    @Override
    public String getName() {
        return "Highlight";
    }

    @Override
    public BloomSettings bloomSettings() {
        return new BloomSettings(0.1, 0.05, 0.2, 1);
    }
}
