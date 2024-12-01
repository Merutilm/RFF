package kr.merutilm.fractal.struct;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class LWBigDecimal extends Number{

    private final int exp2;
    private final BigInteger value;
    private static final BigDecimal BD_TWO = BigDecimal.valueOf(2);

    private static final double LOG10_2 = Math.log10(2);

    private LWBigDecimal(int exp2, BigInteger value){
        this.exp2 = exp2;
        this.value = value;
    }

    public static LWBigDecimal zero(int precision){
        return new LWBigDecimal(precisionToExp2(precision), BigInteger.ZERO);
    }

    public static int precisionToExp2(int precision){
        return (int)(precision / LOG10_2);
    }
    public static int exp2ToPrecision(int exp2){
        return (int)(exp2 * LOG10_2);
    }
    public static LWBigDecimal valueOf(String value, int precision){
        BigDecimal d1 = new BigDecimal(value);
        BigInteger r;
        int exp2 = precisionToExp2(precision);
        
        if(exp2 < 0){
            r = d1.multiply(BD_TWO.pow(-exp2)).toBigInteger();
        }else{
            r = d1.divide(BD_TWO.pow(exp2)).toBigInteger();    
        }
        
        return new LWBigDecimal(exp2, r);
    }

    public boolean isZero(){
        return value.signum() == 0;
    }

    public LWBigDecimal add(LWBigDecimal v){
        return add(this, v, this.exp2);
    }

    public LWBigDecimal subtract(LWBigDecimal v){
        return subtract(this, v, this.exp2);
    }

    public LWBigDecimal add(LWBigDecimal v, int precision){
        return add(this, v, precisionToExp2(precision));
    }

    public LWBigDecimal subtract(LWBigDecimal v, int precision){
        return subtract(this, v, precisionToExp2(precision));
    }

    private static LWBigDecimal add(LWBigDecimal a, LWBigDecimal b, int newExp2){
        return computeAdd(a, b, newExp2);
    }

    private static LWBigDecimal subtract(LWBigDecimal a, LWBigDecimal b, int newExp2){
        return computeAdd(a, b.negate(), newExp2);
    }

    private static LWBigDecimal computeAdd(LWBigDecimal a, LWBigDecimal b, int newExp2){
        int dExp = a.exp2 - b.exp2;
        boolean doSwap = dExp < 0;

        if(doSwap){
            LWBigDecimal ta = a;
            a = b;
            b = ta;
            dExp = -dExp;
        }
        
        int e = a.exp2 - newExp2;
        BigInteger v = a.value.add(b.value.shiftRight(dExp)).shiftLeft(e);

        return new LWBigDecimal(newExp2, v);
    }



    public LWBigDecimal multiply(LWBigDecimal v){
        return computeMultiply(this, v, this.exp2 + v.exp2);
    }

    public LWBigDecimal multiply(LWBigDecimal v, int precision){
        return computeMultiply(this, v, precisionToExp2(precision));
    }

    private static LWBigDecimal computeMultiply(LWBigDecimal a, LWBigDecimal b, int newExp2){
        int rExp = a.exp2 + b.exp2;
        int e = rExp - newExp2;
        BigInteger v = a.value.multiply(b.value).shiftLeft(e);
        return new LWBigDecimal(newExp2, v);
    }

    public LWBigDecimal divide(LWBigDecimal v){
        return computeDivide(this, v, this.exp2);
    }

    public LWBigDecimal divide(LWBigDecimal v, int precision){
        return computeDivide(this, v, precisionToExp2(precision));
    }

    private static LWBigDecimal computeDivide(LWBigDecimal a, LWBigDecimal b, int newExp2){
        int vbl = b.value.bitLength();
        int rExp = a.exp2 - vbl - b.exp2;
        int e = rExp - newExp2;
        BigInteger v = a.value.shiftLeft(vbl).divide(b.value).shiftLeft(e);
        return new LWBigDecimal(newExp2, v);
    }

    public LWBigDecimal multiplyExpOf2(int exp2){
        return new LWBigDecimal(this.exp2 + exp2, value);
    }

    public LWBigDecimal divideExpOf2(int exp2){
        return new LWBigDecimal(this.exp2 - exp2, value);
    }
    
    public LWBigDecimal square(){
        return computeSquare(exp2 * 2);
    }
    public LWBigDecimal square(int precision){
        return computeSquare(precisionToExp2(precision * 2));
    }
    private LWBigDecimal computeSquare(int newExp2){
        int e = exp2 * 2 - newExp2;
        BigInteger v = value.multiply(value).shiftLeft(e);
        return new LWBigDecimal(newExp2, v);
    }

    public LWBigDecimal limitBitLength(int bitLength){
        return setExp2(exp2 + value.bitLength() - bitLength);
    }


    public LWBigDecimal setPrecision(int precision){
        return setExp2(precisionToExp2(precision));
    }


    public LWBigDecimal setExp2(int exp2){
        BigInteger v = this.value.shiftLeft(this.exp2 - exp2);
        return new LWBigDecimal(exp2, v);
    }

    public LWBigDecimal negate(){
        return new LWBigDecimal(exp2, value.negate());
    }

    public boolean isPositive(){
        return value.compareTo(BigInteger.ZERO) > 0;
    }

    public boolean isNegative(){
        return value.compareTo(BigInteger.ZERO) < 0;
    }

    

    @Override
    public double doubleValue(){
        return value.doubleValue() * Math.pow(2, exp2);
    }

    @Override
    public int intValue() {
        return value.intValue() << exp2;
    }
    
    @Override
    public long longValue() {
        return value.longValue() << exp2;
    }
    
    @Override
    public float floatValue() {
        return value.floatValue() * (float)Math.pow(2, exp2);
    }


    private BigDecimal toBigDecimal(){
        BigDecimal d = new BigDecimal(value);
        if(exp2 < 0){
            d = d.divide(BD_TWO.pow(-exp2));
        }
        if(exp2 > 0){
            d = d.multiply(BD_TWO.pow(exp2));    
        }
        d = d.setScale(-exp2ToPrecision(exp2), RoundingMode.FLOOR);
        return d;
    }

    public BigInteger getValue() {
        return value;
    }

    public int getExp2() {
        return exp2;
    }

    @Override
    public String toString() {
        return toBigDecimal().toString();
    }

    public String toEngineeringString(){
        return toBigDecimal().toEngineeringString();
    }

    public String toPlainString(){
        return toBigDecimal().toPlainString();
    }

}