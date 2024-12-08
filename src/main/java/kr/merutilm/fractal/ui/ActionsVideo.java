package kr.merutilm.fractal.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import kr.merutilm.base.selectable.Ease;
import kr.merutilm.base.util.ConsoleUtils;
import kr.merutilm.base.util.TaskManager;
import kr.merutilm.fractal.RFFUtils;
import kr.merutilm.fractal.settings.AnimationSettings;
import kr.merutilm.fractal.settings.DataSettings;
import kr.merutilm.fractal.settings.ExportSettings;
import kr.merutilm.fractal.settings.VideoSettings;

enum ActionsVideo implements Actions {
    DATA("Data", (master, name) -> new SettingsWindow(name, panel -> {
        DataSettings data = getVideoSettings(master).dataSettings();
        
        Consumer<UnaryOperator<DataSettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setVideoSettings(e2 -> e2.edit().setDataSettings(e3 -> e.apply(e3.edit()).build()).build()).build());

        panel.createTextInput("Default Zoom Increment", null, data.defaultZoomIncrement(), Double::parseDouble, e -> 
            applier.accept(f -> f.setDefaultZoomIncrement(e))
        );
    })),

    ANIMATE("Animation", (master, name) -> new SettingsWindow(name, panel -> {
        AnimationSettings animation = getVideoSettings(master).animationSettings();
        
        Consumer<UnaryOperator<AnimationSettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setVideoSettings(e2 -> e2.edit().setAnimationSettings(e3 -> e.apply(e3.edit()).build()).build()).build());

        panel.createSelectInput("Animation Ease", null, animation.stripeAnimationEase(), Ease.values(), e -> 
            applier.accept(f -> f.setStripeAnimationEase(e)));
        panel.createTextInput("Animation Speed", null, animation.stripeAnimationSpeed(), Double::parseDouble, e -> 
            applier.accept(f -> f.setStripeAnimationSpeed(e))
        );
    })),

    EXPORT_SETTINGS("Export Settings", (master, name) -> new SettingsWindow(name, panel -> {
        ExportSettings export = getVideoSettings(master).exportSettings();
        
        Consumer<UnaryOperator<ExportSettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setVideoSettings(e2 -> e2.edit().setExportSettings(e3 -> e.apply(e3.edit()).build()).build()).build());

        panel.createTextInput("FPS", null, export.fps(), Double::parseDouble, e -> 
            applier.accept(f -> f.setFps(e))
        );
        panel.createTextInput("MPS", null, export.mps(), Double::parseDouble, e -> 
            applier.accept(f -> f.setMps(e))
        );
        panel.createTextInput("Over Zoom", null, export.overZoom(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setOverZoom(e))
        );
        panel.createTextInput("Multi Sampling", null, export.multiSampling(), Double::parseDouble, e -> 
            applier.accept(f -> f.setMultiSampling(e))
        );
        panel.createTextInput("Bitrate", null, export.bitrate(), Integer::parseInt, e -> 
            applier.accept(f -> f.setBitrate(e))
        );
    })),
    CREATE_VIDEO_DATA("Create Video Data", (master, name) -> {
        File defOpen = new File(RFFUtils.getOriginalResource(), RFFUtils.DefaultDirectory.MAP_AS_VIDEO_DATA.toString());
        File dir = defOpen.isDirectory() ? defOpen : RFFUtils.selectFolder("Folder to Export Samples");
        DataSettings dataSettings = master.getSettings().videoSettings().dataSettings();
        RFFRenderer render = Actions.getRenderer(master);
        if(dir == null){
            return;
        }   
        try{
            if(dir.exists()){
                for (File f : dir.listFiles()) {
                    Files.delete(f.toPath());
                }
            }

            TaskManager.runTask(() -> {

                try{
                    int id = render.getState().getId();

                    while(master.getSettings().calculationSettings().logZoom() > 1 && id == render.getState().getId()){
                        id++;
                        ActionsExplore.RECOMPUTE.accept(master);
                        render.waitUntilRenderEnds();
                        render.getCurrentMap().exportAsVideoData(dir);
                        master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit().zoomOut(Math.log10(dataSettings.defaultZoomIncrement())).build()).build());
                    }

                    dataSettings.export(dir);

                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            });

           

        }catch(IOException e){
            ConsoleUtils.logError(e);
        }
    })
    ;
   

    private final String name;
    private final BiConsumer<RFF, String> generator;

    private ActionsVideo(String name, BiConsumer<RFF, String> generator){
        this.name = name;
        this.generator = generator;
    }


    @Override
    public String toString() {
        return name;
    }

    @Override
    public void accept(RFF master) {
        generator.accept(master, name);
    }

    private static VideoSettings getVideoSettings(RFF master){
        return master.getSettings().videoSettings();
    }




}
