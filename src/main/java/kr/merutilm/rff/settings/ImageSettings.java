package kr.merutilm.rff.settings;

import java.util.function.DoubleUnaryOperator;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;



public record ImageSettings(
    double resolutionMultiplier
) implements Struct<ImageSettings> {


    @Override
    public Builder edit() {
        return new Builder()
        .setResolutionMultiplier(resolutionMultiplier);
    }

    public static final class Builder implements StructBuilder<ImageSettings>{
        
        private double resolutionMultiplier;
        

    
        public Builder setResolutionMultiplier(double resolutionMultiplier) {
            this.resolutionMultiplier = resolutionMultiplier;
            return this;
        }

      
        public Builder setResolutionMultiplier(DoubleUnaryOperator changes) {
            this.resolutionMultiplier = changes.applyAsDouble(resolutionMultiplier);
            return this;
        }
        

        @Override
        public ImageSettings build() {
            return new ImageSettings(resolutionMultiplier);
        }
    }



    
}
