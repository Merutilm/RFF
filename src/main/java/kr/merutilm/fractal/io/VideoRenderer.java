package kr.merutilm.fractal.io;

import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

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
import kr.merutilm.customswing.CSFrame;
import kr.merutilm.fractal.RFFUtils;
import kr.merutilm.fractal.settings.Settings;
import kr.merutilm.fractal.ui.ShaderProcessor;
import kr.merutilm.fractal.util.MathUtilities;
import static kr.merutilm.fractal.RFFUtils.*;

public class VideoRenderer {
    private VideoRenderer(){

    }
    public static final String EXPORT_DIR = "Videos";
    private static final int VIDEO_PREVIEW_WINDOW_MAX_LEN = 640;

    
    private static byte[] iterationToByteArray(double iteration){
            
        long v = Double.doubleToLongBits(iteration);
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

    private static double byteArrayToIteration(byte[] arr){
            
        long a1 = (((long) arr[0]) & 0xff) << 56;
        long a2 = (((long) arr[1]) & 0xff) << 48;
        long a3 = (((long) arr[2]) & 0xff) << 40;
        long a4 = (((long) arr[3]) & 0xff) << 32;
        long a5 = (((long) arr[4]) & 0xff) << 24;
        long a6 = (((long) arr[5]) & 0xff) << 16;
        long a7 = (((long) arr[6]) & 0xff) << 8;
        long a8 = arr[7] & 0xff;
        long result = a1 | a2 | a3 | a4 | a5 | a6 | a7 | a8;
        return Double.longBitsToDouble(result);
    }
    
    
    public static void exportMap(DoubleMatrix iterations){
        File file = RFFUtils.mkdir(EXPORT_DIR);
        File dest = RFFUtils.generateNewFile(file, EXTENSION_MAP);

        try(FileOutputStream stream = new FileOutputStream(dest)) {
            double[] canvas = iterations.getCanvas();
            int width = iterations.getWidth();

            stream.write((byte)((width >>> 8) & 0x000000ffL));
            stream.write((byte)((width) & 0x000000ffL));
            byte[] arr = new byte[canvas.length * 8];
            for (int i = 0; i < canvas.length; i++) {
                System.arraycopy(iterationToByteArray(canvas[i]), 0, arr, i * 8, 8);
            }

            stream.write(arr);
            
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    private static DoubleMatrix readMapByID(int id){
        File file = RFFUtils.mkdir(EXPORT_DIR);
        File dest = new File(file, RFFUtils.numberToDefaultFileName(id) + "." + EXTENSION_MAP);
        if(!dest.exists()){
            return null;
        }
        try(FileInputStream stream = new FileInputStream(dest)) {
            byte[] data = stream.readAllBytes();
            int width = ((data[0] << 8) | (data[1] & 0xff));
            int len = (data.length - 2) / 8;
            int height = len / width;
            double[] iterations = new double[len];
            for (int i = 2; i < data.length; i+=8) {
                iterations[i / 8] = byteArrayToIteration(Arrays.copyOfRange(data, i, i+8));
            }
            return new DoubleMatrix(width, height, iterations);
        }catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    private static DoubleMatrix getFrame(RenderState state, int currentID, double frame) throws IllegalRenderStateException, InterruptedException{
        int f1 = (int) frame; // it is smaller
        int f2 = f1 + 1; 
        //frame size : f1 = 1x, f2 = 2x
        double r = 1 - frame + f1;
        DoubleMatrix m1 = readMapByID(f1);
        DoubleMatrix m2 = readMapByID(f2);

        if(m1 == null || m2 == null){
            return null;
        }

        double ssr = Math.pow(2, r - 1);
        double lsr = ssr * 2;

        DoubleMatrix result = new DoubleMatrix(m1.getWidth(), m1.getHeight());
        DoubleArrayDispatcher dispatcher = new DoubleArrayDispatcher(state, currentID, result);
        dispatcher.createRenderer((x, y, xRes, yRes, rx, ry, i, col, t) -> {
            double dx = xRes * (rx - 0.5);
            double dy = yRes * (ry - 0.5);
            double x1 = xRes / 2.0 + dx / ssr;
            double y1 = yRes / 2.0 + dy / ssr;
            double x2 = xRes / 2.0 + dx / lsr;
            double y2 = yRes / 2.0 + dy / lsr;
            if(x1 < 0 || x1 > xRes || y1 < 0 || y1 > yRes){
                return m2.pipetteAdvanced(x2, y2);
            }
            return m1.pipetteAdvanced(x1, y1);
        });
        dispatcher.dispatch();
        return result;
    }

    public static void exportZoomingVideo(Settings settings){
        double logZoomPerSecond = 0.5;
        double frameZoomingPerSecond = logZoomPerSecond / MathUtilities.LOG2;
        double fps = 25;
        double frameInterval = frameZoomingPerSecond / fps;
        File directory = new File(RFFUtils.getOriginalResource(), EXPORT_DIR);
        File out = new File(directory, "video.mp4");
        
        RenderState state = new RenderState();
        int currentID = state.getId();

        DoubleMatrix targetMatrix = readMapByID(1);
        if(targetMatrix == null){
            JOptionPane.showMessageDialog(null, "Cannot create video. There is no samples in the directory : Videos", "Export video", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int imgWidth = targetMatrix.getWidth();
        int imgHeight = targetMatrix.getHeight();

        TaskManager.runTask(() -> {
            
            try(
                FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(out, imgWidth, imgHeight);
                Frame f = new Frame(imgWidth, imgHeight, Frame.DEPTH_BYTE, 4);
                ){  
                DoubleMatrix frame;
                avutil.av_log_set_level(avutil.AV_LOG_QUIET);
                double currentFrameNumber = RFFUtils.generateFileNameNumber(directory, EXTENSION_MAP) - 1.0;
                recorder.setFrameRate(fps);
                recorder.setVideoQuality(0); // maximum quality  
                recorder.setFormat("mp4");
                recorder.setVideoBitrate(9000);
                recorder.start(); 
                AtomicReference<BufferedImage> img = new AtomicReference<>();
                double m = Math.min((double) VIDEO_PREVIEW_WINDOW_MAX_LEN / imgWidth, (double) VIDEO_PREVIEW_WINDOW_MAX_LEN / imgHeight);
                int w = (int)(imgWidth * m);
                int h = (int)(imgHeight * m);  
                CSFrame window = new CSFrame("Preview", RFFUtils.getApplicationIcon(), w, h){
                    @Override
                    public void paint(Graphics g) {
                        super.paint(g);
                        g.drawImage(img.get(), 0, 0, w, h, null);
                    }
                };
                
                window.setResizable(false);
                window.pack();
                window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                window.addWindowListener(new WindowAdapter(){
                    @Override
                    public void windowClosing(WindowEvent e){
                        state.createBreakpoint();
                        try{
                            Files.delete(out.toPath());
                        }catch(IOException f){
                            ConsoleUtils.logError(f);
                        }
                    }
                });
                while(true) {    
                    currentFrameNumber -= frameInterval;
                    frame = getFrame(state, currentID, currentFrameNumber);
                    if(frame == null){
                        break;
                    }
                    
                    img.set(ShaderProcessor.createImage(state, currentID, frame, settings, false));
                    Java2DFrameConverter.copy(img.get(), f);
                    recorder.record(f, avutil.AV_PIX_FMT_ABGR);
                    window.repaint();
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
