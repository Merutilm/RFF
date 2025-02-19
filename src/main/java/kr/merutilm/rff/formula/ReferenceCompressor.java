package kr.merutilm.rff.formula;

import java.util.List;

public record ReferenceCompressor(int startReferenceIndex, int length, int startIteration, int endIteration) {

    private static int iterationToCompressedIteration(List<ReferenceCompressor> compressors, int iteration) {

        // Since large value are compressed to small, and it compresses again.

        // Example
        // 10000 -> 15000 -> 1 -> 5001
        // 1000 -> 1500 -> 1 -> 501
        // 100 -> 150 = 1 -> 51
        //
        // test input : 11111
        // 11111 -> 11111 - 10000 + 1 = 1112
        // 1112 -> 1112 - 1000 + 1 = 113,
        // 113 -> 113 - 100 + 1 = 14
        //
        // test input : 1400
        // 1400 -> 1400 - 1000 + 1 = 401

        // Use the REVERSED FOR statement because it must be used recursively.
        
        for (int i = compressors.size() - 1; i >= 0; i--) {
            ReferenceCompressor compressor = compressors.get(i);
            if (compressor.startIteration() <= iteration && iteration <= compressor.endIteration()) {
                iteration -= compressor.startIteration() - compressor.startReferenceIndex();
            }
        }

        return iteration;
    }

    private static int compressedIterationToReferenceIndex(List<ReferenceCompressor> compressors, int compressedIteration) {

        // "Compressed" iteration is not a target of compressions.
        // The space created by compression is filled by pushing indices to front. This is the definition of this method.
        //
        // Example
        // let's assume we have two compressors below.
        // 26 -> 39 = 1 -> 14,
        // 51 -> 65 = 1 -> 15
        //
        // the reference orbit will be shown :
        // 1 2 3 ... 25 26 27 28 ... 36 37 38 39 40 41 42 43 - index
        // 1 2 3 ... 25 40 41 42 ... 50 66 67 68 69 70 71 72 - actual iteration
        //
        // Approach : Check if the iteration is included in compressors
        //
        // test input : 60 (expected : 10)
        // second compressor : 51 -> 65 = 1 -> 15,
        // therefore, this iteration is 60 - 51 + 1 = 10.

        int index = compressedIteration;

        for (ReferenceCompressor compressor : compressors) {

            // Since it already processed the cases within the range : startIteration < compressedIteration < endIteration,
            // there is no reason to consider that condition.

            if (compressedIteration > compressor.endIteration()) {
                index -= compressor.length();
            } else {
                break;
            }
        }

        return index;
    }

    public static int iterationToReferenceIndex(List<ReferenceCompressor> compressors, int iteration) {
        int compressedIteration = iterationToCompressedIteration(compressors, iteration);
        return compressedIterationToReferenceIndex(compressors, compressedIteration);
    }

}
