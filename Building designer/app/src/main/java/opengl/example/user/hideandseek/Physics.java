package opengl.example.user.hideandseek;

import android.content.Context;

/**
 * Created by User on 12.8.2016 г..
 */
public class Physics {
    float[] velocity = new float[3];
    float[] position = new float[3];
    float[] lastPosition = new float[3];
    float acceleration, torque, orientation, angularVelocity, radius=0.1f, mass;

    public boolean isMode() {
        return mode;
    }

    public void setMode(boolean mode) {
        this.mode = mode;
    }

    boolean mode;

    public float[] getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(float[] lastPosition) {
        this.lastPosition = lastPosition;
    }


    public Physics(float[] position) {
        this.position = position;
        this.lastPosition = position;
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public float[] getVelocity() {
        return velocity;
    }

    public void setVelocity(float[] velocity) {
        this.velocity = velocity;
    }

    public float[] getPosition() {
        return position;
    }

    public void setPosition(float[] position) {
        this.position = position;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public float getTorque() {
        return torque;
    }

    public void setTorque(float torque) {
        this.torque = torque;
    }

    public float getOrientation() {
        return orientation;
    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }



}
