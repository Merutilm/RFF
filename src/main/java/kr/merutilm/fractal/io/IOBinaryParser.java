package kr.merutilm.fractal.io;

public class IOBinaryParser {
    private IOBinaryParser(){

    }
    


    public static byte[] longToByteArray(long v){
        byte[] arr = new byte[8];

        arr[0] = (byte)((v >>> 56) & 0xff);
        arr[1] = (byte)((v >>> 48) & 0xff);
        arr[2] = (byte)((v >>> 40) & 0xff);
        arr[3] = (byte)((v >>> 32) & 0xff);
        arr[4] = (byte)((v >>> 24) & 0xff);
        arr[5] = (byte)((v >>> 16) & 0xff);
        arr[6] = (byte)((v >>> 8) & 0xff);
        arr[7] = (byte)(v & 0xff);
        
        return arr;
    }

    public static long byteArrayToLong(byte[] arr){
        
        long a1 = (((long) arr[0]) & 0xff) << 56;
        long a2 = (((long) arr[1]) & 0xff) << 48;
        long a3 = (((long) arr[2]) & 0xff) << 40;
        long a4 = (((long) arr[3]) & 0xff) << 32;
        long a5 = (((long) arr[4]) & 0xff) << 24;
        long a6 = (((long) arr[5]) & 0xff) << 16;
        long a7 = (((long) arr[6]) & 0xff) << 8;
        long a8 = arr[7] & 0xff;
        return a1 | a2 | a3 | a4 | a5 | a6 | a7 | a8;
    }

    
    public static byte[] doubleToByteArray(double v){
        return longToByteArray(Double.doubleToLongBits(v));
    }

    public static double byteArrayToDouble(byte[] arr){
        return Double.longBitsToDouble(byteArrayToLong(arr));
    }

    public static byte[] intToByteArray(int v){
        byte[] arr = new byte[4];

        arr[0] = (byte)((v >>> 24) & 0xff);
        arr[1] = (byte)((v >>> 16) & 0xff);
        arr[2] = (byte)((v >>> 8) & 0xff);
        arr[3] = (byte)(v & 0xff);
        
        return arr;
    }

    
    public static int byteArrayToInt(byte[] arr){
        int a1 = (arr[0] & 0xff) << 24;
        int a2 = (arr[1] & 0xff) << 16;
        int a3 = (arr[2] & 0xff) << 8;
        int a4 = arr[3] & 0xff;
        return a1 | a2 | a3 | a4;
    }
    
}
