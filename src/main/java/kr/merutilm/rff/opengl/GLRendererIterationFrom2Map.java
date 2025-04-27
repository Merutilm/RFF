package kr.merutilm.rff.opengl;

import kr.merutilm.rff.settings.DataSettings;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL46.*;

public class GLRendererIterationFrom2Map extends GLRenderer implements GLIterationTextureProvider{

    private int iterWidth;
    private int iterHeight;
    private double maxIteration;
    private DataSettings dataSettings;
    private float currentFrame;
    private int iterationTextureID;
    private FloatBuffer iterationBuffer;


    public GLRendererIterationFrom2Map() {
        super(new GLShaderLoader(GLRenderer.DEFAULT_VERTEX_PATH, "iteration_from_2_map"));
    }

    public void reloadIterationBuffer(int iterWidth, int iterHeight, long maxIteration) {
        this.iterationBuffer = emptyIterationBuffer(iterWidth, iterHeight);
        this.iterationTextureID = shader.recreateTexture2D(iterationTextureID, iterWidth, iterHeight, GLShaderLoader.TextureFormat.FLOAT4, false);
        this.iterWidth = iterWidth;
        this.iterHeight = iterHeight;
        this.maxIteration = maxIteration;
    }

    public void setCurrentFrame(float currentFrame) {
        this.currentFrame = currentFrame;
    }

    @Override
    public int getIterationTextureID() {
        return shader.getFboTextureID();
    }

    @Override
    public float getResolutionMultiplier() {
        return (float) iterWidth / w;
    }

    public void setDataSettings(DataSettings dataSettings) {
        this.dataSettings = dataSettings;
    }

    @Override
    protected void update() {

        iterationBuffer.flip();
        shader.upload2i("resolution", w, h);
        shader.uploadDouble("maxIteration", maxIteration);
        shader.uploadFloat("resolutionMultiplier", getResolutionMultiplier());
        shader.uploadTexture2D("normalAndZoomed", GL_TEXTURE0, iterationTextureID, iterationBuffer, iterWidth, iterHeight, GLShaderLoader.TextureFormat.FLOAT4);
        shader.uploadFloat("defaultZoomIncrement", (float) dataSettings.defaultZoomIncrement());
        shader.uploadFloat("currentFrame", currentFrame);
    }

    private static float[] doubleToTwoIntBits(double normal, double zoomed) {
        long na = Double.doubleToLongBits(normal);
        int nHigh = (int) (na >>> 32);
        int nLow = (int) (na & 0xffffffffL);
        long za = Double.doubleToLongBits(zoomed);
        int zHigh = (int) (za >>> 32);
        int zLow = (int) (za & 0xffffffffL);

        return new float[]{Float.intBitsToFloat(nHigh), Float.intBitsToFloat(nLow), Float.intBitsToFloat(zHigh), Float.intBitsToFloat(zLow)};
    }

    private static FloatBuffer emptyIterationBuffer(int w, int h) {
        return BufferUtils.createFloatBuffer(w * h * 4);
    }

    public void setAllIterations(double[] normal, double[] zoomed) {
        for (int i = 0; i < iterWidth * iterHeight; i++) {
            iterationBuffer.put(doubleToTwoIntBits(normal[i], zoomed[i]));
        }
        iterationBuffer.flip();
    }



    @Override
    public void destroy() {
        super.destroy();
        glDeleteTextures(iterationTextureID);
    }

}
