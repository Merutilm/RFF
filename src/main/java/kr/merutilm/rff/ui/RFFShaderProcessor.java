package kr.merutilm.rff.ui;


import java.awt.image.BufferedImage;
import java.util.Arrays;

import kr.merutilm.rff.io.BitMap;
import kr.merutilm.rff.struct.DoubleMatrix;
import kr.merutilm.rff.struct.HexColor;
import kr.merutilm.rff.io.RFFMap;
import kr.merutilm.rff.parallel.Bloom;
import kr.merutilm.rff.parallel.ColorFilter;
import kr.merutilm.rff.parallel.Fog;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelBitMapDispatcher;
import kr.merutilm.rff.parallel.ParallelRenderProcessVisualizer;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.parallel.Slope;
import kr.merutilm.rff.settings.ColorSettings;
import kr.merutilm.rff.settings.ImageSettings;
import kr.merutilm.rff.settings.Settings;
import kr.merutilm.rff.settings.ShaderSettings;
import kr.merutilm.rff.settings.StripeSettings;

final class RFFShaderProcessor {
    private RFFShaderProcessor(){
        
    }
    
    public static final int NOT_RENDERED = -1;
    private static final double COMPRESSION_CRITERIA = 0.5;

    public static void fillInit(DoubleMatrix iterations){
        Arrays.fill(iterations.getCanvas(), NOT_RENDERED);
    }

    public static BufferedImage createImage(ParallelRenderState state, int currentID, RFFMap map, Settings settings, boolean compressed) throws IllegalParallelRenderStateException, InterruptedException{
        return createImage(state, currentID, map, settings, compressed, new ParallelRenderProcessVisualizer[]{null, null});
    }
    public static BufferedImage createImage(ParallelRenderState state, int currentID, RFFMap map, Settings settings, boolean compressed, ParallelRenderProcessVisualizer[] visualizers) throws IllegalParallelRenderStateException, InterruptedException{
    
        DoubleMatrix iterations = map.iterations();
        settings = map.modifyToMapSettings(settings);
        int compressDivisor = getImageCompressDivisor(settings.imageSettings());
        int currentDivisor = compressed ? compressDivisor : 1;
        //compressDivisor : divisor to make compressed image
        //currentDivisor : current divisor to create image 

        BitMap bitMap = new BitMap(iterations.getWidth() / currentDivisor, iterations.getHeight() / currentDivisor);
        shade(state, currentID, bitMap, iterations, settings, visualizers);

        return bitMap.getImage();
    }

    private static HexColor iterationToColor(Settings settings, double iteration) {

        if (iteration >= settings.calculationSettings().maxIteration()) {
            return HexColor.BLACK;
        }
        if (iteration == NOT_RENDERED) {
            return HexColor.BLACK;
        }
        ColorSettings col = settings.shaderSettings().colorSettings();
        StripeSettings stp = settings.shaderSettings().stripeSettings();
       
        double value = switch (col.colorSmoothing()) {
            case NONE -> (long) iteration;
            case REVERSED -> (long) iteration + 1 - (iteration % 1);
            default -> iteration;
        };

        HexColor c1 = col.getColor(value);

        if(!stp.use()){
            return c1;
        }

        double si1 = stp.firstInterval();
        double si2 = stp.secondInterval();
        double sof = stp.offset();
        
        double m = (((value - sof) % si1 + si1) % si1) * (((value - sof) % si2 + si2) % si2) / (si1 * si2);
        
        return HexColor.ratioDivide(c1, HexColor.BLACK, m * stp.opacity());
    }


    public static int getImageCompressDivisor(ImageSettings img) {
        return Math.max(1, (int) ((int) img.resolutionMultiplier() / COMPRESSION_CRITERIA));
    }

    private static void shade(ParallelRenderState state, int currentID, BitMap bitMap, DoubleMatrix iterations, Settings settings, ParallelRenderProcessVisualizer... visualizers) throws IllegalParallelRenderStateException, InterruptedException {
        
        ImageSettings img = settings.imageSettings();
        ShaderSettings shd = settings.shaderSettings();
        ParallelBitMapDispatcher pp1 = new ParallelBitMapDispatcher(state, currentID, bitMap);
        //if compressed, it is bigger than 1.
        int multiplierCurrentToOriginal = iterations.getWidth() / bitMap.getWidth(); 
        int multiplierCompressedToOriginal = getImageCompressDivisor(img);

        pp1.createRenderer((x, y, _, _, _, _, _, _, _) -> {
            int ix = x * multiplierCurrentToOriginal;
            int iy = y * multiplierCurrentToOriginal;
            return iterationToColor(settings, iterations.pipette(ix, iy));
        });
        pp1.createRenderer(new Slope(iterations, shd.slopeSettings(), img.resolutionMultiplier(), multiplierCurrentToOriginal));
        pp1.createRenderer(new ColorFilter(shd.colorFilterSettings()));


        if (multiplierCurrentToOriginal > 1) {
            pp1.dispatch();
        } else {
            pp1.process(visualizers[0], 400);
        }

        BitMap gaussBitMap = multiplierCompressedToOriginal > 1 ? new BitMap(iterations.getWidth() / multiplierCompressedToOriginal, iterations.getHeight() / multiplierCompressedToOriginal) : bitMap;
        
        //Creates canvas for bloom, fog, and any shaders which requires gaussian-blur.
        if (multiplierCompressedToOriginal > 1) { 
            ParallelBitMapDispatcher pp1g = new ParallelBitMapDispatcher(state, currentID, gaussBitMap);

            pp1g.createRenderer((x, y, _, _, _, _, _, _, _) -> {
                int ix = x * multiplierCompressedToOriginal;
                int iy = y * multiplierCompressedToOriginal;
                return iterationToColor(settings, iterations.pipette(ix, iy));
            });
            pp1g.createRenderer(new Slope(iterations, shd.slopeSettings(), img.resolutionMultiplier(), multiplierCompressedToOriginal));
            pp1g.createRenderer(new ColorFilter(shd.colorFilterSettings()));
            pp1g.dispatch();
            //Always-compressed image. it not requires visualizer because it is fast
        }
        //You got the BitMap for gaussian blur!!

        ParallelBitMapDispatcher pp2 = new ParallelBitMapDispatcher(state, currentID, bitMap);
        pp2.createRenderer(new Fog(bitMap, gaussBitMap, shd.fogSettings()));
        pp2.createRenderer(new Bloom(bitMap, gaussBitMap, shd.bloomSettings()));
        pp2.createRenderer((x, y, _, _, _, _, _, c, _) -> {
            HexColor a1 = pp2.texture2D(x, y + 1);
            HexColor a2 = pp2.texture2D(x, y - 1);
            HexColor a3 = pp2.texture2D(x + 1, y);
            HexColor a4 = pp2.texture2D(x - 1, y);
            return HexColor.average(a1, a2, a3, a4, c);
        });

        if (multiplierCurrentToOriginal > 1) {
            pp2.dispatch();
        } else {
            pp2.process(visualizers[1], 400);
        }

    }
}
