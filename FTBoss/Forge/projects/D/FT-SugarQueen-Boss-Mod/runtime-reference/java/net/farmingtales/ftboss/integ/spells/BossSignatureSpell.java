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
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.FullPowerBossSpell;
import net.farmingtales.ftboss.util.BossSpellRules;
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

public final class BossSignatureSpell
extends FullPowerBossSpell {
    public final BossSpellRules.Kind kind;
    private final ResourceLocation id;
    private final DefaultConfig config;

    public BossSignatureSpell(BossSpellRules.Kind kind) {
        super(kind.mana, kind.damage, 10, BossSpellRules.support(kind) ? 1 : 4);
        this.kind = kind;
        this.id = new ResourceLocation("ftboss", kind.spell);
        this.config = new DefaultConfig().setMinRarity(SpellRarity.LEGENDARY).setMaxLevel(1).setSchoolResource(new ResourceLocation("irons_spellbooks", kind.school)).setCooldownSeconds((double)kind.cooldown).setAllowCrafting(true).build();
        this.castTime = BossSpellRules.castTicks(kind);
    }

    public ResourceLocation getSpellResource() {
        return this.id;
    }

    public DefaultConfig getDefaultConfig() {
        return this.config;
    }

    public CastType getCastType() {
        return CastType.LONG;
    }

    public int getEffectiveCastTime(int level, LivingEntity caster) {
        int ticks = super.getEffectiveCastTime(level, caster);
        return BossSpellTuning.regularBoss(caster) ? BossSpellTuning.bossCast(caster, ticks) : ticks;
    }

    public boolean allowLooting() {
        return false;
    }

    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.f_11739_);
    }

    public Optional<SoundEvent> getCastFinishSound() {
        if (this.kind.school.equals("blood")) {
            return Optional.of(SoundEvents.f_12389_);
        }
        return Optional.of(switch (this.kind.boss) {
            case "sugar_queen" -> SoundEvents.f_11983_;
            case "tea_tree" -> SoundEvents.f_144073_;
            case "wine_barrel" -> SoundEvents.f_11769_;
            case "defect_core" -> SoundEvents.f_12521_;
            case "compost_boss" -> SoundEvents.f_12389_;
            case "apple_boss" -> SoundEvents.f_144243_;
            default -> SoundEvents.f_12517_;
        });
    }

    public List<MutableComponent> getUniqueInfo(int level, LivingEntity caster) {
        String stat = BossSpellRules.healing(this.kind) ? "healing" : (BossSpellRules.shielding(this.kind) ? "shield" : "damage");
        int amount = BossSpellRules.shielding(this.kind) ? BossSpellRules.shield(this.getSpellPower(level, (Entity)caster)) : Math.round(this.getSpellPower(level, (Entity)caster));
        return List.of(Component.m_237110_((String)("ui.ftboss.signature." + stat), (Object[])new Object[]{amount}), Component.m_237115_((String)"ui.ftboss.signature.full_power"), Component.m_237115_((String)("spell.ftboss." + this.kind.spell + ".effect")), Component.m_237110_((String)"ui.ftboss.signature.source", (Object[])new Object[]{Component.m_237115_((String)("entity.ftboss." + this.kind.boss))}));
    }

    public void onServerPreCast(Level level, int spellLevel, LivingEntity caster, MagicData data) {
        data.setAdditionalCastData((ICastData)new Aim(BossSpellEffects.aim(caster)));
        super.onServerPreCast(level, spellLevel, caster, data);
    }

    public void onServerCastTick(Level level, int spellLevel, LivingEntity caster, MagicData data) {
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            if (caster.f_19797_ % 4 == 0) {
                BossSpellEffects.warning(server, caster, this.kind, this.point(caster, data));
            }
        }
    }

    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData data) {
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            if (caster.m_6084_()) {
                BossSpellEffects.cast(server, caster, this, BossSpellRules.level(spellLevel), this.point(caster, data));
                super.onCast(level, spellLevel, caster, source, data);
            }
        }
    }

    private Vec3 point(LivingEntity caster, MagicData data) {
        Vec3 vec3;
        ICastData iCastData;
        if (BossSpellRules.selfCentered(this.kind)) {
            return caster.m_20182_();
        }
        if (data != null && (iCastData = data.getAdditionalCastData()) instanceof Aim) {
            Aim aim = (Aim)iCastData;
            vec3 = aim.point();
        } else {
            vec3 = BossSpellEffects.aim(caster);
        }
        return vec3;
    }

    public record Aim(Vec3 point) implements ICastData
    {
        public void reset() {
        }
    }
}

