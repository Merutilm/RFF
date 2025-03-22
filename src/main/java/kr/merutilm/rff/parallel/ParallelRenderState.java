package kr.merutilm.rff.parallel;
import java.util.function.IntConsumer;


public final class ParallelRenderState {

    private volatile int stateID = 0;

    private Thread currentThread = null;
    /**
     * Throws the {@link IllegalParallelRenderStateException#IllegalRenderStateException() Exception} when state ID and current ID do not match
     * @see ParallelRenderState#createBreakpoint()
     */
    public void tryBreak(int currentID) throws IllegalParallelRenderStateException {
        if (currentID != this.stateID) {
            throw new IllegalParallelRenderStateException("Render ID Changed during rendering");
        }
    }

    /**
     * Creates The Thread. it only works if the state ID and current ID match.
     * Otherwise, The {@link IllegalParallelRenderStateException#IllegalRenderStateException() Exception} will be thrown.
     * @param run Run a task what you want.
     */
    public synchronized void createThread(IntConsumer run) throws InterruptedException{
        if(currentThread != null){
            cancel();
        }

        int currentID = currentID();
                
        currentThread = new Thread(() -> run.accept(currentID));
        currentThread.start();
    }

    /**
     * Stops safely the thread.
     * @throws InterruptedException When {@link Thread#interrupt()} has invoked during {@link Thread#join()}.
     */
    public synchronized void cancel() throws InterruptedException{
        if (currentThread != null) {
            stateID++;
            currentThread.interrupt();
            currentThread.join();
            currentThread = null;
        }
    }


    /**
     * get current ID
     */
    public int currentID() {
        return stateID;
    }
}
