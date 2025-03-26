package kr.merutilm.rff.preset.location;

public class LocationDebug implements Location{
    @Override
    public String getName() {
        return "Debug";
    }

    @Override
    public String real() {
        return "-1.765328304866448095944478090965071042190801096349123533507038677216888673264012585274305227644990432425677276289239672171955863785708574636427125355126428478336753044";
    }
    @Override
    public String imag() {
        return "-0.011166941784534805485928722718138232941859473030486144070448890345620165093083935599279975959207657642367311054128318635789525953156188864476684828509118805997690328";
    }

    @Override
    public long maxIteration() {
        return 3000000000L;
    }

    @Override
    public double logZoom() {
        return 148.2728332805643;
    }
}
