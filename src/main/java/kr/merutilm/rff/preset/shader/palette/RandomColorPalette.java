package kr.merutilm.rff.preset.shader.palette;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.*;
import kr.merutilm.rff.struct.HexColor;

public interface RandomColorPalette extends ShaderPreset.Palette {

    static PaletteSettings colorSettings(int num) {
        PaletteSettings.Builder p = new PaletteSettings.Builder();
        for (double i = 0; i < num; i++) {
            p.add(HexColor.random());
        }
        p.setIterationInterval(num * 50);

        return p.build();
    }

}
