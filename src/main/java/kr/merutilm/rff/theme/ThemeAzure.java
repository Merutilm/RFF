package kr.merutilm.rff.theme;

import kr.merutilm.rff.struct.HexColor;
import kr.merutilm.rff.settings.BloomSettings;
import kr.merutilm.rff.settings.ColorFilterSettings;
import kr.merutilm.rff.settings.ColorSettings;
import kr.merutilm.rff.settings.FogSettings;
import kr.merutilm.rff.settings.SlopeSettings;
import kr.merutilm.rff.settings.StripeSettings;

public class ThemeAzure implements BasicTheme{

    @Override
    public String getName() {
        return "Azure";
    }

    @Override
    public ColorSettings colorSettings() {
        ColorSettings.Builder p = new ColorSettings.Builder();
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

    @Override
    public SlopeSettings slopeSettings() {
        return new SlopeSettings(200, 0.4, 1, 60, 135);      
    }

    @Override
    public StripeSettings stripeSettings() {
        return new StripeSettings(true, 4, 40, 0.4, 0);
    }

    @Override
    public ColorFilterSettings colorFilterSettings() {
        return new ColorFilterSettings(1,0.2,0.5,0.0,0);
    }

    @Override
    public FogSettings fogSettings() {
        return new FogSettings(0.2, 0.5);
    }

    @Override
    public BloomSettings bloomSettings() {
        return new BloomSettings(0.03, 0.125, 0, 1);
    }
    
}
