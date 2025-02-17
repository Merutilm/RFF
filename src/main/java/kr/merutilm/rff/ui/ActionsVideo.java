package kr.merutilm.rff.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import javax.swing.KeyStroke;

import kr.merutilm.rff.shader.IllegalRenderStateException;
import kr.merutilm.rff.selectable.Ease;
import kr.merutilm.rff.util.ConsoleUtils;
import kr.merutilm.rff.io.IOUtilities;
import kr.merutilm.rff.settings.AnimationSettings;
import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.settings.DataSettings;
import kr.merutilm.rff.settings.ExportSettings;
import kr.merutilm.rff.settings.VideoSettings;

enum ActionsVideo implements Actions {
    DATA("Data", "Open the video data Settings. Used when generating video data files.", (master, name) -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        DataSettings data = getVideoSettings(master).dataSettings();
        
        Consumer<UnaryOperator<DataSettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setVideoSettings(e2 -> e2.edit().setDataSettings(e3 -> e.apply(e3.edit()).build()).build()).build());

        panel.createTextInput("Default Zoom Increment", "Set the log-Zoom interval between two adjacent video data.", data.defaultZoomIncrement(), Double::parseDouble, e ->
            applier.accept(f -> f.setDefaultZoomIncrement(e))
        );
    }), null),

    ANIMATION("Animation", "Open the video animation settings. Used when creating video.", (master, name) -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        AnimationSettings animation = getVideoSettings(master).animationSettings();
        
        Consumer<UnaryOperator<AnimationSettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setVideoSettings(e2 -> e2.edit().setAnimationSettings(e3 -> e.apply(e3.edit()).build()).build()).build());

        panel.createTextInput("Over Zoom", "Zoom the final video data.", animation.overZoom(), Double::parseDouble, e ->
        	applier.accept(f -> f.setOverZoom(e))
        );
        panel.createBoolInput("Show Text", "Show the text on video.", animation.showText(), e ->
        	applier.accept(f -> f.setShowText(e))
        );
        panel.createTextInput("MPS", "Map per second, Number of video data used per second in video", animation.mps(), Double::parseDouble, e ->
            applier.accept(f -> f.setMps(e))
        );
        panel.createSelectInput("Animation Ease", "Stripe Animation Ease", animation.stripeAnimationEase(), Ease.values(), e ->
            applier.accept(f -> f.setStripeAnimationEase(e)), true);
        panel.createTextInput("Animation Speed", "Stripe Animation Speed, The stripes' offset(iterations) per second.", animation.stripeAnimationSpeed(), Double::parseDouble, e ->
            applier.accept(f -> f.setStripeAnimationSpeed(e))
        );
    }), null),

    EXPORT_SETTINGS("Export Settings", "Open the video export settings. You can set the quality of the video to export.", (master, name) -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        ExportSettings export = getVideoSettings(master).exportSettings();
        
        Consumer<UnaryOperator<ExportSettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setVideoSettings(e2 -> e2.edit().setExportSettings(e3 -> e.apply(e3.edit()).build()).build()).build());

        panel.createTextInput("FPS", "Frame per second of the video to export", export.fps(), Double::parseDouble, e ->
            applier.accept(f -> f.setFps(e))
        );
        panel.createTextInput("Multi Sampling", "Video frame size multiplier", export.multiSampling(), Double::parseDouble, e ->
            applier.accept(f -> f.setMultiSampling(e))
        );
        panel.createTextInput("Bitrate", "The Bitrate of the video to export", export.bitrate(), Integer::parseInt, e ->
            applier.accept(f -> f.setBitrate(e))
        );

    }), null),
    GENERATE_VIDEO_DATA("Generate Video Data", "Generate the video data to directory.", (master, name) -> {
        File defOpen = new File(IOUtilities.getOriginalResource(), IOUtilities.DefaultDirectory.MAP_AS_VIDEO_DATA.toString());
        File dir = defOpen.isDirectory() ? defOpen : IOUtilities.selectFolder("Folder to Export Samples");
        DataSettings dataSettings = master.getSettings().videoSettings().dataSettings();
        RFFRenderPanel render = Actions.getRenderer(master);
        if(dir == null){
            return;
        }

        try{

            render.getState().createThread(id -> {
                try {
                    if (dir.exists()) {
                        for (File f : Objects.requireNonNull(dir.listFiles())) {
                            Files.delete(f.toPath());
                        }
                    }

                    while (master.getSettings().calculationSettings().logZoom() > CalculationSettings.MINIMUM_ZOOM) {
                        render.compute(id);
                        render.getCurrentMap().exportAsVideoData(dir);
                        master.setSettings(e -> e.edit().setCalculationSettings(
                                e1 -> e1.edit().zoomOut(Math.log10(dataSettings.defaultZoomIncrement())).build())
                                .build());
                    }

                } catch (IOException e) {
                    ConsoleUtils.logError(e);
                } catch (IllegalRenderStateException e) {
                    RFFLoggers.logCancelledMessage(name, id);
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)),
    EXPORT_ZOOMING_VIDEO("Export Zooming Video", "Export zooming video using generated video data files.", (master, name) -> {
        File defOpen = new File(IOUtilities.getOriginalResource(), IOUtilities.DefaultDirectory.MAP_AS_VIDEO_DATA.toString());
            File selected = defOpen.isDirectory() ? defOpen : IOUtilities.selectFolder("Select Sample Folder");
            if(selected == null){
                return;
            }
            File toSave = defOpen.isDirectory() ? new File(defOpen, IOUtilities.DefaultFileName.VIDEO + ".mp4") : IOUtilities.saveFile(name, "mp4", "video");
            if(toSave == null){
                return;
            }
            RFFVideoWindow.createVideo(master.getSettings(), selected, toSave);
    }, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)),
    ;
   

    private final String name;
    private final BiConsumer<RFF, String> action;
    private final KeyStroke keyStroke;
    private final String description;

    @Override
    public KeyStroke keyStroke() {
        return keyStroke;
    }

    public String description() {
        return description;
    }

    ActionsVideo(String name, String description, BiConsumer<RFF, String> generator, KeyStroke keyStroke) {
        this.name = name;
        this.description = description;
        this.action = generator;
        this.keyStroke = keyStroke;
    }


    @Override
    public String toString() {
        return name;
    }

    @Override
    public void accept(RFF master) {
        action.accept(master, name);
    }

    private static VideoSettings getVideoSettings(RFF master){
        return master.getSettings().videoSettings();
    }




}
