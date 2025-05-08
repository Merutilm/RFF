package kr.merutilm.rff.settings;

import kr.merutilm.rff.selectable.Selectable;

public enum     ReuseReferenceMethod implements Selectable{
    /**
     * Reuse current reference
     */
    CURRENT_REFERENCE("Current"),
    /**
     * Get the centered reference using its period, and reuse this.
     */
    CENTERED_REFERENCE("Centered"),
    /**
     * Do not reuse reference, and recalculate reference every perturbator.
     */
    DISABLED("Disabled");
    
    private final String name;

    @Override
    public String toString() {
        return name;
    }

    ReuseReferenceMethod(String name){
        this.name = name;
    }
}
