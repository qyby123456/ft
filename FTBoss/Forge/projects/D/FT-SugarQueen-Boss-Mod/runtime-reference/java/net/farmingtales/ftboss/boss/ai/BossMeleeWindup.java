/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.function.Predicate;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public final class BossMeleeWindup {
    private final Mob owner;
    private final BossActionState action;
    private final int impactTick;
    private final int duration;
    private final double reach;
    private LivingEntity target;

    public BossMeleeWindup(Mob owner, BossActionState action, int impactTick, int duration, double reach) {
        this.owner = owner;
        this.action = action;
        this.impactTick = impactTick;
        this.duration = duration;
        this.reach = reach;
    }

    public void start(Entity candidate) {
        LivingEntity living;
        if (this.owner.m_9236_().f_46443_ || !this.owner.m_6084_() || this.action.active()) {
            return;
        }
        if (!(candidate instanceof LivingEntity) || !this.valid(living = (LivingEntity)candidate)) {
            return;
        }
        this.target = living;
        this.action.start("melee", this.duration);
        this.owner.m_21563_().m_24960_((Entity)living, 30.0f, 30.0f);
    }

    public void tick(Predicate<LivingEntity> hit) {
        if (this.target == null || this.owner.m_9236_().f_46443_) {
            return;
        }
        if (!"melee".equals(this.action.name())) {
            this.target = null;
            return;
        }
        if (this.action.elapsed(0.0f) < (float)this.impactTick) {
            return;
        }
        LivingEntity victim = this.target;
        this.target = null;
        Level level = this.owner.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel server = (ServerLevel)level;
        server.m_5594_(null, this.owner.m_20183_(), SoundEvents.f_12317_, SoundSource.HOSTILE, 0.8f, 0.8f);
        if (!this.valid(victim) || !hit.test(victim)) {
            return;
        }
        server.m_8767_((ParticleOptions)ParticleTypes.f_123797_, victim.m_20185_(), victim.m_20186_() + (double)victim.m_20206_() * 0.5, victim.m_20189_(), 6, 0.18, 0.18, 0.18, 0.08);
    }

    private boolean valid(LivingEntity candidate) {
        return candidate.m_6084_() && !candidate.m_213877_() && candidate.m_9236_() == this.owner.m_9236_() && BossTargets.valid(candidate) && this.owner.m_20280_((Entity)candidate) <= this.reach * this.reach && this.owner.m_142582_((Entity)candidate);
    }
}

