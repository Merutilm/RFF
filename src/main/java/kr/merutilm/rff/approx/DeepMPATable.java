package kr.merutilm.rff.approx;

import kr.merutilm.rff.formula.DeepMandelbrotReference;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.MPASettings;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.util.DoubleExponentMath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class DeepMPATable extends MPATable {

    private final List<List<DeepPA>> table;

    public DeepMPATable(ParallelRenderState state, int currentID, DeepMandelbrotReference reference, MPASettings MPASettings, DoubleExponent dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException {
        super(reference, MPASettings);
        this.table = createTable(state, currentID, reference, dcMax, actionPerCreatingTableIteration);
    }

    private List<List<DeepPA>> createTable(ParallelRenderState state, int currentID, DeepMandelbrotReference reference, DoubleExponent dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException{

        if(mpaPeriod == null){
            return Collections.emptyList();
        }

        int[] tablePeriod = mpaPeriod.tablePeriod();
        
        int longestPeriod = tablePeriod[tablePeriod.length - 1];
        int[] periodCount = new int[tablePeriod.length];
        
        if(longestPeriod < settings.minSkipReference()){
            return Collections.emptyList();
        }
        
        List<List<DeepPA>> table = new ArrayList<>();
        DeepPA.Builder[] currentStep = new DeepPA.Builder[tablePeriod.length];
        double epsilon = Math.pow(10, settings.epsilonPower());
   

        int iteration = 1;
        
        while (iteration <= longestPeriod) {

            state.tryBreak(currentID);
            actionPerCreatingTableIteration.accept(iteration, (double) iteration / longestPeriod);

            boolean independent = pulledMPACompressor == null || pulledMPACompressor.isIndependent(iterationToPulledTableIndex(mpaPeriod, iteration));
            
            for (int j = tablePeriod.length - 1; j >= 0; j--) {


                if(periodCount[j] == 0 && independent){
                    currentStep[j] = DeepPA.Builder.create(reference, epsilon, dcMax, iteration);
                }

                if(currentStep[j] != null && periodCount[j] + REQUIRED_PERTURBATION < tablePeriod[j]){
                    currentStep[j] = currentStep[j].step();
                }
                
                
                periodCount[j]++;

                if(periodCount[j] == tablePeriod[j]){
                    for(int k = j; k >= 0; k--){
                        
                        DeepPA.Builder currentLevel = currentStep[k];
                        
                        if(currentLevel != null && periodCount[k] == tablePeriod[k]){
                            int index = iterationToCompTableIndex(currentLevel.start());
                            safetyMatchTableSize(table, index);
           
                            List<DeepPA> r3a = table.get(index);
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



    public DeepPA lookup(int iteration, DoubleExponent dzr, DoubleExponent dzi) {

        if(iteration == 0 || mpaPeriod == null){
            return null;
        }
        
        int index = iterationToCompTableIndex(iteration);

        if(index == -1 || index >= table.size()){
            return null;
        }
        int[] tablePeriod = mpaPeriod.tablePeriod();
        int longestPeriod = tablePeriod[tablePeriod.length - 1];
        int maxSkip = longestPeriod - iteration;
        
        List<DeepPA> table = this.table.get(index);
        
        if(table == null || table.isEmpty()){
            return null;
        }
        DoubleExponent r = DoubleExponentMath.hypotApproximate(dzr, dzi);
        
        return switch (settings.mpaSelectionMethod()) {
            case LOWEST -> {
                DeepPA r3a = null;

                for(DeepPA test : table){
                    if (maxSkip >= test.skip() && test.isValid(r)) {
                        r3a = test;
                    }else yield r3a;
                }
                yield r3a;
            }
            case HIGHEST -> {

                DeepPA r3a = table.getFirst();
                if(!r3a.isValid(r)){
                    yield null;
                }

                for (int j = table.size() - 1; j >= 0; j--) {
                    DeepPA test = table.get(j);
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
