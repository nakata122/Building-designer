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

import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class Blueprint {

//    "uniform mat4 uMVPMatrix;" +
//            "attribute vec4 vPosition;" +
    private final String vertexShaderCode =
        "uniform mat4 u_MVPMatrix;    \n" +
                "attribute vec4 a_Position;      \n" +
                " \n" +
                "varying vec3 v_Position;        \n" +
                "int df=1;\n" +

                "void main()\n" +
                "{\n" +

                "    v_Position = vec3(a_Position);\n" +
                "    gl_Position = u_MVPMatrix * a_Position;\n" +
                "}";

    private final String fragmentShaderCode =
            "precision mediump float;        \n" +
                    "varying vec3 v_Position;        \n" +
                    "void main()\n" +
                    "{\n" +
                    "    gl_FragColor =  vec4(0.5, 0, 255, 0);\n" +
                    "  }";

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;
    private int mMVPMatrixHandle;
    private int mapHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    Bitmap bricks;
    public static float[] squareCoords = new float[30000]; // verts

    public static short[] drawOrder = new short[50000];// order to draw vertices

    public static float[] texCords = new float[50000];

    public static boolean[][] bones = new boolean[3][10000];
    public static int vcount;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    final int[] textureHandle = new int[1];

    int temp=0;
    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };
    float lightPos[] = {0,0,0};

    Bitmap texture;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Blueprint() {

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);


        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

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


        // Recycle the bitmap, since its data has been loaded into OpenGL.

        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        temp=0;
        for(int i=0;i<vcount*3;i+=3){
            if(bones[0][temp])
                squareCoords[i]-=0.001f;
            temp++;
        }
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);
        // Set program handles. These will later be used to pass in values to the program.
        // get handle to vertex shader's vPosition member
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");



        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
//        mColorHandle = GLES20.glGetUniformLocation(mProgram, "v_Color");
//        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");


        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_LINE_LOOP, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }

}