package kr.merutilm.rff.formula;

import kr.merutilm.rff.precision.LWBigComplex;

public interface Reference {
    
    String STR_FORMULA = "\nFormula : ";
    String STR_CENTER = "\nReference Center : ";
    String STR_PERIOD = "\nPeriod : ";

    /**
     * Gets the ignition formula.
     */
    Formula formula();

    /**
     * Gets the center of reference.
     */
    LWBigComplex refCenter();
    /**
     * The Period of reference orbit.
     */
    int[] period();

    /**
     * The Length of reference orbit.
     */
    int length();

    default int longestPeriod(){
        return period()[period().length - 1];
    }


}
