package kr.merutilm.rff.preset.resolution;

import java.awt.*;

public class ResolutionL1 implements Resolution {
    @Override
    public String getName() {
        return "640x360";
    }

    @Override
    public Dimension getResolution() {
        return new Dimension(640, 360);
    }
}
