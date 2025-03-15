package kr.merutilm.rff.preset.calc;

import kr.merutilm.rff.settings.R3ACompressionMethod;
import kr.merutilm.rff.settings.R3ASelectionMethod;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.settings.ReferenceCompressionSettings;

public class CalculationUltraFast implements Calculation{
    @Override
    public String getName() {
        return "Ultra Fast";
    }
    @Override
    public R3ASettings r3aSettings() {
        return new R3ASettings(16, 16, -3, R3ASelectionMethod.HIGHEST, R3ACompressionMethod.NO_COMPRESSION);
    }

    @Override
    public ReferenceCompressionSettings referenceCompressionSettings() {
        return new ReferenceCompressionSettings(-1, -1);
    }
    
}
