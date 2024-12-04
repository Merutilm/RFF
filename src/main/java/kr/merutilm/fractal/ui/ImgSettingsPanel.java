package kr.merutilm.fractal.ui;

import javax.swing.*;

import kr.merutilm.base.struct.HexColor;
import kr.merutilm.customswing.CSFrame;
import kr.merutilm.customswing.CSPanel;
import kr.merutilm.customswing.CSValueSelectionInputPanel;
import kr.merutilm.customswing.CSValueTextInputPanel;
import kr.merutilm.fractal.theme.BasicThemes;

import static kr.merutilm.fractal.ui.StatusWindow.BAR_HEIGHT;

import java.awt.*;
import java.util.Objects;

final class ImgSettingsPanel extends CSPanel {

    private final transient RFF master;

    private ShaderSettingsPanels currentShader = null;

    private final CSPanel shaderSettingsPanel;
    
    public ImgSettingsPanel(RFF master, CSFrame window, Rectangle r) {
        super(window);
        this.master = master;
        setBorder(null);
        setBackground(new HexColor(70, 70, 70, 255).toAWT());
        setBounds(r);
       
        CSValueSelectionInputPanel<BasicThemes> themePanel = new CSValueSelectionInputPanel<>(window, this, 
        new Rectangle(0, 0, r.width, BAR_HEIGHT), 
        "Fractal Theme", Objects.requireNonNull(BasicThemes.tryMatch(master.getTheme())), e -> {
            master.setTheme(e.getTheme());
            getPainter().reloadAndPaintCurrentIterations();
            SwingUtilities.invokeLater(this::newUI);

        }, false, BasicThemes.values());
       

        CSValueTextInputPanel<Double> resPanel = new CSValueTextInputPanel<>(window,
                new Rectangle(0, BAR_HEIGHT, r.width, BAR_HEIGHT),
                "Image Resolution Multiplier", master.getSettings().imageSettings().resolutionMultiplier(), Double::parseDouble, false) {
            @Override
            public void enterFunction(Double value) {
                master.setSettings(e1 -> e1.edit().setImageSettings(
                    e2 -> e2.edit().setResolutionMultiplier(value).build()
                    ).build()
                );
                getPainter().recompute();
            }
        };

        
        shaderSettingsPanel = new CSPanel(window);

        CSValueSelectionInputPanel<ShaderSettingsPanels> shaderPanel = new CSValueSelectionInputPanel<>(window, this,
        new Rectangle(0, BAR_HEIGHT * 2, r.width, BAR_HEIGHT),
        "Shaders", ShaderSettingsPanels.COLOR, e -> {
            currentShader = e;
            SwingUtilities.invokeLater(this::newUI);
        }, false, ShaderSettingsPanels.values());

 
        shaderSettingsPanel.setBounds(0, BAR_HEIGHT * 3, r.width, getHeight() - BAR_HEIGHT * 3);
        shaderSettingsPanel.setBaseColor(HexColor.get(30,30,30));
        add(themePanel);
        add(resPanel); 
        add(shaderPanel);
        add(shaderSettingsPanel);
    }


    public CSPanel getShaderSettingsPanel() {
        return shaderSettingsPanel;
    }

    private void newUI(){
        
        shaderSettingsPanel.removeAll();
        if(currentShader == null){
            return;
        }
        currentShader.getGenerator().accept(master);
        repaint();
        shaderSettingsPanel.repaint();
    }


    private RenderPanel getPainter(){
        return master.getFractalRender().getPainter();
    }
}