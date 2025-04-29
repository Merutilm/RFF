package kr.merutilm.rff.preset.shader.fog;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.FogSettings;

public class FogMedium implements ShaderPreset.Fog {

    @Override
    public String getName() {
        return "Medium";
    }

    @Override
    public FogSettings fogSettings() {
        return new FogSettings(0.1, 0.5);
    }


}
