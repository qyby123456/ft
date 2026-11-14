package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.GingerbreadMinionEntity;

/** 小姜饼人 GeckoLib 模型提供器: geo + 贴图 + 动画 */
public class GingerbreadMinionGeoModel extends PathGeoModel<GingerbreadMinionEntity> {

    public GingerbreadMinionGeoModel() {
        super("geo/gingerbread_minion.geo.json", "textures/entity/gingerbread_minion.png",
                "animations/gingerbread_minion.animation.json");
    }
}
