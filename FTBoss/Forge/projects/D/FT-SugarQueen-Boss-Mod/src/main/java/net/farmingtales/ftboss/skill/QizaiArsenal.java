package net.farmingtales.ftboss.skill;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiStarEntity;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.QizaiArsenalRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class QizaiArsenal {
    private static final class Salvo {
        final long start;
        final Vec3 center;
        final List<Vec3> crystals = new ArrayList<>();
        final List<Vec3> ends = new ArrayList<>();
        final Set<UUID> hit = new HashSet<>();
        int age;
        Salvo(long start, Vec3 center) { this.start = start; this.center = center; }
    }

    private final EasterBossEntity boss;
    private final List<Salvo> salvos = new ArrayList<>();
    private final List<QizaiStarEntity> missiles = new ArrayList<>();
    private int cooldown = 60;
    private int pushCooldown;
    private int pushTicks;
    private int pushFade;
    private Vec3 pushOrigin = Vec3.ZERO;
    private long pushStarted;

    public QizaiArsenal(EasterBossEntity boss) { this.boss = boss; }

    private boolean eligible() {
        return boss.getVariant() == EasterBossEntity.QIZAI_VARIANT && boss.isAlive() && !boss.isRemoved()
                && !boss.isMini() && !boss.isGiant() && !boss.isNoAi() && !ManorFight.isFinaleMember(boss);
    }

    private boolean enemy(Player player) {
        return player.isAlive() && !player.isRemoved() && !player.isCreative() && !player.isSpectator()
                && !boss.isAlliedTo(player) && !player.isAlliedTo(boss);
    }

    public boolean cast(ServerLevel level, Vec3 aim) {
        if (!eligible() || boss.isInvulnerable() || level != boss.level() || salvos.size() >= QizaiArsenalRules.MAX_SALVOS
                || !Double.isFinite(aim.x + aim.y + aim.z)) return false;
        Salvo salvo = new Salvo(level.getGameTime(), boss.position());
        Vec3 target = aim;
        for (int index = 0; index < QizaiArsenalRules.CRYSTALS; index++) {
            double angle = index * Math.PI * 2 / QizaiArsenalRules.CRYSTALS;
            Vec3 origin = boss.position().add(Math.cos(angle) * 2.3, 2.6 + (index % 2) * 0.8, Math.sin(angle) * 2.3);
            Vec3 end = origin.add(target.subtract(origin).normalize().scale(QizaiArsenalRules.RANGE));
            salvo.crystals.add(origin);
            salvo.ends.add(clip(level, origin, end));
        }
        salvos.add(salvo);
        cooldown = QizaiArsenalRules.COOLDOWN;
        sync();
        level.playSound(null, boss.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.2F, 0.75F);
        return true;
    }

    public void tick(ServerLevel level, boolean skyCasting, boolean primaryBusy) {
        if (!eligible()) { cancel(); return; }
        if (skyCasting || boss.isInvulnerable()) { cancel(); return; }
        missiles.removeIf(star -> star.isRemoved() || !star.isAlive());
        if (cooldown > 0) cooldown--;
        if (pushCooldown > 0) pushCooldown--;
        if (pushFade > 0) pushFade--;
        if (!primaryBusy && cooldown == 0 && salvos.isEmpty() && boss.getTarget() instanceof Player target
                && enemy(target) && boss.distanceToSqr(target) <= QizaiArsenalRules.RANGE * QizaiArsenalRules.RANGE
                && boss.hasLineOfSight(target)) cast(level, target.getEyePosition());
        for (var iterator = salvos.iterator(); iterator.hasNext();) {
            Salvo salvo = iterator.next();
            if (++salvo.age == QizaiArsenalRules.CHARGE) launch(level, salvo);
            for (int index = 0; index < QizaiArsenalRules.CRYSTALS; index++) {
                if (salvo.age == QizaiArsenalRules.CHARGE + index * QizaiArsenalRules.LASER_GAP) laser(level, salvo, index);
            }
            if (salvo.age >= QizaiArsenalRules.SALVO_DURATION) iterator.remove();
        }
        if (pushTicks > 0) {
            if (--pushTicks == 0) repel(level);
        } else if (pushCooldown == 0) {
            for (Player player : level.players()) {
                Vec3 offset = player.position().subtract(boss.position());
                if (!enemy(player) || offset.horizontalDistanceSqr() > QizaiArsenalRules.PUSH_TRIGGER * QizaiArsenalRules.PUSH_TRIGGER
                        || Math.abs(offset.y) > 3 || !boss.hasLineOfSight(player)) continue;
                pushOrigin = boss.position();
                pushStarted = level.getGameTime();
                pushTicks = QizaiArsenalRules.PUSH_WINDUP;
                pushCooldown = QizaiArsenalRules.PUSH_COOLDOWN;
                level.playSound(null, boss.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1, 1.6F);
                break;
            }
        }
        sync();
    }

    private void launch(ServerLevel level, Salvo salvo) {
        List<Player> targets = new ArrayList<>();
        for (Player player : level.players()) if (enemy(player) && boss.hasLineOfSight(player)
                && player.distanceToSqr(salvo.center) <= QizaiArsenalRules.RANGE * QizaiArsenalRules.RANGE) targets.add(player);
        targets.sort(java.util.Comparator.comparingDouble(player -> player.distanceToSqr(salvo.center)));
        if (targets.isEmpty()) return;
        for (int index = 0; index < QizaiArsenalRules.MISSILES; index++) {
            Vec3 origin = salvo.crystals.get(index % salvo.crystals.size());
            double angle = index * Math.PI * 2 / QizaiArsenalRules.MISSILES;
            var star = new QizaiStarEntity(level, boss, origin);
            star.track(targets.get(index % targets.size()), QizaiArsenalRules.MISSILE_LIFE);
            star.setDeltaMovement(new Vec3(Math.cos(angle), 0.2, Math.sin(angle)).normalize().scale(QizaiArsenalRules.MISSILE_SPEED));
            level.addFreshEntity(star);
            missiles.add(star);
        }
        level.playSound(null, boss.blockPosition(), SoundEvents.SHULKER_SHOOT, SoundSource.HOSTILE, 1.1F, 1.2F);
    }

    private void laser(ServerLevel level, Salvo salvo, int index) {
        Vec3 origin = salvo.crystals.get(index), end = clip(level, origin, salvo.ends.get(index));
        salvo.ends.set(index, end);
        for (Player player : level.players()) {
            var box = player.getBoundingBox().inflate(QizaiArsenalRules.LASER_WIDTH);
            if (!enemy(player) || salvo.hit.contains(player.getUUID())
                    || !box.contains(origin) && box.clip(origin, end).isEmpty()) continue;
            salvo.hit.add(player.getUUID());
            player.hurt(level.damageSources().indirectMagic(boss, boss), QizaiArsenalRules.LASER_DAMAGE);
        }
        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 0.7F, 1.7F);
    }

    private Vec3 clip(ServerLevel level, Vec3 origin, Vec3 end) {
        return level.clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss)).getLocation();
    }

    private void repel(ServerLevel level) {
        pushStarted = level.getGameTime();
        pushFade = QizaiArsenalRules.PUSH_FADE;
        for (Player player : level.players()) {
            Vec3 offset = player.position().subtract(pushOrigin);
            if (!enemy(player) || !QizaiArsenalRules.insidePush(offset.horizontalDistanceSqr(), offset.y)
                    || level.clip(new ClipContext(pushOrigin.add(0, 1, 0), player.getEyePosition(),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss)).getType() != HitResult.Type.MISS) continue;
            Vec3 direction = offset.multiply(1, 0, 1).normalize();
            if (direction.lengthSqr() < 0.01) direction = new Vec3(1, 0, 0);
            BossKnockback.push(boss, player, direction.x * 5.2, 0.8, direction.z * 5.2);
            player.hurtMarked = true;
        }
        level.playSound(null, pushOrigin.x, pushOrigin.y, pushOrigin.z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.1F, 1.4F);
    }

    private void sync() {
        CompoundTag cue = new CompoundTag();
        if (boss.isQizaiProtectedCasting()) cue.putBoolean("shield", true);
        ListTag list = new ListTag();
        for (Salvo salvo : salvos) {
            CompoundTag cast = new CompoundTag();
            cast.putLong("start", salvo.start);
            ListTag crystals = new ListTag();
            for (int index = 0; index < salvo.crystals.size(); index++) {
                CompoundTag crystal = new CompoundTag();
                putPoint(crystal, "", salvo.crystals.get(index));
                putPoint(crystal, "end", salvo.ends.get(index));
                crystals.add(crystal);
            }
            cast.put("crystals", crystals);
            list.add(cast);
        }
        if (!list.isEmpty()) cue.put("salvos", list);
        if (pushTicks > 0 || pushFade > 0) {
            CompoundTag push = new CompoundTag();
            putPoint(push, "", pushOrigin);
            push.putLong("start", pushStarted);
            push.putBoolean("released", pushTicks == 0);
            cue.put("push", push);
        }
        if (!cue.equals(boss.getQizaiArsenalCue())) boss.setQizaiArsenalCue(cue);
    }

    private static void putPoint(CompoundTag tag, String prefix, Vec3 point) {
        tag.putDouble(prefix + "x", point.x);
        tag.putDouble(prefix + "y", point.y);
        tag.putDouble(prefix + "z", point.z);
    }

    public void cancel() {
        salvos.clear();
        missiles.forEach(QizaiStarEntity::discard);
        missiles.clear();
        pushTicks = 0;
        pushFade = 0;
        if (!boss.getQizaiArsenalCue().isEmpty()) boss.setQizaiArsenalCue(new CompoundTag());
    }
}
