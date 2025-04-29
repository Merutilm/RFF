package kr.merutilm.rff.preset.shader.stripe;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.selectable.StripeType;
import kr.merutilm.rff.settings.StripeSettings;

public class StripeSmooth implements ShaderPreset.Stripe {
    @Override
    public String getName() {
        return "Smooth";
    }

    @Override
    public StripeSettings stripeSettings() {
        return new StripeSettings(StripeType.SMOOTH_SQUARED, 1, 1, 1, 0, 0.25);
    }
}
