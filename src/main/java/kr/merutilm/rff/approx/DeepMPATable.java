package kr.merutilm.rff.approx;

import kr.merutilm.rff.formula.DeepMandelbrotReference;
import kr.merutilm.rff.functions.ArrayCompressionTool;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.settings.MPASettings;
import kr.merutilm.rff.struct.DoubleExponent;
import kr.merutilm.rff.util.DoubleExponentMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class DeepMPATable extends MPATable {

    private final List<List<DeepPA>> table;

    public DeepMPATable(ParallelRenderState state, int currentID, DeepMandelbrotReference reference, MPASettings MPASettings, DoubleExponent dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException {
        super(reference, MPASettings);
        this.table = createTable(state, currentID, reference, dcMax, actionPerCreatingTableIteration);
    }

    private List<List<DeepPA>> createTable(ParallelRenderState state, int currentID, DeepMandelbrotReference reference, DoubleExponent dcMax, BiConsumer<Integer, Double> actionPerCreatingTableIteration) throws IllegalParallelRenderStateException {

        if (mpaPeriod == null) {
            return Collections.emptyList();
        }

        int[] tablePeriod = mpaPeriod.tablePeriod();
        int longestPeriod = tablePeriod[tablePeriod.length - 1];
        int[] tableElements = mpaPeriod.tableElements();

        int[] periodCount = new int[tablePeriod.length];

        if (longestPeriod < settings.minSkipReference()) {
            return Collections.emptyList();
        }

        List<List<DeepPA>> table = new ArrayList<>();
        double epsilon = Math.pow(10, settings.epsilonPower());
        int iteration = 1;

        DeepPA.Builder[] currentPA = new DeepPA.Builder[tablePeriod.length];
        List<DeepPA> mainReferenceMPA = Collections.emptyList();


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

                List<DeepPA> pa = table.get(compTableIndex);
                DeepPA mainReferencePA = mainReferenceMPA.get(level);
                int skip = mainReferencePA.skip();

                for (int i = 0; i < currentPA.length; i++) {
                    if (i <= level) {
                        pa.add(mainReferenceMPA.get(i));
                        int count = skip;
                        for (int j = level - 1; j >= i; j--) {
                            count %= tablePeriod[j];
                        }

                        periodCount[i] = count;
                    } else {
                        if (currentPA[i] == null) {
                            //its count is zero but has no element? -> Artificial PA
                            currentPA[i] = DeepPA.Builder.create(reference, epsilon, dcMax, iteration).merge(mainReferencePA);
                        } else {
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
                    currentPA[i] = DeepPA.Builder.create(reference, epsilon, dcMax, iteration);
                }

                if (currentPA[i] != null && periodCount[i] + REQUIRED_PERTURBATION < tablePeriod[i]) {
                    currentPA[i] = currentPA[i].step();
                }


                periodCount[i]++;

                if (periodCount[i] == tablePeriod[i]) {


                    DeepPA.Builder currentLevel = currentPA[i];

                    if (currentLevel != null && currentLevel.skip() == tablePeriod[i] - REQUIRED_PERTURBATION) {
                        int compTableIndex = iterationToCompTableIndex(currentLevel.start());
                        safetyMatchTableSize(table, compTableIndex);

                        List<DeepPA> pa = table.get(compTableIndex);
                        pa.add(currentLevel.build());

                        if (compTableIndex == 0) {
                            mainReferenceMPA = pa;
                        }
                    }

                    currentPA[i] = null;
                    resetLowerLevel = true;
                }

                if (resetLowerLevel) {
                    periodCount[i] = 0;
                }

            }
            resetLowerLevel = false;
            iteration++;
        }
        return Collections.unmodifiableList(table);
    }


    public DeepPA lookup(int iteration, DoubleExponent dzr, DoubleExponent dzi) {

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

        List<DeepPA> table = this.table.get(index);
        if(table == null){
            return null;
        }

        DoubleExponent r = DoubleExponentMath.hypotApproximate(dzr, dzi);

        return switch (settings.mpaSelectionMethod()) {
            case LOWEST -> {
                DeepPA r3a = null;

                for (DeepPA test : table) {
                    if (maxSkip >= test.skip() && test.isValid(r)) {
                        r3a = test;
                    } else yield r3a;
                }
                yield r3a;
            }
            case HIGHEST -> {

                DeepPA r3a = table.getFirst();
                if (!r3a.isValid(r)) {
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
