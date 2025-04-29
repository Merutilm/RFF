package kr.merutilm.rff.preset.shader.palette;

import kr.merutilm.rff.settings.PaletteSettings;

public class PaletteRandomSemiInf implements RandomColorPalette {

    @Override
    public String getName() {
        return "Random 65536";
    }

    @Override
    public PaletteSettings paletteSettings() {
        return RandomColorPalette.colorSettings(65536);
    }

    
}
