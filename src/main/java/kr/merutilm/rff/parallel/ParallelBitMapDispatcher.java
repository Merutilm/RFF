package kr.merutilm.rff.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import kr.merutilm.rff.io.BitMap;
import kr.merutilm.rff.struct.HexColor;
import kr.merutilm.rff.struct.Point2D;
import kr.merutilm.rff.util.AdvancedMath;
import kr.merutilm.rff.util.ConsoleUtils;

public class ParallelBitMapDispatcher extends ParallelArrayDispatcher<BitMap, ParallelBitMapRenderer> {

    public ParallelBitMapDispatcher(ParallelRenderState renderState, int renderID, BitMap bitMap)
            throws IllegalParallelRenderStateException {
        super(renderState, renderID, bitMap);
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
        final BitMap tex2DOriginal = this.original; //The elements of tex2D are unmodifiable.

        final int[] canvas = matrix.getCanvas();
        final int threads = Runtime.getRuntime().availableProcessors();
        final int rpy = matrix.getHeight() / threads + 1;
        final int xRes = matrix.getWidth();
        final int yRes = matrix.getHeight();

        for (ParallelBitMapRenderer renderer : renderers) {

            if(!renderer.isValid()){
                continue;
            }
            
            boolean[] renderedPixels = new boolean[matrix.getLength()];
            original = matrix.cloneCanvas(); // update tex2D to the canvas with applied previous shaders
            

            try(ExecutorService executor = Executors.newFixedThreadPool(threads)) {

                List<Future<Boolean>> processors = new ArrayList<>();

                for (int sy = 0; sy < yRes; sy += rpy) {

                    int finalSy = sy;
    
                    processors.add(executor.submit(() -> {
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
                                        HexColor c = renderer.execute(x, py, xRes, yRes, (double) x / xRes, (double) py / yRes, i, HexColor.fromInteger(original.pipette(i)), time);
                                        canvas[i] = c == null ? 0 : c.toRGB().toInteger();
                                        renderedAmount.getAndIncrement();
                                    }
                                }
                            }
    
    
                            for (int i = canvas.length - 1; i >= 0; i--) {
                                Point2D p = matrix.convertLocation(i);
                                renderState.tryBreak(renderID);
    
    
                                if (!renderedPixels[i]) {
                                    renderedPixels[i] = true;
                                    HexColor c = renderer.execute((int) p.x(), (int) p.y(), xRes, yRes, p.x() / xRes, p.y() / yRes, i, HexColor.fromInteger(original.pipette(i)), time);
                                    canvas[i] = c == null ? 0 : c.toRGB().toInteger();
                                    renderedAmount.getAndIncrement();
                                }
                            }
                            return true;
                        } catch (IllegalParallelRenderStateException ignored) {
                            return false;
                        }
                    }));
                    
                }

                for (Future<Boolean> processor : processors) {
                    if(Boolean.FALSE.equals(processor.get())){
                        return;
                    }
                }
            } catch (ExecutionException e) {
                ConsoleUtils.logError(e);
                return;
            }
        

        }

        if (original != tex2DOriginal) {
            original = tex2DOriginal; // revert to original canvas for reuse
        }

    }

    public HexColor texture2D(int x, int y) {
        return HexColor.fromInteger(original.pipette(AdvancedMath.restrict(0, matrix.getWidth() - 1, x),
                AdvancedMath.restrict(0, matrix.getHeight() - 1, y)));
    }

    public HexColor texture2D(Point2D p) {
        return texture2D((int) p.x(), (int) p.y());
    }

}
