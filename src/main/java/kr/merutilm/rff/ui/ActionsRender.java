package kr.merutilm.rff.ui;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import javax.swing.KeyStroke;

import kr.merutilm.rff.settings.RenderSettings;

enum ActionsRender implements ItemActions {

    RESOLUTION("Set Resolution", "Set the resolution of map. It contains \"Recompute\" operation.", null,
            (master, name, description, accelerator) ->
                    ItemActions.createItem(name, description, accelerator, () -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {

                        RenderSettings image = getRenderSettings(master);

                        Consumer<UnaryOperator<RenderSettings.Builder>> applier = e -> master
                                .setSettings(e1 -> e1.setRenderSettings(e));

                        panel.createTextInput(name, "The resolution multiplier of current window.", image.resolutionMultiplier(), Double::parseDouble, e -> {
                            applier.accept(t -> t.setResolutionMultiplier(e));
                            ItemActions.getRenderer(master).requestRecompute();
                        });

                    }))),
    ANTIALIASING("Use Anti-aliasing", "Set the anti-aliasing option.", null,
            (master, name, description, accelerator) ->
                    ItemActions.createCheckBoxItem(name, description, accelerator, getRenderSettings(master).antialiasing(), b -> {
                        master.setSettings(e -> e.setRenderSettings(e2 -> e2.setAntialiasing(b)));
                        ItemActions.getRenderer(master).requestColor();
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

    ActionsRender(String name, String description, KeyStroke accelerator, Initializer initializer) {
        this.name = name;
        this.description = description;
        this.accelerator = accelerator;
        this.initializer = initializer;
    }

    @Override
    public String toString() {
        return name;
    }

    private static RenderSettings getRenderSettings(RFF master) {
        return master.getSettings().renderSettings();
    }
}
