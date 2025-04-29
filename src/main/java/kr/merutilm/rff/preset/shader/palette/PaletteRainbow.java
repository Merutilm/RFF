package kr.merutilm.rff.preset.shader.palette;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.PaletteSettings;

public class PaletteRainbow implements ShaderPreset.Palette {

    @Override
    public String getName() {
        return "Rainbow";
    }

    @Override
    public PaletteSettings paletteSettings() {
        PaletteSettings.Builder p = new PaletteSettings.Builder();
        p.addRainbow();
        p.setIterationInterval(150);
        return p.build();

    }
}
