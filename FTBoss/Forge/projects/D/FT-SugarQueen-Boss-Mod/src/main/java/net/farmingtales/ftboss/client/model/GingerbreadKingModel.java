package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.SugarQueenEntity;

/** 糖霜女王(姜饼人国王) GeckoLib 模型提供器 */
public class GingerbreadKingModel extends PathGeoModel<SugarQueenEntity> {

    public GingerbreadKingModel() {
        super("geo/sugar_queen_v3.geo.json", "textures/entity/sugar_queen_v3.png",
                "animations/sugar_queen_v3.animation.json");
    }
}
