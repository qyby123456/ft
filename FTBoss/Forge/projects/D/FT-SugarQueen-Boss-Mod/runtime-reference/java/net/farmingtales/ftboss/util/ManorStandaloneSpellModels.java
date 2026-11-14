/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.EnumSet;
import java.util.Set;
import net.farmingtales.ftboss.util.SpellVisualRules;

public final class ManorStandaloneSpellModels {
    public static final int MAX_MODELS_PER_FRAME = 12;
    public static final int MAX_QUADS = 2600;
    public static final int CHARGED_ORB_REFERENCE_QUADS = 258;
    private static final Set<SpellVisualRules.Spell> MODELED = EnumSet.of(SpellVisualRules.Spell.CANDLE_PRISON, new SpellVisualRules.Spell[]{SpellVisualRules.Spell.RENDING_CLEAVE, SpellVisualRules.Spell.CRIMSON_OATH, SpellVisualRules.Spell.SEARING_PAN, SpellVisualRules.Spell.FOUR_TIDES, SpellVisualRules.Spell.ORBITAL_GEARS, SpellVisualRules.Spell.SHADOW_BRAND, SpellVisualRules.Spell.VOID_ECHO, SpellVisualRules.Spell.HALO_SANCTUARY, SpellVisualRules.Spell.BLOOD_VERDICT, SpellVisualRules.Spell.CITRUS_NOVA, SpellVisualRules.Spell.WINTER_CLOCK, SpellVisualRules.Spell.FEATHER_REFUGE});

    private ManorStandaloneSpellModels() {
    }

    public static Set<SpellVisualRules.Spell> modeled() {
        return Set.copyOf(MODELED);
    }

    public static boolean supports(SpellVisualRules.Spell spell) {
        return MODELED.contains((Object)spell);
    }

    public static boolean renders(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase) {
        return ManorStandaloneSpellModels.supports(spell) && phase != SpellVisualRules.Phase.BEAM;
    }

    public static int draw(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase, double age, Sink output) {
        if (!ManorStandaloneSpellModels.renders(spell, phase) || !Double.isFinite(age) || age < 0.0 || output == null) {
            return 0;
        }
        Mesh mesh = new Mesh(output);
        double enter = phase == SpellVisualRules.Phase.CHARGE ? 0.18 + ManorStandaloneSpellModels.smooth((age + 1.0) / 9.0) * 0.72 : 1.0;
        double pulse = phase == SpellVisualRules.Phase.PULSE ? 1.0 + Math.sin(age * 0.28) * 0.055 : 1.0;
        Sink sink = ManorStandaloneSpellModels.transform(0.0, 0.0, 0.0, age * 0.006, 0.0, 0.0, enter * pulse, mesh::face);
        switch (spell) {
            case CANDLE_PRISON: {
                ManorStandaloneSpellModels.candlePrison(age, sink);
                break;
            }
            case RENDING_CLEAVE: {
                ManorStandaloneSpellModels.rendingCleave(age, sink);
                break;
            }
            case CRIMSON_OATH: {
                ManorStandaloneSpellModels.crimsonOath(age, sink);
                break;
            }
            case SEARING_PAN: {
                ManorStandaloneSpellModels.searingPan(age, sink);
                break;
            }
            case FOUR_TIDES: {
                ManorStandaloneSpellModels.fourTides(age, sink);
                break;
            }
            case ORBITAL_GEARS: {
                ManorStandaloneSpellModels.orbitalGears(age, sink);
                break;
            }
            case SHADOW_BRAND: {
                ManorStandaloneSpellModels.shadowBrand(age, sink);
                break;
            }
            case VOID_ECHO: {
                ManorStandaloneSpellModels.voidEcho(age, sink);
                break;
            }
            case HALO_SANCTUARY: {
                ManorStandaloneSpellModels.haloSanctuary(age, sink);
                break;
            }
            case BLOOD_VERDICT: {
                ManorStandaloneSpellModels.bloodVerdict(age, sink);
                break;
            }
            case CITRUS_NOVA: {
                ManorStandaloneSpellModels.citrusNova(age, sink);
                break;
            }
            case WINTER_CLOCK: {
                ManorStandaloneSpellModels.winterClock(age, sink);
                break;
            }
            case FEATHER_REFUGE: {
                ManorStandaloneSpellModels.featherRefuge(age, sink);
                break;
            }
        }
        return mesh.count;
    }

    private static void candlePrison(double age, Sink s) {
        int wax = 14932181;
        int metal = 3419461;
        int violet = 10384068;
        int flame = 16242059;
        for (int i = 0; i < 8; ++i) {
            double a = (double)i * Math.PI / 4.0 + age * 0.009;
            Point foot = ManorStandaloneSpellModels.polar(2.55, a, 0.12);
            ManorStandaloneSpellModels.candle(foot, 1.25 + (double)(i & 1) * 0.35, wax, metal, flame, s);
            ManorStandaloneSpellModels.strut(foot.add(new Point(0.0, 0.65, 0.0)), ManorStandaloneSpellModels.polar(2.55, a + 0.7853981633974483, 0.72 + (double)(i & 1) * 0.16), 0.045, metal, s);
            ManorStandaloneSpellModels.strut(foot.add(new Point(0.0, 1.02, 0.0)), new Point(0.0, 1.85, 0.0), 0.032, violet, s);
        }
        ManorStandaloneSpellModels.taper(new Point(0.0, 0.25, 0.0), 0.58, 0.42, 0.76, 8, metal, s);
        ManorStandaloneSpellModels.box(new Point(0.0, 1.12, 0.0), 0.42, 0.48, 0.22, age * 0.018, 0.0, 0.0, violet, s);
        ManorStandaloneSpellModels.arcTorus(new Point(0.0, 1.58, 0.0), 0.54, 0.08, 14, 5, -2.8902652413026098, 5.7805304826052195, 1.5707963267948966, 0.0, 0.0, metal, s);
        ManorStandaloneSpellModels.crystal(new Point(0.0, 1.08, -0.25), 0.12, 0.34, 6, flame, s);
    }

    private static void rendingCleave(double age, Sink s) {
        int i;
        for (i = 0; i < 3; ++i) {
            double side = (double)(i - 1) * 1.25;
            double roll = (double)(i - 1) * -0.16 + Math.sin(age * 0.08 + (double)i) * 0.025;
            ManorStandaloneSpellModels.cleaver(2433838, 7826041, 12998000, 15833770, ManorStandaloneSpellModels.transform(side, 0.1 + (double)i * 0.24, 1.8 + (double)i * 0.8, 0.0, 0.0, roll, 1.0, s));
        }
        for (i = -2; i <= 2; ++i) {
            ManorStandaloneSpellModels.strut(new Point((double)i * 0.8, 0.08, 0.7), new Point((double)i * 1.2, 0.12, 4.8), 0.035, 12998000, s);
        }
    }

    private static void crimsonOath(double age, Sink s) {
        ManorStandaloneSpellModels.crystal(new Point(0.0, 1.55, 0.0), 0.42, 1.15, 8, 15822201, s);
        ManorStandaloneSpellModels.torus(new Point(0.0, 1.55, 0.0), 1.0, 0.075, 18, 5, age * 0.035, 1.5707963267948966, 0.0, 13146722, s);
        ManorStandaloneSpellModels.torus(new Point(0.0, 1.55, 0.0), 1.32, 0.045, 20, 4, -age * 0.025, -0.65, 0.25, 11026261, s);
        for (int i = 0; i < 3; ++i) {
            double a = age * 0.022 + (double)i * Math.PI * 2.0 / 3.0;
            Point p = ManorStandaloneSpellModels.polar(1.8, a, 1.15 + Math.sin(age * 0.12 + (double)i) * 0.14);
            ManorStandaloneSpellModels.shieldTablet(3810352, 11026261, 13146722, ManorStandaloneSpellModels.transform(p.x, p.y, p.z, -a, 0.0, 0.0, 0.8, s));
        }
        ManorStandaloneSpellModels.taper(new Point(0.0, 0.08, 0.0), 1.55, 1.25, 0.18, 12, 3810352, s);
    }

    private static void searingPan(double age, Sink s) {
        Sink pan = ManorStandaloneSpellModels.transform(0.0, 0.82, 2.15, 0.15, -0.18, Math.sin(age * 0.08) * 0.06, 1.0, s);
        ManorStandaloneSpellModels.taper(new Point(0.0, 0.0, 0.0), 1.08, 0.82, 0.28, 16, 3026998, pan);
        ManorStandaloneSpellModels.torus(new Point(0.0, 0.25, 0.0), 1.05, 0.09, 20, 5, 0.0, 0.0, 0.0, 0x76777A, pan);
        ManorStandaloneSpellModels.strut(new Point(0.0, 0.14, -0.8), new Point(0.0, 0.23, -2.75), 0.16, 3026998, pan);
        ManorStandaloneSpellModels.box(new Point(0.0, 0.23, -2.7), 0.24, 0.18, 0.38, 0.0, 0.0, 0.0, 12610366, pan);
        for (int i = 0; i < 7; ++i) {
            double a = (double)i * Math.PI * 2.0 / 7.0 + age * 0.03;
            ManorStandaloneSpellModels.crystal(ManorStandaloneSpellModels.polar(0.55, a, 0.28), 0.16, 0.7 + (double)(i % 3) * 0.18, 5, i % 2 == 0 ? 16762221 : 15890499, pan);
        }
        ManorStandaloneSpellModels.arcTorus(new Point(0.0, 0.7, 1.8), 2.4, 0.13, 18, 5, 0.37699111843077515, 2.3876104167282426, 1.5707963267948966, 0.0, 0.0, 16762221, s);
    }

    private static void fourTides(double age, Sink s) {
        for (int i = 0; i < 4; ++i) {
            double x = ((double)i - 1.5) * 1.15;
            double z = 1.35 + (double)i * 0.72 + Math.min(1.0, age / 10.0) * 1.2;
            Sink t = ManorStandaloneSpellModels.transform(x, 0.25 + Math.sin(age * 0.09 + (double)i) * 0.14, z, 0.0, -0.08, 0.0, 0.82, s);
            ManorStandaloneSpellModels.strut(new Point(0.0, 0.65, -1.1), new Point(0.0, 0.65, 1.3), 0.07, 2382963, t);
            for (int f = -1; f <= 1; ++f) {
                Point root = new Point((double)f * 0.34, 0.65, 0.7);
                Point tip = new Point((double)f * 0.48, 0.65 + (f == 0 ? 0.16 : 0.0), 1.65);
                ManorStandaloneSpellModels.strut(root, tip, 0.075, f == 0 ? 0xD6FFFF : 6609370, t);
                ManorStandaloneSpellModels.crystal(tip, 0.1, 0.4, 5, 0xD6FFFF, t);
            }
            ManorStandaloneSpellModels.strut(new Point(-0.48, 0.65, 0.75), new Point(0.48, 0.65, 0.75), 0.065, 7904940, t);
            for (int f : new int[]{-1, 1}) {
                ManorStandaloneSpellModels.extrude(new Point[]{new Point(0.0, 0.0, 0.0), new Point((double)f * 0.75, 0.1, -0.55), new Point((double)f * 0.42, 0.12, 0.45), new Point(0.0, 0.0, 0.8)}, 0.035, 6609370, 2382963, t);
            }
        }
    }

    private static void orbitalGears(double age, Sink s) {
        for (int i = 0; i < 3; ++i) {
            double a = age * 0.045 + (double)i * Math.PI * 2.0 / 3.0;
            Point p = ManorStandaloneSpellModels.polar(2.15, a, 1.05 + (double)i * 0.28);
            Sink w = ManorStandaloneSpellModels.transform(p.x, p.y, p.z, -a, 1.5707963267948966, age * (i % 2 == 0 ? 0.08 : -0.07), 0.9, s);
            ManorStandaloneSpellModels.gear(new Point(0.0, 0.0, 0.0), 0.84, 0.58, 0.15, 10, 5401976, 10542311, w);
            ManorStandaloneSpellModels.torus(new Point(0.0, 0.0, 0.0), 0.32, 0.075, 14, 5, 0.0, 0.0, 0.0, 13018475, w);
            ManorStandaloneSpellModels.crystal(new Point(0.0, 0.0, 0.0), 0.18, 0.55, 6, 10542311, w);
            ManorStandaloneSpellModels.strut(p, new Point(0.0, 1.55, 0.0), 0.025, 2570306, s);
        }
        ManorStandaloneSpellModels.gear(new Point(0.0, 0.16, 0.0), 1.15, 0.92, 0.09, 14, 2570306, 5401976, ManorStandaloneSpellModels.transform(0.0, 0.0, 0.0, -age * 0.015, 0.0, 0.0, 1.0, s));
    }

    private static void shadowBrand(double age, Sink s) {
        ManorStandaloneSpellModels.taper(new Point(0.0, 0.15, 0.0), 0.68, 0.38, 2.1, 6, 1709350, s);
        ManorStandaloneSpellModels.crystal(new Point(0.0, 2.0, 0.0), 0.38, 1.0, 6, 10254798, s);
        ManorStandaloneSpellModels.torus(new Point(0.0, 1.45, 0.0), 1.1, 0.07, 18, 4, age * 0.03, 1.5707963267948966, 0.0, 14137330, s);
        for (int i = 0; i < 6; ++i) {
            double a = (double)i * Math.PI / 3.0 - age * 0.012;
            Point foot = ManorStandaloneSpellModels.polar(2.7, a, 0.08);
            Point head = ManorStandaloneSpellModels.polar(2.2, a, 1.75);
            ManorStandaloneSpellModels.strut(foot, head, 0.12, 1709350, s);
            ManorStandaloneSpellModels.crystal(head, 0.22, 0.72, 5, 4928873, s);
            ManorStandaloneSpellModels.strut(head, new Point(0.0, 1.45, 0.0), 0.045, 10254798, s);
        }
        ManorStandaloneSpellModels.gear(new Point(0.0, 0.1, 0.0), 2.05, 1.72, 0.055, 12, 4928873, 1709350, ManorStandaloneSpellModels.transform(0.0, 0.0, 0.0, age * 0.01, 0.0, 0.0, 1.0, s));
    }

    private static void voidEcho(double age, Sink s) {
        ManorStandaloneSpellModels.crystal(new Point(0.0, 1.35, 0.0), 0.42, 1.2, 8, 1512225, s);
        for (int r = 0; r < 3; ++r) {
            ManorStandaloneSpellModels.torus(new Point(0.0, 1.35, 0.0), 0.8 + (double)r * 0.48, 0.07 - (double)r * 0.01, 20, 5, age * (r % 2 == 0 ? 0.035 : -0.028), 1.5707963267948966 + (double)r * 0.48, (double)r * 0.31, r == 2 ? 13341924 : 6900363, s);
        }
        for (int side : new int[]{-1, 1}) {
            Sink bell = ManorStandaloneSpellModels.transform((double)side * 1.65, 1.4, 0.0, (double)side * -Math.PI / 2.0, 0.0, 0.0, 0.9, s);
            ManorStandaloneSpellModels.taper(new Point(0.0, -0.5, 0.0), 0.55, 0.23, 0.85, 10, 6900363, bell);
            ManorStandaloneSpellModels.torus(new Point(0.0, 0.32, 0.0), 0.55, 0.07, 16, 4, 0.0, 0.0, 0.0, 15258868, bell);
            ManorStandaloneSpellModels.crystal(new Point(0.0, -0.68, 0.0), 0.12, 0.34, 5, 13341924, bell);
            for (int w = 0; w < 3; ++w) {
                ManorStandaloneSpellModels.arcTorus(new Point(0.0, 0.15, (double)side * 0.25), 0.75 + (double)w * 0.32, 0.035, 12, 4, -1.1309733552923256, 2.261946710584651, 1.5707963267948966, 0.0, 0.0, 13341924, bell);
            }
        }
    }

    private static void haloSanctuary(double age, Sink s) {
        ManorStandaloneSpellModels.taper(new Point(0.0, 0.1, 0.0), 0.95, 0.72, 0.4, 12, 0x665E66, s);
        ManorStandaloneSpellModels.taper(new Point(0.0, 0.48, 0.0), 0.68, 0.42, 1.35, 8, 15261640, s);
        ManorStandaloneSpellModels.crystal(new Point(0.0, 1.82, 0.0), 0.3, 0.9, 8, 15851424, s);
        ManorStandaloneSpellModels.torus(new Point(0.0, 2.25, 0.0), 0.82, 0.08, 18, 5, age * 0.018, 1.5707963267948966, 0.0, 13150300, s);
        for (int i = 0; i < 8; ++i) {
            double a = (double)i * Math.PI / 4.0 + age * 0.005;
            Point foot = ManorStandaloneSpellModels.polar(3.0, a, 0.12);
            Point shoulder = ManorStandaloneSpellModels.polar(2.45, a, 2.15);
            Point crown = ManorStandaloneSpellModels.polar(1.28, a, 3.15);
            ManorStandaloneSpellModels.strut(foot, shoulder, 0.09, 0x665E66, s);
            ManorStandaloneSpellModels.strut(shoulder, crown, 0.075, 13150300, s);
            ManorStandaloneSpellModels.crystal(shoulder, 0.15, 0.5, 6, 15851424, s);
        }
        ManorStandaloneSpellModels.torus(new Point(0.0, 3.18, 0.0), 1.28, 0.07, 20, 5, 0.0, 0.0, 0.0, 15261640, s);
    }

    private static void bloodVerdict(double age, Sink s) {
        Sink sword = ManorStandaloneSpellModels.transform(0.0, 0.18 + Math.max(0.0, 1.0 - Math.min(1.0, age / 10.0)) * 2.2, 0.0, 0.0, 0.0, 0.0, 1.1, s);
        Point[] blade = new Point[]{new Point(-0.34, 0.0, 0.0), new Point(-0.48, 2.55, 0.0), new Point(0.0, 3.45, 0.0), new Point(0.48, 2.55, 0.0), new Point(0.34, 0.0, 0.0), new Point(0.0, -0.72, 0.0)};
        ManorStandaloneSpellModels.extrude(blade, 0.12, 9073274, 2564394, sword);
        ManorStandaloneSpellModels.strut(new Point(-1.0, 2.72, 0.0), new Point(1.0, 2.72, 0.0), 0.12, 13211228, sword);
        ManorStandaloneSpellModels.strut(new Point(0.0, 2.7, 0.0), new Point(0.0, 4.05, 0.0), 0.14, 2564394, sword);
        ManorStandaloneSpellModels.crystal(new Point(0.0, 4.18, 0.0), 0.24, 0.58, 6, 15754371, sword);
        for (int side : new int[]{-1, 1}) {
            Point chain = new Point((double)side * 1.65, 2.6, 0.0);
            ManorStandaloneSpellModels.strut(new Point((double)side * 0.75, 2.72, 0.0), chain, 0.035, 13211228, sword);
            ManorStandaloneSpellModels.taper(chain.add(new Point(0.0, -0.5, 0.0)), 0.58, 0.42, 0.16, 8, 15754371, sword);
            ManorStandaloneSpellModels.box(chain.add(new Point(0.0, -0.65, 0.0)), 0.52, 0.07, 0.38, 0.0, 0.0, 0.0, 2564394, sword);
        }
        for (int inlay = 0; inlay < 7; ++inlay) {
            double y = 0.32 + (double)inlay * 0.39;
            double width = 0.26 * (1.0 - y / 4.2);
            ManorStandaloneSpellModels.crystal(new Point(-width, y, -0.16), 0.045, 0.14, 8, inlay % 2 == 0 ? 16167097 : 13211228, sword);
            ManorStandaloneSpellModels.crystal(new Point(width, y, -0.16), 0.045, 0.14, 8, inlay % 2 == 0 ? 16167097 : 13211228, sword);
        }
        ManorStandaloneSpellModels.torus(new Point(0.0, 2.72, 0.0), 0.42, 0.045, 20, 6, 0.0, 1.5707963267948966, 0.0, 13211228, sword);
        for (int r = 0; r < 8; ++r) {
            ManorStandaloneSpellModels.strut(new Point(0.0, 0.08, 0.0), ManorStandaloneSpellModels.polar(2.7, (double)r * Math.PI / 4.0, 0.08), 0.045, 15754371, s);
        }
    }

    private static void citrusNova(double age, Sink s) {
        int i;
        Sink fruit = ManorStandaloneSpellModels.transform(0.0, 1.55, 0.0, age * 0.026, 0.0, 0.0, 1.0, s);
        ManorStandaloneSpellModels.orb(new Point(0.0, 0.0, 0.0), 1.08, 8, 12, 15047991, fruit);
        for (i = 0; i < 8; ++i) {
            ManorStandaloneSpellModels.torus(new Point(0.0, 0.0, 0.0), 1.09, 0.035, 18, 4, (double)i * Math.PI / 4.0, 1.5707963267948966, 0.0, i % 2 == 0 ? 16770464 : 16765275, fruit);
        }
        ManorStandaloneSpellModels.torus(new Point(0.0, 0.0, 0.0), 1.1, 0.055, 20, 4, 0.0, 0.0, 0.0, 8081706, fruit);
        for (i = 0; i < 2; ++i) {
            Sink leaf = ManorStandaloneSpellModels.transform(0.0, 1.0, 0.0, i == 0 ? -0.55 : 0.7, -0.5, 0.0, 1.0, fruit);
            ManorStandaloneSpellModels.extrude(new Point[]{new Point(0.0, 0.0, 0.0), new Point(-0.5, 0.42, 0.0), new Point(0.0, 1.12, 0.0), new Point(0.5, 0.42, 0.0)}, 0.055, 7186522, 4092221, leaf);
        }
        for (i = 0; i < 10; ++i) {
            double a = (double)i * Math.PI / 5.0 + age * 0.012;
            Point base = ManorStandaloneSpellModels.polar(1.5, a, 1.55);
            Point tip = ManorStandaloneSpellModels.polar(2.7 + Math.sin(age * 0.12 + (double)i) * 0.18, a, 1.55);
            ManorStandaloneSpellModels.strut(base, tip, 0.07, i % 2 == 0 ? 16765275 : 16770464, s);
            ManorStandaloneSpellModels.crystal(tip, 0.12, 0.42, 5, 16765275, s);
        }
    }

    private static void winterClock(double age, Sink s) {
        Sink face = ManorStandaloneSpellModels.transform(0.0, 2.35, 0.0, 0.0, 1.5707963267948966, 0.0, 1.0, s);
        ManorStandaloneSpellModels.torus(new Point(0.0, 0.0, 0.0), 1.75, 0.13, 24, 6, 0.0, 0.0, 0.0, 5406106, face);
        ManorStandaloneSpellModels.torus(new Point(0.0, 0.0, 0.0), 1.43, 0.045, 20, 4, 0.0, 0.0, 0.0, 0xE8F8FF, face);
        ManorStandaloneSpellModels.gear(new Point(0.0, 0.0, 0.12), 1.25, 0.98, 0.07, 12, 2571349, 10411775, ManorStandaloneSpellModels.transform(0.0, 0.0, 0.0, -age * 0.018, 0.0, 0.0, 1.0, face));
        for (int i = 0; i < 12; ++i) {
            double a = (double)i * Math.PI / 6.0;
            ManorStandaloneSpellModels.crystal(new Point(Math.cos(a) * 1.5, Math.sin(a) * 1.5, -0.1), 0.09, i % 3 == 0 ? 0.48 : 0.3, 5, i % 3 == 0 ? 0xE8F8FF : 10411775, face);
        }
        double minute = -age * 0.055;
        double hour = -age * 0.009;
        ManorStandaloneSpellModels.strut(new Point(0.0, 0.0, -0.18), new Point(Math.cos(minute) * 1.2, Math.sin(minute) * 1.2, -0.18), 0.055, 0xE8F8FF, face);
        ManorStandaloneSpellModels.strut(new Point(0.0, 0.0, -0.22), new Point(Math.cos(hour) * 0.82, Math.sin(hour) * 0.82, -0.22), 0.085, 5406106, face);
        ManorStandaloneSpellModels.crystal(new Point(0.0, 0.0, -0.25), 0.16, 0.4, 6, 10411775, face);
        for (int i = 0; i < 6; ++i) {
            ManorStandaloneSpellModels.crystal(ManorStandaloneSpellModels.polar(2.55, (double)i * Math.PI / 3.0 + age * 0.01, 0.12), 0.2, 1.1 + (double)(i % 2) * 0.45, 6, 10411775, s);
        }
    }

    private static void featherRefuge(double age, Sink s) {
        for (int i = 0; i < 10; ++i) {
            double a = (double)i * Math.PI / 5.0 + age * 0.004;
            Point foot = ManorStandaloneSpellModels.polar(3.1, a, 0.15);
            ManorStandaloneSpellModels.feather(14274233, 16774102, 8352095, ManorStandaloneSpellModels.transform(foot.x, foot.y, foot.z, -a, -0.48, 0.0, 1.15, s));
            ManorStandaloneSpellModels.strut(foot.add(new Point(0.0, 0.15, 0.0)), ManorStandaloneSpellModels.polar(0.55, a, 3.45), 0.035, 13149033, s);
        }
        ManorStandaloneSpellModels.torus(new Point(0.0, 3.55, 0.0), 0.72, 0.09, 20, 5, age * 0.02, 0.0, 0.0, 13149033, s);
        ManorStandaloneSpellModels.crystal(new Point(0.0, 3.55, 0.0), 0.25, 0.85, 7, 16774102, s);
        ManorStandaloneSpellModels.gear(new Point(0.0, 0.08, 0.0), 2.55, 2.28, 0.045, 20, 8352095, 14274233, ManorStandaloneSpellModels.transform(0.0, 0.0, 0.0, -age * 0.006, 0.0, 0.0, 1.0, s));
    }

    private static void candle(Point foot, double h, int wax, int metal, int flame, Sink s) {
        ManorStandaloneSpellModels.taper(foot, 0.22, 0.22, h, 8, wax, s);
        ManorStandaloneSpellModels.taper(foot.add(new Point(0.0, -0.05, 0.0)), 0.32, 0.27, 0.16, 8, metal, s);
        ManorStandaloneSpellModels.torus(foot.add(new Point(0.0, h * 0.92, 0.0)), 0.22, 0.035, 12, 4, 0.0, 0.0, 0.0, metal, s);
        ManorStandaloneSpellModels.crystal(foot.add(new Point(0.0, h + 0.2, 0.0)), 0.12, 0.48, 5, flame, s);
    }

    private static void cleaver(int iron, int steel, int blood, int edge, Sink s) {
        Point[] blade = new Point[]{new Point(-1.7, 0.05, 0.0), new Point(-1.48, 1.0, 0.0), new Point(-0.85, 1.5, 0.0), new Point(1.25, 1.38, 0.0), new Point(1.72, 0.75, 0.0), new Point(1.42, 0.18, 0.0)};
        ManorStandaloneSpellModels.extrude(blade, 0.13, steel, iron, s);
        ManorStandaloneSpellModels.extrude(new Point[]{new Point(-1.55, 0.08, -0.14), new Point(1.38, 0.2, -0.14), new Point(1.62, 0.62, -0.14), new Point(-1.25, 0.45, -0.14)}, 0.025, edge, blood, s);
        ManorStandaloneSpellModels.strut(new Point(-1.48, 0.18, -0.18), new Point(1.42, 0.29, -0.18), 0.026, edge, s);
        ManorStandaloneSpellModels.strut(new Point(-1.12, 1.18, -0.18), new Point(1.22, 1.08, -0.18), 0.021, blood, s);
        for (int rivet = 0; rivet < 5; ++rivet) {
            ManorStandaloneSpellModels.crystal(new Point(-1.15 + (double)rivet * 0.52, 0.72 + (double)(rivet & 1) * 0.12, -0.2), 0.055, 0.15, 8, edge, s);
        }
        ManorStandaloneSpellModels.strut(new Point(-0.95, 1.25, 0.0), new Point(-1.38, 2.25, 0.0), 0.14, iron, s);
        ManorStandaloneSpellModels.torus(new Point(-1.25, 1.75, 0.0), 0.23, 0.035, 18, 6, 0.0, 1.5707963267948966, 0.0, edge, s);
        ManorStandaloneSpellModels.box(new Point(-1.43, 2.38, 0.0), 0.22, 0.38, 0.2, 0.0, 0.0, -0.38, blood, s);
    }

    private static void shieldTablet(int dark, int color, int trim, Sink s) {
        Point[] body = new Point[]{new Point(-0.52, -0.72, 0.0), new Point(-0.58, 0.48, 0.0), new Point(0.0, 0.86, 0.0), new Point(0.58, 0.48, 0.0), new Point(0.52, -0.72, 0.0), new Point(0.0, -1.05, 0.0)};
        ManorStandaloneSpellModels.extrude(body, 0.12, color, dark, s);
        ManorStandaloneSpellModels.strut(new Point(-0.37, 0.35, -0.14), new Point(0.37, 0.35, -0.14), 0.035, trim, s);
        ManorStandaloneSpellModels.strut(new Point(0.0, -0.55, -0.14), new Point(0.0, 0.62, -0.14), 0.04, trim, s);
        ManorStandaloneSpellModels.crystal(new Point(0.0, 0.12, -0.2), 0.12, 0.38, 6, trim, s);
    }

    private static void feather(int body, int edge, int quill, Sink s) {
        Point[] p = new Point[]{new Point(0.0, 0.0, 0.0), new Point(-0.38, 0.55, 0.0), new Point(-0.52, 1.5, 0.0), new Point(-0.22, 2.55, 0.0), new Point(0.0, 3.05, 0.0), new Point(0.22, 2.55, 0.0), new Point(0.52, 1.5, 0.0), new Point(0.38, 0.55, 0.0)};
        ManorStandaloneSpellModels.extrude(p, 0.055, body, quill, s);
        ManorStandaloneSpellModels.strut(new Point(0.0, -0.25, -0.08), new Point(0.0, 2.75, -0.08), 0.045, quill, s);
        for (int i = 1; i <= 5; ++i) {
            double y = 0.35 + (double)i * 0.4;
            double w = 0.5 - (double)i * 0.055;
            ManorStandaloneSpellModels.strut(new Point(0.0, y, -0.09), new Point(-w, y + 0.2, -0.09), 0.024, edge, s);
            ManorStandaloneSpellModels.strut(new Point(0.0, y, -0.09), new Point(w, y + 0.2, -0.09), 0.024, edge, s);
        }
    }

    private static void box(Point c, double hx, double hy, double hz, double yaw, double pitch, double roll, int color, Sink s) {
        int[][] f;
        Point[] p = new Point[8];
        int n = 0;
        for (int y : new int[]{-1, 1}) {
            for (int z : new int[]{-1, 1}) {
                for (int x : new int[]{-1, 1}) {
                    p[n++] = ManorStandaloneSpellModels.pose(new Point((double)x * hx, (double)y * hy, (double)z * hz), c.x, c.y, c.z, yaw, pitch, roll, 1.0);
                }
            }
        }
        for (int[] q : f = new int[][]{{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}}) {
            ManorStandaloneSpellModels.shade(p[q[0]], p[q[1]], p[q[2]], p[q[3]], color, s);
        }
    }

    private static void taper(Point bottom, double br, double tr, double h, int sides, int color, Sink s) {
        sides = Math.max(12, sides);
        Point top = bottom.add(new Point(0.0, h, 0.0));
        for (int i = 0; i < sides; ++i) {
            double a = (double)i * Math.PI * 2.0 / (double)sides;
            double b = (double)(i + 1) * Math.PI * 2.0 / (double)sides;
            Point ba = bottom.add(new Point(Math.cos(a) * br, 0.0, Math.sin(a) * br));
            Point bb = bottom.add(new Point(Math.cos(b) * br, 0.0, Math.sin(b) * br));
            Point ta = top.add(new Point(Math.cos(a) * tr, 0.0, Math.sin(a) * tr));
            Point tb = top.add(new Point(Math.cos(b) * tr, 0.0, Math.sin(b) * tr));
            ManorStandaloneSpellModels.shade(ba, bb, tb, ta, color, s);
            ManorStandaloneSpellModels.shade(bottom, bb, ba, ba, ManorStandaloneSpellModels.mix(color, 1512737, 0.28), s);
            ManorStandaloneSpellModels.shade(top, ta, tb, tb, ManorStandaloneSpellModels.mix(color, 0xFFFFFF, 0.12), s);
        }
    }

    private static void crystal(Point c, double r, double h, int sides, int color, Sink s) {
        sides = Math.max(10, sides);
        Point bottom = c.add(new Point(0.0, -h * 0.52, 0.0));
        Point top = c.add(new Point(0.0, h * 0.52, 0.0));
        for (int i = 0; i < sides; ++i) {
            double a = (double)i * Math.PI * 2.0 / (double)sides;
            double b = (double)(i + 1) * Math.PI * 2.0 / (double)sides;
            Point l = c.add(new Point(Math.cos(a) * r, 0.0, Math.sin(a) * r));
            Point q = c.add(new Point(Math.cos(b) * r, 0.0, Math.sin(b) * r));
            ManorStandaloneSpellModels.shade(bottom, q, l, l, ManorStandaloneSpellModels.mix(color, 1512737, 0.2), s);
            ManorStandaloneSpellModels.shade(l, q, top, top, color, s);
        }
    }

    private static void orb(Point c, double r, int bands, int sides, int color, Sink s) {
        bands = Math.max(12, bands);
        sides = Math.max(18, sides);
        for (int y = 0; y < bands; ++y) {
            double low = -1.5707963267948966 + (double)y * Math.PI / (double)bands;
            double high = -1.5707963267948966 + (double)(y + 1) * Math.PI / (double)bands;
            for (int i = 0; i < sides; ++i) {
                double a = (double)i * Math.PI * 2.0 / (double)sides;
                double b = (double)(i + 1) * Math.PI * 2.0 / (double)sides;
                ManorStandaloneSpellModels.shade(c.add(ManorStandaloneSpellModels.spherical(r, low, a)), c.add(ManorStandaloneSpellModels.spherical(r, low, b)), c.add(ManorStandaloneSpellModels.spherical(r, high, b)), c.add(ManorStandaloneSpellModels.spherical(r, high, a)), ManorStandaloneSpellModels.mix(color, i % 3 == 0 ? 0xFFFFFF : 2432041, i % 3 == 0 ? 0.16 : 0.08), s);
            }
        }
    }

    private static void torus(Point c, double r, double tube, int seg, int sides, double yaw, double pitch, double roll, int color, Sink s) {
        ManorStandaloneSpellModels.arcTorus(c, r, tube, seg, sides, 0.0, Math.PI * 2, yaw, pitch, roll, color, s);
    }

    private static void arcTorus(Point c, double r, double tube, int seg, int sides, double start, double arc, double yaw, double pitch, double roll, int color, Sink s) {
        seg = Math.max(24, seg);
        sides = Math.max(6, sides);
        Sink t = ManorStandaloneSpellModels.transform(c.x, c.y, c.z, yaw, pitch, roll, 1.0, s);
        for (int i = 0; i < seg; ++i) {
            double a = start + arc * (double)i / (double)seg;
            double b = start + arc * (double)(i + 1) / (double)seg;
            for (int j = 0; j < sides; ++j) {
                double u = (double)j * Math.PI * 2.0 / (double)sides;
                double v = (double)(j + 1) * Math.PI * 2.0 / (double)sides;
                ManorStandaloneSpellModels.shade(ManorStandaloneSpellModels.torusPoint(r, tube, a, u), ManorStandaloneSpellModels.torusPoint(r, tube, b, u), ManorStandaloneSpellModels.torusPoint(r, tube, b, v), ManorStandaloneSpellModels.torusPoint(r, tube, a, v), color, t);
            }
        }
    }

    private static void gear(Point c, double outer, double inner, double hh, int teeth, int body, int edge, Sink s) {
        int count = teeth * 4;
        Point top = c.add(new Point(0.0, hh, 0.0));
        Point bottom = c.add(new Point(0.0, -hh, 0.0));
        for (int i = 0; i < count; ++i) {
            double a = (double)i * Math.PI * 2.0 / (double)count;
            double b = (double)(i + 1) * Math.PI * 2.0 / (double)count;
            double ra = i % 4 < 2 ? outer : inner;
            double rb = (i + 1) % 4 < 2 ? outer : inner;
            Point ta = c.add(new Point(Math.cos(a) * ra, hh, Math.sin(a) * ra));
            Point tb = c.add(new Point(Math.cos(b) * rb, hh, Math.sin(b) * rb));
            Point ba = ta.add(new Point(0.0, -hh * 2.0, 0.0));
            Point bb = tb.add(new Point(0.0, -hh * 2.0, 0.0));
            ManorStandaloneSpellModels.shade(top, tb, ta, ta, edge, s);
            ManorStandaloneSpellModels.shade(bottom, ba, bb, bb, ManorStandaloneSpellModels.mix(body, 0x101018, 0.3), s);
            ManorStandaloneSpellModels.shade(ba, ta, tb, bb, body, s);
        }
    }

    private static void strut(Point a, Point b, double w, int color, Sink s) {
        int i;
        Point d = b.sub(a).normal();
        Point ref = Math.abs(d.y) < 0.88 ? new Point(0.0, 1.0, 0.0) : new Point(1.0, 0.0, 0.0);
        Point u = d.cross(ref).normal();
        Point v = d.cross(u).normal();
        int facets = 8;
        Point[] low = new Point[facets];
        Point[] high = new Point[facets];
        for (i = 0; i < facets; ++i) {
            double angle = (double)i * Math.PI * 2.0 / (double)facets;
            Point offset = u.scale(Math.cos(angle) * w * 1.32).add(v.scale(Math.sin(angle) * w * 1.32));
            low[i] = a.add(offset);
            high[i] = b.add(offset);
        }
        for (i = 0; i < facets; ++i) {
            int q = (i + 1) % facets;
            ManorStandaloneSpellModels.shade(low[i], low[q], high[q], high[i], color, s);
        }
    }

    private static void extrude(Point[] p, double depth, int frontColor, int sideColor, Sink s) {
        Point front = ManorStandaloneSpellModels.average(p).add(new Point(0.0, 0.0, -depth));
        Point back = ManorStandaloneSpellModels.average(p).add(new Point(0.0, 0.0, depth));
        for (int i = 0; i < p.length; ++i) {
            int q = (i + 1) % p.length;
            Point a = p[i].add(new Point(0.0, 0.0, -depth));
            Point b = p[q].add(new Point(0.0, 0.0, -depth));
            Point c = p[i].add(new Point(0.0, 0.0, depth));
            Point d = p[q].add(new Point(0.0, 0.0, depth));
            ManorStandaloneSpellModels.shade(front, a, b, b, frontColor, s);
            ManorStandaloneSpellModels.shade(back, d, c, c, ManorStandaloneSpellModels.mix(frontColor, 1512737, 0.25), s);
            ManorStandaloneSpellModels.shade(a, c, d, b, sideColor, s);
        }
    }

    private static Point average(Point[] points) {
        Point sum = new Point(0.0, 0.0, 0.0);
        for (Point p : points) {
            sum = sum.add(p);
        }
        return sum.scale(1.0 / (double)points.length);
    }

    private static Sink transform(double x, double y, double z, double yaw, double pitch, double roll, double scale, Sink sink) {
        return (a, b, c, d, color) -> sink.quad(ManorStandaloneSpellModels.pose(a, x, y, z, yaw, pitch, roll, scale), ManorStandaloneSpellModels.pose(b, x, y, z, yaw, pitch, roll, scale), ManorStandaloneSpellModels.pose(c, x, y, z, yaw, pitch, roll, scale), ManorStandaloneSpellModels.pose(d, x, y, z, yaw, pitch, roll, scale), color);
    }

    private static Point pose(Point p, double ox, double oy, double oz, double yaw, double pitch, double roll, double scale) {
        double x = p.x * scale;
        double y = p.y * scale;
        double z = p.z * scale;
        double rx = x * Math.cos(roll) - y * Math.sin(roll);
        double ry = x * Math.sin(roll) + y * Math.cos(roll);
        double py = ry * Math.cos(pitch) - z * Math.sin(pitch);
        double pz = ry * Math.sin(pitch) + z * Math.cos(pitch);
        return new Point(ox + rx * Math.cos(yaw) + pz * Math.sin(yaw), oy + py, oz - rx * Math.sin(yaw) + pz * Math.cos(yaw));
    }

    private static Point polar(double r, double a, double y) {
        return new Point(Math.cos(a) * r, y, Math.sin(a) * r);
    }

    private static Point spherical(double r, double e, double a) {
        double h = Math.cos(e) * r;
        return new Point(Math.cos(a) * h, Math.sin(e) * r, Math.sin(a) * h);
    }

    private static Point torusPoint(double r, double t, double a, double side) {
        double d = r + Math.cos(side) * t;
        return new Point(Math.cos(a) * d, Math.sin(side) * t, Math.sin(a) * d);
    }

    private static void shade(Point a, Point b, Point c, Point d, int color, Sink s) {
        Point n = b.sub(a).cross(c.sub(a));
        double len = n.length();
        double light = len < 1.0E-9 ? 0.82 : 0.62 + 0.38 * Math.abs((n.x * 0.38 + n.y * 0.82 + n.z * 0.43) / len);
        s.quad(a, b, c, d, ManorStandaloneSpellModels.multiply(color, light));
    }

    private static int multiply(int c, double n) {
        int r = Math.min(255, (int)Math.round((double)(c >> 16 & 0xFF) * n));
        int g = Math.min(255, (int)Math.round((double)(c >> 8 & 0xFF) * n));
        int b = Math.min(255, (int)Math.round((double)(c & 0xFF) * n));
        return r << 16 | g << 8 | b;
    }

    private static int mix(int a, int b, double n) {
        int c = 0;
        for (int shift = 0; shift <= 16; shift += 8) {
            c |= (int)Math.round((double)(a >> shift & 0xFF) * (1.0 - n) + (double)(b >> shift & 0xFF) * n) << shift;
        }
        return c;
    }

    private static double smooth(double n) {
        double v = Math.max(0.0, Math.min(1.0, n));
        return v * v * (3.0 - 2.0 * v);
    }

    private static final class Mesh {
        private final Sink output;
        private int count;

        private Mesh(Sink output) {
            this.output = output;
        }

        private void face(Point a, Point b, Point c, Point d, int color) {
            if (this.count >= 2600 || b.sub(a).cross(c.sub(a)).length() + c.sub(a).cross(d.sub(a)).length() < 1.0E-9) {
                return;
            }
            this.output.quad(a, b, c, d, color);
            ++this.count;
        }
    }

    @FunctionalInterface
    public static interface Sink {
        public void quad(Point var1, Point var2, Point var3, Point var4, int var5);
    }

    public record Point(double x, double y, double z) {
        Point add(Point p) {
            return new Point(this.x + p.x, this.y + p.y, this.z + p.z);
        }

        Point sub(Point p) {
            return new Point(this.x - p.x, this.y - p.y, this.z - p.z);
        }

        Point scale(double n) {
            return new Point(this.x * n, this.y * n, this.z * n);
        }

        double length() {
            return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        }

        Point normal() {
            double n = this.length();
            return n < 1.0E-9 ? new Point(0.0, 1.0, 0.0) : this.scale(1.0 / n);
        }

        Point cross(Point p) {
            return new Point(this.y * p.z - this.z * p.y, this.z * p.x - this.x * p.z, this.x * p.y - this.y * p.x);
        }
    }
}

