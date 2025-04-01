package kr.merutilm.rff.opengl;

import java.util.function.Consumer;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

public class GLRendererGaussianBlurSinglePass extends GLRenderer {


    private Consumer<GLShader> additionalParams = _ -> {};

    public GLRendererGaussianBlurSinglePass(String name){
        super(new GLShader(DEFAULT_VERTEX_PATH, name));
    }

    public void setAdditionalParams(Consumer<GLShader> additionalParams) {
        this.additionalParams = additionalParams;
    }

    @Override
    protected void update() {
        shader.uploadTexture2D("inputTex", GL_TEXTURE0, previousFBOTextureID);
        shader.upload2i("resolution", w, h);
        additionalParams.accept(shader);
    }
}
