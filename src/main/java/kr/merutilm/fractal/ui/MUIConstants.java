package kr.merutilm.fractal.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

final class MUIConstants {
    private MUIConstants() {

    }

    public static final int UI_HEIGHT = 30;
    public static final int UI_SELECTION_HEIGHT = 25;

    public static final int SCROLL_BAR_WIDTH = 10;

    public static final int SCROLL_BAR_HEIGHT = 30;

    public static final Font DEFAULT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    public static final Color BUTTON_BACKGROUND = new Color(120, 120, 120);

    public static final Color SELECTED_BUTTON_BACKGROUND = new Color(130, 170, 170);
    
    public static final Color PANEL_BACKGROUND = new Color(80, 80, 80);

    public static final Color INPUT_BACKGROUND = new Color(40, 40, 40);
    
    public static final Color TEXT_COLOR = Color.WHITE;
    
    public static final Color SELECTED_TEXT_COLOR = Color.BLACK;

    public static final Border PANEL_BORDER = new LineBorder(Color.BLACK);

    public static final Border BUTTON_BORDER = new LineBorder(new Color(80, 80, 80));

    public static final Border INPUT_BORDER = BorderFactory.createEmptyBorder();

    public static final Color UNSAVED_TEXT_COLOR = new Color(255,200,200);
    
    public static final Color SAVED_TEXT_COLOR = new Color(200,225,255);
    
    public static final Color ERROR_TEXT_COLOR = new Color(255,100,100);
    
}
