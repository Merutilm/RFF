package kr.merutilm.rff.preset.location;

public class LocationLittleMinibrot1 implements Location{
    @Override
    public String real() {
        return "-0.7499831592705825251505394477389455403331";
    }

    @Override
    public String imag() {
        return "0.0386069146194831008037756148309259489230";
    }

    @Override
    public double logZoom() {
        return 23.61999999999997;
    }

    @Override
    public long maxIteration() {
        return 25000;
    }

    @Override
    public String getName() {
        return "Little Minibrot";
    }
}
