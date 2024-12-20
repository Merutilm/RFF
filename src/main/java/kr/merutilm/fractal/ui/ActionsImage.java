package kr.merutilm.fractal.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import javax.swing.KeyStroke;

import kr.merutilm.base.io.BitMapImage;
import kr.merutilm.fractal.io.IOUtilities;
import kr.merutilm.fractal.settings.ImageSettings;
import kr.merutilm.fractal.theme.BasicThemes;

enum ActionsImage implements Actions {
    THEME("Set Theme", (master, name) -> new RFFSettingsWindow(name, panel -> {panel.createSelectInput(name,
            BasicThemes.tryMatch(master.getTheme()), BasicThemes.values(), e -> {
                master.setTheme(e.getTheme());
                ActionsExplore.REFRESH_COLOR.accept(master);
            }, true);
            panel.setSize(panel.getWidth(), 150);
        }), KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK)),
    RESOLUTION("Set Resolution", (master, name) -> new RFFSettingsWindow(name, panel -> {

        ImageSettings image = getImageSettings(master);

        Consumer<UnaryOperator<ImageSettings.Builder>> applier = e -> master
                .setSettings(e1 -> e1.edit().setImageSettings(e2 -> e.apply(e2.edit()).build()).build());

        panel.createTextInput(name, image.resolutionMultiplier(), Double::parseDouble, e -> {
            applier.accept(t -> t.setResolutionMultiplier(e));
            Actions.getRenderer(master).recompute();
        });

    }), null),
    SAVE_IMAGE("Save Image", (master, name) -> {
        File file = IOUtilities.saveFile(name, "png", "Image");
        if(file == null){
            return;
        }
        try {
            new BitMapImage(Actions.getRenderer(master).getCurrentImage()).export(file);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)),
    ;

    private final String name;
    private final BiConsumer<RFF, String> action;
    private final KeyStroke keyStroke;

    @Override
    public KeyStroke keyStroke() {
        return keyStroke;
    }

    private ActionsImage(String name, BiConsumer<RFF, String> generator, KeyStroke keyStroke) {
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

    private static ImageSettings getImageSettings(RFF master) {
        return master.getSettings().imageSettings();
    }
}
