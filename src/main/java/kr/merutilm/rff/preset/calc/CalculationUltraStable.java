package kr.merutilm.rff.preset.calc;

import kr.merutilm.rff.settings.R3ACompressionMethod;
import kr.merutilm.rff.settings.R3ASelectionMethod;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.settings.ReferenceCompressionSettings;

public class CalculationUltraStable implements Calculation {
    @Override
    public String getName() {
        return "Ultra Stable";
    }
    @Override
    public R3ASettings r3aSettings() {
        return new R3ASettings(16, 4, -4, R3ASelectionMethod.HIGHEST, R3ACompressionMethod.STRONGEST);
    }

    @Override
    public ReferenceCompressionSettings referenceCompressionSettings() {
        return new ReferenceCompressionSettings(10000, 6);
    }
}
