package kr.merutilm.rff.preset.shader.opengl;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL45.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL45.glEnableVertexAttribArray;

public class GLRendererExample implements GLRenderer{

    protected GLShader shader;

    public GLRendererExample() {
        super();
        shader = new GLShader(DEFAULT_VERTEX_PATH, "/shader/iteration_to_shaded.glsl");
    }



    @Override
    public void update() {

        shader.use();

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        shader.uploadFloat("time", (float)glfwGetTime());

        shader.draw();

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        shader.detach();
    }
}
