package opengl.example.user.hideandseek;

import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

class WallBx {
    public int startX = 0, endX = 0, startZ = 0, endZ = 0;
    public float startY = 0, endY = 0;
}

public class MainPhysics {
    public Physics[] mPhysics = new Physics[100];
    private WallBx[] wallBox = new WallBx[1000];
    private int wallBr=0;
    public float[] touchPos = new float[3], startTouch = new float[3], lastTouch = new float[3];
    private final float gravity = 0.0001f;
    private float[] modelPos = new float[3], defaultPos={0,0,0};
    private float[] wallVerts = new float[20];
    public float foxy=1;
    public static float[] vertsRay;
    private int[][] terrainMat = new int[200][200];
    private float[][] itemsSort = new float[Model.br][2];
    Random rand = new Random();

    final MyGLRenderer render;

    MainPhysics(MyGLRenderer render) {
        this.render = render;
        for(int i = 0; i< 30; i++){
            mPhysics[i] = new Physics(defaultPos);
            if(i!=2)
            mPhysics[i].setMode(true);
        }
        for(int i=0;i<12;i++){
            wallVerts[i]=0;
        }
        render.wall.changeVerts(wallVerts);
    }

    public void setMat(){
        float startX = Math.round(startTouch[0] / 2) * 2,
                startY = Math.round(startTouch[1] / 2) * 2,
                curX = Math.round(touchPos[0] / 2) * 2,
                curY = Math.round(touchPos[1] / 2) * 2;
        int diff = (int)(curX - startX);
        float step = (curY-startY)/Math.abs(diff);
        wallBox[wallBr] = new WallBx();
        wallBox[wallBr].endX = (int)startX;
        wallBox[wallBr].endY = startY;
        wallBox[wallBr].startX = (int)startX;
        wallBox[wallBr].startY = startY;

        for(int i=0;i<Math.abs(diff);i++) {
            wallBox[wallBr+i].endX = wallBox[wallBr+i].startX+1;
            if(i!=0)
            wallBox[wallBr+i].endY = wallBox[wallBr+i-1].endY+step;
            else wallBox[wallBr+i].endY++;
            wallBox[wallBr+i].startZ = 0;
            wallBox[wallBr+i].endZ = 4;

            wallBox[wallBr+i+1] = new WallBx();
            wallBox[wallBr+i+1].startX = wallBox[wallBr+i].endX;
            wallBox[wallBr+i+1].startY = wallBox[wallBr+i].endY;
            Log.e("SD", String.valueOf(wallBr));
        }
        wallBr+=Math.abs(diff)+1;
    }

    public void onMoveFrame() {
        boolean t = false;

        for (int i = 0; i < Model.br; i++) {
            float[] accsel = {0, 0, 0}, pos = {0, 0, 0};
            if (render.selected == -1 || render.selected == i) t = true;

            //Moving
            if (mPhysics[i].isMode()) {
                for (int j = 0; j < 3; j++)
                    accsel[j] = mPhysics[i].getVelocity()[j] + (mPhysics[i].getPosition()[j] - mPhysics[i].getLastPosition()[j]) / 50;
                mPhysics[i].setVelocity(accsel);
                for (int j = 0; j < 3; j++)
                    pos[j] = mPhysics[i].getVelocity()[j] + mPhysics[i].getPosition()[j];

                if (render.items[i] && t) {
                    mPhysics[i].setPosition(pos);
                    mPhysics[i].setLastPosition(pos);
                }
                for (int j = 0; j < 3; j++)
                    accsel[j] *= 0.9;
                //accsel[1] -= gravity;
                mPhysics[i].setVelocity(accsel);
            } else {
                float dist = 0;
                float[] newVel = {0, 0, 0};
                for (int j = 0; j < Model.br; j++) {
                    if (j != i) {
                        dist = (float) (Math.pow(mPhysics[j].getPosition()[0] - mPhysics[i].getPosition()[0], 2) + Math.pow(mPhysics[j].getPosition()[1] - mPhysics[i].getPosition()[1], 2) + Math.pow(mPhysics[j].getPosition()[2] - mPhysics[i].getPosition()[2], 2));
                        if (dist <= Math.pow(mPhysics[j].radius + mPhysics[i].radius, 2)) {
                            newVel[0] = (mPhysics[j].getPosition()[0] - mPhysics[i].getPosition()[0]) / 50;
                            newVel[1] = (mPhysics[j].getPosition()[1] - mPhysics[i].getPosition()[1]) / 50;
                            newVel[2] = (mPhysics[j].getPosition()[2] - mPhysics[i].getPosition()[2]) / 50;
                            mPhysics[j].setVelocity(newVel);

                        }
                    }
                }
            }

            if (render.mode == render.MOVING) {
                if (Math.sqrt(Math.pow(touchPos[0] - mPhysics[i].getPosition()[0], 2) + Math.pow(touchPos[1] - mPhysics[i].getPosition()[1], 2)) <= 2 && render.items[i] && t && render.moving) {
                    modelPos = touchPos;
                    modelPos[2] = render.zoom;
                    mPhysics[i].setPosition(modelPos);
                    render.selected = i;
                }
            }

            render.moveMatrix(i, mPhysics[i].getPosition()[0], mPhysics[i].getPosition()[1]);
            t = false;
        }

        if (render.mode == render.TERRAFORM)
            for (int q = 0; q < 300; q += 3) {
                if (Math.abs(Terrain.verts[q] - touchPos[0]) < 2 && Math.abs(Terrain.verts[q + 1] - touchPos[1]) < 2 && render.moving) {
                    Terrain.verts[q + 2] += 0.01;
                }
            }

        if (render.mode == render.WALLS && render.moving) {

            float startX = Math.round(startTouch[0] / 2) * 2,
                startY = Math.round(startTouch[1] / 2) * 2,
                curX = Math.round(touchPos[0] / 2) * 2,
                curY = Math.round(touchPos[1] / 2) * 2;
            wallVerts[6] = startX;
            wallVerts[7] = startY;
            wallVerts[8] = 0;


            wallVerts[0] = startX;
            wallVerts[1] = startY;
            wallVerts[2] = 6;


            wallVerts[9] = curX;
            wallVerts[10] = curY;
            wallVerts[11] = 0;


            wallVerts[3] = curX;
            wallVerts[4] = curY;
            wallVerts[5] = 6;
            render.wall.changeVerts(wallVerts);

            if((int)Math.round(lastTouch[0] / 2) + 100 != wallVerts[9] / 2 + 100 || (int)Math.round(lastTouch[1] / 2) + 100 != wallVerts[10] / 2 + 100) {
                terrainMat[(int) wallVerts[9] / 2 + 100][(int) wallVerts[10] / 2 + 100] = 1;
                terrainMat[Math.round(lastTouch[0] / 2) + 100][Math.round(lastTouch[1] / 2) + 100] = 0;
            }

        }
        if (render.mode == render.DELETE) {
            float camX =  -(float)Math.sin(render.mAngle/100)*render.zoom + render.worldX,
                    camY = -(float)Math.cos(render.mAngle/100)*render.zoom +render.worldY,
                    camZ = render.zoom;
            for(int i=0;i<wallBr;i++){
                float sX=(wallBox[i].startX - camX)/(camX-touchPos[0]),
                        sY=(wallBox[i].startY - camY)/(camY-touchPos[1]),
                        eX=(wallBox[i].endX - camX)/(camX-touchPos[0]),
                        eY=(wallBox[i].endY - camY)/(camY-touchPos[1]),
                        sZ=(wallBox[i].startZ - camZ)/(camZ),
                        eZ=(wallBox[i].endZ - camZ)/(camZ);

                if(wallBox[i].startX == Math.round(touchPos[0] / 2)){
                    render.wall.texList[i] = -1;
                }
            }
        }
        if (render.mode == render.TEXTURING) {
            float camX =  -(float)Math.sin(render.mAngle/100)*render.zoom + render.worldX,
                  camY = -(float)Math.cos(render.mAngle/100)*render.zoom +render.worldY,
                  camZ = render.zoom;
            for(int i=0;i<wallBr;i++){
                float sX=(wallBox[i].startX - camX)/(camX-touchPos[0]),
                        sY=(wallBox[i].startY - camY)/(camY-touchPos[1]),
                        eX=(wallBox[i].endX - camX)/(camX-touchPos[0]),
                        eY=(wallBox[i].endY - camY)/(camY-touchPos[1]),
                        sZ=(wallBox[i].startZ - camZ)/(camZ),
                        eZ=(wallBox[i].endZ - camZ)/(camZ);

//                if(sX>sY && eX<eY && sX>sZ && eX<eZ && render.wall.texList[i]!=-1){
//                    render.wall.texList[i] = render.paitTex;
//                }
                Log.e("StartTuuch", String.valueOf(Math.round(touchPos[0] / 2)) + " " + String.valueOf(wallBox[i].startX));
                if(wallBox[i].startX == Math.round(touchPos[0] / 2) && render.wall.texList[i]!=-1){
                    render.wall.texList[i] = render.paitTex;
                }
            }
        }
        lastTouch=touchPos;
    }

    public float[] rayPicking(float realX, float realY){
        float[]  far = new float[4],near = new float[4], ray = new float[4];
        float[] cam = {-(float)Math.sin(render.mAngle/100)*render.zoom + render.worldX, -(float)Math.cos(render.mAngle/100)*render.zoom + render.worldY, render.zoom};

        float[] view = {(float)Math.sin(render.mAngle/100), (float)Math.cos(render.mAngle/100), render.zoom,1};

        float[] rhvVec = {realX, realY,-render.zoom,0};
        float[] rhvVec2 = {realX, realY,1,1};

        int[] viewport = {0,0,(int)render.wid,(int)render.hei};

        float[] viewInv = new float[16], mViewMatrix = new float[16];
        Matrix.setLookAtM(mViewMatrix, 0, -(float)Math.sin(render.mAngle/100)*render.zoom + render.worldX, -(float)Math.cos(render.mAngle/100)*render.zoom +render.worldY, render.zoom, render.worldX, render.worldY, 0, (float)Math.sin(render.mAngle/100), (float)Math.cos(render.mAngle/100), 0);
        Matrix.invertM(viewInv, 0, mViewMatrix,0);

        float[] projInv = new float[16];
        Matrix.invertM(projInv,0, render.mProjectionMatrix,0);


        float[] combo = new float[4];


        Matrix.multiplyMV(combo, 0, projInv, 0 , rhvVec, 0);
        combo[2]=-render.zoom;

        Matrix.multiplyMV(far, 0,viewInv , 0 , combo, 0);
        //Matrix.multiplyMV(near, 0, combo, 0 ,rhvVec2 , 0);
        //far = normalizeVector3(far);
        //near = normalizeVector3(near);
        float viewRatio = (float)Math.tan(((float) Math.PI / (180.f/20) / 2.00f)) * render.zoom;
        //float d = 1/result[3];

        float x = (float)Math.sin(render.mAngle/100)*render.zoom,
                y = (float)Math.cos(render.mAngle/100)*render.zoom;
        //float mag = (float) Math.sqrt((result[0]* result[0]) + (result[1] * result[1]) + (result[2] * result[2]));

        float theta = 70;
        //float fi = result[1]/result[0];
        float directionX = (float)Math.sin(theta) + (float)Math.cos(theta);
        //float directionY = mag * (float)Math.sin(theta) * (float)Math.sin(fi);
        ray[0] = (far[0]-cam[0]);
        ray[1] = (far[1]-cam[1]);

        //if(Math.cos(render.mAngle/100)*render.zoom<0 && result[1]>0) result[1]*=2;
        GLU.gluUnProject(realX,(viewport[3] - realY), 0,mViewMatrix,0, render.mProjectionMatrix,0, viewport,0, near,0);
        GLU.gluUnProject(realX,(viewport[3] - realY), 1,mViewMatrix,0, render.mProjectionMatrix,0, viewport,0, far,0);

        near = normalizeVector3(near);
        far = normalizeVector3(far);
        far[0]/=999;far[1]/=999;far[2]/=999;
        float distance = (float)Math.sqrt(((far[0]-cam[0]) * (far[0]-cam[0])) + ((far[1]-cam[1]) * (far[1]-cam[1])) + ((far[2]-cam[2]) * (far[2]-cam[2])));

        float t = cam[2]/far[2];
        ray[0] = (far[0] - (near[0]-cam[0])*t)+near[0];
        ray[1] = (far[1] - (near[1]-cam[1])*t)+near[1];
        ray[2] = (far[2] - (near[2]-cam[2])*t)+near[2];


        float[] endResult = { ray[0],ray[1], render.zoom};

        return endResult;
    }
    public float[] normalizeVector3(float[] vector3)
    {
        float[] normalizedVector = new float[3];
        float magnitude = (float) Math.sqrt((vector3[0] * vector3[0]) + (vector3[1] * vector3[1]) + (vector3[2] * vector3[2]));
        normalizedVector[0] = vector3[0] / vector3[3];
        normalizedVector[1] = vector3[1] / vector3[3];
        normalizedVector[2] = vector3[2] / vector3[3];
        return normalizedVector;
    }
    public float[] crossProduct(float[] a, float[] b) {
        float tempX = a[1] * b[2] - a[2] * b[1];
        float tempY = a[2] * b[1] - a[1] * b[2];
        float tempZ = a[1] * b[1] - a[1] * b[1];

        float[] cross = {tempX,tempY,tempZ};

        return cross;
    }
    public float dotProduct(float[] a, float[] b) {
        float product = a[0]*b[0] + a[1] * b[1] + a[2] * b[2];

        return product;
    }

}
