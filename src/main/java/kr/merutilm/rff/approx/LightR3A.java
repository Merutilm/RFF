package kr.merutilm.rff.approx;

import kr.merutilm.rff.formula.LightMandelbrotReference;
import kr.merutilm.rff.util.AdvancedMath;

public record LightR3A(double anr, double ani, double bnr, double bni, int skip, double radius) implements R3A{

    public static final class Builder{
        private final int start;
        private double anr; 
        private double ani; 
        private double bnr;
        private double bni;
        private int skip; 
        private double radius;

        private Builder(double anr, double ani, double bnr, double bni, int start, int skip, double radius){
            this.anr = anr;
            this.ani = ani;
            this.bnr = bnr;
            this.bni = bni;
            this.start = start;
            this.skip = skip;
            this.radius = radius;
        }

        public static Builder create(int start) {
            return new Builder(1, 0, 0, 0, start, 0, Double.MAX_VALUE);
        }

        public int start(){
            return start;
        }

        public Builder step(LightMandelbrotReference reference, double epsilon, double dcMax) {

            int iter = start + skip++; //n+k
            int index = reference.index(iter);

            double z2r = 2 * reference.refReal()[index];
            double z2i = 2 * reference.refImag()[index];
            double anrStep = anr * z2r - ani * z2i;
            double aniStep = anr * z2i + ani * z2r;
            double bnrStep = bnr * z2r - bni * z2i + 1;
            double bniStep = bnr * z2i + bni * z2r;
    
            double z2l = AdvancedMath.hypotApproximate(z2r, z2i);
            double anlOriginal = AdvancedMath.hypotApproximate(anr, ani);
            double bnlOriginal = AdvancedMath.hypotApproximate(bnr, bni);
    
            
            radius = Math.min(radius, (epsilon * z2l - bnlOriginal * dcMax) / anlOriginal);
            anr = anrStep;
            ani = aniStep;
            bnr = bnrStep;
            bni = bniStep;
            return this;
        }

        public LightR3A build(){
            return new LightR3A(anr, ani, bnr, bni, skip, radius);
        }
    }
    

   

    public boolean isValid(double dzr, double dzi){
        return AdvancedMath.hypotApproximate(dzr, dzi) < radius;
    }
}
