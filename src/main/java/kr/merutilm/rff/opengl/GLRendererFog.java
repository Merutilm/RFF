package kr.merutilm.rff.opengl;

import kr.merutilm.rff.settings.FogSettings;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;

public class GLRendererFog extends GLRendererRequiredGaussianBlur{

    private FogSettings fogSettings;

    public GLRendererFog(){
        super(new GLShader(DEFAULT_VERTEX_PATH, "fog"), "gaussian_blur_single_pass", 2);
    }


    public void setFogSettings(FogSettings fogSettings) {
        this.fogSettings = fogSettings;
        setAdditionalBlurParams(e -> {
            e.uploadFloat("radius", (float) fogSettings.radius());
        });
    }


    @Override
    protected void update() {
        shader.uploadTexture2D("inputTex", GL_TEXTURE0, previousFBOTextureID);
        shader.uploadTexture2D("blurred", GL_TEXTURE1, getBlurredTextureID());
        shader.upload2i("resolution", w, h);
        shader.uploadFloat("opacity", (float) fogSettings.opacity());
    }
}
