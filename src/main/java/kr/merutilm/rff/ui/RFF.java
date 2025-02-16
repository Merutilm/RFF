package kr.merutilm.rff.ui;

import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.settings.ImageSettings;
import kr.merutilm.rff.settings.Settings;
import kr.merutilm.rff.theme.BasicTheme;
import kr.merutilm.rff.theme.Theme;

import java.util.function.UnaryOperator;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


/**
 * <h2>Ridiculously Fast Fractal</h2>
 * Check how RFF works : <a href="https://docs.google.com/document/d/1IZMfTcF_-2f3HcX7AS9v8I57P8M1yTJgpzfBPYq9JIg/edit?pli=1&tab=t.jphmxxug3mna#heading=h.j5b0q4ufohul">...</a>
 */
final class RFF {

    private final RFFRenderWindow window;

    private static final int INIT_WIDTH = 1294;
    private static final int INIT_HEIGHT = 803;

    private Theme theme = BasicTheme.DEFAULT_THEME.getTheme();
    private Settings settings = theme.generate();

    public RFF() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        this.window = new RFFRenderWindow(this, INIT_WIDTH, INIT_HEIGHT);
    }

    public RFFRenderWindow getWindow() {
        return window;
    }

    public Settings getSettings() {
        return settings;
    }
    

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
        
        if(theme instanceof BasicTheme bt){
            Settings s = bt.generate();
            CalculationSettings calc = settings.calculationSettings();
            ImageSettings img = settings.imageSettings();
            
            this.settings = s.edit()
                .setCalculationSettings(calc)
                .setImageSettings(e1 -> e1.edit()
                    .setResolutionMultiplier(img.resolutionMultiplier())
                    .build())
                .build();
        }else{
            this.settings = theme.generate();
        }
    }

    public void setSettings(UnaryOperator<Settings> changes) {
        this.settings = changes.apply(settings);
    }
    

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        new RFF();
    }

}
