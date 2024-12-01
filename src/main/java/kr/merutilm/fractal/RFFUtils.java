package kr.merutilm.fractal;

import java.awt.*;
import java.io.File;

public final class RFFUtils {

    private RFFUtils() {

    }



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

    public static File generateNewFile(File file, String extension) {
        int count = 0;
        File originalFile = file;
        do {
            file = new File(originalFile, numberToDefaultFileName(++count) + "." + extension);
        } while (file.exists());
        return file;
    }
}
