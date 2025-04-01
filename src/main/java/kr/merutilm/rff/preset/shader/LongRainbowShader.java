package kr.merutilm.rff.preset.shader;

import kr.merutilm.rff.settings.ColorSettings;
import kr.merutilm.rff.struct.HexColor;
import kr.merutilm.rff.util.AdvancedMath;

import java.util.ArrayList;

public interface LongRainbowShader extends Shader{


    @Override
    default ColorSettings colorSettings(){
        return initColorSettings();
    };

    static ColorSettings initColorSettings() {
        ColorSettings.Builder p = new ColorSettings.Builder();

        p.addRainbow();
        p.setIterationInterval(1);

        ColorSettings p1 = p.build();
        p.setColors(new ArrayList<>());
        double r2 = 0.011;

        for (int i = 0; i < p1.colors().length / r2; i++) {

            double t = r2 * i;
            HexColor c2 = p1.getColor(t / p1.colors().length);
            p.add(c2.functionExceptAlpha(HexColor.random(), (e, ta) -> e + ta / 6));
        }

        ColorSettings p2 = p.build();
        p.setColors(new ArrayList<>());
        double r1 = 0.02;

        for (int i = 0; i < p2.colors().length / r1; i++) {
            double t = r1 * i;

            HexColor c2 = p2.getColor(t / p2.colors().length);
            HexColor c2g = c2.grayScale().functionExceptAlpha(e -> (int) (e / (1 + AdvancedMath.doubleRandom(2))));

            p.add(c2.blend(HexColor.ColorBlendMode.NORMAL, c2g, 0.5 + 0.5 * Math.sin(t % 1 * Math.PI * 50)));
        }




        p.setIterationInterval(1000000);
        p.setOffsetRatio(0.55);
        return p.build();
    }
}
