package net.farmingtales.ftboss.util.foundations;

import java.util.List;

/** 武器功能就绪判定；封印槽保留材料但不参与功能结算，不因进化退级锁死整件武器。 */
public final class FoundationReadinessRules {
    private FoundationReadinessRules() {}

    public static boolean ready(List<String> missing, List<Integer> unsupported) {
        return missing.isEmpty() && unsupported.isEmpty();
    }
}
