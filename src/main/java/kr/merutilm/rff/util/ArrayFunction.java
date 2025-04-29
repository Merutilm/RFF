package kr.merutilm.rff.util;


import java.util.function.IntFunction;

public final class ArrayFunction {
    private ArrayFunction() {

    }

    public static <T> T[] exp2xArr(T[] arr, IntFunction<T[]> constructor){
        T[] arr2 = constructor.apply(arr.length * 2);
        System.arraycopy(arr, 0, arr2, 0, arr.length);
        return arr2;
    }

    public static boolean[] exp2xArr(boolean[] arr){
        boolean[] arr2 = new boolean[arr.length * 2];
        System.arraycopy(arr, 0, arr2, 0, arr.length);
        return arr2;
    }
    public static long[] exp2xArr(long[] arr){
        long[] arr2 = new long[arr.length * 2];
        System.arraycopy(arr, 0, arr2, 0, arr.length);
        return arr2;
    }
    public static int[] exp2xArr(int[] arr){
        int[] arr2 = new int[arr.length * 2];
        System.arraycopy(arr, 0, arr2, 0, arr.length);
        return arr2;
    }
    public static double[] exp2xArr(double[] arr){
        double[] arr2 = new double[arr.length * 2];
        System.arraycopy(arr, 0, arr2, 0, arr.length);
        return arr2;
    }
}
