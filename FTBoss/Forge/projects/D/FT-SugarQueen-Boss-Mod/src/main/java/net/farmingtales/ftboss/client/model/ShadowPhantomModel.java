package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.ShadowPhantomEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * 【折耳根紫色残影的模型】(2026-09-24)。
 *
 * <p>用户要求「独立做一个折耳根人形紫色残影的模型」—— 所以这里不是复用本体那套
 * {@code EasterBossModel}（它的泛型绑死在 {@code EasterBossEntity} 上），
 * 而是给残影自己的实体一个标准人形模型（{@link HumanoidModel} + 原版 {@code ModelLayers.PLAYER} 骨架，
 * 与本体同比例，所以轮廓一致）。
 *
 * <p>姿势**完全静止**：不走动、不摆臂、不转头、不呼吸 —— 残影是一张"定格照片"，
 * 所以 {@code setupAnim} 一律按 0 传入（连 ageInTicks 也给 0，免得出现呼吸起伏）。
 */
public class ShadowPhantomModel extends HumanoidModel<ShadowPhantomEntity> {

    public ShadowPhantomModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(ShadowPhantomEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        super.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        this.attackTime = 0.0F;
        this.crouching = false;
        this.riding = false;
        this.young = false;
        this.swimAmount = 0.0F;
    }
}
