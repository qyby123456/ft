/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$StartTracking
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneWardClock;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneWardPacket;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ArcaneWardNetwork {
    private static final Map<UUID, Watch> watches = new LinkedHashMap<UUID, Watch>();
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "qizai_ward_v5"), () -> "1", (Predicate)NetworkRegistry.acceptMissingOr((String)"1"), (Predicate)NetworkRegistry.acceptMissingOr((String)"1"));

    private ArcaneWardNetwork() {
    }

    public static boolean protectedState(EasterBossEntity easterBossEntity) {
        return ArcaneWardClock.eligible(easterBossEntity.getVariant(), easterBossEntity.m_6084_(), easterBossEntity.m_213877_(), easterBossEntity.isQizaiProtectedCasting(), easterBossEntity.m_20147_());
    }

    private static ArcaneWardPacket packet(EasterBossEntity easterBossEntity) {
        return new ArcaneWardPacket(easterBossEntity.m_19879_(), easterBossEntity.m_20148_(), easterBossEntity.m_9236_().m_46472_().m_135782_(), easterBossEntity.m_9236_().m_46467_(), ArcaneWardNetwork.protectedState(easterBossEntity));
    }

    @Mod.EventBusSubscriber(modid="ftboss")
    public static final class Events {
        private Events() {
        }

        @SubscribeEvent
        public static void observed(LivingEvent.LivingTickEvent livingTickEvent) {
            EasterBossEntity easterBossEntity;
            Object object = livingTickEvent.getEntity();
            if (!(object instanceof EasterBossEntity) || (easterBossEntity = (EasterBossEntity)object).m_9236_().m_5776_() || easterBossEntity.getVariant() != 11) {
                return;
            }
            object = easterBossEntity.m_20148_();
            if (!watches.containsKey(object) && watches.size() >= 128) {
                watches.remove(watches.keySet().iterator().next());
            }
            watches.putIfAbsent((UUID)object, new Watch(easterBossEntity, false, Long.MIN_VALUE));
        }

        @SubscribeEvent
        public static void tracking(PlayerEvent.StartTracking startTracking) {
            EasterBossEntity easterBossEntity;
            Entity entity = startTracking.getTarget();
            if (entity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)entity).getVariant() == 11 && (entity = startTracking.getEntity()) instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)entity;
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)ArcaneWardNetwork.packet(easterBossEntity));
            }
        }

        @SubscribeEvent
        public static void tick(TickEvent.ServerTickEvent serverTickEvent) {
            if (serverTickEvent.phase != TickEvent.Phase.END) {
                return;
            }
            Iterator<Map.Entry<UUID, Watch>> iterator = watches.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Watch> entry = iterator.next();
                Watch watch = entry.getValue();
                EasterBossEntity easterBossEntity = watch.boss();
                long l = easterBossEntity.m_9236_().m_46467_();
                boolean bl = ArcaneWardNetwork.protectedState(easterBossEntity);
                if (bl != watch.active() || watch.sent() == Long.MIN_VALUE || l < watch.sent() || l - watch.sent() >= 10L) {
                    CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> easterBossEntity), (Object)ArcaneWardNetwork.packet(easterBossEntity));
                    entry.setValue(new Watch(easterBossEntity, bl, l));
                }
                if (!easterBossEntity.m_213877_() && easterBossEntity.m_6084_() && easterBossEntity.getVariant() == 11) continue;
                iterator.remove();
            }
        }

        @SubscribeEvent
        public static void stopped(ServerStoppedEvent serverStoppedEvent) {
            watches.clear();
        }
    }

    @Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Setup {
        private Setup() {
        }

        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent fMLCommonSetupEvent) {
            CHANNEL.messageBuilder(ArcaneWardPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(ArcaneWardPacket::encode).decoder(ArcaneWardPacket::decode).consumerMainThread(ArcaneWardPacket::handle).add();
        }
    }

    private record Watch(EasterBossEntity boss, boolean active, long sent) {
    }
}

