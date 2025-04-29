package kr.merutilm.rff.preset.render;

import kr.merutilm.rff.settings.RenderSettings;

public class RenderLow implements Render{
    @Override
    public String getName() {
        return "Low";
    }
    @Override
    public RenderSettings createImageSettings() {
        return new RenderSettings(0.25, true);
    }
    
}
