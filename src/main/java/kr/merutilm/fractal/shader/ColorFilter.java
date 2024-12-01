package kr.merutilm.fractal.shader;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.parallel.ShaderRenderer;
import kr.merutilm.base.struct.HexColor;
import kr.merutilm.base.util.AdvancedMath;
import kr.merutilm.fractal.settings.ColorFilterSettings;

public class ColorFilter implements ShaderRenderer{

    private final double gamma;
    private final double exposure;
    private final double saturation;
    private final double brightness;
    private final double contrast;

    public ColorFilter(ColorFilterSettings cfs) {
        this.gamma = cfs.gamma();
        this.exposure = cfs.exposure();
        this.saturation = cfs.saturation();
        this.brightness = cfs.brightness();
        this.contrast = cfs.contrast();
    }

    @Override
    public HexColor execute(int x, int y, int xRes, int yRes, double rx, double ry, int i, HexColor c, double t)
            throws IllegalRenderStateException {
        if(c == null){
            return null;
        }
        
        return c
        .functionExceptAlpha(e -> HexColor.safetyFix((int)(255 * Math.pow(e / 255.0, 1 / gamma))))
        .functionExceptAlpha(e -> HexColor.safetyFix((int)(e * (1 + exposure) / (1 - exposure))))
        .functionExceptAlpha((c2, e) -> HexColor.safetyFix((int)(AdvancedMath.ratioDivide(e, c2.grayScaleValue(), -saturation))))
        .functionExceptAlpha(e -> HexColor.safetyFix((int)(e + brightness * 255)))
        .functionExceptAlpha(e -> HexColor.safetyFix((int)((e - 127.5) / (1 - contrast) * (1 + contrast) + 127.5)));
    }

    @Override
    public boolean isValid(){
        return !(gamma == 1 && exposure == 0 && saturation == 0 && brightness == 0 && contrast == 0);
    }
    
}
