package kr.merutilm.rff.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.KeyStroke;

import kr.merutilm.rff.io.BitMap;
import kr.merutilm.rff.io.RFFMap;
import kr.merutilm.rff.util.IOUtilities;

enum ActionsFile implements ItemActions {

    OPEN_MAP("Open Map", "Open RFF Map file(" + IOUtilities.Extension.MAP + ")", KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK),
    (master, name, description, accelerator) ->
    ItemActions.createItem(name, description, accelerator, () -> {
        RFFRenderPanel renderer = master.getWindow().getRenderer();
        File defOpen = new File(IOUtilities.getOriginalResource(), IOUtilities.DefaultDirectory.MAP.toString());
        File file = IOUtilities.openFile(name, defOpen, IOUtilities.Extension.MAP.toString(), "RFF Map");
        if (file == null) {
            return;
        }
        RFFMap map = RFFMap.read(file);
        renderer.requestOpenMap(map);
        master.getWindow().getStatusPanel().setPeriodText(map.period(), 0, 0);
        master.getWindow().getStatusPanel().setZoomText(map.zoom());
        master.getWindow().getRenderer().requestColor();

    })),
    SAVE_MAP("Save Map", "Save RFF Map file(" + IOUtilities.Extension.MAP + ")", KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK),
            (master, name, description, accelerator) ->
                    ItemActions.createItem(name, description, accelerator, () -> {
                        RFFRenderPanel renderer = master.getWindow().getRenderer();
                        File defSave = new File(IOUtilities.getOriginalResource(), IOUtilities.DefaultDirectory.MAP.toString());
                        File file = IOUtilities.saveFile(name, defSave, IOUtilities.Extension.MAP.toString(), "RFF Map");
                        if (file == null) {
                            return;
                        }
                        renderer.getCurrentMap().export(file);
                    })),
    SAVE_IMAGE("Save Image", "Export current rendered image to file", KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
            (master, name, description, accelerator) ->
                    ItemActions.createItem(name, description, accelerator, () -> {
                        File defSave = new File(IOUtilities.getOriginalResource(), IOUtilities.DefaultDirectory.IMAGE.toString());
                        File file = IOUtilities.saveFile(name, defSave, IOUtilities.Extension.IMAGE.toString(), name);
                        if(file == null){
                            return;
                        }
                        new Thread(() -> {
                            try {
                                RFFRenderPanel render = ItemActions.getRenderer(master);
                                render.requestCreateImage();
                                render.waitUntilCreateImage();
                                BitMap.export(render.getCurrentImage(), file);
                            } catch (IOException e) {
                                throw new IllegalStateException();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    })),
    ;
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
