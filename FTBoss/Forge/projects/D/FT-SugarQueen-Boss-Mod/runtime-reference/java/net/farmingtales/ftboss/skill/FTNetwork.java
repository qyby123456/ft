/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.PacketDistributor$TargetPoint
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package net.farmingtales.ftboss.skill;

import java.util.function.Predicate;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.DandanScytheFxPacket;
import net.farmingtales.ftboss.skill.ManorAssaultPacket;
import net.farmingtales.ftboss.skill.ManorDomainPacket;
import net.farmingtales.ftboss.skill.ManorIntroPacket;
import net.farmingtales.ftboss.skill.ManorRecallBoundaryPacket;
import net.farmingtales.ftboss.skill.ManorTransitionPacket;
import net.farmingtales.ftboss.skill.ManorVictoryPacket;
import net.farmingtales.ftboss.skill.NonManorWeaponPacket;
import net.farmingtales.ftboss.skill.NonManorWeaponSkills;
import net.farmingtales.ftboss.skill.ShadowSigilPacket;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class FTNetwork {
    private static final String PROTOCOL = "12";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "main"), () -> "12", (Predicate)NetworkRegistry.acceptMissingOr((String)"12"), (Predicate)NetworkRegistry.acceptMissingOr((String)"12"));
    private static boolean registered;
    public static final double NEAR_RADIUS = 64.0;

    private FTNetwork() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        CHANNEL.messageBuilder(NonManorWeaponPacket.class, 8, NetworkDirection.PLAY_TO_SERVER).encoder(NonManorWeaponPacket::encode).decoder(NonManorWeaponPacket::decode).consumerMainThread(NonManorWeaponPacket::handle).add();
        MinecraftForge.EVENT_BUS.register(NonManorWeaponSkills.class);
        CHANNEL.messageBuilder(ManorRecallBoundaryPacket.class, 7, NetworkDirection.PLAY_TO_CLIENT).encoder(ManorRecallBoundaryPacket::encode).decoder(ManorRecallBoundaryPacket::decode).consumerMainThread(ManorRecallBoundaryPacket::handle).add();
        CHANNEL.messageBuilder(ManorTransitionPacket.class, 6, NetworkDirection.PLAY_TO_CLIENT).encoder(ManorTransitionPacket::encode).decoder(ManorTransitionPacket::decode).consumerMainThread(ManorTransitionPacket::handle).add();
        CHANNEL.messageBuilder(ManorVictoryPacket.class, 5, NetworkDirection.PLAY_TO_CLIENT).encoder(ManorVictoryPacket::encode).decoder(ManorVictoryPacket::decode).consumerMainThread(ManorVictoryPacket::handle).add();
        CHANNEL.messageBuilder(SpellVisualPacket.class, 4, NetworkDirection.PLAY_TO_CLIENT).encoder(SpellVisualPacket::encode).decoder(SpellVisualPacket::decode).consumerMainThread(SpellVisualPacket::handle).add();
        CHANNEL.messageBuilder(DandanScytheFxPacket.class, 3, NetworkDirection.PLAY_TO_CLIENT).encoder(DandanScytheFxPacket::encode).decoder(DandanScytheFxPacket::decode).consumerMainThread(DandanScytheFxPacket::handle).add();
        CHANNEL.messageBuilder(ShadowSigilPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(ShadowSigilPacket::encode).decoder(ShadowSigilPacket::decode).consumerMainThread(ShadowSigilPacket::handle).add();
        CHANNEL.messageBuilder(ManorIntroPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT).encoder(ManorIntroPacket::encode).decoder(ManorIntroPacket::decode).consumerMainThread(ManorIntroPacket::handle).add();
        ManorDomainPacket.register();
        ManorAssaultPacket.register();
    }

    public static void sendSigilNear(ServerPlayer source, double x, double y, double z, boolean pillar) {
        Level level;
        if (source == null || !((level = source.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        ShadowSigilPacket msg = new ShadowSigilPacket(x, y, z, pillar);
        try {
            CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(null, x, y, z, 4096.0, sl.m_46472_())), (Object)msg);
            FTBossMod.LOGGER.info("[FTBoss][\u7f51\u7edc] \u5df2\u53d1\u6697\u5f71\u9547\u5370\u5305: ({}, {}, {}) \u67f1={}", new Object[]{String.format("%.2f", x), String.format("%.2f", y), String.format("%.2f", z), pillar});
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][\u7f51\u7edc] \u4f20\u9001\u6cd5\u9635\u5305\u53d1\u9001\u5931\u8d25: {}", (Object)t.toString());
        }
    }
}

