package kr.merutilm.fractal.ui;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import kr.merutilm.fractal.settings.ImageSettings;
import kr.merutilm.fractal.theme.BasicThemes;

enum ActionsImage implements Actions {
    THEME("Set Theme", (master, name) -> new SettingsWindow(name, panel -> {panel.createSelectInput(name, null,
            BasicThemes.tryMatch(master.getTheme()), BasicThemes.values(), e -> {
                master.setTheme(e.getTheme());
                ActionsExplore.REFRESH_COLOR.accept(master);
            });
            panel.setSize(panel.getWidth(), 150);
        })),
    RESOLUTION("Set Resolution", (master, name) -> new SettingsWindow(name, panel -> {

        ImageSettings image = getImageSettings(master);

        Consumer<UnaryOperator<ImageSettings.Builder>> applier = e -> master
                .setSettings(e1 -> e1.edit().setImageSettings(e2 -> e.apply(e2.edit()).build()).build());

        panel.createTextInput(name, null, image.resolutionMultiplier(), Double::parseDouble, e -> {
            applier.accept(t -> t.setResolutionMultiplier(e));
            Actions.getRenderer(master).recompute();
        });

    })),
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
