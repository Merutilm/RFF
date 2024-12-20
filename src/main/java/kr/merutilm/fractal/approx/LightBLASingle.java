package kr.merutilm.fractal.approx;

import kr.merutilm.base.util.AdvancedMath;

public class LightBLASingle implements LightBLA {

    private final int targetIter;
    private final double anr;
    private final double ani;
    private final double bnr;
    private final double bni;
    private final int skip;
    private final double radius;

    public LightBLASingle(int targetIter, double zr, double zi, double epsilon, double dcMax) {
        this.targetIter = targetIter;
        this.anr = 2 * zr;
        this.ani = 2 * zi;
        this.bnr = 1;
        this.bni = 0;
        this.skip = 1;
        double rz = AdvancedMath.hypotApproximate(zr, zi);
        
        this.radius = Math.max(0, 2 * epsilon * rz - dcMax / (2 * rz));
    }

    @Override
    public double radius() {
        return radius;
    }

    @Override
    public int targetIter() {
        return targetIter;
    }

    @Override
    public double anr() {
        return anr;
    }

    @Override
    public double ani() {
        return ani;
    }

    @Override
    public double bnr() {
        return bnr;
    }

    @Override
    public int skip() {
        return skip;
    }

    @Override
    public double bni() {
        return bni;
    }

}
