package kr.merutilm.rff.approx;

import kr.merutilm.rff.formula.DeepMandelbrotReference;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.struct.DoubleExponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class DeepR3ATable extends R3ATable{

    private final List<List<DeepR3A>> table;

    public DeepR3ATable(ParallelRenderState state, int currentID, DeepMandelbrotReference reference, R3ASettings r3aSettings, DoubleExponent dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException {
        super(reference, r3aSettings);
        this.table = createTable(state, currentID, reference, dcMax, actionPerCreatingTableIteration);
    }

    private List<List<DeepR3A>> createTable(ParallelRenderState state, int currentID, DeepMandelbrotReference reference, DoubleExponent dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException{

        if(r3aPeriod == null){
            return Collections.emptyList();
        }

        int[] tablePeriod = r3aPeriod.tablePeriod();
        
        int longestPeriod = tablePeriod[tablePeriod.length - 1];
        int[] periodCount = new int[tablePeriod.length];
        
        if(longestPeriod < settings.minSkipReference()){
            return Collections.emptyList();
        }
        
        List<List<DeepR3A>> table = new ArrayList<>();
        DeepR3A.Builder[] currentStep = new DeepR3A.Builder[tablePeriod.length];
        double epsilon = Math.pow(10, settings.epsilonPower());
   

        int iteration = 1;
        
        while (iteration <= longestPeriod) {

            state.tryBreak(currentID);
            actionPerCreatingTableIteration.accept(iteration, (double) iteration / longestPeriod);

            int unCompressedTableIndex = iterationToOriginalTableIndex(r3aPeriod, iteration);
            boolean independant = r3aCompressor == null || r3aCompressor.isIndependant(unCompressedTableIndex);
            
            for (int j = tablePeriod.length - 1; j >= 0; j--) {

                int requiredPerturbationCount = r3aPeriod.requiredPerturbation()[j];
                
                
                if(periodCount[j] == 0 && independant){
                    currentStep[j] = DeepR3A.Builder.create(reference, epsilon, dcMax, iteration);
                }

                if(currentStep[j] != null && periodCount[j] + requiredPerturbationCount < tablePeriod[j]){
                    currentStep[j] = currentStep[j].step();
                }
                
                
                periodCount[j]++;

                if(periodCount[j] == tablePeriod[j]){
                    for(int k = j; k >= 0; k--){
                        
                        DeepR3A.Builder currentLevel = currentStep[k];
                        
                        if(currentLevel != null && periodCount[k] == tablePeriod[k]){
                            int index = iterationToTableIndex(currentLevel.start());
                            safetyMatchTable(table, index);
           
                            List<DeepR3A> r3a = table.get(index);
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



    public DeepR3A lookup(int iteration, DoubleExponent dzr, DoubleExponent dzi) {

        if(iteration == 0 || r3aPeriod == null){
            return null;
        }
        
        int index = iterationToTableIndex(iteration);

        if(index == -1 || index >= table.size()){
            return null;
        }
        int[] tablePeriod = r3aPeriod.tablePeriod();
        int longestPeriod = tablePeriod[tablePeriod.length - 1];
        int maxSkip = longestPeriod - iteration;
        

        
        List<DeepR3A> table = this.table.get(index);

        if(table == null || table.isEmpty()){
            return null;
        }
        
        return switch (settings.r3aSelectionMethod()) {
            case LOWEST -> {
                DeepR3A r3a = null;

                for(DeepR3A test : table){
                    if (maxSkip >= test.skip() && test.isValid(dzr, dzi)) {
                        r3a = test;
                    }else yield r3a;
                }
                yield r3a;
            }
            case HIGHEST -> {

                DeepR3A r3a = table.getFirst();
                if(!r3a.isValid(dzr, dzi)){
                    yield null;
                }

                for (int j = table.size() - 1; j >= 0; j--) {
                    DeepR3A test = table.get(j);
                    if (maxSkip >= test.skip() && test.isValid(dzr, dzi)) {
                        yield test;
                    }
                }
                
                yield r3a;

            }
        };
    }
}
