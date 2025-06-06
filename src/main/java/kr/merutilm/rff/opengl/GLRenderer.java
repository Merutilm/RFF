package kr.merutilm.rff.opengl;


import static org.lwjgl.opengl.GL45.*;

public abstract class GLRenderer {
    protected final GLShaderLoader shader;
    protected int w;
    protected int h;

    protected int previousFBOTextureID;


    protected GLRenderer(GLShaderLoader shader) {
        this.shader = shader;
        previousFBOTextureID = 0;
    }

    public void setPreviousFBOTextureID(int previousFBOTextureID) {
        this.previousFBOTextureID = previousFBOTextureID;
    }

    public static final String DEFAULT_VERTEX_PATH = "default_vertex";

    public GLShaderLoader getShader() {
        return shader;
    }


    public void reloadSize(int w, int h) {
        this.w = w;
        this.h = h;
        shader.resetFBOTexture(w, h);
    }



    protected abstract void update();

    protected void beforeUpdate(){
        shader.bindFBO();
    }

    protected void afterUpdate(){
        shader.unbindFBO();

        int error = glGetError();
        while (error != GL_NO_ERROR) {
            System.err.println("GL error : " + error + " at " + getClass().getSimpleName());
            error = glGetError();
        }
    }

    public final void render() {
        synchronized (shader) {
            beforeUpdate();
            shader.use();
            update();
            shader.draw();
            shader.detach();
            afterUpdate();
        }
    }

    public void destroy(){

    }
}
