package kr.merutilm.fractal.shader;

import static java.lang.Math.*;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.parallel.ShaderRenderer;
import kr.merutilm.base.struct.DoubleMatrix;
import kr.merutilm.base.struct.HexColor;
import kr.merutilm.base.util.AdvancedMath;
import kr.merutilm.fractal.settings.SlopeSettings;

public class Slope implements ShaderRenderer{

    private final DoubleMatrix altitudes;
    private final double reflectionRatio;
    private final double opacity;
    private final double zenith;
    private final double azimuth;
    private final double depth;
    private final double resolutionMultiplier;
    private final int ad;

    public Slope(DoubleMatrix altitudes, SlopeSettings slopeSettings, double resolutionMultiplier, int altitudesDivisor) {
        this.altitudes = altitudes;
        this.reflectionRatio = slopeSettings.reflectionRatio();
        this.opacity = slopeSettings.opacity();
        this.azimuth = slopeSettings.azimuth();
        this.zenith = slopeSettings.zenith();
        this.depth = slopeSettings.depth();
        this.resolutionMultiplier = resolutionMultiplier;
        this.ad = altitudesDivisor;
    }

    @Override
    public HexColor execute(int x, int y, int xRes, int yRes, double rx, double ry, int i, HexColor c, double t)
            throws IllegalRenderStateException {
                if(c == null){
                    return null;
                }

                double aRad = toRadians(azimuth);
                double zRad = toRadians(zenith);

                int xd = x * ad;
                int yd = y * ad;

                double ld = altitudes.pipette(xd - ad, yd + ad);
                double d = altitudes.pipette(xd, yd + ad);
                double rd = altitudes.pipette(xd + ad, yd + ad); 
                double l = altitudes.pipette(xd - ad, yd);
                double r = altitudes.pipette(xd + ad, yd);
                double lu = altitudes.pipette(xd - ad, yd - ad);
                double u = altitudes.pipette(xd, yd - ad);
                double ru = altitudes.pipette(xd + ad, yd - ad);

                double dzdx = ((rd + 2 * r + ru) - (ld + 2 * l + lu)) * depth * resolutionMultiplier / ad; 
                double dzdy = ((lu + 2 * u + ru) - (ld + 2 * d + rd)) * depth * resolutionMultiplier / ad; 
                double slope = atan(toRadians(hypot(dzdx, dzdy)));
                double aspect = atan2(dzdy, -dzdx); 
                double shade = Math.max(reflectionRatio, cos(zRad) * cos(slope) + sin(zRad) * sin(slope) * cos(aRad + aspect)); 
                double fShade = AdvancedMath.ratioDivide(1 - opacity, 1, shade);
                return c.functionExceptAlpha(e -> (int)(e * fShade));    
    }

    @Override
    public boolean isValid(){
        return opacity > 0 && reflectionRatio < 1 && depth != 0;
    }
}
