package kr.merutilm.rff.preset.location;

public class LocationDebug implements Location{
    @Override
    public String getName() {
        return "Debug Location";
    }

    @Override
    public String real() {
        return "-1.74333809768792994084178534356760177859720000525242911281075615845296593297085459317788654";
    }
    @Override
    public String imag() {
        return "-0.00000180836819716880795128873613161993554089471597685393367018109950778129103344440149385";
    }

    @Override
    public long maxIteration() {
        return 10_000_000_000L;
    }

    @Override
    public double logZoom() {
        return 72.02999999999986;
    }
}
