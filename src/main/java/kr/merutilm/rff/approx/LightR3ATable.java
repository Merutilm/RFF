package kr.merutilm.rff.approx;

import kr.merutilm.rff.formula.LightMandelbrotReference;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.util.AdvancedMath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class LightR3ATable extends R3ATable{

    private final List<List<LightR3A>> table;

    public LightR3ATable(ParallelRenderState state, int currentID, LightMandelbrotReference reference, R3ASettings r3aSettings, double dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException {
        super(reference, r3aSettings);
        this.table = createTable(state, currentID, reference, dcMax, actionPerCreatingTableIteration);
    }

    private List<List<LightR3A>> createTable(ParallelRenderState state, int currentID, LightMandelbrotReference reference, double dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException{

        if(r3aPeriod == null){
            return Collections.emptyList();
        }

        int[] tablePeriod = r3aPeriod.tablePeriod();
        
        int longestPeriod = tablePeriod[tablePeriod.length - 1];
        int[] periodCount = new int[tablePeriod.length];
        
        if(longestPeriod < settings.minSkipReference()){
            return Collections.emptyList();
        }
        
        List<List<LightR3A>> table = new ArrayList<>();
        LightR3A.Builder[] currentStep = new LightR3A.Builder[tablePeriod.length];
        double epsilon = Math.pow(10, settings.epsilonPower());
   

        int iteration = 1;
        
        while (iteration <= longestPeriod) {

            state.tryBreak(currentID);
            actionPerCreatingTableIteration.accept(iteration, (double) iteration / longestPeriod);
            int pulledTableIndex = iterationToPulledTableIndex(r3aPeriod, iteration);
            boolean independent = pulledR3ACompressor == null || pulledR3ACompressor.isIndependent(pulledTableIndex);
//            int r3aCompressorContainedIndex = pulledR3ACompressor == null ? -1 : pulledR3ACompressor.containedIndex(pulledTableIndex);
//            boolean independent = pulledR3ACompressor == null || r3aCompressorContainedIndex == -1;
//            if(!independent){
//                LightR3A matchingMergeR3A = pulledR3ACompressor.getMatchingR3A(r3aCompressorContainedIndex);
//                boolean exceeds = false;
//                int iterationIncrement = matchingMergeR3A.skip();
//
//                for(int j = tablePeriod.length - 1; j >= 0; j--){
//
//                    int count = exceeds ? periodCount[j + 1] : (periodCount[j] + iterationIncrement);
//                    if(count >= tablePeriod[j]){
//                        count %= tablePeriod[j];
//                        exceeds = true;
//                    }
//
//                    periodCount[j] = count;
//                    if(currentStep[j] != null){
//                        currentStep[j] = currentStep[j].merge(matchingMergeR3A);
//                    }
//                }
//                iteration += iterationIncrement;
//            }

            for (int j = tablePeriod.length - 1; j >= 0; j--) {

                int requiredPerturbationCount = r3aPeriod.requiredPerturbation()[j];

                if(periodCount[j] == 0 && independent){
                    currentStep[j] = LightR3A.Builder.create(reference, epsilon, dcMax, iteration);
                }

                if(currentStep[j] != null && periodCount[j] + requiredPerturbationCount < tablePeriod[j]){
                    currentStep[j] = currentStep[j].step();
                }


                periodCount[j]++;

                if(periodCount[j] == tablePeriod[j]){
                    for(int k = j; k >= 0; k--){

                        //Stop all lower level iteration for efficiency
                        //because it is too hard to skipping to next part of the periodic point
                        LightR3A.Builder currentLevel = currentStep[k];

                        if(currentLevel != null && periodCount[k] == tablePeriod[k]){
                            //If the skip count is lower than its current period,
                            //it can be replaced to several lower-period RRA.

                            int compTableIndex = iterationToCompTableIndex(currentLevel.start());
                            safetyMatchTableSize(table, compTableIndex);

                            List<LightR3A> r3a = table.get(compTableIndex);
                            r3a.add(currentLevel.build());
                        }

                        currentStep[k] = null;
                        periodCount[k] = 0;
                    }
                    break;
                }
            }

            iteration++;

        }
        
        return Collections.unmodifiableList(table);
    }


    public LightR3A lookup(int iteration, double dzr, double dzi) {
        
        if(iteration == 0 || r3aPeriod == null){
            return null;
        }
        int index = iterationToCompTableIndex(iteration);
        
        if(index == -1 || index >= table.size()){
            return null;
        }
        int[] tablePeriod = r3aPeriod.tablePeriod();
        int longestPeriod = tablePeriod[tablePeriod.length - 1];
        int maxSkip = longestPeriod - iteration;
        
        
        List<LightR3A> table = this.table.get(index);
        
        if(table == null || table.isEmpty()){
            return null;
        }
        
        double r = AdvancedMath.hypotApproximate(dzr, dzi);
        
        return switch (settings.r3aSelectionMethod()) {
            case LOWEST -> {
                LightR3A r3a = null;

                for(LightR3A test : table){
                    if (maxSkip >= test.skip() && test.isValid(r)) {
                        r3a = test;
                    }else yield r3a;
                }
                yield r3a;
            }
            case HIGHEST -> {

                LightR3A r3a = table.getFirst();
                if(!r3a.isValid(r)){
                    yield null;
                }

                for (int j = table.size() - 1; j >= 0; j--) {
                    LightR3A test = table.get(j);
                    if (maxSkip >= test.skip() && test.isValid(r)) {
                        yield test;
                    }
                }
                
                yield r3a;

            }
        };

    }

    @Override
    public int length() {
        return table.size();
    }
}
