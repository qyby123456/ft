package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.api.OptionalWeaponModule;
import net.farmingtales.ftboss.item.weapons.GourmetBlankBladeItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class GourmetBlankBladeRenderer extends GeoItemRenderer<GourmetBlankBladeItem> {
   private static final float SOCKET_COLOR_BLEND = 0.15F;
   private int renderedSockets;

   public GourmetBlankBladeRenderer() {
      super(new GourmetBlankBladeRenderer.Model());
      this.addRenderLayer(new AutoGlowingGeoLayer(this));
   }

   public RenderType getRenderType(GourmetBlankBladeItem var1, ResourceLocation var2, MultiBufferSource var3, float var4) {
      return RenderType.entityCutoutNoCull(var2);
   }

   public void preRender(
      PoseStack var1,
      GourmetBlankBladeItem var2,
      BakedGeoModel var3,
      MultiBufferSource var4,
      VertexConsumer var5,
      boolean var6,
      float var7,
      int var8,
      int var9,
      int var10
   ) {
      if (!var6) {
         ItemStack var11 = this.getCurrentItemStack();
         this.renderedSockets = var11 != null && !var11.isEmpty() ? OptionalWeaponModule.socketCount(var11) : 0;
      }

      super.preRender(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10);
   }

   public void renderRecursively(
      PoseStack var1,
      GourmetBlankBladeItem var2,
      GeoBone var3,
      RenderType var4,
      MultiBufferSource var5,
      VertexConsumer var6,
      boolean var7,
      float var8,
      int var9,
      int var10,
      int var11
   ) {
      String var12 = var3.getName();
      int var13 = socketStageRequirement(var12);
      if (var13 <= this.renderedSockets) {
         if (var12.startsWith("sigil_")) {
            try {
               int var14 = Integer.parseInt(var12.substring("sigil_".length()));
               if (OptionalWeaponModule.evolutionLevel(this.getCurrentItemStack()) < var14) {
                  return;
               }
            } catch (NumberFormatException var20) {
            }
         }

         if (var12.startsWith("socket_fill_")) {
            int var21;
            try {
               int var15 = Integer.parseInt(var12.substring("socket_fill_".length()));
               var21 = OptionalWeaponModule.socketVisualColor(this.getCurrentItemStack(), var15);
            } catch (NumberFormatException var19) {
               return;
            }

            if (var21 < 0) {
               return;
            }

            float var22 = (float)(var11 >>> 24 & 0xFF) / 255.0F;
            float var16 = (float)(var11 >>> 16 & 0xFF) / 255.0F * subduedSocketChannel(var21 >> 16 & 0xFF, 0.78F);
            float var17 = (float)(var11 >>> 8 & 0xFF) / 255.0F * subduedSocketChannel(var21 >> 8 & 0xFF, 0.69F);
            float var18 = (float)(var11 & 0xFF) / 255.0F * subduedSocketChannel(var21 & 0xFF, 0.48F);
            var11 = ARGB32.colorFromFloat(var22, var16, var17, var18);
         }

         super.renderRecursively(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11);
      }
   }

   private static int socketStageRequirement(String var0) {
      if (var0.startsWith("socket_stage_30")) {
         return 30;
      } else if (var0.startsWith("socket_stage_15")) {
         return 15;
      } else {
         return var0.startsWith("socket_stage_01") ? 1 : 0;
      }
   }

   private static float subduedSocketChannel(int var0, float var1) {
      return var1 * 0.85F + (float)var0 / 255.0F * 0.15F;
   }

   private static final class Model extends GeoModel<GourmetBlankBladeItem> {
      private static final ResourceLocation GEOMETRY = ResourceLocation.fromNamespaceAndPath("ftboss", "geo/gourmet_blank_blade.geo.json");
      private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ftboss", "textures/item/gourmet_blank_blade.png");
      private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath("ftboss", "animations/gourmet_blank_blade.animation.json");

      public ResourceLocation getModelResource(GourmetBlankBladeItem var1) {
         return GEOMETRY;
      }

      public ResourceLocation getTextureResource(GourmetBlankBladeItem var1) {
         return TEXTURE;
      }

      public ResourceLocation getAnimationResource(GourmetBlankBladeItem var1) {
         return ANIMATION;
      }
   }
}
