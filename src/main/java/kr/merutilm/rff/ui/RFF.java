package kr.merutilm.rff.ui;

import kr.merutilm.rff.preset.shader.Shader;
import kr.merutilm.rff.preset.Preset;
import kr.merutilm.rff.preset.Presets;
import kr.merutilm.rff.preset.calc.Calculation;
import kr.merutilm.rff.preset.location.Location;
import kr.merutilm.rff.preset.render.Render;
import kr.merutilm.rff.settings.Settings;

import java.util.function.UnaryOperator;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


/**
 * <h2>Ridiculously Fast Fractal</h2>
 * Check how RFF works : <a href="https://docs.google.com/document/d/1IZMfTcF_-2f3HcX7AS9v8I57P8M1yTJgpzfBPYq9JIg/edit?pli=1&tab=t.jphmxxug3mna#heading=h.j5b0q4ufohul">HERE</a>
 */
final class RFF {

    private final RFFRenderWindow window;

    private static final int INIT_WIDTH = 1280;
    private static final int INIT_HEIGHT = 720;

    private Settings settings = Presets.INIT_SETTINGS;

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
    
    public void setPreset(Preset preset){
        switch (preset) {
            case Calculation p -> {
                setSettings(e -> e.setCalculationSettings(e2 -> e2.setR3ASettings(p.r3aSettings()).setReferenceCompressionSettings(p.referenceCompressionSettings())));
            }
            case Location p -> {
                setSettings(e -> e.setCalculationSettings(e2 -> e2.setCenter(p.createCenter()).setLogZoom(p.logZoom()).setMaxIteration(p.maxIteration())));
            }
            case Render p -> {
                setSettings(e -> e.setImageSettings(p.createImageSettings()));
            }
            case Shader p -> {
                setSettings(e -> e.setShaderSettings(p.createShaderSettings()));
            }
            default -> {
                //noop
            }
        }
    }
    
    public void setSettings(Settings settings) {
        this.settings = settings;
    }
    public void setSettings(UnaryOperator<Settings.Builder> changes) {
        this.settings = changes.apply(settings.edit()).build();
    }
    

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        new RFF();
    }

}
