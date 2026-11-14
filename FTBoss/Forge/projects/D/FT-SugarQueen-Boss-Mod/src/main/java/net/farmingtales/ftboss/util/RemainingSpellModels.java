package net.farmingtales.ftboss.util;

import java.util.EnumSet;
import java.util.Set;
import net.farmingtales.ftboss.util.ManorStandaloneSpellModels.Point;
import net.farmingtales.ftboss.util.ManorStandaloneSpellModels.Sink;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.util.SpellVisualRules.Spell;

public final class RemainingSpellModels {
    public static final int MAX_QUADS = 2600;
    /** The smallest linked Defect charge orb uses 43 authored cubes (258 exposed cube faces). */
    public static final int CHARGED_ORB_REFERENCE_QUADS = 43 * 6;
    private static final int IRON = 0x354050;
    private static final int GOLD = 0xC59B58;
    private static final int SILVER = 0xB8D9DF;
    private static final int WOOD = 0x653E30;
    private static final int LEAF = 0x447F53;
    private static final Set<Spell> MODELED = EnumSet.of(Spell.VINTAGE_VORTEX, Spell.PRISMATIC_BLIGHT,
            Spell.CASK_RUPTURE, Spell.ABYSSAL_GEYSER, Spell.BREACH_SHELL, Spell.HEARTH_FEAST,
            Spell.ELIXIR_RAIN, Spell.WHITEOUT_EDICT, Spell.SUGAR_HAIL, Spell.GLACIAL_PRISM,
            Spell.ROYAL_SHATTER, Spell.BRIAR_PROCESSION, Spell.SCARLET_POLLEN, Spell.INTOXICATING_TOAST,
            Spell.VINTAGE_MINEFIELD, Spell.SPORE_ERUPTION, Spell.ROTTING_REQUIEM, Spell.LIVING_MULCH,
            Spell.SOLAR_ORCHARD, Spell.JUDGMENT_SEED, Spell.UNDERTOW_SNARE, Spell.ABYSSAL_CARAPACE,
            Spell.RIPTIDE_CORRIDOR, Spell.TIDAL_LOTUS);

    private RemainingSpellModels() {}
    public static Set<Spell> modeled() { return Set.copyOf(MODELED); }
    public static boolean supports(Spell spell) { return MODELED.contains(spell); }
    public static boolean renders(Spell spell, Phase phase) { return supports(spell) && phase != null && phase != Phase.BEAM; }

    public static int draw(Spell spell, Phase phase, double age, boolean reduced, Sink output) {
        if (!renders(spell, phase) || output == null || !Double.isFinite(age) || age < 0 || age >= spell.lifetime(phase)) return 0;
        double opening = ease(age / (phase == Phase.CHARGE ? 7 : 5))
                * (1 - 0.65 * ease((age - spell.lifetime(phase) + 6) / 6));
        Mesh mesh = new Mesh(output, age, opening, reduced);
        double turn = age * 0.035;
        int detail = reduced ? 4 : 6;
        switch (spell) {
            case VINTAGE_VORTEX -> {
                mesh.barrel(new Point(0, 1.3, 0), 0.65, 1.4);
                mesh.helix(2.1, 2.8, turn, 0xBE7352);
                for (int index = 0; index < 3; index++) mesh.gem(polar(1.85, turn + index * Math.PI * 2 / 3, 1.4), 0.18, 0.4, 0x8A477E);
            }
            case PRISMATIC_BLIGHT -> {
                mesh.mushroom(new Point(0, 0, 0), 1.25, 0x6FA25A);
                for (int index = 0; index < detail; index++) {
                    Point center = polar(2.1, index * Math.PI * 2 / detail, 0.15);
                    mesh.mushroom(center, 0.45 + index % 2 * 0.15, index % 2 == 0 ? 0xBB804E : 0x8269A1);
                    mesh.gem(center.add(new Point(0, 1.8 + Math.sin(turn + index) * 0.2, 0)), 0.12, 0.22, 0xB8DE65);
                }
            }
            case CASK_RUPTURE -> {
                for (int index = 0; index < 8; index++) {
                    double angle = index * Math.PI / 4;
                    Point base = polar(0.8 + Math.min(age, 12) * 0.025, angle, 0.4);
                    mesh.rod(base, base.add(polar(0.4, angle, 1.3)), 0.17, WOOD);
                }
                mesh.ring(new Point(0, 0.6, 0), 1.05, 0.09, IRON);
                mesh.ring(new Point(0, 1.55, 0), 1.37, 0.07, GOLD);
                mesh.flower(new Point(0, 0.6, 0), 1.2, 7, 0xE8953E, 0xFFCE76, turn);
            }
            case ABYSSAL_GEYSER -> {
                mesh.ring(new Point(0, 0.18, 0), 1.65, 0.13, IRON);
                for (int index = 0; index < detail; index++) {
                    double angle = index * Math.PI * 2 / detail;
                    Point base = polar(0.6, angle, 0.2), apex = polar(1.0, angle, 3.2 + Math.sin(turn + index) * 0.4);
                    mesh.rod(base, apex, 0.16, index % 2 == 0 ? 0x428CB3 : 0x75C7D4);
                    mesh.gem(apex, 0.18, 0.4, 0xB2E4E5);
                }
                mesh.gem(new Point(0, 1.8, 0), 0.4, 1.6, 0x3864A0);
            }
            case BREACH_SHELL -> {
                mesh.prism(new Point(0, 0.2, 0), 0.58, 0.62, 1.5, 8, IRON);
                mesh.gem(new Point(0, 2.0, 0), 0.58, 0.75, 0xD58244);
                mesh.ring(new Point(0, 0.45, 0), 0.65, 0.1, GOLD);
                for (int index = 0; index < 4; index++) {
                    double angle = index * Math.PI / 2;
                    mesh.leaf(polar(0.5, angle, 0.3), polar(1.15, angle, 0.12), 0.27, IRON);
                }
            }
            case HEARTH_FEAST -> {
                mesh.prism(new Point(0, 0.12, 0), 1.45, 1.25, 0.18, 12, IRON);
                mesh.ring(new Point(0, 0.34, 0), 1.43, 0.08, GOLD);
                mesh.box(new Point(0, 0.78, 0), 1.3, 0.5, 0.72, 0xBE884F);
                for (int index = 0; index < 5; index++) {
                    mesh.rod(new Point(-0.48 + index * 0.24, 1.02, -0.35), new Point(-0.48 + index * 0.24, 1.08, 0.35), 0.025, 0xF1D19A);
                }
                for (int index = 0; index < 3; index++) mesh.gem(polar(0.92, index * Math.PI * 2 / 3, 0.55), 0.24, 0.25, LEAF);
                mesh.ring(new Point(0, 1.8, 0), 0.72, 0.04, 0xE9C078);
            }
            case ELIXIR_RAIN -> {
                mesh.ring(new Point(0, 2.85, 0), 1.8, 0.07, GOLD);
                for (int index = 0; index < detail; index++) {
                    double angle = index * Math.PI * 2 / detail + turn * 0.3;
                    mesh.bottle(polar(1.5, angle, 2.35), 0.24, 0x72B28D);
                    double fall = age * 0.04 + index * 0.37;
                    fall -= Math.floor(fall);
                    double droplet = Math.max(0.05, Math.sin(fall * Math.PI));
                    mesh.gem(polar(1.3, angle, 2.1 - fall * 1.6), 0.13 * droplet, 0.27 * droplet, 0x78C8AD);
                }
            }
            case WHITEOUT_EDICT -> {
                mesh.box(new Point(0, 1.65, 0), 1.8, 1.9, 0.18, IRON);
                for (int side : new int[]{-1, 1}) {
                    mesh.box(new Point(0, 1.65, side * 0.12), 1.57, 1.65, 0.1, 0x5C89B4);
                    mesh.snowflake(new Point(0, 1.7, side * 0.2), 0.67, SILVER);
                }
                for (int index = 0; index < 4; index++) mesh.gem(polar(1.6, turn + index * Math.PI / 2, 0.6), 0.18, 0.45, 0x73ACD1);
            }
            case SUGAR_HAIL -> {
                for (int index = 0; index < detail + 2; index++) {
                    double fall = age * 0.045 + index * 0.19;
                    fall -= Math.floor(fall);
                    double size = 0.1 + 0.9 * Math.sin(fall * Math.PI);
                    Point center = polar(0.65 + index % 3 * 0.65, index * 2.4 + turn * 0.2, 3.1 - fall * 2.6);
                    mesh.gem(center, (0.22 + index % 2 * 0.08) * size, 0.35 * size, index % 2 == 0 ? 0xC0DEED : 0x6FA4D1);
                    mesh.ring(center, 0.28 * size, 0.025 * size, SILVER);
                }
            }
            case GLACIAL_PRISM -> {
                mesh.ring(new Point(0, 0.24, 0), 1.78, 0.055, SILVER);
                mesh.gem(new Point(0, 1.6, 0), 0.64, 1.1, 0x568CB1);
                for (int index = 0; index < 6; index++) {
                    double angle = index * Math.PI / 3 + turn * 0.2;
                    Point bottom = polar(1.55, angle, 0.3), top = polar(1.65, angle, 2.75);
                    mesh.leaf(bottom, top, 0.62, index % 2 == 0 ? 0x85C5D5 : 0x477B99);
                    mesh.rod(bottom, top, 0.035, SILVER);
                }
            }
            case ROYAL_SHATTER -> {
                mesh.ring(new Point(0, 0.7, 0), 1.35, 0.14, GOLD);
                for (int index = 0; index < 7; index++) {
                    double angle = index * Math.PI * 2 / 7;
                    mesh.leaf(polar(1.3, angle, 0.65), polar(1.5 + Math.min(age, 18) * 0.02, angle, 1.6 + index % 2 * 0.8), 0.36, 0x7367A9);
                    mesh.gem(polar(2.3, angle + turn, 0.8), 0.15, 0.34, 0xAFB7E6);
                }
            }
            case BRIAR_PROCESSION -> {
                for (int index = 0; index < 5; index++) {
                    Point base = new Point(Math.sin(index * 1.7) * 0.5, 0.12, index * 0.9 - 1.8);
                    mesh.rod(base, base.add(new Point(0, 1.2 + index * 0.16, 0)), 0.08, LEAF);
                    mesh.leaf(base.add(new Point(0, 0.55, 0)), base.add(new Point(-0.65, 1.15, 0.05)), 0.24, 0x648F51);
                    mesh.leaf(base.add(new Point(0, 0.8, 0)), base.add(new Point(0.55, 1.42, 0.1)), 0.23, 0x8AAB65);
                    mesh.gem(base.add(new Point(0, 1.3 + index * 0.16, 0)), 0.16, 0.27, 0xB56889);
                }
            }
            case SCARLET_POLLEN -> {
                mesh.flower(new Point(0, 0.7, 0), 1.4, 8, 0xB54777, 0xE3A0A7, turn * 0.1);
                for (int index = 0; index < detail; index++) {
                    Point center = polar(2, index * Math.PI * 2 / detail + turn, 1.1 + index % 2 * 0.8);
                    mesh.gem(center, 0.2, 0.23, 0xE4B766);
                    mesh.leaf(center, center.add(new Point(0.2, 0.45, 0)), 0.17, 0xD880A8);
                }
            }
            case INTOXICATING_TOAST -> {
                for (int index = 0; index < 2; index++) {
                    Point base = new Point(index == 0 ? -0.65 : 0.65, 0.35, 0);
                    mesh.prism(base, 0.4, 0.3, 0.12, 8, GOLD);
                    mesh.rod(base, base.add(new Point(0, 0.85, 0)), 0.075, GOLD);
                    mesh.prism(base.add(new Point(0, 0.8, 0)), 0.15, 0.48, 0.65, 8, 0xAF754A);
                    mesh.prism(base.add(new Point(0, 1.43, 0)), 0.39, 0.39, 0.035, 8, 0x82495A);
                    mesh.ring(base.add(new Point(0, 1.45, 0)), 0.48, 0.04, GOLD);
                }
                mesh.gem(new Point(0, 2.25 + Math.sin(turn) * 0.1, 0), 0.22, 0.37, 0xD6B86B);
            }
            case VINTAGE_MINEFIELD -> {
                for (int index = 0; index < 4; index++) {
                    Point center = polar(1.5, index * Math.PI / 2, 0.52);
                    mesh.barrel(center, 0.4, 0.82);
                    mesh.rod(center.add(new Point(0, 0.42, 0)), center.add(new Point(0.22, 0.74, 0)), 0.045, IRON);
                    mesh.gem(center.add(new Point(0.22, 0.82, 0)), 0.11, 0.17, 0xE9A15B);
                }
            }
            case SPORE_ERUPTION -> {
                mesh.mushroom(new Point(0, 0.1, 0), 1.5, 0x927542);
                for (int index = 0; index < detail; index++) {
                    double angle = index * Math.PI * 2 / detail;
                    Point pod = polar(1.4, angle, 2.4 + Math.sin(turn + index) * 0.25);
                    mesh.rod(polar(0.25, angle, 0.4), pod, 0.07, 0x697B4A);
                    mesh.gem(pod, 0.26, 0.38, 0xC7C873);
                }
            }
            case ROTTING_REQUIEM -> {
                mesh.prism(new Point(0, 0.15, 0), 0.7, 0.7, 2.4, 6, 0x424150);
                mesh.gem(new Point(0, 2.8, 0), 0.55, 0.65, 0x815A9A);
                for (int index = 0; index < 5; index++) {
                    double angle = index * Math.PI * 2 / 5 + turn * 0.4;
                    Point bell = polar(1.6, angle, 1.5);
                    mesh.prism(bell, 0.28, 0.13, 0.65, 6, 0x697451);
                    mesh.gem(bell.add(new Point(0, -0.13, 0)), 0.09, 0.2, 0xB694BE);
                }
            }
            case LIVING_MULCH -> {
                mesh.ring(new Point(0, 0.11, 0), 1.64, 0.07, 0x8B704C);
                mesh.prism(new Point(0, 0.08, 0), 1.45, 1.1, 0.6, 8, WOOD);
                for (int index = 0; index < detail; index++) {
                    double angle = index * Math.PI * 2 / detail;
                    mesh.rod(polar(0.65, angle, 0.4), polar(2.1, angle, 0.15), 0.1, 0x695139);
                    mesh.leaf(polar(0.8, angle, 0.65), polar(1.8, angle, 1.75), 0.45, LEAF);
                }
                mesh.gem(new Point(0, 1.0, 0), 0.4, 0.7, 0xA7CA74);
            }
            case SOLAR_ORCHARD -> {
                mesh.rod(new Point(0, 0.15, 0), new Point(0, 2.25, 0), 0.16, WOOD);
                for (int index = 0; index < 5; index++) {
                    double angle = index * Math.PI * 2 / 5;
                    Point tip = polar(1.25, angle, 1.6 + index % 2 * 0.6);
                    mesh.rod(new Point(0, 1.0, 0), tip, 0.075, GOLD);
                    mesh.gem(tip.add(new Point(0, -0.15, 0)), 0.29, 0.35, 0xC7964A);
                    mesh.leaf(tip, tip.add(polar(0.62, angle, 0.3)), 0.26, LEAF);
                }
                mesh.ring(new Point(0, 2.85, 0), 0.9, 0.07, GOLD);
            }
            case JUDGMENT_SEED -> {
                mesh.gem(new Point(0, 1.75, 0), 0.5, 0.9, 0xD4AF55);
                for (int index = 0; index < 5; index++) {
                    double angle = turn * 0.3 + index * Math.PI * 2 / 5;
                    mesh.leaf(polar(0.65, angle, 0.6), polar(1.45, angle, 2.75), 0.32, 0x85805B);
                    mesh.rod(polar(0.4, angle, 0.7), polar(0.65, angle, 2.5), 0.028, GOLD);
                }
                mesh.ring(new Point(0, 0.42, 0), 1.1, 0.08, IRON);
            }
            case UNDERTOW_SNARE -> {
                mesh.ring(new Point(0, 0.2, 0), 1.75, 0.15, 0x487685);
                for (int index = 0; index < 6; index++) {
                    double angle = index * Math.PI / 3 + turn * 0.1;
                    mesh.rod(polar(1.7, angle, 0.25), polar(0.65, angle + 0.6, 0.85), 0.07, IRON);
                    mesh.leaf(polar(1.55, angle, 0.3), polar(0.85, angle + 0.35, 1.7), 0.3, 0x71B9C3);
                }
                mesh.helix(0.65, 1.4, -turn, 0x39788C);
            }
            case ABYSSAL_CARAPACE -> {
                mesh.ring(new Point(0, 0.33, 0), 1.62, 0.065, 0x7BC2C1);
                mesh.gem(new Point(0, 1.25, 0), 0.55, 0.65, 0x3C8D85);
                for (int index = 0; index < 7; index++) {
                    double angle = index * Math.PI * 2 / 7;
                    mesh.leaf(polar(1.4, angle, 0.4), polar(0.72, angle, 2.6), 0.72, index % 2 == 0 ? 0x3C6974 : 0x628B95);
                    mesh.rod(polar(1.5, angle, 0.38), polar(0.76, angle, 2.5), 0.035, GOLD);
                }
            }
            case RIPTIDE_CORRIDOR -> {
                mesh.ring(new Point(0, 0.18, 0), 1.52, 0.052, 0x76C8D6);
                for (int index = 0; index < 4; index++) {
                    double depth = index * 1.05 - 1.6;
                    mesh.leaf(new Point(-1.3, 0.2, depth), new Point(-0.75, 2.05, depth + 0.4), 0.4, 0x3C7CA6);
                    mesh.leaf(new Point(1.3, 0.2, depth), new Point(0.75, 2.05, depth + 0.4), 0.4, 0x7AB9D5);
                    mesh.gem(new Point(0, 0.25, depth), 0.19, 0.25, SILVER);
                }
            }
            case TIDAL_LOTUS -> {
                mesh.flower(new Point(0, 0.45, 0), 1.85, 9, 0x337F87, 0x72B5BD, turn * 0.15);
                mesh.flower(new Point(0, 0.8, 0), 1.05, 6, 0x86CBB9, 0xBBD6B0, -turn * 0.2);
                mesh.gem(new Point(0, 1.3, 0), 0.32, 0.65, 0xD6BC71);
                mesh.ring(new Point(0, 0.2, 0), 2.2, 0.045, GOLD);
            }
            default -> throw new IllegalStateException("Missing model: " + spell);
        }
        return mesh.count;
    }

    public static int drawScroll(int kind, double age, double progress, boolean released, Sink output) {
        if (kind < 1 || kind > 4 || output == null || !Double.isFinite(age) || age < 0 || !Double.isFinite(progress)
                || progress < 0 || progress > 1 || released && progress >= 1) return 0;
        Mesh mesh = new Mesh(output, age, released ? 1 - ease(progress) : ease(progress), false);
        switch (kind) {
            case 1 -> {
                mesh.gem(new Point(0, 0, 0), 0.13, 0.42, 0x9F69C0);
                mesh.ring(new Point(0, -0.13, 0), 0.17, 0.035, GOLD);
                for (int index = 0; index < 4; index++) mesh.leaf(polar(0.06, index * Math.PI / 2, -0.12),
                        polar(0.25, index * Math.PI / 2, -0.38), 0.06, 0xCAB6D8);
            }
            case 2 -> {
                mesh.ring(new Point(0, 0.5, 0), 0.7, 0.07, GOLD);
                mesh.gem(new Point(0, 1.05, 0), 0.28, 0.8, 0x8554B3);
                for (int index = 0; index < 5; index++) mesh.gem(polar(0.7, index * Math.PI * 2 / 5 + age * 0.025, 0.65), 0.09, 0.25, 0xB59DCF);
            }
            case 3 -> {
                mesh.box(new Point(0, 0.8, 0), 1.2, 0.2, 0.72, 0x584263);
                mesh.box(new Point(0, 0.92, 0), 1.1, 0.06, 0.65, 0xD9CDB5);
                mesh.rod(new Point(0, 0.98, -0.34), new Point(0, 0.98, 0.34), 0.035, GOLD);
                mesh.gem(new Point(0, 1.55, 0), 0.26, 0.5, 0xB36799);
                mesh.ring(new Point(0, 1.4, 0), 0.75, 0.045, GOLD);
                for (int index = 0; index < 6; index++) mesh.leaf(polar(0.65, index * Math.PI / 3, 0.9),
                        polar(0.82, index * Math.PI / 3, 1.8), 0.16, 0x805D9F);
            }
            case 4 -> {
                for (int index = 0; index < 4; index++) {
                    double angle = index * Math.PI / 2 + age * 0.035;
                    mesh.leaf(polar(0.65, angle, 0.12), polar(0.25, angle + 0.35, 0.75), 0.14, 0x9573B4);
                    mesh.gem(polar(0.65, angle, 0.15), 0.11, 0.2, 0xCAB68D);
                }
                mesh.ring(new Point(0, 0.1, 0), 0.55, 0.035, GOLD);
            }
            default -> throw new IllegalStateException();
        }
        return mesh.count;
    }

    private static double ease(double progress) {
        double clamped = Math.max(0, Math.min(1, progress));
        return clamped * clamped * (3 - 2 * clamped);
    }

    private static Point polar(double radius, double angle, double height) {
        return new Point(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }

    private static final class Mesh {
        private final Sink output;
        private final double age;
        private final double opening;
        private final boolean reduced;
        private int count;
        private Mesh(Sink output, double age, double opening, boolean reduced) {
            this.output = output; this.age = age; this.opening = opening; this.reduced = reduced;
        }

        private void face(Point first, Point second, Point third, Point fourth, int color) {
            Point normal = second.sub(first).cross(third.sub(first));
            double area = normal.length();
            if (area < 1.0E-10) throw new IllegalStateException("Degenerate spell model face");
            if (++count > MAX_QUADS) throw new IllegalStateException("Spell model over budget");
            double light = 0.65 + 0.35 * Math.abs((normal.x() * 0.4 + normal.y() * 0.8 + normal.z() * 0.42) / area);
            int shaded = 0;
            for (int shift : new int[]{0, 8, 16}) shaded |= Math.min(255, (int) (((color >> shift) & 255) * light)) << shift;
            output.quad(reveal(first), reveal(second), reveal(third), reveal(fourth), shaded);
        }

        private Point reveal(Point point) {
            return new Point(point.x() * (0.78 + opening * 0.22), point.y() * (0.88 + opening * 0.12),
                    point.z() * (0.78 + opening * 0.22));
        }

        private void prism(Point center, double lowerRadius, double upperRadius, double height, int sides, int color) {
            int facets = reduced ? Math.max(6, sides) : Math.max(12, sides);
            Point top = center.add(new Point(0, height, 0));
            for (int index = 0; index < facets; index++) {
                double start = index * Math.PI * 2 / facets, end = (index + 1) * Math.PI * 2 / facets;
                Point lower = center.add(polar(lowerRadius, start, 0)), nextLower = center.add(polar(lowerRadius, end, 0));
                Point upper = top.add(polar(upperRadius, start, 0)), nextUpper = top.add(polar(upperRadius, end, 0));
                face(lower, nextLower, nextUpper, upper, color);
                face(center, nextLower, lower, lower, color);
                face(top, upper, nextUpper, nextUpper, color);
            }
        }

        private void gem(Point center, double radius, double height, int color) {
            int facets = reduced ? 6 : 10;
            Point top = center.add(new Point(0, height, 0)), bottom = center.add(new Point(0, -height, 0));
            for (int index = 0; index < facets; index++) {
                Point first = center.add(polar(radius, index * Math.PI * 2 / facets, 0));
                Point second = center.add(polar(radius, (index + 1) * Math.PI * 2 / facets, 0));
                if (!reduced) {
                    Point upper = center.add(polar(radius * 0.72, index * Math.PI * 2 / facets, height * 0.36));
                    Point nextUpper = center.add(polar(radius * 0.72, (index + 1) * Math.PI * 2 / facets, height * 0.36));
                    face(top, upper, nextUpper, nextUpper, color);
                    face(upper, first, second, nextUpper, color);
                } else face(top, first, second, second, color);
                face(bottom, second, first, first, color);
            }
        }

        private void ring(Point center, double radius, double tube, int color) {
            int segments = reduced ? 16 : 24;
            int facets = reduced ? 4 : 6;
            for (int segment = 0; segment < segments; segment++) for (int side = 0; side < facets; side++) {
                double start = segment * Math.PI * 2 / segments, end = (segment + 1) * Math.PI * 2 / segments;
                double lower = side * Math.PI * 2 / facets, upper = (side + 1) * Math.PI * 2 / facets;
                face(center.add(polar(radius + Math.cos(lower) * tube, start, Math.sin(lower) * tube)),
                        center.add(polar(radius + Math.cos(lower) * tube, end, Math.sin(lower) * tube)),
                        center.add(polar(radius + Math.cos(upper) * tube, end, Math.sin(upper) * tube)),
                        center.add(polar(radius + Math.cos(upper) * tube, start, Math.sin(upper) * tube)), color);
            }
        }

        private void rod(Point start, Point end, double width, int color) {
            Point forward = end.sub(start).normal();
            Point across = forward.cross(Math.abs(forward.y()) < 0.9 ? new Point(0, 1, 0) : new Point(1, 0, 0)).normal();
            Point up = forward.cross(across).normal();
            int facets = reduced ? 4 : 8;
            Point[] offsets = new Point[facets];
            for (int index = 0; index < facets; index++) {
                double angle = index * Math.PI * 2 / facets;
                offsets[index] = across.scale(Math.cos(angle) * width).add(up.scale(Math.sin(angle) * width));
            }
            for (int index = 0; index < facets; index++) {
                Point first = offsets[index], second = offsets[(index + 1) % facets];
                face(start.add(first), start.add(second), end.add(second), end.add(first), color);
            }
            if (reduced) {
                face(start.add(offsets[3]), start.add(offsets[2]), start.add(offsets[1]), start.add(offsets[0]), color);
                face(end.add(offsets[0]), end.add(offsets[1]), end.add(offsets[2]), end.add(offsets[3]), color);
            }
        }

        private void box(Point center, double width, double height, double depth, int color) {
            Point[] points = new Point[8];
            for (int index = 0; index < 8; index++) points[index] = center.add(new Point((index & 1) == 0 ? -width / 2 : width / 2,
                    (index & 2) == 0 ? -height / 2 : height / 2, (index & 4) == 0 ? -depth / 2 : depth / 2));
            for (int[] indices : new int[][]{{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}}) {
                face(points[indices[0]], points[indices[1]], points[indices[2]], points[indices[3]], color);
            }
        }

        private void leaf(Point start, Point end, double width, int color) {
            Point forward = end.sub(start).normal();
            Point side = forward.cross(Math.abs(forward.y()) < 0.9 ? new Point(0, 1, 0) : new Point(0, 0, 1)).normal().scale(width);
            Point middle = start.add(end).scale(0.5), bulge = forward.cross(side).normal().scale(width * 0.25);
            Point front = start.add(end.sub(start).scale(0.72)), back = start.add(end.sub(start).scale(0.28));
            Point[] outline = reduced ? new Point[]{start, middle.add(side), end, middle.sub(side)}
                    : new Point[]{start, back.add(side.scale(0.72)), front.add(side.scale(0.85)), end,
                            front.sub(side.scale(0.85)), back.sub(side.scale(0.72))};
            for (int index = 0; index < outline.length; index++) {
                Point first = outline[index], second = outline[(index + 1) % outline.length];
                face(middle.add(bulge), first, second, second, color);
                face(middle.sub(bulge), second, first, first, color);
            }
        }

        private void barrel(Point center, double radius, double height) {
            Point base = center.add(new Point(0, -height / 2, 0));
            prism(base, radius * 0.8, radius, height * 0.5, 10, WOOD);
            prism(center, radius, radius * 0.8, height * 0.5, 10, 0x85533B);
            ring(base.add(new Point(0, height * 0.18, 0)), radius * 0.92, 0.045, IRON);
            ring(base.add(new Point(0, height * 0.82, 0)), radius * 0.92, 0.045, GOLD);
            if (!reduced) for (int stave = 0; stave < 4; stave++) {
                double angle = stave * Math.PI / 2;
                Point middle = center.add(polar(radius * 1.015, angle, 0));
                rod(base.add(polar(radius * 0.85, angle, height * 0.06)), middle, radius * 0.018, 0xB28658);
                rod(middle, base.add(polar(radius * 0.85, angle, height * 0.94)), radius * 0.018, 0xB28658);
            }
        }

        private void flower(Point center, double radius, int petals, int outer, int inner, double angle) {
            for (int index = 0; index < petals; index++) {
                double turn = angle + index * Math.PI * 2 / petals;
                double spread = radius * (0.58 + opening * 0.42);
                double lift = 0.3 + (1 - opening) * 0.55 + Math.sin(age * 0.14 + index * 0.5) * radius * 0.055;
                Point root = center.add(polar(radius * 0.12, turn, 0)), tip = center.add(polar(spread, turn, lift));
                leaf(root, tip, radius * 0.31, index % 2 == 0 ? outer : inner);
                if (!reduced) rod(root.add(new Point(0, 0.055, 0)), root.add(tip.sub(root).scale(0.8))
                        .add(new Point(0, 0.055, 0)), radius * 0.008, inner);
            }
            gem(center.add(new Point(0, 0.25, 0)), radius * 0.2, radius * 0.3, GOLD);
        }

        private void mushroom(Point base, double size, int color) {
            prism(base, size * 0.16, size * 0.11, size * 0.85, 6, 0x9E9875);
            double breathing = 1 + Math.sin(age * 0.2 + base.x()) * 0.035;
            prism(base.add(new Point(0, size * 0.85, 0)), size * 0.68 * breathing, size * 0.22, size * 0.45, 8, color);
            if (!reduced) for (int gill = 0; gill < 4; gill++) {
                double angle = gill * Math.PI / 2;
                rod(base.add(polar(size * 0.16, angle, size * 0.86)),
                        base.add(polar(size * 0.57 * breathing, angle, size * 0.86)), size * 0.013, 0xD6CBA4);
            }
            for (int index = 0; index < 4; index++) gem(base.add(polar(size * 0.35, index * Math.PI / 2, size * 1.15)), size * 0.07, size * 0.06, 0xD6CBA4);
        }

        private void bottle(Point base, double size, int color) {
            prism(base, size, size, size * 1.6, 6, color);
            prism(base.add(new Point(0, size * 1.6, 0)), size, size * 0.4, size * 0.4, 6, color);
            prism(base.add(new Point(0, size * 2, 0)), size * 0.4, size * 0.4, size * 0.6, 6, GOLD);
            if (!reduced) {
                prism(base.add(new Point(0, size * 1.4, 0)), size * 1.02, size * 1.02, size * 0.12, 6, SILVER);
                gem(base.add(new Point(0, size * 0.85, size * 0.94)), size * 0.32, size * 0.35, GOLD);
            }
        }

        private void snowflake(Point center, double radius, int color) {
            for (int index = 0; index < 6; index++) {
                double angle = index * Math.PI / 3;
                Point direction = new Point(Math.cos(angle), Math.sin(angle), 0);
                rod(center, center.add(direction.scale(radius)), 0.028, color);
                for (int branch : new int[]{-1, 1}) rod(center.add(direction.scale(radius * 0.48)),
                        center.add(new Point(Math.cos(angle + branch * 0.35) * radius * 0.82,
                                Math.sin(angle + branch * 0.35) * radius * 0.82, 0)), 0.02, color);
            }
        }

        private void helix(double radius, double height, double angle, int color) {
            for (int index = 0; index < 24; index++) {
                rod(polar(radius * (1 - index / 48.0), angle + index * Math.PI / 6, 0.2 + height * index / 24),
                        polar(radius * (1 - (index + 1) / 48.0), angle + (index + 1) * Math.PI / 6, 0.2 + height * (index + 1) / 24), 0.07, color);
            }
        }
    }
}
