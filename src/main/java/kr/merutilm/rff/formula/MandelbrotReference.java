package kr.merutilm.rff.formula;
import kr.merutilm.rff.approx.R3A;
import kr.merutilm.rff.functions.ReferenceCompressor;
import kr.merutilm.rff.precision.LWBigComplex;

public interface MandelbrotReference extends Reference{

    String STR_REFERENCE_REAL = "\nReference Real : ";
    String STR_REFERENCE_IMAG = "\nReference Imag : ";
    String STR_LAST_REF = "\nLast Reference : ";
    String STR_FPG_BN = "\nFPGBn : ";

    /**
     * Gets last valid reference just before exiting. 
     */
    LWBigComplex lastReference();

    <R extends R3A> ReferenceCompressor<R> referenceCompressor();

    /**
     * Gets FPGBn for center-locating.
     */
    LWBigComplex fpgBn();
}
