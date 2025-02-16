package kr.merutilm.rff.shader;

import java.io.Serial;

public final class IllegalRenderStateException extends Exception{
    @Serial
    private static final long serialVersionUID = -99611161360770158L;

    public IllegalRenderStateException(){
        super();
    }

    public IllegalRenderStateException(String s){
        super(s);
    }
}
