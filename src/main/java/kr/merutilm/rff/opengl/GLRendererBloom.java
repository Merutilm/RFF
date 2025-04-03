package kr.merutilm.rff.opengl;

import kr.merutilm.rff.settings.BloomSettings;

import static org.lwjgl.opengl.GL13.*;

public class GLRendererBloom extends GLRendererRequiredGaussianBlur{

    private BloomSettings bloomSettings;

    public GLRendererBloom(){
        super(new GLShaderLoader(DEFAULT_VERTEX_PATH, "bloom"), "gaussian_blur_for_bloom_single_pass", 3);
    }


    public void setBloomSettings(BloomSettings bloomSettings) {
        this.bloomSettings = bloomSettings;
        setAdditionalBlurParams(e -> {
            e.uploadTexture2D("original", GL_TEXTURE2, previousFBOTextureID);
            e.uploadFloat("radius", (float) bloomSettings.radius());
            e.uploadFloat("threshold", (float) bloomSettings.threshold());
        });
    }

    @Override
    protected void update() {
        shader.uploadTexture2D("inputTex", GL_TEXTURE0, previousFBOTextureID);
        shader.uploadTexture2D("blurred", GL_TEXTURE1, getBlurredTextureID());
        shader.upload2i("resolution", w, h);
        shader.uploadFloat("softness", (float) bloomSettings.softness());
        shader.uploadFloat("intensity", (float) bloomSettings.intensity());

    }
}
