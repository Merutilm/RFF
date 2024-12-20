package kr.merutilm.fractal.approx;

import kr.merutilm.fractal.struct.DoubleExponent;
import kr.merutilm.fractal.util.DoubleExponentMath;

public class DeepBLASingle implements DeepBLA {

    private final int targetIter;
    private final DoubleExponent anr;
    private final DoubleExponent ani;
    private final DoubleExponent bnr;
    private final DoubleExponent bni;
    private final int skip;
    private final DoubleExponent radius;

    public DeepBLASingle(int targetIter, DoubleExponent zr, DoubleExponent zi, DoubleExponent epsilon, DoubleExponent dcMax) {
        this.targetIter = targetIter;
        this.anr = zr.doubled();
        this.ani = zi.doubled();
        this.bnr = DoubleExponent.ONE;
        this.bni = DoubleExponent.ZERO;
        this.skip = 1;
        DoubleExponent rz = DoubleExponentMath.hypotApproximate(zr, zi);
        this.radius = DoubleExponentMath.max(DoubleExponent.ZERO, epsilon.multiply(2).multiply(rz).subtract(dcMax.divide(rz.doubled())));
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
