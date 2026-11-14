package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 本模组全部实体类型(2026-09-12 按"最后一次成功构建的字节码"精确重建, 注册顺序/尺寸/属性顺序保持不变)。
 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FTEntities {

    public static final DeferredRegister<EntityType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FTBossMod.MOD_ID);

    public static final RegistryObject<EntityType<TideFishBossEntity>> TIDE_FISH_BOSS =
            register("tide_fish_boss", TideFishBossEntity::new, 2.4f, 3.5f);
    public static final RegistryObject<EntityType<ReefMinionEntity>> REEF_FISH =
            register("reef_fish", ReefMinionEntity::new, 0.8f, 0.7f);
    public static final RegistryObject<EntityType<ReefMinionEntity>> BRINE_PUFFER =
            register("brine_puffer", ReefMinionEntity::new, 0.8f, 0.8f);
    public static final RegistryObject<EntityType<TideBoltEntity>> TIDE_BOLT =
            register("tide_bolt", TideBoltEntity::new, 0.25f, 0.25f, MobCategory.MISC);

    public static final RegistryObject<EntityType<QizaiStarEntity>> QIZAI_STAR = TYPES.register("qizai_star",
            () -> EntityType.Builder.<QizaiStarEntity>of(QizaiStarEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F).clientTrackingRange(8).updateInterval(1).build("ftboss:qizai_star"));
    public static final RegistryObject<EntityType<QizaiCrystalEntity>> QIZAI_CRYSTAL = TYPES.register("qizai_crystal",
            () -> EntityType.Builder.<QizaiCrystalEntity>of(QizaiCrystalEntity::new, MobCategory.MISC)
                    .sized(0.85F, 1.35F).clientTrackingRange(10).updateInterval(2).noSave().noSummon().build("ftboss:qizai_crystal"));
    public static final RegistryObject<EntityType<QizaiCrystalMissileEntity>> QIZAI_CRYSTAL_MISSILE = TYPES.register("qizai_crystal_missile",
            () -> EntityType.Builder.<QizaiCrystalMissileEntity>of(QizaiCrystalMissileEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(10).updateInterval(1).noSave().noSummon().build("ftboss:qizai_crystal_missile"));
    public static final RegistryObject<EntityType<QizaiSigilEntity>> QIZAI_SIGIL = TYPES.register("qizai_sigil",
            () -> EntityType.Builder.<QizaiSigilEntity>of(QizaiSigilEntity::new, MobCategory.MISC)
                    .sized(1, 1).clientTrackingRange(8).updateInterval(20).noSave().noSummon().build("ftboss:qizai_sigil"));
    public static final RegistryObject<EntityType<QingyiConfluenceTridentEntity>> QINGYI_CONFLUENCE_TRIDENT =
            TYPES.register("qingyi_confluence_trident", () -> EntityType.Builder
                    .<QingyiConfluenceTridentEntity>of(QingyiConfluenceTridentEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(1).build("ftboss:qingyi_confluence_trident"));

    /** 糖霜女皇 / 姜饼王 */
    public static final RegistryObject<EntityType<SugarQueenEntity>> SUGAR_QUEEN =
            register("sugar_queen", SugarQueenEntity::new, 1.2f, 2.7f);
    /** 屠夫(肉山) */
    public static final RegistryObject<EntityType<MeatMountainEntity>> MEAT_MOUNTAIN =
            register("meat_mountain", MeatMountainEntity::new, 1.5f, 2.6f);
    /** 酒桶王的分身(2026-09-12): 与酒桶王同一模型, 1 血、不攻击、无任何 Boss 逻辑 */
    public static final RegistryObject<EntityType<WineBarrelDecoyEntity>> WINE_BARREL_DECOY =
            register("wine_barrel_decoy", WineBarrelDecoyEntity::new, 3.0f, 3.0f);
    /** ★ 2026-09-24: 折耳根的紫色残影 —— 纯视觉实体, 无碰撞体积/无攻击性（见 {@link ShadowPhantomEntity}） */
    public static final RegistryObject<EntityType<ShadowPhantomEntity>> SHADOW_PHANTOM =
            register("shadow_phantom", ShadowPhantomEntity::new,
                    ShadowPhantomEntity.WIDTH, ShadowPhantomEntity.HEIGHT, MobCategory.MISC);
    /** 酒桶王 */
    public static final RegistryObject<EntityType<WineBarrelBossEntity>> WINE_BARREL =
            register("wine_barrel", WineBarrelBossEntity::new, 3.0f, 3.0f);
    /** 鼠群首领 */
    public static final RegistryObject<EntityType<RatBossEntity>> RAT_BOSS =
            register("rat_boss", RatBossEntity::new, 0.6f, 0.975f);
    /** 茶小怪 */
    public static final RegistryObject<EntityType<TeaMinionEntity>> TEA_MINION =
            register("tea_minion", TeaMinionEntity::new, 0.8f, 1.3f);
    /** 姜饼小怪 */
    public static final RegistryObject<EntityType<GingerbreadMinionEntity>> GINGERBREAD_MINION =
            register("gingerbread_minion", GingerbreadMinionEntity::new, 0.8f, 1.1f);
    /** 故障机器人(第 6 个 Boss) */
    public static final RegistryObject<EntityType<DefectCoreEntity>> DEFECT_CORE =
            register("defect_core", DefectCoreEntity::new, 2.4f, 3.8f);
    /** 充能球 */
    public static final RegistryObject<EntityType<ChargedOrbEntity>> CHARGED_ORB =
            register("charged_orb", ChargedOrbEntity::new, 0.5f, 0.5f);
    /** 【！?区区？！】蛆小怪(故障机器人的召唤物) */
    public static final RegistryObject<EntityType<MaggotMinionEntity>> MAGGOT_MINION =
            register("maggot_minion", MaggotMinionEntity::new, 0.7f, 0.5f);
    /** 小酒桶(酒桶王召唤物) */
    public static final RegistryObject<EntityType<SmallBarrelEntity>> BARREL_MINION =
            register("barrel_minion", SmallBarrelEntity::new, 0.8f, 1.2f);
    /** 酒桶炮弹 */
    public static final RegistryObject<EntityType<BarrelCannonballEntity>> BARREL_CANNONBALL =
            register("barrel_cannonball", BarrelCannonballEntity::new, 0.5f, 0.5f,
                    MobCategory.MISC);
    /** 彩色大便投掷物(2026-09-12): 一阶段棕色 / 二阶段彩色, 落地可生成肥水坑 */
    public static final RegistryObject<EntityType<RainbowPoopEntity>> RAINBOW_POOP =
            register("rainbow_poop", RainbowPoopEntity::new, 0.5f, 0.5f, MobCategory.MISC);
    /** 飞轮回旋镖(2026-09-12 v22): 文玄蓝扔出的追踪飞轮(参考"光之女王的飞弹"的手感) */
    /**
     * 【裂瞳投珠】暗影珍珠投射物。
     *
     * <p>⚠ <b>必须用 {@link MobCategory#MISC}</b>（v156 修）：本类以前套的是默认的
     * {@code MONSTER} 那层重载，于是它在原版 {@code DefaultAttributes.validate()} 眼里
     * 就是"一只没有属性表的怪"，每次启动刷 378 行
     * {@code Entity ftboss:shadow_pearl has no attributes}。
     *
     * <p>原版 {@code validate()} 的两个条件（{@code javap} 实测，Forge 1.20.1-47.4.10）：
     * <pre>filter(type -> type.getCategory() != MobCategory.MISC)
     * filter(type -> !DefaultAttributes.hasSupplier(type))</pre>
     * 它<b>只按 category 过滤，完全不检查是不是 {@code LivingEntity}</b> ——
     * 所以"给投射物注册属性"这条路走不通（{@code EntityAttributeCreationEvent.put}
     * 的形参是 {@code EntityType<? extends LivingEntity>}，珍珠不是 LivingEntity，
     * 编译期就过不去）；正确做法是把它归到 {@code MISC}，与同工程的
     * {@code BARREL_CANNONBALL} / {@code RAINBOW_POOP} / {@code FLYWHEEL} /
     * {@code STOCKPOT} / {@code XUELI_KNIFE} 一致。
     */
    public static final RegistryObject<EntityType<ShadowPearlEntity>> SHADOW_PEARL =
            register("shadow_pearl", ShadowPearlEntity::new, 0.25f, 0.25f, MobCategory.MISC);

    public static final RegistryObject<EntityType<FlywheelProjectileEntity>> FLYWHEEL =
            register("flywheel_projectile", FlywheelProjectileEntity::new, 0.4f, 0.4f, MobCategory.MISC);

    public static final RegistryObject<EntityType<PlayerMoonChakramEntity>> PLAYER_MOON_CHAKRAM =
            TYPES.register("player_moon_chakram", () -> EntityType.Builder
                    .<PlayerMoonChakramEntity>of(PlayerMoonChakramEntity::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F).clientTrackingRange(8).updateInterval(1)
                    .build(new ResourceLocation(FTBossMod.MOD_ID, "player_moon_chakram").toString()));
    /** 雪黎的汤锅(2026-09-13 v76): 整个扔出去砸人的远程武器 */
    public static final RegistryObject<EntityType<StockpotEntity>> STOCKPOT =
            register("stockpot", StockpotEntity::new, 0.5f, 0.5f, MobCategory.MISC);

    /** 【雪黎的禁疗飞刀】(2026-09-13 v103): 命中玩家 -> 少量伤害 + 禁疗 30 秒 */
    public static final RegistryObject<EntityType<XueliKnifeEntity>> XUELI_KNIFE =
            register("xueli_knife", XueliKnifeEntity::new, 0.4f, 0.4f, MobCategory.MISC);
    /* 注: 青衣的三叉戟不走自定义实体类型 —— QingyiTridentEntity 以原版 minecraft:trident 类型生成,
       服务端跑子类逻辑(命中回调), 客户端用原版渲染, 因此这里不需要注册类型/渲染器。 */
    /** 大便小怪(2026-09-12): 可移动, 撞到玩家叠缓慢/饥饿 */
    public static final RegistryObject<EntityType<PoopMinionEntity>> POOP_MINION =
            register("poop_minion", PoopMinionEntity::new, 0.8f, 0.9f);
    /** 苍蝇(2026-09-12, 炫彩答辩超人的召唤物): 无重力飞行, 咬到只刷新「屎？！！」时长 */
    public static final RegistryObject<EntityType<FlyMinionEntity>> FLY_MINION =
            register("fly_minion", FlyMinionEntity::new, 0.7f, 0.6f);
    /** 棕黑史莱姆(2026-09-12, 炫彩答辩超人的召唤物): 继承原版史莱姆, 只换棕黑贴图 */
    public static final RegistryObject<EntityType<CompostSlimeEntity>> COMPOST_SLIME =
            register("compost_slime", CompostSlimeEntity::new, 1.0f, 1.0f);
    /** 羊屎蛋(2026-09-12, 炫彩答辩超人的召唤物): 岩浆怪套皮, 死亡后炸开 */
    public static final RegistryObject<EntityType<SheepDungEntity>> SHEEP_DUNG =
            register("sheep_dung", SheepDungEntity::new, 0.8f, 0.8f);
    /* ===== 庄园战召唤物(2026-10-10 按用户要求新增)=====
       用户原话「庄园boss无视效果：斩首，摸金校尉。不作为亡灵生物受到部分武器增伤」。
       此前屠夫/糖霜的波次**直接生成原版 husk / vindicator**，被第三方 mod 的
       `#kaleidoscope_tavern:tomb_raider_disarmable`(缴械白名单)与
       `#kaleidoscope_world_liquor:bosses`(斩首的"是不是boss"判定)按**原版类型 id** 命中；
       换成自有类型后不在任何白名单里, 且可主动加进 boss 名单(见本模组
       resources/data/kaleidoscope_world_liquor/tags/entity_types/bosses.json)。 */
    /** 庄园尸壳: 屠夫「腌肉尸壳」(kind 1) 与 糖霜女皇「焦糖结晶」(kind 1) 共用; getMobType=UNDEFINED */
    public static final RegistryObject<EntityType<ManorHuskEntity>> MANOR_HUSK =
            register("manor_husk", ManorHuskEntity::new, 0.6f, 1.95f);
    /** 庄园卫道士: 屠夫「学徒」(kind 0) 与「队长」(kind 2) 共用, 属性/装备仍由 boostMinion(kind) 区分 */
    public static final RegistryObject<EntityType<ManorVindicatorEntity>> MANOR_VINDICATOR =
            register("manor_vindicator", ManorVindicatorEntity::new, 0.6f, 1.95f);
    /* ===== 苹果爵士(第 8 位 Boss)与它的召唤物(2026-09-12) ===== */
    /** 果虫: 复用蛆的几何/动画, 只换苹果果肉色贴图 */
    public static final RegistryObject<EntityType<AppleWormEntity>> APPLE_WORM =
            register("apple_worm", AppleWormEntity::new, 0.7f, 0.5f);
    /** 果蛾: 复用苍蝇的几何/动画, 只换奶油色贴图 */
    public static final RegistryObject<EntityType<AppleMothEntity>> APPLE_MOTH =
            register("apple_moth", AppleMothEntity::new, 0.7f, 0.6f);
    /** 烂苹果堆: 复用大便小怪的几何/动画, 只换腐烂贴图, 死亡炸开 */
    public static final RegistryObject<EntityType<RottenAppleEntity>> ROTTEN_APPLE =
            register("rotten_apple", RottenAppleEntity::new, 0.8f, 0.9f);

    /** 【青苹果】(2026-09-12 按用户要求新增): 召唤物 · 命中挂 中毒/饥饿/虚弱 */
    public static final RegistryObject<EntityType<GreenAppleEntity>> GREEN_APPLE =
            register("green_apple", GreenAppleEntity::new, 0.8f, 0.9f);

    /** 【金币人】(2026-09-12 按用户要求新增): 召唤物 · 命中挂 缓慢/挖掘疲劳 + 短定身 */
    public static final RegistryObject<EntityType<CoinManEntity>> COIN_MAN =
            register("coin_man", CoinManEntity::new, 0.7f, 1.5f);

    /** 【苹果爵士·分身】(2026-09-12): 会追人冲撞自爆的复制体 */
    public static final RegistryObject<EntityType<AppleCloneEntity>> APPLE_CLONE =
            register("apple_clone", AppleCloneEntity::new, 1.5f, 2.4f);

    /** 【甜苹果庄园·彩蛋 Boss】(2026-09-12): 9 位人形 Boss 共用一个实体类型, 用 variant 区分 */
    public static final RegistryObject<EntityType<EasterBossEntity>> EASTER_BOSS =
            register("easter_boss", EasterBossEntity::new, 0.6f, 1.8f);   // 玩家尺寸(armamentarium Boss 体型)

    /** 【巨型果蛆】(2026-09-12): 苹果爵士召来的极大果蛆, 咬中挂 反胃/饥饿/感染 */
    public static final RegistryObject<EntityType<GiantGrubEntity>> GIANT_GRUB =
            register("giant_grub", GiantGrubEntity::new, 3.0f, 2.0f);
    /** 苹果爵士·蜜脆(第 8 号 Boss) */
    public static final RegistryObject<EntityType<AppleBossEntity>> APPLE_BOSS =
            register("apple_boss", AppleBossEntity::new, 1.7f, 2.7f);
    /** 炫彩答辩超人(第 7 号 Boss, 2026-09-12): 肥料/堆肥主题, 地面型, 手持 Create 土豆加农炮 */
    public static final RegistryObject<EntityType<CompostBossEntity>> COMPOST_BOSS =
            register("compost_boss", CompostBossEntity::new, 1.6f, 3.4f);
    /** 茶树 Boss(沙耶) */
    public static final RegistryObject<EntityType<TeaTreeBossEntity>> TEA_TREE =
            register("tea_tree", TeaTreeBossEntity::new, 1.3f, 2.1f);

    /* ===== 2026-09-14 v127 第 9 位主 Boss【边牧犬王 benger】+ 它的跟班小狗 ===== */
    /** 边牧犬王（模型 1.63 x 3.63 格 → 碰撞箱 1.7 x 3.5） */
    public static final RegistryObject<EntityType<BengerEntity>> BENGER =
            register("benger", BengerEntity::new, 1.7f, 3.5f);
    /** 小狗（同一套 geo，渲染缩到 0.55） */
    public static final RegistryObject<EntityType<BengerPupEntity>> BENGER_PUP =
            register("benger_pup", BengerPupEntity::new, 0.9f, 2.0f);

    /** 标准注册(怪物分类) */
    private static <T extends Entity> RegistryObject<EntityType<T>> register(
            String id, EntityType.EntityFactory<T> factory, float width, float height) {
        return register(id, factory, width, height, MobCategory.MONSTER);
    }

    /** 指定分类注册 */
    private static <T extends Entity> RegistryObject<EntityType<T>> register(
            String id, EntityType.EntityFactory<T> factory, float width, float height, MobCategory category) {
        return TYPES.register(id, () -> EntityType.Builder
                .of(factory, category)
                .sized(width, height)
                .clientTrackingRange(10)
                .build(new ResourceLocation(FTBossMod.MOD_ID, id).toString()));
    }

    /** 属性注册(顺序与原版一致) */
    /**
     * v141 统一挂属性 + **启动自检**（运行时判定，不靠人眼看代码）：
     * 任何 Boss/召唤物的属性表缺 ATTACK_DAMAGE 都会在启动日志里被 ERROR 点名 ——
     * 这正是 v66（屠夫用错 createMobAttributes）与本次（充能球）崩服的根因。
     */
    private static void attr(EntityAttributeCreationEvent e,
                             net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity> type,
                             net.minecraft.world.entity.ai.attributes.AttributeSupplier sup) {
        e.put(type, sup);
        try {
            if (!sup.hasAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)) {
                FTBossMod.LOGGER.error("[FTBoss] 属性自检: {} 的属性表缺 ATTACK_DAMAGE —— 它一近战就会崩服, 请在其 createAttributes() 里补上!",
                        net.minecraft.world.entity.EntityType.getKey(type));
            }
        } catch (Throwable ignored) {}
    }

    @SubscribeEvent
    public void onAttributeCreation(EntityAttributeCreationEvent e) {
        attr(e, QIZAI_CRYSTAL.get(), QizaiCrystalEntity.createAttributes().build());
        attr(e, TIDE_FISH_BOSS.get(), TideFishBossEntity.createAttributes().build());
        attr(e, REEF_FISH.get(), ReefMinionEntity.createAttributes().build());
        attr(e, BRINE_PUFFER.get(), ReefMinionEntity.createAttributes().build());
        attr(e, SUGAR_QUEEN.get(), SugarQueenEntity.createAttributes().build());
        /* 2026-09-12 v66 修崩服: 以前这里手误写成 `MeatMountainEntity.createMobAttributes()`
           —— 那是 **Mob 的通用构造器**, **不含 ATTACK_DAMAGE**, 血量/移速也是默认值(20 / 0.7)。
           于是"柠檬猫召唤的屠夫·助战"开了克隆近战 AI(MeleeAttackGoal) 后, 第一次挥拳读
           ATTACK_DAMAGE 就抛 IllegalArgumentException → **服务端 Ticking entity 崩溃**
           (崩报里那个实体正是 max_health:20 / movement_speed:0.7 且没有 attack_damage)。 */
        attr(e, MEAT_MOUNTAIN.get(), MeatMountainEntity.createAttributes().build());
        attr(e, TEA_TREE.get(), TeaTreeBossEntity.createAttributes().build());
        attr(e, COMPOST_BOSS.get(), CompostBossEntity.createAttributes().build());
        attr(e, POOP_MINION.get(), PoopMinionEntity.createAttributes().build());
        attr(e, FLY_MINION.get(), FlyMinionEntity.createAttributes().build());
        attr(e, COMPOST_SLIME.get(), CompostSlimeEntity.createAttributes().build());
        attr(e, SHEEP_DUNG.get(), SheepDungEntity.createAttributes().build());
        /* 2026-10-10: 庄园战召唤物(原版 husk/vindicator 换自有类型); 属性表与原版一致 */
        attr(e, MANOR_HUSK.get(), ManorHuskEntity.createAttributes().build());
        attr(e, MANOR_VINDICATOR.get(), ManorVindicatorEntity.createAttributes().build());
        attr(e, APPLE_WORM.get(), AppleWormEntity.createAttributes().build());
        attr(e, APPLE_MOTH.get(), AppleMothEntity.createAttributes().build());
        attr(e, ROTTEN_APPLE.get(), RottenAppleEntity.createAttributes().build());
        attr(e, GREEN_APPLE.get(), GreenAppleEntity.createAttributes().build());
        attr(e, COIN_MAN.get(), CoinManEntity.createAttributes().build());
        attr(e, APPLE_CLONE.get(), AppleCloneEntity.createAttributes().build());
        attr(e, EASTER_BOSS.get(), EasterBossEntity.createAttributes().build());
        attr(e, GIANT_GRUB.get(), GiantGrubEntity.createAttributes().build());
        attr(e, APPLE_BOSS.get(), AppleBossEntity.createAttributes().build());
        attr(e, WINE_BARREL.get(), WineBarrelBossEntity.createAttributes().build());
        attr(e, WINE_BARREL_DECOY.get(), WineBarrelDecoyEntity.createAttributes().build());
        attr(e, SHADOW_PHANTOM.get(), ShadowPhantomEntity.createAttributes().build());
        attr(e, RAT_BOSS.get(), RatBossEntity.createAttributes().build());
        attr(e, TEA_MINION.get(), TeaMinionEntity.createAttributes().build());
        attr(e, GINGERBREAD_MINION.get(), GingerbreadMinionEntity.createAttributes().build());
        attr(e, BARREL_MINION.get(), SmallBarrelEntity.createAttributes().build());
        attr(e, DEFECT_CORE.get(), DefectCoreEntity.createAttributes().build());
        attr(e, CHARGED_ORB.get(), ChargedOrbEntity.createAttributes().build());
        attr(e, MAGGOT_MINION.get(), MaggotMinionEntity.createAttributes().build());
        /* v127: 边牧犬王 benger + 小狗 */
        attr(e, BENGER.get(), BengerEntity.createAttributes().build());
        attr(e, BENGER_PUP.get(), BengerPupEntity.createAttributes().build());
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.register(new FTEntities());
    }
}
