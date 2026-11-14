/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.entity;

public enum OrbType {
    GLASS("glass", "\u73bb\u7483\u7403", "\u00a7b"),
    LIGHTNING("lightning", "\u7535\u7403", "\u00a7e"),
    DARK("dark", "\u9ed1\u7403", "\u00a75"),
    FROST("frost", "\u51b0\u7403", "\u00a7f"),
    ION("ion", "\u79bb\u5b50\u7403", "\u00a7d");

    public final String id;
    public final String cn;
    public final String color;

    private OrbType(String id, String cn, String color) {
        this.id = id;
        this.cn = cn;
        this.color = color;
    }

    public static OrbType byId(String id) {
        for (OrbType t : OrbType.values()) {
            if (!t.id.equals(id)) continue;
            return t;
        }
        return GLASS;
    }
}

