package kr.merutilm.rff.preset.shader;

import kr.merutilm.rff.preset.Preset;
import kr.merutilm.rff.settings.*;

public record ShaderPreset(Palette palette, Stripe stripe, Slope slope, Color color, Fog fog, Bloom bloom) {

    public interface Palette extends Preset {
        PaletteSettings paletteSettings();
    }

    public interface Stripe extends Preset {
        StripeSettings stripeSettings();
    }

    public interface Slope extends Preset {
        SlopeSettings slopeSettings();
    }

    public interface Color extends Preset {
        ColorSettings colorSettings();
    }

    public interface Fog extends Preset {
        FogSettings fogSettings();
    }

    public interface Bloom extends Preset {
        BloomSettings bloomSettings();
    }

    public ShaderSettings createShaderSettings() {
        return new ShaderSettings(palette.paletteSettings(), stripe.stripeSettings(), slope.slopeSettings(), color.colorSettings(), fog.fogSettings(), bloom.bloomSettings());
    }
}
