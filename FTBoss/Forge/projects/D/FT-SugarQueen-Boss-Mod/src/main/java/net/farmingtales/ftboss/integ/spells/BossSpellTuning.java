package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.ManorBossCombatRules;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public final class BossSpellTuning {
    private BossSpellTuning() {}

    /* ==================== 2026-09-20 按用户要求：Boss 施法时间变长 1/3 ====================
       用户原话：「boss施法时间变长1/3倍」。
       · 此前 Boss（常规 + 庄园）的实际咏唱时长 = 法术原始时长 × 1/2（旧 halfCast = (t + 1) / 2，向上取整）；
       · 现在改为 × 2/3 —— 也就是旧值的 4/3，比原来长 1/3；仍向上取整、下限 1 tick；
       · 覆盖三条路径：常规 Boss 招牌法术（BossSignatureSpell）、庄园 Boss 招牌法术（ManorSignatureSpell）、
         常规 Boss 的铁魔法 LONG 类法术（fasterLegacyCast）；
       · 只改「咏唱时长」，冷却 / 施法间隔 / 伤害 / 蓝耗一律不动；
       · 招牌法术取消闸门是 60 tick（BossSpellCombat 第 57 行），最长的基础 32 tick → 22 tick，仍在窗口内。 */

    public static final double BOSS_CAST_MULT = 2.0 / 3.0;

    /** Boss 咏唱时长：法术原始时长 × {@link #BOSS_CAST_MULT}（向上取整，下限 1 tick）。 */
    public static int bossCast(int ticks) { return ticks <= 0 ? 0 : Math.max(1, (int) Math.ceil(ticks * BOSS_CAST_MULT)); }

    /** Regular finale bosses retain the global 2/3 cast rule, then cast twice as fast. */
    public static int bossCast(LivingEntity caster, int ticks) {
        return net.farmingtales.ftboss.boss.NonManorFinaleDifficulty.acceleratedTicks(caster, bossCast(ticks));
    }

    public static boolean regularBoss(LivingEntity caster) {
        return caster != null && !BossSpellRules.forBoss(EntityType.getKey(caster.getType()).toString()).isEmpty();
    }

    public static float signaturePower(BossSpellRules.Kind kind) {
        float baseline = switch (kind) {
            case FROST, DEFECT, COMPOST_REAP, WINE_FUSE, APPLE_HARVEST -> 34;
            case TEA, TIDE_SURGE -> 30;
            case WINE, TIDE, COMPOST_ERUPTION -> 32;
            case COMPOST, FROST_HAIL, TEA_SUTURE, APPLE_ORBIT -> 24;
            case APPLE, DEFECT_PULSE, COMPOST_ROT -> 36;
            case FROST_LANCE, TEA_FLESH, APPLE_SEED -> 40;
            case WINE_BOMB, FROST_SHATTER -> 42;
            case TIDE_GEYSER, DEFECT_RAIL -> 44;
            case TEA_THORNS -> 26;
            case TEA_POLLEN -> 21;
            case WINE_BREATH, DEFECT_STORM -> 23;
            case TIDE_RIP -> 22;
            case TEA_SANCTUARY, WINE_TOAST, DEFECT_REBOOT -> 10;
            case APPLE_AEGIS, TEA_RENEWAL -> 8;
            case FROST_PRISM, TIDE_BUBBLE -> 12;
            case COMPOST_MULCH -> 9;
        };
        return BossSpellRules.support(kind) ? baseline : baseline * 2.0F;
    }

    public static float scaledPower(AbstractSpell spell, LivingEntity caster, float base) {
        double config = (Double) io.redspace.ironsspellbooks.api.config.SpellConfigManager.getSpellConfigValue(
                spell, io.redspace.ironsspellbooks.api.config.SpellConfigParameter.POWER_MULTIPLIER);
        double power = base * caster.getAttributeValue(io.redspace.ironsspellbooks.api.registry.AttributeRegistry.SPELL_POWER.get())
                * spell.getSchoolType().getPowerFor(caster) * config;
        return Double.isFinite(power) ? (float) Math.max(0, Math.min(Float.MAX_VALUE, power)) : 0;
    }

    public static void fasterLegacyCast(AbstractSpellCastingMob caster, AbstractSpell spell) {
        if (!regularBoss(caster) || spell instanceof BossSignatureSpell || spell.getCastType() != CastType.LONG
                || !caster.isCasting()) return;
        var magic = caster.getMagicData();
        if (!spell.getSpellId().equals(magic.getCastingSpellId())) return;
        magic.initiateCast(spell, magic.getCastingSpellLevel(), bossCast(caster, magic.getCastDuration()),
                magic.getCastSource(), magic.getCastingEquipmentSlot());
    }

    public static boolean manorBoss(LivingEntity caster) {
        return caster instanceof EasterBossEntity boss && !boss.isMini() && !boss.isGiant();
    }

    /* ==================== 2026-09-19 按用户要求：玩家用 Boss 掉落的法术卷轴施法时加强 ====================
       用户原话：「boss掉落的法术卷轴数值翻倍，buff持续时间翻倍」。
       · 数值（伤害 / 护盾 / 治疗）：玩家施法 ×{@link #PLAYER_POWER_MULT}；
       · 正面效果（buff）持续时间：玩家施法 ×{@link #PLAYER_BUFF_MULT}；
       · 减益（缓慢 / 虚弱 / 中毒…）时长不变；
       · **Boss 自己施法完全不受影响** —— 两条分支（signaturePower / manorBoss）原样保留，
         所以 Boss 战的强度一点没变，只是"卷轴到玩家手里更值钱"。
       判定口径是"施法者是不是玩家"（而不是"从卷轴施法"），因为玩家还能把卷轴抄进法术书，
       那时施法来源不再是 SCROLL，但仍应该享受这份加强。 */

    public static final float PLAYER_POWER_MULT = 2.0F;
    public static final int PLAYER_BUFF_MULT = 2;

    public static boolean playerCast(LivingEntity caster) {
        return caster instanceof net.minecraft.world.entity.player.Player;
    }

    /** 玩家施法：数值翻倍；Boss 施法原样。 */
    public static float playerPower(LivingEntity caster, float power) {
        return playerCast(caster) ? power * PLAYER_POWER_MULT : power;
    }

    /** 玩家施法：正面效果时长翻倍；减益与 Boss 施法原样。 */
    public static int buffTicks(LivingEntity caster, net.minecraft.world.effect.MobEffect effect, int ticks) {
        return playerCast(caster) && effect != null && effect.isBeneficial() ? ticks * PLAYER_BUFF_MULT : ticks;
    }

    public static double radiusScale(LivingEntity caster) {
        return manorBoss(caster) ? ManorBossCombatRules.radiusScale(((EasterBossEntity) caster).getVariant())
                : net.farmingtales.ftboss.skill.SpellVisuals.playerRadiusScale(caster);
    }

    public static double radius(LivingEntity caster, double baseRadius) {
        return manorBoss(caster) ? ManorBossCombatRules.spellRadius(((EasterBossEntity) caster).getVariant(), baseRadius)
                : baseRadius * net.farmingtales.ftboss.skill.SpellVisuals.playerRadiusScale(caster);
    }
}
