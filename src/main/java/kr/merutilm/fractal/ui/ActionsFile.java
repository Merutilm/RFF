package kr.merutilm.fractal.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.function.BiConsumer;

import javax.swing.KeyStroke;

import kr.merutilm.fractal.io.IOUtilities;
import kr.merutilm.fractal.io.RFFMap;

enum ActionsFile implements Actions{
    
    OPEN_MAP("Open Map", (master, name) -> {
        RFFRenderer renderer = master.getWindow().getRenderer();
        File file = IOUtilities.selectFile(name, IOUtilities.Extension.MAP.toString(), "RFF Map");
        if(file == null){
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

    @Override
    public KeyStroke keyStroke() {
        return keyStroke;
    }

    private ActionsFile(String name, BiConsumer<RFF, String> generator, KeyStroke keyStroke) {
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
}
