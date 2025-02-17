package kr.merutilm.rff.settings;

import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record R3ASettings(int minSkipReference,
                          int maxMultiplierBetweenLevel,
                          double epsilonPower,
                          R3ASelectionMethod r3aSelectionMethod,
                          boolean fixGlitches) implements Struct<R3ASettings> {

    @Override
    public Builder edit() {
        return new Builder()
                .setMinSkipReference(minSkipReference)
                .setMaxMultiplierBetweenLevel(maxMultiplierBetweenLevel)
                .setEpsilonPower(epsilonPower)
                .setR3ASelectionMethod(r3aSelectionMethod)
                .setFixGlitches(fixGlitches);
    }


    public static final class Builder implements StructBuilder<R3ASettings> {
        private int minSkipReference;
        private int maxMultiplierBetweenLevel;
        private double epsilonPower;
        private R3ASelectionMethod r3aSelectionMethod;
        private boolean fixGlitches;

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

        public Builder setFixGlitches(boolean fixGlitches) {
            this.fixGlitches = fixGlitches;
            return this;
        }

        public Builder setMinSkipReference(IntUnaryOperator changes) {
            this.minSkipReference = changes.applyAsInt(minSkipReference);
            return this;
        }

        public Builder setMaxMultiplierBetweenLevel(IntUnaryOperator changes) {
            this.maxMultiplierBetweenLevel = changes.applyAsInt(maxMultiplierBetweenLevel);
            return this;
        }

        public Builder setEpsilonPower(DoubleUnaryOperator changes) {
            this.epsilonPower = changes.applyAsDouble(epsilonPower);
            return this;
        }

        public Builder setR3ASelectionMethod(UnaryOperator<R3ASelectionMethod> changes) {
            this.r3aSelectionMethod = changes.apply(r3aSelectionMethod);
            return this;
        }

        public Builder setFixGlitches(UnaryOperator<Boolean> changes) {
            this.fixGlitches = changes.apply(fixGlitches);
            return this;
        }

        @Override
        public R3ASettings build() {
            return new R3ASettings(minSkipReference, maxMultiplierBetweenLevel, epsilonPower, r3aSelectionMethod, fixGlitches);
        }
    }
}
