/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.resources.model.ModelManager
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ModelEvent$RegisterAdditional
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.item.weapons.XueliWeaponItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class XueliWeaponRenderer
extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation PAN = new ResourceLocation("ftboss", "item/xueli/skillet");
    private static final ResourceLocation PIN = new ResourceLocation("ftboss", "item/xueli/rolling_pin");
    private static final ResourceLocation PAN_GLOW = new ResourceLocation("ftboss", "item/xueli/skillet_glow");
    private static final ResourceLocation PIN_GLOW = new ResourceLocation("ftboss", "item/xueli/rolling_pin_glow");

    public XueliWeaponRenderer() {
        super(Minecraft.m_91087_().m_167982_(), Minecraft.m_91087_().m_167973_());
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(PAN);
        event.register(PIN);
        event.register(PAN_GLOW);
        event.register(PIN_GLOW);
    }

    public void m_108829_(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        Item item = stack.m_41720_();
        if (!(item instanceof XueliWeaponItem)) {
            return;
        }
        XueliWeaponItem weapon = (XueliWeaponItem)item;
        Minecraft client = Minecraft.m_91087_();
        ItemRenderer renderer = client.m_91291_();
        ModelManager models = client.m_91304_();
        VertexConsumer solid = ItemRenderer.m_115222_((MultiBufferSource)buffer, (RenderType)RenderType.m_110458_((ResourceLocation)TextureAtlas.f_118259_), (boolean)true, (boolean)stack.m_41790_());
        renderer.m_115189_(models.getModel(weapon.isSkillet() ? PAN : PIN), stack, light, overlay, pose, solid);
        VertexConsumer glow = buffer.m_6299_(RenderType.m_234338_((ResourceLocation)TextureAtlas.f_118259_));
        renderer.m_115189_(models.getModel(weapon.isSkillet() ? PAN_GLOW : PIN_GLOW), stack, 0xF000F0, overlay, pose, glow);
    }
}

