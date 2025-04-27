package kr.merutilm.rff.opengl;

import org.lwjgl.BufferUtils;

import kr.merutilm.rff.util.ConsoleUtils;
import kr.merutilm.rff.util.IOUtilities;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.opengl.GL45.glBindVertexArray;
import static org.lwjgl.opengl.GL45.glGenVertexArrays;

public class GLShaderLoader {

    private final int shaderProgram;
    private static final String TEXTURE_FORMAT_ERROR_MESSAGE = "Unsupported Texture Format";
    private static final String SLASH = "/";
    private static final String DEFAULT_SHADER_PATH = SLASH + "shader" + SLASH;
    private static final String DEFAULT_SHADER_EXTENSION = ".glsl";
    private int fbo;
    private int fboTextureID;

    final int[] elementArray = {
            2, 1, 0,
            0, 1, 3
    };
    private final int vaoID;

    public GLShaderLoader(String vertexName, String fragName) {

        //-----------------------------------------------
        //compile sources
        //-----------------------------------------------

        vertexName = DEFAULT_SHADER_PATH + vertexName + DEFAULT_SHADER_EXTENSION;
        fragName = DEFAULT_SHADER_PATH + fragName + DEFAULT_SHADER_EXTENSION;

        try(
                InputStream vertexStream = IOUtilities.getResourceAsStream(vertexName);
                InputStream fragmentStream = IOUtilities.getResourceAsStream(fragName);
        ) {

            String vertexSrc = new String(vertexStream.readAllBytes());
            String fragSrc = new String(fragmentStream.readAllBytes());

            int vertex = glCreateShader(GL_VERTEX_SHADER);
            compile(vertex, vertexSrc);

            int frag = glCreateShader(GL_FRAGMENT_SHADER);
            compile(frag, fragSrc);

            shaderProgram = glCreateProgram();
            glAttachShader(shaderProgram, vertex);
            glAttachShader(shaderProgram, frag);
            link(shaderProgram);

        } catch (IOException e) {
            ConsoleUtils.logError(e);
            throw new IllegalArgumentException();
        }
        // ----------------------------------------------------------
        // Bind the points
        // ----------------------------------------------------------

        float[] vertexArray = {
                //location     //color
                1, -1, 0,      0, 0, 0, 1,
                -1, 1, 0,      0, 0, 0, 1,
                1, 1, 0,      0, 0, 0, 1,
                -1, -1, 0,      0, 0, 0, 1
        };
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();


        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);


        int vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);



        int eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);


        int positionSize = 3;
        int colorSize = 4;
        int floatSizeBytes = Float.BYTES;
        int vertexSizeBytes = (positionSize + colorSize) * floatSizeBytes;

        glVertexAttribPointer(0, positionSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, (long)positionSize * floatSizeBytes);
        glEnableVertexAttribArray(1);

        //-----------------------------------------------
        //Generate FBO
        //-----------------------------------------------
        fbo = glGenFramebuffers();
        fboTextureID = glGenTextures();

    }

    public synchronized void setAsLastFBO(){
        if(fbo == 0){
            return;
        }

        glDeleteFramebuffers(fbo);
        glDeleteTextures(fboTextureID);

        fbo = 0;
        fboTextureID = 0;
    }



    public synchronized void resetFBOTexture(int panelWidth, int panelHeight) {
        if(fbo == 0){
            return;
        }
        fboTextureID = recreateTexture2D(fboTextureID, panelWidth, panelHeight, GLShaderLoader.TextureFormat.FLOAT4, false);
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fboTextureID, 0);
        glViewport(0, 0, panelWidth, panelHeight);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }


    public int getFboTextureID() {
        return fboTextureID;
    }

    public void bindFBO(){
        if(glIsFramebuffer(fbo)){
            glBindFramebuffer(GL_FRAMEBUFFER, fbo);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fboTextureID, 0);
        }
    }

    public void unbindFBO(){

        if(glIsFramebuffer(fbo)) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, 0, 0);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    public synchronized int recreateTexture2D(int textureID, int width, int height, TextureFormat textureFormat, boolean linearInterpolation) {
        if(glIsTexture(textureID)){
            glDeleteTextures(textureID);
        }

        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        switch (textureFormat.type){
            case GL_INT -> glTexImage2D(GL_TEXTURE_2D, 0, textureFormat.internal, width, height, 0, textureFormat.format, textureFormat.type, (IntBuffer) null);
            case GL_FLOAT -> glTexImage2D(GL_TEXTURE_2D, 0, textureFormat.internal, width, height, 0, textureFormat.format, textureFormat.type, (FloatBuffer) null);
            default -> throw new IllegalArgumentException(TEXTURE_FORMAT_ERROR_MESSAGE);
        }
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        if(linearInterpolation){
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        }else{
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        }
        return textureID;
    }


    public synchronized void use() {
        glUseProgram(shaderProgram);
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

    }

    public synchronized void draw() {
        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);
    }

    public synchronized void detach() {
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        glUseProgram(0);
    }

    public synchronized void uploadTexture2D(String varName, int textureUnit, int textureID){
        glActiveTexture(textureUnit);
        glBindTexture(GL_TEXTURE_2D, textureID);
        uploadInt(varName, textureUnitToIndex(textureUnit));
    }

    public synchronized void uploadTexture2D(String varName, int textureUnit, int textureID, Buffer buffer, int w, int h, TextureFormat textureFormat){
        glActiveTexture(textureUnit);
        glBindTexture(GL_TEXTURE_2D, textureID);
        switch (buffer){
            case IntBuffer b ->  glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, w, h, textureFormat.format, textureFormat.type, b);
            case FloatBuffer b ->  glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, w, h, textureFormat.format, textureFormat.type, b);
            default -> throw new IllegalArgumentException(TEXTURE_FORMAT_ERROR_MESSAGE);
        }


        uploadInt(varName, textureUnitToIndex(textureUnit));
    }


    private static int textureUnitToIndex(int textureUnit){
        return textureUnit - GL_TEXTURE0;
    }

    public synchronized void uploadDouble(String varName, double value){
        int varLocation = getLocation(varName);
        glUniform1d(varLocation, value);
    }

    private int getLocation(String varName){
        int loc = glGetUniformLocation(shaderProgram, varName);
        if(loc == -1){
            throw new IllegalArgumentException("Cannot found uniform : " + varName);
        }
        return loc;
    }

    public synchronized void uploadBool(String varName, boolean value) {
        int varLocation = getLocation(varName);
        glUniform1i(varLocation, value ? 1 : 0);
    }


    public synchronized void uploadFloat(String varName, float value) {
        int varLocation = getLocation(varName);
        glUniform1f(varLocation, value);
    }

    public synchronized void upload2i(String varName, int x, int y) {
        int varLocation = getLocation(varName);
        glUniform2i(varLocation, x, y);
    }


    public synchronized void uploadInt(String varName, int value) {
        int varLocation = getLocation(varName);
        glUniform1i(varLocation, value);
    }

    private synchronized void compile(int shader, String src) {
        glShaderSource(shader, src);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new IllegalArgumentException(glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH)));
        }
    }

    private synchronized void link(int shaderProgram) {
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            throw new IllegalArgumentException(glGetProgramInfoLog(shaderProgram, glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH)));
        }
    }

    public enum TextureFormat {
        INT1(GL_R32I, GL_RED_INTEGER, GL_INT),
        INT2(GL_RG32I, GL_RG_INTEGER, GL_INT),
        INT3(GL_RGB32I, GL_RGB_INTEGER, GL_INT),
        INT4(GL_RGBA32I, GL_RGBA_INTEGER, GL_INT),
        FLOAT1(GL_R32F, GL_RED, GL_FLOAT),
        FLOAT2(GL_RG32F, GL_RG, GL_FLOAT),
        FLOAT3(GL_RGB32F, GL_RGB, GL_FLOAT),
        FLOAT4(GL_RGBA32F, GL_RGBA, GL_FLOAT),

        ;
        private final int internal;
        private final int format;
        private final int type;
        TextureFormat(int internal, int format, int type) {
            this.internal = internal;
            this.format = format;
            this.type = type;
        }

    }
}
