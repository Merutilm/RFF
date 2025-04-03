package kr.merutilm.rff.preset.shader;

import kr.merutilm.rff.settings.*;

public class ShaderRainbow implements Shader{

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
    public StripeSettings stripeSettings() {
        return new StripeSettings(false, 10, 50, 1, 0, 0.5);
    }

    @Override
    public SlopeSettings slopeSettings() {
        return new SlopeSettings(300, 0.3, 1, 60, 135);      
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
