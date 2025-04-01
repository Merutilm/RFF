package kr.merutilm.rff.opengl;

import static org.lwjgl.opengl.GL45.GL_TEXTURE0;

public class GLRendererInterpolation extends GLRenderer{

    public GLRendererInterpolation() {
        super(new GLShader(DEFAULT_VERTEX_PATH, "interpolation"));
    }

    @Override
    protected void update() {

        shader.uploadTexture2D("inputTex", GL_TEXTURE0, previousFBOTextureID);
        shader.upload2i("resolution", w, h);

    }
}
