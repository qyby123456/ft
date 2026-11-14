/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.sound;

public enum BossHurtProfile {
    GINGERBREAD("sugar_queen", "entity.player.hurt"),
    BUTCHER("meat_mountain", "entity.ravager.hurt"),
    TEA("tea_tree", "entity.player.hurt"),
    BARREL("wine_barrel", "entity.iron_golem.hurt"),
    RAT("rat_boss", "entity.silverfish.hurt"),
    ROBOT("defect_core", "entity.iron_golem.hurt"),
    APPLE("apple_boss", "entity.player.hurt"),
    COMPOST("compost_boss", "entity.player.hurt"),
    BENGER("benger", "entity.wolf.hurt"),
    TIDE("tide_fish_boss", "entity.guardian.hurt"),
    MANOR("easter_boss", "entity.player.hurt");

    private final String entityPath;
    private final String soundPath;

    private BossHurtProfile(String string2, String string3) {
        this.entityPath = string2;
        this.soundPath = string3;
    }

    public String soundId() {
        return "minecraft:" + this.soundPath;
    }

    public String entityPath() {
        return this.entityPath;
    }

    public static BossHurtProfile fromType(String string, String string2) {
        if (!"ftboss".equals(string)) {
            return null;
        }
        for (BossHurtProfile bossHurtProfile : BossHurtProfile.values()) {
            if (!bossHurtProfile.entityPath.equals(string2)) continue;
            return bossHurtProfile;
        }
        return null;
    }
}

