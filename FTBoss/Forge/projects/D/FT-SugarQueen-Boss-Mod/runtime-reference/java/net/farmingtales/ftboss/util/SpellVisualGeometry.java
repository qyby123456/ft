/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.QizaiGeometry;
import net.farmingtales.ftboss.util.QizaiRoseStarfallModel;
import net.farmingtales.ftboss.util.RemainingSpellModels;
import net.farmingtales.ftboss.util.SpellVisualRules;

public final class SpellVisualGeometry {
    private SpellVisualGeometry() {
    }

    public static int draw(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase, double age, float opacity, double length, Sink output) {
        return SpellVisualGeometry.draw(spell, phase, age, opacity, length, false, output);
    }

    public static int draw(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase, double age, float opacity, double length, boolean nativeLight, Sink output) {
        if (!Double.isFinite(age) || age < 0.0 || !Float.isFinite(opacity) || opacity <= 0.0f || !Double.isFinite(length) || length < 0.0 || length > 64.0) {
            return 0;
        }
        Mesh mesh = new Mesh(output, spell.color, Math.min(1.0f, opacity) * 0.75f);
        double turn = age * 0.045;
        double radius = spell.radius;
        double swell = Math.min(1.0, (age + 2.0) / 8.0);
        if (phase == SpellVisualRules.Phase.BEAM) {
            SpellVisualGeometry.beam(mesh, spell, age, length, nativeLight);
            return mesh.count;
        }
        if (spell == SpellVisualRules.Spell.STAR_CROSS || spell == SpellVisualRules.Spell.ROSE_STARFALL) {
            boolean burst = phase != SpellVisualRules.Phase.CHARGE;
            double progress = Math.min(1.0, age / (double)spell.lifetime(phase));
            QizaiGeometry.Sink sink = (first, second, third, fourth, color, alpha) -> {
                if (alpha > 0.0f) {
                    mesh.quad(SpellVisualGeometry.qizaiPoint(first), SpellVisualGeometry.qizaiPoint(second), SpellVisualGeometry.qizaiPoint(third), SpellVisualGeometry.qizaiPoint(fourth), color, alpha * opacity, 255);
                }
            };
            if (spell == SpellVisualRules.Spell.STAR_CROSS) {
                QizaiGeometry.cross(progress, burst, sink);
            } else {
                QizaiGeometry.sigil(radius, progress, burst, false, sink);
                mesh.crystal(new Point(0.0, burst ? 0.4 : 1.2 + (1.0 - progress) * 4.0, 0.0), 0.35, burst ? 0.6 * (1.0 - progress) : 0.8, turn);
                for (int index = 0; index < 5; ++index) {
                    double angle = (double)index * Math.PI * 2.0 / 5.0 + turn;
                    mesh.crystal(SpellVisualGeometry.polar(burst ? radius * progress : radius * 0.6, angle, burst ? 0.3 + progress : 1.5), burst ? 0.15 : 0.1, burst ? 0.5 * (1.0 - progress) : 0.25, angle);
                }
                mesh.line(new Point(0.0, 0.1, 0.0), new Point(0.0, burst ? 3.0 * (1.0 - progress) : 5.0, 0.0), burst ? 0.06 : 0.014);
            }
            mesh.glyph(new Point(0.0, 0.045, 0.0), spell == SpellVisualRules.Spell.STAR_CROSS ? 0.65 : radius * 0.75, 0.0, spell.ordinal(), false);
            return mesh.count;
        }
        mesh.glyph(new Point(0.0, 0.045, 0.0), radius * (phase == SpellVisualRules.Phase.CHARGE ? 0.9 : 1.0), turn * 0.3, spell.ordinal(), false);
        if (RemainingSpellModels.renders(spell, phase)) {
            mesh.ring(new Point(0.0, 0.08, 0.0), radius, phase == SpellVisualRules.Phase.CHARGE ? 0.035 : 0.05, 0, 0.0, Math.PI * 2, 48);
            return mesh.count;
        }
        if (spell.ordinal() >= SpellVisualRules.Spell.EFFIGY_PROCESSION.ordinal()) {
            SpellVisualGeometry.additional(mesh, spell, phase, age, turn, swell);
            return mesh.count;
        }
        if (phase == SpellVisualRules.Phase.CHARGE) {
            mesh.ring(new Point(0.0, 0.08, 0.0), radius, 0.035, 0, 0.0, Math.PI * 2, 48);
            for (int index = 0; index < 6; ++index) {
                double angle = turn + (double)index * Math.PI / 3.0;
                Point point = SpellVisualGeometry.polar(radius * 0.85, angle, 0.25 + (age * 0.035 + (double)index * 0.18) % 1.4);
                mesh.crystal(point, 0.07, 0.18, angle);
            }
            mesh.glyph(new Point(0.0, 0.8, 0.0), Math.min(0.65, radius / 2.0), -turn, spell.ordinal(), true);
            return mesh.count;
        }
        switch (spell.form) {
            case CROWN: {
                mesh.ring(new Point(0.0, 0.25, 0.0), radius * 0.7, 0.12, 0, 0.0, Math.PI * 2, 40);
                for (int index = 0; index < 10; ++index) {
                    double angle = (double)index * Math.PI / 5.0;
                    mesh.crystal(SpellVisualGeometry.polar(radius * 0.7, angle, 0.35), 0.26, (index % 2 == 0 ? 2.6 : 1.5) * swell, angle);
                }
                mesh.ring(new Point(0.0, 0.12, 0.0), radius * swell, 0.06, 0, 0.0, Math.PI * 2, 48);
                break;
            }
            case FLOWER: 
            case GARDEN: {
                int index;
                int petals = spell == SpellVisualRules.Spell.HALO_SANCTUARY ? 12 : (spell == SpellVisualRules.Spell.FLESH_BLOOM ? 6 : 8);
                for (index = 0; index < petals; ++index) {
                    double angle = (double)index * Math.PI * 2.0 / (double)petals + turn * 0.4;
                    SpellVisualGeometry.petal(mesh, angle, radius * swell, 0.25, 1.2 + Math.sin(turn) * 0.3);
                }
                mesh.ring(new Point(0.0, 0.2, 0.0), radius * 0.7, 0.06, 0, 0.0, Math.PI * 2, 48);
                if (spell.form == SpellVisualRules.Form.GARDEN) {
                    for (index = 0; index < 4; ++index) {
                        mesh.ring(new Point(0.0, 0.1, 0.0), radius * 0.8, 0.025, 1 + index % 2, turn + (double)index * 0.3, Math.PI, 24);
                    }
                    SpellVisualGeometry.motes(mesh, radius, 2.8, age, 12);
                    break;
                }
                mesh.crystal(new Point(0.0, 0.5, 0.0), 0.4, 1.1 * swell, turn);
                break;
            }
            case VORTEX: {
                for (int arm = 0; arm < 3; ++arm) {
                    Point previous = SpellVisualGeometry.polar(radius, turn + (double)arm * Math.PI * 2.0 / 3.0, 0.1);
                    for (int step = 1; step <= 40; ++step) {
                        double progress = (double)step / 40.0;
                        Point next = SpellVisualGeometry.polar(radius * (1.0 - progress * 0.75), turn + (double)arm * Math.PI * 2.0 / 3.0 + progress * Math.PI * 4.0, 0.1 + progress * 3.0);
                        mesh.line(previous, next, 0.045 + 0.025 * progress);
                        previous = next;
                    }
                }
                if (spell == SpellVisualRules.Spell.VINTAGE_VORTEX) {
                    SpellVisualGeometry.barrel(mesh, new Point(0.0, 1.5, 0.0), 0.45, turn);
                    break;
                }
                if (spell == SpellVisualRules.Spell.ROTTING_REQUIEM) {
                    SpellVisualGeometry.motes(mesh, radius, 2.5, age, 18);
                    break;
                }
                mesh.ring(new Point(0.0, 0.1, 0.0), radius * 0.6, 0.09, 0, -turn, Math.PI * 2, 40);
                break;
            }
            case CORE: {
                mesh.crystal(new Point(0.0, 1.2, 0.0), 0.4, 0.9, turn);
                for (int axis = 0; axis < 3; ++axis) {
                    mesh.ring(new Point(0.0, 1.2, 0.0), 0.9, 0.06, axis, turn, Math.PI * 2, 32);
                }
                break;
            }
            case SPORES: {
                for (int index = 0; index < 7; ++index) {
                    Point base = SpellVisualGeometry.polar(0.7 + (double)(index % 3), (double)index * 2.4, 0.1);
                    if (spell == SpellVisualRules.Spell.SCARLET_POLLEN) {
                        mesh.crystal(base.add(new Point(0.0, 0.7 * swell, 0.0)), 0.18, 0.35, turn + (double)index);
                        SpellVisualGeometry.petal(mesh, (double)index * 2.4 + turn, radius * 0.7, 0.5, 0.6);
                        continue;
                    }
                    mesh.line(base, base.add(new Point(0.0, 0.7 * swell, 0.0)), 0.08);
                    mesh.dome(base.add(new Point(0.0, 0.7 * swell, 0.0)), 0.45, 0.3, turn);
                }
                mesh.ring(new Point(0.0, 0.12, 0.0), radius * swell, 0.08, 0, 0.0, Math.PI * 2, 40);
                SpellVisualGeometry.motes(mesh, radius, 2.0, age, 18);
                break;
            }
            case HALO: {
                for (int band = 0; band < 3; ++band) {
                    mesh.ring(new Point(0.0, 0.3 + (double)band * 0.7, 0.0), radius * swell * (1.0 - (double)band * 0.17), 0.055, 0, turn, Math.PI * 2, 48);
                }
                for (int index = 0; index < 12; ++index) {
                    Point base = SpellVisualGeometry.polar(radius * 0.85 * swell, (double)index * Math.PI / 6.0, 0.15);
                    mesh.line(base, base.add(new Point(0.0, index % 2 == 0 ? 2.2 : 0.8, 0.0)), 0.035);
                }
                break;
            }
            case WAVE: 
            case TRIDENT: {
                double distance = (double)(spell.form == SpellVisualRules.Form.WAVE ? 12 : 10) * Math.min(1.0, age / 12.0);
                if (spell == SpellVisualRules.Spell.RIPTIDE_CORRIDOR) {
                    distance = 1.5;
                }
                if (spell == SpellVisualRules.Spell.CINDER_BREATH) {
                    distance = 10.0 * Math.min(1.0, age / 8.0);
                }
                for (int band = 0; band < (spell.form == SpellVisualRules.Form.WAVE ? 3 : 4); ++band) {
                    double forward = Math.max(0.5, distance - (double)band * 1.1);
                    double width = 1.2 + forward * 0.45;
                    Point previous = new Point(-width, 0.2, forward);
                    for (int step = 1; step <= 24; ++step) {
                        double across = (double)step / 24.0;
                        Point next = new Point((across * 2.0 - 1.0) * width, 0.25 + Math.sin(across * Math.PI) * (1.5 - (double)band * 0.2), forward);
                        mesh.line(previous, next, 0.09);
                        previous = next;
                    }
                }
                if (spell.form != SpellVisualRules.Form.TRIDENT) break;
                for (int fork = -1; fork <= 1; ++fork) {
                    mesh.line(new Point((double)fork * 0.55, 1.2, distance - 1.0), new Point((double)fork * 0.55, 1.2, distance + 0.5), 0.09);
                    mesh.crystal(new Point((double)fork * 0.55, 1.1, distance + 0.5), 0.16, 0.5, turn);
                }
                break;
            }
            case LANCE: 
            case NEEDLES: 
            case PRESSURE: 
            case LASER: 
            case FEATHERS: {
                mesh.ring(new Point(0.0, 1.1, 0.0), 0.7 * swell, 0.075, 2, turn, Math.PI * 2, 32);
                for (int index = 0; index < 4; ++index) {
                    mesh.crystal(SpellVisualGeometry.polar(0.9, turn + (double)index * Math.PI / 2.0, 1.2), 0.12, 0.45, turn);
                }
                break;
            }
            case BARREL: 
            case SHELL: 
            case GEYSER: {
                if (phase != SpellVisualRules.Phase.PULSE) {
                    mesh.ring(new Point(0.0, 0.12, 0.0), radius * (0.95 - 0.25 * swell), 0.07, 0, turn, Math.PI * 2, 48);
                    if (spell.form == SpellVisualRules.Form.BARREL) {
                        SpellVisualGeometry.barrel(mesh, new Point(0.0, 0.7 + Math.sin(turn * 3.0) * 0.15, 0.0), 0.8, turn);
                        break;
                    }
                    if (spell.form == SpellVisualRules.Form.SHELL) {
                        mesh.crystal(new Point(0.0, 0.4, 0.0), 0.45, 1.2, turn);
                        break;
                    }
                    for (int index = 0; index < 3; ++index) {
                        mesh.ring(new Point(0.0, 0.2 + (double)index * 0.15, 0.0), 0.4 + (double)index * 0.6, 0.06, 0, turn, 5.026548245743669, 32);
                    }
                } else {
                    mesh.ring(new Point(0.0, 0.12, 0.0), radius * swell, 0.13, 0, 0.0, Math.PI * 2, 48);
                    if (spell.form == SpellVisualRules.Form.GEYSER) {
                        for (int stream = 0; stream < 8; ++stream) {
                            Point previous = SpellVisualGeometry.polar(0.6, (double)stream * Math.PI / 4.0, 0.15);
                            for (int step = 1; step <= 16; ++step) {
                                double height = (double)step / 16.0;
                                Point next = SpellVisualGeometry.polar(0.6 + height * height, (double)stream * Math.PI / 4.0 + height + turn, height * 5.0 * swell);
                                mesh.line(previous, next, 0.11 - height * 0.05);
                                previous = next;
                            }
                        }
                    } else {
                        for (int index = 0; index < 14; ++index) {
                            mesh.crystal(SpellVisualGeometry.polar(radius * swell * 0.8, (double)index * 2.4, 0.2 + Math.sin(Math.min(1.0, age / 24.0) * Math.PI) * (double)(1 + index % 3)), 0.14, 0.5, turn + (double)index);
                        }
                    }
                }
                break;
            }
            case REPULSOR: {
                for (int axis = 0; axis < 3; ++axis) {
                    mesh.ring(new Point(0.0, 0.5, 0.0), radius * swell, 0.065, axis, turn, Math.PI * 2, 48);
                }
                for (int index = 0; index < 10; ++index) {
                    mesh.line(new Point(0.0, 0.6, 0.0), SpellVisualGeometry.polar(radius * swell, (double)index * Math.PI / 5.0, 0.6), 0.025);
                }
                break;
            }
            case REAP: 
            case CLEAVE: 
            case PAN: {
                if (spell == SpellVisualRules.Spell.BRIAR_PROCESSION) {
                    for (int index = 0; index < 9; ++index) {
                        Point foot = SpellVisualGeometry.polar(radius * (0.3 + (double)(index % 3) * 0.25), (double)index * 2.4, 0.15);
                        mesh.crystal(foot, 0.16, (0.7 + (double)(index % 3) * 0.6) * swell, turn);
                        mesh.line(foot, foot.add(SpellVisualGeometry.polar(0.5, (double)index * 2.4, 1.1)), 0.055);
                    }
                    break;
                }
                if (spell == SpellVisualRules.Spell.VIRULENT_HARVEST || spell == SpellVisualRules.Spell.GOLDEN_HARVEST) {
                    for (int index = 0; index < 5; ++index) {
                        mesh.ring(SpellVisualGeometry.polar(radius * (1.0 - Math.min(0.8, age / 32.0)), (double)index * Math.PI * 0.4 + turn, 0.8), 0.65, 0.12, 0, turn, 4.084070449666731, 18);
                    }
                    break;
                }
                for (int index = 0; index < 3; ++index) {
                    mesh.ring(new Point(0.0, 0.6 + (double)index * 0.35, 0.0), radius * (0.65 + (double)index * 0.3) * swell, spell.form == SpellVisualRules.Form.CLEAVE ? 0.22 : 0.13, 0, 0.37699111843077515, 2.3876104167282426, 28);
                }
                if (spell.form != SpellVisualRules.Form.PAN) break;
                mesh.ring(new Point(0.0, 1.5, 2.0), 0.8, 0.18, 2, turn, Math.PI * 2, 24);
                mesh.line(new Point(0.0, 0.8, 2.0), new Point(0.0, -0.3, 2.0), 0.12);
                SpellVisualGeometry.motes(mesh, 3.0, 2.0, age, 10);
                break;
            }
            case SHIELD: {
                int plates = spell == SpellVisualRules.Spell.ORCHARD_AEGIS ? 6 : 4;
                for (int index = 0; index < plates; ++index) {
                    double angle = turn * 0.5 + (double)index * Math.PI * 2.0 / (double)plates;
                    Point point = SpellVisualGeometry.polar(radius, angle, 1.1);
                    mesh.glyph(point, 0.48, angle, spell.ordinal(), true);
                    mesh.crystal(point.add(new Point(0.0, -0.55, 0.0)), 0.22, 1.1, angle);
                }
                mesh.ring(new Point(0.0, 0.18, 0.0), radius, 0.04, 0, turn, Math.PI * 2, 40);
                break;
            }
            case DOLL: {
                SpellVisualGeometry.doll(mesh, new Point(0.0, 1.4, 0.0), 0.65, turn);
                break;
            }
            case CANDLES: {
                for (int index = 0; index < 8; ++index) {
                    Point foot = SpellVisualGeometry.polar(radius * 0.85, (double)index * Math.PI / 4.0, 0.1);
                    mesh.line(foot, foot.add(new Point(0.0, 1.4, 0.0)), 0.13);
                    mesh.crystal(foot.add(new Point(0.0, 1.45, 0.0)), 0.14, 0.55 + Math.sin(turn * 4.0 + (double)index) * 0.1, turn);
                    mesh.line(foot.add(new Point(0.0, 0.6, 0.0)), SpellVisualGeometry.polar(radius * 0.85, (double)(index + 1) * Math.PI / 4.0, 0.7), 0.04);
                }
                mesh.ring(new Point(0.0, 2.2, 0.0), radius * 0.8, 0.035, 0, turn, Math.PI * 2, 40);
                break;
            }
            case OATH: {
                for (int index = 0; index < 3; ++index) {
                    mesh.ring(new Point(0.0, 0.3 + (double)index * 0.55, 0.0), 1.2, 0.08, 0, turn + (double)(index * 2), 4.39822971502571, 32);
                    mesh.glyph(SpellVisualGeometry.polar(1.2, turn + (double)index * Math.PI * 2.0 / 3.0, 1.4), 0.38, -turn, spell.ordinal(), true);
                }
                break;
            }
            case HEARTH: {
                mesh.ring(new Point(0.0, 0.2, 0.0), 1.2, 0.18, 0, 0.0, Math.PI * 2, 32);
                for (int index = 0; index < 7; ++index) {
                    mesh.crystal(SpellVisualGeometry.polar(0.55, (double)index * Math.PI * 2.0 / 7.0, 0.2), 0.22, 0.7 + Math.sin(turn * 4.0 + (double)index) * 0.3, turn);
                }
                mesh.ring(new Point(0.0, 0.1, 0.0), radius * swell, 0.06, 0, 0.0, Math.PI * 2, 48);
                SpellVisualGeometry.motes(mesh, radius, 2.0, age, 12);
                break;
            }
            case RAIN: 
            case BLIZZARD: {
                mesh.ring(new Point(0.0, 3.3, 0.0), radius * 0.8, 0.055, 0, -turn, Math.PI * 2, 48);
                for (int index = 0; index < 24; ++index) {
                    double height = 3.2 - (age * 0.16 + (double)index * 0.31) % 3.0;
                    Point point = SpellVisualGeometry.polar(radius * (0.2 + (double)index * 0.37 % 0.75), (double)index * 2.4 + (spell.form == SpellVisualRules.Form.BLIZZARD ? turn : 0.0), height);
                    mesh.line(point, point.add(new Point(spell.form == SpellVisualRules.Form.BLIZZARD ? 0.24 : 0.0, 0.3, 0.0)), 0.025);
                    if (index % 3 != 0) continue;
                    mesh.crystal(point, 0.07, 0.18, turn);
                }
                break;
            }
            case GEARS: {
                for (int index = 0; index < 3; ++index) {
                    Point point = SpellVisualGeometry.polar(radius * 0.7, turn + (double)index * Math.PI * 2.0 / 3.0, 0.9 + (double)index * 0.25);
                    mesh.ring(point, 0.65, 0.14, 0, -turn, Math.PI * 2, 32);
                    for (int tooth = 0; tooth < 10; ++tooth) {
                        double angle = (double)tooth * Math.PI / 5.0 - turn * 3.0;
                        mesh.line(point.add(SpellVisualGeometry.polar(0.5, angle, 0.0)), point.add(SpellVisualGeometry.polar(0.85, angle, 0.0)), 0.12);
                    }
                }
                break;
            }
            case BRAND: 
            case VOID: {
                int index;
                for (index = 0; index < 3; ++index) {
                    mesh.ring(new Point(0.0, 0.1 + (double)index * 0.6, 0.0), radius * (1.0 - (double)index * 0.18), 0.05, 0, -turn + (double)index, 5.340707511102648, 40);
                }
                mesh.glyph(new Point(0.0, 1.2, 0.0), 1.0, -turn, spell.ordinal(), true);
                if (spell.form == SpellVisualRules.Form.BRAND) {
                    for (index = 0; index < 6; ++index) {
                        mesh.crystal(SpellVisualGeometry.polar(radius * 0.8, (double)index * Math.PI / 3.0, 0.1), 0.13, 1.6, turn);
                    }
                } else {
                    for (int axis = 1; axis <= 2; ++axis) {
                        mesh.ring(new Point(0.0, 1.3, 0.0), 1.1 * swell, 0.07, axis, turn, Math.PI * 2, 40);
                    }
                }
                break;
            }
            case WINGS: {
                for (int side : new int[]{-1, 1}) {
                    for (int feather = 0; feather < 7; ++feather) {
                        Point root = new Point((double)side * 0.15, 1.3, -0.3);
                        Point tip = new Point((double)side * (0.9 + (double)feather * 0.22), 2.2 - (double)feather * 0.18 + Math.sin(turn * 2.0) * 0.2, -0.5);
                        mesh.line(root, tip, 0.09);
                        mesh.line(tip, tip.add(new Point((double)(-side) * 0.15, -0.45, -0.1)), 0.11);
                    }
                }
                break;
            }
        }
        return mesh.count;
    }

    private static void beam(Mesh mesh, SpellVisualRules.Spell spell, double age, double length, boolean nativeLight) {
        if (spell == SpellVisualRules.Spell.ROSE_STARFALL) {
            QizaiRoseStarfallModel.beam(age, length, false, 1.0f, (first, second, third, fourth, color, alpha) -> mesh.quad(SpellVisualGeometry.rosePoint(first), SpellVisualGeometry.rosePoint(second), SpellVisualGeometry.rosePoint(third), SpellVisualGeometry.rosePoint(fourth), color, alpha * mesh.alpha, 255));
            return;
        }
        Point start = new Point(0.0, 0.0, 0.0);
        Point end = new Point(0.0, 0.0, length);
        double head = Math.min(1.0, (age + 1.0) / 5.0) * length;
        if (spell == SpellVisualRules.Spell.ARCANE_FUSILLADE) {
            SpellVisualGeometry.doraFocusLaser(mesh, age, length);
            return;
        }
        if (spell == SpellVisualRules.Spell.CAPACITOR_OVERLOAD) {
            if (nativeLight) {
                return;
            }
            Point previous = start;
            for (int step = 1; step <= 14; ++step) {
                Point next = step == 14 ? end : new Point(Math.sin((double)step * 14.7 + Math.floor(age / 2.0)) * 0.22, Math.cos((double)step * 7.3) * 0.22, length * (double)step / 14.0);
                mesh.line(previous, next, 0.055);
                if (step % 3 == 0) {
                    mesh.line(next, next.add(new Point(0.35, 0.4, 0.2)), 0.022);
                }
                previous = next;
            }
            return;
        }
        if (!nativeLight) {
            mesh.line(start, end, spell.form == SpellVisualRules.Form.PRESSURE ? 0.11 : 0.035);
        }
        switch (spell.form) {
            case DOLL: {
                SpellVisualGeometry.doll(mesh, new Point(0.0, 0.0, head), 0.5, age * 0.1);
                break;
            }
            case FEATHERS: {
                for (int index = 0; index < 6; ++index) {
                    double offset = (double)index * 0.13;
                    mesh.line(new Point(0.0, 0.0, head - offset), new Point(0.23, 0.0, head - offset - 0.25), 0.04);
                    mesh.line(new Point(0.0, 0.0, head - offset), new Point(-0.23, 0.0, head - offset - 0.25), 0.04);
                }
                break;
            }
            case PRESSURE: {
                for (int index = 1; index <= 8; ++index) {
                    mesh.ring(new Point(0.0, 0.0, length * (double)index / 9.0), 0.35, 0.04, 2, age * 0.1, Math.PI * 2, 16);
                }
                break;
            }
            case LANCE: 
            case NEEDLES: {
                mesh.line(new Point(0.0, 0.0, Math.max(0.0, head - 2.0)), new Point(0.0, 0.0, head), 0.16);
                Point tip = new Point(0.0, 0.0, head + 0.2);
                mesh.solid(new Point(-0.28, 0.0, head - 0.7), tip, new Point(0.28, 0.0, head - 0.7), tip);
                mesh.solid(new Point(0.0, -0.28, head - 0.7), tip, new Point(0.0, 0.28, head - 0.7), tip);
                break;
            }
            default: {
                mesh.ring(new Point(0.0, 0.0, head), 0.24, 0.045, 2, age * 0.1, Math.PI * 2, 16);
                mesh.line(new Point(0.0, 0.0, Math.max(0.0, head - 0.8)), new Point(0.0, 0.0, head), 0.09);
            }
        }
        mesh.ring(end, 0.2 + Math.min(0.5, age * 0.06), 0.04, 2, age * 0.15, Math.PI * 2, 20);
    }

    private static void doraFocusLaser(Mesh mesh, double age, double length) {
        if (length < 0.015) {
            return;
        }
        double pulse = 1.0 + Math.sin(age * 1.35) * 0.1;
        double outer = 0.15 * pulse;
        double middle = 0.072 * pulse;
        double core = 0.024 * pulse;
        Point start = new Point(0.0, 0.0, 0.0);
        Point end = new Point(0.0, 0.0, length);
        SpellVisualGeometry.laserRibbon(mesh, start, end, outer, 3788741, mesh.alpha * 0.28f);
        SpellVisualGeometry.laserRibbon(mesh, start, end, middle, 0x9FFFF0, mesh.alpha * 0.76f);
        SpellVisualGeometry.laserRibbon(mesh, start, end, core, 0xFFFFFF, Math.min(1.0f, mesh.alpha * 1.28f));
        int gates = length < 1.0 ? 2 : 6;
        double ringRadius = 0.19 * Math.min(1.0, 0.35 + length / 2.5);
        for (int gate = 0; gate < gates; ++gate) {
            double along = ((double)gate + 0.32 + age * 0.24) / (double)gates;
            along -= Math.floor(along);
            mesh.ring(new Point(0.0, 0.0, length * along), ringRadius, Math.max(0.012, ringRadius * 0.16), 2, age * 0.16 + (double)gate * 0.53, Math.PI * 2, 12);
        }
        mesh.ring(new Point(0.0, 0.0, length), ringRadius * 1.28, Math.max(0.016, ringRadius * 0.18), 2, -age * 0.24, Math.PI * 2, 16);
    }

    private static void laserRibbon(Mesh mesh, Point start, Point end, double width, int color, float alpha) {
        Point horizontal = new Point(width, 0.0, 0.0);
        Point vertical = new Point(0.0, width, 0.0);
        mesh.quad(start.add(horizontal.scale(-1.0)), end.add(horizontal.scale(-1.0)), end.add(horizontal), start.add(horizontal), color, alpha, 255);
        mesh.quad(start.add(vertical.scale(-1.0)), end.add(vertical.scale(-1.0)), end.add(vertical), start.add(vertical), color, alpha, 255);
    }

    private static void petal(Mesh mesh, double angle, double radius, double height, double rise) {
        Point root = new Point(0.0, height, 0.0);
        Point tip = SpellVisualGeometry.polar(radius, angle, height + 0.2);
        Point left = SpellVisualGeometry.polar(radius * 0.62, angle - 0.25, height + rise);
        Point right = SpellVisualGeometry.polar(radius * 0.62, angle + 0.25, height + rise);
        mesh.solid(root, left, tip, right);
        mesh.line(root, left, 0.025);
        mesh.line(left, tip, 0.025);
        mesh.line(tip, right, 0.025);
    }

    private static void motes(Mesh mesh, double radius, double height, double age, int count) {
        for (int index = 0; index < count; ++index) {
            mesh.crystal(SpellVisualGeometry.polar(radius * (0.3 + (double)index * 0.31 % 0.6), (double)index * 2.4 + age * 0.02, 0.2 + ((double)index * 0.37 + age * 0.04) % height), 0.045, 0.14, age * 0.03);
        }
    }

    private static void doll(Mesh mesh, Point center, double scale, double turn) {
        mesh.crystal(center.add(new Point(0.0, scale * 0.5, 0.0)), scale * 0.32, scale * 0.45, turn);
        mesh.line(center.add(new Point(0.0, scale * 0.4, 0.0)), center.add(new Point(0.0, -scale * 0.4, 0.0)), scale * 0.17);
        for (int side : new int[]{-1, 1}) {
            mesh.line(center, center.add(new Point((double)side * scale * 0.65, scale * 0.15, 0.0)), scale * 0.1);
            mesh.line(center.add(new Point(0.0, -scale * 0.3, 0.0)), center.add(new Point((double)side * scale * 0.35, -scale * 0.9, 0.0)), scale * 0.1);
        }
    }

    private static void barrel(Mesh mesh, Point center, double scale, double turn) {
        for (int index = 0; index < 10; ++index) {
            double angle = (double)index * Math.PI / 5.0 + turn;
            mesh.line(center.add(SpellVisualGeometry.polar(scale * 0.7, angle, -scale)), center.add(SpellVisualGeometry.polar(scale, angle, 0.0)), 0.075);
            mesh.line(center.add(SpellVisualGeometry.polar(scale, angle, 0.0)), center.add(SpellVisualGeometry.polar(scale * 0.7, angle, scale)), 0.075);
        }
        for (int hoop = -1; hoop <= 1; ++hoop) {
            mesh.ring(center.add(new Point(0.0, (double)hoop * scale * 0.75, 0.0)), scale * (hoop == 0 ? 1.0 : 0.8), 0.085, 0, turn, Math.PI * 2, 24);
        }
    }

    private static void additional(Mesh mesh, SpellVisualRules.Spell spell, SpellVisualRules.Phase phase, double age, double turn, double swell) {
        double radius = spell.radius;
        boolean impact = phase == SpellVisualRules.Phase.PULSE;
        double lift = impact ? 0.0 : 2.8;
        mesh.ring(new Point(0.0, 0.09, 0.0), radius, 0.04, 0, turn * 0.2, Math.PI * 2, 48);
        switch (spell.form) {
            case PROCESSION: {
                for (int index = 0; index < 3; ++index) {
                    double angle = turn + (double)index * Math.PI * 2.0 / 3.0;
                    Point center = SpellVisualGeometry.polar(radius * 0.65, angle, 1.2 + Math.sin(turn * 2.0 + (double)index) * 0.2);
                    SpellVisualGeometry.doll(mesh, center, 0.65, angle);
                    mesh.line(center.add(new Point(0.0, 0.8, 0.0)), new Point(0.0, 3.5, 0.0), 0.025);
                    if (!impact) continue;
                    mesh.line(center, new Point(0.0, 0.5, 0.0), 0.06 * (1.0 - Math.min(0.8, age / 20.0)));
                }
                mesh.crystal(new Point(0.0, 3.4, 0.0), 0.3, 0.6, turn);
                break;
            }
            case ECLIPSE: {
                mesh.ring(new Point(0.0, 2.5, 0.0), 1.15, 0.16, 2, turn, 5.183627878423159, 40);
                mesh.ring(new Point(0.25, 2.6, 0.0), 0.9, 0.06, 2, -turn, 5.026548245743669, 36);
                for (int index = 0; index < 6; ++index) {
                    Point base = SpellVisualGeometry.polar(radius * 0.8, (double)index * Math.PI / 3.0, 0.15);
                    mesh.line(base, base.add(new Point(0.0, 1.0, 0.0)), 0.11);
                    mesh.crystal(base.add(new Point(0.0, 1.1, 0.0)), 0.16, 0.45, turn);
                    if (!impact) continue;
                    mesh.line(base.add(new Point(0.0, 1.1, 0.0)), new Point(0.0, 2.5, 0.0), 0.07);
                }
                break;
            }
            case BATTERY: {
                for (int index = 0; index < 3; ++index) {
                    double angle = (double)index * Math.PI * 2.0 / 3.0;
                    Point core = SpellVisualGeometry.polar(2.5, angle, 1.8);
                    mesh.crystal(core, 0.45, 1.2, turn);
                    mesh.ring(core.add(new Point(0.0, 0.35, 0.0)), 0.7, 0.07, 0, -turn, Math.PI * 2, 24);
                    for (int leg = 0; leg < 3; ++leg) {
                        mesh.line(core, core.add(SpellVisualGeometry.polar(0.65, angle + (double)leg * Math.PI * 2.0 / 3.0, -1.65)), 0.06);
                    }
                    if (!impact) continue;
                    mesh.line(core.add(new Point(0.0, 0.3, 0.0)), new Point(0.0, 0.5, 0.0), 0.1);
                }
                break;
            }
            case VERDICT: {
                Point tip = new Point(0.0, 0.2 + lift, 0.0);
                mesh.crystal(tip.add(new Point(0.0, 2.2, 0.0)), 0.38, -2.2, 0.0);
                mesh.line(tip.add(new Point(-0.85, 2.2, 0.0)), tip.add(new Point(0.85, 2.2, 0.0)), 0.13);
                mesh.line(tip.add(new Point(0.0, 2.2, 0.0)), tip.add(new Point(0.0, 3.2, 0.0)), 0.1);
                mesh.crystal(tip.add(new Point(0.0, 3.25, 0.0)), 0.18, 0.35, turn);
                if (!impact) break;
                for (int index = 0; index < 8; ++index) {
                    mesh.line(new Point(0.0, 0.15, 0.0), SpellVisualGeometry.polar(radius * swell, (double)index * Math.PI / 4.0, 0.15), 0.05);
                }
                break;
            }
            case CITRUS: {
                double size = impact ? radius * swell : 1.25;
                mesh.ring(new Point(0.0, impact ? 0.4 : 1.6, 0.0), size, 0.15, impact ? 0 : 2, turn, Math.PI * 2, 40);
                for (int index = 0; index < 8; ++index) {
                    double angle = (double)index * Math.PI / 4.0 + turn;
                    Point center = new Point(0.0, impact ? 0.4 : 1.6, 0.0);
                    Point edge = impact ? SpellVisualGeometry.polar(size * 0.85, angle, 0.0) : new Point(Math.cos(angle) * size * 0.85, Math.sin(angle) * size * 0.85, 0.0);
                    mesh.line(center, center.add(edge), 0.06);
                    if (!impact) continue;
                    mesh.crystal(center.add(edge), 0.18, 0.9, angle);
                }
                mesh.line(new Point(0.8, 2.7, 0.0), new Point(1.7, 3.1, 0.0), 0.18);
                break;
            }
            case LOTUS: {
                int index;
                for (index = 0; index < 10; ++index) {
                    SpellVisualGeometry.petal(mesh, (double)index * Math.PI / 5.0 + turn * 0.2, radius * 0.8, 0.25, 1.4);
                }
                mesh.crystal(new Point(0.0, 0.6, 0.0), 0.3, 1.0, turn);
                for (index = 0; index < 3; ++index) {
                    mesh.ring(new Point(0.0, 0.2 + (double)index * 0.3, 0.0), radius * (0.8 - (double)index * 0.17), 0.05, 0, turn, Math.PI * 2, 40);
                }
                SpellVisualGeometry.motes(mesh, radius, 2.5, age, 12);
                break;
            }
            case MAGNET: {
                mesh.crystal(new Point(0.0, 1.5, 0.0), 0.45, 1.1, turn);
                for (int axis = 0; axis < 3; ++axis) {
                    mesh.ring(new Point(0.0, 1.8, 0.0), 1.2 + (double)axis * 0.25, 0.075, axis, turn * 2.0, 5.340707511102648, 36);
                }
                for (int index = 0; index < 6; ++index) {
                    Point edge = SpellVisualGeometry.polar(radius * 0.85, (double)index * Math.PI / 3.0 + turn, 0.3);
                    mesh.crystal(edge, 0.2, 0.65, turn);
                    if (!impact) continue;
                    Point kink = edge.scale(0.55).add(new Point(0.2 * Math.sin((double)index + age), 1.2, 0.0));
                    mesh.line(edge, kink, 0.065);
                    mesh.line(kink, new Point(0.0, 1.8, 0.0), 0.065);
                }
                break;
            }
            case NIGHTBLADES: {
                for (int index = 0; index < 4; ++index) {
                    double angle = (double)index * Math.PI / 2.0 + turn;
                    Point tip = SpellVisualGeometry.polar(impact ? 0.3 + Math.min(1.0, age / 12.0) * 0.6 : radius * 0.7, angle, 0.8);
                    Point hilt = tip.add(SpellVisualGeometry.polar(1.3, angle, 1.4));
                    mesh.line(tip, hilt, 0.18);
                    mesh.line(hilt.add(SpellVisualGeometry.polar(0.45, angle + 1.5707963267948966, 0.0)), hilt.add(SpellVisualGeometry.polar(0.45, angle - 1.5707963267948966, 0.0)), 0.08);
                    mesh.line(hilt, hilt.add(SpellVisualGeometry.polar(0.4, angle, 0.4)), 0.07);
                }
                mesh.ring(new Point(0.0, 1.3, 0.0), 1.2, 0.05, 2, -turn, Math.PI * 2, 32);
                break;
            }
            case SERAPH: {
                Point core = new Point(0.0, 0.2 + lift, 0.0);
                mesh.line(core, core.add(new Point(0.0, 2.8, 0.0)), 0.1);
                mesh.crystal(core.add(new Point(0.0, 0.65, 0.0)), 0.25, -0.65, 0.0);
                mesh.ring(core.add(new Point(0.0, 2.5, 0.0)), 0.85, 0.08, 0, turn, Math.PI * 2, 32);
                for (int side : new int[]{-1, 1}) {
                    for (int feather = 0; feather < 5; ++feather) {
                        Point root = core.add(new Point((double)side * 0.15, 1.8, 0.0));
                        Point tip = core.add(new Point((double)side * (1.0 + (double)feather * 0.26), 2.7 - (double)feather * 0.25, 0.3));
                        mesh.line(root, tip, 0.1);
                    }
                }
                if (!impact) break;
                mesh.ring(new Point(0.0, 0.25, 0.0), radius * swell, 0.1, 0, turn, Math.PI * 2, 48);
                break;
            }
            case CLOCK: {
                int index;
                Point center = new Point(0.0, 2.3, 0.0);
                mesh.ring(center, 1.7, 0.09, 2, 0.0, Math.PI * 2, 48);
                for (index = 0; index < 12; ++index) {
                    double angle = (double)index * Math.PI / 6.0;
                    mesh.line(center.add(new Point(Math.cos(angle) * 1.4, Math.sin(angle) * 1.4, 0.0)), center.add(new Point(Math.cos(angle) * 1.6, Math.sin(angle) * 1.6, 0.0)), 0.07);
                }
                mesh.line(center, center.add(new Point(Math.cos(-turn * 2.0) * 1.3, Math.sin(-turn * 2.0) * 1.3, 0.0)), 0.06);
                mesh.line(center, center.add(new Point(Math.cos(-turn * 0.3) * 0.8, Math.sin(-turn * 0.3) * 0.8, 0.02)), 0.09);
                for (index = 0; index < 6; ++index) {
                    mesh.crystal(SpellVisualGeometry.polar(radius * 0.85, (double)index * Math.PI / 3.0, 0.1), 0.22, impact ? 1.4 * swell : 0.5, turn);
                }
                break;
            }
            case REFUGE: {
                for (int index = 0; index < 10; ++index) {
                    double angle = (double)index * Math.PI / 5.0 + turn * 0.1;
                    Point foot = SpellVisualGeometry.polar(radius * 0.85, angle, 0.2);
                    Point shoulder = SpellVisualGeometry.polar(radius * 0.65, angle, 2.4);
                    Point tip = SpellVisualGeometry.polar(0.5, angle, 3.4);
                    mesh.line(foot, shoulder, 0.07);
                    mesh.line(shoulder, tip, 0.06);
                    for (int barb = 1; barb < 5; ++barb) {
                        Point base = foot.scale(1.0 - (double)barb / 5.0).add(shoulder.scale((double)barb / 5.0));
                        mesh.line(base, base.add(SpellVisualGeometry.polar(0.4, angle + 1.5707963267948966, 0.3)), 0.04);
                        mesh.line(base, base.add(SpellVisualGeometry.polar(0.4, angle - 1.5707963267948966, 0.3)), 0.04);
                    }
                }
                mesh.ring(new Point(0.0, 3.5, 0.0), 0.55, 0.06, 0, turn, Math.PI * 2, 24);
                break;
            }
        }
    }

    private static Point polar(double radius, double angle, double height) {
        return new Point(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }

    private static Point qizaiPoint(QizaiGeometry.Point point) {
        return new Point(point.x(), point.y(), point.z());
    }

    private static Point rosePoint(QizaiRoseStarfallModel.Point point) {
        return new Point(point.x(), point.y(), point.z());
    }

    @FunctionalInterface
    public static interface Sink {
        public void quad(Point var1, Point var2, Point var3, Point var4, int var5, float var6, int var7);
    }

    private static final class Mesh {
        private final Sink sink;
        private final int color;
        private final float alpha;
        private int count;

        private Mesh(Sink sink, int color, float alpha) {
            this.sink = sink;
            this.color = color;
            this.alpha = alpha;
        }

        private void solid(Point first, Point second, Point third, Point fourth) {
            this.quad(first, second, third, fourth, this.color, this.alpha, 255);
        }

        private void quad(Point first, Point second, Point third, Point fourth, int tint, float opacity, int tile) {
            if (this.count >= 1800) {
                return;
            }
            this.sink.quad(first, second, third, fourth, tint, opacity, tile);
            ++this.count;
        }

        private void line(Point first, Point second, double width) {
            Point direction = second.add(first.scale(-1.0));
            double horizontal = Math.hypot(direction.horizontalX, direction.horizontalZ);
            Point side = horizontal < 0.001 ? new Point(width, 0.0, 0.0) : new Point(direction.horizontalZ / horizontal * width, 0.0, -direction.horizontalX / horizontal * width);
            this.solid(first.add(side), second.add(side), second.add(side.scale(-1.0)), first.add(side.scale(-1.0)));
            Point up = new Point(0.0, width, 0.0);
            if (horizontal > 0.001) {
                this.solid(first.add(up), second.add(up), second.add(up.scale(-1.0)), first.add(up.scale(-1.0)));
            } else {
                Point cross = new Point(0.0, 0.0, width);
                this.solid(first.add(cross), second.add(cross), second.add(cross.scale(-1.0)), first.add(cross.scale(-1.0)));
            }
        }

        private void glyph(Point center, double radius, double turn, int tile, boolean vertical) {
            Point[] points = new Point[4];
            double[][] corners = new double[][]{{-1.0, -1.0}, {-1.0, 1.0}, {1.0, 1.0}, {1.0, -1.0}};
            for (int index = 0; index < 4; ++index) {
                double across = corners[index][0] * radius;
                double depth = corners[index][1] * radius;
                double rotatedX = across * Math.cos(turn) - depth * Math.sin(turn);
                double rotatedZ = across * Math.sin(turn) + depth * Math.cos(turn);
                points[index] = center.add(vertical ? new Point(rotatedX, rotatedZ, 0.0) : new Point(rotatedX, 0.0, rotatedZ));
            }
            this.quad(points[0], points[1], points[2], points[3], 0xFFFFFF, this.alpha * 0.75f, tile);
        }

        private void ring(Point center, double radius, double width, int axis, double start, double arc, int steps) {
            Point previous = null;
            for (int step = 0; step <= steps; ++step) {
                double angle = start + arc * (double)step / (double)steps;
                double across = Math.cos(angle) * radius;
                double depth = Math.sin(angle) * radius;
                Point next = center.add(axis == 0 ? new Point(across, 0.0, depth) : (axis == 1 ? new Point(0.0, across, depth) : new Point(across, depth, 0.0)));
                if (previous != null) {
                    this.line(previous, next, width);
                }
                previous = next;
            }
        }

        private void crystal(Point base, double width, double height, double turn) {
            Point bottom = base.add(new Point(0.0, -height * 0.15, 0.0));
            Point tip = base.add(new Point(0.0, height, 0.0));
            for (int face = 0; face < 4; ++face) {
                Point left = base.add(SpellVisualGeometry.polar(width, turn + (double)face * Math.PI / 2.0, height * 0.3));
                Point right = base.add(SpellVisualGeometry.polar(width, turn + (double)(face + 1) * Math.PI / 2.0, height * 0.3));
                this.solid(left, tip, right, right);
                this.solid(left, right, bottom, bottom);
            }
        }

        private void dome(Point center, double radius, double height, double turn) {
            Point tip = center.add(new Point(0.0, height, 0.0));
            for (int face = 0; face < 12; ++face) {
                Point left = center.add(SpellVisualGeometry.polar(radius, turn + (double)face * Math.PI / 6.0, 0.0));
                Point right = center.add(SpellVisualGeometry.polar(radius, turn + (double)(face + 1) * Math.PI / 6.0, 0.0));
                this.solid(left, tip, right, right);
            }
        }
    }

    public record Point(double horizontalX, double verticalY, double horizontalZ) {
        public Point add(Point other) {
            return new Point(this.horizontalX + other.horizontalX, this.verticalY + other.verticalY, this.horizontalZ + other.horizontalZ);
        }

        public Point scale(double amount) {
            return new Point(this.horizontalX * amount, this.verticalY * amount, this.horizontalZ * amount);
        }
    }
}

