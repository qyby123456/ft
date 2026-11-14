package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.FlyMinionEntity;

/** 苍蝇小怪模型(2026-09-12, 炫彩答辩超人的召唤物) */
public class FlyGeoModel extends PathGeoModel<FlyMinionEntity> {

    public FlyGeoModel() {
        super("geo/fly_minion.geo.json", "textures/entity/fly_minion.png",
                "animations/fly_minion.animation.json");
    }
}
