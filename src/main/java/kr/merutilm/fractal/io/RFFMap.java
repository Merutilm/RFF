package kr.merutilm.fractal.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import kr.merutilm.base.struct.DoubleMatrix;
import kr.merutilm.fractal.RFFUtils;
import kr.merutilm.fractal.settings.Settings;

public record RFFMap(int version, double zoom, long maxIteration, DoubleMatrix iterations) {

    public static final int LATEST = 1;

    public static RFFMap read(File file){
        if(file == null || !file.exists()){
            return null;
        }

        RFFUtils.checkInvalidExtension(file,  RFFUtils.Extension.MAP.toString());

        byte[] data;

        try(FileInputStream stream = new FileInputStream(file)) {

            data = stream.readNBytes(Integer.BYTES);
            int width = IOManager.byteArrayToInt(data);

            data = stream.readNBytes(Integer.BYTES);
            int version = IOManager.byteArrayToInt(data);
            
            data = stream.readNBytes(Double.BYTES);
            double zoom = IOManager.byteArrayToDouble(data);
            
            data = stream.readNBytes(Long.BYTES);
            long maxIteration = IOManager.byteArrayToLong(data);

            data = stream.readNBytes(Integer.MAX_VALUE);
            int len = data.length / 8;
            int height = len / width;
            double[] iterations = new double[len];
            for (int i = 0; i < data.length; i += 8) {
                iterations[i / 8] = IOManager.byteArrayToDouble(Arrays.copyOfRange(data, i, i+8));
            }
            return new RFFMap(version, zoom, maxIteration, new DoubleMatrix(width, height, iterations));
        }catch (IOException e) {
            throw new IllegalStateException();
        }
    }
    /**
     * read map with name to XXXX.extension. (ID is XXXX)
     * @param dir Directory to read
     */
    public static RFFMap readByID(File dir, int id){
        return read(new File(dir, RFFUtils.numberToDefaultFileName(id) + "." + RFFUtils.Extension.MAP));
    }

    /**
     * export map with name to XXXX.extension.
     * This format must always be followed when creating a video.
     * 
     * @param dir Directory to export
     */
    public void exportAsVideoData(File dir){
        File dest = RFFUtils.generateNewFile(dir, RFFUtils.Extension.MAP.toString());
        export(dest);
    }

    public void export(File file){
        RFFUtils.checkInvalidExtension(file,  RFFUtils.Extension.MAP.toString());
        try(FileOutputStream stream = new FileOutputStream(file)) {
            double[] canvas = iterations.getCanvas();
            int width = iterations.getWidth();
            
            stream.write(IOManager.intToByteArray(width));
            stream.write(IOManager.intToByteArray(version));
            stream.write(IOManager.doubleToByteArray(zoom));
            stream.write(IOManager.longToByteArray(maxIteration));
            byte[] arr = new byte[canvas.length * 8];
            for (int i = 0; i < canvas.length; i++) {
                System.arraycopy(IOManager.doubleToByteArray(canvas[i]), 0, arr, i * 8, 8);
            }

            stream.write(arr);
            
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }
    public Settings modifyToMapSettings(Settings target){
        return target.edit().setCalculationSettings(e -> 
            e.edit()
            .setMaxIteration(maxIteration)
            .setLogZoom(zoom)
            .build()
        ).build();
    }
    
}
