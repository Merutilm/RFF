package kr.merutilm.rff.preset.render;

import kr.merutilm.rff.settings.RenderSettings;

public class RenderMedium implements Render {
    @Override
    public String getName() {
        return "Medium";
    }
    @Override
    public RenderSettings createImageSettings() {
        return new RenderSettings(0.5, true);
    }
}
