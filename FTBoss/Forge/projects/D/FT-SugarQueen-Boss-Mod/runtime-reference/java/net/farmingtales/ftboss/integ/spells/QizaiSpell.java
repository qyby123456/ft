/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.config.DefaultConfig
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.CastSource
 *  io.redspace.ironsspellbooks.api.spells.CastType
 *  io.redspace.ironsspellbooks.api.spells.ICastData
 *  io.redspace.ironsspellbooks.api.spells.SpellRarity
 *  io.redspace.ironsspellbooks.damage.DamageSources
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Position
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.entity.QizaiSigilEntity;
import net.farmingtales.ftboss.entity.QizaiStarEntity;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.FullPowerBossSpell;
import net.farmingtales.ftboss.integ.spells.QizaiPlayerSpells;
import net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem;
import net.farmingtales.ftboss.util.QizaiRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class QizaiSpell
extends FullPowerBossSpell {
    public final QizaiPlayerSpells.Kind kind;
    private final DefaultConfig config;

    public QizaiSpell(QizaiPlayerSpells.Kind kind) {
        super(kind.mana, kind.damage, 0, 0);
        this.kind = kind;
        this.castTime = kind.castTicks;
        this.config = new DefaultConfig().setMinRarity(SpellRarity.LEGENDARY).setMaxLevel(1).setSchoolResource(new ResourceLocation("irons_spellbooks", "ender")).setCooldownSeconds((double)kind.cooldown).setAllowCrafting(true).build();
    }

    public ResourceLocation getSpellResource() {
        return new ResourceLocation("ftboss", this.kind.id);
    }

    public DefaultConfig getDefaultConfig() {
        return this.config;
    }

    public CastType getCastType() {
        return CastType.LONG;
    }

    public boolean allowLooting() {
        return false;
    }

    public float getSpellPower(int level, Entity caster) {
        float f;
        float power = super.getSpellPower(level, caster);
        if (caster instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)caster;
            f = BossSpellTuning.playerPower(living, power);
        } else {
            f = power;
        }
        return f;
    }

    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.f_144243_);
    }

    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(this.kind == QizaiPlayerSpells.Kind.STEP ? SoundEvents.f_11852_ : SoundEvents.f_144242_);
    }

    public List<MutableComponent> getUniqueInfo(int level, LivingEntity caster) {
        ArrayList<MutableComponent> lines = new ArrayList<MutableComponent>();
        lines.add(Component.m_237115_((String)("spell.ftboss." + this.kind.id + ".effect")));
        if (this.kind.damage > 0) {
            lines.add(Component.m_237110_((String)"ui.ftboss.manor.pulse_damage", (Object[])new Object[]{Math.round(this.getSpellPower(level, (Entity)caster))}));
        }
        lines.add(Component.m_237115_((String)"ui.ftboss.signature.full_power"));
        lines.add(Component.m_237110_((String)"ui.ftboss.signature.source", (Object[])new Object[]{Component.m_237115_((String)"ui.ftboss.manor.owner.11")}));
        return lines;
    }

    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity caster, MagicData data) {
        if (!caster.m_6084_() || caster.m_5833_() || !this.isEnabled()) {
            return false;
        }
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            Prepared prepared = this.prepare(server, caster);
            if (prepared == null) {
                return false;
            }
            data.setAdditionalCastData((ICastData)prepared);
        }
        return true;
    }

    /*
     * Unable to fully structure code
     */
    public void onServerPreCast(Level level, int spellLevel, LivingEntity caster, MagicData data) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        server = (ServerLevel)level;
        var8_6 = data.getAdditionalCastData();
        if (!(var8_6 instanceof Prepared)) ** GOTO lbl-1000
        current = (Prepared)var8_6;
        if (current.kind == this.kind) {
            v0 = current;
        } else lbl-1000:
        // 2 sources

        {
            v0 = prepared = this.prepare(server, caster);
        }
        if (prepared == null) {
            return;
        }
        data.setAdditionalCastData((ICastData)prepared);
        duration = this.getEffectiveCastTime(spellLevel, caster);
        if (prepared.sigils.isEmpty()) {
            for (Vec3 mark : prepared.marks) {
                sigil = new QizaiSigilEntity(server, caster, mark, this.kind.cue, duration);
                if (!server.m_7967_((Entity)sigil)) continue;
                prepared.sigils.add(sigil);
            }
        }
        if (this.kind == QizaiPlayerSpells.Kind.PRAYER) {
            QizaiPrayerStaffItem.animate(caster, true);
        }
        super.onServerPreCast(level, spellLevel, caster, data);
    }

    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData data) {
        Prepared prepared;
        ServerLevel server;
        block18: {
            block17: {
                ICastData iCastData;
                if (!(level instanceof ServerLevel)) break block17;
                server = (ServerLevel)level;
                if (!caster.m_6084_() || caster.m_5833_() || !this.isEnabled() || !((iCastData = data.getAdditionalCastData()) instanceof Prepared)) break block17;
                prepared = (Prepared)iCastData;
                if (prepared.kind == this.kind && !prepared.released && !prepared.cancelled && prepared.caster == caster && caster.m_9236_() == server) break block18;
            }
            return;
        }
        prepared.released = true;
        float damage = this.getSpellPower(spellLevel, (Entity)caster);
        if (this.kind == QizaiPlayerSpells.Kind.STAR) {
            origin = caster.m_146892_().m_82492_(0.0, 0.15, 0.0);
            Vec3 direction = prepared.aim.m_82546_((Vec3)origin).m_82541_();
            if (direction.m_82556_() < 0.01) {
                direction = caster.m_20154_();
            }
            for (int index = -1; index <= 1; ++index) {
                QizaiStarEntity star = new QizaiStarEntity(server, caster, (Vec3)origin, damage);
                star.m_20256_(direction.m_82524_((float)index * 0.19f).m_82490_(0.8));
                server.m_7967_((Entity)star);
            }
        } else if (this.kind == QizaiPlayerSpells.Kind.STEP) {
            if (QizaiSpell.safeStep(server, caster, prepared.aim)) {
                origin = new QizaiSigilEntity(server, caster, caster.m_20182_(), 4, 12);
                ((QizaiSigilEntity)((Object)origin)).release();
                server.m_7967_((Entity)origin);
                ManorSafeTeleport.move(server, (Entity)caster, prepared.aim);
            }
        } else {
            List targets = server.m_6443_(LivingEntity.class, new AABB(prepared.aim, prepared.aim).m_82377_(12.0, 4.0, 12.0), target -> BossSpellEffects.canTarget(caster, target));
            targets.sort(Comparator.comparingDouble(target -> target.m_20238_(prepared.aim)));
            int hitCount = 0;
            for (LivingEntity target2 : targets) {
                for (Vec3 mark : prepared.marks) {
                    if (!server.m_46805_(BlockPos.m_274446_((Position)mark)) || !QizaiRules.inside(Math.pow(target2.m_20185_() - mark.f_82479_, 2.0) + Math.pow(target2.m_20189_() - mark.f_82481_, 2.0), target2.m_20186_() - mark.f_82480_, this.kind.cue) || !BossSpellEffects.visible(server, caster, mark.m_82520_(0.0, 1.5, 0.0), target2.m_146892_())) continue;
                    DamageSources.applyDamage((Entity)target2, (float)damage, (DamageSource)this.getDamageSource((Entity)caster));
                    ++hitCount;
                    break;
                }
                if (hitCount < 12) continue;
                break;
            }
        }
        for (QizaiSigilEntity sigil : prepared.sigils) {
            if (sigil.m_213877_()) continue;
            sigil.release();
        }
        if (this.kind == QizaiPlayerSpells.Kind.PRAYER) {
            QizaiPrayerStaffItem.animate(caster, false);
        }
        super.onCast(level, spellLevel, caster, source, data);
    }

    public void onServerCastComplete(Level level, int spellLevel, LivingEntity caster, MagicData data, boolean cancelled) {
        ICastData iCastData = data.getAdditionalCastData();
        if (iCastData instanceof Prepared) {
            Prepared prepared = (Prepared)iCastData;
            if (prepared.kind == this.kind) {
                prepared.reset();
            }
        }
        super.onServerCastComplete(level, spellLevel, caster, data, cancelled);
    }

    private Prepared prepare(ServerLevel level, LivingEntity caster) {
        if (this.kind == QizaiPlayerSpells.Kind.STEP) {
            Vec3 forward = caster.m_20154_().m_82542_(1.0, 0.0, 1.0).m_82541_();
            for (double distance = 4.5; distance >= 1.5; distance -= 0.5) {
                Vec3 destination = ManorSafeTeleport.find(level, (Entity)caster, caster.m_20182_().m_82549_(forward.m_82490_(distance)), 0);
                if (destination == null || !QizaiSpell.safeStep(level, caster, destination)) continue;
                return new Prepared(caster, this.kind, destination, List.of(destination));
            }
            return null;
        }
        Vec3 aim = BossSpellEffects.aim(caster);
        if (this.kind == QizaiPlayerSpells.Kind.STAR) {
            return new Prepared(caster, this.kind, aim, List.of());
        }
        Vec3 center = QizaiSpell.ground(level, caster, aim);
        if (center == null) {
            return null;
        }
        ArrayList<Vec3> marks = new ArrayList<Vec3>();
        marks.add(center);
        if (this.kind == QizaiPlayerSpells.Kind.PRAYER) {
            Vec3 forward = center.m_82546_(caster.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
            if (forward.m_82556_() < 0.01) {
                forward = new Vec3(0.0, 0.0, 1.0);
            }
            Vec3 side = new Vec3(-forward.f_82481_, 0.0, forward.f_82479_).m_82490_(7.0);
            for (int sign : new int[]{-1, 1}) {
                Vec3 mark = QizaiSpell.ground(level, caster, center.m_82549_(side.m_82490_((double)sign)));
                if (mark == null) continue;
                marks.add(mark);
            }
        }
        return new Prepared(caster, this.kind, center, List.copyOf(marks));
    }

    private static Vec3 ground(ServerLevel level, LivingEntity caster, Vec3 aim) {
        if (!level.m_46805_(BlockPos.m_274446_((Position)aim)) || !level.m_6857_().m_61937_(BlockPos.m_274446_((Position)aim))) {
            return null;
        }
        BlockHitResult hit = level.m_45547_(new ClipContext(aim.m_82520_(0.0, 0.5, 0.0), aim.m_82520_(0.0, -8.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)caster));
        if (hit.m_6662_() == HitResult.Type.MISS || hit.m_82434_() != Direction.UP) {
            return null;
        }
        Vec3 point = hit.m_82450_().m_82520_(0.0, 0.025, 0.0);
        return BossSpellEffects.visible(level, caster, caster.m_146892_(), point.m_82520_(0.0, 0.1, 0.0)) ? point : null;
    }

    private static boolean safeStep(ServerLevel level, LivingEntity caster, Vec3 point) {
        Vec3 offset = point.m_82546_(caster.m_20182_());
        if (offset.m_82556_() > 25.0 || offset.m_82556_() < 1.0 || Math.abs(offset.f_82480_) > 1.5 || !level.m_46805_(BlockPos.m_274446_((Position)point))) {
            return false;
        }
        Vec3 floor = ManorSafeTeleport.find(level, (Entity)caster, point, 0);
        if (floor == null || floor.m_82557_(point) > 0.01) {
            return false;
        }
        for (int step = 1; step <= 20; ++step) {
            AABB body = caster.m_20191_().m_82383_(offset.m_82490_((double)step / 20.0));
            if (level.m_6857_().m_61935_(body) && level.m_45756_((Entity)caster, body) && !level.m_46855_(body)) continue;
            return false;
        }
        return true;
    }

    private static final class Prepared
    implements ICastData {
        final LivingEntity caster;
        final QizaiPlayerSpells.Kind kind;
        final Vec3 aim;
        final List<Vec3> marks;
        final List<QizaiSigilEntity> sigils = new ArrayList<QizaiSigilEntity>();
        boolean released;
        boolean cancelled;

        Prepared(LivingEntity caster, QizaiPlayerSpells.Kind kind, Vec3 aim, List<Vec3> marks) {
            this.caster = caster;
            this.kind = kind;
            this.aim = aim;
            this.marks = marks;
        }

        public void reset() {
            boolean bl = this.cancelled = !this.released;
            if (!this.released) {
                for (QizaiSigilEntity sigil : this.sigils) {
                    sigil.m_146870_();
                }
            }
            if (this.kind == QizaiPlayerSpells.Kind.PRAYER) {
                QizaiPrayerStaffItem.animate(this.caster, false);
            }
        }
    }
}

