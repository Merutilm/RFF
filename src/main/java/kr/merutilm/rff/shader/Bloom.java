package kr.merutilm.rff.shader;

import java.util.Arrays;

import kr.merutilm.rff.io.BitMap;
import kr.merutilm.rff.struct.HexColor;
import kr.merutilm.rff.util.AdvancedMath;
import kr.merutilm.rff.settings.BloomSettings;

public class Bloom implements BitMapRenderer {


    private final BitMap blurredAvailableBitMap;
    private final double threshold;
    private final double radius;
    private final double intensity;
    private final double softness;
    private final double fitResolutionMultiplier;

    public Bloom(BitMap bitMap, BitMap compressedBitMap, BloomSettings settings) {


        this.threshold = settings.threshold();
        this.radius = settings.radius();
        this.intensity = settings.intensity();
        this.softness = settings.softness();
        if (isValid()) {
            BitMap blurredAvailableBitMap = compressedBitMap.createAnother(Arrays.stream(compressedBitMap.getCanvas())
                    .map(e -> HexColor.grayScaleValue(e) < threshold * 256 ? (255 << 24) : e)
                    .toArray());

            blurredAvailableBitMap.gaussianBlur((int) (radius * compressedBitMap.getWidth()));

            this.blurredAvailableBitMap = blurredAvailableBitMap;
        }else{
            this.blurredAvailableBitMap = null;
        }
        this.fitResolutionMultiplier = (double) compressedBitMap.getWidth() / bitMap.getWidth();

    }

    @Override
    public HexColor execute(int x, int y, int xRes, int yRes, double rx, double ry, int i, HexColor c, double t) {
        if (c == null) {
            return null;
        }
        HexColor bb = HexColor.fromInteger(blurredAvailableBitMap.pipetteAdvanced(x * fitResolutionMultiplier, y * fitResolutionMultiplier));
        return c.functionExceptAlpha(HexColor.ratioDivide(bb, c, Math.max(0, softness)), (e, ta) -> (int) AdvancedMath.restrict(0, 255, e + ta * intensity));
    }

    @Override
    public boolean isValid() {
        return threshold < 1 && softness < 1 && intensity != 0 && radius >= 0;
    }

}
