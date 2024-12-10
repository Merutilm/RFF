package kr.merutilm.fractal.ui;


import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import kr.merutilm.fractal.RFFUtils;
import kr.merutilm.fractal.settings.*;


enum ActionsShader implements Actions {

    PALETTE("Palette", (master, name) -> new SettingsWindow(name, panel -> {
        ColorSettings color = getShaderSettings(master).colorSettings();
        
        Consumer<UnaryOperator<ColorSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setShaderSettings(e2 -> e2.edit().setColorSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            ActionsExplore.REFRESH_COLOR.accept(master);
        };

        panel.createTextInput("Color Pulse Interval", color.iterationInterval(), Double::parseDouble, e -> 
            applier.accept(f -> f.setIterationInterval(e))
        );
        panel.createTextInput(RFFUtils.Constants.OFFSET_RATIO.toString(), color.offsetRatio(), Double::parseDouble, e ->
            applier.accept(f -> f.setOffsetRatio(e))
        );
        panel.createSelectInput("Color Smoothing", color.colorSmoothing(), ColorSmoothingSettings.values(), e ->
            applier.accept(f -> f.setColorSmoothing(e)), false
        );
    })),

    STRIPE("Stripe", (master, name) -> new SettingsWindow(name, panel -> {
        StripeSettings color = getShaderSettings(master).stripeSettings();
        
        Consumer<UnaryOperator<StripeSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setShaderSettings(e2 -> e2.edit().setStripeSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            ActionsExplore.REFRESH_COLOR.accept(master);
        };


        panel.createBoolInput("Use", color.use(), e -> 
            applier.accept(f -> f.setUse(e))
        );
        panel.createTextInput("First Interval", color.firstInterval(), Double::parseDouble, e -> 
            applier.accept(f -> f.setFirstInterval(e))
        );
        panel.createTextInput("Secondary Interval", color.secondInterval(), Double::parseDouble, e ->
            applier.accept(f -> f.setSecondInterval(e))
        );
        panel.createTextInput(RFFUtils.Constants.OPACITY.toString(), color.opacity(), Double::parseDouble, e ->
            applier.accept(f -> f.setOpacity(e))
        );
        panel.createTextInput(RFFUtils.Constants.OFFSET_RATIO.toString(), color.offset(), Double::parseDouble, e ->
            applier.accept(f -> f.setOffset(e))
        );
    })),
    SLOPE("Slope", (master, name) -> new SettingsWindow(name, panel -> {
        SlopeSettings slope = getShaderSettings(master).slopeSettings();
        
        Consumer<UnaryOperator<SlopeSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setShaderSettings(e2 -> e2.edit().setSlopeSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            ActionsExplore.REFRESH_COLOR.accept(master);
        };

        panel.createTextInput("Depth", slope.depth(), Double::parseDouble, e -> 
            applier.accept(f -> f.setDepth(e))
        );
        panel.createTextInput("Reflection Ratio", slope.reflectionRatio(), Double::parseDouble, e -> 
            applier.accept(f -> f.setReflectionRatio(e))
        );
        panel.createTextInput(RFFUtils.Constants.OPACITY.toString(), slope.opacity(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setOpacity(e))
        );
        panel.createTextInput("Zenith", slope.zenith(), Double::parseDouble, e -> 
            applier.accept(f -> f.setZenith(e))
        );
        panel.createTextInput("Azimuth", slope.azimuth(), Double::parseDouble, e -> 
            applier.accept(f -> f.setAzimuth(e))
        );

        
    })),
    COLOR_FILTER("Color Filter", (master, name) -> 
        new SettingsWindow(name, panel -> {
            ColorFilterSettings colorFilter = getShaderSettings(master).colorFilterSettings();
        
            Consumer<UnaryOperator<ColorFilterSettings.Builder>> applier = e -> {
                master.setSettings(e1 -> e1.edit().setShaderSettings(e2 -> e2.edit().setColorFilterSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
                ActionsExplore.REFRESH_COLOR.accept(master);
            };
    
            panel.createTextInput("Gamma", colorFilter.gamma(), Double::parseDouble, e -> 
                applier.accept(f -> f.setGamma(e))
            );
            panel.createTextInput("Exposure", colorFilter.exposure(), Double::parseDouble, e -> 
                applier.accept(f -> f.setExposure(e))        
            );
            panel.createTextInput("Saturation", colorFilter.saturation(), Double::parseDouble, e -> 
                   applier.accept(f -> f.setSaturation(e))
            );
            panel.createTextInput("Brightness", colorFilter.brightness(), Double::parseDouble, e -> 
                applier.accept(f -> f.setBrightness(e))
            );
            panel.createTextInput("Contrast", colorFilter.contrast(), Double::parseDouble, e -> 
                applier.accept(f -> f.setContrast(e))
            );
        })
    ),
    FOG("Fog", (master, name) -> new SettingsWindow(name, panel -> {
        FogSettings fog = getShaderSettings(master).fogSettings();
        
        Consumer<UnaryOperator<FogSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setShaderSettings(e2 -> e2.edit().setFogSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            ActionsExplore.REFRESH_COLOR.accept(master);
        };

        panel.createTextInput("Radius", fog.radius(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setRadius(e))
        );
        panel.createTextInput(RFFUtils.Constants.OPACITY.toString(), fog.opacity(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setOpacity(e))
        );
    })),
    BLOOM("Bloom", (master, name) -> new SettingsWindow(name, panel -> {
        BloomSettings bloom = getShaderSettings(master).bloomSettings();
    
        Consumer<UnaryOperator<BloomSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setShaderSettings(e2 -> e2.edit().setBloomSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            ActionsExplore.REFRESH_COLOR.accept(master);
        };

        panel.createTextInput("Threshold", bloom.threshold(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setThreshold(e))
        );
        panel.createTextInput("Radius", bloom.radius(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setRadius(e))
        );
        panel.createTextInput("Softness", bloom.softness(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setSoftness(e))
        );
        panel.createTextInput("Intensity", bloom.intensity(), Double::parseDouble, e -> 
        	applier.accept(f -> f.setIntensity(e))
        );
    }));

    private final String name;
    private final BiConsumer<RFF, String> generator;
    private ActionsShader(String name, BiConsumer<RFF, String> generator){
        this.name = name;
        this.generator = generator;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void accept(RFF master) {
        generator.accept(master, name);
    }

    private static ShaderSettings getShaderSettings(RFF master){
        return master.getSettings().shaderSettings();
    }

}
