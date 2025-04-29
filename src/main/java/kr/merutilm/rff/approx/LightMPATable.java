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

    public LightMPATable(ParallelRenderState state, int currentID, LightMandelbrotReference reference, MPASettings MPASettings, double dcMax, BiConsumer<Long, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException {
        super(reference, MPASettings);
        this.table = createTable(state, currentID, reference, dcMax, actionPerCreatingTableIteration);
    }

    private List<List<LightPA>> createTable(ParallelRenderState state, int currentID, LightMandelbrotReference reference, double dcMax, BiConsumer<Long, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException {

        if (mpaPeriod == null) {
            return Collections.emptyList();
        }

        long[] tablePeriod = mpaPeriod.tablePeriod();
        long longestPeriod = tablePeriod[tablePeriod.length - 1];
        long[] tableElements = mpaPeriod.tableElements();

        if (longestPeriod < settings.minSkipReference()) {
            return Collections.emptyList();
        }

        List<List<LightPA>> table = new ArrayList<>();
        double epsilon = Math.pow(10, settings.epsilonPower());
        long iteration = 1;

        long[] periodCount = new long[tablePeriod.length];
        LightPA.Builder[] currentPA = new LightPA.Builder[tablePeriod.length];
        List<LightPA> mainReferenceMPA = Collections.emptyList();
        while (iteration <= longestPeriod) {

            state.tryBreak(currentID);
            actionPerCreatingTableIteration.accept(iteration, (double) iteration / longestPeriod);
            long pulledTableIndex = iterationToPulledTableIndex(mpaPeriod, iteration);
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
                long skip = mainReferencePA.skip();

                for (int i = 0; i < currentPA.length; i++) {
                    if(i <= level){
                        pa.add(mainReferenceMPA.get(i));
                        long count = skip;
                        for (int j = level - 1; j >= i; j--) {
                            count %= tablePeriod[j];
                        }

                        periodCount[i] = count;
                    } else {
                        if(currentPA[i] == null) {
                            //its count is zero but has no element? -> Artificial PA
                            currentPA[i] = LightPA.Builder.create(reference, epsilon, dcMax, iteration).merge(mainReferencePA);
                        }else{
                            currentPA[i].merge(mainReferencePA);
                        }
                        periodCount[i] += skip;
                    }
                }

                iteration += skip;
            }
            boolean resetLowerLevel = false;

            for (int i = tablePeriod.length - 1; i >= 0; i--) {

                if (periodCount[i] == 0 && independent) {
                    currentPA[i] = LightPA.Builder.create(reference, epsilon, dcMax, iteration);
                }

                if (currentPA[i] != null && periodCount[i] + REQUIRED_PERTURBATION < tablePeriod[i]) {
                    if (i + 1 < tablePeriod.length && periodCount[i] == periodCount[i + 1]) {
                        currentPA[i] = currentPA[i + 1];
                    } else {
                        currentPA[i] = currentPA[i].step();
                    }
                }


                periodCount[i]++;

                if (periodCount[i] == tablePeriod[i]) {

                    LightPA.Builder currentLevel = currentPA[i];

                    if (currentLevel != null && currentLevel.skip() == tablePeriod[i] - REQUIRED_PERTURBATION) {

                        //If the skip count is lower than its current period - perturbation,
                        //it can be replaced to several lower-level PA.

                        int compTableIndex = iterationToCompTableIndex(currentLevel.start());
                        safetyMatchTableSize(table, compTableIndex);

                        List<LightPA> pa = table.get(compTableIndex);
                        pa.add(currentLevel.build());

                        if (compTableIndex == 0) {
                            mainReferenceMPA = pa;
                        }
                    }
                    //Stop all lower level iteration for efficiency
                    //because it is too hard to skipping to next part of the periodic point
                    currentPA[i] = null;
                    resetLowerLevel = true;
                }

                if(resetLowerLevel){
                    periodCount[i] = 0;
                }
            }
            iteration++;

        }

        return Collections.unmodifiableList(table);
    }


    public LightPA lookup(long refIteration, double dzr, double dzi) {

        if (refIteration == 0 || mpaPeriod == null) {
            return null;
        }
        int index = iterationToCompTableIndex(refIteration);

        if (index == -1 || index >= table.size()) {
            return null;
        }
        long[] tablePeriod = mpaPeriod.tablePeriod();
        long longestPeriod = tablePeriod[tablePeriod.length - 1];
        long maxSkip = longestPeriod - refIteration;

        List<LightPA> table = this.table.get(index);
        if(table == null){
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
                //This table cannot be empty because the pre-processing is done.

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
