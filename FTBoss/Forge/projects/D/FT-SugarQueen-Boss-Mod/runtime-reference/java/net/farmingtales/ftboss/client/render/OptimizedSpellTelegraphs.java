/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.render.OptimizedSpellPlacement;
import net.farmingtales.ftboss.util.SpellVisualGeometry;
import net.farmingtales.ftboss.util.SpellVisualRules;

public final class OptimizedSpellTelegraphs {
    private OptimizedSpellTelegraphs() {
    }

    public static int draw(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase, double d, float f, double d2, boolean bl, SpellVisualGeometry.Sink sink) {
        double d3;
        if (!Double.isFinite(d) || !Double.isFinite(d2) || !Float.isFinite(f) || f <= 0.0f) {
            return 0;
        }
        Painter painter = new Painter(sink, spell.color, Math.min(1.0f, f) * (phase == SpellVisualRules.Phase.CHARGE ? 0.65f : 0.38f));
        if (phase == SpellVisualRules.Phase.BEAM) {
            if (!bl && d2 >= 0.015) {
                painter.line(-0.035, 0.0, 0.035, d2, 0.028);
                return painter.count;
            }
            return 0;
        }
        String string = OptimizedSpellPlacement.cue(spell);
        if (string.equals("NONE")) {
            return 0;
        }
        double d4 = Math.max(0.35, spell.radius);
        double d5 = 0.2 + Math.min(0.42, d4 * 0.08);
        double d6 = d4 / Math.sqrt(2.0);
        if (string.equals("CROSS")) {
            painter.line(-6.0, 0.0, 6.0, 0.0, 0.12);
            painter.line(0.0, -6.0, 0.0, 6.0, 0.12);
            for (double d7 : new double[]{-6.0, -3.0, 3.0, 6.0}) {
                painter.line(d7, -0.7, d7, 0.7, 0.036);
                painter.line(-0.7, d7, 0.7, d7, 0.036);
            }
            return painter.count;
        }
        if (string.equals("WAVE") || string.equals("CORRIDOR") || string.equals("LANES")) {
            double d8 = string.equals("CORRIDOR") ? (double)(spell.id.equals("riptide_corridor") ? 15 : 12) : (string.equals("WAVE") ? (double)(spell.id.equals("tidal_edict") ? 12 : 10) : Math.min(12.0, Math.max(4.0, d2)));
            int n = string.equals("LANES") ? 3 : 2;
            for (int i = 0; i < n; ++i) {
                double d9 = n == 3 ? (double)(i - 1) * 0.55 : (double)(i == 0 ? -1 : 1) * Math.min(d4, 2.7);
                for (int j = 0; j < 3; ++j) {
                    double d10 = d8 * (double)(j + 1) / 3.0;
                    painter.line(d9, d10 - 0.3, d9, d10 + 0.3, 0.032);
                    painter.line(d9 - 0.14, d10 - 0.13, d9, d10 + 0.1, 0.028);
                }
            }
            return painter.count;
        }
        if (string.equals("STARS")) {
            for (int i = 0; i < 4; ++i) {
                painter.corner((double)(i % 2 == 0 ? -1 : 1) * d6, (double)(i < 2 ? -1 : 1) * d6, d5);
            }
            painter.line(-0.1, 0.0, 0.1, 0.0, 0.025);
            painter.line(0.0, -0.1, 0.0, 0.1, 0.025);
            return painter.count;
        }
        for (int i = 0; i < 4; ++i) {
            double d11 = (double)(i % 2 == 0 ? -1 : 1) * d6;
            d3 = (double)(i < 2 ? -1 : 1) * d6;
            painter.corner(d11, d3, d5);
        }
        switch (string) {
            case "SLASH": 
            case "SCARS": {
                for (int i = 0; i < 3; ++i) {
                    d3 = (double)(i - 1) * d4 * 0.4;
                    painter.line(d3 - 0.3, -d4 * 0.35, d3 + 0.3, d4 * 0.35, 0.025);
                }
                break;
            }
            case "LATTICE": {
                for (int i = 0; i < 3; ++i) {
                    d3 = (double)(i - 1) * d4 * 0.55;
                    painter.line(d3, -d4 * 0.48, d3, -d4 * 0.26, 0.025);
                    painter.line(d3, d4 * 0.26, d3, d4 * 0.48, 0.025);
                }
                break;
            }
            case "PULL": 
            case "BIND": {
                for (int i = 0; i < 4; ++i) {
                    d3 = (double)i * Math.PI / 2.0;
                    double d12 = Math.cos(d3) * d4 * 0.7;
                    double d13 = Math.sin(d3) * d4 * 0.7;
                    painter.line(d12, d13, d12 * 0.6, d13 * 0.6, 0.031);
                    painter.line(d12 * 0.6, d13 * 0.6, d12 * 0.72 - d13 * 0.12, d13 * 0.72 + d12 * 0.12, 0.026);
                }
                break;
            }
            case "REPULSE": {
                for (int i = 0; i < 4; ++i) {
                    d3 = (double)i * Math.PI / 2.0;
                    double d14 = Math.cos(d3) * d4 * 0.57;
                    double d15 = Math.sin(d3) * d4 * 0.57;
                    painter.line(d14, d15, d14 * 1.4, d15 * 1.4, 0.031);
                }
                break;
            }
            case "RAIN": 
            case "FROST": {
                for (int i = 0; i < 6; ++i) {
                    d3 = ((double)(i * 7 % 6) - 2.5) * d4 * 0.23;
                    double d16 = (double)(i * 3 % 5 - 2) * d4 * 0.22;
                    painter.line(d3 - 0.08, d16, d3 + 0.08, d16, 0.03);
                    if (!string.equals("FROST")) continue;
                    painter.line(d3, d16 - 0.08, d3, d16 + 0.08, 0.03);
                }
                break;
            }
            case "PETALS": 
            case "SPORES": 
            case "SPRAY": {
                for (int i = 0; i < 7; ++i) {
                    d3 = (double)(i * 5 % 7 - 3) * d4 * 0.2;
                    double d17 = (double)(i * 3 % 7 - 3) * d4 * 0.2;
                    painter.line(d3 - 0.035, d17 - 0.06, d3 + 0.035, d17 + 0.06, 0.038);
                }
                break;
            }
            case "HEAL": {
                for (int i = 0; i < 4; ++i) {
                    d3 = (double)(i % 2 == 0 ? -1 : 1) * d4 * 0.5;
                    double d18 = (double)(i < 2 ? -1 : 1) * d4 * 0.42;
                    painter.line(d3 - 0.13, d18, d3 + 0.13, d18, 0.028);
                    painter.line(d3, d18 - 0.13, d3, d18 + 0.13, 0.028);
                }
                break;
            }
            case "MINES": {
                for (int i = 0; i < 3; ++i) {
                    painter.bracket((double)(i - 1) * d4 * 0.55, (double)(i % 2) * d4 * 0.33 - 0.16, 0.22, 0.14);
                }
                break;
            }
            case "ECHO": {
                for (int i = 0; i < 2; ++i) {
                    d3 = i == 0 ? -d4 * 0.47 : d4 * 0.47;
                    painter.line(d3, -d4 * 0.34, d3, d4 * 0.34, 0.035);
                }
                break;
            }
            case "BLAST": {
                for (int i = 0; i < 4; ++i) {
                    d3 = (double)(i % 2 == 0 ? -1 : 1) * d4 * 0.48;
                    double d19 = (double)(i < 2 ? -1 : 1) * d4 * 0.48;
                    painter.line(d3 * 0.75, d19 * 0.75, d3, d19, 0.032);
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown spell cue " + string);
            }
        }
        return painter.count;
    }

    private static final class Painter {
        private final SpellVisualGeometry.Sink sink;
        private final int color;
        private final float opacity;
        private int count;

        private Painter(SpellVisualGeometry.Sink sink, int n, float f) {
            this.sink = sink;
            this.color = n;
            this.opacity = f;
        }

        private void line(double d, double d2, double d3, double d4, double d5) {
            double d6 = Math.hypot(d3 - d, d4 - d2);
            if (d6 < 1.0E-6) {
                return;
            }
            double d7 = -(d4 - d2) / d6 * d5 * 0.5;
            double d8 = (d3 - d) / d6 * d5 * 0.5;
            double d9 = 0.045 + (double)this.count * 4.0E-4;
            this.sink.quad(new SpellVisualGeometry.Point(d + d7, d9, d2 + d8), new SpellVisualGeometry.Point(d3 + d7, d9, d4 + d8), new SpellVisualGeometry.Point(d3 - d7, d9, d4 - d8), new SpellVisualGeometry.Point(d - d7, d9, d2 - d8), this.color, this.opacity, 255);
            ++this.count;
        }

        private void corner(double d, double d2, double d3) {
            double d4 = d < 0.0 ? 1.0 : -1.0;
            double d5 = d2 < 0.0 ? 1.0 : -1.0;
            this.line(d, d2, d + d4 * d3, d2, 0.043);
            this.line(d, d2, d, d2 + d5 * d3, 0.043);
        }

        private void bracket(double d, double d2, double d3, double d4) {
            for (int i = 0; i < 4; ++i) {
                double d5 = i % 2 == 0 ? -1.0 : 1.0;
                double d6 = i < 2 ? -1.0 : 1.0;
                this.line(d + d5 * d3, d2 + d6 * d3, d + d5 * (d3 - d4), d2 + d6 * d3, 0.03);
            }
        }
    }
}

