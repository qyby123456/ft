package net.farmingtales.ftboss.network;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorDefenseRules;
import net.farmingtales.ftboss.util.ManorDefenseRules.State;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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

/** 防御 S2C：2026-10-13，独立协议与自动注册，保留正式 FTBossMod/FTNetwork 字节码。 */
@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ManorDefenseNetwork {
    private static final String PROTOCOL = "2";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("ftboss", "manor_defense"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    private static boolean registered;
    private static long sequence;

    private ManorDefenseNetwork() {}

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (registered) return;
            registered = true;
            CHANNEL.messageBuilder(Snapshot.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(Snapshot::encode).decoder(Snapshot::decode).consumerMainThread(Snapshot::handle).add();
        });
    }

    public static void sync(EasterBossEntity boss, State state) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> boss), snapshot(boss, state));
    }

    private static Snapshot snapshot(EasterBossEntity boss, State state) {
        long now = boss.level().getGameTime();
        return new Snapshot(boss.level().dimension().location(), boss.getUUID(), boss.getId(), boss.getVariant(),
                state == null ? ManorDefenseRules.IDLE : state.phase, state == null ? now : state.start,
                state == null ? now : state.end, now, ++sequence,
                net.farmingtales.ftboss.boss.ai.ManorDefense.adaptiveKind(boss));
    }

    public record Snapshot(ResourceLocation dimension, UUID uuid, int entityId, int variant, int phase,
                           long start, long end, long sent, long sequence, int adaptiveKind) {
        public boolean valid() {
            return dimension != null && uuid != null && entityId >= 0 && ManorDefenseRules.profile(variant) != null
                    && phase >= ManorDefenseRules.IDLE && phase <= ManorDefenseRules.NATIVE && start >= 0
                    && sent >= start && end >= start && sequence > 0
                    && adaptiveKind >= 0 && adaptiveKind <= 2
                    && (phase == ManorDefenseRules.NATIVE || end - start <= 100);
        }

        public static void encode(Snapshot packet, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(packet.dimension).writeUUID(packet.uuid).writeVarInt(packet.entityId)
                    .writeByte(packet.variant).writeByte(packet.phase).writeLong(packet.start).writeLong(packet.end)
                    .writeLong(packet.sent).writeLong(packet.sequence).writeByte(packet.adaptiveKind);
        }

        public static Snapshot decode(FriendlyByteBuf buffer) {
            return new Snapshot(buffer.readResourceLocation(), buffer.readUUID(), buffer.readVarInt(), buffer.readUnsignedByte(),
                    buffer.readUnsignedByte(), buffer.readLong(), buffer.readLong(), buffer.readLong(), buffer.readLong(), buffer.readUnsignedByte());
        }

        public static void handle(Snapshot packet, Supplier<NetworkEvent.Context> context) {
            if (packet.valid()) DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> net.farmingtales.ftboss.client.render.ManorDefenseVisual.accept(packet));
            context.get().setPacketHandled(true);
        }
    }

    @Mod.EventBusSubscriber(modid = "ftboss")
    public static final class Tracking {
        private Tracking() {}

        @SubscribeEvent
        public static void start(PlayerEvent.StartTracking event) {
            if (event.getTarget() instanceof EasterBossEntity boss && event.getEntity() instanceof ServerPlayer player
                    && ManorDefenseRules.profile(boss.getVariant()) != null) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), snapshot(boss,
                        net.farmingtales.ftboss.boss.ai.ManorDefense.state(boss)));
            }
        }
    }
}
