package kr.merutilm.fractal.shader;


import kr.merutilm.base.io.BitMap;
import kr.merutilm.base.parallel.ShaderRenderer;
import kr.merutilm.base.struct.HexColor;
import kr.merutilm.fractal.settings.FogSettings;

public class Fog implements ShaderRenderer {

    private final BitMap blurredBitMap;
    private final double fitResolutionMultiplier;
    private final double radius;
    private final double opacity;

    public Fog(BitMap bitMap, BitMap compressedBitMap, FogSettings settings){
        this.opacity = settings.opacity();
        this.radius = settings.radius();

        if(isValid()){
            BitMap blurredBitMap = compressedBitMap.cloneCanvas();
            blurredBitMap.gaussianBlur((int)(radius * compressedBitMap.getWidth()));
            this.blurredBitMap = blurredBitMap;
        }else{
            this.blurredBitMap = null;
        }

        this.fitResolutionMultiplier = (double) compressedBitMap.getWidth() / bitMap.getWidth();

        
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