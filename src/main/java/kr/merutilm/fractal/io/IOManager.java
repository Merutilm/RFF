package kr.merutilm.fractal.io;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalProgressBarUI;

import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.parallel.DoubleArrayDispatcher;
import kr.merutilm.base.parallel.RenderState;
import kr.merutilm.base.struct.DoubleMatrix;
import kr.merutilm.base.util.ConsoleUtils;
import kr.merutilm.base.util.TaskManager;
import kr.merutilm.customswing.CSButton;
import kr.merutilm.customswing.CSFrame;
import kr.merutilm.customswing.CSPanel;
import kr.merutilm.fractal.RFFUtils;
import kr.merutilm.fractal.settings.Settings;
import kr.merutilm.fractal.settings.VideoSettings;
import kr.merutilm.fractal.ui.ShaderProcessor;
import kr.merutilm.fractal.util.MathUtilities;
import static kr.merutilm.fractal.RFFUtils.*;

public class IOManager {
    private IOManager(){

    }
    
    private static final int VIDEO_PREVIEW_WINDOW_MAX_LEN = 640;

    
    private static byte[] doubleToByteArray(double v){
        return longToByteArray(Double.doubleToLongBits(v));
    }

    private static byte[] longToByteArray(long v){
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

    private static byte[] intToByteArray(int v){
        byte[] arr = new byte[4];

        arr[0] = (byte)((v >>> 24) & 0xff);
        arr[1] = (byte)((v >>> 16) & 0xff);
        arr[2] = (byte)((v >>> 8) & 0xff);
        arr[3] = (byte)(v & 0xff);
        
        return arr;
    }

    
    private static int byteArrayToInt(byte[] arr){
        int a1 = (arr[0] & 0xff) << 24;
        int a2 = (arr[1] & 0xff) << 16;
        int a3 = (arr[2] & 0xff) << 8;
        int a4 = arr[3] & 0xff;
        return a1 | a2 | a3 | a4;
    }
    

    private static long byteArrayToLong(byte[] arr){
        
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

    private static double byteArrayToDouble(byte[] arr){
        return Double.longBitsToDouble(byteArrayToLong(arr));
    }
    
    public static Settings modifyToMapSettings(RFFMap map, Settings target){
        return target.edit().setCalculationSettings(e -> 
            e.edit().setMaxIteration(map.maxIteration()).build()
        ).build();
    }
    
    public static void exportMap(File dir, long maxIteration, DoubleMatrix iterations){
        File dest = RFFUtils.generateNewFile(dir, EXTENSION_MAP);

        try(FileOutputStream stream = new FileOutputStream(dest)) {
            double[] canvas = iterations.getCanvas();
            int width = iterations.getWidth();
            
            stream.write(intToByteArray(width));
            stream.write(longToByteArray(maxIteration));
            byte[] arr = new byte[canvas.length * 8];
            for (int i = 0; i < canvas.length; i++) {
                System.arraycopy(doubleToByteArray(canvas[i]), 0, arr, i * 8, 8);
            }

            stream.write(arr);
            
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }


    public static RFFMap readMap(File file){
        if(file == null || !file.exists()){
            return null;
        }
        byte[] data;

        try(FileInputStream stream = new FileInputStream(file)) {

            data = stream.readNBytes(Integer.BYTES);
            int width = byteArrayToInt(data);

            data = stream.readNBytes(Long.BYTES);
            long maxIteration = byteArrayToLong(data);

            data = stream.readNBytes(Integer.MAX_VALUE);
            int len = data.length / 8;
            int height = len / width;
            double[] iterations = new double[len];
            for (int i = 0; i < data.length; i += 8) {
                iterations[i / 8] = byteArrayToDouble(Arrays.copyOfRange(data, i, i+8));
            }
            return new RFFMap(maxIteration, new DoubleMatrix(width, height, iterations));
        }catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    private static RFFMap readMapByID(File dir, int id){
        return readMap(new File(dir, RFFUtils.numberToDefaultFileName(id) + "." + EXTENSION_MAP));
        
    }

    private static RFFMap getFrame(RenderState state, int currentID, File dir, double frame, double multiplier) throws IllegalRenderStateException, InterruptedException{
        int f1 = (int) frame; // it is smaller
        int f2 = f1 + 1; 
        //frame size : f1 = 1x, f2 = 2x
        double r = 1 - frame + f1;
        RFFMap im1 = readMapByID(dir, f1);
        RFFMap im2 = readMapByID(dir, f2);

        if(im1 == null || im2 == null){
            return null;
        }
        DoubleMatrix m1 = im1.iterations();
        DoubleMatrix m2 = im2.iterations();

        int w = m1.getWidth();
        int h = m1.getHeight();

        long i1 = im1.maxIteration();
        long i2 = im2.maxIteration();
        long min = Math.min(i1, i2);
        
        double ssr = Math.pow(2, r - 1);
        double lsr = ssr * 2;

        DoubleMatrix result = new DoubleMatrix((int)(w * multiplier), (int)(h * multiplier));
        
        DoubleArrayDispatcher dispatcher = new DoubleArrayDispatcher(state, currentID, result);
        dispatcher.createRenderer((x, y, xRes, yRes, rx, ry, i, v, t) -> {
            
            double dx = w * (rx - 0.5);
            double dy = h * (ry - 0.5);
            double x1 = w / 2.0 + dx / ssr;
            double y1 = h / 2.0 + dy / ssr;
            double x2 = w / 2.0 + dx / lsr;
            double y2 = h / 2.0 + dy / lsr;

            if(x1 < 0 || x1 >= w || y1 < 0 || y1 >= h){
                return m2.pipetteAdvanced(x2, y2);
            }
            return m1.pipetteAdvanced(x1, y1);
        });
        dispatcher.dispatch();
        return new RFFMap(min, result);
    }

    public static void exportZoomingVideo(Settings settings, VideoSettings videoSettings, File dir, File out){
        double logZoomPerSecond = videoSettings.logZoomPerSecond();
        double fps = videoSettings.fps();
        double frameZoomingPerSecond = logZoomPerSecond / MathUtilities.LOG2;
        double frameInterval = frameZoomingPerSecond / fps;
        
        RenderState state = new RenderState();
        int currentID = state.getId();
        RFFMap targetMap = readMapByID(dir, 1);
        if(targetMap == null){
            JOptionPane.showMessageDialog(null, "Cannot create video. There is no samples in the directory : Videos", "Export video", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DoubleMatrix targetMatrix = targetMap.iterations();
        int imgWidth; 
        int imgHeight;
        double multiplier = videoSettings.multiSampling();

        imgWidth = (int)(targetMatrix.getWidth() * multiplier);            
        imgHeight = (int)(targetMatrix.getHeight() * multiplier);
        
        TaskManager.runTask(() -> {
        
            try(
                FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(out, imgWidth, imgHeight);
                Frame f = new Frame(imgWidth, imgHeight, Frame.DEPTH_BYTE, 4);
                ){  
                RFFMap frame;
                avutil.av_log_set_level(avutil.AV_LOG_QUIET);
                int maxNumber = RFFUtils.generateFileNameNumber(dir, EXTENSION_MAP);
                double currentFrameNumber = maxNumber - 1.0;
                recorder.setFrameRate(fps);
                recorder.setVideoQuality(0); // maximum quality  
                recorder.setFormat("mp4");
                recorder.setVideoBitrate(9000);
                recorder.start(); 
                AtomicReference<BufferedImage> img = new AtomicReference<>();
                double m = Math.min((double) VIDEO_PREVIEW_WINDOW_MAX_LEN / imgWidth, (double) VIDEO_PREVIEW_WINDOW_MAX_LEN / imgHeight);
                int w = (int)(imgWidth * m);
                int h = (int)(imgHeight * m);  
                CSFrame window = new CSFrame("Preview", RFFUtils.getApplicationIcon(), w, h + CSButton.BUTTON_HEIGHT){
                    
                };
                CSPanel panel = new CSPanel(window){
                    @Override
                    public void paint(Graphics g) {
                        super.paint(g);
                        g.drawImage(img.get(), 0, 0, w, h, null);
                    }
                };
                panel.setBounds(0, 0, w, h);
                window.add(panel);
                JProgressBar bar = new JProgressBar();
                bar.setLayout(null);
                bar.setBackground(new Color(40,40,40));
                bar.setForeground(new Color(40, 140, 40));
                bar.setUI(new MetalProgressBarUI() {
                    @Override
                    protected Color getSelectionForeground() {
                       return Color.BLACK;
                    }
                    
                    @Override
                    protected Color getSelectionBackground() {
                        return Color.WHITE;
                    }
                });
                bar.setBorder(new LineBorder(Color.BLACK));
                bar.setBounds(0, h, w, CSButton.BUTTON_HEIGHT);
                bar.setMaximum(10000);
                bar.setStringPainted(true);
                bar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                window.add(bar);
                window.setResizable(false);
                window.pack();
                window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                window.getContentPane().setBackground(Color.BLACK);
                window.addWindowListener(new WindowAdapter(){
                    @Override
                    public void windowClosing(WindowEvent e){
                        state.createBreakpoint();
                    }
                });
                long startMillis = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                while(true) {    
                    currentFrameNumber -= frameInterval;
                    frame = getFrame(state, currentID, dir, currentFrameNumber, multiplier);
                    if(frame == null){
                        break;
                    }

                    Settings settingsModified = modifyToMapSettings(frame, settings);
                    img.set(ShaderProcessor.createImage(state, currentID, frame.iterations(), settingsModified, false));
                    Java2DFrameConverter.copy(img.get(), f);
                    recorder.record(f, avutil.AV_PIX_FMT_ABGR);
                    panel.repaint();
                    double progressRatio = (maxNumber - currentFrameNumber - 1) / maxNumber;
                    long spent = System.currentTimeMillis() - startMillis;
                    long remained = (long)((1 - progressRatio) / progressRatio * spent);

                    bar.setValue((int)(progressRatio * 10000));
                    bar.setString("Processing... " + (String.format("%.2f",progressRatio * 100)) + "% [" + sdf.format(new Date(remained)) + "]");
                }
                window.setVisible(false);
                window.dispose();
            
            }catch(IOException e){
                ConsoleUtils.logError(e);
            }catch(IllegalRenderStateException e){
                //noop
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        });
        
    }
}
