package kr.merutilm.rff.parallel;

@FunctionalInterface
public interface ParallelDoubleArrayRenderer extends ParallelArrayRenderer{
    double execute(int x, int y, int xRes, int yRes, double rx, double ry, int i, double c, double t) throws IllegalParallelRenderStateException;

}
