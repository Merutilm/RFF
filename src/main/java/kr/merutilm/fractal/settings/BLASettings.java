package kr.merutilm.fractal.settings;

import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;

import kr.merutilm.base.struct.Struct;
import kr.merutilm.base.struct.StructBuilder;
/**
 * @param use set whether to activate BLA. Default is true.
 * @param epsilonPower set Epsilon power Addition. Useful for glitch reduction. if this value is small, The fractal will be rendered glitch-less but slow, and is large, It will be fast, but maybe shown visible glitches.
 * @param minLevel set minimum level of BLA. (BLA level means the merged BLA will skip 2^(level) iterations)
 */
public record BLASettings(double epsilonPower, int minLevel) implements Struct<BLASettings> {
    @Override
    public Builder edit() {
        return new Builder()
                .setEpsilonPower(epsilonPower)
                .setMinLevel(minLevel);
    }


    public static final class Builder implements StructBuilder<BLASettings> {
        private double epsilonPower;
        private int minLevel;

        public Builder setEpsilonPower(double epsilonPower) {
            this.epsilonPower = epsilonPower;
            return this;
        }

        public Builder setMinLevel(int minLevel) {
            this.minLevel = minLevel;
            return this;
        }

        public Builder setEpsilonPower(DoubleUnaryOperator changes) {
            this.epsilonPower = changes.applyAsDouble(epsilonPower);
            return this;
        }

        public Builder setMinLevel(IntUnaryOperator changes) {
            this.minLevel = changes.applyAsInt(minLevel);
            return this;
        }

        @Override
        public BLASettings build() {
            return new BLASettings(epsilonPower, minLevel);
        }
    }
}
