package kr.merutilm.rff.util;

import java.awt.*;
import java.io.File;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public final class IOUtilities {

    private IOUtilities() {

    }

    public enum Extension{

        MAP("rfm"),
        VIDEO_DATA_SETTINGS("rfv"),
        ALL_SETTINGS("rfs"),
        CALCULATION_SETTINGS("rfc"),
        IMAGE_SETTINGS("rfi"),
        COLOR_PALETTE("rfp"),
        IMAGE("png"),
        VIDEO("mp4"),
        
        ;

        private final String name;
        
        @Override
        public String toString() {
            return name;
        }
        
        Extension(String name){
            this.name = name;
        }
    }

    public enum DefaultDirectory{
        MAP_AS_VIDEO_DATA("videos"),
        MAP("maps"),
        IMAGE("images"),
        ;

        private final String name;
        
        @Override
        public String toString() {
            return name;
        }
        
        DefaultDirectory(String name){
            this.name = name;
        }
    }
    
    public enum DefaultFileName{
        VIDEO("video"),
        VIDEO_DATA_SETTINGS("data"),
        ;

        private final String name;
        
        @Override
        public String toString() {
            return name;
        }
        
        DefaultFileName(String name){
            this.name = name;
        }
    }

    public enum Constants{
        OFFSET_RATIO("Offset Ratio"),
        OPACITY("Opacity"),
        ;

        private final String name;
        
        @Override
        public String toString() {
            return name;
        }
        
        Constants(String name){
            this.name = name;
        }
    }
    
    public static void checkInvalidExtension(File file, String ext){
        if(!file.getName().endsWith("." + ext)){
            throw new IllegalArgumentException("Invalid Extension");
        }
    }
    private static JFileChooser setupChooser(String title, File defaultFilePath, String extension, String desc){
        JFileChooser ch = new JFileChooser(defaultFilePath.isDirectory() ? defaultFilePath : null);
        ch.setDialogTitle(title);
        ch.setMultiSelectionEnabled(false);
        ch.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getPath().endsWith("." + extension);
            }

            @Override
            public String getDescription() {
                return desc + "(." + extension + ")";
            }
        });
        ch.setAcceptAllFileFilterUsed(false);
        return ch;
    }

    public static File saveFile(String title, File defaultFilePath, String extension, String desc){
        JFileChooser ch = setupChooser(title, defaultFilePath, extension, desc);
        int r = ch.showSaveDialog(null);
        if (r == JFileChooser.APPROVE_OPTION) {
		    return ch.getSelectedFile();
        }
        return null;
    }

    public static File openFile(String title, File defaultFilePath, String extension, String desc){
        JFileChooser ch = setupChooser(title, defaultFilePath, extension, desc);
        int r = ch.showOpenDialog(null);
        if (r == JFileChooser.APPROVE_OPTION) {
		    return ch.getSelectedFile();
        }
        return null;
    }

    public static File openFolder(String title, File defaultFilePath){
        JFileChooser ch = new JFileChooser(defaultFilePath.isDirectory() ? defaultFilePath : null);
        ch.setDialogTitle(title);
        ch.setMultiSelectionEnabled(false);
        ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int r = ch.showOpenDialog(null);
        if (r == JFileChooser.APPROVE_OPTION) {
		    return ch.getSelectedFile();
        }
        return null;
    }

    public static Image getApplicationIcon() {
        return Toolkit.getDefaultToolkit().getImage(getResource("/icon.png"));
    }

    public static URL getResource(String file) {
        return IOUtilities.class.getResource(file);
    }


    public static File getOriginalResource() {
        return new File("src/test/documents");
    }

    public static File mkdir(String dir) {
        File file = new File(IOUtilities.getOriginalResource(), dir);
        file.mkdir();//noop
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
