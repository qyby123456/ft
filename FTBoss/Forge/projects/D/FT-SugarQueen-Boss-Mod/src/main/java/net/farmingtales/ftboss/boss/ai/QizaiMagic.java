package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiStarEntity;
import net.farmingtales.ftboss.util.QizaiRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class QizaiMagic {
    private final EasterBossEntity boss;
    /** 第七招「绯星指针」的演出（2026-09-20 新增；数值见 {@link net.farmingtales.ftboss.util.QizaiSweepRules}） */
    private final net.farmingtales.ftboss.skill.QizaiSweep sweep;
    /** 第八招「天陨星裁」的演出（2026-09-20 v189 新增；数值见 {@link net.farmingtales.ftboss.util.QizaiSkyNovaRules}） */
    private final net.farmingtales.ftboss.skill.QizaiSkyNova skyNova;
    private final net.farmingtales.ftboss.skill.QizaiPrismLock prismLock;
    private final net.farmingtales.ftboss.skill.QizaiLaserBarrage laserBarrage;
    private final List<Vec3> marks = new ArrayList<>();
    private Vec3 aim = Vec3.ZERO;
    private int kind;
    private int remaining;
    private int recovery;
    private int releasesLeft;
    private int repeatWait;
    /* ★ v190 按用户要求「冷却减到 1/3」: 初始值也走同一个缩放，保持量纲一致 */
    private int cooldown = net.farmingtales.ftboss.util.QizaiRules.cooldown(40);
    private int stepCooldown = net.farmingtales.ftboss.util.QizaiRules.cooldown(80);
    private int sequence;

    public QizaiMagic(EasterBossEntity boss) {
        this.boss = boss;
        this.sweep = new net.farmingtales.ftboss.skill.QizaiSweep(boss);
        this.skyNova = new net.farmingtales.ftboss.skill.QizaiSkyNova(boss);
        this.prismLock = new net.farmingtales.ftboss.skill.QizaiPrismLock(boss);
        this.laserBarrage = new net.farmingtales.ftboss.skill.QizaiLaserBarrage(boss);
    }
    public boolean busy() { return kind != 0; }
    /** 天陨星裁与万棱星狱一旦起手便不可软打断；死亡、换变体、硬转场等仍可强制清理。 */
    public boolean uninterruptible() {
        if (boss.getVariant() != EasterBossEntity.QIZAI_VARIANT || !boss.isAlive() || boss.isRemoved()) return false;
        return kind == QizaiRules.SKY_NOVA && skyNova.busy()
                || kind == QizaiRules.LASER_BARRAGE && laserBarrage.busy();
    }
    public boolean protectedCast() {
        if (boss.getVariant() != EasterBossEntity.QIZAI_VARIANT || !boss.isAlive() || boss.isRemoved()) return false;
        /* ★★★ 2026-09-24 按用户要求「去除祁崽的绯色激光的无敌效果」★★★
           原来这里还有一条 {@code kind == SWEEP && sweep.busy()}（第七招「绯星指针」全程免伤）。
           那一招的演出就是一圈绯色星束激光，用户点名要去掉它的无敌 → 现在**只剩天陨星裁**
           （第八招，它自己 setInvulnerable 的实现不变）享受免伤。
           注意：绯星指针**仍然**由 {@link #uninterruptible()} 保证"流程不可软打断"
           （起手后不会被普通取消/清目标截断），去掉的只是"打不动"这一层。 */
        return kind == QizaiRules.SKY_NOVA && skyNova.busy();
    }
    public boolean skyCasting() { return kind == QizaiRules.SKY_NOVA && skyNova.busy(); }

    /**
     * 本体这一 tick 还能不能继续打。
     *
     * @param ignoreOwnLocks 天陨星裁或万棱星狱起手后忽略技能自身状态、清目标以及外部临时 NoAI；
     *        死亡、移除、换变体、迷你/巨型身份和终章转场仍属于必须清理的硬边界。
     */
    private boolean eligible(boolean ignoreOwnLocks) {
        return boss.getVariant() == EasterBossEntity.QIZAI_VARIANT && boss.isAlive() && !boss.isRemoved()
                && (ignoreOwnLocks || !boss.isNoAi()) && !boss.isMini() && !boss.isGiant()
                && (ignoreOwnLocks || !boss.isInvulnerable())
                && !ManorFight.isFinaleMember(boss);
    }

    private boolean target(LivingEntity target) {
        return target instanceof Player player && player.level() == boss.level() && player.isAlive()
                && !player.isRemoved() && !player.isCreative() && !player.isSpectator()
                && boss.distanceToSqr(player) <= QizaiRules.RANGE * QizaiRules.RANGE;
    }

    public void tick(ServerLevel level) {
        if (boss.getVariant() != EasterBossEntity.QIZAI_VARIANT) return;
        /* 天陨星裁与万棱星狱成功起手后都进入承诺施放：普通取消、临时 NoAI、目标清空不能截断；
           死亡、移除、换变体、迷你/巨型身份与终章转场仍由 eligible 的硬边界清理。 */
        boolean performing = uninterruptible();
        if (!eligible(performing)) { forceCancel(); return; }
        if (stepCooldown > 0) stepCooldown--;
        LivingEntity target = boss.getTarget();
        if (busy()) {
            boolean prismFinishingLockedShot = kind == QizaiRules.PRISM_LOCK && prismLock.locked();
            if (!performing && kind != QizaiRules.SWEEP && !prismFinishingLockedShot
                    && !target(target)) { cancel(); return; }
            boss.getNavigation().stop();
            boss.getLookControl().setLookAt(aim.x, aim.y, aim.z);
            /* 第八招「天陨星裁」：升空 -> 无法选中 -> 蓄力 5 秒 -> 半径 48 格 500 点。
               同样整场演出由 QizaiSkyNova 自己跑；它返回 false 表示这一招已经收完。 */
            if (kind == QizaiRules.SKY_NOVA) {
                if (skyNova.tick(level)) return;
                kind = 0;
                marks.clear();
                cooldown = boss.getChallengeDifficulty().acceleratedTicks(QizaiRules.cooldown(QizaiRules.REST * 2));
                return;
            }
            /* 第七招「绯星指针」：整场演出由 QizaiSweep 自己跑（起手 -> 扫射 -> 星雨 -> 收招） */
            if (kind == QizaiRules.SWEEP) {
                if (sweep.tick(level)) return;
                kind = 0;
                marks.clear();
                cooldown = boss.getChallengeDifficulty().acceleratedTicks(
                        QizaiRules.cooldown(QizaiRules.REST * net.farmingtales.ftboss.util.QizaiSweepRules.REST_MULTIPLIER));
                return;
            }
            if (kind == QizaiRules.PRISM_LOCK) {
                if (prismLock.tick(level)) return;
                kind = 0;
                marks.clear();
                cooldown = boss.getChallengeDifficulty().acceleratedTicks(QizaiRules.cooldown(
                        QizaiRules.REST * net.farmingtales.ftboss.util.QizaiPrismRules.REST_MULTIPLIER));
                return;
            }
            if (kind == QizaiRules.LASER_BARRAGE) {
                if (laserBarrage.tick(level)) return;
                kind = 0;
                marks.clear();
                cooldown = boss.getChallengeDifficulty().acceleratedTicks(QizaiRules.cooldown(
                        QizaiRules.REST * net.farmingtales.ftboss.util.QizaiLaserBarrageRules.REST_MULTIPLIER));
                return;
            }
            if (remaining == 0 && releasesLeft > 0) {
                if (--repeatWait <= 0) {
                    releasesLeft--;
                    release(level);
                    repeatWait = QizaiRules.REPEAT_INTERVAL;
                    recovery = QizaiRules.recovery(kind);
                    cue(level, recovery, true);
                }
                return;
            }
            if (remaining > 0 && --remaining == 0) {
                releasesLeft = QizaiRules.RELEASES - 1;
                repeatWait = QizaiRules.REPEAT_INTERVAL;
                release(level);
                recovery = QizaiRules.recovery(kind);
                cue(level, recovery, true);
            } else if (remaining == 0 && --recovery <= 0) {
                kind = 0;
                marks.clear();
                boss.setQizaiCue(new CompoundTag());
                cooldown = boss.getChallengeDifficulty().acceleratedTicks(QizaiRules.cooldown(QizaiRules.REST));
            }
            return;
        }
        if (cooldown > 0) cooldown--;
        if (!target(target) || !boss.hasLineOfSight(target)) return;
        if (stepCooldown == 0) {
            Player nearby = level.players().stream().filter(player -> target(player)
                    && boss.position().subtract(player.position()).horizontalDistanceSqr() <= QizaiRules.STEP_TRIGGER_RANGE * QizaiRules.STEP_TRIGGER_RANGE
                    && Math.abs(boss.getY() - player.getY()) <= 3 && boss.hasLineOfSight(player))
                    .min(java.util.Comparator.comparingDouble(boss::distanceToSqr)).orElse(null);
            if (nearby != null) {
                /* ★ 2026-09-21: 星步的重试间隔也过难度（终结难度减半）—— 用户要求把这条漏掉的例外补齐。 */
                stepCooldown = boss.getChallengeDifficulty().acceleratedTicks(QizaiRules.STEP_RETRY_TICKS);
                if (boss.getRandom().nextFloat() < QizaiRules.STEP_CHANCE && step(level, nearby)) return;
            }
        }
        if (cooldown == 0 && begin(level, target, QizaiRules.next(sequence))) sequence++;
    }

    public boolean begin(ServerLevel level, LivingEntity target, int spell) {
        if (level != boss.level() || busy() || !eligible(false) || !target(target) || !boss.hasLineOfSight(target)
                || !QizaiRules.spell(spell)) return false;
        marks.clear();
        releasesLeft = 0;
        repeatWait = 0;
        aim = target.getEyePosition();
        /* 第八招「天陨星裁」：不打地面标记，直接交给 QizaiSkyNova 起手（它自己管完整场演出） */
        if (spell == QizaiRules.SKY_NOVA) {
            if (!skyNova.begin(level, target)) return false;
            kind = QizaiRules.SKY_NOVA;
            remaining = 0;
            recovery = 0;
            return true;
        }
        /* 第七招「绯星指针」：不打地面标记，直接交给 QizaiSweep 起手（它自己管完整场演出） */
        if (spell == QizaiRules.SWEEP) {
            if (!sweep.begin(level, target)) return false;
            kind = QizaiRules.SWEEP;
            remaining = 0;
            recovery = 0;
            return true;
        }
        if (spell == QizaiRules.PRISM_LOCK) {
            if (!prismLock.begin(level, target)) return false;
            kind = QizaiRules.PRISM_LOCK;
            remaining = 0;
            recovery = 0;
            return true;
        }
        if (spell == QizaiRules.LASER_BARRAGE) {
            if (!laserBarrage.begin(level, target)) return false;
            kind = QizaiRules.LASER_BARRAGE;
            remaining = 0;
            recovery = 0;
            return true;
        }
        if (QizaiRules.constellation(spell)) {
            marks.addAll(net.farmingtales.ftboss.skill.QizaiConstellation.marks(level, boss, spell, target.position()));
            if (marks.isEmpty()) return false;
        } else if (spell != QizaiRules.FAN) {
            Vec3 center = floor(level, target.position());
            if (center == null) return false;
            marks.add(center);
            if (spell == QizaiRules.PRAYER) {
                Vec3 side = target.position().subtract(boss.position()).multiply(1, 0, 1).normalize().yRot((float) (Math.PI / 2));
                for (int sign : new int[]{-1, 1}) {
                    Vec3 mark = floor(level, center.add(side.scale(sign * 7)));
                    if (mark != null) marks.add(mark);
                }
            }
        }
        kind = spell;
        remaining = boss.getChallengeDifficulty().acceleratedTicks(QizaiRules.windup(kind));
        recovery = 0;
        boss.getNavigation().stop();
        boss.playCast(remaining + QizaiRules.repeatTicks() + QizaiRules.recovery(kind), kind == QizaiRules.PRAYER ? 0 : kind == QizaiRules.CIRCLE ? 3 : 2);
        cue(level, remaining, false);
        if (kind == QizaiRules.PRAYER) net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem.animate(boss, true);
        level.playSound(null, boss.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.2F, kind == QizaiRules.PRAYER ? 0.65F : 1.2F);
        ManorPredictiveAttacks.extraSpell(level, boss, target, spell);
        return true;
    }

    private Vec3 floor(ServerLevel level, Vec3 position) {
        if (!level.hasChunkAt(net.minecraft.core.BlockPos.containing(position))) return null;
        var hit = level.clip(new ClipContext(position.add(0, 0.5, 0), position.add(0, -10, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getLocation().add(0, 0.035, 0) : null;
    }

    private void release(ServerLevel level) {
        if (QizaiRules.constellation(kind)) {
            /* ★ 2026-09-20 v188 按用户要求「单次施法数量 4 个」+「允许使用铁魔法防御」：
               这一条读条同时放 4 门法术 = 主招 + 另一门自己的招 + 2 门庄园辅助（护盾/治疗）。
               铁魔法没接上时 QizaiMulticast 返回 false，仍旧走下面的纯代码回退。 */
            if (!net.farmingtales.ftboss.skill.QizaiMulticast.cast(level, boss, kind, marks, aim)) {
                float damage = kind == QizaiRules.STAR_CROSS ? QizaiRules.CROSS_DAMAGE : QizaiRules.STARFALL_DAMAGE;
                net.farmingtales.ftboss.skill.QizaiConstellation.cast(level, boss, kind, marks, victim -> {
                    if (target(victim) && !boss.isAlliedTo(victim) && !victim.isAlliedTo(boss)) {
                        victim.invulnerableTime = 0;
                        victim.hurt(level.damageSources().indirectMagic(boss, boss), damage);
                    }
                });
            }
            return;
        }
        if (kind == QizaiRules.PRAYER) net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem.animate(boss, false);
        if (kind == QizaiRules.FAN) {
            Vec3 origin = boss.getEyePosition().add(0, -0.15, 0);
            Vec3 direction = aim.subtract(origin).normalize();
            for (int lane = -1; lane <= 1; lane++) {
                var star = new QizaiStarEntity(level, boss, origin);
                Vec3 velocity = direction.yRot(lane * 0.19F).scale(QizaiRules.STAR_SPEED);
                star.setDeltaMovement(velocity);
                level.addFreshEntity(star);
            }
        } else {
            for (Player player : level.players()) {
                if (!target(player)) continue;
                for (Vec3 mark : marks) {
                    Vec3 delta = player.position().subtract(mark);
                    if (!QizaiRules.inside(delta.horizontalDistanceSqr(), delta.y, kind)) continue;
                    var obstruction = level.clip(new ClipContext(mark.add(0, 1.5, 0), player.getEyePosition(),
                            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss));
                    if (obstruction.getType() != HitResult.Type.MISS) continue;
                    player.invulnerableTime = 0;
                    player.hurt(level.damageSources().indirectMagic(boss, boss),
                            kind == QizaiRules.PRAYER ? QizaiRules.PRAYER_DAMAGE : QizaiRules.CIRCLE_DAMAGE);
                    break;
                }
            }
        }
        level.playSound(null, boss.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 1.3F, kind == QizaiRules.PRAYER ? 0.6F : 1.4F);
    }

    private boolean step(ServerLevel level, LivingEntity target) {
        Vec3 origin = boss.position();
        Vec3 destination = null;
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = boss.getRandom().nextDouble() * Math.PI * 2;
            double radius = QizaiRules.STEP_MIN_DISTANCE + boss.getRandom().nextDouble()
                    * (QizaiRules.STEP_MAX_DISTANCE - QizaiRules.STEP_MIN_DISTANCE);
            Vec3 candidate = ManorSafeTeleport.find(level, boss, origin.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius), 1);
            if (candidate == null) continue;
            double distance = candidate.subtract(origin).horizontalDistanceSqr();
            if (distance < QizaiRules.STEP_MIN_DISTANCE * QizaiRules.STEP_MIN_DISTANCE
                    || distance > QizaiRules.STEP_MAX_DISTANCE * QizaiRules.STEP_MAX_DISTANCE
                    || Math.abs(candidate.y - origin.y) > 3
                    || candidate.distanceToSqr(target.position()) < 36
                    || level.players().stream().anyMatch(player -> target(player) && candidate.distanceToSqr(player.position()) < 9)
                    || level.clip(new ClipContext(boss.getEyePosition(), candidate.add(0, boss.getEyeHeight(), 0),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss)).getType() != HitResult.Type.MISS) continue;
            destination = candidate;
            break;
        }
        if (destination == null) return false;
        marks.clear();
        marks.add(boss.position());
        ManorSafeTeleport.move(level, boss, destination);
        kind = QizaiRules.STEP;
        remaining = 0;
        recovery = 12;
        aim = target.getEyePosition();
        /* ★ 2026-09-21: 星步冷却原先不过难度，现在是最后一条被补齐的例外（终结难度减半）。 */
        stepCooldown = boss.getChallengeDifficulty().acceleratedTicks(QizaiRules.STEP_COOLDOWN);
        cue(level, recovery, true);
        boss.playCast(recovery, 4);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, origin.x, origin.y + 1, origin.z, 16, 0.3, 0.6, 0.3, 0.04);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, destination.x, destination.y + 1, destination.z, 16, 0.3, 0.6, 0.3, 0.04);
        level.playSound(null, net.minecraft.core.BlockPos.containing(origin), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.7F, 1.4F);
        level.playSound(null, boss.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 0.8F, 1.8F);
        return true;
    }

    private void cue(ServerLevel level, int duration, boolean released) {
        CompoundTag cue = new CompoundTag();
        cue.putInt("qizai", kind);
        cue.putLong("start", level.getGameTime());
        cue.putInt("duration", duration);
        cue.putBoolean("released", released);
        cue.putInt("volley", released && QizaiRules.spell(kind) ? QizaiRules.RELEASES - releasesLeft : 0);
        cue.putDouble("radius", QizaiRules.radius(kind));
        ListTag positions = new ListTag();
        for (Vec3 mark : marks) {
            CompoundTag point = new CompoundTag();
            point.putDouble("x", mark.x);
            point.putDouble("y", mark.y);
            point.putDouble("z", mark.z);
            positions.add(point);
        }
        cue.put("marks", positions);
        boss.setQizaiCue(cue);
    }

    /** 普通战斗中断：天陨星裁或万棱星狱已经起手时拒绝中断，其它技能仍按旧规则取消。 */
    public void cancel() {
        if (uninterruptible()) return;
        forceCancel();
    }

    /** 生命周期硬清理：死亡、换变体、转场、实体移除时允许终止两门承诺施放。 */
    public void forceCancel() {
        net.farmingtales.ftboss.skill.QizaiConstellation.cancel(boss);
        sweep.cancel();
        /* ★ 天陨星裁被中断时**必须**先收拾它：它会在蓄力期把本体设成无敌并抬到空中，
           只清 kind 不清它 = 无敌与悬空残留（这个坑很隐蔽，所以单独列一行）。 */
        skyNova.cancel();
        prismLock.cancel();
        laserBarrage.cancel();
        if (kind == QizaiRules.PRAYER) net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem.animate(boss, false);
        if (busy()) boss.setQizaiCue(new CompoundTag());
        kind = 0;
        remaining = 0;
        recovery = 0;
        cooldown = net.farmingtales.ftboss.util.QizaiRules.cooldown(40);
        releasesLeft = 0;
        repeatWait = 0;
        marks.clear();
    }
}
