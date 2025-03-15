package kr.merutilm.rff.preset.shader;

import kr.merutilm.rff.struct.HexColor;
import kr.merutilm.rff.settings.BloomSettings;
import kr.merutilm.rff.settings.ColorFilterSettings;
import kr.merutilm.rff.settings.ColorSettings;
import kr.merutilm.rff.settings.FogSettings;
import kr.merutilm.rff.settings.SlopeSettings;
import kr.merutilm.rff.settings.StripeSettings;

public class ShaderDesert implements BasicTheme {

    @Override
    public String getName() {
        return "Desert";    
    }

   
    @Override
    public ColorSettings colorSettings() {
        ColorSettings.Builder p = new ColorSettings.Builder();
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
    @Override
    public SlopeSettings slopeSettings() {
        return new SlopeSettings(300, 0.4, 1, 60, 135);      
    }

    @Override
    public StripeSettings stripeSettings() {
        return new StripeSettings(true, 10, 50, 1, 0);
    }

    @Override
    public ColorFilterSettings colorFilterSettings() {
        return new ColorFilterSettings(1,0.1,-0.4,0.0,0.1);
    }

    @Override
    public FogSettings fogSettings() {
        return new FogSettings(0.3, 0.5);
    }

    @Override
    public BloomSettings bloomSettings() {
        return new BloomSettings(0.05, 0.06, 0, 2);
    }
}
