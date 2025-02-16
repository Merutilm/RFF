package kr.merutilm.rff.util;

/**
 * 작업 관리자
 */
public final class TaskManager {
    private TaskManager() {
    }

    /**
     * 작업을 실행합니다.
     *
     * @param task 작업
     */
    public static Thread runTask(Runnable task) {
        Thread t = new Thread(task);
        t.start();
        return t;
    }
}
