package kr.merutilm.rff.formula;

import java.util.List;

public record ReferenceCompressor(int startReferenceIndex, int length, int startIteration, int endIteration) {
    
    public static boolean isFullyCompressed(List<ReferenceCompressor> compressors, int startIteration, int endIteration){
        int i1 = containedIterationIndex(compressors, startIteration);
        int i2 = containedIterationIndex(compressors, endIteration);
        return i1 == i2 && i1 != -1;
    }

    public static ReferenceCompressor containedIterationCompressor(List<ReferenceCompressor> compressors, int iteration){
        int index = containedIterationIndex(compressors, iteration);
        if(index == -1){
            return null;
        }
        return compressors.get(index);
    }

    public static int containedIterationIndex(List<ReferenceCompressor> compressors, int iteration){
        return binarySearch(compressors, iteration, 0, (compressors.size() + 1) / 2, compressors.size());
    }

    

    private static int binarySearch(List<ReferenceCompressor> compressors, int iteration, int index, int indexGap, int lastIndexGap){
        if(index < 0 || index >= compressors.size() || compressors.get(0).startIteration() > iteration){
            return -1;
        }

        ReferenceCompressor current = compressors.get(index);
        boolean requiredSmallerIndex = current.startIteration() > iteration;
        boolean requiredLargerIndex = current.endIteration() < iteration;

        if(indexGap == lastIndexGap && (requiredLargerIndex || requiredSmallerIndex)){
            return -1;
        }

        if(requiredSmallerIndex){
            return binarySearch(compressors, iteration, index - indexGap, (indexGap + 1) / 2, indexGap);
        }
        if(requiredLargerIndex){
            return binarySearch(compressors, iteration, index + indexGap, (indexGap + 1) / 2, indexGap);
        }
        
        return index;
    }

    /**
     * Get the index of reference using compressors and given iteration.
     * @param compressors the list of compressors
     * @param iteration the iteration you want
     * @return
     */
    public static int compress(List<ReferenceCompressor> compressors, int iteration) {

        if(compressors.isEmpty() || iteration < compressors.get(0).startIteration()){
            return iteration;
        }

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
        int compressedIteration = iteration;


        for (int i = compressors.size() - 1; i >= 0; i--) {
            ReferenceCompressor compressor = compressors.get(i);
            if (compressor.startIteration() <= compressedIteration && compressedIteration <= compressor.endIteration()) {
                compressedIteration -= compressor.startIteration() - compressor.startReferenceIndex();
            }else if (compressedIteration > compressor.endIteration()){
                break;
            }
        }

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
        // Approach : Subtract the length of compressors. It is already compressed iteration, DO NOT consider that includes compressors' iteration


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
}
