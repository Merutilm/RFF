package kr.merutilm.rff.ui;

import kr.merutilm.rff.preset.resolution.Resolution;
import kr.merutilm.rff.preset.Preset;
import kr.merutilm.rff.preset.Presets;
import kr.merutilm.rff.preset.calc.Calculation;
import kr.merutilm.rff.preset.location.Location;
import kr.merutilm.rff.preset.render.Render;
import kr.merutilm.rff.preset.shader.ShaderPreset;
import kr.merutilm.rff.settings.Settings;

import java.awt.*;
import java.util.function.UnaryOperator;

import javax.swing.*;


/**
 * <h2>Ridiculously Fast Fractal</h2>
 * Check how RFF works : <a href="https://docs.google.com/document/d/1IZMfTcF_-2f3HcX7AS9v8I57P8M1yTJgpzfBPYq9JIg/edit?pli=1&tab=t.jphmxxug3mna#heading=h.j5b0q4ufohul">HERE</a>
 */
final class RFF {

    private final RFFRenderWindow window;

    private static final int INIT_WIDTH = 1280;
    private static final int INIT_HEIGHT = 720;

    private Settings settings = Presets.INIT_SETTINGS;

    public RFF() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        this.window = new RFFRenderWindow(this, INIT_WIDTH, INIT_HEIGHT);
    }

    public RFFRenderWindow getWindow() {
        return window;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setPreset(Preset preset) {
        switch (preset) {
            case Calculation p ->
                    setSettings(e -> e.setCalculationSettings(e2 -> e2.setR3ASettings(p.mpaSettings()).setReferenceCompressionSettings(p.referenceCompressionSettings())));
            case Location p ->
                    setSettings(e -> e.setCalculationSettings(e2 -> e2.setCenter(p.createCenter()).setLogZoom(p.logZoom()).setMaxIteration(p.maxIteration())));
            case Render p -> setSettings(e -> e.setRenderSettings(p.createImageSettings()));
            case ShaderPreset.Palette p -> setSettings(e -> e.setShaderSettings(e2 -> e2.setPaletteSettings(p.paletteSettings())));
            case ShaderPreset.Stripe p -> setSettings(e -> e.setShaderSettings(e2 -> e2.setStripeSettings(p.stripeSettings())));
            case ShaderPreset.Slope p -> setSettings(e -> e.setShaderSettings(e2 -> e2.setSlopeSettings(p.slopeSettings())));
            case ShaderPreset.Color p -> setSettings(e -> e.setShaderSettings(e2 -> e2.setColorSettings(p.colorSettings())));
            case ShaderPreset.Fog p -> setSettings(e -> e.setShaderSettings(e2 -> e2.setFogSettings(p.fogSettings())));
            case ShaderPreset.Bloom p -> setSettings(e -> e.setShaderSettings(e2 -> e2.setBloomSettings(p.bloomSettings())));
            case Resolution p ->
                    setWindowPanelSize(window, window.getRenderer(), p.getResolution().width, p.getResolution().height);
            default -> {
                //noop
            }
        }
    }

    public static void setWindowPanelSize(JFrame frame, Component targetPanel, int w, int h) {
        frame.setPreferredSize(new Dimension(RFFPanel.toLogicalLength(w), RFFPanel.toLogicalLength(h)));
        frame.pack(); //first-packing, it sets the height of statusPanel and menubar, and we will obtain the drawPanel size errors.

        frame.setPreferredSize(new Dimension(RFFPanel.toLogicalLength(2 * w) - targetPanel.getWidth(), RFFPanel.toLogicalLength(2 * h) - targetPanel.getHeight())); //adjust the panel size to fit the init size
        frame.pack(); //re-packing for resizing window


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
