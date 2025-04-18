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

        public Builder setDataSettings(DataSettings dataSettings){
            this.dataSettings = dataSettings;
            return this;
        }

        public Builder setDataSettings(UnaryOperator<DataSettings.Builder> changes) {
            this.dataSettings = changes.apply(dataSettings == null ? null : dataSettings.edit()).build();
            return this;
        }

        public Builder setAnimationSettings(AnimationSettings animationSettings){
            this.animationSettings = animationSettings;
            return this;
        }

        public Builder setAnimationSettings(UnaryOperator<AnimationSettings.Builder> changes) {
            this.animationSettings = changes.apply(animationSettings == null ? null : animationSettings.edit()).build();
            return this;
        }

        public Builder setExportSettings(ExportSettings exportSettings){
            this.exportSettings = exportSettings;
            return this;
        }

        public Builder setExportSettings(UnaryOperator<ExportSettings.Builder> changes) {
            this.exportSettings = changes.apply(exportSettings == null ? null : exportSettings.edit()).build();
            return this;
        }


        @Override
        public VideoSettings build() {
            return new VideoSettings(dataSettings, animationSettings, exportSettings);
        }
    }
}
