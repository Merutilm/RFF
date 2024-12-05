package kr.merutilm.fractal;

import java.awt.*;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public final class RFFUtils {

    private RFFUtils() {

    }

    public static final String EXTENSION_MAP = "rffm";


    
    private static JFileChooser setupChooser(String title, String extension, String desc){
        JFileChooser ch = new JFileChooser();
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

    public static File saveFile(String title, String extension, String desc){
        JFileChooser ch = setupChooser(title, extension, desc);
        int r = ch.showSaveDialog(null);
        if (r == JFileChooser.APPROVE_OPTION) {
		    return ch.getSelectedFile();
        }
        return null;
    }

    public static File selectFile(String title, String extension, String desc){
        JFileChooser ch = setupChooser(title, extension, desc);
        int r = ch.showOpenDialog(null);
        if (r == JFileChooser.APPROVE_OPTION) {
		    return ch.getSelectedFile();
        }
        return null;
    }

    public static File selectFolder(String title){
        JFileChooser ch = new JFileChooser();
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
