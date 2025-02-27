package kr.merutilm.rff.ui;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL45.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL45.glEnableVertexAttribArray;

import org.lwjgl.opengl.awt.AWTGLCanvas;

import kr.merutilm.rff.formula.LightMandelbrotPerturbator;
import kr.merutilm.rff.formula.MandelbrotPerturbator;

public class GLLightMandelbrotShader implements GLRenderer{
    protected GLShader shader;
    private final AWTGLCanvas target;
    private final LightMandelbrotPerturbator perturbator;

    public GLLightMandelbrotShader(AWTGLCanvas target, LightMandelbrotPerturbator perturbator) {
        super();
        this.target = target;
        this.perturbator = perturbator;
        shader = new GLShader(DEFAULT_VERTEX_PATH, "/shader/mandelbrot.glsl");
    }

    

    @Override
    public void update() {

        shader.use();

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        // shader.uploadDouble("x", x);
        // shader.uploadDouble("y", y);
        // shader.uploadDouble("zoom", zoom);
        // shader.uploadLong("maxIteration", perturbator.getCalc().maxIteration());
        // shader.uploadDouble("time", glfwGetTime());
        // shader.upload2f("resolution", target.getWidth(), target.getHeight());

        shader.draw();

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        shader.detach();
    }
} 
