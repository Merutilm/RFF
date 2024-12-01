package kr.merutilm.fractal.settings;

import kr.merutilm.base.struct.Struct;
import kr.merutilm.base.struct.StructBuilder;

public record SlopeSettings(double depth, double reflectionRatio, double opacity, double zenith, double azimuth) implements Struct<SlopeSettings>{

    @Override
    public Builder edit() {
        return new Builder()
        .setDepth(depth)
        .setReflectionRatio(reflectionRatio)
        .setOpacity(opacity)
        .setZenith(zenith)
        .setAzimuth(azimuth);
    }
    public static final class Builder implements StructBuilder<SlopeSettings> {
        private double depth;
        private double reflectionRatio;
        private double opacity;
        private double zenith;
        private double azimuth;

    
        public Builder setDepth(double depth) {
            this.depth = depth;
            return this;
        }

        public Builder setReflectionRatio(double reflectionRatio) {
            this.reflectionRatio = reflectionRatio;
            return this;
        }

        public Builder setOpacity(double opacity) {
            this.opacity = opacity;
            return this;
        }

        public Builder setZenith(double zenith) {
            this.zenith = zenith;
            return this;
        }

        public Builder setAzimuth(double azimuth) {
            this.azimuth = azimuth;
            return this;
        }
        
        @Override
        public SlopeSettings build() {
            return new SlopeSettings(depth, reflectionRatio, opacity, zenith, azimuth);
        }
    }
}
