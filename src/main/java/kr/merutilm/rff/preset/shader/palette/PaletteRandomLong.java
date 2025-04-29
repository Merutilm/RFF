package kr.merutilm.rff.preset.shader.palette;

import kr.merutilm.rff.settings.PaletteSettings;

public class PaletteRandomLong implements RandomColorPalette {

    @Override
    public String getName() {
        return "Random 4096";
    }

    @Override
    public PaletteSettings paletteSettings() {
        return RandomColorPalette.colorSettings(4096);
    }

    
}
