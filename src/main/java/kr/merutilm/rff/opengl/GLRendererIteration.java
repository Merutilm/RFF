package kr.merutilm.rff.opengl;

import kr.merutilm.rff.settings.ColorSettings;
import kr.merutilm.rff.struct.HexColor;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL46.*;

public class GLRendererIteration extends GLRenderer implements GLIterationTextureProvider {

    private ColorSettings colorSettings;
    private int iterWidth;
    private int iterHeight;
    private long maxIteration;

    private int paletteTextureID;
    private FloatBuffer paletteBuffer;

    private int iterationTextureID;
    private IntBuffer iterationBuffer;


    public GLRendererIteration() {
        super(new GLShader(GLRenderer.DEFAULT_VERTEX_PATH, "iteration"));
    }


    public void setIteration(int x, int y, int w, double iteration) {
        synchronized (shader) {
            iterationBuffer.clear();
            iterationBuffer.put((y * w + x) * 2, doubleToTwoIntBits(iteration));
            iterationBuffer.flip();
        }
    }

    public void reloadIterationBuffer(int iterWidth, int iterHeight, long maxIteration) {
        this.iterationBuffer = emptyIterationBuffer(iterWidth, iterHeight);
        this.iterationTextureID = shader.recreateTexture2D(iterationTextureID, iterWidth, iterHeight, GLShader.TextureFormat.INT2);
        this.iterWidth = iterWidth;
        this.iterHeight = iterHeight;
        this.maxIteration = maxIteration;
    }


    public void setColorSettings(ColorSettings settings) {

        this.colorSettings = settings;
        this.paletteBuffer = createPaletteBuffer(colorSettings);
        this.paletteTextureID = shader.recreateTexture1D(paletteTextureID, colorSettings.colors().length, GLShader.TextureFormat.FLOAT4);
    }

    @Override
    public int getIterationTextureID() {
        return iterationTextureID;
    }


    @Override
    protected void update() {

        if (paletteBuffer == null || iterationBuffer == null) {
            return;
        }

        paletteBuffer.flip();
        iterationBuffer.flip();

        shader.upload2i("resolution", w, h);
        shader.uploadTexture2D("iterations", GL_TEXTURE0, iterationTextureID, iterationBuffer, iterWidth, iterHeight, GLShader.TextureFormat.INT2);
        shader.uploadLong("maxIteration", maxIteration);

        shader.uploadTexture1D("palette",  GL_TEXTURE1, paletteTextureID, paletteBuffer, colorSettings.colors().length, GLShader.TextureFormat.FLOAT4);
        shader.uploadFloat("paletteOffset", (float) colorSettings.offsetRatio());
        shader.uploadFloat("paletteInterval", (float) colorSettings.iterationInterval());
        shader.uploadInt("smoothing", colorSettings.colorSmoothing().ordinal());
    }

    private static int[] doubleToTwoIntBits(double v) {
        long a = Double.doubleToLongBits(v);
        int high = (int) (a >>> 32);
        int low = (int) (a & 0xffffffffL);

        return new int[]{high, low};
    }

    private static IntBuffer emptyIterationBuffer(int w, int h) {
        return BufferUtils.createIntBuffer(w * h * 2);
    }


    public void setAllIterations(double[] iterations) {
        for (int i = 0; i < iterWidth * iterHeight; i++) {
            iterationBuffer.put(doubleToTwoIntBits(iterations[i]));
        }
        iterationBuffer.flip();
    }

    private static FloatBuffer createPaletteBuffer(ColorSettings colorSettings) {
        HexColor[] colors = colorSettings.colors();
        FloatBuffer buffer = BufferUtils.createFloatBuffer(colors.length * 4);
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
