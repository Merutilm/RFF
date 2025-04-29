package kr.merutilm.rff.approx;

public interface PABuilder<P extends PA> {

    int start();
    int skip();
    PABuilder<P> step();
    PABuilder<P> merge(P toMerge);
    P build();
}
