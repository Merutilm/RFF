package kr.merutilm.rff.settings;

import java.util.function.UnaryOperator;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.struct.LWBigComplex;

public record CalculationSettings(
        double logZoom,
        long maxIteration,
        double bailout,
        DecimalizeIterationMethod decimalIterationSettings,
        LWBigComplex center,
        boolean autoIteration,
        ReuseReferenceMethod reuseReference,
        R3ASettings r3aSettings,
        int compressCriteria,
        int compressionThresholdPower
) implements Struct<CalculationSettings> {

    public static final double ZOOM_VALUE = 0.235;

    public static final double MINIMUM_ZOOM = 1;

    @Override
    public Builder edit() {
        return new Builder()
                .setLogZoom(logZoom)
                .setMaxIteration(maxIteration)
                .setBailout(bailout)
                .setDecimalIterationSettings(decimalIterationSettings)
                .setCenter(center)
                .setAutoIteration(autoIteration)
                .setReuseReference(reuseReference)
                .setR3ASettings(r3aSettings)
                .setCompressCriteria(compressCriteria)
                .setCompressionThresholdPower(compressionThresholdPower);
    }

    public static final class Builder implements StructBuilder<CalculationSettings> {

        private double logZoom;
        private long maxIteration;
        private double bailout;
        private DecimalizeIterationMethod decimalIterationSettings;
        private LWBigComplex center;
        private boolean autoIteration;
        private ReuseReferenceMethod reuseReference;
        private R3ASettings r3aSettings;
        private int compressCriteria;
        private int compressionThresholdPower;

        public Builder setLogZoom(double logZoom) {
            this.logZoom = logZoom;
            return this;
        }

        public Builder setMaxIteration(long maxIteration) {
            this.maxIteration = maxIteration;
            return this;
        }

        public Builder setCenter(LWBigComplex center) {
            this.center = center;
            return this;
        }

        public Builder setAutoIteration(boolean autoIteration) {
            this.autoIteration = autoIteration;
            return this;
        }

        public Builder setBailout(double bailout) {
            this.bailout = bailout;
            return this;
        }

        public Builder setDecimalIterationSettings(DecimalizeIterationMethod decimalIterationSettings) {
            this.decimalIterationSettings = decimalIterationSettings;
            return this;
        }

        public Builder setReuseReference(ReuseReferenceMethod reuseReference) {
            this.reuseReference = reuseReference;
            return this;
        }

        public Builder setR3ASettings(R3ASettings r3aSettings) {
            this.r3aSettings = r3aSettings;
            return this;
        }

        public Builder setCompressCriteria(int compressCriteria) {
            this.compressCriteria = compressCriteria;
            return this;
        }
        
        public Builder setCompressionThresholdPower(int compressionThresholdPower) {
            this.compressionThresholdPower = compressionThresholdPower;
            return this;
        }

        public Builder setR3ASettings(UnaryOperator<R3ASettings> changes) {
            this.r3aSettings = changes.apply(r3aSettings);
            return this;
        }

        public Builder zoomIn(double v) {
            logZoom += v;
            return this;
        }

        public Builder zoomOut(double v) {
            logZoom = Math.max(MINIMUM_ZOOM, logZoom - v);
            return this;
        }
        public Builder zoomIn() {
            return zoomIn(ZOOM_VALUE);
        }

        public Builder zoomOut() {
            return zoomOut(ZOOM_VALUE);
        }

        public Builder addCenter(LWBigComplex c, int precision) {
            center = center.add(c, precision);
            return this;
        }


        public Builder addCenter(DoubleExponent re, DoubleExponent im, int precision) {
            return addCenter(LWBigComplex.valueOf(re, im, precision), precision);
        }



        @Override
        public CalculationSettings build() {
            return new CalculationSettings(logZoom, maxIteration, bailout, decimalIterationSettings, center, autoIteration, reuseReference, r3aSettings, compressCriteria, compressionThresholdPower);
        }
    }

}
