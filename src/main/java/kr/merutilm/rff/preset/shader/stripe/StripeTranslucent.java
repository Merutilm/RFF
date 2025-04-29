package kr.merutilm.rff.preset.shader.stripe;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.selectable.StripeType;
import kr.merutilm.rff.settings.StripeSettings;

public class StripeTranslucent implements ShaderPreset.Stripe {
    @Override
    public String getName() {
        return "Translucent";
    }

    @Override
    public StripeSettings stripeSettings() {
        return new StripeSettings(StripeType.SMOOTH_SQUARED, 20, 100, 0.5, 0, 1);
    }
}
