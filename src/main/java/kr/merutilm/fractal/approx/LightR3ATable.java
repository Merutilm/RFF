package kr.merutilm.fractal.approx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.parallel.RenderState;
import kr.merutilm.fractal.settings.R3ASettings;
public class LightR3ATable implements R3ATable{


    public LightR3ATable(RenderState state, int currentID, R3ASettings r3aSettings, double[] rr, double[] ri, int[] periodArray, double dcMax) throws IllegalRenderStateException {
        List<LightR3A> table = new ArrayList<>();
        double epsilon = Math.pow(10, r3aSettings.epsilonPower());
        
        rr = new double[100];
        ri = new double[100];
        periodArray = new int[]{3, 11, 26}; //TODO Sample 
        int longestPeriod = periodArray[periodArray.length - 1];

        // example 
        // period : [3, 11, 26]
        // 
        // -- : The space of R3A
        //
        // It : 00 01 02 03   04 05 06   07 08 09   10 11   12 13 14   15 16 17   18 19 20   21 22   23 24 25   26   ... longestPeriod
        //
        // 03 : 00 01 02 03 01 02 03 01 02 03 -- -- 01 02 03 01 02 03 01 02 03 -- -- 01 02 03 -- ...
        // 11 : 00 01 02 03 04 05 06 07 08 09 10 11 01 02 03 04 05 06 07 08 09 10 11 -- -- -- -- ...
        // 26 : 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 ...
        // 
        // == : It can be reused using shorter R3A
        //
        // 03 :  0001 02 03 01 02 03 01 02 03 -- -- 01 02 03 01 02 03 01 02 03 -- -- 01 02 03 -- ...
        // 11 : == == == == 04 05 06 07 08 09 10 11 == == == 04 05 06 07 08 09 10 11 -- -- -- -- ...
        // 26 : == == == == == == == == == == == == 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 ...
        //
        // Algorithms
        // 1. Longest -> Shortest Period array Iterator
        // 2. $Skipping = $CurrentPeriodLength - 1
        // 3-1. $CurrentIteration + $Skipping <= $NextPeriodLength : Do create Table
        // 3-2. or else... Do not create Table
        // 4. If current and shorter elements' start Iteration are the same... It can be reused using Shorter R3A
        
        LightR3A[] currentStep = new LightR3A[periodArray.length];
        
        for (int i = 1; i < longestPeriod; i++) {

            for (int j = periodArray.length - 1; j >= 0; j--) {
                if(currentStep[j] == null){
                    currentStep[j] = LightR3A.create(i).step(rr, ri, epsilon, dcMax);
                    
                }else if(currentStep[j].skip() + 1 == periodArray[j]){

                    for(int k = j; k >= 0; k--){
                        if(currentStep[k] == null){
                            continue;
                        }
                        table.add(currentStep[k]);
                        currentStep[k] = null;
                    }

                }else{
                    currentStep[j] = currentStep[j].step(rr, ri, epsilon, dcMax);
                }
            }
        }
        System.out.println(table.stream().map(e -> e.start() + " " + e.skip()).toList());
    }


    public LightR3A lookup(int iteration, double dzr, double dzi) {
        //iterationInterval is power of 2, 
        // use a bit operator because remainder operation (%) is slow
        // if (iteration >= R3ATable.getMaxSkippableIteration(period, iterationInterval) || ((iteration - 1) & (iterationInterval - 1)) > 0) {
        //     return null;
        // }


        // int i = indices[iteration];

        // if (i == -1) {
        //     return null;
        // }


        // int iNext = indices[iteration + iterationInterval];
        // iNext = iNext == -1 ? table.length - 1 : iNext;

        // if(!table[i].isValid(dzr, dzi)){
        //     return null;
        // }

        // switch (settings.r3aSelectionMethod()) {
        //     case LOWEST -> {
        //         LightR3A valid = null;
        //         for (int j = i + 1; j < iNext; j++) {
        //             if (table[j].isValid(dzr, dzi)) {
        //                 valid = table[j];
        //             }else break;
        //         }
        //         return valid;
        //     }
        //     case HIGHEST -> {

        //         for (int j = iNext - 1; j >= i + 1; j--) {
        //             if (table[j].isValid(dzr, dzi)) {
        //                 return table[j];
        //             }
        //         }
        //     }
        //     default -> {
        //         throw new UnsupportedOperationException();
        //     }
        // }

        
        return null;
    }


}
