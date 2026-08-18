package com.sydneyfoe.techassessment.camera;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import com.sydneyfoe.techassessment.LeadProductionEngineerTechnicalAssessment;

//@EventBusSubscriber(modid = LeadProductionEngineerTechnicalAssessment.MODID)
public class CameraCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("camkey")
            .then(Commands.literal("add")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(context -> addKeyframe(context.getSource(), StringArgumentType.getString(context, "name")))))
            .then(Commands.literal("play")
                .then(Commands.argument("sequence", StringArgumentType.word())
                    .then(Commands.argument("duration", IntegerArgumentType.integer(1, 300))
                        .executes(context -> playSequence(context.getSource(), 
                            StringArgumentType.getString(context, "sequence"), 
                            IntegerArgumentType.getInteger(context, "duration"))))
                    .executes(context -> playSequence(context.getSource(), 
                        StringArgumentType.getString(context, "sequence"), 10))))
            .then(Commands.literal("list")
                .executes(context -> listSequences(context.getSource())))
        );
    }

    private static int addKeyframe(CommandSourceStack source, String name) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can add keyframes"));
            return 0;
        }

        CameraSequenceManager manager = LeadProductionEngineerTechnicalAssessment.getCameraManager();
        if (manager == null) {
            source.sendFailure(Component.literal("Camera system not initialized"));
            return 0;
        }

        Vec3 pos = player.getEyePosition();
        float pitch = player.getXRot();
        float yaw = player.getYRot();
        
        CameraPose pose = new CameraPose(pos.x, pos.y, pos.z, pitch, yaw, 0.0f);
        CameraSequence seq = manager.getSequence(name);
        
        if (seq == null) {
            seq = new CameraSequence(new java.util.ArrayList<>());
        }
        
        long nextTick = seq.keyframes.isEmpty() ? 0 : seq.keyframes.get(seq.keyframes.size() - 1).tick + 20;
        seq.keyframes.add(new CameraKeyframe(nextTick, pose));
        
        manager.addOrUpdateSequence(name, seq);
        source.sendSuccess(() -> Component.literal(String.format("Keyframe added to sequence '%s' at tick %d", name, nextTick)), true);
        return 1;
    }

    private static int playSequence(CommandSourceStack source, String name, int durationSeconds) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can play sequences"));
            return 0;
        }

        CameraSequenceManager manager = LeadProductionEngineerTechnicalAssessment.getCameraManager();
        if (manager == null) {
            source.sendFailure(Component.literal("Camera system not initialized"));
            return 0;
        }

        if (manager.getSequence(name) == null) {
            source.sendFailure(Component.literal(String.format("Sequence '%s' not found", name)));
            return 0;
        }

        manager.startPlayback(name);
        source.sendSuccess(() -> Component.literal(String.format("Playing sequence '%s' for %d seconds", name, durationSeconds)), true);
        return 1;
    }

    private static int listSequences(CommandSourceStack source) {
        CameraSequenceManager manager = LeadProductionEngineerTechnicalAssessment.getCameraManager();
        if (manager == null) {
            source.sendFailure(Component.literal("Camera system not initialized"));
            return 0;
        }

        var sequences = manager.getAllSequences();
        if (sequences.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No sequences saved"), false);
            return 0;
        }

        StringBuilder sb = new StringBuilder("Camera sequences:\n");
        for (var entry : sequences.entrySet()) {
            sb.append(String.format("  - %s (%d keyframes)\n", entry.getKey(), entry.getValue().keyframes.size()));
        }
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }
}
