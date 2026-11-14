/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.ModelResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ModelEvent$ModifyBakingResult
 *  net.minecraftforge.client.model.BakedModelWrapper
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.jetbrains.annotations.Nullable
 */
package net.farmingtales.ftboss.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.client.render.XuanlanWeaponMotion;
import net.farmingtales.ftboss.client.render.XuanlanWeaponRenderer;
import net.farmingtales.ftboss.util.XuanlanMotionRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class XuanlanWeaponModel
extends BakedModelWrapper<BakedModel> {
    private final ItemOverrides overrides;

    private XuanlanWeaponModel(final BakedModel original) {
        super(original);
        this.overrides = new ItemOverrides(){

            public BakedModel m_173464_(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed) {
                BakedModel selected = original.m_7343_().m_173464_(original, stack, level, owner, seed);
                return new HeldModel(selected == null ? original : selected, stack, XuanlanWeaponMotion.sample(owner, stack, Minecraft.m_91087_().m_91296_()));
            }
        };
    }

    public ItemOverrides m_7343_() {
        return this.overrides;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void bake(ModelEvent.ModifyBakingResult event) {
        ModelResourceLocation id = new ModelResourceLocation("ftboss", "wenxuanlan_flywheel", "inventory");
        BakedModel original = (BakedModel)event.getModels().get(id);
        if (original != null) {
            event.getModels().put(id, new XuanlanWeaponModel(original));
        }
        XuanlanWeaponRenderer.clearPrepared();
    }

    private static final class HeldModel
    extends BakedModelWrapper<BakedModel> {
        private final ItemStack stack;
        private final XuanlanMotionRules.Pose motion;

        private HeldModel(BakedModel original, ItemStack stack, XuanlanMotionRules.Pose motion) {
            super(original);
            this.stack = stack;
            this.motion = motion;
        }

        public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) {
            BakedModel transformed = this.originalModel.applyTransform(context, pose, leftHand);
            if (context.m_269069_() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                XuanlanWeaponRenderer.prepare(this.stack, this.motion);
                return this;
            }
            XuanlanWeaponRenderer.clearPrepared();
            return transformed;
        }

        public boolean m_7521_() {
            return true;
        }
    }
}

