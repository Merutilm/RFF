package kr.merutilm.rff.preset.location;

public class LocationDefault implements Location{
    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public String real() {
        return "-0.85";
    }
    @Override
    public String imag() {
        return "0";
    }

    @Override
    public long maxIteration() {
        return 100;
    }

    @Override
    public double logZoom() {
        return 2;
    }
    
}
