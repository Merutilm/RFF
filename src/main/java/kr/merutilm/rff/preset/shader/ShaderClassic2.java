package kr.merutilm.rff.preset.shader;

import kr.merutilm.rff.settings.BloomSettings;
import kr.merutilm.rff.settings.ColorFilterSettings;
import kr.merutilm.rff.settings.FogSettings;
import kr.merutilm.rff.settings.SlopeSettings;
import kr.merutilm.rff.settings.StripeSettings;

public class ShaderClassic2 implements Classic2Shader {
    
    @Override
    public String getName() {
        return "Classic 2";
    }


    @Override
    public StripeSettings stripeSettings() {
        return new StripeSettings(false, 10, 50, 1, 0, 0.5);
    }
    
    @Override
    public SlopeSettings slopeSettings() {
        return new SlopeSettings(0, 0.5, 1, 60, 135);      
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
        return new BloomSettings(0, 0.05, 0, 0);
    }

}
