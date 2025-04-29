package kr.merutilm.rff.preset.shader.color;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.ColorSettings;

public class ColorWeakContrast implements ShaderPreset.Color {
    @Override
    public String getName() {
        return "Weak Contrast";
    }

    @Override
    public ColorSettings colorSettings() {
        return new ColorSettings(1,0.1,0,0,0,0.1);
    }
}
