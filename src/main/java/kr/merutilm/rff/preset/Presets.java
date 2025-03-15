package kr.merutilm.rff.preset;

import kr.merutilm.rff.preset.calc.*;
import kr.merutilm.rff.preset.location.*;
import kr.merutilm.rff.preset.render.*;
import kr.merutilm.rff.preset.shader.*;
import kr.merutilm.rff.selectable.Ease;
import kr.merutilm.rff.selectable.Selectable;
import kr.merutilm.rff.settings.AnimationSettings;
import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.settings.DataSettings;
import kr.merutilm.rff.settings.DecimalizeIterationMethod;
import kr.merutilm.rff.settings.ExportSettings;
import kr.merutilm.rff.settings.ReuseReferenceMethod;
import kr.merutilm.rff.settings.Settings;
import kr.merutilm.rff.settings.VideoSettings;

public class Presets {

    
    private Presets(){

    }

    public static final Calculation INIT_CALCULATION = Calculations.FAST.preset();
    public static final Location INIT_LOCATION = Locations.DEFAULT.preset();

    public static final Render INIT_RENDER = Renders.MEDIUM.preset();
    public static final Shader INIT_SHADER = Shaders.LONG_RAINBOW.preset();

    public static final Settings INIT_SETTINGS = new Settings(
        new CalculationSettings(
            INIT_LOCATION.createCenter(), INIT_LOCATION.logZoom(), 
            INIT_LOCATION.maxIteration(), 
            2, 
            DecimalizeIterationMethod.LOG_LOG, 
            INIT_CALCULATION.r3aSettings(), 
            INIT_CALCULATION.referenceCompressionSettings(), 
            ReuseReferenceMethod.DISABLED, 
            true, 
            false),
        INIT_RENDER.createImageSettings(), 
        INIT_SHADER.createShaderSettings(), 
        new VideoSettings(
            new DataSettings(2), 
            new AnimationSettings(2, true, 1,Ease.LINEAR, 1), 
            new ExportSettings(30,1,5000))
        );

    private interface PresetElement<P extends Preset>{
        P preset();
    }

    public enum Calculations implements Selectable, PresetElement<Calculation>{
        ULTRA_FAST(new CalculationUltraFast()),
        FAST(new CalculationFast()),
        NORMAL(new CalculationNormal()),
        ACCURATE(new CalculationAccurate()),
        STABLE(new CalculationStable()),
        ;
        private final Calculation preset;
    
        Calculations(Calculation generator) {
            this.preset = generator;
        }

        @Override
        public Calculation preset(){
            return preset;            
        }
    
        @Override
        public String toString() {
            return preset.getName();
        }
    }

    public enum Locations implements Selectable, PresetElement<Location>{
        DEFAULT(new LocationDefault()),
        ;
    
    
        private final Location preset;

        Locations(Location generator) {
            this.preset = generator;
        }
    
        @Override
        public Location preset() {
            return preset;
        }

        @Override
        public String toString() {
            return preset.getName();
        }
    }

    public enum Renders implements Selectable, PresetElement<Render>{
        LOW(new RenderLow()),
        MEDIUM(new RenderMedium()),
        HIGH(new RenderHigh()),
        ULTRA(new RenderUltra())
        ;
        private final Render preset;
    
        Renders(Render generator) {
            this.preset = generator;
        }
     
        @Override
        public Render preset() {
            return preset;
        }

        @Override
        public String toString() {
            return preset.getName();
        }
    }
    public enum Shaders implements Selectable, PresetElement<Shader>{
        CLASSIC_1(new ShaderClassic1()),
        CLASSIC_2(new ShaderClassic2()),
        RAINBOW(new ThemeRainbow()),
        LONG_RAINBOW_CLASSIC(new ShaderLongRainbowClassic()),
        LONG_RAINBOW(new ShaderLongRainbow()),
        CINEMATIC(new ShaderCinematic()),
        AZURE(new ShaderAzure()),
        FLAME(new ShaderFlame()),
        DESERT(new ShaderDesert());
    
    
        private final Shader preset;
    
        Shaders(Shader generator) {
            this.preset = generator;
        }

        @Override
        public Shader preset() {
            return preset;
        }

        @Override
        public String toString() {
            return preset.getName();
        }
    
    }
}
