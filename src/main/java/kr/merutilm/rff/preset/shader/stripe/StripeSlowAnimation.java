package kr.merutilm.rff.preset.shader.stripe;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.selectable.StripeType;
import kr.merutilm.rff.settings.StripeSettings;

public class StripeSlowAnimation implements ShaderPreset.Stripe {
    @Override
    public String getName() {
        return "Slow Animated";
    }

    @Override
    public StripeSettings stripeSettings() {
        return new StripeSettings(StripeType.SINGLE_DIRECTION, 10, 50, 1, 0, 0.5);
    }
}
