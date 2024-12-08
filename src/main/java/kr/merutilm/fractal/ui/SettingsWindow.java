package kr.merutilm.fractal.ui;

import java.util.function.Consumer;

import kr.merutilm.customswing.CSFrame;
import kr.merutilm.customswing.CSPanel;
import kr.merutilm.customswing.CSValueInputGroupPanel;
import kr.merutilm.customswing.CSValueInputGroupPanel.InputType;
import kr.merutilm.fractal.RFFUtils;

final class SettingsWindow extends CSFrame{
    public static final int WIDTH = 400;
    public static final int MAX_HEIGHT = 600;

    public SettingsWindow(String name, Consumer<CSValueInputGroupPanel> ui){
        super(name, RFFUtils.getApplicationIcon(), WIDTH, MAX_HEIGHT);
        CSPanel target = new CSPanel(this);
        target.setBounds(0, 0, WIDTH, MAX_HEIGHT);
        CSValueInputGroupPanel panel = new CSValueInputGroupPanel(this, target, "", InputType.VERTICAL, false);
        ui.accept(panel);
        target.add(panel);
        setStrictBounds(0, 0, panel.getWidth(), panel.getHeight());
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        setResizable(false);
        add(target);
        setVisible(true);
    }
    
}
