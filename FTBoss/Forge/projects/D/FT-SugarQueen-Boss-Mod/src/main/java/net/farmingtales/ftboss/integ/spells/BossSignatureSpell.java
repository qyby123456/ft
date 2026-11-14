package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import java.util.List;
import java.util.Optional;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.BossSpellRules.Kind;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class BossSignatureSpell extends FullPowerBossSpell {
    public final Kind kind;
    private final ResourceLocation id;
    private final DefaultConfig config;

    public BossSignatureSpell(Kind kind) {
        super(kind.mana, kind.damage, 10, BossSpellRules.support(kind) ? 1 : 4);
        this.kind = kind;
        id = new ResourceLocation(FTBossMod.MOD_ID, kind.spell);
        config = new DefaultConfig().setMinRarity(SpellRarity.LEGENDARY).setMaxLevel(BossSpellRules.MAX_LEVEL)
                .setSchoolResource(new ResourceLocation("irons_spellbooks", kind.school))
                .setCooldownSeconds(kind.cooldown).setAllowCrafting(true).build();
        castTime = BossSpellRules.castTicks(kind);
    }

    @Override
    public ResourceLocation getSpellResource() { return id; }

    @Override
    public DefaultConfig getDefaultConfig() { return config; }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public int getEffectiveCastTime(int level, LivingEntity caster) {
        int ticks = super.getEffectiveCastTime(level, caster);
        return BossSpellTuning.regularBoss(caster) ? BossSpellTuning.bossCast(caster, ticks) : ticks;
    }

    @Override
    public boolean allowLooting() { return false; }

    @Override
    public Optional<SoundEvent> getCastStartSound() { return Optional.of(SoundEvents.BEACON_POWER_SELECT); }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        if (kind.school.equals("blood")) return Optional.of(SoundEvents.SLIME_BLOCK_BREAK);
        return Optional.of(switch (kind.boss) {
            case "sugar_queen" -> SoundEvents.GLASS_BREAK;
            case "tea_tree" -> SoundEvents.AZALEA_LEAVES_BREAK;
            case "wine_barrel" -> SoundEvents.BOTTLE_EMPTY;
            case "defect_core" -> SoundEvents.TRIDENT_THUNDER;
            case "compost_boss" -> SoundEvents.SLIME_BLOCK_BREAK;
            case "apple_boss" -> SoundEvents.AMETHYST_BLOCK_CHIME;
            default -> SoundEvents.TRIDENT_RIPTIDE_1;
        });
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int level, LivingEntity caster) {
        String stat = BossSpellRules.healing(kind) ? "healing" : BossSpellRules.shielding(kind) ? "shield" : "damage";
        int amount = BossSpellRules.shielding(kind) ? BossSpellRules.shield(getSpellPower(level, caster)) : Math.round(getSpellPower(level, caster));
        return List.of(Component.translatable("ui.ftboss.signature." + stat, amount),
                Component.translatable("ui.ftboss.signature.full_power"),
                Component.translatable("spell.ftboss." + kind.spell + ".effect"),
                Component.translatable("ui.ftboss.signature.source", Component.translatable("entity.ftboss." + kind.boss)));
    }

    @Override
    public void onServerPreCast(Level level, int spellLevel, LivingEntity caster, MagicData data) {
        data.setAdditionalCastData(new Aim(BossSpellEffects.aim(caster)));
        super.onServerPreCast(level, spellLevel, caster, data);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity caster, MagicData data) {
        if (level instanceof ServerLevel server && caster.tickCount % 4 == 0) {
            BossSpellEffects.warning(server, caster, kind, point(caster, data));
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData data) {
        if (level instanceof ServerLevel server && caster.isAlive()) {
            BossSpellEffects.cast(server, caster, this, BossSpellRules.level(spellLevel), point(caster, data));
            super.onCast(level, spellLevel, caster, source, data);
        }
    }

    private Vec3 point(LivingEntity caster, MagicData data) {
        if (BossSpellRules.selfCentered(kind)) return caster.position();
        return data != null && data.getAdditionalCastData() instanceof Aim aim ? aim.point() : BossSpellEffects.aim(caster);
    }

    public record Aim(Vec3 point) implements ICastData {
        @Override
        public void reset() {}
    }
}
