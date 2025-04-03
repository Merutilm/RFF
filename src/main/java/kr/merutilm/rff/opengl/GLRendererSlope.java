package kr.merutilm.rff.opengl;

import kr.merutilm.rff.settings.SlopeSettings;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;

public class GLRendererSlope extends GLRenderer implements GLIterationTextureRenderer{

    private int iterationTextureID;
    private SlopeSettings slopeSettings;

    public GLRendererSlope(){
        super(new GLShaderLoader(DEFAULT_VERTEX_PATH, "slope"));
    }

    @Override
    public void setIterationTextureID(int iterationTextureID) {
        this.iterationTextureID = iterationTextureID;
    }

    public void setSlopeSettings(SlopeSettings slopeSettings) {
        this.slopeSettings = slopeSettings;
    }

    @Override
    protected void update() {
        shader.uploadTexture2D("inputTex", GL_TEXTURE0, previousFBOTextureID);
        shader.uploadTexture2D("iterations", GL_TEXTURE1, iterationTextureID);
        shader.upload2i("resolution", w, h);
        shader.uploadFloat("depth", (float) slopeSettings.depth());
        shader.uploadFloat("reflectionRatio", (float) slopeSettings.reflectionRatio());
        shader.uploadFloat("opacity", (float) slopeSettings.opacity());
        shader.uploadFloat("zenith", (float) slopeSettings.zenith());
        shader.uploadFloat("azimuth", (float) slopeSettings.azimuth());
    }
}
