package kr.merutilm.fractal.ui;

import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;

import kr.merutilm.base.io.BitMapImage;
import kr.merutilm.fractal.RFFUtils;
import kr.merutilm.fractal.io.RFFMap;

enum ActionsFile implements Actions{
    SAVE_IMAGE("Save Image", (master, name) -> {
        File file = RFFUtils.saveFile(name, "png", "Image");
        try {
            new BitMapImage(Actions.getRenderer(master).getCurrentImage()).export(file);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }),
    OPEN_MAP("Open Map", (master, name) -> {
        RFFRenderer renderer = master.getWindow().getRenderer();
        File file = RFFUtils.selectFile(name, RFFUtils.Extension.MAP.toString(), "RFF Map");
        if(file == null){
            return;
        }
        RFFMap map = RFFMap.read(file);
        renderer.setCurrentMap(map);
        ActionsExplore.REFRESH_COLOR.accept(master);
        
    }),
    ;
    
    private final String name;
    private final BiConsumer<RFF, String> action;

    private ActionsFile(String name, BiConsumer<RFF, String> generator) {
        this.name = name;
        this.action = generator;
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
