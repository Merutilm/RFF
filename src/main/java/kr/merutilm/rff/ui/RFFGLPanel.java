package kr.merutilm.rff.ui;

import kr.merutilm.rff.io.RFFMap;
import kr.merutilm.rff.opengl.*;
import kr.merutilm.rff.settings.Settings;
import kr.merutilm.rff.struct.DoubleMatrix;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;

abstract class RFFGLPanel extends AWTGLCanvas {

    protected transient BufferedImage currentImage;
    private static final GLData DEFAULT_DATA;
    public static final ReentrantLock LOCKER = new ReentrantLock();
    static{
        DEFAULT_DATA = new GLData();
        DEFAULT_DATA.majorVersion = 4;
        DEFAULT_DATA.minorVersion = 5;
        DEFAULT_DATA.doubleBuffer = true;
        DEFAULT_DATA.profile = GLData.Profile.CORE;
        DEFAULT_DATA.samples = 4;
    }

    protected RFFGLPanel(){
        super(DEFAULT_DATA);
        setBackground(Color.BLACK);
    }


    public void makeCurrent(){
        platformCanvas.makeCurrent(context);
    }


    public void init(){

        beforeRender();
        if(!initCalled){
            initGL();
            initCalled = true;
        }
    }


    public abstract void applyCurrentMap();

    public abstract void applyColor(Settings settings);
}
