package kr.merutilm.rff.preset;

import kr.merutilm.rff.preset.calc.*;
import kr.merutilm.rff.preset.location.*;
import kr.merutilm.rff.preset.render.*;
import kr.merutilm.rff.preset.resolution.*;
import kr.merutilm.rff.preset.shader.*;
import kr.merutilm.rff.preset.shader.bloom.*;
import kr.merutilm.rff.preset.shader.color.*;
import kr.merutilm.rff.preset.shader.fog.*;
import kr.merutilm.rff.preset.shader.palette.*;
import kr.merutilm.rff.preset.shader.slope.*;
import kr.merutilm.rff.preset.shader.stripe.*;
import kr.merutilm.rff.selectable.Selectable;
import kr.merutilm.rff.settings.*;

public final class Presets implements Selectable{


    public static final Calculation INIT_CALCULATION = Calculations.ULTRA_FAST.preset();
    public static final Location INIT_LOCATION = Locations.DEFAULT.preset();

    public static final Render INIT_RENDER = Renders.HIGH.preset();
    public static final ShaderPreset INIT_SHADER_PRESET = new ShaderPreset(
            Shaders.Palettes.LONG_RAINBOW.preset(),
            Shaders.Stripes.SLOW.preset(),
            Shaders.Slopes.NORMAL.preset(),
            Shaders.Colors.WEAK_CONTRAST.preset(),
            Shaders.Fogs.MEDIUM.preset(),
            Shaders.Blooms.NORMAL.preset()
    );

    public static final Settings INIT_SETTINGS = new Settings(
        new CalculationSettings(
            INIT_LOCATION.createCenter(), INIT_LOCATION.logZoom(), 
            INIT_LOCATION.maxIteration(), 
            2, 
            DecimalizeIterationMethod.LOG_LOG, 
            INIT_CALCULATION.mpaSettings(),
            INIT_CALCULATION.referenceCompressionSettings(), 
            ReuseReferenceMethod.DISABLED, 
            true, 
            false),
        INIT_RENDER.createImageSettings(), 
        INIT_SHADER_PRESET.createShaderSettings(),
        new VideoSettings(
            new DataSettings(2), 
            new AnimationSettings(2, true, 1),
            new ExportSettings(30,1,5000))
        );

    public interface PresetElement<P extends Preset>{
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
        POTATO(new RenderPotato()),
        LOW(new RenderLow()),
        MEDIUM(new RenderMedium()),
        HIGH(new RenderHigh())
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

    public static final class Shaders{
        public enum Palettes implements Selectable, PresetElement<ShaderPreset.Palette>{
            CLASSIC_1(new PaletteClassic1()),
            CLASSIC_2(new PaletteClassic2()),
            AZURE(new PaletteAzure()),
            FLAME(new PaletteFlame()),
            CINEMATIC(new PaletteCinematic()),
            RAINBOW(new PaletteRainbow()),
            LONG_RAINBOW(new PaletteLongRainbow()),
            DESERT(new PaletteDesert()),
            RANDOM_16(new PaletteRandomShort()),
            RANDOM_256(new PaletteRandomMedium()),
            RANDOM_4096(new PaletteRandomLong()),
            RANDOM_65536(new PaletteRandomSemiInf()),
            ;

            private final ShaderPreset.Palette preset;

            Palettes(ShaderPreset.Palette generator) {
                this.preset = generator;
            }

            @Override
            public ShaderPreset.Palette preset() {
                return preset;
            }

            @Override
            public String toString() {
                return preset.getName();
            }
        }
        public enum Stripes implements Selectable, PresetElement<ShaderPreset.Stripe>{
            NONE(new StripeNone()),
            SLOW(new StripeSlowAnimation()),
            FAST(new StripeFastAnimation()),
            SMOOTH(new StripeSmooth()),
            TRANSLUCENT(new StripeTranslucent())
            ;

            private final ShaderPreset.Stripe preset;

            Stripes(ShaderPreset.Stripe generator) {
                this.preset = generator;
            }

            @Override
            public ShaderPreset.Stripe preset() {
                return preset;
            }

            @Override
            public String toString() {
                return preset.getName();
            }
        }
        public enum Slopes implements Selectable, PresetElement<ShaderPreset.Slope>{
            NONE(new SlopeNone()),
            NORMAL(new SlopeNormal()),
            NANO(new SlopeNano()),
            MICRO(new SlopeMicro()),
            NO_REFLECTION(new SlopeNoReflection()),
            REVERSED(new SlopeReversed()),
            ;

            private final ShaderPreset.Slope preset;

            Slopes(ShaderPreset.Slope generator) {
                this.preset = generator;
            }

            @Override
            public ShaderPreset.Slope preset() {
                return preset;
            }

            @Override
            public String toString() {
                return preset.getName();
            }
        }
        public enum Colors implements Selectable, PresetElement<ShaderPreset.Color>{
            NONE(new ColorNone()),
            VIVID(new ColorVivid()),
            DULL(new ColorDull()),
            WEAK_CONTRAST(new ColorWeakContrast()),
            HIGH_CONTRAST(new ColorHighContrast()),
            ;

            private final ShaderPreset.Color preset;

            Colors(ShaderPreset.Color generator) {
                this.preset = generator;
            }

            @Override
            public ShaderPreset.Color preset() {
                return preset;
            }

            @Override
            public String toString() {
                return preset.getName();
            }
        }
        public enum Fogs implements Selectable, PresetElement<ShaderPreset.Fog>{
            NONE(new FogNone()),
            LOW(new FogLow()),
            MEDIUM(new FogMedium()),
            HIGH(new FogHigh()),
            ULTRA(new FogUltra()),
            ;

            private final ShaderPreset.Fog preset;

            Fogs(ShaderPreset.Fog generator) {
                this.preset = generator;
            }

            @Override
            public ShaderPreset.Fog preset() {
                return preset;
            }

            @Override
            public String toString() {
                return preset.getName();
            }
        }
        public enum Blooms implements Selectable, PresetElement<ShaderPreset.Bloom>{
            NONE(new BloomNone()),
            NORMAL(new BloomNormal()),
            STRONG(new BloomStrong()),
            HIGHLIGHT(new BloomHighlight()),
            ITSELF(new BloomItself()),
            ;

            private final ShaderPreset.Bloom preset;

            Blooms(ShaderPreset.Bloom generator) {
                this.preset = generator;
            }

            @Override
            public ShaderPreset.Bloom preset() {
                return preset;
            }

            @Override
            public String toString() {
                return preset.getName();
            }
        }

    }

    public enum Resolutions implements Selectable, PresetElement<Resolution>{
        L1(new ResolutionL1()),
        L2(new ResolutionL2()),
        L3(new ResolutionL3()),
        L4(new ResolutionL4()),
        L5(new ResolutionL5()),
        L6(new ResolutionL6()),
        ;

        private final Resolution preset;

        Resolutions(Resolution generator) {
            this.preset = generator;
        }

        @Override
        public Resolution preset() {
            return preset;
        }

        @Override
        public String toString() {
            return preset.getName();
        }
    }
}
