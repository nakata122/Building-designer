package opengl.example.user.hideandseek;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.util.Random;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    public final int MOVING=0, ROTATE=1, TERRAFORM=2, WALLS=3, FURNITURE=4, DELETE=5, TEXTURING=6;
    public static int paitTex=0;
    private static final String TAG = "MyGLRenderer";
    private Triangle mTriangle;
    private Terrain terrain;
    private CubeMap cubeMap;
    private Model mSquare;
    private MainPhysics mainPhysics;
    public Walls wall;
    public Physics[] mPhysics = new Physics[100];
    private Blueprint mBlue;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    public final float[] mMVPMatrix = new float[16];
    public final float[] terrainMatrix = new float[16];
    public final float[] mProjectionMatrix = new float[16], projCube = new float[16];
    public float[] mViewMatrix = new float[16], tempVMat = new float[16];
    public final float[] mRotationMatrix = new float[16];
    public final float[] mRotationMatrix2 = new float[16];
    public float[][] modelMat = new float[100][16];

    public static boolean[] items = new boolean[100];

    public float mAngle,realX,realY,lastX=0,lastY=0,worldX,worldY,wid,hei;
    public int fps=0;
    long startTime;
    public float zoom = 0;
    public int selected=-1;
    public boolean moving;
    public static int mode;
    public float fovy;
    Random rand = new Random();

    float num;


    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        float[] defaultPos = {0,0,0};
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mTriangle = new Triangle();
        wall = new Walls();
        mainPhysics = new MainPhysics(this);
        terrain = new Terrain();
        cubeMap = new CubeMap();
        mSquare = new Model();
        mBlue = new Blueprint();

        startTime = System.nanoTime();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        if(System.nanoTime() - startTime >= 1000000000) {
            Log.e("fps: ", String.valueOf(fps));
            fps = 0;
            startTime = System.nanoTime();
        }

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // Use culling to remove back faces.

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        //Log.e("ANGLE", String.valueOf(mAngle/100));
        if(mode == MOVING && selected==-1) {
            worldX -= ((realX - lastX))/100;
            worldY += ((realY - lastY))/100;
        }
        Matrix.setLookAtM(mViewMatrix, 0, -(float)Math.sin(mAngle/100)*zoom + worldX, -(float)Math.cos(mAngle/100)*zoom + worldY, zoom, worldX, worldY, 0, (float)Math.sin(mAngle/100), (float)Math.cos(mAngle/100), 0);
        Matrix.setLookAtM(tempVMat, 0, 0, 0, 0,(float)Math.sin(mAngle/100) , (float)Math.cos(mAngle/100), -zoom, (float)Math.sin(mAngle/100), (float)Math.cos(mAngle/100),0);
        Matrix.setRotateEulerM(mRotationMatrix, 0,0, 0,  mAngle);
        lastX = realX;
        lastY = realY;
        //Matrix.setRotateEulerM(mRotationMatrix2, 0, 0, 0, mAngle);
        Matrix.setRotateEulerM(mRotationMatrix2, 0, mAngle, 0,  0);
        //Matrix.multiplyMM(mProjectionMatrix, 0, mProjectionMatrix,0, mRotationMatrix2,0);
        //Matrix.multiplyMM(tempVMat, 0, tempVMat,0, mRotationMatrix,0);
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        Matrix.multiplyMM(terrainMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        Matrix.multiplyMM(projCube, 0, mProjectionMatrix, 0, tempVMat, 0);
        //Matrix.multiplyMM(terrainMatrix, 0, terrainMatrix, 0, mRotationMatrix, 0);
        // Set the camera position (View matrix)
        // Create a rotation for the obj
        //Matrix.setRotateM(mViewMatrix, 0, mAngle, 0, 0,  1);

        //Calculate Physics
        mainPhysics.onMoveFrame();
        Matrix.rotateM(modelMat[1], 0, -90,1,0,0);
        Matrix.rotateM(modelMat[3], 0, -90,1,0,0);
        Matrix.rotateM(modelMat[7], 0, -90,1,0,0);
        Matrix.rotateM(modelMat[8], 0, -90,1,0,0);

        terrain.draw(terrainMatrix);
        wall.draw(mMVPMatrix);
        for(int j=0;j<6;j++)
            cubeMap.draw(projCube,j);

        // Draw
        for(int i=0;i<Model.br; i++)
            if(items[i])
                mSquare.draw(modelMat[i], i);

        Math.abs(num/3);
        num++;
/*
        for(int i=3;i<mSquare.squareCoords.length-3;i+=3) {
            mSquare.squareCoords[i] = noise((int)num);
        }
*/
        fps++;
    }
    public void rotateMatrix(){
        Matrix.multiplyMM(mViewMatrix, 0, mViewMatrix,0, mRotationMatrix,0);
    }
    public void moveMatrix(int o, float xx, float yy){
        Matrix.multiplyMM(modelMat[o], 0, mProjectionMatrix, 0, mViewMatrix, 0);
        Matrix.translateM(modelMat[o], 0, xx, yy , 1);
        Matrix.rotateM(modelMat[o], 0, 90,1,0,0);
    }
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        float top = (float)Math.tan(70 * Math.PI / 360.0f);
        wid = width;
        hei = height;
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio*top, ratio*top, -top, top, 1, 1000);
        float halfwidth = 5/2;
        float halfheight = 5/2;
        if (halfwidth/halfheight > ratio) {
        // use horizontal angle to set fovy
            fovy = 2*(float)Math.atan(halfwidth/ratio);
        } else {
        // use vertical angle to set fovy
            fovy = 5;
        }
        fovy = (float)(fovy*180/Math.PI);
        mainPhysics.foxy = fovy;
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }



    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    public float getAngle() {
        return mAngle;
    }

    public float getZoom() {
        return zoom;
    }


    public void setAngle(float angle) {
        mAngle = angle;
    }

    public void setZoom(float tzoom) { this.zoom = tzoom;}

    public void setPos(float x, float y){
        this.realX = x;
        //realX = (2 * realX)/wid - 1;
        this.realY = y;
        //realY = 1 - (2 * realY)/hei;
        mainPhysics.touchPos = mainPhysics.rayPicking(realX, realY);
    }

    public void setStartPos(float x, float y){
        float tempX = x;
        //tempX = (2 * tempX)/wid - 1;
        float tempY = y;
        //tempY = 1 - (2 * tempY)/hei;
        this.realX = x;
        this.realY = y;
        lastX = x;
        lastY = y;
        mainPhysics.startTouch = mainPhysics.rayPicking(tempX, tempY);
    }

    public void setWall(){
        wall.changeOffset();
        mainPhysics.setMat();
        Log.e("Br", String.valueOf(wall.br));
    }


}