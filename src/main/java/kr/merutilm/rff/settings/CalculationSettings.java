package kr.merutilm.rff.settings;

import java.util.function.UnaryOperator;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.struct.LWBigComplex;

public record CalculationSettings(
        LWBigComplex center,
        double logZoom,
        long maxIteration,
        double bailout,
        DecimalizeIterationMethod decimalizeIterationMethod,
        R3ASettings r3aSettings,
        ReferenceCompressionSettings referenceCompressionSettings,
        ReuseReferenceMethod reuseReference,
        boolean autoIteration,
        boolean absoluteIterationMode
) implements Struct<CalculationSettings> {

    public static final double ZOOM_VALUE = 0.235;

    public static final double MINIMUM_ZOOM = 1;

    @Override
    public Builder edit() {
        return new Builder()
                .setCenter(center)
                .setLogZoom(logZoom)
                .setMaxIteration(maxIteration)
                .setBailout(bailout)
                .setDecimalizeIterationMethod(decimalizeIterationMethod)
                .setR3ASettings(r3aSettings)
                .setReferenceCompressionSettings(referenceCompressionSettings)
                .setReuseReference(reuseReference)
                .setAutoIteration(autoIteration)
                .setAbsoluteIterationMode(absoluteIterationMode);
    }

    public static final class Builder implements StructBuilder<CalculationSettings> {

        private LWBigComplex center;
        private double logZoom;
        private long maxIteration;
        private double bailout;
        private DecimalizeIterationMethod decimalizeIterationMethod;
        private R3ASettings r3aSettings;
        private ReferenceCompressionSettings referenceCompressionSettings;
        private ReuseReferenceMethod reuseReference;
        private boolean autoIteration;
        private boolean absoluteIterationMode;

        public Builder setCenter(LWBigComplex center) {
            this.center = center;
            return this;
        }

        public Builder setLogZoom(double logZoom) {
            this.logZoom = logZoom;
            return this;
        }

        public Builder setMaxIteration(long maxIteration) {
            this.maxIteration = maxIteration;
            return this;
        }

        public Builder setBailout(double bailout) {
            this.bailout = bailout;
            return this;
        }

        public Builder setDecimalizeIterationMethod(DecimalizeIterationMethod decimalizeIterationMethod) {
            this.decimalizeIterationMethod = decimalizeIterationMethod;
            return this;
        }

        public Builder setR3ASettings(R3ASettings r3aSettings){
            this.r3aSettings = r3aSettings;
            return this;
        }

        public Builder setR3ASettings(UnaryOperator<R3ASettings.Builder> changes) {
            this.r3aSettings = changes.apply(r3aSettings == null ? null : r3aSettings.edit()).build();
            return this;
        }

        public Builder setReferenceCompressionSettings(ReferenceCompressionSettings referenceCompressionSettings){
            this.referenceCompressionSettings = referenceCompressionSettings;
            return this;
        }
        
        public Builder setReferenceCompressionSettings(UnaryOperator<ReferenceCompressionSettings.Builder> changes) {
            this.referenceCompressionSettings = changes.apply(referenceCompressionSettings == null ? null : referenceCompressionSettings.edit()).build();
            return this;
        }

        public Builder setReuseReference(ReuseReferenceMethod reuseReference) {
            this.reuseReference = reuseReference;
            return this;
        }
        
        public Builder setAutoIteration(boolean autoIteration) {
            this.autoIteration = autoIteration;
            return this;
        }

        public Builder setAbsoluteIterationMode(boolean absoluteIterationMode) {
            this.absoluteIterationMode = absoluteIterationMode;
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
            return new CalculationSettings(center, logZoom, maxIteration, bailout, decimalizeIterationMethod, r3aSettings, referenceCompressionSettings, reuseReference, autoIteration, absoluteIterationMode);
        }
    }

}
