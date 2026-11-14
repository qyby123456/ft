package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.WineBarrelBossEntity;

/** 酒魔·酒桶王 GeckoLib 模型提供器: 横放巨型酒桶(含滚动/开塞/砸地动画) */
public class GiantBarrelModel extends PathGeoModel<WineBarrelBossEntity> {

    public GiantBarrelModel() {
        super("geo/wine_barrel_boss.geo.json", "textures/entity/wine_barrel_boss_v2.png",
                "animations/wine_barrel_boss.animation.json");
    }
}
