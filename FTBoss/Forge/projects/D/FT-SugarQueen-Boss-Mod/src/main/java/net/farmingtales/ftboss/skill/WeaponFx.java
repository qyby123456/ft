package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.farmingtales.ftboss.util.WeaponFxRules.Kind;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WeaponFx {
    private static final String LAST_HIT = "ftboss:weapon_fx_hit";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FTBossMod.MOD_ID, "weapon_fx"), () -> "2",
            NetworkRegistry.acceptMissingOr("2"), NetworkRegistry.acceptMissingOr("2"));

    private WeaponFx() {}

    public static void burst(ServerLevel level, Vec3 center, double radius, Theme theme) {
        send(level, Kind.BLAST, theme, center, center, radius, 0);
    }

    public static void ring(ServerLevel level, Vec3 center, double radius, Theme theme) {
        send(level, Kind.RING, theme, center, center, radius, 0);
    }

    public static void beam(ServerLevel level, Vec3 start, Vec3 end, Theme theme) {
        send(level, Kind.BEAM, theme, start, end, 0.15, 0);
    }

    public static void send(ServerLevel level, Kind kind, Theme theme, Vec3 start, Vec3 end, double radius, float yaw) {
        WeaponFxPacket packet = new WeaponFxPacket(level.dimension().location(), kind, theme, start, end, (float) radius, yaw);
        if (!packet.valid()) return;
        Vec3 segment = end.subtract(start);
        double length = segment.lengthSqr();
        double range = WeaponFxRules.VIEW_DISTANCE + radius;
        for (ServerPlayer observer : level.players()) {
            double along = length < 1.0E-8 ? 0 : Math.max(0, Math.min(1, observer.position().subtract(start).dot(segment) / length));
            if (observer.position().distanceToSqr(start.add(segment.scale(along))) <= range * range
                    && CHANNEL.isRemotePresent(observer.connection.connection)) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> observer), packet);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void hit(LivingDamageEvent event) {
        if (event.getAmount() <= 0 || !(event.getSource().getDirectEntity() instanceof ServerPlayer player)
                || !event.getSource().is(DamageTypes.PLAYER_ATTACK) || player.isSpectator()) return;
        var weapon = player.getMainHandItem();
        ResourceLocation item = ForgeRegistries.ITEMS.getKey(weapon.getItem());
        String identifier = item == null ? null : item.toString();
        boolean gourmet = "ftboss:gourmet_blank_blade".equals(identifier);
        if (!gourmet && net.farmingtales.ftboss.boss.ai.BossHitFeedback.supports(event.getEntity())) return;
        if (WeaponFxRules.isDoraGunItem(identifier)) return;
        Theme theme = WeaponFxRules.forItem(identifier);
        if (theme == null) return;
        long now = player.serverLevel().getGameTime();
        var tag = player.getPersistentData();
        if (tag.contains(LAST_HIT) && now >= tag.getLong(LAST_HIT) && now - tag.getLong(LAST_HIT) < 6) return;
        tag.putLong(LAST_HIT, now);
        Vec3 center = event.getEntity().getBoundingBox().getCenter();
        Vec3 facing = player.getEyePosition().subtract(center).normalize();
        center = center.add(facing.scale(Math.min(0.6, event.getEntity().getBbWidth() * 0.5)));
        double radius = gourmet
                ? 0.72 + Math.min(8, FoodWeaponEvolution.evolutionLevel(weapon)) * 0.035
                        + Math.min(30, FoodWeaponEvolution.socketCount(weapon)) * 0.008
                : 0.65;
        send(player.serverLevel(), gourmet ? Kind.GOURMET_HIT : Kind.HIT, theme,
                center, center, radius, player.getYRot());
    }

    @Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> CHANNEL.messageBuilder(WeaponFxPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(WeaponFxPacket::encode).decoder(WeaponFxPacket::decode)
                    .consumerMainThread(WeaponFxPacket::handle).add());
        }
    }
}
