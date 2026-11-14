package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public final class ManorKiting {
    public enum Motion { APPROACH, RETREAT, CIRCLE }

    private UUID targetId;
    private Motion motion;
    private Vec3 lastTarget;
    private int nextPath;
    private int retreatUntil;
    private int direction = 1;

    public static boolean enabled(EasterBossEntity boss) {
        return boss.isImpossibleChallenge() && !boss.isMini() && !boss.isGiant()
                && !boss.isNoAi() && boss.isAlive() && !boss.isRemoved()
                && !HiddenBossIntro.isActing(boss.getUUID()) && boss.castsWithoutStopping();
    }

    public static Motion choose(Motion previous, double distance, boolean visible, boolean ranged, boolean engage) {
        if (!visible) return Motion.APPROACH;
        if (engage && !ranged) return distance > 2.3 ? Motion.APPROACH : Motion.CIRCLE;
        double near = ranged ? 6.0 : 3.5;
        double clear = ranged ? 8.0 : 5.0;
        if (distance < near || previous == Motion.RETREAT && distance < clear) return Motion.RETREAT;
        if (distance > (ranged ? 12.0 : 7.0)) return Motion.APPROACH;
        return Motion.CIRCLE;
    }

    public void afterMelee(EasterBossEntity boss) {
        this.retreatUntil = boss.tickCount + 20;
        this.nextPath = 0;
    }

    public void reset(EasterBossEntity boss) {
        this.targetId = null;
        this.motion = null;
        this.lastTarget = null;
        this.nextPath = 0;
        this.retreatUntil = 0;
        boss.getNavigation().stop();
    }

    public boolean evadeSupport(EasterBossEntity boss) {
        if (!enabled(boss) || !(boss.level() instanceof ServerLevel level)) return false;
        var player = BossTargets.nearest(level, boss, 8.0);
        if (player == null) {
            if (this.targetId != null) reset(boss);
            return false;
        }
        move(boss, player, 1.15, true, true);
        return true;
    }

    public void tick(EasterBossEntity boss, LivingEntity target, double speed, boolean ranged) {
        if (!enabled(boss) || !BossTargets.valid(target) || target.level() != boss.level()) {
            reset(boss);
            return;
        }
        move(boss, target, speed, ranged, false);
    }

    private void move(EasterBossEntity boss, LivingEntity target, double speed, boolean ranged, boolean support) {
        int now = boss.tickCount;
        boolean changedTarget = !target.getUUID().equals(this.targetId);
        if (changedTarget) {
            this.targetId = target.getUUID();
            this.motion = null;
            this.lastTarget = null;
            this.retreatUntil = 0;
            this.nextPath = 0;
            this.direction = (boss.getUUID().getLeastSignificantBits() & 1L) == 0 ? 1 : -1;
        }
        boolean engage = !support && !ranged && now >= this.retreatUntil && Math.floorMod(now, 50) >= 30;
        Motion next = support ? Motion.RETREAT
                : choose(this.motion, boss.distanceTo(target), boss.hasLineOfSight(target), ranged, engage);
        boolean changedMotion = this.motion != next;
        this.motion = next;
        if (!changedTarget && !changedMotion && now < this.nextPath) return;
        this.nextPath = now + 6;
        if (!changedTarget && !changedMotion && !boss.getNavigation().isDone()
                && this.lastTarget != null && this.lastTarget.distanceToSqr(target.position()) < 2.25) {
            boss.getNavigation().setSpeedModifier(speed);
            return;
        }
        this.lastTarget = target.position();
        if (next == Motion.APPROACH) {
            Path path = boss.getNavigation().createPath(target, 0);
            if (safe(path) && boss.getNavigation().moveTo(path, speed)) return;
            boss.getNavigation().stop();
            return;
        }
        Vec3 away = boss.position().subtract(target.position()).multiply(1, 0, 1);
        if (away.lengthSqr() < 1.0E-6) away = new Vec3(1, 0, 0);
        away = away.normalize();
        double radius = next == Motion.RETREAT ? (ranged ? 9.0 : 5.5)
                : engage ? 2.3 : ranged ? 9.0 : 5.5;
        double turn = next == Motion.RETREAT ? 0.0 : 0.65 * this.direction;
        for (double offset : new double[] {0, 0.55 * this.direction, -0.55 * this.direction, 1.0, -1.0}) {
            double angle = turn + offset;
            Vec3 radial = new Vec3(away.x * Math.cos(angle) - away.z * Math.sin(angle), 0,
                    away.x * Math.sin(angle) + away.z * Math.cos(angle));
            Vec3 destination = target.position().add(radial.scale(radius));
            Path path = boss.getNavigation().createPath(BlockPos.containing(destination.x, boss.getY(), destination.z), 0);
            if (!safe(path) || !path.canReach()) continue;
            var end = path.getEndNode();
            Vec3 endpoint = new Vec3(end.x + 0.5, end.y, end.z + 0.5);
            if (next == Motion.RETREAT && endpoint.distanceToSqr(target.position()) <= boss.distanceToSqr(target) + 1) continue;
            if (boss.getNavigation().moveTo(path, speed)) return;
        }
        this.direction = -this.direction;
        boss.getNavigation().stop();
    }

    private static boolean safe(Path path) {
        if (path == null || path.getNodeCount() == 0) return false;
        for (int index = 0; index < path.getNodeCount(); index++) {
            BlockPathTypes type = path.getNode(index).type;
            if (type != BlockPathTypes.WALKABLE && type != BlockPathTypes.OPEN
                    && type != BlockPathTypes.DOOR_OPEN && type != BlockPathTypes.WALKABLE_DOOR) return false;
        }
        return path.getEndNode().type != BlockPathTypes.OPEN;
    }
}
