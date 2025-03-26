package kr.merutilm.rff.functions;

import kr.merutilm.rff.approx.R3A;


import java.util.List;

public class ReferenceCompressor<R extends R3A> extends ArrayCompressor{

    private final List<R> compressorR3A;
    public ReferenceCompressor(List<ArrayCompressionTool> tools, List<R> compressorR3A) {
        super(tools);
        this.compressorR3A = compressorR3A;
    }


    public R getMatchingR3A(int index){
        return compressorR3A.get(index);
    }


}
