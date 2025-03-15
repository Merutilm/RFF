package kr.merutilm.rff.formula;
import kr.merutilm.rff.functions.ArrayCompressor;
import kr.merutilm.rff.struct.LWBigComplex;

public interface MandelbrotReference extends Reference{

    String STR_REFERENCE_REAL = "\nReference Real : ";
    String STR_REFERENCE_IMAG = "\nReference Imag : ";
    String STR_LAST_REF = "\nLast Reference : ";
    String STR_FPG_BN = "\nFPGBn : ";

    /**
     * Gets last valid reference just before exiting. 
     */
    LWBigComplex lastReference();

    ArrayCompressor refReal();
    ArrayCompressor refImag();

    /**
     * Gets FPGBn for center-locating.
     */
    LWBigComplex fpgBn();
}
