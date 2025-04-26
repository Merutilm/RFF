package kr.merutilm.rff.approx;

import kr.merutilm.rff.formula.LightMandelbrotReference;
import kr.merutilm.rff.util.AdvancedMath;

public record LightPA(double anr, double ani, double bnr, double bni, int skip, double radius) implements PA {

    public static final class Builder{
        private double anr; 
        private double ani; 
        private double bnr;
        private double bni;
        private int skip; 
        private double radius;

        private final int start;
        private final LightMandelbrotReference reference;
        private final double epsilon;
        private final double dcMax;

        private Builder(LightMandelbrotReference reference, double epsilon, double dcMax, double anr, double ani, double bnr, double bni, int start, int skip, double radius){
            this.anr = anr;
            this.ani = ani;
            this.bnr = bnr;
            this.bni = bni;
            this.skip = skip;
            this.radius = radius;

            this.start = start;
            this.reference = reference;
            this.epsilon = epsilon;
            this.dcMax = dcMax;
        }

        public static Builder create(LightMandelbrotReference reference, double epsilon, double dcMax, int start) {
            return new Builder(reference, epsilon, dcMax, 1, 0, 0, 0, start, 0, Double.MAX_VALUE);
        }

        public int start(){
            return start;
        }

        public Builder merge(LightPA pa){
            double anrMerge = pa.anr * anr - pa.ani * ani;
            double aniMerge = pa.anr * ani + pa.ani * anr;
            double bnrMerge = pa.anr * bnr - pa.ani * bni + pa.bnr;
            double bniMerge = pa.anr * bni + pa.ani * bnr + pa.bni;

            radius = Math.min(radius, pa.radius);
            anr = anrMerge;
            ani = aniMerge;
            bnr = bnrMerge;
            bni = bniMerge;
            skip += pa.skip;
            return this;
        }

        public Builder step() {

            int iter = start + skip++; //n+k
            int index = reference.referenceCompressor().compress(iter);

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

        public LightPA build(){
            return new LightPA(anr, ani, bnr, bni, skip, radius);
        }
    }
    

   

    public boolean isValid(double dzRad){
        return dzRad < radius;
    }
}
