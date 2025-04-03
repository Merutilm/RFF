package kr.merutilm.rff.opengl;

public interface GLTimeRenderer {

    long INIT_TIME = System.currentTimeMillis();

    static float getTime(){
        return (float) ((System.currentTimeMillis() - INIT_TIME) / 1000.0);
    }

    void setTime(float time);
}
