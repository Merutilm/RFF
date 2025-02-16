package kr.merutilm.rff.shader;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import kr.merutilm.rff.struct.DoubleMatrix;
import kr.merutilm.rff.struct.Point2D;
import kr.merutilm.rff.util.AdvancedMath;


public class DoubleArrayDispatcher extends ArrayDispatcher<DoubleMatrix, DoubleArrayRenderer>{
   

    public DoubleArrayDispatcher(RenderState renderState, int renderID, DoubleMatrix matrix) throws IllegalRenderStateException{
        super(renderState, renderID, matrix);
        tryBreak();
    }

  
    public synchronized void dispatch() throws InterruptedException {

        if (renderers.isEmpty()) {
            return;
        }

        if (rendered) {
            throw new IllegalStateException("Dispatcher can execute only once");
        }
        rendered = true;

        final double time = System.currentTimeMillis() / 1000.0 - this.initTime;
        final DoubleMatrix tex2DOriginal = this.original; //The elements of tex2D are unmodifiable.

        final double[] canvas = matrix.getCanvas();


        for (DoubleArrayRenderer renderer : renderers) {

            if(!renderer.isValid()){
                continue;
            }
            boolean[] renderedPixels = new boolean[matrix.getLength()];
            original = matrix.cloneCanvas(); // update tex2D to the canvas with applied previous shaders
            final int rpy = matrix.getHeight() / Runtime.getRuntime().availableProcessors() + 1;
            final int xRes = matrix.getWidth();
            final int yRes = matrix.getHeight();
            List<Thread> renderThreads = new ArrayList<>();


            for (int sy = 0; sy < yRes; sy += rpy) {

                int finalSy = sy;

                Thread t = new Thread(() -> {
                    try {
                        for (int y = 0; y < rpy; y++) {
                            for (int x = 0; x < xRes; x++) {
                                renderState.tryBreak(renderID);
                                int py = finalSy + y;

                                if (py >= yRes) {
                                    continue;
                                }
                                int i = matrix.convertLocation(x, py);

                                if (!renderedPixels[i]) {
                                    renderedPixels[i] = true;
                                    double c = renderer.execute(x, py, xRes, yRes, (double) x / xRes, (double) py / yRes, i, original.pipette(i), time);
                                    canvas[i] = c;
                                    renderedAmount.getAndIncrement();
                                }
                            }
                        }


                        for (int i = canvas.length - 1; i >= 0; i--) {
                            Point2D p = matrix.convertLocation(i);
                            renderState.tryBreak(renderID);


                            if (!renderedPixels[i]) {
                                renderedPixels[i] = true;
                                double c = renderer.execute((int) p.x(), (int) p.y(), xRes, yRes, p.x() / xRes, p.y() / yRes, i, original.pipette(i), time);
                                canvas[i] = c;
                                renderedAmount.getAndIncrement();
                            }
                        }

                    } catch (IllegalRenderStateException ignored) {
                        //noop
                    }
                });

                renderThreads.add(t);


            }
            renderThreads.forEach(Thread::start);

            for (Thread renderThread : renderThreads) {
                renderThread.join();
            }


        }

        if (original != tex2DOriginal) {
            original = tex2DOriginal; // revert to original canvas for reuse
        }

    }


    public double texture2D(int x, int y) {
        return original.pipette(AdvancedMath.restrict(0, matrix.getWidth() - 1, x), AdvancedMath.restrict(0, matrix.getHeight() - 1, y));
    }


    public double texture2D(Point2D p) {
        return texture2D((int) p.x(), (int) p.y());
    }


}
