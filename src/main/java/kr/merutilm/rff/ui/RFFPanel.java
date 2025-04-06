package kr.merutilm.rff.ui;

import javax.swing.*;
import java.awt.*;

class RFFPanel extends JPanel {


    RFFPanel(){
        super();
    }

    RFFPanel(LayoutManager layout){
        super(layout);
    }

    public static int toRealLength(int k){
        return (int) (k * RFFRenderWindow.SCALE_MULTIPLIER);
    }
    public static int toLogicalLength(int k){
        return (int) (k / RFFRenderWindow.SCALE_MULTIPLIER);
    }
    public static Dimension toRealResolution(Dimension d){
        int width = toRealLength(d.width);
        int height = toRealLength(d.height);
        return new Dimension(width, height);
    }
    
    public static Dimension toLogicalResolution(Dimension d){
        int width = toLogicalLength(d.width);
        int height = toLogicalLength(d.height);
        return new Dimension(width, height);
    }
}
