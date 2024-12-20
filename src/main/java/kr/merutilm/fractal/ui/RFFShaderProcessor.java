package kr.merutilm.fractal.ui;


import java.awt.image.BufferedImage;
import java.util.Arrays;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.io.BitMap;
import kr.merutilm.base.parallel.ProcessVisualizer;
import kr.merutilm.base.parallel.RenderState;
import kr.merutilm.base.parallel.ShaderDispatcher;
import kr.merutilm.base.struct.DoubleMatrix;
import kr.merutilm.base.struct.HexColor;
import kr.merutilm.fractal.io.RFFMap;
import kr.merutilm.fractal.settings.ColorSettings;
import kr.merutilm.fractal.settings.ImageSettings;
import kr.merutilm.fractal.settings.Settings;
import kr.merutilm.fractal.settings.ShaderSettings;
import kr.merutilm.fractal.settings.StripeSettings;
import kr.merutilm.fractal.shader.Bloom;
import kr.merutilm.fractal.shader.ColorFilter;
import kr.merutilm.fractal.shader.Fog;
import kr.merutilm.fractal.shader.Slope;

final class RFFShaderProcessor {
    private RFFShaderProcessor(){
        
    }
    
    public static final int NOT_RENDERED = -1;
    private static final double COMPRESSION_CRITERIA = 0.5;

    public static void fillInit(DoubleMatrix iterations){
        Arrays.fill(iterations.getCanvas(), NOT_RENDERED);
    }

    public static BufferedImage createImage(RenderState state, int currentID, RFFMap map, Settings settings, boolean compressed) throws IllegalRenderStateException, InterruptedException{
        ProcessVisualizer na = ProcessVisualizer.na();
        return createImageWithVisualizer(state, currentID, map, settings, compressed, new ProcessVisualizer[]{na, na, na});
    }
    public static BufferedImage createImageWithVisualizer(RenderState state, int currentID, RFFMap map, Settings settings, boolean compressed, ProcessVisualizer[] visualizers) throws IllegalRenderStateException, InterruptedException{
    
        DoubleMatrix iterations = map.iterations();
        settings = map.modifyToMapSettings(settings);
        int compressDivisor = getCompressDivisor(settings.imageSettings());
        int currentDivisor = compressed ? compressDivisor : 1;

        BitMap bitMap = new BitMap(iterations.getWidth() / currentDivisor, iterations.getHeight() / currentDivisor);

        basicShaders(state, currentID, bitMap, iterations, settings, compressed, visualizers);
        postProcessing(state, currentID, bitMap, iterations, settings, compressed, visualizers);

        return bitMap.getImage();
    }

    private static HexColor getColorByIteration(Settings settings, double iteration) {

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
        
        double m = ((value - sof) % si1) * ((value - sof) % si2) / (si1 * si2);
        
        return HexColor.ratioDivide(c1, HexColor.BLACK, m * stp.opacity());
    }
    

    public static int getCompressDivisor(ImageSettings img) {
        return Math.max(1, (int) ((int) img.resolutionMultiplier() / COMPRESSION_CRITERIA));
    }

    private static void basicShaders(RenderState state, int currentID, BitMap bitMap, DoubleMatrix iterations, Settings settings, boolean compressed, ProcessVisualizer... visualizers) throws IllegalRenderStateException, InterruptedException {
        ImageSettings img = settings.imageSettings();
        ShaderSettings shd = settings.shaderSettings();
        ShaderDispatcher pp1 = new ShaderDispatcher(state, currentID, bitMap);
        int fitResolutionMultiplier = iterations.getWidth() / bitMap.getWidth();

        pp1.createRenderer((x, y, xRes, yRes, rx, ry, i, c, t) -> {
            int ix = x * fitResolutionMultiplier;
            int iy = y * fitResolutionMultiplier;
            return getColorByIteration(settings, iterations.pipette(ix, iy));
        });
        pp1.createRenderer(new Slope(iterations, shd.slopeSettings(), img.resolutionMultiplier(), fitResolutionMultiplier));
        pp1.createRenderer(new ColorFilter(shd.colorFilterSettings()));


        if (compressed) {
            pp1.dispatch();
        } else {
            pp1.process(visualizers[0], 400);
        }
    }

    private static void postProcessing(RenderState state, int currentID, BitMap bitMap, DoubleMatrix iterations, Settings settings, boolean compressed, ProcessVisualizer... visualizers) throws IllegalRenderStateException, InterruptedException {

        ImageSettings img = settings.imageSettings();
        ShaderSettings shd = settings.shaderSettings();
        int compressDivisor = getCompressDivisor(img);

        BitMap compressedBitMap = compressDivisor > 1 ? new BitMap(iterations.getWidth() / compressDivisor, iterations.getHeight() / compressDivisor) : bitMap;
        if (compressDivisor > 1) {
            basicShaders(state, currentID, compressedBitMap, iterations, settings, true);
        }

        ShaderDispatcher pp2 = new ShaderDispatcher(state, currentID, bitMap);
        pp2.createRenderer(new Fog(bitMap, compressedBitMap, shd.fogSettings()));


        if (compressed) {
            pp2.dispatch();
        } else {
            pp2.process(visualizers[1], 400);
        }

        ShaderDispatcher pp3 = new ShaderDispatcher(state, currentID, bitMap);

        pp3.createRenderer(new Bloom(bitMap, compressedBitMap, shd.bloomSettings()));
        pp3.createRenderer((x, y, xRes, yRes, rx, ry, i, c, t) -> {
            HexColor a1 = pp3.texture2D(x, y + 1);
            HexColor a2 = pp3.texture2D(x, y - 1);
            HexColor a3 = pp3.texture2D(x + 1, y);
            HexColor a4 = pp3.texture2D(x - 1, y);
            return HexColor.average(a1, a2, a3, a4, c);
        });



        if (compressed) {
            pp3.dispatch();
        } else {
            pp3.process(visualizers[2], 400);
        }
    }
}
