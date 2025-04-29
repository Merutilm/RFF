package kr.merutilm.rff.struct;

import javax.annotation.Nonnull;

import kr.merutilm.rff.functions.FunctionEase;
import kr.merutilm.rff.selectable.Ease;
import kr.merutilm.rff.util.AdvancedMath;

import java.awt.Color;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntBiFunction;
import java.util.stream.Stream;

public record HexColor(int r, int g, int b, int a) implements Struct<HexColor> {
    
    
    public static final int MAX = 255;
    public static final int HALF = 128;

    public HexColor {
        if (r >= 256 || g >= 256 || b >= 256 || a >= 256) {
            throw new IllegalArgumentException("Color value higher than 255\n RGBA provided : " +
                                               String.join(", ", Stream.of(r, g, b, a)
                                                       .map(Object::toString)
                                                       .toArray(String[]::new))
            );
        }
        if (r < 0 || g < 0 || b < 0 || a < 0) {
            throw new IllegalArgumentException("Color value must be positive\n RGBA provided : " +
                                               String.join(", ", Stream.of(r, g, b, a)
                                                       .map(Object::toString)
                                                       .toArray(String[]::new))
            );
        }
    }

    public static HexColor get(int r, int g, int b, int a) {
        return new HexColor(r, g, b, a);
    }

    public static HexColor get(int r, int g, int b) {
        return get(r, g, b, MAX);
    }

    public static HexColor getSafe(int r, int g, int b, int a) {
        return get(safetyFix(r), safetyFix(g), safetyFix(b), safetyFix(a));
    }

    public static HexColor getSafe(int r, int g, int b) {
        return getSafe(r, g, b, MAX);
    }

    public static final HexColor TRANSPARENT = get(0, 0, 0, 0);

    public static final HexColor WHITE = get(MAX, MAX, MAX);
    public static final HexColor BLACK = get(0, 0, 0);
    public static final HexColor RED = get(MAX, 0, 0);
    public static final HexColor GREEN = get(0, MAX, 0);
    public static final HexColor BLUE = get(0, 0, MAX);
    public static final HexColor DARK_RED = get(HALF, 0, 0);
    public static final HexColor DARK_GREEN = get(0, HALF, 0);
    public static final HexColor DARK_BLUE = get(0, 0, HALF);

    public static final HexColor R_RED = get(232, 20, 22);
    public static final HexColor R_ORANGE = get(255, 165, 0);
    public static final HexColor R_YELLOW = get(250, 235, 54);
    public static final HexColor R_GREEN = get(121, 195, 20);
    public static final HexColor R_BLUE = get(72, 125, 231);
    public static final HexColor R_INDIGO = get(75, 54, 157);
    public static final HexColor R_VIOLET = get(112, 54, 157);

    @Override
    public Builder edit() {
        return new Builder(r, g, b, a);
    }

    public static final class Builder implements StructBuilder<HexColor> {
        private int r;
        private int g;
        private int b;
        private int a;

        public Builder(int r, int g, int b, int a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }


        public Builder setR(int r) {
            this.r = r;
            return this;
        }

        public Builder setG(int g) {
            this.g = g;
            return this;
        }

        public Builder setB(int b) {
            this.b = b;
            return this;
        }

        public Builder setA(int a) {
            this.a = a;
            return this;
        }

        public Builder add(HexColor b) {
            this.r += b.r;
            this.g += b.g;
            this.b += b.b;
            return this;
        }



        public Builder function(IntUnaryOperator function) {
            return setR(function.applyAsInt(r))
                    .setG(function.applyAsInt(g))
                    .setB(function.applyAsInt(b))
                    .setA(function.applyAsInt(a));
        }

        public Builder function(HexColor target, IntBinaryOperator function) {
            return setR(function.applyAsInt(r, target.r))
                    .setG(function.applyAsInt(g, target.g))
                    .setB(function.applyAsInt(b, target.b))
                    .setA(function.applyAsInt(a, target.a));
        }


        public Builder functionExceptAlpha(IntUnaryOperator function) {
            return setR(function.applyAsInt(r))
            .setG(function.applyAsInt(g))
            .setB(function.applyAsInt(b));
           }

        public Builder functionExceptAlpha(ToIntBiFunction<HexColor, Integer> function) {
            HexColor c = build();
            return setR(function.applyAsInt(c,r))
            .setG(function.applyAsInt(c,g))
            .setB(function.applyAsInt(c,b));
        }

        public Builder functionExceptAlpha(HexColor target,IntBinaryOperator function) {
            return setR(function.applyAsInt(r, target.r))
            .setG(function.applyAsInt(g, target.g))
            .setB(function.applyAsInt(b, target.b));
           }


        @Override
        public HexColor build() {
            return get(r, g, b, a);
        }

        public HexColor buildSafe() {
            return getSafe(r, g, b, a);
        }
    }

    public static HexColor random() {
        return get(AdvancedMath.intRandom(256), AdvancedMath.intRandom(256), AdvancedMath.intRandom(256));
    }
    public static HexColor fromAWT(java.awt.Color color) {
        return get(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public HexColor function(IntUnaryOperator function) {
        return edit().function(function).buildSafe();
    }

    public HexColor function(HexColor target, IntBinaryOperator function) {
        return edit().function(target, function).buildSafe();
    }

    public HexColor functionExceptAlpha(IntUnaryOperator function) {
        return edit().functionExceptAlpha(function).buildSafe();
    }

    public HexColor functionExceptAlpha(ToIntBiFunction<HexColor,Integer> function) {
        return edit().functionExceptAlpha(function).buildSafe();
    }

    public HexColor functionExceptAlpha(HexColor target, IntBinaryOperator function) {
        return edit().functionExceptAlpha(target, function).buildSafe();
    }

    public static HexColor ratioDivide(HexColor start, HexColor end, double ratio) {
        return ratioDivide(start, end, ratio, Ease.LINEAR.func());
    }
    public static int ratioDivide(int start, int end, double ratio){
        return ratioDivide(start, end, ratio, Ease.LINEAR.func());
    }
    public static int ratioDivide(int start, int end, double ratio, FunctionEase ease){
        int sr = intR(start);
        int sg = intG(start);
        int sb = intB(start);
        int sa = intA(start);
        int er = intR(end);
        int eg = intG(end);
        int eb = intB(end);
        int ea = intA(end);
        int r = (int)AdvancedMath.ratioDivide(sr, er, ratio, ease);
        int g = (int)AdvancedMath.ratioDivide(sg, eg, ratio, ease);
        int b = (int)AdvancedMath.ratioDivide(sb, eb, ratio, ease);
        int a = (int)AdvancedMath.ratioDivide(sa, ea, ratio, ease);
        return toInteger(r, g, b, a);
    }

    public static HexColor ratioDivide(HexColor start, HexColor end, double ratio, FunctionEase ease) {
        if (ratio == 0) {
            return start;
        }
        if (ratio == 1) {
            return end;
        }
        return start.function(end, (e, t) -> (int)AdvancedMath.ratioDivide(e, t, ratio, ease));
    }

    public HexColor invert() {
        return functionExceptAlpha(e -> 255 - e);
    }


    public static double convertOpacityToHex(double opacity) {
        return Math.min(255 * opacity / 100, 255);
    }

    public HexColor opacity(double opacity) {
        if (opacity == 1) {
            return this;
        }
        return get(r, g, b, (int)AdvancedMath.restrict(0, MAX, a * opacity));
    }

    public HexColor blend(ColorBlendMode colorBlendMode, HexColor blend) {
        return blend(colorBlendMode, blend, 1);
    }

    public HexColor blend(ColorBlendMode colorBlendMode, HexColor blend, double opacity) {
        if (opacity == 0) {
            return this;
        }

        HexColor thisColor = toRGB();
        switch (colorBlendMode) {
            case NORMAL -> {
                double ratio = AdvancedMath.restrict(0, 1, opacity * blend.a() / MAX);
                return ratioDivide(thisColor, get(blend.r, blend.g, blend.b), ratio);
            }
            case LINEAR_DODGE, LINEAR_BURN -> { // Is it the formula for Linear-Blend mode??
                double ratio = AdvancedMath.restrict(0, 1, opacity);
                HexColor result = thisColor.functionExceptAlpha(blend.toRGB(), colorBlendMode.function::blend);
                return ratioDivide(thisColor, result, ratio);
            }
            default -> {
                double ratio = AdvancedMath.restrict(0, 1, opacity * blend.a() / MAX);
                HexColor result = thisColor.functionExceptAlpha(blend, colorBlendMode.function::blend);
                return ratioDivide(thisColor, result, ratio);
            }
        }
    }

    public static HexColor average(HexColor... colors) {
        int r = 0;
        int g = 0;
        int b = 0;
        int a = 0;

        for (HexColor color : colors) {
            if(color == null){
                continue;
            }
            r += color.r;
            g += color.g;
            b += color.b;
            a += color.a;
        }

        r /= colors.length;
        g /= colors.length;
        b /= colors.length;
        a /= colors.length;

        return get(Math.min(255, r), Math.min(255, g), Math.min(255, b), Math.min(255, a));
    }

    public static int average(int... colors){
        int r = 0;
        int g = 0;
        int b = 0;
        int a = 0;

        for (int color : colors) {
            r += intR(color);
            g += intG(color);
            b += intB(color);
            a += intA(color);
        }

        r /= colors.length;
        g /= colors.length;
        b /= colors.length;
        a /= colors.length;

        return toInteger(r, g, b, a);
    }

    public static double error(HexColor c1, HexColor c2) {
        HexColor r1 = c1.toRGB();
        HexColor r2 = c2.toRGB();

        double accR = Math.abs(r1.r() - r2.r());
        double accG = Math.abs(r1.g() - r2.g());
        double accB = Math.abs(r1.b() - r2.b());
        return (accR + accG + accB) * 0.130719;
    }


    public static double error(int c1, int c2) {
        return error(fromInteger(c1), fromInteger(c2));
    }

    public HexColor grayScale() {
        int value = grayScaleValue();
        return get(value, value, value, a);
    }

    public static int grayScale(int v) {
        int value = grayScaleValue(v);
        return toInteger(value, value, value);
    }

    public int grayScaleValue() {
        return grayScaleValue(r, g, b);
    }
    public static int grayScaleValue(int v){
        return grayScaleValue(intR(v), intG(v), intB(v));
    }
    public static int grayScaleValue(int r, int g, int b) {
        if(r < 0 || g < 0 || b < 0){
            throw new IllegalArgumentException("cannot provide negative value");
        }
        return (int)(r * 0.3 + g * 0.59 + b * 0.11);
    }

    public double getSaturation() {
        return ((double)AdvancedMath.max(r, g, b) - AdvancedMath.min(r, g, b)) / AdvancedMath.max(r, g, b);
    }

    /**
     * 명도에 따라 alpha 값을 정의한 새로운 색상을 반환합니다.
     */
    public HexColor toRGBA() {
        if (a < 255) {
            throw new IllegalArgumentException("current color exists alpha channel");
        }
        int maxValue = AdvancedMath.max(r, g, b);
        double multiplier = 255.0 / maxValue;
        int r = (int)(this.r * multiplier);
        int g = (int)(this.g * multiplier);
        int b = (int)(this.b * multiplier);
        return get(r, g, b, maxValue);
    }

    /**
     * 투명도에 따라 alpha 채널을 제거한 새로운 색상을 반환합니다.
     */
    public HexColor toRGB() {
        if (a == 255) {
            return this;
        }
        double multiplier = a / 255.0;
        int r = (int)(this.r * multiplier);
        int g = (int)(this.g * multiplier);
        int b = (int)(this.b * multiplier);
        return get(r, g, b);
    }

    /**
     * 명도에 따라 alpha 값을 정의한 새로운 색상을 반환합니다.
     */
    public static int toRGBA(int v) {
        int r = intR(v);
        int g = intG(v);
        int b = intB(v);
        int a = intA(v);
        if (a < 255) {
            throw new IllegalArgumentException("current color exists alpha channel");
        }
        double maxValue = AdvancedMath.max(r, g, b);
        double multiplier = 255.0 / maxValue;
        r = (int)(r * multiplier);
        g = (int)(g * multiplier);
        b = (int)(b * multiplier);
        return toInteger(r, g, b, (int)maxValue);
    }

    /**
     * 투명도에 따라 alpha 채널을 제거한 새로운 색상을 반환합니다.
     */
    public static int toRGB(int v) {
        int r = intR(v);
        int g = intG(v);
        int b = intB(v);
        int a = intA(v);
        if (a == 255) {
            return v;
        }
        double multiplier = a / 255.0;
        r = (int)(r * multiplier);
        g = (int)(g * multiplier);
        b = (int)(b * multiplier);
        return toInteger(r, g, b, MAX);
    }
    public static int safetyFix(int value) {
        return AdvancedMath.restrict(0, 255, value);
    }

    public int toInteger() {
        return toInteger(r, g, b, a);
    }

    public static int toInteger(int r, int g, int b) {
        return toInteger(r, g, b, MAX);
    }

    public static int toInteger(int r, int g, int b, int a) {
        return b + (g << 8) + (r << 16) + (a << 24);
    }

    public static HexColor fromInteger(int v){
        return new HexColor(intR(v),intG(v),intB(v),intA(v));
    }

    public static int intR(int v){
        return v >> 16 & 0xff;
    }
    public static int intG(int v){
        return v >> 8 & 0xff;
    }
    public static int intB(int v){
        return v & 0xff;
    }
    public static int intA(int v){
        return v >> 24 & 0xff;
    }


    public Color toAWT() {
        return new Color(r, g, b, a);
    }

    @Nonnull
    @Override
    public String toString() {
        return String.format("%02x%02x%02x%02x", r, g, b, a);
    }

    public enum ColorBlendMode {
        NORMAL((_, b) -> b),
        MULTIPLY((a, b) -> a * b),
        LINEAR_DODGE((a, b) -> Math.min(1, a + b)),
        DIFFERENCE((a, b) -> Math.abs(a - b)),
        SCREEN((a, b) -> 1 - (1 - a) * (1 - b)),
        OVERLAY((a, b) -> a < 0.5 ? 2 * a * b : 1 - 2 * (1 - a) * (1 - b)),
        HARD_LIGHT((a, b) -> b < 0.5 ? 2 * a * b : 1 - 2 * (1 - a) * (1 - b)),
        SOFT_LIGHT((a, b) -> (1 - 2 * b) * a * a + 2 * a * b),
        LINEAR_BURN((a, b) -> Math.max(0, a + b - 1)),
        COLOR_DODGE((a, b) -> b == 1 ? 1 : Math.min(1, a / (1 - b))),
        COLOR_BURN((a, b) -> b == 0 ? 1 : Math.min(1, 1 - (1 - a) / b)),
        VIVID_LIGHT((a, b) -> b < 0.5 ? 1 - Math.min(1, (1 - a) / (2 * b)) : Math.min(1, a / (1 - 2 * b))),
        DIVIDE((a, b) -> b == 0 ? 1 : Math.min(1, a / b));
        private final BlendFunction function;

        ColorBlendMode(BlendFunction function) {
            this.function = function;
        }
    }

    @FunctionalInterface
    private interface BlendFunction {
        double apply(double base, double blend);

        default int blend(int base, int blend) {
            return (int)(MAX * apply((double)base / MAX, (double)blend / MAX));
        }
    }

}
