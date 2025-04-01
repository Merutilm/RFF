package kr.merutilm.rff.opengl;

import kr.merutilm.rff.settings.ColorFilterSettings;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

public class GLRendererColorFilter extends GLRenderer{

    private ColorFilterSettings colorFilterSettings;

    public GLRendererColorFilter(){
        super(new GLShader(DEFAULT_VERTEX_PATH, "color_filter"));
    }


    public void setColorFilterSettings(ColorFilterSettings colorFilterSettings) {
        this.colorFilterSettings = colorFilterSettings;
    }

    @Override
    protected void update() {
        shader.uploadTexture2D("inputTex", GL_TEXTURE0, previousFBOTextureID);
        shader.upload2i("resolution", w, h);
        shader.uploadFloat("gamma", (float) colorFilterSettings.gamma());
        shader.uploadFloat("exposure", (float) colorFilterSettings.exposure());
        shader.uploadFloat("saturation", (float) colorFilterSettings.saturation());
        shader.uploadFloat("brightness", (float) colorFilterSettings.brightness());
        shader.uploadFloat("contrast", (float) colorFilterSettings.contrast());
    }
}
