package kr.merutilm.rff.util;


public final class ArrayFunction {
    private ArrayFunction() {

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
