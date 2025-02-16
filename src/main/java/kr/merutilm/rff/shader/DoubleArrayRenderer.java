package kr.merutilm.rff.shader;

public interface DoubleArrayRenderer extends ArrayRenderer{
    double execute(int x, int y, int xRes, int yRes, double rx, double ry, int i, double c, double t) throws IllegalRenderStateException;

}
