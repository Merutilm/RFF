package kr.merutilm.rff.preset.calc;

import kr.merutilm.rff.settings.MPACompressionMethod;
import kr.merutilm.rff.settings.MPASelectionMethod;
import kr.merutilm.rff.settings.MPASettings;
import kr.merutilm.rff.settings.ReferenceCompressionSettings;

public class CalculationNormal implements Calculation{

    @Override
    public String getName() {
        return "Normal";
    }
    @Override
    public MPASettings mpaSettings() {
        return new MPASettings(8, 2, -5, MPASelectionMethod.HIGHEST, MPACompressionMethod.LITTLE_COMPRESSION);
    }

    @Override
    public ReferenceCompressionSettings referenceCompressionSettings() {
        return new ReferenceCompressionSettings(1000000, 11);
    }
}
