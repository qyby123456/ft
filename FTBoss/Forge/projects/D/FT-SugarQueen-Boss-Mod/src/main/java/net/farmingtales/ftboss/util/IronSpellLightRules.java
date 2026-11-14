package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.util.SpellVisualRules.Spell;

public final class IronSpellLightRules {
    /**
     * 光束用的原版（铁魔法）粒子形态。
     *
     * <p>⚠ <b>{@code SOUL_RAY} 已停用（v156），别再用它</b>：它对应的
     * {@code io.redspace.ironsspellbooks.particle.SoulfireRayParticle} 自身渲染有 bug ——
     * 2026-09-18 22:59 的客户端崩溃
     * {@code Description: Rendering Particle / IllegalStateException: Not filled all elements of the vertex}
     * 栈顶就是 {@code SoulfireRayParticle.drawQuad(SoulfireRayParticle.java:111)} 里的
     * {@code BufferBuilder.endVertex}。那是铁魔法自己的粒子代码，不是本模组的渲染类；
     * 但**触发它的是我们**（{@code PRESSURE_LANCE} / {@code ION_RAIL} 原先映射到 SOUL_RAY）。
     * 这两条光束现在改走 {@code TRACE}（本工程用得最多、从未出事的形态）。
     * 保留这个枚举常量只是为了不动既有校验脚本的引用，代码里已无任何分支会生成该粒子。
     */
    public enum Beam { NONE, TRACE, FROST_TRACE, ZAP, SOUL_RAY, SIPHON }
    public enum Accent { NONE, RADIAL_ZAP, REBOOT_ZAP, HOLY_RAYS }

    private IronSpellLightRules() {}

    public static Beam beam(Spell spell) {
        return switch (spell) {
            case CAPACITOR_OVERLOAD, STATIC_LATTICE, MAGNETIC_STORM -> Beam.ZAP;
            /* v156: 原本是 Beam.SOUL_RAY —— 那颗原版粒子会崩客户端，改成 TRACE。 */
            case PRESSURE_LANCE, ION_RAIL, STAR_CROSS -> Beam.TRACE;
            case FROSTGLASS_LANCE, FROST_NEEDLES -> Beam.FROST_TRACE;
            case CAMELLIA_BLOOD_BLOOM, SANGUINE_SUTURE, FLESH_BLOOM, CANDLE_ECLIPSE -> Beam.SIPHON;
            case GILDED_VERDICT, ARCANE_FUSILLADE, EFFIGY_VOLLEY, FEATHER_FAN,
                    CAMELLIA_SANCTUARY, CAMELLIA_REBIRTH, VIRULENT_HARVEST, GOLDEN_HARVEST,
                    PRISM_BATTERY, EFFIGY_PROCESSION, NIGHTFALL_BLADES, SERAPH_DESCENT -> Beam.TRACE;
            case CRYSTAL_CORONATION, VINTAGE_VORTEX, PRISMATIC_BLIGHT, TIDAL_EDICT,
                    CASK_RUPTURE, REPULSOR_DISCHARGE, ORCHARD_AEGIS, ABYSSAL_GEYSER,
                    EFFIGY_WARD, CANDLE_REAP, CANDLE_PRISON, BREACH_SHELL, RENDING_CLEAVE,
                    CRIMSON_OATH, SEARING_PAN, HEARTH_FEAST, FOUR_TIDES, ELIXIR_RAIN,
                    ORBITAL_GEARS, SHADOW_BRAND, VOID_ECHO, LUMINOUS_REBUKE, HALO_SANCTUARY,
                    WINGSTEP, WHITEOUT_EDICT, SUGAR_HAIL, GLACIAL_PRISM, ROYAL_SHATTER,
                    BRIAR_PROCESSION, SCARLET_POLLEN, CINDER_BREATH, INTOXICATING_TOAST,
                    VINTAGE_MINEFIELD, EMERGENCY_REBOOT, SPORE_ERUPTION, ROTTING_REQUIEM,
                    LIVING_MULCH, SOLAR_ORCHARD, JUDGMENT_SEED, UNDERTOW_SNARE,
                    ABYSSAL_CARAPACE, RIPTIDE_CORRIDOR, BLOOD_VERDICT, CITRUS_NOVA,
                    TIDAL_LOTUS, WINTER_CLOCK, FEATHER_REFUGE, ROSE_STARFALL -> Beam.NONE;
        };
    }

    public static Accent accent(Spell spell, Phase phase) {
        if (phase != Phase.RELEASE && phase != Phase.PULSE) return Accent.NONE;
        return switch (spell) {
            case REPULSOR_DISCHARGE -> phase == Phase.RELEASE ? Accent.RADIAL_ZAP : Accent.NONE;
            case EMERGENCY_REBOOT -> phase == Phase.PULSE ? Accent.REBOOT_ZAP : Accent.NONE;
            case LUMINOUS_REBUKE -> phase == Phase.RELEASE ? Accent.HOLY_RAYS : Accent.NONE;
            default -> Accent.NONE;
        };
    }

    public static boolean adapted(Spell spell) {
        return beam(spell) != Beam.NONE || accent(spell, Phase.RELEASE) != Accent.NONE || accent(spell, Phase.PULSE) != Accent.NONE;
    }

    public static boolean traceSafe(double coordinate) {
        return SpellVisualRules.validPoint(coordinate) && Math.abs(coordinate - (double) (float) coordinate) <= 1.0 / 64;
    }

    public static int samples(double distance, boolean reduced) {
        if (!Double.isFinite(distance) || distance < 0.05 || distance > 64) return 0;
        return Math.min(reduced ? 6 : 12, Math.max(2, (int) Math.ceil(distance / 2)));
    }

    public static int cost(Beam beam, double distance, boolean reduced) {
        int count = samples(distance, reduced);
        if (count == 0 || beam == Beam.NONE) return 0;
        return switch (beam) {
            /* SOUL_RAY 已停用，正常不会走到这里；留着只是为了 switch 穷尽。 */
            case SOUL_RAY -> 0;
            case SIPHON -> count * 2;
            case ZAP -> 4;
            default -> count;
        };
    }

    public static final class Budget {
        private long tick = Long.MIN_VALUE;
        private int spent;

        public boolean reserve(long now, int amount, int limit) {
            if (now != tick) { tick = now; spent = 0; }
            if (amount <= 0 || amount > limit || spent > limit - amount) return false;
            spent += amount;
            return true;
        }
    }
}
