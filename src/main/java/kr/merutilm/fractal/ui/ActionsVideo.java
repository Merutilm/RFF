package kr.merutilm.fractal.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import javax.swing.KeyStroke;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.selectable.Ease;
import kr.merutilm.base.util.ConsoleUtils;
import kr.merutilm.fractal.io.IOUtilities;
import kr.merutilm.fractal.settings.AnimationSettings;
import kr.merutilm.fractal.settings.CalculationSettings;
import kr.merutilm.fractal.settings.DataSettings;
import kr.merutilm.fractal.settings.ExportSettings;
import kr.merutilm.fractal.settings.VideoSettings;

enum ActionsVideo implements Actions {
    DATA("Data", (master, name) -> new SettingsWindow(name, panel -> {
        DataSettings data = getVideoSettings(master).dataSettings();
        
        Consumer<UnaryOperator<DataSettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setVideoSettings(e2 -> e2.edit().setDataSettings(e3 -> e.apply(e3.edit()).build()).build()).build());

        panel.createTextInput("Default Zoom Increment", data.defaultZoomIncrement(), Double::parseDouble, e -> 
            applier.accept(f -> f.setDefaultZoomIncrement(e))
        );
    }), null),

    ANIMATION("Animation", (master, name) -> new SettingsWindow(name, panel -> {
        AnimationSettings animation = getVideoSettings(master).animationSettings();
        
        Consumer<UnaryOperator<AnimationSettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setVideoSettings(e2 -> e2.edit().setAnimationSettings(e3 -> e.apply(e3.edit()).build()).build()).build());

        panel.createTextInput("Over Zoom", animation.overZoom(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setOverZoom(e))
        );
        panel.createBoolInput("Show Text", animation.showText(), e -> 
        	applier.accept(f -> f.setShowText(e))
        );
        panel.createSelectInput("Animation Ease", animation.stripeAnimationEase(), Ease.values(), e -> 
            applier.accept(f -> f.setStripeAnimationEase(e)), true);
        panel.createTextInput("Animation Speed", animation.stripeAnimationSpeed(), Double::parseDouble, e -> 
            applier.accept(f -> f.setStripeAnimationSpeed(e))
        );
    }), null),

    EXPORT_SETTINGS("Export Settings", (master, name) -> new SettingsWindow(name, panel -> {
        ExportSettings export = getVideoSettings(master).exportSettings();
        
        Consumer<UnaryOperator<ExportSettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setVideoSettings(e2 -> e2.edit().setExportSettings(e3 -> e.apply(e3.edit()).build()).build()).build());

        panel.createTextInput("FPS",export.fps(), Double::parseDouble, e -> 
            applier.accept(f -> f.setFps(e))
        );
        panel.createTextInput("MPS", export.mps(), Double::parseDouble, e -> 
            applier.accept(f -> f.setMps(e))
        );
        panel.createTextInput("Multi Sampling", export.multiSampling(), Double::parseDouble, e -> 
            applier.accept(f -> f.setMultiSampling(e))
        );
        panel.createTextInput("Bitrate", export.bitrate(), Integer::parseInt, e -> 
            applier.accept(f -> f.setBitrate(e))
        );
    }), null),
    CREATE_VIDEO_DATA("Create Video Data", (master, name) -> {
        File defOpen = new File(IOUtilities.getOriginalResource(), IOUtilities.DefaultDirectory.MAP_AS_VIDEO_DATA.toString());
        File dir = defOpen.isDirectory() ? defOpen : IOUtilities.selectFolder("Folder to Export Samples");
        DataSettings dataSettings = master.getSettings().videoSettings().dataSettings();
        RFFRenderer render = Actions.getRenderer(master);
        if(dir == null){
            return;
        }

        try{

            render.getState().createThread(id -> {
                try {
                    if (dir.exists()) {
                        for (File f : dir.listFiles()) {
                            Files.delete(f.toPath());
                        }
                    }

                    while (master.getSettings().calculationSettings().logZoom() > CalculationSettings.MININUM_ZOOM) {
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
    EXPORT_ZOOMING_VIDEO("Export Zooming Video", (master, name) -> {
        File defOpen = new File(IOUtilities.getOriginalResource(), IOUtilities.DefaultDirectory.MAP_AS_VIDEO_DATA.toString());
            File selected = defOpen.isDirectory() ? defOpen : IOUtilities.selectFolder("Select Sample Folder");
            if(selected == null){
                return;
            }
            File toSave = defOpen.isDirectory() ? new File(defOpen, IOUtilities.DefaultFileName.VIDEO + ".mp4") : IOUtilities.saveFile(name, "mp4", "video");
            if(toSave == null){
                return;
            }
            VideoRenderWindow.createVideo(master.getSettings(), selected, toSave);
    }, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)),
    ;
   

    private final String name;
    private final BiConsumer<RFF, String> action;
    private final KeyStroke keyStroke;

    @Override
    public KeyStroke keyStroke() {
        return keyStroke;
    }

    private ActionsVideo(String name, BiConsumer<RFF, String> generator, KeyStroke keyStroke) {
        this.name = name;
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
