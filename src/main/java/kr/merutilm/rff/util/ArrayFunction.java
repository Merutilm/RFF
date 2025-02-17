package kr.merutilm.rff.util;


public final class ArrayFunction {
    private ArrayFunction() {

    }


    public static boolean[] exp2xArr(boolean[] arr){
        boolean[] arr2 = new boolean[arr.length * 2];
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
