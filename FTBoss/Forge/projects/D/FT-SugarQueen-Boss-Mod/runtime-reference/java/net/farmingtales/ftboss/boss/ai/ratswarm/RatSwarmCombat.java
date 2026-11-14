/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3f
 *  software.bernie.geckolib.core.animation.AnimationState
 *  software.bernie.geckolib.core.animation.RawAnimation
 */
package net.farmingtales.ftboss.boss.ai.ratswarm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.ratswarm.RatCombatRules;
import net.farmingtales.ftboss.boss.ai.ratswarm.RatFxBudget;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

public final class RatSwarmCombat {
    private static final Map<RatBossEntity, State> STATES = new WeakHashMap<RatBossEntity, State>();
    private static final ThreadLocal<State> EMITTER = new ThreadLocal();
    private static final DustParticleOptions GREEN = new DustParticleOptions(new Vector3f(0.38f, 0.96f, 0.12f), 1.05f);
    private static final DustParticleOptions GOLD = new DustParticleOptions(new Vector3f(1.0f, 0.75f, 0.18f), 1.15f);
    private static final DustParticleOptions PALE = new DustParticleOptions(new Vector3f(0.9f, 1.0f, 0.52f), 0.85f);
    private static final String[] SPELLS = new String[]{"rat_plague", "rat_fog", "rat_wave"};
    private static final Set<String> OWN_ACTIONS = Set.of("rat_plague", "rat_fog", "rat_wave", "rat_fang", "rat_tail");

    private RatSwarmCombat() {
    }

    public static boolean tryMelee(RatBossEntity ratBossEntity, Entity entity, BossActionState bossActionState) {
        LivingEntity livingEntity;
        if (ratBossEntity.isMinion()) {
            return false;
        }
        if (!(ratBossEntity.m_9236_() instanceof ServerLevel) || !ratBossEntity.m_6084_()) {
            return true;
        }
        if (!(entity instanceof LivingEntity && RatSwarmCombat.valid(ratBossEntity, livingEntity = (LivingEntity)entity) && ratBossEntity.m_142582_((Entity)livingEntity))) {
            return true;
        }
        State state = RatSwarmCombat.state(ratBossEntity);
        long l = ratBossEntity.m_9236_().m_46467_();
        if (bossActionState.active() || l < state.nextMelee || ratBossEntity.m_20280_((Entity)livingEntity) > 9.0) {
            return true;
        }
        boolean bl = state.meleeSequence++ % 2 == 1;
        RatSwarmCombat.begin(ratBossEntity, state, bossActionState, bl ? "rat_tail" : "rat_fang", bl ? 24 : 26, livingEntity);
        state.nextMelee = l + 32L;
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void tick(RatBossEntity ratBossEntity, BossActionState bossActionState) {
        Object object = ratBossEntity.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        if (!ratBossEntity.m_6084_()) {
            STATES.remove((Object)ratBossEntity);
            return;
        }
        if (ratBossEntity.isMinion()) {
            return;
        }
        object = RatSwarmCombat.state(ratBossEntity);
        long l = serverLevel.m_46467_();
        if (object.lastTick == l) {
            return;
        }
        object.lastTick = l;
        if (l >= object.nextAudienceCheck) {
            object.visible = !serverLevel.m_45976_(ServerPlayer.class, ratBossEntity.m_20191_().m_82400_(48.0)).isEmpty();
            object.nextAudienceCheck = l + 10L;
        }
        State state = EMITTER.get();
        EMITTER.set((State)object);
        try {
            RatSwarmCombat.tickBolts(ratBossEntity, serverLevel, (State)object);
            RatSwarmCombat.tickFog(ratBossEntity, serverLevel, (State)object);
            RatSwarmCombat.tickGlows(serverLevel, (State)object);
            RatSwarmCombat.tickAction(ratBossEntity, bossActionState, serverLevel, (State)object, l);
        }
        finally {
            if (state == null) {
                EMITTER.remove();
            } else {
                EMITTER.set(state);
            }
        }
    }

    private static void tickAction(RatBossEntity ratBossEntity, BossActionState bossActionState, ServerLevel serverLevel, State state, long l) {
        String string = bossActionState.name();
        int n = (int)bossActionState.elapsed(0.0f);
        if (string.equals("call")) {
            RatSwarmCombat.summonRing(ratBossEntity, serverLevel, n);
            return;
        }
        if (OWN_ACTIONS.contains(string)) {
            if (state.origin == null || state.aim == null || state.direction == null) {
                bossActionState.start("", 0);
                return;
            }
            ratBossEntity.m_21573_().m_26573_();
            ratBossEntity.m_21564_(0.0f);
            LivingEntity livingEntity = ratBossEntity.m_5448_();
            if (RatSwarmCombat.valid(ratBossEntity, livingEntity)) {
                ratBossEntity.m_21563_().m_24960_((Entity)livingEntity, 30.0f, 30.0f);
            }
            if (!string.equals("rat_fang") || n < 10 || n > 15) {
                Object object = ratBossEntity.m_20184_();
                ratBossEntity.m_20256_(new Vec3(0.0, ((Vec3)object).f_82480_, 0.0));
            }
            switch (string) {
                case "rat_plague": {
                    RatSwarmCombat.plague(ratBossEntity, serverLevel, state, n);
                    break;
                }
                case "rat_fog": {
                    RatSwarmCombat.fogCast(serverLevel, state, n);
                    break;
                }
                case "rat_wave": {
                    RatSwarmCombat.wave(ratBossEntity, serverLevel, state, n);
                    break;
                }
                case "rat_fang": {
                    RatSwarmCombat.fang(ratBossEntity, serverLevel, state, n);
                    break;
                }
                case "rat_tail": {
                    RatSwarmCombat.tail(ratBossEntity, serverLevel, state, n);
                    break;
                }
            }
            return;
        }
        if (bossActionState.active()) {
            return;
        }
        LivingEntity livingEntity = ratBossEntity.m_5448_();
        if (!RatSwarmCombat.valid(ratBossEntity, livingEntity) || !ratBossEntity.m_142582_((Entity)livingEntity)) {
            return;
        }
        double d = Math.sqrt(ratBossEntity.m_20280_((Entity)livingEntity));
        if (d < 3.0 || d > 24.0 || l < state.nextSpell) {
            return;
        }
        int n2 = RatCombatRules.spell(state.spellSequence++, d);
        RatSwarmCombat.begin(ratBossEntity, state, bossActionState, SPELLS[n2], n2 == 1 ? 40 : 34, livingEntity);
        state.nextSpell = l + 90L;
    }

    public static boolean animate(RatBossEntity ratBossEntity, BossActionState bossActionState, AnimationState<?> animationState) {
        String string;
        if (!ratBossEntity.m_6084_()) {
            return false;
        }
        switch (bossActionState.name()) {
            case "rat_plague": {
                String string2 = "plague_cast";
                break;
            }
            case "rat_fog": {
                String string2 = "fog_cast";
                break;
            }
            case "rat_wave": {
                String string2 = "wave_cast";
                break;
            }
            case "rat_fang": {
                String string2 = "fang_lunge";
                break;
            }
            case "rat_tail": {
                String string2 = "tail_sweep";
                break;
            }
            default: {
                String string2 = string = null;
            }
        }
        if (string == null) {
            return false;
        }
        animationState.setAnimation(RawAnimation.begin().thenPlay("animation.rat." + string));
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean spawn(Level level, Entity entity, RatBossEntity ratBossEntity) {
        boolean bl = level.m_7967_(entity);
        if (bl && level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (ratBossEntity.m_6084_() && !ratBossEntity.isMinion()) {
                Vec3 vec3 = entity.m_20182_();
                State state = RatSwarmCombat.state(ratBossEntity);
                if (state.glows.size() < 12) {
                    state.glows.add(new Glow(vec3));
                }
                State state2 = EMITTER.get();
                EMITTER.set(state);
                try {
                    RatSwarmCombat.ring(serverLevel, vec3, 0.65, 10, (ParticleOptions)GOLD, 0.0);
                    RatSwarmCombat.cloud(serverLevel, (ParticleOptions)PALE, vec3.m_82520_(0.0, 0.45, 0.0), 6, 0.12, 0.02);
                }
                finally {
                    if (state2 == null) {
                        EMITTER.remove();
                    } else {
                        EMITTER.set(state2);
                    }
                }
            }
        }
        return bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void bite(RatBossEntity ratBossEntity, LivingEntity livingEntity) {
        ServerLevel serverLevel;
        Object object;
        block10: {
            block9: {
                object = ratBossEntity.m_9236_();
                if (!(object instanceof ServerLevel)) break block9;
                serverLevel = (ServerLevel)object;
                if (BossTargets.valid(livingEntity)) break block10;
            }
            return;
        }
        object = EMITTER.get();
        EMITTER.set(RatSwarmCombat.state(ratBossEntity));
        try {
            RatSwarmCombat.cloud(serverLevel, (ParticleOptions)GOLD, livingEntity.m_20182_().m_82520_(0.0, 0.35, 0.0), 3, 0.1, 0.01);
        }
        finally {
            if (object == null) {
                EMITTER.remove();
            } else {
                EMITTER.set((State)object);
            }
        }
    }

    private static State state(RatBossEntity ratBossEntity) {
        return STATES.computeIfAbsent(ratBossEntity, ratBossEntity2 -> new State(ratBossEntity.m_9236_().m_46467_()));
    }

    private static void begin(RatBossEntity ratBossEntity, State state, BossActionState bossActionState, String string, int n, LivingEntity livingEntity) {
        state.aim = livingEntity.m_20182_();
        if (string.equals("rat_fog")) {
            BlockHitResult blockHitResult = ratBossEntity.m_9236_().m_45547_(new ClipContext(state.aim.m_82520_(0.0, 1.0, 0.0), state.aim.m_82520_(0.0, -6.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)ratBossEntity));
            if (blockHitResult.m_6662_() != HitResult.Type.MISS) {
                state.aim = blockHitResult.m_82450_();
            } else {
                string = "rat_plague";
                n = 34;
            }
        }
        state.origin = ratBossEntity.m_20182_();
        state.direction = new Vec3(livingEntity.m_20185_() - ratBossEntity.m_20185_(), 0.0, livingEntity.m_20189_() - ratBossEntity.m_20189_()).m_82541_();
        state.emitted = 0;
        state.impact = false;
        state.waveHits.clear();
        bossActionState.start(string, n);
        ratBossEntity.m_21573_().m_26573_();
        if (string.equals("rat_plague") || string.equals("rat_fog") || string.equals("rat_wave")) {
            Fx.sound(ratBossEntity.m_9236_(), ratBossEntity.m_20185_(), ratBossEntity.m_20186_(), ratBossEntity.m_20189_(), SoundEvents.f_11862_, 0.65f, 1.45f);
        }
    }

    private static boolean valid(RatBossEntity ratBossEntity, LivingEntity livingEntity) {
        return BossTargets.valid(livingEntity) && livingEntity != ratBossEntity && !(livingEntity instanceof RatBossEntity) && livingEntity.m_9236_() == ratBossEntity.m_9236_();
    }

    private static List<LivingEntity> victims(RatBossEntity ratBossEntity, AABB aABB) {
        return ratBossEntity.m_9236_().m_6443_(LivingEntity.class, aABB, livingEntity -> RatSwarmCombat.valid(ratBossEntity, livingEntity) && (livingEntity instanceof Player || livingEntity == ratBossEntity.m_5448_()));
    }

    private static boolean damage(RatBossEntity ratBossEntity, LivingEntity livingEntity, float f) {
        return RatSwarmCombat.valid(ratBossEntity, livingEntity) && livingEntity.m_6469_(ratBossEntity.m_269291_().m_269333_((LivingEntity)ratBossEntity), f);
    }

    private static void plague(RatBossEntity ratBossEntity, ServerLevel serverLevel, State state, int n) {
        if (n < 10 && n % 3 == 0) {
            RatSwarmCombat.ring(serverLevel, ratBossEntity.m_20182_().m_82520_(0.0, 0.12, 0.0), 0.9, 10, (ParticleOptions)GREEN, (double)n * 0.13);
        }
        if (state.emitted >= 3 || n < 10 + state.emitted * 5) {
            return;
        }
        Vec3 vec3 = ratBossEntity.m_20182_().m_82520_(state.direction.f_82479_ * 0.55, 0.65, state.direction.f_82481_ * 0.55);
        Vec3 vec32 = state.aim.m_82520_(0.0, 0.65, 0.0);
        double d = (double)(state.emitted - 1) * 0.11;
        Vec3 vec33 = vec32.m_82546_(vec3).m_82541_().m_82524_((float)d);
        if (!RatSwarmCombat.clear(serverLevel, (Entity)ratBossEntity, ratBossEntity.m_20182_().m_82520_(0.0, 0.65, 0.0), vec3)) {
            vec3 = ratBossEntity.m_20182_().m_82520_(0.0, 0.65, 0.0);
        }
        if (state.bolts.size() < 3) {
            state.bolts.add(new Bolt(vec3, vec33.m_82490_(0.58)));
        }
        ++state.emitted;
        RatSwarmCombat.cloud(serverLevel, (ParticleOptions)PALE, vec3, 4, 0.08, 0.02);
    }

    private static void tickBolts(RatBossEntity ratBossEntity, ServerLevel serverLevel, State state) {
        Iterator<Bolt> iterator = state.bolts.iterator();
        while (iterator.hasNext()) {
            Bolt bolt = iterator.next();
            Vec3 vec3 = bolt.position.m_82549_(bolt.velocity);
            BlockHitResult blockHitResult = serverLevel.m_45547_(new ClipContext(bolt.position, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)ratBossEntity));
            Vec3 vec32 = blockHitResult.m_6662_() == HitResult.Type.MISS ? vec3 : blockHitResult.m_82450_();
            LivingEntity livingEntity = null;
            double d = Double.POSITIVE_INFINITY;
            for (LivingEntity livingEntity2 : RatSwarmCombat.victims(ratBossEntity, new AABB(bolt.position, vec32).m_82400_(0.35))) {
                double d2;
                AABB aABB = livingEntity2.m_20191_().m_82400_(0.18);
                Vec3 vec33 = aABB.m_82390_(bolt.position) ? bolt.position : (Vec3)aABB.m_82371_(bolt.position, vec32).orElse(null);
                if (vec33 == null || !((d2 = vec33.m_82557_(bolt.position)) < d)) continue;
                livingEntity = livingEntity2;
                d = d2;
            }
            if (livingEntity != null) {
                if (RatSwarmCombat.damage(ratBossEntity, livingEntity, 4.5f)) {
                    livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19614_, 60, 0));
                }
                Vec3 vec34 = bolt.position.m_82549_(bolt.velocity.m_82541_().m_82490_(Math.sqrt(d)));
                RatSwarmCombat.cloud(serverLevel, (ParticleOptions)GREEN, vec34, 6, 0.12, 0.02);
                iterator.remove();
                continue;
            }
            if (blockHitResult.m_6662_() != HitResult.Type.MISS || ++bolt.age >= 42) {
                RatSwarmCombat.cloud(serverLevel, (ParticleOptions)GOLD, vec32, 4, 0.12, 0.01);
                iterator.remove();
                continue;
            }
            bolt.position = vec3;
            RatSwarmCombat.cloud(serverLevel, (ParticleOptions)GREEN, vec3, 2, 0.025, 0.0);
            if (bolt.age % 2 != 0) continue;
            RatSwarmCombat.cloud(serverLevel, (ParticleOptions)GOLD, vec3.m_82546_(bolt.velocity.m_82490_(0.5)), 1, 0.02, 0.0);
            RatSwarmCombat.cloud(serverLevel, (ParticleOptions)ParticleTypes.f_123810_, vec3, 1, 0.01, 0.0);
        }
    }

    private static void fogCast(ServerLevel serverLevel, State state, int n) {
        if (n < 20) {
            if (n % 4 == 0) {
                RatSwarmCombat.ring(serverLevel, state.aim.m_82520_(0.0, 0.1, 0.0), 2.5, 20, (ParticleOptions)GOLD, (double)n * 0.1);
                RatSwarmCombat.ring(serverLevel, state.aim.m_82520_(0.0, 0.15, 0.0), 0.4 + (double)n * 0.08, 12, (ParticleOptions)GREEN, (double)(-n) * 0.1);
            }
            return;
        }
        if (!state.impact) {
            state.impact = true;
            state.fog = new Fog(state.aim);
            RatSwarmCombat.cloud(serverLevel, (ParticleOptions)GREEN, state.aim.m_82520_(0.0, 0.35, 0.0), 16, 0.6, 0.015);
        }
    }

    private static void tickFog(RatBossEntity ratBossEntity, ServerLevel serverLevel, State state) {
        Fog fog = state.fog;
        if (fog == null) {
            return;
        }
        if (fog.age % 5 == 0) {
            RatSwarmCombat.ring(serverLevel, fog.origin.m_82520_(0.0, 0.1, 0.0), 2.5, 18, (ParticleOptions)GREEN, (double)fog.age * 0.035);
            RatSwarmCombat.cloud(serverLevel, (ParticleOptions)GREEN, fog.origin.m_82520_(0.0, 0.35, 0.0), 6, 0.65, 0.01);
        }
        if (fog.age % 10 == 0) {
            AABB aABB = new AABB(fog.origin.f_82479_ - 2.5, fog.origin.f_82480_ - 0.3, fog.origin.f_82481_ - 2.5, fog.origin.f_82479_ + 2.5, fog.origin.f_82480_ + 2.0, fog.origin.f_82481_ + 2.5);
            for (LivingEntity livingEntity : RatSwarmCombat.victims(ratBossEntity, aABB)) {
                double d = Math.hypot(livingEntity.m_20185_() - fog.origin.f_82479_, livingEntity.m_20189_() - fog.origin.f_82481_);
                if (d > 2.5 || !RatSwarmCombat.clear(serverLevel, (Entity)ratBossEntity, fog.origin.m_82520_(0.0, 0.7, 0.0), livingEntity.m_20182_().m_82520_(0.0, 0.7, 0.0)) || !RatSwarmCombat.damage(ratBossEntity, livingEntity, 2.0f)) continue;
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 30, 0));
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19614_, 40, 0));
            }
        }
        if (++fog.age >= 50) {
            state.fog = null;
        }
    }

    private static void wave(RatBossEntity ratBossEntity, ServerLevel serverLevel, State state, int n) {
        if (n < 14) {
            if (n % 3 == 0) {
                RatSwarmCombat.ring(serverLevel, state.origin.m_82520_(0.0, 0.12, 0.0), 0.7 + (double)n * 0.035, 16, (ParticleOptions)GOLD, (double)n * 0.08);
            }
            return;
        }
        double d = RatCombatRules.waveRadius(n);
        if (d <= 0.0 || n > 28) {
            return;
        }
        if (n % 2 == 0) {
            RatSwarmCombat.ring(serverLevel, state.origin.m_82520_(0.0, 0.18, 0.0), d, 24, (ParticleOptions)GREEN, 0.0);
            RatSwarmCombat.ring(serverLevel, state.origin.m_82520_(0.0, 0.3, 0.0), Math.max(0.1, d - 0.15), 16, (ParticleOptions)GOLD, 0.1);
        }
        for (LivingEntity livingEntity : RatSwarmCombat.victims(ratBossEntity, new AABB(state.origin, state.origin).m_82377_(6.0, 2.0, 6.0))) {
            double d2 = Math.hypot(livingEntity.m_20185_() - state.origin.f_82479_, livingEntity.m_20189_() - state.origin.f_82481_);
            if (!RatCombatRules.inWave(d2, d) || state.waveHits.contains(livingEntity.m_20148_()) || !RatSwarmCombat.clear(serverLevel, (Entity)ratBossEntity, state.origin.m_82520_(0.0, 0.6, 0.0), livingEntity.m_20182_().m_82520_(0.0, 0.6, 0.0))) continue;
            state.waveHits.add(livingEntity.m_20148_());
            if (!RatSwarmCombat.damage(ratBossEntity, livingEntity, 5.0f)) continue;
            livingEntity.m_147240_(0.55, state.origin.f_82479_ - livingEntity.m_20185_(), state.origin.f_82481_ - livingEntity.m_20189_());
            livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 35, 0));
        }
    }

    private static void fang(RatBossEntity ratBossEntity, ServerLevel serverLevel, State state, int n) {
        if (n < 10) {
            if (n % 3 == 0) {
                RatSwarmCombat.arc(serverLevel, ratBossEntity.m_20182_(), state.direction, 0.9, 1.2, (ParticleOptions)GOLD);
            }
            return;
        }
        if (n < 16) {
            Vec3 vec3;
            double d = (state.aim.f_82479_ - ratBossEntity.m_20185_()) * state.direction.f_82479_ + (state.aim.f_82481_ - ratBossEntity.m_20189_()) * state.direction.f_82481_;
            double d2 = RatCombatRules.lungeSpeed(d);
            Vec3 vec32 = ratBossEntity.m_20182_().m_82520_(0.0, 0.4, 0.0);
            BlockHitResult blockHitResult = serverLevel.m_45547_(new ClipContext(vec32, vec3 = vec32.m_82549_(state.direction.m_82490_(d2 + 0.3)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)ratBossEntity));
            if (blockHitResult.m_6662_() != HitResult.Type.MISS) {
                d2 = Math.max(0.0, Math.min(d2, blockHitResult.m_82450_().m_82554_(vec32) - 0.3));
            }
            ratBossEntity.m_20256_(new Vec3(state.direction.f_82479_ * d2, ratBossEntity.m_20184_().f_82480_, state.direction.f_82481_ * d2));
            RatSwarmCombat.cloud(serverLevel, (ParticleOptions)GOLD, ratBossEntity.m_20182_().m_82520_(0.0, 0.3, 0.0), 3, 0.08, 0.0);
        }
        if (n < 16 || state.impact) {
            return;
        }
        state.impact = true;
        RatSwarmCombat.arc(serverLevel, ratBossEntity.m_20182_(), state.direction, 1.7, 1.0, (ParticleOptions)PALE);
        Fx.sound((Level)serverLevel, ratBossEntity.m_20185_(), ratBossEntity.m_20186_(), ratBossEntity.m_20189_(), SoundEvents.f_11945_, 0.7f, 0.8f);
        for (LivingEntity livingEntity : RatSwarmCombat.victims(ratBossEntity, ratBossEntity.m_20191_().m_82400_(2.1))) {
            if (Math.abs(livingEntity.m_20186_() - ratBossEntity.m_20186_()) > 1.5 || !ratBossEntity.m_142582_((Entity)livingEntity) || !RatCombatRules.inCone(livingEntity.m_20185_() - ratBossEntity.m_20185_(), livingEntity.m_20189_() - ratBossEntity.m_20189_(), state.direction.f_82479_, state.direction.f_82481_, 2.1) || !RatSwarmCombat.damage(ratBossEntity, livingEntity, 7.0f)) continue;
            ratBossEntity.m_5634_(1.0f);
            RatSwarmCombat.bite(ratBossEntity, livingEntity);
        }
    }

    private static void tail(RatBossEntity ratBossEntity, ServerLevel serverLevel, State state, int n) {
        if (n < 12) {
            if (n % 3 == 0) {
                RatSwarmCombat.ring(serverLevel, ratBossEntity.m_20182_().m_82520_(0.0, 0.13, 0.0), 2.8, 18, (ParticleOptions)GOLD, 0.0);
            }
            return;
        }
        if (n <= 16) {
            RatSwarmCombat.arc(serverLevel, ratBossEntity.m_20182_(), state.direction, 2.4, 3.1, (ParticleOptions)(n % 2 == 0 ? GREEN : GOLD));
        }
        if (state.impact) {
            return;
        }
        state.impact = true;
        Fx.sound((Level)serverLevel, ratBossEntity.m_20185_(), ratBossEntity.m_20186_(), ratBossEntity.m_20189_(), SoundEvents.f_12317_, 0.7f, 0.8f);
        for (LivingEntity livingEntity : RatSwarmCombat.victims(ratBossEntity, ratBossEntity.m_20191_().m_82400_(2.8))) {
            if (ratBossEntity.m_20280_((Entity)livingEntity) > 7.84 || Math.abs(livingEntity.m_20186_() - ratBossEntity.m_20186_()) > 1.5 || !ratBossEntity.m_142582_((Entity)livingEntity) || !RatSwarmCombat.damage(ratBossEntity, livingEntity, 6.0f)) continue;
            livingEntity.m_147240_(0.7, ratBossEntity.m_20185_() - livingEntity.m_20185_(), ratBossEntity.m_20189_() - livingEntity.m_20189_());
        }
    }

    private static boolean clear(ServerLevel serverLevel, Entity entity, Vec3 vec3, Vec3 vec32) {
        return serverLevel.m_45547_(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).m_6662_() == HitResult.Type.MISS;
    }

    private static void summonRing(RatBossEntity ratBossEntity, ServerLevel serverLevel, int n) {
        if (n % 3 != 0) {
            return;
        }
        if (n == 0) {
            Fx.sound((Level)serverLevel, ratBossEntity.m_20185_(), ratBossEntity.m_20186_(), ratBossEntity.m_20189_(), SoundEvents.f_144243_, 0.8f, 0.65f);
        }
        Vec3 vec3 = ratBossEntity.m_20182_().m_82520_(0.0, 0.12, 0.0);
        RatSwarmCombat.ring(serverLevel, vec3, 1.0 + (double)Math.min(n, 9) * 0.2, 16, (ParticleOptions)GOLD, (double)n * 0.08);
        RatSwarmCombat.ring(serverLevel, vec3.m_82520_(0.0, 0.1, 0.0), 0.8, 12, (ParticleOptions)GREEN, (double)(-n) * 0.15);
        for (int i = 0; i < 4; ++i) {
            double d = (double)n * 0.19 + (double)i * Math.PI / 2.0;
            RatSwarmCombat.cloud(serverLevel, (ParticleOptions)PALE, vec3.m_82520_(Math.cos(d) * 1.1, 0.3 + (double)n * 0.055, Math.sin(d) * 1.1), 1, 0.02, 0.0);
        }
    }

    private static void tickGlows(ServerLevel serverLevel, State state) {
        Iterator<Glow> iterator = state.glows.iterator();
        while (iterator.hasNext()) {
            Glow glow = iterator.next();
            if (glow.age % 3 == 0) {
                double d = (double)glow.age / 16.0;
                RatSwarmCombat.ring(serverLevel, glow.origin.m_82520_(0.0, 0.05 + d * 1.7, 0.0), 0.65 * (1.0 - d * 0.5), 8, (ParticleOptions)GOLD, (double)glow.age * 0.15);
                RatSwarmCombat.cloud(serverLevel, (ParticleOptions)PALE, glow.origin.m_82520_(0.0, d * 2.0, 0.0), 2, 0.05, 0.025);
                RatSwarmCombat.cloud(serverLevel, (ParticleOptions)ParticleTypes.f_123810_, glow.origin.m_82520_(0.0, d * 2.0, 0.0), 2, 0.05, 0.015);
            }
            if (++glow.age < 16) continue;
            iterator.remove();
        }
    }

    private static void arc(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32, double d, double d2, ParticleOptions particleOptions) {
        double d3 = Math.atan2(vec32.f_82481_, vec32.f_82479_);
        for (int i = 0; i < 15; ++i) {
            double d4 = d3 - d2 + d2 * 2.0 * (double)i / 14.0;
            RatSwarmCombat.cloud(serverLevel, particleOptions, vec3.m_82520_(Math.cos(d4) * d, 0.45 + Math.sin((double)i * Math.PI / 14.0) * 0.3, Math.sin(d4) * d), 1, 0.01, 0.0);
        }
    }

    private static void ring(ServerLevel serverLevel, Vec3 vec3, double d, int n, ParticleOptions particleOptions, double d2) {
        for (int i = 0; i < n; ++i) {
            double d3 = d2 + (double)i * Math.PI * 2.0 / (double)n;
            RatSwarmCombat.cloud(serverLevel, particleOptions, vec3.m_82520_(Math.cos(d3) * d, 0.0, Math.sin(d3) * d), 1, 0.01, 0.0);
        }
    }

    private static void cloud(ServerLevel serverLevel, ParticleOptions particleOptions, Vec3 vec3, int n, double d, double d2) {
        State state = EMITTER.get();
        if (state == null || !state.visible) {
            return;
        }
        int n2 = state.budget.claim(serverLevel.m_46467_(), n);
        if (n2 > 0) {
            serverLevel.m_8767_(particleOptions, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, n2, d, d, d, d2);
        }
    }

    private static final class State {
        private long nextSpell;
        private long nextMelee;
        private long lastTick = Long.MIN_VALUE;
        private long nextAudienceCheck;
        private boolean visible = true;
        private final RatFxBudget budget = new RatFxBudget();
        private int spellSequence;
        private int meleeSequence;
        private int emitted;
        private boolean impact;
        private Vec3 aim;
        private Vec3 origin;
        private Vec3 direction;
        private Fog fog;
        private final List<Bolt> bolts = new ArrayList<Bolt>();
        private final List<Glow> glows = new ArrayList<Glow>();
        private final Set<UUID> waveHits = new HashSet<UUID>();

        private State(long l) {
            this.nextSpell = l + 50L;
        }
    }

    private static final class Glow {
        private final Vec3 origin;
        private int age;

        private Glow(Vec3 vec3) {
            this.origin = vec3;
        }
    }

    private static final class Bolt {
        private Vec3 position;
        private final Vec3 velocity;
        private int age;

        private Bolt(Vec3 vec3, Vec3 vec32) {
            this.position = vec3;
            this.velocity = vec32;
        }
    }

    private static final class Fog {
        private final Vec3 origin;
        private int age;

        private Fog(Vec3 vec3) {
            this.origin = vec3;
        }
    }
}

