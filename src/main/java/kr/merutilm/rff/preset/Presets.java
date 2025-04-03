package kr.merutilm.rff.preset;

import kr.merutilm.rff.preset.calc.*;
import kr.merutilm.rff.preset.location.*;
import kr.merutilm.rff.preset.render.*;
import kr.merutilm.rff.preset.shader.*;
import kr.merutilm.rff.selectable.Selectable;
import kr.merutilm.rff.settings.*;

public final class Presets implements Selectable{


    public static final Calculation INIT_CALCULATION = Calculations.ULTRA_FAST.preset();
    public static final Location INIT_LOCATION = Locations.DEFAULT.preset();

    public static final Render INIT_RENDER = Renders.MEDIUM.preset();
    public static final Shader INIT_SHADER = Shaders.LONG_RAINBOW_FILTERED.preset();

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
            new AnimationSettings(2, true, 1),
            new ExportSettings(30,1,5000, VideoZoomingMethod.IMAGE))
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
        ULTRA_STABLE(new CalculationUltraStable()),
        EXTREME_STABLE(new CalculationExtremeStable()),
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
        BILLION_PERIODS(new LocationBillionPeriod()),
        MINIBROT1(new LocationLittleMinibrot1())
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
        CLASSIC_1_FILTERED(new ShaderClassicFiltered1()),
        CLASSIC_2_FILTERED(new ShaderClassicFiltered2()),
        RAINBOW(new ShaderRainbow()),
        LONG_RAINBOW(new ShaderLongRainbow()),
        LONG_RAINBOW_FILTERED(new ShaderLongRainbowFiltered()),
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
