/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraftforge.event.RegisterCommandsEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.item;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class EvolutionTestCommand {
    private EvolutionTestCommand() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register((LiteralArgumentBuilder)Commands.m_82127_((String)"ftboss").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"evolution").requires(source -> source.m_6761_(2))).then(Commands.m_82127_((String)"set").then(Commands.m_82129_((String)"value", (ArgumentType)IntegerArgumentType.integer((int)0, (int)100000)).executes(context -> FoodWeaponEvolution.setEvolutionExperienceForTesting(((CommandSourceStack)context.getSource()).m_81375_(), IntegerArgumentType.getInteger((CommandContext)context, (String)"value"), false))))).then(Commands.m_82127_((String)"add").then(Commands.m_82129_((String)"value", (ArgumentType)IntegerArgumentType.integer((int)-100000, (int)100000)).executes(context -> FoodWeaponEvolution.setEvolutionExperienceForTesting(((CommandSourceStack)context.getSource()).m_81375_(), IntegerArgumentType.getInteger((CommandContext)context, (String)"value"), true))))));
    }
}

