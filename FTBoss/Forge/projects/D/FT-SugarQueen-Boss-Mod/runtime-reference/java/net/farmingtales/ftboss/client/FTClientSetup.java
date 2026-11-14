/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.color.item.ItemColors
 *  net.minecraft.client.renderer.entity.ThrownItemRenderer
 *  net.minecraft.client.renderer.item.ItemProperties
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.SpawnEggItem
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.EntityRenderersEvent$RegisterLayerDefinitions
 *  net.minecraftforge.client.event.EntityRenderersEvent$RegisterRenderers
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
 *  net.minecraftforge.registries.RegistryObject
 */
package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.client.model.AppleBossModel;
import net.farmingtales.ftboss.client.model.ButcherBossModel;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.model.PathGeoModel;
import net.farmingtales.ftboss.client.model.TideFishBossModel;
import net.farmingtales.ftboss.client.render.BarrelCannonballRenderer;
import net.farmingtales.ftboss.client.render.BarrelMinionRenderer;
import net.farmingtales.ftboss.client.render.BengerRenderer;
import net.farmingtales.ftboss.client.render.ChargedOrbRenderer;
import net.farmingtales.ftboss.client.render.CompostRenderer;
import net.farmingtales.ftboss.client.render.CompostSlimeRenderer;
import net.farmingtales.ftboss.client.render.DefectCoreRenderer;
import net.farmingtales.ftboss.client.render.EasterBossRenderer;
import net.farmingtales.ftboss.client.render.FlyRenderer;
import net.farmingtales.ftboss.client.render.FlywheelRenderer;
import net.farmingtales.ftboss.client.render.GingerbreadMinionRenderer;
import net.farmingtales.ftboss.client.render.MaggotRenderer;
import net.farmingtales.ftboss.client.render.MaqueFeatherVisual;
import net.farmingtales.ftboss.client.render.MeatMountainRenderer;
import net.farmingtales.ftboss.client.render.PathGeoRenderer;
import net.farmingtales.ftboss.client.render.PoopMinionRenderer;
import net.farmingtales.ftboss.client.render.QingyiConfluenceProjectileRenderer;
import net.farmingtales.ftboss.client.render.QizaiCrystalMissileRenderer;
import net.farmingtales.ftboss.client.render.QizaiCrystalRenderer;
import net.farmingtales.ftboss.client.render.QizaiSigilRenderer;
import net.farmingtales.ftboss.client.render.QizaiStarRenderer;
import net.farmingtales.ftboss.client.render.RatGeoRenderer;
import net.farmingtales.ftboss.client.render.ShadowPhantomRenderer;
import net.farmingtales.ftboss.client.render.SheepDungRenderer;
import net.farmingtales.ftboss.client.render.SugarQueenGeoRenderer;
import net.farmingtales.ftboss.client.render.TeaMinionRenderer;
import net.farmingtales.ftboss.client.render.TeaTreeRenderer;
import net.farmingtales.ftboss.client.render.WineBarrelDecoyRenderer;
import net.farmingtales.ftboss.client.render.WineBarrelGeoRenderer;
import net.farmingtales.ftboss.client.render.XueliKnifeRenderer;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.item.BarrelCrossbowItem;
import net.farmingtales.ftboss.item.EasterSpawnEggItem;
import net.farmingtales.ftboss.item.FTBossItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class FTClientSetup {
    private FTClientSetup() {
    }

    @SubscribeEvent
    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer((EntityType)FTEntities.TIDE_FISH_BOSS.get(), context -> new PathGeoRenderer<TideFishBossEntity>(context, new TideFishBossModel(), 1.1f));
        e.registerEntityRenderer((EntityType)FTEntities.REEF_FISH.get(), context -> new PathGeoRenderer(context, new PathGeoModel("geo/reef_fish.geo.json", "textures/entity/reef_fish.png", "animations/reef_minion.animation.json"), 0.35f));
        e.registerEntityRenderer((EntityType)FTEntities.BRINE_PUFFER.get(), context -> new PathGeoRenderer(context, new PathGeoModel("geo/brine_puffer.geo.json", "textures/entity/brine_puffer.png", "animations/reef_minion.animation.json"), 0.35f));
        e.registerEntityRenderer((EntityType)FTEntities.TIDE_BOLT.get(), ThrownItemRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.QIZAI_STAR.get(), QizaiStarRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.QIZAI_CRYSTAL.get(), QizaiCrystalRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.QIZAI_CRYSTAL_MISSILE.get(), QizaiCrystalMissileRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.QIZAI_SIGIL.get(), QizaiSigilRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.QINGYI_CONFLUENCE_TRIDENT.get(), QingyiConfluenceProjectileRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.SUGAR_QUEEN.get(), SugarQueenGeoRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.MEAT_MOUNTAIN.get(), MeatMountainRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.TEA_TREE.get(), TeaTreeRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.TEA_MINION.get(), TeaMinionRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.WINE_BARREL.get(), WineBarrelGeoRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.WINE_BARREL_DECOY.get(), WineBarrelDecoyRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.RAT_BOSS.get(), RatGeoRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.GINGERBREAD_MINION.get(), GingerbreadMinionRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.BARREL_CANNONBALL.get(), BarrelCannonballRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.BARREL_MINION.get(), BarrelMinionRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.DEFECT_CORE.get(), DefectCoreRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.CHARGED_ORB.get(), ChargedOrbRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.SHADOW_PEARL.get(), ThrownItemRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.MAGGOT_MINION.get(), MaggotRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.COMPOST_BOSS.get(), CompostRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.POOP_MINION.get(), PoopMinionRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.FLY_MINION.get(), FlyRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.COMPOST_SLIME.get(), CompostSlimeRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.SHEEP_DUNG.get(), SheepDungRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.APPLE_BOSS.get(), ctx -> new PathGeoRenderer<AppleBossEntity>(ctx, new AppleBossModel(), 0.9f));
        e.registerEntityRenderer((EntityType)FTEntities.APPLE_WORM.get(), ctx -> new PathGeoRenderer(ctx, new PathGeoModel("geo/maggot_minion.geo.json", "textures/entity/apple_worm.png", "animations/maggot_minion.animation.json"), 0.3f));
        e.registerEntityRenderer((EntityType)FTEntities.APPLE_MOTH.get(), ctx -> new PathGeoRenderer(ctx, new PathGeoModel("geo/fly_minion.geo.json", "textures/entity/apple_moth.png", "animations/fly_minion.animation.json"), 0.2f));
        e.registerEntityRenderer((EntityType)FTEntities.ROTTEN_APPLE.get(), ctx -> new PathGeoRenderer(ctx, new PathGeoModel("geo/poop_minion.geo.json", "textures/entity/rotten_apple.png", "animations/poop_minion.animation.json"), 0.35f));
        e.registerEntityRenderer((EntityType)FTEntities.GREEN_APPLE.get(), ctx -> new PathGeoRenderer(ctx, new PathGeoModel("geo/green_apple.geo.json", "textures/entity/green_apple.png", "animations/green_apple.animation.json"), 0.3f));
        e.registerEntityRenderer((EntityType)FTEntities.COIN_MAN.get(), ctx -> new PathGeoRenderer(ctx, new PathGeoModel("geo/gingerbread_minion.geo.json", "textures/entity/coin_man.png", "animations/gingerbread_minion.animation.json"), 0.4f));
        e.registerEntityRenderer((EntityType)FTEntities.GIANT_GRUB.get(), ctx -> new PathGeoRenderer(ctx, new PathGeoModel("geo/maggot_minion.geo.json", "textures/entity/apple_worm.png", "animations/maggot_minion.animation.json"), 1.6f).withScale(3.5f));
        e.registerEntityRenderer((EntityType)FTEntities.EASTER_BOSS.get(), EasterBossRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.SHADOW_PHANTOM.get(), ShadowPhantomRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.APPLE_CLONE.get(), ctx -> new PathGeoRenderer(ctx, new PathGeoModel("geo/apple_boss.geo.json", "textures/entity/apple_boss.png", "animations/apple_boss.animation.json"), 0.8f).withScale(0.85f));
        e.registerEntityRenderer((EntityType)FTEntities.RAINBOW_POOP.get(), ctx -> new ThrownItemRenderer(ctx, 1.0f, true));
        e.registerEntityRenderer((EntityType)FTEntities.FLYWHEEL.get(), FlywheelRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.STOCKPOT.get(), ctx -> new ThrownItemRenderer(ctx, 1.2f, true));
        e.registerEntityRenderer((EntityType)FTEntities.XUELI_KNIFE.get(), XueliKnifeRenderer::new);
        e.registerEntityRenderer((EntityType)FTEntities.BENGER.get(), ctx -> new BengerRenderer(ctx, 1.0f));
        e.registerEntityRenderer((EntityType)FTEntities.BENGER_PUP.get(), ctx -> new PathGeoRenderer(ctx, new PathGeoModel("geo/benger.geo.json", "textures/entity/benger.png", "animations/benger.animation.json"), 0.35f).withScale(0.55f));
    }

    @SubscribeEvent
    public static void onLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(ButcherBossModel.LAYER, ButcherBossModel::createBodyLayer);
        e.registerLayerDefinition(EasterBossModel.LAYER, () -> EasterBossModel.createBodyLayer(true));
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.register(MaqueFeatherVisual.class);
            ItemColors colors = Minecraft.m_91087_().getItemColors();
            Item[] eggs = (Item[])FTBossItems.EASTER_EGGS.stream().map(RegistryObject::get).toArray(Item[]::new);
            colors.m_92689_((stack, tint) -> {
                int n;
                Item patt12792$temp = stack.m_41720_();
                if (patt12792$temp instanceof EasterSpawnEggItem) {
                    EasterSpawnEggItem egg = (EasterSpawnEggItem)patt12792$temp;
                    n = egg.getColor(tint);
                } else {
                    n = 0xFFFFFF;
                }
                return n;
            }, (ItemLike[])eggs);
            ItemProperties.register((Item)((Item)FTBossItems.BARREL_CROSSBOW.get()), (ResourceLocation)new ResourceLocation("ftboss", "loaded"), (stack, level, entity, seed) -> BarrelCrossbowItem.isLoaded(stack) ? 1.0f : 0.0f);
            colors.m_92689_((stack, tint) -> {
                int n;
                Item patt13803$temp = stack.m_41720_();
                if (patt13803$temp instanceof SpawnEggItem) {
                    SpawnEggItem egg = (SpawnEggItem)patt13803$temp;
                    n = egg.m_43211_(tint);
                } else {
                    n = 0xFFFFFF;
                }
                return n;
            }, new ItemLike[]{(ItemLike)FTBossItems.BENGER_SPAWN_EGG.get()});
        });
    }
}

