package kr.merutilm.rff.preset.shader.palette;

import kr.merutilm.rff.settings.PaletteSettings;

public class PaletteRandomShort implements RandomColorPalette {

    @Override
    public String getName() {
        return "Random 16";
    }

    @Override
    public PaletteSettings paletteSettings() {
        return RandomColorPalette.colorSettings(16);
    }

    
}
