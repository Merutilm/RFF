package kr.merutilm.fractal.settings;

import kr.merutilm.base.struct.Struct;
import kr.merutilm.base.struct.StructBuilder;

public record ExportSettings(double fps, double mps, double multiSampling, int bitrate) implements Struct<ExportSettings> {
    @Override
    public Builder edit() {
        return new Builder()
        .setFps(fps)
        .setMps(mps)
        .setMultiSampling(multiSampling)
        .setBitrate(bitrate);
    }

    public static final class Builder implements StructBuilder<ExportSettings>{

        private double fps;
        private double mps;
        private double multiSampling;
        private int bitrate;

        public Builder setFps(double fps) {
            this.fps = fps;
            return this;
        }

        public Builder setMps(double logZoomPerSecond) {
            this.mps = logZoomPerSecond;
            return this;
        }

        public Builder setMultiSampling(double multiSampling) {
            this.multiSampling = multiSampling;
            return this;
        }

        public Builder setBitrate(int bitrate) {
            this.bitrate = bitrate;
            return this;
        }

        @Override
        public ExportSettings build() {
            return new ExportSettings(fps, mps, multiSampling, bitrate);
        }
    }
}
