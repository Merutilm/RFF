package kr.merutilm.rff.ui;


import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import javax.swing.KeyStroke;

import kr.merutilm.rff.selectable.StripeType;
import kr.merutilm.rff.settings.*;
import kr.merutilm.rff.util.IOUtilities;


enum ActionsShader implements ItemActions {

    PALETTE("Palette", "Shader:Palette", null, 
    (master, name, description, accelerator) -> 
    ItemActions.createItem(name, description, accelerator, () -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        PaletteSettings color = getShaderSettings(master).paletteSettings();
        
        Consumer<UnaryOperator<PaletteSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.setShaderSettings(e2 -> e2.setPaletteSettings(e)));
            master.getWindow().getRenderer().requestColor();
        };

        panel.createTextInput("Color Pulse Interval", "Required iterations for the palette to cycle once", color.iterationInterval(), Double::parseDouble, e ->
            applier.accept(f -> f.setIterationInterval(e))
        );
        panel.createTextInput(IOUtilities.Constants.OFFSET_RATIO.toString(), "Start offset ratio of cycling palette", color.offsetRatio(), Double::parseDouble, e ->
            applier.accept(f -> f.setOffsetRatio(e))
        );
        panel.createTextInput("Animation Speed", "Color Animation Speed, The colors' offset(iterations) per second.", color.animationSpeed(), Double::parseDouble, e ->
                applier.accept(f -> f.setAnimationSpeed(e))
        );
        panel.createSelectInput("Color Smoothing", "Color Smoothing method", color.colorSmoothing(), ColorSmoothingSettings.values(), e ->
            applier.accept(f -> f.setColorSmoothing(e)), false
        );

    }))),

    STRIPE("Stripe", "Shader:Stripe", null, 
    (master, name, description, accelerator) -> 
    ItemActions.createItem(name, description, accelerator, () -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        StripeSettings color = getShaderSettings(master).stripeSettings();
        
        Consumer<UnaryOperator<StripeSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.setShaderSettings(e2 -> e2.setStripeSettings(e)));
            master.getWindow().getRenderer().requestColor();
        };


        panel.createSelectInput("Stripe Type", "Stripe Type", color.stripeType(), StripeType.values(), e ->
            applier.accept(f -> f.setStripeType(e))
        , true);
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
        panel.createTextInput("Animation Speed", "Stripe Animation Speed, The stripes' offset(iterations) per second.", color.animationSpeed(), Double::parseDouble, e ->
                applier.accept(f -> f.setAnimationSpeed(e))
        );
    }))),
    SLOPE("Slope","Shader:Slope", null, 
    (master, name, description, accelerator) -> 
    ItemActions.createItem(name, description, accelerator, () -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        SlopeSettings slope = getShaderSettings(master).slopeSettings();
        
        Consumer<UnaryOperator<SlopeSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.setShaderSettings(e2 -> e2.setSlopeSettings(e)));
            master.getWindow().getRenderer().requestColor();
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

        
    }))),
    COLOR_FILTER("Color","Shader:Color", null,
        (master, name, description, accelerator) -> 
        ItemActions.createItem(name, description, accelerator, () -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
            ColorSettings colorFilter = getShaderSettings(master).colorSettings();
        
            Consumer<UnaryOperator<ColorSettings.Builder>> applier = e -> {
                master.setSettings(e1 -> e1.setShaderSettings(e2 -> e2.setColorSettings(e)));
                master.getWindow().getRenderer().requestColor();
            };
    
            panel.createTextInput("Gamma", "Gamma value", colorFilter.gamma(), Double::parseDouble, e ->
                applier.accept(f -> f.setGamma(e))
            );
            panel.createTextInput("Exposure", "Exposure value", colorFilter.exposure(), Double::parseDouble, e ->
                applier.accept(f -> f.setExposure(e))        
            );
            panel.createTextInput("Hue", "Hue value", colorFilter.hue(), Double::parseDouble, e ->
                    applier.accept(f -> f.setHue(e))
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
        }))),
    FOG("Fog", "Shader:Fog", null,
    (master, name, description, accelerator) -> 
    ItemActions.createItem(name, description, accelerator, () ->  new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        FogSettings fog = getShaderSettings(master).fogSettings();
        
        Consumer<UnaryOperator<FogSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.setShaderSettings(e2 -> e2.setFogSettings(e)));
            master.getWindow().getRenderer().requestColor();
        };

        panel.createTextInput("Radius", "Fog radius ratio in rendered image, fully blurred when the value is 1.", fog.radius(), Double::parseDouble, e ->
        	applier.accept(f -> f.setRadius(e))
        );
        panel.createTextInput(IOUtilities.Constants.OPACITY.toString(), "Fog opacity", fog.opacity(), Double::parseDouble, e ->
        	applier.accept(f -> f.setOpacity(e))
        );
    }))),
    BLOOM("Bloom", "Shader:Bloom", null, 
    (master, name, description, accelerator) -> 
    ItemActions.createItem(name, description, accelerator, () ->  new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        BloomSettings bloom = getShaderSettings(master).bloomSettings();
    
        Consumer<UnaryOperator<BloomSettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.setShaderSettings(e2 -> e2.setBloomSettings(e)));
            master.getWindow().getRenderer().requestColor();
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
    })));


    private final String name;
    private final String description;
    private final KeyStroke accelerator;
    private final Initializer initializer;

    @Override
    public KeyStroke keyStroke() {
        return accelerator;
    }

    public String description() {
        return description;
    }

    public Initializer initializer() {
        return initializer;
    }

    ActionsShader(String name, String description, KeyStroke accelerator, Initializer initializer) {
        this.name = name;
        this.description = description;
        this.accelerator = accelerator;
        this.initializer = initializer;
    }

    @Override
    public String toString() {
        return name;
    }

    private static ShaderSettings getShaderSettings(RFF master){
        return master.getSettings().shaderSettings();
    }

}
