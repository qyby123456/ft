package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.PoopMinionEntity;

public class PoopMinionModel extends PathGeoModel<PoopMinionEntity> {

    public PoopMinionModel() {
        super("geo/poop_minion.geo.json", "textures/entity/poop_minion.png",
                "animations/poop_minion.animation.json");
    }
}
