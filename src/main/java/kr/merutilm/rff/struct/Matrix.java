package kr.merutilm.rff.struct;

import kr.merutilm.rff.util.AdvancedMath;

public interface Matrix {

    int getWidth();
    int getHeight();
    int getLength();



    default int convertLocation(int x, int y) {
        return convertLocation(x, y, getWidth(), getHeight());
    }
    
    default  Point2D convertLocation(int index) {
        return convertLocation(index, getWidth(), getHeight());
    }

    static int convertLocation(int x, int y, int width, int height) {
        x = AdvancedMath.restrict(0, width - 1, x);
        y = AdvancedMath.restrict(0, height - 1, y);

        return width * y + x;
    }
    
    static Point2D convertLocation(int index, int width, int height) {
        int x = index % width;
        int y = index / width;
        if (y >= height) {
            throw new ArrayIndexOutOfBoundsException("out of pixel range");
        }
        return new Point2D(x, y);
    }
}
