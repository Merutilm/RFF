package kr.merutilm.rff.preset.shader;

import kr.merutilm.rff.settings.*;

public class ShaderLongRainbowClassic implements LongRainbowTheme {

    @Override
    public String getName(){
        return "Long Rainbow";
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
        return new ColorFilterSettings(1,0.1,0,0,0.1);
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
