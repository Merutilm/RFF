package kr.merutilm.rff.preset.render;

import kr.merutilm.rff.settings.ImageSettings;

public class RenderUltra implements Render {
    @Override
    public String getName() {
        return "Ultra";
    }
    @Override
    public ImageSettings createImageSettings() {
        return new ImageSettings(3);
    }
}
