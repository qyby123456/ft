package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.entity.TeaTreeBossEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.client.model.SayaPlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** 沙耶渲染: Alex 人形(玩家模型) + 沙耶皮肤 + 手写铁魔法施法姿势 */
public class TeaTreeRenderer extends MobRenderer<TeaTreeBossEntity, SayaPlayerModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("ftboss", "textures/entity/tea_alex.png");

    public TeaTreeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new SayaPlayerModel(ctx.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(TeaTreeBossEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(TeaTreeBossEntity entity, PoseStack pose, float partialTick) {
        if (entity.isTeaMinion()) {
            pose.scale(0.55f, 0.55f, 0.55f);
        } else {
            // Alex 人形模型原点即脚底, 直接对齐碰撞箱底; 不可再加早期树模型的 +1.5 上移
            pose.scale(1.3f, 1.3f, 1.3f);
        }
    }
}
