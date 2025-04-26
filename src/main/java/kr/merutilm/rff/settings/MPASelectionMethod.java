package kr.merutilm.rff.settings;

import kr.merutilm.rff.selectable.Selectable;

public enum MPASelectionMethod implements Selectable{
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

    MPASelectionMethod(String name){
        this.name = name;
    }
}
