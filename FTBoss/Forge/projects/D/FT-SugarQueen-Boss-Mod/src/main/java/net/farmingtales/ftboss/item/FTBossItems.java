package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Boss mod 的物品注册 */
public final class FTBossItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FTBossMod.MOD_ID);

    public static final RegistryObject<Item> TIDE_BANQUET = ITEMS.register("tide_banquet",
            () -> new TideBanquetItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> TIDE_TRIDENT = ITEMS.register("tide_trident",
            () -> new TideTridentItem(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> TIDE_PEARL = ITEMS.register("tide_pearl",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MUSIC_DISC_TIDE_FISH = ITEMS.register("music_disc_tide_fish",
            () -> new net.minecraft.world.item.RecordItem(15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_TIDE_FISH,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 1980));
    public static final RegistryObject<Item> MUSIC_DISC_TIDE_FISH_P2 = ITEMS.register("music_disc_tide_fish_p2",
            () -> new net.minecraft.world.item.RecordItem(15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_TIDE_FISH_P2,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 1927));

    public static final RegistryObject<Item> DORA_NOVA_LAUNCHER = ITEMS.register("dora_nova_launcher",
            net.farmingtales.ftboss.item.weapons.DoraSubmachineGunItem::new);
    public static final RegistryObject<Item> DORA_PULSE_GUN = ITEMS.register("dora_pulse_gun",
            () -> new net.farmingtales.ftboss.item.weapons.DoraWeaponItem(false));
    public static final RegistryObject<Item> DORA_LANCE = ITEMS.register("dora_lance",
            () -> new net.farmingtales.ftboss.item.weapons.DoraWeaponItem(true));

    public static final RegistryObject<Item> MAQUE_BLESSING_FEATHER =
            ITEMS.register("maque_blessing_feather", net.farmingtales.ftboss.item.weapons.MaqueBlessingFeatherItem::new);

    public static final RegistryObject<Item> SUGAR_QUEEN_CRYSTAL =
            ITEMS.register("sugar_queen_crystal", SugarQueenCrystalItem::new);

    public static final RegistryObject<Item> MANOR_STOP_TOKEN =
            ITEMS.register("manor_stop_token", ManorStopTokenItem::new);

    public static final RegistryObject<Item> MEAT_HEART =
            ITEMS.register("meat_heart", MeatHeartItem::new);

    // ===== 故障机器人(第 6 个 Boss, 2026-09-11) =====
    /** 故障机器人: 右键召唤 Boss(可多只) */
    public static final RegistryObject<Item> DEFECT_CORE =
            ITEMS.register("defect_core", DefectCoreItem::new);

    public static final RegistryObject<Item> DEFECT_CLAW =
            ITEMS.register("defect_claw", () -> new net.minecraft.world.item.SwordItem(
                    net.minecraft.world.item.Tiers.DIAMOND, 3, -2.4F,
                    new Item.Properties().rarity(Rarity.EPIC)));

    /** 茶树种子(占位召唤物) */
    public static final RegistryObject<Item> TEA_SEED =
            ITEMS.register("tea_seed", TeaSeedItem::new);

    public static final RegistryObject<Item> THORN_CROWN =
            ITEMS.register("thorn_crown", ThornCrownItem::new);
    public static final RegistryObject<Item> THORN_CHESTPLATE = ITEMS.register("thorn_chestplate",
            () -> new ThornArmorItem(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> THORN_LEGGINGS = ITEMS.register("thorn_leggings",
            () -> new ThornArmorItem(net.minecraft.world.item.ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> THORN_BOOTS = ITEMS.register("thorn_boots",
            () -> new ThornArmorItem(net.minecraft.world.item.ArmorItem.Type.BOOTS));
    public static final RegistryObject<Item> SILVER_KEY =
            ITEMS.register("silver_key", SilverKeyItem::new);
    public static final RegistryObject<Item> FOOD_APPRAISAL_MONOCLE =
            ITEMS.register("food_appraisal_monocle", FoodAppraisalMonocleItem::new);

    /** 酒引(酒桶Boss召唤物) */
    public static final RegistryObject<Item> WINE_CHARM =
            ITEMS.register("wine_charm", WineCharmItem::new);

    /** 鼠群诱饵(鼠群召唤物) */
    public static final RegistryObject<Item> RAT_BAIT =
            ITEMS.register("rat_bait", RatSwarmBaitItem::new);

    // 酒系: 酒桶王 —— 低攻速, 手持醉酒 + 3 个随机酒类 buff 轮替
    public static final RegistryObject<Item> WINE_BOTTLE_MACE =
            ITEMS.register("wine_bottle_mace", net.farmingtales.ftboss.item.weapons.WineBottleMaceItem::new);
    // 奶酪系: 鼠群 —— 同酒锤但 buff 少(2 个)、伤害更高
    public static final RegistryObject<Item> CHEESE_WHEEL_MACE =
            ITEMS.register("cheese_wheel_mace", net.farmingtales.ftboss.item.weapons.FTWeapons.CheeseWheelMace::new);

    public static final RegistryObject<Item> COMPOST_CROWN =
            ITEMS.register("compost_crown", net.farmingtales.ftboss.item.weapons.CompostCrownItem::new);

    public static final RegistryObject<Item> LEMONCAT_SKILLET = ITEMS.register("lemoncat_skillet",
            () -> new net.farmingtales.ftboss.item.weapons.LemonCatWeaponItem(
                    net.farmingtales.ftboss.item.weapons.LemonCatWeaponItem.Kind.SKILLET));

    public static final RegistryObject<Item> LEMONCAT_SPATULA = ITEMS.register("lemoncat_spatula",
            () -> new net.farmingtales.ftboss.item.weapons.LemonCatWeaponItem(
                    net.farmingtales.ftboss.item.weapons.LemonCatWeaponItem.Kind.SPATULA));

    /** 炫彩堆肥桶: 炫彩答辩超人的召唤物 */
    public static final RegistryObject<Item> COMPOST_SUMMON =
            ITEMS.register("compost_summon", () -> new CompostSummonItem(new Item.Properties().stacksTo(1)));

    /** 【黑色切割者】(2026-09-12): 彩蛋 Boss「功底」的武器(自制 3D 模型)
     *  <p>2026-09-12 v62 按用户要求: **基础伤害改到 25**。
     *  原版口径: 显示的攻击伤害 = 1(玩家基础) + 参数 + 材质加成(下界合金 4) —— 所以参数取 20 => 25。 */
    public static final RegistryObject<Item> BLACK_CLEAVER =
            ITEMS.register("black_cleaver", net.farmingtales.ftboss.item.weapons.BlackCleaverItem::new);   // v155: GeckoLib 骨骼物品(继承原 BlackCleaver, 玩法/数值不变)

    /* ===== 2026-09-14 v139 按用户提供的模型导入: 屠夫的【大肉腿】(turkey_legs) =====
       用户给的是一份**Java 物品模型**(Blockbench 导出, 9 个 cube / 带 rotation), 只把贴图引用
       改成 ftboss:item/turkey_legs 并挂上 item/handheld 就入库了(贴图 64x64 一并导入)。 */
    /** 【大肉腿】屠夫的武器: 一根烤火鸡腿当大锤抡 */
    public static final RegistryObject<Item> TURKEY_LEGS =
            ITEMS.register("turkey_legs", () -> new net.minecraft.world.item.SwordItem(
                    net.farmingtales.ftboss.item.FTWeaponTier.INSTANCE, 6, -2.8f, new Item.Properties()));

    public static final RegistryObject<Item> DANDAN_SCYTHE =
            ITEMS.register("dandan_scythe", net.farmingtales.ftboss.item.weapons.DandanScytheItem::new);

    public static final RegistryObject<Item> QINGYI_CONFLUENCE_TRIDENT =
            ITEMS.register("qingyi_confluence_trident", net.farmingtales.ftboss.item.weapons.QingyiConfluenceTridentItem::new);

    /** 【暗影剑】(2026-09-12): 彩蛋 Boss「折耳根」的武器(参考饥荒暗影剑) */
    public static final RegistryObject<Item> SHADOW_SWORD =
            ITEMS.register("shadow_sword", net.farmingtales.ftboss.item.weapons.ShadowSwordItem::new);   // v19: GeckoLib 骨骼物品(继承原 ShadowSword, 玩法不变)

    /* ===== 2026-09-14 v127 第 9 位主 Boss【边牧犬王 benger】(用户提供的参考图: 黑白边牧) =====
       三件套: 召唤物【牧羊哨】/ 掉落武器【打狗棒】/ 刷怪蛋。 */

    /** 【打狗棒】benger 的掉落武器: 命中流血 + 对犬类额外真实伤害, 右键横扫 */
    public static final RegistryObject<Item> DOG_BEATING_STICK =
            ITEMS.register("dog_beating_stick", net.farmingtales.ftboss.item.weapons.FTWeapons.DogBeatingStick::new);

    /** 【牧羊哨】benger 的召唤物(右键主世界召唤) */
    public static final RegistryObject<Item> BENGER_WHISTLE =
            ITEMS.register("benger_whistle", () -> new BengerWhistleItem(new Item.Properties().stacksTo(1)));

    /** benger 的刷怪蛋 */
    public static final RegistryObject<Item> BENGER_SPAWN_EGG =
            ITEMS.register("benger_spawn_egg", () -> new OverworldBossSpawnEggItem(
                    net.farmingtales.ftboss.entity.FTEntities.BENGER, 0x2A2A2E, 0xEDEAE2,
                    new Item.Properties()));

    /** 【河豚棒】(2026-09-12 入库, 2026-09-18 补技能): 彩蛋 Boss「紫菜」的武器。
     *  右键放【胀气毒雾】，潜行右键放【毒刺膨爆】；毒云持续伤害，新技能近身击退并对中毒目标增伤；见 {@link net.farmingtales.ftboss.util.PufferBatRules}。 */
    public static final RegistryObject<Item> PUFFER_BAT =
            ITEMS.register("puffer_bat", net.farmingtales.ftboss.item.weapons.PufferBatItem::new);

    /** 【禁果】: 苹果爵士·蜜脆(第 8 位 Boss)的召唤物(2026-09-12) */
    public static final RegistryObject<Item> FORBIDDEN_APPLE =
            ITEMS.register("forbidden_apple", () -> new ForbiddenAppleItem(new Item.Properties().stacksTo(1)));

    /* 【苹果核】apple_core 已于 2026-09-18 <b>彻底删除</b>（用户口径：「删掉苹果核这个掉落物」
       → 追问确认要连物品注册一起摘掉）：
         · 物品注册（原本在这里）
         · 创造模式物品栏条目
         · 苹果爵士死亡掉落（原本掉 2~4 个）
       全工程已无任何引用；Boss 的「烂苹果投掷」与「环绕苹果」两招投射物外观改用
       <b>原版苹果</b>（{@code Items.APPLE}），见 {@code AppleBossEntity.throwRotten / throwOrbitApples}。
       {@code assets/ftboss/models/item/apple_core.json} 与对应贴图已一并删除。 */

    /** 【苹果爵士的果梗】(2026-09-12): 苹果爵士的武器掉落(下界合金级剑) */
    public static final RegistryObject<Item> APPLE_STEM =
            ITEMS.register("apple_stem", () -> new net.minecraft.world.item.SwordItem(
                    net.minecraft.world.item.Tiers.NETHERITE, 3, -2.2f, new Item.Properties()));

    /** 糖果拐杖: 姜饼王 Boss 的武器掉落(2026-09-12) */
    public static final RegistryObject<Item> CANDY_CANE =
            ITEMS.register("candy_cane", () -> new CandyCaneItem(new Item.Properties()));

    /** 【柚子小像】(2026-09-12 v15): 彩蛋 Boss「柚子」的武器 —— **缩小版它自己**。
     *  模型见 tools/model3d/gen_statue_youzi.cjs(玩家模型 6 方块 + 盒式 UV 1:1 对 64x64 皮肤) */
    public static final RegistryObject<Item> STATUE_YOUZI =
            ITEMS.register("statue_youzi", YouziStatueItem::new);

    /** 【文玄蓝的飞轮】(2026-09-12 v35 按用户要求单独画模型贴图): 飞轮投射物的外观物品 */
    public static final RegistryObject<Item> WENXUANLAN_FLYWHEEL =
            ITEMS.register("wenxuanlan_flywheel", XuanlanMoonWeaponItem::new);

    /** 【绽放的荆棘】(2026-09-12 v57 按用户要求): 沙耶(茶树)的掉落武器 —— 用户提供的 Blockbench 模型。
     *  模型导入见 {@code tools/import_thorns_model.cjs}(原文件 UV 在 0..16 空间, 导入时 ×4 归一到 64 像素空间)。 */
    public static final RegistryObject<Item> BLOOMING_THORNS =
            ITEMS.register("blooming_thorns", () -> new net.minecraft.world.item.SwordItem(
                    net.minecraft.world.item.Tiers.NETHERITE, 4, -2.4f, new Item.Properties()));

    /* ===== 2026-09-14 v108 按用户要求导入「酒桶王(酒魔)的武器」——【酒桶弩】=====
     * 用户提供的 Blockbench 模型 + 128×128 贴图, 直接按原样导入
     * (`models/item/barrel_crossbow[_ready].json`, 只把贴图引用改成 `ftboss:item/barrel_crossbow`)。
     * 两个模型 = **空弦** / **装填完成(ready)**: 酒桶王齐射酒桶炮弹前 1 秒会换成 ready 那一版,
     * 算是给玩家一个"它要开炮了"的视觉预告(见 {@code WineBarrelBossEntity.tickCrossbow})。
     * 挂在实体身上的渲染层见 {@code client.render.WineBarrelGeoRenderer}。 */

    /** 【酒桶弩】空弦(普通状态)。
     *  v123 按用户要求「完善一下酒桶弩的攻击效果，装填任意啤酒后发射爆炸酒桶造成爆炸」:
     *  从"纯外观的普通 Item"改成**能真的用的武器** {@link BarrelCrossbowItem}
     *  (长按右键拉弦装填任意啤酒 -> 右键打出爆炸酒桶)。耐久 384, 一次发射 -1。 */
    public static final RegistryObject<Item> BARREL_CROSSBOW =
            ITEMS.register("barrel_crossbow", () -> new BarrelCrossbowItem(
                    new Item.Properties().stacksTo(1).durability(384).rarity(Rarity.RARE)));

    /** 【酒桶弩·装填】齐射前的"上弦完成"外观(纯外观件, 由 Boss 自己换上/换下) */
    public static final RegistryObject<Item> BARREL_CROSSBOW_READY =
            ITEMS.register("barrel_crossbow_ready", () -> new Item(new Item.Properties()));

    /* ===== 2026-09-14 v109 按用户要求: 隐藏 Boss(庄园十人)的刷怪蛋 =====
     * "随便做一下, 能召唤测试就行" —— 外观直接借原版刷怪蛋贴图(蛋壳 + 斑点两层染色),
     * **零新增贴图**; 变体由 {@link EasterSpawnEggItem#useOn} 在生成时写进实体。
     * 10 个蛋 = {@code EasterBossEntity.MANOR_VARIANTS}(0~8 九人 + 10 雪黎; 9 号麻雀是支援位, 不发蛋)。 */

    private static RegistryObject<Item> easterEgg(String id, int variant, int base, int spot) {
        return ITEMS.register(id, () -> new EasterSpawnEggItem(variant, base, spot));
    }

    public static final RegistryObject<Item> EASTER_EGG_YOUZI = easterEgg("easter_egg_youzi", 0, 0xE8C54A, 0x6E9B4E);
    public static final RegistryObject<Item> EASTER_EGG_DANDAN = easterEgg("easter_egg_dandan", 1, 0xF2E9D8, 0xE0A33C);
    public static final RegistryObject<Item> EASTER_EGG_DUOLA = easterEgg("easter_egg_duola", 2, 0xE86FA8, 0xFFF0B3);
    public static final RegistryObject<Item> EASTER_EGG_GONGDI = easterEgg("easter_egg_gongdi", 3, 0x8C1C1C, 0x2B2B2B);
    public static final RegistryObject<Item> EASTER_EGG_NINGMENGMAO = easterEgg("easter_egg_ningmengmao", 4, 0xF7E24B, 0x4A4A4A);
    public static final RegistryObject<Item> EASTER_EGG_QINGYI = easterEgg("easter_egg_qingyi", 5, 0x2E7D8A, 0xBFE8E8);
    public static final RegistryObject<Item> EASTER_EGG_WENXUANLAN = easterEgg("easter_egg_wenxuanlan", 6, 0x2B4C9B, 0x9FB6E8);
    public static final RegistryObject<Item> EASTER_EGG_ZHEERGEN = easterEgg("easter_egg_zheergen", 7, 0x4E7A3A, 0xD9D2A0);
    public static final RegistryObject<Item> EASTER_EGG_ZICAI = easterEgg("easter_egg_zicai", 8, 0x6A2E6E, 0xC24B6A);
    public static final RegistryObject<Item> EASTER_EGG_XUELI = easterEgg("easter_egg_xueli", 10, 0xBFE3F5, 0x2E5A78);
    public static final RegistryObject<Item> EASTER_EGG_QIZAI = easterEgg("easter_egg_qizai", 11, 0xFFF3F6, 0xED82AE);
    public static final RegistryObject<Item> QIZAI_PRAYER_STAFF = ITEMS.register("qizai_prayer_staff",
            net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem::new);

    /** Creative-only zero-base weapon: 22 food sockets and eight random statue evolutions. */
    public static final RegistryObject<Item> GOURMET_BLANK_BLADE = ITEMS.register("gourmet_blank_blade",
            net.farmingtales.ftboss.item.weapons.GourmetBlankBladeItem::new);

    /** 10 个隐藏 Boss 刷怪蛋(顺序 = MANOR_VARIANTS): 创造页遍历 + 客户端染色都读这一份 */
    public static final java.util.List<RegistryObject<Item>> EASTER_EGGS = java.util.List.of(
            EASTER_EGG_YOUZI, EASTER_EGG_DANDAN, EASTER_EGG_DUOLA, EASTER_EGG_GONGDI, EASTER_EGG_NINGMENGMAO,
            EASTER_EGG_QINGYI, EASTER_EGG_WENXUANLAN, EASTER_EGG_ZHEERGEN, EASTER_EGG_ZICAI, EASTER_EGG_XUELI, EASTER_EGG_QIZAI);

    /* ===== 2026-09-13 晚: 旧样片 6/7(poc6/poc7)退役, 换成庄园的正式三张 =====
     * 长度参数必须等于音频真实时长, 否则唱片机会提前判定"放完了"(见 RecordItem 的
     * shouldRecordStopPlaying)。唱片还必须加进 `data/minecraft/tags/items/music_discs.json`,
     * 否则 JukeboxBlockEntity.setItem() 会静默拒绝, 而 RecordItem.useOn() 仍会 shrink(1)。 */

    /** 【庄园·序】演出曲 45.8 s = 916 tick（v112 重测: 曲子重渲染过, 旧值 836 会让唱片提前 4 秒停） */
    public static final RegistryObject<Item> MUSIC_DISC_MANOR_CUE =
            ITEMS.register("music_disc_manor_cue", () -> new net.minecraft.world.item.RecordItem(
                    15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_MANOR_CUE,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 916));

    /** 【庄园·战】战斗曲 258.5 s = 5170 tick（v112 重测, 旧值 5353 = 267.6 s） */
    public static final RegistryObject<Item> MUSIC_DISC_MANOR_BATTLE =
            ITEMS.register("music_disc_manor_battle", () -> new net.minecraft.world.item.RecordItem(
                    15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_MANOR_BATTLE,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 5170));

    /** 【庄园·双轨】坦克版 + 真吉他版各 0.5 叠放 256.5 s = 5130 tick（v112 重测） */
    public static final RegistryObject<Item> MUSIC_DISC_MANOR_DUAL =
            ITEMS.register("music_disc_manor_dual", () -> new net.minecraft.world.item.RecordItem(
                    15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_MANOR_DUAL,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 5130));

    /** 【庄园·鏖战】第二首庄园战斗曲 208.67 s = 4173 tick
     *  （v125 按用户要求「新加入的战斗曲做成唱片」; 时长用 `tools/audio_tools/ogg_dur.cjs` 实测，
     *  与 `BossMusic.GUIGNOL_TICKS` 一致）。 */
    public static final RegistryObject<Item> MUSIC_DISC_MANOR_GUIGNOL =
            ITEMS.register("music_disc_manor_guignol", () -> new net.minecraft.world.item.RecordItem(
                    15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_MANOR_GUIGNOL,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 4173));

    /** 【牧野犬王】边牧犬王(v127 第 9 位主 Boss)专属曲 185.41 s = **3708 tick**
     *  （用 `tools/audio_tools/ogg_dur.cjs` 实测, 与 `BossMusic.BENGER_TICKS` 一致）。 */
    public static final RegistryObject<Item> MUSIC_DISC_BENGER =
            ITEMS.register("music_disc_benger", () -> new net.minecraft.world.item.RecordItem(
                    15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_BENGER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 3708));

    /* ==================== v112 食物系 Boss 全曲唱片(2026-09-13 按用户要求) ====================
       8 位食物系 Boss 的专属 BGM **全部**做成唱片: 苹果爵士与炫彩答辩超人各 2 首 → 共 10 张。
       长度全部用 `tools/audio_tools/ogg_dur.cjs` 现场测出来的真实时长(秒 × 20), 别凭印象写 ——
       RecordItem 的长度对不上时, 唱片机会提前判定"放完了"(见上面的说明)。 */

    /** 【果园教父】苹果爵士·蜜脆 一阶段 99.98 s = 2000 tick */
    public static final RegistryObject<Item> MUSIC_DISC_APPLE =
            ITEMS.register("music_disc_apple", () -> new net.minecraft.world.item.RecordItem(
                    15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_APPLE,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 2000));

    /** 【果园教父·虫蛀暴走】苹果爵士·蜜脆 二阶段 82.20 s = 1644 tick */
    public static final RegistryObject<Item> MUSIC_DISC_APPLE_P2 =
            ITEMS.register("music_disc_apple_p2", () -> new net.minecraft.world.item.RecordItem(
                    15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_APPLE_P2,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 1644));

    /** 【木偶剧场·加洛普】糖霜女皇 115.38 s = 2308 tick */
    public static final RegistryObject<Item> MUSIC_DISC_GINGERBREAD =
            ITEMS.register("music_disc_gingerbread", () -> new net.minecraft.world.item.RecordItem(
                    15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_GINGERBREAD,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 2308));

    /** 【肉山祭坛】屠夫 145.34 s = 2907 tick */
    public static final RegistryObject<Item> MUSIC_DISC_BUTCHER =
            ITEMS.register("music_disc_butcher", () -> new net.minecraft.world.item.RecordItem(
                    15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_BUTCHER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 4568));

    /** 【茶の唄】沙耶(茶树) 131.65 s = 2633 tick */
    public static final RegistryObject<Item> MUSIC_DISC_SAYA =
            ITEMS.register("music_disc_saya", () -> new net.minecraft.world.item.RecordItem(
                    15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_SAYA,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 2633));

    /** 【醉乡酒馆】酒魔·酒桶王 123.92 s = 2478 tick */
    public static final RegistryObject<Item> MUSIC_DISC_TAVERN =
            ITEMS.register("music_disc_tavern", () -> new net.minecraft.world.item.RecordItem(
                    15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_TAVERN,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 2478));

    /** 【鼠潮夜行】鼠群 111.81 s = 2236 tick */
    public static final RegistryObject<Item> MUSIC_DISC_RAT =
            ITEMS.register("music_disc_rat", () -> new net.minecraft.world.item.RecordItem(
                    15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_RAT,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 2236));

    /** 【故障机器人】故障机器人 156.04 s = 3121 tick */
    public static final RegistryObject<Item> MUSIC_DISC_DEFECT =
            ITEMS.register("music_disc_defect", () -> new net.minecraft.world.item.RecordItem(
                    15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_DEFECT,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 3696));

    /** 【堆肥·强袭】炫彩答辩超人 一阶段 89.08 s = 1782 tick */
    public static final RegistryObject<Item> MUSIC_DISC_COMPOST =
            ITEMS.register("music_disc_compost", () -> new net.minecraft.world.item.RecordItem(
                    15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_COMPOST,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 1782));

    /** 【堆肥·熔毁】炫彩答辩超人 二阶段 85.68 s = 1714 tick */
    public static final RegistryObject<Item> MUSIC_DISC_COMPOST_P2 =
            ITEMS.register("music_disc_compost_p2", () -> new net.minecraft.world.item.RecordItem(
                    15, net.farmingtales.ftboss.sound.FTSounds.MUSIC_DISC_COMPOST_P2,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 1714));

    /** 10 张食物系唱片(创造页遍历用) */
    public static final java.util.List<RegistryObject<Item>> FOOD_MUSIC_DISCS = java.util.List.of(
            MUSIC_DISC_APPLE, MUSIC_DISC_APPLE_P2, MUSIC_DISC_GINGERBREAD, MUSIC_DISC_BUTCHER,
            MUSIC_DISC_SAYA, MUSIC_DISC_TAVERN, MUSIC_DISC_RAT, MUSIC_DISC_DEFECT,
            MUSIC_DISC_COMPOST, MUSIC_DISC_COMPOST_P2, MUSIC_DISC_TIDE_FISH, MUSIC_DISC_TIDE_FISH_P2);

    private FTBossItems() {}

    public static void register(IEventBus bus) {
        BossRelicItems.init();
        ITEMS.register(bus);
        FTRecipeSerializers.RECIPE_SERIALIZERS.register(bus);
    }
}
