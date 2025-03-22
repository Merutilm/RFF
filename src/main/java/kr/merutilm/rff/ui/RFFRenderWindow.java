package kr.merutilm.rff.ui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import kr.merutilm.rff.util.IOUtilities;

final class RFFRenderWindow extends JFrame {

    private final RFFRenderPanel drawPanel;
    private final RFFStatusPanel statusPanel;
    private RFFSettingsWindow currentSettingsWindow;

    public RFFRenderWindow(RFF master, int w, int h) {
        setTitle("RFF");
        setIconImage(IOUtilities.getApplicationIcon());
        setPreferredSize(new Dimension(w, h));
        setLayout(new BorderLayout(0, 0));

        JMenuBar menuBar = new JMenuBar();
        drawPanel = new RFFRenderPanel(master);
        statusPanel = new RFFStatusPanel();
        Arrays.stream(RFFSettingsMenu.values()).map(e -> e.getMenu(master)).forEach(menuBar::add);
        setMinimumSize(new Dimension(100, 100));
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                if (e.getComponent().isShowing()) {
                    SwingUtilities.invokeLater(drawPanel::recompute);
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                super.windowActivated(e);
                if(currentSettingsWindow != null) {
                    currentSettingsWindow.toFront();
                }
            }
        });

        add(menuBar, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.SOUTH);
        add(drawPanel, BorderLayout.CENTER);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setWindowSize(w, h);
        setLocationRelativeTo(null);
        setVisible(true);
        SwingUtilities.invokeLater(drawPanel::recompute);
    }

    public void setWindowSize(int w, int h){
        pack(); //first-packing, it sets the height of statusPanel and menubar, and we will obtain the drawPanel size errors. 
        setPreferredSize(new Dimension(w * 2 - drawPanel.getWidth(), h * 2 - drawPanel.getHeight())); //adjust the panel size to fit the init size
        pack(); //re-packing for resizing window
    }

    public void setCurrentSettingsWindow(RFFSettingsWindow currentSettingsWindow) {
        this.currentSettingsWindow = currentSettingsWindow;
    }

    public RFFStatusPanel getStatusPanel() {
        return statusPanel;
    }


    public RFFRenderPanel getRenderer() {
        return drawPanel;
    }


}
