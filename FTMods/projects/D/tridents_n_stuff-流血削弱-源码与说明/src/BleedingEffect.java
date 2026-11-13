package me.fzzyhmstrs.tridents_n_stuff;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Patched Bleeding effect (Sanguine Bond / 血腥契约 nerf).
 *
 * <p>Identical to the original in every way except:
 * <ul>
 *   <li>the effect is ticked every tick (the damage cadence of the original
 *       {@code duration % (50 >> amplifier) == 0} rule is reproduced inside
 *       {@link #m_6742_(LivingEntity, int)});</li>
 *   <li>the stored amplifier is defensively capped at 4 (5 levels);</li>
 *   <li>10 seconds (200 ticks) after the last refresh the level decays by one
 *       every 2 seconds (40 ticks) until it reaches 0.</li>
 * </ul>
 *
 * <p>The effect is applied with a 400 tick (20 s) duration so that the 10 s
 * "full strength" window plus the decay tail fit inside a single instance.
 */
public final class BleedingEffect extends MobEffect {

    /** Highest stored amplifier: 4 == level V == 5 stacks. */
    private static final int MAX_AMPLIFIER = 4;

    /** Level decay begins once this much duration is left (10 s after the last refresh). */
    private static final int DECAY_START_DURATION = 200;

    /** One level is lost every this many ticks (2 s). */
    private static final int DECAY_INTERVAL = 40;

    public BleedingEffect() {
        super(MobEffectCategory.HARMFUL, 8126506);
    }

    /** Tick every tick; the original damage cadence is applied inside applyEffectTick. */
    @Override
    public boolean m_6584_(int duration, int amplifier) {
        return true;
    }

    @Override
    public void m_6742_(LivingEntity entity, int amplifier) {
        MobEffectInstance instance = entity.m_21124_(this);
        if (instance == null) {
            return;
        }

        int duration = instance.m_19557_();
        int amp = instance.m_19564_();
        if (amp > MAX_AMPLIFIER) {
            amp = MAX_AMPLIFIER;
        }

        // --- original damage cadence: 1.5 damage every (50 >> amplifier) ticks ---
        int interval = 50 >> amp;
        if (interval > 0 && duration % interval == 0) {
            entity.m_6469_(entity.m_269291_().m_269483_(), 1.5F);
        }

        // --- decay: after 10 s without a refresh, lose one level every 2 s ---
        if (entity.m_9236_().f_46443_) {
            return; // client copy is kept in sync by the server's update packet
        }
        if (amp > 0 && duration > 0 && duration <= DECAY_START_DURATION && duration % DECAY_INTERVAL == 0) {
            MobEffectInstance decayed = new MobEffectInstance(
                    this,
                    duration - 1,
                    amp - 1,
                    instance.m_19571_(),
                    instance.m_19572_(),
                    instance.m_19575_(),
                    null,
                    instance.m_216895_());
            entity.m_147215_(decayed, null);
        }
    }
}
