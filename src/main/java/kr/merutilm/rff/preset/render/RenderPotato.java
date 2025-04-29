package kr.merutilm.rff.preset.render;

import kr.merutilm.rff.settings.RenderSettings;

public class RenderPotato implements Render{
    @Override
    public String getName() {
        return "Potato";
    }
    @Override
    public RenderSettings createImageSettings() {
        return new RenderSettings(0.1, true);
    }
}
