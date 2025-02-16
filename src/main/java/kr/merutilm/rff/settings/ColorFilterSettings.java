package kr.merutilm.rff.settings;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record ColorFilterSettings(double gamma, double exposure, double saturation, double brightness, double contrast) implements Struct<ColorFilterSettings>{


    @Override
    public Builder edit() {
        return new Builder()
        .setGamma(gamma)
        .setExposure(exposure)
        .setSaturation(saturation)
        .setBrightness(brightness)
        .setContrast(contrast);
    }

    public static final class Builder implements StructBuilder<ColorFilterSettings>{
        private double gamma;
        private double exposure;
        private double saturation;
        private double brightness;
        private double contrast;

        
        
        public Builder setGamma(double gamma) {
            this.gamma = gamma;
            return this;
        }
        public Builder setExposure(double exposure) {
            this.exposure = exposure;
            return this;
        }
        public Builder setSaturation(double saturation) {
            this.saturation = saturation;
            return this;
        }
        public Builder setBrightness(double brightness) {
            this.brightness = brightness;
            return this;
        }
        public Builder setContrast(double contrast) {
            this.contrast = contrast;
            return this;
        }

        @Override
        public ColorFilterSettings build() {
            return new ColorFilterSettings(gamma, exposure, saturation, brightness, contrast);
        }

    }
}
