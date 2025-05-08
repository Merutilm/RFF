package kr.merutilm.rff.settings;

import kr.merutilm.rff.selectable.Selectable;

public enum MPASelectionMethod implements Selectable{
    /**
     * Check the lowest-level MPA first, increases the level until not valid.
     */
    LOWEST("Lowest"),
    /**
     * Check the highest-level MPA first. decreases the level if not valid.
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
