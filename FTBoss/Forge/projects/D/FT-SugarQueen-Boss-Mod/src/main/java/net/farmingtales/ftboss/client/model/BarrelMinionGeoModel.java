package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.SmallBarrelEntity;

/** 小酒桶的 GeckoLib 模型(2026-09-11 新增, 由 tools/model3d/gen_barrel_minion.cjs 生成的三件套) */
public class BarrelMinionGeoModel extends PathGeoModel<SmallBarrelEntity> {

    public BarrelMinionGeoModel() {
        super("geo/barrel_minion.geo.json", "textures/entity/barrel_minion.png",
                "animations/barrel_minion.animation.json");
    }
}
