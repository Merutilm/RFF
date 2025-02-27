package kr.merutilm.rff.parallel;

import kr.merutilm.rff.struct.HexColor;

@FunctionalInterface
public interface ParallelBitMapRenderer extends ParallelArrayRenderer{
    
    HexColor execute(int x, int y, int xRes, int yRes, double rx, double ry, int i, HexColor c, double t);

}