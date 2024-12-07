package kr.merutilm.fractal.theme;

import kr.merutilm.base.struct.HexColor;
import kr.merutilm.fractal.settings.BloomSettings;
import kr.merutilm.fractal.settings.ColorFilterSettings;
import kr.merutilm.fractal.settings.ColorSettings;
import kr.merutilm.fractal.settings.FogSettings;
import kr.merutilm.fractal.settings.SlopeSettings;
import kr.merutilm.fractal.settings.StripeSettings;

public class ThemeClassic2 implements BasicTheme {
    
    @Override
    public String getName() {
        return "Classic 2";
    }

    @Override
    public ColorSettings colorSettings() {
        ColorSettings.Builder p = new ColorSettings.Builder();
        for (double i = 0; i < Math.PI * 2; i += Math.PI / 100) {

            double r = 127.5 + 127.5 * Math.sin(i - 2);
            double g = 127.5 + 127.5 * Math.sin(i - 0.6);
            double b = 127.5 + 127.5 * Math.sin(i - 1.3);
            HexColor c = new HexColor((int)r, (int)g, (int)b, 255);
            p.add(c);
        }
        p.setIterationInterval(250);
        return p.build();
    }
    
    @Override
    public SlopeSettings slopeSettings() {
        return new SlopeSettings(0, 0.5, 1, 60, 135);      
    }

    @Override
    public StripeSettings stripeSettings() {
        return new StripeSettings(false, 10, 50, 1, 0);
    }

    @Override
    public ColorFilterSettings colorFilterSettings() {
        return new ColorFilterSettings(1,0,0,0,0);
    }
    
    @Override
    public FogSettings fogSettings() {
        return new FogSettings(0, 0);
    }

    @Override
    public BloomSettings bloomSettings() {
        return new BloomSettings(0, 0.1, 0, 0);
    }

}
