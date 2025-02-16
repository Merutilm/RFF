package kr.merutilm.rff.struct;


public interface Struct<T extends Record & Struct<T>> {
    StructBuilder<T> edit();
}
