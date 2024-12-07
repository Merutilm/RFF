package kr.merutilm.fractal.settings;

import kr.merutilm.base.struct.Struct;
import kr.merutilm.base.struct.StructBuilder;

public record VideoExportSettings(double fps, double mps, double overZoom, double multiSampling, int bitrate) implements Struct<VideoExportSettings> {
    @Override
    public Builder edit() {
        return new Builder()
        .setFps(fps)
        .setMps(mps)
        .setOverZoom(overZoom)
        .setMultiSampling(multiSampling)
        .setBitrate(bitrate);
    }

    public static final class Builder implements StructBuilder<VideoExportSettings>{

        private double fps = 30;
        private double mps = 1;
        private double overZoom = 2;
        private double multiSampling = 1;
        private int bitrate = 5000;

        public Builder setFps(double fps) {
            this.fps = fps;
            return this;
        }

        public Builder setMps(double logZoomPerSecond) {
            this.mps = logZoomPerSecond;
            return this;
        }

        public Builder setOverZoom(double overZoomValue) {
            this.overZoom = overZoomValue;
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
        public VideoExportSettings build() {
            return new VideoExportSettings(fps, mps, overZoom, multiSampling, bitrate);
        }
    }
}
