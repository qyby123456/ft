/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Multimap
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.IntTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.LongTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.TagKey
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.food.FoodProperties
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.DiggerItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.ShearsItem
 *  net.minecraft.world.item.TridentItem
 *  net.minecraft.world.item.UseAnim
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeManager
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.GrindstoneEvent$OnPlaceItem
 *  net.minecraftforge.event.ItemAttributeModifierEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.EntityItemPickupEvent
 *  net.minecraftforge.event.entity.player.ItemTooltipEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$BreakSpeed
 *  net.minecraftforge.event.entity.player.PlayerEvent$Clone
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import com.google.common.collect.Multimap;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.api.SwordMaterialDefinition;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.item.AdditionalWeaponUiEligibility;
import net.farmingtales.ftboss.item.FoodAppraisalMonocleItem;
import net.farmingtales.ftboss.item.FoodSocketFoodCatalog;
import net.farmingtales.ftboss.item.FoodSocketPowers;
import net.farmingtales.ftboss.item.FoodWeaponBondHits;
import net.farmingtales.ftboss.item.FoodWeaponDamageSources;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponFinalDamage;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponMaterialFingerprint;
import net.farmingtales.ftboss.item.FoodWeaponOriginalSequence;
import net.farmingtales.ftboss.item.FoodWeaponProgression;
import net.farmingtales.ftboss.item.FoodWeaponRepair;
import net.farmingtales.ftboss.item.FoodWeaponSkillRarity;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.FourWeaponUseGuard;
import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.item.weapons.GourmetEvolutionWeapon;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponDamageAccounting;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss")
public final class FoodWeaponEvolution {
    private static boolean registered;
    private static final String FOOD_BLESSINGS = "FoodBlessings";
    private static final String LAST_FOOD_BLESSING = "LastFoodBlessing.";
    private static final UUID SOCKET_LUCK;
    private static final String DAILY_ROOT = "FTBossFoodEvolutionDailies";
    private static final String LEVEL = "Level";
    private static final String LAST_SOCKET_PROC = "LastSocketProc";
    private static final EnumMap<FoodType, List<ResourceLocation>> DAILY_FOOD_POOLS;
    private static final String DAILY_REWARD_AMOUNT = "RewardAmount";
    private static final UUID EVOLUTION_DAMAGE;
    private static final String GOURMET_SKILLS = "EvolutionSkills";
    private static RecipeManager dailyRecipeManager;
    private static final UUID SOCKET_HEALTH;
    private static final String SECOND_PROFILE = "SecondProfile";
    private static final ResourceLocation COIN_ID;
    private static final int[] GOURMET_THRESHOLDS;
    private static final String CREATE_NAMESPACE = "create";
    private static final UUID SOCKET_MOVEMENT;
    private static final String XP = "Experience";
    private static final String PROFILE = "Profile";
    private static final String DAILY_REQUEST = "RequestedItem";
    private static final Set<String> VANILLA_NATURAL_DAILY_FOODS;
    private static final String SOCKETS = "Sockets";
    private static final UUID SOCKET_ARMOR;
    public static final int DAILY_TASK_COUNT = 12;
    private static final String DAILY_REWARD_TYPE = "RewardType";
    private static final UUID SOCKET_TOUGHNESS;
    private static final String DAILY_DAY = "MinecraftDay";
    private static final String LAST_SKILL_PROC = "LastSkillProc";
    private static final String SKILL = "EvolutionSkill";
    private static final int MAX_FOOD_BLESSINGS = 6;
    private static final List<TagKey<Item>> DAILY_NATURAL_FOOD_TAGS;
    private static final UUID EVOLUTION_SPEED;
    public static final int DAILY_FOOD_COUNT = 8;
    private static final String ROOT = "FTBossFoodEvolution";
    private static final String GOURMET_ACTIVE_PROFILE = "ActiveProfile";
    private static Set<ResourceLocation> dailyRecipeOutputs;
    public static final int FIRST_THRESHOLD = 100;
    public static final int ON_HIT_LIFESTEAL_COOLDOWN_TICKS = 60;
    private static final String GOURMET_STATS = "EvolutionStats";
    private static final ThreadLocal<Boolean> EXTRA_DAMAGE;
    private static int dailyRecipeFingerprint;
    public static final int SECOND_THRESHOLD = 250;
    public static final int GOURMET_MAX_LEVEL = 8;
    public static final int GOURMET_SOCKET_CAPACITY = 30;
    private static final String GOURMET_ACTIVE_SKILL = "ActiveSkillIndex";
    private static final String DAILY_REQUIRED = "Required";
    private static final String BLESSING_WASHED = "BlessingWashed";

    private static void applyFoodBlessingHits(LivingHurtEvent event, ServerPlayer player, LivingEntity target, CompoundTag data, long now) {
        ItemStack weapon = player.m_21205_();
        ListTag blessingList = FoodWeaponEvolution.foodBlessingList(weapon);
        String fortuneKey = "FTBossFoodFortune:" + player.m_20148_();
        String fortuneLevelKey = fortuneKey + ":Level";
        if (target.getPersistentData().m_128454_(fortuneKey) >= now) {
            int markLevel = Math.max(1, target.getPersistentData().m_128451_(fortuneLevelKey));
            event.setAmount(event.getAmount() * (1.12f + (float)markLevel * 0.06f));
            target.getPersistentData().m_128473_(fortuneKey);
            target.getPersistentData().m_128473_(fortuneLevelKey);
        }
        block23: for (int i = 0; i < blessingList.size(); ++i) {
            CompoundTag entry = blessingList.m_128728_(i);
            FoodBlessing blessing = FoodBlessing.byId(entry.m_128461_("Blessing"));
            if (blessing == null) continue;
            int level = FoodWeaponEvolution.foodBlessingLevel(entry);
            float chance = Math.min(0.36f, 0.12f + (float)level * 0.05f);
            switch (blessing.kind) {
                case CRIT: {
                    if (!(player.m_217043_().m_188501_() < chance)) continue block23;
                    event.setAmount(event.getAmount() * (1.3f + (float)level * 0.12f));
                    continue block23;
                }
                case EXECUTE: {
                    double ratio = target.m_21223_() / Math.max(1.0f, target.m_21233_());
                    if (!(ratio <= 0.12 + (double)level * 0.05)) continue block23;
                    event.setAmount(event.getAmount() * (1.2f + (float)level * 0.12f));
                    continue block23;
                }
                case ABSORPTION: {
                    if (!(player.m_217043_().m_188501_() < chance)) continue block23;
                    player.m_147207_(new MobEffectInstance(MobEffects.f_19617_, 60 + level * 20, level - 1, false, false, true), (Entity)player);
                    continue block23;
                }
                case CHAIN: {
                    if (!(player.m_217043_().m_188501_() < chance)) continue block23;
                    FoodWeaponEvolution.chainArc(player, target, 2.0f + (float)level * 1.5f);
                    continue block23;
                }
                case OVERLOAD: {
                    if (!(player.m_217043_().m_188501_() < chance)) continue block23;
                    FoodWeaponEvolution.overloadBurst(player, target, 1.5f + (float)level * 1.25f);
                    continue block23;
                }
                case BURN: {
                    if (!(player.m_217043_().m_188501_() < chance)) continue block23;
                    target.m_20254_(2 + level * 2);
                    continue block23;
                }
                case MARK: {
                    if (!(player.m_217043_().m_188501_() < chance)) continue block23;
                    target.getPersistentData().m_128356_(fortuneKey, now + 100L + (long)level * 40L);
                    target.getPersistentData().m_128405_(fortuneLevelKey, level);
                    target.m_147207_(new MobEffectInstance(MobEffects.f_19619_, 100 + level * 40, 0, false, false, true), (Entity)player);
                    continue block23;
                }
                case LIFESTEAL: {
                    String key = LAST_FOOD_BLESSING + blessing.id;
                    if (now - data.m_128454_(key) < 60L) continue block23;
                    player.m_5634_(0.35f * (float)level);
                    data.m_128356_(key, now);
                    continue block23;
                }
                case POISON: {
                    if (!(player.m_217043_().m_188501_() < chance)) continue block23;
                    target.m_147207_(new MobEffectInstance(MobEffects.f_19614_, 40 + level * 20, Math.min(2, level - 1), false, false, true), (Entity)player);
                    continue block23;
                }
                case WITHER: {
                    if (!(player.m_217043_().m_188501_() < chance)) continue block23;
                    target.m_147207_(new MobEffectInstance(MobEffects.f_19615_, 30 + level * 15, Math.min(1, level - 1), false, false, true), (Entity)player);
                    continue block23;
                }
                case ROOT: {
                    if (!(player.m_217043_().m_188501_() < chance)) continue block23;
                    target.m_147207_(new MobEffectInstance(MobEffects.f_19597_, 60 + level * 25, Math.min(3, level), false, false, true), (Entity)player);
                    continue block23;
                }
                case REGEN: {
                    if (!(player.m_217043_().m_188501_() < chance)) continue block23;
                    player.m_147207_(new MobEffectInstance(MobEffects.f_19605_, 40 + level * 20, level - 1, false, false, true), (Entity)player);
                    continue block23;
                }
                case CORK_RUSH: {
                    if (!(player.m_217043_().m_188501_() < chance)) continue block23;
                    player.m_147207_(new MobEffectInstance(MobEffects.f_19598_, 80 + level * 20, level - 1, false, false, true), (Entity)player);
                    player.m_147207_(new MobEffectInstance(MobEffects.f_19596_, 80 + level * 20, level - 1, false, false, true), (Entity)player);
                    continue block23;
                }
                case FRENZY: {
                    long last = data.m_128454_(LAST_FOOD_BLESSING + blessing.id);
                    int stacks = now - last <= 60L ? Math.min(level + 2, data.m_128451_("CheeseFrenzyStacks") + 1) : 1;
                    data.m_128405_("CheeseFrenzyStacks", stacks);
                    data.m_128356_(LAST_FOOD_BLESSING + blessing.id, now);
                    event.setAmount(event.getAmount() * (1.0f + (float)stacks * (0.025f + (float)level * 0.01f)));
                    continue block23;
                }
                case PRISMATIC: {
                    if (!(player.m_217043_().m_188501_() < chance)) continue block23;
                    target.m_147207_(new MobEffectInstance(MobEffects.f_19613_, 60 + level * 20, Math.min(2, level - 1), false, false, true), (Entity)player);
                    target.m_147207_(new MobEffectInstance(MobEffects.f_19604_, 80 + level * 20, 0, false, false, true), (Entity)player);
                    continue block23;
                }
                case REPAIR: {
                    String key = LAST_FOOD_BLESSING + blessing.id;
                    if (!weapon.m_41768_() || now - data.m_128454_(key) < (long)Math.max(20, 70 - level * 15)) continue block23;
                    weapon.m_41721_(Math.max(0, weapon.m_41773_() - level));
                    data.m_128356_(key, now);
                    continue block23;
                }
                case EMERGENCY_HEART: {
                    String key = LAST_FOOD_BLESSING + blessing.id;
                    if (!(player.m_21223_() <= player.m_21233_() * 0.5f) || now - data.m_128454_(key) < 100L) continue block23;
                    player.m_5634_(0.75f * (float)level);
                    player.m_147207_(new MobEffectInstance(MobEffects.f_19617_, 60, 0, false, false, true), (Entity)player);
                    data.m_128356_(key, now);
                    continue block23;
                }
                case UNDERTOW: {
                    if (!target.m_20071_()) continue block23;
                    event.setAmount(event.getAmount() * (1.1f + (float)level * 0.05f));
                    player.m_147207_(new MobEffectInstance(MobEffects.f_19593_, 60 + level * 20, 0, false, false, true), (Entity)player);
                    continue block23;
                }
                case BRINE: {
                    if (!(player.m_217043_().m_188501_() < chance)) continue block23;
                    target.m_147207_(new MobEffectInstance(MobEffects.f_19599_, 60 + level * 25, Math.min(2, level - 1), false, false, true), (Entity)player);
                    continue block23;
                }
                case ISOLATION: {
                    boolean isolated = target.m_9236_().m_6443_(LivingEntity.class, target.m_20191_().m_82400_(4.0), other -> other != target && FoodWeaponEvolution.canTarget(player, other)).isEmpty();
                    if (!isolated) continue block23;
                    event.setAmount(event.getAmount() * (1.1f + (float)level * 0.05f));
                    continue block23;
                }
                case HUNT: {
                    String huntKey = "FTBossFoodHunt:" + player.m_20148_();
                    if (target.getPersistentData().m_128454_(huntKey) >= now) {
                        event.setAmount(event.getAmount() * (1.05f + (float)level * 0.035f));
                        continue block23;
                    }
                    target.getPersistentData().m_128356_(huntKey, now + 160L + (long)level * 40L);
                    target.m_147207_(new MobEffectInstance(MobEffects.f_19619_, 160 + level * 40, 0, false, false, true), (Entity)player);
                    player.m_147207_(new MobEffectInstance(MobEffects.f_19596_, 60 + level * 20, Math.min(1, level - 1), false, false, true), (Entity)player);
                    continue block23;
                }
            }
        }
    }

    private static /* synthetic */ boolean lambda$overloadBurst$8(LivingEntity origin, ServerPlayer player, LivingEntity target) {
        return target != origin && FoodWeaponEvolution.canTarget(player, target);
    }

    private static /* synthetic */ boolean lambda$chainArc$7(LivingEntity origin, ServerPlayer player, LivingEntity target) {
        return target != origin && FoodWeaponEvolution.canTarget(player, target);
    }

    private static void applyEvolutionSkill(LivingHurtEvent livingHurtEvent, ServerPlayer serverPlayer, LivingEntity livingEntity, CompoundTag compoundTag, EvolutionSkill evolutionSkill, long l, String string, float f) {
        int n = evolutionSkill.effect == SkillEffect.LIFESTEAL ? 60 : 20;
        boolean bl = l - compoundTag.m_128454_(string) >= (long)n;
        boolean bl2 = bl && serverPlayer.m_217043_().m_188501_() < 0.28f;
        switch (evolutionSkill.effect) {
            case DAMAGE: {
                livingHurtEvent.setAmount(livingHurtEvent.getAmount() * (1.0f + 0.18f * f));
                break;
            }
            case CRIT: {
                if (!bl2) break;
                livingHurtEvent.setAmount(livingHurtEvent.getAmount() * (1.0f + 0.6f * f));
                break;
            }
            case LIFESTEAL: {
                if (!bl) break;
                serverPlayer.m_5634_(1.0f);
                bl2 = true;
                break;
            }
            case SLOW: {
                if (!bl2) break;
                livingEntity.m_147207_(new MobEffectInstance(MobEffects.f_19597_, 80, 1), (Entity)serverPlayer);
                break;
            }
            case POISON: {
                if (!bl2) break;
                livingEntity.m_147207_(new MobEffectInstance(MobEffects.f_19614_, 80, 0), (Entity)serverPlayer);
                break;
            }
            case FIRE: {
                if (!bl2) break;
                livingEntity.m_20254_(4);
                break;
            }
            case GUARD: {
                if (!bl2) break;
                serverPlayer.m_147207_(new MobEffectInstance(MobEffects.f_19617_, 100, 0), (Entity)serverPlayer);
                break;
            }
            case HASTE: {
                if (!bl2) break;
                serverPlayer.m_147207_(new MobEffectInstance(MobEffects.f_19596_, 100, 0), (Entity)serverPlayer);
                serverPlayer.m_147207_(new MobEffectInstance(MobEffects.f_19598_, 100, 0), (Entity)serverPlayer);
                break;
            }
            case WEAKEN: {
                if (!bl2) break;
                livingEntity.m_147207_(new MobEffectInstance(MobEffects.f_19613_, 100, 0), (Entity)serverPlayer);
                break;
            }
            case KNOCKBACK: {
                if (!bl2) break;
                FoodWeaponEvolution.knockAway(livingEntity, serverPlayer.m_20182_(), 0.85);
                break;
            }
            case CHAIN: {
                if (!bl2) break;
                FoodWeaponEvolution.chainArc(serverPlayer, livingEntity, 5.0f * f);
                break;
            }
            case REGEN: {
                if (!bl2) break;
                serverPlayer.m_147207_(new MobEffectInstance(MobEffects.f_19605_, 80, 0), (Entity)serverPlayer);
            }
        }
        if (bl2) {
            compoundTag.m_128356_(string, l);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void chainArc(ServerPlayer serverPlayer, LivingEntity livingEntity, float f) {
        Level level = livingEntity.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        level = serverLevel.m_6443_(LivingEntity.class, livingEntity.m_20191_().m_82400_(4.0), livingEntity2 -> livingEntity2 != livingEntity && FoodWeaponEvolution.canTarget(serverPlayer, livingEntity2)).stream().min(Comparator.comparingDouble(arg_0 -> ((LivingEntity)livingEntity).m_20280_(arg_0))).orElse(null);
        if (level == null) {
            return;
        }
        EXTRA_DAMAGE.set(true);
        try {
            level.f_19802_ = 0;
            level.m_6469_(serverLevel.m_269111_().m_269425_(), f);
        }
        finally {
            EXTRA_DAMAGE.set(false);
        }
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_175830_, level.m_20185_(), level.m_20186_() + (double)level.m_20206_() * 0.5, level.m_20189_(), 12, 0.22, 0.35, 0.22, 0.12);
    }

    private static int addExperience(ItemStack itemStack, Profile profile, int n) {
        if (n <= 0 || !FoodWeaponEvolution.attune(itemStack, profile) || FoodWeaponEvolution.evolutionLevel(itemStack) >= FoodWeaponEvolution.maxLevel(itemStack)) {
            return 0;
        }
        int n2 = FoodWeaponEvolution.threshold(itemStack, FoodWeaponEvolution.evolutionLevel(itemStack));
        FoodWeaponProgression.normalizeExperience(itemStack);
        CompoundTag compoundTag = FoodWeaponEvolution.writableData(itemStack, profile);
        int n3 = FoodWeaponEvolution.evolutionExperience(itemStack);
        int n4 = (int)Math.min((long)n2, (long)n3 + (long)n);
        compoundTag.m_128405_(XP, n4);
        return n4 - n3;
    }

    public static boolean evolveWeaponFromUi(ServerPlayer serverPlayer, int n) {
        serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.offering_required"), true);
        return false;
    }

    public static int foodQualityTier(ItemStack itemStack) {
        return FoodWeaponEvolution.foodTier((ItemStack)itemStack).level;
    }

    public static int evolutionExperience(ItemStack itemStack) {
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            return FourWeaponFoundationAdapter.read(itemStack).experience();
        }
        CompoundTag compoundTag = FoodWeaponEvolution.data(itemStack);
        return compoundTag == null ? 0 : FoodWeaponProgression.experience(itemStack, compoundTag.m_128451_(XP));
    }

    private static CompoundTag data(ItemStack itemStack) {
        return itemStack.m_41782_() ? itemStack.m_41737_(ROOT) : null;
    }

    private static void addSocketIngredientTooltip(List<Component> list, ItemStack itemStack, FoodType foodType) {
        SocketTier socketTier = FoodWeaponEvolution.foodTier(itemStack);
        list.add((Component)Component.m_237110_((String)"tooltip.ftboss.socket.material", (Object[])new Object[]{foodType.displayName()}).m_130940_(ChatFormatting.GOLD));
        list.add((Component)Component.m_237110_((String)"tooltip.ftboss.socket.tier", (Object[])new Object[]{socketTier.displayName(), FoodWeaponEvolution.formatNumber(socketTier.multiplier)}).m_130940_(socketTier == SocketTier.GOURMET ? ChatFormatting.LIGHT_PURPLE : (socketTier == SocketTier.FINE ? ChatFormatting.AQUA : ChatFormatting.GRAY)));
        list.add((Component)foodType.effectDescription().m_6881_().m_130940_(ChatFormatting.GRAY));
        list.add((Component)Component.m_237110_((String)"tooltip.ftboss.socket.set_preview", (Object[])new Object[]{foodType.setDescription()}).m_130940_(ChatFormatting.DARK_GREEN));
    }

    private static int storedSocketCount(ItemStack itemStack) {
        CompoundTag compoundTag = FoodWeaponEvolution.data(itemStack);
        if (compoundTag == null || !compoundTag.m_128425_(SOCKETS, 9)) {
            return 0;
        }
        int n = FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack) ? 30 : 10;
        return Math.min(n, compoundTag.m_128437_(SOCKETS, 10).size());
    }

    private static Component dailyRewardName(CompoundTag compoundTag) {
        DailyRewardType dailyRewardType = DailyRewardType.byId(compoundTag.m_128461_(DAILY_REWARD_TYPE));
        return dailyRewardType == null ? Component.m_237113_((String)"?") : dailyRewardType.displayName(compoundTag.m_128451_(DAILY_REWARD_AMOUNT));
    }

    private static int socketCapacityInternal(ItemStack itemStack) {
        if (FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack)) {
            return 30;
        }
        int n = FoodWeaponEvolution.unlockedSocketCapacity(itemStack);
        return Math.max(n, FoodWeaponEvolution.storedSocketCount(itemStack));
    }

    private static double evolutionArmorBonus(WeaponRole weaponRole, int n, boolean bl) {
        return weaponRole == WeaponRole.GUARDIAN ? (double)n * (bl ? 0.4 : 1.5) : 0.0;
    }

    private static boolean externalWeaponCandidate(ItemStack itemStack) {
        if (itemStack.m_41619_() || itemStack.m_41613_() != 1 || FoodWeaponEvolution.isEvolutionExcluded(itemStack) || FoodWeaponEvolution.nativeProfile(itemStack) != null) {
            return false;
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        if (resourceLocation == null) {
            return false;
        }
        WeaponRole weaponRole = FoodWeaponEvolution.weaponRole(itemStack);
        if (weaponRole == WeaponRole.RANGED || weaponRole == WeaponRole.ARCANE || weaponRole == WeaponRole.TOOL) {
            return true;
        }
        return itemStack.m_41720_().getAttributeModifiers(EquipmentSlot.MAINHAND, itemStack).get((Object)Attributes.f_22281_).stream().anyMatch(attributeModifier -> attributeModifier.m_22218_() > 0.0);
    }

    public static String repairProfileId(ItemStack itemStack) {
        Profile profile = FoodWeaponEvolution.boundProfile(itemStack);
        return profile == null ? "" : profile.id;
    }

    public static boolean selectGourmetSkillFromUi(ServerPlayer serverPlayer, int n) {
        return FoodWeaponEvolution.selectGourmetSkillFromUi(serverPlayer, FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer), n);
    }

    private static Profile boundProfile(ItemStack itemStack) {
        if (FoodWeaponEvolution.isEvolutionExcluded(itemStack)) {
            return null;
        }
        Profile profile = FoodWeaponEvolution.nativeProfile(itemStack);
        if (profile != null) {
            return profile;
        }
        CompoundTag compoundTag = FoodWeaponEvolution.data(itemStack);
        return compoundTag == null ? null : Profile.byId(compoundTag.m_128461_(PROFILE));
    }

    private static void playEvolutionSuccessParticles(ServerPlayer serverPlayer, boolean bl) {
        ServerLevel serverLevel = serverPlayer.m_284548_();
        int n = bl ? 16 : 12;
        double d = bl ? 0.74 : 0.62;
        double d2 = serverPlayer.m_20186_() + 0.95;
        for (int i = 0; i < n; ++i) {
            double d3 = Math.PI * 2 * (double)i / (double)n;
            double d4 = serverPlayer.m_20185_() + Math.cos(d3) * d;
            double d5 = serverPlayer.m_20189_() + Math.sin(d3) * d;
            double d6 = d2 + 0.12 * Math.sin(d3 * 2.0);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, d4, d6, d5, 1, 0.015, 0.015, 0.015, 0.01);
        }
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123809_, serverPlayer.m_20185_(), serverPlayer.m_20186_() + 1.0, serverPlayer.m_20189_(), bl ? 24 : 16, 0.32, 0.45, 0.32, 0.06);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void overloadBurst(ServerPlayer serverPlayer, LivingEntity livingEntity, float f) {
        Object object = livingEntity.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = serverLevel.m_6443_(LivingEntity.class, livingEntity.m_20191_().m_82400_(3.0), livingEntity2 -> livingEntity2 != livingEntity && FoodWeaponEvolution.canTarget(serverPlayer, livingEntity2));
        EXTRA_DAMAGE.set(true);
        try {
            Iterator iterator = object.iterator();
            while (iterator.hasNext()) {
                LivingEntity livingEntity3 = (LivingEntity)iterator.next();
                livingEntity3.f_19802_ = 0;
                livingEntity3.m_6469_(serverLevel.m_269111_().m_269425_(), f);
            }
        }
        finally {
            EXTRA_DAMAGE.set(false);
        }
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_175830_, livingEntity.m_20185_(), livingEntity.m_20186_() + (double)livingEntity.m_20206_() * 0.5, livingEntity.m_20189_(), 24 + object.size() * 4, 1.1, 0.7, 1.1, 0.16);
    }

    public static double damageEvolutionMultiplier(ItemStack itemStack) {
        WeaponRole weaponRole;
        if (itemStack == null || itemStack.m_41619_() || itemStack.m_41613_() != 1) {
            return 0.0;
        }
        if (FoodWeaponEvolution.isEvolutionExcluded(itemStack)) {
            return 1.0;
        }
        if (FoodWeaponLifecycle.isBroken(itemStack)) {
            return 0.0;
        }
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            FourWeaponFoundationAdapter.View view = FourWeaponFoundationAdapter.read(itemStack);
            if (!view.valid() || !view.persisted() || view.level() < 0 || view.level() > 4 || !view.status().ready() || FourWeaponUseGuard.blocksUse(itemStack) || FoodWeaponMaterialFingerprint.foundationSnapshot(itemStack).m_128456_()) {
                return 0.0;
            }
            return switch (view.profile().family()) {
                case "bow", "crossbow", "trident" -> 1.0 + (double)view.level() * 0.12;
                case "staff" -> 1.0f + (float)view.level() * 0.25f;
                case "axe" -> 1.0f + (float)view.level() * 0.18f;
                default -> 0.0;
            };
        }
        if (!FoodWeaponEvolution.isEvolutionCandidate(itemStack) || itemStack.m_41783_() != null && itemStack.m_41783_().m_128441_(ROOT) && !itemStack.m_41783_().m_128425_(ROOT, 10)) {
            return 0.0;
        }
        CompoundTag compoundTag = FoodWeaponEvolution.data(itemStack);
        if (compoundTag != null) {
            if (compoundTag.m_128441_("Schema") || compoundTag.m_128441_("Version") || compoundTag.m_128441_("ProgressionVersion") && (!compoundTag.m_128425_("ProgressionVersion", 3) || compoundTag.m_128451_("ProgressionVersion") < 0 || compoundTag.m_128451_("ProgressionVersion") > 2) || compoundTag.m_128441_(LEVEL) && (!compoundTag.m_128425_(LEVEL, 3) || compoundTag.m_128451_(LEVEL) < 0 || compoundTag.m_128451_(LEVEL) > FoodWeaponEvolution.maxLevel(itemStack)) || compoundTag.m_128441_(PROFILE) && (!compoundTag.m_128425_(PROFILE, 8) || Profile.byId(compoundTag.m_128461_(PROFILE)) == null) || compoundTag.m_128441_(SOCKETS) && !compoundTag.m_128425_(SOCKETS, 9)) {
                return 0.0;
            }
            Tag tag = compoundTag.m_128423_(SOCKETS);
            if (tag instanceof ListTag && !(weaponRole = (ListTag)tag).isEmpty() && weaponRole.m_7264_() != 10) {
                return 0.0;
            }
            weaponRole = compoundTag.m_128437_(SOCKETS, 10);
            if (weaponRole.size() > FoodWeaponEvolution.unlockedSocketCapacity(itemStack)) {
                return 0.0;
            }
            for (int i = 0; i < weaponRole.size(); ++i) {
                CompoundTag compoundTag2 = weaponRole.m_128728_(i);
                if (!compoundTag2.m_128425_("Type", 8) || !compoundTag2.m_128425_("Item", 8) || ResourceLocation.m_135820_((String)compoundTag2.m_128461_("Item")) == null || compoundTag2.m_128441_("Tier") && (!compoundTag2.m_128425_("Tier", 3) || compoundTag2.m_128451_("Tier") < 1 || compoundTag2.m_128451_("Tier") > 3)) {
                    return 0.0;
                }
                try {
                    FoodType.valueOf(compoundTag2.m_128461_("Type"));
                    continue;
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    return 0.0;
                }
            }
        }
        if ((weaponRole = FoodWeaponEvolution.weaponRole(itemStack)) == WeaponRole.RANGED) {
            SocketStats socketStats = FoodWeaponEvolution.socketStats(itemStack);
            double d = socketStats.effectPower(FoodType.WINE, weaponRole) + socketStats.effectPower(FoodType.FISH, weaponRole) + socketStats.effectPower(FoodType.ENERGY, weaponRole);
            return 1.0 + (double)FoodWeaponEvolution.evolutionLevel(itemStack) * (FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack) ? 0.05 : 0.12) + Math.min(0.6, d * 0.04);
        }
        double d = FoodWeaponEvolution.activeSkillMultiplier(itemStack);
        if (weaponRole == WeaponRole.ARCANE) {
            d /= WeaponMaterialBuild.build(itemStack).damage();
        }
        return FoodWeaponDamageAccounting.positive(d) ? d : 0.0;
    }

    public static String firstProfileId(ItemStack itemStack) {
        Profile profile = FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack) ? FoodWeaponEvolution.activeProfile(itemStack) : FoodWeaponEvolution.boundProfile(itemStack);
        return profile == null ? "" : profile.id;
    }

    public static boolean isSocketCandidate(ItemStack itemStack, ItemStack itemStack2, int n) {
        Profile profile = Profile.byTint(n);
        Profile profile2 = FoodWeaponEvolution.boundProfile(itemStack);
        return profile != null && profile2 != null && !itemStack2.m_41619_() && FoodWeaponEvolution.foodType(itemStack2) != null && FoodWeaponEvolution.sockets(itemStack).size() < FoodWeaponEvolution.unlockedSocketCapacity(itemStack);
    }

    private static int greatestCommonDivisor(int n, int n2) {
        while (n2 != 0) {
            int n3 = n % n2;
            n = n2;
            n2 = n3;
        }
        return Math.abs(n);
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        MinecraftForge.EVENT_BUS.register(FoodWeaponEvolution.class);
    }

    public static boolean isNativeWeapon(ItemStack itemStack) {
        return FoodWeaponEvolution.nativeProfile(itemStack) != null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void applyPlanOverload(FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge, ServerPlayer serverPlayer, LivingEntity livingEntity, List<LivingEntity> list, float f) {
        EXTRA_DAMAGE.set(true);
        try {
            for (LivingEntity livingEntity2 : list) {
                if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
                    return;
                }
                FoodWeaponDamageSources.hurtListedSecondary(serverPlayer, livingEntity2, f, childHurtBridge);
                if (FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) continue;
                return;
            }
        }
        finally {
            EXTRA_DAMAGE.set(false);
        }
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        ((ServerLevel)livingEntity.m_9236_()).m_8767_((ParticleOptions)ParticleTypes.f_175830_, livingEntity.m_20185_(), livingEntity.m_20186_() + (double)livingEntity.m_20206_() * 0.5, livingEntity.m_20189_(), 24 + list.size() * 4, 1.1, 0.7, 1.1, 0.16);
    }

    private static long dailySeed(ServerPlayer serverPlayer, Profile profile, long l) {
        UUID uUID = serverPlayer.m_20148_();
        return FoodWeaponEvolution.mix64(uUID.getMostSignificantBits() ^ Long.rotateLeft(uUID.getLeastSignificantBits(), 23) ^ ((long)profile.ordinal() + 1L) * -7046029254386353131L ^ l * -4658895280553007687L);
    }

    private static void addFoodBlessingAttributes(ItemAttributeModifierEvent itemAttributeModifierEvent, ItemStack itemStack) {
        ListTag listTag = FoodWeaponEvolution.foodBlessingList(itemStack);
        for (int i = 0; i < listTag.size(); ++i) {
            Attribute attribute;
            double d;
            CompoundTag compoundTag = listTag.m_128728_(i);
            FoodBlessing foodBlessing = FoodBlessing.byId(compoundTag.m_128461_("Blessing"));
            if (foodBlessing == null) continue;
            int n = FoodWeaponEvolution.foodBlessingLevel(compoundTag);
            switch (foodBlessing.kind) {
                case ATTACK_DAMAGE: {
                    double d2 = 0.75 * (double)n;
                    break;
                }
                case ATTACK_SPEED: {
                    double d2 = 0.04 * (double)n;
                    break;
                }
                case MAX_HEALTH: {
                    double d2 = 1.5 * (double)n;
                    break;
                }
                case MOVEMENT_SPEED: {
                    double d2 = 0.012 * (double)n;
                    break;
                }
                case KNOCKBACK_RESISTANCE: {
                    double d2 = 0.08 * (double)n;
                    break;
                }
                default: {
                    double d2 = d = 0.0;
                }
            }
            if (d <= 0.0) continue;
            AttributeModifier.Operation operation = foodBlessing.kind == FoodBlessingKind.MOVEMENT_SPEED || foodBlessing.kind == FoodBlessingKind.KNOCKBACK_RESISTANCE ? AttributeModifier.Operation.MULTIPLY_BASE : AttributeModifier.Operation.ADDITION;
            switch (foodBlessing.kind) {
                case ATTACK_DAMAGE: {
                    Attribute attribute2 = Attributes.f_22281_;
                    break;
                }
                case ATTACK_SPEED: {
                    Attribute attribute2 = Attributes.f_22283_;
                    break;
                }
                case MAX_HEALTH: {
                    Attribute attribute2 = Attributes.f_22276_;
                    break;
                }
                case MOVEMENT_SPEED: {
                    Attribute attribute2 = Attributes.f_22279_;
                    break;
                }
                case KNOCKBACK_RESISTANCE: {
                    Attribute attribute2 = Attributes.f_22278_;
                    break;
                }
                default: {
                    Attribute attribute2 = attribute = null;
                }
            }
            if (attribute == null) continue;
            itemAttributeModifierEvent.addModifier(attribute, new AttributeModifier(foodBlessing.modifierId, "ftboss.food_blessing." + foodBlessing.id, d, operation));
        }
    }

    public static String profileId(int n) {
        Profile profile = Profile.byTint(n);
        return profile == null ? "" : profile.id;
    }

    private static void quoteChainArc(FoodWeaponHitPlan.Builder builder, ServerPlayer serverPlayer, LivingEntity livingEntity, float f, FoodWeaponSourceLedger.AttemptDomain attemptDomain, CompoundTag compoundTag, String string, long l) {
        ServerLevel serverLevel;
        Level level;
        block5: {
            block4: {
                level = livingEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break block4;
                serverLevel = (ServerLevel)level;
                if (FoodWeaponDamageSources.attributeSecondary(serverPlayer) != null) break block5;
            }
            return;
        }
        level = serverLevel.m_6443_(LivingEntity.class, livingEntity.m_20191_().m_82400_(4.0), livingEntity2 -> livingEntity2 != livingEntity && FoodWeaponEvolution.canTarget(serverPlayer, livingEntity2)).stream().min(Comparator.comparingDouble(arg_0 -> ((LivingEntity)livingEntity).m_20280_(arg_0))).orElse(null);
        if (level == null) {
            return;
        }
        builder.chain(attemptDomain, serverPlayer, (LivingEntity)level, f, compoundTag, string, l);
    }

    public static boolean isGourmetEvolutionWeapon(ItemStack itemStack) {
        return itemStack.m_41720_() instanceof GourmetEvolutionWeapon;
    }

    private static Profile nativeProfile(ItemStack itemStack) {
        return switch (FoodWeaponEvolution.itemId(itemStack)) {
            case "ftboss:candy_cane" -> Profile.SUGAR;
            case "ftboss:turkey_legs" -> Profile.BUTCHER;
            case "ftboss:blooming_thorns" -> Profile.SAYA;
            case "ftboss:wine_bottle_mace", "ftboss:barrel_crossbow" -> Profile.WINE;
            case "ftboss:cheese_wheel_mace" -> Profile.RAT;
            case "ftboss:defect_claw" -> Profile.DEFECT;
            case "ftboss:compost_crown" -> Profile.COMPOST;
            case "ftboss:apple_stem" -> Profile.APPLE;
            case "ftboss:tide_trident" -> Profile.TIDE;
            case "ftboss:dog_beating_stick" -> Profile.BENGER;
            default -> null;
        };
    }

    private static boolean isNaturallyObtainableDailyFood(ItemStack itemStack, ResourceLocation resourceLocation) {
        if ("minecraft".equals(resourceLocation.m_135827_()) && VANILLA_NATURAL_DAILY_FOODS.contains(resourceLocation.m_135815_())) {
            return true;
        }
        for (TagKey<Item> tagKey : DAILY_NATURAL_FOOD_TAGS) {
            if (!itemStack.m_204117_(tagKey)) continue;
            return true;
        }
        return false;
    }

    private static double evolutionSpeedBonus(WeaponRole weaponRole, int n, boolean bl) {
        double d;
        if (bl) {
            switch (weaponRole) {
                case VANGUARD: {
                    d = 0.01;
                    break;
                }
                case DUELIST: {
                    d = 0.025;
                    break;
                }
                case BALANCED: {
                    d = 0.02;
                    break;
                }
                default: {
                    d = 0.0;
                    break;
                }
            }
        } else {
            switch (weaponRole) {
                case VANGUARD: {
                    d = 0.04;
                    break;
                }
                case DUELIST: {
                    d = 0.14;
                    break;
                }
                case BALANCED: {
                    d = 0.08;
                    break;
                }
                default: {
                    d = 0.0;
                }
            }
        }
        double d2 = d;
        return (double)n * d2;
    }

    public static int setEvolutionExperienceForTesting(ServerPlayer serverPlayer, int n, boolean bl) {
        if (serverPlayer == null || !serverPlayer.m_150110_().f_35937_) {
            if (serverPlayer != null) {
                serverPlayer.m_213846_((Component)Component.m_237115_((String)"command.ftboss.evolution.creative_only").m_130940_(ChatFormatting.RED));
            }
            return 0;
        }
        ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
        Profile profile = FoodWeaponEvolution.boundProfile(itemStack);
        if (itemStack.m_41619_() || profile == null) {
            serverPlayer.m_213846_((Component)Component.m_237115_((String)"command.ftboss.evolution.no_bound_weapon").m_130940_(ChatFormatting.RED));
            return 0;
        }
        CompoundTag compoundTag = FoodWeaponEvolution.writableData(itemStack, profile);
        FoodWeaponProgression.normalizeExperience(itemStack);
        int n2 = FoodWeaponEvolution.evolutionExperience(itemStack);
        int n3 = FoodWeaponEvolution.evolutionLevel(itemStack) >= FoodWeaponEvolution.maxLevel(itemStack) ? 0 : FoodWeaponEvolution.threshold(itemStack, FoodWeaponEvolution.evolutionLevel(itemStack));
        long l = bl ? (long)n2 + (long)n : (long)n;
        int n4 = (int)Math.max(0L, Math.min((long)n3, l));
        compoundTag.m_128405_(XP, n4);
        serverPlayer.m_150109_().m_6596_();
        serverPlayer.m_213846_((Component)Component.m_237110_((String)"command.ftboss.evolution.changed", (Object[])new Object[]{itemStack.m_41786_(), n2, n4, n3}).m_130940_(ChatFormatting.AQUA));
        return 1;
    }

    private static Component dailyRequestName(CompoundTag compoundTag, Profile profile) {
        ResourceLocation resourceLocation = FoodWeaponEvolution.dailyRequestId(compoundTag);
        Item item = resourceLocation == null ? null : (Item)ForgeRegistries.ITEMS.getValue(resourceLocation);
        return item == null ? profile.food.displayName() : new ItemStack((ItemLike)item).m_41786_();
    }

    private static void submitDailyFood(ServerPlayer serverPlayer, Profile profile, ItemStack itemStack, FoodType foodType) {
        int n;
        CompoundTag compoundTag = FoodWeaponEvolution.daily(serverPlayer, profile);
        CompoundTag compoundTag2 = null;
        int n2 = -1;
        boolean bl = false;
        for (n = 0; n < 12; ++n) {
            CompoundTag compoundTag3 = compoundTag.m_128469_(FoodWeaponEvolution.taskKey(n));
            if (!FoodWeaponEvolution.matchesDailyRequest(compoundTag3, profile, itemStack, foodType)) continue;
            if (!compoundTag3.m_128471_("Claimed") && !compoundTag3.m_128471_("Ready")) {
                compoundTag2 = compoundTag3;
                n2 = n;
                break;
            }
            bl = true;
        }
        if (compoundTag2 == null) {
            if (bl) {
                serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.evolution.daily_item_done", (Object[])new Object[]{itemStack.m_41786_()}), true);
            } else {
                serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.evolution.wrong_food_multi", (Object[])new Object[]{FoodWeaponEvolution.unfinishedDailyFoods(compoundTag, profile)}), true);
            }
            return;
        }
        n = compoundTag2.m_128451_(DAILY_REQUIRED);
        int n3 = Math.max(0, compoundTag2.m_128451_("Progress"));
        int n4 = Math.min(itemStack.m_41613_(), n - n3);
        if (n4 <= 0) {
            return;
        }
        if (!serverPlayer.m_150110_().f_35937_) {
            itemStack.m_41774_(n4);
        }
        int n5 = n3 + n4;
        compoundTag2.m_128405_("Progress", n5);
        compoundTag.m_128365_(FoodWeaponEvolution.taskKey(n2), (Tag)compoundTag2);
        if (n5 >= n) {
            DailyRewardType dailyRewardType = DailyRewardType.byId(compoundTag2.m_128461_(DAILY_REWARD_TYPE));
            if (dailyRewardType == DailyRewardType.EVOLUTION) {
                compoundTag2.m_128379_("Ready", true);
                serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.daily_evolution_ready", (Object[])new Object[]{n2 + 1, FoodWeaponEvolution.dailyRewardName(compoundTag2)}).m_130940_(ChatFormatting.GOLD));
            } else {
                FoodWeaponEvolution.grantImmediateDailyReward(serverPlayer, dailyRewardType, compoundTag2.m_128451_(DAILY_REWARD_AMOUNT));
                compoundTag2.m_128379_("Claimed", true);
                serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.daily_rewarded", (Object[])new Object[]{n2 + 1, FoodWeaponEvolution.dailyRewardName(compoundTag2)}).m_130940_(ChatFormatting.GOLD));
            }
            serverPlayer.m_284548_().m_5594_(null, serverPlayer.m_20183_(), SoundEvents.f_12275_, SoundSource.PLAYERS, 0.65f, 1.35f);
        } else {
            serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.evolution.daily_progress", (Object[])new Object[]{FoodWeaponEvolution.dailyRequestName(compoundTag2, profile), n5, n}), true);
        }
        FoodWeaponEvolution.saveDaily(serverPlayer, profile, compoundTag);
    }

    private FoodWeaponEvolution() {
    }

    private static int positiveMod(long l, int n) {
        return (int)Math.floorMod(l, (long)n);
    }

    @SubscribeEvent
    public static void gourmetBladePickup(EntityItemPickupEvent entityItemPickupEvent) {
        GourmetEvolutionWeapon.playPickupEffect(entityItemPickupEvent);
    }

    static String planHitTimerProfile(Profile profile) {
        return profile == null ? null : profile.id;
    }

    private static SocketStats socketStats(ItemStack itemStack) {
        return new SocketStats(FoodWeaponEvolution.sockets(itemStack));
    }

    public static int socketCount(ItemStack itemStack) {
        return FoodWeaponEvolution.socketStats(itemStack).size();
    }

    private static TagKey<Item> dailyFoodTag(String string) {
        ResourceLocation resourceLocation = ResourceLocation.m_135820_((String)string);
        if (resourceLocation == null) {
            throw new IllegalArgumentException("Invalid daily food tag: " + string);
        }
        return TagKey.m_203882_((ResourceKey)Registries.f_256913_, (ResourceLocation)resourceLocation);
    }

    private static long currentDailyDay(ServerPlayer serverPlayer) {
        long l = serverPlayer.m_284548_().m_7654_().m_129783_().m_46468_();
        return Math.floorDiv(l, 24000L);
    }

    private static double gourmetStat(ItemStack itemStack, EvolutionStat evolutionStat) {
        CompoundTag compoundTag = FoodWeaponEvolution.data(itemStack);
        if (compoundTag == null || !compoundTag.m_128425_(GOURMET_STATS, 10)) {
            return 0.0;
        }
        return Math.max(0.0, compoundTag.m_128469_(GOURMET_STATS).m_128459_(evolutionStat.id));
    }

    public static String secondProfileId(ItemStack itemStack) {
        if (FoodWeaponEvolution.isEvolutionExcluded(itemStack)) {
            return "";
        }
        CompoundTag compoundTag = FoodWeaponEvolution.data(itemStack);
        return FoodWeaponProgression.secondProfile(itemStack, compoundTag == null ? "" : compoundTag.m_128461_(SECOND_PROFILE));
    }

    public static boolean insertSocketFromUi(ServerPlayer serverPlayer, int n, int n2) {
        Profile profile = Profile.byTint(n);
        ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
        if (profile == null || itemStack.m_41619_() || n2 < 0 || n2 > 40 || n2 >= 36 && n2 < 40) {
            return false;
        }
        ItemStack itemStack2 = serverPlayer.m_150109_().m_8020_(n2);
        FoodType foodType = FoodWeaponEvolution.foodType(itemStack2);
        if (itemStack2.m_41619_() || itemStack2 == itemStack || foodType == null) {
            return false;
        }
        int n3 = FoodWeaponEvolution.socketCount(itemStack);
        FoodWeaponEvolution.insertSocket(serverPlayer, profile, itemStack, itemStack2, foodType);
        return FoodWeaponEvolution.socketCount(itemStack) > n3;
    }

    public static boolean matchesStatueFood(ItemStack itemStack, int n) {
        Profile profile = Profile.byTint(n);
        return profile != null && FoodWeaponEvolution.foodType(itemStack) == profile.food;
    }

    private static void addFoodBlessingTooltip(List<Component> list, ItemStack itemStack) {
        ListTag listTag = FoodWeaponEvolution.foodBlessingList(itemStack);
        if (listTag.isEmpty()) {
            return;
        }
        list.add((Component)Component.m_237115_((String)"tooltip.ftboss.food_blessings.header").m_130940_(ChatFormatting.LIGHT_PURPLE));
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.m_128728_(i);
            FoodBlessing foodBlessing = FoodBlessing.byId(compoundTag.m_128461_("Blessing"));
            if (foodBlessing == null) continue;
            int n = FoodWeaponEvolution.foodBlessingLevel(compoundTag);
            list.add((Component)Component.m_237113_((String)"  ").m_7220_(foodBlessing.nameComponent()).m_7220_((Component)Component.m_237113_((String)(" " + FoodWeaponEvolution.toRoman(n)))).m_130940_(ChatFormatting.GOLD));
            list.add((Component)Component.m_237113_((String)"    ").m_7220_(foodBlessing.descriptionComponent(n)).m_130940_(ChatFormatting.GRAY));
        }
        list.add((Component)Component.m_237115_((String)"tooltip.ftboss.food_blessings.grindstone").m_130940_(ChatFormatting.DARK_GRAY));
    }

    public static List<StatueDailyView> statueDailyViews(ServerPlayer serverPlayer, int n) {
        Profile profile = Profile.byTint(n);
        if (profile == null) {
            return List.of();
        }
        CompoundTag compoundTag = FoodWeaponEvolution.daily(serverPlayer, profile);
        ArrayList<StatueDailyView> arrayList = new ArrayList<StatueDailyView>(12);
        for (int i = 0; i < 12; ++i) {
            CompoundTag compoundTag2 = compoundTag.m_128469_(FoodWeaponEvolution.taskKey(i));
            int n2 = Math.max(1, compoundTag2.m_128451_(DAILY_REQUIRED));
            int n3 = Math.min(n2, Math.max(0, compoundTag2.m_128451_("Progress")));
            int n4 = compoundTag2.m_128471_("Claimed") ? 2 : (compoundTag2.m_128471_("Ready") ? 1 : 0);
            ResourceLocation resourceLocation = FoodWeaponEvolution.dailyRequestId(compoundTag2);
            arrayList.add(new StatueDailyView(FoodWeaponEvolution.dailyRequestName(compoundTag2, profile), resourceLocation == null ? "" : resourceLocation.toString(), n3, n2, FoodWeaponEvolution.dailyRewardName(compoundTag2), n4));
        }
        return List.copyOf(arrayList);
    }

    private static void handleWeapon(ServerPlayer serverPlayer, Profile profile, ItemStack itemStack, boolean bl) {
        NativeFoodWeaponWear.manage(serverPlayer, itemStack);
        Profile profile2 = FoodWeaponEvolution.boundProfile(itemStack);
        if (profile2 == null) {
            if (!FoodWeaponEvolution.attune(itemStack, profile)) {
                serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.not_weapon"), true);
                return;
            }
            serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.attuned", (Object[])new Object[]{itemStack.m_41786_(), profile.displayName()}).m_130940_(ChatFormatting.AQUA));
        }
        CompoundTag compoundTag = FoodWeaponEvolution.daily(serverPlayer, profile);
        CompoundTag compoundTag2 = FoodWeaponEvolution.readyEvolutionTask(compoundTag);
        if (!bl && compoundTag2 != null) {
            int n = FoodWeaponEvolution.addExperience(itemStack, profile, compoundTag2.m_128451_(DAILY_REWARD_AMOUNT));
            if (n <= 0) {
                serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.maxed"), true);
                return;
            }
            compoundTag2.m_128379_("Claimed", true);
            FoodWeaponEvolution.saveDaily(serverPlayer, profile, compoundTag);
            serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.claimed", (Object[])new Object[]{itemStack.m_41786_(), n, FoodWeaponEvolution.writableData(itemStack, profile).m_128451_(XP)}).m_130940_(ChatFormatting.GREEN));
            return;
        }
        if (bl) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.offering_required"), true);
        } else {
            FoodWeaponEvolution.showWeapon(serverPlayer, itemStack, profile);
        }
    }

    private static double evolutionDamageBonus(WeaponRole weaponRole, int n, boolean bl) {
        double d;
        if (bl) {
            switch (weaponRole) {
                default: {
                    throw new IncompatibleClassChangeError();
                }
                case VANGUARD: {
                    d = 0.75;
                    break;
                }
                case DUELIST: {
                    d = 0.3;
                    break;
                }
                case GUARDIAN: {
                    d = 0.25;
                    break;
                }
                case BALANCED: {
                    d = 0.5;
                    break;
                }
                case RANGED: 
                case ARCANE: 
                case TOOL: {
                    d = 0.0;
                    break;
                }
            }
        } else {
            switch (weaponRole) {
                default: {
                    throw new IncompatibleClassChangeError();
                }
                case VANGUARD: {
                    d = 3.0;
                    break;
                }
                case DUELIST: {
                    d = 1.25;
                    break;
                }
                case GUARDIAN: {
                    d = 1.0;
                    break;
                }
                case BALANCED: {
                    d = 2.0;
                    break;
                }
                case RANGED: 
                case ARCANE: 
                case TOOL: {
                    d = 0.0;
                }
            }
        }
        double d2 = d;
        return (double)n * d2;
    }

    private static int foodBlessingLevel(CompoundTag compoundTag) {
        return Math.max(1, Math.min(3, compoundTag.m_128451_(LEVEL)));
    }

    private static double evolutionLuckBonus(WeaponRole weaponRole, int n, boolean bl) {
        return weaponRole == WeaponRole.ARCANE ? (double)n * (bl ? 0.15 : 0.5) : 0.0;
    }

    private static int washBlessings(ItemStack itemStack) {
        int n;
        FoodWeaponLifecycle.ensureTracked(itemStack);
        CompoundTag compoundTag = FoodWeaponEvolution.data(itemStack);
        if (compoundTag == null) {
            return 0;
        }
        FoodWeaponRepair.invalidateForProgression(itemStack);
        int n2 = n = compoundTag.m_128425_(SKILL, 8) ? 1 : 0;
        if (compoundTag.m_128425_(GOURMET_SKILLS, 9)) {
            n += compoundTag.m_128437_(GOURMET_SKILLS, 10).size();
        }
        if (compoundTag.m_128425_(FOOD_BLESSINGS, 9)) {
            n += compoundTag.m_128437_(FOOD_BLESSINGS, 10).size();
        }
        if (compoundTag.m_128425_(GOURMET_STATS, 10)) {
            n += compoundTag.m_128469_(GOURMET_STATS).m_128431_().size();
        }
        if (compoundTag.m_128451_(XP) > 0) {
            ++n;
        }
        if (compoundTag.m_128451_(LEVEL) > 0) {
            ++n;
        }
        compoundTag.m_128405_(XP, 0);
        compoundTag.m_128405_(LEVEL, 0);
        compoundTag.m_128473_(SECOND_PROFILE);
        compoundTag.m_128473_(GOURMET_ACTIVE_PROFILE);
        compoundTag.m_128473_(GOURMET_ACTIVE_SKILL);
        compoundTag.m_128473_(SKILL);
        compoundTag.m_128473_(GOURMET_SKILLS);
        compoundTag.m_128473_(GOURMET_STATS);
        compoundTag.m_128473_(FOOD_BLESSINGS);
        for (String string : new ArrayList(compoundTag.m_128431_())) {
            if (!string.startsWith(LAST_SKILL_PROC) && !string.startsWith(LAST_FOOD_BLESSING)) continue;
            compoundTag.m_128473_(string);
        }
        compoundTag.m_128379_(BLESSING_WASHED, true);
        FoodWeaponProgression.clear(itemStack);
        return n;
    }

    public static boolean isDailySubmissionCandidate(List<StatueDailyView> list, ItemStack itemStack, int n) {
        FoodType foodType = FoodWeaponEvolution.foodType(itemStack);
        Profile profile = Profile.byTint(n);
        if (itemStack.m_41619_() || foodType == null || profile == null) {
            return false;
        }
        for (StatueDailyView statueDailyView : list) {
            if (statueDailyView.state() != 0 || !(statueDailyView.requestItemId().isBlank() ? foodType == profile.food : statueDailyView.requestItemId().equals(FoodWeaponEvolution.itemId(itemStack)))) continue;
            return true;
        }
        return false;
    }

    private static boolean matchesDailyRequest(CompoundTag compoundTag, Profile profile, ItemStack itemStack, FoodType foodType) {
        ResourceLocation resourceLocation = FoodWeaponEvolution.dailyRequestId(compoundTag);
        return resourceLocation == null ? foodType == profile.food : resourceLocation.toString().equals(FoodWeaponEvolution.itemId(itemStack));
    }

    private static void saveDaily(ServerPlayer serverPlayer, Profile profile, CompoundTag compoundTag) {
        CompoundTag compoundTag2 = serverPlayer.getPersistentData().m_128469_(DAILY_ROOT);
        compoundTag2.m_128365_(profile.id, (Tag)compoundTag);
        serverPlayer.getPersistentData().m_128365_(DAILY_ROOT, (Tag)compoundTag2);
    }

    private static List<FoodBlessing> blessingPool(Profile profile) {
        return switch (profile) {
            default -> throw new IncompatibleClassChangeError();
            case Profile.SUGAR -> List.of(FoodBlessing.CANDIED_EDGE, FoodBlessing.SYRUP_STEP, FoodBlessing.FROSTED_SHELL, FoodBlessing.GOLDEN_FORTUNE, FoodBlessing.ROOTED_FOCUS, FoodBlessing.CORK_RUSH);
            case Profile.BUTCHER -> List.of(FoodBlessing.CLEAVER_MIGHT, FoodBlessing.BUTCHER_HIDE, FoodBlessing.BLOOD_FEAST, FoodBlessing.SHEPHERD_MIGHT, FoodBlessing.PLAGUE_TOOTH, FoodBlessing.BARREL_PLATE);
            case Profile.SAYA -> List.of(FoodBlessing.TEA_THORNS, FoodBlessing.CAMELLIA_GRACE, FoodBlessing.ROOTED_FOCUS, FoodBlessing.FERTILE_HASTE, FoodBlessing.PRISMATIC_VENOM, FoodBlessing.HONEYCRISP_HEART);
            case Profile.WINE -> List.of(FoodBlessing.VINTAGE_CRIT, FoodBlessing.BARREL_PLATE, FoodBlessing.CORK_RUSH, FoodBlessing.OVERLOAD_DAMAGE, FoodBlessing.SCAVENGER_LUCK, FoodBlessing.HUNTING_STEP);
            case Profile.RAT -> List.of(FoodBlessing.CHEESE_FRENZY, FoodBlessing.PLAGUE_TOOTH, FoodBlessing.SCAVENGER_LUCK, FoodBlessing.BLOOD_FEAST, FoodBlessing.MAGNETIC_PLATE, FoodBlessing.ORCHARD_EDGE);
            case Profile.DEFECT -> List.of(FoodBlessing.OVERLOAD_DAMAGE, FoodBlessing.VOLT_STEP, FoodBlessing.MAGNETIC_PLATE, FoodBlessing.VINTAGE_CRIT, FoodBlessing.CHEESE_FRENZY, FoodBlessing.ROOTED_FOCUS);
            case Profile.COMPOST -> List.of(FoodBlessing.PRISMATIC_VENOM, FoodBlessing.RECYCLE_HEART, FoodBlessing.FERTILE_HASTE, FoodBlessing.CAMELLIA_GRACE, FoodBlessing.GOLDEN_FORTUNE, FoodBlessing.PLAGUE_TOOTH);
            case Profile.APPLE -> List.of(FoodBlessing.HONEYCRISP_HEART, FoodBlessing.GOLDEN_FORTUNE, FoodBlessing.ORCHARD_EDGE, FoodBlessing.FERTILE_HASTE, FoodBlessing.FROSTED_SHELL, FoodBlessing.CLEAVER_MIGHT);
            case Profile.TIDE -> List.of(FoodBlessing.UNDERTOW_STEP, FoodBlessing.BRINE_SLOW, FoodBlessing.TIDAL_BULWARK, FoodBlessing.ROOTED_FOCUS, FoodBlessing.CORK_RUSH, FoodBlessing.PACK_FEAST);
            case Profile.BENGER -> List.of(FoodBlessing.SHEPHERD_MIGHT, FoodBlessing.HUNTING_STEP, FoodBlessing.PACK_FEAST, FoodBlessing.BLOOD_FEAST, FoodBlessing.ORCHARD_EDGE, FoodBlessing.TIDAL_BULWARK);
        };
    }

    private static boolean attune(ItemStack itemStack, Profile profile) {
        Profile profile2 = FoodWeaponEvolution.boundProfile(itemStack);
        if (profile2 != null) {
            return true;
        }
        if (!FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack) && !FoodWeaponEvolution.externalWeaponCandidate(itemStack)) {
            return false;
        }
        CompoundTag compoundTag = FoodWeaponEvolution.writableData(itemStack, profile);
        compoundTag.m_128405_(XP, 0);
        compoundTag.m_128405_(LEVEL, 0);
        NativeFoodWeaponWear.manageBound(itemStack);
        return true;
    }

    private static String itemId(ItemStack itemStack) {
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        return resourceLocation == null ? "" : resourceLocation.toString();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean hurtPlanSecondary(ServerPlayer serverPlayer, LivingEntity livingEntity, float f, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        EXTRA_DAMAGE.set(true);
        try {
            boolean bl = FoodWeaponDamageSources.hurtListedSecondary(serverPlayer, livingEntity, f, childHurtBridge);
            return bl;
        }
        finally {
            EXTRA_DAMAGE.set(false);
        }
    }

    public static List<FoodBlessingView> foodBlessingViews(ItemStack itemStack) {
        ArrayList<FoodBlessingView> arrayList = new ArrayList<FoodBlessingView>();
        ArrayList<String> arrayList2 = new ArrayList<String>();
        ListTag listTag = FoodWeaponEvolution.foodBlessingList(itemStack);
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.m_128728_(i);
            FoodBlessing foodBlessing = FoodBlessing.byId(compoundTag.m_128461_("Blessing"));
            if (foodBlessing == null || arrayList2.contains(foodBlessing.id)) continue;
            arrayList2.add(foodBlessing.id);
            int n = FoodWeaponEvolution.foodBlessingLevel(compoundTag);
            arrayList.add(new FoodBlessingView(foodBlessing.nameComponent(), foodBlessing.descriptionComponent(n), FoodWeaponEvolution.toRoman(n)));
        }
        return List.copyOf(arrayList);
    }

    private static int unlockedSocketCapacity(ItemStack itemStack) {
        return FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack) ? 30 : Math.min(10, 2 + 2 * FoodWeaponEvolution.evolutionLevel(itemStack));
    }

    private static SocketTier foodTierById(String string) {
        ResourceLocation resourceLocation = ResourceLocation.m_135820_((String)string);
        Item item = resourceLocation == null ? null : (Item)ForgeRegistries.ITEMS.getValue(resourceLocation);
        return item == null || item == Items.f_41852_ ? SocketTier.BASIC : FoodWeaponEvolution.foodTier(new ItemStack((ItemLike)item));
    }

    private static int dailyPoolIndex(ServerPlayer serverPlayer, Profile profile, long l, int n) {
        if (n <= 1) {
            return 0;
        }
        UUID uUID = serverPlayer.m_20148_();
        long l2 = FoodWeaponEvolution.mix64(uUID.getMostSignificantBits() ^ Long.rotateLeft(uUID.getLeastSignificantBits(), 23) ^ ((long)profile.ordinal() + 1L) * -7046029254386353131L);
        int n2 = FoodWeaponEvolution.positiveMod(l2, n);
        int n3 = 1 + FoodWeaponEvolution.positiveMod(FoodWeaponEvolution.mix64(l2 ^ 0xD1B54A32D192ED03L), n - 1);
        while (FoodWeaponEvolution.greatestCommonDivisor(n3, n) != 1) {
            n3 = n3 == n - 1 ? 1 : n3 + 1;
        }
        return FoodWeaponEvolution.positiveMod((long)n2 + l * (long)n3, n);
    }

    private static double evolutionHealthBonus(WeaponRole weaponRole, int n, boolean bl) {
        return weaponRole == WeaponRole.GUARDIAN ? (double)n * (bl ? 0.5 : 2.0) : 0.0;
    }

    @SubscribeEvent
    public static void heldEffects(TickEvent.PlayerTickEvent playerTickEvent) {
        Profile profile;
        ServerPlayer serverPlayer;
        Player player;
        block13: {
            block12: {
                if (playerTickEvent.phase != TickEvent.Phase.END || !((player = playerTickEvent.player) instanceof ServerPlayer)) break block12;
                serverPlayer = (ServerPlayer)player;
                if (serverPlayer.f_19797_ % 40 == 0) break block13;
            }
            return;
        }
        player = serverPlayer.m_21205_();
        if (FoodWeaponEvolution.boundProfile((ItemStack)player) == null || FoodWeaponLifecycle.isBroken((ItemStack)player)) {
            return;
        }
        SocketStats socketStats = FoodWeaponEvolution.socketStats((ItemStack)player);
        if (socketStats.count(FoodType.FISH) >= 3 && serverPlayer.m_20071_()) {
            int n = socketStats.setTier(FoodType.FISH) - 1;
            serverPlayer.m_7292_(new MobEffectInstance(MobEffects.f_19608_, 80, n, false, false, true));
            serverPlayer.m_7292_(new MobEffectInstance(MobEffects.f_19593_, 60, n, false, false, true));
        }
        if (socketStats.count(FoodType.TEA) >= 3) {
            serverPlayer.m_7292_(new MobEffectInstance(MobEffects.f_19598_, 60, socketStats.setTier(FoodType.TEA) - 1, false, false, true));
        }
        if ((profile = FoodWeaponEvolution.activeProfile((ItemStack)player)) != null && socketStats.count(profile.food) >= 3) {
            int n = socketStats.setTier(profile.food) - 1;
            switch (profile.food) {
                case MEAT: {
                    serverPlayer.m_7292_(new MobEffectInstance(MobEffects.f_19600_, 60, n, false, false, true));
                    break;
                }
                case APPLE: {
                    serverPlayer.m_7292_(new MobEffectInstance(MobEffects.f_19605_, 60, n, false, false, true));
                    break;
                }
                case VEGETABLE: {
                    serverPlayer.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 60, n, false, false, true));
                    break;
                }
            }
        }
    }

    static void writePlanHitTimer(ItemStack itemStack, Profile profile, String string, Tag tag, FoodWeaponSourceLedger.OwnerWriterStep ownerWriterStep, FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit, int n, FoodWeaponSourceLedger.OwnerWriterPublication ownerWriterPublication, Object object) {
        FoodWeaponOriginalSequence.Session session = FoodWeaponHitPlan.originalSequence(finalEffectsCommit);
        FoodWeaponOriginalSequence.Step step = FoodWeaponHitPlan.beginOriginalTimer(finalEffectsCommit, object, string, FoodWeaponEvolution.planHitTimerProfile(profile));
        try {
            CompoundTag compoundTag = FoodWeaponEvolution.writableData(itemStack, profile);
            if (session != null) {
                try {
                    session.originalCopyBegins(step, compoundTag, tag);
                }
                catch (Throwable throwable) {
                    session.observationFailed(step, 0, throwable);
                }
            }
            Tag tag2 = tag.m_6426_();
            if (session != null) {
                try {
                    session.originalCopyReturned(step, compoundTag, tag, tag2);
                }
                catch (Throwable throwable) {
                    session.observationFailed(step, 0, throwable);
                }
                try {
                    session.entryReturned(step, compoundTag, tag, tag2);
                }
                catch (Throwable throwable) {
                    session.observationFailed(step, 0, throwable);
                }
            }
            Tag tag3 = compoundTag.m_128365_(string, tag2);
            FoodWeaponHitPlan.timerOriginalPutReturned(finalEffectsCommit);
            if (session != null) {
                try {
                    session.originalReturned(step, tag3);
                }
                catch (Throwable throwable) {
                    session.observationFailed(step, 1, throwable);
                }
            }
        }
        catch (Throwable throwable) {
            if (session != null) {
                session.originalThrown(step, throwable);
            }
            FoodWeaponHitPlan.retainOriginalTimerThrowable(finalEffectsCommit, throwable);
            throw throwable;
        }
        finally {
            if (session != null) {
                try {
                    session.close(step);
                }
                catch (Throwable throwable) {
                    session.observationFailed(step, 3, throwable);
                }
            }
        }
    }

    private static Component unfinishedDailyFoods(CompoundTag compoundTag, Profile profile) {
        MutableComponent mutableComponent = Component.m_237119_();
        boolean bl = true;
        for (int i = 0; i < 12; ++i) {
            CompoundTag compoundTag2 = compoundTag.m_128469_(FoodWeaponEvolution.taskKey(i));
            if (compoundTag2.m_128471_("Claimed") || compoundTag2.m_128471_("Ready")) continue;
            if (!bl) {
                mutableComponent.m_7220_((Component)Component.m_237113_((String)" / "));
            }
            mutableComponent.m_7220_(FoodWeaponEvolution.dailyRequestName(compoundTag2, profile));
            bl = false;
        }
        return bl ? Component.m_237115_((String)"message.ftboss.evolution.daily_all_done") : mutableComponent;
    }

    public static List<SocketView> socketViews(ItemStack itemStack) {
        ArrayList<SocketView> arrayList = new ArrayList<SocketView>();
        Profile profile = FoodWeaponEvolution.activeProfile(itemStack);
        for (Socket socket : FoodWeaponEvolution.sockets(itemStack)) {
            ItemStack itemStack2 = FoodWeaponEvolution.socketStack(socket);
            int n = FoodWeaponEvolution.socketColor(socket.type);
            arrayList.add(new SocketView(itemStack2, socket.type.displayName(), socket.tier.displayName(), socket.type.effectDescription(), socket.type.setDescription(), n, profile != null && socket.type == profile.food));
        }
        return List.copyOf(arrayList);
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone clone) {
        if (clone.getOriginal().getPersistentData().m_128425_(DAILY_ROOT, 10)) {
            clone.getEntity().getPersistentData().m_128365_(DAILY_ROOT, (Tag)clone.getOriginal().getPersistentData().m_128469_(DAILY_ROOT).m_6426_());
        }
    }

    private static void insertSocket(ServerPlayer serverPlayer, Profile profile, ItemStack itemStack, ItemStack itemStack2, FoodType foodType) {
        Profile profile2 = FoodWeaponEvolution.boundProfile(itemStack);
        if (profile2 == null) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.socket_attune_first"), true);
            return;
        }
        List<Socket> list = FoodWeaponEvolution.sockets(itemStack);
        int n = FoodWeaponEvolution.unlockedSocketCapacity(itemStack);
        if (list.size() >= n) {
            serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.evolution.socket_full", (Object[])new Object[]{n}), true);
            return;
        }
        CompoundTag compoundTag = FoodWeaponEvolution.writableData(itemStack, profile2);
        ListTag listTag = compoundTag.m_128437_(SOCKETS, 10);
        MutableComponent mutableComponent = itemStack2.m_41786_().m_6881_();
        SocketTier socketTier = FoodWeaponEvolution.foodTier(itemStack2);
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.m_128359_("Type", foodType.name());
        compoundTag2.m_128359_("Item", FoodWeaponEvolution.itemId(itemStack2));
        compoundTag2.m_128405_("Tier", socketTier.level);
        ItemStack itemStack3 = itemStack2.m_41777_();
        itemStack3.m_41764_(1);
        compoundTag2.m_128365_("Stack", (Tag)itemStack3.m_41739_(new CompoundTag()));
        listTag.add((Object)compoundTag2);
        compoundTag.m_128365_(SOCKETS, (Tag)listTag);
        if (FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack)) {
            GourmetEvolutionWeapon.animateSocket(serverPlayer, itemStack);
            serverPlayer.m_284548_().m_8767_((ParticleOptions)ParticleTypes.f_123810_, serverPlayer.m_20185_(), serverPlayer.m_20186_() + 1.0, serverPlayer.m_20189_(), 12, 0.35, 0.45, 0.35, 0.06);
        }
        if (!serverPlayer.m_150110_().f_35937_) {
            itemStack2.m_41774_(1);
        }
        serverPlayer.m_284548_().m_5594_(null, serverPlayer.m_20183_(), SoundEvents.f_11671_, SoundSource.PLAYERS, 0.45f, 1.55f);
        serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.socketed", (Object[])new Object[]{mutableComponent, foodType.displayName(), socketTier.displayName(), listTag.size(), n}).m_130940_(foodType == profile2.food ? ChatFormatting.GOLD : ChatFormatting.GREEN));
        serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.socket_effect", (Object[])new Object[]{foodType.displayName(), socketTier.displayName(), FoodWeaponEvolution.formatNumber(socketTier.multiplier * 1.5 * FoodWeaponEvolution.weaponRole(itemStack).socketMultiplier(foodType)), foodType.effectDescription()}).m_130940_(ChatFormatting.GRAY));
        SocketStats socketStats = FoodWeaponEvolution.socketStats(itemStack);
        int n2 = socketStats.setCount(foodType);
        serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.socket_set_progress", (Object[])new Object[]{Math.min(3, n2), SocketTier.byLevel(socketStats.setTier(foodType)).displayName(), foodType.setDescription()}).m_130940_(n2 >= 3 ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
    }

    private static CompoundTag daily(ServerPlayer serverPlayer, Profile profile) {
        CompoundTag compoundTag;
        CompoundTag compoundTag2 = serverPlayer.getPersistentData().m_128469_(DAILY_ROOT);
        long l = FoodWeaponEvolution.currentDailyDay(serverPlayer);
        if (!compoundTag2.m_128425_(DAILY_DAY, 4) || compoundTag2.m_128454_(DAILY_DAY) != l) {
            boolean bl = !compoundTag2.m_128456_();
            compoundTag2 = new CompoundTag();
            compoundTag2.m_128356_(DAILY_DAY, l);
            serverPlayer.getPersistentData().m_128365_(DAILY_ROOT, (Tag)compoundTag2);
            if (bl) {
                serverPlayer.m_213846_((Component)Component.m_237115_((String)"message.ftboss.evolution.daily_refreshed").m_130940_(ChatFormatting.AQUA));
            }
        }
        if (FoodWeaponEvolution.ensureDailyTasks(serverPlayer, profile, l, compoundTag = compoundTag2.m_128469_(profile.id))) {
            compoundTag2.m_128365_(profile.id, (Tag)compoundTag);
            serverPlayer.getPersistentData().m_128365_(DAILY_ROOT, (Tag)compoundTag2);
        }
        return compoundTag;
    }

    public static int nativeSkillRank(ItemStack itemStack) {
        return FoodWeaponEvolution.nativeProfile(itemStack) == null ? 0 : FoodWeaponEvolution.evolutionLevel(itemStack) + 1;
    }

    public static boolean removeSocketFromUi(ServerPlayer serverPlayer2, int n, int n2) {
        ItemStack itemStack;
        Item item;
        Object object;
        Profile profile = Profile.byTint(n);
        ItemStack itemStack2 = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer2);
        Profile profile2 = FoodWeaponEvolution.boundProfile(itemStack2);
        if (profile == null || itemStack2.m_41619_() || profile2 == null) {
            return false;
        }
        CompoundTag compoundTag = FoodWeaponEvolution.writableData(itemStack2, profile2);
        ListTag listTag = compoundTag.m_128437_(SOCKETS, 10);
        if (n2 < 0 || n2 >= listTag.size()) {
            return false;
        }
        ItemStack itemStack3 = ItemStack.f_41583_;
        for (int i = 0; i <= 40; ++i) {
            if (i >= 36 && i < 40 || !(object = serverPlayer2.m_150109_().m_8020_(i)).m_150930_(Items.f_42574_)) continue;
            itemStack3 = object;
            break;
        }
        if (itemStack3.m_41619_() && !serverPlayer2.m_150110_().f_35937_) {
            serverPlayer2.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.socket_need_shears"), true);
            return false;
        }
        CompoundTag compoundTag2 = listTag.m_128728_(n2);
        listTag.remove(n2);
        compoundTag.m_128365_(SOCKETS, (Tag)listTag);
        object = compoundTag2.m_128461_("Item");
        ResourceLocation resourceLocation = ResourceLocation.m_135820_((String)object);
        Item item2 = item = resourceLocation == null ? null : (Item)ForgeRegistries.ITEMS.getValue(resourceLocation);
        ItemStack itemStack4 = compoundTag2.m_128425_("Stack", 10) ? ItemStack.m_41712_((CompoundTag)compoundTag2.m_128469_("Stack")) : (itemStack = item == null || item == Items.f_41852_ ? ItemStack.f_41583_ : new ItemStack((ItemLike)item));
        if (!itemStack.m_41619_() && !serverPlayer2.m_150109_().m_36054_(itemStack)) {
            serverPlayer2.m_36176_(itemStack, false);
        }
        if (!serverPlayer2.m_150110_().f_35937_) {
            itemStack3.m_41622_(1, (LivingEntity)serverPlayer2, serverPlayer -> {});
        }
        serverPlayer2.m_284548_().m_5594_(null, serverPlayer2.m_20183_(), SoundEvents.f_11998_, SoundSource.PLAYERS, 0.7f, 1.25f);
        SocketTier socketTier = SocketTier.byLevel(compoundTag2.m_128425_("Tier", 3) ? compoundTag2.m_128451_("Tier") : FoodWeaponEvolution.foodTierById((String)object).level);
        serverPlayer2.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.socket_removed", (Object[])new Object[]{FoodWeaponEvolution.socketItemName((String)object), socketTier.displayName(), listTag.size(), FoodWeaponEvolution.socketCapacityInternal(itemStack2)}).m_130940_(ChatFormatting.GREEN));
        return true;
    }

    private static CompoundTag readyEvolutionTask(CompoundTag compoundTag) {
        for (int i = 0; i < 12; ++i) {
            CompoundTag compoundTag2 = compoundTag.m_128469_(FoodWeaponEvolution.taskKey(i));
            if (!compoundTag2.m_128471_("Ready") || compoundTag2.m_128471_("Claimed") || DailyRewardType.byId(compoundTag2.m_128461_(DAILY_REWARD_TYPE)) != DailyRewardType.EVOLUTION) continue;
            return compoundTag2;
        }
        return null;
    }

    public static int evolutionLevel(ItemStack itemStack) {
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            return FourWeaponFoundationAdapter.read(itemStack).level();
        }
        if (FoodWeaponEvolution.isEvolutionExcluded(itemStack)) {
            return 0;
        }
        CompoundTag compoundTag = FoodWeaponEvolution.data(itemStack);
        int n = FoodWeaponEvolution.maxLevel(itemStack);
        return compoundTag == null ? 0 : Math.max(0, Math.min(n, compoundTag.m_128451_(LEVEL)));
    }

    private static long mix64(long l) {
        l = (l ^ l >>> 30) * -4658895280553007687L;
        l = (l ^ l >>> 27) * -7723592293110705685L;
        return l ^ l >>> 31;
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void weaponHit(LivingHurtEvent livingHurtEvent) {
        FoodWeaponFinalDamage.captureEvolution(livingHurtEvent);
    }

    public static int currentEvolutionFloor(ItemStack itemStack) {
        return 0;
    }

    private static void quoteOverloadBurst(FoodWeaponHitPlan.Builder builder, ServerPlayer serverPlayer, LivingEntity livingEntity, float f, FoodWeaponSourceLedger.AttemptDomain attemptDomain) {
        ServerLevel serverLevel;
        Object object;
        block5: {
            block4: {
                object = livingEntity.m_9236_();
                if (!(object instanceof ServerLevel)) break block4;
                serverLevel = (ServerLevel)object;
                if (FoodWeaponDamageSources.attributeSecondary(serverPlayer) != null) break block5;
            }
            return;
        }
        object = List.copyOf(serverLevel.m_6443_(LivingEntity.class, livingEntity.m_20191_().m_82400_(3.0), livingEntity2 -> livingEntity2 != livingEntity && FoodWeaponEvolution.canTarget(serverPlayer, livingEntity2)));
        if (object.isEmpty()) {
            return;
        }
        builder.overload(attemptDomain, serverPlayer, livingEntity, (List<LivingEntity>)object, f);
    }

    private static ResourceLocation dailyRequestId(CompoundTag compoundTag) {
        String string = compoundTag.m_128461_(DAILY_REQUEST);
        return string.isBlank() ? null : ResourceLocation.m_135820_((String)string);
    }

    public static boolean selectGourmetSkillFromUi(ServerPlayer serverPlayer, ItemStack itemStack, int n) {
        if (itemStack != serverPlayer.m_21205_() && itemStack != serverPlayer.m_21206_()) {
            return false;
        }
        Profile profile = FoodWeaponEvolution.boundProfile(itemStack);
        if (!itemStack.m_41619_() && !FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack)) {
            return FoodWeaponProgression.select(serverPlayer, itemStack, n);
        }
        if (itemStack.m_41619_() || profile == null) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.gourmet.switch_wrong_weapon"), true);
            return false;
        }
        List<AcquiredSkill> list = FoodWeaponEvolution.gourmetSkills(itemStack);
        if (n < 0 || n >= list.size() || n >= 8) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.gourmet.switch_unavailable"), true);
            return false;
        }
        AcquiredSkill acquiredSkill = list.get(n);
        CompoundTag compoundTag = FoodWeaponEvolution.writableData(itemStack, profile);
        compoundTag.m_128405_(GOURMET_ACTIVE_SKILL, n);
        compoundTag.m_128359_(GOURMET_ACTIVE_PROFILE, acquiredSkill.profile.id);
        serverPlayer.m_284548_().m_5594_(null, serverPlayer.m_20183_(), SoundEvents.f_144243_, SoundSource.PLAYERS, 0.75f, 1.15f + (float)n * 0.04f);
        serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.gourmet.skill_switched", (Object[])new Object[]{n + 1, acquiredSkill.profile.displayName()}), true);
        FTBossMod.LOGGER.info("[FTBoss][\u4e07\u5473\u80da\u5203] {} \u5207\u6362\u5f53\u524d\u8fdb\u5316\u6280\u80fd\u5230 #{} ({})", new Object[]{serverPlayer.m_7755_().getString(), n + 1, acquiredSkill.profile.id});
        return true;
    }

    @SubscribeEvent
    public static void attributes(ItemAttributeModifierEvent itemAttributeModifierEvent) {
        if (itemAttributeModifierEvent.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }
        ItemStack itemStack = itemAttributeModifierEvent.getItemStack();
        if (FoodWeaponEvolution.boundProfile(itemStack) == null || FoodWeaponLifecycle.isBroken(itemStack)) {
            return;
        }
        FoodWeaponEvolution.addFoodBlessingAttributes(itemAttributeModifierEvent, itemStack);
        boolean bl = FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack);
        int n = FoodWeaponEvolution.evolutionLevel(itemStack);
        WeaponRole weaponRole = FoodWeaponEvolution.weaponRole(itemStack);
        SocketStats socketStats = FoodWeaponEvolution.socketStats(itemStack);
        double d = socketStats.effectPower(FoodType.MEAT, weaponRole);
        double d2 = socketStats.effectPower(FoodType.WINE, weaponRole);
        double d3 = socketStats.effectPower(FoodType.ENERGY, weaponRole);
        double d4 = socketStats.effectPower(FoodType.TEA, weaponRole);
        Profile profile = FoodWeaponEvolution.activeProfile(itemStack);
        double d5 = profile == null ? 0.0 : socketStats.effectPower(profile.food, weaponRole);
        double d6 = socketStats.effectPower(FoodType.CHEESE, weaponRole);
        double d7 = socketStats.effectPower(FoodType.VEGETABLE, weaponRole);
        double d8 = socketStats.effectPower(FoodType.APPLE, weaponRole);
        double d9 = socketStats.effectPower(FoodType.FISH, weaponRole);
        double d10 = socketStats.effectPower(FoodType.SWEET, weaponRole) + socketStats.effectPower(FoodType.FOOD, weaponRole);
        double d11 = FoodWeaponEvolution.evolutionDamageBonus(weaponRole, n, bl) + (bl ? FoodWeaponEvolution.gourmetStat(itemStack, EvolutionStat.ATTACK_DAMAGE) : 0.0) + d + d2 * 0.5 + d3 * 0.5 + d5 * 0.5;
        double d12 = FoodWeaponEvolution.evolutionSpeedBonus(weaponRole, n, bl) + (bl ? FoodWeaponEvolution.gourmetStat(itemStack, EvolutionStat.ATTACK_SPEED) : 0.0) + d4 * 0.05 + d3 * 0.02;
        double d13 = FoodWeaponEvolution.evolutionArmorBonus(weaponRole, n, bl) + d6 + (bl ? FoodWeaponEvolution.gourmetStat(itemStack, EvolutionStat.ARMOR) : 0.0);
        double d14 = FoodWeaponEvolution.evolutionHealthBonus(weaponRole, n, bl) + d8 * 2.0 + (bl ? FoodWeaponEvolution.gourmetStat(itemStack, EvolutionStat.MAX_HEALTH) : 0.0);
        double d15 = FoodWeaponEvolution.evolutionMovementBonus(weaponRole, n, bl) + d9 * 0.03 + (bl ? FoodWeaponEvolution.gourmetStat(itemStack, EvolutionStat.MOVEMENT_SPEED) : 0.0);
        double d16 = FoodWeaponEvolution.evolutionLuckBonus(weaponRole, n, bl) + d10 + (bl ? FoodWeaponEvolution.gourmetStat(itemStack, EvolutionStat.LUCK) : 0.0);
        if (d11 > 0.0) {
            itemAttributeModifierEvent.addModifier(Attributes.f_22281_, new AttributeModifier(EVOLUTION_DAMAGE, "ftboss.food_evolution.damage", d11, AttributeModifier.Operation.ADDITION));
        }
        if (d12 > 0.0) {
            itemAttributeModifierEvent.addModifier(Attributes.f_22283_, new AttributeModifier(EVOLUTION_SPEED, "ftboss.food_evolution.speed", d12, AttributeModifier.Operation.ADDITION));
        }
        if (d13 > 0.0) {
            itemAttributeModifierEvent.addModifier(Attributes.f_22284_, new AttributeModifier(SOCKET_ARMOR, "ftboss.food_socket.cheese", d13, AttributeModifier.Operation.ADDITION));
        }
        if (d7 > 0.0) {
            itemAttributeModifierEvent.addModifier(Attributes.f_22285_, new AttributeModifier(SOCKET_TOUGHNESS, "ftboss.food_socket.vegetable", d7 * 0.5, AttributeModifier.Operation.ADDITION));
        }
        if (d14 > 0.0) {
            itemAttributeModifierEvent.addModifier(Attributes.f_22276_, new AttributeModifier(SOCKET_HEALTH, "ftboss.food_socket.apple", d14, AttributeModifier.Operation.ADDITION));
        }
        if (d15 > 0.0) {
            itemAttributeModifierEvent.addModifier(Attributes.f_22279_, new AttributeModifier(SOCKET_MOVEMENT, "ftboss.food_socket.fish", d15, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        if (d16 > 0.0) {
            itemAttributeModifierEvent.addModifier(Attributes.f_22286_, new AttributeModifier(SOCKET_LUCK, "ftboss.food_socket.sweet", d16, AttributeModifier.Operation.ADDITION));
        }
    }

    private static ListTag foodBlessingList(ItemStack itemStack) {
        CompoundTag compoundTag = FoodWeaponEvolution.data(itemStack);
        return compoundTag == null ? new ListTag() : compoundTag.m_128437_(FOOD_BLESSINGS, 10);
    }

    public static boolean submitDailyFromUi(ServerPlayer serverPlayer, int n, int n2) {
        Profile profile = Profile.byTint(n);
        if (profile == null || n2 < 0 || n2 > 40 || n2 >= 36 && n2 < 40) {
            return false;
        }
        ItemStack itemStack = serverPlayer.m_150109_().m_8020_(n2);
        FoodType foodType = FoodWeaponEvolution.foodType(itemStack);
        if (itemStack.m_41619_() || foodType == null) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.wrong_food"), true);
            return false;
        }
        FoodWeaponEvolution.submitDailyFood(serverPlayer, profile, itemStack, foodType);
        return true;
    }

    private static SocketTier baseFoodTier(ItemStack itemStack) {
        if (itemStack.m_41619_()) {
            return SocketTier.BASIC;
        }
        if (itemStack.m_41782_() && itemStack.m_41783_().m_128425_("FTBossSocketTier", 3)) {
            return SocketTier.byLevel(itemStack.m_41783_().m_128451_("FTBossSocketTier"));
        }
        if (itemStack.m_41782_() && itemStack.m_41783_().m_128425_("BrewLevel", 99)) {
            int n = itemStack.m_41783_().m_128451_("BrewLevel");
            return n >= 5 ? SocketTier.GOURMET : (n >= 4 ? SocketTier.FINE : SocketTier.BASIC);
        }
        String string = FoodWeaponEvolution.itemId(itemStack).toLowerCase(Locale.ROOT);
        String string2 = string.substring(Math.max(0, string.indexOf(58) + 1));
        if (string.equals("minecraft:golden_apple") || string.equals("minecraft:enchanted_golden_apple") || FoodWeaponEvolution.containsFoodWord(string2, "deluxe", "supreme", "royal", "gourmet", "legendary", "ultimate", "banquet", "feast", "grand", "stuffed", "honey_glazed", "truffle", "lobster")) {
            return SocketTier.GOURMET;
        }
        Rarity rarity = itemStack.m_41791_();
        if (rarity == Rarity.EPIC || rarity == Rarity.RARE) {
            return SocketTier.GOURMET;
        }
        FoodProperties foodProperties = itemStack.getFoodProperties(null);
        if (foodProperties != null) {
            int n = foodProperties.m_38744_();
            float f = foodProperties.m_38745_();
            if (n >= 8 && f >= 0.8f || (float)n + f * 4.0f >= 11.0f) {
                return SocketTier.GOURMET;
            }
            if ((float)n + f * 4.0f >= 6.0f) {
                return SocketTier.FINE;
            }
        }
        if (rarity == Rarity.UNCOMMON || FoodWeaponEvolution.containsFoodWord(string2, "cooked", "baked", "grilled", "smoked", "fried", "roast", "sandwich", "burger", "stew", "soup", "pie", "cake", "tart", "pizza", "sushi", "nigiri", "roll", "latte", "cappuccino", "cocktail")) {
            return SocketTier.FINE;
        }
        return SocketTier.BASIC;
    }

    public static List<WeaponDetailView> weaponDetailViews(ItemStack itemStack) {
        int n;
        Object object;
        Profile profile = FoodWeaponEvolution.boundProfile(itemStack);
        if (profile == null) {
            return List.of();
        }
        ArrayList<WeaponDetailView> arrayList = new ArrayList<WeaponDetailView>();
        WeaponRole weaponRole = FoodWeaponEvolution.weaponRole(itemStack);
        arrayList.add(new WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.category.role"), weaponRole.displayName(), weaponRole.bonusDescription()));
        CompoundTag compoundTag = FoodWeaponEvolution.data(itemStack);
        int n2 = FoodWeaponEvolution.evolutionLevel(itemStack);
        boolean bl = FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack);
        if (FoodWeaponEvolution.nativeProfile(itemStack) != null) {
            arrayList.add(new WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.category.skill"), (Component)Component.m_237110_((String)"tooltip.ftboss.evolution.native_skill", (Object[])new Object[]{n2 + 1, Math.round((FoodWeaponEvolution.activeSkillMultiplier(itemStack) - 1.0f) * 100.0f)}), (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.native_skill_help")));
        } else if (bl) {
            object = FoodWeaponEvolution.gourmetSkills(itemStack);
            n = FoodWeaponEvolution.activeGourmetSkillIndex(itemStack);
            for (int i = 0; i < object.size(); ++i) {
                AcquiredSkill object2 = (AcquiredSkill)object.get(i);
                WeaponEvolutionSkillPool.Aspect aspect = WeaponEvolutionSkillPool.Aspect.fromId(object2.profile.id);
                Component component = aspect == null ? object2.profile.displayName() : Component.m_237115_((String)aspect.translationKey());
                boolean bl2 = i == n;
                arrayList.add(new WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.category.skill"), (Component)Component.m_237110_((String)"tooltip.ftboss.gourmet.skill_line", (Object[])new Object[]{i + 1, object2.profile.displayName(), object2.skill.nameComponent()}), (Component)Component.m_237110_((String)(bl2 ? "screen.ftboss.statue_hub.details.gourmet_skill_selected" : "screen.ftboss.statue_hub.details.gourmet_skill_switch"), (Object[])new Object[]{component, Component.m_237115_((String)object2.skill.effect.key())}), i, bl2));
            }
        } else if (n2 > 0 && compoundTag != null && (object = EvolutionSkill.byId(compoundTag.m_128461_(SKILL))) != null) {
            arrayList.add(new WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.category.skill"), (Component)Component.m_237110_((String)"tooltip.ftboss.evolution.drawn_skill", (Object[])new Object[]{((EvolutionSkill)((Object)object)).nameComponent()}), (Component)Component.m_237115_((String)((EvolutionSkill)((Object)object)).effect.key())));
        }
        if (n2 > 0) {
            object = WeaponEvolutionSkillPool.Aspect.fromId(FoodWeaponEvolution.firstProfileId(itemStack));
            if (bl && object != null) {
                arrayList.add(new WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.category.skill"), (Component)Component.m_237110_((String)"tooltip.ftboss.gourmet.active", (Object[])new Object[]{Component.m_237115_((String)((WeaponEvolutionSkillPool.Aspect)((Object)object)).translationKey())}), (Component)Component.m_237115_((String)((WeaponEvolutionSkillPool.Aspect)((Object)object)).descriptionKey())));
            } else if (object != null && FoodWeaponProgression.activeStage(itemStack) == 1) {
                arrayList.add(new WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.category.skill"), (Component)Component.m_237110_((String)"tooltip.ftboss.evolution.active_primary", (Object[])new Object[]{Component.m_237115_((String)((WeaponEvolutionSkillPool.Aspect)((Object)object)).translationKey())}), (Component)Component.m_237115_((String)((WeaponEvolutionSkillPool.Aspect)((Object)object)).descriptionKey())));
            } else if (object != null) {
                WeaponEvolutionSkillPool.Fusion fusion;
                Object object3 = WeaponEvolutionSkillPool.Aspect.fromId(FoodWeaponEvolution.secondProfileId(itemStack));
                if (object3 == null) {
                    object3 = object;
                }
                if ((fusion = WeaponEvolutionSkillPool.fusion((WeaponEvolutionSkillPool.Aspect)((Object)object), object3)) != null) {
                    arrayList.add(new WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.category.skill"), (Component)Component.m_237110_((String)"tooltip.ftboss.evolution.active_primary", (Object[])new Object[]{Component.m_237115_((String)fusion.primaryTranslationKey())}), (Component)Component.m_237115_((String)fusion.styleDescriptionKey())));
                    arrayList.add(new WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.category.skill"), (Component)Component.m_237110_((String)"tooltip.ftboss.evolution.active_secondary", (Object[])new Object[]{Component.m_237115_((String)fusion.secondaryTranslationKey())}), (Component)Component.m_237115_((String)fusion.styleDescriptionKey())));
                }
            }
        }
        if (bl) {
            for (EvolutionStat evolutionStat : EvolutionStat.values()) {
                double d = FoodWeaponEvolution.gourmetStat(itemStack, evolutionStat);
                if (d <= 0.0) continue;
                arrayList.add(new WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.category.stat"), evolutionStat.displayName(d), (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.stat_help")));
            }
        }
        for (FoodBlessingView foodBlessingView : FoodWeaponEvolution.foodBlessingViews(itemStack)) {
            arrayList.add(new WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.category.blessing"), (Component)foodBlessingView.name().m_6881_().m_7220_((Component)Component.m_237113_((String)(" " + foodBlessingView.rank()))), foodBlessingView.description()));
        }
        object = FoodWeaponEvolution.socketViews(itemStack);
        for (n = 0; n < object.size(); ++n) {
            SocketView socketView = (SocketView)object.get(n);
            arrayList.add(new WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.category.socket"), (Component)Component.m_237113_((String)("#" + (n + 1) + " ")).m_7220_(socketView.stack().m_41786_()).m_7220_((Component)Component.m_237113_((String)" \u00b7 ")).m_7220_(socketView.type()).m_7220_((Component)Component.m_237113_((String)" \u00b7 ")).m_7220_(socketView.tier()), (Component)socketView.effect().m_6881_().m_7220_((Component)Component.m_237113_((String)" \u00b7 ")).m_7220_(socketView.synergy())));
        }
        arrayList.addAll(FoodWeaponProgression.details(itemStack));
        arrayList.addAll(FoodSocketPowers.details(itemStack));
        return List.copyOf(arrayList);
    }

    private static int socketColor(FoodType foodType) {
        return switch (foodType) {
            default -> throw new IncompatibleClassChangeError();
            case FoodType.MEAT -> 13777476;
            case FoodType.CHEESE -> 16239937;
            case FoodType.TEA -> 4243050;
            case FoodType.WINE -> 9387702;
            case FoodType.SWEET -> 16738495;
            case FoodType.APPLE -> 14891583;
            case FoodType.FISH -> 4116466;
            case FoodType.VEGETABLE -> 5883723;
            case FoodType.ENERGY -> 16748078;
            case FoodType.FOOD -> 0xEEEEDE;
        };
    }

    private static void removeSocket(ServerPlayer serverPlayer2, Profile profile, ItemStack itemStack, ItemStack itemStack2, InteractionHand interactionHand) {
        ItemStack itemStack3;
        Item item;
        Profile profile2 = FoodWeaponEvolution.boundProfile(itemStack);
        if (profile2 == null) {
            serverPlayer2.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.socket_attune_first"), true);
            return;
        }
        CompoundTag compoundTag = FoodWeaponEvolution.writableData(itemStack, profile2);
        ListTag listTag = compoundTag.m_128437_(SOCKETS, 10);
        if (listTag.isEmpty()) {
            serverPlayer2.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.socket_empty"), true);
            return;
        }
        CompoundTag compoundTag2 = listTag.m_128728_(listTag.size() - 1);
        listTag.remove(listTag.size() - 1);
        compoundTag.m_128365_(SOCKETS, (Tag)listTag);
        String string = compoundTag2.m_128461_("Item");
        ResourceLocation resourceLocation = ResourceLocation.m_135820_((String)string);
        Item item2 = item = resourceLocation == null ? null : (Item)ForgeRegistries.ITEMS.getValue(resourceLocation);
        ItemStack itemStack4 = compoundTag2.m_128425_("Stack", 10) ? ItemStack.m_41712_((CompoundTag)compoundTag2.m_128469_("Stack")) : (itemStack3 = item == null || item == Items.f_41852_ ? ItemStack.f_41583_ : new ItemStack((ItemLike)item));
        if (!itemStack3.m_41619_() && !serverPlayer2.m_150109_().m_36054_(itemStack3)) {
            serverPlayer2.m_36176_(itemStack3, false);
        }
        if (!serverPlayer2.m_150110_().f_35937_) {
            itemStack2.m_41622_(1, (LivingEntity)serverPlayer2, serverPlayer -> serverPlayer.m_21190_(interactionHand));
        }
        serverPlayer2.m_284548_().m_5594_(null, serverPlayer2.m_20183_(), SoundEvents.f_11998_, SoundSource.PLAYERS, 0.7f, 1.25f);
        SocketTier socketTier = SocketTier.byLevel(compoundTag2.m_128425_("Tier", 3) ? compoundTag2.m_128451_("Tier") : FoodWeaponEvolution.foodTierById((String)string).level);
        serverPlayer2.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.socket_removed", (Object[])new Object[]{FoodWeaponEvolution.socketItemName(string), socketTier.displayName(), listTag.size(), FoodWeaponEvolution.socketCapacityInternal(itemStack)}).m_130940_(ChatFormatting.GREEN));
    }

    private static void knockAway(LivingEntity livingEntity, Vec3 vec3, double d) {
        Vec3 vec32 = livingEntity.m_20182_().m_82546_(vec3).m_82542_(1.0, 0.0, 1.0).m_82541_();
        double d2 = Math.max(0.0, Math.min(1.0, livingEntity.m_21133_(Attributes.f_22278_)));
        double d3 = d * (1.0 - d2);
        if (vec32.m_82556_() < 1.0E-6 || d3 <= 0.0) {
            return;
        }
        livingEntity.m_5997_(vec32.f_82479_ * d3, 0.18 * (1.0 - d2), vec32.f_82481_ * d3);
        livingEntity.f_19864_ = true;
    }

    private static boolean isDailyFoodCandidate(ResourceLocation resourceLocation, FoodType foodType, Set<ResourceLocation> set) {
        Item item = (Item)ForgeRegistries.ITEMS.getValue(resourceLocation);
        if (item == null || item == Items.f_41852_) {
            return false;
        }
        ItemStack itemStack = new ItemStack((ItemLike)item);
        if (itemStack.m_41619_() || FoodWeaponEvolution.foodType(itemStack) != foodType) {
            return false;
        }
        boolean bl = itemStack.m_41614_() || itemStack.m_41780_() == UseAnim.DRINK || FoodWeaponEvolution.isNamedDrink(resourceLocation.m_135815_(), foodType);
        return bl && (set.contains(resourceLocation) || FoodWeaponEvolution.isNaturallyObtainableDailyFood(itemStack, resourceLocation));
    }

    public static boolean isGourmetBlankBlade(ItemStack itemStack) {
        return "ftboss:gourmet_blank_blade".equals(FoodWeaponEvolution.itemId(itemStack));
    }

    @SubscribeEvent
    public static void toolBreakSpeed(PlayerEvent.BreakSpeed breakSpeed) {
        ItemStack itemStack = breakSpeed.getEntity().m_21205_();
        WeaponRole weaponRole = FoodWeaponEvolution.weaponRole(itemStack);
        if (FoodWeaponEvolution.boundProfile(itemStack) == null || FoodWeaponLifecycle.isBroken(itemStack) || weaponRole != WeaponRole.TOOL || breakSpeed.getNewSpeed() <= 0.0f) {
            return;
        }
        boolean bl = FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack);
        SocketStats socketStats = FoodWeaponEvolution.socketStats(itemStack);
        double d = (double)FoodWeaponEvolution.evolutionLevel(itemStack) * (bl ? 0.05 : 0.15);
        double d2 = socketStats.effectPower(FoodType.TEA, weaponRole) * 0.1 + socketStats.effectPower(FoodType.ENERGY, weaponRole) * 0.12 + socketStats.effectPower(FoodType.VEGETABLE, weaponRole) * 0.06;
        double d3 = 1.0 + d + Math.min(2.0, d2);
        breakSpeed.setNewSpeed((float)((double)breakSpeed.getNewSpeed() * d3));
    }

    private static void quoteEvolutionSkill(FoodWeaponHitPlan.Builder builder, ServerPlayer serverPlayer, LivingEntity livingEntity, CompoundTag compoundTag, EvolutionSkill evolutionSkill, long l, String string, float f, FoodWeaponHitPlan.RandomQuotes randomQuotes) {
        int n = evolutionSkill.effect == SkillEffect.LIFESTEAL ? 60 : 20;
        boolean bl = l - compoundTag.m_128454_(string) >= (long)n;
        FoodWeaponSourceLedger.AttemptDomain attemptDomain = FoodWeaponSourceLedger.AttemptDomain.valueOf("EVOLUTION_" + evolutionSkill.name());
        if (evolutionSkill.effect == SkillEffect.DAMAGE) {
            if (builder.roll(attemptDomain, 1.0, randomQuotes)) {
                builder.multiply(1.0f + 0.18f * f);
            }
            return;
        }
        if (evolutionSkill.effect == SkillEffect.LIFESTEAL) {
            if (bl && builder.roll(attemptDomain, 1.0, randomQuotes)) {
                builder.heal(attemptDomain, (LivingEntity)serverPlayer, 1.0f);
                builder.writeAfterWorld(attemptDomain, compoundTag, string, (Tag)LongTag.m_128882_((long)l));
            }
            return;
        }
        if (!bl || evolutionSkill.effect == SkillEffect.CHAIN && FoodWeaponDamageSources.attributeSecondary(serverPlayer) == null) {
            return;
        }
        if (!builder.roll(attemptDomain, 0.28, randomQuotes)) {
            return;
        }
        if (evolutionSkill.effect == SkillEffect.CHAIN) {
            FoodWeaponEvolution.quoteChainArc(builder, serverPlayer, livingEntity, 5.0f * f, attemptDomain, compoundTag, string, l);
            return;
        }
        boolean bl2 = true;
        switch (evolutionSkill.effect) {
            case DAMAGE: {
                builder.multiply(1.0f + 0.6f * f);
                break;
            }
            case CRIT: {
                builder.effect(attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_SLOW, livingEntity, new MobEffectInstance(MobEffects.f_19597_, 80, 1), serverPlayer);
                break;
            }
            case LIFESTEAL: {
                builder.effect(attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_POISON, livingEntity, new MobEffectInstance(MobEffects.f_19614_, 80, 0), serverPlayer);
                break;
            }
            case SLOW: {
                builder.fire(attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_FIRE, livingEntity, 4);
                break;
            }
            case POISON: {
                builder.effect(attemptDomain, null, (LivingEntity)serverPlayer, new MobEffectInstance(MobEffects.f_19617_, 100, 0), serverPlayer);
                break;
            }
            case FIRE: {
                builder.effect(attemptDomain, null, (LivingEntity)serverPlayer, new MobEffectInstance(MobEffects.f_19596_, 100, 0), serverPlayer);
                builder.effect(attemptDomain, null, (LivingEntity)serverPlayer, new MobEffectInstance(MobEffects.f_19598_, 100, 0), serverPlayer);
                break;
            }
            case GUARD: {
                builder.effect(attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_WEAKEN, livingEntity, new MobEffectInstance(MobEffects.f_19613_, 100, 0), serverPlayer);
                break;
            }
            case HASTE: {
                builder.pushAway(attemptDomain, livingEntity, serverPlayer.m_20182_(), 0.85);
                break;
            }
            case WEAKEN: {
                builder.effect(attemptDomain, null, (LivingEntity)serverPlayer, new MobEffectInstance(MobEffects.f_19605_, 80, 0), serverPlayer);
                break;
            }
            default: {
                bl2 = false;
            }
        }
        if (bl2) {
            builder.writeAfterWorld(attemptDomain, compoundTag, string, (Tag)LongTag.m_128882_((long)l));
        }
    }

    public static boolean isEvolutionCandidate(ItemStack itemStack) {
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            return FourWeaponFoundationAdapter.read(itemStack).valid();
        }
        return FoodWeaponEvolution.weaponCandidate(itemStack);
    }

    public static int socketVisualColor(ItemStack itemStack, int n) {
        List<Socket> list = FoodWeaponEvolution.sockets(itemStack);
        if (n < 1 || n > list.size()) {
            return -1;
        }
        return FoodWeaponEvolution.socketColor(list.get((int)(n - 1)).type);
    }

    private static void showDaily(ServerPlayer serverPlayer, Profile profile) {
        CompoundTag compoundTag = FoodWeaponEvolution.daily(serverPlayer, profile);
        serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.daily_header", (Object[])new Object[]{profile.displayName(), 12}).m_130940_(ChatFormatting.GOLD));
        for (int i = 0; i < 12; ++i) {
            CompoundTag compoundTag2 = compoundTag.m_128469_(FoodWeaponEvolution.taskKey(i));
            int n = compoundTag2.m_128451_(DAILY_REQUIRED);
            int n2 = Math.min(n, compoundTag2.m_128451_("Progress"));
            Component component = FoodWeaponEvolution.dailyRewardName(compoundTag2);
            if (compoundTag2.m_128471_("Claimed")) {
                serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.daily_task_claimed", (Object[])new Object[]{i + 1, FoodWeaponEvolution.dailyRequestName(compoundTag2, profile), component}).m_130940_(ChatFormatting.DARK_GRAY));
                continue;
            }
            if (compoundTag2.m_128471_("Ready")) {
                serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.daily_task_ready", (Object[])new Object[]{i + 1, FoodWeaponEvolution.dailyRequestName(compoundTag2, profile), component}).m_130940_(ChatFormatting.GREEN));
                continue;
            }
            serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.daily_task", (Object[])new Object[]{i + 1, FoodWeaponEvolution.dailyRequestName(compoundTag2, profile), n2, n, component}).m_130940_(ChatFormatting.YELLOW));
        }
        serverPlayer.m_213846_((Component)Component.m_237115_((String)"message.ftboss.evolution.statue_help").m_130940_(ChatFormatting.DARK_GRAY));
    }

    public static boolean isSocketIngredient(ItemStack itemStack) {
        return FoodWeaponEvolution.foodType(itemStack) != null;
    }

    private static boolean applyFoodBlessing(ServerPlayer serverPlayer, Profile profile, ItemStack itemStack, ItemStack itemStack2, FoodType foodType) {
        int n;
        Profile profile2 = FoodWeaponEvolution.boundProfile(itemStack);
        if (profile2 == null) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.food_blessing.attune_first"), true);
            return false;
        }
        if (foodType != profile.food) {
            serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.food_blessing.wrong_food", (Object[])new Object[]{profile.food.displayName()}), true);
            return false;
        }
        ListTag listTag = FoodWeaponEvolution.foodBlessingList(itemStack);
        if (listTag.size() >= 6) {
            serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.food_blessing.full", (Object[])new Object[]{6}), true);
            return false;
        }
        ArrayList<FoodBlessing> arrayList = new ArrayList<FoodBlessing>();
        for (FoodBlessing enum_2 : FoodWeaponEvolution.blessingPool(profile)) {
            n = 0;
            for (int compoundTag = 0; compoundTag < listTag.size(); ++compoundTag) {
                if (!enum_2.id.equals(listTag.m_128728_(compoundTag).m_128461_("Blessing"))) continue;
                n = 1;
                break;
            }
            if (n != 0) continue;
            arrayList.add(enum_2);
        }
        if (arrayList.isEmpty()) {
            serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.food_blessing.complete", (Object[])new Object[]{profile.displayName()}), true);
            return false;
        }
        FoodBlessing foodBlessing = (FoodBlessing)((Object)arrayList.get(serverPlayer.m_217043_().m_188503_(arrayList.size())));
        SocketTier socketTier = FoodWeaponEvolution.foodTier(itemStack2);
        n = switch (socketTier) {
            default -> throw new IncompatibleClassChangeError();
            case SocketTier.BASIC -> 1;
            case SocketTier.FINE -> {
                if (serverPlayer.m_217043_().m_188499_()) {
                    yield 2;
                }
                yield 1;
            }
            case SocketTier.GOURMET -> serverPlayer.m_217043_().m_188499_() ? 3 : 2;
        };
        CompoundTag compoundTag = FoodWeaponEvolution.writableData(itemStack, profile2);
        ListTag listTag2 = compoundTag.m_128437_(FOOD_BLESSINGS, 10);
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.m_128359_(PROFILE, profile.id);
        compoundTag2.m_128359_("Blessing", foodBlessing.id);
        compoundTag2.m_128405_(LEVEL, n);
        compoundTag2.m_128405_("FoodTier", socketTier.level);
        compoundTag2.m_128359_("Food", FoodWeaponEvolution.itemId(itemStack2));
        listTag2.add((Object)compoundTag2);
        compoundTag.m_128365_(FOOD_BLESSINGS, (Tag)listTag2);
        if (!serverPlayer.m_150110_().f_35937_) {
            itemStack2.m_41774_(1);
        }
        serverPlayer.m_284548_().m_8767_((ParticleOptions)ParticleTypes.f_123809_, serverPlayer.m_20185_(), serverPlayer.m_20186_() + 1.0, serverPlayer.m_20189_(), 72, 0.7, 0.8, 0.7, 0.15);
        serverPlayer.m_284548_().m_8767_((ParticleOptions)ParticleTypes.f_123810_, serverPlayer.m_20185_(), serverPlayer.m_20186_() + 1.0, serverPlayer.m_20189_(), 18, 0.4, 0.5, 0.4, 0.08);
        serverPlayer.m_284548_().m_5594_(null, serverPlayer.m_20183_(), SoundEvents.f_11887_, SoundSource.PLAYERS, 0.9f, 1.1f + (float)socketTier.level * 0.12f);
        serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.food_blessing.applied", (Object[])new Object[]{foodBlessing.nameComponent(), FoodWeaponEvolution.toRoman(n), socketTier.displayName()}).m_130940_(ChatFormatting.LIGHT_PURPLE));
        serverPlayer.m_213846_((Component)foodBlessing.descriptionComponent(n).m_6881_().m_130940_(ChatFormatting.GRAY));
        FTBossMod.LOGGER.info("[FTBoss][\u98df\u7269\u9644\u9b54] {} \u7684 {} \u5728 {} \u77f3\u50cf\u83b7\u5f97 {} \u7b49\u7ea7{}\uff08\u98df\u7269\u7b49\u7ea7{}\uff09", new Object[]{serverPlayer.m_7755_().getString(), FoodWeaponEvolution.itemId(itemStack), profile.id, foodBlessing.id, n, socketTier.level});
        return true;
    }

    public static int activeGourmetSkillIndex(ItemStack itemStack) {
        Profile profile;
        int n;
        if (!FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack)) {
            return -1;
        }
        List<AcquiredSkill> list = FoodWeaponEvolution.gourmetSkills(itemStack);
        if (list.isEmpty()) {
            return -1;
        }
        CompoundTag compoundTag = FoodWeaponEvolution.data(itemStack);
        if (compoundTag != null && compoundTag.m_128425_(GOURMET_ACTIVE_SKILL, 3) && (n = compoundTag.m_128451_(GOURMET_ACTIVE_SKILL)) >= 0 && n < list.size()) {
            return n;
        }
        Profile profile2 = profile = compoundTag == null ? null : Profile.byId(compoundTag.m_128461_(GOURMET_ACTIVE_PROFILE));
        if (profile != null) {
            for (int i = list.size() - 1; i >= 0; --i) {
                if (list.get((int)i).profile != profile) continue;
                return i;
            }
        }
        return list.size() - 1;
    }

    private static FoodType explicitOfferingType(String string) {
        return switch (string) {
            case "ftboss:sugar_queen_crystal", "ftboss:frost_crown_charm" -> FoodType.SWEET;
            case "ftboss:meat_heart", "ftboss:butcher_fang_charm", "ftboss:benger_whistle", "ftboss:shepherd_whistle_charm" -> FoodType.MEAT;
            case "ftboss:tea_seed", "ftboss:tea_heart_pendant" -> FoodType.TEA;
            case "ftboss:wine_charm", "ftboss:aged_wine_seal" -> FoodType.WINE;
            case "ftboss:rat_bait", "ftboss:cheese_moon_charm" -> FoodType.CHEESE;
            case "ftboss:defect_core", "ftboss:charged_core_pendant" -> FoodType.ENERGY;
            case "ftboss:compost_summon", "ftboss:rainbow_spore_charm" -> FoodType.VEGETABLE;
            case "ftboss:forbidden_apple", "ftboss:golden_apple_brooch" -> FoodType.APPLE;
            case "ftboss:tide_banquet", "ftboss:tide_pearl", "ftboss:tide_pearl_pendant" -> FoodType.FISH;
            default -> null;
        };
    }

    private static boolean canTarget(ServerPlayer serverPlayer, LivingEntity livingEntity) {
        if (livingEntity == serverPlayer || !livingEntity.m_6084_() || livingEntity.m_213877_() || serverPlayer.m_7307_((Entity)livingEntity) || livingEntity.m_7307_((Entity)serverPlayer) || serverPlayer.m_20365_((Entity)livingEntity)) {
            return false;
        }
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            return !player.m_7500_() && !player.m_5833_() && serverPlayer.m_284548_().m_7654_().m_129799_() && serverPlayer.m_7099_(player);
        }
        return true;
    }

    public static int socketCapacity(ItemStack itemStack) {
        return FoodWeaponEvolution.socketCapacityInternal(itemStack);
    }

    private static Profile activeProfile(ItemStack itemStack) {
        if (!FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack)) {
            return FoodWeaponEvolution.boundProfile(itemStack);
        }
        List<AcquiredSkill> list = FoodWeaponEvolution.gourmetSkills(itemStack);
        int n = FoodWeaponEvolution.activeGourmetSkillIndex(itemStack);
        if (n >= 0 && n < list.size()) {
            return list.get((int)n).profile;
        }
        CompoundTag compoundTag = FoodWeaponEvolution.data(itemStack);
        Profile profile = compoundTag == null ? null : Profile.byId(compoundTag.m_128461_(GOURMET_ACTIVE_PROFILE));
        return profile == null ? FoodWeaponEvolution.boundProfile(itemStack) : profile;
    }

    public static boolean isBlessingCandidate(ItemStack itemStack, ItemStack itemStack2, int n) {
        Profile profile = Profile.byTint(n);
        Profile profile2 = FoodWeaponEvolution.boundProfile(itemStack);
        FoodType foodType = FoodWeaponEvolution.foodType(itemStack2);
        return profile != null && profile2 != null && !itemStack2.m_41619_() && !itemStack.m_150930_(Items.f_42690_) && itemStack2 != itemStack && !FoodWeaponEvolution.weaponCandidate(itemStack2) && foodType == profile.food && FoodWeaponEvolution.foodBlessingList(itemStack).size() < 6;
    }

    public static void foodBlessingDefense(LivingHurtEvent livingHurtEvent) {
        LivingEntity livingEntity;
        if (Boolean.TRUE.equals(EXTRA_DAMAGE.get()) || livingHurtEvent.getAmount() <= 0.0f || !((livingEntity = livingHurtEvent.getEntity()) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
        livingEntity = serverPlayer.m_21205_();
        if (FoodWeaponEvolution.boundProfile((ItemStack)livingEntity) == null || FoodWeaponLifecycle.isBroken((ItemStack)livingEntity)) {
            return;
        }
        FoodBlessingStats foodBlessingStats = new FoodBlessingStats((ItemStack)livingEntity);
        int n = foodBlessingStats.level(FoodBlessingKind.LOW_HEALTH_GUARD);
        int n2 = foodBlessingStats.level(FoodBlessingKind.PROJECTILE_GUARD);
        int n3 = foodBlessingStats.level(FoodBlessingKind.WET_GUARD);
        float f = 1.0f;
        if (n > 0 && serverPlayer.m_21223_() <= serverPlayer.m_21233_() * 0.5f) {
            f *= Math.max(0.55f, 1.0f - (float)n * 0.09f);
        }
        if (n2 > 0 && livingHurtEvent.getSource().m_7640_() instanceof Projectile) {
            f *= Math.max(0.55f, 1.0f - (float)n2 * 0.1f);
        }
        if (n3 > 0 && serverPlayer.m_20071_()) {
            f *= Math.max(0.55f, 1.0f - (float)n3 * 0.08f);
        }
        if (f < 1.0f) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * f);
        }
    }

    private static double evolutionMovementBonus(WeaponRole weaponRole, int n, boolean bl) {
        if (weaponRole == WeaponRole.DUELIST) {
            return (double)n * (bl ? 0.004 : 0.015);
        }
        if (weaponRole == WeaponRole.RANGED) {
            return (double)n * (bl ? 0.003 : 0.01);
        }
        return 0.0;
    }

    static Component skillDescription(EvolutionSkill evolutionSkill) {
        return Component.m_237115_((String)evolutionSkill.effect.key());
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent itemTooltipEvent) {
        FoodType foodType;
        ItemStack itemStack = itemTooltipEvent.getItemStack();
        boolean bl = FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack);
        Profile profile = FoodWeaponEvolution.boundProfile(itemStack);
        FoodType foodType2 = foodType = profile == null && !bl ? FoodWeaponEvolution.foodType(itemStack) : null;
        if (profile == null && !bl && foodType == null) {
            return;
        }
        if (!FoodAppraisalMonocleItem.isEquipped((LivingEntity)itemTooltipEvent.getEntity())) {
            return;
        }
        List list = itemTooltipEvent.getToolTip();
        list.add(Component.m_237119_());
        list.add(Component.m_237115_((String)"tooltip.ftboss.food_appraisal_monocle.readout").m_130940_(ChatFormatting.DARK_AQUA));
        if (profile == null) {
            if (bl) {
                list.add(Component.m_237110_((String)"tooltip.ftboss.gourmet.unattuned", (Object[])new Object[]{8, 30}).m_130940_(ChatFormatting.AQUA));
                list.add(Component.m_237115_((String)"tooltip.ftboss.evolution.ui_hint").m_130940_(ChatFormatting.DARK_GRAY));
                return;
            }
            FoodWeaponEvolution.addSocketIngredientTooltip(list, itemStack, foodType);
            return;
        }
        int n = FoodWeaponEvolution.evolutionLevel(itemStack);
        int n2 = FoodWeaponEvolution.maxLevel(itemStack);
        int n3 = FoodWeaponEvolution.evolutionExperience(itemStack);
        int n4 = n >= n2 ? n3 : FoodWeaponEvolution.threshold(itemStack, n);
        list.add(Component.m_237110_((String)"tooltip.ftboss.evolution.progress", (Object[])new Object[]{profile.displayName(), n, n2, n3, n4}).m_130940_(ChatFormatting.AQUA));
        list.add(Component.m_237110_((String)"tooltip.ftboss.evolution.sockets", (Object[])new Object[]{FoodWeaponEvolution.socketCount(itemStack), FoodWeaponEvolution.socketCapacityInternal(itemStack)}).m_130940_(ChatFormatting.GREEN));
        list.add(Component.m_237115_((String)"tooltip.ftboss.evolution.ui_hint").m_130940_(ChatFormatting.DARK_GRAY));
    }

    private static boolean ensureDailyTasks(ServerPlayer serverPlayer, Profile profile, long l, CompoundTag compoundTag) {
        boolean bl;
        int n;
        int n2;
        Object object;
        CompoundTag compoundTag2;
        boolean bl2 = false;
        if (compoundTag.m_128425_(DAILY_REQUEST, 8) && !compoundTag.m_128425_(FoodWeaponEvolution.taskKey(0), 10)) {
            compoundTag2 = new CompoundTag();
            compoundTag2.m_128359_(DAILY_REQUEST, compoundTag.m_128461_(DAILY_REQUEST));
            compoundTag2.m_128405_(DAILY_REQUIRED, 8);
            compoundTag2.m_128359_(DAILY_REWARD_TYPE, DailyRewardType.EVOLUTION.id);
            compoundTag2.m_128405_(DAILY_REWARD_AMOUNT, 40);
            compoundTag2.m_128405_("Progress", compoundTag.m_128451_("Progress"));
            compoundTag2.m_128379_("Ready", compoundTag.m_128471_("Ready"));
            compoundTag2.m_128379_("Claimed", compoundTag.m_128471_("Claimed"));
            compoundTag.m_128365_(FoodWeaponEvolution.taskKey(0), (Tag)compoundTag2);
            object = List.of(DAILY_REQUEST, "Progress", "Ready", "Claimed").iterator();
            while (object.hasNext()) {
                String string = (String)object.next();
                compoundTag.m_128473_(string);
            }
            bl2 = true;
        }
        compoundTag2 = FoodWeaponEvolution.dailyFoodPool(serverPlayer, profile.food);
        object = new boolean[12];
        int n3 = 0;
        ArrayList<ResourceLocation> arrayList = new ArrayList<ResourceLocation>();
        for (int i = 0; i < 12; ++i) {
            ResourceLocation resourceLocation;
            CompoundTag compoundTag3 = compoundTag.m_128469_(FoodWeaponEvolution.taskKey(i));
            object[i] = FoodWeaponEvolution.validDailyTask(compoundTag3, (List<ResourceLocation>)compoundTag2);
            if (!object[i]) continue;
            if (DailyRewardType.byId(compoundTag3.m_128461_(DAILY_REWARD_TYPE)) == DailyRewardType.EVOLUTION) {
                ++n3;
            }
            if ((resourceLocation = FoodWeaponEvolution.dailyRequestId(compoundTag3)) == null) continue;
            arrayList.add(resourceLocation);
        }
        long l2 = FoodWeaponEvolution.dailySeed(serverPlayer, profile, l);
        int n4 = FoodWeaponEvolution.positiveMod(FoodWeaponEvolution.mix64(l2 ^ 0x632BE59BD9B4E019L), 12);
        int n5 = -1;
        if (n3 < 3) {
            if (!object[n4]) {
                n5 = n4;
            } else {
                for (n2 = 11; n2 >= 0; --n2) {
                    if (object[n2]) continue;
                    n5 = n2;
                    break;
                }
            }
        }
        n2 = 0;
        boolean[] blArray = object;
        int n6 = blArray.length;
        for (n = 0; n < n6; ++n) {
            bl = blArray[n];
            if (bl) continue;
            ++n2;
        }
        int n7 = FoodWeaponEvolution.positiveMod(FoodWeaponEvolution.mix64(l2 ^ 0x94D049BB133111EBL), 2);
        for (n6 = 0; n6 < 12; ++n6) {
            DailyRewardType dailyRewardType;
            if (object[n6] != false) continue;
            n = 3 - n3;
            boolean bl3 = bl = n > 0 && (n6 == n5 || n2 <= n);
            DailyRewardType dailyRewardType2 = bl ? DailyRewardType.EVOLUTION : (dailyRewardType = (n6 + n7 & 1) == 0 ? DailyRewardType.EXPERIENCE : DailyRewardType.COINS);
            if (bl) {
                ++n3;
            }
            --n2;
            CompoundTag compoundTag4 = FoodWeaponEvolution.createDailyTask(serverPlayer, profile, l, n6, dailyRewardType, (List<ResourceLocation>)compoundTag2, arrayList);
            compoundTag.m_128365_(FoodWeaponEvolution.taskKey(n6), (Tag)compoundTag4);
            ResourceLocation resourceLocation = FoodWeaponEvolution.dailyRequestId(compoundTag4);
            if (resourceLocation != null) {
                arrayList.add(resourceLocation);
            }
            bl2 = true;
        }
        return bl2;
    }

    public static boolean claimEvolutionFromUi(ServerPlayer serverPlayer, int n) {
        Profile profile = Profile.byTint(n);
        ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
        if (profile == null || itemStack.m_41619_()) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.not_weapon"), true);
            return false;
        }
        FoodWeaponEvolution.handleWeapon(serverPlayer, profile, itemStack, false);
        return true;
    }

    private static void grantImmediateDailyReward(ServerPlayer serverPlayer, DailyRewardType dailyRewardType, int n) {
        if (dailyRewardType == DailyRewardType.EXPERIENCE) {
            serverPlayer.m_6756_(n);
        } else if (dailyRewardType == DailyRewardType.COINS) {
            ItemStack itemStack;
            Item item;
            Item item2 = item = COIN_ID == null ? null : (Item)ForgeRegistries.ITEMS.getValue(COIN_ID);
            if (item == null || item == Items.f_41852_) {
                item = Items.f_42587_;
            }
            for (int i = n; i > 0; i -= itemStack.m_41613_()) {
                itemStack = new ItemStack((ItemLike)item, Math.min(i, item.m_41459_()));
                if (serverPlayer.m_36356_(itemStack) || itemStack.m_41619_()) continue;
                serverPlayer.m_36176_(itemStack, false);
            }
        }
    }

    public static float activeSkillMultiplier(ItemStack itemStack) {
        if (FoodWeaponEvolution.isEvolutionExcluded(itemStack)) {
            return 1.0f;
        }
        boolean bl = FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack);
        WeaponRole weaponRole = FoodWeaponEvolution.weaponRole(itemStack);
        float f = (float)FoodWeaponEvolution.evolutionLevel(itemStack) * weaponRole.skillPowerPerLevel(bl);
        float f2 = 0.0f;
        if (weaponRole == WeaponRole.ARCANE) {
            SocketStats socketStats = FoodWeaponEvolution.socketStats(itemStack);
            f2 = (float)Math.min(0.4, (socketStats.effectPower(FoodType.TEA, weaponRole) + socketStats.effectPower(FoodType.SWEET, weaponRole) + socketStats.effectPower(FoodType.FOOD, weaponRole) + socketStats.effectPower(FoodType.ENERGY, weaponRole)) * 0.015);
        }
        float f3 = 1.0f + f + f2;
        if (weaponRole == WeaponRole.ARCANE || weaponRole == WeaponRole.RANGED) {
            f3 *= (float)WeaponMaterialBuild.build(itemStack).damage();
        }
        return f3;
    }

    private static ItemStack socketStack(Socket socket) {
        ResourceLocation resourceLocation = ResourceLocation.m_135820_((String)socket.item);
        Item item = resourceLocation == null ? null : (Item)ForgeRegistries.ITEMS.getValue(resourceLocation);
        return item == null || item == Items.f_41852_ ? ItemStack.f_41583_ : new ItemStack((ItemLike)item);
    }

    private static List<AcquiredSkill> gourmetSkills(ItemStack itemStack) {
        ArrayList<AcquiredSkill> arrayList = new ArrayList<AcquiredSkill>();
        CompoundTag compoundTag = FoodWeaponEvolution.data(itemStack);
        if (compoundTag == null || !compoundTag.m_128425_(GOURMET_SKILLS, 9)) {
            return arrayList;
        }
        ListTag listTag = compoundTag.m_128437_(GOURMET_SKILLS, 10);
        for (int i = 0; i < listTag.size() && i < 8; ++i) {
            CompoundTag compoundTag2 = listTag.m_128728_(i);
            Profile profile = Profile.byId(compoundTag2.m_128461_(PROFILE));
            EvolutionSkill evolutionSkill = EvolutionSkill.byId(compoundTag2.m_128461_(SKILL));
            if (profile == null || evolutionSkill == null) continue;
            arrayList.add(new AcquiredSkill(profile, evolutionSkill));
        }
        return arrayList;
    }

    public static FoodWeaponHitPlan quoteFinalWeaponHit(LivingHurtEvent livingHurtEvent, FoodWeaponBondHits.Hit hit, ItemStack itemStack, FoodWeaponHitPlan.RandomQuotes randomQuotes) {
        double d;
        ServerPlayer serverPlayer;
        Entity entity;
        FoodWeaponHitPlan.Builder builder = new FoodWeaponHitPlan.Builder(livingHurtEvent.getAmount());
        if (hit == null || livingHurtEvent.isCanceled() || FoodWeaponBondHits.secondary(livingHurtEvent.getSource()) || !Float.isFinite(livingHurtEvent.getAmount()) || livingHurtEvent.getAmount() <= 0.0f || Boolean.TRUE.equals(EXTRA_DAMAGE.get()) || !((entity = livingHurtEvent.getSource().m_7639_()) instanceof ServerPlayer) || !(serverPlayer = (ServerPlayer)entity).m_6084_() || serverPlayer.m_5833_() || livingHurtEvent.getEntity() == serverPlayer) {
            return builder.build();
        }
        entity = itemStack == null || itemStack.m_41619_() ? hit.weapon() : itemStack;
        Profile profile = FoodWeaponEvolution.activeProfile((ItemStack)entity);
        LivingEntity livingEntity = livingHurtEvent.getEntity();
        if (profile == null || FoodWeaponLifecycle.isBroken((ItemStack)entity) || !FoodWeaponEvolution.canTarget(serverPlayer, livingEntity)) {
            return builder.build();
        }
        CompoundTag compoundTag = FoodWeaponEvolution.data((ItemStack)entity);
        CompoundTag compoundTag2 = compoundTag == null ? new CompoundTag() : compoundTag.m_6426_();
        CompoundTag compoundTag3 = compoundTag2.m_6426_();
        CompoundTag compoundTag4 = serverPlayer.getPersistentData().m_128469_("FTBossRangedProcTimers");
        for (String string : compoundTag4.m_128431_()) {
            if (!string.startsWith("Last") || !compoundTag4.m_128425_(string, 4)) continue;
            compoundTag3.m_128356_(string, Math.max(compoundTag3.m_128454_(string), compoundTag4.m_128454_(string)));
        }
        CompoundTag compoundTag5 = compoundTag3.m_6426_();
        long l = serverPlayer.m_284548_().m_7654_().m_129783_().m_46467_();
        FoodWeaponEvolution.quoteFoodBlessingHits(builder, serverPlayer, livingEntity, compoundTag3, l, (ItemStack)entity, randomQuotes);
        WeaponRole weaponRole = FoodWeaponEvolution.weaponRole((ItemStack)entity);
        SocketStats socketStats = FoodWeaponEvolution.socketStats((ItemStack)entity);
        double d2 = socketStats.effectPower(FoodType.WINE, weaponRole);
        if (d2 > 0.0 && builder.roll(FoodWeaponSourceLedger.AttemptDomain.SOCKET_WINE_CRIT, Math.min(0.75, d2 * 0.07), randomQuotes)) {
            builder.multiply(1.5f);
        }
        double d3 = socketStats.effectPower(FoodType.VEGETABLE, weaponRole);
        int n = socketStats.count(FoodType.VEGETABLE);
        if (d3 > 0.0 && builder.roll(FoodWeaponSourceLedger.AttemptDomain.SOCKET_VEGETABLE_POISON, Math.min(0.85, d3 * 0.15), randomQuotes)) {
            builder.effect(FoodWeaponSourceLedger.AttemptDomain.SOCKET_VEGETABLE_POISON, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_POISON, livingEntity, new MobEffectInstance(MobEffects.f_19614_, 40 + (int)Math.round(d3 * 20.0), Math.min(1, n - 1)), serverPlayer);
        }
        if ((d = socketStats.effectPower(FoodType.MEAT, weaponRole)) > 0.0 && l - compoundTag3.m_128454_(LAST_SOCKET_PROC) >= 60L && builder.roll(FoodWeaponSourceLedger.AttemptDomain.SOCKET_MEAT_HEAL, 1.0, randomQuotes)) {
            builder.heal(FoodWeaponSourceLedger.AttemptDomain.SOCKET_MEAT_HEAL, (LivingEntity)serverPlayer, (float)(d * 0.35));
            builder.writeAfterWorld(FoodWeaponSourceLedger.AttemptDomain.SOCKET_MEAT_HEAL, compoundTag3, LAST_SOCKET_PROC, (Tag)LongTag.m_128882_((long)l));
        }
        if (socketStats.setCount(FoodType.SWEET) >= 3 && l - compoundTag3.m_128454_("LastSweetGuard") >= 100L && builder.roll(FoodWeaponSourceLedger.AttemptDomain.SOCKET_SWEET_GUARD, 1.0, randomQuotes)) {
            builder.effect(FoodWeaponSourceLedger.AttemptDomain.SOCKET_SWEET_GUARD, null, (LivingEntity)serverPlayer, new MobEffectInstance(MobEffects.f_19617_, 80, socketStats.setTier(FoodType.SWEET) - 1), serverPlayer);
            builder.writeAfterWorld(FoodWeaponSourceLedger.AttemptDomain.SOCKET_SWEET_GUARD, compoundTag3, "LastSweetGuard", (Tag)LongTag.m_128882_((long)l));
        }
        if (socketStats.count(FoodType.ENERGY) >= 3 && l - compoundTag3.m_128454_("LastEnergyArc") >= 40L) {
            FoodWeaponEvolution.quoteChainArc(builder, serverPlayer, livingEntity, 4.0f + 2.0f * (float)(socketStats.setTier(FoodType.ENERGY) - 1), null, compoundTag3, "LastEnergyArc", l);
        }
        if (profile.food == FoodType.CHEESE && socketStats.count(FoodType.CHEESE) >= 3) {
            builder.effect(null, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_WEAKEN, livingEntity, new MobEffectInstance(MobEffects.f_19613_, 80, socketStats.setTier(FoodType.CHEESE) - 1), serverPlayer);
        }
        if (profile.food == FoodType.WINE && socketStats.count(FoodType.WINE) >= 3) {
            builder.effect(null, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_WEAKEN, livingEntity, new MobEffectInstance(MobEffects.f_19604_, 80 + 40 * (socketStats.setTier(FoodType.WINE) - 1), 0), serverPlayer);
        }
        if (FoodWeaponEvolution.isGourmetEvolutionWeapon((ItemStack)entity) && FoodWeaponEvolution.evolutionLevel((ItemStack)entity) > 0) {
            for (AcquiredSkill acquiredSkill : FoodWeaponEvolution.gourmetSkills((ItemStack)entity)) {
                FoodWeaponEvolution.quoteEvolutionSkill(builder, serverPlayer, livingEntity, compoundTag3, acquiredSkill.skill, l, "LastSkillProc." + acquiredSkill.skill.id, (float)FoodWeaponSkillRarity.passiveMultiplier((ItemStack)entity, acquiredSkill.skill.id), randomQuotes);
            }
        } else if (FoodWeaponEvolution.evolutionLevel((ItemStack)entity) > 0) {
            for (EvolutionSkill evolutionSkill : FoodWeaponProgression.passiveSkills((ItemStack)entity)) {
                FoodWeaponEvolution.quoteEvolutionSkill(builder, serverPlayer, livingEntity, compoundTag3, evolutionSkill, l, "LastSkillProc." + evolutionSkill.id, (float)FoodWeaponSkillRarity.passiveMultiplier((ItemStack)entity, evolutionSkill.id), randomQuotes);
            }
        }
        builder.saveHitTimers((ItemStack)entity, profile, serverPlayer, livingHurtEvent.getSource(), compoundTag5, compoundTag3);
        return builder.build();
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void socketKillEffects(LivingDeathEvent livingDeathEvent) {
        if (livingDeathEvent.isCanceled()) {
            return;
        }
        ServerPlayer serverPlayer = FoodWeaponEvolution.resolvePlayer(livingDeathEvent.getSource().m_7639_());
        if (serverPlayer == null || LemonSummonRewards.suppressed((Entity)livingDeathEvent.getEntity())) {
            return;
        }
        ItemStack itemStack = serverPlayer.m_21205_();
        if (FoodWeaponEvolution.boundProfile(itemStack) == null || FoodWeaponLifecycle.isBroken(itemStack)) {
            return;
        }
        WeaponRole weaponRole = FoodWeaponEvolution.weaponRole(itemStack);
        SocketStats socketStats = FoodWeaponEvolution.socketStats(itemStack);
        double d = socketStats.effectPower(FoodType.APPLE, weaponRole);
        int n = socketStats.count(FoodType.MEAT);
        if (d > 0.0 || n >= 3) {
            serverPlayer.m_5634_((float)(d * 2.0 + (n >= 3 ? 2.0 * (double)socketStats.setTier(FoodType.MEAT) : 0.0)));
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void foodBlessingKillFeast(LivingDeathEvent livingDeathEvent) {
        if (livingDeathEvent.isCanceled()) {
            return;
        }
        ServerPlayer serverPlayer = FoodWeaponEvolution.resolvePlayer(livingDeathEvent.getSource().m_7639_());
        if (serverPlayer == null || LemonSummonRewards.suppressed((Entity)livingDeathEvent.getEntity())) {
            return;
        }
        ItemStack itemStack = serverPlayer.m_21205_();
        if (FoodWeaponEvolution.boundProfile(itemStack) == null || FoodWeaponLifecycle.isBroken(itemStack)) {
            return;
        }
        int n = FoodWeaponEvolution.foodBlessingLevel(itemStack, FoodBlessingKind.KILL_FEAST);
        if (n <= 0) {
            return;
        }
        serverPlayer.m_5634_(0.75f + (float)n * 0.5f);
        serverPlayer.m_36324_().m_38707_(1 + n, 0.2f + (float)n * 0.1f);
    }

    private static SocketTier foodTier(ItemStack itemStack) {
        SocketTier socketTier = FoodWeaponEvolution.baseFoodTier(itemStack);
        if (itemStack.m_41782_() && itemStack.m_41783_().m_128425_("quality_food", 10) && itemStack.m_41783_().m_128469_("quality_food").m_128425_("quality", 99)) {
            int n = Math.max(0, Math.min(3, itemStack.m_41783_().m_128469_("quality_food").m_128451_("quality")));
            return SocketTier.byLevel(Math.max(socketTier.level, 1 + n));
        }
        return socketTier;
    }

    static String skillId(EvolutionSkill evolutionSkill) {
        return evolutionSkill.id;
    }

    private static String taskKey(int n) {
        return "Task" + n;
    }

    private static int threshold(ItemStack itemStack, int n) {
        return FoodWeaponBuildRules.evolutionCost(n);
    }

    private static synchronized List<ResourceLocation> dailyFoodPool(ServerPlayer serverPlayer, FoodType foodType) {
        Set<ResourceLocation> set = FoodWeaponEvolution.dailyRecipeOutputs(serverPlayer);
        List<ResourceLocation> list = DAILY_FOOD_POOLS.get((Object)foodType);
        if (list != null) {
            return list;
        }
        ArrayList<ResourceLocation> arrayList = new ArrayList<ResourceLocation>();
        for (ResourceLocation resourceLocation : ForgeRegistries.ITEMS.getKeys()) {
            if (!FoodWeaponEvolution.isDailyFoodCandidate(resourceLocation, foodType, set)) continue;
            arrayList.add(resourceLocation);
        }
        arrayList.sort(ResourceLocation::compareTo);
        List list2 = List.copyOf(arrayList);
        DAILY_FOOD_POOLS.put(foodType, list2);
        return list2;
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 3 -> "III";
            case 2 -> "II";
            default -> "I";
        };
    }

    private static boolean isNamedDrink(String string, FoodType foodType) {
        if (FoodWeaponEvolution.containsFoodWord(string, "seed", "seeds", "leaf", "leaves", "blend", "bag", "crop", "block", "crate", "bucket", "spawn_egg", "powder", "bean", "mace", "charm")) {
            return false;
        }
        return switch (foodType) {
            case FoodType.TEA -> FoodWeaponEvolution.containsFoodWord(string, "tea", "matcha", "oolong", "kombucha", "herbal_infusion");
            case FoodType.WINE -> FoodWeaponEvolution.containsFoodWord(string, "wine", "beer", "ale", "mead", "cider", "liquor", "vodka", "whiskey", "whisky", "brew", "sake", "rum", "stout", "grog", "champagne", "brandy", "bourbon", "absinthe", "tequila", "sangria", "cocktail", "lager", "kvass", "gin_tonic");
            case FoodType.ENERGY -> FoodWeaponEvolution.containsFoodWord(string, "coffee", "espresso", "latte", "cappuccino", "mocha", "hot_cocoa", "cola", "energy_drink");
            default -> false;
        };
    }

    public static int maxEvolutionLevel(ItemStack itemStack) {
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            return 4;
        }
        return FoodWeaponEvolution.maxLevel(itemStack);
    }

    private static Component socketItemName(String string) {
        try {
            ResourceLocation resourceLocation = new ResourceLocation(string);
            Item item = (Item)ForgeRegistries.ITEMS.getValue(resourceLocation);
            return item == null ? Component.m_237113_((String)string) : item.m_41466_();
        }
        catch (RuntimeException runtimeException) {
            return Component.m_237113_((String)string);
        }
    }

    private static boolean isEvolutionExcluded(ItemStack itemStack) {
        if (itemStack.m_41619_()) {
            return false;
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        return resourceLocation != null && CREATE_NAMESPACE.equals(resourceLocation.m_135827_());
    }

    private static boolean containsFoodWord(String string, String ... stringArray) {
        String string2 = "_" + string.replace('/', '_').replace('-', '_').replace('.', '_') + "_";
        for (String string3 : stringArray) {
            if (!string2.contains("_" + string3 + "_")) continue;
            return true;
        }
        return false;
    }

    private static EvolutionSkill drawGourmetSkill(ItemStack itemStack, Profile profile, RandomSource randomSource) {
        List<AcquiredSkill> list = FoodWeaponEvolution.gourmetSkills(itemStack);
        ArrayList<EvolutionSkill> arrayList = new ArrayList<EvolutionSkill>();
        for (EvolutionSkill evolutionSkill : profile.pool) {
            if (!list.stream().noneMatch(acquiredSkill -> acquiredSkill.skill == evolutionSkill)) continue;
            arrayList.add(evolutionSkill);
        }
        if (arrayList.isEmpty()) {
            for (EvolutionSkill evolutionSkill : EvolutionSkill.values()) {
                if (!list.stream().noneMatch(acquiredSkill -> acquiredSkill.skill == evolutionSkill)) continue;
                arrayList.add(evolutionSkill);
            }
        }
        return arrayList.isEmpty() ? null : (EvolutionSkill)((Object)arrayList.get(randomSource.m_188503_(arrayList.size())));
    }

    public static int nextEvolutionRequirement(ItemStack itemStack) {
        int n;
        int n2 = FoodWeaponEvolution.evolutionLevel(itemStack);
        return n2 >= (n = FoodWeaponEvolution.maxLevel(itemStack)) ? 0 : FoodWeaponEvolution.threshold(itemStack, n2);
    }

    private static void quoteFoodBlessingHits(FoodWeaponHitPlan.Builder builder, ServerPlayer serverPlayer, LivingEntity livingEntity, CompoundTag compoundTag, long l, ItemStack itemStack, FoodWeaponHitPlan.RandomQuotes randomQuotes) {
        int n;
        ListTag listTag = FoodWeaponEvolution.foodBlessingList(itemStack);
        String string = "FTBossFoodFortune:" + serverPlayer.m_20148_();
        String string2 = string + ":Level";
        if (livingEntity.getPersistentData().m_128454_(string) >= l) {
            n = Math.max(1, livingEntity.getPersistentData().m_128451_(string2));
            builder.multiply(1.12f + (float)n * 0.06f);
            builder.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_FORTUNE, livingEntity);
            builder.write(null, livingEntity.getPersistentData(), string, null);
            builder.write(null, livingEntity.getPersistentData(), string2, null);
        }
        block26: for (n = 0; n < listTag.size(); ++n) {
            FoodWeaponSourceLedger.AttemptDomain attemptDomain;
            CompoundTag compoundTag2 = listTag.m_128728_(n);
            FoodBlessing foodBlessing = FoodBlessing.byId(compoundTag2.m_128461_("Blessing"));
            if (foodBlessing == null) continue;
            int n2 = FoodWeaponEvolution.foodBlessingLevel(compoundTag2);
            float f = Math.min(0.36f, 0.12f + (float)n2 * 0.05f);
            switch (foodBlessing.kind) {
                case CRIT: 
                case EXECUTE: 
                case ABSORPTION: 
                case CHAIN: 
                case OVERLOAD: 
                case BURN: 
                case MARK: 
                case LIFESTEAL: 
                case POISON: 
                case WITHER: 
                case ROOT: 
                case REGEN: 
                case CORK_RUSH: {
                    FoodWeaponSourceLedger.AttemptDomain attemptDomain2 = FoodWeaponSourceLedger.AttemptDomain.valueOf("BLESSING_" + foodBlessing.kind.name());
                    break;
                }
                default: {
                    FoodWeaponSourceLedger.AttemptDomain attemptDomain2 = attemptDomain = null;
                }
            }
            if ((foodBlessing.kind == FoodBlessingKind.CHAIN || foodBlessing.kind == FoodBlessingKind.OVERLOAD) && FoodWeaponDamageSources.attributeSecondary(serverPlayer) == null) continue;
            boolean bl = attemptDomain != null && builder.roll(attemptDomain, f, randomQuotes);
            switch (foodBlessing.kind) {
                case CRIT: {
                    if (!bl) continue block26;
                    builder.multiply(1.3f + (float)n2 * 0.12f);
                    continue block26;
                }
                case FRENZY: {
                    double d = livingEntity.m_21223_() / Math.max(1.0f, livingEntity.m_21233_());
                    if (!(d <= 0.12 + (double)n2 * 0.05)) continue block26;
                    builder.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_EXECUTE, livingEntity);
                    builder.multiply(1.2f + (float)n2 * 0.12f);
                    continue block26;
                }
                case EXECUTE: {
                    if (!bl) continue block26;
                    builder.effect(attemptDomain, null, (LivingEntity)serverPlayer, new MobEffectInstance(MobEffects.f_19617_, 60 + n2 * 20, n2 - 1, false, false, true), serverPlayer);
                    continue block26;
                }
                case ABSORPTION: {
                    if (!bl) continue block26;
                    FoodWeaponEvolution.quoteChainArc(builder, serverPlayer, livingEntity, 2.0f + (float)n2 * 1.5f, attemptDomain, null, null, 0L);
                    continue block26;
                }
                case CHAIN: {
                    if (!bl) continue block26;
                    FoodWeaponEvolution.quoteOverloadBurst(builder, serverPlayer, livingEntity, 1.5f + (float)n2 * 1.25f, attemptDomain);
                    continue block26;
                }
                case OVERLOAD: {
                    if (!bl) continue block26;
                    builder.fire(attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_FIRE, livingEntity, 2 + n2 * 2);
                    continue block26;
                }
                case BURN: {
                    if (!bl) continue block26;
                    builder.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_FORTUNE, livingEntity);
                    builder.write(attemptDomain, livingEntity.getPersistentData(), string, (Tag)LongTag.m_128882_((long)(l + 100L + (long)n2 * 40L)));
                    builder.write(attemptDomain, livingEntity.getPersistentData(), string2, (Tag)IntTag.m_128679_((int)n2));
                    builder.effect(attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_FORTUNE, livingEntity, new MobEffectInstance(MobEffects.f_19619_, 100 + n2 * 40, 0, false, false, true), serverPlayer);
                    continue block26;
                }
                case PRISMATIC: {
                    String string3 = LAST_FOOD_BLESSING + foodBlessing.id;
                    if (l - compoundTag.m_128454_(string3) < 60L || !builder.roll(FoodWeaponSourceLedger.AttemptDomain.BLESSING_LIFESTEAL, 1.0, randomQuotes)) continue block26;
                    builder.heal(FoodWeaponSourceLedger.AttemptDomain.BLESSING_LIFESTEAL, (LivingEntity)serverPlayer, 0.35f * (float)n2);
                    builder.writeAfterWorld(FoodWeaponSourceLedger.AttemptDomain.BLESSING_LIFESTEAL, compoundTag, string3, (Tag)LongTag.m_128882_((long)l));
                    continue block26;
                }
                case MARK: {
                    if (!bl) continue block26;
                    builder.effect(attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_POISON, livingEntity, new MobEffectInstance(MobEffects.f_19614_, 40 + n2 * 20, Math.min(2, n2 - 1), false, false, true), serverPlayer);
                    continue block26;
                }
                case LIFESTEAL: {
                    if (!bl) continue block26;
                    builder.effect(attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_WITHER, livingEntity, new MobEffectInstance(MobEffects.f_19615_, 30 + n2 * 15, Math.min(1, n2 - 1), false, false, true), serverPlayer);
                    continue block26;
                }
                case POISON: {
                    if (!bl) continue block26;
                    builder.effect(attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_SLOW, livingEntity, new MobEffectInstance(MobEffects.f_19597_, 60 + n2 * 25, Math.min(3, n2), false, false, true), serverPlayer);
                    continue block26;
                }
                case WITHER: {
                    if (!bl) continue block26;
                    builder.effect(attemptDomain, null, (LivingEntity)serverPlayer, new MobEffectInstance(MobEffects.f_19605_, 40 + n2 * 20, n2 - 1, false, false, true), serverPlayer);
                    continue block26;
                }
                case ROOT: {
                    if (!bl) continue block26;
                    builder.effect(attemptDomain, null, (LivingEntity)serverPlayer, new MobEffectInstance(MobEffects.f_19598_, 80 + n2 * 20, n2 - 1, false, false, true), serverPlayer);
                    builder.effect(attemptDomain, null, (LivingEntity)serverPlayer, new MobEffectInstance(MobEffects.f_19596_, 80 + n2 * 20, n2 - 1, false, false, true), serverPlayer);
                    continue block26;
                }
                case REPAIR: {
                    long l2 = compoundTag.m_128454_(LAST_FOOD_BLESSING + foodBlessing.id);
                    boolean bl2 = builder.roll(FoodWeaponSourceLedger.AttemptDomain.BLESSING_FRENZY, 1.0, randomQuotes);
                    if (!bl2) continue block26;
                    int n3 = bl2 ? (l - l2 <= 60L ? Math.min(n2 + 2, compoundTag.m_128451_("CheeseFrenzyStacks") + 1) : 1) : (l - l2 <= 60L ? Math.max(1, compoundTag.m_128451_("CheeseFrenzyStacks")) : 1);
                    builder.multiply(1.0f + (float)n3 * (0.025f + (float)n2 * 0.01f));
                    if (!bl2) continue block26;
                    builder.write(FoodWeaponSourceLedger.AttemptDomain.BLESSING_FRENZY, compoundTag, "CheeseFrenzyStacks", (Tag)IntTag.m_128679_((int)n3));
                    builder.write(FoodWeaponSourceLedger.AttemptDomain.BLESSING_FRENZY, compoundTag, LAST_FOOD_BLESSING + foodBlessing.id, (Tag)LongTag.m_128882_((long)l));
                    continue block26;
                }
                case REGEN: {
                    if (!bl) continue block26;
                    builder.effect(attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_WEAKEN, livingEntity, new MobEffectInstance(MobEffects.f_19613_, 60 + n2 * 20, Math.min(2, n2 - 1), false, false, true), serverPlayer);
                    builder.effect(attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_WEAKEN, livingEntity, new MobEffectInstance(MobEffects.f_19604_, 80 + n2 * 20, 0, false, false, true), serverPlayer);
                    continue block26;
                }
                case EMERGENCY_HEART: {
                    String string4 = LAST_FOOD_BLESSING + foodBlessing.id;
                    if (!itemStack.m_41768_() || l - compoundTag.m_128454_(string4) < (long)Math.max(20, 70 - n2 * 15) || !builder.roll(FoodWeaponSourceLedger.AttemptDomain.BLESSING_REPAIR, 1.0, randomQuotes)) continue block26;
                    builder.repair(FoodWeaponSourceLedger.AttemptDomain.BLESSING_REPAIR, itemStack, n2);
                    builder.writeAfterWorld(FoodWeaponSourceLedger.AttemptDomain.BLESSING_REPAIR, compoundTag, string4, (Tag)LongTag.m_128882_((long)l));
                    continue block26;
                }
                case UNDERTOW: {
                    String string5 = LAST_FOOD_BLESSING + foodBlessing.id;
                    if (!(serverPlayer.m_21223_() <= serverPlayer.m_21233_() * 0.5f) || l - compoundTag.m_128454_(string5) < 100L || !builder.roll(FoodWeaponSourceLedger.AttemptDomain.BLESSING_EMERGENCY_HEART, 1.0, randomQuotes)) continue block26;
                    builder.heal(FoodWeaponSourceLedger.AttemptDomain.BLESSING_EMERGENCY_HEART, (LivingEntity)serverPlayer, 0.75f * (float)n2);
                    builder.effect(FoodWeaponSourceLedger.AttemptDomain.BLESSING_EMERGENCY_HEART, null, (LivingEntity)serverPlayer, new MobEffectInstance(MobEffects.f_19617_, 60, 0, false, false, true), serverPlayer);
                    builder.writeAfterWorld(FoodWeaponSourceLedger.AttemptDomain.BLESSING_EMERGENCY_HEART, compoundTag, string5, (Tag)LongTag.m_128882_((long)l));
                    continue block26;
                }
                case BRINE: {
                    if (!livingEntity.m_20071_()) continue block26;
                    builder.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_UNDERTOW, livingEntity);
                    builder.multiply(1.1f + (float)n2 * 0.05f);
                    builder.effect(null, null, (LivingEntity)serverPlayer, new MobEffectInstance(MobEffects.f_19593_, 60 + n2 * 20, 0, false, false, true), serverPlayer);
                    continue block26;
                }
                case CORK_RUSH: {
                    if (!bl) continue block26;
                    builder.effect(attemptDomain, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_SLOW, livingEntity, new MobEffectInstance(MobEffects.f_19599_, 60 + n2 * 25, Math.min(2, n2 - 1), false, false, true), serverPlayer);
                    continue block26;
                }
                case ISOLATION: {
                    boolean bl3 = livingEntity.m_9236_().m_6443_(LivingEntity.class, livingEntity.m_20191_().m_82400_(4.0), livingEntity2 -> livingEntity2 != livingEntity && FoodWeaponEvolution.canTarget(serverPlayer, livingEntity2)).isEmpty();
                    if (!bl3) continue block26;
                    builder.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_ISOLATION, livingEntity);
                    builder.multiply(1.1f + (float)n2 * 0.05f);
                    continue block26;
                }
                case HUNT: {
                    String string6 = "FTBossFoodHunt:" + serverPlayer.m_20148_();
                    if (livingEntity.getPersistentData().m_128454_(string6) >= l) {
                        builder.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_HUNT, livingEntity);
                        builder.multiply(1.05f + (float)n2 * 0.035f);
                        continue block26;
                    }
                    builder.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_HUNT, livingEntity);
                    builder.write(null, livingEntity.getPersistentData(), string6, (Tag)LongTag.m_128882_((long)(l + 160L + (long)n2 * 40L)));
                    builder.effect(null, FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_HUNT, livingEntity, new MobEffectInstance(MobEffects.f_19619_, 160 + n2 * 40, 0, false, false, true), serverPlayer);
                    builder.effect(null, null, (LivingEntity)serverPlayer, new MobEffectInstance(MobEffects.f_19596_, 60 + n2 * 20, Math.min(1, n2 - 1), false, false, true), serverPlayer);
                    continue block26;
                }
            }
        }
    }

    private static synchronized Set<ResourceLocation> dailyRecipeOutputs(ServerPlayer serverPlayer) {
        RecipeManager recipeManager = serverPlayer.m_284548_().m_7654_().m_129894_();
        int n = 31 * recipeManager.m_44051_().size() + recipeManager.m_44073_().mapToInt(ResourceLocation::hashCode).sum();
        if (recipeManager == dailyRecipeManager && n == dailyRecipeFingerprint) {
            return dailyRecipeOutputs;
        }
        HashSet<ResourceLocation> hashSet = new HashSet<ResourceLocation>();
        for (Recipe recipe : recipeManager.m_44051_()) {
            try {
                ItemStack itemStack = recipe.m_8043_(serverPlayer.m_284548_().m_9598_());
                if (itemStack.m_41619_()) continue;
                hashSet.add(ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_()));
            }
            catch (RuntimeException runtimeException) {}
        }
        hashSet.remove(null);
        dailyRecipeManager = recipeManager;
        dailyRecipeFingerprint = n;
        dailyRecipeOutputs = Set.copyOf(hashSet);
        DAILY_FOOD_POOLS.clear();
        return dailyRecipeOutputs;
    }

    public static SocketView ingredientView(ItemStack itemStack) {
        FoodType foodType = FoodWeaponEvolution.foodType(itemStack);
        if (foodType == null) {
            return null;
        }
        SocketTier socketTier = FoodWeaponEvolution.foodTier(itemStack);
        return new SocketView(itemStack.m_41777_(), foodType.displayName(), socketTier.displayName(), foodType.effectDescription(), foodType.setDescription(), FoodWeaponEvolution.socketColor(foodType), false);
    }

    private static boolean weaponCandidate(ItemStack itemStack) {
        if (AdditionalWeaponUiEligibility.applies(itemStack)) {
            return true;
        }
        return FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack) || FoodWeaponEvolution.nativeProfile(itemStack) != null || FoodWeaponEvolution.boundProfile(itemStack) != null || FoodWeaponEvolution.externalWeaponCandidate(itemStack);
    }

    public static int socketSetTier(ItemStack itemStack, FoodType foodType) {
        return FoodWeaponLifecycle.isBroken(itemStack) ? 0 : FoodWeaponEvolution.socketStats(itemStack).setTier(foodType);
    }

    private static void addWeaponRoleTooltip(List<Component> list, ItemStack itemStack) {
        WeaponRole weaponRole = FoodWeaponEvolution.weaponRole(itemStack);
        list.add((Component)Component.m_237110_((String)"tooltip.ftboss.evolution.role", (Object[])new Object[]{weaponRole.displayName()}).m_130940_(ChatFormatting.GOLD));
        list.add((Component)weaponRole.bonusDescription().m_6881_().m_130940_(ChatFormatting.GRAY));
    }

    @SubscribeEvent
    public static void grindstoneBlessing(GrindstoneEvent.OnPlaceItem onPlaceItem) {
        ItemStack itemStack = onPlaceItem.getTopItem();
        ItemStack itemStack2 = onPlaceItem.getBottomItem();
        ItemStack itemStack3 = ItemStack.f_41583_;
        if (itemStack2.m_41619_() && FoodWeaponEvolution.hasWashableBlessing(itemStack)) {
            itemStack3 = itemStack;
        } else if (itemStack.m_41619_() && FoodWeaponEvolution.hasWashableBlessing(itemStack2)) {
            itemStack3 = itemStack2;
        }
        if (itemStack3.m_41619_()) {
            return;
        }
        ItemStack itemStack4 = itemStack3.m_41777_();
        itemStack4.m_41764_(1);
        int n = FoodWeaponEvolution.washBlessings(itemStack4);
        int n2 = 0;
        Map map = EnchantmentHelper.m_44831_((ItemStack)itemStack4);
        ArrayList<Enchantment> arrayList = new ArrayList<Enchantment>();
        for (Map.Entry enchantment : map.entrySet()) {
            if (((Enchantment)enchantment.getKey()).m_6589_()) continue;
            arrayList.add((Enchantment)enchantment.getKey());
            n2 += ((Integer)enchantment.getValue()).intValue();
        }
        for (Enchantment enchantment : arrayList) {
            map.remove(enchantment);
        }
        EnchantmentHelper.m_44865_((Map)map, (ItemStack)itemStack4);
        itemStack4.m_41742_(0);
        onPlaceItem.setOutput(itemStack4);
        onPlaceItem.setXp(Math.max(1, n * 3 + n2));
    }

    private static int maxLevel(ItemStack itemStack) {
        return FoodWeaponEvolution.isGourmetBlankBlade(itemStack) ? 8 : 4;
    }

    private static CompoundTag writableData(ItemStack itemStack, Profile profile) {
        CompoundTag compoundTag = itemStack.m_41698_(ROOT);
        if (profile != null && !compoundTag.m_128425_(PROFILE, 8)) {
            compoundTag.m_128359_(PROFILE, profile.id);
        }
        return compoundTag;
    }

    private static void showWeapon(ServerPlayer serverPlayer, ItemStack itemStack, Profile profile) {
        CompoundTag compoundTag = FoodWeaponEvolution.writableData(itemStack, profile);
        int n = FoodWeaponEvolution.evolutionLevel(itemStack);
        int n2 = FoodWeaponEvolution.maxLevel(itemStack);
        int n3 = Math.max(0, compoundTag.m_128451_(XP));
        int n4 = n >= n2 ? n3 : FoodWeaponEvolution.threshold(itemStack, n);
        serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.weapon_status", (Object[])new Object[]{itemStack.m_41786_(), n, n2, n3, n4, FoodWeaponEvolution.sockets(itemStack).size(), FoodWeaponEvolution.socketCapacityInternal(itemStack)}).m_130940_(ChatFormatting.AQUA));
        if (n < n2 && n3 >= n4) {
            serverPlayer.m_213846_((Component)Component.m_237115_((String)"message.ftboss.evolution.can_evolve").m_130940_(ChatFormatting.LIGHT_PURPLE));
        }
    }

    private static WeaponRole weaponRole(ItemStack itemStack) {
        WeaponRole weaponRole;
        String string;
        if (itemStack.m_41619_()) {
            return WeaponRole.BALANCED;
        }
        switch (string = FoodWeaponEvolution.itemId(itemStack)) {
            case "ftboss:gourmet_blank_blade": 
            case "ftboss:turkey_legs": 
            case "ftboss:wine_bottle_mace": {
                WeaponRole weaponRole2 = WeaponRole.VANGUARD;
                break;
            }
            case "ftboss:defect_claw": 
            case "ftboss:dog_beating_stick": {
                WeaponRole weaponRole2 = WeaponRole.DUELIST;
                break;
            }
            case "ftboss:barrel_crossbow": 
            case "ftboss:tide_trident": {
                WeaponRole weaponRole2 = WeaponRole.RANGED;
                break;
            }
            case "ftboss:compost_crown": {
                WeaponRole weaponRole2 = WeaponRole.ARCANE;
                break;
            }
            case "ftboss:cheese_wheel_mace": 
            case "ftboss:apple_stem": {
                WeaponRole weaponRole2 = WeaponRole.GUARDIAN;
                break;
            }
            case "ftboss:candy_cane": 
            case "ftboss:blooming_thorns": {
                WeaponRole weaponRole2 = WeaponRole.BALANCED;
                break;
            }
            default: {
                WeaponRole weaponRole2 = weaponRole = null;
            }
        }
        if (weaponRole != null) {
            return weaponRole;
        }
        String string2 = itemStack.m_41720_();
        if (FoodWeaponFamilyBuild.kind(itemStack) == FoodWeaponFamilyRules.Kind.STAFF) {
            return WeaponRole.ARCANE;
        }
        if (string2 instanceof DiggerItem || string2 instanceof ShearsItem) {
            return WeaponRole.TOOL;
        }
        if (string2 instanceof BowItem || string2 instanceof CrossbowItem || string2 instanceof TridentItem) {
            return WeaponRole.RANGED;
        }
        String string3 = string.substring(Math.max(0, string.indexOf(58) + 1)).toLowerCase(Locale.ROOT);
        if (FoodWeaponEvolution.containsFoodWord(string3, "bow", "crossbow", "trident", "gun", "rifle", "pistol", "musket", "launcher")) {
            return WeaponRole.RANGED;
        }
        if (FoodWeaponEvolution.containsFoodWord(string3, "staff", "wand", "scepter", "spellbook", "tome", "focus", "grimoire")) {
            return WeaponRole.ARCANE;
        }
        if (FoodWeaponEvolution.containsFoodWord(string3, "pickaxe", "shovel", "hoe", "mattock", "drill", "saw", "wrench", "excavator")) {
            return WeaponRole.TOOL;
        }
        if (FoodWeaponEvolution.containsFoodWord(string3, "shield", "bulwark", "tower", "guardian")) {
            return WeaponRole.GUARDIAN;
        }
        if (FoodWeaponEvolution.containsFoodWord(string3, "dagger", "rapier", "claw", "knife", "katana", "sabre", "saber", "scimitar")) {
            return WeaponRole.DUELIST;
        }
        if (FoodWeaponEvolution.containsFoodWord(string3, "hammer", "greataxe", "greatsword", "cleaver", "mace", "maul")) {
            return WeaponRole.VANGUARD;
        }
        Multimap multimap = string2.m_7167_(EquipmentSlot.MAINHAND);
        double d = multimap.get((Object)Attributes.f_22281_).stream().mapToDouble(AttributeModifier::m_22218_).sum();
        double d2 = multimap.get((Object)Attributes.f_22283_).stream().mapToDouble(AttributeModifier::m_22218_).sum();
        if (d >= 8.0 && d2 <= -2.7) {
            return WeaponRole.VANGUARD;
        }
        if (d2 >= -2.2) {
            return WeaponRole.DUELIST;
        }
        return WeaponRole.BALANCED;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static int socketTypeCount(ItemStack itemStack, FoodType foodType) {
        if (itemStack == null) return 0;
        if (foodType == null) return 0;
        if (itemStack.m_41619_()) {
            return 0;
        }
        if (FourWeaponFoundationAdapter.applies(itemStack) || itemStack.m_41737_("FTBossFourWeaponFoundation") != null) {
            FourWeaponFoundationAdapter.View view = FourWeaponFoundationAdapter.read(itemStack);
            if (itemStack.m_41613_() != 1) return 0;
            if (!view.valid()) return 0;
            if (!view.persisted()) return 0;
            if (view.level() < 0) return 0;
            if (view.level() > 4) return 0;
            int n = 0;
            for (Map.Entry<Integer, FourWeaponFoundationAdapter.Entry> entry : view.entries().entrySet()) {
                Optional<SwordMaterialDefinition> optional;
                ItemStack itemStack2;
                FourWeaponFoundationAdapter.Entry entry2 = entry.getValue();
                if (!entry2.paid() || !FourWeaponFoundationRules.unlocked(view.profile(), entry.getKey(), view.level()) || (itemStack2 = entry2.storedItem()).m_41619_() || itemStack2.m_41613_() != 1 || !(optional = SwordMaterialExtensions.resolve(itemStack2)).isPresent() || !optional.get().categories().contains((Object)SwordMaterialDefinition.Category.FOOD) || FoodWeaponEvolution.foodType(itemStack2) != foodType) continue;
                ++n;
            }
            if (n == 0) return n;
            if (!view.status().ready()) return 0;
            if (FourWeaponUseGuard.blocksUse(itemStack)) return 0;
            if (FoodWeaponLifecycle.isBroken(itemStack)) return 0;
            if (!FoodWeaponMaterialFingerprint.foundationSnapshot(itemStack).m_128456_()) return n;
            return 0;
        }
        if (FoodWeaponLifecycle.isBroken(itemStack)) {
            return 0;
        }
        int n = FoodWeaponEvolution.socketStats(itemStack).count(foodType);
        return n;
    }

    static EvolutionSkill drawStageSkill(Profile profile, Set<EvolutionSkill> set, RandomSource randomSource) {
        ArrayList<EvolutionSkill> arrayList = new ArrayList<EvolutionSkill>();
        for (EvolutionSkill evolutionSkill : profile.pool) {
            if (set.contains((Object)evolutionSkill)) continue;
            arrayList.add(evolutionSkill);
        }
        if (arrayList.isEmpty()) {
            for (EvolutionSkill evolutionSkill : EvolutionSkill.values()) {
                if (set.contains((Object)evolutionSkill)) continue;
                arrayList.add(evolutionSkill);
            }
        }
        return arrayList.isEmpty() ? null : (EvolutionSkill)((Object)arrayList.get(randomSource.m_188503_(arrayList.size())));
    }

    private static int foodBlessingLevel(ItemStack itemStack, FoodBlessingKind foodBlessingKind) {
        return new FoodBlessingStats(itemStack).level(foodBlessingKind);
    }

    private static CompoundTag createDailyTask(ServerPlayer serverPlayer, Profile profile, long l, int n, DailyRewardType dailyRewardType, List<ResourceLocation> list, List<ResourceLocation> list2) {
        int n2;
        long l2;
        CompoundTag compoundTag = new CompoundTag();
        if (!list.isEmpty()) {
            l2 = l * 12L + (long)n;
            ResourceLocation resourceLocation = null;
            for (n2 = 0; n2 < list.size(); ++n2) {
                ResourceLocation resourceLocation2 = list.get(FoodWeaponEvolution.dailyPoolIndex(serverPlayer, profile, l2 + (long)n2, list.size()));
                if (list2.contains(resourceLocation2) && list2.size() < list.size()) continue;
                resourceLocation = resourceLocation2;
                break;
            }
            if (resourceLocation == null) {
                resourceLocation = list.get(FoodWeaponEvolution.dailyPoolIndex(serverPlayer, profile, l2, list.size()));
            }
            compoundTag.m_128359_(DAILY_REQUEST, resourceLocation.toString());
        }
        l2 = FoodWeaponEvolution.dailySeed(serverPlayer, profile, l) ^ ((long)n + 1L) * -3335678366873096957L;
        int n3 = 4 + FoodWeaponEvolution.positiveMod(FoodWeaponEvolution.mix64(l2), 4) * 2;
        n2 = FoodWeaponEvolution.positiveMod(FoodWeaponEvolution.mix64(l2 ^ 0x94D049BB133111EBL), 4);
        int n4 = switch (dailyRewardType) {
            default -> throw new IncompatibleClassChangeError();
            case DailyRewardType.EVOLUTION -> 25 + n2 * 5;
            case DailyRewardType.EXPERIENCE -> 900 + n2 * 300;
            case DailyRewardType.COINS -> 180 + n2 * 60;
        };
        compoundTag.m_128405_(DAILY_REQUIRED, n3);
        compoundTag.m_128359_(DAILY_REWARD_TYPE, dailyRewardType.id);
        compoundTag.m_128405_(DAILY_REWARD_AMOUNT, n4);
        return compoundTag;
    }

    static void completeEvolution(ServerPlayer serverPlayer, Profile profile, ItemStack itemStack) {
        Object object;
        ListTag listTag;
        int n;
        CompoundTag compoundTag = FoodWeaponEvolution.writableData(itemStack, profile);
        int n2 = Math.max(0, compoundTag.m_128451_(LEVEL));
        if (n2 >= (n = FoodWeaponEvolution.maxLevel(itemStack))) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.maxed"), true);
            return;
        }
        int n3 = FoodWeaponEvolution.threshold(itemStack, n2);
        int n4 = Math.max(0, compoundTag.m_128451_(XP));
        if (n4 < n3) {
            serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.evolution.need_xp", (Object[])new Object[]{n4, n3}), true);
            return;
        }
        FoodWeaponRepair.invalidateForProgression(itemStack);
        compoundTag.m_128405_(LEVEL, ++n2);
        compoundTag.m_128473_(BLESSING_WASHED);
        boolean bl = FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack);
        if (!bl && n2 == 2) {
            compoundTag.m_128359_(SECOND_PROFILE, profile.id);
        }
        EvolutionSkill evolutionSkill = null;
        EvolutionStat evolutionStat = null;
        if (bl) {
            compoundTag.m_128359_(GOURMET_ACTIVE_PROFILE, profile.id);
            evolutionSkill = FoodWeaponEvolution.drawGourmetSkill(itemStack, profile, serverPlayer.m_217043_());
            if (evolutionSkill != null) {
                listTag = compoundTag.m_128437_(GOURMET_SKILLS, 10);
                object = new CompoundTag();
                object.m_128359_(PROFILE, profile.id);
                object.m_128359_(SKILL, evolutionSkill.id);
                listTag.add(object);
                compoundTag.m_128365_(GOURMET_SKILLS, (Tag)listTag);
                compoundTag.m_128405_(GOURMET_ACTIVE_SKILL, Math.max(0, FoodWeaponEvolution.gourmetSkills(itemStack).size() - 1));
            }
            evolutionStat = EvolutionStat.random(FoodWeaponEvolution.weaponRole(itemStack), serverPlayer.m_217043_());
            listTag = compoundTag.m_128469_(GOURMET_STATS);
            listTag.m_128347_(evolutionStat.id, Math.max(0.0, listTag.m_128459_(evolutionStat.id)) + evolutionStat.gain);
            compoundTag.m_128365_(GOURMET_STATS, (Tag)listTag);
            GourmetEvolutionWeapon.animateEvolution(serverPlayer, itemStack);
        }
        FoodWeaponEvolution.playEvolutionSuccessParticles(serverPlayer, bl);
        serverPlayer.m_284548_().m_5594_(null, serverPlayer.m_20183_(), SoundEvents.f_11887_, SoundSource.PLAYERS, 1.0f, 0.75f + (float)Math.min(n2, 5) * 0.12f);
        if (evolutionSkill != null) {
            listTag = evolutionSkill.nameComponent();
        } else {
            WeaponEvolutionSkillPool.Aspect aspect;
            WeaponEvolutionSkillPool.Fusion fusion;
            object = WeaponEvolutionSkillPool.Aspect.fromId(FoodWeaponEvolution.boundProfile((ItemStack)itemStack).id);
            listTag = n2 == 1 || object == null ? (object == null ? Component.m_237115_((String)"message.ftboss.evolution.native_skill_up") : Component.m_237115_((String)((WeaponEvolutionSkillPool.Aspect)((Object)object)).translationKey())) : ((fusion = WeaponEvolutionSkillPool.fusion((WeaponEvolutionSkillPool.Aspect)((Object)object), aspect = WeaponEvolutionSkillPool.Aspect.fromId(profile.id))) == null ? Component.m_237115_((String)"message.ftboss.evolution.native_skill_up") : Component.m_237110_((String)"message.ftboss.evolution.fusion_unlock", (Object[])new Object[]{Component.m_237115_((String)fusion.primaryTranslationKey()), Component.m_237115_((String)fusion.secondaryTranslationKey())}));
        }
        serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.evolution.evolved", (Object[])new Object[]{itemStack.m_41786_(), n2, listTag, FoodWeaponEvolution.socketCapacityInternal(itemStack)}).m_130940_(ChatFormatting.LIGHT_PURPLE));
        if (bl && evolutionStat != null) {
            serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.gourmet.stat_gained", (Object[])new Object[]{evolutionStat.displayName(evolutionStat.gain)}).m_130940_(ChatFormatting.GOLD));
        }
        FTBossMod.LOGGER.info("[FTBoss][\u98df\u6b66\u8fdb\u5316] {} \u7684 {} \u5728 {} \u77f3\u50cf\u8fdb\u5316\u5230 {} \u7ea7, skill={}, stat={}, slots={}", new Object[]{serverPlayer.m_7755_().getString(), FoodWeaponEvolution.itemId(itemStack), profile.id, n2, evolutionSkill == null ? "native+1" : evolutionSkill.id, evolutionStat == null ? "none" : evolutionStat.id, FoodWeaponEvolution.socketCapacityInternal(itemStack)});
    }

    private static String formatNumber(double d) {
        if (Math.abs(d - Math.rint(d)) < 1.0E-4) {
            return Integer.toString((int)Math.rint(d));
        }
        return String.format(Locale.ROOT, "%.2f", d).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    public static boolean evolveWeaponFromUi(ServerPlayer serverPlayer, int n, int n2) {
        return FoodWeaponProgression.evolve(serverPlayer, n, n2);
    }

    static {
        GOURMET_THRESHOLDS = new int[]{100, 250, 450, 700, 1000, 1350, 1750, 2200};
        COIN_ID = ResourceLocation.m_135820_((String)"kubejs:coin");
        EXTRA_DAMAGE = ThreadLocal.withInitial(() -> false);
        DAILY_FOOD_POOLS = new EnumMap(FoodType.class);
        DAILY_NATURAL_FOOD_TAGS = List.of(FoodWeaponEvolution.dailyFoodTag("forge:crops"), FoodWeaponEvolution.dailyFoodTag("forge:fruits"), FoodWeaponEvolution.dailyFoodTag("forge:berries"), FoodWeaponEvolution.dailyFoodTag("forge:vegetables"), FoodWeaponEvolution.dailyFoodTag("forge:raw_meats"), FoodWeaponEvolution.dailyFoodTag("forge:raw_fishes"));
        VANILLA_NATURAL_DAILY_FOODS = Set.of("apple", "beef", "porkchop", "chicken", "mutton", "rabbit", "cod", "salmon", "tropical_fish", "pufferfish", "carrot", "potato", "beetroot", "melon_slice", "sweet_berries", "glow_berries", "chorus_fruit", "honey_bottle");
        dailyRecipeFingerprint = Integer.MIN_VALUE;
        dailyRecipeOutputs = Set.of();
        EVOLUTION_DAMAGE = UUID.fromString("4c678802-b15b-4a2a-b399-d6559081bd1f");
        EVOLUTION_SPEED = UUID.fromString("2ae62943-4400-4296-ad06-efec0d0e98a9");
        SOCKET_ARMOR = UUID.fromString("be787aed-adb4-46ec-a466-f64dd529a11c");
        SOCKET_TOUGHNESS = UUID.fromString("c5ddd11c-b0db-4503-ab92-b53f0302809f");
        SOCKET_HEALTH = UUID.fromString("b14aec63-216e-46d9-8c31-b66495b2f087");
        SOCKET_MOVEMENT = UUID.fromString("80a6b7ea-22cd-4c99-a32e-e825d83f3fe7");
        SOCKET_LUCK = UUID.fromString("13fb758d-c241-4071-a7d4-a8e0f74e6258");
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void useStatue(PlayerInteractEvent.RightClickBlock rightClickBlock) {
    }

    private static ServerPlayer resolvePlayer(Entity entity) {
        Projectile projectile;
        Entity entity2;
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            return serverPlayer;
        }
        if (entity instanceof Projectile && (entity2 = (projectile = (Projectile)entity).m_19749_()) instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity2;
            return serverPlayer;
        }
        return null;
    }

    private static boolean hasWashableBlessing(ItemStack itemStack) {
        CompoundTag compoundTag = FoodWeaponEvolution.data(itemStack);
        if (compoundTag == null) {
            return false;
        }
        if (compoundTag.m_128451_(XP) > 0 || compoundTag.m_128451_(LEVEL) > 0) {
            return true;
        }
        if (compoundTag.m_128425_(SKILL, 8)) {
            return true;
        }
        if (compoundTag.m_128425_(GOURMET_SKILLS, 9) && !compoundTag.m_128437_(GOURMET_SKILLS, 10).isEmpty()) {
            return true;
        }
        if (compoundTag.m_128425_(FOOD_BLESSINGS, 9) && !compoundTag.m_128437_(FOOD_BLESSINGS, 10).isEmpty()) {
            return true;
        }
        return compoundTag.m_128425_(GOURMET_STATS, 10) && !compoundTag.m_128469_(GOURMET_STATS).m_128431_().isEmpty();
    }

    private static void addCurrentSocketEffects(List<Component> list, ItemStack itemStack, Profile profile) {
        list.add((Component)Component.m_237115_((String)"tooltip.ftboss.socket.current").m_130940_(ChatFormatting.GOLD));
        boolean bl = false;
        WeaponRole weaponRole = FoodWeaponEvolution.weaponRole(itemStack);
        SocketStats socketStats = FoodWeaponEvolution.socketStats(itemStack);
        for (FoodType foodType : FoodType.values()) {
            int n = socketStats.count(foodType);
            if (n <= 0) continue;
            double d = socketStats.effectPower(foodType, weaponRole);
            list.add((Component)Component.m_237110_((String)"tooltip.ftboss.socket.effect_line", (Object[])new Object[]{foodType.displayName(), n, FoodWeaponEvolution.formatNumber(d), foodType.effectDescription()}).m_130940_(ChatFormatting.GRAY));
            if (foodType == profile.food) {
                list.add((Component)Component.m_237110_((String)"tooltip.ftboss.socket.matching", (Object[])new Object[]{profile.displayName(), FoodWeaponEvolution.formatNumber(d * 0.5), FoodWeaponEvolution.formatNumber(d)}).m_130940_(ChatFormatting.YELLOW));
            }
            if ((foodType == FoodType.SWEET || foodType == FoodType.FOOD) && bl) continue;
            if (foodType == FoodType.SWEET || foodType == FoodType.FOOD) {
                bl = true;
            }
            int n2 = Math.min(3, socketStats.setCount(foodType));
            SocketTier socketTier = SocketTier.byLevel(socketStats.setTier(foodType));
            list.add((Component)Component.m_237110_((String)"tooltip.ftboss.socket.set_progress", (Object[])new Object[]{n2, socketTier.displayName(), foodType.setDescription()}).m_130940_(n2 >= 3 ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
        }
        list.add((Component)Component.m_237115_((String)"tooltip.ftboss.socket.remove_hint").m_130940_(ChatFormatting.DARK_GRAY));
    }

    public static ItemStack heldEvolutionWeapon(ServerPlayer serverPlayer) {
        ItemStack itemStack = serverPlayer.m_21205_();
        if (FoodWeaponEvolution.isEvolutionCandidate(itemStack)) {
            return itemStack;
        }
        ItemStack itemStack2 = serverPlayer.m_21206_();
        return FoodWeaponEvolution.isEvolutionCandidate(itemStack2) ? itemStack2 : ItemStack.f_41583_;
    }

    public static boolean applyFoodBlessingFromUi(ServerPlayer serverPlayer, int n, int n2) {
        Profile profile = Profile.byTint(n);
        ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
        if (profile == null || itemStack.m_41619_() || n2 < 0 || n2 > 40 || n2 >= 36 && n2 < 40) {
            return false;
        }
        if (itemStack.m_150930_(Items.f_42690_)) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.food_blessing.no_books"), true);
            return false;
        }
        ItemStack itemStack2 = serverPlayer.m_150109_().m_8020_(n2);
        FoodType foodType = FoodWeaponEvolution.foodType(itemStack2);
        if (itemStack2.m_41619_() || itemStack2 == itemStack || FoodWeaponEvolution.weaponCandidate(itemStack2) || foodType == null) {
            serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.food_blessing.wrong_food", (Object[])new Object[]{profile.food.displayName()}), true);
            return false;
        }
        return FoodWeaponEvolution.applyFoodBlessing(serverPlayer, profile, itemStack, itemStack2, foodType);
    }

    public static int skillCooldown(ItemStack itemStack, int n) {
        if (FoodWeaponEvolution.isEvolutionExcluded(itemStack)) {
            return n;
        }
        WeaponRole weaponRole = FoodWeaponEvolution.weaponRole(itemStack);
        double d = (double)FoodWeaponEvolution.evolutionLevel(itemStack) * weaponRole.cooldownReductionPerLevel(FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack));
        double d2 = d + FoodWeaponEvolution.socketStats(itemStack).effectPower(FoodType.TEA, weaponRole) * 0.05;
        return Math.max(20, (int)Math.round((double)n * Math.max(0.55, 1.0 - d2)));
    }

    private static boolean validDailyTask(CompoundTag compoundTag, List<ResourceLocation> list) {
        if (compoundTag.m_128456_() || compoundTag.m_128451_(DAILY_REQUIRED) <= 0 || compoundTag.m_128451_(DAILY_REWARD_AMOUNT) <= 0 || DailyRewardType.byId(compoundTag.m_128461_(DAILY_REWARD_TYPE)) == null) {
            return false;
        }
        String string = compoundTag.m_128461_(DAILY_REQUEST);
        if (string.isBlank()) {
            return list.isEmpty();
        }
        ResourceLocation resourceLocation = ResourceLocation.m_135820_((String)string);
        return resourceLocation != null && list.contains(resourceLocation);
    }

    private static FoodType foodType(ItemStack itemStack) {
        if (itemStack.m_41619_()) {
            return null;
        }
        String string = FoodWeaponEvolution.itemId(itemStack).toLowerCase(Locale.ROOT);
        FoodType foodType = FoodWeaponEvolution.explicitOfferingType(string);
        return foodType != null ? foodType : FoodSocketFoodCatalog.type(itemStack);
    }

    private static List<Socket> sockets(ItemStack itemStack) {
        ArrayList<Socket> arrayList = new ArrayList<Socket>();
        CompoundTag compoundTag = FoodWeaponEvolution.data(itemStack);
        if (compoundTag == null || !compoundTag.m_128425_(SOCKETS, 9)) {
            return arrayList;
        }
        ListTag listTag = compoundTag.m_128437_(SOCKETS, 10);
        int n = FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack) ? 30 : 10;
        for (int i = 0; i < listTag.size() && i < n; ++i) {
            CompoundTag compoundTag2 = listTag.m_128728_(i);
            try {
                FoodType foodType = FoodType.valueOf(compoundTag2.m_128461_("Type"));
                String string = compoundTag2.m_128461_("Item");
                foodType = FoodSocketFoodCatalog.savedType(foodType, string);
                int n2 = compoundTag2.m_128425_("Tier", 3) ? compoundTag2.m_128451_("Tier") : FoodWeaponEvolution.foodTierById((String)string).level;
                arrayList.add(new Socket(foodType, string, SocketTier.byLevel(n2)));
                continue;
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        return arrayList;
    }

    public static enum Profile {
        SUGAR("sugar", 1, FoodType.SWEET, EvolutionSkill.FROST_BITE, EvolutionSkill.SUGAR_RUSH, EvolutionSkill.CANDY_GUARD, EvolutionSkill.GOLDEN_PEEL, EvolutionSkill.TEA_RECOVERY, EvolutionSkill.HIGH_VOLTAGE),
        BUTCHER("butcher", 2, FoodType.MEAT, EvolutionSkill.BLOOD_FEAST, EvolutionSkill.CLEAVER_FORCE, EvolutionSkill.HEAVY_IMPACT, EvolutionSkill.SHEPHERD_MIGHT, EvolutionSkill.PLAGUE_BITE, EvolutionSkill.CORK_BURST),
        SAYA("saya", 3, FoodType.TEA, EvolutionSkill.THORN_VENOM, EvolutionSkill.TEA_RECOVERY, EvolutionSkill.ROOTED_GRIP, EvolutionSkill.FERTILE_REGROWTH, EvolutionSkill.PRISMATIC_TOXIN, EvolutionSkill.BRINE_BITE),
        WINE("wine", 4, FoodType.WINE, EvolutionSkill.VINTAGE_FURY, EvolutionSkill.TIPSY_GUARD, EvolutionSkill.CORK_BURST, EvolutionSkill.HIGH_VOLTAGE, EvolutionSkill.SCAVENGER_FEAST, EvolutionSkill.HEAVY_IMPACT),
        RAT("rat", 5, FoodType.CHEESE, EvolutionSkill.CHEESE_FRENZY, EvolutionSkill.PLAGUE_BITE, EvolutionSkill.SCAVENGER_FEAST, EvolutionSkill.BLOOD_FEAST, EvolutionSkill.PRISMATIC_TOXIN, EvolutionSkill.GOLDEN_PEEL),
        DEFECT("defect", 6, FoodType.ENERGY, EvolutionSkill.OVERLOAD_ARC, EvolutionSkill.HIGH_VOLTAGE, EvolutionSkill.MAGNETIC_LOCK, EvolutionSkill.VINTAGE_FURY, EvolutionSkill.CHEESE_FRENZY, EvolutionSkill.ROOTED_GRIP),
        COMPOST("compost", 7, FoodType.VEGETABLE, EvolutionSkill.PRISMATIC_TOXIN, EvolutionSkill.RECYCLE_GUARD, EvolutionSkill.FERTILE_REGROWTH, EvolutionSkill.TEA_RECOVERY, EvolutionSkill.HONEYCRISP_REBIRTH, EvolutionSkill.PLAGUE_BITE),
        APPLE("apple", 8, FoodType.APPLE, EvolutionSkill.HONEYCRISP_REBIRTH, EvolutionSkill.GOLDEN_PEEL, EvolutionSkill.ORCHARD_STRIKE, EvolutionSkill.FERTILE_REGROWTH, EvolutionSkill.CANDY_GUARD, EvolutionSkill.CLEAVER_FORCE),
        TIDE("tide", 9, FoodType.FISH, EvolutionSkill.UNDERTOW, EvolutionSkill.BRINE_BITE, EvolutionSkill.TIDAL_RECOVERY, EvolutionSkill.ROOTED_GRIP, EvolutionSkill.TIPSY_GUARD, EvolutionSkill.PACK_FEAST),
        BENGER("benger", 10, FoodType.MEAT, EvolutionSkill.SHEPHERD_MIGHT, EvolutionSkill.HUNTING_HOWL, EvolutionSkill.PACK_FEAST, EvolutionSkill.BLOOD_FEAST, EvolutionSkill.ORCHARD_STRIKE, EvolutionSkill.UNDERTOW);

        private final String id;
        private final int tint;
        private final FoodType food;
        private final EvolutionSkill[] pool;

        private Profile(String id, int tint, FoodType food, EvolutionSkill ... pool) {
            this.id = id;
            this.tint = tint;
            this.food = food;
            this.pool = pool;
        }

        Component displayName() {
            return Component.m_237115_((String)("profile.ftboss.evolution." + this.id));
        }

        static Profile byId(String id) {
            for (Profile profile : Profile.values()) {
                if (!profile.id.equals(id)) continue;
                return profile;
            }
            return null;
        }

        static Profile byTint(int tint) {
            for (Profile profile : Profile.values()) {
                if (profile.tint != tint) continue;
                return profile;
            }
            return null;
        }
    }

    private static enum FoodBlessing {
        CANDIED_EDGE("candied_edge", FoodBlessingKind.EXECUTE),
        SYRUP_STEP("syrup_step", FoodBlessingKind.ATTACK_SPEED),
        FROSTED_SHELL("frosted_shell", FoodBlessingKind.ABSORPTION),
        CLEAVER_MIGHT("cleaver_might", FoodBlessingKind.ATTACK_DAMAGE),
        BUTCHER_HIDE("butcher_hide", FoodBlessingKind.LOW_HEALTH_GUARD),
        BLOOD_FEAST("blood_feast", FoodBlessingKind.LIFESTEAL),
        TEA_THORNS("tea_thorns", FoodBlessingKind.POISON),
        CAMELLIA_GRACE("camellia_grace", FoodBlessingKind.REGEN),
        ROOTED_FOCUS("rooted_focus", FoodBlessingKind.ROOT),
        VINTAGE_CRIT("vintage_crit", FoodBlessingKind.CRIT),
        BARREL_PLATE("barrel_plate", FoodBlessingKind.PROJECTILE_GUARD),
        CORK_RUSH("cork_rush", FoodBlessingKind.CORK_RUSH),
        CHEESE_FRENZY("cheese_frenzy", FoodBlessingKind.FRENZY),
        PLAGUE_TOOTH("plague_tooth", FoodBlessingKind.WITHER),
        SCAVENGER_LUCK("scavenger_luck", FoodBlessingKind.CHAIN),
        OVERLOAD_DAMAGE("overload_damage", FoodBlessingKind.OVERLOAD),
        VOLT_STEP("volt_step", FoodBlessingKind.MOVEMENT_SPEED),
        MAGNETIC_PLATE("magnetic_plate", FoodBlessingKind.KNOCKBACK_RESISTANCE),
        PRISMATIC_VENOM("prismatic_venom", FoodBlessingKind.PRISMATIC),
        RECYCLE_HEART("recycle_heart", FoodBlessingKind.MAX_HEALTH),
        FERTILE_HASTE("fertile_haste", FoodBlessingKind.REPAIR),
        HONEYCRISP_HEART("honeycrisp_heart", FoodBlessingKind.EMERGENCY_HEART),
        GOLDEN_FORTUNE("golden_fortune", FoodBlessingKind.MARK),
        ORCHARD_EDGE("orchard_edge", FoodBlessingKind.BURN),
        UNDERTOW_STEP("undertow_step", FoodBlessingKind.UNDERTOW),
        BRINE_SLOW("brine_slow", FoodBlessingKind.BRINE),
        TIDAL_BULWARK("tidal_bulwark", FoodBlessingKind.WET_GUARD),
        SHEPHERD_MIGHT("shepherd_might", FoodBlessingKind.ISOLATION),
        HUNTING_STEP("hunting_step", FoodBlessingKind.HUNT),
        PACK_FEAST("pack_feast", FoodBlessingKind.KILL_FEAST);

        private static final Map<String, FoodBlessing> BY_ID;
        private final String id;
        private final FoodBlessingKind kind;
        private final UUID modifierId;

        private FoodBlessing(String id, FoodBlessingKind kind) {
            this.id = id;
            this.kind = kind;
            this.modifierId = UUID.nameUUIDFromBytes(("ftboss.food_blessing." + id).getBytes(StandardCharsets.UTF_8));
        }

        Component nameComponent() {
            return Component.m_237115_((String)("food_blessing.ftboss." + this.id));
        }

        Component descriptionComponent(int level) {
            return Component.m_237110_((String)("tooltip.ftboss.food_blessing." + this.id), (Object[])new Object[]{level});
        }

        static FoodBlessing byId(String id) {
            return id == null ? null : BY_ID.get(id);
        }

        static {
            HashMap<String, FoodBlessing> byId = new HashMap<String, FoodBlessing>();
            for (FoodBlessing blessing : FoodBlessing.values()) {
                byId.put(blessing.id, blessing);
            }
            BY_ID = Map.copyOf(byId);
        }
    }

    private static enum WeaponRole {
        VANGUARD("vanguard"),
        DUELIST("duelist"),
        RANGED("ranged"),
        ARCANE("arcane"),
        GUARDIAN("guardian"),
        TOOL("tool"),
        BALANCED("balanced");

        private final String id;

        private WeaponRole(String id) {
            this.id = id;
        }

        Component displayName() {
            return Component.m_237115_((String)("role.ftboss.weapon." + this.id));
        }

        Component bonusDescription() {
            return Component.m_237115_((String)("tooltip.ftboss.evolution.role_bonus." + this.id));
        }

        boolean specializes(FoodType type) {
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case VANGUARD -> {
                    if (type == FoodType.MEAT || type == FoodType.WINE || type == FoodType.ENERGY) {
                        yield true;
                    }
                    yield false;
                }
                case DUELIST -> {
                    if (type == FoodType.TEA || type == FoodType.FISH || type == FoodType.ENERGY) {
                        yield true;
                    }
                    yield false;
                }
                case RANGED -> {
                    if (type == FoodType.WINE || type == FoodType.FISH || type == FoodType.ENERGY) {
                        yield true;
                    }
                    yield false;
                }
                case ARCANE -> {
                    if (type == FoodType.TEA || type == FoodType.SWEET || type == FoodType.FOOD || type == FoodType.ENERGY) {
                        yield true;
                    }
                    yield false;
                }
                case GUARDIAN -> {
                    if (type == FoodType.CHEESE || type == FoodType.VEGETABLE || type == FoodType.APPLE) {
                        yield true;
                    }
                    yield false;
                }
                case TOOL -> {
                    if (type == FoodType.TEA || type == FoodType.ENERGY || type == FoodType.VEGETABLE) {
                        yield true;
                    }
                    yield false;
                }
                case BALANCED -> true;
            };
        }

        double socketMultiplier(FoodType type) {
            return this == BALANCED ? 1.1 : (this.specializes(type) ? 1.25 : 1.0);
        }

        float skillPowerPerLevel(boolean gourmet) {
            if (gourmet) {
                return switch (this) {
                    default -> throw new IncompatibleClassChangeError();
                    case VANGUARD -> 0.06f;
                    case DUELIST -> 0.07f;
                    case RANGED -> 0.08f;
                    case ARCANE -> 0.14f;
                    case GUARDIAN -> 0.06f;
                    case TOOL -> 0.08f;
                    case BALANCED -> 0.1f;
                };
            }
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case VANGUARD -> 0.18f;
                case DUELIST -> 0.18f;
                case RANGED -> 0.2f;
                case ARCANE -> 0.25f;
                case GUARDIAN -> 0.16f;
                case TOOL -> 0.18f;
                case BALANCED -> 0.2f;
            };
        }

        double cooldownReductionPerLevel(boolean gourmet) {
            if (gourmet) {
                return switch (this) {
                    default -> throw new IncompatibleClassChangeError();
                    case VANGUARD, GUARDIAN -> 0.03;
                    case DUELIST, RANGED, TOOL, BALANCED -> 0.04;
                    case ARCANE -> 0.045;
                };
            }
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case VANGUARD, GUARDIAN -> 0.08;
                case DUELIST -> 0.12;
                case RANGED, TOOL, BALANCED -> 0.1;
                case ARCANE -> 0.14;
            };
        }

        int statWeight(EvolutionStat stat) {
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case VANGUARD -> {
                    switch (stat) {
                        default: {
                            throw new IncompatibleClassChangeError();
                        }
                        case ATTACK_DAMAGE: {
                            yield 6;
                        }
                        case ATTACK_SPEED: {
                            yield 2;
                        }
                        case ARMOR: {
                            yield 1;
                        }
                        case MAX_HEALTH: {
                            yield 2;
                        }
                        case MOVEMENT_SPEED: 
                        case LUCK: 
                    }
                    yield 1;
                }
                case DUELIST -> {
                    switch (stat) {
                        default: {
                            throw new IncompatibleClassChangeError();
                        }
                        case ATTACK_DAMAGE: {
                            yield 3;
                        }
                        case ATTACK_SPEED: {
                            yield 6;
                        }
                        case ARMOR: 
                        case MAX_HEALTH: 
                        case LUCK: {
                            yield 1;
                        }
                        case MOVEMENT_SPEED: 
                    }
                    yield 4;
                }
                case RANGED -> {
                    switch (stat) {
                        default: {
                            throw new IncompatibleClassChangeError();
                        }
                        case ATTACK_DAMAGE: 
                        case ATTACK_SPEED: {
                            yield 0;
                        }
                        case ARMOR: {
                            yield 1;
                        }
                        case MAX_HEALTH: {
                            yield 2;
                        }
                        case MOVEMENT_SPEED: 
                        case LUCK: 
                    }
                    yield 4;
                }
                case ARCANE -> {
                    switch (stat) {
                        default: {
                            throw new IncompatibleClassChangeError();
                        }
                        case ATTACK_DAMAGE: 
                        case ATTACK_SPEED: {
                            yield 0;
                        }
                        case ARMOR: 
                        case MOVEMENT_SPEED: {
                            yield 2;
                        }
                        case MAX_HEALTH: {
                            yield 3;
                        }
                        case LUCK: 
                    }
                    yield 5;
                }
                case GUARDIAN -> {
                    switch (stat) {
                        default: {
                            throw new IncompatibleClassChangeError();
                        }
                        case ATTACK_DAMAGE: 
                        case MOVEMENT_SPEED: 
                        case LUCK: {
                            yield 1;
                        }
                        case ATTACK_SPEED: {
                            yield 0;
                        }
                        case ARMOR: 
                        case MAX_HEALTH: 
                    }
                    yield 6;
                }
                case TOOL -> {
                    switch (stat) {
                        default: {
                            throw new IncompatibleClassChangeError();
                        }
                        case ATTACK_DAMAGE: 
                        case ATTACK_SPEED: {
                            yield 0;
                        }
                        case ARMOR: 
                        case MAX_HEALTH: {
                            yield 2;
                        }
                        case MOVEMENT_SPEED: {
                            yield 5;
                        }
                        case LUCK: 
                    }
                    yield 4;
                }
                case BALANCED -> stat.weight;
            };
        }
    }

    private static final class SocketStats {
        private final List<Socket> installed;
        private final int[] counts = new int[FoodType.values().length];
        private final double[] powers = new double[FoodType.values().length];

        int size() {
            return this.installed.size();
        }

        int count(FoodType type) {
            return this.counts[type.ordinal()];
        }

        double rawPower(FoodType type) {
            return this.powers[type.ordinal()];
        }

        double effectPower(FoodType type, WeaponRole role) {
            return this.rawPower(type) * role.socketMultiplier(type);
        }

        int setCount(FoodType type) {
            if (type == FoodType.SWEET || type == FoodType.FOOD) {
                return this.count(FoodType.SWEET) + this.count(FoodType.FOOD);
            }
            return this.count(type);
        }

        private SocketStats(List<Socket> list) {
            this.installed = list;
            for (Socket socket : list) {
                int n;
                int n2 = n = socket.type.ordinal();
                this.counts[n2] = this.counts[n2] + 1;
                int n3 = n;
                this.powers[n3] = this.powers[n3] + socket.tier.multiplier * 1.5;
            }
        }

        int setTier(FoodType foodType) {
            int n = 0;
            int n2 = 0;
            int n3 = 0;
            int n4 = 0;
            for (Socket socket : this.installed) {
                boolean bl;
                boolean bl2 = bl = socket.type == foodType || (foodType == FoodType.SWEET || foodType == FoodType.FOOD) && (socket.type == FoodType.SWEET || socket.type == FoodType.FOOD);
                if (!bl) continue;
                ++n4;
                int n5 = socket.tier.level;
                if (n5 >= n) {
                    n3 = n2;
                    n2 = n;
                    n = n5;
                    continue;
                }
                if (n5 >= n2) {
                    n3 = n2;
                    n2 = n5;
                    continue;
                }
                if (n5 <= n3) continue;
                n3 = n5;
            }
            if (n4 == 0) {
                return 1;
            }
            int n6 = Math.min(3, n4);
            int n7 = n + (n6 >= 2 ? n2 : 0) + (n6 >= 3 ? n3 : 0);
            return Math.max(1, Math.min(4, n7 / n6 + FoodSocketPowers.setBonus(this.counts, foodType)));
        }
    }

    public static enum FoodType {
        MEAT,
        CHEESE,
        TEA,
        WINE,
        SWEET,
        APPLE,
        FISH,
        VEGETABLE,
        ENERGY,
        FOOD;


        private String id() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        Component displayName() {
            return Component.m_237115_((String)("food.ftboss.socket." + this.id()));
        }

        Component effectDescription() {
            return Component.m_237115_((String)("tooltip.ftboss.socket.effect." + this.id()));
        }

        Component setDescription() {
            String setId = this == FOOD ? SWEET.id() : this.id();
            return Component.m_237115_((String)("tooltip.ftboss.socket.set." + setId));
        }
    }

    private static enum SocketTier {
        BASIC(1, 1.0),
        FINE(2, 1.25),
        GOURMET(3, 1.5);

        private final int level;
        private final double multiplier;

        private SocketTier(int level, double multiplier) {
            this.level = level;
            this.multiplier = multiplier;
        }

        static SocketTier byLevel(int level) {
            if (level >= 3) {
                return GOURMET;
            }
            if (level == 2) {
                return FINE;
            }
            return BASIC;
        }

        Component displayName() {
            return Component.m_237115_((String)("tier.ftboss.socket." + this.level));
        }
    }

    private record Socket(FoodType type, String item, SocketTier tier) {
    }

    private static enum FoodBlessingKind {
        EXECUTE,
        ATTACK_SPEED,
        ABSORPTION,
        ATTACK_DAMAGE,
        LOW_HEALTH_GUARD,
        LIFESTEAL,
        POISON,
        REGEN,
        ROOT,
        CRIT,
        PROJECTILE_GUARD,
        CORK_RUSH,
        FRENZY,
        WITHER,
        CHAIN,
        OVERLOAD,
        MOVEMENT_SPEED,
        KNOCKBACK_RESISTANCE,
        PRISMATIC,
        MAX_HEALTH,
        REPAIR,
        EMERGENCY_HEART,
        MARK,
        BURN,
        UNDERTOW,
        BRINE,
        WET_GUARD,
        ISOLATION,
        HUNT,
        KILL_FEAST;

    }

    private static final class FoodBlessingStats {
        private final int[] levels = new int[FoodBlessingKind.values().length];

        private FoodBlessingStats(ItemStack stack) {
            ListTag list = FoodWeaponEvolution.foodBlessingList(stack);
            for (int i = 0; i < list.size(); ++i) {
                CompoundTag entry = list.m_128728_(i);
                FoodBlessing blessing = FoodBlessing.byId(entry.m_128461_("Blessing"));
                if (blessing == null) continue;
                int index = blessing.kind.ordinal();
                this.levels[index] = Math.max(this.levels[index], FoodWeaponEvolution.foodBlessingLevel(entry));
            }
        }

        int level(FoodBlessingKind kind) {
            return this.levels[kind.ordinal()];
        }
    }

    public record FoodBlessingView(Component name, Component description, String rank) {
    }

    public record WeaponDetailView(Component category, Component title, Component description, int gourmetSkillIndex, boolean selected) {
        public WeaponDetailView(Component category, Component title, Component description) {
            this(category, title, description, -1, false);
        }

        public boolean selectable() {
            return this.gourmetSkillIndex >= 0;
        }
    }

    private record AcquiredSkill(Profile profile, EvolutionSkill skill) {
    }

    public static enum EvolutionSkill {
        FROST_BITE("frost_bite", SkillEffect.SLOW),
        SUGAR_RUSH("sugar_rush", SkillEffect.HASTE),
        CANDY_GUARD("candy_guard", SkillEffect.GUARD),
        BLOOD_FEAST("blood_feast", SkillEffect.LIFESTEAL),
        CLEAVER_FORCE("cleaver_force", SkillEffect.DAMAGE),
        HEAVY_IMPACT("heavy_impact", SkillEffect.KNOCKBACK),
        THORN_VENOM("thorn_venom", SkillEffect.POISON),
        TEA_RECOVERY("tea_recovery", SkillEffect.REGEN),
        ROOTED_GRIP("rooted_grip", SkillEffect.SLOW),
        VINTAGE_FURY("vintage_fury", SkillEffect.CRIT),
        TIPSY_GUARD("tipsy_guard", SkillEffect.GUARD),
        CORK_BURST("cork_burst", SkillEffect.KNOCKBACK),
        CHEESE_FRENZY("cheese_frenzy", SkillEffect.HASTE),
        PLAGUE_BITE("plague_bite", SkillEffect.POISON),
        SCAVENGER_FEAST("scavenger_feast", SkillEffect.LIFESTEAL),
        OVERLOAD_ARC("overload_arc", SkillEffect.CHAIN),
        HIGH_VOLTAGE("high_voltage", SkillEffect.CRIT),
        MAGNETIC_LOCK("magnetic_lock", SkillEffect.SLOW),
        PRISMATIC_TOXIN("prismatic_toxin", SkillEffect.POISON),
        RECYCLE_GUARD("recycle_guard", SkillEffect.GUARD),
        FERTILE_REGROWTH("fertile_regrowth", SkillEffect.REGEN),
        HONEYCRISP_REBIRTH("honeycrisp_rebirth", SkillEffect.REGEN),
        GOLDEN_PEEL("golden_peel", SkillEffect.GUARD),
        ORCHARD_STRIKE("orchard_strike", SkillEffect.DAMAGE),
        UNDERTOW("undertow", SkillEffect.KNOCKBACK),
        BRINE_BITE("brine_bite", SkillEffect.SLOW),
        TIDAL_RECOVERY("tidal_recovery", SkillEffect.REGEN),
        SHEPHERD_MIGHT("shepherd_might", SkillEffect.DAMAGE),
        HUNTING_HOWL("hunting_howl", SkillEffect.WEAKEN),
        PACK_FEAST("pack_feast", SkillEffect.LIFESTEAL);

        private final String id;
        private final SkillEffect effect;

        private EvolutionSkill(String id, SkillEffect effect) {
            this.id = id;
            this.effect = effect;
        }

        Component nameComponent() {
            return Component.m_237115_((String)("skill.ftboss.evolution." + this.id));
        }

        static EvolutionSkill byId(String id) {
            for (EvolutionSkill skill : EvolutionSkill.values()) {
                if (!skill.id.equals(id)) continue;
                return skill;
            }
            return null;
        }
    }

    private static enum SkillEffect {
        DAMAGE,
        LIFESTEAL,
        SLOW,
        POISON,
        FIRE,
        CRIT,
        GUARD,
        HASTE,
        WEAKEN,
        KNOCKBACK,
        CHAIN,
        REGEN;


        String key() {
            return "skill.ftboss.evolution.effect." + this.name().toLowerCase(Locale.ROOT);
        }
    }

    private static enum EvolutionStat {
        ATTACK_DAMAGE("attack_damage", 2.0, 3),
        ATTACK_SPEED("attack_speed", 0.08, 2),
        ARMOR("armor", 1.5, 1),
        MAX_HEALTH("max_health", 2.0, 1),
        MOVEMENT_SPEED("movement_speed", 0.03, 1),
        LUCK("luck", 1.0, 1);

        private final String id;
        private final double gain;
        private final int weight;

        private EvolutionStat(String id, double gain, int weight) {
            this.id = id;
            this.gain = gain;
            this.weight = weight;
        }

        Component displayName(double amount) {
            return Component.m_237110_((String)("tooltip.ftboss.gourmet.stat." + this.id), (Object[])new Object[]{FoodWeaponEvolution.formatNumber(amount)});
        }

        static EvolutionStat random(WeaponRole role, RandomSource random) {
            int total = 0;
            for (EvolutionStat stat : EvolutionStat.values()) {
                total += role.statWeight(stat);
            }
            int roll = random.m_188503_(total);
            for (EvolutionStat stat : EvolutionStat.values()) {
                if ((roll -= role.statWeight(stat)) >= 0) continue;
                return stat;
            }
            return LUCK;
        }
    }

    public record SocketView(ItemStack stack, Component type, Component tier, Component effect, Component synergy, int color, boolean matching) {
    }

    public record StatueDailyView(Component request, String requestItemId, int progress, int required, Component reward, int state) {
    }

    private static enum DailyRewardType {
        EVOLUTION("evolution"),
        EXPERIENCE("experience"),
        COINS("coins");

        private final String id;

        private DailyRewardType(String id) {
            this.id = id;
        }

        static DailyRewardType byId(String id) {
            for (DailyRewardType type : DailyRewardType.values()) {
                if (!type.id.equals(id)) continue;
                return type;
            }
            return null;
        }

        Component displayName(int amount) {
            return Component.m_237110_((String)("reward.ftboss.statue." + this.id), (Object[])new Object[]{amount});
        }
    }
}

