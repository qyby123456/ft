package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.TeaMinionEntity;

/** 茶芽小妖 GeckoLib 模型提供器: 山茶花芽 geo + 贴图 + 动画 */
public class TeaMinionGeoModel extends PathGeoModel<TeaMinionEntity> {

    public TeaMinionGeoModel() {
        super("geo/camellia_sprout.geo.json", "textures/entity/camellia_sprout.png",
                "animations/camellia_sprout.animation.json");
    }
}
