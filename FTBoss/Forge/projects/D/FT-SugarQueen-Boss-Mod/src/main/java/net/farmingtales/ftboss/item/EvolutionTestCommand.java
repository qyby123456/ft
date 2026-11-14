package net.farmingtales.ftboss.item;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Creative-only progression control for testing statue evolution thresholds. */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EvolutionTestCommand {
    private EvolutionTestCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("ftboss")
                .then(Commands.literal("evolution").requires(source -> source.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer(0, 100_000))
                                        .executes(context -> FoodWeaponEvolution.setEvolutionExperienceForTesting(
                                                context.getSource().getPlayerOrException(),
                                                IntegerArgumentType.getInteger(context, "value"), false))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("value", IntegerArgumentType.integer(-100_000, 100_000))
                                        .executes(context -> FoodWeaponEvolution.setEvolutionExperienceForTesting(
                                                context.getSource().getPlayerOrException(),
                                                IntegerArgumentType.getInteger(context, "value"), true))))));
    }
}
