package kr.merutilm.rff.approx;

public interface R3ATable {


    static int getRequiredPerturbationCount(boolean fixGlitches, boolean[] isArtificial, int index) {
        int required;
        if(isArtificial[index]){
            required = 0;
            //artificially-created period's result iteration usually not a periodic point.
        }else{
            required = fixGlitches ? 2 : 1;
            //If the "period - 1" iterations are skipped, the resulting iteration is a periodic point.
            //That is, it is very small, which can cause floating-point errors, such as z + dz = 0 (e.g. big - big = small).
        }
        return required;
    }
}
