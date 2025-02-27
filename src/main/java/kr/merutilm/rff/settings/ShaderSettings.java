package kr.merutilm.rff.settings;

import java.util.function.UnaryOperator;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record ShaderSettings(
    ColorSettings colorSettings,
    StripeSettings stripeSettings,
    SlopeSettings slopeSettings,
    ColorFilterSettings colorFilterSettings,
    FogSettings fogSettings,
    BloomSettings bloomSettings
) implements Struct<ShaderSettings>{
    
    @Override
    public Builder edit() {
        return new Builder()
        .setColorSettings(colorSettings)
        .setStripeSettings(stripeSettings)
        .setSlopeSettings(slopeSettings)
        .setColorFilterSettings(colorFilterSettings)
        .setFogSettings(fogSettings)
        .setBloomSettings(bloomSettings);
    }

    public static final class Builder implements StructBuilder<ShaderSettings>{
        
        private ColorSettings colorSettings;
        private StripeSettings stripeSettings;
        private SlopeSettings slopeSettings;
        private ColorFilterSettings colorFilterSettings;
        private BloomSettings bloomSettings;
        private FogSettings fogSettings;
        

        public Builder setColorSettings(ColorSettings colorSettings) {
            this.colorSettings = colorSettings;
            return this;
        }

        public Builder setStripeSettings(StripeSettings stripeSettings) {
            this.stripeSettings = stripeSettings;
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

        public Builder setColorSettings(UnaryOperator<ColorSettings> changes) {
            this.colorSettings = changes.apply(colorSettings);
            return this;
        }

        public Builder setStripeSettings(UnaryOperator<StripeSettings> changes) {
            this.stripeSettings = changes.apply(stripeSettings);
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
        public ShaderSettings build() {
            return new ShaderSettings(colorSettings, stripeSettings, slopeSettings, colorFilterSettings, fogSettings, bloomSettings);
        }
    }
}    
        