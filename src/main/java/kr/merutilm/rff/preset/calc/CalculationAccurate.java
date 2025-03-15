package kr.merutilm.rff.preset.calc;

import kr.merutilm.rff.settings.R3ACompressionMethod;
import kr.merutilm.rff.settings.R3ASelectionMethod;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.settings.ReferenceCompressionSettings;

public class CalculationAccurate implements Calculation {
    @Override
    public String getName() {
        return "Accurate";
    }
    @Override
    public R3ASettings r3aSettings() {
        return new R3ASettings(16, 16, -6, R3ASelectionMethod.HIGHEST, R3ACompressionMethod.LITTLE_COMPRESSION);
    }

    @Override
    public ReferenceCompressionSettings referenceCompressionSettings() {
        return new ReferenceCompressionSettings(10000, 15);
    }
}
