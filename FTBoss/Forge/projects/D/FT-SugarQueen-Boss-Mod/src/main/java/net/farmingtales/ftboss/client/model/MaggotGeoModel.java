package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.MaggotMinionEntity;

/** 蛆小怪模型(2026-09-11) */
public class MaggotGeoModel extends PathGeoModel<MaggotMinionEntity> {

    public MaggotGeoModel() {
        super("geo/maggot_minion.geo.json", "textures/entity/maggot_minion.png",
                "animations/maggot_minion.animation.json");
    }
}
