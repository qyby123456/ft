package net.farmingtales.ftboss.network;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.boss.ai.ManorFarthestSigilRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** 独立的多人庄园法阵同步通道，不修改既有场地攻击数据包。 */
@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ManorFarthestSigilNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("ftboss", "manor_farthest_sigil"),
            () -> "1", "1"::equals, "1"::equals);

    private ManorFarthestSigilNetwork() {}

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CHANNEL.messageBuilder(Burst.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Burst::encode).decoder(Burst::decode).consumerMainThread(Burst::handle).add());
    }

    public static void show(ServerLevel level, LivingEntity owner, Vec3 center, double yaw, int style, int phase) {
        Burst burst = new Burst(level.dimension().location(), owner.getUUID(), owner.getId(), level.getGameTime(), center, yaw, style, phase);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(center) <= ManorFarthestSigilRules.VIEW_RANGE * ManorFarthestSigilRules.VIEW_RANGE || phase == 2)
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), burst);
        }
    }

    public record Burst(ResourceLocation dimension, UUID owner, int ownerId, long born, Vec3 center, double yaw, int style, int phase) {
        public boolean valid() {
            return ManorFarthestSigilRules.finite(center.x, center.y, center.z) && Double.isFinite(yaw)
                    && style >= 0 && style < 3 && phase >= 0 && phase < 3;
        }

        public static void encode(Burst burst, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(burst.dimension).writeUUID(burst.owner).writeVarInt(burst.ownerId).writeLong(burst.born);
            buffer.writeDouble(burst.center.x).writeDouble(burst.center.y).writeDouble(burst.center.z).writeDouble(burst.yaw);
            buffer.writeVarInt(burst.style).writeVarInt(burst.phase);
        }

        public static Burst decode(FriendlyByteBuf buffer) {
            return new Burst(buffer.readResourceLocation(), buffer.readUUID(), buffer.readVarInt(), buffer.readLong(),
                    new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readDouble(),
                    buffer.readVarInt(), buffer.readVarInt());
        }

        public static void handle(Burst burst, Supplier<NetworkEvent.Context> context) {
            if (burst.valid()) DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> net.farmingtales.ftboss.client.render.ManorFarthestSigilVisual.accept(burst));
            context.get().setPacketHandled(true);
        }
    }
}
