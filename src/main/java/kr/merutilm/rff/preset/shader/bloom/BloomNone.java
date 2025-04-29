package kr.merutilm.rff.preset.shader.bloom;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.BloomSettings;

public class BloomNone implements ShaderPreset.Bloom {
    @Override
    public String getName() {
        return "None";
    }

    @Override
    public BloomSettings bloomSettings() {
        return new BloomSettings(0, 0, 0, 0);
    }
}
