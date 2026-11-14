package net.farmingtales.ftboss.util;

import java.util.EnumSet;
import java.util.Set;
import net.farmingtales.ftboss.util.ManorStandaloneSpellModels.Point;
import net.farmingtales.ftboss.util.ManorStandaloneSpellModels.Sink;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.util.SpellVisualRules.Spell;

public final class SpellBeamModels {
    public static final int MAX_MODELS_PER_FRAME = 24;
    public static final int MAX_QUADS = 2200;
    public static final int CHARGED_ORB_REFERENCE_QUADS = RemainingSpellModels.CHARGED_ORB_REFERENCE_QUADS;
    private static final int METAL = 0x34445D;
    private static final int SILVER = 0xCADCE7;
    private static final int GOLD = 0xDAB273;
    private static final Set<Spell> ACCENTS = EnumSet.of(Spell.REPULSOR_DISCHARGE, Spell.EMERGENCY_REBOOT, Spell.LUMINOUS_REBUKE);
    private static final Set<Spell> MODELED = EnumSet.of(Spell.CAPACITOR_OVERLOAD, Spell.STATIC_LATTICE,
            Spell.MAGNETIC_STORM, Spell.PRESSURE_LANCE, Spell.ION_RAIL, Spell.STAR_CROSS,
            Spell.FROSTGLASS_LANCE, Spell.FROST_NEEDLES, Spell.CAMELLIA_BLOOD_BLOOM, Spell.SANGUINE_SUTURE,
            Spell.FLESH_BLOOM, Spell.CANDLE_ECLIPSE, Spell.GILDED_VERDICT, Spell.ARCANE_FUSILLADE,
            Spell.EFFIGY_VOLLEY, Spell.FEATHER_FAN, Spell.CAMELLIA_SANCTUARY, Spell.CAMELLIA_REBIRTH,
            Spell.VIRULENT_HARVEST, Spell.GOLDEN_HARVEST, Spell.PRISM_BATTERY, Spell.EFFIGY_PROCESSION,
            Spell.NIGHTFALL_BLADES, Spell.SERAPH_DESCENT, Spell.TIDAL_EDICT, Spell.CINDER_BREATH, Spell.CANDLE_REAP);

    private SpellBeamModels() {}
    public static Set<Spell> modeled() { return Set.copyOf(MODELED); }
    public static Set<Spell> modeledAccents() { return Set.copyOf(ACCENTS); }
    public static boolean rendersAccent(Spell spell, Phase phase) {
        return ACCENTS.contains(spell) && IronSpellLightRules.accent(spell, phase) != IronSpellLightRules.Accent.NONE;
    }
    public static boolean renders(Spell spell, Phase phase) { return phase == Phase.BEAM && MODELED.contains(spell); }
    public static boolean replaces(Spell spell, Phase phase) {
        return rendersAccent(spell, phase) || renders(spell, phase) || phase == Phase.BEAM && (spell == Spell.RENDING_CLEAVE
                || spell == Spell.SEARING_PAN || spell == Spell.FOUR_TIDES || spell == Spell.ROSE_STARFALL);
    }

    public static int drawAccent(Spell spell, Phase phase, double age, Sink output) {
        if (!rendersAccent(spell, phase) || output == null || !Double.isFinite(age) || age < 0 || age >= spell.lifetime(phase)) return 0;
        int[] count = {0};
        Sink counted = (first, second, third, fourth, color) -> {
            if (++count[0] > MAX_QUADS) throw new IllegalStateException("Accent model exceeded budget: " + spell.id);
            output.quad(first, second, third, fourth, color);
        };
        double opening = ease(age / 5) * (1 - 0.7 * ease((age - spell.lifetime(phase) + 6) / 6));
        Mesh mesh = new Mesh(transform(new Point(0, 1.05, 0), 1, age * 0.015, counted), spell.color, age, false);
        switch (spell) {
            case REPULSOR_DISCHARGE -> {
                mesh.gem(new Point(0, 0, 0), 0.24, 0.36, mesh.color);
                for (int axis = 0; axis < 3; axis++) mesh.pitch(axis * Math.PI / 3 + age * (axis == 1 ? -0.085 : 0.06))
                        .ring(0.56 + axis * 0.13, 0.065, 0, 16, axis == 1 ? GOLD : SILVER);
                for (int arm = 0; arm < 4; arm++) {
                    double angle = arm * Math.PI / 2;
                    double reach = 0.8 + opening * 0.35;
                    mesh.leaf(radial(0.53, angle, 0), radial(reach, angle, 0.13), 0.17, METAL);
                    mesh.gem(radial(reach, angle, 0.13), 0.09, 0.14, mesh.color);
                    mesh.rod(radial(0.6, angle, 0.06), radial(reach - 0.08, angle, 0.17), 0.02, GOLD);
                }
            }
            case EMERGENCY_REBOOT -> {
                mesh.box(new Point(0, 0, 0), 0.62, 0.62, 0.58, METAL);
                mesh.box(new Point(0, 0, 0.31), 0.12, 0.44, 0.05, mesh.color);
                mesh.box(new Point(0, 0, 0.31), 0.44, 0.12, 0.05, mesh.color);
                for (int side : new int[]{-1, 1}) for (int vent = 0; vent < 3; vent++) {
                    mesh.box(new Point(side * 0.24, -0.12 + vent * 0.12, 0.3), 0.065, 0.035, 0.035, SILVER);
                }
                for (int arm = 0; arm < 4; arm++) {
                    double angle = arm * Math.PI / 2;
                    double flex = Math.sin(age * 0.28 + arm * Math.PI / 2) * 0.12;
                    Point joint = radial(0.48 + opening * 0.2, angle + 0.3, 0.1);
                    Point tip = radial(0.4 + opening * 0.18, angle + 0.55 + flex, 0.3 + opening * 0.18);
                    mesh.rod(radial(0.32, angle, 0), joint, 0.05, GOLD);
                    mesh.rod(joint, tip, 0.04, SILVER);
                    mesh.gem(joint, 0.065, 0.075, METAL);
                    mesh.gem(tip, 0.085, 0.11, mesh.color);
                }
                mesh.pitch(Math.PI / 2 + Math.sin(age * 0.14) * 0.18).ring(0.81, 0.045, -0.17, 16, GOLD);
            }
            case LUMINOUS_REBUKE -> {
                Mesh crown = mesh.pitch(-Math.PI / 2);
                crown.taper(-0.46, 0.48, 0.2, 0.26, GOLD);
                crown.ring(0.5, 0.06, -0.45, 16, SILVER);
                crown.gem(new Point(0, 0, 0.35), 0.22, 0.3, mesh.color);
                for (int point = 0; point < 6; point++) {
                    double angle = point * Math.PI / 3;
                    crown.leaf(radial(0.31, angle, 0.1), radial(0.32 + opening * 0.2, angle, 0.65 + opening * 0.2), 0.12, SILVER);
                    crown.gem(radial(0.39, angle, -0.16), 0.07, 0.13, mesh.color);
                }
                mesh.ring(0.88, 0.045, 0, 20, GOLD);
            }
            default -> throw new IllegalStateException("Missing accent model: " + spell.id);
        }
        return count[0];
    }

    public static int draw(Spell spell, Phase phase, double age, double length, boolean reduced, Sink output) {
        if (!renders(spell, phase) || output == null || !Double.isFinite(age) || age < 0
                || age >= spell.lifetime(phase) || !Double.isFinite(length) || length < 0.015
                || length > (spell == Spell.STAR_CROSS ? 128 : 64)) return 0;
        int[] count = {0};
        Sink bounded = (first, second, third, fourth, color) -> {
            if (++count[0] > MAX_QUADS) throw new IllegalStateException("Beam model exceeded budget: " + spell.id);
            output.quad(first, second, third, fourth, color);
        };
        if (spell == Spell.ARCANE_FUSILLADE) {
            doraFocusLaser(new Mesh(bounded, spell.color), age, length, reduced);
            return count[0];
        }
        boolean tether = switch (spell) {
            case CAPACITOR_OVERLOAD, STATIC_LATTICE, MAGNETIC_STORM, CAMELLIA_BLOOD_BLOOM,
                    SANGUINE_SUTURE, FLESH_BLOOM, CANDLE_ECLIPSE, CAMELLIA_SANCTUARY, CAMELLIA_REBIRTH,
                    VIRULENT_HARVEST, GOLDEN_HARVEST -> true;
            default -> false;
        };
        double progress = ease((age + 0.5) / 6);
        double scale = Math.min(1, length / 3.2);
        double center = tether ? length * 0.5 : 1.5 * scale + Math.max(0, length - 3 * scale) * progress;
        double roll = switch (spell) {
            case STAR_CROSS, PRISM_BATTERY -> age * 0.12;
            case GILDED_VERDICT, SERAPH_DESCENT, TIDAL_EDICT, EFFIGY_VOLLEY, EFFIGY_PROCESSION,
                    GOLDEN_HARVEST, CAMELLIA_REBIRTH, FLESH_BLOOM -> Math.sin(age * 0.25) * 0.035;
            default -> age * 0.025;
        };
        Mesh mesh = new Mesh(transform(new Point(0, 0, center), scale, roll, bounded), spell.color, age, reduced);
        switch (spell) {
            case CAPACITOR_OVERLOAD -> capacitor(mesh, age);
            case STATIC_LATTICE -> lattice(mesh, age);
            case MAGNETIC_STORM -> magnet(mesh, age);
            case PRESSURE_LANCE -> pressure(mesh, age);
            case ION_RAIL -> rail(mesh, age);
            case STAR_CROSS -> star(mesh, age);
            case FROSTGLASS_LANCE -> frostSpear(mesh);
            case FROST_NEEDLES -> needles(mesh);
            case CAMELLIA_BLOOD_BLOOM -> bloodFlower(mesh, age);
            case SANGUINE_SUTURE -> suture(mesh, age);
            case FLESH_BLOOM -> heart(mesh, age);
            case CANDLE_ECLIPSE -> eclipse(mesh, age);
            case GILDED_VERDICT -> verdict(mesh);
            case EFFIGY_VOLLEY -> puppet(mesh, new Point(0, 0, 0), 1, false);
            case FEATHER_FAN -> feather(mesh, 0, 0, 1);
            case CAMELLIA_SANCTUARY -> sanctuary(mesh, age);
            case CAMELLIA_REBIRTH -> rebirth(mesh, age);
            case VIRULENT_HARVEST -> sickle(mesh.at(new Point(0, 0, 0), 1, Math.sin(age * 0.3) * 0.25), false);
            case GOLDEN_HARVEST -> branch(mesh, age);
            case PRISM_BATTERY -> prism(mesh, age);
            case EFFIGY_PROCESSION -> procession(mesh);
            case NIGHTFALL_BLADES -> nightBlades(mesh);
            case SERAPH_DESCENT -> seraph(mesh);
            case TIDAL_EDICT -> tide(mesh);
            case CINDER_BREATH -> cinder(mesh, age);
            case CANDLE_REAP -> candleSickle(mesh);
            default -> throw new IllegalStateException("Missing beam mesh: " + spell.id);
        }
        int knots = reduced ? 3 : tether ? 7 : 5;
        Mesh path = new Mesh(bounded, spell.color);
        for (int knot = 1; knot <= knots; knot++) {
            double depth = length * knot / (knots + 1);
            if (!tether && depth > center - 1.5 * scale) continue;
            double radius = (tether ? 0.025 : 0.015) * (0.7 + 0.3 * Math.sin(age * 0.3 + knot));
            path.gem(new Point(0, 0, depth), radius * scale, Math.min(0.1, length / 30), spell.color);
        }
        return count[0];
    }

    private static void capacitor(Mesh mesh, double age) {
        mesh.gem(new Point(0, 0, 0), 0.25, 0.65, mesh.color);
        for (int prong = 0; prong < 4; prong++) {
            double angle = prong * Math.PI / 2 + age * 0.025;
            Point rear = radial(0.28, angle, -0.62), outer = radial(0.3 + mesh.open() * 0.23, angle, 0.08);
            Point tip = radial(0.15 + mesh.open() * 0.12, angle, 0.6);
            mesh.rod(rear, outer, 0.055, METAL); mesh.rod(outer, tip, 0.04, GOLD);
            mesh.gem(outer, 0.095, 0.16, SILVER);
        }
        mesh.ring(0.3, 0.04, -0.4, 12, GOLD);
        mesh.at(new Point(0, 0, 0), 1, -age * 0.12).ornament(0.31, -0.4, 4, SILVER);
    }

    private static void lattice(Mesh mesh, double age) {
        for (int gate = -1; gate <= 1; gate++) {
            double depth = gate * 0.65;
            for (int edge = 0; edge < 4; edge++) {
                double angle = edge * Math.PI / 2 + Math.PI / 4 + Math.sin(age * 0.22 + gate) * 0.22;
                Point first = radial(0.5, angle, depth), second = radial(0.5, angle + Math.PI / 2, depth);
                mesh.rod(first, second, 0.035, METAL);
                mesh.gem(first, 0.07, 0.1, mesh.color);
                if (!mesh.reduced) mesh.rod(first.scale(0.75).add(new Point(0, 0, depth * 0.25)),
                        second.scale(0.75).add(new Point(0, 0, depth * 0.25)), 0.014, GOLD);
            }
        }
        mesh.gem(new Point(0, 0, Math.sin(age * 0.18) * 0.4), 0.16, 0.24, mesh.color);
    }

    private static void magnet(Mesh mesh, double age) {
        for (int side : new int[]{-1, 1}) {
            double offset = side * (0.18 + 0.12 * mesh.open());
            mesh.rod(new Point(offset, -0.33, -0.4), new Point(offset, -0.33, 0.4), 0.085, METAL);
            for (int pole : new int[]{-1, 1}) {
                mesh.rod(new Point(offset, -0.33, pole * 0.4), new Point(offset, 0.22, pole * 0.4), 0.085, SILVER);
                mesh.box(new Point(offset, 0.23, pole * 0.4), 0.17, 0.18, 0.18, pole > 0 ? mesh.color : 0xBF596D);
            }
        }
        mesh.gem(new Point(0, 0.34, 0), 0.13 + 0.025 * Math.sin(age * 0.4), 0.2, mesh.color);
        mesh.at(new Point(0, 0.34, 0), 1, 0).pitch(age * 0.08).ring(0.25, 0.025, 0, 12, GOLD);
    }

    private static void pressure(Mesh mesh, double age) {
        mesh.taper(-0.95, 0.17, 0.32, 0.17, METAL);
        for (int coil = 0; coil < 5; coil++) {
            double depth = -0.85 + coil * (0.18 + 0.05 * mesh.open());
            mesh.ring(0.26, 0.065, depth, 12, coil % 2 == 0 ? SILVER : GOLD);
            if (!mesh.reduced) mesh.at(new Point(0, 0, 0), 1, coil * 0.4 + age * 0.08).ornament(0.27, depth, 3, mesh.color);
        }
        mesh.taper(0.35, 0.18, 1.22, 0, mesh.color);
        for (int fin = 0; fin < 3; fin++) mesh.leaf(radial(0.18, fin * Math.PI * 2 / 3, -0.5),
                radial(0.24 + mesh.open() * 0.22, fin * Math.PI * 2 / 3, -0.9), 0.11, GOLD);
    }

    private static void rail(Mesh mesh, double age) {
        for (int side : new int[]{-1, 1}) {
            double spread = side * (0.17 + mesh.open() * 0.08);
            mesh.box(new Point(spread, 0, -0.12), 0.11, 0.18, 1.6, METAL);
            mesh.rod(new Point(spread, 0.105, -0.78), new Point(spread, 0.105, 0.63), 0.02, mesh.color);
            for (int tooth = 0; tooth < 4; tooth++) mesh.box(new Point(spread, 0, -0.66 + tooth * 0.36), 0.16, 0.24, 0.1, GOLD);
        }
        mesh.taper(-0.75, 0.065, 0.65, 0.065, mesh.color);
        mesh.taper(0.65, 0.11, 1.3, 0, SILVER);
        mesh.ring(0.33, 0.05, -0.88, 8, mesh.color);
    }

    private static void star(Mesh mesh, double age) {
        mesh.gem(new Point(0, 0, 0), 0.23, 0.35, SILVER);
        for (int blade = 0; blade < 4; blade++) {
            double angle = blade * Math.PI / 2;
            double reach = 0.48 + mesh.open() * 0.34;
            mesh.leaf(radial(0.12, angle, 0), radial(reach, angle, 0.15), 0.16, mesh.color);
            if (!mesh.reduced) mesh.leaf(radial(0.19, angle, 0.05), radial(reach * 0.88, angle, 0.2), 0.055, SILVER);
        }
        mesh.ring(0.35, 0.035, -0.07, 12, GOLD);
        mesh.at(new Point(0, 0, 0), 1, -age * 0.09).ring(0.48, 0.022, 0.04, 16, SILVER);
        mesh.taper(0.2, 0.11, 0.85, 0, mesh.color);
    }

    private static void frostSpear(Mesh mesh) {
        mesh.taper(-1.05, 0.08, 0.25, 0.08, SILVER);
        mesh.taper(0.2, 0.18, 1.36, 0, mesh.color);
        for (int barb = 0; barb < 4; barb++) mesh.leaf(radial(0.04, barb * Math.PI / 2, 0.25),
                radial(0.2 + mesh.open() * 0.17, barb * Math.PI / 2, -0.25), 0.1, 0x73A4C9);
        mesh.ring(0.14, 0.045, -0.64, 8, GOLD);
        mesh.ring(0.19, 0.026, -0.28, 12, 0x8DD8E6);
        mesh.ornament(0.1, -0.32, 4, 0x8DD8E6);
    }

    private static void needles(Mesh mesh) {
        for (int needle = 0; needle < 3; needle++) {
            Point center = radial(0.12 + mesh.open() * 0.12, needle * Math.PI * 2 / 3 + mesh.age * 0.06,
                    needle * 0.14 - 0.15 + Math.sin(mesh.age * 0.3 + needle) * 0.07);
            Mesh part = mesh.at(center, 1, 0);
            part.taper(-0.92, 0.055, 0.08, 0.085, 0x426887);
            part.taper(0.08, 0.085, 1.0, 0, mesh.color);
            part.ring(0.1, 0.025, -0.55, 6, SILVER);
            part.ornament(0.1, -0.25, 3, mesh.color);
        }
    }

    private static void bloodFlower(Mesh mesh, double age) {
        mesh.flower(new Point(0, 0, 0), 8, 0.48, mesh.color);
        if (!mesh.reduced) mesh.at(new Point(0, 0, 0.09), 1, Math.PI / 8).flower(new Point(0, 0, 0), 5, 0.27, 0xE58C9C);
        mesh.gem(new Point(0, 0, 0.17), 0.16, 0.26, GOLD);
        for (int thorn = 0; thorn < 4; thorn++) mesh.leaf(radial(0.18, thorn * Math.PI / 2, -0.2),
                radial(0.5, thorn * Math.PI / 2 + 0.35, -0.7), 0.085, 0x583C50);
    }

    private static void suture(Mesh mesh, double age) {
        mesh.taper(-1.08, 0.06, 0.65, 0.06, SILVER);
        mesh.taper(0.65, 0.09, 1.23, 0, 0xF6DFD7);
        mesh.ring(0.15, 0.035, -0.78, 10, 0x8C3B53);
        for (int stitch = 0; stitch < 4; stitch++) {
            double depth = -0.55 + stitch * 0.28;
            double pull = 0.1 + 0.13 * (0.5 + 0.5 * Math.sin(age * 0.36 - stitch * 0.7));
            Point knot = new Point(0, 0.1, depth + 0.08);
            mesh.rod(new Point(-pull, -0.1, depth), knot, 0.025, mesh.color);
            mesh.rod(knot, new Point(pull, -0.1, depth + 0.16), 0.025, mesh.color);
            mesh.gem(knot, 0.04, 0.045, GOLD);
        }
    }

    private static void heart(Mesh mesh, double age) {
        double pulse = 1 + Math.sin(age * 0.65) * 0.075;
        Mesh organ = mesh.at(new Point(0, 0, 0), pulse, 0);
        organ.gem(new Point(-0.14, 0.12, 0), 0.25, 0.34, 0x8D2F4B);
        organ.gem(new Point(0.14, 0.12, 0), 0.25, 0.34, mesh.color);
        organ.leaf(new Point(0, 0.15, 0), new Point(0, -0.5, 0.25), 0.29, 0xA9405E);
        for (int vessel = 0; vessel < 3; vessel++) organ.rod(new Point((vessel - 1) * 0.12, 0.2, 0),
                new Point((vessel - 1) * 0.24, 0.6, -0.3), 0.055, 0xE28C92);
        if (!mesh.reduced) for (int side : new int[]{-1, 1}) {
            Point fork = new Point(side * 0.14, 0.08, 0.3);
            organ.rod(new Point(0, -0.29, 0.25), fork, 0.019, 0xEFB1B5);
            organ.rod(fork, new Point(side * 0.3, 0.14, 0.19), 0.013, 0xC4687C);
            organ.rod(fork, new Point(side * 0.1, 0.3, 0.19), 0.013, 0xC4687C);
        }
    }

    private static void eclipse(Mesh mesh, double age) {
        mesh.gem(new Point(0, 0, 0), 0.33, 0.11, 0x31263E);
        mesh.ring(0.38, 0.055, 0, 16, GOLD);
        for (int candle = 0; candle < 6; candle++) {
            Point center = radial(0.45 + 0.12 * mesh.open(), candle * Math.PI / 3 - age * 0.06, 0);
            mesh.at(center, 1, 0).taper(-0.28, 0.065, 0.12, 0.065, SILVER);
            mesh.gem(new Point(center.x(), center.y(), 0.22), 0.07, 0.14 + Math.sin(age * 0.8 + candle) * 0.035, mesh.color);
            mesh.at(center, 1, 0).ring(0.1, 0.03, -0.25, 8, GOLD);
        }
    }

    private static void verdict(Mesh mesh) {
        sword(mesh, GOLD, 0xFFF0BC);
        Mesh halo = mesh.at(new Point(0, 0, -0.28), 1, -mesh.age * 0.12).pitch(Math.sin(mesh.age * 0.3) * 0.2);
        halo.ring(0.46, 0.055, 0, 12, GOLD);
        for (int ray = 0; ray < 4; ray++) halo.gem(radial(0.5 + mesh.open() * 0.1, ray * Math.PI / 2, 0), 0.055, 0.1, mesh.color);
    }

    private static void doraFocusLaser(Mesh mesh, double age, double length, boolean reduced) {
        double scale = Math.min(1.0, length / 1.5);
        double core = Math.max(0.0008, 0.052 * scale);
        double cage = Math.max(core * 1.8, 0.18 * scale);
        mesh.taper(0, core, length, core, 0xE8FFFF);

        int rails = reduced ? 3 : 4;
        int sections = reduced ? 3 : 6;
        double twist = age * 0.09;
        double railRadius = Math.max(0.0005, 0.018 * scale);
        double railPadding = Math.min(length * 0.2, Math.max(0.001, railRadius * 1.5));
        double railLength = Math.max(0, length - railPadding * 2);
        for (int rail = 0; rail < rails; rail++) {
            double base = rail * Math.PI * 2 / rails;
            for (int section = 0; section < sections; section++) {
                double firstDepth = railPadding + railLength * section / sections;
                double secondDepth = railPadding + railLength * (section + 1.0) / sections;
                double firstAngle = base + twist + firstDepth / Math.max(0.015, length) * Math.PI * 0.55;
                double secondAngle = base + twist + secondDepth / Math.max(0.015, length) * Math.PI * 0.55;
                mesh.rod(radial(cage, firstAngle, firstDepth), radial(cage, secondAngle, secondDepth),
                        railRadius, rail % 2 == 0 ? 0x39CFC5 : 0x9FFFF0);
            }
        }

        if (length < 0.052) return;
        int gates = reduced ? 3 : 5;
        double usable = length - 0.052;
        for (int gate = 0; gate < gates; gate++) {
            double along = (gate + 0.28 + age * 0.20) / gates;
            along -= Math.floor(along);
            mesh.ring(cage * 1.12, Math.max(0.001, cage * 0.18), 0.026 + usable * along,
                    reduced ? 8 : 12, gate % 2 == 0 ? 0x67FFE0 : 0xBFFFF7);
        }
        mesh.ring(cage * 1.35, Math.max(0.001, cage * 0.20), 0.026, reduced ? 8 : 12, 0xD9FFF7);
        mesh.ring(cage * 1.50, Math.max(0.001, cage * 0.16), length - 0.026,
                reduced ? 8 : 12, 0xFFFFFF);
    }

    private static void puppet(Mesh mesh, Point center, double scale, boolean crowned) {
        double step = Math.sin(mesh.age * 0.48 + center.x() * 4);
        Mesh doll = mesh.at(center.add(new Point(0, step * 0.025, 0)), scale, step * 0.06);
        Mesh head = doll.at(new Point(0, 0.26, 0.08), 1, -step * 0.1);
        head.gem(new Point(0, 0, 0), 0.17, 0.18, 0xF3DAB8);
        doll.box(new Point(0, -0.06, 0), 0.27, 0.32, 0.2, 0x625171);
        doll.box(new Point(0, -0.02, 0.115), 0.12, 0.18, 0.04, mesh.color);
        for (int side : new int[]{-1, 1}) {
            Point shoulder = new Point(side * 0.13, 0.02, 0);
            Point elbow = new Point(side * (0.19 + 0.07 * mesh.open()), -0.1, side * step * 0.08);
            Point hand = new Point(side * (0.23 + 0.12 * mesh.open()), -0.16 + step * side * 0.06, 0.08);
            Point knee = new Point(side * 0.12, -0.34, step * side * 0.09);
            Point foot = new Point(side * 0.17, -0.49 + step * side * 0.02, 0.05 - step * side * 0.08);
            doll.rod(shoulder, elbow, 0.05, GOLD); doll.rod(elbow, hand, 0.04, SILVER);
            doll.rod(new Point(side * 0.07, -0.2, 0), knee, 0.055, GOLD);
            doll.rod(knee, foot, 0.045, SILVER);
            doll.gem(elbow, 0.052, 0.056, METAL); doll.gem(knee, 0.054, 0.057, METAL);
            doll.box(foot, 0.1, 0.075, 0.14, 0x625171);
            head.box(new Point(side * 0.06, 0.02, 0.17), 0.045, 0.04, 0.025, 0x344157);
        }
        if (crowned) for (int spike = 0; spike < 3; spike++) head.gem(new Point((spike - 1) * 0.1, 0.24, 0), 0.05, 0.11, GOLD);
        else doll.ring(0.25, 0.035, -0.06, 12, GOLD);
    }

    private static void feather(Mesh mesh, double offset, double depth, double scale) {
        Mesh quill = mesh.at(new Point(offset, 0, depth), scale, 0);
        quill.rod(new Point(0, 0, -1), new Point(0, 0, 1.15), 0.028, GOLD);
        quill.ring(0.22, 0.028, -0.78, 16, GOLD);
        for (int barb = 0; barb < 6; barb++) {
            double along = -0.55 + barb * 0.25;
            double width = (0.12 + Math.sin((barb + 1) * Math.PI / 8) * 0.22) * (0.65 + 0.35 * mesh.open());
            for (int side : new int[]{-1, 1}) quill.leaf(new Point(0, 0, along),
                    new Point(side * width, 0.025 + Math.sin(mesh.age * 0.4 - barb * 0.6) * 0.055, along - 0.3),
                    0.07, barb % 2 == 0 ? SILVER : mesh.color);
        }
    }

    private static void sanctuary(Mesh mesh, double age) {
        mesh.flower(new Point(0, 0, 0), 6, 0.46, mesh.color);
        mesh.ring(0.53, 0.04, -0.13, 16, GOLD);
        mesh.at(new Point(0, 0, 0), 1, -age * 0.08).ornament(0.54, -0.13, 6, SILVER);
        mesh.box(new Point(0, 0, 0.24), 0.1, 0.38, 0.06, SILVER);
        mesh.box(new Point(0, 0, 0.24), 0.3, 0.09, 0.06, SILVER);
    }

    private static void rebirth(Mesh mesh, double age) {
        mesh.gem(new Point(0, -0.16, 0), 0.24, 0.36, 0xCDA373);
        mesh.rod(new Point(0, -0.18, 0), new Point(0, 0.65, 0.08), 0.045, 0x426756);
        for (int leaf = 0; leaf < 4; leaf++) mesh.leaf(new Point(0, 0.05 + leaf * 0.13, 0),
                new Point((leaf % 2 == 0 ? -1 : 1) * (0.18 + mesh.open() * 0.2), 0.24 + leaf * 0.13,
                        0.09 + Math.sin(age * 0.25 + leaf) * 0.06), 0.12, mesh.color);
        mesh.flower(new Point(0, 0.65, 0.08), 5, 0.17, 0xEBD9BA);
    }

    private static void sickle(Mesh mesh, boolean golden) {
        mesh.rod(new Point(0, -0.5, -0.68), new Point(0, 0.32, 0.4), 0.055, golden ? GOLD : METAL);
        for (int blade = 0; blade < 12; blade++) {
            double angle = blade * Math.PI * 0.86 / 12, next = (blade + 1) * Math.PI * 0.86 / 12;
            double bladeWidth = 0.14 + 0.05 * mesh.open();
            double width = bladeWidth * Math.sin((blade + 1) * Math.PI / 13);
            double nextWidth = blade == 11 ? 0 : bladeWidth * Math.sin((blade + 2) * Math.PI / 13);
            Point outer = new Point(Math.sin(angle) * 0.7, 0.34, 0.4 + (1 - Math.cos(angle)) * 0.32);
            Point nextOuter = new Point(Math.sin(next) * 0.7, 0.34, 0.4 + (1 - Math.cos(next)) * 0.32);
            Point inner = outer.add(new Point(-width, 0, -width * 0.35));
            Point nextInner = nextOuter.add(new Point(-nextWidth, 0, -nextWidth * 0.35));
            Point thickness = new Point(0, -0.045, 0);
            mesh.sink.quad(outer, nextOuter, nextInner, inner, mesh.color);
            mesh.sink.quad(inner.add(thickness), nextInner.add(thickness), nextOuter.add(thickness), outer.add(thickness), shade(mesh.color, 0.55));
            mesh.sink.quad(outer, outer.add(thickness), nextOuter.add(thickness), nextOuter, SILVER);
            mesh.sink.quad(inner, nextInner, nextInner.add(thickness), inner.add(thickness), golden ? GOLD : METAL);
        }
        mesh.gem(new Point(0, 0.28, 0.36), 0.11, 0.14, golden ? SILVER : 0x7A8E43);
        if (!mesh.reduced) for (int wrap = 0; wrap < 4; wrap++) {
            Point center = new Point(0, -0.4 + wrap * 0.1, -0.55 + wrap * 0.132);
            mesh.at(center, 1, 0).pitch(-0.65).ring(0.073, 0.025, 0, 8, GOLD);
        }
    }

    private static void branch(Mesh mesh, double age) {
        mesh.rod(new Point(0, -0.55, -0.5), new Point(0, 0.6, 0.65), 0.055, GOLD);
        for (int twig = 0; twig < 3; twig++) {
            int sign = twig % 2 == 0 ? -1 : 1;
            Point root = new Point(0, -0.3 + twig * 0.32, -0.2 + twig * 0.25);
            Point tip = root.add(new Point(sign * (0.25 + mesh.open() * 0.13), 0.13 + Math.sin(age * 0.3 + twig) * 0.05, 0.15));
            mesh.rod(root, tip, 0.032, 0x806447);
            mesh.gem(tip, 0.15, 0.2, mesh.color);
            mesh.leaf(tip, tip.add(new Point(sign * 0.15, 0.23, 0)), 0.07, 0x93AC71);
        }
    }

    private static void prism(Mesh mesh, double age) {
        mesh.gem(new Point(0, 0, 0), 0.28, 0.72, mesh.color);
        for (int strut = 0; strut < 3; strut++) {
            double angle = strut * Math.PI * 2 / 3 - age * 0.17;
            mesh.rod(radial(0.37, angle, -0.65), radial(0.37, angle + 0.2, 0.58), 0.045, METAL);
            mesh.gem(radial(0.37, angle, -0.65), 0.085, 0.12, GOLD);
        }
        mesh.ring(0.4, 0.05, -0.53, 12, GOLD);
        mesh.ring(0.35, 0.04, 0.5, 12, SILVER);
        mesh.pitch(Math.sin(age * 0.28) * 0.22).ring(0.43, 0.025, 0, 12, mesh.color);
        mesh.at(new Point(0, 0, 0), 1, age * 0.07).ornament(0.4, -0.53, 6, SILVER);
    }

    private static void procession(Mesh mesh) {
        puppet(mesh, new Point(0, 0.14, 0.3), 0.8, true);
        puppet(mesh, new Point(-0.44, -0.12, -0.3), 0.58, true);
        puppet(mesh, new Point(0.44, -0.12, -0.3), 0.58, true);
        mesh.rod(new Point(-0.45, 0.27, -0.34), new Point(0.45, 0.27, -0.34), 0.025, GOLD);
    }

    private static void nightBlades(Mesh mesh) {
        for (int side : new int[]{-1, 1}) sword(mesh.at(new Point(side * (0.17 + 0.1 * mesh.open()),
                side * 0.06, side * (0.12 + Math.sin(mesh.age * 0.35) * 0.08)), 0.84,
                side * (0.08 + 0.12 * mesh.open())), 0x352B48, mesh.color);
        mesh.gem(new Point(0, 0.25, 0), 0.09, 0.19, SILVER);
    }

    private static void seraph(Mesh mesh) {
        sword(mesh, GOLD, SILVER);
        for (int side : new int[]{-1, 1}) for (int plume = 0; plume < 4; plume++) {
            mesh.leaf(new Point(side * 0.13, 0, -0.25 + plume * 0.09),
                    new Point(side * (0.73 - plume * 0.09) * (0.6 + 0.4 * mesh.open()),
                            0.05 + Math.sin(mesh.age * 0.38 - plume * 0.3) * 0.09, -0.87 + plume * 0.23),
                    0.085, plume % 2 == 0 ? SILVER : mesh.color);
        }
        mesh.ring(0.25, 0.04, 0.44, 12, GOLD);
    }

    private static void sword(Mesh mesh, int guard, int edge) {
        mesh.leaf(new Point(0, 0, -0.35), new Point(0, 0, 1.3), 0.19, edge);
        mesh.rod(new Point(-0.33, 0, -0.35), new Point(0.33, 0, -0.35), 0.055, guard);
        mesh.taper(-0.85, 0.055, -0.37, 0.055, METAL);
        mesh.gem(new Point(0, 0, -0.94), 0.09, 0.14, guard);
        if (!mesh.reduced) {
            mesh.leaf(new Point(0, 0.053, -0.22), new Point(0, 0.027, 1.04), 0.044, mesh.color);
            mesh.gem(new Point(0, 0.064, -0.34), 0.075, 0.11, mesh.color);
            for (int wrap = 0; wrap < 3; wrap++) mesh.ring(0.07, 0.022, -0.76 + wrap * 0.13, 8, guard);
            for (int side : new int[]{-1, 1}) mesh.leaf(new Point(side * 0.12, 0, -0.35),
                    new Point(side * 0.36, 0, -0.17), 0.055, guard);
        }
    }

    private static void tide(Mesh mesh) {
        for (int crest = 0; crest < 12; crest++) {
            double depth = -1.1 + crest * 2.2 / 12, next = -1.1 + (crest + 1) * 2.2 / 12;
            double height = waveHeight(crest, mesh.age);
            double nextHeight = waveHeight(crest + 1, mesh.age);
            Point base = new Point(0.15, -0.3, depth), nextBase = new Point(0.15, -0.3, next);
            Point crestLip = new Point(0.21, height, depth), nextLip = new Point(0.21, nextHeight, next);
            Point back = new Point(-0.15, -0.3, depth), nextBack = new Point(-0.15, -0.3, next);
            Point crestBack = new Point(-0.06, height + 0.05, depth), nextCrestBack = new Point(-0.06, nextHeight + 0.05, next);
            mesh.sink.quad(base, nextBase, nextLip, crestLip, crest % 3 == 0 ? 0x437FA1 : mesh.color);
            mesh.sink.quad(crestLip, nextLip, nextCrestBack, crestBack, SILVER);
            mesh.sink.quad(crestBack, nextCrestBack, nextBack, back, 0x345F85);
            mesh.sink.quad(back, nextBack, nextBase, base, 0x29435E);
            if (!mesh.reduced && crest % 3 == 0) {
                mesh.rod(new Point(-0.12, height - 0.02, depth), new Point(-0.12, nextHeight - 0.02, next), 0.012, GOLD);
            }
            if (!mesh.reduced && crest % 2 == 0) {
                mesh.gem(crestLip.add(new Point(0.04, 0.05, 0)), 0.043, 0.07, 0xBCE6E9);
                mesh.rod(new Point(0.175, -0.06, depth), new Point(0.18, 0.12, next), 0.014, 0x8DD3DD);
            }
        }
    }

    private static void cinder(Mesh mesh, double age) {
        mesh.taper(-1.02, 0.07, -0.45, 0.16, 0xA6402C);
        mesh.taper(-0.45, 0.16, 0.45, 0.12, 0xDD853B);
        mesh.taper(0.45, 0.12, 1.28, 0, mesh.color);
        for (int flame = 0; flame < 5; flame++) {
            double depth = -0.9 + flame * 0.43;
            double height = 0.45 + Math.sin(flame * 1.3 + age * 0.3) * 0.2;
            mesh.leaf(new Point(0, -0.12, depth), new Point(0.05, height, depth + 0.22), 0.18, 0xB85536);
            mesh.leaf(new Point(0.045, -0.15, depth + 0.03), new Point(0.05, height * 0.7, depth + 0.25), 0.1, mesh.color);
        }
        mesh.ring(0.17, 0.06, -1.03, 8, GOLD);
    }

    private static void candleSickle(Mesh mesh) {
        sickle(mesh, true);
        mesh.at(new Point(0, -0.12, -0.15), 1, 0).taper(-0.2, 0.085, 0.25, 0.085, 0xE4DBC5);
        mesh.gem(new Point(0.025 * Math.sin(mesh.age * 0.5), -0.12, 0.27), 0.09,
                0.17 + Math.sin(mesh.age * 0.75) * 0.035, 0xF4CB76);
        mesh.ring(0.18, 0.04, -0.38, 10, GOLD);
    }

    private static double waveHeight(int crest, double age) {
        return 0.1 + Math.sin(crest * Math.PI / 12) * (0.5 + Math.sin(age * 0.38 - crest * 0.3) * 0.1);
    }

    private static double ease(double progress) {
        double clamped = Math.max(0, Math.min(1, progress));
        return clamped * clamped * (3 - 2 * clamped);
    }

    private static Sink transform(Point center, double scale, double roll, Sink sink) {
        return (first, second, third, fourth, color) -> sink.quad(place(first, center, scale, roll), place(second, center, scale, roll),
                place(third, center, scale, roll), place(fourth, center, scale, roll), color);
    }

    private static Point place(Point point, Point center, double scale, double roll) {
        return center.add(new Point((point.x() * Math.cos(roll) - point.y() * Math.sin(roll)) * scale,
                (point.x() * Math.sin(roll) + point.y() * Math.cos(roll)) * scale, point.z() * scale));
    }

    private static Point pitch(Point point, double angle) {
        return new Point(point.x(), point.y() * Math.cos(angle) - point.z() * Math.sin(angle),
                point.y() * Math.sin(angle) + point.z() * Math.cos(angle));
    }

    private static Point radial(double radius, double angle, double depth) {
        return new Point(Math.cos(angle) * radius, Math.sin(angle) * radius, depth);
    }

    private static int shade(int color, double amount) {
        int result = 0;
        for (int shift = 0; shift <= 16; shift += 8) result |= Math.min(255, (int) (((color >> shift) & 255) * amount)) << shift;
        return result;
    }

    private static final class Mesh {
        private final Sink sink;
        private final int color;
        private final double age;
        private final boolean reduced;
        private Mesh(Sink sink, int color) { this(sink, color, 0, true); }
        private Mesh(Sink sink, int color, double age, boolean reduced) {
            this.sink = sink; this.color = color; this.age = age; this.reduced = reduced;
        }
        private double open() { return ease(age / 3.5) * (1 - 0.55 * ease((age - 8) / 4)); }
        private Mesh at(Point center, double scale, double roll) {
            return new Mesh(transform(center, scale, roll, sink), color, age, reduced);
        }
        private Mesh pitch(double angle) {
            return new Mesh((first, second, third, fourth, tint) -> sink.quad(SpellBeamModels.pitch(first, angle),
                    SpellBeamModels.pitch(second, angle), SpellBeamModels.pitch(third, angle), SpellBeamModels.pitch(fourth, angle), tint), color, age, reduced);
        }

        private void taper(double rear, double rearRadius, double front, double frontRadius, int tint) {
            int facets = reduced ? 8 : 12;
            for (int facet = 0; facet < facets; facet++) {
                double angle = facet * Math.PI * 2 / facets, next = (facet + 1) * Math.PI * 2 / facets;
                sink.quad(radial(rearRadius, angle, rear), radial(rearRadius, next, rear),
                        radial(frontRadius, next, front), radial(frontRadius, angle, front), shade(tint, 0.56 + facet * 0.44 / Math.max(1, facets - 1)));
            }
        }

        private void gem(Point center, double radius, double halfLength, int tint) {
            Mesh moved = at(center, 1, 0);
            if (reduced || radius < 0.06) {
                moved.taper(-halfLength, 0, 0, radius, tint);
                moved.taper(0, radius, halfLength, 0, tint);
            } else {
                moved.taper(-halfLength, 0, -halfLength * 0.18, radius, shade(tint, 0.8));
                moved.taper(-halfLength * 0.18, radius, halfLength * 0.28, radius * 0.88, tint);
                moved.taper(halfLength * 0.28, radius * 0.88, halfLength, 0, tint);
            }
        }

        private void ornament(double radius, double depth, int count, int tint) {
            if (reduced) return;
            for (int mark = 0; mark < count; mark++) {
                double angle = mark * Math.PI * 2 / count;
                leaf(radial(radius - 0.025, angle - 0.08, depth), radial(radius + 0.025, angle + 0.08, depth), 0.022, tint);
            }
        }

        private void ring(double radius, double width, double depth, int segments, int tint) {
            segments = reduced ? Math.max(8, segments) : Math.max(20, segments);
            for (int segment = 0; segment < segments; segment++) {
                double angle = segment * Math.PI * 2 / segments, next = (segment + 1) * Math.PI * 2 / segments;
                Point outerRear = radial(radius, angle, depth - 0.025), nextRear = radial(radius, next, depth - 0.025);
                Point outerFront = radial(radius, angle, depth + 0.025), nextFront = radial(radius, next, depth + 0.025);
                Point innerRear = radial(radius - width, angle, depth - 0.025), nextInnerRear = radial(radius - width, next, depth - 0.025);
                Point innerFront = radial(radius - width, angle, depth + 0.025), nextInnerFront = radial(radius - width, next, depth + 0.025);
                sink.quad(outerRear, nextRear, nextFront, outerFront, shade(tint, 0.68));
                sink.quad(innerFront, nextInnerFront, nextInnerRear, innerRear, shade(tint, 0.56));
                sink.quad(outerFront, nextFront, nextInnerFront, innerFront, tint);
                sink.quad(innerRear, nextInnerRear, nextRear, outerRear, shade(tint, 0.8));
            }
        }

        private void rod(Point start, Point end, double radius, int tint) {
            Point direction = end.sub(start).normal();
            Point across = direction.cross(Math.abs(direction.y()) > 0.9 ? new Point(1, 0, 0) : new Point(0, 1, 0)).normal();
            Point upward = direction.cross(across).normal();
            int facets = reduced ? 6 : 10;
            for (int facet = 0; facet < facets; facet++) {
                double angle = facet * Math.PI * 2 / facets, next = (facet + 1) * Math.PI * 2 / facets;
                Point first = across.scale(Math.cos(angle) * radius).add(upward.scale(Math.sin(angle) * radius));
                Point second = across.scale(Math.cos(next) * radius).add(upward.scale(Math.sin(next) * radius));
                sink.quad(start.add(first), start.add(second), end.add(second), end.add(first), shade(tint, 0.65 + facet * 0.33 / Math.max(1, facets - 1)));
            }
        }

        private void leaf(Point root, Point tip, double width, int tint) {
            Point direction = tip.sub(root).normal();
            Point across = direction.cross(Math.abs(direction.z()) < 0.45 ? new Point(0, 0, 1) : new Point(0, 1, 0)).normal();
            Point normal = direction.cross(across).normal().scale(width * 0.26);
            if (!reduced && width >= 0.065) {
                Point previousLeft = root, previousRight = root, previousTop = root, previousBottom = root;
                for (int section = 1; section <= 4; section++) {
                    double progress = section / 4.0;
                    double profile = section == 4 ? 0 : Math.sin(progress * Math.PI) * (1.1 - progress * 0.3);
                    Point center = root.add(tip.sub(root).scale(progress));
                    Point left = center.add(across.scale(width * profile));
                    Point right = center.sub(across.scale(width * profile));
                    Point top = center.add(normal.scale(profile)), bottom = center.sub(normal.scale(profile * 0.65));
                    if (section == 1) {
                        sink.quad(root, left, top, top, shade(tint, 0.84));
                        sink.quad(root, top, right, right, tint);
                        sink.quad(root, bottom, left, left, shade(tint, 0.55));
                        sink.quad(root, right, bottom, bottom, shade(tint, 0.65));
                    } else if (section == 4) {
                        sink.quad(previousLeft, tip, previousTop, previousTop, shade(tint, 0.84));
                        sink.quad(previousTop, tip, previousRight, previousRight, tint);
                        sink.quad(previousBottom, tip, previousLeft, previousLeft, shade(tint, 0.55));
                        sink.quad(previousRight, tip, previousBottom, previousBottom, shade(tint, 0.65));
                    } else {
                        sink.quad(previousLeft, left, top, previousTop, shade(tint, 0.84));
                        sink.quad(previousTop, top, right, previousRight, tint);
                        sink.quad(previousBottom, bottom, left, previousLeft, shade(tint, 0.55));
                        sink.quad(previousRight, right, bottom, previousBottom, shade(tint, 0.65));
                    }
                    previousLeft = left; previousRight = right; previousTop = top; previousBottom = bottom;
                }
                return;
            }
            Point middle = root.add(tip.sub(root).scale(0.4));
            Point left = middle.add(across.scale(width)), right = middle.sub(across.scale(width));
            Point upper = middle.add(normal), lower = middle.sub(normal);
            sink.quad(root, left, upper, upper, shade(tint, 0.72));
            sink.quad(left, tip, upper, upper, tint);
            sink.quad(tip, right, upper, upper, shade(tint, 0.91));
            sink.quad(right, root, upper, upper, shade(tint, 0.8));
            sink.quad(left, root, lower, lower, shade(tint, 0.52));
            sink.quad(tip, left, lower, lower, shade(tint, 0.66));
            sink.quad(right, tip, lower, lower, shade(tint, 0.6));
            sink.quad(root, right, lower, lower, shade(tint, 0.48));
        }

        private void flower(Point center, int petals, double radius, int tint) {
            for (int petal = 0; petal < petals; petal++) {
                double angle = petal * Math.PI * 2 / petals;
                double bloom = 0.52 + 0.48 * open();
                double curl = 0.12 + radius * (1 - bloom) * 0.7 + Math.sin(age * 0.3 + petal * 0.6) * radius * 0.06;
                Point tip = center.add(radial(radius * bloom, angle, curl));
                if (reduced) leaf(center, tip, radius * 0.31, tint);
                else petal(center, radius, angle, bloom, curl, tint);
                if (!reduced) rod(center.add(new Point(0, 0, 0.025)), tip.scale(0.8).add(center.scale(0.2))
                        .add(new Point(0, 0, 0.025)), 0.01, SILVER);
            }
        }

        private void box(Point center, double width, double height, double depth, int tint) {
            if (!reduced) {
                double bevel = Math.min(width, height) * 0.18;
                Point[] outline = {new Point(-width / 2 + bevel, -height / 2, 0), new Point(width / 2 - bevel, -height / 2, 0),
                        new Point(width / 2, -height / 2 + bevel, 0), new Point(width / 2, height / 2 - bevel, 0),
                        new Point(width / 2 - bevel, height / 2, 0), new Point(-width / 2 + bevel, height / 2, 0),
                        new Point(-width / 2, height / 2 - bevel, 0), new Point(-width / 2, -height / 2 + bevel, 0)};
                Point front = center.add(new Point(0, 0, depth / 2)), rear = center.sub(new Point(0, 0, depth / 2));
                for (int edge = 0; edge < 8; edge++) {
                    Point first = outline[edge], second = outline[(edge + 1) % 8];
                    sink.quad(rear.add(first), rear.add(second), front.add(second), front.add(first), shade(tint, 0.65 + edge * 0.04));
                }
                for (int section = 1; section <= 5; section += 2) {
                    sink.quad(front.add(outline[0]), front.add(outline[section]), front.add(outline[section + 1]),
                            front.add(outline[section + 2]), tint);
                    sink.quad(rear.add(outline[0]), rear.add(outline[section + 2]), rear.add(outline[section + 1]),
                            rear.add(outline[section]), shade(tint, 0.6));
                }
                return;
            }
            Point[] corners = new Point[8];
            for (int corner = 0; corner < 8; corner++) corners[corner] = center.add(new Point(
                    ((corner & 1) == 0 ? -1 : 1) * width / 2, ((corner & 2) == 0 ? -1 : 1) * height / 2,
                    ((corner & 4) == 0 ? -1 : 1) * depth / 2));
            int[][] faces = {{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}};
            for (int face = 0; face < faces.length; face++) {
                int[] indices = faces[face];
                sink.quad(corners[indices[0]], corners[indices[1]], corners[indices[2]], corners[indices[3]], shade(tint, 0.63 + face * 0.065));
            }
        }

        private void petal(Point center, double radius, double angle, double bloom, double curl, int tint) {
            Point sideways = radial(1, angle + Math.PI / 2, 0);
            Point[] previous = null;
            for (int segment = 0; segment <= 5; segment++) {
                double along = segment / 5.0;
                double profile = Math.sin(along * Math.PI * 0.9);
                double width = radius * (0.025 + 0.31 * profile);
                double arch = radius * 0.2 * Math.sin(along * Math.PI);
                Point middle = center.add(radial(radius * bloom * along, angle, curl * along + arch));
                double thickness = radius * (0.012 + 0.055 * profile);
                Point[] section = {middle.add(sideways.scale(width)), middle.add(new Point(0, 0, thickness)),
                        middle.sub(sideways.scale(width)), middle.sub(new Point(0, 0, thickness * 0.5))};
                if (previous == null) sink.quad(section[0], section[3], section[2], section[1], shade(tint, 0.62));
                else for (int side = 0; side < 4; side++) sink.quad(previous[side], section[side],
                        section[(side + 1) % 4], previous[(side + 1) % 4], shade(tint, side < 2 ? 1 - side * 0.12 : 0.6));
                if (segment == 5) sink.quad(section[0], section[1], section[2], section[3], tint);
                previous = section;
            }
        }
    }
}
