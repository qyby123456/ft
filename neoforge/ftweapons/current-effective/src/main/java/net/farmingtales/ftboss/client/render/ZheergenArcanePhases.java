package net.farmingtales.ftboss.client.render;

public final class ZheergenArcanePhases {
    private ZheergenArcanePhases() {
    }

    private static int priority(String var0) {
        return switch (var0) {
            case "PULSE" -> 3;
            case "RELEASE" -> 2;
            case "CHARGE" -> 1;
            default -> 0;
        };
    }

    public static boolean prefer(String var0, long var1, String var3, long var4) {
        return var1 > var4 || var1 == var4 && priority(var0) > priority(var3);
    }
}
