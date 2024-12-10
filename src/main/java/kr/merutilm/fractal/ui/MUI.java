package kr.merutilm.fractal.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

public class MUI {
    private MUI() {

    }

    public static final int UI_HEIGHT = 25;

    public static final Font DEFAULT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    public static final Color BUTTON_BACKGROUND = new Color(110, 110, 110);

    public static final Color PANEL_BACKGROUND = new Color(80, 80, 80);

    public static final Color INPUT_BACKGROUND = new Color(40, 40, 40);

    public static final Color TEXT_COLOR = Color.WHITE;

    public static final Color SELECTED_TEXT_COLOR = Color.BLACK;

    public static final Border PANEL_BORDER = BorderFactory.createLineBorder(Color.BLACK);

    public static final Border BUTTON_BORDER = BorderFactory.createLineBorder(new Color(80,80,80));

    public static final Border INPUT_BORDER = BorderFactory.createEmptyBorder();

    public static final Color UNSAVED_TEXT_COLOR = new Color(255,200,200);
    
    public static final Color SAVED_TEXT_COLOR = new Color(200,225,255);
    
    public static final Color ERROR_TEXT_COLOR = new Color(255,100,100);
    
}
