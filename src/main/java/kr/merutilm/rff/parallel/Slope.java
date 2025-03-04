package kr.merutilm.rff.parallel;

import static java.lang.Math.*;

import kr.merutilm.rff.struct.DoubleMatrix;
import kr.merutilm.rff.struct.HexColor;
import kr.merutilm.rff.util.AdvancedMath;
import kr.merutilm.rff.settings.SlopeSettings;

public class Slope implements ParallelBitMapRenderer {

    private final DoubleMatrix altitudes;
    private final double reflectionRatio;
    private final double opacity;
    private final double zenith;
    private final double azimuth;
    private final double depth;
    private final double resMul;
    private final int mcto;

    public Slope(DoubleMatrix altitudes, SlopeSettings slopeSettings, double resolutionMultiplier, int multiplierCompressedToOriginal) {
        this.altitudes = altitudes; // it is original canvas. very BIG.
        this.reflectionRatio = slopeSettings.reflectionRatio();
        this.opacity = slopeSettings.opacity();
        this.azimuth = slopeSettings.azimuth();
        this.zenith = slopeSettings.zenith();
        this.depth = slopeSettings.depth();
        this.resMul = resolutionMultiplier;
        this.mcto = multiplierCompressedToOriginal;
    }

    @Override
    public HexColor execute(int x, int y, int xRes, int yRes, double rx, double ry, int i, HexColor c, double t) {
                if(c == null){
                    return null;
                }

                double aRad = toRadians(azimuth);
                double zRad = toRadians(zenith);

                int xd = x * mcto; //big image pixel coordinate from small image.
                int yd = y * mcto;

                double ld = altitudes.pipette(xd - mcto, yd + mcto);
                double d = altitudes.pipette(xd, yd + mcto);
                double rd = altitudes.pipette(xd + mcto, yd + mcto); 
                double l = altitudes.pipette(xd - mcto, yd);
                double r = altitudes.pipette(xd + mcto, yd);
                double lu = altitudes.pipette(xd - mcto, yd - mcto);
                double u = altitudes.pipette(xd, yd - mcto);
                double ru = altitudes.pipette(xd + mcto, yd - mcto);

                //As resolution increases, the relative altitude decreases, so multiply by the resMul.
                double dzDx = ((rd + 2 * r + ru) - (ld + 2 * l + lu)) * depth * resMul / mcto;
                double dzDy = ((lu + 2 * u + ru) - (ld + 2 * d + rd)) * depth * resMul / mcto;
                double slope = atan(toRadians(hypot(dzDx, dzDy)));
                double aspect = atan2(dzDy, -dzDx);
                double shade = Math.max(reflectionRatio, cos(zRad) * cos(slope) + sin(zRad) * sin(slope) * cos(aRad + aspect)); 
                double fShade = AdvancedMath.ratioDivide(1 - opacity, 1, shade);
                return c.functionExceptAlpha(e -> (int)(e * fShade));    
    }

    @Override
    public boolean isValid(){
        return opacity > 0 && reflectionRatio < 1 && depth != 0;
    }
}
