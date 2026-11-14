package net.farmingtales.ftboss.network;

import java.util.List;
import java.util.function.Supplier;
import net.farmingtales.ftboss.util.ZicaiVoidRules;
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

@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ZicaiVoidNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("ftboss", "zicai_void"),
            () -> "1", "1"::equals, "1"::equals);

    private ZicaiVoidNetwork() {}

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CHANNEL.messageBuilder(Burst.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Burst::encode).decoder(Burst::decode).consumerMainThread(Burst::handle).add());
    }

    public static void show(ServerLevel level, LivingEntity caster, boolean fountain, List<Mark> marks) {
        Burst burst = new Burst(level.dimension().location(), caster.getId(), level.getGameTime(), fountain, List.copyOf(marks));
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(caster) <= ZicaiVoidRules.VIEW_RANGE * ZicaiVoidRules.VIEW_RANGE)
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), burst);
        }
    }

    public record Mark(Vec3 position, int warning, int strike) {}

    public record Burst(ResourceLocation dimension, int owner, long born, boolean fountain, List<Mark> marks) {
        public static void encode(Burst burst, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(burst.dimension).writeVarInt(burst.owner).writeLong(burst.born).writeBoolean(burst.fountain);
            buffer.writeCollection(burst.marks, (output, mark) -> {
                output.writeDouble(mark.position.x).writeDouble(mark.position.y).writeDouble(mark.position.z);
                output.writeVarInt(mark.warning).writeVarInt(mark.strike);
            });
        }

        public static Burst decode(FriendlyByteBuf buffer) {
            return new Burst(buffer.readResourceLocation(), buffer.readVarInt(), buffer.readLong(), buffer.readBoolean(),
                    buffer.readCollection(FriendlyByteBuf.limitValue(java.util.ArrayList::new, ZicaiVoidRules.MAX_MARKS),
                            input -> new Mark(new Vec3(input.readDouble(), input.readDouble(), input.readDouble()),
                                    input.readVarInt(), input.readVarInt())));
        }

        public static void handle(Burst burst, Supplier<NetworkEvent.Context> context) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> net.farmingtales.ftboss.client.render.ZicaiVoidVisual.accept(burst));
            context.get().setPacketHandled(true);
        }
    }
}
