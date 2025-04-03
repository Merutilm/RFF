package kr.merutilm.rff.ui;

import kr.merutilm.rff.opengl.*;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.lwjgl.opengl.GL.createCapabilities;

class RFFGLPanel extends AWTGLCanvas {

    protected final transient RFF master;
    protected transient GLMultiPassRenderer renderer;
    protected transient BufferedImage currentImage;
    protected transient GLRendererIteration rendererIteration;
    protected transient GLRendererStripe rendererStripe;
    protected transient GLRendererSlope rendererSlope;
    protected transient GLRendererColorFilter rendererColorFilter;
    protected transient GLRendererFog rendererFog;
    protected transient GLRendererBloom rendererBloom;
    private static final GLData DEFAULT_DATA;
    static{
        DEFAULT_DATA = new GLData();
        DEFAULT_DATA.majorVersion = 4;
        DEFAULT_DATA.minorVersion = 5;
        DEFAULT_DATA.doubleBuffer = true;
        DEFAULT_DATA.profile = GLData.Profile.CORE;
        DEFAULT_DATA.samples = 4;
    }

    protected RFFGLPanel(RFF master){
        super(DEFAULT_DATA);
        this.master = master;
        setBackground(Color.BLACK);
    }

    @Override
    public void initGL() {

        createCapabilities();

        renderer = new GLMultiPassRenderer();

        rendererIteration = new GLRendererIteration();
        rendererStripe = new GLRendererStripe();
        rendererSlope = new GLRendererSlope();
        rendererColorFilter = new GLRendererColorFilter();
        rendererFog = new GLRendererFog();
        rendererBloom = new GLRendererBloom();
        GLRendererInterpolation interpolation = new GLRendererInterpolation();


        renderer.addRenderer(rendererIteration);
        renderer.addRenderer(rendererStripe);
        renderer.addRenderer(rendererSlope);
        renderer.addRenderer(rendererColorFilter);
        renderer.addRenderer(rendererFog);
        renderer.addRenderer(rendererBloom);
        renderer.addRenderer(interpolation);
    }

    @Override
    public void paintGL() {

    }
}
