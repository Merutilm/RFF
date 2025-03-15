package kr.merutilm.rff.functions;

import java.util.List;

public class CompressedDoubleArray extends ArrayCompressor{
    private final double[] array;

    public CompressedDoubleArray(double[] array, List<ArrayCompressionTool> tools){
        super(tools);
        this.array = array;
    }

    public double[] getArray(){
        return array;
    }

    public double get(int index){
        return array[compress(index)];
    }

}
