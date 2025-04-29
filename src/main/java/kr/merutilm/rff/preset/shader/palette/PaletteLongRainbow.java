package kr.merutilm.rff.preset.shader.palette;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.PaletteSettings;
import kr.merutilm.rff.struct.HexColor;
import kr.merutilm.rff.util.AdvancedMath;

import java.util.ArrayList;

public class PaletteLongRainbow implements ShaderPreset.Palette {
    @Override
    public String getName() {
        return "Long Rainbow";
    }

    @Override
    public PaletteSettings paletteSettings() {
        PaletteSettings.Builder p = new PaletteSettings.Builder();

        p.addRainbow();
        p.setIterationInterval(1);

        PaletteSettings p1 = p.build();
        p.setColors(new ArrayList<>());
        double r2 = 0.01;

        for (int i = 0; i < p1.colors().length / r2; i++) {

            double t = r2 * i;
            HexColor c2 = p1.getColor(t / p1.colors().length);
            p.add(c2.functionExceptAlpha(HexColor.random(), (e, ta) -> e + ta / 6));
        }

        PaletteSettings p2 = p.build();
        p.setColors(new ArrayList<>());
        double r1 = 0.01;

        for (int i = 0; i < p2.colors().length / r1; i++) {
            double t = r1 * i;

            HexColor c2 = p2.getColor(t / p2.colors().length);
            HexColor c2g = c2.grayScale().functionExceptAlpha(e -> (int) (e / (1 + AdvancedMath.doubleRandom(2))));

            p.add(c2.blend(HexColor.ColorBlendMode.NORMAL, c2g, 0.5 + 0.5 * Math.sin(t % 1 * Math.PI * 50)));
        }




        p.setIterationInterval(2000000);
        p.setOffsetRatio(0.55);
        return p.build();
    }
}
