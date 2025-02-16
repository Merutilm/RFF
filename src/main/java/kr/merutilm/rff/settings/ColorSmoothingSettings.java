package kr.merutilm.rff.settings;

import kr.merutilm.rff.selectable.Selectable;

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
