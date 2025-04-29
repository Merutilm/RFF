package kr.merutilm.rff.preset.shader.color;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.ColorSettings;

public class ColorDull implements ShaderPreset.Color {
    @Override
    public String getName() {
        return "Dull";
    }

    @Override
    public ColorSettings colorSettings() {
        return new ColorSettings(1,0.05, 0,-0.3,0.0,0.05);
    }
}
