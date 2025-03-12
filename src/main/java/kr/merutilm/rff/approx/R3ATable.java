package kr.merutilm.rff.approx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kr.merutilm.rff.formula.ArrayCompressor;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.util.ArrayFunction;

public class R3ATable {


    static int getRequiredPerturbationCount(boolean[] isArtificial, int index) {
        int required;
        if(isArtificial[index]){
            required = 0;
            //artificially-created period's result iteration usually not a periodic point.
        }else{
            required = 2;
            //If the "period - 1" iterations are skipped, the resulting iteration is a periodic point.
            //That is, it is very small, which can cause floating-point errors, such as z + dz = 0 (e.g. big - big = small).
            //Therefore, Skip until a previous point of periodic point, In other words, skip "period - 2" iterations.
        }
        return required;
    }
    
    protected static int[] generatePeriodElements(int[] tablePeriod){
        
        
        // index compression : [3, 11, 26, 77] // index compression : [3, 11, 26, 77]
        // startIteration : 1  4  7 12 15 18 23 27
        // index :          0  1  2  3  4  5  6  7
        
        // 3 6 9 12 15 18 21 24 -> 3 6 9 -> 1 4 7 (11/3 = 3.xxx)
        // 11 22 33 44 55 66 -> 11, 22 -> 1 12 (26/11 = 2.xxx)
        // 26 52 78 104 130 -> 26, 52 -> 1 27 (77/26 = 2.xxx)
        // 
        // the remainder of [2]/[1] can also be divided by smaller period.
        // it can be recurive.
        //
        // period 11 : 11/3 =3.xxx (3 elements)                                                                              elements = 3 
        // period 26 : 26/11=2.xxx (3*2 elements), 26%11 = 4, 4/3 = 1.xxx (1 element)                                        elements = 3*2+1=7
        // period 77 : 77/26=2.xxx (7*2 elements), 77%26 = 25,25/11= 2.xxx (3*2 elements), 25%11=4, 4/3 = 1.xxx (1 element), elements = 7*2+3*2+1=21
        // Stored elements to memory

        int[] tablePeriodElements = new int[tablePeriod.length];
        for (int i = 0; i < tablePeriodElements.length; i++) {
            if(i == 0){
                tablePeriodElements[i] = 1;
                continue;
            }
            int elements = 0;
            int remainder = tablePeriod[i];
            for (int j = i - 1; j >= 0; j--) {
                int groupAmount = remainder / tablePeriod[j];
                remainder %= tablePeriod[j];
                elements += groupAmount * tablePeriodElements[j];
            }
            tablePeriodElements[i] = elements;
        }
        return tablePeriodElements;
    }


    protected static List<ArrayCompressor> generateR3ACompressors(int[] tablePeriod, int[] tablePeriodElements, List<ArrayCompressor> refCompressors){
        

        List<ArrayCompressor> r3aCompressors = new ArrayList<>();

        for (ArrayCompressor refCompressor : refCompressors) {
            
            int start = refCompressor.start();
            int length = refCompressor.length();
            int index = Arrays.binarySearch(tablePeriod, length + 1);
            //if the reference compressor is same as period
            if(index >= 0){
                int tableIndex = iterationToTableIndex(tablePeriod, tablePeriodElements, Collections.emptyList(), start);
                int periodElements = tablePeriodElements[index];
                
                r3aCompressors.add(new ArrayCompressor(1, tableIndex + 1, tableIndex + periodElements - 1));
            }
        }
        return r3aCompressors;
    }

    protected static int iterationToUncompressedTableIndex(int[] tablePeriod, int[] tablePeriodElements, int iteration){
        //
        // get index <=> Inverse calculation of index compression
        // First approach : check the remainder == 1
        //
        // [3, 11, 26]
        // 1 4 7 12 15 18 23 27 30 33 38
        //
        // test input : 23
        // search period : period 11
        // 23 % 11 = 1, 23/11 = 2.xxx(3*2 elements)
        // 1 % 3 = 1, 1/3 = 0.xxx(1*0 elements) 
        // result = 3*2=6
        //
        //
        // test input : 30
        // search period : period 26
        // 30 % 26 = 4, 30/26 = 1.xxx(7*1 elements)
        // 4 % 3 = 1, 4/3 = 1.xxx(1 element)
        // result = 7*1+1=8
        //
        // test input : 29
        // search period : period 26
        // 29 % 26 = 3, 29/26 = 1.xxx(7*1 elements)
        // 3 % 3 = 0, 3/3 = 1.xxx(1 element)
        // result = -1 (last remainder is not one)
        // 
        // 

        if(iteration == 0){
            return -1;
        }

        int index = 0;
        int remainder = iteration;
        
        for (int i = tablePeriod.length - 1; i >= 0; i--) {
            if(remainder < tablePeriod[i]){
                continue;
            }
            if(i < tablePeriod.length - 1 && remainder + tablePeriod[0] > tablePeriod[i + 1]){
                return -1;
            }
            

            index += remainder / tablePeriod[i] * tablePeriodElements[i];
            remainder %= tablePeriod[i];
        }
        return remainder == 1 ? index : -1;
    }
    
    protected static int iterationToTableIndex(int[] tablePeriod, int[] tablePeriodElements, List<ArrayCompressor> r3aCompressors, int iteration){
        int index = iterationToUncompressedTableIndex(tablePeriod, tablePeriodElements, iteration);
        return index == -1 ? -1 : ArrayCompressor.compress(r3aCompressors, index);
    }



    protected record PeriodTemp(int[] tablePeriod, boolean[] isArtificial) {
        @Override
        public final boolean equals(Object o) {
            return o instanceof PeriodTemp(int[] t, boolean[] i) &&
                Arrays.equals(tablePeriod, t) &&
                Arrays.equals(isArtificial, i);
        }
    
        @Override
        public final String toString() {
            return "Period : " + Arrays.toString(tablePeriod)
                + "\nArtificial : " + Arrays.toString(isArtificial); 
        }
    
        @Override
        public final int hashCode() {
            return Arrays.hashCode(tablePeriod) + Arrays.hashCode(isArtificial);
        }

        protected static PeriodTemp generateR3APeriod(int[] referencePeriod, R3ASettings r3aSettings){
            
    
            // example 
            // period : [3, 11, 26]
            // 
            // -- : The space of R3A
            //
            // It : 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 ... longestPeriod
            //
            // 03 : 00 01 02 03 01 02 03 01 02 03 -- -- 01 02 03 01 02 03 01 02 03 -- -- 01 02 03 -- ...
            // 11 : 00 01 02 03 04 05 06 07 08 09 10 11 01 02 03 04 05 06 07 08 09 10 11 -- -- -- -- ...
            // 26 : 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 ...
            //
            //
            // Skips
            //  1 + 2 |  1 + 10 |  1 + 25
            //  4 + 2 | 12 + 10 | 27 + 25
            //  7 + 2 | 27 + 10
            // 12 + 2
            // 15 + 2
            // 18 + 2
            // 23 + 2
            // 27 + 2
            // ...
    
            int maxMultiplier = r3aSettings.maxMultiplierBetweenLevel();
            int minSkip = r3aSettings.minSkipReference();
            int longestPeriod = referencePeriod[referencePeriod.length - 1];
    
            int[] refPeriodTemp = new int[1];
            boolean[] isArtificial = new boolean[1];
    
            int periodArraySize = 1;
            int currentRefPeriod = minSkip;
    
    
            refPeriodTemp[0] = currentRefPeriod;
            isArtificial[0] = Arrays.binarySearch(referencePeriod, currentRefPeriod) < 0;
            //first period is always minimum skip iteration when the longest period is larger than this
            //and it is artificially-created period if generated period is not an element of generated period.
    
            for (int p : referencePeriod) {
    
                //Generate Period Array
    
                if(p >= minSkip && (p == longestPeriod && currentRefPeriod != longestPeriod || currentRefPeriod * maxMultiplier <= p)){
    
                    //If next valid period is "maxMultiplierBetweenLevel^2" times larger than "currentPeriod",
                    //add currentPeriod * "maxMultiplierBetweenLevel" period
                    //until the multiplier between level is lower than the square of "maxMultiplierBetweenLevel".
                    //It is artificially-created period.
    
                    while (currentRefPeriod >= minSkip && currentRefPeriod * maxMultiplier * maxMultiplier < p) {
                        if (periodArraySize == refPeriodTemp.length) {
                            refPeriodTemp = ArrayFunction.exp2xArr(refPeriodTemp);
                            isArtificial = ArrayFunction.exp2xArr(isArtificial);
                        }
                        refPeriodTemp[periodArraySize] = currentRefPeriod * maxMultiplier;
                        isArtificial[periodArraySize] = true;
    
                        periodArraySize++;
                        currentRefPeriod *= maxMultiplier;
                    }
    
                    //Otherwise, add generated period to period array.
    
                    if (periodArraySize == refPeriodTemp.length) {
                        refPeriodTemp = ArrayFunction.exp2xArr(refPeriodTemp);
                        isArtificial = ArrayFunction.exp2xArr(isArtificial);
                    }
                    refPeriodTemp[periodArraySize] = p;
                    periodArraySize++;
                    currentRefPeriod = p;
    
    
                }
            }
    
            return new PeriodTemp(
                Arrays.copyOfRange(refPeriodTemp, 0, periodArraySize), 
                Arrays.copyOfRange(isArtificial, 0, periodArraySize)
            );
        }
        
    }
}
