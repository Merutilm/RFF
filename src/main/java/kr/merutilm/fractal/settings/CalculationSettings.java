package kr.merutilm.fractal.settings;

import java.util.function.DoubleUnaryOperator;
import java.util.function.LongUnaryOperator;
import java.util.function.UnaryOperator;

import kr.merutilm.base.struct.Struct;
import kr.merutilm.base.struct.StructBuilder;
import kr.merutilm.fractal.struct.DoubleExponent;
import kr.merutilm.fractal.struct.LWBigComplex;

public record CalculationSettings(
        double logZoom,
        long maxIteration,
        double bailout,
        DecimalIterationSettings decimalIterationSettings,
        LWBigComplex center,
        boolean autoIteration,
        ReuseReferenceSettings reuseReference,
        BLASettings blaSettings
) implements Struct<CalculationSettings> {

    public static final double ZOOM_VALUE = 0.235;

    public static final double MININUM_ZOOM = 2;

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
                .setBLASettings(blaSettings);
    }

    public static final class Builder implements StructBuilder<CalculationSettings> {

        private double logZoom;
        private long maxIteration;
        private double bailout;
        private DecimalIterationSettings decimalIterationSettings;
        private LWBigComplex center;
        private boolean autoIteration;
        private ReuseReferenceSettings reuseReference;
        private BLASettings blaSettings;

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

        public Builder setDecimalIterationSettings(DecimalIterationSettings decimalIterationSettings) {
            this.decimalIterationSettings = decimalIterationSettings;
            return this;
        }

        public Builder setReuseReference(ReuseReferenceSettings reuseReference) {
            this.reuseReference = reuseReference;
            return this;
        }

        public Builder setBLASettings(BLASettings blaSettings) {
            this.blaSettings = blaSettings;
            return this;
        }

        public Builder setLogZoom(DoubleUnaryOperator changes) {
            this.logZoom = changes.applyAsDouble(logZoom);
            return this;
        }

        public Builder setMaxIteration(LongUnaryOperator changes) {
            this.maxIteration = changes.applyAsLong(maxIteration);
            return this;
        }

        public Builder setBLASettings(UnaryOperator<BLASettings> changes) {
            this.blaSettings = changes.apply(blaSettings);
            return this;
        }

        public Builder zoomIn(double v) {
            logZoom += v;
            return this;
        }

        public Builder zoomOut(double v) {
            logZoom = Math.max(MININUM_ZOOM, logZoom - v);
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
            return new CalculationSettings(logZoom, maxIteration, bailout, decimalIterationSettings, center, autoIteration, reuseReference, blaSettings);
        }
    }

}
