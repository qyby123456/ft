/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.butcherskills;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.butcherskills.ButcherSkillRules;

public final class ButcherFxPlan {
    private static final List<Point>[][] SPELLS = ButcherFxPlan.createPlans(ButcherSkillRules.Skill.values().length);
    private static final List<Point>[][] SUMMONS;

    private ButcherFxPlan() {
    }

    private static List<Point>[][] createPlans(int n) {
        return new List[n][33];
    }

    private static void ring(List<Point> list, double d, int n, boolean bl) {
        for (int i = 0; i < n; ++i) {
            double d2 = (double)i * Math.PI * 2.0 / (double)n;
            list.add(new Point(Math.cos(d2) * d, 0.12, Math.sin(d2) * d, bl));
        }
    }

    private static void cone(List<Point> list, double d, double d2, double d3, boolean bl, boolean bl2) {
        int n;
        for (n = 0; n <= 16; ++n) {
            double d4 = -d2 + d2 * 2.0 * (double)n / 16.0;
            list.add(new Point(Math.cos(d4) * d, d3, Math.sin(d4) * d, bl));
        }
        if (!bl2) {
            return;
        }
        for (n = 1; n <= 4; ++n) {
            for (int n2 : new int[]{-1, 1}) {
                double d5 = (double)n2 * d2;
                list.add(new Point(Math.cos(d5) * d * (double)n / 5.0, 0.15, Math.sin(d5) * d * (double)n / 5.0, bl));
            }
        }
    }

    private static List<Point> makeSpell(ButcherSkillRules.Skill skill, int n) {
        boolean bl;
        ArrayList<Point> arrayList = new ArrayList<Point>();
        boolean bl2 = bl = n >= skill.windup;
        if (skill == ButcherSkillRules.Skill.CLEAVER_SWEEP || skill == ButcherSkillRules.Skill.BLOOD_TIDE) {
            double d;
            double d2 = d = skill == ButcherSkillRules.Skill.CLEAVER_SWEEP ? 1.0471975511965976 : 0.7853981633974483;
            if (!bl) {
                ButcherFxPlan.cone(arrayList, skill.radius, d, 0.15, skill.melee(), true);
            } else {
                for (int i = 1; i <= 3; ++i) {
                    ButcherFxPlan.cone(arrayList, skill.radius * (double)i / 3.0, d, (double)i * 0.3, skill.melee(), false);
                }
            }
        } else {
            ButcherFxPlan.ring(arrayList, skill.radius, bl ? 24 : 20, skill.melee());
            if (!bl) {
                ButcherFxPlan.ring(arrayList, 0.25 + ButcherSkillRules.progress(n, skill.windup) * 0.8, 8, false);
            } else {
                for (int i = 0; i < 8; ++i) {
                    double d = (double)i * Math.PI * 2.0 / 8.0;
                    for (int j = 0; j < 3; ++j) {
                        arrayList.add(new Point(Math.cos(d) * skill.radius * 0.7, 0.2 + (double)j * 0.35, Math.sin(d) * skill.radius * 0.7, j == 2 || skill.melee()));
                    }
                }
            }
        }
        return List.copyOf(arrayList);
    }

    private static List<Point> makeSummon(boolean bl, int n) {
        ArrayList<Point> arrayList = new ArrayList<Point>();
        double d = ButcherSkillRules.progress(n, 32);
        double d2 = (bl ? 2.3 : 1.0) * (0.5 + Math.sin(d * Math.PI) * 0.5);
        ButcherFxPlan.ring(arrayList, d2, bl ? 20 : 12, false);
        int n2 = bl ? 8 : 4;
        for (int i = 0; i < n2; ++i) {
            double d3 = d * Math.PI * 3.0 + (double)i * Math.PI * 2.0 / (double)n2;
            arrayList.add(new Point(Math.cos(d3) * d2 * (1.0 - d * 0.4), d * (bl ? 2.2 : 1.1), Math.sin(d3) * d2 * (1.0 - d * 0.4), true));
        }
        return List.copyOf(arrayList);
    }

    public static List<Point> spell(ButcherSkillRules.Skill skill, int n) {
        return SPELLS[skill.ordinal()][Math.max(0, Math.min(32, n))];
    }

    public static List<Point> summon(boolean bl, int n) {
        return SUMMONS[bl ? 1 : 0][Math.max(0, Math.min(32, n))];
    }

    static {
        ButcherSkillRules.Skill[] skillArray = ButcherSkillRules.Skill.values();
        int n = skillArray.length;
        for (int i = 0; i < n; ++i) {
            ButcherSkillRules.Skill skill = skillArray[i];
            for (int j = 0; j <= 32; ++j) {
                ButcherFxPlan.SPELLS[skill.ordinal()][j] = ButcherFxPlan.makeSpell(skill, j);
            }
        }
        SUMMONS = ButcherFxPlan.createPlans(2);
        for (int i = 0; i < 2; ++i) {
            for (n = 0; n <= 32; ++n) {
                ButcherFxPlan.SUMMONS[i][n] = ButcherFxPlan.makeSummon(i == 1, n);
            }
        }
    }

    public record Point(double offsetX, double height, double offsetZ, boolean bone) {
    }
}

