package com.sydneyfoe.techassessment.camera;

import net.minecraft.client.Camera;

import net.neoforged.api.distmarker.Dist;

import net.neoforged.bus.api.SubscribeEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(
        modid = "yourmodid",
        value = Dist.CLIENT
)
public class CameraEvents {

    /**
     * Runs every Minecraft client tick.
     */
    @SubscribeEvent
    public static void onClientTick(
            ClientTickEvent.Post event
    ) {

        CameraSequenceManager manager =
                CameraClient.getManager();

        manager.tickPlayback();
    }

    /**
     * Runs when Minecraft is calculating
     * the camera's rotation.
     */
    @SubscribeEvent
    public static void onCameraAngles(
            ViewportEvent.ComputeCameraAngles event
    ) {

        CameraSequenceManager manager =
                CameraClient.getManager();

        if (!manager.isPlayingBack()) {
            return;
        }

        CameraPose pose =
                manager.getCurrentPose();

        if (pose == null) {
            return;
        }

        // Change camera rotation
        event.setYaw(
                pose.yaw
        );

        event.setPitch(
                pose.pitch
        );
    }
}
