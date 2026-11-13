package net.farmingtales.ftdungeonmobs.client;

import net.farmingtales.ftdungeonmobs.DungeonSkeletonEntity;
import net.farmingtales.ftdungeonmobs.DungeonGateBlock;
import net.farmingtales.ftdungeonmobs.DungeonGatePalette;
import net.farmingtales.ftdungeonmobs.BloodPoolContent;
import net.farmingtales.ftdungeonmobs.DungeonArchitectureContent;
import net.farmingtales.ftdungeonmobs.FTDungeonMobs;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mod.EventBusSubscriber(modid = FTDungeonMobs.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DungeonClient {
    private DungeonClient() {}

    @SubscribeEvent
    public static void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.BUILDING_BLOCKS)) {
            DungeonArchitectureContent.ENTRIES.forEach(entry -> event.accept(entry.item()));
        }
        if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) {
            event.accept(FTDungeonMobs.WARRIOR_EGG);
            event.accept(FTDungeonMobs.ARCHER_EGG);
            event.accept(FTDungeonMobs.MAGE_EGG);
            event.accept(FTDungeonMobs.KING_EGG);
            net.farmingtales.ftdungeonmobs.ImportedCreatures.EGGS.forEach(event::accept);
        }
        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            event.accept(FTDungeonMobs.BLOODTHORN_BRAZIER_ITEM);
            event.accept(FTDungeonMobs.DUNGEON_ENTRY_GATE_ITEM);
            event.accept(FTDungeonMobs.DUNGEON_RETURN_GATE_ITEM);
            BloodPoolContent.BUILDING_ITEMS.forEach(event::accept);
            event.accept(BloodPoolContent.BLOOD_BUCKET);
        }
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ImportedCreatureRenderers.register(event);
        event.registerBlockEntityRenderer(FTDungeonMobs.DUNGEON_GATE_ENTITY.get(), DungeonGateRenderer::new);
        event.registerEntityRenderer(FTDungeonMobs.WARRIOR.get(), DungeonRenderer::new);
        event.registerEntityRenderer(FTDungeonMobs.ARCHER.get(), DungeonRenderer::new);
        event.registerEntityRenderer(FTDungeonMobs.MAGE.get(), DungeonRenderer::new);
        event.registerEntityRenderer(FTDungeonMobs.KING.get(), DungeonRenderer::new);
    }

    @SubscribeEvent
    public static void setupBloodFluid(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(BloodPoolContent.BLOOD.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BloodPoolContent.FLOWING_BLOOD.get(), RenderType.translucent());
        });
    }

    @SubscribeEvent
    public static void registerGateModels(ModelEvent.RegisterAdditional event) {
        for (int part = 0; part < 12; part++) {
            event.register(DungeonGateRenderer.movingModel(part));
        }
    }

    @SubscribeEvent
    public static void registerGateColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, position, tint) -> tint == 0
                ? DungeonGatePalette.color(state.getValue(DungeonGateBlock.PORTAL_TIER)) : -1,
                FTDungeonMobs.DUNGEON_ENTRY_GATE.get(), FTDungeonMobs.DUNGEON_RETURN_GATE.get());
    }

    @SubscribeEvent
    public static void registerGateItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> tint == 0 ? DungeonGatePalette.color(1) : -1,
                FTDungeonMobs.DUNGEON_ENTRY_GATE_ITEM.get(), FTDungeonMobs.DUNGEON_RETURN_GATE_ITEM.get());
    }

    private static final class DungeonRenderer extends GeoEntityRenderer<DungeonSkeletonEntity> {
        private DungeonRenderer(EntityRendererProvider.Context context) {
            super(context, new DungeonModel());
            shadowRadius = 0.5F;
        }
    }

    private static final class DungeonModel extends GeoModel<DungeonSkeletonEntity> {
        @Override
        public ResourceLocation getModelResource(DungeonSkeletonEntity entity) {
            return FTDungeonMobs.resource("geo/" + entity.variant().id() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(DungeonSkeletonEntity entity) {
            return FTDungeonMobs.resource("textures/entity/" + entity.variant().id() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(DungeonSkeletonEntity entity) {
            return FTDungeonMobs.resource("animations/" + entity.variant().id() + ".animation.json");
        }
    }
}
