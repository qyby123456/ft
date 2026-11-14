package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.WineBarrelDecoyEntity;

/** 酒桶王分身模型(与酒桶王**同一套** geo/动画/贴图, 2026-09-12) */
public class WineBarrelDecoyModel extends PathGeoModel<WineBarrelDecoyEntity> {

    public WineBarrelDecoyModel() {
        super("geo/wine_barrel_boss.geo.json", "textures/entity/wine_barrel_boss_v2.png",
                "animations/wine_barrel_boss.animation.json");
    }
}
