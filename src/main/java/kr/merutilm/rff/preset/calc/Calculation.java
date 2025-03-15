package kr.merutilm.rff.preset.calc;

import kr.merutilm.rff.preset.Preset;
import kr.merutilm.rff.settings.CalculationSettings;

public interface Calculation extends Preset{
    CalculationSettings generate();
}
