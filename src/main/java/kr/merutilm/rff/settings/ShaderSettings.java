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
        .setColorSettings(_ -> colorSettings.edit())
        .setStripeSettings(_ -> stripeSettings.edit())
        .setSlopeSettings(_ -> slopeSettings.edit())
        .setColorFilterSettings(_ -> colorFilterSettings.edit())
        .setFogSettings(_ -> fogSettings.edit())
        .setBloomSettings(_ -> bloomSettings.edit());
    }

    public static final class Builder implements StructBuilder<ShaderSettings>{
        
        private ColorSettings colorSettings;
        private StripeSettings stripeSettings;
        private SlopeSettings slopeSettings;
        private ColorFilterSettings colorFilterSettings;
        private BloomSettings bloomSettings;
        private FogSettings fogSettings;

        public Builder setColorSettings(UnaryOperator<ColorSettings.Builder> changes) {
            this.colorSettings = changes.apply(colorSettings == null ? null : colorSettings.edit()).build();
            return this;
        }

        public Builder setStripeSettings(UnaryOperator<StripeSettings.Builder> changes) {
            this.stripeSettings = changes.apply(stripeSettings == null ? null : stripeSettings.edit()).build();
            return this;
        }

        public Builder setSlopeSettings(UnaryOperator<SlopeSettings.Builder> changes) {
            this.slopeSettings = changes.apply(slopeSettings == null ? null : slopeSettings.edit()).build();
            return this;
        }
    
        public Builder setColorFilterSettings(UnaryOperator<ColorFilterSettings.Builder> changes) {
            this.colorFilterSettings = changes.apply(colorFilterSettings == null ? null : colorFilterSettings.edit()).build();
            return this;
        }

        public Builder setFogSettings(UnaryOperator<FogSettings.Builder> changes) {
            this.fogSettings = changes.apply(fogSettings == null ? null : fogSettings.edit()).build();
            return this;
        }
        
        public Builder setBloomSettings(UnaryOperator<BloomSettings.Builder> changes) {
            this.bloomSettings = changes.apply(bloomSettings == null ? null : bloomSettings.edit()).build();
            return this;
        }
        

        @Override
        public ShaderSettings build() {
            return new ShaderSettings(colorSettings, stripeSettings, slopeSettings, colorFilterSettings, fogSettings, bloomSettings);
        }
    }
}    
        