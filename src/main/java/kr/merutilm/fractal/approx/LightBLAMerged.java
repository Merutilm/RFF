package kr.merutilm.fractal.approx;

import kr.merutilm.base.util.AdvancedMath;

public class LightBLAMerged implements LightBLA {

    public final LightBLA x;
    public final LightBLA y;
    private final int targetIter;
    private final int skip;

    private final double anr;
    private final double ani;
    private final double bnr;
    private final double bni;
    private final double radius;

    public LightBLAMerged(LightBLA x, LightBLA y, double dcMax) {
        this.x = x;
        this.y = y;
        if (x.targetIter() > y.targetIter()) {
            throw new IllegalArgumentException("X target Iteration must be equal or smaller than Y target Iteration");
        }
        if (x.targetIter() + x.skip() != y.targetIter()) {
            throw new IllegalArgumentException("Cannot Merge X and Y because X and Y are not adjacent to each other");
        }

        this.targetIter = x.targetIter();
        this.skip = x.skip() + y.skip();
        this.anr = y.anr() * x.anr() - y.ani() * x.ani();
        this.ani = y.anr() * x.ani() + y.ani() * x.anr();
        this.bnr = y.anr() * x.bnr() - y.ani() * x.bni() + y.bnr();
        this.bni = y.anr() * x.bni() + y.ani() * x.bnr() + y.bni();
        double rax = AdvancedMath.hypotApproximate(x.anr(), x.ani());
        double rbx = AdvancedMath.hypotApproximate(x.bnr(), x.bni());
        this.radius = rax == 0 ? 0 : Math.min(Math.max((y.radius() - rbx * dcMax) / rax, 0), x.radius());
    }

    @Override
    public int targetIter() {
        return targetIter;
    }

    @Override
    public double radius() {
        return radius;
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
    public double bni() {
        return bni;
    }

    @Override
    public int skip() {
        return skip;
    }
}
