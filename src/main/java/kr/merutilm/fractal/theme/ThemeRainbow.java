package kr.merutilm.fractal.theme;

import kr.merutilm.fractal.settings.*;

public class ThemeRainbow implements BasicTheme{

    @Override
    public String getName() {
        return "Rainbow";
    }

    @Override
    public ColorSettings colorSettings() {
        ColorSettings.Builder p = new ColorSettings.Builder();
        p.addRainbow();
        p.setIterationInterval(150);
        return p.build();
      
    }

    @Override
    public SlopeSettings slopeSettings() {
        return new SlopeSettings(300, 0.3, 1, 60, 135);      
    }

    @Override
    public StripeSettings stripeSettings() {
        return new StripeSettings(true, 10, 50, 1, 0);
    }

    @Override
    public ColorFilterSettings colorFilterSettings() {
        return new ColorFilterSettings(1,0.3,-0.3,0,0);
    }

    @Override
    public FogSettings fogSettings() {
        return new FogSettings(0.3, 0.05);
    }

    @Override
    public BloomSettings bloomSettings() {
        return new BloomSettings(0.03, 0.125, 0, 0.8);
    }
}
