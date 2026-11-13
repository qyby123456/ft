package net.farmingtales.ftdungeonmobs;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public final class DungeonBossGateCommands {
    private DungeonBossGateCommands() {}

    @FunctionalInterface
    private interface Operation { int run() throws CommandSyntaxException; }

    private static int checked(Operation operation) throws CommandSyntaxException {
        try {
            return operation.run();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new SimpleCommandExceptionType(Component.literal(exception.getMessage())).create();
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ftdungeon").requires(source -> source.hasPermission(2))
                .then(Commands.literal("bossgate").then(create()).then(bind()).then(remove())
                        .then(Commands.literal("status").executes(context -> checked(() -> status(context.getSource()))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("create").then(Commands.argument("gate", BlockPosArgument.blockPos())
                .then(Commands.argument("facing", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                new String[]{"north", "south", "east", "west"}, builder))
                        .executes(context -> checked(() -> {
                            DungeonBossGateApi.create(context.getSource().getLevel(),
                                    BlockPosArgument.getBlockPos(context, "gate"),
                                    Direction.byName(StringArgumentType.getString(context, "facing")));
                            context.getSource().sendSuccess(() -> Component.translatable(
                                    "message.ftdungeonmobs.boss_gate.created"), true);
                            return 1;
                        }))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> bind() {
        return Commands.literal("bind").then(Commands.argument("gate", BlockPosArgument.blockPos())
                .then(Commands.argument("room_min", BlockPosArgument.blockPos())
                        .then(Commands.argument("room_max", BlockPosArgument.blockPos())
                                .then(Commands.argument("arrival", BlockPosArgument.blockPos())
                                        .executes(context -> checked(() -> {
                                            ServerLevel level = context.getSource().getLevel();
                                            BlockPos gate = BlockPosArgument.getLoadedBlockPos(context, "gate");
                                            var state = level.getBlockState(gate);
                                            if (!state.is(FTDungeonMobs.DUNGEON_BOSS_GATE.get())
                                                    || state.getValue(DungeonGateBlock.PART) != DungeonGateBlock.ANCHOR_PART) {
                                                throw new IllegalArgumentException("Select the bottom-middle block of a Boss gate.");
                                            }
                                            DungeonBossGateApi.bind(level, new DungeonBossGateApi.Binding(gate,
                                                    state.getValue(DungeonGateBlock.FACING),
                                                    BlockPosArgument.getBlockPos(context, "room_min"),
                                                    BlockPosArgument.getBlockPos(context, "room_max"),
                                                    BlockPosArgument.getBlockPos(context, "arrival")));
                                            context.getSource().sendSuccess(() -> Component.translatable(
                                                    "message.ftdungeonmobs.boss_gate.bound"), true);
                                            return 1;
                                        }))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> remove() {
        return Commands.literal("remove").then(Commands.argument("gate", BlockPosArgument.blockPos())
                .executes(context -> checked(() -> {
                    boolean removed = DungeonBossGateApi.unbind(context.getSource().getLevel(),
                            BlockPosArgument.getBlockPos(context, "gate"));
                    context.getSource().sendSuccess(() -> Component.translatable(
                            "message.ftdungeonmobs.boss_gate.removed", removed ? 1 : 0), true);
                    return removed ? 1 : 0;
                })));
    }

    private static int status(CommandSourceStack source) {
        DungeonBossGateData data = DungeonBossGateApi.state(source.getLevel());
        source.sendSuccess(() -> Component.translatable("message.ftdungeonmobs.boss_gate.status",
                data.bindings().size(), data.engaged()), false);
        for (DungeonBossGateApi.Binding binding : data.bindings()) {
            source.sendSuccess(() -> Component.literal(binding.gate().toShortString() + " / " + binding.facing().getName()
                    + " -> " + binding.minimum().toShortString() + " .. " + binding.maximum().toShortString()
                    + " / arrival " + binding.arrival().toShortString()), false);
        }
        return data.bindings().size();
    }
}
