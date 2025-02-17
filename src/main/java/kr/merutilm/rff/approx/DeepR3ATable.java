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
import java.util.function.BiConsumer;

public class DeepR3ATable implements R3ATable{

    private final List<List<DeepR3A>> table;
    private final R3ASettings settings;

    public DeepR3ATable(RenderState state, int currentID, R3ASettings r3aSettings, DoubleExponent[] rr, DoubleExponent[] ri, int[] period, DoubleExponent dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalRenderStateException {
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
        isArtificial[0] = Arrays.binarySearch(period, currentPeriod) < 0;

        for (int p : period) {

            if(p >= minSkip && (p == longestPeriod && currentPeriod != longestPeriod || currentPeriod * maxMultiplier <= p)){

                while (currentPeriod >= minSkip && currentPeriod * maxMultiplier * maxMultiplier < p) {
                    if (periodArraySize == periodTemp.length) {
                        periodTemp = ArrayFunction.exp2xArr(periodTemp);
                        isArtificial = ArrayFunction.exp2xArr(isArtificial);
                    }
                    currentPeriod *= maxMultiplier;

                    periodTemp[periodArraySize] = currentPeriod;
                    isArtificial[periodArraySize] = true;

                    periodArraySize++;
                }


                if (periodArraySize == periodTemp.length) {
                    periodTemp = ArrayFunction.exp2xArr(periodTemp);
                    isArtificial = ArrayFunction.exp2xArr(isArtificial);
                }

                currentPeriod = p;

                periodTemp[periodArraySize] = currentPeriod;

                periodArraySize++;

            }
        }

        period = Arrays.copyOfRange(periodTemp, 0, periodArraySize);
        isArtificial = Arrays.copyOfRange(isArtificial, 0, periodArraySize);

        DeepR3A[] currentStep = new DeepR3A[period.length];

        for (int i = 1; i <= longestPeriod; i++) {

            state.tryBreak(currentID);
            actionPerCreatingTableIteration.accept(i, (double) i / longestPeriod);

            for (int j = period.length - 1; j >= 0; j--) {
                int requiredPerturbationCount = R3ATable.getRequiredPerturbationCount(r3aSettings.fixGlitches(), isArtificial, j);

                if(currentStep[j] == null){
                    currentStep[j] = DeepR3A.create(i).step(rr, ri, epsilon, dcMax); // step 1
                }else if(currentStep[j].skip() + requiredPerturbationCount == period[j]){

                    for(int k = j; k >= 0; k--){ //Stop all lower level iteration
                        DeepR3A currentLevel = currentStep[k];

                        if(currentLevel != null && currentLevel.skip() + requiredPerturbationCount == period[k]){
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
