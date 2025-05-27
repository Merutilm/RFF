package kr.merutilm.rff.preset.calc;

import kr.merutilm.rff.settings.MPACompressionMethod;
import kr.merutilm.rff.settings.MPASelectionMethod;
import kr.merutilm.rff.settings.MPASettings;
import kr.merutilm.rff.settings.ReferenceCompressionSettings;

public class CalculationUltraStable implements Calculation {
    @Override
    public String getName() {
        return "Ultra Stable";
    }
    @Override
    public MPASettings mpaSettings() {
        return new MPASettings(16, 4, -4, MPASelectionMethod.HIGHEST, MPACompressionMethod.STRONGEST);
    }

    @Override
    public ReferenceCompressionSettings referenceCompressionSettings() {
        return new ReferenceCompressionSettings(10000, 6);
    }
}
