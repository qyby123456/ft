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
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import java.util.List;
import java.util.Optional;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.dandaniron.IronHooks;
import net.farmingtales.ftboss.doramotion.DoraMotionSync;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.FullPowerBossSpell;
import net.farmingtales.ftboss.integ.spells.ManorSpellEffects;
import net.farmingtales.ftboss.integ.spells.QizaiIronSpells;
import net.farmingtales.ftboss.integ.spells.youzifacing.YouziCastFacing;
import net.farmingtales.ftboss.lemoncasting.CastingHooks;
import net.farmingtales.ftboss.skill.QizaiConstellation;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.zheergenphantom.CastReadability;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ManorSignatureSpell
extends FullPowerBossSpell {
    public final ManorSpellRules.Kind kind;
    private final DefaultConfig config;

    public ManorSignatureSpell(ManorSpellRules.Kind kind) {
        super(kind.mana, kind.damage, 8, kind.support() ? 0 : 2);
        this.kind = kind;
        this.config = new DefaultConfig().setMinRarity(SpellRarity.LEGENDARY).setMaxLevel(1).setSchoolResource(new ResourceLocation("irons_spellbooks", kind.school)).setCooldownSeconds((double)kind.cooldown).setAllowCrafting(true).build();
        this.castTime = kind.castTicks;
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

    public int getEffectiveCastTime(int level, LivingEntity caster) {
        int n;
        EasterBossEntity qizai;
        int duration;
        int ticks = super.getEffectiveCastTime(level, caster);
        int n2 = duration = BossSpellTuning.manorBoss(caster) && !QizaiIronSpells.matches(this.kind) ? BossSpellTuning.bossCast(ticks) : ticks;
        if (caster instanceof EasterBossEntity && (qizai = (EasterBossEntity)caster).getVariant() == 11) {
            duration = Math.max(1, (duration + 1) / 2);
        }
        if (caster instanceof EasterBossEntity) {
            EasterBossEntity boss = (EasterBossEntity)caster;
            n = boss.getChallengeDifficulty().acceleratedTicks(duration);
        } else {
            n = duration;
        }
        return ManorBalanceHooks.castTicks(CastReadability.ironCaster(n, caster), caster);
    }

    public boolean allowLooting() {
        return false;
    }

    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.f_11739_);
    }

    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(switch (this.kind.school) {
            case "ice" -> SoundEvents.f_11983_;
            case "fire" -> SoundEvents.f_11705_;
            case "lightning" -> SoundEvents.f_12521_;
            case "ender" -> SoundEvents.f_11852_;
            default -> SoundEvents.f_144243_;
        });
    }

    public List<MutableComponent> getUniqueInfo(int level, LivingEntity caster) {
        return List.of(Component.m_237115_((String)("spell.ftboss." + this.kind.id + ".effect")), this.kind.support() ? Component.m_237115_((String)"ui.ftboss.manor.support") : Component.m_237110_((String)"ui.ftboss.manor.pulse_damage", (Object[])new Object[]{Math.round(this.getSpellPower(level, (Entity)caster))}), Component.m_237115_((String)"ui.ftboss.signature.full_power"), Component.m_237110_((String)"ui.ftboss.signature.source", (Object[])new Object[]{Component.m_237115_((String)("ui.ftboss.manor.owner." + this.kind.variant))}));
    }

    /*
     * WARNING - void declaration
     */
    public void onServerPreCast(Level level, int n, LivingEntity livingEntity, MagicData magicData) {
        void spellLevel;
        void data;
        Record record;
        void caster;
        ServerLevel server;
        void level2;
        YouziCastFacing.prepare(livingEntity, this.kind, magicData);
        if (QizaiIronSpells.matches(this.kind) && level2 instanceof ServerLevel) {
            server = (ServerLevel)level2;
            record = QizaiIronSpells.aim(server, (LivingEntity)caster, this.kind);
        } else {
            record = new BossSignatureSpell.Aim(BossSpellEffects.aim((LivingEntity)caster));
        }
        data.setAdditionalCastData((ICastData)record);
        super.onServerPreCast((Level)level2, (int)spellLevel, (LivingEntity)caster, (MagicData)data);
        if (level2 instanceof ServerLevel) {
            server = (ServerLevel)level2;
            ICastData iCastData = data.getAdditionalCastData();
            if (iCastData instanceof QizaiIronSpells.Aim) {
                QizaiIronSpells.Aim aim = (QizaiIronSpells.Aim)iCastData;
                QizaiConstellation.warning(server, (LivingEntity)caster, QizaiIronSpells.skill(this.kind), aim.marks());
            }
        }
        CastingHooks.begin(this.kind, (LivingEntity)caster, (MagicData)data);
        DoraMotionSync.begin(this.kind, (LivingEntity)caster, (MagicData)data);
        IronHooks.begin(this, (int)spellLevel, (LivingEntity)caster, (MagicData)data);
    }

    /*
     * WARNING - void declaration
     */
    public void onServerCastTick(Level level, int n, LivingEntity livingEntity, MagicData magicData) {
        void level2;
        YouziCastFacing.prepare(livingEntity, this.kind, magicData);
        IronHooks.tick(this, n, livingEntity, magicData);
        if (level2 instanceof ServerLevel) {
            void caster;
            ServerLevel server = (ServerLevel)level2;
            if (caster.f_19797_ % 6 == 0) {
                void data;
                ICastData iCastData;
                if (QizaiIronSpells.matches(this.kind) && (iCastData = data.getAdditionalCastData()) instanceof QizaiIronSpells.Aim) {
                    QizaiIronSpells.Aim aim = (QizaiIronSpells.Aim)iCastData;
                    QizaiConstellation.warning(server, (LivingEntity)caster, QizaiIronSpells.skill(this.kind), aim.marks());
                } else {
                    ManorSpellEffects.warning(server, (LivingEntity)caster, this.kind, this.point((LivingEntity)caster, (MagicData)data));
                }
            }
        }
    }

    /*
     * WARNING - void declaration
     */
    public void onCast(Level level, int n, LivingEntity livingEntity, CastSource castSource, MagicData magicData) {
        void level2;
        YouziCastFacing.prepare(livingEntity, this.kind, magicData);
        IronHooks.release(this, livingEntity);
        if (level2 instanceof ServerLevel) {
            void caster;
            ServerLevel server = (ServerLevel)level2;
            if (caster.m_6084_() && this.isEnabled()) {
                void source;
                void spellLevel;
                void data;
                if (QizaiIronSpells.matches(this.kind)) {
                    QizaiIronSpells.cast(server, (LivingEntity)caster, this, (MagicData)data);
                } else {
                    ManorSpellEffects.cast(server, (LivingEntity)caster, this, ManorSpellRules.level((int)spellLevel), this.point((LivingEntity)caster, (MagicData)data));
                }
                CastingHooks.release(this.kind, (LivingEntity)caster, (MagicData)data);
                super.onCast((Level)level2, (int)spellLevel, (LivingEntity)caster, (CastSource)source, (MagicData)data);
                DoraMotionSync.release(this.kind, (LivingEntity)caster, (MagicData)data);
            }
        }
    }

    private Vec3 point(LivingEntity caster, MagicData data) {
        Vec3 vec3;
        ICastData iCastData;
        if (data != null && (iCastData = data.getAdditionalCastData()) instanceof BossSignatureSpell.Aim) {
            BossSignatureSpell.Aim aim = (BossSignatureSpell.Aim)iCastData;
            vec3 = aim.point();
        } else {
            vec3 = BossSpellEffects.aim(caster);
        }
        return vec3;
    }

    public void onServerCastComplete(Level level, int n, LivingEntity livingEntity, MagicData magicData, boolean bl) {
        IronHooks.complete(this, livingEntity, bl);
        super.onServerCastComplete(level, n, livingEntity, magicData, bl);
        CastingHooks.complete(this.kind, livingEntity, magicData, bl);
        DoraMotionSync.complete(this.kind, livingEntity, magicData, bl);
    }
}

