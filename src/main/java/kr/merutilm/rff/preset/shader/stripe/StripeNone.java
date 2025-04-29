package kr.merutilm.rff.preset.shader.stripe;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.selectable.StripeType;
import kr.merutilm.rff.settings.StripeSettings;

public class StripeNone implements ShaderPreset.Stripe {
    @Override
    public String getName() {
        return "None";
    }

    @Override
    public StripeSettings stripeSettings() {
        return new StripeSettings(StripeType.NONE, 10, 50, 1, 0, 0);
    }
}
