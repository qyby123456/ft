package net.farmingtales.ftdungeonmobs;

import com.mojang.brigadier.CommandDispatcher;
import cn.ft.biomedungeon.runtime.BiomeDungeonBackend;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class DungeonGeneratorCommands {
    private DungeonGeneratorCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ftdungeon").requires(source -> source.hasPermission(2))
                .then(Commands.literal("generator")
                        .then(Commands.literal("list").executes(context -> {
                            for (var provider : DungeonGenerators.entries()) {
                                context.getSource().sendSuccess(() -> Component.literal(provider.id() + " ").append(provider.name()), false);
                            }
                            return DungeonGenerators.entries().size();
                        }))
                        .then(Commands.literal("status").executes(context -> status(context.getSource(), false)))
                        .then(Commands.literal("prepare").then(Commands.literal("amethyst_biome")
                                .executes(context -> status(context.getSource(), true))))));
    }

    private static int status(CommandSourceStack source, boolean prepare) {
        var level = source.getServer().getLevel(DungeonGateTravel.DUNGEON);
        if (level == null) {
            source.sendFailure(Component.translatable("message.ftdungeonmobs.rooms.dimension"));
            return 0;
        }
        DungeonGenerators.Preparation preparation = prepare
                ? DungeonGenerators.get(DungeonGenerators.AMETHYST_BIOME).prepare(level) : null;
        if (preparation != null && preparation.state() == DungeonGenerators.State.FAILED) {
            source.sendFailure(preparation.message());
        }
        var status = BiomeDungeonBackend.current(level);
        source.sendSuccess(() -> Component.literal("amethyst_biome state=" + status.state() + " stage=" + status.stage()
                + " completed=" + status.completed() + "/" + status.total() + " observedTicks=" + status.observedTicks()
                + " instance=" + status.instanceId() + " error=" + status.error()), false);
        return status.state() == BiomeDungeonBackend.State.FAILED
                || preparation != null && preparation.state() == DungeonGenerators.State.FAILED ? 0 : 1;
    }
}
