package kr.merutilm.rff.preset.shader.fog;

import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.FogSettings;

public class FogHigh implements ShaderPreset.Fog {

    @Override
    public String getName() {
        return "High";
    }

    @Override
    public FogSettings fogSettings() {
        return new FogSettings(0.15, 0.8);
    }


}
