package kr.merutilm.rff.approx;

import kr.merutilm.rff.formula.LightMandelbrotReference;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.R3ASettings;


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

            int unCompressedTableIndex = iterationToOriginalTableIndex(r3aPeriod, iteration);
            boolean independant = r3aCompressor == null || r3aCompressor.isIndependant(unCompressedTableIndex);
            
            // if(compressedTableIndex == 1 && unCompressedTableIndex != compressedTableIndex){
            //     //If "uncompressed index" is not equal to "compressed index",
            //     //It can be replaced the lighter R3A.
            //     //In the other words, by merging current R3A and exising r3a, the table creating speed will be significantly faster.
                
            //     List<LightR3A> toMerge = table.get(0);
                
            //     for (int j = tablePeriod.length - 1; j >= 0; j--) {
            //         if(periodCount[j] == 0){
            //             //reuse existing r3a
                        
            //         }else{
            //             //merge existing r3a

            //         }
            //     }

                

            //     System.out.println(iteration + " | " + Arrays.toString(periodCount));
            // }
            
            for (int j = tablePeriod.length - 1; j >= 0; j--) {

                int requiredPerturbationCount = r3aPeriod.requiredPerturbation()[j];
                
                
                if(periodCount[j] == 0 && independant){
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
                            
                            int index = iterationToTableIndex(currentLevel.start());
                            safetyMatchTable(table, index);
           
                            List<LightR3A> r3a = table.get(index);
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
        
        int index = iterationToTableIndex(iteration);

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
        
        return switch (settings.r3aSelectionMethod()) {
            case LOWEST -> {
                LightR3A r3a = null;

                for(LightR3A test : table){
                    if (maxSkip >= test.skip() && test.isValid(dzr, dzi)) {
                        r3a = test;
                    }else yield r3a;
                }
                yield r3a;
            }
            case HIGHEST -> {

                LightR3A r3a = table.getFirst();
                if(!r3a.isValid(dzr, dzi)){
                    yield null;
                }

                for (int j = table.size() - 1; j >= 0; j--) {
                    LightR3A test = table.get(j);
                    if (maxSkip >= test.skip() && test.isValid(dzr, dzi)) {
                        yield test;
                    }
                }
                
                yield r3a;

            }
        };

    }


}
