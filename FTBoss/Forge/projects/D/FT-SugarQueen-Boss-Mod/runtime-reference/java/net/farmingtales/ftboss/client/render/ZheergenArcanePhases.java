/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

public final class ZheergenArcanePhases {
    private ZheergenArcanePhases() {
    }

    private static int priority(String string) {
        return switch (string) {
            case "PULSE" -> 3;
            case "RELEASE" -> 2;
            case "CHARGE" -> 1;
            default -> 0;
        };
    }

    public static boolean prefer(String string, long l, String string2, long l2) {
        return l > l2 || l == l2 && ZheergenArcanePhases.priority(string) > ZheergenArcanePhases.priority(string2);
    }
}

