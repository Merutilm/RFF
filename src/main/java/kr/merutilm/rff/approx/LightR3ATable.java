package kr.merutilm.rff.approx;

import java.util.*;
import java.util.function.BiConsumer;

import kr.merutilm.rff.shader.IllegalRenderStateException;
import kr.merutilm.rff.shader.RenderState;
import kr.merutilm.rff.util.ArrayFunction;
import kr.merutilm.rff.settings.R3ASettings;
public class LightR3ATable implements R3ATable{

    private final List<List<LightR3A>> table;
    private final R3ASettings settings;

    public LightR3ATable(RenderState state, int currentID, R3ASettings r3aSettings, double[] rr, double[] ri, int[] period, double dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalRenderStateException {
        double epsilon = Math.pow(10, r3aSettings.epsilonPower());

        int longestPeriod = period[period.length - 1];
        this.table = new ArrayList<>(Collections.nCopies(longestPeriod + 1, null));
        this.settings = r3aSettings;


        int minSkip = r3aSettings.minSkipReference();
        int maxMultiplier = r3aSettings.maxMultiplierBetweenLevel();

        int[] periodTemp = new int[1];
        boolean[] isArtificial = new boolean[1];

        int periodArraySize = 1;
        int currentPeriod = minSkip;

        periodTemp[0] = currentPeriod;
        //first period is always minimum skip iteration.
        isArtificial[0] =  Arrays.binarySearch(period, currentPeriod) < 0;
        //and it is artificially-created period if generated period is not an element of generated period.

        for (int p : period) {

            //Generate Period Array

            if(p >= minSkip && (p == longestPeriod && currentPeriod != longestPeriod || currentPeriod * maxMultiplier <= p)){

                //If next valid period is "maxMultiplierBetweenLevel^2" times larger than "currentPeriod",
                //add currentPeriod * "maxMultiplierBetweenLevel" period
                //until the multiplier between level is lower than the square of "maxMultiplierBetweenLevel".
                //It is artificially-created period.

                while (currentPeriod >= minSkip && currentPeriod * maxMultiplier * maxMultiplier < p) {
                    if (periodArraySize == periodTemp.length) {
                        periodTemp = ArrayFunction.exp2xArr(periodTemp);
                        isArtificial = ArrayFunction.exp2xArr(isArtificial);
                    }
                    periodTemp[periodArraySize] = currentPeriod * maxMultiplier;
                    isArtificial[periodArraySize] = true;

                    periodArraySize++;
                    currentPeriod *= maxMultiplier;
                }

                //Otherwise, add generated period to period array.

                if (periodArraySize == periodTemp.length) {
                    periodTemp = ArrayFunction.exp2xArr(periodTemp);
                    isArtificial = ArrayFunction.exp2xArr(isArtificial);
                }
                periodTemp[periodArraySize] = p;
                periodArraySize++;
                currentPeriod = p;


            }
        }

        period = Arrays.copyOfRange(periodTemp, 0, periodArraySize);
        isArtificial = Arrays.copyOfRange(isArtificial, 0, periodArraySize);

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
        // == : It can be reused using higher-level RRA
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
            actionPerCreatingTableIteration.accept(i, (double) i / longestPeriod);

            for (int j = period.length - 1; j >= 0; j--) {

                int requiredPerturbationCount = R3ATable.getRequiredPerturbationCount(r3aSettings.fixGlitches(), isArtificial, j);

                if(currentStep[j] == null){
                    currentStep[j] = LightR3A.create(i).step(rr, ri, epsilon, dcMax); // step 1
                }else if(currentStep[j].skip() + requiredPerturbationCount == period[j]){

                    for(int k = j; k >= 0; k--){
                        //Stop all lower level iteration for efficiency
                        //because it is too hard to skipping to next part of the periodic point
                        LightR3A currentLevel = currentStep[k];

                        if(currentLevel != null && currentLevel.skip() + requiredPerturbationCount == period[k]){
                            //If the skip count is lower than its current period,
                            //it can be replaced to several lower-period RRA.

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
                        //reuse upper level if both start iterations are the same
                        currentStep[j] = currentStep[j + 1];
                    }else{
                        currentStep[j] = currentStep[j].step(rr, ri, epsilon, dcMax);
                    }
                }
            }
        }
    }



    public LightR3A lookup(int iteration, double dzr, double dzi) {

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
