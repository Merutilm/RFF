package kr.merutilm.rff.ui;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import javax.swing.KeyStroke;

import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.settings.*;
import kr.merutilm.rff.util.IOUtilities;

enum ActionsVideo implements ItemActions {
    DATA("Data", "Open the video data Settings. Used when generating video data files.", null,
            (master, name, description, accelerator) ->
                    ItemActions.createItem(name, description, accelerator, () -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
                        DataSettings data = getVideoSettings(master).dataSettings();

                        Consumer<UnaryOperator<DataSettings.Builder>> applier = e ->
                                master.setSettings(e1 -> e1.setVideoSettings(e2 -> e2.setDataSettings(e)));

                        panel.createTextInput("Default Zoom Increment", "Set the log-Zoom interval between two adjacent video data.", data.defaultZoomIncrement(), Double::parseDouble, e ->
                                applier.accept(f -> f.setDefaultZoomIncrement(e))
                        );
                    }))),

    ANIMATION("Animation", "Open the video animation settings. Used when creating video.", null,
            (master, name, description, accelerator) ->
                    ItemActions.createItem(name, description, accelerator, () -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
                        AnimationSettings animation = getVideoSettings(master).animationSettings();

                        Consumer<UnaryOperator<AnimationSettings.Builder>> applier = e ->
                                master.setSettings(e1 -> e1.setVideoSettings(e2 -> e2.setAnimationSettings(e)));

                        panel.createTextInput("Over Zoom", "Zoom the final video data.", animation.overZoom(), Double::parseDouble, e ->
                                applier.accept(f -> f.setOverZoom(e))
                        );
                        panel.createBoolInput("Show Text", "Show the text on video.", animation.showText(), e ->
                                applier.accept(f -> f.setShowText(e))
                        );
                        panel.createTextInput("MPS", "Map per second, Number of video data used per second in video", animation.mps(), Double::parseDouble, e ->
                                applier.accept(f -> f.setMps(e))
                        );
                    }))),

    EXPORT_SETTINGS("Export Settings", "Open the video export settings. You can set the quality of the video to export.", null,
            (master, name, description, accelerator) ->
                    ItemActions.createItem(name, description, accelerator, () -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
                        ExportSettings export = getVideoSettings(master).exportSettings();

                        Consumer<UnaryOperator<ExportSettings.Builder>> applier = e ->
                                master.setSettings(e1 -> e1.setVideoSettings(e2 -> e2.setExportSettings(e)));

                        panel.createTextInput("FPS", "Frame per second of the video to export", export.fps(), Double::parseDouble, e ->
                                applier.accept(f -> f.setFps(e))
                        );
                        panel.createTextInput("Multi Sampling", "Video frame size multiplier", export.multiSampling(), Integer::parseInt, e ->
                                applier.accept(f -> f.setMultiSampling(e))
                        );
                        panel.createTextInput("Bitrate", "The Bitrate of the video to export", export.bitrate(), Integer::parseInt, e ->
                                applier.accept(f -> f.setBitrate(e))
                        );

                    }))),
    GENERATE_VIDEO_DATA("Generate Video Data", "Generate the video data to directory.", null,
            (master, name, description, accelerator) ->
                    ItemActions.createItem(name, description, accelerator, () -> {
                        File defOpen = new File(IOUtilities.getOriginalResource(), IOUtilities.DefaultDirectory.MAP_AS_VIDEO_DATA.toString());
                        File dir = IOUtilities.openFolder("Folder to Export Samples", defOpen);
                        DataSettings dataSettings = master.getSettings().videoSettings().dataSettings();
                        RFFRenderPanel render = ItemActions.getRenderer(master);
                        if (dir == null) {
                            return;
                        }

                        new Thread(() -> {
                            int id = render.getState().currentID();
                            try {
                                int count = 0;

                                while (master.getSettings().calculationSettings().logZoom() > CalculationSettings.MINIMUM_ZOOM) {
                                    render.getState().tryBreak(id + count++);
                                    render.requestRecompute();
                                    render.waitUntilComputeFinished();
                                    render.getCurrentMap().exportAsVideoData(dir);
                                    master.setSettings(e -> e.setCalculationSettings(
                                            e1 -> e1.zoomOut(Math.log10(dataSettings.defaultZoomIncrement()))));
                                }
                            } catch (IllegalParallelRenderStateException | InterruptedException e) {
                                Thread.currentThread().interrupt();
                                RFFLoggers.logCancelledMessage(name, id);
                            }
                        }).start();

                    })),
    EXPORT_ZOOMING_VIDEO("Export Zooming Video", "Export zooming video using generated video data files.", null,
            (master, name, description, accelerator) ->
                    ItemActions.createItem(name, description, accelerator, () -> {
                        File defOpen = new File(IOUtilities.getOriginalResource(), IOUtilities.DefaultDirectory.MAP_AS_VIDEO_DATA.toString());
                        File selected = IOUtilities.openFolder("Select Sample Folder", defOpen);
                        if (selected == null) {
                            return;
                        }
                        File defSave = new File(IOUtilities.getOriginalResource(), IOUtilities.DefaultDirectory.MAP_AS_VIDEO_DATA.toString());
                        File toSave = IOUtilities.saveFile(name, defSave, "mp4", "video");
                        if (toSave == null) {
                            return;
                        }
                        RFFVideoWindow.createVideo(master.getSettings(), selected, toSave);
                    })),
    ;


    private final String name;
    private final String description;
    private final KeyStroke accelerator;
    private final Initializer initializer;

    @Override
    public KeyStroke keyStroke() {
        return accelerator;
    }

    public String description() {
        return description;
    }

    public Initializer initializer() {
        return initializer;
    }

    ActionsVideo(String name, String description, KeyStroke accelerator, Initializer initializer) {
        this.name = name;
        this.description = description;
        this.accelerator = accelerator;
        this.initializer = initializer;
    }


    @Override
    public String toString() {
        return name;
    }

    private static VideoSettings getVideoSettings(RFF master) {
        return master.getSettings().videoSettings();
    }


}
