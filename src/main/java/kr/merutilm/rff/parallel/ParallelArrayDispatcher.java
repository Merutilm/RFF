package kr.merutilm.rff.parallel;

import kr.merutilm.rff.struct.Matrix;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ParallelArrayDispatcher<M extends Matrix, R extends ParallelArrayRenderer> {

    protected final ParallelRenderState renderState;
    protected final int renderID;
    protected final double initTime;
    protected boolean rendered = false;
    protected final AtomicInteger renderedAmount = new AtomicInteger();
    protected final List<R> renderers = new ArrayList<>();
    protected final M matrix;
    protected M original;

    protected ParallelArrayDispatcher(ParallelRenderState renderState, int renderID, M matrix) {
        this.renderState = renderState;
        this.renderID = renderID;
        this.matrix = matrix;
        this.initTime = System.currentTimeMillis() / 1000.0;
    }

    public abstract void dispatch() throws InterruptedException, IllegalParallelRenderStateException;

    public synchronized void process(ParallelRenderProcessVisualizer visualizer, long intervalMS) throws IllegalParallelRenderStateException{
        AtomicBoolean processing = new AtomicBoolean(true);
        tryBreak();
        
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (processing.get() && visualizer != null) {
                        tryBreak();
                        visualizer.run((double) renderedAmount.get() / matrix.getLength() / renderers.size());
                    }else{
                        cancel();
                    }
                } catch (IllegalParallelRenderStateException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                    cancel();
                }
            }
        }, intervalMS, intervalMS);

        
        try {
            dispatch();
            processing.set(false);
            t.cancel();
            tryBreak();

            if (visualizer != null) {
                visualizer.run(1);
            }
        } catch (InterruptedException e) {
            processing.set(false);
            Thread.currentThread().interrupt();
        }

    }


    public final void tryBreak() throws IllegalParallelRenderStateException {
        renderState.tryBreak(renderID);
    }


    public void createRenderer(R renderer) throws IllegalParallelRenderStateException{
        this.renderers.add(renderer);
        tryBreak();
    }



    public M getOriginalMatrix() {
        return original;
    }

    public M getMatrix() {
        return matrix;
    }


}
