package kr.merutilm.fractal.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
import kr.merutilm.base.util.AdvancedMath;
import kr.merutilm.base.util.ConsoleUtils;
import kr.merutilm.base.util.TaskManager;
import kr.merutilm.customswing.CSButton;
import kr.merutilm.customswing.CSFrame;
import kr.merutilm.customswing.CSPanel;
import kr.merutilm.fractal.RFFUtils;
import kr.merutilm.fractal.io.RFFMap;
import kr.merutilm.fractal.settings.Settings;
import kr.merutilm.fractal.settings.VideoDataSettings;
import kr.merutilm.fractal.settings.VideoExportSettings;

public final class VideoRenderWindow extends CSFrame{

    private static final int VIDEO_PREVIEW_WINDOW_MAX_LEN = 640;
    
    private final CSPanel panel;
    private final JProgressBar bar;
    private transient BufferedImage img;

    private VideoRenderWindow(int imageWidth, int imageHeight){
        super("Preview Video", RFFUtils.getApplicationIcon(), imageWidth, imageHeight + CSButton.BUTTON_HEIGHT);
        RenderState state = new RenderState();
        
        this.panel = new CSPanel(this){
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.drawImage(img, 0, 0, imageWidth, imageHeight, null);
            }
        };
        panel.setBounds(0, 0, imageWidth, imageHeight);
        add(panel);
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
        bar.setBounds(0, imageHeight, imageWidth, CSButton.BUTTON_HEIGHT);
        bar.setMaximum(10000);
        bar.setStringPainted(true);
        bar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        add(bar);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.BLACK);
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                state.createBreakpoint();
            }
        });
        
        setVisible(true);
    }

    public static void createVideo(Settings settings, VideoExportSettings videoSettings, double stripeAnimationSpeed,
            File dir, File out) {
        double fps = videoSettings.fps();
        double frameInterval = videoSettings.mps() / fps;
        VideoDataSettings vds = VideoDataSettings.read(dir);
        
        RenderState state = new RenderState();
        int currentID = state.getId();
        RFFMap targetMap = RFFMap.readByID(dir, 1);
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
                int maxNumber = RFFUtils.generateFileNameNumber(dir, RFFUtils.Extension.MAP.toString()) - 1;
                double minNumber = -videoSettings.overZoom();
                double currentFrameNumber = maxNumber;
                recorder.setFrameRate(fps);
                recorder.setVideoQuality(0); // maximum quality  
                recorder.setFormat("mp4");
                recorder.setVideoBitrate(videoSettings.bitrate());
                recorder.start(); 
                double m = Math.min((double) VIDEO_PREVIEW_WINDOW_MAX_LEN / imgWidth, (double) VIDEO_PREVIEW_WINDOW_MAX_LEN / imgHeight);
                double currentSec = 0;
                int w = (int)(imgWidth * m);
                int h = (int)(imgHeight * m);  
                VideoRenderWindow window = new VideoRenderWindow(w, h);
                long startMillis = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

                while(currentFrameNumber > minNumber) {    
                    currentFrameNumber -= frameInterval;
                    currentSec += 1 / fps;

                    frame = getFrame(state, currentID, dir, currentFrameNumber, vds.defaultZoomIncrement(), multiplier);

                    Settings settingsModified = modifyToImageSettings(frame, settings,
                            stripeAnimationSpeed * currentSec);
                    
                    window.setImage(ShaderProcessor.createImage(state, currentID, frame.iterations(), settingsModified, false));
                    
                    Java2DFrameConverter.copy(window.img, f);
                    recorder.record(f, avutil.AV_PIX_FMT_ABGR);
                    double progressRatio = (maxNumber - currentFrameNumber) / (maxNumber + videoSettings.overZoom());
                    long spent = System.currentTimeMillis() - startMillis;
                    long remained = (long)((1 - progressRatio) / progressRatio * spent);

                    window.panel.repaint();
                    window.bar.setValue((int)(progressRatio * 10000));
                    window.bar.setString("Processing... " + (String.format("%.2f",progressRatio * 100)) + "% [" + sdf.format(new Date(remained)) + "]");
                }
                window.setVisible(false);
                window.dispose();
                JOptionPane.showMessageDialog(null, "Render Finished");
            
            }catch(IOException e){
                ConsoleUtils.logError(e);
            }catch(IllegalRenderStateException e){
                //noop
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        });
        
    }

    private static Settings modifyToImageSettings(RFFMap frame, Settings settings, double stripeOffset) {
        return frame.modifyToMapSettings(settings).edit()
                .setImageSettings(e -> e.edit()
                        .setStripeSettings(e1 -> e1.edit()
                                .setOffset(stripeOffset)
                                .build())
                        .build())
                .build();
    }

    private void setImage(BufferedImage img){
        this.img = img;
    }


    private static RFFMap getFrame(RenderState state, int currentID, File dir, double frame, double defaultZoomIncrement, double multiplier) throws IllegalRenderStateException, InterruptedException{
        
        if (frame < 1) {

            double r = 1 - frame;
            RFFMap im1 = RFFMap.readByID(dir, 1);

            if (im1 == null) {
                return null;
            }

            DoubleMatrix m1 = im1.iterations();

            int w = m1.getWidth();
            int h = m1.getHeight();

            long im = im1.maxIteration();
            
            double z1 = im1.zoom();
            double zc = AdvancedMath.ratioDivide(z1, z1 + Math.log10(defaultZoomIncrement), r);
            double lsr = Math.pow(defaultZoomIncrement, r);
            
            DoubleMatrix result = new DoubleMatrix((int) (w * multiplier), (int) (h * multiplier));

            DoubleArrayDispatcher dispatcher = new DoubleArrayDispatcher(state, currentID, result);
            dispatcher.createRenderer((x, y, xRes, yRes, rx, ry, i, v, t) -> {

                double dx = w * (rx - 0.5);
                double dy = h * (ry - 0.5);
                double x1 = w / 2.0 + dx / lsr;
                double y1 = h / 2.0 + dy / lsr;
                return m1.pipetteAdvanced(x1, y1);
            });
            dispatcher.dispatch();
            return new RFFMap(RFFMap.LATEST, zc, im, result);

        } else {
            int f1 = (int) frame; // it is smaller
            int f2 = f1 + 1; 
            //frame size : f1 = 1x, f2 = 2x
            double r = f2 - frame;
    
            RFFMap im1 = RFFMap.readByID(dir, f1);
            RFFMap im2 = RFFMap.readByID(dir, f2);
    
            if(im1 == null || im2 == null){
                return null;
            }
            DoubleMatrix m1 = im1.iterations();
            DoubleMatrix m2 = im2.iterations();
    
            int w = m1.getWidth();
            int h = m1.getHeight();
    
            long i1 = im1.maxIteration();
            long i2 = im2.maxIteration();
            long im = Math.min(i1, i2);
    
            double z1 = im1.zoom();
            double z2 = im2.zoom();
            double zg = Math.pow(10, z1 - z2);
            double zc = AdvancedMath.ratioDivide(z2, z1, r);
            double ssr = Math.pow(zg, r - 1);
            double lsr = ssr * zg;
    
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
            return new RFFMap(RFFMap.LATEST, zc, im, result);
        }
    }

}
