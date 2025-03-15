package kr.merutilm.rff.preset.render;

import kr.merutilm.rff.settings.ImageSettings;

public class RenderHigh implements Render{
    @Override
    public String getName() {
        return "High";
    }
    @Override
    public ImageSettings createImageSettings() {
        return new ImageSettings(2);
    }
}
