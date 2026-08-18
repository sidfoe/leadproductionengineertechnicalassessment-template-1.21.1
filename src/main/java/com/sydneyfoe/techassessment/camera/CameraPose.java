package com.sydneyfoe.techassessment.camera;

import com.google.gson.annotations.SerializedName;

public class CameraPose {
    @SerializedName("x")
    public final double x;
    @SerializedName("y")
    public final double y;
    @SerializedName("z")
    public final double z;
    @SerializedName("pitch")
    public final float pitch;
    @SerializedName("yaw")
    public final float yaw;
    @SerializedName("roll")
    public final float roll;

    public CameraPose(double x, double y, double z, float pitch, float yaw, float roll) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getPitch() { return pitch; }
    public float getYaw() { return yaw; }
    public float getRoll() { return roll; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CameraPose)) return false;
        CameraPose p = (CameraPose) o;
        return Math.abs(p.x - x) < 0.0001 && Math.abs(p.y - y) < 0.0001 && 
               Math.abs(p.z - z) < 0.0001 && Math.abs(p.pitch - pitch) < 0.0001 &&
               Math.abs(p.yaw - yaw) < 0.0001 && Math.abs(p.roll - roll) < 0.0001;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(x) ^ Double.hashCode(y) ^ Double.hashCode(z) ^ 
               Float.hashCode(pitch) ^ Float.hashCode(yaw) ^ Float.hashCode(roll);
    }

    @Override
    public String toString() {
        return String.format("CameraPose(%.2f, %.2f, %.2f, %.2f, %.2f, %.2f)", x, y, z, pitch, yaw, roll);
    }

    public static CameraPose lerp(CameraPose a, CameraPose b, double t) {
        double x = a.x + (b.x - a.x) * t;
        double y = a.y + (b.y - a.y) * t;
        double z = a.z + (b.z - a.z) * t;
        float pitch = (float)(a.pitch + (b.pitch - a.pitch) * t);
        float yaw = (float)(a.yaw + (b.yaw - a.yaw) * t);
        float roll = (float)(a.roll + (b.roll - a.roll) * t);
        return new CameraPose(x, y, z, pitch, yaw, roll);
    }
}
