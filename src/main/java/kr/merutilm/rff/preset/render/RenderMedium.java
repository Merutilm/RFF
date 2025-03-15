package kr.merutilm.rff.preset.render;

import kr.merutilm.rff.settings.ImageSettings;

public class RenderMedium implements Render{
    @Override
    public String getName() {
        return "Medium";
    }
    @Override
    public ImageSettings createImageSettings() {
        return new ImageSettings(1);
    }
    
}
