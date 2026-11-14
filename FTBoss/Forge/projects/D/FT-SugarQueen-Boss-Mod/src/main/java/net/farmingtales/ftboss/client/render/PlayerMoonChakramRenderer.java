package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.entity.PlayerMoonChakramEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.util.XuanlanMotionRules;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class PlayerMoonChakramRenderer extends EntityRenderer<PlayerMoonChakramEntity> {
    public static final float PROJECTILE_SCALE = 0.7F;
    private final ItemStack model = new ItemStack(FTBossItems.WENXUANLAN_FLYWHEEL.get());

    public PlayerMoonChakramRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(PlayerMoonChakramEntity entity, float yaw, float partial, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.scale(PROJECTILE_SCALE, PROJECTILE_SCALE, PROJECTILE_SCALE);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.mulPose(Axis.ZP.rotationDegrees((entity.tickCount + partial) * 40));
        pose.pushPose();
        pose.scale(0.5F, 0.5F, 0.5F);
        pose.translate(-0.5, -0.5, -0.5);
        XuanlanWeaponRenderer.renderRig(model, true, new XuanlanMotionRules.Pose(0, 0,
                entity.isReturning() ? 0.35F : 0, 0, entity.tickCount + partial),
                pose, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        XuanlanWeaponVisual.ring(pose, buffers.getBuffer(XuanlanGlow.TYPE), Vec3.ZERO,
                new Vec3(1, 0, 0), new Vec3(0, 1, 0), 0.29, 0.016,
                entity.isReturning() ? 0x67FFCC : 0xDBEEFF, 0.65F, 0, true);
        pose.popPose();
        super.render(entity, yaw, partial, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(PlayerMoonChakramEntity entity) { return TextureAtlas.LOCATION_BLOCKS; }
}
