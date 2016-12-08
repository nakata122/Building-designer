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

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGL;
import javax.microedition.khronos.egl.EGLContext;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;
    private float moveX, moveY,xzoom,yzoom,lastX,lastY;
    public boolean zoom=false, move=true, rotate=false;
    GLES20 gl;
    int br=0;

    public MyGLSurfaceView(Context context) {
        super(context);
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

                // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);


        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_UP:
                zoom = false;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                zoom = true;
                move = false;
                lastX=e.getX(1);
                lastY=e.getY(1);
                break;
            case MotionEvent.ACTION_UP:
                mRenderer.moving = false;
                mRenderer.selected = -1;
                if(mRenderer.mode == mRenderer.WALLS){
                    mRenderer.setWall();
                }
                break;
            case MotionEvent.ACTION_DOWN:
                move=true;
                mRenderer.setStartPos(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if(rotate) {
                    float dx = x - mPreviousX;
                    float dy = y - mPreviousY;

                    // reverse direction of rotation above the mid-line
                    if (y > getHeight() / 2) {
                        dx = dx * -1;
                    }

                    // reverse direction of rotation to left of the mid-line
                    if (x < getWidth() / 2) {
                        dy = dy * -1;
                    }

                    mRenderer.setAngle(
                            mRenderer.getAngle() +
                                    ((dx + dy) * TOUCH_SCALE_FACTOR));  // = 180.0f / 320
                    mRenderer.rotateMatrix();
                }
                if(move){
                    mRenderer.moving = true;
                    mRenderer.setPos(x,y);
                    requestRender();
                }
                if(zoom ){
                    float x2 = e.getX(1);
                    float y2 = e.getY(1);
                    if(x2>x)
                        xzoom = ((x2 - lastX) - (x-mPreviousX));
                    else
                        xzoom = ( (x-mPreviousX) - (x2 - lastX) );
                    if(y2>y)
                        yzoom = ((y2 - lastY) - (y-mPreviousY));
                    else
                        yzoom = ( (y-mPreviousY) - (y2 - lastY) );

                    mRenderer.setZoom(mRenderer.getZoom() - (xzoom+yzoom)/150);

                    requestRender();
                    lastX=x2;
                    lastY=y2;
                }
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

}
