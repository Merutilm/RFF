package kr.merutilm.rff.struct;


public interface StructBuilder<T extends Record & Struct<T>> {
    T build();
}
