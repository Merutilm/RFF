package kr.merutilm.rff.struct;

public record LWBigComplex(LWBigDecimal re, LWBigDecimal im) {


    public static LWBigComplex zero(int precision) {
        return new LWBigComplex(LWBigDecimal.zero(precision), LWBigDecimal.zero(precision));
    }

    public static LWBigComplex valueOf(double re, double im, int precision){
        return valueOf(String.valueOf(re), String.valueOf(im), precision);
    }

    public static LWBigComplex valueOf(DoubleExponent re, DoubleExponent im, int precision) {
        return new LWBigComplex(re.toLWBigDecimal(precision), im.toLWBigDecimal(precision));
    }

    public static LWBigComplex valueOf(String re, String im) {
        return valueOf(re, im, -Math.max(re.length(), im.length()));
    }

    public static LWBigComplex valueOf(String re, String im, int precision) {
        return new LWBigComplex(LWBigDecimal.valueOf(re, precision), LWBigDecimal.valueOf(im, precision));
    }

    public LWBigComplex add(LWBigComplex complex, int precision) {

        LWBigDecimal re = this.re.add(complex.re, precision);
        LWBigDecimal im = this.im.add(complex.im, precision);

        return new LWBigComplex(re, im);
    }

    public LWBigComplex square(int precision){
        LWBigDecimal apb = this.re.add(this.im, precision);
        LWBigDecimal amb = this.re.subtract(this.im, precision);
        
        LWBigDecimal ab = this.re.multiply(this.im, precision);
        LWBigDecimal re = apb.multiply(amb, precision);
       
        LWBigDecimal im = ab.multiplyExpOf2(1);
        return new LWBigComplex(re, im);

    }


    public LWBigComplex subtract(LWBigComplex complex, int precision) {
        LWBigDecimal re = this.re.subtract(complex.re, precision);
        LWBigDecimal im = this.im.subtract(complex.im, precision);

        return new LWBigComplex(re, im);
    }

    public LWBigComplex multiply(LWBigComplex complex, int precision) {
        LWBigDecimal re = this.re.multiply(complex.re, precision).subtract(this.im.multiply(complex.im, precision), precision);
        LWBigDecimal im = this.re.multiply(complex.im, precision).add(this.im.multiply(complex.re, precision), precision);
        return new LWBigComplex(re, im);
    }


    public LWBigComplex divide(LWBigComplex complex, int precision) {
        LWBigDecimal div = complex.radius2();
        LWBigDecimal re = this.re.multiply(complex.re, precision).add(this.im.multiply(complex.im, precision), precision).divide(div, precision);
        LWBigDecimal im = this.re.multiply(complex.im, precision).negate().add(this.im.multiply(complex.re, precision), precision).divide(div, precision);
        return new LWBigComplex(re, im);
    }

    public LWBigComplex doubled() {
        return new LWBigComplex(re.multiplyExpOf2(1), im.multiplyExpOf2(1));
    }

    public LWBigComplex multiply(LWBigDecimal m, int precision) {
        return new LWBigComplex(re.multiply(m, precision), im.multiply(m, precision));
    }

    public LWBigDecimal radius2() {
        return re.multiply(re).add(im.multiply(im));
    }


    @Override
    public boolean equals(Object o) {
        return o instanceof LWBigComplex c && re.equals(c.re) && im.equals(c.im);
    }

    @Override
    public String toString() {
        if (im.isPositive()) {
            return (re.isZero() ? "" : re + "+") + im + "i";
        }
        if (im.isNegative()) {
            return (re.isZero() ? "" : String.valueOf(re)) + im + "i";
        }
        return String.valueOf(re);
    }

    public LWBigComplex negate() {
        return new LWBigComplex(re.negate(), im.negate());
    }
}

