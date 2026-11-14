/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3d
 *  software.bernie.geckolib.cache.object.GeoBone
 */
package net.farmingtales.ftboss.client.defectzero;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.defectzero.DefectSkillHost;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.GeoBone;

public final class DefectSkillAnchors {
    private static final Map<UUID, Map<String, Anchor>> ANCHORS = new HashMap<UUID, Map<String, Anchor>>();

    private DefectSkillAnchors() {
    }

    public static void clear() {
        ANCHORS.clear();
    }

    public static void capture(DefectCoreEntity defectCoreEntity, GeoBone geoBone, boolean bl) {
        if (bl) {
            return;
        }
        if (!(geoBone.getName().equals("core") || geoBone.getName().equals("hand_l") || geoBone.getName().equals("hand_r"))) {
            return;
        }
        Vector3d vector3d = geoBone.getWorldPosition();
        if (!(Double.isFinite(vector3d.x) && Double.isFinite(vector3d.y) && Double.isFinite(vector3d.z))) {
            return;
        }
        if (ANCHORS.size() > 128) {
            ANCHORS.clear();
        }
        ANCHORS.computeIfAbsent(defectCoreEntity.m_20148_(), uUID -> new HashMap()).put(geoBone.getName(), new Anchor(new Vec3(vector3d.x, vector3d.y, vector3d.z), defectCoreEntity.m_9236_().m_46467_()));
    }

    public static Vec3 core(Entity entity) {
        if (entity == null || !(entity instanceof DefectSkillHost)) {
            return null;
        }
        Map<String, Anchor> map = ANCHORS.get(entity.m_20148_());
        Anchor anchor = map == null ? null : map.get("core");
        return anchor != null && Math.abs(entity.m_9236_().m_46467_() - anchor.tick) <= 2L ? anchor.point : null;
    }

    private record Anchor(Vec3 point, long tick) {
    }
}

