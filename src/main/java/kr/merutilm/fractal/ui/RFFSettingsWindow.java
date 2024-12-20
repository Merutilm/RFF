package kr.merutilm.fractal.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.function.Consumer;

import javax.swing.JFrame;

import kr.merutilm.fractal.io.IOUtilities;

final class RFFSettingsWindow extends JFrame{
    public static final int WIDTH = 400;

    public RFFSettingsWindow(String name, Consumer<MUISettingElementsPanel> ui){
        super(name);
        setIconImage(IOUtilities.getApplicationIcon());
        setLayout(new BorderLayout());
        
        MUISettingElementsPanel panel = new MUISettingElementsPanel();
        ui.accept(panel);
        panel.refresh();
        add(panel);
        setAlwaysOnTop(true);
        setSize(0,0);
        getContentPane().setPreferredSize(new Dimension(WIDTH, MUIConstants.UI_HEIGHT * panel.getComponentCount()));
        setResizable(false);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }
    
}
