/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public final class BossApproach {
    private Motion motion = Motion.HOLD;
    private UUID targetId;
    private Vec3 lastTarget;
    private long nextPath;

    public static Motion choose(Motion previous, double distance, boolean visible, double stop, double resume, double retreatStart, double retreatStop) {
        if (!visible) {
            return Motion.APPROACH;
        }
        if (retreatStart > 0.0 && (distance < retreatStart || previous == Motion.RETREAT && distance < retreatStop)) {
            return Motion.RETREAT;
        }
        if (distance > resume || previous == Motion.APPROACH && distance > stop) {
            return Motion.APPROACH;
        }
        return Motion.HOLD;
    }

    public void tick(Mob owner, LivingEntity target, double speed, double stop, double resume, double retreatStart, double retreatStop) {
        Motion next;
        boolean changedTarget;
        boolean bl = changedTarget = !target.m_20148_().equals(this.targetId);
        if (changedTarget) {
            this.targetId = target.m_20148_();
            this.motion = Motion.HOLD;
            this.lastTarget = null;
            this.nextPath = 0L;
        }
        boolean changedMotion = (next = BossApproach.choose(this.motion, owner.m_20270_((Entity)target), owner.m_142582_((Entity)target), stop, resume, retreatStart, retreatStop)) != this.motion;
        this.motion = next;
        if (next == Motion.HOLD) {
            if (!owner.m_21573_().m_26571_()) {
                owner.m_21573_().m_26573_();
            }
            return;
        }
        long now = owner.m_9236_().m_46467_();
        if (!changedTarget && !changedMotion && now < this.nextPath) {
            return;
        }
        this.nextPath = now + 10L;
        if (!changedTarget && !changedMotion && !owner.m_21573_().m_26571_() && this.lastTarget != null && this.lastTarget.m_82557_(target.m_20182_()) < 1.0) {
            owner.m_21573_().m_26517_(speed);
            return;
        }
        this.lastTarget = target.m_20182_();
        if (next == Motion.APPROACH) {
            owner.m_21573_().m_5624_((Entity)target, speed);
        } else {
            Vec3 away = owner.m_20182_().m_82546_(target.m_20182_()).m_82542_(1.0, 0.0, 1.0);
            if (away.m_82556_() < 1.0E-6) {
                away = new Vec3(1.0, 0.0, 0.0);
            }
            Vec3 destination = target.m_20182_().m_82549_(away.m_82541_().m_82490_(retreatStop + 0.5));
            owner.m_21573_().m_26519_(destination.f_82479_, owner.m_20186_(), destination.f_82481_, speed);
        }
    }

    public void reset(Mob owner) {
        this.motion = Motion.HOLD;
        this.targetId = null;
        this.lastTarget = null;
        this.nextPath = 0L;
        owner.m_21573_().m_26573_();
    }

    public static enum Motion {
        HOLD,
        APPROACH,
        RETREAT;

    }
}

