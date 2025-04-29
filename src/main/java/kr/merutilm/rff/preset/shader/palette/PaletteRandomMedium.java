package kr.merutilm.rff.preset.shader.palette;

import kr.merutilm.rff.settings.*;

public class PaletteRandomMedium implements RandomColorPalette {

    @Override
    public String getName() {
        return "Random 256";
    }

    @Override
    public PaletteSettings paletteSettings() {
        return RandomColorPalette.colorSettings(256);
    }

    
}
