package kr.merutilm.fractal.ui;

import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import kr.merutilm.base.io.BitMapImage;
import kr.merutilm.fractal.RFFUtils;
import kr.merutilm.fractal.settings.ImageSettings;
import kr.merutilm.fractal.theme.BasicThemes;

enum ActionsImage implements Actions {
    THEME("Set Theme", (master, name) -> new SettingsWindow(name, panel -> {panel.createSelectInput(name,
            BasicThemes.tryMatch(master.getTheme()), BasicThemes.values(), e -> {
                master.setTheme(e.getTheme());
                ActionsExplore.REFRESH_COLOR.accept(master);
            }, true);
            panel.setSize(panel.getWidth(), 150);
        })),
    RESOLUTION("Set Resolution", (master, name) -> new SettingsWindow(name, panel -> {

        ImageSettings image = getImageSettings(master);

        Consumer<UnaryOperator<ImageSettings.Builder>> applier = e -> master
                .setSettings(e1 -> e1.edit().setImageSettings(e2 -> e.apply(e2.edit()).build()).build());

        panel.createTextInput(name, image.resolutionMultiplier(), Double::parseDouble, e -> {
            applier.accept(t -> t.setResolutionMultiplier(e));
            Actions.getRenderer(master).recompute();
        });

    })),
    SAVE_IMAGE("Save Image", (master, name) -> {
        File file = RFFUtils.saveFile(name, "png", "Image");
        try {
            new BitMapImage(Actions.getRenderer(master).getCurrentImage()).export(file);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }),
    ;

    private final String name;
    private final BiConsumer<RFF, String> generator;

    private ActionsImage(String name, BiConsumer<RFF, String> generator) {
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

    private static ImageSettings getImageSettings(RFF master) {
        return master.getSettings().imageSettings();
    }
}
