package kr.merutilm.rff.parallel;

import java.io.Serial;

public final class IllegalParallelRenderStateException extends Exception{
    @Serial
    private static final long serialVersionUID = -99611161360770158L;

    public IllegalParallelRenderStateException(){
        super();
    }

    public IllegalParallelRenderStateException(String s){
        super(s);
    }
}
