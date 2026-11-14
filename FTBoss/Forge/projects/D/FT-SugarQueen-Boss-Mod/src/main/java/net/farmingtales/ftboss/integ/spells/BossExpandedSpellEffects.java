package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.BossSpellRules.Kind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BossExpandedSpellEffects {
    private static final int MAX_FIELDS = 64;
    private static final Map<ServerLevel, List<Field>> FIELDS = new WeakHashMap<>();

    private BossExpandedSpellEffects() {}

    public static void cast(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Vec3 center, float power) {
        if (!BossSpellRules.expanded(spell.kind) || !spell.isEnabled() || !Float.isFinite(power) || power <= 0
                || !BossSpellEffects.visible(level, caster, caster.getEyePosition(), center.add(0, 0.1, 0))) return;
        Vec3 direction = center.subtract(caster.position()).multiply(1, 0, 1).normalize();
        if (direction.lengthSqr() < 0.01) direction = caster.getLookAngle().multiply(1, 0, 1).normalize();
        UUID target = null;
        if (spell.kind == Kind.TEA_SUTURE) {
            LivingEntity victim = BossSpellEffects.targets(level, caster, center, 3).stream()
                    .filter(candidate -> caster.distanceToSqr(candidate) <= 18 * 18 && caster.hasLineOfSight(candidate))
                    .findFirst().orElse(null);
            if (victim == null) return;
            target = victim.getUUID();
        }
        Field field = new Field(caster.getUUID(), spell.kind, caster.position(), center, direction, target, power, level.getGameTime());
        List<Field> fields = FIELDS.computeIfAbsent(level, ignored -> new ArrayList<>());
        fields.removeIf(previous -> previous.caster.equals(field.caster) && previous.kind == field.kind);
        warning(level, field);
        if (BossSpellRules.firstPulse(field.kind) == 0) {
            pulse(level, caster, field, 0);
            field.lastPulse = 0;
        }
        if (BossSpellRules.lastPulse(field.kind) > 0) {
            if (fields.size() >= MAX_FIELDS) fields.remove(0);
            fields.add(field);
        }
        if (fields.isEmpty()) FIELDS.remove(level);
    }

    private static Vec3 point(Field field, int pulse) {
        return switch (field.kind) {
            case TEA_THORNS -> field.origin.add(field.direction.scale(4 + pulse * 4));
            case TIDE_SURGE -> field.origin.add(field.direction.scale(5 + pulse * 5));
            case WINE_FUSE -> field.center.add(Math.cos(pulse * Math.PI * 2 / 3) * 2.5, 0, Math.sin(pulse * Math.PI * 2 / 3) * 2.5);
            default -> field.center;
        };
    }

    private static void warning(ServerLevel level, Field field) {
        int points = field.kind == Kind.WINE_FUSE || field.kind == Kind.TEA_THORNS || field.kind == Kind.TIDE_SURGE ? 3 : 1;
        for (int index = 0; index < points; index++) {
            BossSpellEffects.ring(level, field.kind, point(field, index), points == 1 ? 3.5 : 2, 12);
        }
    }

    private static void effect(LivingEntity target, LivingEntity caster, MobEffect effect, int duration, int amplifier) {
        target.addEffect(new MobEffectInstance(effect, BossSpellTuning.buffTicks(caster, effect, duration), amplifier), caster);
    }

    private static void shield(LivingEntity caster, float power, int duration) {
        int amplifier = BossSpellRules.shield(power) / 4 - 1;
        MobEffectInstance previous = caster.getEffect(MobEffects.ABSORPTION);
        if (previous != null) {
            amplifier = Math.max(amplifier, previous.getAmplifier());
            duration = Math.max(duration, previous.getDuration());
        }
        effect(caster, caster, MobEffects.ABSORPTION, duration, amplifier);
    }

    private static List<LivingEntity> allies(ServerLevel level, LivingEntity caster, double radius) {
        List<LivingEntity> allies = new ArrayList<>();
        allies.add(caster);
        allies.addAll(level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(radius),
                target -> target != caster && BossSecondSpellEffects.canHelp(caster, target)
                        && caster.distanceToSqr(target) <= radius * radius && caster.hasLineOfSight(target)).stream()
                .sorted(Comparator.comparingDouble(caster::distanceToSqr)).limit(BossSpellRules.MAX_TARGETS - 1).toList());
        return allies;
    }

    private static void pulse(ServerLevel level, LivingEntity caster, Field field, int pulse) {
        BossSignatureSpell spell = BossIronSpells.spell(field.kind);
        Vec3 center = point(field, pulse);
        if (BossSpellRules.selfCentered(field.kind)) center = caster.position();
        if (!level.hasChunkAt(BlockPos.containing(center))) return;
        if (field.kind == Kind.TEA_THORNS || field.kind == Kind.TIDE_SURGE) {
            if (!BossSpellEffects.visible(level, caster, field.origin.add(0, 0.5, 0), center.add(0, 0.5, 0))) return;
        } else if (field.kind == Kind.WINE_FUSE
                && !BossSpellEffects.visible(level, caster, field.center.add(0, 0.3, 0), center.add(0, 0.3, 0))) return;
        visuals(level, caster, field.kind, center, pulse);
        switch (field.kind) {
            case FROST_PRISM -> {
                shield(caster, field.power, 160);
                effect(caster, caster, MobEffects.DAMAGE_RESISTANCE, 80, 0);
                for (LivingEntity target : BossSpellEffects.targets(level, caster, center, 3)) {
                    effect(target, caster, MobEffects.MOVEMENT_SLOWDOWN, 60, 2);
                    target.setTicksFrozen(Math.max(100, target.getTicksFrozen()));
                }
            }
            case TEA_RENEWAL -> {
                for (LivingEntity ally : allies(level, caster, 5)) {
                    ally.heal(field.power);
                    ally.removeEffect(MobEffects.POISON);
                    BossSpellEffects.beam(level, caster, field.kind, caster.getEyePosition(), ally.getEyePosition());
                }
            }
            case WINE_TOAST -> {
                shield(caster, field.power, 160);
                effect(caster, caster, MobEffects.DAMAGE_BOOST, 120, 0);
                effect(caster, caster, MobEffects.MOVEMENT_SPEED, 120, 0);
            }
            case DEFECT_REBOOT -> {
                caster.heal(field.power);
                caster.removeEffect(MobEffects.WEAKNESS);
                caster.removeEffect(MobEffects.DIG_SLOWDOWN);
                effect(caster, caster, MobEffects.DAMAGE_RESISTANCE, 100, 0);
                effect(caster, caster, MobEffects.MOVEMENT_SLOWDOWN, 30, 1);
            }
            case COMPOST_MULCH -> {
                for (LivingEntity ally : allies(level, caster, 4)) {
                    if (ally.hasEffect(MobEffects.POISON) || ally.hasEffect(MobEffects.WITHER)) shield(ally, 8, 140);
                    ally.removeEffect(MobEffects.POISON);
                    ally.removeEffect(MobEffects.WITHER);
                    ally.heal(field.power);
                }
            }
            case TIDE_BUBBLE -> {
                shield(caster, field.power, 180);
                caster.clearFire();
                effect(caster, caster, MobEffects.WATER_BREATHING, 200, 0);
                effect(caster, caster, MobEffects.DAMAGE_RESISTANCE, 100, 0);
                for (LivingEntity target : BossSpellEffects.targets(level, caster, center, 4)) push(target, center, 0.5, 0.15);
            }
            case TEA_SUTURE -> {
                if (level.getEntity(field.target) instanceof LivingEntity target && BossSpellEffects.canTarget(caster, target)
                        && caster.distanceToSqr(target) <= 18 * 18 && caster.hasLineOfSight(target)) {
                    float before = target.getHealth();
                    BossSpellEffects.beam(level, caster, field.kind, caster.getEyePosition(), target.getBoundingBox().getCenter());
                    if (DamageSources.applyDamage(target, field.power, spell.getDamageSource(caster))) {
                        caster.heal(Math.min(4, Math.max(0, before - target.getHealth()) * 0.3F));
                        effect(target, caster, MobEffects.WEAKNESS, 45, 0);
                    }
                }
            }
            case DEFECT_RAIL -> rail(level, caster, spell, field);
            case WINE_BREATH -> breath(level, caster, spell, field);
            default -> area(level, caster, spell, field, center, pulse);
        }
    }

    private static void push(LivingEntity target, Vec3 center, double horizontal, double upward) {
        Vec3 direction = target.position().subtract(center).multiply(1, 0, 1).normalize();
        net.farmingtales.ftboss.util.BossKnockback.push(target, direction.x * horizontal, upward, direction.z * horizontal);
        target.hurtMarked = true;
    }

    private static void area(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Field field, Vec3 center, int pulse) {
        double radius = switch (field.kind) {
            case TEA_THORNS, WINE_FUSE -> 2.2;
            case TIDE_SURGE -> 3;
            case COMPOST_ROT -> 2 + pulse * 2;
            case APPLE_ORBIT -> 3.5;
            default -> 4;
        };
        BossSpellEffects.ring(level, field.kind, center, radius, 24);
        float stolen = 0;
        int harvested = 0;
        for (LivingEntity target : BossSpellEffects.targets(level, caster, center, radius)) {
            boolean once = field.kind == Kind.TEA_THORNS || field.kind == Kind.COMPOST_ROT || field.kind == Kind.TIDE_SURGE;
            if (once && (field.hits.size() >= BossSpellRules.MAX_TARGETS || !field.hits.add(target.getUUID()))) continue;
            boolean marked = switch (field.kind) {
                case FROST_SHATTER -> target.getTicksFrozen() > 0;
                case COMPOST_ROT -> target.hasEffect(MobEffects.POISON);
                case APPLE_SEED, APPLE_HARVEST -> target.hasEffect(MobEffects.GLOWING);
                default -> false;
            };
            float before = target.getHealth();
            if (!DamageSources.applyDamage(target, BossSpellRules.bonusDamage(field.power, marked), spell.getDamageSource(caster))) continue;
            switch (field.kind) {
                case FROST_HAIL -> {
                    target.setTicksFrozen(Math.max(100, target.getTicksFrozen()));
                    effect(target, caster, MobEffects.MOVEMENT_SLOWDOWN, 50, 1);
                }
                case FROST_SHATTER -> {
                    target.setTicksFrozen(0);
                    push(target, center, 0.35, 0.15);
                }
                case TEA_THORNS -> effect(target, caster, MobEffects.MOVEMENT_SLOWDOWN, 45, 3);
                case TEA_POLLEN -> {
                    effect(target, caster, MobEffects.POISON, 60, 0);
                    effect(target, caster, MobEffects.WEAKNESS, 60, 0);
                }
                case TEA_FLESH -> {
                    stolen += Math.max(0, before - target.getHealth()) * 0.2F;
                    effect(target, caster, MobEffects.MOVEMENT_SLOWDOWN, 60, 2);
                    BossSpellEffects.beam(level, caster, field.kind, target.getEyePosition(), caster.getEyePosition());
                }
                case WINE_FUSE -> {
                    target.setSecondsOnFire(3);
                    push(target, center, 0.35, 0.2);
                }
                case DEFECT_STORM -> {
                    effect(target, caster, MobEffects.MOVEMENT_SLOWDOWN, 35, 1);
                    effect(target, caster, MobEffects.DIG_SLOWDOWN, 60, 1);
                    BossSpellEffects.beam(level, caster, field.kind, center.add(-2, 2, -2), target.getEyePosition());
                }
                case COMPOST_ERUPTION -> {
                    net.farmingtales.ftboss.util.BossKnockback.push(target, 0, 0.5, 0);
                    target.hurtMarked = true;
                    effect(target, caster, MobEffects.POISON, 100, 0);
                }
                case COMPOST_ROT -> {
                    if (marked) target.removeEffect(MobEffects.POISON);
                    effect(target, caster, MobEffects.WITHER, 60, 0);
                }
                case APPLE_ORBIT -> {
                    effect(target, caster, MobEffects.GLOWING, 100, 0);
                    push(target, center, 0.15, 0);
                }
                case APPLE_SEED -> {
                    if (marked) target.removeEffect(MobEffects.GLOWING);
                    effect(target, caster, MobEffects.WEAKNESS, 80, 0);
                }
                case APPLE_HARVEST -> {
                    if (marked) { target.removeEffect(MobEffects.GLOWING); harvested++; }
                    BossSpellEffects.beam(level, caster, field.kind, target.getEyePosition(), caster.getEyePosition());
                }
                case TIDE_RIP -> {
                    push(target, center, -0.28, 0.05);
                    effect(target, caster, MobEffects.WEAKNESS, 50, 0);
                }
                case TIDE_SURGE -> {
                    net.farmingtales.ftboss.util.BossKnockback.push(target, field.direction.x * 0.65, 0.2, field.direction.z * 0.65);
                    target.hurtMarked = true;
                    target.clearFire();
                    effect(target, caster, MobEffects.MOVEMENT_SLOWDOWN, 50, 1);
                }
                default -> throw new IllegalArgumentException("Not an area spell: " + field.kind);
            }
        }
        if (stolen > 0) caster.heal(Math.min(6, stolen));
        if (harvested > 0) shield(caster, Math.min(12, harvested * 4), 120);
    }

    private static void rail(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Field field) {
        Vec3 start = caster.getEyePosition();
        Vec3 direction = field.center.subtract(start).normalize();
        if (direction.lengthSqr() < 0.01) direction = caster.getLookAngle();
        Vec3 end = level.clip(new ClipContext(start, start.add(direction.scale(24)), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, caster)).getLocation();
        Vec3 segment = end.subtract(start);
        BossSpellEffects.beam(level, caster, field.kind, start, end);
        int hits = 0;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(0.9),
                candidate -> BossSpellEffects.canTarget(caster, candidate)).stream()
                .sorted(Comparator.comparingDouble(caster::distanceToSqr)).limit(BossSpellRules.MAX_TARGETS).toList()) {
            Vec3 body = target.getBoundingBox().getCenter();
            double progress = segment.lengthSqr() < 0.01 ? -1 : body.subtract(start).dot(segment) / segment.lengthSqr();
            if (progress < 0 || progress > 1 || body.distanceToSqr(start.add(segment.scale(progress))) > 0.81
                    || !BossSpellEffects.visible(level, caster, start, body)) continue;
            if (DamageSources.applyDamage(target, field.power * (1 - hits * 0.15F), spell.getDamageSource(caster))) {
                effect(target, caster, MobEffects.GLOWING, 80, 0);
            }
            if (++hits >= 4) break;
        }
    }

    private static void breath(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Field field) {
        for (int distance = 2; distance <= 10; distance += 2) {
            Vec3 middle = field.origin.add(field.direction.scale(distance)).add(0, 0.8, 0);
            Vec3 side = new Vec3(-field.direction.z, 0, field.direction.x).scale(distance * 0.35);
            BossSpellEffects.beam(level, caster, field.kind, middle.subtract(side), middle.add(side));
        }
        for (LivingEntity target : BossSpellEffects.targets(level, caster, field.origin, 12)) {
            Vec3 offset = target.position().subtract(field.origin);
            double forward = offset.dot(field.direction);
            double sideways = offset.x * -field.direction.z + offset.z * field.direction.x;
            if (forward < 0 || forward > 10 || Math.abs(sideways) > 0.8 + forward * 0.35 || Math.abs(offset.y) > 3) continue;
            if (DamageSources.applyDamage(target, field.power, spell.getDamageSource(caster))) target.setSecondsOnFire(3);
        }
    }

    private static void visuals(ServerLevel level, LivingEntity caster, Kind kind, Vec3 center, int pulse) {
        net.farmingtales.ftboss.skill.SpellVisuals.show(level, caster, kind.spell,
                net.farmingtales.ftboss.util.SpellVisualRules.Phase.PULSE, center);
        var particle = switch (kind.boss) {
            case "sugar_queen" -> ParticleTypes.SNOWFLAKE;
            case "tea_tree" -> kind.school.equals("blood") ? ParticleTypes.CRIMSON_SPORE : ParticleTypes.SPORE_BLOSSOM_AIR;
            case "wine_barrel" -> kind == Kind.WINE_TOAST ? ParticleTypes.ENCHANT : ParticleTypes.FLAME;
            case "defect_core" -> ParticleTypes.ELECTRIC_SPARK;
            case "compost_boss" -> ParticleTypes.SPORE_BLOSSOM_AIR;
            case "apple_boss" -> ParticleTypes.END_ROD;
            default -> ParticleTypes.SPLASH;
        };
        level.sendParticles(particle, center.x, center.y + 0.8, center.z, 24, 1, 0.7, 1, 0.04);
        if (kind == Kind.DEFECT_STORM) {
            for (int offset = -2; offset <= 2; offset += 2) {
                BossSpellEffects.beam(level, caster, kind, center.add(offset, 0.3, -3), center.add(offset, 0.3, 3));
                BossSpellEffects.beam(level, caster, kind, center.add(-3, 0.3, offset), center.add(3, 0.3, offset));
            }
        } else if (kind == Kind.APPLE_ORBIT) {
            for (int fruit = 0; fruit < 3; fruit++) {
                double angle = fruit * Math.PI * 2 / 3 + pulse * 0.7;
                BossSpellEffects.ring(level, kind, center.add(Math.cos(angle) * 2.5, 0.8, Math.sin(angle) * 2.5), 0.4, 8);
            }
        } else if (kind == Kind.TEA_FLESH) {
            for (int petal = 0; petal < 6; petal++) {
                double angle = petal * Math.PI / 3;
                BossSpellEffects.beam(level, caster, kind, center.add(0, 0.2, 0), center.add(Math.cos(angle) * 3, 1.2, Math.sin(angle) * 3));
            }
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        List<Field> fields = FIELDS.get(level);
        if (fields == null) return;
        fields.removeIf(field -> {
            long age = level.getGameTime() - field.started;
            if (age < 0 || age > BossSpellRules.lastPulse(field.kind)
                    || !(level.getEntity(field.caster) instanceof LivingEntity caster) || !caster.isAlive()
                    || caster.isRemoved() || !net.farmingtales.ftboss.boss.ai.LemonSacrifice.allowsSpellEffect(caster)
                    || caster.distanceToSqr(field.center) > 48 * 48
                    || !level.hasChunkAt(BlockPos.containing(field.center)) || !BossIronSpells.spell(field.kind).isEnabled()) return true;
            if (field.kind == Kind.TEA_SUTURE && (!(level.getEntity(field.target) instanceof LivingEntity target)
                    || !BossSpellEffects.canTarget(caster, target) || caster.distanceToSqr(target) > 18 * 18 || !caster.hasLineOfSight(target))) return true;
            int pulse = BossSpellRules.pulseIndex(field.kind, age);
            if (pulse > field.lastPulse) {
                net.farmingtales.ftboss.boss.ai.LemonSacrifice.spellEffect(caster, () -> pulse(level, caster, field, pulse));
                field.lastPulse = pulse;
            } else if (age % 6 == 0) warning(level, field);
            return age >= BossSpellRules.lastPulse(field.kind);
        });
        if (fields.isEmpty()) FIELDS.remove(level);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { FIELDS.remove(event.getLevel()); }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) { FIELDS.clear(); }

    private static final class Field {
        final UUID caster;
        final Kind kind;
        final Vec3 origin;
        final Vec3 center;
        final Vec3 direction;
        final UUID target;
        final float power;
        final long started;
        final Set<UUID> hits = new HashSet<>();
        int lastPulse = -1;

        Field(UUID caster, Kind kind, Vec3 origin, Vec3 center, Vec3 direction, UUID target, float power, long started) {
            this.caster = caster;
            this.kind = kind;
            this.origin = origin;
            this.center = center;
            this.direction = direction;
            this.target = target;
            this.power = power;
            this.started = started;
        }
    }
}
