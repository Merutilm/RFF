package kr.merutilm.rff.settings;

import java.util.function.UnaryOperator;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record Settings(
    CalculationSettings calculationSettings,
    ImageSettings imageSettings,
    ShaderSettings shaderSettings,
    VideoSettings videoSettings
) implements Struct<Settings>{

    

    @Override
    public Builder edit() {
        return new Builder()
        .setCalculationSettings(calculationSettings)
        .setImageSettings(imageSettings)
        .setShaderSettings(shaderSettings)
        .setVideoSettings(videoSettings);
    }

    public static final class Builder implements StructBuilder<Settings>{

        private CalculationSettings calculationSettings;
        private ImageSettings imageSettings;
        private ShaderSettings shaderSettings;
        private VideoSettings videoSettings;

        public Builder setCalculationSettings(CalculationSettings calculationSettings) {
            this.calculationSettings = calculationSettings;
            return this;
        }

        public Builder setImageSettings(ImageSettings imageSettings) {
            this.imageSettings = imageSettings;
            return this;
        }

        public Builder setShaderSettings(ShaderSettings shaderSettings) {
            this.shaderSettings = shaderSettings;
            return this;
        }
        
        public Builder setVideoSettings(VideoSettings videoSettings) {
            this.videoSettings = videoSettings;
            return this;
        }
        
        public Builder setCalculationSettings(UnaryOperator<CalculationSettings> changes) {
            this.calculationSettings = changes.apply(calculationSettings);
            return this;
        }

        public Builder setImageSettings(UnaryOperator<ImageSettings> changes) {
            this.imageSettings = changes.apply(imageSettings);
            return this;
        }

        public Builder setShaderSettings(UnaryOperator<ShaderSettings> changes) {
            this.shaderSettings = changes.apply(shaderSettings);
            return this;
        }

        public Builder setVideoSettings(UnaryOperator<VideoSettings> changes) {
            this.videoSettings = changes.apply(videoSettings);
            return this;
        }


        @Override
        public Settings build() {
            return new Settings(calculationSettings, imageSettings, shaderSettings, videoSettings);
        }
    }


}
