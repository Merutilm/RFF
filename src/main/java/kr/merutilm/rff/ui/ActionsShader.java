package kr.merutilm.rff.ui;


import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import javax.swing.KeyStroke;

import kr.merutilm.rff.settings.*;
import kr.merutilm.rff.util.IOUtilities;


enum ActionsShader implements Actions {

    PALETTE("Palette", "Shader:Palette", (master, name) -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        ColorSettings color = getShaderSettings(master).colorSettings();
        
        Consumer<UnaryOperator<ColorSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setShaderSettings(e2 -> e2.edit().setColorSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            ActionsExplore.REFRESH_COLOR.accept(master);
        };

        panel.createTextInput("Color Pulse Interval", "Required iterations for the palette to cycle once", color.iterationInterval(), Double::parseDouble, e ->
            applier.accept(f -> f.setIterationInterval(e))
        );
        panel.createTextInput(IOUtilities.Constants.OFFSET_RATIO.toString(), "Start offset ratio of cycling palette", color.offsetRatio(), Double::parseDouble, e ->
            applier.accept(f -> f.setOffsetRatio(e))
        );
        panel.createSelectInput("Color Smoothing", "Color Smoothing method", color.colorSmoothing(), ColorSmoothingSettings.values(), e ->
            applier.accept(f -> f.setColorSmoothing(e)), false
        );
    }), null),

    STRIPE("Stripe", "Shader:Stripe", (master, name) -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        StripeSettings color = getShaderSettings(master).stripeSettings();
        
        Consumer<UnaryOperator<StripeSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setShaderSettings(e2 -> e2.edit().setStripeSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            ActionsExplore.REFRESH_COLOR.accept(master);
        };


        panel.createBoolInput("Use", "Use Stripe", color.use(), e ->
            applier.accept(f -> f.setUse(e))
        );
        panel.createTextInput("First Interval", "Stripe interval 1", color.firstInterval(), Double::parseDouble, e ->
            applier.accept(f -> f.setFirstInterval(e))
        );
        panel.createTextInput("Secondary Interval", "Stripe interval 2", color.secondInterval(), Double::parseDouble, e ->
            applier.accept(f -> f.setSecondInterval(e))
        );
        panel.createTextInput(IOUtilities.Constants.OPACITY.toString(), "Stripe Opacity", color.opacity(), Double::parseDouble, e ->
            applier.accept(f -> f.setOpacity(e))
        );
        panel.createTextInput(IOUtilities.Constants.OFFSET_RATIO.toString(), "Stripe offset ratio", color.offset(), Double::parseDouble, e ->
            applier.accept(f -> f.setOffset(e))
        );
    }), null),
    SLOPE("Slope","Shader:Slope", (master, name) -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        SlopeSettings slope = getShaderSettings(master).slopeSettings();
        
        Consumer<UnaryOperator<SlopeSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setShaderSettings(e2 -> e2.edit().setSlopeSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            ActionsExplore.REFRESH_COLOR.accept(master);
        };

        panel.createTextInput("Depth", "Slope depth", slope.depth(), Double::parseDouble, e ->
            applier.accept(f -> f.setDepth(e))
        );
        panel.createTextInput("Reflection Ratio", "Slope reflection ratio, Darker part is set to the threshold.", slope.reflectionRatio(), Double::parseDouble, e ->
            applier.accept(f -> f.setReflectionRatio(e))
        );
        panel.createTextInput(IOUtilities.Constants.OPACITY.toString(), "Slope opacity", slope.opacity(), Double::parseDouble, e ->
        	applier.accept(f -> f.setOpacity(e))
        );
        panel.createTextInput("Zenith", "The zenith of light source", slope.zenith(), Double::parseDouble, e ->
            applier.accept(f -> f.setZenith(e))
        );
        panel.createTextInput("Azimuth", "The azimuth of light source", slope.azimuth(), Double::parseDouble, e ->
            applier.accept(f -> f.setAzimuth(e))
        );

        
    }), null),
    COLOR_FILTER("Color Filter","Shader:ColorFilter", (master, name) ->
        new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
            ColorFilterSettings colorFilter = getShaderSettings(master).colorFilterSettings();
        
            Consumer<UnaryOperator<ColorFilterSettings.Builder>> applier = e -> {
                master.setSettings(e1 -> e1.edit().setShaderSettings(e2 -> e2.edit().setColorFilterSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
                ActionsExplore.REFRESH_COLOR.accept(master);
            };
    
            panel.createTextInput("Gamma", "Gamma value", colorFilter.gamma(), Double::parseDouble, e ->
                applier.accept(f -> f.setGamma(e))
            );
            panel.createTextInput("Exposure", "Exposure value", colorFilter.exposure(), Double::parseDouble, e ->
                applier.accept(f -> f.setExposure(e))        
            );
            panel.createTextInput("Saturation", "Saturation value", colorFilter.saturation(), Double::parseDouble, e ->
                   applier.accept(f -> f.setSaturation(e))
            );
            panel.createTextInput("Brightness", "Brightness value", colorFilter.brightness(), Double::parseDouble, e ->
                applier.accept(f -> f.setBrightness(e))
            );
            panel.createTextInput("Contrast", "Contrast value", colorFilter.contrast(), Double::parseDouble, e ->
                applier.accept(f -> f.setContrast(e))
            );
        }), null),
    FOG("Fog", "Shader:Fog", (master, name) -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        FogSettings fog = getShaderSettings(master).fogSettings();
        
        Consumer<UnaryOperator<FogSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setShaderSettings(e2 -> e2.edit().setFogSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            ActionsExplore.REFRESH_COLOR.accept(master);
        };

        panel.createTextInput("Radius", "Fog radius ratio in rendered image, fully blurred when the value is 1.", fog.radius(), Double::parseDouble, e ->
        	applier.accept(f -> f.setRadius(e))
        );
        panel.createTextInput(IOUtilities.Constants.OPACITY.toString(), "Fog opacity", fog.opacity(), Double::parseDouble, e ->
        	applier.accept(f -> f.setOpacity(e))
        );
    }), null),
    BLOOM("Bloom", "Shader:Bloom", (master, name) -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        BloomSettings bloom = getShaderSettings(master).bloomSettings();
    
        Consumer<UnaryOperator<BloomSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setShaderSettings(e2 -> e2.edit().setBloomSettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            ActionsExplore.REFRESH_COLOR.accept(master);
        };

        panel.createTextInput("Threshold", "Threshold to apply", bloom.threshold(), Double::parseDouble, e ->
        	applier.accept(f -> f.setThreshold(e))
        );
        panel.createTextInput("Radius", "Bloom radius ratio in rendered image, fully blurred when the value is 1.", bloom.radius(), Double::parseDouble, e ->
        	applier.accept(f -> f.setRadius(e))
        );
        panel.createTextInput("Softness", "Bloom softness", bloom.softness(), Double::parseDouble, e ->
        	applier.accept(f -> f.setSoftness(e))
        );
        panel.createTextInput("Intensity", "Bloom intensity", bloom.intensity(), Double::parseDouble, e ->
        	applier.accept(f -> f.setIntensity(e))
        );
    }), null);

    private final String name;
    private final BiConsumer<RFF, String> action;
    private final KeyStroke keyStroke;
    private final String description;

    @Override
    public KeyStroke keyStroke() {
        return keyStroke;
    }

    public String description() {
        return description;
    }

    ActionsShader(String name, String description, BiConsumer<RFF, String> generator, KeyStroke keyStroke) {
        this.name = name;
        this.description = description;
        this.action = generator;
        this.keyStroke = keyStroke;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void accept(RFF master) {
        action.accept(master, name);
    }

    private static ShaderSettings getShaderSettings(RFF master){
        return master.getSettings().shaderSettings();
    }

}
