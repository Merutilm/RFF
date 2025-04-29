package kr.merutilm.rff.preset.shader.color;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.ColorSettings;

public class ColorNone implements ShaderPreset.Color {
    @Override
    public String getName() {
        return "None";
    }

    @Override
    public ColorSettings colorSettings() {
        return new ColorSettings(1,0, 0, 0, 0, 0);
    }
}
