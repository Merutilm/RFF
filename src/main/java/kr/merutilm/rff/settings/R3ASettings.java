package kr.merutilm.rff.settings;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record R3ASettings(int minSkipReference,
                          int maxMultiplierBetweenLevel,
                          double epsilonPower,
                          R3ASelectionMethod r3aSelectionMethod,
                          R3ACompressionMethod r3aCompressionMethod) implements Struct<R3ASettings> {

    @Override
    public Builder edit() {
        return new Builder()
                .setMinSkipReference(minSkipReference)
                .setMaxMultiplierBetweenLevel(maxMultiplierBetweenLevel)
                .setEpsilonPower(epsilonPower)
                .setR3ASelectionMethod(r3aSelectionMethod)
                .setR3ACompressionMethod(r3aCompressionMethod);
    }


    public static final class Builder implements StructBuilder<R3ASettings> {
        private int minSkipReference;
        private int maxMultiplierBetweenLevel;
        private double epsilonPower;
        private R3ASelectionMethod r3aSelectionMethod;
        private R3ACompressionMethod r3aCompressionMethod;

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

        public Builder setR3ASelectionMethod(R3ASelectionMethod blaSelectionMethod) {
            this.r3aSelectionMethod = blaSelectionMethod;
            return this;
        }

        public Builder setR3ACompressionMethod(R3ACompressionMethod r3aCompressionMethod) {
            this.r3aCompressionMethod = r3aCompressionMethod;
            return this;
        }

        @Override
        public R3ASettings build() {
            return new R3ASettings(minSkipReference, maxMultiplierBetweenLevel, epsilonPower, r3aSelectionMethod, r3aCompressionMethod);
        }
    }
}
