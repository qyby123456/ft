/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityType$Builder
 *  net.minecraft.world.entity.EntityType$EntityFactory
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraftforge.event.entity.EntityAttributeCreationEvent
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.AppleCloneEntity;
import net.farmingtales.ftboss.entity.AppleMothEntity;
import net.farmingtales.ftboss.entity.AppleWormEntity;
import net.farmingtales.ftboss.entity.BarrelCannonballEntity;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.BengerPupEntity;
import net.farmingtales.ftboss.entity.ChargedOrbEntity;
import net.farmingtales.ftboss.entity.CoinManEntity;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.CompostSlimeEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FlyMinionEntity;
import net.farmingtales.ftboss.entity.FlywheelProjectileEntity;
import net.farmingtales.ftboss.entity.GiantGrubEntity;
import net.farmingtales.ftboss.entity.GingerbreadMinionEntity;
import net.farmingtales.ftboss.entity.GreenAppleEntity;
import net.farmingtales.ftboss.entity.MaggotMinionEntity;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.PlayerMoonChakramEntity;
import net.farmingtales.ftboss.entity.PoopMinionEntity;
import net.farmingtales.ftboss.entity.QingyiConfluenceTridentEntity;
import net.farmingtales.ftboss.entity.QizaiCrystalEntity;
import net.farmingtales.ftboss.entity.QizaiCrystalMissileEntity;
import net.farmingtales.ftboss.entity.QizaiSigilEntity;
import net.farmingtales.ftboss.entity.QizaiStarEntity;
import net.farmingtales.ftboss.entity.RainbowPoopEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.ReefMinionEntity;
import net.farmingtales.ftboss.entity.RottenAppleEntity;
import net.farmingtales.ftboss.entity.ShadowPearlEntity;
import net.farmingtales.ftboss.entity.ShadowPhantomEntity;
import net.farmingtales.ftboss.entity.SheepDungEntity;
import net.farmingtales.ftboss.entity.SmallBarrelEntity;
import net.farmingtales.ftboss.entity.StockpotEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.TeaMinionEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.entity.TideBoltEntity;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.farmingtales.ftboss.entity.WineBarrelDecoyEntity;
import net.farmingtales.ftboss.entity.XueliKnifeEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public class FTEntities {
    public static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create((IForgeRegistry)ForgeRegistries.ENTITY_TYPES, (String)"ftboss");
    public static final RegistryObject<EntityType<TideFishBossEntity>> TIDE_FISH_BOSS = FTEntities.register("tide_fish_boss", TideFishBossEntity::new, 2.4f, 3.5f);
    public static final RegistryObject<EntityType<ReefMinionEntity>> REEF_FISH = FTEntities.register("reef_fish", ReefMinionEntity::new, 0.8f, 0.7f);
    public static final RegistryObject<EntityType<ReefMinionEntity>> BRINE_PUFFER = FTEntities.register("brine_puffer", ReefMinionEntity::new, 0.8f, 0.8f);
    public static final RegistryObject<EntityType<TideBoltEntity>> TIDE_BOLT = FTEntities.register("tide_bolt", TideBoltEntity::new, 0.25f, 0.25f, MobCategory.MISC);
    public static final RegistryObject<EntityType<QizaiStarEntity>> QIZAI_STAR = TYPES.register("qizai_star", () -> EntityType.Builder.m_20704_(QizaiStarEntity::new, (MobCategory)MobCategory.MISC).m_20699_(0.3f, 0.3f).m_20702_(8).m_20717_(1).m_20712_("ftboss:qizai_star"));
    public static final RegistryObject<EntityType<QizaiCrystalEntity>> QIZAI_CRYSTAL = TYPES.register("qizai_crystal", () -> EntityType.Builder.m_20704_(QizaiCrystalEntity::new, (MobCategory)MobCategory.MISC).m_20699_(0.85f, 1.35f).m_20702_(10).m_20717_(2).m_20716_().m_20698_().m_20712_("ftboss:qizai_crystal"));
    public static final RegistryObject<EntityType<QizaiCrystalMissileEntity>> QIZAI_CRYSTAL_MISSILE = TYPES.register("qizai_crystal_missile", () -> EntityType.Builder.m_20704_(QizaiCrystalMissileEntity::new, (MobCategory)MobCategory.MISC).m_20699_(0.25f, 0.25f).m_20702_(10).m_20717_(1).m_20716_().m_20698_().m_20712_("ftboss:qizai_crystal_missile"));
    public static final RegistryObject<EntityType<QizaiSigilEntity>> QIZAI_SIGIL = TYPES.register("qizai_sigil", () -> EntityType.Builder.m_20704_(QizaiSigilEntity::new, (MobCategory)MobCategory.MISC).m_20699_(1.0f, 1.0f).m_20702_(8).m_20717_(20).m_20716_().m_20698_().m_20712_("ftboss:qizai_sigil"));
    public static final RegistryObject<EntityType<QingyiConfluenceTridentEntity>> QINGYI_CONFLUENCE_TRIDENT = TYPES.register("qingyi_confluence_trident", () -> EntityType.Builder.m_20704_(QingyiConfluenceTridentEntity::new, (MobCategory)MobCategory.MISC).m_20699_(0.5f, 0.5f).m_20702_(8).m_20717_(1).m_20712_("ftboss:qingyi_confluence_trident"));
    public static final RegistryObject<EntityType<SugarQueenEntity>> SUGAR_QUEEN = FTEntities.register("sugar_queen", SugarQueenEntity::new, 1.2f, 2.7f);
    public static final RegistryObject<EntityType<MeatMountainEntity>> MEAT_MOUNTAIN = FTEntities.register("meat_mountain", MeatMountainEntity::new, 1.5f, 2.6f);
    public static final RegistryObject<EntityType<WineBarrelDecoyEntity>> WINE_BARREL_DECOY = FTEntities.register("wine_barrel_decoy", WineBarrelDecoyEntity::new, 3.0f, 3.0f);
    public static final RegistryObject<EntityType<ShadowPhantomEntity>> SHADOW_PHANTOM = FTEntities.register("shadow_phantom", ShadowPhantomEntity::new, 0.6f, 1.8f, MobCategory.MISC);
    public static final RegistryObject<EntityType<WineBarrelBossEntity>> WINE_BARREL = FTEntities.register("wine_barrel", WineBarrelBossEntity::new, 3.0f, 3.0f);
    public static final RegistryObject<EntityType<RatBossEntity>> RAT_BOSS = FTEntities.register("rat_boss", RatBossEntity::new, 0.6f, 0.975f);
    public static final RegistryObject<EntityType<TeaMinionEntity>> TEA_MINION = FTEntities.register("tea_minion", TeaMinionEntity::new, 0.8f, 1.3f);
    public static final RegistryObject<EntityType<GingerbreadMinionEntity>> GINGERBREAD_MINION = FTEntities.register("gingerbread_minion", GingerbreadMinionEntity::new, 0.8f, 1.1f);
    public static final RegistryObject<EntityType<DefectCoreEntity>> DEFECT_CORE = FTEntities.register("defect_core", DefectCoreEntity::new, 2.4f, 3.8f);
    public static final RegistryObject<EntityType<ChargedOrbEntity>> CHARGED_ORB = FTEntities.register("charged_orb", ChargedOrbEntity::new, 0.5f, 0.5f);
    public static final RegistryObject<EntityType<MaggotMinionEntity>> MAGGOT_MINION = FTEntities.register("maggot_minion", MaggotMinionEntity::new, 0.7f, 0.5f);
    public static final RegistryObject<EntityType<SmallBarrelEntity>> BARREL_MINION = FTEntities.register("barrel_minion", SmallBarrelEntity::new, 0.8f, 1.2f);
    public static final RegistryObject<EntityType<BarrelCannonballEntity>> BARREL_CANNONBALL = FTEntities.register("barrel_cannonball", BarrelCannonballEntity::new, 0.5f, 0.5f, MobCategory.MISC);
    public static final RegistryObject<EntityType<RainbowPoopEntity>> RAINBOW_POOP = FTEntities.register("rainbow_poop", RainbowPoopEntity::new, 0.5f, 0.5f, MobCategory.MISC);
    public static final RegistryObject<EntityType<ShadowPearlEntity>> SHADOW_PEARL = FTEntities.register("shadow_pearl", ShadowPearlEntity::new, 0.25f, 0.25f, MobCategory.MISC);
    public static final RegistryObject<EntityType<FlywheelProjectileEntity>> FLYWHEEL = FTEntities.register("flywheel_projectile", FlywheelProjectileEntity::new, 0.4f, 0.4f, MobCategory.MISC);
    public static final RegistryObject<EntityType<PlayerMoonChakramEntity>> PLAYER_MOON_CHAKRAM = TYPES.register("player_moon_chakram", () -> EntityType.Builder.m_20704_(PlayerMoonChakramEntity::new, (MobCategory)MobCategory.MISC).m_20699_(0.4f, 0.4f).m_20702_(8).m_20717_(1).m_20712_(new ResourceLocation("ftboss", "player_moon_chakram").toString()));
    public static final RegistryObject<EntityType<StockpotEntity>> STOCKPOT = FTEntities.register("stockpot", StockpotEntity::new, 0.5f, 0.5f, MobCategory.MISC);
    public static final RegistryObject<EntityType<XueliKnifeEntity>> XUELI_KNIFE = FTEntities.register("xueli_knife", XueliKnifeEntity::new, 0.4f, 0.4f, MobCategory.MISC);
    public static final RegistryObject<EntityType<PoopMinionEntity>> POOP_MINION = FTEntities.register("poop_minion", PoopMinionEntity::new, 0.8f, 0.9f);
    public static final RegistryObject<EntityType<FlyMinionEntity>> FLY_MINION = FTEntities.register("fly_minion", FlyMinionEntity::new, 0.7f, 0.6f);
    public static final RegistryObject<EntityType<CompostSlimeEntity>> COMPOST_SLIME = FTEntities.register("compost_slime", CompostSlimeEntity::new, 1.0f, 1.0f);
    public static final RegistryObject<EntityType<SheepDungEntity>> SHEEP_DUNG = FTEntities.register("sheep_dung", SheepDungEntity::new, 0.8f, 0.8f);
    public static final RegistryObject<EntityType<AppleWormEntity>> APPLE_WORM = FTEntities.register("apple_worm", AppleWormEntity::new, 0.7f, 0.5f);
    public static final RegistryObject<EntityType<AppleMothEntity>> APPLE_MOTH = FTEntities.register("apple_moth", AppleMothEntity::new, 0.7f, 0.6f);
    public static final RegistryObject<EntityType<RottenAppleEntity>> ROTTEN_APPLE = FTEntities.register("rotten_apple", RottenAppleEntity::new, 0.8f, 0.9f);
    public static final RegistryObject<EntityType<GreenAppleEntity>> GREEN_APPLE = FTEntities.register("green_apple", GreenAppleEntity::new, 0.8f, 0.9f);
    public static final RegistryObject<EntityType<CoinManEntity>> COIN_MAN = FTEntities.register("coin_man", CoinManEntity::new, 0.7f, 1.5f);
    public static final RegistryObject<EntityType<AppleCloneEntity>> APPLE_CLONE = FTEntities.register("apple_clone", AppleCloneEntity::new, 1.5f, 2.4f);
    public static final RegistryObject<EntityType<EasterBossEntity>> EASTER_BOSS = FTEntities.register("easter_boss", EasterBossEntity::new, 0.6f, 1.8f);
    public static final RegistryObject<EntityType<GiantGrubEntity>> GIANT_GRUB = FTEntities.register("giant_grub", GiantGrubEntity::new, 3.0f, 2.0f);
    public static final RegistryObject<EntityType<AppleBossEntity>> APPLE_BOSS = FTEntities.register("apple_boss", AppleBossEntity::new, 1.7f, 2.7f);
    public static final RegistryObject<EntityType<CompostBossEntity>> COMPOST_BOSS = FTEntities.register("compost_boss", CompostBossEntity::new, 1.6f, 3.4f);
    public static final RegistryObject<EntityType<TeaTreeBossEntity>> TEA_TREE = FTEntities.register("tea_tree", TeaTreeBossEntity::new, 1.3f, 2.1f);
    public static final RegistryObject<EntityType<BengerEntity>> BENGER = FTEntities.register("benger", BengerEntity::new, 1.7f, 3.5f);
    public static final RegistryObject<EntityType<BengerPupEntity>> BENGER_PUP = FTEntities.register("benger_pup", BengerPupEntity::new, 0.9f, 2.0f);

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String id, EntityType.EntityFactory<T> factory, float width, float height) {
        return FTEntities.register(id, factory, width, height, MobCategory.MONSTER);
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String id, EntityType.EntityFactory<T> factory, float width, float height, MobCategory category) {
        return TYPES.register(id, () -> EntityType.Builder.m_20704_((EntityType.EntityFactory)factory, (MobCategory)category).m_20699_(width, height).m_20702_(10).m_20712_(new ResourceLocation("ftboss", id).toString()));
    }

    private static void attr(EntityAttributeCreationEvent e, EntityType<? extends LivingEntity> type, AttributeSupplier sup) {
        e.put(type, sup);
        try {
            if (!sup.m_22258_(Attributes.f_22281_)) {
                FTBossMod.LOGGER.error("[FTBoss] \u5c5e\u6027\u81ea\u68c0: {} \u7684\u5c5e\u6027\u8868\u7f3a ATTACK_DAMAGE \u2014\u2014 \u5b83\u4e00\u8fd1\u6218\u5c31\u4f1a\u5d29\u670d, \u8bf7\u5728\u5176 createAttributes() \u91cc\u8865\u4e0a!", (Object)EntityType.m_20613_(type));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @SubscribeEvent
    public void onAttributeCreation(EntityAttributeCreationEvent e) {
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)QIZAI_CRYSTAL.get()), QizaiCrystalEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)TIDE_FISH_BOSS.get()), TideFishBossEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)REEF_FISH.get()), ReefMinionEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)BRINE_PUFFER.get()), ReefMinionEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)SUGAR_QUEEN.get()), SugarQueenEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)MEAT_MOUNTAIN.get()), MeatMountainEntity.m_34104_().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)TEA_TREE.get()), TeaTreeBossEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)COMPOST_BOSS.get()), CompostBossEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)POOP_MINION.get()), PoopMinionEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)FLY_MINION.get()), FlyMinionEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)COMPOST_SLIME.get()), CompostSlimeEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)SHEEP_DUNG.get()), SheepDungEntity.m_33000_().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)APPLE_WORM.get()), AppleWormEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)APPLE_MOTH.get()), AppleMothEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)ROTTEN_APPLE.get()), RottenAppleEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)GREEN_APPLE.get()), GreenAppleEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)COIN_MAN.get()), CoinManEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)APPLE_CLONE.get()), AppleCloneEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)EASTER_BOSS.get()), EasterBossEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)GIANT_GRUB.get()), GiantGrubEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)APPLE_BOSS.get()), AppleBossEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)WINE_BARREL.get()), WineBarrelBossEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)WINE_BARREL_DECOY.get()), WineBarrelDecoyEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)SHADOW_PHANTOM.get()), ShadowPhantomEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)RAT_BOSS.get()), RatBossEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)TEA_MINION.get()), TeaMinionEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)GINGERBREAD_MINION.get()), GingerbreadMinionEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)BARREL_MINION.get()), SmallBarrelEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)DEFECT_CORE.get()), DefectCoreEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)CHARGED_ORB.get()), ChargedOrbEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)MAGGOT_MINION.get()), MaggotMinionEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)BENGER.get()), BengerEntity.createAttributes().m_22265_());
        FTEntities.attr(e, (EntityType<? extends LivingEntity>)((EntityType)BENGER_PUP.get()), BengerPupEntity.createAttributes().m_22265_());
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.register((Object)new FTEntities());
    }
}

