package kr.merutilm.rff.approx;

import java.util.*;
import java.util.function.BiConsumer;

import kr.merutilm.rff.formula.LightMandelbrotReference;
import kr.merutilm.rff.formula.ReferenceCompressor;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.util.ArrayFunction;
import kr.merutilm.rff.settings.R3ASettings;
public class LightR3ATable implements R3ATable{

    private final List<List<LightR3A>> table;
    private final R3ASettings settings;
    private final List<ReferenceCompressor> compressors;

    public LightR3ATable(ParallelRenderState state, int currentID, LightMandelbrotReference reference, R3ASettings r3aSettings, double dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException {
        double epsilon = Math.pow(10, r3aSettings.epsilonPower());

        int longestPeriod = reference.period()[reference.period().length - 1];
        this.table = new ArrayList<>(Collections.nCopies(longestPeriod + 1, null));
        this.settings = r3aSettings;
        this.compressors = reference.compressors();


        int minSkip = r3aSettings.minSkipReference();
        int maxMultiplier = r3aSettings.maxMultiplierBetweenLevel();

        int[] periodTemp = new int[1];
        boolean[] isArtificial = new boolean[1];

        int periodArraySize = 1;
        int currentPeriod = minSkip;

        periodTemp[0] = currentPeriod;
        //first period is always minimum skip iteration.
        isArtificial[0] =  Arrays.binarySearch(reference.period(), currentPeriod) < 0;
        //and it is artificially-created period if generated period is not an element of generated period.

        for (int p : reference.period()) {

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

        int[] period = Arrays.copyOfRange(periodTemp, 0, periodArraySize);
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
                    currentStep[j] = LightR3A.create(i).step(reference, epsilon, dcMax); // step 1
                }else if(currentStep[j].skip() + requiredPerturbationCount == period[j]){
                    boolean canCompress = false;
                    for(int k = j; k >= 0; k--){
                        //Stop all lower level iteration for efficiency
                        //because it is too hard to skipping to next part of the periodic point
                        LightR3A currentLevel = currentStep[k];
                        
                        if(currentLevel == null){
                            continue;
                        }
                        if(!canCompress){
                            ReferenceCompressor currentCompressor = ReferenceCompressor.containedIterationCompressor(compressors, currentLevel.start());
                            
                            if(currentCompressor != null && currentLevel.start() + currentLevel.skip() <= currentCompressor.endIteration()){
                                //Since it is fully included in the compression range of a compressor, 
                                //it can be replaced with the "compressed iteration table." 
                                //Therefore, it is excluded from the list.
    
                                //The current "for statement" (period array) is looked up in order from largest to smallest. 
                                //It means the "skip value" is also looked up in order from largest to smallest. 
                                //That is, if the current element can be compressed, ALL elements following it can be compressed.
                                canCompress = true;
                            }else if(currentLevel.skip() + requiredPerturbationCount == period[k]){
                                //If the skip count is lower than its current period,
                                //it can be replaced to several lower-period RRA.
    
                                List<LightR3A> elem = table.get(currentLevel.start());
                                
    
                                if(elem == null){
                                    elem = new ArrayList<>();
                                }
    
                                elem.add(currentLevel);
                                table.set(currentLevel.start(), elem);
                            }
                        }

                        currentStep[k] = null;
                    }
                    break;
                }else {
                    if(j < currentStep.length - 1 && currentStep[j].start() == currentStep[j + 1].start()){
                        //reuse upper level if both start iterations are the same
                        currentStep[j] = currentStep[j + 1];
                    }else{
                        currentStep[j] = currentStep[j].step(reference, epsilon, dcMax);
                    }
                }
            }
        }
    }



    public LightR3A lookup(int iteration, double dzr, double dzi) {
        
        int compressedIteration = ReferenceCompressor.compress(compressors, iteration);
        
        List<LightR3A> table = this.table.get(iteration);
        List<LightR3A> compressedIterationTable = this.table.get(compressedIteration);

        if (table == null) {
            return null;
        }

        //If the skip value is larger than the compression size of a compressor, It is impossible to compress. 
        //Conversely, If the skip value is smaller than the compression size of a compressor, It is possible to compress, and will be replaced to low-level R3A.
        //That is, low-level R3A ​​can be excluded from the "Uncompressed iteration" table by pre-tasks, 
        //if it is compressed, the start Iteration is replaced to a compressed Iteration.

        //If the skip value is small, "Compressed Iteration" table is used.
        //On the other hand, if the skip value is large, compression is not possible, so the "Uncompressed iteration" table is used.
        //When iterating in order from the smallest to largest, the "Compressed iteration" table is used first. 
        //Conversely, when iterating in order from the largest to smallest,
        //Check the first element of the "compressed iteration" table can be used, 
        //and then the "Uncompressed Iteration" table is iterated in reverse order.
        ReferenceCompressor containedIterationCompressor = ReferenceCompressor.containedIterationCompressor(compressors, iteration);
        
        int compressorEndIteration = containedIterationCompressor == null ? 0 : containedIterationCompressor.endIteration();
        //compressorEndIteration = 0 means compressedIteration = iteration because given iteration cannot be compressed

        return switch (settings.r3aSelectionMethod()) {
            case LOWEST -> {
                LightR3A r3a = null;

                if(compressedIterationTable != null){
                    for (LightR3A test : compressedIterationTable) {
                        if(compressedIterationTable == table || iteration + test.skip() > compressorEndIteration){
                            break;
                        }
                        if (test.isValid(dzr, dzi)) {
                            r3a = test;
                        }else yield r3a;
                    } 
                }
                
                for(LightR3A test : table){
                    if (test.isValid(dzr, dzi)) {
                        r3a = test;
                    }else yield r3a;
                }
                yield r3a;
            }
            case HIGHEST -> {

                LightR3A r3a = compressedIterationTable == null ? table.getFirst() : compressedIterationTable.getFirst();
                if(!r3a.isValid(dzr, dzi)){
                    yield null;
                }

                for (int j = table.size() - 1; j >= 0; j--) {
                    LightR3A test = table.get(j);
                    if (test.isValid(dzr, dzi)) {
                        yield test;
                    }
                }
                
                if(compressedIterationTable == table || compressedIterationTable == null){
                    yield r3a;
                }

                for(int j = compressedIterationTable.size() - 1; j >= 0; j--){
                    LightR3A test = compressedIterationTable.get(j);
                    //compressorEndIteration = -1 removed because already broken in if(compressedIterationTable == table)  state
                    if(iteration + test.skip() > compressorEndIteration){ 
                        continue;
                    }
                    if (test.isValid(dzr, dzi)) {
                        yield test;
                    }
                }
                yield r3a;
            }
        };

    }


}
