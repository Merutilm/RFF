package kr.merutilm.fractal.approx;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.parallel.RenderState;
import kr.merutilm.fractal.settings.BLASettings;
import kr.merutilm.fractal.struct.DoubleExponent;
import kr.merutilm.fractal.util.DoubleExponentMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DeepBLATable implements BLATable{
    public final int iterationInterval;

    private final DeepBLA[] table;
    private final int[] indices;
    private final int max;

    public DeepBLATable(RenderState state, int currentID, BLASettings blaSettings, DoubleExponent[] zr, DoubleExponent[] zi, DoubleExponent dcMax) throws IllegalRenderStateException {
        List<DeepBLA> table = new ArrayList<>();
        int minLevel = blaSettings.minLevel();
        this.max = zr.length;
        this.iterationInterval = (int) Math.pow(2, minLevel);
        DoubleExponent epsilon = DoubleExponentMath.pow10(blaSettings.epsilonPower());

        for (int i = 1; i <= max - iterationInterval; i += iterationInterval) {
            List<DeepBLA> merged = new ArrayList<>();
            for (int j = 0; j < iterationInterval; j++) {
                merged.add(new DeepBLASingle(i + j, zr[i + j], zi[i + j], dcMax, epsilon));
            }
            while (merged.size() > 1) {

                List<DeepBLA> temp = new ArrayList<>();
                for (int j = 0; j < merged.size(); j += 2) {
                    temp.add(new DeepBLAMerged(merged.get(j), merged.get(j + 1), dcMax));
                }
                merged = temp;
            }

            table.add(merged.get(0));

            state.tryBreak(currentID);
        }

        List<DeepBLA> higherBLA = table;

        while (higherBLA.size() > 1) {
            List<DeepBLA> higherBLATemp = new ArrayList<>();

            for (int j = 0; j <= higherBLA.size() - 2; j += 2) {
                higherBLATemp.add(new DeepBLAMerged(higherBLA.get(j), higherBLA.get(j + 1), dcMax));
            }
            table.addAll(higherBLATemp);
            higherBLA = higherBLATemp;
            state.tryBreak(currentID);
        }
        table = table.stream().sorted(Comparator.comparing(BLA::targetIter)).toList();

        indices = new int[max];
        Arrays.fill(indices, -1);
        for (int i = 0; i < table.size(); i++) {
            int iteration = table.get(i).targetIter();
            if (indices[iteration] == -1) {
                indices[iteration] = i;
            }
            state.tryBreak(currentID);
        }

        this.table = table.toArray(DeepBLA[]::new);

    }


    public DeepBLA lookup(int iteration, DoubleExponent dzr, DoubleExponent dzi) {
        if (iteration >= max - iterationInterval) {
            return null;
        }

        int i = indices[iteration];
        int iNext = indices[iteration + iterationInterval];
        iNext = iNext == -1 ? table.length - 1 : iNext;


        if (i == -1) {
            return null;
        }

        if(!table[i].isValid(dzr, dzi)){
            return null;
        }

        for (int j = iNext - 1; j >= i; j--) {
            if (table[j].isValid(dzr, dzi)) {
                return table[j];
            }
        }
        return null;
    }
}
