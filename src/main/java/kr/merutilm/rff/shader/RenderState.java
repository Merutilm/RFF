package kr.merutilm.rff.shader;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import kr.merutilm.rff.util.TaskManager;


public final class RenderState {

    private final AtomicInteger stateID = new AtomicInteger();

    private Thread currentThread = null;
    /**
     * Throws the {@link IllegalRenderStateException#IllegalRenderStateException() Exception} when state ID and current ID do not match
     * @see RenderState#createBreakpoint()
     */
    public void tryBreak(int currentID) throws IllegalRenderStateException {
        if (currentID != this.stateID.get()) {
            throw new IllegalRenderStateException("Render ID Changed during rendering");
        }
    }

    /**
     * Creates The Thread. it only works if the state ID and current ID match.
     * Otherwise, The {@link IllegalRenderStateException#IllegalRenderStateException() Exception} will be thrown.
     * @param run Run a task what you want.
     */
    public synchronized void createThread(IntConsumer run) throws InterruptedException{
        if(currentThread != null){
            cancel();
        }

        int currentID = currentID();
        currentThread = TaskManager.runTask(() -> run.accept(currentID));
    }

    /**
     * Stops safely the thread.
     * @throws InterruptedException When {@link Thread#interrupt()} has invoked during {@link Thread#join()}.
     */
    public synchronized void cancel() throws InterruptedException{
        if (currentThread != null) {
            createBreakpoint();
            currentThread.interrupt();
            currentThread.join();
            currentThread = null;
        }
    }


    /**
     * get current ID
     */
    public int currentID() {
        return stateID.get();
    }
    /**
     * Increases ID value.
     * The previously created thread will be thrown an {@link IllegalRenderStateException#IllegalRenderStateException() exception} and exit because the state ID and current ID do not match.
     */
    private void createBreakpoint(){
        stateID.getAndIncrement();
    }
}
