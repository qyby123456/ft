package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.weapons.XueliWeaponItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class XueliWeaponRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation PAN = new ResourceLocation(FTBossMod.MOD_ID, "item/xueli/skillet");
    private static final ResourceLocation PIN = new ResourceLocation(FTBossMod.MOD_ID, "item/xueli/rolling_pin");
    private static final ResourceLocation PAN_GLOW = new ResourceLocation(FTBossMod.MOD_ID, "item/xueli/skillet_glow");
    private static final ResourceLocation PIN_GLOW = new ResourceLocation(FTBossMod.MOD_ID, "item/xueli/rolling_pin_glow");

    public XueliWeaponRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(PAN);
        event.register(PIN);
        event.register(PAN_GLOW);
        event.register(PIN_GLOW);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        if (!(stack.getItem() instanceof XueliWeaponItem weapon)) return;
        Minecraft client = Minecraft.getInstance();
        ItemRenderer renderer = client.getItemRenderer();
        var models = client.getModelManager();
        var solid = ItemRenderer.getFoilBufferDirect(buffer, RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS), true, stack.hasFoil());
        renderer.renderModelLists(models.getModel(weapon.isSkillet() ? PAN : PIN), stack, light, overlay, pose, solid);
        var glow = buffer.getBuffer(RenderType.entityTranslucentEmissive(TextureAtlas.LOCATION_BLOCKS));
        renderer.renderModelLists(models.getModel(weapon.isSkillet() ? PAN_GLOW : PIN_GLOW), stack, LightTexture.FULL_BRIGHT, overlay, pose, glow);
    }
}
