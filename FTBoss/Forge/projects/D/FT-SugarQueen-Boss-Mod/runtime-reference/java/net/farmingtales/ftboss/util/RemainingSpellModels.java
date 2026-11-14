/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.EnumSet;
import java.util.Set;
import net.farmingtales.ftboss.util.ManorStandaloneSpellModels;
import net.farmingtales.ftboss.util.SpellVisualRules;

public final class RemainingSpellModels {
    public static final int MAX_QUADS = 2600;
    public static final int CHARGED_ORB_REFERENCE_QUADS = 258;
    private static final int IRON = 3489872;
    private static final int GOLD = 12950360;
    private static final int SILVER = 12114399;
    private static final int WOOD = 6635056;
    private static final int LEAF = 4489043;
    private static final Set<SpellVisualRules.Spell> MODELED = EnumSet.of(SpellVisualRules.Spell.CAMELLIA_BLOOD_BLOOM, new SpellVisualRules.Spell[]{SpellVisualRules.Spell.VINTAGE_VORTEX, SpellVisualRules.Spell.CAPACITOR_OVERLOAD, SpellVisualRules.Spell.PRISMATIC_BLIGHT, SpellVisualRules.Spell.GILDED_VERDICT, SpellVisualRules.Spell.TIDAL_EDICT, SpellVisualRules.Spell.FROSTGLASS_LANCE, SpellVisualRules.Spell.CAMELLIA_SANCTUARY, SpellVisualRules.Spell.CASK_RUPTURE, SpellVisualRules.Spell.REPULSOR_DISCHARGE, SpellVisualRules.Spell.VIRULENT_HARVEST, SpellVisualRules.Spell.ABYSSAL_GEYSER, SpellVisualRules.Spell.BREACH_SHELL, SpellVisualRules.Spell.HEARTH_FEAST, SpellVisualRules.Spell.ELIXIR_RAIN, SpellVisualRules.Spell.WHITEOUT_EDICT, SpellVisualRules.Spell.SUGAR_HAIL, SpellVisualRules.Spell.GLACIAL_PRISM, SpellVisualRules.Spell.ROYAL_SHATTER, SpellVisualRules.Spell.BRIAR_PROCESSION, SpellVisualRules.Spell.SCARLET_POLLEN, SpellVisualRules.Spell.CAMELLIA_REBIRTH, SpellVisualRules.Spell.FLESH_BLOOM, SpellVisualRules.Spell.SANGUINE_SUTURE, SpellVisualRules.Spell.CINDER_BREATH, SpellVisualRules.Spell.INTOXICATING_TOAST, SpellVisualRules.Spell.VINTAGE_MINEFIELD, SpellVisualRules.Spell.ION_RAIL, SpellVisualRules.Spell.STATIC_LATTICE, SpellVisualRules.Spell.EMERGENCY_REBOOT, SpellVisualRules.Spell.SPORE_ERUPTION, SpellVisualRules.Spell.ROTTING_REQUIEM, SpellVisualRules.Spell.LIVING_MULCH, SpellVisualRules.Spell.SOLAR_ORCHARD, SpellVisualRules.Spell.JUDGMENT_SEED, SpellVisualRules.Spell.GOLDEN_HARVEST, SpellVisualRules.Spell.UNDERTOW_SNARE, SpellVisualRules.Spell.ABYSSAL_CARAPACE, SpellVisualRules.Spell.RIPTIDE_CORRIDOR, SpellVisualRules.Spell.TIDAL_LOTUS});

    private RemainingSpellModels() {
    }

    public static Set<SpellVisualRules.Spell> modeled() {
        return Set.copyOf(MODELED);
    }

    public static boolean supports(SpellVisualRules.Spell spell) {
        return MODELED.contains((Object)spell);
    }

    public static boolean renders(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase) {
        return RemainingSpellModels.supports(spell) && phase != null && phase != SpellVisualRules.Phase.BEAM;
    }

    public static int draw(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase, double d, boolean bl, ManorStandaloneSpellModels.Sink sink) {
        if (!RemainingSpellModels.renders(spell, phase) || sink == null || !Double.isFinite(d) || d < 0.0 || d >= (double)spell.lifetime(phase)) {
            return 0;
        }
        double d2 = RemainingSpellModels.ease(d / (double)(phase == SpellVisualRules.Phase.CHARGE ? 7 : 5)) * (1.0 - 0.65 * RemainingSpellModels.ease((d - (double)spell.lifetime(phase) + 6.0) / 6.0));
        Mesh mesh = new Mesh(sink, d, d2, bl);
        double d3 = d * 0.035;
        int n = bl ? 4 : 6;
        double d4 = phase == SpellVisualRules.Phase.CHARGE ? d2 * 0.55 : RemainingSpellModels.ease((d + 1.0) / 6.0);
        double d5 = phase == SpellVisualRules.Phase.CHARGE ? 0.0 : Math.sin(Math.min(1.0, d / 9.0) * Math.PI);
        switch (spell) {
            case CRYSTAL_CORONATION: {
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.48, 0.0), 1.42, 0.12, 14153215);
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.72, 0.0), 1.18 + d4 * 0.18, 0.045, 6995951);
                for (int i = 0; i < 10; ++i) {
                    double d6 = (double)i * Math.PI / 5.0 + d3 * 0.12;
                    double d7 = (i % 2 == 0 ? 2.65 : 1.72) * (0.62 + d2 * 0.38);
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(1.38, d6, 0.5);
                    ManorStandaloneSpellModels.Point point2 = RemainingSpellModels.polar(1.16 + d5 * 0.45, d6, d7);
                    mesh.leaf(point, point2, i % 2 == 0 ? 0.34 : 0.24, i % 2 == 0 ? 8837375 : 12447487);
                    mesh.rod(point, point2, 0.032, 0xF4FDFF);
                    if (i % 2 != 0) continue;
                    mesh.gem(point2, 0.105, 0.24, 0xFFFFFF);
                }
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.18 + d4 * 0.32, 0.0), 0.33, 0.72, 11134719);
                break;
            }
            case CAMELLIA_BLOOD_BLOOM: {
                mesh.prism(new ManorStandaloneSpellModels.Point(0.0, 0.14, 0.0), 0.18, 0.12, 1.05, 6, 5904424);
                mesh.flower(new ManorStandaloneSpellModels.Point(0.0, 1.02, 0.0), 1.48, 9, 11673414, 16739466, d3 * 0.18);
                mesh.flower(new ManorStandaloneSpellModels.Point(0.0, 1.24, 0.0), 0.86, 6, 7999531, 14892133, -d3 * 0.3);
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.62, 0.0), 0.31, 0.48, 16765660);
                for (int i = 0; i < 5; ++i) {
                    double d8 = (double)i * Math.PI * 2.0 / 5.0 + d3;
                    double d9 = phase == SpellVisualRules.Phase.CHARGE ? 0.0 : (d * 0.11 + (double)i * 0.17) % 1.0;
                    mesh.gem(RemainingSpellModels.polar(1.0 + (double)(i % 2) * 0.26, d8, 0.82 - d9 * 0.72), 0.085, 0.18, 13113414);
                }
                break;
            }
            case CAPACITOR_OVERLOAD: {
                mesh.box(new ManorStandaloneSpellModels.Point(0.0, 1.22, 0.0), 0.74, 0.74, 0.74, 1586258);
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.22, 0.0), 0.31 + d5 * 0.12, 0.56, 6678527);
                for (int i = 0; i < 4; ++i) {
                    double d10 = (double)i * Math.PI / 2.0 + d3 * 1.4;
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(0.48, d10, 1.22);
                    ManorStandaloneSpellModels.Point point3 = RemainingSpellModels.polar(0.88 + d4 * 0.35, d10, 1.22 + Math.sin(d * 0.18 + (double)i) * 0.12);
                    mesh.rod(point, point3, 0.075, 13146690);
                    mesh.gem(point3, 0.13, 0.23, i % 2 == 0 ? 8112895 : 14022655);
                }
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 1.22, 0.0), 1.0 + d5 * 0.42, 0.055, 15189357);
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.16, 0.0), 1.48 + d5 * 0.75, 0.04, 5224703);
                break;
            }
            case GILDED_VERDICT: {
                mesh.rod(new ManorStandaloneSpellModels.Point(0.0, 0.28, 0.0), new ManorStandaloneSpellModels.Point(0.0, 3.18, 0.0), 0.13, 5916449);
                mesh.leaf(new ManorStandaloneSpellModels.Point(0.0, 1.0, 0.0), new ManorStandaloneSpellModels.Point(0.0, 3.62 + d5 * 0.45, 0.0), 0.52, 16108370);
                mesh.rod(new ManorStandaloneSpellModels.Point(-1.28, 2.45, 0.0), new ManorStandaloneSpellModels.Point(1.28, 2.45, 0.0), 0.08, 16773283);
                for (int n2 : new int[]{-1, 1}) {
                    ManorStandaloneSpellModels.Point point = new ManorStandaloneSpellModels.Point((double)n2 * 1.02, 2.43, 0.0);
                    ManorStandaloneSpellModels.Point point4 = new ManorStandaloneSpellModels.Point((double)n2 * 1.02, 1.62 + d5 * (double)n2 * 0.18, 0.0);
                    mesh.rod(point, point4, 0.025, 14068020);
                    mesh.prism(point4.add(new ManorStandaloneSpellModels.Point(0.0, -0.08, 0.0)), 0.42, 0.3, 0.13, 8, 13212459);
                }
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 3.0, 0.0), 0.82 + d4 * 0.22, 0.045, 16774852);
                break;
            }
            case TIDAL_EDICT: {
                int n3;
                mesh.rod(new ManorStandaloneSpellModels.Point(0.0, 0.16, -0.72), new ManorStandaloneSpellModels.Point(0.0, 0.74, 2.18 + d4), 0.105, 2247800);
                for (n3 = -1; n3 <= 1; ++n3) {
                    ManorStandaloneSpellModels.Point point = new ManorStandaloneSpellModels.Point((double)n3 * 0.38, 0.72, 1.45 + d4);
                    ManorStandaloneSpellModels.Point point5 = new ManorStandaloneSpellModels.Point((double)n3 * 0.52, 0.82 + (n3 == 0 ? 0.24 : 0.0), 2.82 + d4 + d5 * 0.65);
                    mesh.rod(point, point5, 0.085, n3 == 0 ? 0xD9FFFF : 5029345);
                    mesh.leaf(point, point5, 0.16, 10152703);
                }
                for (n3 = -2; n3 <= 2; ++n3) {
                    double d11 = (double)n3 * 0.72;
                    mesh.leaf(new ManorStandaloneSpellModels.Point(d11, 0.12, -0.6), new ManorStandaloneSpellModels.Point(d11 * 1.25, 1.05 + 0.18 * (double)(2 - Math.abs(n3)), 0.45 + d5), 0.32, n3 % 2 == 0 ? 2982837 : 6474728);
                }
                break;
            }
            case FROSTGLASS_LANCE: {
                int n4;
                double d12 = 1.25 + d4 * 2.1;
                mesh.rod(new ManorStandaloneSpellModels.Point(0.0, 1.1, -1.7), new ManorStandaloneSpellModels.Point(0.0, 1.1, d12), 0.11, 13235711);
                for (n4 = 0; n4 < 4; ++n4) {
                    double d13 = (double)n4 * Math.PI / 2.0;
                    ManorStandaloneSpellModels.Point point = new ManorStandaloneSpellModels.Point(Math.cos(d13) * 0.38, 1.1 + Math.sin(d13) * 0.38, d12 - 0.72);
                    ManorStandaloneSpellModels.Point point6 = new ManorStandaloneSpellModels.Point(0.0, 1.1, d12 + 1.18 + d5 * 0.52);
                    mesh.leaf(point, point6, 0.24, n4 % 2 == 0 ? 10938879 : 15203327);
                }
                for (n4 = 0; n4 < 4; ++n4) {
                    mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.1, -1.2 - (double)n4 * 0.48), 0.08 - (double)n4 * 0.012, 0.16, 7589874);
                }
                break;
            }
            case CAMELLIA_SANCTUARY: {
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.15, 0.0), 2.15, 0.1, 5913644);
                for (int i = 0; i < 8; ++i) {
                    double d14 = (double)i * Math.PI / 4.0;
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(2.05, d14, 0.18);
                    ManorStandaloneSpellModels.Point point7 = RemainingSpellModels.polar(1.52, d14, 2.45 + d4 * 0.35);
                    mesh.rod(point, point7, 0.095, 6979138);
                    mesh.flower(point7, 0.44, 5, 16167623, 16773590, d14);
                    mesh.rod(point7, new ManorStandaloneSpellModels.Point(0.0, 3.05, 0.0), 0.05, 14069594);
                }
                mesh.flower(new ManorStandaloneSpellModels.Point(0.0, 2.92, 0.0), 0.92, 8, 16040917, 16774078, -d3 * 0.2);
                break;
            }
            case REPULSOR_DISCHARGE: {
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.05, 0.0), 0.38, 0.58, 9099519);
                for (int i = 0; i < 6; ++i) {
                    double d15 = (double)i * Math.PI / 3.0 + d3;
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(0.58, d15, 1.05);
                    ManorStandaloneSpellModels.Point point8 = RemainingSpellModels.polar(1.05 + d4 * 1.12, d15, 1.05 + (double)(i % 2) * 0.14);
                    mesh.rod(point, point8, 0.1, 4610411);
                    mesh.box(point8, 0.25, 0.25, 0.25, i % 2 == 0 ? 13234175 : 6602219);
                }
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.12, 0.0), 1.1 + d5 * 2.0, 0.07, 7395071);
                break;
            }
            case VIRULENT_HARVEST: {
                mesh.rod(new ManorStandaloneSpellModels.Point(-0.65, 0.12, 0.0), new ManorStandaloneSpellModels.Point(0.55, 3.05, 0.0), 0.105, 0x3B2B23);
                ManorStandaloneSpellModels.Point point = new ManorStandaloneSpellModels.Point(0.42, 2.7, 0.0);
                ManorStandaloneSpellModels.Point point9 = new ManorStandaloneSpellModels.Point(1.35 + d4 * 0.22, 3.15, 0.0);
                ManorStandaloneSpellModels.Point point10 = new ManorStandaloneSpellModels.Point(2.35 + d5 * 0.65, 2.45, 0.0);
                mesh.leaf(point, point9, 0.38, 8826680);
                mesh.leaf(point9, point10, 0.28, 11983194);
                mesh.rod(point, point10, 0.035, 14085756);
                for (int i = 0; i < 4; ++i) {
                    mesh.gem(new ManorStandaloneSpellModels.Point(-0.42 + (double)i * 0.48, 1.25 + (double)(i % 2) * 0.3, 0.0), 0.13, 0.24, i % 2 == 0 ? 7557678 : 10401852);
                }
                mesh.gem(point.add(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.18)), 0.16, 0.28, 6651442);
                for (int n5 : new int[]{-1, 1}) {
                    mesh.rod(point.add(new ManorStandaloneSpellModels.Point(0.0, 0.0, (double)n5 * 0.11)), point10.add(new ManorStandaloneSpellModels.Point(0.0, 0.0, (double)n5 * 0.11)), 0.025, n5 < 0 ? 7048498 : 13099881);
                }
                break;
            }
            case ORCHARD_AEGIS: {
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.35, 0.0), 0.52, 0.72, 15185996);
                for (int i = 0; i < 6; ++i) {
                    double d16 = (double)i * Math.PI / 3.0 + d3 * 0.22;
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(0.72, d16, 0.42);
                    ManorStandaloneSpellModels.Point point11 = RemainingSpellModels.polar(1.78 + d4 * 0.18, d16, 2.28);
                    mesh.leaf(point, point11, 0.66, i % 2 == 0 ? 14130745 : 15779675);
                    mesh.rod(point, point11, 0.038, 16773288);
                    mesh.leaf(point11, point11.add(RemainingSpellModels.polar(0.55, d16 + 0.45, 0.28)), 0.22, 6525246);
                }
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.28, 0.0), 1.82 + d5 * 0.34, 0.055, 16374645);
                break;
            }
            case CAMELLIA_REBIRTH: {
                mesh.prism(new ManorStandaloneSpellModels.Point(0.0, 0.14, 0.0), 0.17, 0.11, 1.45 + d4 * 0.55, 6, 0x477443);
                mesh.flower(new ManorStandaloneSpellModels.Point(0.0, 1.3, 0.0), 1.18, 8, 14121122, 16769483, d3 * 0.12);
                double d17 = 2.0 + d4 * 1.35;
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, d17, 0.0), 0.28, 0.58, 16773570);
                mesh.flower(new ManorStandaloneSpellModels.Point(0.0, d17 + 0.24, 0.0), 0.62 + d4 * 0.4, 6, 15769790, 0xFFF4DD, -d3 * 0.3);
                for (int i = 0; i < 5; ++i) {
                    mesh.gem(RemainingSpellModels.polar(0.78, (double)i * Math.PI * 0.4 + d3, 1.35 + d4 * 1.1), 0.065, 0.11, 16308600);
                }
                break;
            }
            case FLESH_BLOOM: {
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.38, 0.0), 0.46 + Math.sin(d * 0.45) * 0.045, 0.72, 12003148);
                for (int i = 0; i < 8; ++i) {
                    double d18 = (double)i * Math.PI / 4.0;
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(0.34, d18, 1.35);
                    ManorStandaloneSpellModels.Point point12 = RemainingSpellModels.polar(1.25 + d4 * 0.45, d18, 2.2 - (double)(i % 2) * 0.28);
                    ManorStandaloneSpellModels.Point point13 = RemainingSpellModels.polar(1.55 + d5 * 0.2, d18, 0.72);
                    mesh.rod(point, point12, 0.095, 15249576);
                    mesh.rod(point12, point13, 0.07, 9381698);
                    mesh.leaf(point13, point12, 0.25, i % 2 == 0 ? 13124190 : 8069176);
                }
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.18, 0.0), 1.55, 0.055, 5969194);
                break;
            }
            case SANGUINE_SUTURE: {
                double d19;
                int n6;
                double d20 = d4 * 2.1;
                mesh.rod(new ManorStandaloneSpellModels.Point(-1.6, 0.82, -0.8), new ManorStandaloneSpellModels.Point(1.6, 0.82, -0.8), 0.08, 6624812);
                mesh.rod(new ManorStandaloneSpellModels.Point(-1.6, 0.82, 0.8), new ManorStandaloneSpellModels.Point(1.6, 0.82, 0.8), 0.08, 6624812);
                for (n6 = -3; n6 <= 3; ++n6) {
                    d19 = (double)n6 * 0.45;
                    mesh.rod(new ManorStandaloneSpellModels.Point(d19 - 0.2, 0.9, -0.72), new ManorStandaloneSpellModels.Point(d19 + 0.2, 1.18, 0.72), 0.025, 16766169);
                    mesh.rod(new ManorStandaloneSpellModels.Point(d19 + 0.2, 1.18, -0.72), new ManorStandaloneSpellModels.Point(d19 - 0.2, 0.9, 0.72), 0.025, 15100802);
                }
                mesh.rod(new ManorStandaloneSpellModels.Point(-0.2, 1.72, -1.8 + d20), new ManorStandaloneSpellModels.Point(0.2, 1.72, 1.4 + d20), 0.055, 14279912);
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.72, 1.58 + d20), 0.09, 0.22, 0xFFFFFF);
                for (n6 = 0; n6 < 12; ++n6) {
                    d19 = (double)n6 * Math.PI / 6.0;
                    double d21 = (double)(n6 + 1) * Math.PI / 6.0;
                    ManorStandaloneSpellModels.Point point = new ManorStandaloneSpellModels.Point(-1.36, 1.28 + Math.cos(d19) * 0.34, Math.sin(d19) * 0.34);
                    ManorStandaloneSpellModels.Point point14 = new ManorStandaloneSpellModels.Point(-1.36, 1.28 + Math.cos(d21) * 0.34, Math.sin(d21) * 0.34);
                    mesh.rod(point, point14, 0.018, n6 % 2 == 0 ? 16766169 : 13126760);
                }
                break;
            }
            case CINDER_BREATH: {
                for (int n7 : new int[]{-1, 1}) {
                    mesh.leaf(new ManorStandaloneSpellModels.Point((double)n7 * 0.2, 0.86, -0.9), new ManorStandaloneSpellModels.Point((double)n7 * (1.2 + d4 * 0.25), 1.25, 0.45), 0.38, 4925729);
                    mesh.leaf(new ManorStandaloneSpellModels.Point((double)n7 * 0.2, 0.7, -0.9), new ManorStandaloneSpellModels.Point((double)n7 * (1.05 + d4 * 0.22), 0.25, 0.5), 0.32, 7941921);
                    for (int i = 0; i < 3; ++i) {
                        mesh.gem(new ManorStandaloneSpellModels.Point((double)n7 * (0.38 + (double)i * 0.25), 0.76, -0.1 + (double)i * 0.22), 0.075, 0.16, 15784374);
                    }
                }
                for (int i = 0; i < 9; ++i) {
                    double d22 = 0.45 + (double)(i % 3) * (0.8 + d4 * 0.7);
                    double d23 = 0.18 + d22 * 0.28;
                    mesh.gem(new ManorStandaloneSpellModels.Point(Math.sin((double)i * 2.4) * d23, 0.78 + Math.cos((double)i * 1.7) * d23 * 0.35, d22), 0.09 + (double)(i % 2) * 0.04, 0.2, i % 3 == 0 ? 16769674 : 16738862);
                }
                break;
            }
            case ION_RAIL: {
                double d24 = phase == SpellVisualRules.Phase.CHARGE ? d4 * 0.8 : 1.1 + d5 * 0.75;
                for (int n8 : new int[]{-1, 1}) {
                    mesh.rod(new ManorStandaloneSpellModels.Point((double)n8 * 0.34, 0.86, -1.8), new ManorStandaloneSpellModels.Point((double)n8 * 0.34, 0.86, 2.75 + d24), 0.075, 2572120);
                    mesh.rod(new ManorStandaloneSpellModels.Point((double)n8 * 0.58, 0.86, 2.2 + d24), new ManorStandaloneSpellModels.Point((double)n8 * 0.34, 0.86, 3.35 + d24), 0.055, 10218239);
                }
                for (int i = 0; i < 5; ++i) {
                    double d25 = -1.4 + (double)i * 0.78 + d24 * 0.35;
                    mesh.rod(new ManorStandaloneSpellModels.Point(-0.34, 0.86, d25), new ManorStandaloneSpellModels.Point(0.34, 0.86, d25), 0.035, i % 2 == 0 ? 14202712 : 7458559);
                }
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 0.86, 3.55 + d24), 0.16, 0.34, 0xE8FFFF);
                break;
            }
            case STATIC_LATTICE: {
                int[][] nArrayArray;
                double d26 = 1.0 + d4 * 0.42;
                ManorStandaloneSpellModels.Point[] pointArray = new ManorStandaloneSpellModels.Point[8];
                for (int i = 0; i < 8; ++i) {
                    pointArray[i] = new ManorStandaloneSpellModels.Point((i & 1) == 0 ? -d26 : d26, (i & 2) == 0 ? 0.35 : 2.35, (i & 4) == 0 ? -d26 : d26);
                    mesh.gem(pointArray[i], 0.11, 0.2, i % 2 == 0 ? 9297151 : 16773258);
                }
                for (int[] nArray : nArrayArray = new int[][]{{0, 1}, {2, 3}, {4, 5}, {6, 7}, {0, 2}, {1, 3}, {4, 6}, {5, 7}, {0, 4}, {1, 5}, {2, 6}, {3, 7}}) {
                    mesh.rod(pointArray[nArray[0]], pointArray[nArray[1]], 0.035 + d5 * 0.015, nArray[0] % 2 == 0 ? 5215944 : 14203213);
                }
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.35, 0.0), 0.26, 0.42, 14219263);
                break;
            }
            case EMERGENCY_REBOOT: {
                mesh.box(new ManorStandaloneSpellModels.Point(0.0, 1.15, 0.0), 1.0, 1.0, 0.72, 2111818);
                mesh.box(new ManorStandaloneSpellModels.Point(0.0, 1.15, 0.39), 0.22, 0.72, 0.08, 7595969);
                mesh.box(new ManorStandaloneSpellModels.Point(0.0, 1.15, 0.39), 0.72, 0.22, 0.08, 7595969);
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 1.15, 0.0), 1.28 + d4 * 0.28, 0.06, 15779419);
                for (int i = 0; i < 4; ++i) {
                    double d27 = (double)i * Math.PI / 2.0 - d3 * 1.8;
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(1.45, d27, 1.15 + Math.sin(d * 0.24 + (double)i) * 0.18);
                    mesh.box(point, 0.24, 0.18, 0.28, i % 2 == 0 ? 10415064 : 5733777);
                    mesh.rod(point, point.add(RemainingSpellModels.polar(0.36, d27 + 0.7, 0.22)), 0.026, 15323255);
                }
                break;
            }
            case GOLDEN_HARVEST: {
                mesh.rod(new ManorStandaloneSpellModels.Point(-0.72, 0.15, 0.0), new ManorStandaloneSpellModels.Point(0.42, 3.0, 0.0), 0.095, 6964002);
                ManorStandaloneSpellModels.Point point = new ManorStandaloneSpellModels.Point(0.28, 2.65, 0.0);
                ManorStandaloneSpellModels.Point point15 = new ManorStandaloneSpellModels.Point(1.36 + d4 * 0.18, 3.08, 0.0);
                ManorStandaloneSpellModels.Point point16 = new ManorStandaloneSpellModels.Point(2.28 + d5 * 0.52, 2.35, 0.0);
                mesh.leaf(point, point15, 0.34, 14858307);
                mesh.leaf(point15, point16, 0.23, 16773540);
                mesh.gem(point.add(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.16)), 0.15, 0.26, 16110443);
                for (int n9 : new int[]{-1, 1}) {
                    mesh.rod(point.add(new ManorStandaloneSpellModels.Point(0.0, 0.0, (double)n9 * 0.105)), point16.add(new ManorStandaloneSpellModels.Point(0.0, 0.0, (double)n9 * 0.105)), 0.022, n9 < 0 ? 12685101 : 0xFFF0A0);
                }
                for (int i = 0; i < 5; ++i) {
                    double d28 = -1.25 + (double)i * 0.62;
                    ManorStandaloneSpellModels.Point point17 = new ManorStandaloneSpellModels.Point(d28, 0.12, -0.35 + (double)(i % 2) * 0.7);
                    ManorStandaloneSpellModels.Point point18 = point17.add(new ManorStandaloneSpellModels.Point(0.0, 1.45 + (double)(i % 3) * 0.18, 0.0));
                    mesh.rod(point17, point18, 0.035, 10975784);
                    for (int n10 : new int[]{-1, 1}) {
                        mesh.leaf(point18.add(new ManorStandaloneSpellModels.Point(0.0, -0.45, 0.0)), point18.add(new ManorStandaloneSpellModels.Point((double)n10 * 0.26, -0.05, 0.0)), 0.09, 14991187);
                    }
                }
                break;
            }
            case VINTAGE_VORTEX: {
                mesh.barrel(new ManorStandaloneSpellModels.Point(0.0, 1.3, 0.0), 0.65, 1.4);
                mesh.helix(2.1, 2.8, d3, 12481362);
                for (int i = 0; i < 3; ++i) {
                    mesh.gem(RemainingSpellModels.polar(1.85, d3 + (double)i * Math.PI * 2.0 / 3.0, 1.4), 0.18, 0.4, 9062270);
                }
                break;
            }
            case PRISMATIC_BLIGHT: {
                mesh.mushroom(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 1.25, 7316058);
                for (int i = 0; i < n; ++i) {
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(2.1, (double)i * Math.PI * 2.0 / (double)n, 0.15);
                    mesh.mushroom(point, 0.45 + (double)(i % 2) * 0.15, i % 2 == 0 ? 12288078 : 8546721);
                    mesh.gem(point.add(new ManorStandaloneSpellModels.Point(0.0, 1.8 + Math.sin(d3 + (double)i) * 0.2, 0.0)), 0.12, 0.22, 12115557);
                }
                break;
            }
            case CASK_RUPTURE: {
                for (int i = 0; i < 8; ++i) {
                    double d29 = (double)i * Math.PI / 4.0;
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(0.8 + Math.min(d, 12.0) * 0.025, d29, 0.4);
                    mesh.rod(point, point.add(RemainingSpellModels.polar(0.4, d29, 1.3)), 0.17, 6635056);
                }
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.6, 0.0), 1.05, 0.09, 3489872);
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 1.55, 0.0), 1.37, 0.07, 12950360);
                mesh.flower(new ManorStandaloneSpellModels.Point(0.0, 0.6, 0.0), 1.2, 7, 15242558, 16764534, d3);
                break;
            }
            case ABYSSAL_GEYSER: {
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.18, 0.0), 1.65, 0.13, 3489872);
                for (int i = 0; i < n; ++i) {
                    double d30 = (double)i * Math.PI * 2.0 / (double)n;
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(0.6, d30, 0.2);
                    ManorStandaloneSpellModels.Point point19 = RemainingSpellModels.polar(1.0, d30, 3.2 + Math.sin(d3 + (double)i) * 0.4);
                    mesh.rod(point, point19, 0.16, i % 2 == 0 ? 4361395 : 7718868);
                    mesh.gem(point19, 0.18, 0.4, 11724005);
                }
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.8, 0.0), 0.4, 1.6, 3695776);
                break;
            }
            case BREACH_SHELL: {
                mesh.prism(new ManorStandaloneSpellModels.Point(0.0, 0.2, 0.0), 0.58, 0.62, 1.5, 8, 3489872);
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 2.0, 0.0), 0.58, 0.75, 13992516);
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.45, 0.0), 0.65, 0.1, 12950360);
                for (int i = 0; i < 4; ++i) {
                    double d31 = (double)i * Math.PI / 2.0;
                    mesh.leaf(RemainingSpellModels.polar(0.5, d31, 0.3), RemainingSpellModels.polar(1.15, d31, 0.12), 0.27, 3489872);
                }
                break;
            }
            case HEARTH_FEAST: {
                int n11;
                mesh.prism(new ManorStandaloneSpellModels.Point(0.0, 0.12, 0.0), 1.45, 1.25, 0.18, 12, 3489872);
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.34, 0.0), 1.43, 0.08, 12950360);
                mesh.box(new ManorStandaloneSpellModels.Point(0.0, 0.78, 0.0), 1.3, 0.5, 0.72, 12486735);
                for (n11 = 0; n11 < 5; ++n11) {
                    mesh.rod(new ManorStandaloneSpellModels.Point(-0.48 + (double)n11 * 0.24, 1.02, -0.35), new ManorStandaloneSpellModels.Point(-0.48 + (double)n11 * 0.24, 1.08, 0.35), 0.025, 15847834);
                }
                for (n11 = 0; n11 < 3; ++n11) {
                    mesh.gem(RemainingSpellModels.polar(0.92, (double)n11 * Math.PI * 2.0 / 3.0, 0.55), 0.24, 0.25, 4489043);
                }
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 1.8, 0.0), 0.72, 0.04, 15319160);
                break;
            }
            case ELIXIR_RAIN: {
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 2.85, 0.0), 1.8, 0.07, 12950360);
                for (int i = 0; i < n; ++i) {
                    double d32 = (double)i * Math.PI * 2.0 / (double)n + d3 * 0.3;
                    mesh.bottle(RemainingSpellModels.polar(1.5, d32, 2.35), 0.24, 7516813);
                    double d33 = d * 0.04 + (double)i * 0.37;
                    d33 -= Math.floor(d33);
                    double d34 = Math.max(0.05, Math.sin(d33 * Math.PI));
                    mesh.gem(RemainingSpellModels.polar(1.3, d32, 2.1 - d33 * 1.6), 0.13 * d34, 0.27 * d34, 7915693);
                }
                break;
            }
            case WHITEOUT_EDICT: {
                mesh.box(new ManorStandaloneSpellModels.Point(0.0, 1.65, 0.0), 1.8, 1.9, 0.18, 3489872);
                for (int n12 : new int[]{-1, 1}) {
                    mesh.box(new ManorStandaloneSpellModels.Point(0.0, 1.65, (double)n12 * 0.12), 1.57, 1.65, 0.1, 6064564);
                    mesh.snowflake(new ManorStandaloneSpellModels.Point(0.0, 1.7, (double)n12 * 0.2), 0.67, 12114399);
                }
                for (int i = 0; i < 4; ++i) {
                    mesh.gem(RemainingSpellModels.polar(1.6, d3 + (double)i * Math.PI / 2.0, 0.6), 0.18, 0.45, 7580881);
                }
                break;
            }
            case SUGAR_HAIL: {
                for (int i = 0; i < n + 2; ++i) {
                    double d35 = d * 0.045 + (double)i * 0.19;
                    d35 -= Math.floor(d35);
                    double d36 = 0.1 + 0.9 * Math.sin(d35 * Math.PI);
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(0.65 + (double)(i % 3) * 0.65, (double)i * 2.4 + d3 * 0.2, 3.1 - d35 * 2.6);
                    mesh.gem(point, (0.22 + (double)(i % 2) * 0.08) * d36, 0.35 * d36, i % 2 == 0 ? 12639981 : 7316689);
                    mesh.ring(point, 0.28 * d36, 0.025 * d36, 12114399);
                }
                break;
            }
            case GLACIAL_PRISM: {
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.24, 0.0), 1.78, 0.055, 12114399);
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.6, 0.0), 0.64, 1.1, 5672113);
                for (int i = 0; i < 6; ++i) {
                    double d37 = (double)i * Math.PI / 3.0 + d3 * 0.2;
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(1.55, d37, 0.3);
                    ManorStandaloneSpellModels.Point point20 = RemainingSpellModels.polar(1.65, d37, 2.75);
                    mesh.leaf(point, point20, 0.62, i % 2 == 0 ? 8766933 : 4684697);
                    mesh.rod(point, point20, 0.035, 12114399);
                }
                break;
            }
            case ROYAL_SHATTER: {
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.7, 0.0), 1.35, 0.14, 12950360);
                for (int i = 0; i < 7; ++i) {
                    double d38 = (double)i * Math.PI * 2.0 / 7.0;
                    mesh.leaf(RemainingSpellModels.polar(1.3, d38, 0.65), RemainingSpellModels.polar(1.5 + Math.min(d, 18.0) * 0.02, d38, 1.6 + (double)(i % 2) * 0.8), 0.36, 7563177);
                    mesh.gem(RemainingSpellModels.polar(2.3, d38 + d3, 0.8), 0.15, 0.34, 11515878);
                }
                break;
            }
            case BRIAR_PROCESSION: {
                for (int i = 0; i < 5; ++i) {
                    ManorStandaloneSpellModels.Point point = new ManorStandaloneSpellModels.Point(Math.sin((double)i * 1.7) * 0.5, 0.12, (double)i * 0.9 - 1.8);
                    mesh.rod(point, point.add(new ManorStandaloneSpellModels.Point(0.0, 1.2 + (double)i * 0.16, 0.0)), 0.08, 4489043);
                    mesh.leaf(point.add(new ManorStandaloneSpellModels.Point(0.0, 0.55, 0.0)), point.add(new ManorStandaloneSpellModels.Point(-0.65, 1.15, 0.05)), 0.24, 6590289);
                    mesh.leaf(point.add(new ManorStandaloneSpellModels.Point(0.0, 0.8, 0.0)), point.add(new ManorStandaloneSpellModels.Point(0.55, 1.42, 0.1)), 0.23, 9087845);
                    mesh.gem(point.add(new ManorStandaloneSpellModels.Point(0.0, 1.3 + (double)i * 0.16, 0.0)), 0.16, 0.27, 11888777);
                }
                break;
            }
            case SCARLET_POLLEN: {
                mesh.flower(new ManorStandaloneSpellModels.Point(0.0, 0.7, 0.0), 1.4, 8, 11880311, 14917799, d3 * 0.1);
                for (int i = 0; i < n; ++i) {
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(2.0, (double)i * Math.PI * 2.0 / (double)n + d3, 1.1 + (double)(i % 2) * 0.8);
                    mesh.gem(point, 0.2, 0.23, 14989158);
                    mesh.leaf(point, point.add(new ManorStandaloneSpellModels.Point(0.2, 0.45, 0.0)), 0.17, 14188712);
                }
                break;
            }
            case INTOXICATING_TOAST: {
                for (int i = 0; i < 2; ++i) {
                    ManorStandaloneSpellModels.Point point = new ManorStandaloneSpellModels.Point(i == 0 ? -0.65 : 0.65, 0.35, 0.0);
                    mesh.prism(point, 0.4, 0.3, 0.12, 8, 12950360);
                    mesh.rod(point, point.add(new ManorStandaloneSpellModels.Point(0.0, 0.85, 0.0)), 0.075, 12950360);
                    mesh.prism(point.add(new ManorStandaloneSpellModels.Point(0.0, 0.8, 0.0)), 0.15, 0.48, 0.65, 8, 11498826);
                    mesh.prism(point.add(new ManorStandaloneSpellModels.Point(0.0, 1.43, 0.0)), 0.39, 0.39, 0.035, 8, 8538458);
                    mesh.ring(point.add(new ManorStandaloneSpellModels.Point(0.0, 1.45, 0.0)), 0.48, 0.04, 12950360);
                }
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 2.25 + Math.sin(d3) * 0.1, 0.0), 0.22, 0.37, 14071915);
                break;
            }
            case VINTAGE_MINEFIELD: {
                for (int i = 0; i < 4; ++i) {
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(1.5, (double)i * Math.PI / 2.0, 0.52);
                    mesh.barrel(point, 0.4, 0.82);
                    mesh.rod(point.add(new ManorStandaloneSpellModels.Point(0.0, 0.42, 0.0)), point.add(new ManorStandaloneSpellModels.Point(0.22, 0.74, 0.0)), 0.045, 3489872);
                    mesh.gem(point.add(new ManorStandaloneSpellModels.Point(0.22, 0.82, 0.0)), 0.11, 0.17, 15311195);
                }
                break;
            }
            case SPORE_ERUPTION: {
                mesh.mushroom(new ManorStandaloneSpellModels.Point(0.0, 0.1, 0.0), 1.5, 9598274);
                for (int i = 0; i < n; ++i) {
                    double d39 = (double)i * Math.PI * 2.0 / (double)n;
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(1.4, d39, 2.4 + Math.sin(d3 + (double)i) * 0.25);
                    mesh.rod(RemainingSpellModels.polar(0.25, d39, 0.4), point, 0.07, 6912842);
                    mesh.gem(point, 0.26, 0.38, 13092979);
                }
                break;
            }
            case ROTTING_REQUIEM: {
                mesh.prism(new ManorStandaloneSpellModels.Point(0.0, 0.15, 0.0), 0.7, 0.7, 2.4, 6, 4342096);
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 2.8, 0.0), 0.55, 0.65, 8477338);
                for (int i = 0; i < 5; ++i) {
                    double d40 = (double)i * Math.PI * 2.0 / 5.0 + d3 * 0.4;
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(1.6, d40, 1.5);
                    mesh.prism(point, 0.28, 0.13, 0.65, 6, 6911057);
                    mesh.gem(point.add(new ManorStandaloneSpellModels.Point(0.0, -0.13, 0.0)), 0.09, 0.2, 11965630);
                }
                break;
            }
            case LIVING_MULCH: {
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.11, 0.0), 1.64, 0.07, 9138252);
                mesh.prism(new ManorStandaloneSpellModels.Point(0.0, 0.08, 0.0), 1.45, 1.1, 0.6, 8, 6635056);
                for (int i = 0; i < n; ++i) {
                    double d41 = (double)i * Math.PI * 2.0 / (double)n;
                    mesh.rod(RemainingSpellModels.polar(0.65, d41, 0.4), RemainingSpellModels.polar(2.1, d41, 0.15), 0.1, 6902073);
                    mesh.leaf(RemainingSpellModels.polar(0.8, d41, 0.65), RemainingSpellModels.polar(1.8, d41, 1.75), 0.45, 4489043);
                }
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.0, 0.0), 0.4, 0.7, 10996340);
                break;
            }
            case SOLAR_ORCHARD: {
                mesh.rod(new ManorStandaloneSpellModels.Point(0.0, 0.15, 0.0), new ManorStandaloneSpellModels.Point(0.0, 2.25, 0.0), 0.16, 6635056);
                for (int i = 0; i < 5; ++i) {
                    double d42 = (double)i * Math.PI * 2.0 / 5.0;
                    ManorStandaloneSpellModels.Point point = RemainingSpellModels.polar(1.25, d42, 1.6 + (double)(i % 2) * 0.6);
                    mesh.rod(new ManorStandaloneSpellModels.Point(0.0, 1.0, 0.0), point, 0.075, 12950360);
                    mesh.gem(point.add(new ManorStandaloneSpellModels.Point(0.0, -0.15, 0.0)), 0.29, 0.35, 13080138);
                    mesh.leaf(point, point.add(RemainingSpellModels.polar(0.62, d42, 0.3)), 0.26, 4489043);
                }
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 2.85, 0.0), 0.9, 0.07, 12950360);
                break;
            }
            case JUDGMENT_SEED: {
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.75, 0.0), 0.5, 0.9, 13938517);
                for (int i = 0; i < 5; ++i) {
                    double d43 = d3 * 0.3 + (double)i * Math.PI * 2.0 / 5.0;
                    mesh.leaf(RemainingSpellModels.polar(0.65, d43, 0.6), RemainingSpellModels.polar(1.45, d43, 2.75), 0.32, 8749147);
                    mesh.rod(RemainingSpellModels.polar(0.4, d43, 0.7), RemainingSpellModels.polar(0.65, d43, 2.5), 0.028, 12950360);
                }
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.42, 0.0), 1.1, 0.08, 3489872);
                break;
            }
            case UNDERTOW_SNARE: {
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.2, 0.0), 1.75, 0.15, 4748933);
                for (int i = 0; i < 6; ++i) {
                    double d44 = (double)i * Math.PI / 3.0 + d3 * 0.1;
                    mesh.rod(RemainingSpellModels.polar(1.7, d44, 0.25), RemainingSpellModels.polar(0.65, d44 + 0.6, 0.85), 0.07, 3489872);
                    mesh.leaf(RemainingSpellModels.polar(1.55, d44, 0.3), RemainingSpellModels.polar(0.85, d44 + 0.35, 1.7), 0.3, 7453123);
                }
                mesh.helix(0.65, 1.4, -d3, 3766412);
                break;
            }
            case ABYSSAL_CARAPACE: {
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.33, 0.0), 1.62, 0.065, 8110785);
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.25, 0.0), 0.55, 0.65, 3968389);
                for (int i = 0; i < 7; ++i) {
                    double d45 = (double)i * Math.PI * 2.0 / 7.0;
                    mesh.leaf(RemainingSpellModels.polar(1.4, d45, 0.4), RemainingSpellModels.polar(0.72, d45, 2.6), 0.72, i % 2 == 0 ? 3959156 : 6458261);
                    mesh.rod(RemainingSpellModels.polar(1.5, d45, 0.38), RemainingSpellModels.polar(0.76, d45, 2.5), 0.035, 12950360);
                }
                break;
            }
            case RIPTIDE_CORRIDOR: {
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.18, 0.0), 1.52, 0.052, 7784662);
                for (int i = 0; i < 4; ++i) {
                    double d46 = (double)i * 1.05 - 1.6;
                    mesh.leaf(new ManorStandaloneSpellModels.Point(-1.3, 0.2, d46), new ManorStandaloneSpellModels.Point(-0.75, 2.05, d46 + 0.4), 0.4, 3964070);
                    mesh.leaf(new ManorStandaloneSpellModels.Point(1.3, 0.2, d46), new ManorStandaloneSpellModels.Point(0.75, 2.05, d46 + 0.4), 0.4, 8042965);
                    mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 0.25, d46), 0.19, 0.25, 12114399);
                }
                break;
            }
            case TIDAL_LOTUS: {
                mesh.flower(new ManorStandaloneSpellModels.Point(0.0, 0.45, 0.0), 1.85, 9, 3374983, 7517629, d3 * 0.15);
                mesh.flower(new ManorStandaloneSpellModels.Point(0.0, 0.8, 0.0), 1.05, 6, 8833977, 12310192, -d3 * 0.2);
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.3, 0.0), 0.32, 0.65, 14072945);
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.2, 0.0), 2.2, 0.045, 12950360);
                break;
            }
            default: {
                throw new IllegalStateException("Missing model: " + spell);
            }
        }
        return mesh.count;
    }

    public static int drawScroll(int n, double d, double d2, boolean bl, ManorStandaloneSpellModels.Sink sink) {
        if (n < 1 || n > 4 || sink == null || !Double.isFinite(d) || d < 0.0 || !Double.isFinite(d2) || d2 < 0.0 || d2 > 1.0 || bl && d2 >= 1.0) {
            return 0;
        }
        Mesh mesh = new Mesh(sink, d, bl ? 1.0 - RemainingSpellModels.ease(d2) : RemainingSpellModels.ease(d2), false);
        switch (n) {
            case 1: {
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 0.13, 0.42, 10447296);
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, -0.13, 0.0), 0.17, 0.035, 12950360);
                for (int i = 0; i < 4; ++i) {
                    mesh.leaf(RemainingSpellModels.polar(0.06, (double)i * Math.PI / 2.0, -0.12), RemainingSpellModels.polar(0.25, (double)i * Math.PI / 2.0, -0.38), 0.06, 13285080);
                }
                break;
            }
            case 2: {
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.5, 0.0), 0.7, 0.07, 12950360);
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.05, 0.0), 0.28, 0.8, 8737971);
                for (int i = 0; i < 5; ++i) {
                    mesh.gem(RemainingSpellModels.polar(0.7, (double)i * Math.PI * 2.0 / 5.0 + d * 0.025, 0.65), 0.09, 0.25, 11902415);
                }
                break;
            }
            case 3: {
                mesh.box(new ManorStandaloneSpellModels.Point(0.0, 0.8, 0.0), 1.2, 0.2, 0.72, 5784163);
                mesh.box(new ManorStandaloneSpellModels.Point(0.0, 0.92, 0.0), 1.1, 0.06, 0.65, 14273973);
                mesh.rod(new ManorStandaloneSpellModels.Point(0.0, 0.98, -0.34), new ManorStandaloneSpellModels.Point(0.0, 0.98, 0.34), 0.035, 12950360);
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 1.55, 0.0), 0.26, 0.5, 11757465);
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 1.4, 0.0), 0.75, 0.045, 12950360);
                for (int i = 0; i < 6; ++i) {
                    mesh.leaf(RemainingSpellModels.polar(0.65, (double)i * Math.PI / 3.0, 0.9), RemainingSpellModels.polar(0.82, (double)i * Math.PI / 3.0, 1.8), 0.16, 8412575);
                }
                break;
            }
            case 4: {
                for (int i = 0; i < 4; ++i) {
                    double d3 = (double)i * Math.PI / 2.0 + d * 0.035;
                    mesh.leaf(RemainingSpellModels.polar(0.65, d3, 0.12), RemainingSpellModels.polar(0.25, d3 + 0.35, 0.75), 0.14, 9794484);
                    mesh.gem(RemainingSpellModels.polar(0.65, d3, 0.15), 0.11, 0.2, 13285005);
                }
                mesh.ring(new ManorStandaloneSpellModels.Point(0.0, 0.1, 0.0), 0.55, 0.035, 12950360);
                break;
            }
            default: {
                throw new IllegalStateException();
            }
        }
        return mesh.count;
    }

    private static double ease(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    private static ManorStandaloneSpellModels.Point polar(double d, double d2, double d3) {
        return new ManorStandaloneSpellModels.Point(Math.cos(d2) * d, d3, Math.sin(d2) * d);
    }

    private static final class Mesh {
        private final ManorStandaloneSpellModels.Sink output;
        private final double age;
        private final double opening;
        private final boolean reduced;
        private int count;

        private Mesh(ManorStandaloneSpellModels.Sink sink, double d, double d2, boolean bl) {
            this.output = sink;
            this.age = d;
            this.opening = d2;
            this.reduced = bl;
        }

        private void face(ManorStandaloneSpellModels.Point point, ManorStandaloneSpellModels.Point point2, ManorStandaloneSpellModels.Point point3, ManorStandaloneSpellModels.Point point4, int n) {
            ManorStandaloneSpellModels.Point point5 = point2.sub(point).cross(point3.sub(point));
            double d = point5.length();
            if (d < 1.0E-10) {
                throw new IllegalStateException("Degenerate spell model face");
            }
            if (++this.count > 2600) {
                throw new IllegalStateException("Spell model over budget");
            }
            double d2 = 0.65 + 0.35 * Math.abs((point5.x() * 0.4 + point5.y() * 0.8 + point5.z() * 0.42) / d);
            int n2 = 0;
            for (int n3 : new int[]{0, 8, 16}) {
                n2 |= Math.min(255, (int)((double)(n >> n3 & 0xFF) * d2)) << n3;
            }
            this.output.quad(this.reveal(point), this.reveal(point2), this.reveal(point3), this.reveal(point4), n2);
        }

        private ManorStandaloneSpellModels.Point reveal(ManorStandaloneSpellModels.Point point) {
            return new ManorStandaloneSpellModels.Point(point.x() * (0.78 + this.opening * 0.22), point.y() * (0.88 + this.opening * 0.12), point.z() * (0.78 + this.opening * 0.22));
        }

        private void prism(ManorStandaloneSpellModels.Point point, double d, double d2, double d3, int n, int n2) {
            int n3 = this.reduced ? Math.max(6, n) : Math.max(12, n);
            ManorStandaloneSpellModels.Point point2 = point.add(new ManorStandaloneSpellModels.Point(0.0, d3, 0.0));
            for (int i = 0; i < n3; ++i) {
                double d4 = (double)i * Math.PI * 2.0 / (double)n3;
                double d5 = (double)(i + 1) * Math.PI * 2.0 / (double)n3;
                ManorStandaloneSpellModels.Point point3 = point.add(RemainingSpellModels.polar(d, d4, 0.0));
                ManorStandaloneSpellModels.Point point4 = point.add(RemainingSpellModels.polar(d, d5, 0.0));
                ManorStandaloneSpellModels.Point point5 = point2.add(RemainingSpellModels.polar(d2, d4, 0.0));
                ManorStandaloneSpellModels.Point point6 = point2.add(RemainingSpellModels.polar(d2, d5, 0.0));
                this.face(point3, point4, point6, point5, n2);
                this.face(point, point4, point3, point3, n2);
                this.face(point2, point5, point6, point6, n2);
            }
        }

        private void gem(ManorStandaloneSpellModels.Point point, double d, double d2, int n) {
            int n2 = this.reduced ? 6 : 10;
            ManorStandaloneSpellModels.Point point2 = point.add(new ManorStandaloneSpellModels.Point(0.0, d2, 0.0));
            ManorStandaloneSpellModels.Point point3 = point.add(new ManorStandaloneSpellModels.Point(0.0, -d2, 0.0));
            for (int i = 0; i < n2; ++i) {
                ManorStandaloneSpellModels.Point point4 = point.add(RemainingSpellModels.polar(d, (double)i * Math.PI * 2.0 / (double)n2, 0.0));
                ManorStandaloneSpellModels.Point point5 = point.add(RemainingSpellModels.polar(d, (double)(i + 1) * Math.PI * 2.0 / (double)n2, 0.0));
                if (!this.reduced) {
                    ManorStandaloneSpellModels.Point point6 = point.add(RemainingSpellModels.polar(d * 0.72, (double)i * Math.PI * 2.0 / (double)n2, d2 * 0.36));
                    ManorStandaloneSpellModels.Point point7 = point.add(RemainingSpellModels.polar(d * 0.72, (double)(i + 1) * Math.PI * 2.0 / (double)n2, d2 * 0.36));
                    this.face(point2, point6, point7, point7, n);
                    this.face(point6, point4, point5, point7, n);
                } else {
                    this.face(point2, point4, point5, point5, n);
                }
                this.face(point3, point5, point4, point4, n);
            }
        }

        private void ring(ManorStandaloneSpellModels.Point point, double d, double d2, int n) {
            int n2 = this.reduced ? 16 : 24;
            int n3 = this.reduced ? 4 : 6;
            for (int i = 0; i < n2; ++i) {
                for (int j = 0; j < n3; ++j) {
                    double d3 = (double)i * Math.PI * 2.0 / (double)n2;
                    double d4 = (double)(i + 1) * Math.PI * 2.0 / (double)n2;
                    double d5 = (double)j * Math.PI * 2.0 / (double)n3;
                    double d6 = (double)(j + 1) * Math.PI * 2.0 / (double)n3;
                    this.face(point.add(RemainingSpellModels.polar(d + Math.cos(d5) * d2, d3, Math.sin(d5) * d2)), point.add(RemainingSpellModels.polar(d + Math.cos(d5) * d2, d4, Math.sin(d5) * d2)), point.add(RemainingSpellModels.polar(d + Math.cos(d6) * d2, d4, Math.sin(d6) * d2)), point.add(RemainingSpellModels.polar(d + Math.cos(d6) * d2, d3, Math.sin(d6) * d2)), n);
                }
            }
        }

        private void rod(ManorStandaloneSpellModels.Point point, ManorStandaloneSpellModels.Point point2, double d, int n) {
            int n2;
            ManorStandaloneSpellModels.Point point3;
            ManorStandaloneSpellModels.Point point4 = point3.cross(Math.abs((point3 = point2.sub(point).normal()).y()) < 0.9 ? new ManorStandaloneSpellModels.Point(0.0, 1.0, 0.0) : new ManorStandaloneSpellModels.Point(1.0, 0.0, 0.0)).normal();
            ManorStandaloneSpellModels.Point point5 = point3.cross(point4).normal();
            int n3 = this.reduced ? 4 : 8;
            ManorStandaloneSpellModels.Point[] pointArray = new ManorStandaloneSpellModels.Point[n3];
            for (n2 = 0; n2 < n3; ++n2) {
                double d2 = (double)n2 * Math.PI * 2.0 / (double)n3;
                pointArray[n2] = point4.scale(Math.cos(d2) * d).add(point5.scale(Math.sin(d2) * d));
            }
            for (n2 = 0; n2 < n3; ++n2) {
                ManorStandaloneSpellModels.Point point6 = pointArray[n2];
                ManorStandaloneSpellModels.Point point7 = pointArray[(n2 + 1) % n3];
                this.face(point.add(point6), point.add(point7), point2.add(point7), point2.add(point6), n);
            }
            if (this.reduced) {
                this.face(point.add(pointArray[3]), point.add(pointArray[2]), point.add(pointArray[1]), point.add(pointArray[0]), n);
                this.face(point2.add(pointArray[0]), point2.add(pointArray[1]), point2.add(pointArray[2]), point2.add(pointArray[3]), n);
            }
        }

        private void box(ManorStandaloneSpellModels.Point point, double d, double d2, double d3, int n) {
            ManorStandaloneSpellModels.Point[] pointArray = new ManorStandaloneSpellModels.Point[8];
            for (int i = 0; i < 8; ++i) {
                pointArray[i] = point.add(new ManorStandaloneSpellModels.Point((i & 1) == 0 ? -d / 2.0 : d / 2.0, (i & 2) == 0 ? -d2 / 2.0 : d2 / 2.0, (i & 4) == 0 ? -d3 / 2.0 : d3 / 2.0));
            }
            for (int[] nArray : new int[][]{{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}}) {
                this.face(pointArray[nArray[0]], pointArray[nArray[1]], pointArray[nArray[2]], pointArray[nArray[3]], n);
            }
        }

        private void leaf(ManorStandaloneSpellModels.Point point, ManorStandaloneSpellModels.Point point2, double d, int n) {
            ManorStandaloneSpellModels.Point[] pointArray;
            ManorStandaloneSpellModels.Point[] pointArray2;
            ManorStandaloneSpellModels.Point point3;
            ManorStandaloneSpellModels.Point point4 = point3.cross(Math.abs((point3 = point2.sub(point).normal()).y()) < 0.9 ? new ManorStandaloneSpellModels.Point(0.0, 1.0, 0.0) : new ManorStandaloneSpellModels.Point(0.0, 0.0, 1.0)).normal().scale(d);
            ManorStandaloneSpellModels.Point point5 = point.add(point2).scale(0.5);
            ManorStandaloneSpellModels.Point point6 = point3.cross(point4).normal().scale(d * 0.25);
            ManorStandaloneSpellModels.Point point7 = point.add(point2.sub(point).scale(0.72));
            ManorStandaloneSpellModels.Point point8 = point.add(point2.sub(point).scale(0.28));
            if (this.reduced) {
                pointArray2 = new ManorStandaloneSpellModels.Point[4];
                pointArray2[0] = point;
                pointArray2[1] = point5.add(point4);
                pointArray2[2] = point2;
                pointArray = pointArray2;
                pointArray2[3] = point5.sub(point4);
            } else {
                pointArray2 = new ManorStandaloneSpellModels.Point[6];
                pointArray2[0] = point;
                pointArray2[1] = point8.add(point4.scale(0.72));
                pointArray2[2] = point7.add(point4.scale(0.85));
                pointArray2[3] = point2;
                pointArray2[4] = point7.sub(point4.scale(0.85));
                pointArray = pointArray2;
                pointArray2[5] = point8.sub(point4.scale(0.72));
            }
            pointArray2 = pointArray;
            for (int i = 0; i < pointArray2.length; ++i) {
                ManorStandaloneSpellModels.Point point9 = pointArray2[i];
                ManorStandaloneSpellModels.Point point10 = pointArray2[(i + 1) % pointArray2.length];
                this.face(point5.add(point6), point9, point10, point10, n);
                this.face(point5.sub(point6), point10, point9, point9, n);
            }
        }

        private void barrel(ManorStandaloneSpellModels.Point point, double d, double d2) {
            ManorStandaloneSpellModels.Point point2 = point.add(new ManorStandaloneSpellModels.Point(0.0, -d2 / 2.0, 0.0));
            this.prism(point2, d * 0.8, d, d2 * 0.5, 10, 6635056);
            this.prism(point, d, d * 0.8, d2 * 0.5, 10, 8737595);
            this.ring(point2.add(new ManorStandaloneSpellModels.Point(0.0, d2 * 0.18, 0.0)), d * 0.92, 0.045, 3489872);
            this.ring(point2.add(new ManorStandaloneSpellModels.Point(0.0, d2 * 0.82, 0.0)), d * 0.92, 0.045, 12950360);
            if (!this.reduced) {
                for (int i = 0; i < 4; ++i) {
                    double d3 = (double)i * Math.PI / 2.0;
                    ManorStandaloneSpellModels.Point point3 = point.add(RemainingSpellModels.polar(d * 1.015, d3, 0.0));
                    this.rod(point2.add(RemainingSpellModels.polar(d * 0.85, d3, d2 * 0.06)), point3, d * 0.018, 11699800);
                    this.rod(point3, point2.add(RemainingSpellModels.polar(d * 0.85, d3, d2 * 0.94)), d * 0.018, 11699800);
                }
            }
        }

        private void flower(ManorStandaloneSpellModels.Point point, double d, int n, int n2, int n3, double d2) {
            for (int i = 0; i < n; ++i) {
                double d3 = d2 + (double)i * Math.PI * 2.0 / (double)n;
                double d4 = d * (0.58 + this.opening * 0.42);
                double d5 = 0.3 + (1.0 - this.opening) * 0.55 + Math.sin(this.age * 0.14 + (double)i * 0.5) * d * 0.055;
                ManorStandaloneSpellModels.Point point2 = point.add(RemainingSpellModels.polar(d * 0.12, d3, 0.0));
                ManorStandaloneSpellModels.Point point3 = point.add(RemainingSpellModels.polar(d4, d3, d5));
                this.leaf(point2, point3, d * 0.31, i % 2 == 0 ? n2 : n3);
                if (this.reduced) continue;
                this.rod(point2.add(new ManorStandaloneSpellModels.Point(0.0, 0.055, 0.0)), point2.add(point3.sub(point2).scale(0.8)).add(new ManorStandaloneSpellModels.Point(0.0, 0.055, 0.0)), d * 0.008, n3);
            }
            this.gem(point.add(new ManorStandaloneSpellModels.Point(0.0, 0.25, 0.0)), d * 0.2, d * 0.3, 12950360);
        }

        private void mushroom(ManorStandaloneSpellModels.Point point, double d, int n) {
            int n2;
            this.prism(point, d * 0.16, d * 0.11, d * 0.85, 6, 10393717);
            double d2 = 1.0 + Math.sin(this.age * 0.2 + point.x()) * 0.035;
            this.prism(point.add(new ManorStandaloneSpellModels.Point(0.0, d * 0.85, 0.0)), d * 0.68 * d2, d * 0.22, d * 0.45, 8, n);
            if (!this.reduced) {
                for (n2 = 0; n2 < 4; ++n2) {
                    double d3 = (double)n2 * Math.PI / 2.0;
                    this.rod(point.add(RemainingSpellModels.polar(d * 0.16, d3, d * 0.86)), point.add(RemainingSpellModels.polar(d * 0.57 * d2, d3, d * 0.86)), d * 0.013, 14076836);
                }
            }
            for (n2 = 0; n2 < 4; ++n2) {
                this.gem(point.add(RemainingSpellModels.polar(d * 0.35, (double)n2 * Math.PI / 2.0, d * 1.15)), d * 0.07, d * 0.06, 14076836);
            }
        }

        private void bottle(ManorStandaloneSpellModels.Point point, double d, int n) {
            this.prism(point, d, d, d * 1.6, 6, n);
            this.prism(point.add(new ManorStandaloneSpellModels.Point(0.0, d * 1.6, 0.0)), d, d * 0.4, d * 0.4, 6, n);
            this.prism(point.add(new ManorStandaloneSpellModels.Point(0.0, d * 2.0, 0.0)), d * 0.4, d * 0.4, d * 0.6, 6, 12950360);
            if (!this.reduced) {
                this.prism(point.add(new ManorStandaloneSpellModels.Point(0.0, d * 1.4, 0.0)), d * 1.02, d * 1.02, d * 0.12, 6, 12114399);
                this.gem(point.add(new ManorStandaloneSpellModels.Point(0.0, d * 0.85, d * 0.94)), d * 0.32, d * 0.35, 12950360);
            }
        }

        private void snowflake(ManorStandaloneSpellModels.Point point, double d, int n) {
            for (int i = 0; i < 6; ++i) {
                double d2 = (double)i * Math.PI / 3.0;
                ManorStandaloneSpellModels.Point point2 = new ManorStandaloneSpellModels.Point(Math.cos(d2), Math.sin(d2), 0.0);
                this.rod(point, point.add(point2.scale(d)), 0.028, n);
                for (int n2 : new int[]{-1, 1}) {
                    this.rod(point.add(point2.scale(d * 0.48)), point.add(new ManorStandaloneSpellModels.Point(Math.cos(d2 + (double)n2 * 0.35) * d * 0.82, Math.sin(d2 + (double)n2 * 0.35) * d * 0.82, 0.0)), 0.02, n);
                }
            }
        }

        private void helix(double d, double d2, double d3, int n) {
            for (int i = 0; i < 24; ++i) {
                this.rod(RemainingSpellModels.polar(d * (1.0 - (double)i / 48.0), d3 + (double)i * Math.PI / 6.0, 0.2 + d2 * (double)i / 24.0), RemainingSpellModels.polar(d * (1.0 - (double)(i + 1) / 48.0), d3 + (double)(i + 1) * Math.PI / 6.0, 0.2 + d2 * (double)(i + 1) / 24.0), 0.07, n);
            }
        }
    }
}

