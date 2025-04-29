package kr.merutilm.rff.preset.shader.palette;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.PaletteSettings;
import kr.merutilm.rff.struct.HexColor;

public class PaletteAzure implements ShaderPreset.Palette {
    @Override
    public String getName() {
        return "Azure";
    }

    @Override
    public PaletteSettings paletteSettings() {
        PaletteSettings.Builder p = new PaletteSettings.Builder();
        for (double i = 0; i < Math.PI * 2; i += Math.PI / 100) {

            double r = 127.5 + 127.5 * Math.sin(1.5 * Math.sin(i) - 0.5);
            double g = 127.5 + 127.5 * Math.sin(1.5 * Math.sin(i));
            double b = 127.5 + 127.5 * Math.sin(1.5 * Math.sin(i) + 0.5);
            HexColor c = new HexColor((int)r, (int)g, (int)b, 255);
            p.add(c);
        }
        p.setIterationInterval(300);
        p.setOffsetRatio(0.7);

        return p.build();
    }
}
