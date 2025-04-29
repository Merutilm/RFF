package kr.merutilm.rff.preset.shader.color;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.ColorSettings;

public class ColorVivid implements ShaderPreset.Color {
    @Override
    public String getName() {
        return "Vivid";
    }

    @Override
    public ColorSettings colorSettings() {
        return new ColorSettings(1,0.2, 0,0.5,0.0,0.05);
    }
}
