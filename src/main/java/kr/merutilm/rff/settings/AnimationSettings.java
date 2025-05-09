package kr.merutilm.rff.settings;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record AnimationSettings(
        double overZoom,
        boolean showText,
        double mps
) implements Struct<AnimationSettings> {

    @Override
    public Builder edit() {
        return new Builder()
                .setOverZoom(overZoom)
                .setShowText(showText)
                .setMps(mps);
    }

    public static final class Builder implements StructBuilder<AnimationSettings> {
        private double overZoom;
        private boolean showText;
        private double mps;

        public Builder setOverZoom(double overZoomValue) {
            this.overZoom = overZoomValue;
            return this;
        }

        public Builder setShowText(boolean showText) {
            this.showText = showText;
            return this;
        }

        public Builder setMps(double mps) {
            this.mps = mps;
            return this;
        }

        @Override
        public AnimationSettings build() {
            return new AnimationSettings(overZoom, showText, mps);
        }

    }
}
