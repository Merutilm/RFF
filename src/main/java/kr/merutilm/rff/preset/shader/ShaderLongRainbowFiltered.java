package kr.merutilm.rff.preset.shader;

import kr.merutilm.rff.settings.BloomSettings;
import kr.merutilm.rff.settings.ColorFilterSettings;
import kr.merutilm.rff.settings.FogSettings;
import kr.merutilm.rff.settings.SlopeSettings;
import kr.merutilm.rff.settings.StripeSettings;

public class ShaderLongRainbowFiltered implements LongRainbowShader {

    @Override
    public String getName(){
        return "Filtered Long Rainbow";
    }

    @Override
    public StripeSettings stripeSettings() {
        return new StripeSettings(true, 10, 50, 1, 0, 0.5);
    }
    
    @Override
    public SlopeSettings slopeSettings() {
        return new SlopeSettings(300, 0.5, 1, 60, 135);
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
