package kr.merutilm.rff.shader;

import kr.merutilm.rff.struct.Matrix;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ArrayDispatcher<M extends Matrix, R extends ArrayRenderer> {

    protected final RenderState renderState;
    protected final int renderID;
    protected final double initTime;
    protected boolean rendered = false;
    protected final AtomicInteger renderedAmount = new AtomicInteger();
    protected final List<R> renderers = new ArrayList<>();
    protected final M matrix;
    protected M original;

    protected ArrayDispatcher(RenderState renderState, int renderID, M matrix) {
        this.renderState = renderState;
        this.renderID = renderID;
        this.matrix = matrix;
        this.initTime = System.currentTimeMillis() / 1000.0;
    }

    public abstract void dispatch() throws InterruptedException;

    public synchronized void process(ProcessVisualizer visualizer, long intervalMS) {
        AtomicBoolean processing = new AtomicBoolean(true);
        
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
                } catch (IllegalRenderStateException | InterruptedException e) {
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
        } catch (IllegalRenderStateException | InterruptedException e) {
            processing.set(false);
            Thread.currentThread().interrupt();
        }

    }


    public final void tryBreak() throws IllegalRenderStateException {
        renderState.tryBreak(renderID);
    }


    public void createRenderer(R renderer) throws IllegalRenderStateException{
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
