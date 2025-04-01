package kr.merutilm.rff.settings;

import kr.merutilm.rff.selectable.Selectable;

public enum VideoZoomingMethod implements Selectable {

    IMAGE("Image"),

    ITERATION_DATA("Iteration Data"),
    ;

    private final String name;

    @Override
    public String toString() {
        return name;
    }

    VideoZoomingMethod(String name){
        this.name = name;
    }
}
