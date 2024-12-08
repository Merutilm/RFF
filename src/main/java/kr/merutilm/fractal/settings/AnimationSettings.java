package kr.merutilm.fractal.settings;

import kr.merutilm.base.selectable.Ease;
import kr.merutilm.base.struct.Struct;
import kr.merutilm.base.struct.StructBuilder;

public record AnimationSettings(
    Ease stripeAnimationEase,
    double stripeAnimationSpeed 
    ) implements Struct<AnimationSettings>{
    
    @Override
    public Builder edit() {
        return new Builder()
        .setStripeAnimationEase(stripeAnimationEase)
        .setStripeAnimationSpeed(stripeAnimationSpeed);
    }

    public static final class Builder implements StructBuilder<AnimationSettings>{
        private Ease stripeAnimationEase;
        private double stripeAnimationSpeed;

        public Builder setStripeAnimationEase(Ease stripeAnimationEase) {
            this.stripeAnimationEase = stripeAnimationEase;
            return this;
        }
        public Builder setStripeAnimationSpeed(double stripeAnimationSpeed) {
            this.stripeAnimationSpeed = stripeAnimationSpeed;
            return this;
        }
          

        @Override
        public AnimationSettings build() {
            return new AnimationSettings(stripeAnimationEase, stripeAnimationSpeed);
        }

    }
}
