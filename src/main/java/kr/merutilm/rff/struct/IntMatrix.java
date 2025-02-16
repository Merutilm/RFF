package kr.merutilm.rff.struct;

import java.util.Arrays;


public class IntMatrix implements Matrix{
    protected final int width;
    protected final int height;
    protected final int[] canvas;

    public IntMatrix(int width, int height, int[] canvas){
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

    public IntMatrix(int width, int height){
        this(width, height, new int[width * height]);
    }


    public int pipette(int i){
        return canvas[i];
    }

    public int pipette(int x, int y){
        return canvas[convertLocation(x, y)];
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


    public int[] getCanvas(){
        return canvas;
    }

    public int[] captureCurrentCanvas(){
        return Arrays.copyOf(canvas, canvas.length);
    }

    public IntMatrix cloneCanvas(){
        return new IntMatrix(width, height, captureCurrentCanvas());
    }

    public IntMatrix createAnother(int[] another){
        return new IntMatrix(width, height, another);
    }

}
