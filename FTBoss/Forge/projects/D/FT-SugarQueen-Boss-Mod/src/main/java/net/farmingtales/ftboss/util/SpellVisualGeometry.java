package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.SpellVisualRules.Form;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.util.SpellVisualRules.Spell;

public final class SpellVisualGeometry {
    public record Point(double horizontalX, double verticalY, double horizontalZ) {
        public Point add(Point other) { return new Point(horizontalX + other.horizontalX, verticalY + other.verticalY, horizontalZ + other.horizontalZ); }
        public Point scale(double amount) { return new Point(horizontalX * amount, verticalY * amount, horizontalZ * amount); }
    }

    @FunctionalInterface
    public interface Sink {
        void quad(Point first, Point second, Point third, Point fourth, int color, float alpha, int tile);
    }

    private SpellVisualGeometry() {}

    public static int draw(Spell spell, Phase phase, double age, float opacity, double length, Sink output) {
        return draw(spell, phase, age, opacity, length, false, output);
    }

    public static int draw(Spell spell, Phase phase, double age, float opacity, double length, boolean nativeLight, Sink output) {
        if (!Double.isFinite(age) || age < 0 || !Float.isFinite(opacity) || opacity <= 0
                || !Double.isFinite(length) || length < 0 || length > (spell == Spell.STAR_CROSS ? 128 : 64)) return 0;
        Mesh mesh = new Mesh(output, spell.color, Math.min(1, opacity) * 0.75F);
        double turn = age * 0.045;
        double radius = spell.radius;
        double swell = Math.min(1, (age + 2) / 8);
        if (phase == Phase.BEAM) {
            beam(mesh, spell, age, length, nativeLight);
            return mesh.count;
        }
        if (spell == Spell.STAR_CROSS || spell == Spell.ROSE_STARFALL) {
            boolean burst = phase != Phase.CHARGE;
            double progress = Math.min(1, age / spell.lifetime(phase));
            QizaiGeometry.Sink sink = (first, second, third, fourth, color, alpha) -> { if (alpha > 0) mesh.quad(
                    qizaiPoint(first), qizaiPoint(second), qizaiPoint(third), qizaiPoint(fourth), color,
                    alpha * opacity, SpellVisualRules.SOLID_TILE); };
            if (spell == Spell.STAR_CROSS) QizaiGeometry.cross(progress, burst, sink);
            else {
                QizaiGeometry.sigil(radius, progress, burst, false, sink);
                mesh.crystal(new Point(0, burst ? 0.4 : 1.2 + (1 - progress) * 4, 0), 0.35, burst ? 0.6 * (1 - progress) : 0.8, turn);
                for (int index = 0; index < 5; index++) {
                    double angle = index * Math.PI * 2 / 5 + turn;
                    mesh.crystal(polar(burst ? radius * progress : radius * 0.6, angle, burst ? 0.3 + progress : 1.5),
                            burst ? 0.15 : 0.1, burst ? 0.5 * (1 - progress) : 0.25, angle);
                }
                mesh.line(new Point(0, 0.1, 0), new Point(0, burst ? 3 * (1 - progress) : 5, 0), burst ? 0.06 : 0.014);
            }
            mesh.glyph(new Point(0, 0.045, 0), spell == Spell.STAR_CROSS ? 0.65 : radius * 0.75, 0, spell.ordinal(), false);
            return mesh.count;
        }
        mesh.glyph(new Point(0, 0.045, 0), radius * (phase == Phase.CHARGE ? 0.9 : 1), turn * 0.3, spell.ordinal(), false);
        if (RemainingSpellModels.renders(spell, phase)) {
            mesh.ring(new Point(0, 0.08, 0), radius, phase == Phase.CHARGE ? 0.035 : 0.05, 0, 0, Math.PI * 2, 48);
            return mesh.count;
        }
        if (spell.ordinal() >= Spell.EFFIGY_PROCESSION.ordinal()) {
            additional(mesh, spell, phase, age, turn, swell);
            return mesh.count;
        }
        if (phase == Phase.CHARGE) {
            mesh.ring(new Point(0, 0.08, 0), radius, 0.035, 0, 0, Math.PI * 2, 48);
            for (int index = 0; index < 6; index++) {
                double angle = turn + index * Math.PI / 3;
                Point point = polar(radius * 0.85, angle, 0.25 + (age * 0.035 + index * 0.18) % 1.4);
                mesh.crystal(point, 0.07, 0.18, angle);
            }
            mesh.glyph(new Point(0, 0.8, 0), Math.min(0.65, radius / 2), -turn, spell.ordinal(), true);
            return mesh.count;
        }
        switch (spell.form) {
            case CROWN -> {
                mesh.ring(new Point(0, 0.25, 0), radius * 0.7, 0.12, 0, 0, Math.PI * 2, 40);
                for (int index = 0; index < 10; index++) {
                    double angle = index * Math.PI / 5;
                    mesh.crystal(polar(radius * 0.7, angle, 0.35), 0.26, (index % 2 == 0 ? 2.6 : 1.5) * swell, angle);
                }
                mesh.ring(new Point(0, 0.12, 0), radius * swell, 0.06, 0, 0, Math.PI * 2, 48);
            }
            case FLOWER, GARDEN -> {
                int petals = spell == Spell.HALO_SANCTUARY ? 12 : spell == Spell.FLESH_BLOOM ? 6 : 8;
                for (int index = 0; index < petals; index++) {
                    double angle = index * Math.PI * 2 / petals + turn * 0.4;
                    petal(mesh, angle, radius * swell, 0.25, 1.2 + Math.sin(turn) * 0.3);
                }
                mesh.ring(new Point(0, 0.2, 0), radius * 0.7, 0.06, 0, 0, Math.PI * 2, 48);
                if (spell.form == Form.GARDEN) {
                    for (int index = 0; index < 4; index++) mesh.ring(new Point(0, 0.1, 0), radius * 0.8, 0.025,
                            1 + index % 2, turn + index * 0.3, Math.PI, 24);
                    motes(mesh, radius, 2.8, age, 12);
                } else mesh.crystal(new Point(0, 0.5, 0), 0.4, 1.1 * swell, turn);
            }
            case VORTEX -> {
                for (int arm = 0; arm < 3; arm++) {
                    Point previous = polar(radius, turn + arm * Math.PI * 2 / 3, 0.1);
                    for (int step = 1; step <= 40; step++) {
                        double progress = step / 40.0;
                        Point next = polar(radius * (1 - progress * 0.75), turn + arm * Math.PI * 2 / 3 + progress * Math.PI * 4, 0.1 + progress * 3);
                        mesh.line(previous, next, 0.045 + 0.025 * progress);
                        previous = next;
                    }
                }
                if (spell == Spell.VINTAGE_VORTEX) barrel(mesh, new Point(0, 1.5, 0), 0.45, turn);
                else if (spell == Spell.ROTTING_REQUIEM) motes(mesh, radius, 2.5, age, 18);
                else mesh.ring(new Point(0, 0.1, 0), radius * 0.6, 0.09, 0, -turn, Math.PI * 2, 40);
            }
            case CORE -> {
                mesh.crystal(new Point(0, 1.2, 0), 0.4, 0.9, turn);
                for (int axis = 0; axis < 3; axis++) mesh.ring(new Point(0, 1.2, 0), 0.9, 0.06, axis, turn, Math.PI * 2, 32);
            }
            case SPORES -> {
                for (int index = 0; index < 7; index++) {
                    Point base = polar(0.7 + index % 3, index * 2.4, 0.1);
                    if (spell == Spell.SCARLET_POLLEN) {
                        mesh.crystal(base.add(new Point(0, 0.7 * swell, 0)), 0.18, 0.35, turn + index);
                        petal(mesh, index * 2.4 + turn, radius * 0.7, 0.5, 0.6);
                    } else {
                        mesh.line(base, base.add(new Point(0, 0.7 * swell, 0)), 0.08);
                        mesh.dome(base.add(new Point(0, 0.7 * swell, 0)), 0.45, 0.3, turn);
                    }
                }
                mesh.ring(new Point(0, 0.12, 0), radius * swell, 0.08, 0, 0, Math.PI * 2, 40);
                motes(mesh, radius, 2, age, 18);
            }
            case HALO -> {
                for (int band = 0; band < 3; band++) mesh.ring(new Point(0, 0.3 + band * 0.7, 0),
                        radius * swell * (1 - band * 0.17), 0.055, 0, turn, Math.PI * 2, 48);
                for (int index = 0; index < 12; index++) {
                    Point base = polar(radius * 0.85 * swell, index * Math.PI / 6, 0.15);
                    mesh.line(base, base.add(new Point(0, index % 2 == 0 ? 2.2 : 0.8, 0)), 0.035);
                }
            }
            case WAVE, TRIDENT -> {
                double distance = (spell.form == Form.WAVE ? 12 : 10) * Math.min(1, age / 12);
                if (spell == Spell.RIPTIDE_CORRIDOR) distance = 1.5;
                if (spell == Spell.CINDER_BREATH) distance = 10 * Math.min(1, age / 8);
                for (int band = 0; band < (spell.form == Form.WAVE ? 3 : 4); band++) {
                    double forward = Math.max(0.5, distance - band * 1.1);
                    double width = 1.2 + forward * 0.45;
                    Point previous = new Point(-width, 0.2, forward);
                    for (int step = 1; step <= 24; step++) {
                        double across = step / 24.0;
                        Point next = new Point((across * 2 - 1) * width, 0.25 + Math.sin(across * Math.PI) * (1.5 - band * 0.2), forward);
                        mesh.line(previous, next, 0.09);
                        previous = next;
                    }
                }
                if (spell.form == Form.TRIDENT) {
                    for (int fork = -1; fork <= 1; fork++) {
                        mesh.line(new Point(fork * 0.55, 1.2, distance - 1), new Point(fork * 0.55, 1.2, distance + 0.5), 0.09);
                        mesh.crystal(new Point(fork * 0.55, 1.1, distance + 0.5), 0.16, 0.5, turn);
                    }
                }
            }
            case LANCE, NEEDLES, PRESSURE, LASER, FEATHERS -> {
                mesh.ring(new Point(0, 1.1, 0), 0.7 * swell, 0.075, 2, turn, Math.PI * 2, 32);
                for (int index = 0; index < 4; index++) mesh.crystal(polar(0.9, turn + index * Math.PI / 2, 1.2), 0.12, 0.45, turn);
            }
            case BARREL, SHELL, GEYSER -> {
                if (phase != Phase.PULSE) {
                    mesh.ring(new Point(0, 0.12, 0), radius * (0.95 - 0.25 * swell), 0.07, 0, turn, Math.PI * 2, 48);
                    if (spell.form == Form.BARREL) barrel(mesh, new Point(0, 0.7 + Math.sin(turn * 3) * 0.15, 0), 0.8, turn);
                    else if (spell.form == Form.SHELL) mesh.crystal(new Point(0, 0.4, 0), 0.45, 1.2, turn);
                    else for (int index = 0; index < 3; index++) mesh.ring(new Point(0, 0.2 + index * 0.15, 0), 0.4 + index * 0.6, 0.06, 0, turn, Math.PI * 1.6, 32);
                } else {
                    mesh.ring(new Point(0, 0.12, 0), radius * swell, 0.13, 0, 0, Math.PI * 2, 48);
                    if (spell.form == Form.GEYSER) {
                        for (int stream = 0; stream < 8; stream++) {
                            Point previous = polar(0.6, stream * Math.PI / 4, 0.15);
                            for (int step = 1; step <= 16; step++) {
                                double height = step / 16.0;
                                Point next = polar(0.6 + height * height, stream * Math.PI / 4 + height + turn, height * 5 * swell);
                                mesh.line(previous, next, 0.11 - height * 0.05);
                                previous = next;
                            }
                        }
                    } else {
                        for (int index = 0; index < 14; index++) mesh.crystal(polar(radius * swell * 0.8, index * 2.4,
                                0.2 + Math.sin(Math.min(1, age / 24) * Math.PI) * (1 + index % 3)), 0.14, 0.5, turn + index);
                    }
                }
            }
            case REPULSOR -> {
                for (int axis = 0; axis < 3; axis++) mesh.ring(new Point(0, 0.5, 0), radius * swell, 0.065, axis, turn, Math.PI * 2, 48);
                for (int index = 0; index < 10; index++) mesh.line(new Point(0, 0.6, 0), polar(radius * swell, index * Math.PI / 5, 0.6), 0.025);
            }
            case REAP, CLEAVE, PAN -> {
                if (spell == Spell.BRIAR_PROCESSION) {
                    for (int index = 0; index < 9; index++) {
                        Point foot = polar(radius * (0.3 + index % 3 * 0.25), index * 2.4, 0.15);
                        mesh.crystal(foot, 0.16, (0.7 + index % 3 * 0.6) * swell, turn);
                        mesh.line(foot, foot.add(polar(0.5, index * 2.4, 1.1)), 0.055);
                    }
                } else if (spell == Spell.VIRULENT_HARVEST || spell == Spell.GOLDEN_HARVEST) {
                    for (int index = 0; index < 5; index++) mesh.ring(polar(radius * (1 - Math.min(0.8, age / 32)), index * Math.PI * 0.4 + turn, 0.8),
                            0.65, 0.12, 0, turn, Math.PI * 1.3, 18);
                } else {
                    for (int index = 0; index < 3; index++) mesh.ring(new Point(0, 0.6 + index * 0.35, 0),
                            radius * (0.65 + index * 0.3) * swell, spell.form == Form.CLEAVE ? 0.22 : 0.13, 0,
                            Math.PI * 0.12, Math.PI * 0.76, 28);
                    if (spell.form == Form.PAN) {
                        mesh.ring(new Point(0, 1.5, 2), 0.8, 0.18, 2, turn, Math.PI * 2, 24);
                        mesh.line(new Point(0, 0.8, 2), new Point(0, -0.3, 2), 0.12);
                        motes(mesh, 3, 2, age, 10);
                    }
                }
            }
            case SHIELD -> {
                int plates = spell == Spell.ORCHARD_AEGIS ? 6 : 4;
                for (int index = 0; index < plates; index++) {
                    double angle = turn * 0.5 + index * Math.PI * 2 / plates;
                    Point point = polar(radius, angle, 1.1);
                    mesh.glyph(point, 0.48, angle, spell.ordinal(), true);
                    mesh.crystal(point.add(new Point(0, -0.55, 0)), 0.22, 1.1, angle);
                }
                mesh.ring(new Point(0, 0.18, 0), radius, 0.04, 0, turn, Math.PI * 2, 40);
            }
            case DOLL -> doll(mesh, new Point(0, 1.4, 0), 0.65, turn);
            case CANDLES -> {
                for (int index = 0; index < 8; index++) {
                    Point foot = polar(radius * 0.85, index * Math.PI / 4, 0.1);
                    mesh.line(foot, foot.add(new Point(0, 1.4, 0)), 0.13);
                    mesh.crystal(foot.add(new Point(0, 1.45, 0)), 0.14, 0.55 + Math.sin(turn * 4 + index) * 0.1, turn);
                    mesh.line(foot.add(new Point(0, 0.6, 0)), polar(radius * 0.85, (index + 1) * Math.PI / 4, 0.7), 0.04);
                }
                mesh.ring(new Point(0, 2.2, 0), radius * 0.8, 0.035, 0, turn, Math.PI * 2, 40);
            }
            case OATH -> {
                for (int index = 0; index < 3; index++) {
                    mesh.ring(new Point(0, 0.3 + index * 0.55, 0), 1.2, 0.08, 0, turn + index * 2, Math.PI * 1.4, 32);
                    mesh.glyph(polar(1.2, turn + index * Math.PI * 2 / 3, 1.4), 0.38, -turn, spell.ordinal(), true);
                }
            }
            case HEARTH -> {
                mesh.ring(new Point(0, 0.2, 0), 1.2, 0.18, 0, 0, Math.PI * 2, 32);
                for (int index = 0; index < 7; index++) mesh.crystal(polar(0.55, index * Math.PI * 2 / 7, 0.2),
                        0.22, 0.7 + Math.sin(turn * 4 + index) * 0.3, turn);
                mesh.ring(new Point(0, 0.1, 0), radius * swell, 0.06, 0, 0, Math.PI * 2, 48);
                motes(mesh, radius, 2, age, 12);
            }
            case RAIN, BLIZZARD -> {
                mesh.ring(new Point(0, 3.3, 0), radius * 0.8, 0.055, 0, -turn, Math.PI * 2, 48);
                for (int index = 0; index < 24; index++) {
                    double height = 3.2 - (age * 0.16 + index * 0.31) % 3;
                    Point point = polar(radius * (0.2 + (index * 0.37) % 0.75), index * 2.4 + (spell.form == Form.BLIZZARD ? turn : 0), height);
                    mesh.line(point, point.add(new Point(spell.form == Form.BLIZZARD ? 0.24 : 0, 0.3, 0)), 0.025);
                    if (index % 3 == 0) mesh.crystal(point, 0.07, 0.18, turn);
                }
            }
            case GEARS -> {
                for (int index = 0; index < 3; index++) {
                    Point point = polar(radius * 0.7, turn + index * Math.PI * 2 / 3, 0.9 + index * 0.25);
                    mesh.ring(point, 0.65, 0.14, 0, -turn, Math.PI * 2, 32);
                    for (int tooth = 0; tooth < 10; tooth++) {
                        double angle = tooth * Math.PI / 5 - turn * 3;
                        mesh.line(point.add(polar(0.5, angle, 0)), point.add(polar(0.85, angle, 0)), 0.12);
                    }
                }
            }
            case BRAND, VOID -> {
                for (int index = 0; index < 3; index++) mesh.ring(new Point(0, 0.1 + index * 0.6, 0),
                        radius * (1 - index * 0.18), 0.05, 0, -turn + index, Math.PI * 1.7, 40);
                mesh.glyph(new Point(0, 1.2, 0), 1, -turn, spell.ordinal(), true);
                if (spell.form == Form.BRAND) for (int index = 0; index < 6; index++) mesh.crystal(polar(radius * 0.8, index * Math.PI / 3, 0.1), 0.13, 1.6, turn);
                else for (int axis = 1; axis <= 2; axis++) mesh.ring(new Point(0, 1.3, 0), 1.1 * swell, 0.07, axis, turn, Math.PI * 2, 40);
            }
            case WINGS -> {
                for (int side : new int[] {-1, 1}) for (int feather = 0; feather < 7; feather++) {
                    Point root = new Point(side * 0.15, 1.3, -0.3);
                    Point tip = new Point(side * (0.9 + feather * 0.22), 2.2 - feather * 0.18 + Math.sin(turn * 2) * 0.2, -0.5);
                    mesh.line(root, tip, 0.09);
                    mesh.line(tip, tip.add(new Point(-side * 0.15, -0.45, -0.1)), 0.11);
                }
            }
        }
        return mesh.count;
    }

    private static void beam(Mesh mesh, Spell spell, double age, double length, boolean nativeLight) {
        if (spell == Spell.ROSE_STARFALL) {
            QizaiRoseStarfallModel.beam(age, length, false, 1.0F,
                    (first, second, third, fourth, color, alpha) -> mesh.quad(
                            rosePoint(first), rosePoint(second), rosePoint(third), rosePoint(fourth),
                            color, alpha * mesh.alpha, SpellVisualRules.SOLID_TILE));
            return;
        }
        Point start = new Point(0, 0, 0);
        Point end = new Point(0, 0, length);
        double head = Math.min(1, (age + 1) / 5) * length;
        if (spell == Spell.ARCANE_FUSILLADE) {
            doraFocusLaser(mesh, age, length);
            return;
        }
        if (spell == Spell.CAPACITOR_OVERLOAD) {
            if (nativeLight) return;
            Point previous = start;
            for (int step = 1; step <= 14; step++) {
                Point next = step == 14 ? end : new Point(Math.sin(step * 14.7 + Math.floor(age / 2)) * 0.22,
                        Math.cos(step * 7.3) * 0.22, length * step / 14);
                mesh.line(previous, next, 0.055);
                if (step % 3 == 0) mesh.line(next, next.add(new Point(0.35, 0.4, 0.2)), 0.022);
                previous = next;
            }
            return;
        }
        if (!nativeLight) mesh.line(start, end, spell.form == Form.PRESSURE ? 0.11 : 0.035);
        switch (spell.form) {
            case DOLL -> doll(mesh, new Point(0, 0, head), 0.5, age * 0.1);
            case FEATHERS -> {
                for (int index = 0; index < 6; index++) {
                    double offset = index * 0.13;
                    mesh.line(new Point(0, 0, head - offset), new Point(0.23, 0, head - offset - 0.25), 0.04);
                    mesh.line(new Point(0, 0, head - offset), new Point(-0.23, 0, head - offset - 0.25), 0.04);
                }
            }
            case PRESSURE -> {
                for (int index = 1; index <= 8; index++) mesh.ring(new Point(0, 0, length * index / 9), 0.35, 0.04, 2, age * 0.1, Math.PI * 2, 16);
            }
            case LANCE, NEEDLES -> {
                mesh.line(new Point(0, 0, Math.max(0, head - 2)), new Point(0, 0, head), 0.16);
                Point tip = new Point(0, 0, head + 0.2);
                mesh.solid(new Point(-0.28, 0, head - 0.7), tip, new Point(0.28, 0, head - 0.7), tip);
                mesh.solid(new Point(0, -0.28, head - 0.7), tip, new Point(0, 0.28, head - 0.7), tip);
            }
            default -> {
                mesh.ring(new Point(0, 0, head), 0.24, 0.045, 2, age * 0.1, Math.PI * 2, 16);
                mesh.line(new Point(0, 0, Math.max(0, head - 0.8)), new Point(0, 0, head), 0.09);
            }
        }
        mesh.ring(end, 0.2 + Math.min(0.5, age * 0.06), 0.04, 2, age * 0.15, Math.PI * 2, 20);
    }

    private static void doraFocusLaser(Mesh mesh, double age, double length) {
        if (length < 0.015) return;
        double pulse = 1.0 + Math.sin(age * 1.35) * 0.10;
        double outer = 0.15 * pulse;
        double middle = 0.072 * pulse;
        double core = 0.024 * pulse;
        Point start = new Point(0, 0, 0);
        Point end = new Point(0, 0, length);
        laserRibbon(mesh, start, end, outer, 0x39CFC5, mesh.alpha * 0.28F);
        laserRibbon(mesh, start, end, middle, 0x9FFFF0, mesh.alpha * 0.76F);
        laserRibbon(mesh, start, end, core, 0xFFFFFF, Math.min(1.0F, mesh.alpha * 1.28F));

        int gates = length < 1.0 ? 2 : 6;
        double ringRadius = 0.19 * Math.min(1.0, 0.35 + length / 2.5);
        for (int gate = 0; gate < gates; gate++) {
            double along = (gate + 0.32 + age * 0.24) / gates;
            along -= Math.floor(along);
            mesh.ring(new Point(0, 0, length * along), ringRadius, Math.max(0.012, ringRadius * 0.16),
                    2, age * 0.16 + gate * 0.53, Math.PI * 2, 12);
        }
        mesh.ring(new Point(0, 0, length), ringRadius * 1.28, Math.max(0.016, ringRadius * 0.18),
                2, -age * 0.24, Math.PI * 2, 16);
    }

    private static void laserRibbon(Mesh mesh, Point start, Point end, double width, int color, float alpha) {
        Point horizontal = new Point(width, 0, 0);
        Point vertical = new Point(0, width, 0);
        mesh.quad(start.add(horizontal.scale(-1)), end.add(horizontal.scale(-1)), end.add(horizontal),
                start.add(horizontal), color, alpha, SpellVisualRules.SOLID_TILE);
        mesh.quad(start.add(vertical.scale(-1)), end.add(vertical.scale(-1)), end.add(vertical),
                start.add(vertical), color, alpha, SpellVisualRules.SOLID_TILE);
    }

    private static void petal(Mesh mesh, double angle, double radius, double height, double rise) {
        Point root = new Point(0, height, 0);
        Point tip = polar(radius, angle, height + 0.2);
        Point left = polar(radius * 0.62, angle - 0.25, height + rise);
        Point right = polar(radius * 0.62, angle + 0.25, height + rise);
        mesh.solid(root, left, tip, right);
        mesh.line(root, left, 0.025);
        mesh.line(left, tip, 0.025);
        mesh.line(tip, right, 0.025);
    }

    private static void motes(Mesh mesh, double radius, double height, double age, int count) {
        for (int index = 0; index < count; index++) mesh.crystal(polar(radius * (0.3 + (index * 0.31) % 0.6),
                index * 2.4 + age * 0.02, 0.2 + (index * 0.37 + age * 0.04) % height), 0.045, 0.14, age * 0.03);
    }

    private static void doll(Mesh mesh, Point center, double scale, double turn) {
        mesh.crystal(center.add(new Point(0, scale * 0.5, 0)), scale * 0.32, scale * 0.45, turn);
        mesh.line(center.add(new Point(0, scale * 0.4, 0)), center.add(new Point(0, -scale * 0.4, 0)), scale * 0.17);
        for (int side : new int[] {-1, 1}) {
            mesh.line(center, center.add(new Point(side * scale * 0.65, scale * 0.15, 0)), scale * 0.1);
            mesh.line(center.add(new Point(0, -scale * 0.3, 0)), center.add(new Point(side * scale * 0.35, -scale * 0.9, 0)), scale * 0.1);
        }
    }

    private static void barrel(Mesh mesh, Point center, double scale, double turn) {
        for (int index = 0; index < 10; index++) {
            double angle = index * Math.PI / 5 + turn;
            mesh.line(center.add(polar(scale * 0.7, angle, -scale)), center.add(polar(scale, angle, 0)), 0.075);
            mesh.line(center.add(polar(scale, angle, 0)), center.add(polar(scale * 0.7, angle, scale)), 0.075);
        }
        for (int hoop = -1; hoop <= 1; hoop++) mesh.ring(center.add(new Point(0, hoop * scale * 0.75, 0)), scale * (hoop == 0 ? 1 : 0.8), 0.085, 0, turn, Math.PI * 2, 24);
    }

    private static void additional(Mesh mesh, Spell spell, Phase phase, double age, double turn, double swell) {
        double radius = spell.radius;
        boolean impact = phase == Phase.PULSE;
        double lift = impact ? 0 : 2.8;
        mesh.ring(new Point(0, 0.09, 0), radius, 0.04, 0, turn * 0.2, Math.PI * 2, 48);
        switch (spell.form) {
            case PROCESSION -> {
                for (int index = 0; index < 3; index++) {
                    double angle = turn + index * Math.PI * 2 / 3;
                    Point center = polar(radius * 0.65, angle, 1.2 + Math.sin(turn * 2 + index) * 0.2);
                    doll(mesh, center, 0.65, angle);
                    mesh.line(center.add(new Point(0, 0.8, 0)), new Point(0, 3.5, 0), 0.025);
                    if (impact) mesh.line(center, new Point(0, 0.5, 0), 0.06 * (1 - Math.min(0.8, age / 20)));
                }
                mesh.crystal(new Point(0, 3.4, 0), 0.3, 0.6, turn);
            }
            case ECLIPSE -> {
                mesh.ring(new Point(0, 2.5, 0), 1.15, 0.16, 2, turn, Math.PI * 1.65, 40);
                mesh.ring(new Point(0.25, 2.6, 0), 0.9, 0.06, 2, -turn, Math.PI * 1.6, 36);
                for (int index = 0; index < 6; index++) {
                    Point base = polar(radius * 0.8, index * Math.PI / 3, 0.15);
                    mesh.line(base, base.add(new Point(0, 1.0, 0)), 0.11);
                    mesh.crystal(base.add(new Point(0, 1.1, 0)), 0.16, 0.45, turn);
                    if (impact) mesh.line(base.add(new Point(0, 1.1, 0)), new Point(0, 2.5, 0), 0.07);
                }
            }
            case BATTERY -> {
                for (int index = 0; index < 3; index++) {
                    double angle = index * Math.PI * 2 / 3;
                    Point core = polar(2.5, angle, 1.8);
                    mesh.crystal(core, 0.45, 1.2, turn);
                    mesh.ring(core.add(new Point(0, 0.35, 0)), 0.7, 0.07, 0, -turn, Math.PI * 2, 24);
                    for (int leg = 0; leg < 3; leg++) mesh.line(core, core.add(polar(0.65, angle + leg * Math.PI * 2 / 3, -1.65)), 0.06);
                    if (impact) mesh.line(core.add(new Point(0, 0.3, 0)), new Point(0, 0.5, 0), 0.1);
                }
            }
            case VERDICT -> {
                Point tip = new Point(0, 0.2 + lift, 0);
                mesh.crystal(tip.add(new Point(0, 2.2, 0)), 0.38, -2.2, 0);
                mesh.line(tip.add(new Point(-0.85, 2.2, 0)), tip.add(new Point(0.85, 2.2, 0)), 0.13);
                mesh.line(tip.add(new Point(0, 2.2, 0)), tip.add(new Point(0, 3.2, 0)), 0.1);
                mesh.crystal(tip.add(new Point(0, 3.25, 0)), 0.18, 0.35, turn);
                if (impact) for (int index = 0; index < 8; index++) mesh.line(new Point(0, 0.15, 0), polar(radius * swell, index * Math.PI / 4, 0.15), 0.05);
            }
            case CITRUS -> {
                double size = impact ? radius * swell : 1.25;
                mesh.ring(new Point(0, impact ? 0.4 : 1.6, 0), size, 0.15, impact ? 0 : 2, turn, Math.PI * 2, 40);
                for (int index = 0; index < 8; index++) {
                    double angle = index * Math.PI / 4 + turn;
                    Point center = new Point(0, impact ? 0.4 : 1.6, 0);
                    Point edge = impact ? polar(size * 0.85, angle, 0) : new Point(Math.cos(angle) * size * 0.85, Math.sin(angle) * size * 0.85, 0);
                    mesh.line(center, center.add(edge), 0.06);
                    if (impact) mesh.crystal(center.add(edge), 0.18, 0.9, angle);
                }
                mesh.line(new Point(0.8, 2.7, 0), new Point(1.7, 3.1, 0), 0.18);
            }
            case LOTUS -> {
                for (int index = 0; index < 10; index++) petal(mesh, index * Math.PI / 5 + turn * 0.2, radius * 0.8, 0.25, 1.4);
                mesh.crystal(new Point(0, 0.6, 0), 0.3, 1, turn);
                for (int index = 0; index < 3; index++) mesh.ring(new Point(0, 0.2 + index * 0.3, 0), radius * (0.8 - index * 0.17), 0.05, 0, turn, Math.PI * 2, 40);
                motes(mesh, radius, 2.5, age, 12);
            }
            case MAGNET -> {
                mesh.crystal(new Point(0, 1.5, 0), 0.45, 1.1, turn);
                for (int axis = 0; axis < 3; axis++) mesh.ring(new Point(0, 1.8, 0), 1.2 + axis * 0.25, 0.075, axis, turn * 2, Math.PI * 1.7, 36);
                for (int index = 0; index < 6; index++) {
                    Point edge = polar(radius * 0.85, index * Math.PI / 3 + turn, 0.3);
                    mesh.crystal(edge, 0.2, 0.65, turn);
                    if (impact) {
                        Point kink = edge.scale(0.55).add(new Point(0.2 * Math.sin(index + age), 1.2, 0));
                        mesh.line(edge, kink, 0.065);
                        mesh.line(kink, new Point(0, 1.8, 0), 0.065);
                    }
                }
            }
            case NIGHTBLADES -> {
                for (int index = 0; index < 4; index++) {
                    double angle = index * Math.PI / 2 + turn;
                    Point tip = polar(impact ? 0.3 + Math.min(1, age / 12) * 0.6 : radius * 0.7, angle, 0.8);
                    Point hilt = tip.add(polar(1.3, angle, 1.4));
                    mesh.line(tip, hilt, 0.18);
                    mesh.line(hilt.add(polar(0.45, angle + Math.PI / 2, 0)), hilt.add(polar(0.45, angle - Math.PI / 2, 0)), 0.08);
                    mesh.line(hilt, hilt.add(polar(0.4, angle, 0.4)), 0.07);
                }
                mesh.ring(new Point(0, 1.3, 0), 1.2, 0.05, 2, -turn, Math.PI * 2, 32);
            }
            case SERAPH -> {
                Point core = new Point(0, 0.2 + lift, 0);
                mesh.line(core, core.add(new Point(0, 2.8, 0)), 0.1);
                mesh.crystal(core.add(new Point(0, 0.65, 0)), 0.25, -0.65, 0);
                mesh.ring(core.add(new Point(0, 2.5, 0)), 0.85, 0.08, 0, turn, Math.PI * 2, 32);
                for (int side : new int[]{-1, 1}) for (int feather = 0; feather < 5; feather++) {
                    Point root = core.add(new Point(side * 0.15, 1.8, 0));
                    Point tip = core.add(new Point(side * (1.0 + feather * 0.26), 2.7 - feather * 0.25, 0.3));
                    mesh.line(root, tip, 0.1);
                }
                if (impact) mesh.ring(new Point(0, 0.25, 0), radius * swell, 0.1, 0, turn, Math.PI * 2, 48);
            }
            case CLOCK -> {
                Point center = new Point(0, 2.3, 0);
                mesh.ring(center, 1.7, 0.09, 2, 0, Math.PI * 2, 48);
                for (int index = 0; index < 12; index++) {
                    double angle = index * Math.PI / 6;
                    mesh.line(center.add(new Point(Math.cos(angle) * 1.4, Math.sin(angle) * 1.4, 0)), center.add(new Point(Math.cos(angle) * 1.6, Math.sin(angle) * 1.6, 0)), 0.07);
                }
                mesh.line(center, center.add(new Point(Math.cos(-turn * 2) * 1.3, Math.sin(-turn * 2) * 1.3, 0)), 0.06);
                mesh.line(center, center.add(new Point(Math.cos(-turn * 0.3) * 0.8, Math.sin(-turn * 0.3) * 0.8, 0.02)), 0.09);
                for (int index = 0; index < 6; index++) mesh.crystal(polar(radius * 0.85, index * Math.PI / 3, 0.1), 0.22, impact ? 1.4 * swell : 0.5, turn);
            }
            case REFUGE -> {
                for (int index = 0; index < 10; index++) {
                    double angle = index * Math.PI / 5 + turn * 0.1;
                    Point foot = polar(radius * 0.85, angle, 0.2);
                    Point shoulder = polar(radius * 0.65, angle, 2.4);
                    Point tip = polar(0.5, angle, 3.4);
                    mesh.line(foot, shoulder, 0.07);
                    mesh.line(shoulder, tip, 0.06);
                    for (int barb = 1; barb < 5; barb++) {
                        Point base = foot.scale(1 - barb / 5.0).add(shoulder.scale(barb / 5.0));
                        mesh.line(base, base.add(polar(0.4, angle + Math.PI / 2, 0.3)), 0.04);
                        mesh.line(base, base.add(polar(0.4, angle - Math.PI / 2, 0.3)), 0.04);
                    }
                }
                mesh.ring(new Point(0, 3.5, 0), 0.55, 0.06, 0, turn, Math.PI * 2, 24);
            }
            default -> { }
        }
    }

    private static Point polar(double radius, double angle, double height) {
        return new Point(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }

    private static Point qizaiPoint(QizaiGeometry.Point point) { return new Point(point.x(), point.y(), point.z()); }
    private static Point rosePoint(QizaiRoseStarfallModel.Point point) { return new Point(point.x(), point.y(), point.z()); }

    private static final class Mesh {
        private final Sink sink;
        private final int color;
        private final float alpha;
        private int count;

        private Mesh(Sink sink, int color, float alpha) { this.sink = sink; this.color = color; this.alpha = alpha; }

        private void solid(Point first, Point second, Point third, Point fourth) {
            quad(first, second, third, fourth, color, alpha, SpellVisualRules.SOLID_TILE);
        }

        private void quad(Point first, Point second, Point third, Point fourth, int tint, float opacity, int tile) {
            if (count >= SpellVisualRules.MAX_QUADS) return;
            sink.quad(first, second, third, fourth, tint, opacity, tile);
            count++;
        }

        private void line(Point first, Point second, double width) {
            Point direction = second.add(first.scale(-1));
            double horizontal = Math.hypot(direction.horizontalX, direction.horizontalZ);
            Point side = horizontal < 0.001 ? new Point(width, 0, 0)
                    : new Point(direction.horizontalZ / horizontal * width, 0, -direction.horizontalX / horizontal * width);
            solid(first.add(side), second.add(side), second.add(side.scale(-1)), first.add(side.scale(-1)));
            Point up = new Point(0, width, 0);
            if (horizontal > 0.001) solid(first.add(up), second.add(up), second.add(up.scale(-1)), first.add(up.scale(-1)));
            else {
                Point cross = new Point(0, 0, width);
                solid(first.add(cross), second.add(cross), second.add(cross.scale(-1)), first.add(cross.scale(-1)));
            }
        }

        private void glyph(Point center, double radius, double turn, int tile, boolean vertical) {
            Point[] points = new Point[4];
            double[][] corners = {{-1, -1}, {-1, 1}, {1, 1}, {1, -1}};
            for (int index = 0; index < 4; index++) {
                double across = corners[index][0] * radius;
                double depth = corners[index][1] * radius;
                double rotatedX = across * Math.cos(turn) - depth * Math.sin(turn);
                double rotatedZ = across * Math.sin(turn) + depth * Math.cos(turn);
                points[index] = center.add(vertical ? new Point(rotatedX, rotatedZ, 0) : new Point(rotatedX, 0, rotatedZ));
            }
            quad(points[0], points[1], points[2], points[3], 0xFFFFFF, alpha * 0.75F, tile);
        }

        private void ring(Point center, double radius, double width, int axis, double start, double arc, int steps) {
            Point previous = null;
            for (int step = 0; step <= steps; step++) {
                double angle = start + arc * step / steps;
                double across = Math.cos(angle) * radius;
                double depth = Math.sin(angle) * radius;
                Point next = center.add(axis == 0 ? new Point(across, 0, depth) : axis == 1 ? new Point(0, across, depth) : new Point(across, depth, 0));
                if (previous != null) line(previous, next, width);
                previous = next;
            }
        }

        private void crystal(Point base, double width, double height, double turn) {
            Point bottom = base.add(new Point(0, -height * 0.15, 0));
            Point tip = base.add(new Point(0, height, 0));
            for (int face = 0; face < 4; face++) {
                Point left = base.add(polar(width, turn + face * Math.PI / 2, height * 0.3));
                Point right = base.add(polar(width, turn + (face + 1) * Math.PI / 2, height * 0.3));
                solid(left, tip, right, right);
                solid(left, right, bottom, bottom);
            }
        }

        private void dome(Point center, double radius, double height, double turn) {
            Point tip = center.add(new Point(0, height, 0));
            for (int face = 0; face < 12; face++) {
                Point left = center.add(polar(radius, turn + face * Math.PI / 6, 0));
                Point right = center.add(polar(radius, turn + (face + 1) * Math.PI / 6, 0));
                solid(left, tip, right, right);
            }
        }
    }
}
