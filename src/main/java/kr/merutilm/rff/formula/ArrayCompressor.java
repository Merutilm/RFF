package kr.merutilm.rff.formula;

import java.util.List;

/**
 * A class for compressing when detecting duplicated elements in an array.
 * @param target the target index to compress. It must be smaller than start index.
 * @param start the start index of uncompressed array
 * @param end the end index of uncompressed array
 */
public record ArrayCompressor(int target, int start, int end) {
    
    public ArrayCompressor {
        if(target >= start){
            throw new IllegalArgumentException("target index must be smaller than start index");
        }
    }

    public int length(){
        return end - start + 1;
    }

    

    /**
     * Get the index of reference using compressors and given iteration.
     * @param compressors the list of compressors
     * @param index the index you want
     * @return
     */
    public static int compress(List<ArrayCompressor> compressors, int index) {

        if(compressors.isEmpty() || index < compressors.get(0).start()){
            return index;
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
        int compressedWithoutPulling = index;


        for (int i = compressors.size() - 1; i >= 0; i--) {
            ArrayCompressor compressor = compressors.get(i);
            if (compressor.start() <= compressedWithoutPulling && compressedWithoutPulling <= compressor.end()) {
                compressedWithoutPulling -= compressor.start() - compressor.target();
            }else if (compressedWithoutPulling > compressor.end()){
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
        //
        
        int compressed = compressedWithoutPulling;

        for (ArrayCompressor compressor : compressors) {

            // Since it already processed the cases within the range : startIteration < compressedIteration < endIteration,
            // there is no reason to consider that condition.

            if (compressedWithoutPulling > compressor.end()) {
                compressed -= compressor.length();
            } else {
                break;
            }
        }

        return compressed;
    }
}
