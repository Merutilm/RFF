package kr.merutilm.rff.settings;

import kr.merutilm.rff.selectable.Ease;
import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record AnimationSettings(
    double overZoom,
    boolean showText,
    double mps,
    Ease stripeAnimationEase,
    double stripeAnimationSpeed 
    ) implements Struct<AnimationSettings>{
    
    @Override
    public Builder edit() {
        return new Builder()
        .setOverZoom(overZoom)
        .setShowText(showText)
        .setMps(mps)
        .setStripeAnimationEase(stripeAnimationEase)
        .setStripeAnimationSpeed(stripeAnimationSpeed);
    }

    public static final class Builder implements StructBuilder<AnimationSettings>{
        private double overZoom;
        private boolean showText;
        private double mps;
        private Ease stripeAnimationEase;
        private double stripeAnimationSpeed;

        public Builder setOverZoom(double overZoomValue) {
            this.overZoom = overZoomValue;
            return this;
        }

        public Builder setShowText(boolean showText) {
            this.showText = showText;
            return this;
        }

        public Builder setMps(double mps) {
            this.mps = mps;
            return this;
        }

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
            return new AnimationSettings(overZoom, showText, mps, stripeAnimationEase, stripeAnimationSpeed);
        }

    }
}
