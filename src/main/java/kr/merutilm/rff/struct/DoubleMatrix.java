package kr.merutilm.rff.struct;

import java.util.Arrays;

import kr.merutilm.rff.util.AdvancedMath;


public class DoubleMatrix implements Matrix{
    protected final int width;
    protected final int height;
    protected final double[] canvas;

    public DoubleMatrix(int width, int height, double[] canvas){
        if(width <= 0 || height <= 0){
            throw new IllegalArgumentException("Length : " + width + "x" + height);
        }
        if(canvas.length != width * height){
            throw new IllegalArgumentException("Length mismatch : " + width + "x" + height + "=" + width * height + ", but the provided array length is " + canvas.length);
        }

        this.width = width;
        this.height = height;
        this.canvas = canvas;
    }

    public DoubleMatrix(int width, int height){
        this(width, height, new double[width * height]);
    }


    public double pipette(int i){
        return canvas[i];
    }

    public double pipette(int x, int y){
        return canvas[convertLocation(x, y)];
    }

    public double pipetteAdvanced(double x, double y) {

        double px = x % 1;
        double py = y % 1;
        int x1 = (int) x;
        int y1 = (int) y;
        int x2 = (int) x + 1;
        int y2 = (int) y + 1;

        double c1 = pipette(x1, y1);
        double c2 = pipette(x2, y1);
        double c3 = pipette(x1, y2);
        double c4 = pipette(x2, y2);

        double cc1 = AdvancedMath.ratioDivide(c1, c2, px);
        double cc2 = AdvancedMath.ratioDivide(c3, c4, px);

        return AdvancedMath.ratioDivide(cc1, cc2, py);
    }
    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getLength(){
        return width * height;
    }


    public double[] getCanvas(){
        return canvas;
    }

    public double[] captureCurrentCanvas(){
        return Arrays.copyOf(canvas, canvas.length);
    }

    public DoubleMatrix cloneCanvas(){
        return new DoubleMatrix(width, height, captureCurrentCanvas());
    }

    public DoubleMatrix createAnother(double[] another){
        return new DoubleMatrix(width, height, another);
    }

}
