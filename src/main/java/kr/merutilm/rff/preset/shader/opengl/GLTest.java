package kr.merutilm.rff.preset.shader.opengl;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import javax.swing.*;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.opengl.GL.createCapabilities;

public class GLTest extends JFrame {

    public GLTest() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(500, 500));
        setVisible(true);
        pack();
        transferFocus();

    }

    public static void main(String[] args) {
        GLData data = new GLData();
        AWTGLCanvas canvas = new AWTGLCanvas(data) {
            private transient GLRenderer shader;
            @Override
            public void initGL() {
                createCapabilities();
                glClearColor(0.3f, 0.4f, 0.5f, 1);
                glfwInit();
                shader = new GLRendererExample();
            }

            @Override
            public void paintGL() {
                int w = getWidth();
                int h = getHeight();
                glClear(GL_COLOR_BUFFER_BIT);
                shader.update();
                glEnd();
                swapBuffers();
            }
        };
        GLTest wnd = new GLTest();
        wnd.add(canvas);

        Runnable renderLoop = new Runnable() {
            @Override
            public void run() {
                if (!canvas.isValid()) {
                    GL.setCapabilities(null);
                    return;
                }
                canvas.render();
                SwingUtilities.invokeLater(this);
            }
        };
        SwingUtilities.invokeLater(renderLoop);
    }


}
