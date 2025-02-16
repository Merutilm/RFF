package kr.merutilm.rff.util;

import kr.merutilm.rff.struct.DoubleExponent;

public class DoubleExponentMath {
    public static final double LN2 = Math.log(2);
    public static final double LN10 = Math.log(10);

    private DoubleExponentMath(){

    }

    public static DoubleExponent hypot2(DoubleExponent a, DoubleExponent b){
        return a.square().add(b.square());
    }

    public static DoubleExponent hypot(DoubleExponent a, DoubleExponent b){
        return hypot2(a,b).sqrt();
    }

    public static DoubleExponent hypotApproximate(DoubleExponent a, DoubleExponent b){
        a = a.abs();
        b = b.abs();
        DoubleExponent min = min(a,b);
        DoubleExponent max = max(a,b);
        if(min.isZero()){
            return max;
        }
        if(max.isZero()){
            return DoubleExponent.ZERO;
        }

        return min.square().multiply(0.428).divide(max).add(max);
    }

    public static DoubleExponent pow10(double v){
        //   10 ^ a
        //   2 ^ (a * log(10) / log(2))
        double exp = v * LN10 / LN2;
        return DoubleExponent.valueOf((int)exp, Math.pow(2, exp - (int)exp));
    }
    
    public static DoubleExponent max(DoubleExponent a, DoubleExponent b){
        if(a.isLargerThan(b)){
            return a;
        }
        return b;
    }
    public static DoubleExponent min(DoubleExponent a, DoubleExponent b){
        if(a.isLargerThan(b)){
            return b;
        }
        return a;
    }
    public static DoubleExponent atan2(DoubleExponent y, DoubleExponent x){
        double a = Math.atan2(y.doubleValue(), x.doubleValue());
        return a == 0 ? y.divide(x) : DoubleExponent.valueOf(a);
    }
    public static DoubleExponent sin(DoubleExponent e){
        double s = Math.sin(e.doubleValue());
        return s == 0 ? e : DoubleExponent.valueOf(s);
    }
    public static DoubleExponent cos(DoubleExponent e){
        return DoubleExponent.valueOf(Math.cos(e.doubleValue()));
    }

    public static DoubleExponent tan(DoubleExponent e){
        double t = Math.tan(e.doubleValue());
        return t == 0 ? e : DoubleExponent.valueOf(t);
    }


    public static DoubleExponent clamp(DoubleExponent value, DoubleExponent min, DoubleExponent max) {
        return min(max, max(min, value));
    }
}
