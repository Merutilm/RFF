package kr.merutilm.fractal.theme;

import javax.annotation.Nullable;

import kr.merutilm.base.selectable.Selectable;

import java.util.Arrays;

public enum BasicThemes implements Selectable{
    CLASSIC_1(new ThemeClassic1()),
    CLASSIC_2(new ThemeClassic2()),
    RAINBOW(new ThemeRainbow()),
    ADVANCED_RAINBOW(new ThemeAdvancedRainbow()),
    CINEMATIC(new ThemeCinematic()),
    AZURE(new ThemeAzure()),
    FLAME(new ThemeFlame()),
    DESERT(new ThemeDesert());


    private final BasicTheme generator;

    public BasicTheme getTheme() {
        return generator;
    }


    private BasicThemes(BasicTheme generator) {
        this.generator = generator;
    }


    @Nullable
    public static BasicThemes tryMatch(Theme theme){
        return Arrays.stream(values())
        .filter(e -> theme == e.getTheme())
        .findAny()
        .orElse(null);
    }

    @Override
    public String toString() {
        return generator.getName();
    }

}
