/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.EntityLeaveLevelEvent
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.youzi;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.youzi.YouziCombatNetwork;
import net.farmingtales.ftboss.youzi.YouziCombatPacket;
import net.farmingtales.ftboss.youzi.YouziHomingFlight;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class YouziCombatEvents {
    public static final String[] IDENTIFIERS = new String[]{"clone_spawn", "clone_depart", "melee_swing", "hit_spark", "resonance_burst", "share_thread", "cast_focus", "ward_react", "echo_trace"};
    private static final AtomicLong SEQUENCE = new AtomicLong();
    private static final Map<EasterBossEntity, State> STATES = new WeakHashMap<EasterBossEntity, State>();

    public static boolean validIdentifier(String string) {
        return Arrays.asList(IDENTIFIERS).contains(string);
    }

    private static boolean youzi(Entity entity) {
        EasterBossEntity easterBossEntity;
        return entity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)entity).getVariant() == 0;
    }

    private static YouziHomingFlight.Vector feet(Entity entity) {
        return new YouziHomingFlight.Vector(entity.m_20185_(), entity.m_20186_(), entity.m_20189_());
    }

    public static void show(Entity entity, String string, YouziHomingFlight.Vector vector, YouziHomingFlight.Vector vector2, double d, double d2) {
        ServerLevel serverLevel;
        block3: {
            block2: {
                Level level;
                if (!YouziCombatEvents.youzi(entity) || !((level = entity.m_9236_()) instanceof ServerLevel)) break block2;
                serverLevel = (ServerLevel)level;
                if (YouziCombatEvents.validIdentifier(string)) break block3;
            }
            return;
        }
        YouziCombatEvents.send(serverLevel, string, vector, vector2, d, 180.0 - d2, entity);
    }

    private static void send(ServerLevel serverLevel, String string, YouziHomingFlight.Vector vector, YouziHomingFlight.Vector vector2, double d, double d2, Entity entity) {
        if (!Double.isFinite(d) || d <= 0.0 || d > 4.0 || !Double.isFinite(d2)) {
            return;
        }
        YouziCombatPacket youziCombatPacket = new YouziCombatPacket(string, serverLevel.m_46472_().m_135782_().toString(), SEQUENCE.incrementAndGet(), serverLevel.m_46467_(), vector, vector2, d, d2, entity.m_19879_(), entity.m_20148_());
        YouziCombatNetwork.send(serverLevel, youziCombatPacket);
    }

    public static void statueFeedback(ServerPlayer serverPlayer, int n, Vec3 vec3, Vec3 vec32) {
        ServerLevel serverLevel;
        Object object;
        block6: {
            block5: {
                object = serverPlayer.m_9236_();
                if (!(object instanceof ServerLevel)) break block5;
                serverLevel = (ServerLevel)object;
                if (vec3 != null && vec32 != null) break block6;
            }
            return;
        }
        object = new YouziHomingFlight.Vector(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_);
        YouziHomingFlight.Vector vector = new YouziHomingFlight.Vector(vec32.f_82479_, vec32.f_82480_, vec32.f_82481_);
        if (n == 2) {
            YouziCombatEvents.send(serverLevel, "echo_trace", (YouziHomingFlight.Vector)object, vector, 1.0, 0.0, (Entity)serverPlayer);
        }
        if (n == 4) {
            YouziCombatEvents.send(serverLevel, "ward_react", (YouziHomingFlight.Vector)object, vector, Math.max(0.25, Math.min(4.0, (double)serverPlayer.m_20206_() / 1.8)), 180.0f - serverPlayer.m_146908_(), (Entity)serverPlayer);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void joined(EntityJoinLevelEvent entityJoinLevelEvent) {
        if (entityJoinLevelEvent.isCanceled() || entityJoinLevelEvent.loadedFromDisk() || !(entityJoinLevelEvent.getLevel() instanceof ServerLevel) || !YouziCombatEvents.youzi(entityJoinLevelEvent.getEntity())) {
            return;
        }
        EasterBossEntity easterBossEntity = (EasterBossEntity)entityJoinLevelEvent.getEntity();
        if (easterBossEntity.isMini()) {
            YouziCombatEvents.show((Entity)easterBossEntity, "clone_spawn", YouziCombatEvents.feet((Entity)easterBossEntity), YouziCombatEvents.feet((Entity)easterBossEntity), Math.max(0.25, Math.min(4.0, (double)easterBossEntity.m_20206_() / 0.9)), easterBossEntity.m_146908_());
        }
    }

    @SubscribeEvent
    public static void left(EntityLeaveLevelEvent entityLeaveLevelEvent) {
        if (!(entityLeaveLevelEvent.getLevel() instanceof ServerLevel) || !YouziCombatEvents.youzi(entityLeaveLevelEvent.getEntity())) {
            return;
        }
        EasterBossEntity easterBossEntity = (EasterBossEntity)entityLeaveLevelEvent.getEntity();
        STATES.remove(easterBossEntity);
        Entity.RemovalReason removalReason = easterBossEntity.m_146911_();
        if (easterBossEntity.isMini() && (removalReason == Entity.RemovalReason.KILLED || removalReason == Entity.RemovalReason.DISCARDED)) {
            YouziCombatEvents.show((Entity)easterBossEntity, "clone_depart", YouziCombatEvents.feet((Entity)easterBossEntity), YouziCombatEvents.feet((Entity)easterBossEntity), Math.max(0.25, Math.min(4.0, (double)easterBossEntity.m_20206_() / 0.9)), easterBossEntity.m_146908_());
        }
    }

    @SubscribeEvent
    public static void livingTick(LivingEvent.LivingTickEvent livingTickEvent) {
        boolean bl;
        if (!(livingTickEvent.getEntity().m_9236_() instanceof ServerLevel) || !YouziCombatEvents.youzi((Entity)livingTickEvent.getEntity())) {
            return;
        }
        EasterBossEntity easterBossEntity = (EasterBossEntity)livingTickEvent.getEntity();
        if (!easterBossEntity.m_6084_()) {
            STATES.remove(easterBossEntity);
            return;
        }
        int n = easterBossEntity.getActionTicks();
        State state = STATES.put(easterBossEntity, new State(n, bl = easterBossEntity.isCastingLike()));
        if (state == null) {
            return;
        }
        double d = Math.max(0.25, Math.min(4.0, (double)easterBossEntity.m_20206_() / 1.8));
        if (n > state.action() && !bl) {
            YouziCombatEvents.show((Entity)easterBossEntity, "melee_swing", YouziCombatEvents.feet((Entity)easterBossEntity), YouziCombatEvents.feet((Entity)easterBossEntity), d, easterBossEntity.m_146908_());
        }
        if (bl && !state.casting()) {
            YouziCombatEvents.show((Entity)easterBossEntity, "cast_focus", YouziCombatEvents.feet((Entity)easterBossEntity), YouziCombatEvents.feet((Entity)easterBossEntity), d, easterBossEntity.m_146908_());
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void damage(LivingDamageEvent livingDamageEvent) {
        Entity entity = livingDamageEvent.getSource().m_7639_();
        if (livingDamageEvent.isCanceled() || livingDamageEvent.getAmount() <= 0.0f || !YouziCombatEvents.youzi(entity) || !(livingDamageEvent.getEntity().m_9236_() instanceof ServerLevel)) {
            return;
        }
        LivingEntity livingEntity = livingDamageEvent.getEntity();
        YouziHomingFlight.Vector vector = YouziCombatEvents.feet((Entity)livingEntity).add(new YouziHomingFlight.Vector(0.0, (double)livingEntity.m_20206_() * 0.55, 0.0));
        YouziCombatEvents.show(entity, "hit_spark", vector, vector, 1.0, entity.m_146908_());
    }

    public static void resonance(EasterBossEntity easterBossEntity, LivingEntity livingEntity) {
        YouziHomingFlight.Vector vector = livingEntity == null ? YouziCombatEvents.feet((Entity)easterBossEntity) : YouziCombatEvents.feet((Entity)livingEntity);
        YouziCombatEvents.show((Entity)easterBossEntity, "resonance_burst", vector, vector, Math.min(4.0, BossSpellTuning.radiusScale((LivingEntity)easterBossEntity)), easterBossEntity.m_146908_());
    }

    public static void sharedDamage(EasterBossEntity easterBossEntity, EasterBossEntity easterBossEntity2) {
        if (easterBossEntity2 == null || !easterBossEntity2.isMini() || easterBossEntity2.getVariant() != 0) {
            return;
        }
        YouziCombatEvents.show((Entity)easterBossEntity, "share_thread", YouziCombatEvents.feet((Entity)easterBossEntity), YouziCombatEvents.feet((Entity)easterBossEntity2), 1.0, 0.0);
    }

    public static void guarded(Entity entity, LivingEntity livingEntity) {
        if (livingEntity == null) {
            return;
        }
        YouziCombatEvents.show(entity, "ward_react", YouziCombatEvents.feet((Entity)livingEntity), YouziCombatEvents.feet((Entity)livingEntity), 1.0, entity.m_146908_());
    }

    public static void echo(Entity entity, YouziHomingFlight.Vector vector, YouziHomingFlight.Vector vector2) {
        YouziCombatEvents.show(entity, "echo_trace", vector, vector2, 1.0, 0.0);
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent serverStoppedEvent) {
        STATES.clear();
    }

    private record State(int action, boolean casting) {
    }
}

