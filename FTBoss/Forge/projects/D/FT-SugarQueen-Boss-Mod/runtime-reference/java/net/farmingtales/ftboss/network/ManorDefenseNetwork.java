/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.event.entity.player.PlayerEvent$StartTracking
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package net.farmingtales.ftboss.network;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.boss.ai.ManorDefense;
import net.farmingtales.ftboss.client.render.ManorDefenseVisual;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorDefenseRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class ManorDefenseNetwork {
    private static final String PROTOCOL = "2";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "manor_defense"), () -> "2", "2"::equals, "2"::equals);
    private static boolean registered;
    private static long sequence;

    private ManorDefenseNetwork() {
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (registered) {
                return;
            }
            registered = true;
            CHANNEL.messageBuilder(Snapshot.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(Snapshot::encode).decoder(Snapshot::decode).consumerMainThread(Snapshot::handle).add();
        });
    }

    public static void sync(EasterBossEntity boss, ManorDefenseRules.State state) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> boss), (Object)ManorDefenseNetwork.snapshot(boss, state));
    }

    private static Snapshot snapshot(EasterBossEntity boss, ManorDefenseRules.State state) {
        long now = boss.m_9236_().m_46467_();
        return new Snapshot(boss.m_9236_().m_46472_().m_135782_(), boss.m_20148_(), boss.m_19879_(), boss.getVariant(), state == null ? 0 : state.phase, state == null ? now : state.start, state == null ? now : state.end, now, ++sequence, ManorDefense.adaptiveKind(boss));
    }

    public record Snapshot(ResourceLocation dimension, UUID uuid, int entityId, int variant, int phase, long start, long end, long sent, long sequence, int adaptiveKind) {
        public boolean valid() {
            return this.dimension != null && this.uuid != null && this.entityId >= 0 && ManorDefenseRules.profile(this.variant) != null && this.phase >= 0 && this.phase <= 4 && this.start >= 0L && this.sent >= this.start && this.end >= this.start && this.sequence > 0L && this.adaptiveKind >= 0 && this.adaptiveKind <= 2 && (this.phase == 4 || this.end - this.start <= 100L);
        }

        public static void encode(Snapshot packet, FriendlyByteBuf buffer) {
            buffer.m_130085_(packet.dimension).m_130077_(packet.uuid).m_130130_(packet.entityId).writeByte(packet.variant).writeByte(packet.phase).writeLong(packet.start).writeLong(packet.end).writeLong(packet.sent).writeLong(packet.sequence).writeByte(packet.adaptiveKind);
        }

        public static Snapshot decode(FriendlyByteBuf buffer) {
            return new Snapshot(buffer.m_130281_(), buffer.m_130259_(), buffer.m_130242_(), buffer.readUnsignedByte(), buffer.readUnsignedByte(), buffer.readLong(), buffer.readLong(), buffer.readLong(), buffer.readLong(), buffer.readUnsignedByte());
        }

        public static void handle(Snapshot packet, Supplier<NetworkEvent.Context> context) {
            if (packet.valid()) {
                DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ManorDefenseVisual.accept(packet));
            }
            context.get().setPacketHandled(true);
        }
    }

    @Mod.EventBusSubscriber(modid="ftboss")
    public static final class Tracking {
        private Tracking() {
        }

        @SubscribeEvent
        public static void start(PlayerEvent.StartTracking event) {
            Entity entity = event.getTarget();
            if (entity instanceof EasterBossEntity) {
                EasterBossEntity boss = (EasterBossEntity)entity;
                entity = event.getEntity();
                if (entity instanceof ServerPlayer) {
                    ServerPlayer player = (ServerPlayer)entity;
                    if (ManorDefenseRules.profile(boss.getVariant()) != null) {
                        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)ManorDefenseNetwork.snapshot(boss, ManorDefense.state(boss)));
                    }
                }
            }
        }
    }
}

