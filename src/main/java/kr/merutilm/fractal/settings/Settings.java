package kr.merutilm.fractal.settings;

import java.util.function.UnaryOperator;

import kr.merutilm.base.struct.Struct;
import kr.merutilm.base.struct.StructBuilder;

public record Settings(
    CalculationSettings calculationSettings,
    ImageSettings imageSettings
) implements Struct<Settings>{

    

    @Override
    public Builder edit() {
        return new Builder()
        .setCalculationSettings(calculationSettings)
        .setImageSettings(imageSettings);
    }

    public static final class Builder implements StructBuilder<Settings>{

        private CalculationSettings calculationSettings;
        private ImageSettings imageSettings;

        public Builder setCalculationSettings(CalculationSettings calculationSettings) {
            this.calculationSettings = calculationSettings;
            return this;
        }

        public Builder setImageSettings(ImageSettings imageSettings) {
            this.imageSettings = imageSettings;
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

        @Override
        public Settings build() {
            return new Settings(calculationSettings, imageSettings);
        }
    }


}
