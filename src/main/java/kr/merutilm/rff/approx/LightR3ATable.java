package kr.merutilm.rff.approx;

import java.util.*;
import java.util.function.BiConsumer;

import kr.merutilm.rff.formula.LightMandelbrotReference;
import kr.merutilm.rff.formula.ArrayCompressor;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.R3ACompressionMethod;
import kr.merutilm.rff.settings.R3ASettings;
public class LightR3ATable extends R3ATable{


    private final List<List<LightR3A>> table;
    private final List<ArrayCompressor> r3aCompressors;
    private final R3ASettings settings;
    private final int[] tablePeriod;
    private final int[] tablePeriodElements;

    public LightR3ATable(ParallelRenderState state, int currentID, LightMandelbrotReference reference, R3ASettings r3aSettings, double dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException {
        
        int[] referencePeriod = reference.period();
        int longestPeriod = reference.longestPeriod();
        int minSkip = r3aSettings.minSkipReference();
        
        if(longestPeriod < minSkip){
            this.settings = r3aSettings;
            this.tablePeriod = new int[0];
            this.tablePeriodElements = new int[0];
            this.table = Collections.emptyList();
            this.r3aCompressors = Collections.emptyList();
            return;
        }

        R3ACompressionMethod compressionMethod = r3aSettings.r3aCompressionMethod();
        PeriodTemp r3aPeriod = R3ATable.PeriodTemp.generateR3APeriod(referencePeriod, r3aSettings);
        int[] tablePeriodElements = generatePeriodElements(r3aPeriod.tablePeriod());
        List<ArrayCompressor> r3aCompressors = generateR3ACompressors(r3aPeriod.tablePeriod(), tablePeriodElements, reference.refCompressors());        
        List<List<LightR3A>> table = generateTable(state, currentID, reference, r3aSettings, r3aPeriod, r3aCompressors, tablePeriodElements, dcMax, compressionMethod, actionPerCreatingTableIteration);
        
        this.settings = r3aSettings;
        this.tablePeriod = r3aPeriod.tablePeriod();
        this.tablePeriodElements = tablePeriodElements;
        this.table = table;
        this.r3aCompressors = r3aCompressors;
        
    }



    private static List<List<LightR3A>> generateTable(ParallelRenderState state, int currentID, LightMandelbrotReference reference, R3ASettings r3aSettings, PeriodTemp periodTemp, List<ArrayCompressor> r3aCompressors, int[] tablePeriodElements, double dcMax, R3ACompressionMethod compressionMethod, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException{
        List<List<LightR3A>> table = new ArrayList<>();
        int[] tablePeriod = periodTemp.tablePeriod();
        int longestPeriod = tablePeriod[tablePeriod.length - 1];
        int[] periodCount = new int[tablePeriod.length];
        LightR3A.Builder[] currentStep = new LightR3A.Builder[tablePeriod.length];
        double epsilon = Math.pow(10, r3aSettings.epsilonPower());

        for (int i = 1; i <= longestPeriod; i++) {

            state.tryBreak(currentID);
            actionPerCreatingTableIteration.accept(i, (double) i / longestPeriod);
            

            for (int j = tablePeriod.length - 1; j >= 0; j--) {

                int requiredPerturbationCount = R3ATable.getRequiredPerturbationCount(periodTemp.isArtificial(), j);
                
                if(periodCount[j] == 0){
                    currentStep[j] = LightR3A.Builder.create(i);
                }

                if(periodCount[j] + requiredPerturbationCount < tablePeriod[j]){
                    currentStep[j] = currentStep[j].step(reference, epsilon, dcMax);
                }
                
                
                periodCount[j]++;

                if(periodCount[j] == tablePeriod[j]){
                    for(int k = j; k >= 0; k--){
                        
                        //Stop all lower level iteration for efficiency
                        //because it is too hard to skipping to next part of the periodic point
                        LightR3A.Builder currentLevel = currentStep[k];
                        
                        if(currentLevel == null){
                            continue;
                        }
                       
                        
                        if(periodCount[k] == tablePeriod[k]){
                            //If the skip count is lower than its current period,
                            //it can be replaced to several lower-period RRA.
                            
                            int index = switch(compressionMethod){
                                case NO_COMPRESSION -> currentLevel.start();
                                case LITTLE_COMPRESSION -> iterationToTableIndex(tablePeriod, tablePeriodElements, Collections.emptyList(), currentLevel.start());
                                case STRONGEST -> iterationToTableIndex(tablePeriod, tablePeriodElements, r3aCompressors, currentLevel.start());
                                default -> -1;
                            };
                            
                            while(table.size() <= index){
                                table.add(new ArrayList<>());
                            }

                            List<LightR3A> r3a = table.get(index);
                            if(k == r3a.size()){ //prevent to add duplicate elements
                                r3a.add(currentLevel.build());
                            }
                            
                        }

                        currentStep[k] = null;
                        periodCount[k] = 0;
                    }
                    break;
                }
            }    
        }
        return table.stream().map(Collections::unmodifiableList).toList();
    }


    public LightR3A lookup(int iteration, double dzr, double dzi) {
        if(iteration == 0){
            return null;
        }
        
        int index = switch(settings.r3aCompressionMethod()){
            case NO_COMPRESSION -> iteration;
            case LITTLE_COMPRESSION -> iterationToTableIndex(tablePeriod, tablePeriodElements, Collections.emptyList(), iteration);
            case STRONGEST -> iterationToTableIndex(tablePeriod, tablePeriodElements, r3aCompressors, iteration);
            default -> -1;
        };
        if(index == -1 || index >= table.size()){
            return null;
        }

        int longestPeriod = tablePeriod[tablePeriod.length - 1];
        int maxSkip = longestPeriod;
        int remainder = iteration;

        for(int i = tablePeriod.length - 1; i >= 0; i--){
            int p = tablePeriod[i];
            remainder %= p;
            if(remainder == 1){
                maxSkip = tablePeriod[i];
                break;
            }
        } //TODO : SOLVE BUG
        

        
        List<LightR3A> table = this.table.get(index);

        if(table.isEmpty()){
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
