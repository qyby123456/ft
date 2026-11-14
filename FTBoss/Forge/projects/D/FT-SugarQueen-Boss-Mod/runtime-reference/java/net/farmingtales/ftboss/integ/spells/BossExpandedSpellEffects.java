/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.damage.DamageSources
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$LevelTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
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
import net.farmingtales.ftboss.boss.ai.LemonSacrifice;
import net.farmingtales.ftboss.integ.spells.BossIronSpells;
import net.farmingtales.ftboss.integ.spells.BossSecondSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BossExpandedSpellEffects {
    private static final int MAX_FIELDS = 64;
    private static final Map<ServerLevel, List<Field>> FIELDS = new WeakHashMap<ServerLevel, List<Field>>();

    private BossExpandedSpellEffects() {
    }

    public static void cast(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Vec3 center, float power) {
        if (!(BossSpellRules.expanded(spell.kind) && spell.isEnabled() && Float.isFinite(power) && !(power <= 0.0f) && BossSpellEffects.visible(level, caster, caster.m_146892_(), center.m_82520_(0.0, 0.1, 0.0)))) {
            return;
        }
        Vec3 direction = center.m_82546_(caster.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
        if (direction.m_82556_() < 0.01) {
            direction = caster.m_20154_().m_82542_(1.0, 0.0, 1.0).m_82541_();
        }
        UUID target = null;
        if (spell.kind == BossSpellRules.Kind.TEA_SUTURE) {
            LivingEntity victim = BossSpellEffects.targets(level, caster, center, 3.0).stream().filter(candidate -> caster.m_20280_((Entity)candidate) <= 324.0 && caster.m_142582_((Entity)candidate)).findFirst().orElse(null);
            if (victim == null) {
                return;
            }
            target = victim.m_20148_();
        }
        Field field = new Field(caster.m_20148_(), spell.kind, caster.m_20182_(), center, direction, target, power, level.m_46467_());
        List fields = FIELDS.computeIfAbsent(level, ignored -> new ArrayList());
        fields.removeIf(previous -> previous.caster.equals(field.caster) && previous.kind == field.kind);
        BossExpandedSpellEffects.warning(level, field);
        if (BossSpellRules.firstPulse(field.kind) == 0) {
            BossExpandedSpellEffects.pulse(level, caster, field, 0);
            field.lastPulse = 0;
        }
        if (BossSpellRules.lastPulse(field.kind) > 0) {
            if (fields.size() >= 64) {
                fields.remove(0);
            }
            fields.add(field);
        }
        if (fields.isEmpty()) {
            FIELDS.remove(level);
        }
    }

    private static Vec3 point(Field field, int pulse) {
        return switch (field.kind) {
            case BossSpellRules.Kind.TEA_THORNS -> field.origin.m_82549_(field.direction.m_82490_((double)(4 + pulse * 4)));
            case BossSpellRules.Kind.TIDE_SURGE -> field.origin.m_82549_(field.direction.m_82490_((double)(5 + pulse * 5)));
            case BossSpellRules.Kind.WINE_FUSE -> field.center.m_82520_(Math.cos((double)pulse * Math.PI * 2.0 / 3.0) * 2.5, 0.0, Math.sin((double)pulse * Math.PI * 2.0 / 3.0) * 2.5);
            default -> field.center;
        };
    }

    private static void warning(ServerLevel level, Field field) {
        int points = field.kind == BossSpellRules.Kind.WINE_FUSE || field.kind == BossSpellRules.Kind.TEA_THORNS || field.kind == BossSpellRules.Kind.TIDE_SURGE ? 3 : 1;
        for (int index = 0; index < points; ++index) {
            BossSpellEffects.ring(level, field.kind, BossExpandedSpellEffects.point(field, index), points == 1 ? 3.5 : 2.0, 12);
        }
    }

    private static void effect(LivingEntity target, LivingEntity caster, MobEffect effect, int duration, int amplifier) {
        target.m_147207_(new MobEffectInstance(effect, BossSpellTuning.buffTicks(caster, effect, duration), amplifier), (Entity)caster);
    }

    private static void shield(LivingEntity caster, float power, int duration) {
        int amplifier = BossSpellRules.shield(power) / 4 - 1;
        MobEffectInstance previous = caster.m_21124_(MobEffects.f_19617_);
        if (previous != null) {
            amplifier = Math.max(amplifier, previous.m_19564_());
            duration = Math.max(duration, previous.m_19557_());
        }
        BossExpandedSpellEffects.effect(caster, caster, MobEffects.f_19617_, duration, amplifier);
    }

    private static List<LivingEntity> allies(ServerLevel level, LivingEntity caster, double radius) {
        ArrayList<LivingEntity> allies = new ArrayList<LivingEntity>();
        allies.add(caster);
        allies.addAll(level.m_6443_(LivingEntity.class, caster.m_20191_().m_82400_(radius), target -> target != caster && BossSecondSpellEffects.canHelp(caster, target) && caster.m_20280_((Entity)target) <= radius * radius && caster.m_142582_((Entity)target)).stream().sorted(Comparator.comparingDouble(arg_0 -> ((LivingEntity)caster).m_20280_(arg_0))).limit(15L).toList());
        return allies;
    }

    private static void pulse(ServerLevel level, LivingEntity caster, Field field, int pulse) {
        BossSignatureSpell spell = BossIronSpells.spell(field.kind);
        Vec3 center = BossExpandedSpellEffects.point(field, pulse);
        if (BossSpellRules.selfCentered(field.kind)) {
            center = caster.m_20182_();
        }
        if (!level.m_46805_(BlockPos.m_274446_((Position)center))) {
            return;
        }
        if (field.kind == BossSpellRules.Kind.TEA_THORNS || field.kind == BossSpellRules.Kind.TIDE_SURGE ? !BossSpellEffects.visible(level, caster, field.origin.m_82520_(0.0, 0.5, 0.0), center.m_82520_(0.0, 0.5, 0.0)) : field.kind == BossSpellRules.Kind.WINE_FUSE && !BossSpellEffects.visible(level, caster, field.center.m_82520_(0.0, 0.3, 0.0), center.m_82520_(0.0, 0.3, 0.0))) {
            return;
        }
        BossExpandedSpellEffects.visuals(level, caster, field.kind, center, pulse);
        switch (field.kind) {
            case FROST_PRISM: {
                BossExpandedSpellEffects.shield(caster, field.power, 160);
                BossExpandedSpellEffects.effect(caster, caster, MobEffects.f_19606_, 80, 0);
                for (LivingEntity target : BossSpellEffects.targets(level, caster, center, 3.0)) {
                    BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19597_, 60, 2);
                    target.m_146917_(Math.max(100, target.m_146888_()));
                }
                break;
            }
            case TEA_RENEWAL: {
                for (LivingEntity ally : BossExpandedSpellEffects.allies(level, caster, 5.0)) {
                    ally.m_5634_(field.power);
                    ally.m_21195_(MobEffects.f_19614_);
                    BossSpellEffects.beam(level, caster, field.kind, caster.m_146892_(), ally.m_146892_());
                }
                break;
            }
            case WINE_TOAST: {
                BossExpandedSpellEffects.shield(caster, field.power, 160);
                BossExpandedSpellEffects.effect(caster, caster, MobEffects.f_19600_, 120, 0);
                BossExpandedSpellEffects.effect(caster, caster, MobEffects.f_19596_, 120, 0);
                break;
            }
            case DEFECT_REBOOT: {
                caster.m_5634_(field.power);
                caster.m_21195_(MobEffects.f_19613_);
                caster.m_21195_(MobEffects.f_19599_);
                BossExpandedSpellEffects.effect(caster, caster, MobEffects.f_19606_, 100, 0);
                BossExpandedSpellEffects.effect(caster, caster, MobEffects.f_19597_, 30, 1);
                break;
            }
            case COMPOST_MULCH: {
                for (LivingEntity ally : BossExpandedSpellEffects.allies(level, caster, 4.0)) {
                    if (ally.m_21023_(MobEffects.f_19614_) || ally.m_21023_(MobEffects.f_19615_)) {
                        BossExpandedSpellEffects.shield(ally, 8.0f, 140);
                    }
                    ally.m_21195_(MobEffects.f_19614_);
                    ally.m_21195_(MobEffects.f_19615_);
                    ally.m_5634_(field.power);
                }
                break;
            }
            case TIDE_BUBBLE: {
                BossExpandedSpellEffects.shield(caster, field.power, 180);
                caster.m_20095_();
                BossExpandedSpellEffects.effect(caster, caster, MobEffects.f_19608_, 200, 0);
                BossExpandedSpellEffects.effect(caster, caster, MobEffects.f_19606_, 100, 0);
                for (LivingEntity target2 : BossSpellEffects.targets(level, caster, center, 4.0)) {
                    BossExpandedSpellEffects.push(target2, center, 0.5, 0.15);
                }
                break;
            }
            case TEA_SUTURE: {
                LivingEntity target;
                Entity target2 = level.m_8791_(field.target);
                if (!(target2 instanceof LivingEntity) || !BossSpellEffects.canTarget(caster, target = (LivingEntity)target2) || !(caster.m_20280_((Entity)target) <= 324.0) || !caster.m_142582_((Entity)target)) break;
                float before = target.m_21223_();
                BossSpellEffects.beam(level, caster, field.kind, caster.m_146892_(), target.m_20191_().m_82399_());
                if (!DamageSources.applyDamage((Entity)target, (float)field.power, (DamageSource)spell.getDamageSource((Entity)caster))) break;
                caster.m_5634_(Math.min(4.0f, Math.max(0.0f, before - target.m_21223_()) * 0.3f));
                BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19613_, 45, 0);
                break;
            }
            case DEFECT_RAIL: {
                BossExpandedSpellEffects.rail(level, caster, spell, field);
                break;
            }
            case WINE_BREATH: {
                BossExpandedSpellEffects.breath(level, caster, spell, field);
                break;
            }
            default: {
                BossExpandedSpellEffects.area(level, caster, spell, field, center, pulse);
            }
        }
    }

    private static void push(LivingEntity target, Vec3 center, double horizontal, double upward) {
        Vec3 direction = target.m_20182_().m_82546_(center).m_82542_(1.0, 0.0, 1.0).m_82541_();
        BossKnockback.push((Entity)target, direction.f_82479_ * horizontal, upward, direction.f_82481_ * horizontal);
        target.f_19864_ = true;
    }

    private static void area(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Field field, Vec3 center, int pulse) {
        double radius = switch (field.kind) {
            case BossSpellRules.Kind.TEA_THORNS, BossSpellRules.Kind.WINE_FUSE -> 2.2;
            case BossSpellRules.Kind.TIDE_SURGE -> 3.0;
            case BossSpellRules.Kind.COMPOST_ROT -> 2 + pulse * 2;
            case BossSpellRules.Kind.APPLE_ORBIT -> 3.5;
            default -> 4.0;
        };
        BossSpellEffects.ring(level, field.kind, center, radius, 24);
        float stolen = 0.0f;
        int harvested = 0;
        block27: for (LivingEntity target : BossSpellEffects.targets(level, caster, center, radius)) {
            boolean once;
            boolean bl = once = field.kind == BossSpellRules.Kind.TEA_THORNS || field.kind == BossSpellRules.Kind.COMPOST_ROT || field.kind == BossSpellRules.Kind.TIDE_SURGE;
            if (once && (field.hits.size() >= 16 || !field.hits.add(target.m_20148_()))) continue;
            boolean marked = switch (field.kind) {
                case BossSpellRules.Kind.FROST_SHATTER -> {
                    if (target.m_146888_() > 0) {
                        yield true;
                    }
                    yield false;
                }
                case BossSpellRules.Kind.COMPOST_ROT -> target.m_21023_(MobEffects.f_19614_);
                case BossSpellRules.Kind.APPLE_SEED, BossSpellRules.Kind.APPLE_HARVEST -> target.m_21023_(MobEffects.f_19619_);
                default -> false;
            };
            float before = target.m_21223_();
            if (!DamageSources.applyDamage((Entity)target, (float)BossSpellRules.bonusDamage(field.power, marked), (DamageSource)spell.getDamageSource((Entity)caster))) continue;
            switch (field.kind) {
                case FROST_HAIL: {
                    target.m_146917_(Math.max(100, target.m_146888_()));
                    BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19597_, 50, 1);
                    continue block27;
                }
                case FROST_SHATTER: {
                    target.m_146917_(0);
                    BossExpandedSpellEffects.push(target, center, 0.35, 0.15);
                    continue block27;
                }
                case TEA_THORNS: {
                    BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19597_, 45, 3);
                    continue block27;
                }
                case TEA_POLLEN: {
                    BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19614_, 60, 0);
                    BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19613_, 60, 0);
                    continue block27;
                }
                case TEA_FLESH: {
                    stolen += Math.max(0.0f, before - target.m_21223_()) * 0.2f;
                    BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19597_, 60, 2);
                    BossSpellEffects.beam(level, caster, field.kind, target.m_146892_(), caster.m_146892_());
                    continue block27;
                }
                case WINE_FUSE: {
                    target.m_20254_(3);
                    BossExpandedSpellEffects.push(target, center, 0.35, 0.2);
                    continue block27;
                }
                case DEFECT_STORM: {
                    BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19597_, 35, 1);
                    BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19599_, 60, 1);
                    BossSpellEffects.beam(level, caster, field.kind, center.m_82520_(-2.0, 2.0, -2.0), target.m_146892_());
                    continue block27;
                }
                case COMPOST_ERUPTION: {
                    BossKnockback.push((Entity)target, 0.0, 0.5, 0.0);
                    target.f_19864_ = true;
                    BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19614_, 100, 0);
                    continue block27;
                }
                case COMPOST_ROT: {
                    if (marked) {
                        target.m_21195_(MobEffects.f_19614_);
                    }
                    BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19615_, 60, 0);
                    continue block27;
                }
                case APPLE_ORBIT: {
                    BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19619_, 100, 0);
                    BossExpandedSpellEffects.push(target, center, 0.15, 0.0);
                    continue block27;
                }
                case APPLE_SEED: {
                    if (marked) {
                        target.m_21195_(MobEffects.f_19619_);
                    }
                    BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19613_, 80, 0);
                    continue block27;
                }
                case APPLE_HARVEST: {
                    if (marked) {
                        target.m_21195_(MobEffects.f_19619_);
                        ++harvested;
                    }
                    BossSpellEffects.beam(level, caster, field.kind, target.m_146892_(), caster.m_146892_());
                    continue block27;
                }
                case TIDE_RIP: {
                    BossExpandedSpellEffects.push(target, center, -0.28, 0.05);
                    BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19613_, 50, 0);
                    continue block27;
                }
                case TIDE_SURGE: {
                    BossKnockback.push((Entity)target, field.direction.f_82479_ * 0.65, 0.2, field.direction.f_82481_ * 0.65);
                    target.f_19864_ = true;
                    target.m_20095_();
                    BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19597_, 50, 1);
                    continue block27;
                }
            }
            throw new IllegalArgumentException("Not an area spell: " + field.kind);
        }
        if (stolen > 0.0f) {
            caster.m_5634_(Math.min(6.0f, stolen));
        }
        if (harvested > 0) {
            BossExpandedSpellEffects.shield(caster, Math.min(12, harvested * 4), 120);
        }
    }

    private static void rail(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Field field) {
        Vec3 start = caster.m_146892_();
        Vec3 direction = field.center.m_82546_(start).m_82541_();
        if (direction.m_82556_() < 0.01) {
            direction = caster.m_20154_();
        }
        Vec3 end = level.m_45547_(new ClipContext(start, start.m_82549_(direction.m_82490_(24.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)caster)).m_82450_();
        Vec3 segment = end.m_82546_(start);
        BossSpellEffects.beam(level, caster, field.kind, start, end);
        int hits = 0;
        for (LivingEntity target : level.m_6443_(LivingEntity.class, new AABB(start, end).m_82400_(0.9), candidate -> BossSpellEffects.canTarget(caster, candidate)).stream().sorted(Comparator.comparingDouble(arg_0 -> ((LivingEntity)caster).m_20280_(arg_0))).limit(16L).toList()) {
            Vec3 body = target.m_20191_().m_82399_();
            double progress = segment.m_82556_() < 0.01 ? -1.0 : body.m_82546_(start).m_82526_(segment) / segment.m_82556_();
            if (progress < 0.0 || progress > 1.0 || body.m_82557_(start.m_82549_(segment.m_82490_(progress))) > 0.81 || !BossSpellEffects.visible(level, caster, start, body)) continue;
            if (DamageSources.applyDamage((Entity)target, (float)(field.power * (1.0f - (float)hits * 0.15f)), (DamageSource)spell.getDamageSource((Entity)caster))) {
                BossExpandedSpellEffects.effect(target, caster, MobEffects.f_19619_, 80, 0);
            }
            if (++hits < 4) continue;
            break;
        }
    }

    private static void breath(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Field field) {
        for (int distance = 2; distance <= 10; distance += 2) {
            Vec3 middle = field.origin.m_82549_(field.direction.m_82490_((double)distance)).m_82520_(0.0, 0.8, 0.0);
            Vec3 side = new Vec3(-field.direction.f_82481_, 0.0, field.direction.f_82479_).m_82490_((double)distance * 0.35);
            BossSpellEffects.beam(level, caster, field.kind, middle.m_82546_(side), middle.m_82549_(side));
        }
        for (LivingEntity target : BossSpellEffects.targets(level, caster, field.origin, 12.0)) {
            Vec3 offset = target.m_20182_().m_82546_(field.origin);
            double forward = offset.m_82526_(field.direction);
            double sideways = offset.f_82479_ * -field.direction.f_82481_ + offset.f_82481_ * field.direction.f_82479_;
            if (forward < 0.0 || forward > 10.0 || Math.abs(sideways) > 0.8 + forward * 0.35 || Math.abs(offset.f_82480_) > 3.0 || !DamageSources.applyDamage((Entity)target, (float)field.power, (DamageSource)spell.getDamageSource((Entity)caster))) continue;
            target.m_20254_(3);
        }
    }

    private static void visuals(ServerLevel level, LivingEntity caster, BossSpellRules.Kind kind, Vec3 center, int pulse) {
        block22: {
            block23: {
                block21: {
                    SpellVisuals.show(level, caster, kind.spell, SpellVisualRules.Phase.PULSE, center);
                    SimpleParticleType particle = switch (kind.boss) {
                        case "sugar_queen" -> ParticleTypes.f_175821_;
                        case "tea_tree" -> {
                            if (kind.school.equals("blood")) {
                                yield ParticleTypes.f_123784_;
                            }
                            yield ParticleTypes.f_175833_;
                        }
                        case "wine_barrel" -> {
                            if (kind == BossSpellRules.Kind.WINE_TOAST) {
                                yield ParticleTypes.f_123809_;
                            }
                            yield ParticleTypes.f_123744_;
                        }
                        case "defect_core" -> ParticleTypes.f_175830_;
                        case "compost_boss" -> ParticleTypes.f_175833_;
                        case "apple_boss" -> ParticleTypes.f_123810_;
                        default -> ParticleTypes.f_123769_;
                    };
                    level.m_8767_((ParticleOptions)particle, center.f_82479_, center.f_82480_ + 0.8, center.f_82481_, 24, 1.0, 0.7, 1.0, 0.04);
                    if (kind != BossSpellRules.Kind.DEFECT_STORM) break block21;
                    for (int offset = -2; offset <= 2; offset += 2) {
                        BossSpellEffects.beam(level, caster, kind, center.m_82520_((double)offset, 0.3, -3.0), center.m_82520_((double)offset, 0.3, 3.0));
                        BossSpellEffects.beam(level, caster, kind, center.m_82520_(-3.0, 0.3, (double)offset), center.m_82520_(3.0, 0.3, (double)offset));
                    }
                    break block22;
                }
                if (kind != BossSpellRules.Kind.APPLE_ORBIT) break block23;
                for (int fruit = 0; fruit < 3; ++fruit) {
                    double angle = (double)fruit * Math.PI * 2.0 / 3.0 + (double)pulse * 0.7;
                    BossSpellEffects.ring(level, kind, center.m_82520_(Math.cos(angle) * 2.5, 0.8, Math.sin(angle) * 2.5), 0.4, 8);
                }
                break block22;
            }
            if (kind != BossSpellRules.Kind.TEA_FLESH) break block22;
            for (int petal = 0; petal < 6; ++petal) {
                double angle = (double)petal * Math.PI / 3.0;
                BossSpellEffects.beam(level, caster, kind, center.m_82520_(0.0, 0.2, 0.0), center.m_82520_(Math.cos(angle) * 3.0, 1.2, Math.sin(angle) * 3.0));
            }
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        Level level;
        if (event.phase != TickEvent.Phase.END || !((level = event.level) instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        List<Field> fields = FIELDS.get(level2);
        if (fields == null) {
            return;
        }
        fields.removeIf(field -> {
            LivingEntity target;
            Entity patt20295$temp;
            LivingEntity caster;
            Entity patt19830$temp;
            long age = level2.m_46467_() - field.started;
            if (!(age >= 0L && age <= (long)BossSpellRules.lastPulse(field.kind) && (patt19830$temp = level2.m_8791_(field.caster)) instanceof LivingEntity && (caster = (LivingEntity)patt19830$temp).m_6084_() && !caster.m_213877_() && LemonSacrifice.allowsSpellEffect(caster) && !(caster.m_20238_(field.center) > 2304.0) && level2.m_46805_(BlockPos.m_274446_((Position)field.center)) && BossIronSpells.spell(field.kind).isEnabled())) {
                return true;
            }
            if (!(field.kind != BossSpellRules.Kind.TEA_SUTURE || (patt20295$temp = level2.m_8791_(field.target)) instanceof LivingEntity && BossSpellEffects.canTarget(caster, target = (LivingEntity)patt20295$temp) && !(caster.m_20280_((Entity)target) > 324.0) && caster.m_142582_((Entity)target))) {
                return true;
            }
            int pulse = BossSpellRules.pulseIndex(field.kind, age);
            if (pulse > field.lastPulse) {
                LemonSacrifice.spellEffect(caster, () -> BossExpandedSpellEffects.pulse(level2, caster, field, pulse));
                field.lastPulse = pulse;
            } else if (age % 6L == 0L) {
                BossExpandedSpellEffects.warning(level2, field);
            }
            return age >= (long)BossSpellRules.lastPulse(field.kind);
        });
        if (fields.isEmpty()) {
            FIELDS.remove(level2);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        FIELDS.remove(event.getLevel());
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        FIELDS.clear();
    }

    private static final class Field {
        final UUID caster;
        final BossSpellRules.Kind kind;
        final Vec3 origin;
        final Vec3 center;
        final Vec3 direction;
        final UUID target;
        final float power;
        final long started;
        final Set<UUID> hits = new HashSet<UUID>();
        int lastPulse = -1;

        Field(UUID caster, BossSpellRules.Kind kind, Vec3 origin, Vec3 center, Vec3 direction, UUID target, float power, long started) {
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

