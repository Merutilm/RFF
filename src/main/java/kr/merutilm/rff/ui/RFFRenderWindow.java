package kr.merutilm.rff.ui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.util.*;

import javax.swing.JFrame;
import kr.merutilm.rff.util.IOUtilities;
import org.lwjgl.opengl.awt.GLData;


final class RFFRenderWindow extends JFrame {

    private final RFFStatusPanel statusPanel;
    private final RFFGLRenderPanel glRenderPanel;
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
        GLData data = new GLData();
        data.majorVersion = 3;
        data.minorVersion = 3;
        data.profile = GLData.Profile.CORE;
        data.samples = 4;
        glRenderPanel = new RFFGLRenderPanel(master, data);
        statusPanel = new RFFStatusPanel();
        Arrays.stream(RFFSettingsMenu.values()).map(e -> e.getMenu(master)).forEach(menuBar::add);
        setMinimumSize(new Dimension(300, 300));
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                if (e.getComponent().isShowing()) {
                    glRenderPanel.requestResize();
                    glRenderPanel.requestRecompute();
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
        add(glRenderPanel, BorderLayout.CENTER);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setWindowSize(w, h);

        setLocationRelativeTo(null);
        setVisible(true);

        glRenderPanel.renderLoop();
    }

    public void setWindowSize(int w, int h){
        setPreferredSize(new Dimension(RFFPanel.toLogicalLength(w), RFFPanel.toLogicalLength(h)));
        pack(); //first-packing, it sets the height of statusPanel and menubar, and we will obtain the drawPanel size errors. 
        setPreferredSize(new Dimension(RFFPanel.toLogicalLength(2 * w) - glRenderPanel.getWidth(), RFFPanel.toLogicalLength(2 * h) - glRenderPanel.getHeight())); //adjust the panel size to fit the init size
        pack(); //re-packing for resizing window

    }

    public void setCurrentSettingsWindow(RFFSettingsWindow currentSettingsWindow) {
        this.currentSettingsWindow = currentSettingsWindow;
    }

    public RFFStatusPanel getStatusPanel() {
        return statusPanel;
    }


    public RFFGLRenderPanel getRenderer() {
        return glRenderPanel;
    }

}
