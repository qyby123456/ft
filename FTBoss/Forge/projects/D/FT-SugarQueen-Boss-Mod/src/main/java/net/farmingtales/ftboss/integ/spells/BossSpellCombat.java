package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.BossSpellRules.Kind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BossSpellCombat {
    static final String READY = "ftboss:signature_ready";
    static final String CAST = "ftboss:signature_cast";
    private static final String NEXT = "ftboss:signature_next";

    private BossSpellCombat() {}

    public static List<Kind> spellPool(Mob boss) {
        if (boss instanceof net.farmingtales.ftboss.entity.EasterBossEntity lemon) return lemon.lemonSacrifice().spells();
        return BossSpellRules.forBoss(EntityType.getKey(boss.getType()).toString());
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob boss) || !(boss.level() instanceof ServerLevel level)) return;
        if (boss instanceof net.farmingtales.ftboss.entity.EasterBossEntity) {
            boss.getPersistentData().remove(CAST);
            return;
        }
        List<Kind> pair = spellPool(boss);
        if (pair.isEmpty()) return;
        CompoundTag data = boss.getPersistentData();
        LivingEntity target = boss.getTarget();
        if (!boss.isAlive() || boss.isRemoved() || boss.isNoAi() || boss.isInvulnerable()
                || target == null || target.level() != level || !BossSpellEffects.canTarget(boss, target)
                || boss.distanceToSqr(target) > 32 * 32 || !boss.hasLineOfSight(target)) {
            data.remove(CAST);
            return;
        }
        long now = level.getGameTime();
        if (data.contains(CAST, 10)) {
            CompoundTag cast = data.getCompound(CAST);
            Kind kind = pair.stream().filter(candidate -> candidate.spell.equals(cast.getString("spell"))).findFirst().orElse(null);
            if (kind == null) {
                data.remove(CAST);
                return;
            }
            BossSignatureSpell spell = BossIronSpells.spell(kind);
            long elapsed = now - cast.getLong("started");
            if (!spell.isEnabled() || elapsed < 0 || elapsed > 60 || !cast.hasUUID("target") || !target.getUUID().equals(cast.getUUID("target"))) {
                data.remove(CAST);
                return;
            }
            Vec3 center = new Vec3(cast.getDouble("x"), cast.getDouble("y"), cast.getDouble("z"));
            if (BossSpellRules.selfCentered(kind)) center = boss.position();
            if (elapsed < spell.getEffectiveCastTime(1, boss)) {
                if (elapsed % 4 == 0) BossSpellEffects.warning(level, boss, kind, center);
                return;
            }
            data.remove(CAST);
            MagicData magic = new MagicData();
            magic.setAdditionalCastData(new BossSignatureSpell.Aim(center));
            if (boss instanceof net.farmingtales.ftboss.entity.EasterBossEntity lemon) {
                lemon.lemonSacrifice().runInherited(() -> spell.onCast(level, Math.min(2, spell.getMaxLevel()), boss, CastSource.MOB, magic));
            } else spell.onCast(level, Math.min(2, spell.getMaxLevel()), boss, CastSource.MOB, magic);
            FTBossMod.LOGGER.info("[FTBoss][Signature] {} released {}", boss.getName().getString(), spell.getSpellId());
            return;
        }
        if (!data.contains(READY, 99)) {
            data.putLong(READY, now + net.farmingtales.ftboss.boss.NonManorFinaleDifficulty.acceleratedTicks(
                    boss, BossSpellPacing.SIGNATURE_OPENING_TICKS + Math.floorMod(boss.getId(), 20)));
            return;
        }
        if (now < data.getLong(READY) || boss.distanceToSqr(target) > BossSpellRules.RANGE * BossSpellRules.RANGE) return;
        int preferred = Math.floorMod(data.getInt(NEXT), pair.size());
        Kind kind = null;
        for (int offset = 0; offset < pair.size(); offset++) {
            Kind candidate = pair.get((preferred + offset) % pair.size());
            if (BossIronSpells.spell(candidate).isEnabled() && now >= data.getLong(READY + ":" + candidate.spell)) {
                kind = candidate;
                data.putInt(NEXT, (preferred + offset + 1) % pair.size());
                break;
            }
        }
        if (kind == null) return;
        BossSignatureSpell spell = BossIronSpells.spell(kind);
        data.putLong(READY, now + net.farmingtales.ftboss.boss.NonManorFinaleDifficulty.acceleratedTicks(
                boss, BossSpellPacing.SIGNATURE_INTERVAL_TICKS));
        data.putLong(READY + ":" + kind.spell, now
                + net.farmingtales.ftboss.boss.NonManorFinaleDifficulty.acceleratedTicks(
                boss, BossSpellPacing.signatureCooldown(spell.getSpellCooldown())));
        Vec3 center = BossSpellRules.selfCentered(kind) ? boss.position() : BossSpellEffects.aim(boss);
        CompoundTag cast = new CompoundTag();
        cast.putLong("started", now);
        cast.putString("spell", kind.spell);
        cast.putUUID("target", target.getUUID());
        cast.putDouble("x", center.x);
        cast.putDouble("y", center.y);
        cast.putDouble("z", center.z);
        data.put(CAST, cast);
        spell.playSound(spell.getCastStartSound(), boss);
        BossSpellEffects.warning(level, boss, kind, center);
        if (boss instanceof RegularBossCastHost animated) {
            animated.startSignatureCast(kind, spell.getEffectiveCastTime(1, boss));
        }
        FTBossMod.LOGGER.info("[FTBoss][Signature] {} charging {}", boss.getName().getString(), spell.getSpellId());
    }
}
