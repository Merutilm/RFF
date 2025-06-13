package kr.merutilm.rff.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalProgressBarUI;

import kr.merutilm.rff.io.BitMap;
import kr.merutilm.rff.selectable.Selectable;
import kr.merutilm.rff.settings.*;

import kr.merutilm.rff.struct.DoubleMatrix;
import kr.merutilm.rff.util.AdvancedMath;
import kr.merutilm.rff.util.ConsoleUtils;
import kr.merutilm.rff.util.IOUtilities;
import kr.merutilm.rff.io.RFFMap;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelDoubleArrayDispatcher;
import kr.merutilm.rff.parallel.ParallelRenderState;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

final class RFFVideoWindow extends JFrame{

    private final RFFVideoWindowPanel panel;
    private final JProgressBar bar;
    private transient BufferedImage img;

    private RFFVideoWindow(int imageWidth, int imageHeight){
        super("Preview Video");
        setIconImage(IOUtilities.getApplicationIcon());
        setLayout(new BorderLayout());

        this.panel = new RFFVideoWindowPanel();
        
        this.bar = new JProgressBar();
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
        bar.setMaximum(10000);
        bar.setStringPainted(true);
        bar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));


        add(panel, BorderLayout.CENTER);
        add(bar, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(imageWidth, imageHeight + MUIConstants.UI_HEIGHT));
        RFF.setWindowPanelSize(this, panel, imageWidth, imageHeight);

        setResizable(false);
        getContentPane().setBackground(Color.BLACK);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        
        pack();
        setVisible(true);
    }

    public static void createVideo(Settings settings, File dir, File out) {
                
        DataSettings dataSettings = settings.videoSettings().dataSettings();
        AnimationSettings animationSettings = settings.videoSettings().animationSettings();
        ExportSettings exportSettings = settings.videoSettings().exportSettings();
        double fps = exportSettings.fps();
        float frameInterval = (float) (animationSettings.mps() / fps);
        RFFMap targetMap = RFFMap.readByID(dir, 1);
        if(targetMap == null){
            JOptionPane.showMessageDialog(null, "Cannot create video. There is no samples in the directory : Videos", "Export video", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DoubleMatrix targetMatrix = targetMap.iterations();
        int imgWidth; 
        int imgHeight;
        int multiplier = exportSettings.multiSampling();

        imgWidth = targetMatrix.getWidth() * multiplier;
        imgHeight = targetMatrix.getHeight() * multiplier;
        
        new Thread(() -> {

            try(
                    FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(out, imgWidth, imgHeight);
                    Frame f = new Frame(imgWidth, imgHeight, Frame.DEPTH_BYTE, 4)
            ){

                avutil.av_log_set_level(avutil.AV_LOG_QUIET);
                int maxNumber = IOUtilities.generateFileNameNumber(dir, IOUtilities.Extension.MAP.toString()) - 1;
                double minNumber = -animationSettings.overZoom();
                float currentFrameNumber = maxNumber;
                FFmpegLogCallback.set();
                recorder.setFrameRate(fps);
                recorder.setVideoQuality(0); // maximum quality
                recorder.setFormat("mp4");
                recorder.setVideoBitrate(exportSettings.bitrate());
                recorder.start();
                double currentSec = 0;
                RFFVideoWindow window = new RFFVideoWindow(imgWidth, imgHeight);
                long startMillis = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                RFFGLPanel.LOCKER.lock();
                window.panel.init();
                window.panel.getRenderer().reloadSize(imgWidth, imgHeight);
                RFFGLPanel.LOCKER.unlock();

                while(currentFrameNumber > minNumber) {
                    currentFrameNumber -= frameInterval;
                    currentSec += 1 / fps;
                    RFFMap zoomed;
                    RFFMap normal;
                    if(currentFrameNumber < 1){
                        normal = RFFMap.readByID(dir, 1);
                        zoomed = null;
                    }else{
                        int f1 = (int) currentFrameNumber;
                        int f2 = f1 + 1;
                        zoomed = RFFMap.readByID(dir, f1);
                        normal = RFFMap.readByID(dir, f2);
                    }


                    RFFGLPanel.LOCKER.lock();
                    if(!window.isVisible()){
                        RFFGLPanel.LOCKER.unlock();
                        break;
                    }
                    window.panel.makeCurrent();
                    window.panel.setMap(currentFrameNumber, normal, zoomed);
                    window.panel.applyCurrentMap();
                    window.panel.applyColor(settings);
                    window.panel.getRenderer().setTime((float) currentSec);
                    double zoom = window.panel.calculateZoom(dataSettings.defaultZoomIncrement());
                    window.panel.render();

                    RFFGLPanel.LOCKER.unlock();
                    if(animationSettings.showText()){
                        Graphics2D g = window.panel.getImage().createGraphics();
                        int xg = 20;
                        int yg = 10;
                        int size = imgWidth / 40;
                        int off = size / 15;
                        g.setFont(new Font(Font.SERIF, Font.BOLD, size));
                        String zoomStr = String.format("Zoom : %.6fE%d", Math.pow(10, zoom % 1), (int) zoom);
                        g.setColor(new Color(0,0,0,128));
                        g.drawString(zoomStr, xg + off, size + yg + off);
                        g.setColor(new Color(255,255,255,128));
                        g.drawString(zoomStr, xg, size + yg);

                    }

                    window.setImage(window.panel.getImage());

                    Java2DFrameConverter.copy(window.img, f);
                    recorder.record(f, avutil.AV_PIX_FMT_ABGR);
                    double progressRatio = (maxNumber - currentFrameNumber) / (maxNumber + animationSettings.overZoom());
                    long spent = System.currentTimeMillis() - startMillis;
                    long remained = (long)((1 - progressRatio) / progressRatio * spent);

                    window.panel.repaint();
                    window.bar.setValue((int)(progressRatio * 10000));
                    window.bar.setString(String.format("Processing... %.2f", progressRatio * 100) + "% [" + sdf.format(new Date(remained)) + "]");
                }
                if(window.isVisible()){
                    window.setVisible(false);
                    window.dispose();
                }
                JOptionPane.showMessageDialog(null, "Render Finished");

            }catch(IOException e){
                ConsoleUtils.logError(e);
            }
        }).start();
    }

    private void setImage(BufferedImage img){
        this.img = img;
    }
}