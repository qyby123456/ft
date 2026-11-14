package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.BarrelCannonballEntity;

/**
 * 酒桶炮弹的模型(2026-09-10 新增): **直接复用酒桶王的巨桶 geo 与贴图**, 靠渲染器缩放成 0.5 格左右的"酒桶炮弹",
 * 这样不用新增任何美术资源, 而且一眼就能看出是酒桶王丢出来的东西。
 * (动画文件只是 GeckoLib 的必填项; 渲染器不注册任何控制器 → 保持静止, 由渲染器的翻滚给出动感。)
 */
public class BarrelCannonballModel extends PathGeoModel<BarrelCannonballEntity> {

    public BarrelCannonballModel() {
        super("geo/wine_barrel_boss.geo.json", "textures/entity/wine_barrel_boss_v2.png",
                "animations/wine_barrel_boss.animation.json");
    }
}
