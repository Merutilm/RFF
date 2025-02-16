package kr.merutilm.rff.settings;

import java.util.function.UnaryOperator;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record VideoSettings(
    DataSettings dataSettings,
    AnimationSettings animationSettings,
    ExportSettings exportSettings
) implements Struct<VideoSettings>{

    @Override
    public Builder edit() {
        return new Builder()
        .setDataSettings(dataSettings)
        .setAnimationSettings(animationSettings)
        .setExportSettings(exportSettings);
    }
    
    public static final class Builder implements StructBuilder<VideoSettings>{
        private DataSettings dataSettings;
        private AnimationSettings animationSettings;
        private ExportSettings exportSettings;

        public Builder setDataSettings(DataSettings videoDataSettings) {
            this.dataSettings = videoDataSettings;
            return this;
        }

        public Builder setAnimationSettings(AnimationSettings videoAnimateSettings) {
            this.animationSettings = videoAnimateSettings;
            return this;
        }

        public Builder setExportSettings(ExportSettings videoExportSettings) {
            this.exportSettings = videoExportSettings;
            return this;
        }

        public Builder setDataSettings(UnaryOperator<DataSettings> changes) {
            this.dataSettings = changes.apply(dataSettings);
            return this;
        }

        public Builder setAnimationSettings(UnaryOperator<AnimationSettings> changes) {
            this.animationSettings = changes.apply(animationSettings);
            return this;
        }

        public Builder setExportSettings(UnaryOperator<ExportSettings> changes) {
            this.exportSettings = changes.apply(exportSettings);
            return this;
        }


        @Override
        public VideoSettings build() {
            return new VideoSettings(dataSettings, animationSettings, exportSettings);
        }
    }
}
