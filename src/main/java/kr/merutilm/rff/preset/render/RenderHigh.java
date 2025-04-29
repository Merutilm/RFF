package kr.merutilm.rff.preset.render;

import kr.merutilm.rff.settings.RenderSettings;

public class RenderHigh implements Render{
    @Override
    public String getName() {
        return "High";
    }
    @Override
    public RenderSettings createImageSettings() {
        return new RenderSettings(1, true);
    }
}
