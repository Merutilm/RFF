package kr.merutilm.fractal.approx;

import kr.merutilm.fractal.struct.DoubleExponent;
import kr.merutilm.fractal.util.DoubleExponentMath;

public class DeepBLAMerged implements DeepBLA {

    public final DeepBLA x;
    public final DeepBLA y;
    private final int targetIter;
    private final int skip;

    private final DoubleExponent anr;
    private final DoubleExponent ani;
    private final DoubleExponent bnr;
    private final DoubleExponent bni;
    private final DoubleExponent radius;

    public DeepBLAMerged(DeepBLA x, DeepBLA y, DoubleExponent dcMax) {
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
        this.anr = y.anr().multiply(x.anr()).subtract(y.ani().multiply(x.ani()));
        this.ani = y.anr().multiply(x.ani()).add(y.ani().multiply(x.anr()));
        this.bnr = y.anr().multiply(x.bnr()).subtract(y.ani().multiply(x.bni())).add(y.bnr());
        this.bni = y.anr().multiply(x.bni()).add(y.ani().multiply(x.bnr())).add(y.bni());
        DoubleExponent rax = DoubleExponentMath.hypotApproximate(x.anr(), x.ani());
        DoubleExponent rbx = DoubleExponentMath.hypotApproximate(x.bnr(), x.bni());
        this.radius = rax.isZero() ? DoubleExponent.ZERO : DoubleExponentMath.clamp((y.radius().subtract(rbx.multiply(dcMax))).divide(rax), DoubleExponent.ZERO, x.radius());
    }
    @Override
    public DoubleExponent radius() {
        return radius;
    }

    @Override
    public int targetIter() {
        return targetIter;
    }

    @Override
    public DoubleExponent anr() {
        return anr;
    }

    @Override
    public DoubleExponent ani() {
        return ani;
    }

    @Override
    public DoubleExponent bnr() {
        return bnr;
    }

    @Override
    public DoubleExponent bni() {
        return bni;
    }

    @Override
    public int skip() {
        return skip;
    }
}
