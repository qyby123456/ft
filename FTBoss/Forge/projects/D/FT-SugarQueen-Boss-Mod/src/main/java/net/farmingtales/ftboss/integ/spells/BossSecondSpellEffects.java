package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.BossSpellRules.Kind;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BossSecondSpellEffects {
    private static final Map<ServerLevel, List<Pending>> PENDING = new WeakHashMap<>();

    private BossSecondSpellEffects() {}

    public static void cast(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, int spellLevel, Vec3 center, float power) {
        switch (spell.kind) {
            case FROST_LANCE -> lance(level, caster, spell, center, power);
            case TEA_SANCTUARY -> sanctuary(level, caster, power);
            case WINE_BOMB, TIDE_GEYSER -> {
                List<Pending> pending = PENDING.computeIfAbsent(level, ignored -> new ArrayList<>());
                pending.removeIf(entry -> entry.caster().equals(caster.getUUID()) && entry.kind() == spell.kind);
                if (pending.size() >= 64) pending.remove(0);
                pending.add(new Pending(caster.getUUID(), spell.kind, center, power, level.getGameTime()));
                BossSpellEffects.ring(level, spell.kind, center, spell.kind == Kind.WINE_BOMB ? 5 : 2.5, 24);
            }
            case DEFECT_PULSE -> discharge(level, caster, spell, power);
            case COMPOST_REAP -> harvest(level, caster, spell, center, power);
            case APPLE_AEGIS -> {
                int shield = BossSpellRules.shield(power);
                caster.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, BossSpellTuning.buffTicks(caster, MobEffects.ABSORPTION, 160), shield / 4 - 1), caster);
                caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BossSpellTuning.buffTicks(caster, MobEffects.DAMAGE_RESISTANCE, 80), 0), caster);
                for (int height = 0; height < 3; height++) {
                    BossSpellEffects.ring(level, spell.kind, caster.position().add(0, height * 0.65, 0), 1.4, 16);
                }
                level.sendParticles(ParticleTypes.ENCHANT, caster.getX(), caster.getY() + 1, caster.getZ(), 24, 0.8, 0.8, 0.8, 0.1);
            }
            default -> throw new IllegalArgumentException("Not a secondary spell: " + spell.kind);
        }
    }

    private static void lance(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Vec3 center, float damage) {
        Vec3 start = caster.getEyePosition();
        Vec3 direction = center.subtract(start).normalize();
        if (direction.lengthSqr() < 0.01) direction = caster.getLookAngle();
        Vec3 end = level.clip(new ClipContext(start, start.add(direction.scale(BossSpellRules.RANGE)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster)).getLocation();
        Vec3 segment = end.subtract(start);
        BossSpellEffects.beam(level, caster, spell.kind, start, end);
        int hits = 0;
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(0.8),
                target -> BossSpellEffects.canTarget(caster, target)).stream()
                .sorted(Comparator.comparingDouble(caster::distanceToSqr)).limit(BossSpellRules.MAX_TARGETS).toList();
        for (LivingEntity target : targets) {
            Vec3 body = target.getBoundingBox().getCenter();
            double progress = segment.lengthSqr() < 0.01 ? -1 : body.subtract(start).dot(segment) / segment.lengthSqr();
            if (progress < 0 || progress > 1 || body.distanceToSqr(start.add(segment.scale(progress))) > 0.64
                    || !BossSpellEffects.visible(level, caster, start, body)) continue;
            boolean frozen = target.getTicksFrozen() > 0;
            if (DamageSources.applyDamage(target, BossSpellRules.bonusDamage(damage, frozen), spell.getDamageSource(caster))) {
                if (frozen) target.setTicksFrozen(0);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BossSpellTuning.buffTicks(caster, MobEffects.MOVEMENT_SLOWDOWN, 40), 1), caster);
                level.sendParticles(ParticleTypes.SNOWFLAKE, body.x, body.y, body.z, 16, 0.3, 0.5, 0.3, 0.08);
            }
            if (++hits >= 4) break;
        }
    }

    public static boolean canHelp(LivingEntity caster, LivingEntity target) {
        if (!target.isAlive() || target.isRemoved() || target.isSpectator()) return false;
        if (target == caster || caster.isAlliedTo(target) || target.isAlliedTo(caster)) return true;
        if (target instanceof OwnableEntity pet && caster.getUUID().equals(pet.getOwnerUUID())) return true;
        return caster instanceof Mob && EntityType.getKey(caster.getType()).getNamespace().equals("ftboss")
                && EntityType.getKey(target.getType()).getNamespace().equals("ftboss");
    }

    private static void sanctuary(ServerLevel level, LivingEntity caster, float healing) {
        List<LivingEntity> allies = new ArrayList<>();
        allies.add(caster);
        allies.addAll(level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(4),
                target -> target != caster && canHelp(caster, target) && caster.distanceToSqr(target) <= 16
                        && BossSpellEffects.visible(level, caster, caster.getEyePosition(), target.getEyePosition()))
                .stream().sorted(Comparator.comparingDouble(caster::distanceToSqr)).limit(BossSpellRules.MAX_TARGETS - 1).toList());
        BossSpellEffects.ring(level, Kind.TEA_SANCTUARY, caster.position(), 4, 32);
        for (LivingEntity target : allies) {
            target.heal(healing);
            target.removeEffect(MobEffects.POISON);
            target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BossSpellTuning.buffTicks(caster, MobEffects.DAMAGE_RESISTANCE, 100), 0), caster);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 1, target.getZ(), 12, 0.4, 0.6, 0.4, 0);
            if (target != caster) BossSpellEffects.beam(level, caster, Kind.TEA_SANCTUARY, caster.getEyePosition(), target.getEyePosition());
        }
    }

    private static void discharge(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, float damage) {
        for (int radius = 1; radius <= 5; radius += 2) BossSpellEffects.ring(level, spell.kind, caster.position(), radius, 24);
        for (LivingEntity target : BossSpellEffects.targets(level, caster, caster.position(), 5)) {
            if (!DamageSources.applyDamage(target, damage, spell.getDamageSource(caster))) continue;
            Vec3 direction = target.position().subtract(caster.position()).multiply(1, 0, 1).normalize();
            net.farmingtales.ftboss.util.BossKnockback.knockback(target, 0.8, -direction.x, -direction.z);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BossSpellTuning.buffTicks(caster, MobEffects.MOVEMENT_SLOWDOWN, 50), 1), caster);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1, target.getZ(), 12, 0.4, 0.6, 0.4, 0.1);
        }
    }

    private static void harvest(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Vec3 center, float damage) {
        BossSpellEffects.ring(level, spell.kind, center, 4, 32);
        int harvested = 0;
        for (LivingEntity target : BossSpellEffects.targets(level, caster, center, 4)) {
            boolean poisoned = target.hasEffect(MobEffects.POISON);
            if (!DamageSources.applyDamage(target, BossSpellRules.bonusDamage(damage, poisoned), spell.getDamageSource(caster))) continue;
            if (poisoned) {
                target.removeEffect(MobEffects.POISON);
                harvested++;
                BossSpellEffects.beam(level, caster, spell.kind, target.getEyePosition(), caster.getEyePosition());
            }
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY() + 1, target.getZ(), 16, 0.5, 0.6, 0.5, 0);
        }
        if (harvested > 0) caster.heal(Math.min(4, harvested));
    }

    private static void detonate(ServerLevel level, LivingEntity caster, Pending entry) {
        BossSignatureSpell spell = BossIronSpells.spell(entry.kind());
        boolean wine = entry.kind() == Kind.WINE_BOMB;
        Vec3 center = entry.center();
        SpellVisuals.show(level, caster, entry.kind().spell, Phase.PULSE, center);
        BossSpellEffects.ring(level, entry.kind(), center, wine ? 5 : 2.5, 32);
        if (wine) {
            level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.6, center.z, 48, 1.5, 0.8, 1.5, 0.08);
            level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.8F, 1.25F);
        } else {
            for (int height = 0; height < 5; height++) {
                level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + height * 0.65, center.z, 12, 0.65, 0.2, 0.65, 0.08);
            }
            level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1, 0.7F);
        }
        for (LivingEntity target : BossSpellEffects.targets(level, caster, center, wine ? 5 : 2.5)) {
            float damage = entry.power() * (wine ? BossSpellRules.blastScale(target.position().distanceTo(center)) : 1);
            if (!DamageSources.applyDamage(target, damage, spell.getDamageSource(caster))) continue;
            if (wine) {
                target.setSecondsOnFire(3);
                Vec3 direction = target.position().subtract(center).multiply(1, 0, 1).normalize();
                net.farmingtales.ftboss.util.BossKnockback.knockback(target, 0.6, -direction.x, -direction.z);
            } else {
                net.farmingtales.ftboss.util.BossKnockback.push(target, 0, 0.65, 0);
                target.hurtMarked = true;
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BossSpellTuning.buffTicks(caster, MobEffects.MOVEMENT_SLOWDOWN, 60), 2), caster);
            }
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        List<Pending> pending = PENDING.get(level);
        if (pending == null) return;
        pending.removeIf(entry -> {
            long elapsed = level.getGameTime() - entry.started();
            if (elapsed < 0 || elapsed > 40 || !(level.getEntity(entry.caster()) instanceof LivingEntity caster)
                    || !caster.isAlive() || !net.farmingtales.ftboss.boss.ai.LemonSacrifice.allowsSpellEffect(caster)
                    || caster.distanceToSqr(entry.center()) > 48 * 48
                    || !level.hasChunkAt(BlockPos.containing(entry.center())) || !BossIronSpells.spell(entry.kind()).isEnabled()) return true;
            if (elapsed >= BossSpellRules.delay(entry.kind())) {
                detonate(level, caster, entry);
                return true;
            }
            if (elapsed % 4 == 0) BossSpellEffects.ring(level, entry.kind(), entry.center(),
                    (entry.kind() == Kind.WINE_BOMB ? 5 : 2.5) * (1 - elapsed / (double) BossSpellRules.delay(entry.kind())), 16);
            return false;
        });
        if (pending.isEmpty()) PENDING.remove(level);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { PENDING.remove(event.getLevel()); }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) { PENDING.clear(); }

    private record Pending(UUID caster, Kind kind, Vec3 center, float power, long started) {}
}
