package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayDeque;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.farmingtales.ftboss.util.XuanlanSkyRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class XuanlanSkyStrike {
    private final EasterBossEntity boss;
    private int uses;
    private int age;
    private int strikes;
    private int warnings;
    private int recovery;
    private boolean active;
    private boolean oldInvisible;
    private boolean oldNoGravity;
    private boolean oldNoPhysics;
    private Vec3 home = Vec3.ZERO;
    private Vec3 landing = Vec3.ZERO;
    private record PendingStrike(Vec3 locked, Vec3 beamTop, int impactAge) {}
    private final ArrayDeque<PendingStrike> pendingStrikes = new ArrayDeque<>();
    private Player target;

    public XuanlanSkyStrike(EasterBossEntity boss) { this.boss = boss; }

    public boolean active() { return active; }

    public boolean tick(ServerLevel level) {
        if (!active) {
            if (recovery > 0) { recovery--; return false; }
            if (boss.getVariant() != 6 || boss.isMini() || boss.isGiant() || boss.isNoAi()
                    || boss.isInvulnerable() || boss.isPerformingSkill() || ManorFight.isFinaleMember(boss)
                    || HiddenBossIntro.isActing(boss.getUUID())
                    || !XuanlanSkyRules.ready(uses, boss.getHealth(), boss.getMaxHealth())
                    || !(boss.getTarget() instanceof Player player) || !valid(player, boss.position())) return false;
            Vec3 safeHome = ManorSafeTeleport.find(level, boss, boss.position(), 4);
            if (safeHome == null) return false;
            home = safeHome;
            landing = home;
            target = player;
            oldInvisible = boss.isInvisible();
            oldNoGravity = boss.isNoGravity();
            oldNoPhysics = boss.noPhysics;
            active = true;
            uses++;
            age = 0;
            strikes = 0;
            warnings = 0;
            pendingStrikes.clear();
            boss.stopRiding();
            boss.setNoGravity(true);
            boss.noPhysics = true;
            send(level, XuanlanFxPacket.SKY_ASCEND, home, home.add(0, XuanlanSkyRules.HEIGHT, 0));
            level.playSound(null, home.x, home.y, home.z, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.8F, 1.5F);
            for (var viewer : level.players()) {
                if (viewer.distanceToSqr(home) <= XuanlanSkyRules.RANGE * XuanlanSkyRules.RANGE) {
                    viewer.displayClientMessage(Component.literal("§b§l玄蓝 · 天穹锁击 §f" + XuanlanSkyRules.STRIKES
                            + "连击！§e离开脚下预警圈！"), true);
                }
            }
            FTBossMod.LOGGER.info("[FTBoss][XuanlanSky] {} begin use={}/{}", boss.getUUID(), uses, XuanlanSkyRules.MAX_USES);
        }
        if (!boss.isAlive() || boss.isNoAi() || ManorFight.isFinaleMember(boss)) {
            cancel();
            return true;
        }
        boss.getNavigation().stop();
        boss.setDeltaMovement(Vec3.ZERO);
        boss.fallDistance = 0;
        if (age < XuanlanSkyRules.ASCEND_TICKS) {
            move(home.add(0, XuanlanSkyRules.HEIGHT * XuanlanSkyRules.rise((age + 1.0) / XuanlanSkyRules.ASCEND_TICKS), 0));
            level.sendParticles(ParticleTypes.END_ROD, boss.getX(), boss.getY() + 0.8, boss.getZ(), 5, 0.35, 0.5, 0.35, 0.04);
        } else {
            int attackAge = age - XuanlanSkyRules.ASCEND_TICKS;
            int attackTicks = XuanlanSkyRules.attackTicks();
            if (attackAge < attackTicks) {
                boss.setXuanlanSkyHidden(true);
                boss.setInvisible(true);
                move(home.add(0, XuanlanSkyRules.HEIGHT, 0));
                if (warnings < XuanlanSkyRules.STRIKES && attackAge == XuanlanSkyRules.warningTick(warnings)) {
                    if (!valid(target, home)) target = level.players().stream().filter(player -> valid(player, home))
                            .min(java.util.Comparator.comparingDouble(player -> player.distanceToSqr(home))).orElse(null);
                    if (target == null) { cancel(); return true; }
                    Vec3 feet = target.position();
                    Vec3 locked = level.clip(new ClipContext(feet.add(0, 0.5, 0), feet.add(0, -12, 0),
                            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss)).getLocation().add(0, 0.04, 0);
                    Vec3 beamTop = locked.add(0, Math.max(XuanlanSkyRules.HEIGHT, feet.y - locked.y + 8), 0);
                    pendingStrikes.addLast(new PendingStrike(locked, beamTop, attackAge + XuanlanSkyRules.WARNING_TICKS));
                    queuePrediction(level, target, attackAge + XuanlanSkyRules.WARNING_TICKS);
                    warnings++;
                    send(level, XuanlanFxPacket.SKY_WARNING, locked, beamTop);
                    level.playSound(null, locked.x, locked.y, locked.z, SoundEvents.NOTE_BLOCK_CHIME.value(),
                            SoundSource.HOSTILE, 1.2F, 1.6F);
                }
                while (!pendingStrikes.isEmpty() && pendingStrikes.peekFirst().impactAge() <= attackAge) {
                    strike(level, pendingStrikes.removeFirst());
                    strikes++;
                }
            } else {
                int descentAge = attackAge - attackTicks;
                if (descentAge == 0) {
                    Vec3 safe = ManorSafeTeleport.find(level, boss, home, 8);
                    if (safe != null) landing = safe;
                    boss.setXuanlanSkyHidden(false);
                    boss.setInvisible(oldInvisible);
                    send(level, XuanlanFxPacket.SKY_DESCEND, landing, landing.add(0, XuanlanSkyRules.HEIGHT, 0));
                }
                move(landing.add(0, XuanlanSkyRules.HEIGHT * (1 - XuanlanSkyRules.rise(
                        (descentAge + 1.0) / XuanlanSkyRules.DESCEND_TICKS)), 0));
                if (descentAge + 1 >= XuanlanSkyRules.DESCEND_TICKS) { cancel(); return true; }
            }
        }
        age++;
        return true;
    }

    private boolean valid(Player player, Vec3 center) {
        return player != null && player.level() == boss.level() && player.isAlive() && !player.isRemoved()
                && !player.isCreative() && !player.isSpectator() && !boss.isAlliedTo(player)
                && player.distanceToSqr(center) <= XuanlanSkyRules.RANGE * XuanlanSkyRules.RANGE;
    }

    private void queuePrediction(ServerLevel level, net.minecraft.world.entity.LivingEntity victim, int impactAge) {
        Vec3 point = ManorPredictiveAttacks.skyPrediction(level, boss, victim, home);
        if (point == null) return;
        Vec3 top = point.add(0, Math.max(24, victim.getY() - point.y + 8), 0);
        pendingStrikes.addLast(new PendingStrike(point, top, impactAge));
        send(level, XuanlanFxPacket.SKY_WARNING, point, top);
    }

    private void move(Vec3 position) {
        boss.teleportTo(position.x, position.y, position.z);
        boss.setDeltaMovement(Vec3.ZERO);
        boss.fallDistance = 0;
    }

    private void strike(ServerLevel level, PendingStrike pending) {
        Vec3 locked = pending.locked();
        Vec3 beamTop = pending.beamTop();
        send(level, XuanlanFxPacket.SKY_IMPACT, locked, beamTop);
        level.playSound(null, locked.x, locked.y, locked.z, SoundEvents.GUARDIAN_ATTACK, SoundSource.HOSTILE, 1.5F, 1.4F);
        level.sendParticles(ParticleTypes.END_ROD, locked.x, locked.y + 0.3, locked.z, 24, 1.4, 0.3, 1.4, 0.16);
        double radius = XuanlanSkyRules.RADIUS;
        AABB column = new AABB(locked.x - radius, locked.y - 0.1, locked.z - radius,
                locked.x + radius, beamTop.y, locked.z + radius);
        for (Player player : level.getEntitiesOfClass(Player.class, column, player -> valid(player, home))) {
            AABB body = player.getBoundingBox();
            double nearestX = Math.max(body.minX, Math.min(locked.x, body.maxX));
            double nearestZ = Math.max(body.minZ, Math.min(locked.z, body.maxZ));
            if ((nearestX - locked.x) * (nearestX - locked.x) + (nearestZ - locked.z) * (nearestZ - locked.z) > radius * radius) continue;
            int previousInvulnerability = player.invulnerableTime;
            player.invulnerableTime = 0;
            try {
                player.hurt(level.damageSources().indirectMagic(boss, boss), boss.manorSkillDamage(player, XuanlanSkyRules.DAMAGE, 2.0));
            } finally {
                player.invulnerableTime = Math.max(previousInvulnerability, player.invulnerableTime);
            }
        }
    }

    private void send(ServerLevel level, int kind, Vec3 from, Vec3 to) {
        XuanlanFxPacket.send(level, kind, boss.getId(), from, to);
    }

    public void cancel() {
        if (!active) return;
        if (boss.level() instanceof ServerLevel level) {
            send(level, XuanlanFxPacket.SKY_CANCEL, home, boss.position());
            Vec3 safe = ManorSafeTeleport.find(level, boss, landing, 8);
            move(safe == null ? home : safe);
            level.playSound(null, boss.getX(), boss.getY(), boss.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 1.2F, 1.4F);
        }
        boss.setXuanlanSkyHidden(false);
        boss.setInvisible(oldInvisible);
        boss.setNoGravity(oldNoGravity);
        boss.noPhysics = oldNoPhysics;
        active = false;
        pendingStrikes.clear();
        target = null;
        recovery = XuanlanSkyRules.RECOVERY_TICKS;
        FTBossMod.LOGGER.info("[FTBoss][XuanlanSky] {} end strikes={} uses={}", boss.getUUID(), strikes, uses);
    }

    public void save(CompoundTag tag) {
        CompoundTag state = new CompoundTag();
        state.putInt("Uses", uses);
        state.putInt("Recovery", recovery);
        state.putBoolean("Active", active);
        state.putBoolean("Invisible", oldInvisible);
        state.putBoolean("NoGravity", oldNoGravity);
        state.putBoolean("NoPhysics", oldNoPhysics);
        state.putDouble("HomeX", home.x);
        state.putDouble("HomeY", home.y);
        state.putDouble("HomeZ", home.z);
        tag.put("XuanlanSkyStrike", state);
    }

    public void load(CompoundTag tag) {
        CompoundTag state = tag.getCompound("XuanlanSkyStrike");
        uses = Math.max(0, Math.min(XuanlanSkyRules.MAX_USES, state.getInt("Uses")));
        recovery = Math.max(0, Math.min(XuanlanSkyRules.RECOVERY_TICKS, state.getInt("Recovery")));
        if (!state.getBoolean("Active")) return;
        home = new Vec3(state.getDouble("HomeX"), state.getDouble("HomeY"), state.getDouble("HomeZ"));
        if (!Double.isFinite(home.x) || !Double.isFinite(home.y) || !Double.isFinite(home.z)) home = boss.position();
        landing = home;
        oldInvisible = state.getBoolean("Invisible");
        oldNoGravity = state.getBoolean("NoGravity");
        oldNoPhysics = state.getBoolean("NoPhysics");
        active = true;
        cancel();
    }
}
