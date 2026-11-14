/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.damage.DamageSources
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.OwnableEntity
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ai.LemonSacrifice;
import net.farmingtales.ftboss.integ.spells.BossIronSpells;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BossSecondSpellEffects {
    private static final Map<ServerLevel, List<Pending>> PENDING = new WeakHashMap<ServerLevel, List<Pending>>();

    private BossSecondSpellEffects() {
    }

    public static void cast(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, int spellLevel, Vec3 center, float power) {
        switch (spell.kind) {
            case FROST_LANCE: {
                BossSecondSpellEffects.lance(level, caster, spell, center, power);
                break;
            }
            case TEA_SANCTUARY: {
                BossSecondSpellEffects.sanctuary(level, caster, power);
                break;
            }
            case WINE_BOMB: 
            case TIDE_GEYSER: {
                List pending = PENDING.computeIfAbsent(level, ignored -> new ArrayList());
                pending.removeIf(entry -> entry.caster().equals(caster.m_20148_()) && entry.kind() == spell.kind);
                if (pending.size() >= 64) {
                    pending.remove(0);
                }
                pending.add(new Pending(caster.m_20148_(), spell.kind, center, power, level.m_46467_()));
                BossSpellEffects.ring(level, spell.kind, center, spell.kind == BossSpellRules.Kind.WINE_BOMB ? 5.0 : 2.5, 24);
                break;
            }
            case DEFECT_PULSE: {
                BossSecondSpellEffects.discharge(level, caster, spell, power);
                break;
            }
            case COMPOST_REAP: {
                BossSecondSpellEffects.harvest(level, caster, spell, center, power);
                break;
            }
            case APPLE_AEGIS: {
                int shield = BossSpellRules.shield(power);
                caster.m_147207_(new MobEffectInstance(MobEffects.f_19617_, BossSpellTuning.buffTicks(caster, MobEffects.f_19617_, 160), shield / 4 - 1), (Entity)caster);
                caster.m_147207_(new MobEffectInstance(MobEffects.f_19606_, BossSpellTuning.buffTicks(caster, MobEffects.f_19606_, 80), 0), (Entity)caster);
                for (int height = 0; height < 3; ++height) {
                    BossSpellEffects.ring(level, spell.kind, caster.m_20182_().m_82520_(0.0, (double)height * 0.65, 0.0), 1.4, 16);
                }
                level.m_8767_((ParticleOptions)ParticleTypes.f_123809_, caster.m_20185_(), caster.m_20186_() + 1.0, caster.m_20189_(), 24, 0.8, 0.8, 0.8, 0.1);
                break;
            }
            default: {
                throw new IllegalArgumentException("Not a secondary spell: " + spell.kind);
            }
        }
    }

    private static void lance(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Vec3 center, float damage) {
        Vec3 start = caster.m_146892_();
        Vec3 direction = center.m_82546_(start).m_82541_();
        if (direction.m_82556_() < 0.01) {
            direction = caster.m_20154_();
        }
        Vec3 end = level.m_45547_(new ClipContext(start, start.m_82549_(direction.m_82490_(24.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)caster)).m_82450_();
        Vec3 segment = end.m_82546_(start);
        BossSpellEffects.beam(level, caster, spell.kind, start, end);
        int hits = 0;
        List<LivingEntity> targets = level.m_6443_(LivingEntity.class, new AABB(start, end).m_82400_(0.8), target -> BossSpellEffects.canTarget(caster, target)).stream().sorted(Comparator.comparingDouble(arg_0 -> ((LivingEntity)caster).m_20280_(arg_0))).limit(16L).toList();
        for (LivingEntity target2 : targets) {
            boolean frozen;
            Vec3 body = target2.m_20191_().m_82399_();
            double progress = segment.m_82556_() < 0.01 ? -1.0 : body.m_82546_(start).m_82526_(segment) / segment.m_82556_();
            if (progress < 0.0 || progress > 1.0 || body.m_82557_(start.m_82549_(segment.m_82490_(progress))) > 0.64 || !BossSpellEffects.visible(level, caster, start, body)) continue;
            boolean bl = frozen = target2.m_146888_() > 0;
            if (DamageSources.applyDamage((Entity)target2, (float)BossSpellRules.bonusDamage(damage, frozen), (DamageSource)spell.getDamageSource((Entity)caster))) {
                if (frozen) {
                    target2.m_146917_(0);
                }
                target2.m_147207_(new MobEffectInstance(MobEffects.f_19597_, BossSpellTuning.buffTicks(caster, MobEffects.f_19597_, 40), 1), (Entity)caster);
                level.m_8767_((ParticleOptions)ParticleTypes.f_175821_, body.f_82479_, body.f_82480_, body.f_82481_, 16, 0.3, 0.5, 0.3, 0.08);
            }
            if (++hits < 4) continue;
            break;
        }
    }

    public static boolean canHelp(LivingEntity caster, LivingEntity target) {
        if (!target.m_6084_() || target.m_213877_() || target.m_5833_()) {
            return false;
        }
        if (target == caster || caster.m_7307_((Entity)target) || target.m_7307_((Entity)caster)) {
            return true;
        }
        if (target instanceof OwnableEntity) {
            OwnableEntity pet = (OwnableEntity)target;
            if (caster.m_20148_().equals(pet.m_21805_())) {
                return true;
            }
        }
        return caster instanceof Mob && EntityType.m_20613_((EntityType)caster.m_6095_()).m_135827_().equals("ftboss") && EntityType.m_20613_((EntityType)target.m_6095_()).m_135827_().equals("ftboss");
    }

    private static void sanctuary(ServerLevel level, LivingEntity caster, float healing) {
        ArrayList<LivingEntity> allies = new ArrayList<LivingEntity>();
        allies.add(caster);
        allies.addAll(level.m_6443_(LivingEntity.class, caster.m_20191_().m_82400_(4.0), target -> target != caster && BossSecondSpellEffects.canHelp(caster, target) && caster.m_20280_((Entity)target) <= 16.0 && BossSpellEffects.visible(level, caster, caster.m_146892_(), target.m_146892_())).stream().sorted(Comparator.comparingDouble(arg_0 -> ((LivingEntity)caster).m_20280_(arg_0))).limit(15L).toList());
        BossSpellEffects.ring(level, BossSpellRules.Kind.TEA_SANCTUARY, caster.m_20182_(), 4.0, 32);
        for (LivingEntity target2 : allies) {
            target2.m_5634_(healing);
            target2.m_21195_(MobEffects.f_19614_);
            target2.m_147207_(new MobEffectInstance(MobEffects.f_19606_, BossSpellTuning.buffTicks(caster, MobEffects.f_19606_, 100), 0), (Entity)caster);
            level.m_8767_((ParticleOptions)ParticleTypes.f_123748_, target2.m_20185_(), target2.m_20186_() + 1.0, target2.m_20189_(), 12, 0.4, 0.6, 0.4, 0.0);
            if (target2 == caster) continue;
            BossSpellEffects.beam(level, caster, BossSpellRules.Kind.TEA_SANCTUARY, caster.m_146892_(), target2.m_146892_());
        }
    }

    private static void discharge(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, float damage) {
        for (int radius = 1; radius <= 5; radius += 2) {
            BossSpellEffects.ring(level, spell.kind, caster.m_20182_(), radius, 24);
        }
        for (LivingEntity target : BossSpellEffects.targets(level, caster, caster.m_20182_(), 5.0)) {
            if (!DamageSources.applyDamage((Entity)target, (float)damage, (DamageSource)spell.getDamageSource((Entity)caster))) continue;
            Vec3 direction = target.m_20182_().m_82546_(caster.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
            BossKnockback.knockback(target, 0.8, -direction.f_82479_, -direction.f_82481_);
            target.m_147207_(new MobEffectInstance(MobEffects.f_19597_, BossSpellTuning.buffTicks(caster, MobEffects.f_19597_, 50), 1), (Entity)caster);
            level.m_8767_((ParticleOptions)ParticleTypes.f_175830_, target.m_20185_(), target.m_20186_() + 1.0, target.m_20189_(), 12, 0.4, 0.6, 0.4, 0.1);
        }
    }

    private static void harvest(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Vec3 center, float damage) {
        BossSpellEffects.ring(level, spell.kind, center, 4.0, 32);
        int harvested = 0;
        for (LivingEntity target : BossSpellEffects.targets(level, caster, center, 4.0)) {
            boolean poisoned;
            if (!DamageSources.applyDamage((Entity)target, (float)BossSpellRules.bonusDamage(damage, poisoned = target.m_21023_(MobEffects.f_19614_)), (DamageSource)spell.getDamageSource((Entity)caster))) continue;
            if (poisoned) {
                target.m_21195_(MobEffects.f_19614_);
                ++harvested;
                BossSpellEffects.beam(level, caster, spell.kind, target.m_146892_(), caster.m_146892_());
            }
            level.m_8767_((ParticleOptions)ParticleTypes.f_175833_, target.m_20185_(), target.m_20186_() + 1.0, target.m_20189_(), 16, 0.5, 0.6, 0.5, 0.0);
        }
        if (harvested > 0) {
            caster.m_5634_((float)Math.min(4, harvested));
        }
    }

    private static void detonate(ServerLevel level, LivingEntity caster, Pending entry) {
        BossSignatureSpell spell = BossIronSpells.spell(entry.kind());
        boolean wine = entry.kind() == BossSpellRules.Kind.WINE_BOMB;
        Vec3 center = entry.center();
        SpellVisuals.show(level, caster, entry.kind().spell, SpellVisualRules.Phase.PULSE, center);
        BossSpellEffects.ring(level, entry.kind(), center, wine ? 5.0 : 2.5, 32);
        if (wine) {
            level.m_8767_((ParticleOptions)ParticleTypes.f_123744_, center.f_82479_, center.f_82480_ + 0.6, center.f_82481_, 48, 1.5, 0.8, 1.5, 0.08);
            level.m_6263_(null, center.f_82479_, center.f_82480_, center.f_82481_, SoundEvents.f_11913_, SoundSource.PLAYERS, 0.8f, 1.25f);
        } else {
            for (int height = 0; height < 5; ++height) {
                level.m_8767_((ParticleOptions)ParticleTypes.f_123769_, center.f_82479_, center.f_82480_ + (double)height * 0.65, center.f_82481_, 12, 0.65, 0.2, 0.65, 0.08);
            }
            level.m_6263_(null, center.f_82479_, center.f_82480_, center.f_82481_, SoundEvents.f_11917_, SoundSource.PLAYERS, 1.0f, 0.7f);
        }
        for (LivingEntity target : BossSpellEffects.targets(level, caster, center, wine ? 5.0 : 2.5)) {
            float damage;
            if (!DamageSources.applyDamage((Entity)target, (float)(damage = entry.power() * (wine ? BossSpellRules.blastScale(target.m_20182_().m_82554_(center)) : 1.0f)), (DamageSource)spell.getDamageSource((Entity)caster))) continue;
            if (wine) {
                target.m_20254_(3);
                Vec3 direction = target.m_20182_().m_82546_(center).m_82542_(1.0, 0.0, 1.0).m_82541_();
                BossKnockback.knockback(target, 0.6, -direction.f_82479_, -direction.f_82481_);
                continue;
            }
            BossKnockback.push((Entity)target, 0.0, 0.65, 0.0);
            target.f_19864_ = true;
            target.m_147207_(new MobEffectInstance(MobEffects.f_19597_, BossSpellTuning.buffTicks(caster, MobEffects.f_19597_, 60), 2), (Entity)caster);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        Level level;
        if (event.phase != TickEvent.Phase.END || !((level = event.level) instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        List<Pending> pending = PENDING.get(level2);
        if (pending == null) {
            return;
        }
        pending.removeIf(entry -> {
            LivingEntity caster;
            Entity patt11804$temp;
            long elapsed = level2.m_46467_() - entry.started();
            if (!(elapsed >= 0L && elapsed <= 40L && (patt11804$temp = level2.m_8791_(entry.caster())) instanceof LivingEntity && (caster = (LivingEntity)patt11804$temp).m_6084_() && LemonSacrifice.allowsSpellEffect(caster) && !(caster.m_20238_(entry.center()) > 2304.0) && level2.m_46805_(BlockPos.m_274446_((Position)entry.center())) && BossIronSpells.spell(entry.kind()).isEnabled())) {
                return true;
            }
            if (elapsed >= (long)BossSpellRules.delay(entry.kind())) {
                BossSecondSpellEffects.detonate(level2, caster, entry);
                return true;
            }
            if (elapsed % 4L == 0L) {
                BossSpellEffects.ring(level2, entry.kind(), entry.center(), (entry.kind() == BossSpellRules.Kind.WINE_BOMB ? 5.0 : 2.5) * (1.0 - (double)elapsed / (double)BossSpellRules.delay(entry.kind())), 16);
            }
            return false;
        });
        if (pending.isEmpty()) {
            PENDING.remove(level2);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        PENDING.remove(event.getLevel());
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        PENDING.clear();
    }

    private record Pending(UUID caster, BossSpellRules.Kind kind, Vec3 center, float power, long started) {
    }
}

