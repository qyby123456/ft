/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package net.farmingtales.ftboss.youzi.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import java.util.Optional;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.youzi.spells.YouziSpellPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class YouziSpellNetwork {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "youzi_spell_runtime"), () -> "1", string -> VERSION.equals(string) || NetworkRegistry.ABSENT.equals(string) || NetworkRegistry.ACCEPTVANILLA.equals(string), string -> VERSION.equals(string) || NetworkRegistry.ABSENT.equals(string) || NetworkRegistry.ACCEPTVANILLA.equals(string));
    private static boolean registered;

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent fMLCommonSetupEvent) {
        fMLCommonSetupEvent.enqueueWork(YouziSpellNetwork::register);
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        CHANNEL.registerMessage(0, YouziSpellPacket.class, YouziSpellPacket::encode, YouziSpellPacket::decode, YouziSpellPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registered = true;
    }

    public static void emit(ServerLevel serverLevel, SpellVisualPacket spellVisualPacket) {
        LivingEntity livingEntity;
        if (!(registered && spellVisualPacket.valid() && YouziSpellNetwork.supports(spellVisualPacket.spell().id))) {
            return;
        }
        Entity entity = serverLevel.m_8791_(spellVisualPacket.owner());
        if (!(entity instanceof LivingEntity) || !(livingEntity = (LivingEntity)entity).m_6084_()) {
            return;
        }
        MagicData magicData = MagicData.getPlayerMagicData((LivingEntity)livingEntity);
        long l = serverLevel.m_46467_();
        int n = magicData.isCasting() ? Math.max(1, magicData.getCastDuration()) : 0;
        long l2 = n == 0 ? l : Math.max(0L, l - (long)n + (long)magicData.getCastDurationRemaining());
        YouziSpellPacket youziSpellPacket = new YouziSpellPacket(spellVisualPacket, l, l2, n, BossSpellTuning.radiusScale(livingEntity));
        for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
            double d;
            if (serverPlayer.f_8906_ == null || !CHANNEL.isRemotePresent(serverPlayer.f_8906_.f_9742_) || !((d = Math.min(serverPlayer.m_20275_(spellVisualPacket.start().f_82479_, spellVisualPacket.start().f_82480_, spellVisualPacket.start().f_82481_), serverPlayer.m_20275_(spellVisualPacket.end().f_82479_, spellVisualPacket.end().f_82480_, spellVisualPacket.end().f_82481_))) <= 4096.0)) continue;
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)youziSpellPacket);
        }
    }

    private static boolean supports(String string) {
        return string.equals("effigy_volley") || string.equals("effigy_ward") || string.equals("effigy_procession");
    }
}

