package kr.merutilm.rff.preset.resolution;

import java.awt.*;

public class ResolutionL4 implements Resolution {
    @Override
    public String getName() {
        return "1600x900";
    }

    @Override
    public Dimension getResolution() {
        return new Dimension(1600, 900);
    }
}
