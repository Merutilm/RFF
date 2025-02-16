package kr.merutilm.rff.shader;

public interface ProcessVisualizer{
    void run(double progress) throws IllegalRenderStateException, InterruptedException;

    static ProcessVisualizer na(){
        return a -> {};
    }
}
