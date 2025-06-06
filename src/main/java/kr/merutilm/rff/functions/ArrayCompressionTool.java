package kr.merutilm.rff.functions;

/**
 * <h2>Array Compression Tool</h2>
 * A class for compressing when detecting duplicated elements in an array. <p>
 * It can be used if there are duplicate values in the array. <p>
 * Given a range, rebase the first element in that range sequentially starting with index {@code 1}. 
 * <p><p>
 * (e.g., if the given ranges are {@code 11-15}, and the rebase index is {@code 1}, the first value in the range, {@code 11}, will be rebased to index {@code 1} when compressed. <p>
 * at the same time, the other elements will be also rebased {@code 12 → 2}, {@code 13 → 3}, {@code 14 → 4}, and {@code 15 → 5}.)

 * @param rebase the index to rebase
 * @param start the start index of uncompressed array
 * @param end the end index of uncompressed array
 */

public record ArrayCompressionTool(long rebase, long start, long end) {



    public ArrayCompressionTool {
        if(rebase >= start){
            throw new IllegalArgumentException("rebase index must be smaller than start index");
        }
    }

    /**
     * Gets the range of current tool : {@code end - start + 1}
     * @return The range of current tool.
     */
    public long range(){
        return end - start + 1;
    }
    
}
