package kr.merutilm.rff.preset.shader.palette;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.PaletteSettings;
import kr.merutilm.rff.struct.HexColor;

public class PaletteCinematic implements ShaderPreset.Palette {
    @Override
    public String getName() {
        return "Cinematic";
    }

    @Override
    public PaletteSettings paletteSettings() {
        PaletteSettings.Builder p = new PaletteSettings.Builder();
        for (double i = 0; i < Math.PI * 2; i += Math.PI / 100) {

            int v = (int)(127.5 + 127.5 * Math.sin(i));
            HexColor c = new HexColor(v, v, v, 255);
            c = c.blend(HexColor.ColorBlendMode.NORMAL, HexColor.R_ORANGE, v / 255.0 * 0.2);
            p.add(c);
        }
        p.setIterationInterval(100);
        p.setOffsetRatio(0.7);
        return p.build();
    }
}
