package kr.merutilm.rff.approx;

import java.util.*;
import java.util.function.Supplier;

import kr.merutilm.rff.shader.IllegalRenderStateException;
import kr.merutilm.rff.shader.RenderState;
import kr.merutilm.rff.util.ArrayFunction;
import kr.merutilm.rff.settings.R3ASettings;
public class LightR3ATable implements R3ATable{

    private final List<List<LightR3A>> table;
    private final R3ASettings settings;

    public LightR3ATable(RenderState state, int currentID, R3ASettings r3aSettings, double[] rr, double[] ri, int[] period, double dcMax) throws IllegalRenderStateException {
        double epsilon = Math.pow(10, r3aSettings.epsilonPower());

        int longestPeriod = period[period.length - 1];
        this.table = new ArrayList<>(Collections.nCopies(longestPeriod + 1, null));
        this.settings = r3aSettings;


        int currentPeriod = period[0];
        int[] periodTemp = new int[1];
        int periodArraySize = 0;
        int minSkip = r3aSettings.minSkipReference();
        int maxMultiplier = r3aSettings.maxMultiplierBetweenLevel();

        for (int p : period) {

            if (p == 0) {
                break;
            }
            if(p >= minSkip && (p == longestPeriod && currentPeriod != longestPeriod || currentPeriod * maxMultiplier <= p)){

                while (currentPeriod >= minSkip && currentPeriod * maxMultiplier * maxMultiplier < p) {
                    if (periodArraySize == periodTemp.length) {
                        periodTemp = ArrayFunction.exp2xArr(periodTemp);
                    }
                    periodTemp[periodArraySize] = currentPeriod * maxMultiplier;
                    periodArraySize++;
                    currentPeriod *= maxMultiplier;
                }


                if (periodArraySize == periodTemp.length) {
                    periodTemp = ArrayFunction.exp2xArr(periodTemp);
                }
                periodTemp[periodArraySize] = p;
                periodArraySize++;
                currentPeriod = p;


            }
        }

        period = Arrays.copyOfRange(periodTemp, 0, periodArraySize);

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
        // == : It can be reused using shorter R3A
        //
        // 03 : == == == == 01 02 03 01 02 03 -- -- == == == 01 02 03 01 02 03 -- -- 01 02 03 -- ...
        // 11 : == == == == == == == == == == == == 01 02 03 04 05 06 07 08 09 10 11 -- -- -- -- ...
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

        
        LightR3A[] currentStep = new LightR3A[period.length];

        for (int i = 1; i <= longestPeriod; i++) {

            state.tryBreak(currentID);

            for (int j = period.length - 1; j >= 0; j--) {
                if(currentStep[j] == null){
                    currentStep[j] = LightR3A.create(i).step(rr, ri, epsilon, dcMax); // step 1
                }else if(currentStep[j].skip() + 1 == period[j]){

                    for(int k = j; k >= 0; k--){ //Stop all lower level iteration
                        LightR3A currentLevel = currentStep[k];

                        if(currentLevel != null && currentLevel.skip() + 1 == period[k]){
                            List<LightR3A> elem = table.get(currentLevel.start());

                            if(elem == null){
                                elem = new ArrayList<>();
                            }

                            elem.add(currentLevel);
                            table.set(currentLevel.start(), elem);
                        }

                        currentStep[k] = null;
                    }
                    break;
                }else {
                    if(j < currentStep.length - 1 && currentStep[j].start() == currentStep[j + 1].start()){
                        currentStep[j] = currentStep[j + 1];
                    }else{
                        currentStep[j] = currentStep[j].step(rr, ri, epsilon, dcMax);
                    }
                }
            }
        }
    }



    public LightR3A lookup(int iteration, double dzr, double dzi) {
        //iterationInterval is power of 2, 
        // use a bit operator because remainder operation (%) is slow


         List<LightR3A> table = this.table.get(iteration);

         if (table == null) {
             return null;
         }

         LightR3A r3a = table.getFirst();

         if(!r3a.isValid(dzr, dzi)){
             return null;
         }

         return switch (settings.r3aSelectionMethod()) {
             case LOWEST -> {
                 for (LightR3A test : table) {
                     if (test.isValid(dzr, dzi)) {
                         r3a = test;
                     }else break;
                 }
                 yield r3a;
             }
             case HIGHEST -> {

                 for (int j = table.size() - 1; j >= 0; j--) {
                     LightR3A test = table.get(j);
                     if (test.isValid(dzr, dzi)) {
                         yield test;
                     }
                 }
                 yield r3a;
             }
         };

    }


}
