package kr.merutilm.fractal.ui;

import java.awt.*;

import kr.merutilm.base.struct.HexColor;
import kr.merutilm.customswing.CSButton;
import kr.merutilm.customswing.CSFrame;
import kr.merutilm.fractal.RFFUtils;
final class StatusWindow extends CSFrame {

    private final CalcSettingsPanel fractalCalc;
    private final ImgSettingsPanel fractalImg;
    public static final int BAR_HEIGHT = CSButton.BUTTON_HEIGHT;
    public static final int W = 560;
    public static final int H = 400;

    public StatusWindow(RFF master) {
        super("Status", RFFUtils.getApplicationIcon(), W , H);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(new HexColor(10,10,10,255).toAWT());
        setResizable(false);
        fractalCalc = new CalcSettingsPanel(master, this, new Rectangle(0, 0, W, BAR_HEIGHT * 6));
        fractalImg = new ImgSettingsPanel(master, this, new Rectangle(0, fractalCalc.getHeight(), W, H - fractalCalc.getHeight()));
        add(fractalCalc);
        add(fractalImg);
        pack();
    }

    public CalcSettingsPanel getFractalCalc() {
        return fractalCalc;
    }

    public ImgSettingsPanel getFractalImg() {
        return fractalImg;
    }

}
