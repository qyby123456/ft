package net.farmingtales.ftboss.boss.ai;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.farmingtales.ftboss.skill.BossImpactPacket;
import net.farmingtales.ftboss.util.BossImpactGeometry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class BossHitFeedback {
    private record Pending(float before, float expected, float confirmed, BossImpactPacket packet) {}
    private record Stamp(ServerLevel level, long time) {}
    private static final Map<LivingEntity, Pending> PENDING = new LinkedHashMap<>();
    private static final Map<UUID, Stamp> LAST = new LinkedHashMap<>();
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new net.minecraft.resources.ResourceLocation(FTBossMod.MOD_ID, "boss_impact"), () -> "1",
            NetworkRegistry.acceptMissingOr("1"), NetworkRegistry.acceptMissingOr("1"));

    private BossHitFeedback() {}

    public static boolean supports(LivingEntity boss) {
        if (boss instanceof EasterBossEntity easter) return !easter.isMini();
        if (boss instanceof RatBossEntity rat) return !rat.isMinion();
        if (boss instanceof TeaTreeBossEntity tea) return !tea.isTeaMinion();
        return boss instanceof SugarQueenEntity || boss instanceof MeatMountainEntity || boss instanceof WineBarrelBossEntity
                || boss instanceof DefectCoreEntity || boss instanceof CompostBossEntity || boss instanceof AppleBossEntity
                || boss instanceof TideFishBossEntity || boss instanceof BengerEntity;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void damage(LivingDamageEvent event) {
        if (!event.isCanceled()) queue(event.getEntity(), event.getSource(), event.getAmount(), false, false);
    }

    public static void confirmed(LivingEntity boss, DamageSource source, float damage, boolean shield) {
        queue(boss, source, damage, shield, true);
    }

    private static void queue(LivingEntity boss, DamageSource source, float damage, boolean shield, boolean confirmed) {
        if (!(boss.level() instanceof ServerLevel) || !supports(boss) || source == null || !Float.isFinite(damage)
                || damage <= 0 || source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || source.getEntity() == null && source.getDirectEntity() == null) return;
        Pending previous = PENDING.get(boss);
        if (previous == null && PENDING.size() >= 128) return;
        BossImpactPacket packet = packet(boss, source, damage, shield);
        if (!packet.valid()) return;
        float before = previous == null ? boss.getHealth() : previous.before;
        float expected = Math.min(boss.getMaxHealth(), (previous == null ? 0 : previous.expected) + (confirmed ? 0 : damage));
        float settled = Math.min(boss.getMaxHealth(), (previous == null ? 0 : previous.confirmed) + (confirmed ? damage : 0));
        if (previous != null && (!previous.packet.shield() && shield || previous.packet.strength() > packet.strength() && previous.packet.shield() == shield)) {
            packet = previous.packet;
        }
        PENDING.put(boss, new Pending(before, expected, settled, packet));
    }

    public static BossImpactPacket packet(LivingEntity boss, DamageSource source, float damage, boolean shield) {
        Vec3 center = boss.getBoundingBox().getCenter();
        var direct = source.getDirectEntity();
        Vec3 origin = source.getSourcePosition();
        if (direct != null && direct != source.getEntity() && direct.getDeltaMovement().lengthSqr() > 1.0e-6) {
            origin = center.subtract(direct.getDeltaMovement().normalize().scale(8));
        } else if (source.getEntity() instanceof LivingEntity attacker) origin = attacker.getEyePosition();
        Vec3 direction = origin == null ? boss.getLookAngle().scale(-1) : origin.subtract(center);
        if (direction.lengthSqr() < 1.0e-6) direction = new Vec3(0, 0, -1);
        direction = direction.normalize();
        double distance = Double.POSITIVE_INFINITY;
        double[] components = {direction.x, direction.y, direction.z};
        double[] extents = {boss.getBbWidth() * 0.5, boss.getBbHeight() * 0.5, boss.getBbWidth() * 0.5};
        for (int axis = 0; axis < 3; axis++) {
            if (Math.abs(components[axis]) > 1.0e-6) distance = Math.min(distance, extents[axis] / Math.abs(components[axis]));
        }
        Vec3 position = center.add(direction.scale(distance + 0.065));
        float size = Math.max(0.35f, Math.min(1.5f, boss.getBbWidth() * 0.35f + 0.3f));
        return new BossImpactPacket(boss.level().dimension().location(), position, direction, size,
                BossImpactGeometry.strength(damage, boss.getMaxHealth()), color(boss), shield);
    }

    private static int color(LivingEntity boss) {
        if (boss instanceof EasterBossEntity easter) return switch (easter.getVariant()) {
            case 1, 7 -> 0xbfa0ff;
            case 5 -> 0x62dce6;
            case 6, 8 -> 0xa9e89b;
            default -> 0xf2d08b;
        };
        if (boss instanceof SugarQueenEntity) return 0xffb6d9;
        if (boss instanceof TeaTreeBossEntity || boss instanceof AppleBossEntity) return 0xa6df75;
        if (boss instanceof WineBarrelBossEntity) return 0xdf91bc;
        if (boss instanceof DefectCoreEntity || boss instanceof TideFishBossEntity) return 0x73dce5;
        return 0xe5be85;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) flush();
    }

    private static void flush() {
        LAST.entrySet().removeIf(entry -> entry.getValue().level.getGameTime() < entry.getValue().time
                || entry.getValue().level.getGameTime() - entry.getValue().time > 100);
        int remaining = 64;
        for (var entry : PENDING.entrySet()) {
            LivingEntity boss = entry.getKey();
            Pending pending = entry.getValue();
            ServerLevel level = (ServerLevel) boss.level();
            float damage = pending.confirmed + Math.min(pending.expected, Math.max(0, pending.before - boss.getHealth()));
            if (!(damage > 0) || remaining <= 0) continue;
            long now = level.getGameTime();
            Stamp stamp = LAST.get(boss.getUUID());
            if (stamp != null && stamp.level == level && !BossImpactGeometry.ready(now, stamp.time)) continue;
            BossImpactPacket original = pending.packet;
            BossImpactPacket packet = new BossImpactPacket(original.dimension(), original.position(), original.normal(), original.size(),
                    BossImpactGeometry.strength(damage, boss.getMaxHealth()), original.color(), original.shield());
            if (LAST.size() >= 256) LAST.remove(LAST.keySet().iterator().next());
            LAST.put(boss.getUUID(), new Stamp(level, now));
            remaining--;
            if (pending.confirmed > 0 && !packet.shield() && boss instanceof EasterBossEntity && boss.isAlive() && !boss.isRemoved()) {
                level.getChunkSource().broadcast(boss,
                        new net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket(boss.getId(), 0));
            }
            for (var player : level.players()) {
                if (player.distanceToSqr(packet.position()) <= BossImpactGeometry.VIEW_DISTANCE * BossImpactGeometry.VIEW_DISTANCE
                        && CHANNEL.isRemotePresent(player.connection.connection)) {
                    CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
                }
            }
        }
        PENDING.clear();
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) return;
        PENDING.keySet().removeIf(boss -> boss.level() == event.getLevel());
        LAST.values().removeIf(stamp -> stamp.level == event.getLevel());
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) { PENDING.clear(); LAST.clear(); }

    @Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> CHANNEL.messageBuilder(BossImpactPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(BossImpactPacket::encode).decoder(BossImpactPacket::decode)
                    .consumerMainThread(BossImpactPacket::handle).add());
        }
    }
}
