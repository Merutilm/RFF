package kr.merutilm.rff.preset.shader;

import kr.merutilm.rff.preset.Preset;
import kr.merutilm.rff.settings.BloomSettings;
import kr.merutilm.rff.settings.ColorFilterSettings;
import kr.merutilm.rff.settings.ColorSettings;
import kr.merutilm.rff.settings.FogSettings;
import kr.merutilm.rff.settings.ShaderSettings;
import kr.merutilm.rff.settings.SlopeSettings;
import kr.merutilm.rff.settings.StripeSettings;

public interface Shader extends Preset{
    ColorSettings colorSettings();
    StripeSettings stripeSettings();
    SlopeSettings slopeSettings();
    ColorFilterSettings colorFilterSettings();
    FogSettings fogSettings();
    BloomSettings bloomSettings();


    default ShaderSettings createShaderSettings(){
        return new ShaderSettings(colorSettings(), stripeSettings(), slopeSettings(), colorFilterSettings(), fogSettings(), bloomSettings());
    }
}
