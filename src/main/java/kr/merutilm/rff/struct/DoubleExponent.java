package kr.merutilm.rff.struct;

import java.math.BigInteger;
import java.util.Objects;

import kr.merutilm.rff.util.DoubleExponentMath;



public class DoubleExponent extends Number implements Comparable<DoubleExponent>{

    
    public final int exp;
    public final long bits;

    public static final int DOUBLE_PRECISION = 52;

    public static final long SIGNUM_BIT = 0x8000000000000000L;
    public static final long EXP0_BITS = 0x3ff0000000000000L;
    public static final long EXP_BITS = 0x7ff0000000000000L;
    public static final long DECIMAL_SIGNUM_BITS = 0x800fffffffffffffL;

    public static final DoubleExponent ZERO = new DoubleExponent(Integer.MIN_VALUE, 0);
    public static final DoubleExponent ONE = new DoubleExponent(0, 0);
    
    public static final DoubleExponent NAN = new DoubleExponent(Integer.MAX_VALUE, 0x0000000000000000L);
    public static final DoubleExponent POSITIVE_INFINITY = new DoubleExponent(Integer.MAX_VALUE, 0x000fffffffffffffL);
    public static final DoubleExponent NEGATIVE_INFINITY = new DoubleExponent(Integer.MAX_VALUE, 0x800fffffffffffffL);

    private DoubleExponent(int exp, long bits){
        this.exp = exp;
        this.bits = bits;
    }


    public static double longBitsToDecimal(long bits){
        return longBitsExpToDouble(bits, EXP0_BITS);
    }

    public static double longBitsExpToDouble(long bits, long expBits){
        return Double.longBitsToDouble(bits | expBits);
    }

    public double longBitsToDecimal(){
        return longBitsToDecimal(bits);
    }

    public static long decimalToLongBits(double value){
        return (Double.doubleToLongBits(value) & DECIMAL_SIGNUM_BITS);
    }

    public static DoubleExponent valueOf(double e){
        return valueOf(0, e);
    }


    public static DoubleExponent valueOf(LWBigDecimal v){
        
        BigInteger bi = v.getValue();
        byte[] bt = bi.abs().toByteArray();
        long bits = 0;
        int len = bi.bitLength();
        int dExp2 = len - DOUBLE_PRECISION;
        for (int i = 0; i < Math.min(bt.length, 8); i++) {
            bits = (bits << 8) | (bt[i] & 0xff);
        }

        //XXXXXXXXXXXXXXXXXXXXXXXXXXXX
        //exp : 52, 51, 50, ....
        //double : 0, -1, -2, -3, ....
        int shift = Long.bitCount((Long.highestOneBit(bits) - 1) << 1) - DOUBLE_PRECISION;
        if(shift > 0){
            bits >>>= shift;
        }else{
            bits <<= -shift;
        }

        long sig = bi.signum() == 1 ? 0 : 0x8000000000000000L;
        return new DoubleExponent(v.getExp2() + DOUBLE_PRECISION + dExp2 - 1, sig | bits & DECIMAL_SIGNUM_BITS);
    }


    public static DoubleExponent valueOf(int expBy2, double value){
        if(Double.isInfinite(value)){
            return value > 0 ? POSITIVE_INFINITY : NEGATIVE_INFINITY;    
        }
        if(Double.isNaN(value)){
            return NAN;
        }
        
        return unsafeValueOf(expBy2, value);
    }

    private static DoubleExponent unsafeValueOf(int expBy2, double value){

        if(value == 0){
            return ZERO;
        }

        int exp2 = Math.getExponent(value);
        long val = decimalToLongBits(value);

        return new DoubleExponent(expBy2 + exp2, val);
    }
    


    public DoubleExponent invert(){
        if(isInfinite()){
            return ZERO;
        }
        if(isNaN()){
            return NAN;
        }
        if(isZero()){
            return POSITIVE_INFINITY;
        }
        return valueOf(-exp, 1/longBitsToDecimal());
    }

    public DoubleExponent negate(){
        if(isInfinite()){
            return signum() ? NEGATIVE_INFINITY : POSITIVE_INFINITY;
        }
        if(isNaN()){
            return NAN;
        }
        if(isZero()){
            return ZERO;
        }
        return new DoubleExponent(exp, bits ^ SIGNUM_BIT);
    }

    public DoubleExponent doubled(){
        return new DoubleExponent(exp + 1, bits);
    }

    public DoubleExponent quadrupled(){
        return new DoubleExponent(exp + 2, bits);
    }

    public DoubleExponent halved(){
        return new DoubleExponent(exp - 1, bits);
    }

    public DoubleExponent quartered(){
        return new DoubleExponent(exp - 2, bits);
    }
    public DoubleExponent add(double v){
        return add(valueOf(v));
    }

    public DoubleExponent add(DoubleExponent v){
        if(isNaN() || v.isNaN()){
            return NAN;
        }
        if(isInfinite() && v.isInfinite()){
            if(signum() == v.signum()){
                return NAN;
            }else{
                return this;
            }
        }
        if(isInfinite() || v.isZero()){
            return this;
        }
        if(v.isInfinite() || isZero()){
            return v;
        }

        if(exp < v.exp){
            long d = (long) v.exp - exp;
            if(d >= DOUBLE_PRECISION){
                return v;
            }
            return unsafeValueOf(v.exp, longBitsExpToDouble(bits, EXP0_BITS - (d << DOUBLE_PRECISION)) + v.longBitsToDecimal());        
        }
        if(exp > v.exp){
            long d = (long) exp - v.exp;
            if(d >= DOUBLE_PRECISION){
                return this;
            }
            return unsafeValueOf(exp, longBitsToDecimal() + longBitsExpToDouble(v.bits, EXP0_BITS - (d << DOUBLE_PRECISION)));
        }
        return unsafeValueOf(exp, longBitsToDecimal() + v.longBitsToDecimal());
    }

    public DoubleExponent subtract(double v){
        return subtract(valueOf(v));
    }

    public DoubleExponent subtract(DoubleExponent v){
        if(isNaN() || v.isNaN()){
            return NAN;
        }
        if(isInfinite() && v.isInfinite()){
            if(signum() == v.signum()){
                return this;
            }else{
                return NAN;
            }
        }

        if(isInfinite() || v.isZero()){
            return this;
        }
        if(v.isInfinite() || isZero()){
            return v.negate();
        }

        if(exp < v.exp){
            long d = (long)v.exp - exp;
            if(d >= DOUBLE_PRECISION){
                return v.negate();
            }
            return unsafeValueOf(v.exp, longBitsExpToDouble(bits, EXP0_BITS - (d << DOUBLE_PRECISION)) - v.longBitsToDecimal());        
        }
        if(exp > v.exp){
            long d = (long)exp - v.exp;
            if(d >= DOUBLE_PRECISION){
                return this;
            }
            return unsafeValueOf(exp, longBitsToDecimal() - longBitsExpToDouble(v.bits, EXP0_BITS - (d << DOUBLE_PRECISION)));
        }
        return unsafeValueOf(exp, longBitsToDecimal() - v.longBitsToDecimal());
    }


    public boolean isZero(){ 
        return ZERO.exp == exp && ZERO.bits == bits;
    }

    public boolean isNaN(){
        return NAN.exp == exp && NAN.bits == bits;
    }

    public boolean signum(){
        return bits >>> 63 == 0;
    }
    
    public boolean isInfinite(){
        return (POSITIVE_INFINITY.exp == exp && POSITIVE_INFINITY.bits == bits) || (NEGATIVE_INFINITY.exp == exp && NEGATIVE_INFINITY.bits == bits);
    }

    public boolean isLargerThan(double v){
        return isLargerThan(valueOf(v));
    }

    public boolean isLargerThan(DoubleExponent v){
        return compareTo(v) > 0;
    }

    public boolean isSmallerThan(double v){
        return isSmallerThan(valueOf(v));
    }

    public boolean isSmallerThan(DoubleExponent v){
        return compareTo(v) < 0;
    }


    public DoubleExponent abs(){
        if(isNaN()){
            return DoubleExponent.NAN;
        }
        if(isInfinite()){
            return DoubleExponent.POSITIVE_INFINITY;
        }
        if(isZero()){
            return DoubleExponent.ZERO;
        }

        return !signum() ? negate() : this;
    }

    public DoubleExponent square(){
        if(isZero()){
            return DoubleExponent.ZERO;
        }
        if(isNaN()){
            return DoubleExponent.NAN;
        }
        if(isInfinite()){
            return DoubleExponent.POSITIVE_INFINITY;
        }
        //   (a * 2 ^ b) ^ 2
        // = a ^ 2 * 2 ^ (2 * b)
        double d = longBitsToDecimal();
        return DoubleExponent.valueOf(exp * 2, d * d);
    }
    public DoubleExponent sqrt(){
        if(isZero()){
            return DoubleExponent.ZERO;
        }
        if(isNaN() || !signum()){
            return DoubleExponent.NAN;
        }
        if(isInfinite()){
            return DoubleExponent.POSITIVE_INFINITY;
        }
        //   sqrt(a * 2 ^ b)
        //   sqrt(a) * 2 ^ (b / 2)

        int fl = (int) Math.floor(exp / 2.0);
        double r = exp / 2.0 - fl;


        return DoubleExponent.valueOf(fl, Math.sqrt(longBitsToDecimal()) * Math.pow(2, r));
    }
    

    public DoubleExponent multiply(double v){
        return multiply(valueOf(v));
    }


    public DoubleExponent multiply(DoubleExponent v){
        if(isZero() || v.isZero()){
            return ZERO;
        }
        if(isNaN() || v.isNaN()){
            return NAN;
        }
        if(isInfinite() || v.isInfinite()){
            return signum() == v.signum() ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
        }
        //   a * 10 ^ b * c * 10 ^ d 
        // = a * c * 10 ^ b * 10 ^ d 
        // = a * c * 10 ^ (b + d)
        return unsafeValueOf(exp + v.exp, longBitsToDecimal() * v.longBitsToDecimal());
    }

    public DoubleExponent divide(double v){
        return divide(valueOf(v));
    }

    public DoubleExponent divide(DoubleExponent v){
        if(isZero() || v.isInfinite()){
            return ZERO;
        }
        if(v.isZero() || isInfinite()){
            return signum() ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
        }
        if(isZero() && v.isZero()){
            return NAN;
        }
        
        //   a * 2 ^ b / (c * 2 ^ d)
        // = a / c * 2 ^ b / 2 ^ d 
        // = a / c * 2 ^ (b - d)
        return unsafeValueOf(exp - v.exp, longBitsToDecimal() / v.longBitsToDecimal());
    }

    public double log(){
        //   log(a * 2 ^ b)
        // = log(a) + log(2 ^ b)
        // = log(a) + b * log(2)
        if(!signum()){
            return Double.NaN;
        }
        if(isZero()){
            return Double.NEGATIVE_INFINITY;
        }
        return exp * DoubleExponentMath.LN2 + Math.log(longBitsToDecimal());
    }
    public double log10(){
        //   log10(a * 2 ^ b)
        // = log10(a) + log10(2 ^ b)
        // = log10(a) + b * log(2) / log(10)
        if(!signum()){
            return Double.NaN;
        }
        if(isZero()){
            return Double.NEGATIVE_INFINITY;
        }
        return exp * DoubleExponentMath.LN2 / DoubleExponentMath.LN10 + Math.log10(longBitsToDecimal());
    }


    public LWBigDecimal toLWBigDecimal(int precision){
        if(isZero()){
            return LWBigDecimal.zero(precision);
        }
        if(isInfinite() || isNaN()){
            return null;
        }
        LWBigDecimal result = LWBigDecimal.valueOf(String.valueOf(longBitsToDecimal()), precision);
        return result.multiplyExpOf2(exp);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DoubleExponent o && o.exp == exp && o.bits == bits;
    }

    @Override
    public int hashCode() {
        return Objects.hash(exp, bits);
    }
    
    @Override
    public String toString() {
        if(isZero()){
            return String.valueOf(0);
        }
        if(isInfinite()){
            if(signum()){
                return String.valueOf(Double.POSITIVE_INFINITY);
            }else{
                return String.valueOf(Double.NEGATIVE_INFINITY);
            }
        }

        if(isInfinite()){
            if(signum()){
                return String.valueOf(Double.POSITIVE_INFINITY);
            }else{
                return String.valueOf(Double.NEGATIVE_INFINITY);
            }
        }
        
        return toLWBigDecimal(LWBigDecimal.exp2ToPrecision(exp - 15)).toString();
    }


    @Override
    public int compareTo(DoubleExponent e) {
        if(signum() != e.signum()){
            return signum() ? 1 : -1;
        }
        if(exp != e.exp){
            return exp > e.exp ? 1 : -1;
        }
        if(bits != e.bits){
            return bits > e.bits ? 1 : -1;
        }
        return 0;
    }

    @Override
    public int intValue() {
        return (int)doubleValue();
    }

    @Override
    public long longValue() {
        return (long)doubleValue();
    }

    @Override
    public float floatValue() {
        return (float)doubleValue();
    }

    @Override
    public double doubleValue() {
        if(isNaN()){
            return Double.NaN;
        }
        if(exp >= 1024 || isInfinite()){
            return !signum() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
        if(exp <= -1024){
            return 0;
        }
        long expBit = EXP0_BITS + ((long)exp << DOUBLE_PRECISION);
        long bit = expBit | bits;
        return longBitsExpToDouble(bit, expBit);
    }
}