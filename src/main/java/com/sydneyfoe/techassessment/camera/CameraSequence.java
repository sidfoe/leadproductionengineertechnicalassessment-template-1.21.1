package com.sydneyfoe.techassessment.camera;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.SerializedName;

public class CameraSequence {
    @SerializedName("keyframes")
    public final List<CameraKeyframe> keyframes;

    public CameraSequence(List<CameraKeyframe> keyframes) {
        this.keyframes = new ArrayList<>(keyframes);
    }

    public List<CameraKeyframe> getKeyframes() {
        return new ArrayList<>(keyframes);
    }

    public CameraPose interpolateAtTick(long tick) {
        if (keyframes.isEmpty()) return null;
        if (keyframes.size() == 1) return keyframes.get(0).pose;

        // Find surrounding keyframes
        CameraKeyframe before = null;
        CameraKeyframe after = null;

        for (CameraKeyframe kf : keyframes) {
            if (kf.tick <= tick) {
                before = kf;
            }
            if (kf.tick >= tick && after == null) {
                after = kf;
            }
        }

        if (before == null) before = keyframes.get(0);
        if (after == null) after = keyframes.get(keyframes.size() - 1);

        if (before.tick == after.tick) return before.pose;

        double t = (double)(tick - before.tick) / (double)(after.tick - before.tick);
        t = Math.max(0, Math.min(1, t));
        return CameraPose.lerp(before.pose, after.pose, t);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CameraSequence)) return false;
        CameraSequence s = (CameraSequence) o;
        return keyframes.equals(s.keyframes);
    }

    @Override
    public int hashCode() {
        return keyframes.hashCode();
    }

    @Override
    public String toString() {
        return String.format("CameraSequence(%d keyframes)", keyframes.size());
    }
}
