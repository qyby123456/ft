/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.entity.EntityTypeTest
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.render.XuanlanWeaponVisual;
import net.farmingtales.ftboss.integ.tridents.SanguineBondHooks;
import net.farmingtales.ftboss.mixin.SanguineBondEntityAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class SanguineBondTrailVisual {
    private static final ResourceLocation TRAIL = new ResourceLocation("ftboss", "textures/fx/sanguine_bond_trail.png");
    private static final RenderType TYPE = RenderType.m_110488_((ResourceLocation)TRAIL);

    private SanguineBondTrailVisual() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft client = Minecraft.m_91087_();
        ClientLevel level = client.f_91073_;
        if (level == null || client.m_91288_() == null) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        AABB view = new AABB(camera, camera).m_82400_(64.0);
        ParticleStatus status = (ParticleStatus)client.f_91066_.m_231929_().m_231551_();
        int max = status == ParticleStatus.MINIMAL ? 3 : (status == ParticleStatus.DECREASED ? 8 : 16);
        MultiBufferSourceHack.draw(event.getPoseStack(), (Level)level, view, camera, event.getPartialTick(), max);
    }

    private static final class MultiBufferSourceHack {
        private MultiBufferSourceHack() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private static void draw(PoseStack pose, Level level, AABB view, Vec3 camera, float partial, int max) {
            MultiBufferSource.BufferSource buffers = Minecraft.m_91087_().m_91269_().m_110104_();
            VertexConsumer vertices = null;
            int drawn = 0;
            pose.m_85836_();
            try {
                pose.m_85837_(-camera.f_82479_, -camera.f_82480_, -camera.f_82481_);
                for (Entity entity : level.m_142425_(EntityTypeTest.m_156916_(Entity.class), view, candidate -> candidate instanceof SanguineBondEntityAccess)) {
                    if (drawn >= max) break;
                    if (entity.m_213877_()) {
                        break;
                    }
                    ItemStack stack = ((SanguineBondEntityAccess)entity).ftboss$getTridentStack();
                    if (!SanguineBondHooks.isSanguine(stack)) continue;
                    Vec3 now = entity.m_20318_(partial);
                    Vec3 motion = entity.m_20184_();
                    if (motion.m_82556_() < 1.0E-5) continue;
                    Vec3 tail = now.m_82546_(motion.m_82541_().m_82490_(2.5));
                    if (vertices == null) {
                        vertices = buffers.m_6299_(TYPE);
                    }
                    float alpha = 0.62f + 0.18f * (float)Math.sin((double)entity.f_19797_ * 0.55);
                    XuanlanWeaponVisual.line(pose, vertices, tail, now, camera, 0.16, 15015750, alpha);
                    XuanlanWeaponVisual.line(pose, vertices, tail, now, camera, 0.035, 0xFFF0F4, 0.92f);
                    ++drawn;
                }
            }
            finally {
                pose.m_85849_();
                if (vertices != null) {
                    buffers.m_109912_(TYPE);
                }
            }
        }
    }
}

