package kr.merutilm.rff.preset.render;

import kr.merutilm.rff.preset.Preset;
import kr.merutilm.rff.settings.RenderSettings;

public interface Render extends Preset{
    RenderSettings createImageSettings();
}
