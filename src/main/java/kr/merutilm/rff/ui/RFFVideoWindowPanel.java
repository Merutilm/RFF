package kr.merutilm.rff.ui;


import kr.merutilm.rff.io.BitMap;
import kr.merutilm.rff.io.RFFMap;
import kr.merutilm.rff.opengl.*;
import kr.merutilm.rff.settings.Settings;
import kr.merutilm.rff.struct.DoubleMatrix;
import kr.merutilm.rff.util.AdvancedMath;
import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;

class RFFVideoWindowPanel extends RFFGLPanel {

    private transient BufferedImage image;
    private transient RFFMap normal;
    private transient RFFMap zoomed;
    private transient float currentFrame;
    private transient GLMultiPassRenderer renderer;
    private transient GLRendererIterationPaletteFrom2Map rendererIterationFrom2Map;
    private transient GLRendererIterationPalette rendererIteration;
    private transient GLRendererStripe rendererStripe;
    private transient GLRendererSlope rendererSlope;
    private transient GLRendererColor rendererColorFilter;
    private transient GLRendererFog rendererFog;
    private transient GLRendererBloom rendererBloom;
    private transient GLRendererAntialiasing rendererAntialiasing;

    public RFFVideoWindowPanel() {
        super();
    }

    public BufferedImage getImage() {
        return image;
    }


    @Override
    public void applyCurrentMap(){

        DoubleMatrix normalI = normal.iterations();
        rendererIterationFrom2Map.setCurrentFrame(currentFrame);
        if(currentFrame < 1){
            long maxIteration = normal.maxIteration();
            rendererIterationFrom2Map.reloadIterationBuffer(normalI.getWidth(), normalI.getHeight(), maxIteration);
            rendererIterationFrom2Map.setAllIterations(normalI.getCanvas(), new double[normalI.getLength()]);
            rendererIteration.setMaxIteration(maxIteration);
        }else{
            DoubleMatrix zoomedI = zoomed.iterations();
            long maxIteration = Math.min(zoomed.maxIteration(), normal.maxIteration());
            rendererIterationFrom2Map.reloadIterationBuffer(normalI.getWidth(), normalI.getHeight(), maxIteration);
            rendererIterationFrom2Map.setAllIterations(normalI.getCanvas(), zoomedI.getCanvas());
            rendererIteration.setMaxIteration(maxIteration);
        }

    }

    @Override
    public void applyColor(Settings settings){
        rendererIterationFrom2Map.setDataSettings(settings.videoSettings().dataSettings());
        rendererIteration.setPaletteSettings(settings.shaderSettings().paletteSettings());
        rendererStripe.setStripeSettings(settings.shaderSettings().stripeSettings());
        rendererSlope.setSlopeSettings(settings.shaderSettings().slopeSettings());
        rendererColorFilter.setColorFilterSettings(settings.shaderSettings().colorSettings());
        rendererFog.setFogSettings(settings.shaderSettings().fogSettings());
        rendererBloom.setBloomSettings(settings.shaderSettings().bloomSettings());
        rendererAntialiasing.setUse(settings.renderSettings().antialiasing());
    }

    @Override
    public void initGL() {
        createCapabilities();

        renderer = new GLMultiPassRenderer();

        rendererIterationFrom2Map = new GLRendererIterationPaletteFrom2Map();
        rendererIteration = new GLRendererIterationPalette();
        rendererStripe = new GLRendererStripe();
        rendererSlope = new GLRendererSlope();
        rendererColorFilter = new GLRendererColor();
        rendererFog = new GLRendererFog();
        rendererBloom = new GLRendererBloom();
        rendererAntialiasing = new GLRendererAntialiasing();

        renderer.addRenderer(rendererIterationFrom2Map);
        renderer.addRenderer(rendererIteration);
        renderer.addRenderer(rendererStripe);
        renderer.addRenderer(rendererSlope);
        renderer.addRenderer(rendererColorFilter);
        renderer.addRenderer(rendererFog);
        renderer.addRenderer(rendererBloom);
        renderer.addRenderer(rendererAntialiasing);

    }

    public double calculateZoom(double defaultZoomIncrement){

        if (currentFrame < 1) {

            double r = 1 - currentFrame;

            if (normal == null) {
                return 0;
            }

            double z1 = normal.zoom();
            return AdvancedMath.ratioDivide(z1, z1 + Math.log10(defaultZoomIncrement), r);

        } else {
            int f1 = (int) currentFrame; // it is smaller
            int f2 = f1 + 1;
            //frame size : f1 = 1x, f2 = 2x
            double r = f2 - currentFrame;

            if(zoomed == null || normal == null){
                return 0;
            }

            double z1 = zoomed.zoom();
            double z2 = normal.zoom();
            return AdvancedMath.ratioDivide(z2, z1, r);
        }
    }

    public GLMultiPassRenderer getRenderer() {
        return renderer;
    }

    public void setMap(float currentFrame, RFFMap normal, RFFMap zoomed){
        this.normal = normal;
        this.zoomed = zoomed;
        this.currentFrame = currentFrame;
    }

    @Override
    public void paintGL() {
        glClear(GL_COLOR_BUFFER_BIT);
        renderer.update();
        swapBuffers();
        int w = normal.iterations().getWidth();
        int h = normal.iterations().getHeight();
        ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * 4);
        glReadPixels(0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        image = BitMap.getImage(w, h, buffer);
    }
}
