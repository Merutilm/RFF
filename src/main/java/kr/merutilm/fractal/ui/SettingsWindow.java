package kr.merutilm.fractal.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.function.Consumer;

import javax.swing.JFrame;
import kr.merutilm.fractal.RFFUtils;

final class SettingsWindow extends JFrame{
    public static final int WIDTH = 400;

    public SettingsWindow(String name, Consumer<MSettingElementsPanel> ui){
        super(name);
        setIconImage(RFFUtils.getApplicationIcon());
        setLayout(new BorderLayout());
        
        MSettingElementsPanel panel = new MSettingElementsPanel();
        ui.accept(panel);
        panel.refresh();
        add(panel);
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        setPreferredSize(new Dimension(WIDTH, MUI.UI_HEIGHT * panel.getComponentCount()));
        setResizable(false);
        pack();
        setVisible(true);
    }
    
}
