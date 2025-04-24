package kr.merutilm.rff.preset.resolution;

import java.awt.*;

public class ResolutionL5 implements Resolution {
    @Override
    public String getName() {
        return "1920x1080";
    }

    @Override
    public Dimension getResolution() {
        return new Dimension(1920, 1080);
    }
}
