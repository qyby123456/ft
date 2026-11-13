package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.item.weapons.BlackCleaverItem;
import net.farmingtales.ftboss.item.weapons.CompostCrownItem;
import net.farmingtales.ftboss.item.weapons.DandanScytheItem;
import net.farmingtales.ftboss.item.weapons.DoraSubmachineGunItem;
import net.farmingtales.ftboss.item.weapons.DoraWeaponItem;
import net.farmingtales.ftboss.item.weapons.GourmetBlankBladeItem;
import net.farmingtales.ftboss.item.weapons.LemonCatWeaponItem;
import net.farmingtales.ftboss.item.weapons.ManorSpearItem;
import net.farmingtales.ftboss.item.weapons.ManorTridentItem;
import net.farmingtales.ftboss.item.weapons.MaqueBlessingFeatherItem;
import net.farmingtales.ftboss.item.weapons.PufferBatItem;
import net.farmingtales.ftboss.item.weapons.QingyiConfluenceTridentItem;
import net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem;
import net.farmingtales.ftboss.item.weapons.SanguineBondItem;
import net.farmingtales.ftboss.item.weapons.ShadowSwordItem;
import net.farmingtales.ftboss.item.weapons.WineBottleMaceItem;
import net.farmingtales.ftboss.item.weapons.FTWeapons.CheeseWheelMace;
import net.farmingtales.ftboss.item.weapons.FTWeapons.DogBeatingStick;
import net.farmingtales.ftboss.item.weapons.LemonCatWeaponItem.Kind;
import net.farmingtales.ftboss.item.weapons.ManorTridentItem.LegacyEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FTBossItems {
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, "ftboss");
   public static final DeferredHolder<Item, Item> TIDE_BANQUET = ITEMS.register(
      "tide_banquet", () -> new TideBanquetItem(new Properties().stacksTo(1).rarity(Rarity.EPIC))
   );
   public static final DeferredHolder<Item, Item> TIDE_TRIDENT = ITEMS.register(
      "tide_trident", () -> new TideTridentItem(new Properties().rarity(Rarity.EPIC).fireResistant())
   );
   public static final DeferredHolder<Item, Item> TIDE_PEARL = ITEMS.register("tide_pearl", () -> new Item(new Properties().rarity(Rarity.RARE)));
   public static final DeferredHolder<Item, Item> MUSIC_DISC_TIDE_FISH = ITEMS.register(
      "music_disc_tide_fish", () -> musicDisc("music_disc_tide_fish", Rarity.RARE)
   );
   public static final DeferredHolder<Item, Item> MUSIC_DISC_TIDE_FISH_P2 = ITEMS.register(
      "music_disc_tide_fish_p2", () -> musicDisc("music_disc_tide_fish_p2", Rarity.EPIC)
   );
   public static final DeferredHolder<Item, Item> DORA_NOVA_LAUNCHER = ITEMS.register("dora_nova_launcher", DoraSubmachineGunItem::new);
   public static final DeferredHolder<Item, Item> DORA_PULSE_GUN = ITEMS.register("dora_pulse_gun", () -> new DoraWeaponItem(false));
   public static final DeferredHolder<Item, Item> DORA_LANCE = ITEMS.register("dora_lance", () -> new DoraWeaponItem(true));
   public static final DeferredHolder<Item, Item> MAQUE_BLESSING_FEATHER = ITEMS.register("maque_blessing_feather", MaqueBlessingFeatherItem::new);
   public static final DeferredHolder<Item, Item> SUGAR_QUEEN_CRYSTAL = ITEMS.register("sugar_queen_crystal", SugarQueenCrystalItem::new);
   public static final DeferredHolder<Item, Item> MANOR_STOP_TOKEN = ITEMS.register("manor_stop_token", ManorStopTokenItem::new);
   public static final DeferredHolder<Item, Item> MEAT_HEART = ITEMS.register("meat_heart", MeatHeartItem::new);
   public static final DeferredHolder<Item, Item> DEFECT_CORE = ITEMS.register("defect_core", DefectCoreItem::new);
   public static final DeferredHolder<Item, Item> DEFECT_CLAW = ITEMS.register(
      "defect_claw", () -> new SwordItem(Tiers.DIAMOND, new Properties().rarity(Rarity.EPIC).attributes(SwordItem.createAttributes(Tiers.DIAMOND, 3, -2.4F)))
   );
   public static final DeferredHolder<Item, Item> TEA_SEED = ITEMS.register("tea_seed", TeaSeedItem::new);
   public static final DeferredHolder<Item, Item> THORN_CROWN = ITEMS.register("thorn_crown", ThornCrownItem::new);
   public static final DeferredHolder<Item, Item> THORN_CHESTPLATE = ITEMS.register("thorn_chestplate", () -> new ThornArmorItem(Type.CHESTPLATE));
   public static final DeferredHolder<Item, Item> THORN_LEGGINGS = ITEMS.register("thorn_leggings", () -> new ThornArmorItem(Type.LEGGINGS));
   public static final DeferredHolder<Item, Item> THORN_BOOTS = ITEMS.register("thorn_boots", () -> new ThornArmorItem(Type.BOOTS));
   public static final DeferredHolder<Item, Item> SILVER_KEY = ITEMS.register("silver_key", SilverKeyItem::new);
   public static final DeferredHolder<Item, Item> FOOD_APPRAISAL_MONOCLE = ITEMS.register("food_appraisal_monocle", FoodAppraisalMonocleItem::new);
   public static final DeferredHolder<Item, Item> WINE_CHARM = ITEMS.register("wine_charm", WineCharmItem::new);
   public static final DeferredHolder<Item, Item> RAT_BAIT = ITEMS.register("rat_bait", RatSwarmBaitItem::new);
   public static final DeferredHolder<Item, Item> WINE_BOTTLE_MACE = ITEMS.register("wine_bottle_mace", WineBottleMaceItem::new);
   public static final DeferredHolder<Item, Item> CHEESE_WHEEL_MACE = ITEMS.register("cheese_wheel_mace", CheeseWheelMace::new);
   public static final DeferredHolder<Item, Item> COMPOST_CROWN = ITEMS.register("compost_crown", CompostCrownItem::new);
   public static final DeferredHolder<Item, Item> LEMONCAT_SKILLET = ITEMS.register("lemoncat_skillet", () -> new LemonCatWeaponItem(Kind.SKILLET));
   public static final DeferredHolder<Item, Item> LEMONCAT_SPATULA = ITEMS.register("lemoncat_spatula", () -> new LemonCatWeaponItem(Kind.SPATULA));
   public static final DeferredHolder<Item, Item> COMPOST_SUMMON = ITEMS.register("compost_summon", () -> new CompostSummonItem(new Properties().stacksTo(1)));
   public static final DeferredHolder<Item, Item> BLACK_CLEAVER = ITEMS.register("black_cleaver", BlackCleaverItem::new);
   public static final DeferredHolder<Item, Item> TURKEY_LEGS = ITEMS.register(
      "turkey_legs", () -> new SwordItem(FTWeaponTier.INSTANCE, new Properties().attributes(SwordItem.createAttributes(FTWeaponTier.INSTANCE, 6, -2.8F)))
   );
   public static final DeferredHolder<Item, Item> DANDAN_SCYTHE = ITEMS.register("dandan_scythe", DandanScytheItem::new);
   public static final DeferredHolder<Item, Item> QINGYI_CONFLUENCE_TRIDENT = ITEMS.register("qingyi_confluence_trident", QingyiConfluenceTridentItem::new);
   public static final DeferredHolder<Item, Item> TRIDENT_STORMSEEKER = ITEMS.register(
      "trident_stormseeker",
      () -> new ManorTridentItem("stormseeker", "textures/trident/trident_stormseeker.png", 2250, 11.0F, Rarity.RARE, LegacyEffect.STORM_BLESSED, false)
   );
   public static final DeferredHolder<Item, Item> TRIDENT_THE_DESECRATOR = ITEMS.register(
      "trident_the_desecrator",
      () -> new ManorTridentItem("the_desecrator", "textures/trident/trident_the_desecrator.png", 2250, 13.0F, Rarity.RARE, LegacyEffect.VILE, false)
   );
   public static final DeferredHolder<Item, Item> TRIDENT_ECHO_OF_THE_DEEP = ITEMS.register(
      "trident_echo_of_the_deep",
      () -> new ManorTridentItem("echo_of_the_deep", "textures/trident/trident_echo_of_the_deep.png", 2250, 9.0F, Rarity.RARE, LegacyEffect.ECHOING, false)
   );
   public static final DeferredHolder<Item, Item> TRIDENT_STELLARIS = ITEMS.register(
      "trident_stellaris",
      () -> new ManorTridentItem("stellaris", "textures/trident/trident_stellaris.png", 2250, 11.0F, Rarity.RARE, LegacyEffect.STELLAR, false)
   );
   public static final DeferredHolder<Item, Item> SANGUINE_BOND = ITEMS.register("sanguine_bond", SanguineBondItem::new);
   public static final DeferredHolder<Item, Item> SLUMBERING_TRIDENT = ITEMS.register(
      "slumbering_trident",
      () -> new ManorTridentItem("slumbering_trident", "textures/trident/slumbering_trident.png", 650, 9.0F, Rarity.UNCOMMON, LegacyEffect.NONE, false)
   );
   public static final DeferredHolder<Item, Item> FRENZIED_TRIDENT = ITEMS.register(
      "frenzied_trident",
      () -> new ManorTridentItem("frenzied_trident", "textures/trident/frenzied_trident.png", 350, 6.0F, Rarity.UNCOMMON, LegacyEffect.NONE, false)
   );
   public static final DeferredHolder<Item, Item> ANCIENT_TRIDENT = ITEMS.register(
      "ancient_trident",
      () -> new ManorTridentItem("ancient_trident", "textures/trident/ancient_trident.png", 2031, 11.0F, Rarity.UNCOMMON, LegacyEffect.NONE, false)
   );
   public static final DeferredHolder<Item, Item> OCEANIC_TRIDENT = ITEMS.register(
      "oceanic_trident",
      () -> new ManorTridentItem("oceanic_trident", "textures/trident/oceanic_trident.png", 350, 9.0F, Rarity.UNCOMMON, LegacyEffect.OCEANIC, false)
   );
   public static final DeferredHolder<Item, Item> HOLY_TRIDENT = ITEMS.register(
      "holy_trident", () -> new ManorTridentItem("holy_trident", "textures/trident/holy_trident.png", 350, 9.0F, Rarity.UNCOMMON, LegacyEffect.HOLY, false)
   );
   public static final DeferredHolder<Item, Item> FARSHOT_TRIDENT = ITEMS.register(
      "farshot_trident",
      () -> new ManorTridentItem("farshot_trident", "textures/trident/farshot_trident.png", 350, 9.0F, Rarity.UNCOMMON, LegacyEffect.NONE, true)
   );
   public static final DeferredHolder<Item, Item> WOODEN_SPEAR = ITEMS.register("wooden_spear", () -> new ManorSpearItem("wooden_spear", Tiers.WOOD));
   public static final DeferredHolder<Item, Item> STONE_SPEAR = ITEMS.register("stone_spear", () -> new ManorSpearItem("stone_spear", Tiers.STONE));
   public static final DeferredHolder<Item, Item> IRON_SPEAR = ITEMS.register("iron_spear", () -> new ManorSpearItem("iron_spear", Tiers.IRON));
   public static final DeferredHolder<Item, Item> GOLDEN_SPEAR = ITEMS.register("golden_spear", () -> new ManorSpearItem("golden_spear", Tiers.GOLD));
   public static final DeferredHolder<Item, Item> DIAMOND_SPEAR = ITEMS.register("diamond_spear", () -> new ManorSpearItem("diamond_spear", Tiers.DIAMOND));
   public static final DeferredHolder<Item, Item> NETHERITE_SPEAR = ITEMS.register(
      "netherite_spear", () -> new ManorSpearItem("netherite_spear", Tiers.NETHERITE)
   );
   public static final DeferredHolder<Item, Item> SHADOW_SWORD = ITEMS.register("shadow_sword", ShadowSwordItem::new);
   public static final DeferredHolder<Item, Item> DOG_BEATING_STICK = ITEMS.register("dog_beating_stick", DogBeatingStick::new);
   public static final DeferredHolder<Item, Item> BENGER_WHISTLE = ITEMS.register("benger_whistle", () -> new BengerWhistleItem(new Properties().stacksTo(1)));
   public static final DeferredHolder<Item, Item> BENGER_SPAWN_EGG = ITEMS.register(
      "benger_spawn_egg", () -> new OverworldBossSpawnEggItem(FTEntities.BENGER, 2763310, 15592162, new Properties())
   );
   public static final DeferredHolder<Item, Item> PUFFER_BAT = ITEMS.register("puffer_bat", PufferBatItem::new);
   public static final DeferredHolder<Item, Item> FORBIDDEN_APPLE = ITEMS.register(
      "forbidden_apple", () -> new ForbiddenAppleItem(new Properties().stacksTo(1))
   );
   public static final DeferredHolder<Item, Item> APPLE_STEM = ITEMS.register(
      "apple_stem", () -> new SwordItem(Tiers.NETHERITE, new Properties().attributes(SwordItem.createAttributes(Tiers.NETHERITE, 3, -2.2F)))
   );
   public static final DeferredHolder<Item, Item> CANDY_CANE = ITEMS.register("candy_cane", () -> new CandyCaneItem(new Properties()));
   public static final DeferredHolder<Item, Item> STATUE_YOUZI = ITEMS.register("statue_youzi", YouziStatueItem::new);
   public static final DeferredHolder<Item, Item> WENXUANLAN_FLYWHEEL = ITEMS.register("wenxuanlan_flywheel", XuanlanMoonWeaponItem::new);
   public static final DeferredHolder<Item, Item> BLOOMING_THORNS = ITEMS.register(
      "blooming_thorns", () -> new SwordItem(Tiers.NETHERITE, new Properties().attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4, -2.4F)))
   );
   public static final DeferredHolder<Item, Item> BARREL_CROSSBOW = ITEMS.register(
      "barrel_crossbow", () -> new BarrelCrossbowItem(new Properties().stacksTo(1).durability(384).rarity(Rarity.RARE))
   );
   public static final DeferredHolder<Item, Item> BARREL_CROSSBOW_READY = ITEMS.register("barrel_crossbow_ready", () -> new Item(new Properties()));
   public static final DeferredHolder<Item, Item> EASTER_EGG_YOUZI = easterEgg("easter_egg_youzi", 0, 15254858, 7248718);
   public static final DeferredHolder<Item, Item> EASTER_EGG_DANDAN = easterEgg("easter_egg_dandan", 1, 15919576, 14721852);
   public static final DeferredHolder<Item, Item> EASTER_EGG_DUOLA = easterEgg("easter_egg_duola", 2, 15232936, 16773299);
   public static final DeferredHolder<Item, Item> EASTER_EGG_GONGDI = easterEgg("easter_egg_gongdi", 3, 9182236, 2829099);
   public static final DeferredHolder<Item, Item> EASTER_EGG_NINGMENGMAO = easterEgg("easter_egg_ningmengmao", 4, 16245323, 4868682);
   public static final DeferredHolder<Item, Item> EASTER_EGG_QINGYI = easterEgg("easter_egg_qingyi", 5, 3046794, 12577000);
   public static final DeferredHolder<Item, Item> EASTER_EGG_WENXUANLAN = easterEgg("easter_egg_wenxuanlan", 6, 2837659, 10467048);
   public static final DeferredHolder<Item, Item> EASTER_EGG_ZHEERGEN = easterEgg("easter_egg_zheergen", 7, 5143098, 14275232);
   public static final DeferredHolder<Item, Item> EASTER_EGG_ZICAI = easterEgg("easter_egg_zicai", 8, 6958702, 12733290);
   public static final DeferredHolder<Item, Item> EASTER_EGG_XUELI = easterEgg("easter_egg_xueli", 10, 12575733, 3037816);
   public static final DeferredHolder<Item, Item> EASTER_EGG_QIZAI = easterEgg("easter_egg_qizai", 11, 16774134, 15565486);
   public static final DeferredHolder<Item, Item> QIZAI_PRAYER_STAFF = ITEMS.register("qizai_prayer_staff", QizaiPrayerStaffItem::new);
   public static final DeferredHolder<Item, Item> GOURMET_BLANK_BLADE = ITEMS.register("gourmet_blank_blade", GourmetBlankBladeItem::new);
   public static final List<DeferredHolder<Item, Item>> EASTER_EGGS = List.of(
      EASTER_EGG_YOUZI,
      EASTER_EGG_DANDAN,
      EASTER_EGG_DUOLA,
      EASTER_EGG_GONGDI,
      EASTER_EGG_NINGMENGMAO,
      EASTER_EGG_QINGYI,
      EASTER_EGG_WENXUANLAN,
      EASTER_EGG_ZHEERGEN,
      EASTER_EGG_ZICAI,
      EASTER_EGG_XUELI,
      EASTER_EGG_QIZAI
   );
   public static final DeferredHolder<Item, Item> MUSIC_DISC_MANOR_CUE = ITEMS.register(
      "music_disc_manor_cue", () -> musicDisc("music_disc_manor_cue", Rarity.RARE)
   );
   public static final DeferredHolder<Item, Item> MUSIC_DISC_MANOR_BATTLE = ITEMS.register(
      "music_disc_manor_battle", () -> musicDisc("music_disc_manor_battle", Rarity.EPIC)
   );
   public static final DeferredHolder<Item, Item> MUSIC_DISC_MANOR_DUAL = ITEMS.register(
      "music_disc_manor_dual", () -> musicDisc("music_disc_manor_dual", Rarity.EPIC)
   );
   public static final DeferredHolder<Item, Item> MUSIC_DISC_MANOR_GUIGNOL = ITEMS.register(
      "music_disc_manor_guignol", () -> musicDisc("music_disc_manor_guignol", Rarity.EPIC)
   );
   public static final DeferredHolder<Item, Item> MUSIC_DISC_BENGER = ITEMS.register("music_disc_benger", () -> musicDisc("music_disc_benger", Rarity.EPIC));
   public static final DeferredHolder<Item, Item> MUSIC_DISC_APPLE = ITEMS.register("music_disc_apple", () -> musicDisc("music_disc_apple", Rarity.RARE));
   public static final DeferredHolder<Item, Item> MUSIC_DISC_APPLE_P2 = ITEMS.register(
      "music_disc_apple_p2", () -> musicDisc("music_disc_apple_p2", Rarity.RARE)
   );
   public static final DeferredHolder<Item, Item> MUSIC_DISC_GINGERBREAD = ITEMS.register(
      "music_disc_gingerbread", () -> musicDisc("music_disc_gingerbread", Rarity.RARE)
   );
   public static final DeferredHolder<Item, Item> MUSIC_DISC_BUTCHER = ITEMS.register("music_disc_butcher", () -> musicDisc("music_disc_butcher", Rarity.RARE));
   public static final DeferredHolder<Item, Item> MUSIC_DISC_SAYA = ITEMS.register("music_disc_saya", () -> musicDisc("music_disc_saya", Rarity.RARE));
   public static final DeferredHolder<Item, Item> MUSIC_DISC_TAVERN = ITEMS.register("music_disc_tavern", () -> musicDisc("music_disc_tavern", Rarity.RARE));
   public static final DeferredHolder<Item, Item> MUSIC_DISC_RAT = ITEMS.register("music_disc_rat", () -> musicDisc("music_disc_rat", Rarity.RARE));
   public static final DeferredHolder<Item, Item> MUSIC_DISC_DEFECT = ITEMS.register("music_disc_defect", () -> musicDisc("music_disc_defect", Rarity.RARE));
   public static final DeferredHolder<Item, Item> MUSIC_DISC_COMPOST = ITEMS.register("music_disc_compost", () -> musicDisc("music_disc_compost", Rarity.RARE));
   public static final DeferredHolder<Item, Item> MUSIC_DISC_COMPOST_P2 = ITEMS.register(
      "music_disc_compost_p2", () -> musicDisc("music_disc_compost_p2", Rarity.RARE)
   );
   public static final List<DeferredHolder<Item, Item>> FOOD_MUSIC_DISCS = List.of(
      MUSIC_DISC_APPLE,
      MUSIC_DISC_APPLE_P2,
      MUSIC_DISC_GINGERBREAD,
      MUSIC_DISC_BUTCHER,
      MUSIC_DISC_SAYA,
      MUSIC_DISC_TAVERN,
      MUSIC_DISC_RAT,
      MUSIC_DISC_DEFECT,
      MUSIC_DISC_COMPOST,
      MUSIC_DISC_COMPOST_P2,
      MUSIC_DISC_TIDE_FISH,
      MUSIC_DISC_TIDE_FISH_P2
   );

   private static Item musicDisc(String var0, Rarity var1) {
      return new Item(
         new Properties()
            .stacksTo(1)
            .rarity(var1)
            .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath("ftboss", var0)))
      );
   }

   private static DeferredHolder<Item, Item> easterEgg(String var0, int var1, int var2, int var3) {
      return ITEMS.register(var0, () -> new EasterSpawnEggItem(var1, var2, var3));
   }

   private FTBossItems() {
   }

   public static void register(IEventBus var0) {
      BossRelicItems.init();
      BossMaterialDrops.init();
      ITEMS.register(var0);
      FTRecipeSerializers.RECIPE_SERIALIZERS.register(var0);
   }
}
