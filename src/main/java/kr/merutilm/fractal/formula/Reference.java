package kr.merutilm.fractal.formula;

import kr.merutilm.fractal.struct.LWBigComplex;

public interface Reference {
    
    String STR_FORMULA = "\nFormula : ";
    String STR_CENTER = "\nReference Center : ";
    String STR_PERIOD = "\nPeriod : ";

    /**
     * Gets the ignition formula.
     */
    Formula formula();

    /**
     * Gets the reference center.
     */
    LWBigComplex refCenter();
    /**
     * The Period of reference orbit.
     */
    int period();
}
