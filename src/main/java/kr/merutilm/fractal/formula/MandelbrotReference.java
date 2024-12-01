package kr.merutilm.fractal.formula;
import kr.merutilm.fractal.struct.LWBigComplex;

public interface MandelbrotReference extends Reference{

    String STR_REFERENCE_REAL = "\nReferance Real : ";
    String STR_REFERENCE_IMAG = "\nReferance Imag : ";
    String STR_LAST_REF = "\nLast Reference : ";
    String STR_FPG_BN = "\nFPGBn : ";
    double FPG_MAX_EPSILON = Math.pow(2, -5);
    
    /**
     * Gets last valid reference just before exiting. 
     */
    LWBigComplex lastReference();
    /**
     * Gets FPGBn for center-locating.
     */
    LWBigComplex fpgBn();
}
