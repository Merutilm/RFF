package kr.merutilm.fractal.ui;


import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import kr.merutilm.base.selectable.Selectable;
import kr.merutilm.customswing.CSPanel;
import kr.merutilm.customswing.CSValueInputGroupPanel;
import kr.merutilm.customswing.CSValueInputGroupPanel.InputType;
import kr.merutilm.fractal.settings.*;


enum ShaderSettingsPanels implements Selectable{
    COLOR("Color", master -> {
        CSPanel target = targetPanel(master);
        
        CSValueInputGroupPanel panel = new CSValueInputGroupPanel(window(master), target, "", InputType.VERTICAL, false);
        
        ColorSettings color = getImageSettings(master).colorSettings();
        
        Consumer<UnaryOperator<ColorSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setImageSettings(e2 -> e2.edit().setColorSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            reloadColor(master);
        };

        panel.createTextInput("Color Pulse Interval", null, color.iterationInterval(), Double::parseDouble, e -> 
            applier.accept(f -> f.setIterationInterval(e))
        );
        panel.createTextInput("Offset Ratio", null, color.offsetRatio(), Double::parseDouble, e ->
            applier.accept(f -> f.setOffsetRatio(e))
        );
        panel.createSelectInput("Color Smoothing", null, color.colorSmoothing(), ColorSmoothingSettings.values(), e ->
            applier.accept(f -> f.setColorSmoothing(e))
        );
        target.add(panel);
    }),

    SLOPE("Slope", master -> {
        CSPanel target = targetPanel(master);
        
        CSValueInputGroupPanel panel = new CSValueInputGroupPanel(window(master), target, "", InputType.VERTICAL, false);
        
        SlopeSettings slope = getImageSettings(master).slopeSettings();
        
        Consumer<UnaryOperator<SlopeSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setImageSettings(e2 -> e2.edit().setSlopeSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            reloadColor(master);
        };

        panel.createTextInput("Depth", null, slope.depth(), Double::parseDouble, e -> 
            applier.accept(f -> f.setDepth(e))
        );
        panel.createTextInput("Reflection Ratio", null, slope.reflectionRatio(), Double::parseDouble, e -> 
            applier.accept(f -> f.setReflectionRatio(e))
        );
        panel.createTextInput("Opacity", null, slope.opacity(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setOpacity(e))
        );
        panel.createTextInput("Zenith", null, slope.zenith(), Double::parseDouble, e -> 
            applier.accept(f -> f.setZenith(e))
        );
        panel.createTextInput("Azimuth", null, slope.azimuth(), Double::parseDouble, e -> 
            applier.accept(f -> f.setAzimuth(e))
        );
        
        target.add(panel);
    }),
    COLOR_FILTER("Color Filter", master -> {
        CSPanel target = targetPanel(master);
        
        CSValueInputGroupPanel panel = new CSValueInputGroupPanel(window(master), target, "", InputType.VERTICAL, false);
        
        ColorFilterSettings colorFilter = getImageSettings(master).colorFilterSettings();
        
        Consumer<UnaryOperator<ColorFilterSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setImageSettings(e2 -> e2.edit().setColorFilterSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            reloadColor(master);
        };

        panel.createTextInput("Gamma", null, colorFilter.gamma(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setGamma(e))
        );
        panel.createTextInput("Exposure", null, colorFilter.exposure(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setExposure(e))        
        );
        panel.createTextInput("Saturation", null, colorFilter.saturation(), Double::parseDouble, e -> 
       		applier.accept(f -> f.setSaturation(e))
        );
        panel.createTextInput("Brightness", null, colorFilter.brightness(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setBrightness(e))
        );
        panel.createTextInput("Contrast", null, colorFilter.contrast(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setContrast(e))
        );
                

        target.add(panel);
    }),
    FOG("Fog", master -> {
        CSPanel target = targetPanel(master);
        
        CSValueInputGroupPanel panel = new CSValueInputGroupPanel(window(master), target, "", InputType.VERTICAL, false);
           
        FogSettings fog = getImageSettings(master).fogSettings();
        
        Consumer<UnaryOperator<FogSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setImageSettings(e2 -> e2.edit().setFogSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            reloadColor(master);
        };

        panel.createTextInput("Radius", null, fog.radius(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setRadius(e))
        );
        panel.createTextInput("Opacity", null, fog.opacity(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setOpacity(e))
        );


        target.add(panel);
    }),
    BLOOM("Bloom", master -> {
        CSPanel target = targetPanel(master);
        
        CSValueInputGroupPanel panel = new CSValueInputGroupPanel(window(master), target, "", InputType.VERTICAL, false);
        
        BloomSettings bloom = getImageSettings(master).bloomSettings();
        
        Consumer<UnaryOperator<BloomSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setImageSettings(e2 -> e2.edit().setBloomSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            reloadColor(master);
        };

        panel.createTextInput("Threshold", null, bloom.threshold(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setThreshold(e))
        );
        panel.createTextInput("Radius", 
        null, bloom.radius(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setRadius(e))
        );
        panel.createTextInput("Softness", null, bloom.softness(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setSoftness(e))
        );
        panel.createTextInput("Intensity", null, bloom.intensity(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setIntensity(e))
        );

        target.add(panel);
    });

    private final String name;
    private final Consumer<RFF> generator;
   
    private ShaderSettingsPanels(String name, Consumer<RFF> generator){
        this.name = name;
        this.generator = generator;
    }

    @Override
    public String toString() {
        return name;
    }

    public Consumer<RFF> getGenerator() {
        return generator;
    }

    private static StatusWindow window(RFF master){
        return master.getFractalStatus();
    }

    private static CSPanel targetPanel(RFF master){
        return master.getFractalStatus().getFractalImg().getShaderSettingsPanel();
    }

    private static ImageSettings getImageSettings(RFF master){
        return master.getSettings().imageSettings();
    }

    private static void reloadColor(RFF master){
        master.getFractalRender().getPainter().reloadAndPaintCurrentMap();
    }
}
