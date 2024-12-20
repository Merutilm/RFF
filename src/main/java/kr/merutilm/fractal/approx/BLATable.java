package kr.merutilm.fractal.approx;

public interface BLATable {

    static int getMaxSkippableIteration(int period, int iterationInterval){
        return period - iterationInterval;
    }
}
