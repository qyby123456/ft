/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.client.model.PathGeoModel;
import net.farmingtales.ftboss.entity.FlyMinionEntity;

public class FlyGeoModel
extends PathGeoModel<FlyMinionEntity> {
    public FlyGeoModel() {
        super("geo/fly_minion.geo.json", "textures/entity/fly_minion.png", "animations/fly_minion.animation.json");
    }
}

