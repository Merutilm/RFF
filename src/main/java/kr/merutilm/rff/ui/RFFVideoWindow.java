package kr.merutilm.rff.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalProgressBarUI;

import kr.merutilm.rff.settings.*;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import kr.merutilm.rff.struct.DoubleMatrix;
import kr.merutilm.rff.util.AdvancedMath;
import kr.merutilm.rff.util.ConsoleUtils;
import kr.merutilm.rff.util.IOUtilities;
import kr.merutilm.rff.io.RFFMap;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelDoubleArrayDispatcher;
import kr.merutilm.rff.parallel.ParallelRenderState;

final class RFFVideoWindow extends JFrame{

    private static final int VIDEO_PREVIEW_WINDOW_MAX_LEN = 640;
    
    private final RFFVideoWindowPanel panel;
    private final JProgressBar bar;
    private transient BufferedImage img;

    private RFFVideoWindow(RFF master, int imageWidth, int imageHeight){
        super("Preview Video");
        setIconImage(IOUtilities.getApplicationIcon());
        setLayout(new BorderLayout());

        ParallelRenderState state = new ParallelRenderState();

        this.panel = new RFFVideoWindowPanel(master);
        
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

        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                try{
                    state.cancel();
                }catch(InterruptedException f){
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        pack();
        setVisible(true);
    }

    public static void createVideo(Settings settings, File dir, File out) {
                
        DataSettings dataSettings = settings.videoSettings().dataSettings();
        AnimationSettings animationSettings = settings.videoSettings().animationSettings();
        ExportSettings exportSettings = settings.videoSettings().exportSettings();
        double fps = exportSettings.fps();
        double frameInterval = animationSettings.mps() / fps;
        ParallelRenderState state = new ParallelRenderState();
        int currentID = state.currentID();
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

//            try(
//                FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(out, imgWidth, imgHeight);
//                Frame f = new Frame(imgWidth, imgHeight, Frame.DEPTH_BYTE, 4)
//            ){
//                RFFMap frame;
//                avutil.av_log_set_level(avutil.AV_LOG_QUIET);
//                int maxNumber = IOUtilities.generateFileNameNumber(dir, IOUtilities.Extension.MAP.toString()) - 1;
//                double minNumber = -animationSettings.overZoom();
//                double currentFrameNumber = maxNumber;
//                recorder.setFrameRate(fps);
//                recorder.setVideoQuality(0); // maximum quality
//                recorder.setFormat("mp4");
//                recorder.setVideoBitrate(exportSettings.bitrate());
//                recorder.start();
//                double m = Math.min((double) VIDEO_PREVIEW_WINDOW_MAX_LEN / imgWidth, (double) VIDEO_PREVIEW_WINDOW_MAX_LEN / imgHeight);
//                double currentSec = 0;
//                int w = (int)(imgWidth * m);
//                int h = (int)(imgHeight * m);
//                RFFVideoWindow window = new RFFVideoWindow(w, h);
//                RFFRenderPanel renderPanel = ;
//                long startMillis = System.currentTimeMillis();
//                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
//                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//
//                while(currentFrameNumber > minNumber) {
//                    currentFrameNumber -= frameInterval;
//                    currentSec += 1 / fps;
//
//                    frame = getFrame(state, currentID, dir, currentFrameNumber, dataSettings, multiplier);
//
//                    if(frame == null){
//                        break;
//                    }
//                    Settings settingsModified = modifyToVideoSettings(frame, settings, currentSec);
//                    BufferedImage img = RFFRenderPanel.createImage(state, currentID, frame, settingsModified);
//                    if(animationSettings.showText()){
//                        Graphics2D g = img.createGraphics();
//                        int xg = 20;
//                        int yg = 10;
//                        int size = imgWidth / 40;
//                        int off = size / 15;
//                        g.setFont(new Font(Font.SERIF, Font.BOLD, size));
//                        String zoomStr = String.format("Zoom : E%.3f", frame.zoom());
//                        g.setColor(new Color(0,0,0,128));
//                        g.drawString(zoomStr, xg + off, size + yg + off);
//                        g.setColor(new Color(255,255,255,128));
//                        g.drawString(zoomStr, xg, size + yg);
//
//                    }
//                    if(!window.isDisplayable()){
//                        break;
//                    }
//                    window.setImage(img);
//
//                    Java2DFrameConverter.copy(window.img, f);
//                    recorder.record(f, avutil.AV_PIX_FMT_ABGR);
//                    double progressRatio = (maxNumber - currentFrameNumber) / (maxNumber + animationSettings.overZoom());
//                    long spent = System.currentTimeMillis() - startMillis;
//                    long remained = (long)((1 - progressRatio) / progressRatio * spent);
//
//                    window.panel.repaint();
//                    window.bar.setValue((int)(progressRatio * 10000));
//                    window.bar.setString(String.format("Processing... %.2f", progressRatio * 100) + "% [" + sdf.format(new Date(remained)) + "]");
//                }
//                window.setVisible(false);
//                window.dispose();
//                JOptionPane.showMessageDialog(null, "Render Finished");
//
//            }catch(IOException e){
//                ConsoleUtils.logError(e);
//            }catch(IllegalParallelRenderStateException e){
//                //noop
//            }catch(InterruptedException e){
//                Thread.currentThread().interrupt();
//            }
        }).start();
    }

    private static Settings modifyToVideoSettings(RFFMap frame, Settings settings, double currentSec) {
        StripeSettings animation = settings.shaderSettings().stripeSettings();
        double sof = currentSec * animation.stripeAnimationSpeed();

        return frame.modifyToMapSettings(settings).edit()
                .setShaderSettings(e -> e
                        .setStripeSettings(e1 -> e1
                                .setOffset(sof))
                        )
                .build();
    }

    private void setImage(BufferedImage img){
        this.img = img;
    }


    private static RFFMap getFrame(ParallelRenderState state, int currentID, File dir, double frame, DataSettings vds, int multiplier) throws IllegalParallelRenderStateException, InterruptedException{
        
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
            double zc = AdvancedMath.ratioDivide(z1, z1 + Math.log10(vds.defaultZoomIncrement()), r);
            double lsr = Math.pow(vds.defaultZoomIncrement(), r);
            
            DoubleMatrix result = new DoubleMatrix((int) (w * multiplier), (int) (h * multiplier));

            ParallelDoubleArrayDispatcher dispatcher = new ParallelDoubleArrayDispatcher(state, currentID, result);
            dispatcher.createRenderer((_, _, _, _, rx, ry, _, _, _) -> {

                double dx = w * (rx - 0.5);
                double dy = h * (ry - 0.5);
                double x1 = w / 2.0 + dx / lsr;
                double y1 = h / 2.0 + dy / lsr;
                return m1.pipetteAdvanced(x1, y1);
            });
            dispatcher.dispatch();
            return new RFFMap(zc, im1.period(), im, result);

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
            
            ParallelDoubleArrayDispatcher dispatcher = new ParallelDoubleArrayDispatcher(state, currentID, result);
            dispatcher.createRenderer((_, _, _, _, rx, ry, _, _, _) -> {
                
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
            return new RFFMap(zc, im1.period(), im, result);
        }
    }
}