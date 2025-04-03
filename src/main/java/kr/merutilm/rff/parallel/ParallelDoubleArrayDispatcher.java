package kr.merutilm.rff.parallel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import kr.merutilm.rff.struct.DoubleMatrix;
import kr.merutilm.rff.struct.Point2D;
import kr.merutilm.rff.util.AdvancedMath;
import kr.merutilm.rff.util.ConsoleUtils;


public class ParallelDoubleArrayDispatcher extends ParallelArrayDispatcher<DoubleMatrix, ParallelDoubleArrayRenderer>{
   

    public ParallelDoubleArrayDispatcher(ParallelRenderState renderState, int renderID, DoubleMatrix matrix) throws IllegalParallelRenderStateException{
        super(renderState, renderID, matrix);
        tryBreak();
    }

  
    public synchronized void dispatch() throws InterruptedException, IllegalParallelRenderStateException {

        if (renderers.isEmpty()) {
            return;
        }

        if (rendered) {
            throw new IllegalStateException("Dispatcher can execute only once");
        }
        rendered = true;

        final double time = System.currentTimeMillis() / 1000.0 - this.initTime;
        final DoubleMatrix tex2DOriginal = this.original; //The elements of tex2D are unmodifiable.
        final int threads = Runtime.getRuntime().availableProcessors();
        final int rpy = matrix.getHeight() / threads + 1;
        final int xRes = matrix.getWidth();
        final int yRes = matrix.getHeight();
        final double[] canvas = matrix.getCanvas();

        renderState.tryBreak(renderID);
        int[] rpyIndices = getRenderPriority(rpy);


        for (ParallelDoubleArrayRenderer renderer : renderers) {

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
                            for (int y : rpyIndices) {
                                int py = finalSy + y;
                                if (py >= yRes) {
                                    continue;
                                }

                                for (int x = 0; x < xRes; x++) {

                                    renderState.tryBreak(renderID);
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
                            return true;
                        } catch (IllegalParallelRenderStateException e) {
                            return false;
                        }
                    }));
                }

                for (Future<Boolean> processor : processors) {
                    if(Boolean.FALSE.equals(processor.get())){
                        tryBreak();
                        return;
                    }
                }
            } catch(ExecutionException e){
                ConsoleUtils.logError(e);
                return;
            }




        }

        if (original != tex2DOriginal) {
            original = tex2DOriginal; // revert to original canvas for reuse
        }

    }

    private static int[] getRenderPriority(int rpy) {
        int[] result = new int[rpy];
        int count = rpy / 2;
        int repetition = 1;
        int index = 1;

        while(count > 0) {

            for (int j = 0; j < repetition; j++) {
                result[index] = result[j] + count;
                index++;
            }

            repetition *= 2;
            count /= 2;
        }
        int[] cpy = Arrays.copyOfRange(result, 0, index);
        Arrays.sort(cpy);

        int cpyIndex = 0;
        while (index < result.length) {
            if(cpy.length <= cpyIndex || cpy[cpyIndex] != cpyIndex + count){
                result[index] = cpyIndex + count;
                index++;
                count++;

            }else cpyIndex++;
        }
        return result;
    }


    public double texture2D(int x, int y) {
        return original.pipette(AdvancedMath.restrict(0, matrix.getWidth() - 1, x), AdvancedMath.restrict(0, matrix.getHeight() - 1, y));
    }


    public double texture2D(Point2D p) {
        return texture2D((int) p.x(), (int) p.y());
    }


}
