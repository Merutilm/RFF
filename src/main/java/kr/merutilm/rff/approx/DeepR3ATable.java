package kr.merutilm.rff.approx;

import kr.merutilm.rff.shader.IllegalRenderStateException;
import kr.merutilm.rff.shader.RenderState;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.util.ArrayFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeepR3ATable implements R3ATable{

    private final List<List<DeepR3A>> table;
    private final R3ASettings settings;

    public DeepR3ATable(RenderState state, int currentID, R3ASettings r3aSettings, DoubleExponent[] rr, DoubleExponent[] ri, int[] period, DoubleExponent dcMax) throws IllegalRenderStateException {
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

        int perturbationCount = r3aSettings.fixFloatingPointErrors() ? 2 : 1;

        DeepR3A[] currentStep = new DeepR3A[period.length];

        for (int i = 1; i <= longestPeriod; i++) {

            state.tryBreak(currentID);

            for (int j = period.length - 1; j >= 0; j--) {
                if(currentStep[j] == null){
                    currentStep[j] = DeepR3A.create(i).step(rr, ri, epsilon, dcMax); // step 1
                }else if(currentStep[j].skip() + perturbationCount == period[j]){

                    for(int k = j; k >= 0; k--){ //Stop all lower level iteration
                        DeepR3A currentLevel = currentStep[k];

                        if(currentLevel != null && currentLevel.skip() + perturbationCount == period[k]){
                            List<DeepR3A> elem = table.get(currentLevel.start());

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


    public DeepR3A lookup(int iteration, DoubleExponent dzr, DoubleExponent dzi) {
        //iterationInterval is power of 2, 
        // use a bit operator because remainder operation (%) is slow


        List<DeepR3A> table = this.table.get(iteration);

        if (table == null) {
            return null;
        }

        DeepR3A r3a = table.getFirst();

        if(!r3a.isValid(dzr, dzi)){
            return null;
        }

        return switch (settings.r3aSelectionMethod()) {
            case LOWEST -> {
                for (DeepR3A test : table) {
                    if (test.isValid(dzr, dzi)) {
                        r3a = test;
                    }else break;
                }
                yield r3a;
            }
            case HIGHEST -> {

                for (int j = table.size() - 1; j >= 0; j--) {
                    DeepR3A test = table.get(j);
                    if (test.isValid(dzr, dzi)) {
                        yield test;
                    }
                }
                yield r3a;
            }
        };
    }
}
