package kr.merutilm.rff.parallel;


import kr.merutilm.rff.io.BitMap;
import kr.merutilm.rff.struct.HexColor;
import kr.merutilm.rff.settings.FogSettings;

public class Fog implements ParallelBitMapRenderer {

    private final BitMap blurredBitMap;
    private final double fitResolutionMultiplier;
    private final double radius;
    private final double opacity;

    public Fog(BitMap bitMap, BitMap gaussBitMap, FogSettings settings){
        this.opacity = settings.opacity();
        this.radius = settings.radius();

        if(isValid()){
            BitMap blurredBitMap = gaussBitMap.cloneCanvas();
            blurredBitMap.gaussianBlur((int)(radius * gaussBitMap.getWidth()));
            this.blurredBitMap = blurredBitMap;
        }else{
            this.blurredBitMap = null;
        }

        this.fitResolutionMultiplier = (double) gaussBitMap.getWidth() / bitMap.getWidth();

        
    }

    @Override
    public HexColor execute(int x, int y, int xRes, int yRes, double rx, double ry, int i, HexColor c, double t) {
        if (c == null){
            return null;
        }
        HexColor bb = HexColor.fromInteger(blurredBitMap.pipetteAdvanced(x * fitResolutionMultiplier, y * fitResolutionMultiplier));
        HexColor cf = c.blend(HexColor.ColorBlendMode.NORMAL, bb, opacity);


        return c.grayScaleValue() > cf.grayScaleValue() ? c : cf;
                
    }
    

    @Override
    public boolean isValid(){
        return opacity > 0 && radius > 0;
    }
}
