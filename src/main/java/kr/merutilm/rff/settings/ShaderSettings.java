package kr.merutilm.rff.settings;

import java.util.function.UnaryOperator;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record ShaderSettings(
    PaletteSettings paletteSettings,
    StripeSettings stripeSettings,
    SlopeSettings slopeSettings,
    ColorSettings colorSettings,
    FogSettings fogSettings,
    BloomSettings bloomSettings
) implements Struct<ShaderSettings>{
    
    @Override
    public Builder edit() {
        return new Builder()
        .setPaletteSettings(paletteSettings)
        .setStripeSettings(stripeSettings)
        .setSlopeSettings(slopeSettings)
        .setColorSettings(colorSettings)
        .setFogSettings(fogSettings)
        .setBloomSettings(bloomSettings);
    }

    public static final class Builder implements StructBuilder<ShaderSettings>{
        
        private PaletteSettings paletteSettings;
        private StripeSettings stripeSettings;
        private SlopeSettings slopeSettings;
        private ColorSettings colorSettings;
        private BloomSettings bloomSettings;
        private FogSettings fogSettings;

        public Builder setPaletteSettings(PaletteSettings paletteSettings){
            this.paletteSettings = paletteSettings;
            return this;
        }

        public Builder setPaletteSettings(UnaryOperator<PaletteSettings.Builder> changes) {
            this.paletteSettings = changes.apply(paletteSettings == null ? null : paletteSettings.edit()).build();
            return this;
        }
        
        public Builder setStripeSettings(StripeSettings stripeSettings){
            this.stripeSettings = stripeSettings;
            return this;
        }

        public Builder setStripeSettings(UnaryOperator<StripeSettings.Builder> changes) {
            this.stripeSettings = changes.apply(stripeSettings == null ? null : stripeSettings.edit()).build();
            return this;
        }

        public Builder setSlopeSettings(SlopeSettings slopeSettings){
            this.slopeSettings = slopeSettings;
            return this;
        }

        public Builder setSlopeSettings(UnaryOperator<SlopeSettings.Builder> changes) {
            this.slopeSettings = changes.apply(slopeSettings == null ? null : slopeSettings.edit()).build();
            return this;
        }

        public Builder setColorSettings(ColorSettings colorSettings){
            this.colorSettings = colorSettings;
            return this;
        }
    
        public Builder setColorSettings(UnaryOperator<ColorSettings.Builder> changes) {
            this.colorSettings = changes.apply(colorSettings == null ? null : colorSettings.edit()).build();
            return this;
        }

        public Builder setFogSettings(FogSettings fogSettings){
            this.fogSettings = fogSettings;
            return this;
        }

        public Builder setFogSettings(UnaryOperator<FogSettings.Builder> changes) {
            this.fogSettings = changes.apply(fogSettings == null ? null : fogSettings.edit()).build();
            return this;
        }

        public Builder setBloomSettings(BloomSettings bloomSettings){
            this.bloomSettings = bloomSettings;
            return this;
        }
        
        public Builder setBloomSettings(UnaryOperator<BloomSettings.Builder> changes) {
            this.bloomSettings = changes.apply(bloomSettings == null ? null : bloomSettings.edit()).build();
            return this;
        }
        

        @Override
        public ShaderSettings build() {
            return new ShaderSettings(paletteSettings, stripeSettings, slopeSettings, colorSettings, fogSettings, bloomSettings);
        }
    }
}    
        