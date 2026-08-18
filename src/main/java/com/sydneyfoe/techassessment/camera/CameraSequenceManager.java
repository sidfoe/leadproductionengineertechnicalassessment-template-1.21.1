package com.sydneyfoe.techassessment.camera;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class CameraSequenceManager {

    private static final Logger LOGGER =
            LogUtils.getLogger();

    private static final String SEQUENCES_FILE =
            "camera_sequences.json";

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private final Map<String, CameraSequence> sequences =
            new HashMap<>();

    private String activeSequenceName;

    private long playbackStartTick;

    private long playbackDurationTicks;

    private CameraPose currentPose;

    private final Path worldDir;

    public CameraSequenceManager(Path worldDir) {

        this.worldDir = worldDir;

        loadFromFile();
    }

    // =========================================================
    // JSON
    // =========================================================

    private void loadFromFile() {

        File file =
                worldDir.resolve(SEQUENCES_FILE).toFile();

        if (!file.exists()) {

            LOGGER.info(
                    "No camera sequences file found at {}",
                    file.getAbsolutePath()
            );

            return;
        }

        try (FileReader reader =
                     new FileReader(file)) {

            JsonObject json =
                    GSON.fromJson(
                            reader,
                            JsonObject.class
                    );

            if (json != null &&
                    json.has("sequences")) {

                JsonObject seqs =
                        json.getAsJsonObject(
                                "sequences"
                        );

                for (String name : seqs.keySet()) {

                    CameraSequence sequence =
                            GSON.fromJson(
                                    seqs.get(name),
                                    CameraSequence.class
                            );

                    sequences.put(
                            name,
                            sequence
                    );
                }
            }

            LOGGER.info(
                    "Loaded {} camera sequences",
                    sequences.size()
            );

        } catch (IOException e) {

            LOGGER.error(
                    "Failed to load camera sequences",
                    e
            );
        }
    }

    public void saveToFile() {

        File file =
                worldDir.resolve(SEQUENCES_FILE).toFile();

        try (FileWriter writer =
                     new FileWriter(file)) {

            JsonObject root =
                    new JsonObject();

            JsonObject seqs =
                    new JsonObject();

            for (Map.Entry<String, CameraSequence> entry :
                    sequences.entrySet()) {

                seqs.add(
                        entry.getKey(),
                        GSON.toJsonTree(
                                entry.getValue()
                        )
                );
            }

            root.add(
                    "sequences",
                    seqs
            );

            GSON.toJson(
                    root,
                    writer
            );

            LOGGER.info(
                    "Saved {} camera sequences to {}",
                    sequences.size(),
                    file.getAbsolutePath()
            );

        } catch (IOException e) {

            LOGGER.error(
                    "Failed to save camera sequences",
                    e
            );
        }
    }

    // =========================================================
    // Sequence Management
    // =========================================================

    public void addOrUpdateSequence(
            String name,
            CameraSequence sequence
    ) {

        sequences.put(
                name,
                sequence
        );

        saveToFile();

        LOGGER.info(
                "Sequence '{}' saved with {} keyframes",
                name,
                sequence.keyframes.size()
        );
    }

    public CameraSequence getSequence(
            String name
    ) {

        return sequences.get(name);
    }

    public Map<String, CameraSequence> getAllSequences() {

        return new HashMap<>(
                sequences
        );
    }

    // =========================================================
    // Playback
    // =========================================================

    public void startPlayback(
            String sequenceName
    ) {

        CameraSequence sequence =
                sequences.get(sequenceName);

        if (sequence == null) {

            LOGGER.warn(
                    "Sequence '{}' not found",
                    sequenceName
            );

            return;
        }

        if (sequence.keyframes == null ||
                sequence.keyframes.isEmpty()) {

            LOGGER.warn(
                    "Sequence '{}' has no keyframes",
                    sequenceName
            );

            return;
        }

        this.activeSequenceName =
                sequenceName;

        this.playbackStartTick = 0;

        /*
         * The final keyframe determines
         * how long the sequence lasts.
         */
        CameraKeyframe lastKeyframe =
                sequence.keyframes.get(
                        sequence.keyframes.size() - 1
                );

        this.playbackDurationTicks =
                lastKeyframe.tick;

        this.currentPose =
                sequence.interpolateAtTick(0);

        LOGGER.info(
                "Started playback of sequence '{}'",
                sequenceName
        );
    }

    public void stopPlayback() {

        activeSequenceName = null;

        playbackStartTick = 0;

        playbackDurationTicks = 0;

        currentPose = null;

        LOGGER.info(
                "Stopped camera playback"
        );
    }

    public boolean isPlayingBack() {

        return activeSequenceName != null;
    }

    /**
     * Called every client tick.
     */
    public void tickPlayback() {

        if (!isPlayingBack()) {
            return;
        }

        CameraSequence sequence =
                sequences.get(
                        activeSequenceName
                );

        if (sequence == null) {

            stopPlayback();

            return;
        }

        // Has the sequence finished?
        if (playbackStartTick >
                playbackDurationTicks) {

            stopPlayback();

            return;
        }

        /*
         * Ask the sequence where the camera
         * should be at this exact tick.
         */
        currentPose =
                sequence.interpolateAtTick(
                        playbackStartTick
                );

        playbackStartTick++;
    }

    // =========================================================
    // Getters
    // =========================================================

    public CameraPose getCurrentPose() {

        return currentPose;
    }

    public String getActiveSequenceName() {

        return activeSequenceName;
    }

    public long getPlaybackProgress() {

        return playbackStartTick;
    }

    public long getPlaybackDuration() {

        return playbackDurationTicks;
    }
}