package opengl.example.user.hideandseek;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by User on 2.10.2016 г..
 */
public class Walls {
    public float[] verts = new float[10000];
    public int br=1, tempBr=1;
    public int[] texList = new int[10000];
    boolean running=false;
    private int vOffset=0;
    private FloatBuffer[] vertBuff = new FloatBuffer[1000];
    private FloatBuffer[] texBuff = new FloatBuffer[1000];
    final float[] cubeTexture =
            {
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    };
    private ShortBuffer orderBuff;
    private final int mProgram;
    final int[] vbo = new int[3];
    float distX;
    private int mPositionHandle;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;
    private int mMVPMatrixHandle;

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
    final int[] textureHandle = new int[7];
    Bitmap tex;

    public Walls() {

        GLES20.glGenTextures(6, textureHandle, 0);
        for(int i=0;i<5;i++) {
            tex = MainActivity.textures[i];
            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[i]);
            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, tex, 0);
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }


        ByteBuffer bb = ByteBuffer.allocateDirect(verts.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertBuff[0] = bb.asFloatBuffer();
        vertBuff[0].put(verts);
        vertBuff[0].position(0);


        bb = ByteBuffer.allocateDirect(cubeTexture.length * 4);
        bb.order(ByteOrder.nativeOrder());
        texBuff[0] = bb.asFloatBuffer();
        texBuff[0].put(cubeTexture);
        texBuff[0].position(0);

        GLES20.glGenBuffers(3,vbo,0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertBuff[0].capacity() * 4*50, null, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);



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

    public void changeVerts(float[] v){
        distX = (v[9] - v[0]);
        if(distX==0)distX=1;
//        if(v[0] - v[9]!=0){
//            cubeTexture[3]=(v[9] - v[0]);
//            cubeTexture[9]=(v[9] - v[0]);
//        }
        float[] vert = v;
        float diff=(v[10]-v[1])/Math.abs(distX);
        vert[10] = vert[1];
        vert[4] = vert[1];
        for(int i=0; i<Math.abs(distX);i++) {
            if(distX>0){
                vert[3] = vert[0]+1;
                vert[9] = vert[0]+1;
            }else{
                vert[3] = vert[0]-1;
                vert[9] = vert[0]-1;
            }
            vert[10] += diff;
            vert[4] += diff;
            ByteBuffer bb = ByteBuffer.allocateDirect(vert.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertBuff[br+i] = bb.asFloatBuffer();
            vertBuff[br+i].put(vert);
            vertBuff[br+i].position(0);


            bb = ByteBuffer.allocateDirect(vert.length * 4);
            bb.order(ByteOrder.nativeOrder());
            texBuff[br+i] = bb.asFloatBuffer();
            texBuff[br+i].put(cubeTexture);
            texBuff[br+i].position(0);


            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
            GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vOffset+1000*i, vertBuff[br+i].capacity() * 4, vertBuff[br+i]);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

            if(distX>0){
                vert[0]++;
                vert[6]++;
            }else{
                vert[0]--;
                vert[6]--;
            }
            vert[1] = vert[10];
            vert[7] = vert[10];
        }
        tempBr=br+(int)Math.abs(distX)-1;
        running=true;
    }

    public void changeOffset(){
        running=false;
        vOffset+=1000*Math.abs(distX);
        br=tempBr+1;
        Log.e("TOOO", String.valueOf(verts.length*distX));
    }

    public void draw(float[] mvpMatrix) {
        for(int j=0;j<tempBr;j++) {
            if (texList[j] != -1) {
                GLES20.glUseProgram(mProgram);

                mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
                mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");


                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
                GLES20.glEnableVertexAttribArray(mPositionHandle);
                GLES20.glVertexAttribPointer(
                        mPositionHandle, 3,
                        GLES20.GL_FLOAT, false,
                        0, j * 1000);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

                mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
                mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");

                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[texList[j]]);

                // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
                GLES20.glUniform1i(mTextureUniformHandle, 0);

                if (running) j++;
                GLES20.glVertexAttribPointer(
                        mTextureCoordinateHandle, 2,
                        GLES20.GL_FLOAT, false,
                        3 * 4, texBuff[j]);
                GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
                if (running) j--;

                MyGLRenderer.checkGlError("glGetUniformLocation");

                GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
                MyGLRenderer.checkGlError("glUniformMatrix4fv");


                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);


                // Disable vertex array
                GLES20.glDisableVertexAttribArray(mPositionHandle);
                GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
                GLES20.glDisableVertexAttribArray(mTextureUniformHandle);
                GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);
            }
        }
    }
}
