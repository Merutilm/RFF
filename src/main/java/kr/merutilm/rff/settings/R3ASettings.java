package kr.merutilm.rff.settings;

import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;
/**
 * @param minSkipReference Set minimum skipping reference iteration when creating a table.
 * @param maxMultiplierBetweenLevel Set maximum multiplier between adjacent skipping levels. This means the maximum multiplier of two adjacent periods for the new period that inserts between them, So the multiplier between the two periods may in the worst case be the square of this.
 * @param epsilonPower Set Epsilon power Addition. Useful for glitch reduction. if this value is small, The fractal will be rendered glitch-less but slow, and is large, It will be fast, but maybe shown visible glitches.
 * @param r3aSelectionMethod Set the selection method of R3A.
 */
public record R3ASettings(int minSkipReference, int maxMultiplierBetweenLevel, double epsilonPower, R3ASelectionMethod r3aSelectionMethod) implements Struct<R3ASettings> {
    
    @Override
    public Builder edit() {
        return new Builder()
        .setMinSkipReference(minSkipReference)
        .setMaxMultiplierBetweenLevel(maxMultiplierBetweenLevel)
        .setEpsilonPower(epsilonPower)
        .setR3ASelectionMethod(r3aSelectionMethod);
    }


    public static final class Builder implements StructBuilder<R3ASettings> {
        private int minSkipReference;
        private int maxMultiplierBetweenLevel;
        private double epsilonPower;
        private R3ASelectionMethod r3aSelectionMethod;

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

        @Override
        public R3ASettings build() {
            return new R3ASettings(minSkipReference, maxMultiplierBetweenLevel, epsilonPower, r3aSelectionMethod);
        }
    }
}
