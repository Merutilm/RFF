package kr.merutilm.rff.settings;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record BloomSettings(double threshold, double radius, double softness, double intensity) implements Struct<BloomSettings>{


    @Override
    public Builder edit() {
        return new Builder()
        .setThreshold(threshold)
        .setRadius(radius)
        .setSoftness(softness)
        .setIntensity(intensity);
    }

    public static final class Builder implements StructBuilder<BloomSettings>{
        private double threshold;
        private double radius;
        private double softness;
        private double intensity;

        public Builder setThreshold(double threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder setRadius(double radius) {
            this.radius = radius;
            return this;
        }

        public Builder setSoftness(double softness) {
            this.softness = softness;
            return this;
        }


        public Builder setIntensity(double intensity) {
            this.intensity = intensity;
            return this;
        }

        @Override
        public BloomSettings build() {
            return new BloomSettings(threshold, radius, softness, intensity);
        }

    }
}
