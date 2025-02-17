package kr.merutilm.rff.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JFrame;

import kr.merutilm.rff.io.IOUtilities;

final class RFFSettingsWindow extends JFrame{
    public static final int WIDTH = 400;

    public RFFSettingsWindow(RFFRenderWindow window, String name, BiConsumer<RFFSettingsWindow, MUISettingElementsPanel> ui){
        super(name);
        window.setCurrentSettingsWindow(this);
        window.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                window.setCurrentSettingsWindow(null);
            }
        });
        setIconImage(IOUtilities.getApplicationIcon());
        setLayout(new BorderLayout());

        MUISettingElementsPanel panel = new MUISettingElementsPanel();
        ui.accept(this, panel);
        panel.refresh();
        add(panel);
        setSize(0,0);
        getContentPane().setPreferredSize(new Dimension(WIDTH, MUIConstants.UI_HEIGHT * panel.getComponentCount()));
        setResizable(false);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }
    
}
