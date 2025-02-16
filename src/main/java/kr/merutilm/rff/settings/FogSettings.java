package kr.merutilm.rff.settings;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record FogSettings(double radius, double opacity) implements Struct<FogSettings>{


    @Override
    public Builder edit() {
        return new Builder()
        .setRadius(radius)
        .setOpacity(opacity);
    }

    public static final class Builder implements StructBuilder<FogSettings>{
        private double radius;
        private double opacity;

        public Builder setRadius(double radius) {
            this.radius = radius;
            return this;
        }


        public Builder setOpacity(double opacity) {
            this.opacity = opacity;
            return this;
        }

        @Override
        public FogSettings build() {
            return new FogSettings(radius, opacity);
        }

    }
}
