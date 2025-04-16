package kr.merutilm.rff.struct;

import java.math.BigInteger;
import java.util.Objects;

import kr.merutilm.rff.precision.LWBigDecimal;
import kr.merutilm.rff.util.DoubleExponentMath;



public class DoubleExponent extends Number implements Comparable<DoubleExponent>{

    
    public final int exp2;
    public final long bits;

    public static final double EXP_DEADLINE = 290;

    public static final int DOUBLE_PRECISION = 52;

    public static final long SIGNUM_BIT = 0x8000000000000000L;
    public static final long EXP0_BITS = 0x3ff0000000000000L;
    public static final long DECIMAL_SIGNUM_BITS = 0x800fffffffffffffL;

    public static final DoubleExponent ZERO = new DoubleExponent(Integer.MIN_VALUE, 0);
    public static final DoubleExponent ONE = new DoubleExponent(0, 0);
    
    public static final DoubleExponent NAN = new DoubleExponent(Integer.MAX_VALUE, 0x0000000000000000L);
    public static final DoubleExponent POSITIVE_INFINITY = new DoubleExponent(Integer.MAX_VALUE, 0x000fffffffffffffL);
    public static final DoubleExponent NEGATIVE_INFINITY = new DoubleExponent(Integer.MAX_VALUE, 0x800fffffffffffffL);

    private DoubleExponent(int exp2, long bits){
        this.exp2 = exp2;
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
        
        BigInteger value = v.getValue();
        int len = value.abs().bitLength();
        int shift = len - DoubleExponent.DOUBLE_PRECISION - 1;
        byte[] ba = value.abs().shiftRight(shift).toByteArray();
        long bits = 0;
        for (byte b : ba) {
            bits = (bits << 8) | (b & 0xff);
        }
        bits = (bits) & DoubleExponent.DECIMAL_SIGNUM_BITS;
        int fExp2 = v.getExp2() + shift + DoubleExponent.DOUBLE_PRECISION;
        long sig = value.signum() == 1 ? 0 : DoubleExponent.SIGNUM_BIT;
        return new DoubleExponent(fExp2, sig | bits);
    }


    public static DoubleExponent valueOf(int expBy2, double value){
        if(value == 0){
            return ZERO;
        }
        if(Double.isInfinite(value)){
            return value > 0 ? POSITIVE_INFINITY : NEGATIVE_INFINITY;    
        }
        if(Double.isNaN(value)){
            return NAN;
        }


        return fastUnsafeValue(expBy2, value);
    }

    private static DoubleExponent fastUnsafeValue(int expBy2, double value){

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
        return fastUnsafeValue(-exp2, 1 / longBitsToDecimal());
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
        return new DoubleExponent(exp2, bits ^ SIGNUM_BIT);
    }

    public DoubleExponent doubled(){
        return new DoubleExponent(exp2 + 1, bits);
    }

    public DoubleExponent quadrupled(){
        return new DoubleExponent(exp2 + 2, bits);
    }

    public DoubleExponent halved(){
        return new DoubleExponent(exp2 - 1, bits);
    }

    public DoubleExponent quartered(){
        return new DoubleExponent(exp2 - 2, bits);
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

        if(exp2 < v.exp2){
            long d = (long) v.exp2 - exp2;
            if(d >= DOUBLE_PRECISION){
                return v;
            }
            double dec = longBitsExpToDouble(bits, EXP0_BITS - (d << DOUBLE_PRECISION)) + v.longBitsToDecimal();
            return dec == 0 ? ZERO : fastUnsafeValue(v.exp2, dec);
        }
        if(exp2 > v.exp2){
            long d = (long) exp2 - v.exp2;
            if(d >= DOUBLE_PRECISION){
                return this;
            }
            double dec = longBitsToDecimal() + longBitsExpToDouble(v.bits, EXP0_BITS - (d << DOUBLE_PRECISION));
            return dec == 0 ? ZERO : fastUnsafeValue(exp2, dec);
        }
        double dec = longBitsToDecimal() + v.longBitsToDecimal();
        return dec == 0 ? ZERO : fastUnsafeValue(exp2, dec);
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

        if(exp2 < v.exp2){
            long d = (long)v.exp2 - exp2;
            if(d >= DOUBLE_PRECISION){
                return v.negate();
            }
            double dec = longBitsExpToDouble(bits, EXP0_BITS - (d << DOUBLE_PRECISION)) - v.longBitsToDecimal();
            return fastUnsafeValue(v.exp2, dec);
        }
        if(exp2 > v.exp2){
            long d = (long) exp2 - v.exp2;
            if(d >= DOUBLE_PRECISION){
                return this;
            }
            double dec = longBitsToDecimal() - longBitsExpToDouble(v.bits, EXP0_BITS - (d << DOUBLE_PRECISION));
            return fastUnsafeValue(exp2, dec);
        }
        double dec = longBitsToDecimal() - v.longBitsToDecimal();
        return fastUnsafeValue(exp2, dec);
    }


    public boolean isZero(){ 
        return ZERO.exp2 == exp2 && ZERO.bits == bits;
    }

    public boolean isNaN(){
        return NAN.exp2 == exp2 && NAN.bits == bits;
    }

    public boolean signum(){
        return bits >>> 63 == 0;
    }
    
    public boolean isInfinite(){
        return (POSITIVE_INFINITY.exp2 == exp2 && POSITIVE_INFINITY.bits == bits) || (NEGATIVE_INFINITY.exp2 == exp2 && NEGATIVE_INFINITY.bits == bits);
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
        return fastUnsafeValue(exp2 * 2, d * d);
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

        int fl = (int) Math.floor(exp2 / 2.0);
        double r = exp2 / 2.0 - fl;


        return fastUnsafeValue(fl, Math.sqrt(longBitsToDecimal()) * Math.pow(2, r));
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
        return fastUnsafeValue(exp2 + v.exp2, longBitsToDecimal() * v.longBitsToDecimal());
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
        return fastUnsafeValue(exp2 - v.exp2, longBitsToDecimal() / v.longBitsToDecimal());
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
        return exp2 * DoubleExponentMath.LN2 + Math.log(longBitsToDecimal());
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
        return exp2 * DoubleExponentMath.LN2 / DoubleExponentMath.LN10 + Math.log10(longBitsToDecimal());
    }


    public LWBigDecimal toLWBigDecimal(int precision){
        if(isZero()){
            return LWBigDecimal.zero(precision);
        }
        if(isInfinite() || isNaN()){
            return null;
        }
        LWBigDecimal result = LWBigDecimal.valueOf(String.valueOf(longBitsToDecimal()), precision);
        return result.multiplyExpOf2(exp2);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DoubleExponent o && o.exp2 == exp2 && o.bits == bits;
    }

    @Override
    public int hashCode() {
        return Objects.hash(exp2, bits);
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
        
        return toLWBigDecimal(LWBigDecimal.exp2ToPrecision(exp2 - DOUBLE_PRECISION)).toString();
    }


    @Override
    public int compareTo(DoubleExponent e) {
        if(signum() != e.signum()){
            return signum() ? 1 : -1;
        }
        if(exp2 != e.exp2){
            return exp2 > e.exp2 ? 1 : -1;
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
        if(exp2 >= 1024 || isInfinite()){
            return !signum() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
        if(exp2 <= -1024){
            return 0;
        }
        long expBit = EXP0_BITS + ((long) exp2 << DOUBLE_PRECISION);
        long bit = expBit | bits;
        return longBitsExpToDouble(bit, expBit);
    }
}