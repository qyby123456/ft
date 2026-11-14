package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class BossSpellPacing {
    public static final int SIGNATURE_OPENING_TICKS = 60;
    public static final int SIGNATURE_INTERVAL_TICKS = 60;
    public static final int LEGACY_INTERVAL_TICKS = 240;
    public static final int LEGACY_RETRY_TICKS = 60;
    public static final float LEGACY_CHANCE = 0.25F;
    private static final String LEGACY_READY = "ftboss:legacy_spell_ready";

    private BossSpellPacing() {}

    public static int signatureCooldown(int spellCooldown) {
        return Math.max(160, (int) Math.ceil(spellCooldown * 0.75));
    }

    public static boolean tryLegacyCast(LivingEntity caster, AbstractSpell spell) {
        if (!(caster instanceof Mob boss) || !(boss.level() instanceof ServerLevel level)
                || spell == null || !spell.getSpellId().startsWith("irons_spellbooks:")) return true;
        var signatures = BossSpellCombat.spellPool(boss);
        if (signatures.stream().noneMatch(kind -> BossIronSpells.spell(kind).isEnabled())) return true;
        CompoundTag data = boss.getPersistentData();
        if (boss instanceof IMagicEntity magic && magic.isCasting()) return false;
        long now = level.getGameTime();
        if (now < data.getLong(LEGACY_READY)) return false;
        boolean selected = boss.getRandom().nextFloat() < LEGACY_CHANCE;
        data.putLong(LEGACY_READY, now + net.farmingtales.ftboss.boss.NonManorFinaleDifficulty.acceleratedTicks(
                boss, selected ? LEGACY_INTERVAL_TICKS : LEGACY_RETRY_TICKS));
        return selected;
    }
}
