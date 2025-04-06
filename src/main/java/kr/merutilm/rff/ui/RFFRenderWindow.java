package kr.merutilm.rff.ui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.util.*;

import javax.swing.*;

import kr.merutilm.rff.util.IOUtilities;


final class RFFRenderWindow extends JFrame {

    private final RFFStatusPanel statusPanel;
    private final RFFRenderPanel renderPanel;
    private RFFSettingsWindow currentSettingsWindow;
    public static final double SCALE_MULTIPLIER;
    static {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration config = device.getDefaultConfiguration();
        AffineTransform transform = config.getDefaultTransform();
        SCALE_MULTIPLIER = transform.getScaleX();
    }

    public RFFRenderWindow(RFF master, int w, int h) {
        setTitle("RFF");
        setIconImage(IOUtilities.getApplicationIcon());

        setLayout(new BorderLayout(0, 0));

        RFFMenuBar menuBar = new RFFMenuBar();

        renderPanel = new RFFRenderPanel(master);
        statusPanel = new RFFStatusPanel();
        Arrays.stream(RFFSettingsMenu.values()).map(e -> e.getMenu(master)).forEach(menuBar::add);
        setMinimumSize(new Dimension(300, 300));
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                if (e.getComponent().isShowing()) {
                    renderPanel.requestResize();
                    renderPanel.requestRecompute();
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
        add(renderPanel, BorderLayout.CENTER);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        RFF.setWindowPanelSize(this, renderPanel, w, h);

        setLocationRelativeTo(null);
        setVisible(true);

        renderPanel.renderLoop();
    }


    public void setCurrentSettingsWindow(RFFSettingsWindow currentSettingsWindow) {
        this.currentSettingsWindow = currentSettingsWindow;
    }

    public RFFStatusPanel getStatusPanel() {
        return statusPanel;
    }


    public RFFRenderPanel getRenderer() {
        return renderPanel;
    }

}
