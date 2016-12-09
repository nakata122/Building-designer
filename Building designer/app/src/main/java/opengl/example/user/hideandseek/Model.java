/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package opengl.example.user.hideandseek;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class Model {

//    "uniform mat4 uMVPMatrix;" +
//            "attribute vec4 vPosition;" +
    private final String vertexShaderCode =
        "uniform mat4 u_MVPMatrix;    \n" +
                "attribute vec4 a_Position;      \n" +
                "attribute vec2 a_TexCoordinate; \n" +
                " \n" +
                "varying vec3 v_Position;        \n" +
                "varying vec2 v_TexCoordinate;   \n" +

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

    private static FloatBuffer[] vertexBuffer = new FloatBuffer[100];
    private static ShortBuffer[] drawListBuffer = new ShortBuffer[10];
    private static FloatBuffer[] colorBuffer = new FloatBuffer[100];
    final static float[] cubeTexture =
            {
                    1.0f, 1.0f,
                    1.0f, 0.0f,
                    1.0f, 1.0f,
                    0.0f, 0.0f,
                    0.0f, 0.0f,
                    0.0f, 0.0f
            };
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;
    private int mMVPMatrixHandle;
    private int mapHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    //VBOS IBOS
    final private static int vbos[] = new int[100], ibos[] = new int[12], texCoord[] = new int[100];

    public static boolean[][] bones = new boolean[3][10000];
    public static int vcount, br = 0, br2 = 0, br3 = 0;
    private static  int[]  listLen = new int[100];

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    final int[] texHandle = new int[100];
    public static Bitmap[] textures = new Bitmap[50];
    int temp=0;
    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };
    float lightPos[] = {0,0,0};

    Bitmap texture;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Model() {

        for(int i=0;i<br;i++) {
            Log.e("FIRST", String.valueOf(i));
            GLES20.glGenTextures(1, texHandle, i);
            texture = textures[i];

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texHandle[i]);
            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0);
            texture.recycle();
        }
        GLES20.glGenBuffers(100, vbos, 0);
        GLES20.glGenBuffers(100, texCoord, 0);

        for(int i=0;i<br;i++){

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[i]);

            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer[i].capacity() * 4,
                    vertexBuffer[i], GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texCoord[i]);

            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, colorBuffer[i].capacity() * 4,
                    colorBuffer[i], GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        }

        if (texHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }




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

    public static void loadVertBuff(float[] modelCoords){

        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                (modelCoords.length * 4));
        listLen[br2] = modelCoords.length;
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer[br] = bb.asFloatBuffer();
        vertexBuffer[br].put(modelCoords);
        vertexBuffer[br].position(0);


        br++;
    }

    public static void loadTexBuff(float[] txCoords){
        ByteBuffer txb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                txCoords.length * 4);
        txb.order(ByteOrder.nativeOrder());
        colorBuffer[br3] = txb.asFloatBuffer();
        colorBuffer[br3].put(txCoords);
        colorBuffer[br3].position(0);
        br3++;
    }

    public void draw(float[] mvpMatrix, int modelNum) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);



        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");

        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");


        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texCoord[modelNum]);
        GLES20.glVertexAttribPointer(
                mTextureCoordinateHandle, 2,
                GLES20.GL_FLOAT, false,
                0,0);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texHandle[modelNum]);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // get handle to fragment shader's vColor member
//        mColorHandle = GLES20.glGetUniformLocation(mProgram, "v_Color");
//        GLES20.glUniform4fv(mColorHandle, 1, color, 0);


        // Draw the square
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[modelNum]);
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glDrawArrays(
                GLES20.GL_TRIANGLES, 0, 50000);


        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glDisableVertexAttribArray(mTextureUniformHandle);
        GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);
    }

}