package kr.merutilm.fractal.ui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import kr.merutilm.fractal.io.IOUtilities;

final class RFFRenderWindow extends JFrame {

    private final RFFRenderPanel drawPanel;
    private final RFFStatusPanel statusPanel;
    
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

        add(menuBar, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.SOUTH);
        add(drawPanel, BorderLayout.CENTER);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        drawPanel.recompute();
    }

    public RFFStatusPanel getStatusPanel() {
        return statusPanel;
    }


    public RFFRenderPanel getRenderer() {
        return drawPanel;
    }


}
