package kr.merutilm.fractal.ui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import kr.merutilm.fractal.RFFUtils;

final class RFFWindow extends JFrame {

    private final RFFRenderer drawPanel;
    private final StatusPanel statusPanel;

    public RFFWindow(RFF master, int w, int h) {
        setTitle("RFF");
        setIconImage(RFFUtils.getApplicationIcon());
        setPreferredSize(new Dimension(w, h));
        setLayout(new BorderLayout(0, 0));

        JMenuBar menuBar = new JMenuBar();
        drawPanel = new RFFRenderer(master);
        statusPanel = new StatusPanel();
        Arrays.stream(SettingsMenu.values()).map(e -> e.getMenu(master)).forEach(menuBar::add);
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
    }

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }


    public RFFRenderer getRenderer() {
        return drawPanel;
    }


}
