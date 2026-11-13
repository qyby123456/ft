package net.farmingtales.ftdungeonchallenges;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class ArenaChallengeCommands {
    private ArenaChallengeCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("ftchallenge")
                .executes(context -> {
                    context.getSource().sendSuccess(() -> Component.translatable("challenge.ftdungeonmobs.help"), false);
                    return 1;
                });
        for (String action : new String[]{"start", "join", "status", "stop"}) {
            root.then(Commands.literal(action)
                    .executes(context -> execute(context.getSource(), action, lookedAt(context.getSource())))
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                            .executes(context -> execute(context.getSource(), action, BlockPosArgument.getLoadedBlockPos(context, "pos")))));
        }
        root.then(Commands.literal("cancel").requires(source -> source.hasPermission(2))
                .then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(context -> {
                    boolean stopped = ArenaChallengeApi.cancel(context.getSource().getLevel(), BlockPosArgument.getLoadedBlockPos(context, "pos"));
                    context.getSource().sendSuccess(() -> Component.translatable("challenge.ftdungeonmobs.cancelled", stopped ? 1 : 0), true);
                    return stopped ? 1 : 0;
                })));
        root.then(Commands.literal("list").requires(source -> source.hasPermission(2)).executes(context -> {
            var challenges = ArenaChallengeApi.active(context.getSource().getServer());
            for (ArenaChallengeApi.Snapshot challenge : challenges) {
                context.getSource().sendSuccess(() -> Component.literal(challenge.dimension() + " " + challenge.torch().toShortString() + " | ")
                        .append(ArenaChallengeApi.describe(challenge)), false);
            }
            return challenges.size();
        }));
        root.then(Commands.literal("reload").requires(source -> source.hasPermission(2)).executes(context -> {
            try {
                ArenaChallengeConfig.reloadFromDisk();
                context.getSource().sendSuccess(() -> Component.translatable("challenge.ftdungeonmobs.reloaded"), true);
                return 1;
            } catch (RuntimeException exception) {
                throw new SimpleCommandExceptionType(Component.translatable("challenge.ftdungeonmobs.error.reload")).create();
            }
        }));
        dispatcher.register(root);
    }

    private static BlockPos lookedAt(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        HitResult hit = player.pick(6, 0, false);
        if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK) { return block.getBlockPos(); }
        throw new SimpleCommandExceptionType(Component.translatable("challenge.ftdungeonmobs.error.torch")).create();
    }

    private static int execute(CommandSourceStack source, String action, BlockPos position) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        try {
            ArenaChallengeApi.Snapshot snapshot = switch (action) {
                case "start" -> ArenaChallengeApi.start(player, position);
                case "join" -> ArenaChallengeApi.join(player, position);
                case "status" -> ArenaChallengeApi.find(source.getLevel(), position).orElseThrow(() ->
                        new IllegalArgumentException("No active arena challenge."));
                case "stop" -> {
                    boolean stopped = ArenaChallengeApi.stop(player, position);
                    source.sendSuccess(() -> Component.translatable("challenge.ftdungeonmobs.cancelled", stopped ? 1 : 0), false);
                    yield null;
                }
                default -> throw new IllegalArgumentException("Unsupported challenge command: " + action);
            };
            if (snapshot != null) { source.sendSuccess(() -> ArenaChallengeApi.describe(snapshot), false); }
            return 1;
        } catch (ArenaChallengeApi.ChallengeException exception) {
            throw new SimpleCommandExceptionType(exception.component()).create();
        } catch (IllegalArgumentException exception) {
            throw new SimpleCommandExceptionType(Component.translatable("challenge.ftdungeonmobs.error.not_active")).create();
        }
    }
}
