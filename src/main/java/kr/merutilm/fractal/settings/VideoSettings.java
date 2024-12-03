package kr.merutilm.fractal.settings;

import kr.merutilm.base.struct.Struct;
import kr.merutilm.base.struct.StructBuilder;

public record VideoSettings(double fps, double logZoomPerSecond, double multiSampling) implements Struct<VideoSettings> {
    @Override
    public Builder edit() {
        return new Builder()
        .setFps(fps)
        .setLogZoomPerSecond(logZoomPerSecond)
        .setMultiSampling(multiSampling);
    }

    public static final class Builder implements StructBuilder<VideoSettings>{

        private double fps = 30;
        private double logZoomPerSecond = 0.5;
        private double multiSampling = 1;

        public Builder setFps(double fps) {
            this.fps = fps;
            return this;
        }

        public Builder setLogZoomPerSecond(double logZoomPerSecond) {
            this.logZoomPerSecond = logZoomPerSecond;
            return this;
        }

        public Builder setMultiSampling(double multiSampling) {
            this.multiSampling = multiSampling;
            return this;
        }

        @Override
        public VideoSettings build() {
            return new VideoSettings(fps, logZoomPerSecond, multiSampling);
        }
    }
}
