package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public final class BossApproach {
    public enum Motion { HOLD, APPROACH, RETREAT }

    private Motion motion = Motion.HOLD;
    private UUID targetId;
    private Vec3 lastTarget;
    private long nextPath;

    public static Motion choose(Motion previous, double distance, boolean visible,
                                double stop, double resume, double retreatStart, double retreatStop) {
        if (!visible) return Motion.APPROACH;
        if (retreatStart > 0 && (distance < retreatStart
                || previous == Motion.RETREAT && distance < retreatStop)) return Motion.RETREAT;
        if (distance > resume || previous == Motion.APPROACH && distance > stop) return Motion.APPROACH;
        return Motion.HOLD;
    }

    public void tick(Mob owner, LivingEntity target, double speed, double stop, double resume,
                     double retreatStart, double retreatStop) {
        boolean changedTarget = !target.getUUID().equals(this.targetId);
        if (changedTarget) {
            this.targetId = target.getUUID();
            this.motion = Motion.HOLD;
            this.lastTarget = null;
            this.nextPath = 0;
        }
        Motion next = choose(this.motion, owner.distanceTo(target), owner.hasLineOfSight(target),
                stop, resume, retreatStart, retreatStop);
        boolean changedMotion = next != this.motion;
        this.motion = next;
        if (next == Motion.HOLD) {
            if (!owner.getNavigation().isDone()) owner.getNavigation().stop();
            return;
        }
        long now = owner.level().getGameTime();
        if (!changedTarget && !changedMotion && now < this.nextPath) return;
        this.nextPath = now + 10;
        if (!changedTarget && !changedMotion && !owner.getNavigation().isDone()
                && this.lastTarget != null && this.lastTarget.distanceToSqr(target.position()) < 1.0) {
            owner.getNavigation().setSpeedModifier(speed);
            return;
        }
        this.lastTarget = target.position();
        if (next == Motion.APPROACH) owner.getNavigation().moveTo(target, speed);
        else {
            Vec3 away = owner.position().subtract(target.position()).multiply(1, 0, 1);
            if (away.lengthSqr() < 1.0E-6) away = new Vec3(1, 0, 0);
            Vec3 destination = target.position().add(away.normalize().scale(retreatStop + 0.5));
            owner.getNavigation().moveTo(destination.x, owner.getY(), destination.z, speed);
        }
    }

    public void reset(Mob owner) {
        this.motion = Motion.HOLD;
        this.targetId = null;
        this.lastTarget = null;
        this.nextPath = 0;
        owner.getNavigation().stop();
    }
}
