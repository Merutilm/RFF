package kr.merutilm.rff.util;

import java.util.Arrays;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

import kr.merutilm.rff.functions.FunctionEase;
import kr.merutilm.rff.selectable.Ease;

public final class AdvancedMath {
    private AdvancedMath() {
    }

    private static final Random RANDOM = new Random();


    private static final double PI = Math.PI;
    private static final double PI_D2 = Math.PI / 2;
    private static final double PI_D4 = Math.PI / 4;
    private static final double E = Math.E;

    public static double max(double... v) {
        if (v.length == 2) {
            return Math.max(v[0], v[1]);
        }
        return Arrays.stream(v).max().orElse(Double.NaN);
    }

    public static double min(double... v) {
        if (v.length == 2) {
            return Math.min(v[0], v[1]);
        }
        return Arrays.stream(v).min().orElse(Double.NaN);
    }

    public static int max(int... v) {
        if (v.length == 2) {
            return Math.max(v[0], v[1]);
        }
        return Arrays.stream(v).max().orElse(0);
    }

    public static int min(int... v) {
        if (v.length == 2) {
            return Math.min(v[0], v[1]);
        }
        return Arrays.stream(v).min().orElse(0);
    }

    public static int intRandom(int bounds) {
        return RANDOM.nextInt(bounds);
    }

    public static long longRandom(long bounds) {
        return RANDOM.nextLong(bounds);
    }

    public static double doubleRandom(double bounds) {
        return RANDOM.nextDouble() * bounds;
    }


    /**
     * cur 각도를 prev 와 가장 가까운 각도로 360을 가감하여 보정합니다.
     *
     * @param cur  현재 각도
     * @param prev 이전 각도
     * @return 보정된 현재 각도
     */
    public static double angleCorrection(double cur, double prev) {
        if (Double.isNaN(cur) || Double.isNaN(prev)) {
            throw new IllegalArgumentException("NaN");
        }
        while (Math.abs(cur - prev) > 180) {
            cur += cur < prev ? 360 : -360;
        }
        return cur;
    }


    public static double ratioDivide(double p1, double p2, double ratio) {
        return ratioDivide(p1, p2, ratio, Ease.LINEAR.fun());
    }

    public static double ratioDivide(double p1, double p2, double ratio, FunctionEase ease) {
        if (Double.isNaN(p1)) {
            throw new IllegalArgumentException("p1 is NaN");
        }
        if (Double.isNaN(p2)) {
            throw new IllegalArgumentException("p2 is NaN");
        }
        if (Double.isNaN(ratio)) {
            throw new IllegalArgumentException("ratio is NaN");
        }
        double fr = ease.apply(ratio);
        return p1 + fr * (p2 - p1);
    }


    public static double getRatio(double start, double end, double test) {
        if (Double.isNaN(start) || Double.isNaN(end) || Double.isNaN(test)) {
            throw new IllegalArgumentException("NaN");
        }
        if (start == end) {
            throw new IllegalArgumentException("start value equals end value");
        }
        return (test - start) / (end - start);
    }

    public static double fixDouble(double v) {
        return fixDouble(v, 6);
    }

    public static double fixDouble(double v, int digit) {

        if (Double.isNaN(v)) {
            return Double.NaN;
        }
        if(digit < 0){
            throw new IllegalArgumentException("Negative digit");
        }

        double val = switch (digit){
            case 0 -> 1;
            case 1 -> 10;
            case 2 -> 100;
            case 3 -> 1000;
            case 4 -> 10000;
            case 5 -> 100000;
            case 6 -> 1000000;
            default -> Math.pow(10, digit);
        };

        return Math.round(v * val) / val;
    }

    /**
     * 지정된 범위 안에 있는지 검사합니다.
     *
     * @param b1   범위 1 (해당 수 포함)
     * @param b2   범위 2 (해당 수 포함)
     * @param test 검사할 수
     */
    public static boolean isInnerBound(double b1, double b2, double test) {
        if (Double.isNaN(b1) || Double.isNaN(b2) || Double.isNaN(test)) {
            throw new IllegalArgumentException("NaN");
        }
        if (b1 == b2) {
            return b1 == test;
        }
        double ratio = getRatio(b1, b2, test);
        return 0 <= ratio && ratio <= 1;
    }

    public static double random(double b1, double b2) {
        if (b1 == b2) {
            return b1;
        }
        if (Double.isNaN(b1) || Double.isNaN(b2)) {
            throw new IllegalArgumentException("NaN");
        }
        return b1 + RANDOM.nextDouble() * (b2 - b1);
    }

    /**
     * 랜덤함수이지만 ease 에 따라 확률이 분배됩니다
     * Out 계열이면 b2, In 계열이면 b1에 가까운 값이 나올 확률이 증가합니다.
     */
    public static double random(double b1, double b2, FunctionEase ease) {
        if (Double.isNaN(b1) || Double.isNaN(b2)) {
            throw new IllegalArgumentException("NaN");
        }
        double ratio = ease.apply(doubleRandom(1));
        return AdvancedMath.ratioDivide(b1, b2, ratio);
    }

    /**
     * 확률을 검사합니다.
     */
    public static boolean isExecuted(double percentage) {
        if (Double.isNaN(percentage)) {
            return false;
        }
        if (Double.isInfinite(percentage)) {
            return true;
        }
        if (percentage == 0) {
            return false;
        }
        if (percentage >= 100) {
            return true;
        }
        return doubleRandom(100) < percentage;
    }

    public static double average(double... e) {
        return Arrays.stream(e).average().orElse(Double.NaN);
    }

    public static double rSqrt(double x) {
        if (Double.isNaN(x)) {
            throw new IllegalArgumentException("NaN");
        }
        double half = 0.5d * x;
        long i = Double.doubleToLongBits(x);
        i = 0x5fe6ec85e7de30daL - (i >> 1);
        x = Double.longBitsToDouble(i);
        x *= (1.5d - half * x * x);
        return x;
    }

    public static int restrict(int min, int max, int t) {
        return min(max, max(t, min));
    }

    public static double restrict(double min, double max, double t) {
        return min(max, max(t, min));
    }

    public static double hypot(double x, double y) {
        return Math.sqrt(x * x + y * y);
    }
    public static int abs(int v) {
        return v < 0 ? -v : v;
    }
    public static double abs(double v) {
        return v < 0 ? -v : v;
    }

    public static double hypotApproximate(double a, double b){
        a = abs(a);
        b = abs(b);
        double min = min(a,b);
        double max = max(a,b);
        if(min == 0){
            return max;
        }
        if(max == 0){
            return 0;
        }

        return max + 0.428 * min / max * min;
    }

    public static double atan2(double y, double x) {

        if(x == 0){
            if(y == 0){
                return 0;
            }else{
                return y >= 0 ? PI_D2 : -PI_D2;
            }
        }


        double a = min(abs(x), abs(y)) / max(abs(x), abs(y));
        double s = a * a;
        double r = ((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a;
        if (abs(y) > abs(x)) {
            r = 1.57079637 - r;
        }
        if (x < 0) {
            r = 3.14159274 - r;
        }
        if (y < 0) {
            r = -r;
        }
        return r;
    }

    public static double square(double v){
        return v * v;
    }
    public static DoubleUnaryOperator normalDistribution(double sigma){
        double a = rSqrt(2 * PI) / sigma;
        double b = 2 * sigma * sigma;
        return e -> a * Math.pow(E, -(e * e) / b);
    }

    public static double atan(double x) {
        return PI_D4 * x - x * (abs(x) - 1) * (0.2447 + 0.0663 * abs(x));
    }
    public static double atan2Approximate(double y, double x) {
        if(x == 0){
            if(y == 0){
                return 0;
            }else{
                return y >= 0 ? PI_D2 : -PI_D2;
            }
        }

        if (x >= 0) {
            if (y >= 0) {
                if (y < x) {
                    return atan(y / x);
                } else {
                    return PI_D2 - atan(x / y);
                }
            } else {
                if (-y < x) {
                    return atan(y / x);
                } else {
                    return -PI_D2 - atan(x / y);
                }
            }
        } else {
            if (y >= 0) {
                if (y < -x) {
                    return atan(y / x) + PI;
                } else {
                    return PI_D2 - atan(x / y);
                }
            } else {
                if (-y < -x) {
                    return atan(y / x) - PI;
                } else {
                    return -PI_D2 - atan(x / y);
                }
            }
        }
    }

}
