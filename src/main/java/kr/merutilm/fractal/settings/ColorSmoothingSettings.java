package kr.merutilm.fractal.settings;

import kr.merutilm.base.selectable.Selectable;

public enum ColorSmoothingSettings implements Selectable{
    NONE("None"), 
    NORMAL("Normal"),
    REVERSED("Reversed");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

    private ColorSmoothingSettings(String name){
        this.name = name;
    }
    
}
