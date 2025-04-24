package kr.merutilm.rff.settings;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record StripeSettings(
        boolean use,
        double firstInterval,
        double secondInterval,
        double opacity,
        double offset,
        double animationSpeed
) implements Struct<StripeSettings> {

    @Override
    public Builder edit() {
        return new Builder()
                .setUse(use)
                .setFirstInterval(firstInterval)
                .setSecondInterval(secondInterval)
                .setOpacity(opacity)
                .setOffset(offset)
                .setAnimationSpeed(animationSpeed);
    }

    public static final class Builder implements StructBuilder<StripeSettings> {
        private boolean use;
        private double firstInterval;
        private double secondInterval;
        private double opacity;
        private double offset;
        private double animationSpeed;

        public Builder setUse(boolean use) {
            this.use = use;
            return this;
        }

        public Builder setFirstInterval(double firstInterval) {
            this.firstInterval = firstInterval;
            return this;
        }

        public Builder setSecondInterval(double secondInterval) {
            this.secondInterval = secondInterval;
            return this;
        }

        public Builder setOpacity(double opacity) {
            this.opacity = opacity;
            return this;
        }

        public Builder setOffset(double offset) {
            this.offset = offset;
            return this;
        }

        public Builder setAnimationSpeed(double animationSpeed) {
            this.animationSpeed = animationSpeed;
            return this;
        }

        @Override
        public StripeSettings build() {
            return new StripeSettings(use, firstInterval, secondInterval, opacity, offset, animationSpeed);
        }
    }
}
