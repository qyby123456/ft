package net.farmingtales.ftboss.integ.spells;

import net.farmingtales.ftboss.util.BossSpellRules;

/**
 * Server-to-client animation hook for the seven regular (non-Manor) bosses.
 * Signature spells are driven by {@link BossSpellCombat}, outside Iron's normal
 * {@code MagicData} casting pipeline, so their cast pose has to be synchronized explicitly.
 */
public interface RegularBossCastHost {
    String ATTACK_ACTION = "signature_cast";
    String SUPPORT_ACTION = "signature_self_cast";
    int MIN_VISUAL_TICKS = 30;
    int RELEASE_TICKS = 8;

    void startSignatureCast(BossSpellRules.Kind kind, int castTicks);

    static String action(BossSpellRules.Kind kind) {
        return BossSpellRules.support(kind) ? SUPPORT_ACTION : ATTACK_ACTION;
    }

    static int visualTicks(int castTicks) {
        return Math.max(MIN_VISUAL_TICKS, Math.max(0, castTicks) + RELEASE_TICKS);
    }
}