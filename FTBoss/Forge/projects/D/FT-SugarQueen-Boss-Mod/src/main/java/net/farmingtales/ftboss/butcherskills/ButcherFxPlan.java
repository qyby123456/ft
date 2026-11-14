package net.farmingtales.ftboss.butcherskills;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.butcherskills.ButcherSkillRules.Skill;

public final class ButcherFxPlan {
    public record Point(double offsetX, double height, double offsetZ, boolean bone) {}
    private static final List<Point>[][] SPELLS;
    private static final List<Point>[][] SUMMONS;

    static {
        SPELLS = createPlans(Skill.values().length);
        for (Skill skill : Skill.values()) {
            for (int elapsed = 0; elapsed <= ButcherSkillRules.SUMMON_DURATION; elapsed++) SPELLS[skill.ordinal()][elapsed] = makeSpell(skill, elapsed);
        }
        SUMMONS = createPlans(2);
        for (int kind = 0; kind < 2; kind++) for (int elapsed = 0; elapsed <= ButcherSkillRules.SUMMON_DURATION; elapsed++) SUMMONS[kind][elapsed] = makeSummon(kind == 1, elapsed);
    }

    private ButcherFxPlan() {}

    @SuppressWarnings("unchecked")
    private static List<Point>[][] createPlans(int count) { return (List<Point>[][]) new List<?>[count][ButcherSkillRules.SUMMON_DURATION + 1]; }

    private static void ring(List<Point> points, double radius, int count, boolean bone) {
        for (int index = 0; index < count; index++) {
            double angle = index * Math.PI * 2 / count;
            points.add(new Point(Math.cos(angle) * radius, 0.12, Math.sin(angle) * radius, bone));
        }
    }

    private static void cone(List<Point> points, double radius, double halfAngle, double height, boolean bone, boolean edges) {
        for (int index = 0; index <= 16; index++) {
            double angle = -halfAngle + halfAngle * 2 * index / 16;
            points.add(new Point(Math.cos(angle) * radius, height, Math.sin(angle) * radius, bone));
        }
        if (!edges) return;
        for (int index = 1; index <= 4; index++) for (int side : new int[]{-1, 1}) {
            double angle = side * halfAngle;
            points.add(new Point(Math.cos(angle) * radius * index / 5, 0.15, Math.sin(angle) * radius * index / 5, bone));
        }
    }

    private static List<Point> makeSpell(Skill skill, int elapsed) {
        List<Point> points = new ArrayList<>();
        boolean release = elapsed >= skill.windup;
        if (skill == Skill.CLEAVER_SWEEP || skill == Skill.BLOOD_TIDE) {
            double halfAngle = skill == Skill.CLEAVER_SWEEP ? Math.PI / 3 : Math.PI / 4;
            if (!release) cone(points, skill.radius, halfAngle, 0.15, skill.melee(), true);
            else for (int band = 1; band <= 3; band++) cone(points, skill.radius * band / 3, halfAngle, band * 0.3, skill.melee(), false);
        } else {
            ring(points, skill.radius, release ? 24 : 20, skill.melee());
            if (!release) ring(points, 0.25 + ButcherSkillRules.progress(elapsed, skill.windup) * 0.8, 8, false);
            else for (int index = 0; index < 8; index++) {
                double angle = index * Math.PI * 2 / 8;
                for (int layer = 0; layer < 3; layer++) points.add(new Point(Math.cos(angle) * skill.radius * 0.7,
                        0.2 + layer * 0.35, Math.sin(angle) * skill.radius * 0.7, layer == 2 || skill.melee()));
            }
        }
        return List.copyOf(points);
    }

    private static List<Point> makeSummon(boolean boss, int elapsed) {
        List<Point> points = new ArrayList<>();
        double progress = ButcherSkillRules.progress(elapsed, ButcherSkillRules.SUMMON_DURATION);
        double radius = (boss ? 2.3 : 1) * (0.5 + Math.sin(progress * Math.PI) * 0.5);
        ring(points, radius, boss ? 20 : 12, false);
        int strands = boss ? 8 : 4;
        for (int index = 0; index < strands; index++) {
            double angle = progress * Math.PI * 3 + index * Math.PI * 2 / strands;
            points.add(new Point(Math.cos(angle) * radius * (1 - progress * 0.4), progress * (boss ? 2.2 : 1.1),
                    Math.sin(angle) * radius * (1 - progress * 0.4), true));
        }
        return List.copyOf(points);
    }

    public static List<Point> spell(Skill skill, int elapsed) { return SPELLS[skill.ordinal()][Math.max(0, Math.min(ButcherSkillRules.SUMMON_DURATION, elapsed))]; }
    public static List<Point> summon(boolean boss, int elapsed) { return SUMMONS[boss ? 1 : 0][Math.max(0, Math.min(ButcherSkillRules.SUMMON_DURATION, elapsed))]; }
}
