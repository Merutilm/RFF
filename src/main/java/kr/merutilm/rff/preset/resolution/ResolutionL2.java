package kr.merutilm.rff.preset.resolution;

import java.awt.*;

public class ResolutionL2 implements Resolution {
    @Override
    public String getName() {
        return "960x540";
    }

    @Override
    public Dimension getResolution() {
        return new Dimension(960, 540);
    }
}
