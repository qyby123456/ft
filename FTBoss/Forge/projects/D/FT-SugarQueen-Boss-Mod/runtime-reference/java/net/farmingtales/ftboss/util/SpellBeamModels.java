/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.EnumSet;
import java.util.Set;
import net.farmingtales.ftboss.util.IronSpellLightRules;
import net.farmingtales.ftboss.util.ManorStandaloneSpellModels;
import net.farmingtales.ftboss.util.SpellVisualRules;

public final class SpellBeamModels {
    public static final int MAX_MODELS_PER_FRAME = 24;
    public static final int MAX_QUADS = 2200;
    public static final int CHARGED_ORB_REFERENCE_QUADS = 258;
    private static final int METAL = 3425373;
    private static final int SILVER = 13294823;
    private static final int GOLD = 14332531;
    private static final Set<SpellVisualRules.Spell> ACCENTS = EnumSet.of(SpellVisualRules.Spell.REPULSOR_DISCHARGE, SpellVisualRules.Spell.EMERGENCY_REBOOT, SpellVisualRules.Spell.LUMINOUS_REBUKE);
    private static final Set<SpellVisualRules.Spell> MODELED = EnumSet.of(SpellVisualRules.Spell.CAPACITOR_OVERLOAD, new SpellVisualRules.Spell[]{SpellVisualRules.Spell.STATIC_LATTICE, SpellVisualRules.Spell.MAGNETIC_STORM, SpellVisualRules.Spell.PRESSURE_LANCE, SpellVisualRules.Spell.ION_RAIL, SpellVisualRules.Spell.STAR_CROSS, SpellVisualRules.Spell.FROSTGLASS_LANCE, SpellVisualRules.Spell.FROST_NEEDLES, SpellVisualRules.Spell.CAMELLIA_BLOOD_BLOOM, SpellVisualRules.Spell.SANGUINE_SUTURE, SpellVisualRules.Spell.FLESH_BLOOM, SpellVisualRules.Spell.CANDLE_ECLIPSE, SpellVisualRules.Spell.GILDED_VERDICT, SpellVisualRules.Spell.ARCANE_FUSILLADE, SpellVisualRules.Spell.EFFIGY_VOLLEY, SpellVisualRules.Spell.FEATHER_FAN, SpellVisualRules.Spell.CAMELLIA_SANCTUARY, SpellVisualRules.Spell.CAMELLIA_REBIRTH, SpellVisualRules.Spell.VIRULENT_HARVEST, SpellVisualRules.Spell.GOLDEN_HARVEST, SpellVisualRules.Spell.PRISM_BATTERY, SpellVisualRules.Spell.EFFIGY_PROCESSION, SpellVisualRules.Spell.NIGHTFALL_BLADES, SpellVisualRules.Spell.SERAPH_DESCENT, SpellVisualRules.Spell.TIDAL_EDICT, SpellVisualRules.Spell.CINDER_BREATH, SpellVisualRules.Spell.CANDLE_REAP});

    private SpellBeamModels() {
    }

    public static Set<SpellVisualRules.Spell> modeled() {
        return Set.copyOf(MODELED);
    }

    public static Set<SpellVisualRules.Spell> modeledAccents() {
        return Set.copyOf(ACCENTS);
    }

    public static boolean rendersAccent(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase) {
        return ACCENTS.contains((Object)spell) && IronSpellLightRules.accent(spell, phase) != IronSpellLightRules.Accent.NONE;
    }

    public static boolean renders(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase) {
        return phase == SpellVisualRules.Phase.BEAM && MODELED.contains((Object)spell);
    }

    public static boolean replaces(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase) {
        return SpellBeamModels.rendersAccent(spell, phase) || SpellBeamModels.renders(spell, phase) || phase == SpellVisualRules.Phase.BEAM && (spell == SpellVisualRules.Spell.RENDING_CLEAVE || spell == SpellVisualRules.Spell.SEARING_PAN || spell == SpellVisualRules.Spell.FOUR_TIDES || spell == SpellVisualRules.Spell.ROSE_STARFALL);
    }

    public static int drawAccent(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase, double age, ManorStandaloneSpellModels.Sink output) {
        if (!SpellBeamModels.rendersAccent(spell, phase) || output == null || !Double.isFinite(age) || age < 0.0 || age >= (double)spell.lifetime(phase)) {
            return 0;
        }
        int[] count = new int[]{0};
        ManorStandaloneSpellModels.Sink counted = (first, second, third, fourth, color) -> {
            count[0] = count[0] + 1;
            if (count[0] > 2200) {
                throw new IllegalStateException("Accent model exceeded budget: " + spell.id);
            }
            output.quad(first, second, third, fourth, color);
        };
        double opening = SpellBeamModels.ease(age / 5.0) * (1.0 - 0.7 * SpellBeamModels.ease((age - (double)spell.lifetime(phase) + 6.0) / 6.0));
        Mesh mesh = new Mesh(SpellBeamModels.transform(new ManorStandaloneSpellModels.Point(0.0, 1.05, 0.0), 1.0, age * 0.015, counted), spell.color, age, false);
        switch (spell) {
            case REPULSOR_DISCHARGE: {
                mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 0.24, 0.36, mesh.color);
                for (int axis = 0; axis < 3; ++axis) {
                    mesh.pitch((double)axis * Math.PI / 3.0 + age * (axis == 1 ? -0.085 : 0.06)).ring(0.56 + (double)axis * 0.13, 0.065, 0.0, 16, axis == 1 ? 14332531 : 13294823);
                }
                for (int arm = 0; arm < 4; ++arm) {
                    double angle = (double)arm * Math.PI / 2.0;
                    double reach = 0.8 + opening * 0.35;
                    mesh.leaf(SpellBeamModels.radial(0.53, angle, 0.0), SpellBeamModels.radial(reach, angle, 0.13), 0.17, 3425373);
                    mesh.gem(SpellBeamModels.radial(reach, angle, 0.13), 0.09, 0.14, mesh.color);
                    mesh.rod(SpellBeamModels.radial(0.6, angle, 0.06), SpellBeamModels.radial(reach - 0.08, angle, 0.17), 0.02, 14332531);
                }
                break;
            }
            case EMERGENCY_REBOOT: {
                mesh.box(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 0.62, 0.62, 0.58, 3425373);
                mesh.box(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.31), 0.12, 0.44, 0.05, mesh.color);
                mesh.box(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.31), 0.44, 0.12, 0.05, mesh.color);
                for (int side : new int[]{-1, 1}) {
                    for (int vent = 0; vent < 3; ++vent) {
                        mesh.box(new ManorStandaloneSpellModels.Point((double)side * 0.24, -0.12 + (double)vent * 0.12, 0.3), 0.065, 0.035, 0.035, 13294823);
                    }
                }
                for (int arm = 0; arm < 4; ++arm) {
                    double angle = (double)arm * Math.PI / 2.0;
                    double flex = Math.sin(age * 0.28 + (double)arm * Math.PI / 2.0) * 0.12;
                    ManorStandaloneSpellModels.Point joint = SpellBeamModels.radial(0.48 + opening * 0.2, angle + 0.3, 0.1);
                    ManorStandaloneSpellModels.Point tip = SpellBeamModels.radial(0.4 + opening * 0.18, angle + 0.55 + flex, 0.3 + opening * 0.18);
                    mesh.rod(SpellBeamModels.radial(0.32, angle, 0.0), joint, 0.05, 14332531);
                    mesh.rod(joint, tip, 0.04, 13294823);
                    mesh.gem(joint, 0.065, 0.075, 3425373);
                    mesh.gem(tip, 0.085, 0.11, mesh.color);
                }
                mesh.pitch(1.5707963267948966 + Math.sin(age * 0.14) * 0.18).ring(0.81, 0.045, -0.17, 16, 14332531);
                break;
            }
            case LUMINOUS_REBUKE: {
                Mesh crown = mesh.pitch(-1.5707963267948966);
                crown.taper(-0.46, 0.48, 0.2, 0.26, 14332531);
                crown.ring(0.5, 0.06, -0.45, 16, 13294823);
                crown.gem(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.35), 0.22, 0.3, mesh.color);
                for (int point = 0; point < 6; ++point) {
                    double angle = (double)point * Math.PI / 3.0;
                    crown.leaf(SpellBeamModels.radial(0.31, angle, 0.1), SpellBeamModels.radial(0.32 + opening * 0.2, angle, 0.65 + opening * 0.2), 0.12, 13294823);
                    crown.gem(SpellBeamModels.radial(0.39, angle, -0.16), 0.07, 0.13, mesh.color);
                }
                mesh.ring(0.88, 0.045, 0.0, 20, 14332531);
                break;
            }
            default: {
                throw new IllegalStateException("Missing accent model: " + spell.id);
            }
        }
        return count[0];
    }

    public static int draw(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase, double age, double length, boolean reduced, ManorStandaloneSpellModels.Sink output) {
        if (!SpellBeamModels.renders(spell, phase) || output == null || !Double.isFinite(age) || age < 0.0 || age >= (double)spell.lifetime(phase) || !Double.isFinite(length) || length < 0.015 || length > 64.0) {
            return 0;
        }
        int[] count = new int[]{0};
        ManorStandaloneSpellModels.Sink bounded = (first, second, third, fourth, color) -> {
            count[0] = count[0] + 1;
            if (count[0] > 2200) {
                throw new IllegalStateException("Beam model exceeded budget: " + spell.id);
            }
            output.quad(first, second, third, fourth, color);
        };
        if (spell == SpellVisualRules.Spell.ARCANE_FUSILLADE) {
            SpellBeamModels.doraFocusLaser(new Mesh(bounded, spell.color), age, length, reduced);
            return count[0];
        }
        boolean tether = switch (spell) {
            case SpellVisualRules.Spell.CAPACITOR_OVERLOAD, SpellVisualRules.Spell.STATIC_LATTICE, SpellVisualRules.Spell.MAGNETIC_STORM, SpellVisualRules.Spell.CAMELLIA_BLOOD_BLOOM, SpellVisualRules.Spell.SANGUINE_SUTURE, SpellVisualRules.Spell.FLESH_BLOOM, SpellVisualRules.Spell.CANDLE_ECLIPSE, SpellVisualRules.Spell.CAMELLIA_SANCTUARY, SpellVisualRules.Spell.CAMELLIA_REBIRTH, SpellVisualRules.Spell.VIRULENT_HARVEST, SpellVisualRules.Spell.GOLDEN_HARVEST -> true;
            default -> false;
        };
        double progress = SpellBeamModels.ease((age + 0.5) / 6.0);
        double scale = Math.min(1.0, length / 3.2);
        double center = tether ? length * 0.5 : 1.5 * scale + Math.max(0.0, length - 3.0 * scale) * progress;
        double roll = switch (spell) {
            case SpellVisualRules.Spell.STAR_CROSS, SpellVisualRules.Spell.PRISM_BATTERY -> age * 0.12;
            case SpellVisualRules.Spell.FLESH_BLOOM, SpellVisualRules.Spell.CAMELLIA_REBIRTH, SpellVisualRules.Spell.GOLDEN_HARVEST, SpellVisualRules.Spell.GILDED_VERDICT, SpellVisualRules.Spell.SERAPH_DESCENT, SpellVisualRules.Spell.TIDAL_EDICT, SpellVisualRules.Spell.EFFIGY_VOLLEY, SpellVisualRules.Spell.EFFIGY_PROCESSION -> Math.sin(age * 0.25) * 0.035;
            default -> age * 0.025;
        };
        Mesh mesh = new Mesh(SpellBeamModels.transform(new ManorStandaloneSpellModels.Point(0.0, 0.0, center), scale, roll, bounded), spell.color, age, reduced);
        switch (spell) {
            case CAPACITOR_OVERLOAD: {
                SpellBeamModels.capacitor(mesh, age);
                break;
            }
            case STATIC_LATTICE: {
                SpellBeamModels.lattice(mesh, age);
                break;
            }
            case MAGNETIC_STORM: {
                SpellBeamModels.magnet(mesh, age);
                break;
            }
            case PRESSURE_LANCE: {
                SpellBeamModels.pressure(mesh, age);
                break;
            }
            case ION_RAIL: {
                SpellBeamModels.rail(mesh, age);
                break;
            }
            case STAR_CROSS: {
                SpellBeamModels.star(mesh, age);
                break;
            }
            case FROSTGLASS_LANCE: {
                SpellBeamModels.frostSpear(mesh);
                break;
            }
            case FROST_NEEDLES: {
                SpellBeamModels.needles(mesh);
                break;
            }
            case CAMELLIA_BLOOD_BLOOM: {
                SpellBeamModels.bloodFlower(mesh, age);
                break;
            }
            case SANGUINE_SUTURE: {
                SpellBeamModels.suture(mesh, age);
                break;
            }
            case FLESH_BLOOM: {
                SpellBeamModels.heart(mesh, age);
                break;
            }
            case CANDLE_ECLIPSE: {
                SpellBeamModels.eclipse(mesh, age);
                break;
            }
            case GILDED_VERDICT: {
                SpellBeamModels.verdict(mesh);
                break;
            }
            case EFFIGY_VOLLEY: {
                SpellBeamModels.puppet(mesh, new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 1.0, false);
                break;
            }
            case FEATHER_FAN: {
                SpellBeamModels.feather(mesh, 0.0, 0.0, 1.0);
                break;
            }
            case CAMELLIA_SANCTUARY: {
                SpellBeamModels.sanctuary(mesh, age);
                break;
            }
            case CAMELLIA_REBIRTH: {
                SpellBeamModels.rebirth(mesh, age);
                break;
            }
            case VIRULENT_HARVEST: {
                SpellBeamModels.sickle(mesh.at(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 1.0, Math.sin(age * 0.3) * 0.25), false);
                break;
            }
            case GOLDEN_HARVEST: {
                SpellBeamModels.branch(mesh, age);
                break;
            }
            case PRISM_BATTERY: {
                SpellBeamModels.prism(mesh, age);
                break;
            }
            case EFFIGY_PROCESSION: {
                SpellBeamModels.procession(mesh);
                break;
            }
            case NIGHTFALL_BLADES: {
                SpellBeamModels.nightBlades(mesh);
                break;
            }
            case SERAPH_DESCENT: {
                SpellBeamModels.seraph(mesh);
                break;
            }
            case TIDAL_EDICT: {
                SpellBeamModels.tide(mesh);
                break;
            }
            case CINDER_BREATH: {
                SpellBeamModels.cinder(mesh, age);
                break;
            }
            case CANDLE_REAP: {
                SpellBeamModels.candleSickle(mesh);
                break;
            }
            default: {
                throw new IllegalStateException("Missing beam mesh: " + spell.id);
            }
        }
        int knots = reduced ? 3 : (tether ? 7 : 5);
        Mesh path = new Mesh(bounded, spell.color);
        for (int knot = 1; knot <= knots; ++knot) {
            double depth = length * (double)knot / (double)(knots + 1);
            if (!tether && depth > center - 1.5 * scale) continue;
            double radius = (tether ? 0.025 : 0.015) * (0.7 + 0.3 * Math.sin(age * 0.3 + (double)knot));
            path.gem(new ManorStandaloneSpellModels.Point(0.0, 0.0, depth), radius * scale, Math.min(0.1, length / 30.0), spell.color);
        }
        return count[0];
    }

    private static void capacitor(Mesh mesh, double age) {
        mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 0.25, 0.65, mesh.color);
        for (int prong = 0; prong < 4; ++prong) {
            double angle = (double)prong * Math.PI / 2.0 + age * 0.025;
            ManorStandaloneSpellModels.Point rear = SpellBeamModels.radial(0.28, angle, -0.62);
            ManorStandaloneSpellModels.Point outer = SpellBeamModels.radial(0.3 + mesh.open() * 0.23, angle, 0.08);
            ManorStandaloneSpellModels.Point tip = SpellBeamModels.radial(0.15 + mesh.open() * 0.12, angle, 0.6);
            mesh.rod(rear, outer, 0.055, 3425373);
            mesh.rod(outer, tip, 0.04, 14332531);
            mesh.gem(outer, 0.095, 0.16, 13294823);
        }
        mesh.ring(0.3, 0.04, -0.4, 12, 14332531);
        mesh.at(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 1.0, -age * 0.12).ornament(0.31, -0.4, 4, 13294823);
    }

    private static void lattice(Mesh mesh, double age) {
        for (int gate = -1; gate <= 1; ++gate) {
            double depth = (double)gate * 0.65;
            for (int edge = 0; edge < 4; ++edge) {
                double angle = (double)edge * Math.PI / 2.0 + 0.7853981633974483 + Math.sin(age * 0.22 + (double)gate) * 0.22;
                ManorStandaloneSpellModels.Point first = SpellBeamModels.radial(0.5, angle, depth);
                ManorStandaloneSpellModels.Point second = SpellBeamModels.radial(0.5, angle + 1.5707963267948966, depth);
                mesh.rod(first, second, 0.035, 3425373);
                mesh.gem(first, 0.07, 0.1, mesh.color);
                if (mesh.reduced) continue;
                mesh.rod(first.scale(0.75).add(new ManorStandaloneSpellModels.Point(0.0, 0.0, depth * 0.25)), second.scale(0.75).add(new ManorStandaloneSpellModels.Point(0.0, 0.0, depth * 0.25)), 0.014, 14332531);
            }
        }
        mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 0.0, Math.sin(age * 0.18) * 0.4), 0.16, 0.24, mesh.color);
    }

    private static void magnet(Mesh mesh, double age) {
        for (int side : new int[]{-1, 1}) {
            double offset = (double)side * (0.18 + 0.12 * mesh.open());
            mesh.rod(new ManorStandaloneSpellModels.Point(offset, -0.33, -0.4), new ManorStandaloneSpellModels.Point(offset, -0.33, 0.4), 0.085, 3425373);
            for (int pole : new int[]{-1, 1}) {
                mesh.rod(new ManorStandaloneSpellModels.Point(offset, -0.33, (double)pole * 0.4), new ManorStandaloneSpellModels.Point(offset, 0.22, (double)pole * 0.4), 0.085, 13294823);
                mesh.box(new ManorStandaloneSpellModels.Point(offset, 0.23, (double)pole * 0.4), 0.17, 0.18, 0.18, pole > 0 ? mesh.color : 12540269);
            }
        }
        mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 0.34, 0.0), 0.13 + 0.025 * Math.sin(age * 0.4), 0.2, mesh.color);
        mesh.at(new ManorStandaloneSpellModels.Point(0.0, 0.34, 0.0), 1.0, 0.0).pitch(age * 0.08).ring(0.25, 0.025, 0.0, 12, 14332531);
    }

    private static void pressure(Mesh mesh, double age) {
        mesh.taper(-0.95, 0.17, 0.32, 0.17, 3425373);
        for (int coil = 0; coil < 5; ++coil) {
            double depth = -0.85 + (double)coil * (0.18 + 0.05 * mesh.open());
            mesh.ring(0.26, 0.065, depth, 12, coil % 2 == 0 ? 13294823 : 14332531);
            if (mesh.reduced) continue;
            mesh.at(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 1.0, (double)coil * 0.4 + age * 0.08).ornament(0.27, depth, 3, mesh.color);
        }
        mesh.taper(0.35, 0.18, 1.22, 0.0, mesh.color);
        for (int fin = 0; fin < 3; ++fin) {
            mesh.leaf(SpellBeamModels.radial(0.18, (double)fin * Math.PI * 2.0 / 3.0, -0.5), SpellBeamModels.radial(0.24 + mesh.open() * 0.22, (double)fin * Math.PI * 2.0 / 3.0, -0.9), 0.11, 14332531);
        }
    }

    private static void rail(Mesh mesh, double age) {
        for (int side : new int[]{-1, 1}) {
            double spread = (double)side * (0.17 + mesh.open() * 0.08);
            mesh.box(new ManorStandaloneSpellModels.Point(spread, 0.0, -0.12), 0.11, 0.18, 1.6, 3425373);
            mesh.rod(new ManorStandaloneSpellModels.Point(spread, 0.105, -0.78), new ManorStandaloneSpellModels.Point(spread, 0.105, 0.63), 0.02, mesh.color);
            for (int tooth = 0; tooth < 4; ++tooth) {
                mesh.box(new ManorStandaloneSpellModels.Point(spread, 0.0, -0.66 + (double)tooth * 0.36), 0.16, 0.24, 0.1, 14332531);
            }
        }
        mesh.taper(-0.75, 0.065, 0.65, 0.065, mesh.color);
        mesh.taper(0.65, 0.11, 1.3, 0.0, 13294823);
        mesh.ring(0.33, 0.05, -0.88, 8, mesh.color);
    }

    private static void star(Mesh mesh, double age) {
        mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 0.23, 0.35, 13294823);
        for (int blade = 0; blade < 4; ++blade) {
            double angle = (double)blade * Math.PI / 2.0;
            double reach = 0.48 + mesh.open() * 0.34;
            mesh.leaf(SpellBeamModels.radial(0.12, angle, 0.0), SpellBeamModels.radial(reach, angle, 0.15), 0.16, mesh.color);
            if (mesh.reduced) continue;
            mesh.leaf(SpellBeamModels.radial(0.19, angle, 0.05), SpellBeamModels.radial(reach * 0.88, angle, 0.2), 0.055, 13294823);
        }
        mesh.ring(0.35, 0.035, -0.07, 12, 14332531);
        mesh.at(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 1.0, -age * 0.09).ring(0.48, 0.022, 0.04, 16, 13294823);
        mesh.taper(0.2, 0.11, 0.85, 0.0, mesh.color);
    }

    private static void frostSpear(Mesh mesh) {
        mesh.taper(-1.05, 0.08, 0.25, 0.08, 13294823);
        mesh.taper(0.2, 0.18, 1.36, 0.0, mesh.color);
        for (int barb = 0; barb < 4; ++barb) {
            mesh.leaf(SpellBeamModels.radial(0.04, (double)barb * Math.PI / 2.0, 0.25), SpellBeamModels.radial(0.2 + mesh.open() * 0.17, (double)barb * Math.PI / 2.0, -0.25), 0.1, 7578825);
        }
        mesh.ring(0.14, 0.045, -0.64, 8, 14332531);
        mesh.ring(0.19, 0.026, -0.28, 12, 9296102);
        mesh.ornament(0.1, -0.32, 4, 9296102);
    }

    private static void needles(Mesh mesh) {
        for (int needle = 0; needle < 3; ++needle) {
            ManorStandaloneSpellModels.Point center = SpellBeamModels.radial(0.12 + mesh.open() * 0.12, (double)needle * Math.PI * 2.0 / 3.0 + mesh.age * 0.06, (double)needle * 0.14 - 0.15 + Math.sin(mesh.age * 0.3 + (double)needle) * 0.07);
            Mesh part = mesh.at(center, 1.0, 0.0);
            part.taper(-0.92, 0.055, 0.08, 0.085, 4352135);
            part.taper(0.08, 0.085, 1.0, 0.0, mesh.color);
            part.ring(0.1, 0.025, -0.55, 6, 13294823);
            part.ornament(0.1, -0.25, 3, mesh.color);
        }
    }

    private static void bloodFlower(Mesh mesh, double age) {
        mesh.flower(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 8, 0.48, mesh.color);
        if (!mesh.reduced) {
            mesh.at(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.09), 1.0, 0.39269908169872414).flower(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 5, 0.27, 15043740);
        }
        mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.17), 0.16, 0.26, 14332531);
        for (int thorn = 0; thorn < 4; ++thorn) {
            mesh.leaf(SpellBeamModels.radial(0.18, (double)thorn * Math.PI / 2.0, -0.2), SpellBeamModels.radial(0.5, (double)thorn * Math.PI / 2.0 + 0.35, -0.7), 0.085, 5782608);
        }
    }

    private static void suture(Mesh mesh, double age) {
        mesh.taper(-1.08, 0.06, 0.65, 0.06, 13294823);
        mesh.taper(0.65, 0.09, 1.23, 0.0, 16179159);
        mesh.ring(0.15, 0.035, -0.78, 10, 9190227);
        for (int stitch = 0; stitch < 4; ++stitch) {
            double depth = -0.55 + (double)stitch * 0.28;
            double pull = 0.1 + 0.13 * (0.5 + 0.5 * Math.sin(age * 0.36 - (double)stitch * 0.7));
            ManorStandaloneSpellModels.Point knot = new ManorStandaloneSpellModels.Point(0.0, 0.1, depth + 0.08);
            mesh.rod(new ManorStandaloneSpellModels.Point(-pull, -0.1, depth), knot, 0.025, mesh.color);
            mesh.rod(knot, new ManorStandaloneSpellModels.Point(pull, -0.1, depth + 0.16), 0.025, mesh.color);
            mesh.gem(knot, 0.04, 0.045, 14332531);
        }
    }

    private static void heart(Mesh mesh, double age) {
        double pulse = 1.0 + Math.sin(age * 0.65) * 0.075;
        Mesh organ = mesh.at(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), pulse, 0.0);
        organ.gem(new ManorStandaloneSpellModels.Point(-0.14, 0.12, 0.0), 0.25, 0.34, 9252683);
        organ.gem(new ManorStandaloneSpellModels.Point(0.14, 0.12, 0.0), 0.25, 0.34, mesh.color);
        organ.leaf(new ManorStandaloneSpellModels.Point(0.0, 0.15, 0.0), new ManorStandaloneSpellModels.Point(0.0, -0.5, 0.25), 0.29, 11092062);
        for (int vessel = 0; vessel < 3; ++vessel) {
            organ.rod(new ManorStandaloneSpellModels.Point((double)(vessel - 1) * 0.12, 0.2, 0.0), new ManorStandaloneSpellModels.Point((double)(vessel - 1) * 0.24, 0.6, -0.3), 0.055, 14847122);
        }
        if (!mesh.reduced) {
            for (int side : new int[]{-1, 1}) {
                ManorStandaloneSpellModels.Point fork = new ManorStandaloneSpellModels.Point((double)side * 0.14, 0.08, 0.3);
                organ.rod(new ManorStandaloneSpellModels.Point(0.0, -0.29, 0.25), fork, 0.019, 15708597);
                organ.rod(fork, new ManorStandaloneSpellModels.Point((double)side * 0.3, 0.14, 0.19), 0.013, 12871804);
                organ.rod(fork, new ManorStandaloneSpellModels.Point((double)side * 0.1, 0.3, 0.19), 0.013, 12871804);
            }
        }
    }

    private static void eclipse(Mesh mesh, double age) {
        mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 0.33, 0.11, 3221054);
        mesh.ring(0.38, 0.055, 0.0, 16, 14332531);
        for (int candle = 0; candle < 6; ++candle) {
            ManorStandaloneSpellModels.Point center = SpellBeamModels.radial(0.45 + 0.12 * mesh.open(), (double)candle * Math.PI / 3.0 - age * 0.06, 0.0);
            mesh.at(center, 1.0, 0.0).taper(-0.28, 0.065, 0.12, 0.065, 13294823);
            mesh.gem(new ManorStandaloneSpellModels.Point(center.x(), center.y(), 0.22), 0.07, 0.14 + Math.sin(age * 0.8 + (double)candle) * 0.035, mesh.color);
            mesh.at(center, 1.0, 0.0).ring(0.1, 0.03, -0.25, 8, 14332531);
        }
    }

    private static void verdict(Mesh mesh) {
        SpellBeamModels.sword(mesh, 14332531, 16773308);
        Mesh halo = mesh.at(new ManorStandaloneSpellModels.Point(0.0, 0.0, -0.28), 1.0, -mesh.age * 0.12).pitch(Math.sin(mesh.age * 0.3) * 0.2);
        halo.ring(0.46, 0.055, 0.0, 12, 14332531);
        for (int ray = 0; ray < 4; ++ray) {
            halo.gem(SpellBeamModels.radial(0.5 + mesh.open() * 0.1, (double)ray * Math.PI / 2.0, 0.0), 0.055, 0.1, mesh.color);
        }
    }

    private static void doraFocusLaser(Mesh mesh, double age, double length, boolean reduced) {
        double scale = Math.min(1.0, length / 1.5);
        double core = Math.max(8.0E-4, 0.052 * scale);
        double cage = Math.max(core * 1.8, 0.18 * scale);
        mesh.taper(0.0, core, length, core, 0xE8FFFF);
        int rails = reduced ? 3 : 4;
        int sections = reduced ? 3 : 6;
        double twist = age * 0.09;
        double railRadius = Math.max(5.0E-4, 0.018 * scale);
        double railPadding = Math.min(length * 0.2, Math.max(0.001, railRadius * 1.5));
        double railLength = Math.max(0.0, length - railPadding * 2.0);
        for (int rail = 0; rail < rails; ++rail) {
            double base = (double)rail * Math.PI * 2.0 / (double)rails;
            for (int section = 0; section < sections; ++section) {
                double firstDepth = railPadding + railLength * (double)section / (double)sections;
                double secondDepth = railPadding + railLength * ((double)section + 1.0) / (double)sections;
                double firstAngle = base + twist + firstDepth / Math.max(0.015, length) * Math.PI * 0.55;
                double secondAngle = base + twist + secondDepth / Math.max(0.015, length) * Math.PI * 0.55;
                mesh.rod(SpellBeamModels.radial(cage, firstAngle, firstDepth), SpellBeamModels.radial(cage, secondAngle, secondDepth), railRadius, rail % 2 == 0 ? 3788741 : 0x9FFFF0);
            }
        }
        if (length < 0.052) {
            return;
        }
        int gates = reduced ? 3 : 5;
        double usable = length - 0.052;
        for (int gate = 0; gate < gates; ++gate) {
            double along = ((double)gate + 0.28 + age * 0.2) / (double)gates;
            along -= Math.floor(along);
            mesh.ring(cage * 1.12, Math.max(0.001, cage * 0.18), 0.026 + usable * along, reduced ? 8 : 12, gate % 2 == 0 ? 6815712 : 0xBFFFF7);
        }
        mesh.ring(cage * 1.35, Math.max(0.001, cage * 0.2), 0.026, reduced ? 8 : 12, 14286839);
        mesh.ring(cage * 1.5, Math.max(0.001, cage * 0.16), length - 0.026, reduced ? 8 : 12, 0xFFFFFF);
    }

    private static void puppet(Mesh mesh, ManorStandaloneSpellModels.Point center, double scale, boolean crowned) {
        double step = Math.sin(mesh.age * 0.48 + center.x() * 4.0);
        Mesh doll = mesh.at(center.add(new ManorStandaloneSpellModels.Point(0.0, step * 0.025, 0.0)), scale, step * 0.06);
        Mesh head = doll.at(new ManorStandaloneSpellModels.Point(0.0, 0.26, 0.08), 1.0, -step * 0.1);
        head.gem(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 0.17, 0.18, 15981240);
        doll.box(new ManorStandaloneSpellModels.Point(0.0, -0.06, 0.0), 0.27, 0.32, 0.2, 6443377);
        doll.box(new ManorStandaloneSpellModels.Point(0.0, -0.02, 0.115), 0.12, 0.18, 0.04, mesh.color);
        for (int side : new int[]{-1, 1}) {
            ManorStandaloneSpellModels.Point shoulder = new ManorStandaloneSpellModels.Point((double)side * 0.13, 0.02, 0.0);
            ManorStandaloneSpellModels.Point elbow = new ManorStandaloneSpellModels.Point((double)side * (0.19 + 0.07 * mesh.open()), -0.1, (double)side * step * 0.08);
            ManorStandaloneSpellModels.Point hand = new ManorStandaloneSpellModels.Point((double)side * (0.23 + 0.12 * mesh.open()), -0.16 + step * (double)side * 0.06, 0.08);
            ManorStandaloneSpellModels.Point knee = new ManorStandaloneSpellModels.Point((double)side * 0.12, -0.34, step * (double)side * 0.09);
            ManorStandaloneSpellModels.Point foot = new ManorStandaloneSpellModels.Point((double)side * 0.17, -0.49 + step * (double)side * 0.02, 0.05 - step * (double)side * 0.08);
            doll.rod(shoulder, elbow, 0.05, 14332531);
            doll.rod(elbow, hand, 0.04, 13294823);
            doll.rod(new ManorStandaloneSpellModels.Point((double)side * 0.07, -0.2, 0.0), knee, 0.055, 14332531);
            doll.rod(knee, foot, 0.045, 13294823);
            doll.gem(elbow, 0.052, 0.056, 3425373);
            doll.gem(knee, 0.054, 0.057, 3425373);
            doll.box(foot, 0.1, 0.075, 0.14, 6443377);
            head.box(new ManorStandaloneSpellModels.Point((double)side * 0.06, 0.02, 0.17), 0.045, 0.04, 0.025, 3424599);
        }
        if (crowned) {
            for (int spike = 0; spike < 3; ++spike) {
                head.gem(new ManorStandaloneSpellModels.Point((double)(spike - 1) * 0.1, 0.24, 0.0), 0.05, 0.11, 14332531);
            }
        } else {
            doll.ring(0.25, 0.035, -0.06, 12, 14332531);
        }
    }

    private static void feather(Mesh mesh, double offset, double depth, double scale) {
        Mesh quill = mesh.at(new ManorStandaloneSpellModels.Point(offset, 0.0, depth), scale, 0.0);
        quill.rod(new ManorStandaloneSpellModels.Point(0.0, 0.0, -1.0), new ManorStandaloneSpellModels.Point(0.0, 0.0, 1.15), 0.028, 14332531);
        quill.ring(0.22, 0.028, -0.78, 16, 14332531);
        for (int barb = 0; barb < 6; ++barb) {
            double along = -0.55 + (double)barb * 0.25;
            double width = (0.12 + Math.sin((double)(barb + 1) * Math.PI / 8.0) * 0.22) * (0.65 + 0.35 * mesh.open());
            for (int side : new int[]{-1, 1}) {
                quill.leaf(new ManorStandaloneSpellModels.Point(0.0, 0.0, along), new ManorStandaloneSpellModels.Point((double)side * width, 0.025 + Math.sin(mesh.age * 0.4 - (double)barb * 0.6) * 0.055, along - 0.3), 0.07, barb % 2 == 0 ? 13294823 : mesh.color);
            }
        }
    }

    private static void sanctuary(Mesh mesh, double age) {
        mesh.flower(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 6, 0.46, mesh.color);
        mesh.ring(0.53, 0.04, -0.13, 16, 14332531);
        mesh.at(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 1.0, -age * 0.08).ornament(0.54, -0.13, 6, 13294823);
        mesh.box(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.24), 0.1, 0.38, 0.06, 13294823);
        mesh.box(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.24), 0.3, 0.09, 0.06, 13294823);
    }

    private static void rebirth(Mesh mesh, double age) {
        mesh.gem(new ManorStandaloneSpellModels.Point(0.0, -0.16, 0.0), 0.24, 0.36, 13476723);
        mesh.rod(new ManorStandaloneSpellModels.Point(0.0, -0.18, 0.0), new ManorStandaloneSpellModels.Point(0.0, 0.65, 0.08), 0.045, 4351830);
        for (int leaf = 0; leaf < 4; ++leaf) {
            mesh.leaf(new ManorStandaloneSpellModels.Point(0.0, 0.05 + (double)leaf * 0.13, 0.0), new ManorStandaloneSpellModels.Point((double)(leaf % 2 == 0 ? -1 : 1) * (0.18 + mesh.open() * 0.2), 0.24 + (double)leaf * 0.13, 0.09 + Math.sin(age * 0.25 + (double)leaf) * 0.06), 0.12, mesh.color);
        }
        mesh.flower(new ManorStandaloneSpellModels.Point(0.0, 0.65, 0.08), 5, 0.17, 15456698);
    }

    private static void sickle(Mesh mesh, boolean golden) {
        mesh.rod(new ManorStandaloneSpellModels.Point(0.0, -0.5, -0.68), new ManorStandaloneSpellModels.Point(0.0, 0.32, 0.4), 0.055, golden ? 14332531 : 3425373);
        for (int blade = 0; blade < 12; ++blade) {
            double angle = (double)blade * Math.PI * 0.86 / 12.0;
            double next = (double)(blade + 1) * Math.PI * 0.86 / 12.0;
            double bladeWidth = 0.14 + 0.05 * mesh.open();
            double width = bladeWidth * Math.sin((double)(blade + 1) * Math.PI / 13.0);
            double nextWidth = blade == 11 ? 0.0 : bladeWidth * Math.sin((double)(blade + 2) * Math.PI / 13.0);
            ManorStandaloneSpellModels.Point outer = new ManorStandaloneSpellModels.Point(Math.sin(angle) * 0.7, 0.34, 0.4 + (1.0 - Math.cos(angle)) * 0.32);
            ManorStandaloneSpellModels.Point nextOuter = new ManorStandaloneSpellModels.Point(Math.sin(next) * 0.7, 0.34, 0.4 + (1.0 - Math.cos(next)) * 0.32);
            ManorStandaloneSpellModels.Point inner = outer.add(new ManorStandaloneSpellModels.Point(-width, 0.0, -width * 0.35));
            ManorStandaloneSpellModels.Point nextInner = nextOuter.add(new ManorStandaloneSpellModels.Point(-nextWidth, 0.0, -nextWidth * 0.35));
            ManorStandaloneSpellModels.Point thickness = new ManorStandaloneSpellModels.Point(0.0, -0.045, 0.0);
            mesh.sink.quad(outer, nextOuter, nextInner, inner, mesh.color);
            mesh.sink.quad(inner.add(thickness), nextInner.add(thickness), nextOuter.add(thickness), outer.add(thickness), SpellBeamModels.shade(mesh.color, 0.55));
            mesh.sink.quad(outer, outer.add(thickness), nextOuter.add(thickness), nextOuter, 13294823);
            mesh.sink.quad(inner, nextInner, nextInner.add(thickness), inner.add(thickness), golden ? 14332531 : 3425373);
        }
        mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 0.28, 0.36), 0.11, 0.14, golden ? 13294823 : 8031811);
        if (!mesh.reduced) {
            for (int wrap = 0; wrap < 4; ++wrap) {
                ManorStandaloneSpellModels.Point center = new ManorStandaloneSpellModels.Point(0.0, -0.4 + (double)wrap * 0.1, -0.55 + (double)wrap * 0.132);
                mesh.at(center, 1.0, 0.0).pitch(-0.65).ring(0.073, 0.025, 0.0, 8, 14332531);
            }
        }
    }

    private static void branch(Mesh mesh, double age) {
        mesh.rod(new ManorStandaloneSpellModels.Point(0.0, -0.55, -0.5), new ManorStandaloneSpellModels.Point(0.0, 0.6, 0.65), 0.055, 14332531);
        for (int twig = 0; twig < 3; ++twig) {
            int sign = twig % 2 == 0 ? -1 : 1;
            ManorStandaloneSpellModels.Point root = new ManorStandaloneSpellModels.Point(0.0, -0.3 + (double)twig * 0.32, -0.2 + (double)twig * 0.25);
            ManorStandaloneSpellModels.Point tip = root.add(new ManorStandaloneSpellModels.Point((double)sign * (0.25 + mesh.open() * 0.13), 0.13 + Math.sin(age * 0.3 + (double)twig) * 0.05, 0.15));
            mesh.rod(root, tip, 0.032, 8414279);
            mesh.gem(tip, 0.15, 0.2, mesh.color);
            mesh.leaf(tip, tip.add(new ManorStandaloneSpellModels.Point((double)sign * 0.15, 0.23, 0.0)), 0.07, 9677937);
        }
    }

    private static void prism(Mesh mesh, double age) {
        mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 0.28, 0.72, mesh.color);
        for (int strut = 0; strut < 3; ++strut) {
            double angle = (double)strut * Math.PI * 2.0 / 3.0 - age * 0.17;
            mesh.rod(SpellBeamModels.radial(0.37, angle, -0.65), SpellBeamModels.radial(0.37, angle + 0.2, 0.58), 0.045, 3425373);
            mesh.gem(SpellBeamModels.radial(0.37, angle, -0.65), 0.085, 0.12, 14332531);
        }
        mesh.ring(0.4, 0.05, -0.53, 12, 14332531);
        mesh.ring(0.35, 0.04, 0.5, 12, 13294823);
        mesh.pitch(Math.sin(age * 0.28) * 0.22).ring(0.43, 0.025, 0.0, 12, mesh.color);
        mesh.at(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.0), 1.0, age * 0.07).ornament(0.4, -0.53, 6, 13294823);
    }

    private static void procession(Mesh mesh) {
        SpellBeamModels.puppet(mesh, new ManorStandaloneSpellModels.Point(0.0, 0.14, 0.3), 0.8, true);
        SpellBeamModels.puppet(mesh, new ManorStandaloneSpellModels.Point(-0.44, -0.12, -0.3), 0.58, true);
        SpellBeamModels.puppet(mesh, new ManorStandaloneSpellModels.Point(0.44, -0.12, -0.3), 0.58, true);
        mesh.rod(new ManorStandaloneSpellModels.Point(-0.45, 0.27, -0.34), new ManorStandaloneSpellModels.Point(0.45, 0.27, -0.34), 0.025, 14332531);
    }

    private static void nightBlades(Mesh mesh) {
        for (int side : new int[]{-1, 1}) {
            SpellBeamModels.sword(mesh.at(new ManorStandaloneSpellModels.Point((double)side * (0.17 + 0.1 * mesh.open()), (double)side * 0.06, (double)side * (0.12 + Math.sin(mesh.age * 0.35) * 0.08)), 0.84, (double)side * (0.08 + 0.12 * mesh.open())), 3484488, mesh.color);
        }
        mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 0.25, 0.0), 0.09, 0.19, 13294823);
    }

    private static void seraph(Mesh mesh) {
        SpellBeamModels.sword(mesh, 14332531, 13294823);
        for (int side : new int[]{-1, 1}) {
            for (int plume = 0; plume < 4; ++plume) {
                mesh.leaf(new ManorStandaloneSpellModels.Point((double)side * 0.13, 0.0, -0.25 + (double)plume * 0.09), new ManorStandaloneSpellModels.Point((double)side * (0.73 - (double)plume * 0.09) * (0.6 + 0.4 * mesh.open()), 0.05 + Math.sin(mesh.age * 0.38 - (double)plume * 0.3) * 0.09, -0.87 + (double)plume * 0.23), 0.085, plume % 2 == 0 ? 13294823 : mesh.color);
            }
        }
        mesh.ring(0.25, 0.04, 0.44, 12, 14332531);
    }

    private static void sword(Mesh mesh, int guard, int edge) {
        mesh.leaf(new ManorStandaloneSpellModels.Point(0.0, 0.0, -0.35), new ManorStandaloneSpellModels.Point(0.0, 0.0, 1.3), 0.19, edge);
        mesh.rod(new ManorStandaloneSpellModels.Point(-0.33, 0.0, -0.35), new ManorStandaloneSpellModels.Point(0.33, 0.0, -0.35), 0.055, guard);
        mesh.taper(-0.85, 0.055, -0.37, 0.055, 3425373);
        mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 0.0, -0.94), 0.09, 0.14, guard);
        if (!mesh.reduced) {
            mesh.leaf(new ManorStandaloneSpellModels.Point(0.0, 0.053, -0.22), new ManorStandaloneSpellModels.Point(0.0, 0.027, 1.04), 0.044, mesh.color);
            mesh.gem(new ManorStandaloneSpellModels.Point(0.0, 0.064, -0.34), 0.075, 0.11, mesh.color);
            for (int wrap = 0; wrap < 3; ++wrap) {
                mesh.ring(0.07, 0.022, -0.76 + (double)wrap * 0.13, 8, guard);
            }
            for (int side : new int[]{-1, 1}) {
                mesh.leaf(new ManorStandaloneSpellModels.Point((double)side * 0.12, 0.0, -0.35), new ManorStandaloneSpellModels.Point((double)side * 0.36, 0.0, -0.17), 0.055, guard);
            }
        }
    }

    private static void tide(Mesh mesh) {
        for (int crest = 0; crest < 12; ++crest) {
            double depth = -1.1 + (double)crest * 2.2 / 12.0;
            double next = -1.1 + (double)(crest + 1) * 2.2 / 12.0;
            double height = SpellBeamModels.waveHeight(crest, mesh.age);
            double nextHeight = SpellBeamModels.waveHeight(crest + 1, mesh.age);
            ManorStandaloneSpellModels.Point base = new ManorStandaloneSpellModels.Point(0.15, -0.3, depth);
            ManorStandaloneSpellModels.Point nextBase = new ManorStandaloneSpellModels.Point(0.15, -0.3, next);
            ManorStandaloneSpellModels.Point crestLip = new ManorStandaloneSpellModels.Point(0.21, height, depth);
            ManorStandaloneSpellModels.Point nextLip = new ManorStandaloneSpellModels.Point(0.21, nextHeight, next);
            ManorStandaloneSpellModels.Point back = new ManorStandaloneSpellModels.Point(-0.15, -0.3, depth);
            ManorStandaloneSpellModels.Point nextBack = new ManorStandaloneSpellModels.Point(-0.15, -0.3, next);
            ManorStandaloneSpellModels.Point crestBack = new ManorStandaloneSpellModels.Point(-0.06, height + 0.05, depth);
            ManorStandaloneSpellModels.Point nextCrestBack = new ManorStandaloneSpellModels.Point(-0.06, nextHeight + 0.05, next);
            mesh.sink.quad(base, nextBase, nextLip, crestLip, crest % 3 == 0 ? 4423585 : mesh.color);
            mesh.sink.quad(crestLip, nextLip, nextCrestBack, crestBack, 13294823);
            mesh.sink.quad(crestBack, nextCrestBack, nextBack, back, 3432325);
            mesh.sink.quad(back, nextBack, nextBase, base, 2704222);
            if (!mesh.reduced && crest % 3 == 0) {
                mesh.rod(new ManorStandaloneSpellModels.Point(-0.12, height - 0.02, depth), new ManorStandaloneSpellModels.Point(-0.12, nextHeight - 0.02, next), 0.012, 14332531);
            }
            if (mesh.reduced || crest % 2 != 0) continue;
            mesh.gem(crestLip.add(new ManorStandaloneSpellModels.Point(0.04, 0.05, 0.0)), 0.043, 0.07, 12379881);
            mesh.rod(new ManorStandaloneSpellModels.Point(0.175, -0.06, depth), new ManorStandaloneSpellModels.Point(0.18, 0.12, next), 0.014, 0x8DD3DD);
        }
    }

    private static void cinder(Mesh mesh, double age) {
        mesh.taper(-1.02, 0.07, -0.45, 0.16, 10895404);
        mesh.taper(-0.45, 0.16, 0.45, 0.12, 14517563);
        mesh.taper(0.45, 0.12, 1.28, 0.0, mesh.color);
        for (int flame = 0; flame < 5; ++flame) {
            double depth = -0.9 + (double)flame * 0.43;
            double height = 0.45 + Math.sin((double)flame * 1.3 + age * 0.3) * 0.2;
            mesh.leaf(new ManorStandaloneSpellModels.Point(0.0, -0.12, depth), new ManorStandaloneSpellModels.Point(0.05, height, depth + 0.22), 0.18, 12080438);
            mesh.leaf(new ManorStandaloneSpellModels.Point(0.045, -0.15, depth + 0.03), new ManorStandaloneSpellModels.Point(0.05, height * 0.7, depth + 0.25), 0.1, mesh.color);
        }
        mesh.ring(0.17, 0.06, -1.03, 8, 14332531);
    }

    private static void candleSickle(Mesh mesh) {
        SpellBeamModels.sickle(mesh, true);
        mesh.at(new ManorStandaloneSpellModels.Point(0.0, -0.12, -0.15), 1.0, 0.0).taper(-0.2, 0.085, 0.25, 0.085, 14998469);
        mesh.gem(new ManorStandaloneSpellModels.Point(0.025 * Math.sin(mesh.age * 0.5), -0.12, 0.27), 0.09, 0.17 + Math.sin(mesh.age * 0.75) * 0.035, 16042870);
        mesh.ring(0.18, 0.04, -0.38, 10, 14332531);
    }

    private static double waveHeight(int crest, double age) {
        return 0.1 + Math.sin((double)crest * Math.PI / 12.0) * (0.5 + Math.sin(age * 0.38 - (double)crest * 0.3) * 0.1);
    }

    private static double ease(double progress) {
        double clamped = Math.max(0.0, Math.min(1.0, progress));
        return clamped * clamped * (3.0 - 2.0 * clamped);
    }

    private static ManorStandaloneSpellModels.Sink transform(ManorStandaloneSpellModels.Point center, double scale, double roll, ManorStandaloneSpellModels.Sink sink) {
        return (first, second, third, fourth, color) -> sink.quad(SpellBeamModels.place(first, center, scale, roll), SpellBeamModels.place(second, center, scale, roll), SpellBeamModels.place(third, center, scale, roll), SpellBeamModels.place(fourth, center, scale, roll), color);
    }

    private static ManorStandaloneSpellModels.Point place(ManorStandaloneSpellModels.Point point, ManorStandaloneSpellModels.Point center, double scale, double roll) {
        return center.add(new ManorStandaloneSpellModels.Point((point.x() * Math.cos(roll) - point.y() * Math.sin(roll)) * scale, (point.x() * Math.sin(roll) + point.y() * Math.cos(roll)) * scale, point.z() * scale));
    }

    private static ManorStandaloneSpellModels.Point pitch(ManorStandaloneSpellModels.Point point, double angle) {
        return new ManorStandaloneSpellModels.Point(point.x(), point.y() * Math.cos(angle) - point.z() * Math.sin(angle), point.y() * Math.sin(angle) + point.z() * Math.cos(angle));
    }

    private static ManorStandaloneSpellModels.Point radial(double radius, double angle, double depth) {
        return new ManorStandaloneSpellModels.Point(Math.cos(angle) * radius, Math.sin(angle) * radius, depth);
    }

    private static int shade(int color, double amount) {
        int result = 0;
        for (int shift = 0; shift <= 16; shift += 8) {
            result |= Math.min(255, (int)((double)(color >> shift & 0xFF) * amount)) << shift;
        }
        return result;
    }

    private static final class Mesh {
        private final ManorStandaloneSpellModels.Sink sink;
        private final int color;
        private final double age;
        private final boolean reduced;

        private Mesh(ManorStandaloneSpellModels.Sink sink, int color) {
            this(sink, color, 0.0, true);
        }

        private Mesh(ManorStandaloneSpellModels.Sink sink, int color, double age, boolean reduced) {
            this.sink = sink;
            this.color = color;
            this.age = age;
            this.reduced = reduced;
        }

        private double open() {
            return SpellBeamModels.ease(this.age / 3.5) * (1.0 - 0.55 * SpellBeamModels.ease((this.age - 8.0) / 4.0));
        }

        private Mesh at(ManorStandaloneSpellModels.Point center, double scale, double roll) {
            return new Mesh(SpellBeamModels.transform(center, scale, roll, this.sink), this.color, this.age, this.reduced);
        }

        private Mesh pitch(double angle) {
            return new Mesh((first, second, third, fourth, tint) -> this.sink.quad(SpellBeamModels.pitch(first, angle), SpellBeamModels.pitch(second, angle), SpellBeamModels.pitch(third, angle), SpellBeamModels.pitch(fourth, angle), tint), this.color, this.age, this.reduced);
        }

        private void taper(double rear, double rearRadius, double front, double frontRadius, int tint) {
            int facets = this.reduced ? 8 : 12;
            for (int facet = 0; facet < facets; ++facet) {
                double angle = (double)facet * Math.PI * 2.0 / (double)facets;
                double next = (double)(facet + 1) * Math.PI * 2.0 / (double)facets;
                this.sink.quad(SpellBeamModels.radial(rearRadius, angle, rear), SpellBeamModels.radial(rearRadius, next, rear), SpellBeamModels.radial(frontRadius, next, front), SpellBeamModels.radial(frontRadius, angle, front), SpellBeamModels.shade(tint, 0.56 + (double)facet * 0.44 / (double)Math.max(1, facets - 1)));
            }
        }

        private void gem(ManorStandaloneSpellModels.Point center, double radius, double halfLength, int tint) {
            Mesh moved = this.at(center, 1.0, 0.0);
            if (this.reduced || radius < 0.06) {
                moved.taper(-halfLength, 0.0, 0.0, radius, tint);
                moved.taper(0.0, radius, halfLength, 0.0, tint);
            } else {
                moved.taper(-halfLength, 0.0, -halfLength * 0.18, radius, SpellBeamModels.shade(tint, 0.8));
                moved.taper(-halfLength * 0.18, radius, halfLength * 0.28, radius * 0.88, tint);
                moved.taper(halfLength * 0.28, radius * 0.88, halfLength, 0.0, tint);
            }
        }

        private void ornament(double radius, double depth, int count, int tint) {
            if (this.reduced) {
                return;
            }
            for (int mark = 0; mark < count; ++mark) {
                double angle = (double)mark * Math.PI * 2.0 / (double)count;
                this.leaf(SpellBeamModels.radial(radius - 0.025, angle - 0.08, depth), SpellBeamModels.radial(radius + 0.025, angle + 0.08, depth), 0.022, tint);
            }
        }

        private void ring(double radius, double width, double depth, int segments, int tint) {
            segments = this.reduced ? Math.max(8, segments) : Math.max(20, segments);
            for (int segment = 0; segment < segments; ++segment) {
                double angle = (double)segment * Math.PI * 2.0 / (double)segments;
                double next = (double)(segment + 1) * Math.PI * 2.0 / (double)segments;
                ManorStandaloneSpellModels.Point outerRear = SpellBeamModels.radial(radius, angle, depth - 0.025);
                ManorStandaloneSpellModels.Point nextRear = SpellBeamModels.radial(radius, next, depth - 0.025);
                ManorStandaloneSpellModels.Point outerFront = SpellBeamModels.radial(radius, angle, depth + 0.025);
                ManorStandaloneSpellModels.Point nextFront = SpellBeamModels.radial(radius, next, depth + 0.025);
                ManorStandaloneSpellModels.Point innerRear = SpellBeamModels.radial(radius - width, angle, depth - 0.025);
                ManorStandaloneSpellModels.Point nextInnerRear = SpellBeamModels.radial(radius - width, next, depth - 0.025);
                ManorStandaloneSpellModels.Point innerFront = SpellBeamModels.radial(radius - width, angle, depth + 0.025);
                ManorStandaloneSpellModels.Point nextInnerFront = SpellBeamModels.radial(radius - width, next, depth + 0.025);
                this.sink.quad(outerRear, nextRear, nextFront, outerFront, SpellBeamModels.shade(tint, 0.68));
                this.sink.quad(innerFront, nextInnerFront, nextInnerRear, innerRear, SpellBeamModels.shade(tint, 0.56));
                this.sink.quad(outerFront, nextFront, nextInnerFront, innerFront, tint);
                this.sink.quad(innerRear, nextInnerRear, nextRear, outerRear, SpellBeamModels.shade(tint, 0.8));
            }
        }

        private void rod(ManorStandaloneSpellModels.Point start, ManorStandaloneSpellModels.Point end, double radius, int tint) {
            ManorStandaloneSpellModels.Point direction;
            ManorStandaloneSpellModels.Point across = direction.cross(Math.abs((direction = end.sub(start).normal()).y()) > 0.9 ? new ManorStandaloneSpellModels.Point(1.0, 0.0, 0.0) : new ManorStandaloneSpellModels.Point(0.0, 1.0, 0.0)).normal();
            ManorStandaloneSpellModels.Point upward = direction.cross(across).normal();
            int facets = this.reduced ? 6 : 10;
            for (int facet = 0; facet < facets; ++facet) {
                double angle = (double)facet * Math.PI * 2.0 / (double)facets;
                double next = (double)(facet + 1) * Math.PI * 2.0 / (double)facets;
                ManorStandaloneSpellModels.Point first = across.scale(Math.cos(angle) * radius).add(upward.scale(Math.sin(angle) * radius));
                ManorStandaloneSpellModels.Point second = across.scale(Math.cos(next) * radius).add(upward.scale(Math.sin(next) * radius));
                this.sink.quad(start.add(first), start.add(second), end.add(second), end.add(first), SpellBeamModels.shade(tint, 0.65 + (double)facet * 0.33 / (double)Math.max(1, facets - 1)));
            }
        }

        private void leaf(ManorStandaloneSpellModels.Point root, ManorStandaloneSpellModels.Point tip, double width, int tint) {
            ManorStandaloneSpellModels.Point direction;
            ManorStandaloneSpellModels.Point across = direction.cross(Math.abs((direction = tip.sub(root).normal()).z()) < 0.45 ? new ManorStandaloneSpellModels.Point(0.0, 0.0, 1.0) : new ManorStandaloneSpellModels.Point(0.0, 1.0, 0.0)).normal();
            ManorStandaloneSpellModels.Point normal = direction.cross(across).normal().scale(width * 0.26);
            if (!this.reduced && width >= 0.065) {
                ManorStandaloneSpellModels.Point previousLeft = root;
                ManorStandaloneSpellModels.Point previousRight = root;
                ManorStandaloneSpellModels.Point previousTop = root;
                ManorStandaloneSpellModels.Point previousBottom = root;
                for (int section = 1; section <= 4; ++section) {
                    double progress = (double)section / 4.0;
                    double profile = section == 4 ? 0.0 : Math.sin(progress * Math.PI) * (1.1 - progress * 0.3);
                    ManorStandaloneSpellModels.Point center = root.add(tip.sub(root).scale(progress));
                    ManorStandaloneSpellModels.Point left = center.add(across.scale(width * profile));
                    ManorStandaloneSpellModels.Point right = center.sub(across.scale(width * profile));
                    ManorStandaloneSpellModels.Point top = center.add(normal.scale(profile));
                    ManorStandaloneSpellModels.Point bottom = center.sub(normal.scale(profile * 0.65));
                    if (section == 1) {
                        this.sink.quad(root, left, top, top, SpellBeamModels.shade(tint, 0.84));
                        this.sink.quad(root, top, right, right, tint);
                        this.sink.quad(root, bottom, left, left, SpellBeamModels.shade(tint, 0.55));
                        this.sink.quad(root, right, bottom, bottom, SpellBeamModels.shade(tint, 0.65));
                    } else if (section == 4) {
                        this.sink.quad(previousLeft, tip, previousTop, previousTop, SpellBeamModels.shade(tint, 0.84));
                        this.sink.quad(previousTop, tip, previousRight, previousRight, tint);
                        this.sink.quad(previousBottom, tip, previousLeft, previousLeft, SpellBeamModels.shade(tint, 0.55));
                        this.sink.quad(previousRight, tip, previousBottom, previousBottom, SpellBeamModels.shade(tint, 0.65));
                    } else {
                        this.sink.quad(previousLeft, left, top, previousTop, SpellBeamModels.shade(tint, 0.84));
                        this.sink.quad(previousTop, top, right, previousRight, tint);
                        this.sink.quad(previousBottom, bottom, left, previousLeft, SpellBeamModels.shade(tint, 0.55));
                        this.sink.quad(previousRight, right, bottom, previousBottom, SpellBeamModels.shade(tint, 0.65));
                    }
                    previousLeft = left;
                    previousRight = right;
                    previousTop = top;
                    previousBottom = bottom;
                }
                return;
            }
            ManorStandaloneSpellModels.Point middle = root.add(tip.sub(root).scale(0.4));
            ManorStandaloneSpellModels.Point left = middle.add(across.scale(width));
            ManorStandaloneSpellModels.Point right = middle.sub(across.scale(width));
            ManorStandaloneSpellModels.Point upper = middle.add(normal);
            ManorStandaloneSpellModels.Point lower = middle.sub(normal);
            this.sink.quad(root, left, upper, upper, SpellBeamModels.shade(tint, 0.72));
            this.sink.quad(left, tip, upper, upper, tint);
            this.sink.quad(tip, right, upper, upper, SpellBeamModels.shade(tint, 0.91));
            this.sink.quad(right, root, upper, upper, SpellBeamModels.shade(tint, 0.8));
            this.sink.quad(left, root, lower, lower, SpellBeamModels.shade(tint, 0.52));
            this.sink.quad(tip, left, lower, lower, SpellBeamModels.shade(tint, 0.66));
            this.sink.quad(right, tip, lower, lower, SpellBeamModels.shade(tint, 0.6));
            this.sink.quad(root, right, lower, lower, SpellBeamModels.shade(tint, 0.48));
        }

        private void flower(ManorStandaloneSpellModels.Point center, int petals, double radius, int tint) {
            for (int petal = 0; petal < petals; ++petal) {
                double angle = (double)petal * Math.PI * 2.0 / (double)petals;
                double bloom = 0.52 + 0.48 * this.open();
                double curl = 0.12 + radius * (1.0 - bloom) * 0.7 + Math.sin(this.age * 0.3 + (double)petal * 0.6) * radius * 0.06;
                ManorStandaloneSpellModels.Point tip = center.add(SpellBeamModels.radial(radius * bloom, angle, curl));
                if (this.reduced) {
                    this.leaf(center, tip, radius * 0.31, tint);
                } else {
                    this.petal(center, radius, angle, bloom, curl, tint);
                }
                if (this.reduced) continue;
                this.rod(center.add(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.025)), tip.scale(0.8).add(center.scale(0.2)).add(new ManorStandaloneSpellModels.Point(0.0, 0.0, 0.025)), 0.01, 13294823);
            }
        }

        private void box(ManorStandaloneSpellModels.Point center, double width, double height, double depth, int tint) {
            if (!this.reduced) {
                double bevel = Math.min(width, height) * 0.18;
                ManorStandaloneSpellModels.Point[] outline = new ManorStandaloneSpellModels.Point[]{new ManorStandaloneSpellModels.Point(-width / 2.0 + bevel, -height / 2.0, 0.0), new ManorStandaloneSpellModels.Point(width / 2.0 - bevel, -height / 2.0, 0.0), new ManorStandaloneSpellModels.Point(width / 2.0, -height / 2.0 + bevel, 0.0), new ManorStandaloneSpellModels.Point(width / 2.0, height / 2.0 - bevel, 0.0), new ManorStandaloneSpellModels.Point(width / 2.0 - bevel, height / 2.0, 0.0), new ManorStandaloneSpellModels.Point(-width / 2.0 + bevel, height / 2.0, 0.0), new ManorStandaloneSpellModels.Point(-width / 2.0, height / 2.0 - bevel, 0.0), new ManorStandaloneSpellModels.Point(-width / 2.0, -height / 2.0 + bevel, 0.0)};
                ManorStandaloneSpellModels.Point front = center.add(new ManorStandaloneSpellModels.Point(0.0, 0.0, depth / 2.0));
                ManorStandaloneSpellModels.Point rear = center.sub(new ManorStandaloneSpellModels.Point(0.0, 0.0, depth / 2.0));
                for (int edge = 0; edge < 8; ++edge) {
                    ManorStandaloneSpellModels.Point first = outline[edge];
                    ManorStandaloneSpellModels.Point second = outline[(edge + 1) % 8];
                    this.sink.quad(rear.add(first), rear.add(second), front.add(second), front.add(first), SpellBeamModels.shade(tint, 0.65 + (double)edge * 0.04));
                }
                for (int section = 1; section <= 5; section += 2) {
                    this.sink.quad(front.add(outline[0]), front.add(outline[section]), front.add(outline[section + 1]), front.add(outline[section + 2]), tint);
                    this.sink.quad(rear.add(outline[0]), rear.add(outline[section + 2]), rear.add(outline[section + 1]), rear.add(outline[section]), SpellBeamModels.shade(tint, 0.6));
                }
                return;
            }
            ManorStandaloneSpellModels.Point[] corners = new ManorStandaloneSpellModels.Point[8];
            for (int corner = 0; corner < 8; ++corner) {
                corners[corner] = center.add(new ManorStandaloneSpellModels.Point((double)((corner & 1) == 0 ? -1 : 1) * width / 2.0, (double)((corner & 2) == 0 ? -1 : 1) * height / 2.0, (double)((corner & 4) == 0 ? -1 : 1) * depth / 2.0));
            }
            int[][] faces = new int[][]{{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}};
            for (int face = 0; face < faces.length; ++face) {
                int[] indices = faces[face];
                this.sink.quad(corners[indices[0]], corners[indices[1]], corners[indices[2]], corners[indices[3]], SpellBeamModels.shade(tint, 0.63 + (double)face * 0.065));
            }
        }

        private void petal(ManorStandaloneSpellModels.Point center, double radius, double angle, double bloom, double curl, int tint) {
            ManorStandaloneSpellModels.Point sideways = SpellBeamModels.radial(1.0, angle + 1.5707963267948966, 0.0);
            ManorStandaloneSpellModels.Point[] previous = null;
            for (int segment = 0; segment <= 5; ++segment) {
                double along = (double)segment / 5.0;
                double profile = Math.sin(along * Math.PI * 0.9);
                double width = radius * (0.025 + 0.31 * profile);
                double arch = radius * 0.2 * Math.sin(along * Math.PI);
                ManorStandaloneSpellModels.Point middle = center.add(SpellBeamModels.radial(radius * bloom * along, angle, curl * along + arch));
                double thickness = radius * (0.012 + 0.055 * profile);
                ManorStandaloneSpellModels.Point[] section = new ManorStandaloneSpellModels.Point[]{middle.add(sideways.scale(width)), middle.add(new ManorStandaloneSpellModels.Point(0.0, 0.0, thickness)), middle.sub(sideways.scale(width)), middle.sub(new ManorStandaloneSpellModels.Point(0.0, 0.0, thickness * 0.5))};
                if (previous == null) {
                    this.sink.quad(section[0], section[3], section[2], section[1], SpellBeamModels.shade(tint, 0.62));
                } else {
                    for (int side = 0; side < 4; ++side) {
                        this.sink.quad((ManorStandaloneSpellModels.Point)previous[side], section[side], section[(side + 1) % 4], previous[(side + 1) % 4], SpellBeamModels.shade(tint, side < 2 ? 1.0 - (double)side * 0.12 : 0.6));
                    }
                }
                if (segment == 5) {
                    this.sink.quad(section[0], section[1], section[2], section[3], tint);
                }
                previous = section;
            }
        }
    }
}

