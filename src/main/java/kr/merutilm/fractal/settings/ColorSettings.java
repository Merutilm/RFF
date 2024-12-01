package kr.merutilm.fractal.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import kr.merutilm.base.struct.HexColor;
import kr.merutilm.base.struct.Struct;
import kr.merutilm.base.struct.StructBuilder;


public record ColorSettings(
        HexColor[] colors,
        ColorSmoothingSettings colorSmoothing,
        double iterationInterval,
        double offsetRatio
) implements Struct<ColorSettings> {

    @Override
    public Builder edit() {
        return new Builder()
                .setColors(List.of(colors))
                .setColorSmoothing(colorSmoothing)
                .setIterationInterval(iterationInterval)
                .setOffsetRatio(offsetRatio);
    }

    public static final class Builder implements StructBuilder<ColorSettings> {

        private final List<HexColor> colors = new ArrayList<>();
        private ColorSmoothingSettings colorSmoothing = ColorSmoothingSettings.NORMAL;
        private double iterationInterval = 150;
        private double offsetRatio = 0;

        public Builder add(int index, HexColor c) {
            colors.add(index, c);
            return this;
        }

        public Builder add(HexColor c) {
            colors.add(c);
            return this;
        }

        public Builder addRainbow() {
            add(HexColor.R_RED);
            add(HexColor.R_ORANGE);
            add(HexColor.R_YELLOW);
            add(HexColor.R_GREEN);
            add(HexColor.R_BLUE);
            add(HexColor.R_INDIGO);
            add(HexColor.R_VIOLET);
            return this;
        }

        public Builder addReversedRainbow() {
            add(HexColor.R_VIOLET);
            add(HexColor.R_INDIGO);
            add(HexColor.R_BLUE);
            add(HexColor.R_GREEN);
            add(HexColor.R_YELLOW);
            add(HexColor.R_ORANGE);
            add(HexColor.R_RED);
            return this;
        }

        public Builder remove(int i) {
            colors.remove(i);
            return this;
        }

        public Builder setColor(int i, HexColor c) {
            colors.set(i, c);
            return this;
        }

        public Builder setColors(List<HexColor> palette) {
            this.colors.clear();
            this.colors.addAll(palette);
            return this;
        }


        public Builder setColorSmoothing(ColorSmoothingSettings colorSmoothing) {
            this.colorSmoothing = colorSmoothing;
            return this;
        }

        public Builder setIterationInterval(double iterationInterval) {
            this.iterationInterval = iterationInterval;
            return this;
        }

        public Builder setOffsetRatio(double offsetRatio) {
            this.offsetRatio = offsetRatio;
            return this;
        }

        @Override
        public ColorSettings build() {
            return new ColorSettings(colors.toArray(HexColor[]::new), colorSmoothing, iterationInterval, offsetRatio);
        }
    }

    public HexColor getColor(double value) {
        double ratio = ((value / iterationInterval + offsetRatio) % 1 + 1) % 1;
        double i = ratio * colors.length;
        int i0 = (int) i;
        int i1 = i0 + 1;

        HexColor c1 = colors[i0 % colors.length];
        HexColor c2 = colors[i1 % colors.length];
        return HexColor.ratioDivide(c1, c2, i % 1);

    }


    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(colors), colorSmoothing, iterationInterval, offsetRatio);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ColorSettings c
               && Arrays.equals(c.colors, colors)
               && c.colorSmoothing == colorSmoothing
               && c.iterationInterval == iterationInterval
               && c.offsetRatio == offsetRatio;
    }

    @Override
    public final String toString() {
        return Arrays.toString(colors) + ", " + colorSmoothing + ", " + iterationInterval + ", " + offsetRatio;
    }

}
