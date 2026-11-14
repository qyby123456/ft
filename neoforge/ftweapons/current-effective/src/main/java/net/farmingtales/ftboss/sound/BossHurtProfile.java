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

    private BossHurtProfile(String var3, String var4) {
        this.entityPath = var3;
        this.soundPath = var4;
    }

    public String soundId() {
        return "minecraft:" + this.soundPath;
    }

    public String entityPath() {
        return this.entityPath;
    }

    public static BossHurtProfile fromType(String var0, String var1) {
        if (!"ftboss".equals(var0)) {
            return null;
        } else {
            for (BossHurtProfile var5 : values()) {
                if (var5.entityPath.equals(var1)) {
                    return var5;
                }
            }

            return null;
        }
    }
}
