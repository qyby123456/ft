/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.item.ArmorItem$Type
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.RecordItem
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.Tiers
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import java.util.function.Supplier;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.item.BarrelCrossbowItem;
import net.farmingtales.ftboss.item.BengerWhistleItem;
import net.farmingtales.ftboss.item.BossRelicItems;
import net.farmingtales.ftboss.item.CandyCaneItem;
import net.farmingtales.ftboss.item.CompostSummonItem;
import net.farmingtales.ftboss.item.DefectCoreItem;
import net.farmingtales.ftboss.item.EasterSpawnEggItem;
import net.farmingtales.ftboss.item.FTRecipeSerializers;
import net.farmingtales.ftboss.item.FTWeaponTier;
import net.farmingtales.ftboss.item.FoodAppraisalMonocleItem;
import net.farmingtales.ftboss.item.ForbiddenAppleItem;
import net.farmingtales.ftboss.item.ManorStopTokenItem;
import net.farmingtales.ftboss.item.MeatHeartItem;
import net.farmingtales.ftboss.item.OverworldBossSpawnEggItem;
import net.farmingtales.ftboss.item.RatSwarmBaitItem;
import net.farmingtales.ftboss.item.SilverKeyItem;
import net.farmingtales.ftboss.item.SugarQueenCrystalItem;
import net.farmingtales.ftboss.item.TeaSeedItem;
import net.farmingtales.ftboss.item.ThornArmorItem;
import net.farmingtales.ftboss.item.ThornCrownItem;
import net.farmingtales.ftboss.item.TideBanquetItem;
import net.farmingtales.ftboss.item.TideTridentItem;
import net.farmingtales.ftboss.item.WineCharmItem;
import net.farmingtales.ftboss.item.XuanlanMoonWeaponItem;
import net.farmingtales.ftboss.item.YouziStatueItem;
import net.farmingtales.ftboss.item.weapons.BlackCleaverItem;
import net.farmingtales.ftboss.item.weapons.CompostCrownItem;
import net.farmingtales.ftboss.item.weapons.DandanScytheItem;
import net.farmingtales.ftboss.item.weapons.DoraSubmachineGunItem;
import net.farmingtales.ftboss.item.weapons.DoraWeaponItem;
import net.farmingtales.ftboss.item.weapons.FTWeapons;
import net.farmingtales.ftboss.item.weapons.GourmetBlankBladeItem;
import net.farmingtales.ftboss.item.weapons.LemonCatWeaponItem;
import net.farmingtales.ftboss.item.weapons.MaqueBlessingFeatherItem;
import net.farmingtales.ftboss.item.weapons.PufferBatItem;
import net.farmingtales.ftboss.item.weapons.QingyiConfluenceTridentItem;
import net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem;
import net.farmingtales.ftboss.item.weapons.ShadowSwordItem;
import net.farmingtales.ftboss.item.weapons.WineBottleMaceItem;
import net.farmingtales.ftboss.sound.FTSounds;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class FTBossItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.ITEMS, (String)"ftboss");
    public static final RegistryObject<Item> TIDE_BANQUET = ITEMS.register("tide_banquet", () -> new TideBanquetItem(new Item.Properties().m_41487_(1).m_41497_(Rarity.EPIC)));
    public static final RegistryObject<Item> TIDE_TRIDENT = ITEMS.register("tide_trident", () -> new TideTridentItem(new Item.Properties().m_41497_(Rarity.EPIC).m_41486_()));
    public static final RegistryObject<Item> TIDE_PEARL = ITEMS.register("tide_pearl", () -> new Item(new Item.Properties().m_41497_(Rarity.RARE)));
    public static final RegistryObject<Item> MUSIC_DISC_TIDE_FISH = ITEMS.register("music_disc_tide_fish", () -> new RecordItem(15, FTSounds.MUSIC_DISC_TIDE_FISH, new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE), 1980));
    public static final RegistryObject<Item> MUSIC_DISC_TIDE_FISH_P2 = ITEMS.register("music_disc_tide_fish_p2", () -> new RecordItem(15, FTSounds.MUSIC_DISC_TIDE_FISH_P2, new Item.Properties().m_41487_(1).m_41497_(Rarity.EPIC), 1927));
    public static final RegistryObject<Item> DORA_NOVA_LAUNCHER = ITEMS.register("dora_nova_launcher", DoraSubmachineGunItem::new);
    public static final RegistryObject<Item> DORA_PULSE_GUN = ITEMS.register("dora_pulse_gun", () -> new DoraWeaponItem(false));
    public static final RegistryObject<Item> DORA_LANCE = ITEMS.register("dora_lance", () -> new DoraWeaponItem(true));
    public static final RegistryObject<Item> MAQUE_BLESSING_FEATHER = ITEMS.register("maque_blessing_feather", MaqueBlessingFeatherItem::new);
    public static final RegistryObject<Item> SUGAR_QUEEN_CRYSTAL = ITEMS.register("sugar_queen_crystal", SugarQueenCrystalItem::new);
    public static final RegistryObject<Item> MANOR_STOP_TOKEN = ITEMS.register("manor_stop_token", ManorStopTokenItem::new);
    public static final RegistryObject<Item> MEAT_HEART = ITEMS.register("meat_heart", MeatHeartItem::new);
    public static final RegistryObject<Item> DEFECT_CORE = ITEMS.register("defect_core", DefectCoreItem::new);
    public static final RegistryObject<Item> DEFECT_CLAW = ITEMS.register("defect_claw", () -> new SwordItem((Tier)Tiers.DIAMOND, 3, -2.4f, new Item.Properties().m_41497_(Rarity.EPIC)));
    public static final RegistryObject<Item> TEA_SEED = ITEMS.register("tea_seed", TeaSeedItem::new);
    public static final RegistryObject<Item> THORN_CROWN = ITEMS.register("thorn_crown", ThornCrownItem::new);
    public static final RegistryObject<Item> THORN_CHESTPLATE = ITEMS.register("thorn_chestplate", () -> new ThornArmorItem(ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> THORN_LEGGINGS = ITEMS.register("thorn_leggings", () -> new ThornArmorItem(ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> THORN_BOOTS = ITEMS.register("thorn_boots", () -> new ThornArmorItem(ArmorItem.Type.BOOTS));
    public static final RegistryObject<Item> SILVER_KEY = ITEMS.register("silver_key", SilverKeyItem::new);
    public static final RegistryObject<Item> FOOD_APPRAISAL_MONOCLE = ITEMS.register("food_appraisal_monocle", FoodAppraisalMonocleItem::new);
    public static final RegistryObject<Item> WINE_CHARM = ITEMS.register("wine_charm", WineCharmItem::new);
    public static final RegistryObject<Item> RAT_BAIT = ITEMS.register("rat_bait", RatSwarmBaitItem::new);
    public static final RegistryObject<Item> WINE_BOTTLE_MACE = ITEMS.register("wine_bottle_mace", WineBottleMaceItem::new);
    public static final RegistryObject<Item> CHEESE_WHEEL_MACE = ITEMS.register("cheese_wheel_mace", FTWeapons.CheeseWheelMace::new);
    public static final RegistryObject<Item> COMPOST_CROWN = ITEMS.register("compost_crown", CompostCrownItem::new);
    public static final RegistryObject<Item> LEMONCAT_SKILLET = ITEMS.register("lemoncat_skillet", () -> new LemonCatWeaponItem(LemonCatWeaponItem.Kind.SKILLET));
    public static final RegistryObject<Item> LEMONCAT_SPATULA = ITEMS.register("lemoncat_spatula", () -> new LemonCatWeaponItem(LemonCatWeaponItem.Kind.SPATULA));
    public static final RegistryObject<Item> COMPOST_SUMMON = ITEMS.register("compost_summon", () -> new CompostSummonItem(new Item.Properties().m_41487_(1)));
    public static final RegistryObject<Item> BLACK_CLEAVER = ITEMS.register("black_cleaver", BlackCleaverItem::new);
    public static final RegistryObject<Item> TURKEY_LEGS = ITEMS.register("turkey_legs", () -> new SwordItem((Tier)FTWeaponTier.INSTANCE, 6, -2.8f, new Item.Properties()));
    public static final RegistryObject<Item> DANDAN_SCYTHE = ITEMS.register("dandan_scythe", DandanScytheItem::new);
    public static final RegistryObject<Item> QINGYI_CONFLUENCE_TRIDENT = ITEMS.register("qingyi_confluence_trident", QingyiConfluenceTridentItem::new);
    public static final RegistryObject<Item> SHADOW_SWORD = ITEMS.register("shadow_sword", ShadowSwordItem::new);
    public static final RegistryObject<Item> DOG_BEATING_STICK = ITEMS.register("dog_beating_stick", FTWeapons.DogBeatingStick::new);
    public static final RegistryObject<Item> BENGER_WHISTLE = ITEMS.register("benger_whistle", () -> new BengerWhistleItem(new Item.Properties().m_41487_(1)));
    public static final RegistryObject<Item> BENGER_SPAWN_EGG = ITEMS.register("benger_spawn_egg", () -> new OverworldBossSpawnEggItem((Supplier<? extends EntityType<? extends Mob>>)FTEntities.BENGER, 0x2A2A2E, 15592162, new Item.Properties()));
    public static final RegistryObject<Item> PUFFER_BAT = ITEMS.register("puffer_bat", PufferBatItem::new);
    public static final RegistryObject<Item> FORBIDDEN_APPLE = ITEMS.register("forbidden_apple", () -> new ForbiddenAppleItem(new Item.Properties().m_41487_(1)));
    public static final RegistryObject<Item> APPLE_STEM = ITEMS.register("apple_stem", () -> new SwordItem((Tier)Tiers.NETHERITE, 3, -2.2f, new Item.Properties()));
    public static final RegistryObject<Item> CANDY_CANE = ITEMS.register("candy_cane", () -> new CandyCaneItem(new Item.Properties()));
    public static final RegistryObject<Item> STATUE_YOUZI = ITEMS.register("statue_youzi", YouziStatueItem::new);
    public static final RegistryObject<Item> WENXUANLAN_FLYWHEEL = ITEMS.register("wenxuanlan_flywheel", XuanlanMoonWeaponItem::new);
    public static final RegistryObject<Item> BLOOMING_THORNS = ITEMS.register("blooming_thorns", () -> new SwordItem((Tier)Tiers.NETHERITE, 4, -2.4f, new Item.Properties()));
    public static final RegistryObject<Item> BARREL_CROSSBOW = ITEMS.register("barrel_crossbow", () -> new BarrelCrossbowItem(new Item.Properties().m_41487_(1).m_41503_(384).m_41497_(Rarity.RARE)));
    public static final RegistryObject<Item> BARREL_CROSSBOW_READY = ITEMS.register("barrel_crossbow_ready", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> EASTER_EGG_YOUZI = FTBossItems.easterEgg("easter_egg_youzi", 0, 15254858, 7248718);
    public static final RegistryObject<Item> EASTER_EGG_DANDAN = FTBossItems.easterEgg("easter_egg_dandan", 1, 15919576, 14721852);
    public static final RegistryObject<Item> EASTER_EGG_DUOLA = FTBossItems.easterEgg("easter_egg_duola", 2, 15232936, 16773299);
    public static final RegistryObject<Item> EASTER_EGG_GONGDI = FTBossItems.easterEgg("easter_egg_gongdi", 3, 0x8C1C1C, 0x2B2B2B);
    public static final RegistryObject<Item> EASTER_EGG_NINGMENGMAO = FTBossItems.easterEgg("easter_egg_ningmengmao", 4, 16245323, 0x4A4A4A);
    public static final RegistryObject<Item> EASTER_EGG_QINGYI = FTBossItems.easterEgg("easter_egg_qingyi", 5, 3046794, 12577000);
    public static final RegistryObject<Item> EASTER_EGG_WENXUANLAN = FTBossItems.easterEgg("easter_egg_wenxuanlan", 6, 2837659, 10467048);
    public static final RegistryObject<Item> EASTER_EGG_ZHEERGEN = FTBossItems.easterEgg("easter_egg_zheergen", 7, 5143098, 14275232);
    public static final RegistryObject<Item> EASTER_EGG_ZICAI = FTBossItems.easterEgg("easter_egg_zicai", 8, 6958702, 12733290);
    public static final RegistryObject<Item> EASTER_EGG_XUELI = FTBossItems.easterEgg("easter_egg_xueli", 10, 12575733, 3037816);
    public static final RegistryObject<Item> EASTER_EGG_QIZAI = FTBossItems.easterEgg("easter_egg_qizai", 11, 0xFFF3F6, 15565486);
    public static final RegistryObject<Item> QIZAI_PRAYER_STAFF = ITEMS.register("qizai_prayer_staff", QizaiPrayerStaffItem::new);
    public static final RegistryObject<Item> GOURMET_BLANK_BLADE = ITEMS.register("gourmet_blank_blade", GourmetBlankBladeItem::new);
    public static final List<RegistryObject<Item>> EASTER_EGGS = List.of(EASTER_EGG_YOUZI, EASTER_EGG_DANDAN, EASTER_EGG_DUOLA, EASTER_EGG_GONGDI, EASTER_EGG_NINGMENGMAO, EASTER_EGG_QINGYI, EASTER_EGG_WENXUANLAN, EASTER_EGG_ZHEERGEN, EASTER_EGG_ZICAI, EASTER_EGG_XUELI, EASTER_EGG_QIZAI);
    public static final RegistryObject<Item> MUSIC_DISC_MANOR_CUE = ITEMS.register("music_disc_manor_cue", () -> new RecordItem(15, FTSounds.MUSIC_DISC_MANOR_CUE, new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE), 916));
    public static final RegistryObject<Item> MUSIC_DISC_MANOR_BATTLE = ITEMS.register("music_disc_manor_battle", () -> new RecordItem(15, FTSounds.MUSIC_DISC_MANOR_BATTLE, new Item.Properties().m_41487_(1).m_41497_(Rarity.EPIC), 5170));
    public static final RegistryObject<Item> MUSIC_DISC_MANOR_DUAL = ITEMS.register("music_disc_manor_dual", () -> new RecordItem(15, FTSounds.MUSIC_DISC_MANOR_DUAL, new Item.Properties().m_41487_(1).m_41497_(Rarity.EPIC), 5130));
    public static final RegistryObject<Item> MUSIC_DISC_MANOR_GUIGNOL = ITEMS.register("music_disc_manor_guignol", () -> new RecordItem(15, FTSounds.MUSIC_DISC_MANOR_GUIGNOL, new Item.Properties().m_41487_(1).m_41497_(Rarity.EPIC), 4173));
    public static final RegistryObject<Item> MUSIC_DISC_BENGER = ITEMS.register("music_disc_benger", () -> new RecordItem(15, FTSounds.MUSIC_DISC_BENGER, new Item.Properties().m_41487_(1).m_41497_(Rarity.EPIC), 3708));
    public static final RegistryObject<Item> MUSIC_DISC_APPLE = ITEMS.register("music_disc_apple", () -> new RecordItem(15, FTSounds.MUSIC_DISC_APPLE, new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE), 2000));
    public static final RegistryObject<Item> MUSIC_DISC_APPLE_P2 = ITEMS.register("music_disc_apple_p2", () -> new RecordItem(15, FTSounds.MUSIC_DISC_APPLE_P2, new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE), 1644));
    public static final RegistryObject<Item> MUSIC_DISC_GINGERBREAD = ITEMS.register("music_disc_gingerbread", () -> new RecordItem(15, FTSounds.MUSIC_DISC_GINGERBREAD, new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE), 2308));
    public static final RegistryObject<Item> MUSIC_DISC_BUTCHER = ITEMS.register("music_disc_butcher", () -> new RecordItem(15, FTSounds.MUSIC_DISC_BUTCHER, new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE), 4568));
    public static final RegistryObject<Item> MUSIC_DISC_SAYA = ITEMS.register("music_disc_saya", () -> new RecordItem(15, FTSounds.MUSIC_DISC_SAYA, new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE), 2633));
    public static final RegistryObject<Item> MUSIC_DISC_TAVERN = ITEMS.register("music_disc_tavern", () -> new RecordItem(15, FTSounds.MUSIC_DISC_TAVERN, new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE), 2478));
    public static final RegistryObject<Item> MUSIC_DISC_RAT = ITEMS.register("music_disc_rat", () -> new RecordItem(15, FTSounds.MUSIC_DISC_RAT, new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE), 2236));
    public static final RegistryObject<Item> MUSIC_DISC_DEFECT = ITEMS.register("music_disc_defect", () -> new RecordItem(15, FTSounds.MUSIC_DISC_DEFECT, new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE), 3696));
    public static final RegistryObject<Item> MUSIC_DISC_COMPOST = ITEMS.register("music_disc_compost", () -> new RecordItem(15, FTSounds.MUSIC_DISC_COMPOST, new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE), 1782));
    public static final RegistryObject<Item> MUSIC_DISC_COMPOST_P2 = ITEMS.register("music_disc_compost_p2", () -> new RecordItem(15, FTSounds.MUSIC_DISC_COMPOST_P2, new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE), 1714));
    public static final List<RegistryObject<Item>> FOOD_MUSIC_DISCS = List.of(MUSIC_DISC_APPLE, MUSIC_DISC_APPLE_P2, MUSIC_DISC_GINGERBREAD, MUSIC_DISC_BUTCHER, MUSIC_DISC_SAYA, MUSIC_DISC_TAVERN, MUSIC_DISC_RAT, MUSIC_DISC_DEFECT, MUSIC_DISC_COMPOST, MUSIC_DISC_COMPOST_P2, MUSIC_DISC_TIDE_FISH, MUSIC_DISC_TIDE_FISH_P2);

    private static RegistryObject<Item> easterEgg(String id, int variant, int base, int spot) {
        return ITEMS.register(id, () -> new EasterSpawnEggItem(variant, base, spot));
    }

    private FTBossItems() {
    }

    public static void register(IEventBus bus) {
        BossRelicItems.init();
        ITEMS.register(bus);
        FTRecipeSerializers.RECIPE_SERIALIZERS.register(bus);
    }
}

