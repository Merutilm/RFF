package kr.merutilm.rff.preset.calc;

import kr.merutilm.rff.settings.R3ACompressionMethod;
import kr.merutilm.rff.settings.R3ASelectionMethod;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.settings.ReferenceCompressionSettings;

public class CalculationNormal implements Calculation{

    @Override
    public String getName() {
        return "Normal";
    }
    @Override
    public R3ASettings r3aSettings() {
        return new R3ASettings(8, 2, -5, R3ASelectionMethod.HIGHEST, R3ACompressionMethod.LITTLE_COMPRESSION);
    }

    @Override
    public ReferenceCompressionSettings referenceCompressionSettings() {
        return new ReferenceCompressionSettings(100000, 11);
    }
}
