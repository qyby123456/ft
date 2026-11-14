/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package net.farmingtales.ftboss.youzi.motion;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.youzi.motion.YouziBodyPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class YouziBodyNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "youzi_body"), () -> "1", string -> string.equals("1") || string.equals(NetworkRegistry.ABSENT), string -> string.equals("1") || string.equals(NetworkRegistry.ABSENT));
    private static final Map<EasterBossEntity, Long> SUMMONS = new WeakHashMap<EasterBossEntity, Long>();
    private static boolean registered;

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent fMLCommonSetupEvent) {
        fMLCommonSetupEvent.enqueueWork(() -> {
            if (!registered) {
                CHANNEL.registerMessage(0, YouziBodyPacket.class, YouziBodyPacket::encode, YouziBodyPacket::decode, YouziBodyPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
                registered = true;
            }
        });
    }

    public static void summoned(boolean bl, EasterBossEntity easterBossEntity, EasterBossEntity easterBossEntity2) {
        Level level;
        if (!(bl && registered && easterBossEntity.getVariant() == 0 && !easterBossEntity.isMini() && easterBossEntity.m_6084_() && easterBossEntity2 != null && easterBossEntity2.isMini() && easterBossEntity2.getVariant() == 0 && (level = easterBossEntity.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        long l = serverLevel.m_46467_();
        if (SUMMONS.getOrDefault(easterBossEntity, -1L) == l) {
            return;
        }
        SUMMONS.put(easterBossEntity, l);
        YouziBodyPacket youziBodyPacket = new YouziBodyPacket(serverLevel.m_46472_().m_135782_().toString(), easterBossEntity.m_19879_(), easterBossEntity.m_20148_(), l);
        for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
            if (serverPlayer.f_8906_ == null || !(serverPlayer.m_20280_((Entity)easterBossEntity) <= 4096.0) || !CHANNEL.isRemotePresent(serverPlayer.f_8906_.f_9742_)) continue;
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)youziBodyPacket);
        }
    }
}

