package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import java.util.List;
import java.util.Optional;
import net.farmingtales.ftboss.util.ManorSpellCastRules;
import net.farmingtales.ftboss.util.QizaiBarrageRules;
import net.farmingtales.ftboss.util.QizaiLaserBarrageRules;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class QizaiBarrageSpell extends FullPowerBossSpell {
    private final DefaultConfig config;

    public QizaiBarrageSpell() {
        super(QizaiBarrageRules.MANA, (int) QizaiLaserBarrageRules.DAMAGE, 0, 0);
        castTime = ManorSpellCastRules.castTicks(QizaiBarrageRules.ID,
                (int) QizaiLaserBarrageRules.DAMAGE * QizaiBarrageRules.totalBeams(), true);
        config = new DefaultConfig().setMinRarity(SpellRarity.LEGENDARY).setMaxLevel(1)
                .setSchoolResource(new ResourceLocation("irons_spellbooks", "ender"))
                .setCooldownSeconds(QizaiBarrageRules.COOLDOWN_SECONDS).setAllowCrafting(true).build();
    }

    @Override public ResourceLocation getSpellResource() { return new ResourceLocation("ftboss", QizaiBarrageRules.ID); }
    @Override public DefaultConfig getDefaultConfig() { return config; }
    @Override public CastType getCastType() { return CastType.LONG; }
    @Override public boolean allowLooting() { return false; }
    @Override public Optional<SoundEvent> getCastStartSound() { return Optional.of(SoundEvents.BEACON_ACTIVATE); }
    @Override public Optional<SoundEvent> getCastFinishSound() { return Optional.of(SoundEvents.AMETHYST_BLOCK_BREAK); }

    @Override public float getSpellPower(int rank, Entity caster) {
        float power = super.getSpellPower(rank, caster);
        return caster instanceof LivingEntity living ? BossSpellTuning.playerPower(living, power) : power;
    }

    @Override public List<MutableComponent> getUniqueInfo(int rank, LivingEntity caster) {
        return List.of(Component.translatable("spell.ftboss." + QizaiBarrageRules.ID + ".effect"),
                Component.translatable("ui.ftboss.manor.pulse_damage", Math.round(ManorRewardBalance.displayDamage(getSpellPower(rank, caster), caster))),
                Component.translatable("ui.ftboss.signature.full_power"),
                Component.translatable("ui.ftboss.signature.source", Component.translatable("ui.ftboss.manor.owner.11")));
    }

    @Override public boolean checkPreCastConditions(Level level, int rank, LivingEntity caster, MagicData data) {
        return isEnabled() && caster.isAlive() && !caster.isSpectator() && !caster.isRemoved()
                && level.hasChunkAt(caster.blockPosition()) && level.getWorldBorder().isWithinBounds(caster.blockPosition());
    }

    @Override public void onCast(Level level, int rank, LivingEntity caster, CastSource source, MagicData data) {
        if (level instanceof ServerLevel server && checkPreCastConditions(level, rank, caster, data))
            QizaiBarrageEffects.cast(server, caster, this, getSpellPower(rank, caster));
        super.onCast(level, rank, caster, source, data);
    }
}
