package kr.merutilm.rff.preset.shader.palette;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.PaletteSettings;
import kr.merutilm.rff.struct.HexColor;

public class PaletteDesert implements ShaderPreset.Palette {
    @Override
    public String getName() {
        return "Desert";
    }

    @Override
    public PaletteSettings paletteSettings() {
        PaletteSettings.Builder p = new PaletteSettings.Builder();
        for (double i = 0; i < Math.PI * 2; i += Math.PI / 100) {

            double r = 127.5 + 127.5 * Math.sin(0.5);
            double g = 127.5 + 127.5 * Math.sin(0);
            double b = 127.5 + 127.5 * Math.sin(-0.5);
            HexColor c = HexColor.ratioDivide(HexColor.get((int)r, (int)g, (int)b), HexColor.WHITE, -0.3 + 0.3 * Math.cos(i));
            p.add(c);
        }
        p.setIterationInterval(500);
        return p.build();
    }
}
