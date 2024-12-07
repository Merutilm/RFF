package kr.merutilm.fractal.theme;

import kr.merutilm.base.struct.HexColor;
import kr.merutilm.fractal.settings.BloomSettings;
import kr.merutilm.fractal.settings.ColorFilterSettings;
import kr.merutilm.fractal.settings.ColorSettings;
import kr.merutilm.fractal.settings.FogSettings;
import kr.merutilm.fractal.settings.SlopeSettings;
import kr.merutilm.fractal.settings.StripeSettings;

public class ThemeCinematic implements BasicTheme {

    @Override
    public String getName() {
        return "Cinematic";
    }

    @Override
    public ColorSettings colorSettings() {
        ColorSettings.Builder p = new ColorSettings.Builder();
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

    @Override
    public SlopeSettings slopeSettings() {
        return new SlopeSettings(300, 0.4, 1, 60, 135);      
    }
    
    @Override
    public StripeSettings stripeSettings() {
        return new StripeSettings(false, 10, 50, 1, 0);
    }

    @Override
    public ColorFilterSettings colorFilterSettings() {
        return new ColorFilterSettings(1,0,1,0,0);
    }
    
    @Override
    public FogSettings fogSettings() {
        return new FogSettings(0.03, 1);
    }

    @Override
    public BloomSettings bloomSettings() {
        return new BloomSettings(0.03, 0.125, 0, 1);
    }
}
