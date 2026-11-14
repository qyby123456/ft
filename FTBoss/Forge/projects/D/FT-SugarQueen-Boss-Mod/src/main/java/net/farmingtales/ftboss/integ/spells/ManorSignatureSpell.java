package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import java.util.List;
import java.util.Optional;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ManorSignatureSpell extends FullPowerBossSpell {
    public final Kind kind;
    private final DefaultConfig config;

    public ManorSignatureSpell(Kind kind) {
        super(kind.mana, kind.damage, 8, kind.support() ? 0 : 2);
        if (kind == Kind.STAR_INFERNO) baseManaCost = kind.mana;
        this.kind = kind;
        config = new DefaultConfig().setMinRarity(SpellRarity.LEGENDARY).setMaxLevel(ManorSpellRules.MAX_LEVEL)
                .setSchoolResource(new ResourceLocation("irons_spellbooks", kind.school))
                .setCooldownSeconds(kind.cooldown).setAllowCrafting(true).build();
        castTime = kind.castTicks;
    }

    @Override
    public ResourceLocation getSpellResource() { return new ResourceLocation("ftboss", kind.id); }

    @Override
    public DefaultConfig getDefaultConfig() { return config; }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public int getEffectiveCastTime(int level, LivingEntity caster) {
        int ticks = super.getEffectiveCastTime(level, caster);
        int duration = BossSpellTuning.manorBoss(caster) && !QizaiIronSpells.matches(kind) ? BossSpellTuning.bossCast(ticks) : ticks;
        /* ★ v188 按用户要求「祁崽施法速度翻倍」: 它自己的读条(含借来的庄园辅助)再减半 */
        if (caster instanceof net.farmingtales.ftboss.entity.EasterBossEntity qizai
                && qizai.getVariant() == net.farmingtales.ftboss.entity.EasterBossEntity.QIZAI_VARIANT) {
            duration = Math.max(1, (duration + 1) / 2);
        }
        int accelerated = caster instanceof net.farmingtales.ftboss.entity.EasterBossEntity boss
                ? boss.getChallengeDifficulty().acceleratedTicks(duration) : duration;
        return net.farmingtales.ftboss.balance.ManorBalanceHooks.castTicks(
                net.farmingtales.ftboss.zheergenphantom.CastReadability.ironCaster(accelerated, caster), caster);
    }

    @Override
    public boolean allowLooting() { return false; }

    @Override
    public Optional<SoundEvent> getCastStartSound() { return Optional.of(SoundEvents.BEACON_POWER_SELECT); }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(switch (kind.school) {
            case "ice" -> SoundEvents.GLASS_BREAK;
            case "fire" -> SoundEvents.BLAZE_SHOOT;
            case "lightning" -> SoundEvents.TRIDENT_THUNDER;
            case "ender" -> SoundEvents.ENDERMAN_TELEPORT;
            default -> SoundEvents.AMETHYST_BLOCK_CHIME;
        });
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int level, LivingEntity caster) {
        return List.of(Component.translatable("spell.ftboss." + kind.id + ".effect"),
                kind.support() ? Component.translatable("ui.ftboss.manor.support")
                        : Component.translatable("ui.ftboss.manor.pulse_damage", Math.round(ManorRewardBalance.displayDamage(getSpellPower(level, caster), caster))),
                Component.translatable("ui.ftboss.signature.full_power"),
                Component.translatable("ui.ftboss.signature.source", Component.translatable("ui.ftboss.manor.owner." + kind.variant)));
    }

    @Override
    public void onServerPreCast(Level level, int spellLevel, LivingEntity caster, MagicData data) {
        if (kind == Kind.STAR_INFERNO) {
            data.setAdditionalCastData(new BossSignatureSpell.Aim(caster.position()));
            super.onServerPreCast(level, spellLevel, caster, data);
            if (level instanceof ServerLevel server) QizaiInfernoEffects.warning(server, caster);
            return;
        }
        data.setAdditionalCastData(QizaiIronSpells.matches(kind) && level instanceof ServerLevel server
                ? QizaiIronSpells.aim(server, caster, kind) : new BossSignatureSpell.Aim(BossSpellEffects.aim(caster)));
        super.onServerPreCast(level, spellLevel, caster, data);
        if (level instanceof ServerLevel server && data.getAdditionalCastData() instanceof QizaiIronSpells.Aim aim)
            net.farmingtales.ftboss.skill.QizaiConstellation.warning(server, caster, QizaiIronSpells.skill(kind), aim.marks());
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity caster, MagicData data) {
        if (level instanceof ServerLevel server && caster.tickCount % 6 == 0) {
            if (kind == Kind.STAR_INFERNO) {
                QizaiInfernoEffects.warning(server, caster);
                return;
            }
            if (QizaiIronSpells.matches(kind) && data.getAdditionalCastData() instanceof QizaiIronSpells.Aim aim)
                net.farmingtales.ftboss.skill.QizaiConstellation.warning(server, caster, QizaiIronSpells.skill(kind), aim.marks());
            else ManorSpellEffects.warning(server, caster, kind, point(caster, data));
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData data) {
        if (level instanceof ServerLevel server && caster.isAlive() && isEnabled()) {
            if (kind == Kind.STAR_INFERNO) QizaiInfernoEffects.cast(server, caster, this, spellLevel);
            else if (QizaiIronSpells.matches(kind)) QizaiIronSpells.cast(server, caster, this, data);
            else ManorSpellEffects.cast(server, caster, this, ManorSpellRules.level(spellLevel), point(caster, data));
            super.onCast(level, spellLevel, caster, source, data);
        }
    }

    private Vec3 point(LivingEntity caster, MagicData data) {
        return data != null && data.getAdditionalCastData() instanceof BossSignatureSpell.Aim aim
                ? aim.point() : BossSpellEffects.aim(caster);
    }
}
