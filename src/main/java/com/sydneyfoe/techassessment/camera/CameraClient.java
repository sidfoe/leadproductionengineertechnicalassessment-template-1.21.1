package com.sydneyfoe.techassessment.camera;

import java.nio.file.Path;

import net.minecraft.client.Minecraft;

public class CameraClient {

    private static CameraSequenceManager manager;

    public static void initialize() {

        Minecraft minecraft =
                Minecraft.getInstance();

        Path gameDirectory =
                minecraft.gameDirectory.toPath();

        /*
         * For now we are storing the JSON directly
         * in the Minecraft directory.
         */
        manager =
                new CameraSequenceManager(
                        gameDirectory
                );
    }

    public static CameraSequenceManager getManager() {

        if (manager == null) {
            initialize();
        }

        return manager;
    }
}