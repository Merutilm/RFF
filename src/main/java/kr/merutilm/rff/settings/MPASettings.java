package kr.merutilm.rff.settings;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record MPASettings(int minSkipReference,
                          int maxMultiplierBetweenLevel,
                          double epsilonPower,
                          MPASelectionMethod mpaSelectionMethod,
                          MPACompressionMethod mpaCompressionMethod) implements Struct<MPASettings> {

    @Override
    public Builder edit() {
        return new Builder()
                .setMinSkipReference(minSkipReference)
                .setMaxMultiplierBetweenLevel(maxMultiplierBetweenLevel)
                .setEpsilonPower(epsilonPower)
                .setR3ASelectionMethod(mpaSelectionMethod)
                .setR3ACompressionMethod(mpaCompressionMethod);
    }


    public static final class Builder implements StructBuilder<MPASettings> {
        private int minSkipReference;
        private int maxMultiplierBetweenLevel;
        private double epsilonPower;
        private MPASelectionMethod mpaSelectionMethod;
        private MPACompressionMethod mpaCompressionMethod;

        public Builder setMinSkipReference(int minSkipReference) {
            this.minSkipReference = minSkipReference;
            return this;
        }

        public Builder setMaxMultiplierBetweenLevel(int maxMultiplierBetweenLevel) {
            this.maxMultiplierBetweenLevel = maxMultiplierBetweenLevel;
            return this;
        }

        public Builder setEpsilonPower(double epsilonPower) {
            this.epsilonPower = epsilonPower;
            return this;
        }

        public Builder setR3ASelectionMethod(MPASelectionMethod mpaSelectionMethod) {
            this.mpaSelectionMethod = mpaSelectionMethod;
            return this;
        }

        public Builder setR3ACompressionMethod(MPACompressionMethod mpaCompressionMethod) {
            this.mpaCompressionMethod = mpaCompressionMethod;
            return this;
        }

        @Override
        public MPASettings build() {
            return new MPASettings(minSkipReference, maxMultiplierBetweenLevel, epsilonPower, mpaSelectionMethod, mpaCompressionMethod);
        }
    }
}
