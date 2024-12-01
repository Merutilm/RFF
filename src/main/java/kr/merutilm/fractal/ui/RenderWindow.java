package kr.merutilm.fractal.ui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import kr.merutilm.customswing.CSFrame;
import kr.merutilm.fractal.RFFUtils;

final class RenderWindow extends CSFrame {

    private final RenderPanel drawPanel;

    public RenderWindow(RFF master, int w, int h) {
        super("Fractal", RFFUtils.getApplicationIcon(), w, h);
        drawPanel = new RenderPanel(master, this);
        drawPanel.setBounds(0, 0, w, h);
        add(drawPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                if (e.getComponent().isShowing()) {
                    Dimension size = e.getComponent().getSize();
                    int x = size.width + X_CORRECTION_FRAME;
                    int y = size.height + Y_CORRECTION_FRAME;

                    drawPanel.setSize(x, y);
                    drawPanel.recompute();

                }
            }
        });

        pack();

    }



    public RenderPanel getPainter() {
        return drawPanel;
    }


}
