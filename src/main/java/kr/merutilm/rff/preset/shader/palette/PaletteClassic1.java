package kr.merutilm.rff.preset.shader.palette;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.PaletteSettings;
import kr.merutilm.rff.struct.HexColor;

public class PaletteClassic1 implements ShaderPreset.Palette {
    @Override
    public String getName() {
        return "Classic 1";
    }

    @Override
    public PaletteSettings paletteSettings() {
        PaletteSettings.Builder p = new PaletteSettings.Builder();
        for (double i = 0; i < Math.PI * 2; i += Math.PI / 100) {

            double r = 127.5 + 127.5 * Math.sin(i - 2);
            double g = 127.5 + 127.5 * Math.sin(i - 1.3);
            double b = 127.5 + 127.5 * Math.sin(i - 0.6);
            HexColor c = new HexColor((int)r, (int)g, (int)b, 255);
            p.add(c);
        }
        p.setIterationInterval(250);
        return p.build();
    }
}
