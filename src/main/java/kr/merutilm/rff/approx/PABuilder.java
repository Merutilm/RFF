package kr.merutilm.rff.approx;

public interface PABuilder<P extends PA> {

    long start();
    long skip();
    PABuilder<P> step();
    PABuilder<P> merge(P toMerge);
    P build();
}
