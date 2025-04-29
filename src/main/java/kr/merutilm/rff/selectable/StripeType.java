package kr.merutilm.rff.selectable;


public enum StripeType implements Selectable {
    NONE("None"),
    SINGLE_DIRECTION("Single Direction"),
    SMOOTH("Smooth"),
    SMOOTH_SQUARED("Smooth Squared"),
    ;

    private final String name;

    @Override
    public String toString() {
        return name;
    }

    StripeType(String name) {
        this.name = name;
    }

}
