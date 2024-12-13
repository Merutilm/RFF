package kr.merutilm.fractal.theme;

import java.util.ArrayList;

import kr.merutilm.base.struct.HexColor;
import kr.merutilm.base.util.AdvancedMath;
import kr.merutilm.fractal.settings.BloomSettings;
import kr.merutilm.fractal.settings.ColorFilterSettings;
import kr.merutilm.fractal.settings.ColorSettings;
import kr.merutilm.fractal.settings.FogSettings;
import kr.merutilm.fractal.settings.SlopeSettings;
import kr.merutilm.fractal.settings.StripeSettings;

public class ThemeRandomizedRainbowShaded implements BasicTheme {
    @Override
    public String getName() {
        return "Randomized Rainbow Shaded";
    }

    @Override
    public ColorSettings colorSettings() {
        ColorSettings.Builder p = new ColorSettings.Builder();

        p.addRainbow();
        p.setIterationInterval(1);


        ColorSettings p1 = p.build();
        p.setColors(new ArrayList<>());
        double r2 = 0.01;

        for (int i = 0; i < p1.colors().length / r2; i++) {

            double t = r2 * i;
            HexColor c2 = p1.getColor(t / p1.colors().length);
            p.add(c2.functionExceptAlpha(HexColor.random(), (e, ta) -> e + ta / 6));
        }

        ColorSettings p2 = p.build();
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

    @Override
    public SlopeSettings slopeSettings() {
        return new SlopeSettings(300, 0.5, 1, 60, 135);
    }

    @Override
    public StripeSettings stripeSettings() {
        return new StripeSettings(true, 10, 50, 1, 0);
    }

    @Override
    public ColorFilterSettings colorFilterSettings() {
        return new ColorFilterSettings(1,0.1,0,0,0.1);
    }

    @Override
    public FogSettings fogSettings() {
        return new FogSettings(0.1, 0.5);
    }

    @Override
    public BloomSettings bloomSettings() {
        return new BloomSettings(0, 0.1, 0, 1);
    }
}
