package kr.merutilm.rff.struct;

import java.util.Arrays;
import java.util.function.IntFunction;

public class MatrixObj<T> implements Matrix{
    protected final int width;
    protected final int height;
    protected final T[] canvas;

    public MatrixObj(int width, int height, T[] canvas){
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

    public MatrixObj(int width, int height, IntFunction<T[]> generator){
        this(width, height, generator.apply(width * height));
    }


    public T pipette(int i){
        return canvas[i];
    }

    public T pipette(int x, int y){
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


    public T[] getCanvas(){
        return canvas;
    }

    public T[] captureCurrentCanvas(){
        return Arrays.copyOf(canvas, canvas.length);
    }

    public <C extends MatrixObj<T>> C cloneCanvas(Constructor<T, C> constructor){
        return constructor.construct(width, height, captureCurrentCanvas());
    }

    public <C extends MatrixObj<T>> C createAnotherBitMap(T[] another, Constructor<T, C> constructor){
        return constructor.construct(width, height, another);
    }

    @FunctionalInterface
    public interface Constructor<T, C extends MatrixObj<T>>{
        C construct(int width, int height, T[] canvas);
    }

    
}
