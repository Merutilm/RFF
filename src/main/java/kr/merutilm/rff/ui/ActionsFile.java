package kr.merutilm.rff.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.KeyStroke;

import kr.merutilm.rff.io.RFFMap;
import kr.merutilm.rff.util.IOUtilities;

enum ActionsFile implements ItemActions {

    OPEN_MAP("Open Map", "Open RFF Map file(" + IOUtilities.Extension.MAP + ")", KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK), 
    (master, name, description, accelerator) ->
    ItemActions.createItem(name, description, accelerator, () -> {
        RFFRenderPanel renderer = master.getWindow().getRenderer();
        File defOpen = new File(IOUtilities.getOriginalResource(), IOUtilities.DefaultDirectory.MAP.toString());
        File file = IOUtilities.openFile(name, defOpen, IOUtilities.Extension.MAP.toString(), "RFF Map");
        if (file == null) {
            return;
        }
        RFFMap map = RFFMap.read(file);
        renderer.setCurrentMap(map);
        master.getWindow().getRenderer().refreshColor();

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

    ActionsFile(String name, String description, KeyStroke accelerator, Initializer initializer) {
        this.name = name;
        this.description = description;
        this.accelerator = accelerator;
        this.initializer = initializer;
    }

    @Override
    public String toString() {
        return name;
    }

}
