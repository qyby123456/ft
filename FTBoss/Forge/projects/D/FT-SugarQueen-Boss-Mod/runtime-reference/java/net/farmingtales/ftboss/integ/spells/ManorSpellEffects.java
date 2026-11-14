/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.damage.DamageSources
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.OwnableEntity
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$LevelTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.ManorBossSpellProfile;
import net.farmingtales.ftboss.integ.spells.ManorIronSpells;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.integ.spells.ManorSparrowCasting;
import net.farmingtales.ftboss.integ.spells.ManorSpellCombat;
import net.farmingtales.ftboss.integ.spells.ManorThirdSpellEffects;
import net.farmingtales.ftboss.integ.spells.MaqueFeatherCombat;
import net.farmingtales.ftboss.integ.spells.MaqueFeatherFlights;
import net.farmingtales.ftboss.integ.spells.SparrowBorrowedSpells;
import net.farmingtales.ftboss.integ.spells.YouziCloneSpellProfile;
import net.farmingtales.ftboss.integ.spells.YouziSpellFlightBridge;
import net.farmingtales.ftboss.integ.spells.ZheergenAreaSeal;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.SpellBeamModels;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.util.XuanlanLengths;
import net.farmingtales.ftboss.zheergenphantom.ErosionFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector3f;

public final class ManorSpellEffects {
    private static final Map<ManorSpellRules.Kind, DustParticleOptions> COLORS = new EnumMap<ManorSpellRules.Kind, DustParticleOptions>(ManorSpellRules.Kind.class);
    private static final Map<ServerLevel, List<Field>> FIELDS = new WeakHashMap<ServerLevel, List<Field>>();

    private ManorSpellEffects() {
    }

    public static void warning(ServerLevel level, LivingEntity caster, ManorSpellRules.Kind kind, Vec3 point) {
        if (!ManorSpellRules.validPoint(point.m_82557_(caster.m_20182_()))) {
            return;
        }
        SpellVisuals.show(level, caster, kind.id, SpellVisualRules.Phase.CHARGE, point);
        ManorSpellEffects.ring(level, caster, kind, kind.support() || kind == ManorSpellRules.Kind.ORBITAL_GEARS ? caster.m_20182_() : point, 2.0, 14);
        level.m_8767_((ParticleOptions)COLORS.get((Object)kind), caster.m_20185_(), caster.m_20186_() + 1.2, caster.m_20189_(), 5, 0.3, 0.4, 0.3, 0.0);
    }

    /*
     * WARNING - void declaration
     */
    public static void cast(ServerLevel serverLevel, LivingEntity livingEntity, ManorSignatureSpell manorSignatureSpell, int n, Vec3 vec3) {
        void rank;
        ServerLevel level;
        void center;
        void spell;
        EasterBossEntity boss;
        void caster;
        if (MaqueFeatherFlights.cast(serverLevel, livingEntity, manorSignatureSpell, n, vec3)) {
            return;
        }
        if (caster instanceof EasterBossEntity && (boss = (EasterBossEntity)caster).isMini() && (!YouziCloneSpellProfile.isClone((LivingEntity)boss) || !ManorSpellCombat.eligible(boss) || spell.kind.variant != 0)) {
            return;
        }
        if (!ManorSpellRules.validPoint(center.m_82557_(caster.m_20182_())) || !level.m_46805_(BlockPos.m_274446_((Position)center))) {
            return;
        }
        int spellLevel = ManorSpellRules.level((int)rank);
        boolean clone = YouziCloneSpellProfile.isClone((LivingEntity)caster);
        float damage = BossSpellTuning.playerPower((LivingEntity)caster, clone ? YouziCloneSpellProfile.power((ManorSignatureSpell)spell, (LivingEntity)caster) : (BossSpellTuning.manorBoss((LivingEntity)caster) ? ManorBossSpellProfile.power(spell.kind) : spell.getSpellPower(spellLevel, (Entity)caster)));
        if (!Float.isFinite(damage) || damage < 0.0f) {
            return;
        }
        SpellVisuals.show(level, (LivingEntity)caster, spell.kind.id, SpellVisualRules.Phase.RELEASE, (Vec3)center);
        if (BossSpellTuning.manorBoss((LivingEntity)caster) && spell.kind == ManorSpellRules.Kind.SHADOW_BRAND) {
            ManorSpellEffects.ring(level, (LivingEntity)caster, spell.kind, (Vec3)center, 3.5, 24);
            for (LivingEntity target : ZheergenAreaSeal.targets(level, (LivingEntity)caster, (Vec3)center, 3.5)) {
                ZheergenAreaSeal.hurt((LivingEntity)caster, target, (ManorSignatureSpell)spell, damage);
            }
            return;
        }
        if (spell.kind.additional()) {
            if (spell.kind.support()) {
                ManorThirdSpellEffects.support(level, (LivingEntity)caster, (ManorSignatureSpell)spell, spellLevel);
            } else {
                List fields = FIELDS.computeIfAbsent(level, ignored -> new ArrayList());
                fields.removeIf(arg_0 -> ManorSpellEffects.lambda$cast$1((LivingEntity)caster, (ManorSignatureSpell)spell, arg_0));
                if (fields.size() >= 64) {
                    fields.remove(0);
                }
                fields.add(new Field(caster.m_20148_(), spell.kind, (Vec3)center, spellLevel, damage, level.m_46467_(), ManorSparrowCasting.ownerOf((LivingEntity)caster, spell.kind)));
            }
            return;
        }
        switch (spell.kind) {
            case EFFIGY_VOLLEY: 
            case ARCANE_FUSILLADE: 
            case CANDLE_PRISON: 
            case BREACH_SHELL: 
            case HEARTH_FEAST: 
            case ELIXIR_RAIN: 
            case ORBITAL_GEARS: 
            case SHADOW_BRAND: 
            case VOID_ECHO: 
            case HALO_SANCTUARY: 
            case WHITEOUT_EDICT: {
                Field field = new Field(caster.m_20148_(), spell.kind, (Vec3)center, spellLevel, damage, level.m_46467_(), ManorSparrowCasting.ownerOf((LivingEntity)caster, spell.kind));
                List fields = FIELDS.computeIfAbsent(level, ignored -> new ArrayList());
                fields.removeIf(arg_0 -> ManorSpellEffects.lambda$cast$3((LivingEntity)caster, (ManorSignatureSpell)spell, arg_0));
                if (fields.size() >= 64) {
                    fields.remove(0);
                }
                fields.add(field);
                ManorSpellEffects.pulse(level, (LivingEntity)caster, field, 0);
                break;
            }
            case EFFIGY_WARD: {
                ManorSpellEffects.effect((LivingEntity)caster, (LivingEntity)caster, MobEffects.f_19617_, clone ? 80 : 160 + spellLevel * 20, clone ? 0 : (spellLevel - 1) / 2);
                ManorSpellEffects.effect((LivingEntity)caster, (LivingEntity)caster, MobEffects.f_19606_, clone ? 60 : 100 + spellLevel * 20, 0);
                ManorSpellEffects.ring(level, (LivingEntity)caster, spell.kind, caster.m_20182_(), 2.5, 28);
                break;
            }
            case CRIMSON_OATH: {
                ManorSpellEffects.effect((LivingEntity)caster, (LivingEntity)caster, MobEffects.f_19600_, 100 + spellLevel * 20, spellLevel >= 4 ? 1 : 0);
                ManorSpellEffects.effect((LivingEntity)caster, (LivingEntity)caster, MobEffects.f_19606_, 80 + spellLevel * 20, 0);
                ManorSpellEffects.effect((LivingEntity)caster, (LivingEntity)caster, MobEffects.f_19596_, 80 + spellLevel * 20, 0);
                ManorSpellEffects.ring(level, (LivingEntity)caster, spell.kind, caster.m_20182_(), 2.0, 24);
                break;
            }
            case WINGSTEP: {
                ManorSpellEffects.effect((LivingEntity)caster, (LivingEntity)caster, MobEffects.f_19596_, 100 + spellLevel * 20, 1);
                ManorSpellEffects.effect((LivingEntity)caster, (LivingEntity)caster, MobEffects.f_19603_, 100 + spellLevel * 20, 1);
                ManorSpellEffects.effect((LivingEntity)caster, (LivingEntity)caster, MobEffects.f_19591_, 140 + spellLevel * 20, 0);
                level.m_8767_((ParticleOptions)ParticleTypes.f_123796_, caster.m_20185_(), caster.m_20186_() + 0.3, caster.m_20189_(), 24, 0.7, 0.3, 0.7, 0.04);
                break;
            }
            case CANDLE_REAP: 
            case RENDING_CLEAVE: 
            case SEARING_PAN: 
            case FOUR_TIDES: {
                ManorSpellEffects.cone(level, (LivingEntity)caster, (ManorSignatureSpell)spell, (Vec3)center, damage);
                break;
            }
            case PRESSURE_LANCE: {
                ManorSpellEffects.ray(level, (LivingEntity)caster, (ManorSignatureSpell)spell, (Vec3)center, damage, 0.6, true, new HashSet<UUID>());
                break;
            }
            case FEATHER_FAN: 
            case FROST_NEEDLES: {
                Vec3 direction = center.m_82546_(caster.m_146892_()).m_82541_();
                int wings = spell.kind == ManorSpellRules.Kind.FEATHER_FAN ? 2 : 1;
                HashSet<UUID> hit = new HashSet<UUID>();
                for (int offset = -wings; offset <= wings; ++offset) {
                    Vec3 end = caster.m_146892_().m_82549_(direction.m_82524_((float)offset * 0.16f).m_82490_(24.0));
                    ManorSpellEffects.ray(level, (LivingEntity)caster, (ManorSignatureSpell)spell, end, damage, 0.55, false, hit);
                }
                break;
            }
            case LUMINOUS_REBUKE: {
                ManorSpellEffects.ring(level, (LivingEntity)caster, spell.kind, caster.m_20182_(), 5.0, 36);
                for (LivingEntity target : ManorSpellEffects.targets(level, (LivingEntity)caster, caster.m_20182_(), 5.0)) {
                    if (!ManorSpellEffects.hurt((LivingEntity)caster, target, (ManorSignatureSpell)spell, damage)) continue;
                    BossKnockback.knockback((Entity)caster, target, 1.1, caster.m_20185_() - target.m_20185_(), caster.m_20189_() - target.m_20189_());
                    ManorSpellEffects.effect((LivingEntity)caster, target, MobEffects.f_19619_, 100, 0);
                }
                break;
            }
        }
    }

    static boolean hurt(LivingEntity caster, LivingEntity target, ManorSignatureSpell spell, float damage) {
        if (!BossSpellEffects.canTarget(caster, target)) {
            return MaqueFeatherCombat.afterHit(false, caster, target, spell);
        }
        if (BossSpellTuning.manorBoss(caster)) {
            return MaqueFeatherCombat.afterHit(ManorBossSpellProfile.hurt((EasterBossEntity)caster, target, spell, damage), caster, target, spell);
        }
        return MaqueFeatherCombat.afterHit(DamageSources.applyDamage((Entity)target, (float)damage, (DamageSource)spell.getDamageSource((Entity)caster)), caster, target, spell);
    }

    /*
     * WARNING - void declaration
     */
    static void effect(LivingEntity livingEntity, LivingEntity livingEntity2, MobEffect mobEffect, int n, int n2) {
        void amplifier;
        void ticks;
        LivingEntity caster;
        void effect;
        void target;
        if (ErosionFilter.suppress(livingEntity, mobEffect)) {
            return;
        }
        target.m_147207_(new MobEffectInstance((MobEffect)effect, BossSpellTuning.buffTicks(caster, (MobEffect)effect, (int)ticks), (int)amplifier), (Entity)caster);
    }

    private static Vec3 clip(ServerLevel level, LivingEntity caster, Vec3 start, Vec3 end) {
        return level.m_45547_(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)caster)).m_82450_();
    }

    private static boolean visible(ServerLevel level, LivingEntity caster, Vec3 start, Vec3 end) {
        return level.m_45547_(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)caster)).m_6662_() == HitResult.Type.MISS;
    }

    static List<LivingEntity> targets(ServerLevel level, LivingEntity caster, Vec3 center, double radius) {
        double effectiveRadius = BossSpellTuning.radius(caster, radius);
        return level.m_6443_(LivingEntity.class, new AABB(center, center).m_82400_(effectiveRadius), target -> BossSpellEffects.canTarget(caster, target) && target.m_20238_(center) <= effectiveRadius * effectiveRadius && caster.m_142582_((Entity)target) && ManorSpellEffects.visible(level, caster, center.m_82520_(0.0, 0.3, 0.0), target.m_20191_().m_82399_())).stream().sorted(Comparator.comparingDouble(target -> target.m_20238_(center))).limit(12L).toList();
    }

    private static void cone(ServerLevel level, LivingEntity caster, ManorSignatureSpell spell, Vec3 center, float damage) {
        Vec3 direction = center.m_82546_(caster.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
        if (direction.m_82556_() < 0.01) {
            direction = caster.m_20154_().m_82542_(1.0, 0.0, 1.0).m_82541_();
        }
        double baseRange = spell.kind == ManorSpellRules.Kind.FOUR_TIDES ? 10.0 : 6.0;
        double scale = BossSpellTuning.radiusScale(caster);
        double range = baseRange * scale;
        for (int slice = 1; slice <= 4; ++slice) {
            Vec3 middle = caster.m_20182_().m_82549_(direction.m_82490_(range * (double)slice / 4.0)).m_82520_(0.0, 0.8, 0.0);
            Vec3 side = new Vec3(-direction.f_82481_, 0.0, direction.f_82479_).m_82490_(0.8 + range * (double)slice / 4.0 * 0.7);
            ManorSpellEffects.beam(level, caster, spell.kind, middle.m_82546_(side), middle.m_82549_(side));
        }
        float stolen = 0.0f;
        int outerHits = 0;
        for (LivingEntity target : ManorSpellEffects.targets(level, caster, caster.m_20182_(), baseRange + 1.0)) {
            Vec3 offset = target.m_20182_().m_82546_(caster.m_20182_());
            if (Math.abs(offset.f_82480_) > 3.0 * scale || !ManorSpellRules.cone(offset.m_82526_(direction) / scale, (offset.f_82479_ * -direction.f_82481_ + offset.f_82481_ * direction.f_82479_) / scale, baseRange)) continue;
            float before = target.m_21223_();
            if (!ManorSpellEffects.hurt(caster, target, spell, damage)) continue;
            if (BossSpellTuning.manorBoss(caster) && spell.kind == ManorSpellRules.Kind.RENDING_CLEAVE && offset.m_165925_() >= Math.pow(4.5 * scale, 2.0)) {
                ++outerHits;
            }
            switch (spell.kind) {
                case CANDLE_REAP: {
                    stolen += Math.max(0.0f, before - target.m_21223_()) * 0.15f;
                    break;
                }
                case RENDING_CLEAVE: {
                    ManorSpellEffects.effect(caster, target, (MobEffect)FTEffects.ARMOR_SHRED.get(), 60, 0);
                    break;
                }
                case SEARING_PAN: {
                    if (BossSpellTuning.manorBoss(caster)) break;
                    target.m_20254_(4);
                    break;
                }
                case FOUR_TIDES: {
                    BossKnockback.knockback((Entity)caster, target, 0.9, -direction.f_82479_, -direction.f_82481_);
                    ManorSpellEffects.effect(caster, target, MobEffects.f_19597_, 60, 1);
                    target.m_20095_();
                    break;
                }
            }
        }
        if (stolen > 0.0f) {
            caster.m_5634_(Math.min(6.0f, stolen));
        }
        if (BossSpellTuning.manorBoss(caster) && spell.kind == ManorSpellRules.Kind.RENDING_CLEAVE) {
            ((EasterBossEntity)caster).finishSignatureCleave(outerHits);
        }
    }

    private static void ray(ServerLevel level, LivingEntity caster, ManorSignatureSpell spell, Vec3 center, float damage, double width, boolean piercing, Set<UUID> hit) {
        Vec3 start = caster.m_146892_();
        width = BossSpellTuning.radius(caster, width);
        Vec3 direction = center.m_82546_(start);
        if (direction.m_82556_() < 0.001) {
            return;
        }
        Vec3 end = ManorSpellEffects.clip(level, caster, start, start.m_82549_(direction.m_82541_().m_82490_(XuanlanLengths.pressure(Math.min(24.0, direction.m_82553_() + 0.8), caster, spell))));
        Vec3 segment = end.m_82546_(start);
        if (segment.m_82556_() < 0.001) {
            return;
        }
        Vec3 visualEnd = end;
        List<LivingEntity> candidates = level.m_6443_(LivingEntity.class, new AABB(start, end).m_82400_(width + 1.0), target -> BossSpellEffects.canTarget(caster, target)).stream().sorted(Comparator.comparingDouble(arg_0 -> ((LivingEntity)caster).m_20280_(arg_0))).toList();
        int hits = 0;
        for (LivingEntity target2 : candidates) {
            AABB body;
            if (hit.contains(target2.m_20148_()) || !(body = target2.m_20191_().m_82400_(width)).m_82390_(start) && body.m_82371_(start, end).isEmpty() || !caster.m_142582_((Entity)target2)) continue;
            hit.add(target2.m_20148_());
            if (ManorSpellEffects.hurt(caster, target2, spell, damage)) {
                if (spell.kind == ManorSpellRules.Kind.FROST_NEEDLES) {
                    target2.m_146917_(Math.max(target2.m_146888_(), 120));
                    ManorSpellEffects.effect(caster, target2, MobEffects.f_19597_, 80, 1);
                }
                if (spell.kind == ManorSpellRules.Kind.FEATHER_FAN) {
                    ManorSpellEffects.effect(caster, target2, MobEffects.f_19619_, 80, 0);
                }
            }
            if (!piercing) {
                visualEnd = body.m_82390_(start) ? start : body.m_82371_(start, end).orElse(end);
                break;
            }
            if (++hits < 12) continue;
            break;
        }
        ManorSpellEffects.beam(level, caster, spell.kind, start, visualEnd);
    }

    /*
     * WARNING - void declaration
     */
    private static void pulse(ServerLevel serverLevel, LivingEntity livingEntity, Field field, int n) {
        void age;
        void caster;
        ServerLevel level;
        void field2;
        if (YouziSpellFlightBridge.pulse(serverLevel, livingEntity, field.kind(), field.center(), field.damage(), n)) {
            return;
        }
        ManorSignatureSpell spell = ManorIronSpells.spell(field2.kind());
        if (field2.kind().additional()) {
            ManorThirdSpellEffects.pulse(level, (LivingEntity)caster, spell, field2.rank(), field2.center(), field2.damage(), (int)age);
            return;
        }
        Vec3 center = field2.kind() == ManorSpellRules.Kind.ORBITAL_GEARS ? caster.m_20182_() : field2.center();
        switch (field2.kind()) {
            case EFFIGY_VOLLEY: 
            case ARCANE_FUSILLADE: {
                if (!ManorSpellRules.pulse((int)age, 12, field2.kind() == ManorSpellRules.Kind.EFFIGY_VOLLEY ? 3 : 4)) break;
                ManorSpellEffects.ray(level, (LivingEntity)caster, spell, center, field2.damage(), field2.kind() == ManorSpellRules.Kind.EFFIGY_VOLLEY ? 0.9 : 0.35, false, new HashSet<UUID>());
                break;
            }
            case BREACH_SHELL: {
                if (age < 20 && age % 5 == false) {
                    ManorSpellEffects.ring(level, (LivingEntity)caster, field2.kind(), center, 3.5, 20);
                }
                if (age != 20) {
                    return;
                }
                SpellVisuals.show(level, (LivingEntity)caster, field2.kind().id, SpellVisualRules.Phase.PULSE, center);
                ManorSpellEffects.ring(level, (LivingEntity)caster, field2.kind(), center, 4.0, 32);
                level.m_8767_((ParticleOptions)ParticleTypes.f_123813_, center.f_82479_, center.f_82480_, center.f_82481_, 3, 0.3, 0.3, 0.3, 0.0);
                for (LivingEntity target : ManorSpellEffects.targets(level, (LivingEntity)caster, center, 4.0)) {
                    if (!ManorSpellEffects.hurt((LivingEntity)caster, target, spell, field2.damage())) continue;
                    BossKnockback.knockback((Entity)caster, target, 0.8, center.f_82479_ - target.m_20185_(), center.f_82481_ - target.m_20189_());
                    ManorSpellEffects.effect((LivingEntity)caster, target, MobEffects.f_19613_, 80, 0);
                }
                break;
            }
            case HEARTH_FEAST: 
            case HALO_SANCTUARY: {
                if (!ManorSpellRules.pulse((int)age, 20, 4)) {
                    return;
                }
                SpellVisuals.show(level, (LivingEntity)caster, field2.kind().id, SpellVisualRules.Phase.PULSE, center);
                ManorSpellEffects.ring(level, (LivingEntity)caster, field2.kind(), center, 4.0, 28);
                for (LivingEntity ally : ManorSpellEffects.allies(level, (LivingEntity)caster, center)) {
                    ally.m_5634_(field2.kind() == ManorSpellRules.Kind.HEARTH_FEAST ? 1.0f + (float)field2.rank() * 0.5f : 1.0f);
                    ManorSpellEffects.effect((LivingEntity)caster, ally, field2.kind() == ManorSpellRules.Kind.HEARTH_FEAST ? MobEffects.f_19618_ : MobEffects.f_19606_, field2.kind() == ManorSpellRules.Kind.HEARTH_FEAST ? 1 : 40, 0);
                }
                break;
            }
            default: {
                int count;
                int interval;
                int n2 = field2.kind() == ManorSpellRules.Kind.ORBITAL_GEARS ? 12 : (interval = field2.kind() == ManorSpellRules.Kind.VOID_ECHO ? 24 : 20);
                int n3 = field2.kind() == ManorSpellRules.Kind.VOID_ECHO ? 2 : (field2.kind() == ManorSpellRules.Kind.SHADOW_BRAND ? 3 : (count = field2.kind() == ManorSpellRules.Kind.CANDLE_PRISON ? 5 : 4));
                if (!ManorSpellRules.pulse((int)age, interval, count)) {
                    return;
                }
                SpellVisuals.show(level, (LivingEntity)caster, field2.kind().id, SpellVisualRules.Phase.PULSE, center);
                double radius = field2.kind() == ManorSpellRules.Kind.ORBITAL_GEARS ? 3.0 : (field2.kind() == ManorSpellRules.Kind.VOID_ECHO ? 2.5 : 3.5);
                ManorSpellEffects.ring(level, (LivingEntity)caster, field2.kind(), center, radius, 24);
                for (LivingEntity target : ManorSpellEffects.targets(level, (LivingEntity)caster, center, radius)) {
                    if (!ManorSpellEffects.hurt((LivingEntity)caster, target, spell, field2.damage())) continue;
                    switch (field2.kind()) {
                        case CANDLE_PRISON: {
                            Vec3 pull = center.m_82546_(target.m_20182_()).m_82542_(1.0, 0.0, 1.0);
                            if (pull.m_82556_() > 0.3) {
                                Vec3 impulse = pull.m_82541_().m_82490_(0.25);
                                BossKnockback.push((Entity)caster, (Entity)target, impulse.f_82479_, 0.0, impulse.f_82481_);
                                target.f_19864_ = true;
                            }
                            ManorSpellEffects.effect((LivingEntity)caster, target, MobEffects.f_19597_, 35, 1);
                            break;
                        }
                        case ELIXIR_RAIN: {
                            ManorSpellEffects.effect((LivingEntity)caster, target, MobEffects.f_19614_, 60, 0);
                            ManorSpellEffects.effect((LivingEntity)caster, target, MobEffects.f_19613_, 60, 0);
                            break;
                        }
                        case SHADOW_BRAND: {
                            ManorSpellEffects.effect((LivingEntity)caster, target, MobEffects.f_19610_, 40, 0);
                            ManorSpellEffects.effect((LivingEntity)caster, target, MobEffects.f_19597_, 40, 1);
                            break;
                        }
                        case WHITEOUT_EDICT: {
                            ManorSpellEffects.effect((LivingEntity)caster, target, (MobEffect)FTEffects.HEAL_BAN.get(), 60, 0);
                            ManorSpellEffects.effect((LivingEntity)caster, target, MobEffects.f_19597_, 40, 1);
                            target.m_146917_(Math.max(target.m_146888_(), 100));
                            break;
                        }
                    }
                }
            }
        }
    }

    static List<LivingEntity> allies(ServerLevel level, LivingEntity caster, Vec3 center) {
        double radius = 4.0 * BossSpellTuning.radiusScale(caster);
        ArrayList<LivingEntity> result = new ArrayList<LivingEntity>(level.m_6443_(LivingEntity.class, new AABB(center, center).m_82400_(radius), target -> {
            if (!target.m_6084_()) return false;
            if (target.m_5833_()) return false;
            if (target instanceof ArmorStand) return false;
            if (!(target.m_20238_(center) <= radius * radius)) return false;
            if (!caster.m_142582_((Entity)target)) return false;
            if (target == caster) return true;
            if (SparrowBorrowedSpells.allied(caster, (Entity)target)) {
                if (SparrowBorrowedSpells.allied(target, (Entity)caster)) return true;
            }
            if (!(target instanceof OwnableEntity)) return false;
            OwnableEntity pet = (OwnableEntity)target;
            if (!caster.m_20148_().equals(pet.m_21805_())) return false;
            return true;
        }));
        if (caster.m_20238_(center) <= radius * radius && !result.contains(caster)) {
            result.add(0, caster);
        }
        return result.stream().limit(12L).toList();
    }

    private static void ring(ServerLevel level, LivingEntity caster, ManorSpellRules.Kind kind, Vec3 center, double radius, int points) {
        radius = BossSpellTuning.radius(caster, radius);
        for (int index = 0; index < points; ++index) {
            double angle = (double)index * Math.PI * 2.0 / (double)points;
            level.m_8767_((ParticleOptions)COLORS.get((Object)kind), center.f_82479_ + Math.cos(angle) * radius, center.f_82480_ + 0.15, center.f_82481_ + Math.sin(angle) * radius, 1, 0.0, 0.02, 0.0, 0.0);
        }
    }

    private static void beam(ServerLevel level, LivingEntity caster, ManorSpellRules.Kind kind, Vec3 start, Vec3 end) {
        SpellVisuals.beam(level, caster, kind.id, start, end);
        if (SpellBeamModels.replaces(SpellVisualRules.find(kind.id), SpellVisualRules.Phase.BEAM)) {
            return;
        }
        int count = Math.max(2, Math.min(32, (int)(start.m_82554_(end) * 2.0)));
        for (int index = 0; index <= count; ++index) {
            Vec3 point = start.m_165921_(end, (double)index / (double)count);
            level.m_8767_((ParticleOptions)COLORS.get((Object)kind), point.f_82479_, point.f_82480_, point.f_82481_, 1, 0.02, 0.02, 0.02, 0.0);
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
            Player player;
            EasterBossEntity boss;
            EasterBossEntity borrowedBoss;
            boolean borrowed;
            LivingEntity caster;
            long age = level2.m_46467_() - field.started();
            Entity owner = level2.m_8791_(field.caster());
            if (age < 0L || age >= 100L || !(owner instanceof LivingEntity) || !(caster = (LivingEntity)owner).m_6084_()) {
                return true;
            }
            boolean bl = borrowed = caster instanceof EasterBossEntity && (borrowedBoss = (EasterBossEntity)caster).getVariant() == 11 && field.kind().support();
            if (!borrowed && caster instanceof EasterBossEntity && (!ManorSpellCombat.eligible(boss = (EasterBossEntity)caster) || boss.getVariant() != field.kind().variant && !ManorSparrowCasting.validField(level2, boss, field.kind(), field.delegator()))) {
                return true;
            }
            if (caster instanceof Player && (player = (Player)caster).m_5833_() || caster.m_20238_(field.center()) > 2304.0 || !level2.m_46805_(BlockPos.m_274446_((Position)field.center())) || !ManorIronSpells.spell(field.kind()).isEnabled()) {
                return true;
            }
            if (age > 0L) {
                ManorSpellEffects.pulse(level2, caster, field, (int)age);
            }
            return false;
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

    private static /* synthetic */ boolean lambda$cast$3(LivingEntity caster, ManorSignatureSpell spell, Field existing) {
        return existing.caster().equals(caster.m_20148_()) && existing.kind() == spell.kind;
    }

    private static /* synthetic */ boolean lambda$cast$1(LivingEntity caster, ManorSignatureSpell spell, Field existing) {
        return existing.caster().equals(caster.m_20148_()) && existing.kind() == spell.kind;
    }

    static {
        for (ManorSpellRules.Kind kind : ManorSpellRules.Kind.values()) {
            COLORS.put(kind, new DustParticleOptions(new Vector3f((float)(kind.color >> 16 & 0xFF) / 255.0f, (float)(kind.color >> 8 & 0xFF) / 255.0f, (float)(kind.color & 0xFF) / 255.0f), 1.2f));
        }
    }

    private record Field(UUID caster, ManorSpellRules.Kind kind, Vec3 center, int rank, float damage, long started, UUID delegator) {
    }
}

