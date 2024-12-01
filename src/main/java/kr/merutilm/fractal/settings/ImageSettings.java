package kr.merutilm.fractal.settings;

import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;

import kr.merutilm.base.struct.Struct;
import kr.merutilm.base.struct.StructBuilder;



public record ImageSettings(
    double resolutionMultiplier,
    ColorSettings colorSettings,
    SlopeSettings slopeSettings,
    ColorFilterSettings colorFilterSettings,
    FogSettings fogSettings,
    BloomSettings bloomSettings
    
) implements Struct<ImageSettings> {


    @Override
    public Builder edit() {
        return new Builder()
        .setResolutionMultiplier(resolutionMultiplier)
        .setColorSettings(colorSettings)
        .setSlopeSettings(slopeSettings)
        .setColorFilterSettings(colorFilterSettings)
        .setFogSettings(fogSettings)
        .setBloomSettings(bloomSettings);
    }

    public static final class Builder implements StructBuilder<ImageSettings>{
        
        private double resolutionMultiplier;
        private ColorSettings colorSettings;
        private SlopeSettings slopeSettings;
        private ColorFilterSettings colorFilterSettings;
        private BloomSettings bloomSettings;
        private FogSettings fogSettings;
        

    
        public Builder setResolutionMultiplier(double resolutionMultiplier) {
            this.resolutionMultiplier = resolutionMultiplier;
            return this;
        }

        public Builder setColorSettings(ColorSettings colorSettings) {
            this.colorSettings = colorSettings;
            return this;
        }

        public Builder setSlopeSettings(SlopeSettings slopeSettings) {
            this.slopeSettings = slopeSettings;
            return this;
        }
    
        public Builder setColorFilterSettings(ColorFilterSettings colorFilterSettings) {
            this.colorFilterSettings = colorFilterSettings;
            return this;
        }

        public Builder setFogSettings(FogSettings fogSettings) {
            this.fogSettings = fogSettings;
            return this;
        }
        
        public Builder setBloomSettings(BloomSettings bloomSettings) {
            this.bloomSettings = bloomSettings;
            return this;
        }

        public Builder setResolutionMultiplier(DoubleUnaryOperator changes) {
            this.resolutionMultiplier = changes.applyAsDouble(resolutionMultiplier);
            return this;
        }

        public Builder setColorSettings(UnaryOperator<ColorSettings> changes) {
            this.colorSettings = changes.apply(colorSettings);
            return this;
        }

        public Builder setSlopeSettings(UnaryOperator<SlopeSettings> changes) {
            this.slopeSettings = changes.apply(slopeSettings);
            return this;
        }
    
        public Builder setColorFilterSettings(UnaryOperator<ColorFilterSettings> changes) {
            this.colorFilterSettings = changes.apply(colorFilterSettings);
            return this;
        }

        public Builder setFogSettings(UnaryOperator<FogSettings> changes) {
            this.fogSettings = changes.apply(fogSettings);
            return this;
        }
        
        public Builder setBloomSettings(UnaryOperator<BloomSettings> changes) {
            this.bloomSettings = changes.apply(bloomSettings);
            return this;
        }
        

        @Override
        public ImageSettings build() {
            return new ImageSettings(resolutionMultiplier, colorSettings, slopeSettings, colorFilterSettings, fogSettings, bloomSettings);
        }
    }



    
}
