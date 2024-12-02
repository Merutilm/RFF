package kr.merutilm.fractal;

import java.awt.*;
import java.io.File;

public final class RFFUtils {

    private RFFUtils() {

    }

    public static final String EXTENSION_MAP = "rffm";


    public static Image getApplicationIcon() {
        return Toolkit.getDefaultToolkit().getImage(RFFUtils.class.getResource("/icon.png"));
    }

    public static File getOriginalResource() {
        return new File("RFF/src/main/resources");
    }

    public static File mkdir(String dir) {
        File file = new File(RFFUtils.getOriginalResource(), dir);
        if (file.mkdir()) {
            //noop
        } 
        return file;
    }

    public static String numberToDefaultFileName(int number){
        return String.format("%04d", number);
    }

    public static File generateFileName(File dir, int id, String extension){
        return new File(dir, numberToDefaultFileName(id) + "." + extension);
    }

    public static int generateFileNameNumber(File dir, String extension){
        int count = 0;
        File original = dir;
        do {
            dir = generateFileName(original, ++count, extension);
        } while (dir.exists());
        return count;
    }

    public static File generateNewFile(File dir, String extension) {
        int count = 0;
        File originalFile = dir;
        do {
            dir = generateFileName(originalFile, ++count, extension);
        } while (dir.exists());
        return dir;
    }
}
