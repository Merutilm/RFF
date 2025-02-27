package kr.merutilm.rff.parallel;

@FunctionalInterface
public interface ParallelRenderProcessVisualizer{
    void run(double progress) throws IllegalParallelRenderStateException, InterruptedException;
}
