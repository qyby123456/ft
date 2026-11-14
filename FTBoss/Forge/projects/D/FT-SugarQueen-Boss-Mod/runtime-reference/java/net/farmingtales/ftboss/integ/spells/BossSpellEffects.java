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
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.OwnableEntity
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.EntityHitResult
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
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.client.defectzero.DefectSkillSync;
import net.farmingtales.ftboss.integ.spells.BossExpandedSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossIronSpells;
import net.farmingtales.ftboss.integ.spells.BossSecondSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.SparrowBorrowedSpells;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.SpellBeamModels;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector3f;

public final class BossSpellEffects {
    private static final Map<BossSpellRules.Kind, DustParticleOptions> COLORS = new EnumMap<BossSpellRules.Kind, DustParticleOptions>(BossSpellRules.Kind.class);
    private static final Map<ServerLevel, List<SporeField>> FIELDS = new WeakHashMap<ServerLevel, List<SporeField>>();

    private BossSpellEffects() {
    }

    /*
     * WARNING - void declaration
     */
    public static boolean canTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
        Player player;
        LivingEntity caster;
        void target;
        if (SparrowBorrowedSpells.friendly((Entity)livingEntity, livingEntity2)) {
            return false;
        }
        if (target == caster || !target.m_6084_() || target.m_213877_() || target.m_5833_() || target instanceof ArmorStand || DamageSources.isFriendlyFireBetween((Entity)caster, (Entity)target) || caster.m_7307_((Entity)target) || target.m_7307_((Entity)caster)) {
            return false;
        }
        if (target instanceof Player && ((player = (Player)target).m_7500_() || player.m_5833_() || caster instanceof Player && (!((Player)caster).m_7099_(player) || caster.m_20194_() == null || !caster.m_20194_().m_129799_()))) {
            return false;
        }
        if (target instanceof OwnableEntity) {
            OwnableEntity pet = (OwnableEntity)target;
            if (caster.m_20148_().equals(pet.m_21805_()) || pet.m_269323_() != null && caster.m_7307_((Entity)pet.m_269323_())) {
                return false;
            }
        }
        return !(caster instanceof Mob) || !EntityType.m_20613_((EntityType)caster.m_6095_()).m_135827_().equals("ftboss") || !EntityType.m_20613_((EntityType)target.m_6095_()).m_135827_().equals("ftboss");
    }

    public static Vec3 aim(LivingEntity caster) {
        Vec3 clipped;
        EntityHitResult entity;
        Mob mob;
        Vec3 start = caster.m_146892_();
        Vec3 end = start.m_82549_(caster.m_20154_().m_82490_(24.0));
        if (caster instanceof Mob && (mob = (Mob)caster).m_5448_() != null && BossSpellEffects.canTarget(caster, mob.m_5448_()) && caster.m_142582_((Entity)mob.m_5448_())) {
            Vec3 offset = mob.m_5448_().m_20182_().m_82520_(0.0, 0.5, 0.0).m_82546_(start);
            end = start.m_82549_(offset.m_82541_().m_82490_(Math.min(24.0, offset.m_82553_())));
        }
        return (entity = ProjectileUtil.m_37287_((Entity)caster, (Vec3)start, (Vec3)(clipped = caster.m_9236_().m_45547_(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)caster)).m_82450_()), (AABB)caster.m_20191_().m_82369_(clipped.m_82546_(start)).m_82400_(1.0), candidate -> {
            LivingEntity living;
            return candidate instanceof LivingEntity && BossSpellEffects.canTarget(caster, living = (LivingEntity)candidate);
        }, (double)start.m_82557_(clipped))) == null ? clipped : entity.m_82450_();
    }

    static boolean visible(ServerLevel level, LivingEntity caster, Vec3 start, Vec3 end) {
        return level.m_45547_(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)caster)).m_6662_() == HitResult.Type.MISS;
    }

    static List<LivingEntity> targets(ServerLevel level, LivingEntity caster, Vec3 center, double radius) {
        return level.m_6443_(LivingEntity.class, new AABB(center, center).m_82377_(radius, 3.0, radius), target -> BossSpellEffects.canTarget(caster, target) && target.m_20182_().m_82557_(center) <= radius * radius + 4.0 && BossSpellEffects.visible(level, caster, center.m_82520_(0.0, 0.2, 0.0), target.m_146892_())).stream().sorted(Comparator.comparingDouble(target -> target.m_20238_(center))).limit(16L).toList();
    }

    public static void warning(ServerLevel level, LivingEntity caster, BossSpellRules.Kind kind, Vec3 center) {
        SpellVisuals.show(level, caster, kind.spell, SpellVisualRules.Phase.CHARGE, center);
        BossSpellEffects.ring(level, kind, center, 4.0, 16);
        level.m_8767_((ParticleOptions)COLORS.get((Object)kind), caster.m_20185_(), caster.m_20186_() + 1.2, caster.m_20189_(), 4, 0.4, 0.6, 0.4, 0.0);
    }

    static void ring(ServerLevel level, BossSpellRules.Kind kind, Vec3 center, double radius, int points) {
        for (int index = 0; index < points; ++index) {
            double angle = (double)index * Math.PI * 2.0 / (double)points;
            level.m_8767_((ParticleOptions)COLORS.get((Object)kind), center.f_82479_ + Math.cos(angle) * radius, center.f_82480_ + 0.12, center.f_82481_ + Math.sin(angle) * radius, 1, 0.0, 0.02, 0.0, 0.0);
        }
    }

    static void beam(ServerLevel level, LivingEntity caster, BossSpellRules.Kind kind, Vec3 start, Vec3 end) {
        SpellVisuals.beam(level, caster, kind.spell, start, end);
        if (SpellBeamModels.replaces(SpellVisualRules.find(kind.spell), SpellVisualRules.Phase.BEAM)) {
            return;
        }
        int points = Math.max(2, Math.min(40, (int)(start.m_82554_(end) * 2.0)));
        for (int index = 0; index <= points; ++index) {
            Vec3 point = start.m_165921_(end, (double)index / (double)points);
            level.m_8767_((ParticleOptions)COLORS.get((Object)kind), point.f_82479_, point.f_82480_, point.f_82481_, 1, 0.04, 0.04, 0.04, 0.0);
        }
    }

    public static void cast(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, int spellLevel, Vec3 center) {
        if (!(Double.isFinite(center.f_82479_) && Double.isFinite(center.f_82480_) && Double.isFinite(center.f_82481_) && !(center.m_82557_(caster.m_20182_()) > 1024.0) && level.m_46805_(BlockPos.m_274446_((Position)center)))) {
            return;
        }
        float damage = BossSpellTuning.playerPower(caster, BossSpellTuning.regularBoss(caster) ? BossSpellTuning.scaledPower(spell, caster, BossSpellTuning.signaturePower(spell.kind)) : spell.getSpellPower(spellLevel, (Entity)caster));
        SpellVisuals.show(level, caster, spell.kind.spell, SpellVisualRules.Phase.RELEASE, center);
        DefectSkillSync.released(caster, spell.kind.spell);
        if (BossSpellRules.expanded(spell.kind)) {
            BossExpandedSpellEffects.cast(level, caster, spell, center, damage);
            return;
        }
        switch (spell.kind) {
            case FROST: 
            case TEA: 
            case WINE: {
                BossSpellEffects.bloom(level, caster, spell, center, damage);
                break;
            }
            case DEFECT: {
                BossSpellEffects.chain(level, caster, spell, spellLevel, center, damage);
                break;
            }
            case COMPOST: {
                BossSpellEffects.spores(level, caster, spell, center, damage);
                List fields = FIELDS.computeIfAbsent(level, ignored -> new ArrayList());
                fields.removeIf(field -> field.caster().equals(caster.m_20148_()));
                if (fields.size() >= 64) {
                    fields.remove(0);
                }
                fields.add(new SporeField(caster.m_20148_(), center, damage, level.m_46467_()));
                break;
            }
            case APPLE: {
                BossSpellEffects.verdict(level, caster, spell, center, damage);
                break;
            }
            case TIDE: {
                BossSpellEffects.tide(level, caster, spell, center, damage);
                break;
            }
            default: {
                BossSecondSpellEffects.cast(level, caster, spell, spellLevel, center, damage);
            }
        }
    }

    private static void bloom(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Vec3 center, float damage) {
        BossSpellEffects.ring(level, spell.kind, center, 4.0, 36);
        float stolen = 0.0f;
        for (LivingEntity target : BossSpellEffects.targets(level, caster, center, 4.0)) {
            float before = target.m_21223_();
            if (!DamageSources.applyDamage((Entity)target, (float)damage, (DamageSource)spell.getDamageSource((Entity)caster))) continue;
            switch (spell.kind) {
                case FROST: {
                    target.m_147207_(new MobEffectInstance(MobEffects.f_19597_, BossSpellTuning.buffTicks(caster, MobEffects.f_19597_, 80), 2), (Entity)caster);
                    target.m_146917_(Math.max(target.m_146888_(), 100));
                    level.m_8767_((ParticleOptions)ParticleTypes.f_175821_, target.m_20185_(), target.m_20186_() + 1.0, target.m_20189_(), 18, 0.5, 0.8, 0.5, 0.02);
                    break;
                }
                case TEA: {
                    target.m_147207_(new MobEffectInstance(MobEffects.f_19597_, BossSpellTuning.buffTicks(caster, MobEffects.f_19597_, 60), 3), (Entity)caster);
                    stolen += Math.max(0.0f, before - target.m_21223_()) * 0.15f;
                    BossSpellEffects.beam(level, caster, BossSpellRules.Kind.TEA, target.m_146892_(), caster.m_146892_());
                    break;
                }
                case WINE: {
                    Vec3 pull = center.m_82546_(target.m_20182_()).m_82542_(1.0, 0.0, 1.0);
                    if (pull.m_82556_() > 0.04) {
                        BossKnockback.push((Entity)target, pull.m_82541_().f_82479_ * 0.6, 0.12, pull.m_82541_().f_82481_ * 0.6);
                    }
                    target.f_19864_ = true;
                    target.m_147207_(new MobEffectInstance(MobEffects.f_19613_, BossSpellTuning.buffTicks(caster, MobEffects.f_19613_, 100), 0), (Entity)caster);
                    break;
                }
            }
        }
        if (stolen > 0.0f) {
            caster.m_5634_(Math.min(6.0f, stolen));
        }
        if (spell.kind == BossSpellRules.Kind.WINE) {
            for (int height = 0; height < 4; ++height) {
                BossSpellEffects.ring(level, BossSpellRules.Kind.WINE, center.m_82520_(0.0, (double)height * 0.45, 0.0), 3.0 - (double)height * 0.55, 16);
            }
        }
    }

    private static void chain(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, int spellLevel, Vec3 center, float damage) {
        HashSet<UUID> hit = new HashSet<UUID>();
        Vec3 previous = caster.m_146892_();
        Vec3 search = center;
        for (int jump = 0; jump < BossSpellRules.chainTargets(spellLevel); ++jump) {
            Vec3 origin = previous;
            LivingEntity target = BossSpellEffects.targets(level, caster, search, jump == 0 ? 4.0 : 6.0).stream().filter(candidate -> !hit.contains(candidate.m_20148_()) && BossSpellEffects.visible(level, caster, origin, candidate.m_146892_())).findFirst().orElse(null);
            if (target == null) break;
            hit.add(target.m_20148_());
            BossSpellEffects.beam(level, caster, BossSpellRules.Kind.DEFECT, previous, target.m_146892_());
            DamageSources.applyDamage((Entity)target, (float)(damage * BossSpellRules.chainScale(jump)), (DamageSource)spell.getDamageSource((Entity)caster));
            level.m_8767_((ParticleOptions)ParticleTypes.f_175830_, target.m_20185_(), target.m_20186_() + 1.0, target.m_20189_(), 14, 0.4, 0.6, 0.4, 0.04);
            previous = target.m_146892_();
            search = target.m_20182_();
        }
    }

    private static void spores(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Vec3 center, float damage) {
        SpellVisuals.show(level, caster, spell.kind.spell, SpellVisualRules.Phase.PULSE, center);
        BossSpellEffects.ring(level, BossSpellRules.Kind.COMPOST, center, 4.0, 24);
        level.m_8767_((ParticleOptions)COLORS.get((Object)BossSpellRules.Kind.COMPOST), center.f_82479_, center.f_82480_ + 0.7, center.f_82481_, 32, 2.0, 0.65, 2.0, 0.0);
        for (LivingEntity target : BossSpellEffects.targets(level, caster, center, 4.0)) {
            if (!DamageSources.applyDamage((Entity)target, (float)damage, (DamageSource)spell.getDamageSource((Entity)caster))) continue;
            target.m_147207_(new MobEffectInstance(MobEffects.f_19614_, BossSpellTuning.buffTicks(caster, MobEffects.f_19614_, 80), 0), (Entity)caster);
        }
    }

    private static void verdict(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Vec3 center, float damage) {
        Vec3 start = caster.m_146892_();
        Vec3 direction = center.m_82546_(start).m_82541_();
        HashSet<UUID> hit = new HashSet<UUID>();
        for (int ray = -1; ray <= 1; ++ray) {
            Vec3 end = level.m_45547_(new ClipContext(start, start.m_82549_(direction.m_82524_((float)ray * 0.16f).m_82490_(24.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)caster)).m_82450_();
            Vec3 segment = end.m_82546_(start);
            BossSpellEffects.beam(level, caster, BossSpellRules.Kind.APPLE, start, end);
            List<LivingEntity> candidates = level.m_6443_(LivingEntity.class, new AABB(start, end).m_82400_(1.2), target -> BossSpellEffects.canTarget(caster, target)).stream().sorted(Comparator.comparingDouble(arg_0 -> ((LivingEntity)caster).m_20280_(arg_0))).limit(16L).toList();
            for (LivingEntity target2 : candidates) {
                Vec3 body = target2.m_20191_().m_82399_();
                double progress = segment.m_82556_() < 0.01 ? -1.0 : body.m_82546_(start).m_82526_(segment) / segment.m_82556_();
                if (progress < 0.0 || progress > 1.0 || body.m_82557_(start.m_82549_(segment.m_82490_(progress))) > 1.44 || !BossSpellEffects.visible(level, caster, start, body) || !hit.add(target2.m_20148_()) || !DamageSources.applyDamage((Entity)target2, (float)damage, (DamageSource)spell.getDamageSource((Entity)caster))) continue;
                target2.m_147207_(new MobEffectInstance(MobEffects.f_19619_, BossSpellTuning.buffTicks(caster, MobEffects.f_19619_, 100), 0), (Entity)caster);
                caster.m_147207_(new MobEffectInstance(MobEffects.f_19617_, BossSpellTuning.buffTicks(caster, MobEffects.f_19617_, 80), 0), (Entity)caster);
            }
        }
    }

    private static void tide(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Vec3 center, float damage) {
        Vec3 direction = center.m_82546_(caster.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
        if (direction.m_82556_() < 0.01) {
            direction = caster.m_20154_().m_82542_(1.0, 0.0, 1.0).m_82541_();
        }
        for (int step = 1; step <= 6; ++step) {
            double distance = step * 2;
            Vec3 middle = caster.m_20182_().m_82549_(direction.m_82490_(distance)).m_82520_(0.0, 0.7, 0.0);
            Vec3 side = new Vec3(-direction.f_82481_, 0.0, direction.f_82479_).m_82490_(1.2 + distance * 0.45);
            BossSpellEffects.beam(level, caster, BossSpellRules.Kind.TIDE, middle.m_82546_(side), middle.m_82549_(side));
        }
        for (LivingEntity target : BossSpellEffects.targets(level, caster, caster.m_20182_(), 14.0)) {
            Vec3 offset = target.m_20182_().m_82546_(caster.m_20182_());
            double forward = offset.f_82479_ * direction.f_82479_ + offset.f_82481_ * direction.f_82481_;
            double sideways = offset.f_82479_ * -direction.f_82481_ + offset.f_82481_ * direction.f_82479_;
            if (!BossSpellRules.cone(forward, sideways) || Math.abs(offset.f_82480_) > 3.0 || !DamageSources.applyDamage((Entity)target, (float)damage, (DamageSource)spell.getDamageSource((Entity)caster))) continue;
            BossKnockback.knockback(target, 0.9, -direction.f_82479_, -direction.f_82481_);
            target.m_147207_(new MobEffectInstance(MobEffects.f_19597_, BossSpellTuning.buffTicks(caster, MobEffects.f_19597_, 60), 1), (Entity)caster);
            target.m_20095_();
            level.m_8767_((ParticleOptions)ParticleTypes.f_123769_, target.m_20185_(), target.m_20186_() + 0.5, target.m_20189_(), 20, 0.6, 0.6, 0.6, 0.05);
        }
    }

    @SubscribeEvent
    public static void tickFields(TickEvent.LevelTickEvent event) {
        Level level;
        if (event.phase != TickEvent.Phase.END || !((level = event.level) instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        List<SporeField> fields = FIELDS.get(level2);
        if (fields == null) {
            return;
        }
        fields.removeIf(field -> {
            LivingEntity caster;
            long elapsed = level2.m_46467_() - field.started();
            Entity entity = level2.m_8791_(field.caster());
            if (!(elapsed >= 0L && elapsed < 60L && entity instanceof LivingEntity && (caster = (LivingEntity)entity).m_6084_() && level2.m_46805_(BlockPos.m_274446_((Position)field.center())))) {
                return true;
            }
            if (elapsed > 0L && BossSpellRules.sporePulse((int)elapsed)) {
                BossSpellEffects.spores(level2, caster, BossIronSpells.spell(BossSpellRules.Kind.COMPOST), field.center(), field.damage());
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

    static {
        for (BossSpellRules.Kind kind : BossSpellRules.Kind.values()) {
            COLORS.put(kind, new DustParticleOptions(new Vector3f((float)(kind.color >> 16 & 0xFF) / 255.0f, (float)(kind.color >> 8 & 0xFF) / 255.0f, (float)(kind.color & 0xFF) / 255.0f), 1.3f));
        }
    }

    private record SporeField(UUID caster, Vec3 center, float damage, long started) {
    }
}

