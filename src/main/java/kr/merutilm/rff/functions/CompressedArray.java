package kr.merutilm.rff.functions;

import java.util.List;

public class CompressedArray<T> extends ArrayCompressor{
    private final T[] array;

    public CompressedArray(T[] array, List<ArrayCompressionTool> compressor){
        super(compressor);
        this.array = array;
    }

    public T[] getArray() {
        return array;
    }
    
    public T get(int index){
        return array[compress(index)];
    }

}
