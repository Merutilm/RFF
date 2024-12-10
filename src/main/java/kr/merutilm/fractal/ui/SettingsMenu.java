package kr.merutilm.fractal.ui;

import java.util.Arrays;
import java.util.function.Function;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public enum SettingsMenu {
    FILE(master -> {
        JMenu menu = new JMenu("File");
        addAll(master, menu, ActionsFile.values());
        return menu;
    }),
    FRACTAL(master -> {
        JMenu menu = new JMenu("Fractal");
        addAll(master, menu, ActionsFractal.values());
        return menu;
    }),
    IMAGE(master -> {
        JMenu menu = new JMenu("Image");
        addAll(master, menu, ActionsImage.values());
        return menu;
    }),
    SHADER(master -> {
        JMenu menu = new JMenu("Shader");
        addAll(master, menu, ActionsShader.values());
        return menu;
    }),
    VIDEO(master -> {
        JMenu menu = new JMenu("Video");
        addAll(master, menu, ActionsVideo.values());
        return menu;
    }),
    EXPLORE(master -> {
        JMenu menu = new JMenu("Explore");
        addAll(master, menu, ActionsExplore.values());
        return menu;
    }),
    ;

    private final Function<RFF, JMenu> function;

    public static <T extends Actions> void addAll(RFF master, JMenu menu, T[] items){
        Arrays.stream(items).forEach(e -> {
            JMenuItem item = new JMenuItem(e.toString());
            item.addActionListener(k -> e.accept(master));
            menu.add(item);
            menu.setFont(MUI.DEFAULT_FONT);
            item.setFont(MUI.DEFAULT_FONT);
        });
    }

    public JMenu getMenu(RFF master) {
        return function.apply(master);
    }


    private SettingsMenu(Function<RFF, JMenu> getter){
        this.function = getter;
    }
}
