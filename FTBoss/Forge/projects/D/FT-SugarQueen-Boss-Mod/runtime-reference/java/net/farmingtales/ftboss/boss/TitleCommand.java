/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.commands.arguments.EntityArgument
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.event.RegisterCommandsEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossAdvancements;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class TitleCommand {
    private TitleCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent e) {
        e.getDispatcher().register((LiteralArgumentBuilder)Commands.m_82127_((String)"ftboss").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"title").requires(src -> src.m_6761_(2))).executes(ctx -> TitleCommand.grant(((CommandSourceStack)ctx.getSource()).m_81375_(), null))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.m_82129_((String)"player", (ArgumentType)EntityArgument.m_91466_()).executes(ctx -> TitleCommand.grant(EntityArgument.m_91474_((CommandContext)ctx, (String)"player"), null))).then(Commands.m_82127_((String)"memories_end").executes(ctx -> TitleCommand.grant(EntityArgument.m_91474_((CommandContext)ctx, (String)"player"), "title_memories_end")))).then(Commands.m_82127_((String)"food_boss_slayer_all").executes(ctx -> TitleCommand.grant(EntityArgument.m_91474_((CommandContext)ctx, (String)"player"), "title_food_boss_slayer_all")))).then(Commands.m_82127_((String)"manor_impossible").executes(ctx -> TitleCommand.grant(EntityArgument.m_91474_((CommandContext)ctx, (String)"player"), "title_manor_impossible"))))));
    }

    private static int grant(ServerPlayer target, String titleId) {
        List<String> ids = titleId != null ? List.of(titleId) : List.copyOf(BossAdvancements.titleAdvancementIds());
        int granted = 0;
        int notEligible = 0;
        for (String id : ids) {
            String gate = BossAdvancements.titleGate(id);
            if (gate == null) continue;
            if (!gate.isEmpty() && !BossAdvancements.hasEarned(target, gate)) {
                ++notEligible;
                target.m_213846_((Component)Component.m_237113_((String)("\u00a77\u3010\u5934\u8854\u3011\u00a7c\u8fd8\u5dee\u6210\u5c31\u00a7f " + TitleCommand.gateName(gate) + "\u00a7c\uff0c\u6682\u4e0d\u8865\u53d1\u3002")));
                continue;
            }
            boolean ok = BossAdvancements.replayTitle(target, id);
            if (!ok) continue;
            ++granted;
        }
        if (granted > 0) {
            target.m_213846_((Component)Component.m_237113_((String)("\u00a7d\u3010\u5934\u8854\u3011\u00a7f\u5df2\u8865\u53d1 \u00a7e" + granted + " \u00a7f\u4e2a\u5934\u8854 \u2014\u2014 \u00a77\u7528\u5934\u8854\u754c\u9762\uff08\u9ed8\u8ba4\u5feb\u6377\u952e \u00a7fT\u00a77\uff09\u9009\u62e9\u8981\u663e\u793a\u7684\u79f0\u53f7\u3002")));
            FTBossMod.LOGGER.info("[FTBoss][\u5934\u8854] \u4e3a {} \u8865\u53d1 {} \u4e2a\u5934\u8854", (Object)target.m_7755_().getString(), (Object)granted);
        } else if (notEligible == 0) {
            target.m_213846_((Component)Component.m_237113_((String)"\u00a77\u3010\u5934\u8854\u3011\u6ca1\u6709\u53ef\u8865\u53d1\u7684\u5934\u8854\u3002"));
        }
        return granted;
    }

    private static String gateName(String gateId) {
        return switch (gateId) {
            case "memories_end" -> "\u56de\u5fc6\u7684\u7ec8\u7ed3\uff08\u6218\u80dc\u5e84\u56ed\uff09";
            case "food_boss_slayer_all" -> "\u7f8e\u98df\u7684\u4fd8\u864f\uff08\u6218\u80dc\u5341\u4f4d\u975e\u5e84\u56ed Boss\uff09";
            default -> gateId;
        };
    }
}

