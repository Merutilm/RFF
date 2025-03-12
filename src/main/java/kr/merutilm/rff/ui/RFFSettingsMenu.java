package kr.merutilm.rff.ui;

import java.util.Arrays;
import java.util.function.Function;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

enum RFFSettingsMenu {
    FILE(master -> {
        JMenu menu = Actions.createJMenu("File");
        addAll(master, menu, ActionsFile.values());
        return menu;
    }),
    FRACTAL(master -> {
        JMenu menu = Actions.createJMenu("Fractal");
        addAll(master, menu, ActionsFractal.values());
        return menu;
    }),
    IMAGE(master -> {
        JMenu menu = Actions.createJMenu("Image");
        addAll(master, menu, ActionsImage.values());
        return menu;
    }),
    SHADER(master -> {
        JMenu menu = Actions.createJMenu("Shader");
        addAll(master, menu, ActionsShader.values());
        return menu;
    }),
    VIDEO(master -> {
        JMenu menu = Actions.createJMenu("Video");
        addAll(master, menu, ActionsVideo.values());
        return menu;
    }),
    EXPLORE(master -> {
        JMenu menu = Actions.createJMenu("Explore");
        addAll(master, menu, ActionsExplore.values());
        return menu;
    }),
    ;

    private final Function<RFF, JMenu> function;

    public static <T extends Actions> void addAll(RFF master, JMenu menu, T[] items){
        Arrays.stream(items).forEach(e -> {
            JMenuItem item = e.initializer().init(master, e.toString(), e.description(), e.keyStroke());
            Actions.addItem(menu, item);
        });
    }

    public JMenu getMenu(RFF master) {
        return function.apply(master);
    }


    RFFSettingsMenu(Function<RFF, JMenu> getter){
        this.function = getter;
    }
}
