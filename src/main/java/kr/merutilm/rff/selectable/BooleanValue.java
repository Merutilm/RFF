package kr.merutilm.rff.selectable;

import java.util.Arrays;

public enum BooleanValue implements Selectable {
    TRUE(true),
    FALSE(false);

    private final boolean name;

    public boolean bool() {
        return name;
    }
    @Override
    public String toString() {
        return String.valueOf(name);
    }

    BooleanValue(boolean name) {
        this.name = name;
    }
    public static BooleanValue typeOf(String name) {
        return name == null ? null : Arrays.stream(values())
                .filter(value -> String.valueOf(value.name).equals(name))
                .findAny()
                .orElseThrow(() -> new NullPointerException(name));
    }

    public static BooleanValue typeOf(boolean b) {
        return Arrays.stream(values())
                .filter(value -> value.name == b)
                .findAny()
                .orElseThrow(() -> new InternalError("??")); //boolean has only two value (true,false), it cannot be thrown
    }
}
