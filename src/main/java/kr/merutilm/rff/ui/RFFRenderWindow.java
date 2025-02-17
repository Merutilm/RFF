package kr.merutilm.rff.ui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import kr.merutilm.rff.io.IOUtilities;

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
                    drawPanel.recompute();
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

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        drawPanel.recompute();
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
