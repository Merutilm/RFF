package kr.merutilm.fractal.settings;

import kr.merutilm.base.selectable.Selectable;

public enum R3ASelectionMethod implements Selectable{
    /**
     * Check the lowest-level bla first, increases the level until not valid bla.
     */
    LOWEST("Lowest"),
    /**
     * Check the highest-level bla first. decreases the level if not valid.
     */
    HIGHEST("Highest");
    
    private final String name;

    @Override
    public String toString() {
        return name;
    }

    private R3ASelectionMethod(String name){
        this.name = name;
    }
}
