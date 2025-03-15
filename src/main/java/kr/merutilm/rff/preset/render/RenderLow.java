package kr.merutilm.rff.preset.render;

import kr.merutilm.rff.settings.ImageSettings;

public class RenderLow implements Render{
    @Override
    public String getName() {
        return "Low";
    }
    @Override
    public ImageSettings createImageSettings() {
        return new ImageSettings(0.5);
    }
}
