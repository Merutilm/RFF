package kr.merutilm.rff.settings;

import java.util.function.UnaryOperator;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record Settings(
    CalculationSettings calculationSettings,
    RenderSettings renderSettings,
    ShaderSettings shaderSettings,
    VideoSettings videoSettings
) implements Struct<Settings>{



    @Override
    public Builder edit() {
        return new Builder()
        .setCalculationSettings(calculationSettings)
        .setRenderSettings(renderSettings)
        .setShaderSettings(shaderSettings)
        .setVideoSettings(videoSettings);
    }

    public static final class Builder implements StructBuilder<Settings>{

        private CalculationSettings calculationSettings;
        private RenderSettings renderSettings;
        private PaletteSettings paletteSettings;
        private ShaderSettings shaderSettings;
        private VideoSettings videoSettings;


        public Builder setCalculationSettings(CalculationSettings calculationSettings){
            this.calculationSettings = calculationSettings;
            return this;
        }

        public Builder setCalculationSettings(UnaryOperator<CalculationSettings.Builder> changes) {
            this.calculationSettings = changes.apply(calculationSettings == null ? null : calculationSettings.edit()).build();
            return this;
        }

        public Builder setRenderSettings(RenderSettings renderSettings){
            this.renderSettings = renderSettings;
            return this;
        }

        public Builder setRenderSettings(UnaryOperator<RenderSettings.Builder> changes) {
            this.renderSettings = changes.apply(renderSettings == null ? null : renderSettings.edit()).build();
            return this;
        }

        public Builder setShaderSettings(ShaderSettings shaderSettings){
            this.shaderSettings = shaderSettings;
            return this;
        }

        public Builder setShaderSettings(UnaryOperator<ShaderSettings.Builder> changes) {
            this.shaderSettings = changes.apply(shaderSettings == null ? null : shaderSettings.edit()).build();
            return this;
        }

        public Builder setVideoSettings(VideoSettings videoSettings){
            this.videoSettings = videoSettings;
            return this;
        }

        public Builder setVideoSettings(UnaryOperator<VideoSettings.Builder> changes) {
            this.videoSettings = changes.apply(videoSettings == null ? null : videoSettings.edit()).build();
            return this;
        }


        @Override
        public Settings build() {
            return new Settings(calculationSettings, renderSettings, shaderSettings, videoSettings);
        }
    }


}
