package kr.merutilm.rff.settings;

import kr.merutilm.rff.selectable.Selectable;

public enum R3ACompressionMethod implements Selectable{
    /**
     * Do Not Compress. It is the fastest, but allocates the most memory.
     */
    NO_COMPRESSION("No compression"),
    /**
     * Compresses using elements' count each period. Both speed and memory usage are average.
     */
    LITTLE_COMPRESSION("Little Compression"),
    /**
     * Compresses most duplicate elements. It allocates the less memory, but the speed is also slow.
     */
    STRONGEST("Strongest");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

    R3ACompressionMethod(String name){
        this.name = name;
    }
}
