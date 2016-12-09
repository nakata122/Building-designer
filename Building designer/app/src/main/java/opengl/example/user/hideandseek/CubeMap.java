package opengl.example.user.hideandseek;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Created by User on 30.10.2016 г..
 */
public class CubeMap {
    float verts[][] = {

            {
                    100.0f,  100.0f, -100.0f,
                    100.0f,  100.0f, 100.0f,
                    100.0f, -100.0f, -100.0f,
                    100.0f, -100.0f,  100.0f},

            {-100.0f, -100.0f,  -100.0f,
                    -100.0f,  -100.0f,  100.0f,
                    -100.0f,  100.0f,  -100.0f,
                    -100.0f,  100.0f,  100.0f},

            {-100.0f,  100.0f, -100.0f,
        -100.0f,  100.0f, 100.0f,
        100.0f,  100.0f,  -100.0f,
        100.0f,  100.0f,  100.0f},

            {
                    100.0f, -100.0f, -100.0f,
                    100.0f, -100.0f, 100.0f,
                    -100.0f, -100.0f, -100.0f,
                    -100.0f, -100.0f,  100.0f},

            {-100.0f, -100.0f, 100.0f,
                    100.0f, -100.0f,  100.0f,
                    -100.0f,  100.0f,  100.0f,
                    100.0f,  100.0f,  100.0f},

            {100,-100,-100,
                    -100,-100,-100,
                    100,100,-100,
                    -100,100,-100}

};
    short[] indexes = {0,1,2,3};
    final float[] cubeTexture =
            {
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    0.0f, 0.0f,
                    0.0f, 1.0f
            };
    private FloatBuffer vertBuff,texBuff;
    private ShortBuffer indBuff;
    private final int mProgram;
    final int[] vbo = new int[10];
    private int mPositionHandle;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;
    private int mMVPMatrixHandle;
    final int[] textureHandle = new int[7];
    Bitmap tex;
    private final String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;    \n" +
                    "attribute vec4 a_Position;      \n" +
                    "attribute vec2 a_TexCoordinate; \n" +
                    " \n" +
                    "varying vec3 v_Position;        \n" +
                    "varying vec2 v_TexCoordinate;   \n" +
                    "int df=1;\n" +

                    "void main()\n" +
                    "{\n" +
                    "    v_Position = vec3(a_Position);\n" +
                    "    v_TexCoordinate = a_TexCoordinate;\n" +
                    "    gl_Position = u_MVPMatrix * a_Position;\n" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;        \n" +
                    "uniform sampler2D u_Texture;    \n" +
                    "varying vec3 v_Position;        \n" +
                    "varying vec2 v_TexCoordinate;   \n" +
                    "void main()\n" +
                    "{\n" +
                    "    gl_FragColor = texture2D(u_Texture, v_TexCoordinate);\n" +
                    "  }";
    public CubeMap(){
        GLES20.glGenTextures(6, textureHandle, 0);
        // Bind to the texture in OpenGL
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        for(int i=0;i<6;i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[i]);
            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, MainActivity.skyBox[i], 0);
        }
        ByteBuffer bd = ByteBuffer.allocateDirect(cubeTexture.length * 4);
        bd.order(ByteOrder.nativeOrder());
        texBuff = bd.asFloatBuffer();
        texBuff.put(cubeTexture);
        texBuff.position(0);
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, MainActivity.skyBox[2], 0);
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, MainActivity.skyBox[3], 0);
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, MainActivity.skyBox[4], 0);
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, MainActivity.skyBox[5], 0);


        ByteBuffer ib = ByteBuffer.allocateDirect(indexes.length * 4);
        ib.order(ByteOrder.nativeOrder());
        indBuff = ib.asShortBuffer();
        indBuff.put(indexes);
        indBuff.position(0);


        GLES20.glGenBuffers(6, vbo, 0);

        for(int i=0;i<6;i++) {
            ByteBuffer bb = ByteBuffer.allocateDirect(verts[i].length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertBuff = bb.asFloatBuffer();
            vertBuff.put(verts[i]);
            vertBuff.position(0);


            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[i]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertBuff.capacity() * 4, vertBuff, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        }

//        GLES20.glEnableVertexAttribArray(0);
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[1]);
//        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, null);
        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program

        GLES20.glBindAttribLocation(mProgram, 0, "a_Position");
        GLES20.glBindAttribLocation(mProgram, 1, "a_TexCoordinate");


        // Recycle the bitmap, since its data has been loaded into OpenGL.

        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }
    public void draw(float[] mvpMatrix,int curr) {
        GLES20.glUseProgram(mProgram);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glVertexAttribPointer(
                mTextureCoordinateHandle, 2,
                GLES20.GL_FLOAT, false,
                3*4, texBuff);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[curr]);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[curr]);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(
                mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, 0);

        MyGLRenderer.checkGlError("glGetUniformLocation");

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP,4, GLES20.GL_UNSIGNED_SHORT, indBuff);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glDisableVertexAttribArray(mTextureUniformHandle);
        GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);
    }
}
