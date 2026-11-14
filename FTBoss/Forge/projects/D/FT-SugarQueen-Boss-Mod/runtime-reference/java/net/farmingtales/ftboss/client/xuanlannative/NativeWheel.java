/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.client.xuanlannative;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.client.xuanlannative.NativeFacing;
import net.farmingtales.ftboss.entity.FlywheelProjectileEntity;
import net.farmingtales.ftboss.entity.XuanlanEliteFlywheels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public final class NativeWheel {
    private static final ResourceLocation DEDICATED = new ResourceLocation("ftboss", "wenxuanlan_flywheel");
    private static final ResourceLocation FALLBACK = new ResourceLocation("create", "flywheel");

    private NativeWheel() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(FlywheelProjectileEntity flywheelProjectileEntity, Vec3 vec3, double d, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        boolean bl;
        boolean bl2;
        Minecraft minecraft = Minecraft.m_91087_();
        ItemRenderer itemRenderer = minecraft.m_91291_();
        Item item = (Item)ForgeRegistries.ITEMS.getValue(DEDICATED);
        boolean bl3 = bl2 = item != null && item != Items.f_41852_;
        ItemStack itemStack = bl2 ? (flywheelProjectileEntity.m_7846_().m_150930_(item) ? flywheelProjectileEntity.m_7846_() : new ItemStack((ItemLike)item)) : ItemStack.f_41583_;
        boolean bl4 = bl = bl2 && itemRenderer.m_174264_(itemStack, flywheelProjectileEntity.m_9236_(), null, flywheelProjectileEntity.m_19879_()) != minecraft.m_91304_().m_119409_();
        if (!NativeFacing.useDedicated(bl2, bl)) {
            Item item2 = (Item)ForgeRegistries.ITEMS.getValue(FALLBACK);
            if (item2 == null || item2 == Items.f_41852_) {
                return;
            }
            itemStack = new ItemStack((ItemLike)item2);
            if (itemRenderer.m_174264_(itemStack, flywheelProjectileEntity.m_9236_(), null, flywheelProjectileEntity.m_19879_()) == minecraft.m_91304_().m_119409_()) {
                return;
            }
        }
        poseStack.m_85836_();
        try {
            poseStack.m_252781_(NativeFacing.orientation(vec3).rotateZ((float)Math.toRadians(d * 25.0)));
            poseStack.m_85841_(XuanlanEliteFlywheels.wheelScale((Entity)flywheelProjectileEntity, 0.75f), XuanlanEliteFlywheels.wheelScale((Entity)flywheelProjectileEntity, 0.75f), XuanlanEliteFlywheels.wheelScale((Entity)flywheelProjectileEntity, 0.75f));
            itemRenderer.m_269128_(itemStack, ItemDisplayContext.GROUND, n, OverlayTexture.f_118083_, poseStack, multiBufferSource, flywheelProjectileEntity.m_9236_(), flywheelProjectileEntity.m_19879_());
        }
        finally {
            poseStack.m_85849_();
        }
    }
}

