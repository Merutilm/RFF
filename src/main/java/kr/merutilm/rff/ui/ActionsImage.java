package kr.merutilm.rff.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import javax.swing.KeyStroke;

import kr.merutilm.rff.io.BitMapImage;
import kr.merutilm.rff.settings.ImageSettings;
import kr.merutilm.rff.util.IOUtilities;

enum ActionsImage implements ItemActions {
    
    RESOLUTION("Set Resolution", "Set the resolution of rendered image. It contains \"Recompute\" operation.",  null, 
    (master, name, description, accelerator) ->
    ItemActions.createItem(name, description, accelerator, () -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {

        ImageSettings image = getImageSettings(master);

        Consumer<UnaryOperator<ImageSettings.Builder>> applier = e -> master
                .setSettings(e1 -> e1.setImageSettings(e::apply));

        panel.createTextInput(name, "The resolution multiplier of current window.", image.resolutionMultiplier(), Double::parseDouble, e -> {
            applier.accept(t -> t.setResolutionMultiplier(e));
            ItemActions.getRenderer(master).recompute();
        });

    }))),
    SAVE_IMAGE("Save Image", "Export current rendered image to file", KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), 
    (master, name, description, accelerator) ->
    ItemActions.createItem(name, description, accelerator, () -> {
        File defSave = new File(IOUtilities.getOriginalResource(), IOUtilities.DefaultDirectory.IMAGE.toString());
        File file = IOUtilities.saveFile(name, defSave, IOUtilities.Extension.IMAGE.toString(), "Save Image");
        if(file == null){
            return;
        }
        try {
            new BitMapImage(ItemActions.getRenderer(master).getCurrentImage()).export(file);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
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

    ActionsImage(String name, String description, KeyStroke accelerator, Initializer initializer) {
        this.name = name;
        this.description = description;
        this.accelerator = accelerator;
        this.initializer = initializer;
    }

    @Override
    public String toString() {
        return name;
    }

    private static ImageSettings getImageSettings(RFF master) {
        return master.getSettings().imageSettings();
    }
}
