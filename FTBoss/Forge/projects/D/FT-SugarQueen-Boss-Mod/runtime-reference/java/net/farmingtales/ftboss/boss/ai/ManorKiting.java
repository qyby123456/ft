/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.pathfinder.BlockPathTypes
 *  net.minecraft.world.level.pathfinder.Node
 *  net.minecraft.world.level.pathfinder.Path
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public final class ManorKiting {
    private UUID targetId;
    private Motion motion;
    private Vec3 lastTarget;
    private int nextPath;
    private int retreatUntil;
    private int direction = 1;

    public static boolean enabled(EasterBossEntity boss) {
        return boss.isImpossibleChallenge() && !boss.isMini() && !boss.isGiant() && !boss.m_21525_() && boss.m_6084_() && !boss.m_213877_() && !HiddenBossIntro.isActing(boss.m_20148_()) && boss.castsWithoutStopping();
    }

    public static Motion choose(Motion previous, double distance, boolean visible, boolean ranged, boolean engage) {
        double clear;
        if (!visible) {
            return Motion.APPROACH;
        }
        if (engage && !ranged) {
            return distance > 2.3 ? Motion.APPROACH : Motion.CIRCLE;
        }
        double near = ranged ? 6.0 : 3.5;
        double d = clear = ranged ? 8.0 : 5.0;
        if (distance < near || previous == Motion.RETREAT && distance < clear) {
            return Motion.RETREAT;
        }
        double d2 = ranged ? 12.0 : 7.0;
        if (distance > d2) {
            return Motion.APPROACH;
        }
        return Motion.CIRCLE;
    }

    public void afterMelee(EasterBossEntity boss) {
        this.retreatUntil = boss.f_19797_ + 20;
        this.nextPath = 0;
    }

    public void reset(EasterBossEntity boss) {
        this.targetId = null;
        this.motion = null;
        this.lastTarget = null;
        this.nextPath = 0;
        this.retreatUntil = 0;
        boss.m_21573_().m_26573_();
    }

    public boolean evadeSupport(EasterBossEntity boss) {
        Level level;
        if (!ManorKiting.enabled(boss) || !((level = boss.m_9236_()) instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel)level;
        ServerPlayer player = BossTargets.nearest(level2, (Entity)boss, 8.0);
        if (player == null) {
            if (this.targetId != null) {
                this.reset(boss);
            }
            return false;
        }
        this.move(boss, (LivingEntity)player, 1.15, true, true);
        return true;
    }

    public void tick(EasterBossEntity boss, LivingEntity target, double speed, boolean ranged) {
        if (!ManorKiting.enabled(boss) || !BossTargets.valid(target) || target.m_9236_() != boss.m_9236_()) {
            this.reset(boss);
            return;
        }
        this.move(boss, target, speed, ranged, false);
    }

    private void move(EasterBossEntity boss, LivingEntity target, double speed, boolean ranged, boolean support) {
        boolean changedTarget;
        int now = boss.f_19797_;
        boolean bl = changedTarget = !target.m_20148_().equals(this.targetId);
        if (changedTarget) {
            this.targetId = target.m_20148_();
            this.motion = null;
            this.lastTarget = null;
            this.retreatUntil = 0;
            this.nextPath = 0;
            this.direction = (boss.m_20148_().getLeastSignificantBits() & 1L) == 0L ? 1 : -1;
        }
        boolean engage = !support && !ranged && now >= this.retreatUntil && Math.floorMod(now, 50) >= 30;
        Motion next = support ? Motion.RETREAT : ManorKiting.choose(this.motion, boss.m_20270_((Entity)target), boss.m_142582_((Entity)target), ranged, engage);
        boolean changedMotion = this.motion != next;
        this.motion = next;
        if (!changedTarget && !changedMotion && now < this.nextPath) {
            return;
        }
        this.nextPath = now + 6;
        if (!changedTarget && !changedMotion && !boss.m_21573_().m_26571_() && this.lastTarget != null && this.lastTarget.m_82557_(target.m_20182_()) < 2.25) {
            boss.m_21573_().m_26517_(speed);
            return;
        }
        this.lastTarget = target.m_20182_();
        if (next == Motion.APPROACH) {
            Path path = boss.m_21573_().m_6570_((Entity)target, 0);
            if (ManorKiting.safe(path) && boss.m_21573_().m_26536_(path, speed)) {
                return;
            }
            boss.m_21573_().m_26573_();
            return;
        }
        Vec3 away = boss.m_20182_().m_82546_(target.m_20182_()).m_82542_(1.0, 0.0, 1.0);
        if (away.m_82556_() < 1.0E-6) {
            away = new Vec3(1.0, 0.0, 0.0);
        }
        away = away.m_82541_();
        double radius = next == Motion.RETREAT ? (ranged ? 9.0 : 5.5) : (engage ? 2.3 : (ranged ? 9.0 : 5.5));
        double turn = next == Motion.RETREAT ? 0.0 : 0.65 * (double)this.direction;
        for (double offset : new double[]{0.0, 0.55 * (double)this.direction, -0.55 * (double)this.direction, 1.0, -1.0}) {
            double angle = turn + offset;
            Vec3 radial = new Vec3(away.f_82479_ * Math.cos(angle) - away.f_82481_ * Math.sin(angle), 0.0, away.f_82479_ * Math.sin(angle) + away.f_82481_ * Math.cos(angle));
            Vec3 destination = target.m_20182_().m_82549_(radial.m_82490_(radius));
            Path path = boss.m_21573_().m_7864_(BlockPos.m_274561_((double)destination.f_82479_, (double)boss.m_20186_(), (double)destination.f_82481_), 0);
            if (!ManorKiting.safe(path) || !path.m_77403_()) continue;
            Node end = path.m_77395_();
            Vec3 endpoint = new Vec3((double)end.f_77271_ + 0.5, (double)end.f_77272_, (double)end.f_77273_ + 0.5);
            if (next == Motion.RETREAT && endpoint.m_82557_(target.m_20182_()) <= boss.m_20280_((Entity)target) + 1.0 || !boss.m_21573_().m_26536_(path, speed)) continue;
            return;
        }
        this.direction = -this.direction;
        boss.m_21573_().m_26573_();
    }

    private static boolean safe(Path path) {
        if (path == null || path.m_77398_() == 0) {
            return false;
        }
        for (int index = 0; index < path.m_77398_(); ++index) {
            BlockPathTypes type = path.m_77375_((int)index).f_77282_;
            if (type == BlockPathTypes.WALKABLE || type == BlockPathTypes.OPEN || type == BlockPathTypes.DOOR_OPEN || type == BlockPathTypes.WALKABLE_DOOR) continue;
            return false;
        }
        return path.m_77395_().f_77282_ != BlockPathTypes.OPEN;
    }

    public static enum Motion {
        APPROACH,
        RETREAT,
        CIRCLE;

    }
}

