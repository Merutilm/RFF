package kr.merutilm.rff.opengl;

import kr.merutilm.rff.settings.ColorSettings;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

public class GLRendererColor extends GLRenderer{

    private ColorSettings colorSettings;

    public GLRendererColor(){
        super(new GLShaderLoader(DEFAULT_VERTEX_PATH, "color"));
    }


    public void setColorFilterSettings(ColorSettings colorSettings) {
        this.colorSettings = colorSettings;
    }

    @Override
    protected void update() {
        shader.uploadTexture2D("inputTex", GL_TEXTURE0, previousFBOTextureID);
        shader.uploadFloat("gamma", (float) colorSettings.gamma());
        shader.uploadFloat("exposure", (float) colorSettings.exposure());
        shader.uploadFloat("hue", (float) colorSettings.hue());
        shader.uploadFloat("saturation", (float) colorSettings.saturation());
        shader.uploadFloat("brightness", (float) colorSettings.brightness());
        shader.uploadFloat("contrast", (float) colorSettings.contrast());
    }
}
