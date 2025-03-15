package kr.merutilm.rff.preset.shader;

import javax.annotation.Nullable;

import kr.merutilm.rff.selectable.Selectable;

import java.util.Arrays;

public enum BasicThemes implements Selectable{
    CLASSIC_1(new ShaderClassic1()),
    CLASSIC_2(new ShaderClassic2()),
    RAINBOW(new ThemeRainbow()),
    LONG_RAINBOW(new ShaderLongRainbowClassic()),
    LONG_RAINBOW_SHADED(new ShaderLongRainbow()),
    CINEMATIC(new ShaderCinematic()),
    AZURE(new ShaderAzure()),
    FLAME(new ShaderFlame()),
    DESERT(new ShaderDesert());


    private final BasicTheme generator;

    public BasicTheme getTheme() {
        return generator;
    }


    BasicThemes(BasicTheme generator) {
        this.generator = generator;
    }


    @Nullable
    public static BasicThemes tryMatch(Shader theme){
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
