/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.player.Player
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.ButcherCombatRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;

public final class ButcherMinionMeleeGoal
extends MeleeAttackGoal {
    private final int interval;
    private long nextAttack;

    public ButcherMinionMeleeGoal(PathfinderMob owner, int kind) {
        super(owner, 1.3, true);
        this.interval = ButcherCombatRules.minionInterval(kind);
    }

    public boolean m_8036_() {
        return this.eligible(this.f_25540_.m_5448_()) && super.m_8036_();
    }

    public boolean m_8045_() {
        return this.eligible(this.f_25540_.m_5448_()) && super.m_8045_();
    }

    private boolean eligible(LivingEntity target) {
        ServerPlayer player;
        return target instanceof ServerPlayer && BossTargets.valid((Player)(player = (ServerPlayer)target)) && !this.f_25540_.m_7307_((Entity)player) && this.f_25540_.m_9236_() == player.m_9236_();
    }

    protected void m_6739_(LivingEntity target, double distanceSquared) {
        long now = this.f_25540_.m_9236_().m_46467_();
        if (now < this.nextAttack || !this.eligible(target) || !this.f_25540_.m_142582_((Entity)target) || distanceSquared > this.m_6639_(target)) {
            return;
        }
        this.nextAttack = now + (long)this.interval;
        this.f_25540_.m_6674_(InteractionHand.MAIN_HAND);
        this.f_25540_.m_7327_((Entity)target);
    }
}

