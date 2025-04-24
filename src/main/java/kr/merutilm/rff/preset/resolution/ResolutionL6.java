package kr.merutilm.rff.preset.resolution;

import java.awt.*;

public class ResolutionL6 implements Resolution {
    @Override
    public String getName() {
        return "2560x1440";
    }

    @Override
    public Dimension getResolution() {
        return new Dimension(2560, 1440);
    }
}
