package kr.merutilm.rff.opengl;

import static org.lwjgl.opengl.GL45.GL_TEXTURE0;

public class GLRendererAntialiasing extends GLRenderer{

    private boolean use;

    public GLRendererAntialiasing() {
        super(new GLShaderLoader(DEFAULT_VERTEX_PATH, "antialiasing"));
    }

    public void setUse(boolean use) {
        this.use = use;
    }

    @Override
    protected void update() {

        shader.uploadTexture2D("inputTex", GL_TEXTURE0, previousFBOTextureID);
        shader.uploadBool("use", use);

    }
}
