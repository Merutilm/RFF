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
        .setCalculationSettings(_ -> calculationSettings.edit())
        .setImageSettings(_ -> imageSettings.edit())
        .setShaderSettings(_ -> shaderSettings.edit())
        .setVideoSettings(_ -> videoSettings.edit());
    }

    public static final class Builder implements StructBuilder<Settings>{

        private CalculationSettings calculationSettings;
        private ImageSettings imageSettings;
        private ShaderSettings shaderSettings;
        private VideoSettings videoSettings;

        
        public Builder setCalculationSettings(UnaryOperator<CalculationSettings.Builder> changes) {
            this.calculationSettings = changes.apply(calculationSettings == null ? null : calculationSettings.edit()).build();
            return this;
        }

        public Builder setImageSettings(UnaryOperator<ImageSettings.Builder> changes) {
            this.imageSettings = changes.apply(imageSettings == null ? null : imageSettings.edit()).build();
            return this;
        }

        public Builder setShaderSettings(UnaryOperator<ShaderSettings.Builder> changes) {
            this.shaderSettings = changes.apply(shaderSettings == null ? null : shaderSettings.edit()).build();
            return this;
        }

        public Builder setVideoSettings(UnaryOperator<VideoSettings.Builder> changes) {
            this.videoSettings = changes.apply(videoSettings == null ? null : videoSettings.edit()).build();
            return this;
        }


        @Override
        public Settings build() {
            return new Settings(calculationSettings, imageSettings, shaderSettings, videoSettings);
        }
    }


}
