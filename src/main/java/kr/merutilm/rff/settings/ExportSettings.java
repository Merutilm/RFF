package kr.merutilm.rff.settings;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record ExportSettings(double fps, int multiSampling, int bitrate) implements Struct<ExportSettings> {
    @Override
    public Builder edit() {
        return new Builder()
        .setFps(fps)
        .setMultiSampling(multiSampling)
        .setBitrate(bitrate);
    }

    public static final class Builder implements StructBuilder<ExportSettings>{

        private double fps;
        private int multiSampling;
        private int bitrate;

        public Builder setFps(double fps) {
            this.fps = fps;
            return this;
        }

        public Builder setMultiSampling(int multiSampling) {
            this.multiSampling = multiSampling;
            return this;
        }

        public Builder setBitrate(int bitrate) {
            this.bitrate = bitrate;
            return this;
        }

        @Override
        public ExportSettings build() {
            return new ExportSettings(fps, multiSampling, bitrate);
        }
    }
}
