package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.client.model.ButcherBossModel;
import net.farmingtales.ftboss.client.render.BarrelCannonballRenderer;
import net.farmingtales.ftboss.client.render.BarrelMinionRenderer;
import net.farmingtales.ftboss.client.render.ChargedOrbRenderer;
import net.farmingtales.ftboss.client.render.DefectCoreRenderer;
import net.farmingtales.ftboss.client.render.GingerbreadMinionRenderer;
import net.farmingtales.ftboss.client.render.MaggotRenderer;
import net.farmingtales.ftboss.client.render.CompostRenderer;
import net.farmingtales.ftboss.client.render.PoopMinionRenderer;
import net.farmingtales.ftboss.client.render.MeatMountainRenderer;
import net.farmingtales.ftboss.client.render.RatGeoRenderer;
import net.farmingtales.ftboss.client.render.SugarQueenGeoRenderer;
import net.farmingtales.ftboss.client.render.TeaMinionRenderer;
import net.farmingtales.ftboss.client.render.TeaTreeRenderer;
import net.farmingtales.ftboss.client.render.WineBarrelGeoRenderer;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端注册(2026-09-12 按最后一次成功构建的字节码重建): 12 个实体的渲染器 + 屠夫的几何层。
 */
@Mod.EventBusSubscriber(modid = net.farmingtales.ftboss.FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FTClientSetup {

    private FTClientSetup() {}


    @SubscribeEvent
    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(FTEntities.TIDE_FISH_BOSS.get(),
                context -> new net.farmingtales.ftboss.client.render.PathGeoRenderer<>(context,
                        new net.farmingtales.ftboss.client.model.TideFishBossModel(), 1.1f));
        e.registerEntityRenderer(FTEntities.REEF_FISH.get(),
                context -> new net.farmingtales.ftboss.client.render.PathGeoRenderer<>(context,
                        new net.farmingtales.ftboss.client.model.PathGeoModel<>("geo/reef_fish.geo.json",
                                "textures/entity/reef_fish.png", "animations/reef_minion.animation.json"), 0.35f));
        e.registerEntityRenderer(FTEntities.BRINE_PUFFER.get(),
                context -> new net.farmingtales.ftboss.client.render.PathGeoRenderer<>(context,
                        new net.farmingtales.ftboss.client.model.PathGeoModel<>("geo/brine_puffer.geo.json",
                                "textures/entity/brine_puffer.png", "animations/reef_minion.animation.json"), 0.35f));
        e.registerEntityRenderer(FTEntities.TIDE_BOLT.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        e.registerEntityRenderer(FTEntities.QIZAI_STAR.get(), net.farmingtales.ftboss.client.render.QizaiStarRenderer::new);
        e.registerEntityRenderer(FTEntities.QIZAI_CRYSTAL.get(), net.farmingtales.ftboss.client.render.QizaiCrystalRenderer::new);
        e.registerEntityRenderer(FTEntities.QIZAI_CRYSTAL_MISSILE.get(), net.farmingtales.ftboss.client.render.QizaiCrystalMissileRenderer::new);
        e.registerEntityRenderer(FTEntities.QIZAI_SIGIL.get(), net.farmingtales.ftboss.client.render.QizaiSigilRenderer::new);
        e.registerEntityRenderer(FTEntities.QINGYI_CONFLUENCE_TRIDENT.get(),
                net.farmingtales.ftboss.client.render.QingyiConfluenceProjectileRenderer::new);
        e.registerEntityRenderer(FTEntities.SUGAR_QUEEN.get(), SugarQueenGeoRenderer::new);
        e.registerEntityRenderer(FTEntities.MEAT_MOUNTAIN.get(), MeatMountainRenderer::new);
        e.registerEntityRenderer(FTEntities.TEA_TREE.get(), TeaTreeRenderer::new);
        e.registerEntityRenderer(FTEntities.TEA_MINION.get(), TeaMinionRenderer::new);
        e.registerEntityRenderer(FTEntities.WINE_BARREL.get(), WineBarrelGeoRenderer::new);
        // 酒桶王分身(2026-09-12, 同一模型)
        e.registerEntityRenderer(FTEntities.WINE_BARREL_DECOY.get(),
                net.farmingtales.ftboss.client.render.WineBarrelDecoyRenderer::new);
        e.registerEntityRenderer(FTEntities.RAT_BOSS.get(), RatGeoRenderer::new);
        e.registerEntityRenderer(FTEntities.GINGERBREAD_MINION.get(), GingerbreadMinionRenderer::new);
        e.registerEntityRenderer(FTEntities.BARREL_CANNONBALL.get(), BarrelCannonballRenderer::new);
        e.registerEntityRenderer(FTEntities.BARREL_MINION.get(), BarrelMinionRenderer::new);
        e.registerEntityRenderer(FTEntities.DEFECT_CORE.get(), DefectCoreRenderer::new);
        e.registerEntityRenderer(FTEntities.CHARGED_ORB.get(), ChargedOrbRenderer::new);
        // 暗影珍珠: 占位用原版"投掷物物品"渲染器(等 shadow_pearl.png / 模型到位再换)
        e.registerEntityRenderer(FTEntities.SHADOW_PEARL.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        // 【！?区区？！】蛆小怪(2026-09-11)
        e.registerEntityRenderer(FTEntities.MAGGOT_MINION.get(), MaggotRenderer::new);
        // 炫彩答辩超人(2026-09-12)
        e.registerEntityRenderer(FTEntities.COMPOST_BOSS.get(), CompostRenderer::new);
        e.registerEntityRenderer(FTEntities.POOP_MINION.get(), PoopMinionRenderer::new);
        // 苍蝇(2026-09-12, 炫彩答辩超人的召唤物)
        e.registerEntityRenderer(FTEntities.FLY_MINION.get(),
                net.farmingtales.ftboss.client.render.FlyRenderer::new);
        // 棕黑史莱姆(2026-09-12, 复用原版史莱姆几何, 只换贴图)
        e.registerEntityRenderer(FTEntities.COMPOST_SLIME.get(),
                net.farmingtales.ftboss.client.render.CompostSlimeRenderer::new);
        // 羊屎蛋(2026-09-12, 岩浆怪套皮, 死亡爆炸)
        e.registerEntityRenderer(FTEntities.SHEEP_DUNG.get(),
                net.farmingtales.ftboss.client.render.SheepDungRenderer::new);
        // 庄园战召唤物(2026-10-10): 自有类型但外观与原版一致 —— 直接复用原版渲染器
        e.registerEntityRenderer(FTEntities.MANOR_HUSK.get(),
                net.minecraft.client.renderer.entity.HuskRenderer::new);
        e.registerEntityRenderer(FTEntities.MANOR_VINDICATOR.get(),
                net.minecraft.client.renderer.entity.VindicatorRenderer::new);
        // 苹果爵士(第 8 位 Boss) + 3 个召唤物: 召唤物只是换贴图, 用通用 PathGeoModel/Renderer
        e.registerEntityRenderer(FTEntities.APPLE_BOSS.get(),
                ctx -> new net.farmingtales.ftboss.client.render.PathGeoRenderer<>(ctx,
                        new net.farmingtales.ftboss.client.model.AppleBossModel(), 0.9f));
        e.registerEntityRenderer(FTEntities.APPLE_WORM.get(),
                ctx -> new net.farmingtales.ftboss.client.render.PathGeoRenderer<>(ctx,
                        new net.farmingtales.ftboss.client.model.PathGeoModel<>(
                                "geo/maggot_minion.geo.json", "textures/entity/apple_worm.png",
                                "animations/maggot_minion.animation.json"), 0.3f));
        e.registerEntityRenderer(FTEntities.APPLE_MOTH.get(),
                ctx -> new net.farmingtales.ftboss.client.render.PathGeoRenderer<>(ctx,
                        new net.farmingtales.ftboss.client.model.PathGeoModel<>(
                                "geo/fly_minion.geo.json", "textures/entity/apple_moth.png",
                                "animations/fly_minion.animation.json"), 0.2f));
        e.registerEntityRenderer(FTEntities.ROTTEN_APPLE.get(),
                ctx -> new net.farmingtales.ftboss.client.render.PathGeoRenderer<>(ctx,
                        new net.farmingtales.ftboss.client.model.PathGeoModel<>(
                                "geo/poop_minion.geo.json", "textures/entity/rotten_apple.png",
                                "animations/poop_minion.animation.json"), 0.35f));
        // 青苹果 / 金币人(2026-09-12): 同样只换贴图
        e.registerEntityRenderer(FTEntities.GREEN_APPLE.get(),
                ctx -> new net.farmingtales.ftboss.client.render.PathGeoRenderer<>(ctx,
                        new net.farmingtales.ftboss.client.model.PathGeoModel<>(
                                "geo/green_apple.geo.json", "textures/entity/green_apple.png",
                                "animations/green_apple.animation.json"), 0.3f));
        e.registerEntityRenderer(FTEntities.COIN_MAN.get(),
                ctx -> new net.farmingtales.ftboss.client.render.PathGeoRenderer<>(ctx,
                        new net.farmingtales.ftboss.client.model.PathGeoModel<>(
                                "geo/gingerbread_minion.geo.json", "textures/entity/coin_man.png",
                                "animations/gingerbread_minion.animation.json"), 0.4f));
        // 巨型果蛆(2026-09-12): 复用蛆的几何, 渲染放大 3.5 倍 -> "极大"
        e.registerEntityRenderer(FTEntities.GIANT_GRUB.get(),
                ctx -> new net.farmingtales.ftboss.client.render.PathGeoRenderer<>(ctx,
                        new net.farmingtales.ftboss.client.model.PathGeoModel<>(
                                "geo/maggot_minion.geo.json", "textures/entity/apple_worm.png",
                                "animations/maggot_minion.animation.json"), 1.6f).withScale(3.5f));

        // 甜苹果庄园·彩蛋 Boss(2026-09-12): 原版玩家模型 + 9 张皮肤
        e.registerEntityRenderer(FTEntities.EASTER_BOSS.get(),
                net.farmingtales.ftboss.client.render.EasterBossRenderer::new);

        // ★ 2026-09-24 折耳根的紫色残影: 独立的人形模型 + 紫色单色贴图 + 半透明管线
        e.registerEntityRenderer(FTEntities.SHADOW_PHANTOM.get(),
                net.farmingtales.ftboss.client.render.ShadowPhantomRenderer::new);

        // 苹果爵士分身(2026-09-12): 与本体同一套 geo/动画/贴图, 只是渲染时缩小一点便于区分
        e.registerEntityRenderer(FTEntities.APPLE_CLONE.get(),
                ctx -> new net.farmingtales.ftboss.client.render.PathGeoRenderer<>(ctx,
                        new net.farmingtales.ftboss.client.model.PathGeoModel<>(
                                "geo/apple_boss.geo.json", "textures/entity/apple_boss.png",
                                "animations/apple_boss.animation.json"), 0.8f).withScale(0.85f));
        // 彩色大便投掷物: 直接用原版物品投掷渲染器(零新增资源)
        e.registerEntityRenderer(FTEntities.RAINBOW_POOP.get(),
                ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 1.0f, true));
        // 飞轮回旋镖(文玄蓝): 2026-09-12 v42 换成**自定义渲染器**, 让它像真飞轮一样自转
        e.registerEntityRenderer(FTEntities.FLYWHEEL.get(),
                net.farmingtales.ftboss.client.render.FlywheelRenderer::new);
        // 雪黎的汤锅(2026-09-13 v76): 直接用原版物品投掷渲染器(零新增资源, 外观就是汤锅物品本身)
        e.registerEntityRenderer(FTEntities.STOCKPOT.get(),
                ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 1.2f, true));
        /* 2026-09-13 v103 雪黎的禁疗飞刀(和汤锅同一套"投掷物"渲染) */
        e.registerEntityRenderer(FTEntities.XUELI_KNIFE.get(),
                net.farmingtales.ftboss.client.render.XueliKnifeRenderer::new);
        // 青衣的三叉戟: 以原版 minecraft:trident 类型生成, 直接用原版渲染器, 这里无需注册

        /* v127: 第 9 位主 Boss【边牧犬王 benger】+ 它的跟班小狗(同一套 geo/动画, 小狗缩到 0.55) */
        e.registerEntityRenderer(FTEntities.BENGER.get(),
                ctx -> new net.farmingtales.ftboss.client.render.BengerRenderer(ctx, 1.0f));   // v133: 虚影半透明
        e.registerEntityRenderer(FTEntities.BENGER_PUP.get(),
                ctx -> new net.farmingtales.ftboss.client.render.PathGeoRenderer<>(ctx,
                        new net.farmingtales.ftboss.client.model.PathGeoModel<>(
                                "geo/benger.geo.json", "textures/entity/benger.png",
                                "animations/benger.animation.json"), 0.35f).withScale(0.55f));
    }

    @SubscribeEvent
    public static void onLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions e) {
        // 屠夫: 自持几何层(避开 EMF/CEM 对原版守卫士层的接管)
        e.registerLayerDefinition(ButcherBossModel.LAYER, ButcherBossModel::createBodyLayer);
        // 甜苹果庄园·彩蛋 Boss(2026-09-12): 标准玩家骨骼 + 4 根附加骨骼
        // (之前放在渲染器里的嵌套订阅类没生效 -> bakeLayer 拿到空模型 -> getChild 抛异常 -> 渲染器被 Lightspeed 换成空渲染器 -> 完全看不见)
        e.registerLayerDefinition(net.farmingtales.ftboss.client.model.EasterBossModel.LAYER,
                () -> net.farmingtales.ftboss.client.model.EasterBossModel.createBodyLayer(true));   // 2026-09-12: true = 细臂(Alex), 与沙耶一致, 匹配绝大多数自定义皮肤
    }

    /**
     * 隐藏 Boss 刷怪蛋的**两层染色**(2026-09-14 v109)。
     *
     * <p>那 10 个蛋的模型都指向原版 {@code minecraft:item/template_spawn_egg}
     * (layer0 = 蛋壳 {@code item/spawn_egg}, layer1 = 斑点 {@code item/spawn_egg_overlay}),
     * 所以**不需要新贴图** —— 只要在这里按 tintIndex 0/1 给每个蛋注册颜色即可(颜色写在物品实例里)。
     */
    @SubscribeEvent
    public static void onClientSetup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
                    net.farmingtales.ftboss.client.render.MaqueFeatherVisual.class);
            var colors = net.minecraft.client.Minecraft.getInstance().getItemColors();
            net.minecraft.world.item.Item[] eggs = net.farmingtales.ftboss.item.FTBossItems.EASTER_EGGS.stream()
                    .map(net.minecraftforge.registries.RegistryObject::get)
                    .toArray(net.minecraft.world.item.Item[]::new);
            colors.register((stack, tint) -> stack.getItem()
                    instanceof net.farmingtales.ftboss.item.EasterSpawnEggItem egg ? egg.getColor(tint) : 0xFFFFFF, eggs);

            /* v123: 【酒桶弩】装填后换成"装填完成"那一版模型。
               原版的 charged/pulling 谓词只注册给了 minecraft:crossbow, 我们自己的物品要自己注册一个
               (见 `models/item/barrel_crossbow.json` 的 overrides: predicate `ftboss:loaded`)。 */
            net.minecraft.client.renderer.item.ItemProperties.register(
                    net.farmingtales.ftboss.item.FTBossItems.BARREL_CROSSBOW.get(),
                    new net.minecraft.resources.ResourceLocation(net.farmingtales.ftboss.FTBossMod.MOD_ID, "loaded"),
                    (stack, level, entity, seed) -> net.farmingtales.ftboss.item.BarrelCrossbowItem.isLoaded(stack) ? 1.0F : 0.0F);

            /* v127: 第 9 位主 Boss【benger】的刷怪蛋用的是 Forge 的 {@code ForgeSpawnEggItem} ——
               它**不会**自己注册染色(只有彩蛋那 10 个蛋在下面那行手动染过色), 不补这一句的话
               蛋会渲染成"没上色的白蛋"。这里按原版 {@code SpawnEggItem#getColor} 给它上色。 */
            colors.register((stack, tint) -> stack.getItem() instanceof net.minecraft.world.item.SpawnEggItem egg
                            ? egg.getColor(tint) : 0xFFFFFF,
                    net.farmingtales.ftboss.item.FTBossItems.BENGER_SPAWN_EGG.get());
        });
    }
}
