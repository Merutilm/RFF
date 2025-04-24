package kr.merutilm.rff.preset.resolution;

import java.awt.*;

public class ResolutionL3 implements Resolution {
    @Override
    public String getName() {
        return "1280x720";
    }

    @Override
    public Dimension getResolution() {
        return new Dimension(1280, 720);
    }
}
