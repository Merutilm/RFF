package kr.merutilm.rff.preset.location;

public class LocationDebug implements Location{
    @Override
    public String getName() {
        return "Debug Location";
    }

    @Override
    public String real() {
        return "-1.99637964728227251425950443885133046301098773679334503357692908678220358661402173812";
    }
    @Override
    public String imag() {
        return "-0.00000434286540260736147850872236853245834034603952763598772951351124884187288130653";
    }

    @Override
    public long maxIteration() {
        return 3000000;
    }

    @Override
    public double logZoom() {
        return 68.02548525065176;
    }
}
