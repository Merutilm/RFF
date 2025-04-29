package kr.merutilm.rff.preset.shader.fog;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.FogSettings;

public class FogUltra implements ShaderPreset.Fog {

    @Override
    public String getName() {
        return "Ultra";
    }

    @Override
    public FogSettings fogSettings() {
        return new FogSettings(0.15, 1);
    }


}
