package kr.merutilm.rff.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.function.BiConsumer;

import javax.swing.KeyStroke;

import kr.merutilm.rff.io.IOUtilities;
import kr.merutilm.rff.io.RFFMap;

enum ActionsFile implements Actions {

    OPEN_MAP("Open Map", "Open RFF Map file(" + IOUtilities.Extension.MAP + ")", (master, name) -> {
        RFFRenderPanel renderer = master.getWindow().getRenderer();
        File file = IOUtilities.selectFile(name, IOUtilities.Extension.MAP.toString(), "RFF Map");
        if (file == null) {
            return;
        }
        RFFMap map = RFFMap.read(file);
        renderer.setCurrentMap(map);
        ActionsExplore.REFRESH_COLOR.accept(master);

    }, KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK)),
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

    ActionsFile(String name, String description, BiConsumer<RFF, String> generator, KeyStroke keyStroke) {
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
}
