package kr.merutilm.rff.opengl;

import kr.merutilm.rff.settings.ColorSettings;
import kr.merutilm.rff.struct.HexColor;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL46.*;

public class GLRendererIteration extends GLRenderer implements GLIterationTextureProvider, GLTimeRenderer{

    private ColorSettings colorSettings;
    private int iterWidth;
    private int iterHeight;
    private double maxIteration;

    private int paletteTextureID;
    private int paletteWidth;
    private int paletteHeight;
    private int paletteLength;
    private FloatBuffer paletteBuffer;
    private float time;

    private int iterationTextureID;
    private FloatBuffer iterationBuffer;


    public GLRendererIteration() {
        super(new GLShaderLoader(GLRenderer.DEFAULT_VERTEX_PATH, "iteration"));
    }


    public void setIteration(int x, int y, int w, double iteration) {
        synchronized (shader) {
            iterationBuffer.clear();
            iterationBuffer.put((y * w + x) * 2, doubleToTwoIntBits(iteration));
            iterationBuffer.flip();
        }
    }
    @Override
    public void setTime(float time) {
        this.time = time;
    }

    public void setMaxIteration(long maxIteration) {
        this.maxIteration = maxIteration;
    }

    public void reloadIterationBuffer(int iterWidth, int iterHeight, long maxIteration) {
        this.iterationBuffer = emptyIterationBuffer(iterWidth, iterHeight);
        this.iterationTextureID = shader.recreateTexture2D(iterationTextureID, iterWidth, iterHeight, GLShaderLoader.TextureFormat.FLOAT2, false);
        this.iterWidth = iterWidth;
        this.iterHeight = iterHeight;
        this.maxIteration = maxIteration;
    }


    public void setColorSettings(ColorSettings settings) {

        this.colorSettings = settings;
        this.paletteLength = colorSettings.colors().length;
        this.paletteWidth = Math.min(glGetInteger(GL_MAX_TEXTURE_SIZE), paletteLength);
        this.paletteHeight = (paletteLength - 1) / paletteWidth + 1;
        this.paletteBuffer = createPaletteBuffer(colorSettings, paletteWidth, paletteHeight);
        this.paletteTextureID = shader.recreateTexture2D(paletteTextureID, paletteWidth, paletteHeight, GLShaderLoader.TextureFormat.FLOAT4, true);
    }

    @Override
    public int getIterationTextureID() {
        return iterationTextureID;
    }

    @Override
    public float getResolutionMultiplier() {
        return (float) iterWidth / w;
    }

    @Override
    protected void update() {

        if (paletteBuffer == null) {
            return;
        }

        paletteBuffer.flip();

        if(previousFBOTextureID == 0){
            if(iterationBuffer == null){
                return;
            }
            iterationBuffer.flip();
            shader.uploadTexture2D("iterations", GL_TEXTURE0, iterationTextureID, iterationBuffer, iterWidth, iterHeight, GLShaderLoader.TextureFormat.FLOAT2);
            shader.uploadFloat("resolutionMultiplier", getResolutionMultiplier());
        }else{
            shader.uploadTexture2D("iterations", GL_TEXTURE0, previousFBOTextureID);
            shader.uploadFloat("resolutionMultiplier", 1);
        }

        shader.uploadDouble("maxIteration", maxIteration);

        shader.uploadTexture2D("palette",  GL_TEXTURE1, paletteTextureID, paletteBuffer, paletteWidth, paletteHeight, GLShaderLoader.TextureFormat.FLOAT4);
        shader.uploadInt("paletteWidth", paletteWidth);
        shader.uploadInt("paletteHeight", paletteHeight);
        shader.uploadInt("paletteLength", paletteLength);
        shader.uploadFloat("paletteOffset", (float) (colorSettings.offsetRatio() - time * colorSettings.animationSpeed() / colorSettings.iterationInterval()));
        shader.uploadFloat("paletteInterval", (float) colorSettings.iterationInterval());
        shader.uploadInt("smoothing", colorSettings.colorSmoothing().ordinal());
    }

    private static float[] doubleToTwoIntBits(double v) {
        long a = Double.doubleToLongBits(v);
        int high = (int) (a >>> 32);
        int low = (int) (a & 0xffffffffL);

        return new float[]{Float.intBitsToFloat(high), Float.intBitsToFloat(low)};
    }

    private static FloatBuffer emptyIterationBuffer(int w, int h) {
        return BufferUtils.createFloatBuffer(w * h * 2);
    }

    public void setAllIterations(double[] iterations) {
        for (int i = 0; i < iterWidth * iterHeight; i++) {
            iterationBuffer.put(doubleToTwoIntBits(iterations[i]));
        }
        iterationBuffer.flip();
    }

    private static FloatBuffer createPaletteBuffer(ColorSettings colorSettings, int paletteWidth, int paletteHeight) {
        HexColor[] colors = colorSettings.colors();

        FloatBuffer buffer = BufferUtils.createFloatBuffer(paletteWidth * paletteHeight * 4);
        for (HexColor color : colors) {
            buffer.put((float) color.r() / HexColor.MAX);
            buffer.put((float) color.g() / HexColor.MAX);
            buffer.put((float) color.b() / HexColor.MAX);
            buffer.put((float) color.a() / HexColor.MAX);
        }
        buffer.flip();
        return buffer;
    }

    @Override
    public void destroy() {
        super.destroy();
        glDeleteTextures(iterationTextureID);
        glDeleteTextures(paletteTextureID);
    }

}
