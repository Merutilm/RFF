package kr.merutilm.rff.settings;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;


public record RenderSettings(
        double resolutionMultiplier,
        boolean antialiasing
) implements Struct<RenderSettings> {


    @Override
    public Builder edit() {
        return new Builder()
                .setResolutionMultiplier(resolutionMultiplier)
                .setAntialiasing(antialiasing);
    }

    public static final class Builder implements StructBuilder<RenderSettings> {

        private double resolutionMultiplier;
        private boolean antialiasing;


        public Builder setResolutionMultiplier(double resolutionMultiplier) {
            this.resolutionMultiplier = resolutionMultiplier;
            return this;
        }

        public Builder setAntialiasing(boolean antialiasing) {
            this.antialiasing = antialiasing;
            return this;
        }


        @Override
        public RenderSettings build() {
            return new RenderSettings(resolutionMultiplier, antialiasing);
        }
    }


}
