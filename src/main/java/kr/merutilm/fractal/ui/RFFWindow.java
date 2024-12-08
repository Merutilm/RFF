package kr.merutilm.fractal.ui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;

import javax.swing.JMenuBar;

import kr.merutilm.customswing.CSButton;
import kr.merutilm.customswing.CSFrame;
import kr.merutilm.fractal.RFFUtils;

final class RFFWindow extends CSFrame {

    private final RFFRenderer drawPanel;
    private final StatusPanel statusPanel;

    public RFFWindow(RFF master, int w, int h) {
        super("RFF", RFFUtils.getApplicationIcon(), w, h);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBounds(0, 0, w, CSButton.BUTTON_HEIGHT);
        Arrays.stream(SettingsMenu.values()).map(e -> e.getMenu(master)).forEach(menuBar::add);

        drawPanel = new RFFRenderer(master, this);
        drawPanel.setBounds(0, CSButton.BUTTON_HEIGHT, w, h - CSButton.BUTTON_HEIGHT * 2);
        
        statusPanel = new StatusPanel(this);
        statusPanel.setBounds(0, h - CSButton.BUTTON_HEIGHT, w, CSButton.BUTTON_HEIGHT);
        
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                if (e.getComponent().isShowing()) {
                    Dimension size = e.getComponent().getSize();
                    int x = size.width - X_CORRECTION_FRAME;
                    int y = size.height - Y_CORRECTION_FRAME;

                    drawPanel.setBounds(0, CSButton.BUTTON_HEIGHT, x, y - CSButton.BUTTON_HEIGHT * 2);
                    statusPanel.setBounds(0, y - CSButton.BUTTON_HEIGHT, x, CSButton.BUTTON_HEIGHT);
                    drawPanel.recompute();

                }
            }
        });

        add(menuBar);
        add(drawPanel);
        add(statusPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
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
