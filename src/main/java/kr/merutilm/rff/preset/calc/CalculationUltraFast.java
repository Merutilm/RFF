package kr.merutilm.rff.preset.calc;

import kr.merutilm.rff.settings.MPACompressionMethod;
import kr.merutilm.rff.settings.MPASelectionMethod;
import kr.merutilm.rff.settings.MPASettings;
import kr.merutilm.rff.settings.ReferenceCompressionSettings;

public class CalculationUltraFast implements Calculation{
    @Override
    public String getName() {
        return "Ultra Fast";
    }
    @Override
    public MPASettings r3aSettings() {
        return new MPASettings(4, 2, -3, MPASelectionMethod.HIGHEST, MPACompressionMethod.NO_COMPRESSION);
    }

    @Override
    public ReferenceCompressionSettings referenceCompressionSettings() {
        return new ReferenceCompressionSettings(-1, -1);
    }
    
}
