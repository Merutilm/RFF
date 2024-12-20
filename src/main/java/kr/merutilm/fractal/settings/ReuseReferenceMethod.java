package kr.merutilm.fractal.settings;

import kr.merutilm.base.selectable.Selectable;

public enum ReuseReferenceMethod implements Selectable{
    /**
     * Reuse current reference
     */
    CURRENT_REFERENCE("Current"),
    /**
     * Get the centered reference using its period, and reuse this.
     */
    CENTERED_REFERENCE("Centered"),
    /**
     * Do not reuse refernce, and recalculate reference every perturbator.
     */
    DISABLED("Disabled");
    
    private final String name;

    @Override
    public String toString() {
        return name;
    }

    private ReuseReferenceMethod(String name){
        this.name = name;
    }
}
