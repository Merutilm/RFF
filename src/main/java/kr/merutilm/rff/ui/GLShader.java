package kr.merutilm.rff.ui;

import org.lwjgl.BufferUtils;

import kr.merutilm.rff.util.ConsoleUtils;
import kr.merutilm.rff.util.IOUtilities;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;

import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.opengl.GL45.glBindVertexArray;
import static org.lwjgl.opengl.GL45.glGenVertexArrays;
import static org.lwjgl.opengl.GL45.glUniform1d;

public class GLShader {

    private final int shaderProgram;

    private float[] vertexArray = {
            1, -1, 0, 0, 0, 0, 1,
            -1, 1, 0, 0, 0, 0, 1,
            1, 1, 0, 0, 0, 0, 1,
            -1, -1, 0, 0, 0, 0, 1
    };
    final int[] elementArray = {
            2, 1, 0,
            0, 1, 3
    };
    private final int vaoID;
    private final int vboID;
    private final int eboID;

    public GLShader(String vertexPath, String fragPath) {
        try {
            File fileVertex = new File(IOUtilities.getResource(vertexPath).toURI());
            File fileFrag = new File(IOUtilities.getResource(fragPath).toURI());
            String srcVertex = new String(Files.readAllBytes(fileVertex.toPath()));
            String srcFrag = new String(Files.readAllBytes(fileFrag.toPath()));
           
            int vertex = glCreateShader(GL_VERTEX_SHADER);
            compile(vertex, srcVertex);

            int frag = glCreateShader(GL_FRAGMENT_SHADER);
            compile(frag, srcFrag);

            shaderProgram = glCreateProgram();
            glAttachShader(shaderProgram, vertex);
            glAttachShader(shaderProgram, frag);
            link(shaderProgram);


            vaoID = glGenVertexArrays();
            glBindVertexArray(vaoID);

            FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
            vertexBuffer.put(vertexArray).flip();

            vboID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);


            IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
            elementBuffer.put(elementArray).flip();

            eboID = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);


            int positionSize = 3;
            int colorSize = 4;
            int floatSizeBytes = 4;
            int vertexSizeBytes = (positionSize + colorSize) * floatSizeBytes;

            glVertexAttribPointer(0, positionSize, GL_FLOAT, false, vertexSizeBytes, 0);
            glEnableVertexAttribArray(0);

            glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, (long)positionSize * floatSizeBytes);
            glEnableVertexAttribArray(1);

        } catch (IOException | URISyntaxException e) {
            ConsoleUtils.logError(e);
            throw new IllegalArgumentException();
        }
    }


    public void use() {
        glUseProgram(shaderProgram);
        glBindVertexArray(vaoID);
    }

    public void draw() {
        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);
    }

    public void detach() {
        glUseProgram(0);
        glBindVertexArray(0);
    }

    public void uploadDoubleArray(String varName, double[] value){
        int varLocation = glGetUniformLocation(shaderProgram, varName);
        glUniform1dv(varLocation, value);
    }


    public void uploadDouble(String varName, double value) {
        int varLocation = glGetUniformLocation(shaderProgram, varName);
        glUniform1d(varLocation, value);
    }

    public void uploadFloat(String varName, float value) {
        int varLocation = glGetUniformLocation(shaderProgram, varName);
        glUniform1f(varLocation, value);
    }

    public void upload2f(String varName, float x, float y) {
        int varLocation = glGetUniformLocation(shaderProgram, varName);
        glUniform2f(varLocation, x, y);
    }

    public void uploadInt(String varName, int value) {
        int varLocation = glGetUniformLocation(shaderProgram, varName);
        use();
        glUniform1i(varLocation, value);
    }

    private void compile(int shader, String src) {
        glShaderSource(shader, src);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.out.println(glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH)));
            throw new IllegalStateException();
        }
    }

    private void link(int shaderProgram) {
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            System.out.println(glGetProgramInfoLog(shaderProgram, glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH)));
            throw new IllegalStateException();
        }
    }
}
