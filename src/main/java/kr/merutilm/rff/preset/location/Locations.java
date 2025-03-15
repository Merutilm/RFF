package kr.merutilm.rff.preset.location;

import javax.annotation.Nullable;


import java.util.Arrays;

public enum Locations{
    ;


    private final Location generator;

    Locations(Location generator) {
        this.generator = generator;
    }


    @Override
    public String toString() {
        return generator.getName();
    }
}
