package net.farmingtales.ftdungeonmobs;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class DungeonCommands {
    private DungeonCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        DungeonDifficultyCommands.register(dispatcher);
        DungeonGeneratorCommands.register(dispatcher);
        dispatcher.register(Commands.literal("ftdungeon").requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawns").then(Commands.literal("status").executes(context -> {
                    ServerLevel level = context.getSource().getServer().getLevel(DungeonGateTravel.DUNGEON);
                    if (level == null) {
                        context.getSource().sendFailure(Component.translatable("message.ftdungeonmobs.rooms.dimension"));
                        return 0;
                    }
                    context.getSource().sendSuccess(() -> DungeonSpawnPoints.status(level), false);
                    return 1;
                }))));
        dispatcher.register(Commands.literal("ftdungeon").requires(source -> source.hasPermission(2))
                .then(Commands.literal("rooms")
                        .then(Commands.literal("build").executes(context -> {
                            ServerLevel level = context.getSource().getServer().getLevel(DungeonGateTravel.DUNGEON);
                            DungeonRooms.Result result = level == null ? new DungeonRooms.Result("dimension", "") : DungeonRooms.prepare(level);
                            boolean successful = result.successful();
                            if (successful) {
                                context.getSource().sendSuccess(result::message, true);
                            } else {
                                context.getSource().sendFailure(result.message());
                            }
                            if (level != null) {
                                for (DungeonRoomGallery.Report report : DungeonRoomGallery.prepareAll(level, false)) {
                                    if (report.successful()) {
                                        context.getSource().sendSuccess(report::message, true);
                                    } else {
                                        context.getSource().sendFailure(report.message());
                                        successful = false;
                                    }
                                }
                            }
                            return successful ? 1 : 0;
                        }))
                        .then(Commands.literal("status").executes(context -> {
                            ServerLevel level = context.getSource().getServer().getLevel(DungeonGateTravel.DUNGEON);
                            String status = level == null ? "dimension" : DungeonRoomsData.get(level).ready() ? "ready" : "pending";
                            context.getSource().sendSuccess(() -> Component.translatable("message.ftdungeonmobs.rooms." + status), false);
                            boolean ready = level != null && DungeonRoomsData.get(level).ready();
                            if (level != null) {
                                for (DungeonRoomGallery.Report report : DungeonRoomGallery.prepareAll(level, true)) {
                                    context.getSource().sendSuccess(report::message, false);
                                    ready &= report.code().equals("ready");
                                }
                            }
                            return ready ? 1 : 0;
                        }))));
        dispatcher.register(Commands.literal("ftdungeonreturn")
                .executes(context -> DungeonGateTravel.returnToOrigin(context.getSource().getPlayerOrException()) ? 1 : 0));
        dispatcher.register(Commands.literal("ftdungeon").requires(source -> source.hasPermission(2))
                .then(Commands.literal("animation")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(Commands.argument("animation", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                new String[]{"idle", "walk", "attack", "dash", "summon", "animation2"}, builder))
                                        .executes(context -> {
                                            String animation = StringArgumentType.getString(context, "animation");
                                            int count = 0;
                                            for (Entity entity : EntityArgument.getEntities(context, "targets")) {
                                                if (entity instanceof DungeonSkeletonEntity skeleton && skeleton.variant().hasAnimation(animation)) {
                                                    skeleton.previewAnimation(animation);
                                                    count++;
                                                }
                                            }
                                            if (count == 0) {
                                                throw new SimpleCommandExceptionType(Component.literal("没有选中支持此动画的 FT 地牢怪物。")).create();
                                            }
                                            int affected = count;
                                            context.getSource().sendSuccess(() -> Component.literal("FT地牢怪物：已为 " + affected + " 个实体预览动画 " + animation + "（不造成伤害）。"), true);
                                            return affected;
                                        })))));
    }
}
