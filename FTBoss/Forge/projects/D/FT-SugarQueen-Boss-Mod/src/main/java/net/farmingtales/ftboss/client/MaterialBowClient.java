package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.MaterialBowItems;
import net.farmingtales.ftboss.item.MaterialBowItems.Kind;
import net.farmingtales.ftboss.util.MaterialBowRules;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MaterialBowClient {
    private MaterialBowClient() {}

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (Kind kind : Kind.values()) {
                var bow = MaterialBowItems.get(kind);
                ItemProperties.register(bow, ResourceLocation.fromNamespaceAndPath("minecraft", "pull"),
                        (stack, level, entity, seed) -> entity != null && entity.getUseItem() == stack
                                ? MaterialBowRules.profile(kind.itemId).pull(stack.getUseDuration() - entity.getUseItemRemainingTicks()) : 0.0F);
                ItemProperties.register(bow, ResourceLocation.fromNamespaceAndPath("minecraft", "pulling"),
                        (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            }
        });
    }
}
