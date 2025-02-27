package kr.merutilm.rff.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import kr.merutilm.rff.struct.DoubleMatrix;
import kr.merutilm.rff.util.IOUtilities;
import kr.merutilm.rff.settings.Settings;

public record RFFMap(double zoom, int period, long maxIteration, DoubleMatrix iterations) {

    public static RFFMap read(File file){
        if(file == null || !file.exists()){
            return null;
        }

        IOUtilities.checkInvalidExtension(file,  IOUtilities.Extension.MAP.toString());

        byte[] data;

        try(FileInputStream stream = new FileInputStream(file)) {

            data = stream.readNBytes(Integer.BYTES);
            int width = IOBinaryParser.byteArrayToInt(data);

            data = stream.readNBytes(Double.BYTES);
            double zoom = IOBinaryParser.byteArrayToDouble(data);
            
            data = stream.readNBytes(Integer.BYTES);
            int period = IOBinaryParser.byteArrayToInt(data);

            data = stream.readNBytes(Long.BYTES);
            long maxIteration = IOBinaryParser.byteArrayToLong(data);

            data = stream.readNBytes(Integer.MAX_VALUE);
            int len = data.length / 8;
            int height = len / width;
            double[] iterations = new double[len];
            for (int i = 0; i < data.length; i += 8) {
                iterations[i / 8] = IOBinaryParser.byteArrayToDouble(Arrays.copyOfRange(data, i, i+8));
            }
            return new RFFMap(zoom, period, maxIteration, new DoubleMatrix(width, height, iterations));
        }catch (IOException e) {
            throw new IllegalStateException();
        }
    }
    /**
     * read map with name to XX.extension. (ID is XX)
     * @param dir Directory to read
     */
    public static RFFMap readByID(File dir, int id){
        return read(new File(dir, IOUtilities.numberToDefaultFileName(id) + "." + IOUtilities.Extension.MAP));
    }

    /**
     * export map with name to XX.extension. (ID is XX)
     * This format must always be followed when creating a video.
     * 
     * @param dir Directory to export
     */
    public void exportAsVideoData(File dir){
        File dest = IOUtilities.generateNewFile(dir, IOUtilities.Extension.MAP.toString());
        export(dest);
    }

    public void export(File file){
        IOUtilities.checkInvalidExtension(file,  IOUtilities.Extension.MAP.toString());
        try(FileOutputStream stream = new FileOutputStream(file)) {
            double[] canvas = iterations.getCanvas();
            int width = iterations.getWidth();
            
            stream.write(IOBinaryParser.intToByteArray(width));
            stream.write(IOBinaryParser.doubleToByteArray(zoom));
            stream.write(IOBinaryParser.intToByteArray(period));
            stream.write(IOBinaryParser.longToByteArray(maxIteration));
            byte[] arr = new byte[canvas.length * 8];
            for (int i = 0; i < canvas.length; i++) {
                System.arraycopy(IOBinaryParser.doubleToByteArray(canvas[i]), 0, arr, i * 8, 8);
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
