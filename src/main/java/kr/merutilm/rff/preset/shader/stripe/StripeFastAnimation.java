package kr.merutilm.rff.preset.shader.stripe;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.selectable.StripeType;
import kr.merutilm.rff.settings.StripeSettings;

public class StripeFastAnimation implements ShaderPreset.Stripe {
    @Override
    public String getName() {
        return "Fast Animated";
    }

    @Override
    public StripeSettings stripeSettings() {
        return new StripeSettings(StripeType.SINGLE_DIRECTION, 100, 500, 1, 0, 5);
    }
}
