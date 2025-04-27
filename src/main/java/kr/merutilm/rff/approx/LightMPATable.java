package kr.merutilm.rff.approx;

import kr.merutilm.rff.formula.LightMandelbrotReference;
import kr.merutilm.rff.functions.ArrayCompressionTool;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.MPASettings;
import kr.merutilm.rff.util.AdvancedMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class LightMPATable extends MPATable {

    private final List<List<LightPA>> table;

    public LightMPATable(ParallelRenderState state, int currentID, LightMandelbrotReference reference, MPASettings MPASettings, double dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException {
        super(reference, MPASettings);
        this.table = createTable(state, currentID, reference, dcMax, actionPerCreatingTableIteration);
    }

    private List<List<LightPA>> createTable(ParallelRenderState state, int currentID, LightMandelbrotReference reference, double dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException {

        if (mpaPeriod == null) {
            return Collections.emptyList();
        }

        int[] tablePeriod = mpaPeriod.tablePeriod();
        int longestPeriod = tablePeriod[tablePeriod.length - 1];
        int[] tableElements = mpaPeriod.tableElements();

        if (longestPeriod < settings.minSkipReference()) {
            return Collections.emptyList();
        }

        List<List<LightPA>> table = new ArrayList<>();
        double epsilon = Math.pow(10, settings.epsilonPower());


        int iteration = 1;

        // Period sample
        // Period :      16 | 64 | 652 | 2608 | 10432 |  55432
        // IsArtificial : T |  F |   F |    T |     T |      F
        //
        // Uncompressed Array Construction
        //
        // MPA period : [16, 64, 652, 2608, 10432, 55432]
        // period count array init : [0, 0, 0, 0, 0, 0]
        // iteration count start value : 1
        //
        // isArtificial = true,
        // iteration 16, period count Array [16, 16, 16, 16, 16, 16]
        // 16 is matched, reset to 0 for all lower level
        // [0, 16, 16, 16, 16, 16]
        // wait until iteration is reached to 16
        //
        // same operation
        // [0, 32, 32, 32, 32, 32]
        // wait until iteration is reached to 16 * 2 = 32
        //
        // iteration 64, period count Array [16, 64, 64, 64, 64, 64]
        // [0, 0, 64, 64, 64, 64]
        //
        // iteration 128, period count Array [16, 64, 128, 128, 128, 128]
        // [0, 0, 128, 128, 128, 128]
        //
        // repeat this
        // [A, B, C, D, E, 55430] = END
        //


        int[] periodCount = new int[tablePeriod.length];
        LightPA.Builder[] currentStep = new LightPA.Builder[tablePeriod.length];
        List<LightPA> mainReferenceMPA = Collections.emptyList();

        while (iteration <= longestPeriod) {

            state.tryBreak(currentID);
            actionPerCreatingTableIteration.accept(iteration, (double) iteration / longestPeriod);
            int pulledTableIndex = iterationToPulledTableIndex(mpaPeriod, iteration);
            boolean independent = pulledMPACompressor.isIndependent(pulledTableIndex);
            int containedIndex = pulledMPACompressor.containedIndex(pulledTableIndex + 1);
            ArrayCompressionTool containedTool = containedIndex == -1 ? null : pulledMPACompressor.tools().get(containedIndex);

            if (containedTool != null && containedTool.start() == pulledTableIndex + 1) {
                int level = Arrays.binarySearch(tableElements, containedTool.end() - containedTool.start() + 2);
                //count itself and periodic point, +2

                int compTableIndex = iterationToCompTableIndex(iteration);
                safetyMatchTableSize(table, compTableIndex);

                List<LightPA> pa = table.get(compTableIndex);
                LightPA mainReferencePA = mainReferenceMPA.get(level);
                int skip = mainReferencePA.skip();

                for (int i = 0; i < currentStep.length; i++) {
                    if(i <= level){
                        pa.add(mainReferenceMPA.get(i));
                        int count = skip;
                        for (int j = level - 1; j >= i; j--) {
                            count %= tablePeriod[j];
                        }

                        periodCount[i] = count;
                    } else {
                        if(currentStep[i] == null) {
                            //its count is zero but has no element? -> Artificial PA
                            currentStep[i] = LightPA.Builder.create(reference, epsilon, dcMax, iteration).merge(mainReferencePA);
                        }else{
                            currentStep[i].merge(mainReferencePA);
                        }
                        periodCount[i] += skip;
                    }
                }

                iteration += skip;
            }

            for (int i = tablePeriod.length - 1; i >= 0; i--) {

                if (periodCount[i] == 0 && independent) {
                    currentStep[i] = LightPA.Builder.create(reference, epsilon, dcMax, iteration);
                }

                if (currentStep[i] != null && periodCount[i] + REQUIRED_PERTURBATION < tablePeriod[i]) {
                    if (i + 1 < tablePeriod.length && periodCount[i] == periodCount[i + 1]) {
                        currentStep[i] = currentStep[i + 1];
                    } else {
                        currentStep[i] = currentStep[i].step();
                    }
                }


                periodCount[i]++;

                if (periodCount[i] == tablePeriod[i]) {
                    for (int j = i; j >= 0; j--) {

                        //Stop all lower level iteration for efficiency
                        //because it is too hard to skipping to next part of the periodic point
                        LightPA.Builder currentLevel = currentStep[j];

                        if (currentLevel != null && periodCount[j] == tablePeriod[j]) {
                            //If the skip count is lower than its current period,
                            //it can be replaced to several lower-period RRA.

                            int compTableIndex = iterationToCompTableIndex(currentLevel.start());
                            safetyMatchTableSize(table, compTableIndex);

                            List<LightPA> pa = table.get(compTableIndex);
                            pa.add(currentLevel.build());

                            if (compTableIndex == 0) {
                                mainReferenceMPA = pa;
                            }
                        }

                        currentStep[j] = null;
                        periodCount[j] = 0;
                    }
                    break;
                }
            }

            iteration++;

        }

        return Collections.unmodifiableList(table);
    }


    public LightPA lookup(int iteration, double dzr, double dzi) {

        if (iteration == 0 || mpaPeriod == null) {
            return null;
        }
        int index = iterationToCompTableIndex(iteration);

        if (index == -1 || index >= table.size()) {
            return null;
        }
        int[] tablePeriod = mpaPeriod.tablePeriod();
        int longestPeriod = tablePeriod[tablePeriod.length - 1];
        int maxSkip = longestPeriod - iteration;


        List<LightPA> table = this.table.get(index);

        if (table == null || table.isEmpty()) {
            return null;
        }

        double r = AdvancedMath.hypotApproximate(dzr, dzi);

        return switch (settings.mpaSelectionMethod()) {
            case LOWEST -> {
                LightPA r3a = null;

                for (LightPA test : table) {
                    if (maxSkip >= test.skip() && test.isValid(r)) {
                        r3a = test;
                    } else yield r3a;
                }
                yield r3a;
            }
            case HIGHEST -> {

                LightPA r3a = table.getFirst();
                if (!r3a.isValid(r)) {
                    yield null;
                }

                for (int j = table.size() - 1; j >= 0; j--) {
                    LightPA test = table.get(j);
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
