package kr.merutilm.rff.functions;

import java.util.Collections;
import java.util.List;

/**
 * <h2>Array tools</h2>
 * A class for compressing when detecting duplicated elements in an array. <p>
 * It can be used if there are duplicate values in the array. <p>
 * <b> The compression process in this is divided into two steps: </b> <p>
 * <li> 1. rebases the given index based on its data. <p>
 * <li> 2. pulls the subsequent elements to fill the empty space. <p>
 */
public class ArrayCompressor {
    
    private final List<ArrayCompressionTool> tools;

    public ArrayCompressor(List<ArrayCompressionTool> tools){
        this.tools = Collections.unmodifiableList(tools);
    }

    /**
     * Checks whether the given index is independant. <p>
     * The time complexity is {@code O(log N)} because it uses {@link ArrayCompressor#binarySearch(int, int, int, int) binary-search}.
     * @see ArrayCompressor#containedIndex(int) containedIndex
     * @see ArrayCompressor#binarySearch(int, int, int, int) binarySearch
     * @param index To checking index
     * @return {@code true} when given index is independent.
     */
    public boolean isIndependent(int index){
        return containedIndex(index) == -1;
    }

    /**
     * Rebases the given index. <p>
     * The rebased index is not the compressed index because it is a middle step that has not proceeded the pulling process. <p>
     * To get the final index, use the {@link ArrayCompressor#pull(int) pull()} method.
     * @param index To rebasing index
     * @return The rebased index.
     */
    public int rebase(int index){
        if(tools.isEmpty() || index < tools.getFirst().start() || index > tools.getLast().end()){
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
        int rebased = index;


        for (int i = tools.size() - 1; i >= 0; i--) {
            ArrayCompressionTool tool = tools.get(i);
            if (tool.start() <= rebased && rebased <= tool.end()) {
                rebased -= tool.start() - tool.rebase();
            }else if (rebased > tool.end()){
                break;
            }
        }
        return rebased;
    }
    /**
     * Completes the compressed array by pulling subsequent elements into the void in the array made by the rebasing process. <p>
     * You can get the final index of the compressed array. <p>
     * <b>This method assumes that the given index has been completed the rebase process and the given list is the same as the list used in the rebasing process.</b>
     * @param index To pulling rebased index
     * @return The final compressed index.
     */
    public int pull(int index){
        // "Rebased" iteration is not a target of compressions.
        // The space created by compression is filled by pushing indices to front. This is the definition of this method.
        // CAUTION : given index must be not rebaseable.
        //
        // Example
        // let's assume we have two tools below.
        // 26 -> 39 = 1 -> 14,
        // 51 -> 65 = 1 -> 15
        //
        // the reference orbit will be shown :
        // 1 2 3 ... 25 26 27 28 ... 36 37 38 39 40 41 42 43 - index
        // 1 2 3 ... 25 40 41 42 ... 50 66 67 68 69 70 71 72 - actual iteration
        //
        // Approach : Subtract the length of tools. It is already compressed iteration, DO NOT consider that includes tools' iteration
        //

        int compressed = index;

        for (ArrayCompressionTool tool : tools) {
        
            // Since it already processed the cases within the range : startIteration < compressedIteration < endIteration,
            // there is no reason to consider that condition.
        
            if (index > tool.end()) {
                compressed -= tool.range();
            } else {
                break;
            }
        }

        return compressed;
    }

    /**
     * Gets the contained index of the given list. <p>
     * The time complexity is {@code O(log N)} because it uses {@link ArrayCompressor#binarySearch binary-search}.
     * @see ArrayCompressor#binarySearch(int, int, int, int) binarySearch
     * @param index To checking index
     * @return the index of the given list.
     */
    public int containedIndex(int index){
        return binarySearch(index, 0, (tools.size() + 1) / 2, tools.size());
    }

    
    /**
     * A single step of recursive binary-search to get the contained index of the list. <p>
     * 
     * @param index To checking index
     * @param compIndex The index of given list
     * @param indexGap The index gap
     * @param lastIndexGap The last index Gap
     * @return The index of the given list. returns {@code -1} if the given index is unrebaseable.
     */
    private int binarySearch(int index, int compIndex, int indexGap, int lastIndexGap){
        if(compIndex < 0 || compIndex >= tools.size() || tools.getFirst().start() > index){
            return -1;
        }

        ArrayCompressionTool current = tools.get(compIndex);
        boolean requiredSmallerIndex = current.start() > index;
        boolean requiredLargerIndex = current.end() < index;

        if(indexGap == lastIndexGap && (requiredLargerIndex || requiredSmallerIndex)){
            return -1;
        }

        if(requiredSmallerIndex){
            return binarySearch(index, compIndex - indexGap, (indexGap + 1) / 2, indexGap);
        }
        if(requiredLargerIndex){
            return binarySearch(index, compIndex + indexGap, (indexGap + 1) / 2, indexGap);
        }
        
        return compIndex;
    }

    /**
     * Gets the compressed index of compressed array.
     * @param index the index you want
     * @return Finally compressed index
     */
    public int compress(int index) {
        int rebased = rebase(index);
        return pull(rebased);
    }


    public List<ArrayCompressionTool> tools(){
        return tools;
    }

}
