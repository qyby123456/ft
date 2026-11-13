package net.farmingtales.ftdungeonmobs.client;

import net.farmingtales.ftdungeonmobs.FTDungeonMobs;
import net.farmingtales.ftdungeonmobs.ImportedCreatures;
import net.farmingtales.ftdungeonmobs.ImportedMonsterEntity;
import net.farmingtales.ftdungeonmobs.SkeletonMerchantEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.EntityRenderersEvent;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

final class ImportedCreatureRenderers {
    static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ImportedCreatures.GHOST_SCREAMER.get(), context ->
                new GeoEntityRenderer<ImportedMonsterEntity>(context, new Model<>("ghost_screamer")));
        event.registerEntityRenderer(ImportedCreatures.ORE_MONSTER.get(), context ->
                new GeoEntityRenderer<ImportedMonsterEntity>(context, new Model<>("ore_monster")));
        event.registerEntityRenderer(ImportedCreatures.SKELETON_MERCHANT.get(), context ->
                new GeoEntityRenderer<SkeletonMerchantEntity>(context, new Model<>("skeleton_merchant")));
    }

    private static final class Model<Creature extends Entity & GeoEntity> extends GeoModel<Creature> {
        private final String id;

        private Model(String id) {
            this.id = id;
        }

        @Override
        public ResourceLocation getModelResource(Creature entity) {
            return FTDungeonMobs.resource("geo/" + id + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(Creature entity) {
            return FTDungeonMobs.resource("textures/entity/" + id + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(Creature entity) {
            return FTDungeonMobs.resource("animations/" + id + ".animation.json");
        }
    }
}
