package kr.merutilm.rff.selectable;

import kr.merutilm.rff.functions.FunctionEase;

import static java.lang.Math.PI;

public enum Ease implements Selectable {
    LINEAR("Linear", t -> t),
    IN_SINE("InSine", t -> 1 - Math.cos(0.5 * PI * t)),
    OUT_SINE("OutSine", t -> Math.sin(0.5 * PI * t)),
    INOUT_SINE("InOutSine", t -> -(Math.cos(Math.PI * t) - 1) / 2),
    IN_QUAD("InQuad", t -> t * t),
    OUT_QUAD("OutQuad", t -> 1 - (1 - t) * (1 - t)),
    INOUT_QUAD("InOutQuad", t -> t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2),
    IN_CUBIC("InCubic", t -> t * t * t),
    OUT_CUBIC("OutCubic", t -> 1 - (1 - t) * (1 - t) * (1 - t)),
    INOUT_CUBIC("InOutCubic", t -> t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2),
    IN_QUART("InQuart", t -> t * t * t * t),
    OUT_QUART("OutQuart", t -> 1 - (1 - t) * (1 - t) * (1 - t) * (1 - t)),
    INOUT_QUART("InOutQuart", t -> t < 0.5 ? 4 * t * t * t * t : 1 - Math.pow(-2 * t + 2, 4) / 2),
    IN_QUINT("InQuint", t -> t * t * t * t * t),
    OUT_QUINT("OutQuint", t -> 1 - (1 - t) * (1 - t) * (1 - t) * (1 - t) * (1 - t)),
    INOUT_QUINT("InOutQuint", t -> t < 0.5 ? 4 * t * t * t * t * t : 1 - Math.pow(-2 * t + 2, 5) / 2),
    IN_EXPONENTIAL("InExpo", t -> t == 0 ? 0 : Math.pow(2, 10 * t - 10)),
    OUT_EXPONENTIAL("OutExpo", t -> t == 1 ? 1 : 1 - Math.pow(2, -10 * t)),
    INOUT_EXPONENTIAL("InOutExpo", t -> {
        if (t == 0) {
            return 0;
        }
        if (t == 1) {
            return 1;
        }
        return t < 0.5 ? Math.pow(2, 20 * t - 10) / 2 : (2 - Math.pow(2, -20 * t + 10)) / 2;
    }),
    IN_CIRCLE("InCirc", t -> 1 - Math.sqrt(1 - Math.pow(t, 2))),
    OUT_CIRCLE("OutCirc", t -> Math.sqrt(1 - Math.pow(t - 1, 2))),
    INOUT_CIRCLE("InOutCirc", t -> t < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * t, 2))) / 2 : (Math.sqrt(1 - Math.pow(-2 * t + 2, 2)) + 1) / 2),
    IN_ELASTIC("InElastic", t -> {
        final double c4 = (2 * Math.PI) / 3;
        if (t == 0) {
            return 0;
        }
        if (t == 1) {
            return 1;
        }
        return -Math.pow(2, 10 * t - 10) * Math.sin((t * 10 - 10.75) * c4);
    }),
    OUT_ELASTIC("OutElastic", t -> {
        final double c4 = (2 * Math.PI) / 3;
        if (t == 0) {
            return 0;
        }
        if (t == 1) {
            return 1;
        }
        return Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * c4) + 1;
    }),
    INOUT_ELASTIC("InOutElastic", t -> {
        final double c5 = (2 * Math.PI) / 4.5;
        final double c6 = Math.sin((20 * t - 11.125) * c5);
        if (t == 0) {
            return 0;
        }
        if (t == 1) {
            return 1;
        }
        return t < 0.5 ? -(Math.pow(2, 20 * t - 10) * c6) / 2 : (Math.pow(2, -20 * t + 10) * c6) / 2 + 1;
    }),
    IN_BACK("InBack", t -> {
        final double c1 = 1.70158;
        final double c3 = c1 + 1;
        return c3 * t * t * t - c1 * t * t;
    }),
    OUT_BACK("OutBack", t -> {
        final double c1 = 1.70158;
        final double c3 = c1 + 1;
        return t == 0 ? 0 : 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    }),
    INOUT_BACK("InOutBack", t -> {
        final double c1 = 1.70158;
        final double c2 = c1 * 1.525;
        return t < 0.5 ? (Math.pow(2 * t, 2) * ((c2 + 1) * 2 * t - c2)) / 2 : (Math.pow(2 * t - 2, 2) * ((c2 + 1) * (t * 2 - 2) + c2) + 2) / 2;
    }),
    IN_BOUNCE("InBounce", t -> {
        final double n1 = 7.5625;
        final double d1 = 2.75;
        if (1 - t < 1 / d1) {
            return 1 - n1 * (1 - t) * (1 - t);
        } else if (1 - t < 2 / d1) {
            return 1 - n1 * (1 - t - 1.5 / d1) * (1 - t - 1.5 / d1) - 0.75;
        } else if (1 - t < 2.5 / d1) {
            return 1 - n1 * (1 - t - 2.25 / d1) * (1 - t - 2.25 / d1) - 0.9375;
        } else {
            return 1 - n1 * (1 - t - 2.625 / d1) * (1 - t - 2.625 / d1) - 0.984375;
        }
    }),
    OUT_BOUNCE("OutBounce", t -> {
        final double n1 = 7.5625;
        final double d1 = 2.75;
        if (t < 1 / d1) {
            return n1 * t * t;
        } else if (t < 2 / d1) {
            return n1 * (t - 1.5 / d1) * (t - 1.5 / d1) + 0.75;
        } else if (t < 2.5 / d1) {
            return n1 * (t - 2.25 / d1) * (t - 2.25 / d1) + 0.9375;
        } else {
            return n1 * (t - 2.625 / d1) * (t - 2.625 / d1) + 0.984375;
        }
    }),
    INOUT_BOUNCE("InOutBounce", t -> t < 0.5
            ? (1 - OUT_BOUNCE.functionEase.apply(1 - 2 * t)) / 2
            : (1 + OUT_BOUNCE.functionEase.apply(2 * t - 1)) / 2),
    FLASH("Flash", LINEAR.functionEase),
    IN_FLASH("InFlash", IN_QUAD.functionEase),
    OUT_FLASH("OutFlash", OUT_QUAD.functionEase),
    INOUT_FLASH("InOutFlash", INOUT_QUAD.functionEase);

    private final String name;
    private final FunctionEase functionEase;

    @Override
    public String toString() {
        return name;
    }

    public FunctionEase func() {
        return functionEase;
    }

    Ease(String name, FunctionEase functionEase) {
        this.name = name;
        this.functionEase = functionEase;
    }

}
