package kr.merutilm.rff.preset.shader;

import kr.merutilm.rff.settings.*;

public class ShaderClassicFiltered2 implements Classic2Shader{

    @Override
    public String getName() {
        return "Filtered Classic 2";
    }

    @Override
    public StripeSettings stripeSettings() {
        return new StripeSettings(true, 80, 640, 1, 0, 2);
    }

    @Override
    public SlopeSettings slopeSettings() {
        return new SlopeSettings(120, 0.5, 1, 60, 135);
    }

    @Override
    public ColorFilterSettings colorFilterSettings() {
        return new ColorFilterSettings(1,0.2,0,0,0.1);
    }
    
    @Override
    public FogSettings fogSettings() {
        return new FogSettings(0.1, 0.4);
    }

    @Override
    public BloomSettings bloomSettings() {
        return new BloomSettings(0, 0.05, 0, 1);
    }

}
