package kr.merutilm.rff.shader;

import kr.merutilm.rff.struct.HexColor;

/**
 * 셰이더 렌더기
 */
@FunctionalInterface
public interface BitMapRenderer extends ArrayRenderer{
    /**
     * 셰이더를 실행합니다.
     *
     * @param x  픽셀 좌표 x
     * @param y  픽셀 좌표 y
     * @param xRes Resolution X
     * @param yRes Resolution Y
     * @param rx 0~1 사이 상대 좌표 x
     * @param ry 0~1 사이 상대 좌표 y
     * @param i  배열에서 해당 픽셀의 인덱스
     * @param c  해당 픽셀의 색상
     * @param t  시각(초)
     */
    HexColor execute(int x, int y, int xRes, int yRes, double rx, double ry, int i, HexColor c, double t) throws IllegalRenderStateException;

    default boolean isValid(){
        return true;
    }
}