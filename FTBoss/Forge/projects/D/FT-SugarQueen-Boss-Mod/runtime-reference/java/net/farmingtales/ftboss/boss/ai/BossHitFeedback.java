/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
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
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
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

@Mod.EventBusSubscriber(modid="ftboss")
public final class BossHitFeedback {
    private static final Map<LivingEntity, Pending> PENDING = new LinkedHashMap<LivingEntity, Pending>();
    private static final Map<UUID, Stamp> LAST = new LinkedHashMap<UUID, Stamp>();
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "boss_impact"), () -> "1", (Predicate)NetworkRegistry.acceptMissingOr((String)"1"), (Predicate)NetworkRegistry.acceptMissingOr((String)"1"));

    private BossHitFeedback() {
    }

    public static boolean supports(LivingEntity boss) {
        if (boss instanceof EasterBossEntity) {
            EasterBossEntity easter = (EasterBossEntity)boss;
            return !easter.isMini();
        }
        if (boss instanceof RatBossEntity) {
            RatBossEntity rat = (RatBossEntity)boss;
            return !rat.isMinion();
        }
        if (boss instanceof TeaTreeBossEntity) {
            TeaTreeBossEntity tea = (TeaTreeBossEntity)boss;
            return !tea.isTeaMinion();
        }
        return boss instanceof SugarQueenEntity || boss instanceof MeatMountainEntity || boss instanceof WineBarrelBossEntity || boss instanceof DefectCoreEntity || boss instanceof CompostBossEntity || boss instanceof AppleBossEntity || boss instanceof TideFishBossEntity || boss instanceof BengerEntity;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void damage(LivingDamageEvent event) {
        if (!event.isCanceled()) {
            BossHitFeedback.queue(event.getEntity(), event.getSource(), event.getAmount(), false, false);
        }
    }

    public static void confirmed(LivingEntity boss, DamageSource source, float damage, boolean shield) {
        BossHitFeedback.queue(boss, source, damage, shield, true);
    }

    private static void queue(LivingEntity boss, DamageSource source, float damage, boolean shield, boolean confirmed) {
        if (!(boss.m_9236_() instanceof ServerLevel) || !BossHitFeedback.supports(boss) || source == null || !Float.isFinite(damage) || damage <= 0.0f || source.m_276093_(DamageTypes.f_286979_) || source.m_276093_(DamageTypes.f_268724_) || source.m_7639_() == null && source.m_7640_() == null) {
            return;
        }
        Pending previous = PENDING.get(boss);
        if (previous == null && PENDING.size() >= 128) {
            return;
        }
        BossImpactPacket packet = BossHitFeedback.packet(boss, source, damage, shield);
        if (!packet.valid()) {
            return;
        }
        float before = previous == null ? boss.m_21223_() : previous.before;
        float expected = Math.min(boss.m_21233_(), (previous == null ? 0.0f : previous.expected) + (confirmed ? 0.0f : damage));
        float settled = Math.min(boss.m_21233_(), (previous == null ? 0.0f : previous.confirmed) + (confirmed ? damage : 0.0f));
        if (previous != null && (!previous.packet.shield() && shield || previous.packet.strength() > packet.strength() && previous.packet.shield() == shield)) {
            packet = previous.packet;
        }
        PENDING.put(boss, new Pending(before, expected, settled, packet));
    }

    public static BossImpactPacket packet(LivingEntity boss, DamageSource source, float damage, boolean shield) {
        Vec3 direction;
        Vec3 center = boss.m_20191_().m_82399_();
        Entity direct = source.m_7640_();
        Vec3 origin = source.m_7270_();
        if (direct != null && direct != source.m_7639_() && direct.m_20184_().m_82556_() > 1.0E-6) {
            origin = center.m_82546_(direct.m_20184_().m_82541_().m_82490_(8.0));
        } else {
            Entity entity = source.m_7639_();
            if (entity instanceof LivingEntity) {
                LivingEntity attacker = (LivingEntity)entity;
                origin = attacker.m_146892_();
            }
        }
        Vec3 vec3 = direction = origin == null ? boss.m_20154_().m_82490_(-1.0) : origin.m_82546_(center);
        if (direction.m_82556_() < 1.0E-6) {
            direction = new Vec3(0.0, 0.0, -1.0);
        }
        direction = direction.m_82541_();
        double distance = Double.POSITIVE_INFINITY;
        double[] components = new double[]{direction.f_82479_, direction.f_82480_, direction.f_82481_};
        double[] extents = new double[]{(double)boss.m_20205_() * 0.5, (double)boss.m_20206_() * 0.5, (double)boss.m_20205_() * 0.5};
        for (int axis = 0; axis < 3; ++axis) {
            if (!(Math.abs(components[axis]) > 1.0E-6)) continue;
            distance = Math.min(distance, extents[axis] / Math.abs(components[axis]));
        }
        Vec3 position = center.m_82549_(direction.m_82490_(distance + 0.065));
        float size = Math.max(0.35f, Math.min(1.5f, boss.m_20205_() * 0.35f + 0.3f));
        return new BossImpactPacket(boss.m_9236_().m_46472_().m_135782_(), position, direction, size, BossImpactGeometry.strength(damage, boss.m_21233_()), BossHitFeedback.color(boss), shield);
    }

    private static int color(LivingEntity boss) {
        if (boss instanceof EasterBossEntity) {
            EasterBossEntity easter = (EasterBossEntity)boss;
            return switch (easter.getVariant()) {
                case 1, 7 -> 12558591;
                case 5 -> 6479078;
                case 6, 8 -> 11135131;
                default -> 15913099;
            };
        }
        if (boss instanceof SugarQueenEntity) {
            return 16758489;
        }
        if (boss instanceof TeaTreeBossEntity || boss instanceof AppleBossEntity) {
            return 10936181;
        }
        if (boss instanceof WineBarrelBossEntity) {
            return 14651836;
        }
        if (boss instanceof DefectCoreEntity || boss instanceof TideFishBossEntity) {
            return 7593189;
        }
        return 15056517;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            BossHitFeedback.flush();
        }
    }

    private static void flush() {
        LAST.entrySet().removeIf(entry -> ((Stamp)entry.getValue()).level.m_46467_() < ((Stamp)entry.getValue()).time || ((Stamp)entry.getValue()).level.m_46467_() - ((Stamp)entry.getValue()).time > 100L);
        int remaining = 64;
        for (Map.Entry<LivingEntity, Pending> entry2 : PENDING.entrySet()) {
            LivingEntity boss = entry2.getKey();
            Pending pending = entry2.getValue();
            ServerLevel level = (ServerLevel)boss.m_9236_();
            float damage = pending.confirmed + Math.min(pending.expected, Math.max(0.0f, pending.before - boss.m_21223_()));
            if (!(damage > 0.0f) || remaining <= 0) continue;
            long now = level.m_46467_();
            Stamp stamp = LAST.get(boss.m_20148_());
            if (stamp != null && stamp.level == level && !BossImpactGeometry.ready(now, stamp.time)) continue;
            BossImpactPacket original = pending.packet;
            BossImpactPacket packet = new BossImpactPacket(original.dimension(), original.position(), original.normal(), original.size(), BossImpactGeometry.strength(damage, boss.m_21233_()), original.color(), original.shield());
            if (LAST.size() >= 256) {
                LAST.remove(LAST.keySet().iterator().next());
            }
            LAST.put(boss.m_20148_(), new Stamp(level, now));
            --remaining;
            if (pending.confirmed > 0.0f && !packet.shield() && boss instanceof EasterBossEntity && boss.m_6084_() && !boss.m_213877_()) {
                level.m_7726_().m_8445_((Entity)boss, (Packet)new ClientboundHurtAnimationPacket(boss.m_19879_(), 0.0f));
            }
            for (ServerPlayer player : level.m_6907_()) {
                if (!(player.m_20238_(packet.position()) <= 2304.0) || !CHANNEL.isRemotePresent(player.f_8906_.f_9742_)) continue;
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)packet);
            }
        }
        PENDING.clear();
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().m_5776_()) {
            return;
        }
        PENDING.keySet().removeIf(boss -> boss.m_9236_() == event.getLevel());
        LAST.values().removeIf(stamp -> stamp.level == event.getLevel());
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) {
        PENDING.clear();
        LAST.clear();
    }

    private record Pending(float before, float expected, float confirmed, BossImpactPacket packet) {
    }

    private record Stamp(ServerLevel level, long time) {
    }

    @Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> CHANNEL.messageBuilder(BossImpactPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(BossImpactPacket::encode).decoder(BossImpactPacket::decode).consumerMainThread(BossImpactPacket::handle).add());
        }
    }
}

