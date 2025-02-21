package kr.merutilm.rff.shader;

@FunctionalInterface
public interface ProcessVisualizer{
    void run(double progress) throws IllegalRenderStateException, InterruptedException;
}
