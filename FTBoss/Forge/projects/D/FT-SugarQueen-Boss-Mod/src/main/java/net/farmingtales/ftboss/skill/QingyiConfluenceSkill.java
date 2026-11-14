package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.weapons.QingyiConfluenceTridentItem;
import net.farmingtales.ftboss.util.ManorWeaponBalanceRules;
import net.farmingtales.ftboss.util.QingyiConfluenceRules;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class QingyiConfluenceSkill {
    private static final DustParticleOptions[] ELEMENTS = {
            new DustParticleOptions(new Vector3f(0.20F, 0.95F, 1.0F), 1.2F),
            new DustParticleOptions(new Vector3f(0.95F, 0.22F, 0.48F), 1.2F),
            new DustParticleOptions(new Vector3f(0.40F, 0.34F, 1.0F), 1.2F),
            new DustParticleOptions(new Vector3f(1.0F, 0.85F, 0.36F), 1.2F)
    };
    private final EasterBossEntity boss;
    private int elapsed = -1;
    private LivingEntity target;
    private ItemStack weapon = ItemStack.EMPTY;
    private Vec3 center = Vec3.ZERO;

    public QingyiConfluenceSkill(EasterBossEntity boss) {
        this.boss = boss;
    }

    public boolean busy() {
        return elapsed >= 0;
    }

    public void start(ServerLevel level, LivingEntity victim) {
        if (busy() || boss.isMini() || boss.isNoAi() || !boss.isAlive() || boss.getVariant() != 5
                || !canTarget(boss, victim) || !boss.hasLineOfSight(victim)
                || boss.distanceToSqr(victim) > QingyiConfluenceRules.RANGE * QingyiConfluenceRules.RANGE
                || !(boss.getMainHandItem().getItem() instanceof QingyiConfluenceTridentItem)) return;
        target = victim;
        weapon = boss.getMainHandItem();
        Vec3 offset = victim.position().add(0, 0.15, 0).subtract(boss.getEyePosition());
        center = clipped(level, boss, boss.getEyePosition(), boss.getEyePosition().add(
                offset.normalize().scale(Math.min(QingyiConfluenceRules.RANGE, offset.length()))));
        elapsed = QingyiConfluenceRules.CHARGE_TICKS
                - boss.getChallengeDifficulty().acceleratedTicks(QingyiConfluenceRules.CHARGE_TICKS);
        boss.beginManorFiveCast(QingyiConfluenceRules.useTicks() - elapsed, QingyiConfluenceRules.useTicks() - elapsed,
                center, QingyiConfluenceRules.RADIUS, true);
        QingyiConfluenceTridentItem.animate(boss, "charge");
        level.playSound(null, boss.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.1F, 0.7F);
        FTBossMod.LOGGER.info("[FTBoss][Qingyi] 四象归墟 charge -> {}", victim.getName().getString());
    }

    public void tick(ServerLevel level) {
        if (!busy()) return;
        if (!boss.isAlive() || boss.isNoAi() || boss.getVariant() != 5 || boss.getMainHandItem() != weapon
                || target == null || target.level() != level || !canTarget(boss, target)
                || boss.distanceToSqr(target) > 48 * 48) {
            cancel();
            return;
        }
        elapsed++;
        boss.getLookControl().setLookAt(center.x, center.y, center.z, 60, 60);
        if (elapsed < QingyiConfluenceRules.useTicks() && elapsed % 4 == 0) {
            warning(level, center, elapsed);
            chargeFx(level, boss, elapsed);
        }
        if (elapsed == QingyiConfluenceRules.CHARGE_TICKS) QingyiConfluenceTridentItem.animate(boss, "release");
        if (QingyiConfluenceRules.impactAt(elapsed)) {
            boss.finishManorFiveCast(center);
            burst(level, boss, center);
        }
        if (QingyiConfluenceRules.finished(elapsed)) {
            elapsed = -1;
            target = null;
            weapon = ItemStack.EMPTY;
        }
    }

    public void cancel() {
        if (!busy()) return;
        QingyiConfluenceTridentItem.cancel(boss, weapon);
        elapsed = -1;
        target = null;
        weapon = ItemStack.EMPTY;
        boss.cancelManorFiveCast();
    }

    public static boolean canTarget(LivingEntity source, LivingEntity victim) {
        if (victim == source || !victim.isAlive() || victim.isRemoved() || victim.isSpectator()
                || victim instanceof ArmorStand || source.isAlliedTo(victim) || victim.isAlliedTo(source)) return false;
        if (source instanceof EasterBossEntity && !(victim instanceof Player)) return false;
        if (victim instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) return false;
            if (source instanceof Player attacker && (!attacker.canHarmPlayer(player)
                    || source.getServer() == null || !source.getServer().isPvpAllowed())) return false;
        }
        if (victim instanceof OwnableEntity pet) {
            LivingEntity owner = pet.getOwner();
            if (source.getUUID().equals(pet.getOwnerUUID()) || owner != null && source.isAlliedTo(owner)) return false;
        }
        return true;
    }

    private static Vec3 clipped(ServerLevel level, LivingEntity source, Vec3 start, Vec3 end) {
        HitResult result = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source));
        return result.getType() == HitResult.Type.MISS ? end
                : result.getLocation().subtract(end.subtract(start).normalize().scale(0.15));
    }

    public static Vec3 aim(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        Vec3 start = player.getEyePosition();
        Vec3 end = clipped(level, player, start, start.add(player.getLookAngle().scale(QingyiConfluenceRules.RANGE)));
        double nearest = start.distanceToSqr(end);
        Vec3 selected = end;
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(0.5),
                candidate -> canTarget(player, candidate))) {
            var intersection = victim.getBoundingBox().inflate(0.3).clip(start, end);
            if (intersection.isPresent() && start.distanceToSqr(intersection.get()) < nearest) {
                nearest = start.distanceToSqr(intersection.get());
                selected = intersection.get();
            }
        }
        return selected;
    }

    public static void mark(LivingEntity target, int ticks) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 1));
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, ticks, 0));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, ticks, 0));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0));
    }

    public static void burst(ServerLevel level, LivingEntity source, Vec3 center) {
        AABB area = new AABB(center, center).inflate(QingyiConfluenceRules.RADIUS, QingyiConfluenceRules.HALF_HEIGHT,
                QingyiConfluenceRules.RADIUS);
        int hits = 0;
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area, candidate -> canTarget(source, candidate))) {
            Vec3 offset = victim.position().subtract(center);
            if (!QingyiConfluenceRules.inside(offset.x, offset.y, offset.z) || !source.hasLineOfSight(victim)) continue;
            if (level.clip(new ClipContext(center, victim.getBoundingBox().getCenter(), ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, source)).getType() != HitResult.Type.MISS) continue;
            float damage = 144.0F;
            if (source instanceof EasterBossEntity boss) {
                float base = 54.0F + boss.qingyiDamageBonus();
                damage = boss.manorSkillDamage(victim, base, 2.0);
            }
            if (!victim.hurt(level.damageSources().indirectMagic(source, source), damage)) continue;
            mark(victim, QingyiConfluenceRules.DEBUFF_TICKS);
            Vec3 pull = center.subtract(victim.position());
            if (pull.horizontalDistanceSqr() > 0.01) {
                Vec3 direction = new Vec3(pull.x, 0, pull.z).normalize().scale(0.45);
                victim.push(direction.x, 0.12, direction.z);
                victim.hurtMarked = true;
            }
            if (++hits >= 32) break;
        }
        for (int segment = 0; segment < 64; segment++) {
            double angle = segment * Math.PI / 32;
            double radius = QingyiConfluenceRules.RADIUS;
            level.sendParticles(ELEMENTS[segment / 16], center.x + Math.cos(angle) * radius, center.y + 0.15,
                    center.z + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
        }
        Vec3 start = source.getEyePosition();
        for (int point = 0; point <= 32; point++) {
            Vec3 position = start.lerp(center, point / 32.0);
            level.sendParticles(ELEMENTS[point % 4], position.x, position.y, position.z, 1, 0.035, 0.035, 0.035, 0);
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y + 1, center.z, 28, 0.8, 1.3, 0.8, 0.12);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.5, center.z, 16, 0.5, 0.8, 0.5, 0.08);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.2F, 1.25F);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 0.7F, 1.4F);
    }

    public static void chargeFx(ServerLevel level, LivingEntity source, int elapsed) {
        for (int element = 0; element < 4; element++) {
            double angle = elapsed * 0.18 + element * Math.PI / 2;
            double radius = 0.7 * (1 - Math.min(1, elapsed / (double) QingyiConfluenceRules.CHARGE_TICKS)) + 0.12;
            Vec3 point = source.getEyePosition().add(source.getLookAngle().scale(0.6))
                    .add(Math.cos(angle) * radius, Math.sin(angle) * radius, 0);
            level.sendParticles(ELEMENTS[element], point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0);
        }
    }

    private static void warning(ServerLevel level, Vec3 center, int elapsed) {
        for (int point = 0; point < 24; point++) {
            double angle = point * Math.PI / 12 + elapsed * 0.015;
            level.sendParticles(ELEMENTS[point / 6], center.x + Math.cos(angle) * QingyiConfluenceRules.RADIUS,
                    center.y + 0.08, center.z + Math.sin(angle) * QingyiConfluenceRules.RADIUS, 1, 0, 0, 0, 0);
        }
    }
}
