package com.sydneyfoe.techassessment.camera;

import com.google.gson.annotations.SerializedName;

public class CameraKeyframe {
    @SerializedName("tick")
    public final long tick;
    @SerializedName("pose")
    public final CameraPose pose;

    public CameraKeyframe(long tick, CameraPose pose) {
        this.tick = tick;
        this.pose = pose;
    }

    public long getTick() { return tick; }
    public CameraPose getPose() { return pose; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CameraKeyframe)) return false;
        CameraKeyframe k = (CameraKeyframe) o;
        return k.tick == tick && k.pose.equals(pose);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(tick) ^ pose.hashCode();
    }

    @Override
    public String toString() {
        return String.format("CameraKeyframe(tick=%d, %s)", tick, pose);
    }
}
