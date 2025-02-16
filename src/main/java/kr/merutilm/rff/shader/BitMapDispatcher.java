package kr.merutilm.rff.shader;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import kr.merutilm.rff.io.BitMap;
import kr.merutilm.rff.io.BitMapImage;
import kr.merutilm.rff.struct.HexColor;
import kr.merutilm.rff.struct.Point2D;
import kr.merutilm.rff.util.AdvancedMath;


public class BitMapDispatcher extends ArrayDispatcher<BitMap, BitMapRenderer>{


    /**
     * Init Shader Dispatcher
     *
     * @param renderState 렌더링에 필요한 고유 값을 정의합니다.
     * @param renderID    렌더링 고유 번호입니다. renderState와 고유 값이 일치하지 않으면 {@link IllegalRenderStateException 예외}를 발생시킵니다. 초기값은 0입니다.
     * @param bitMap      이미지의 픽셀 데이터입니다.
     * @see RenderState
     * @see BitMapImage#BitMapImage(String)
     */
    public BitMapDispatcher(RenderState renderState, int renderID, BitMap bitMap) throws IllegalRenderStateException{
        super(renderState, renderID, bitMap);
        tryBreak();
    }


    /**
     * 셰이더를 객체 생성에 전달한 원본 canvas 배열에 반영합니다.
     *
     * @return 결과 배열
     * @throws InterruptedException        스레드가 대기 상태일 때 강제 종료될 경우 호출됩니다
     * @throws IllegalStateException       렌더러가 없을 때 호출됩니다
     */
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


        for (BitMapRenderer renderer : renderers) {

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


    /**
     * 해당 셰이더가 적용되기 직전 좌표에 따른 색상을 구합니다. (왼쪽 위 : 0)
     * 적용될 셰이더가 여러 개일 경우, 이전에 사용한 모든 셰이더가 반영된 이미지를 대상으로 합니다.
     */
    public HexColor texture2D(int x, int y) {
        return HexColor.fromInteger(original.pipette(AdvancedMath.restrict(0, matrix.getWidth() - 1, x), AdvancedMath.restrict(0, matrix.getHeight() - 1, y)));
    }


    /**
     * 해당 셰이더가 적용되기 직전 좌표에 따른 색상을 구합니다. (왼쪽 위 : 0)
     * 적용될 셰이더가 여러 개일 경우, 이전에 사용한 모든 셰이더가 반영된 이미지를 대상으로 합니다.
     */
    public HexColor texture2D(Point2D p) {
        return texture2D((int) p.x(), (int) p.y());
    }



}
