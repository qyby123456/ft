package net.farmingtales.ftboss.item;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.tags.TagKey;

/** 食物 Boss 武器进化、每日石像委托与食物插槽。状态全部保存在武器 ItemStack 上。 */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class FoodWeaponEvolution {
    public static final int FIRST_THRESHOLD = 100;
    public static final int SECOND_THRESHOLD = 250;
    public static final int GOURMET_MAX_LEVEL = 8;
    public static final int GOURMET_SOCKET_CAPACITY = 30;
    /** All passive healing-on-hit sources from the food weapon system use a restrained interval. */
    public static final int ON_HIT_LIFESTEAL_COOLDOWN_TICKS = 60;
    private static final int MAX_FOOD_BLESSINGS = net.farmingtales.ftboss.util.FoodWeaponBuildRules.MAX_BLESSINGS;
    public static final int DAILY_TASK_COUNT = net.farmingtales.ftboss.util.FoodWeaponBuildRules.DAILY_TASKS;
    public static final int DAILY_FOOD_COUNT = 8; // Legacy task migration default.

    private static final String ROOT = "FTBossFoodEvolution";
    private static final String PROFILE = "Profile";
    private static final String XP = "Experience";
    private static final String LEVEL = "Level";
    private static final String SKILL = "EvolutionSkill";
    private static final String GOURMET_SKILLS = "EvolutionSkills";
    private static final String GOURMET_ACTIVE_PROFILE = "ActiveProfile";
    /** Index into the current eight-evolution run; cleared by grindstone respec. */
    private static final String GOURMET_ACTIVE_SKILL = "ActiveSkillIndex";
    private static final String GOURMET_STATS = "EvolutionStats";
    private static final String BLESSING_WASHED = "BlessingWashed";
    /** Non-vanilla food-series enchantments stored only on the blessed weapon. */
    private static final String FOOD_BLESSINGS = "FoodBlessings";
    private static final String LAST_FOOD_BLESSING = "LastFoodBlessing.";
    /** Statue used for the second evolution; PROFILE remains the first/bound statue. */
    private static final String SECOND_PROFILE = "SecondProfile";
    private static final String SOCKETS = "Sockets";
    private static final String DAILY_ROOT = "FTBossFoodEvolutionDailies";
    private static final String DAILY_DAY = "MinecraftDay";
    private static final String DAILY_REQUEST = "RequestedItem";
    private static final String DAILY_REQUIRED = "Required";
    private static final String DAILY_REWARD_TYPE = "RewardType";
    private static final String DAILY_REWARD_AMOUNT = "RewardAmount";
    private static final ResourceLocation COIN_ID = ResourceLocation.tryParse("kubejs:coin");
    private static final String LAST_SKILL_PROC = "LastSkillProc";
    private static final String LAST_SOCKET_PROC = "LastSocketProc";
    private static final String CREATE_NAMESPACE = "create";
    private static final ThreadLocal<Boolean> EXTRA_DAMAGE = ThreadLocal.withInitial(() -> false);
    private static final EnumMap<FoodType, List<ResourceLocation>> DAILY_FOOD_POOLS = new EnumMap<>(FoodType.class);
    private static final List<TagKey<Item>> DAILY_NATURAL_FOOD_TAGS = List.of(
            dailyFoodTag("forge:crops"), dailyFoodTag("forge:fruits"), dailyFoodTag("forge:berries"),
            dailyFoodTag("forge:vegetables"), dailyFoodTag("forge:raw_meats"), dailyFoodTag("forge:raw_fishes"));
    private static final Set<String> VANILLA_NATURAL_DAILY_FOODS = Set.of(
            "apple", "beef", "porkchop", "chicken", "mutton", "rabbit", "cod", "salmon",
            "tropical_fish", "pufferfish", "carrot", "potato", "beetroot", "melon_slice",
            "sweet_berries", "glow_berries", "chorus_fruit", "honey_bottle");
    private static RecipeManager dailyRecipeManager;
    private static int dailyRecipeFingerprint = Integer.MIN_VALUE;
    private static Set<ResourceLocation> dailyRecipeOutputs = Set.of();
    private static boolean registered;

    private static final UUID EVOLUTION_DAMAGE = UUID.fromString("4c678802-b15b-4a2a-b399-d6559081bd1f");
    private static final UUID EVOLUTION_SPEED = UUID.fromString("2ae62943-4400-4296-ad06-efec0d0e98a9");
    private static final UUID SOCKET_ARMOR = UUID.fromString("be787aed-adb4-46ec-a466-f64dd529a11c");
    private static final UUID SOCKET_TOUGHNESS = UUID.fromString("c5ddd11c-b0db-4503-ab92-b53f0302809f");
    private static final UUID SOCKET_HEALTH = UUID.fromString("b14aec63-216e-46d9-8c31-b66495b2f087");
    private static final UUID SOCKET_MOVEMENT = UUID.fromString("80a6b7ea-22cd-4c99-a32e-e825d83f3fe7");
    private static final UUID SOCKET_LUCK = UUID.fromString("13fb758d-c241-4071-a7d4-a8e0f74e6258");

    public enum FoodType {
        MEAT, CHEESE, TEA, WINE, SWEET, APPLE, FISH, VEGETABLE, ENERGY, FOOD;

        private String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        Component displayName() {
            return Component.translatable("food.ftboss.socket." + id());
        }

        Component effectDescription() {
            return Component.translatable("tooltip.ftboss.socket.effect." + id());
        }

        Component setDescription() {
            String setId = this == FOOD ? SWEET.id() : id();
            return Component.translatable("tooltip.ftboss.socket.set." + setId);
        }
    }

    private enum WeaponRole {
        VANGUARD("vanguard"), DUELIST("duelist"), RANGED("ranged"), ARCANE("arcane"),
        GUARDIAN("guardian"), TOOL("tool"), BALANCED("balanced");

        private final String id;

        WeaponRole(String id) {
            this.id = id;
        }

        Component displayName() {
            return Component.translatable("role.ftboss.weapon." + id);
        }

        Component bonusDescription() {
            return Component.translatable("tooltip.ftboss.evolution.role_bonus." + id);
        }

        boolean specializes(FoodType type) {
            return switch (this) {
                case VANGUARD -> type == FoodType.MEAT || type == FoodType.WINE || type == FoodType.ENERGY;
                case DUELIST -> type == FoodType.TEA || type == FoodType.FISH || type == FoodType.ENERGY;
                case RANGED -> type == FoodType.WINE || type == FoodType.FISH || type == FoodType.ENERGY;
                case ARCANE -> type == FoodType.TEA || type == FoodType.SWEET
                        || type == FoodType.FOOD || type == FoodType.ENERGY;
                case GUARDIAN -> type == FoodType.CHEESE || type == FoodType.VEGETABLE || type == FoodType.APPLE;
                case TOOL -> type == FoodType.TEA || type == FoodType.ENERGY || type == FoodType.VEGETABLE;
                case BALANCED -> true;
            };
        }

        double socketMultiplier(FoodType type) {
            return this == BALANCED ? 1.10D : specializes(type) ? 1.25D : 1.0D;
        }

        float skillPowerPerLevel(boolean gourmet) {
            if (gourmet) return switch (this) {
                case VANGUARD -> 0.06F;
                case DUELIST -> 0.07F;
                case RANGED -> 0.08F;
                case ARCANE -> 0.14F;
                case GUARDIAN -> 0.06F;
                case TOOL -> 0.08F;
                case BALANCED -> 0.10F;
            };
            return switch (this) {
                case VANGUARD -> 0.18F;
                case DUELIST -> 0.18F;
                case RANGED -> 0.20F;
                case ARCANE -> 0.25F;
                case GUARDIAN -> 0.16F;
                case TOOL -> 0.18F;
                case BALANCED -> 0.20F;
            };
        }

        double cooldownReductionPerLevel(boolean gourmet) {
            if (gourmet) return switch (this) {
                case VANGUARD, GUARDIAN -> 0.03D;
                case DUELIST, RANGED, TOOL, BALANCED -> 0.04D;
                case ARCANE -> 0.045D;
            };
            return switch (this) {
                case VANGUARD, GUARDIAN -> 0.08D;
                case DUELIST -> 0.12D;
                case RANGED, TOOL, BALANCED -> 0.10D;
                case ARCANE -> 0.14D;
            };
        }

        int statWeight(EvolutionStat stat) {
            return switch (this) {
                case VANGUARD -> switch (stat) {
                    case ATTACK_DAMAGE -> 6; case ATTACK_SPEED -> 2; case ARMOR -> 1;
                    case MAX_HEALTH -> 2; case MOVEMENT_SPEED, LUCK -> 1;
                };
                case DUELIST -> switch (stat) {
                    case ATTACK_DAMAGE -> 3; case ATTACK_SPEED -> 6; case ARMOR, MAX_HEALTH, LUCK -> 1;
                    case MOVEMENT_SPEED -> 4;
                };
                case RANGED -> switch (stat) {
                    case ATTACK_DAMAGE, ATTACK_SPEED -> 0; case ARMOR -> 1; case MAX_HEALTH -> 2;
                    case MOVEMENT_SPEED, LUCK -> 4;
                };
                case ARCANE -> switch (stat) {
                    case ATTACK_DAMAGE, ATTACK_SPEED -> 0; case ARMOR, MOVEMENT_SPEED -> 2;
                    case MAX_HEALTH -> 3; case LUCK -> 5;
                };
                case GUARDIAN -> switch (stat) {
                    case ATTACK_DAMAGE, MOVEMENT_SPEED, LUCK -> 1; case ATTACK_SPEED -> 0;
                    case ARMOR, MAX_HEALTH -> 6;
                };
                case TOOL -> switch (stat) {
                    case ATTACK_DAMAGE, ATTACK_SPEED -> 0; case ARMOR, MAX_HEALTH -> 2;
                    case MOVEMENT_SPEED -> 5; case LUCK -> 4;
                };
                case BALANCED -> stat.weight;
            };
        }
    }

    private enum SkillEffect {
        DAMAGE, LIFESTEAL, SLOW, POISON, FIRE, CRIT, GUARD, HASTE, WEAKEN, KNOCKBACK, CHAIN, REGEN;

        String key() {
            return "skill.ftboss.evolution.effect." + name().toLowerCase(Locale.ROOT);
        }
    }

    private enum DailyRewardType {
        EVOLUTION("evolution"), EXPERIENCE("experience"), COINS("coins");

        private final String id;

        DailyRewardType(String id) {
            this.id = id;
        }

        static DailyRewardType byId(String id) {
            for (DailyRewardType type : values()) if (type.id.equals(id)) return type;
            return null;
        }

        Component displayName(int amount) {
            return Component.translatable("reward.ftboss.statue." + id, amount);
        }
    }

    public enum EvolutionSkill {
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

        EvolutionSkill(String id, SkillEffect effect) {
            this.id = id;
            this.effect = effect;
        }

        Component nameComponent() {
            return Component.translatable("skill.ftboss.evolution." + id);
        }

        static EvolutionSkill byId(String id) {
            for (EvolutionSkill skill : values()) if (skill.id.equals(id)) return skill;
            return null;
        }
    }

    private enum EvolutionStat {
        ATTACK_DAMAGE("attack_damage", 2.0, 3),
        ATTACK_SPEED("attack_speed", 0.08, 2),
        ARMOR("armor", 1.5, 1),
        MAX_HEALTH("max_health", 2.0, 1),
        MOVEMENT_SPEED("movement_speed", 0.03, 1),
        LUCK("luck", 1.0, 1);

        private final String id;
        private final double gain;
        private final int weight;

        EvolutionStat(String id, double gain, int weight) {
            this.id = id;
            this.gain = gain;
            this.weight = weight;
        }

        Component displayName(double amount) {
            return Component.translatable("tooltip.ftboss.gourmet.stat." + id, formatNumber(amount));
        }

        static EvolutionStat random(WeaponRole role, net.minecraft.util.RandomSource random) {
            int total = 0;
            for (EvolutionStat stat : values()) total += role.statWeight(stat);
            int roll = random.nextInt(total);
            for (EvolutionStat stat : values()) {
                roll -= role.statWeight(stat);
                if (roll < 0) return stat;
            }
            return LUCK;
        }
    }

    public enum Profile {
        SUGAR("sugar", 1, FoodType.SWEET, EvolutionSkill.FROST_BITE, EvolutionSkill.SUGAR_RUSH, EvolutionSkill.CANDY_GUARD),
        BUTCHER("butcher", 2, FoodType.MEAT, EvolutionSkill.BLOOD_FEAST, EvolutionSkill.CLEAVER_FORCE, EvolutionSkill.HEAVY_IMPACT),
        SAYA("saya", 3, FoodType.TEA, EvolutionSkill.THORN_VENOM, EvolutionSkill.TEA_RECOVERY, EvolutionSkill.ROOTED_GRIP),
        WINE("wine", 4, FoodType.WINE, EvolutionSkill.VINTAGE_FURY, EvolutionSkill.TIPSY_GUARD, EvolutionSkill.CORK_BURST),
        RAT("rat", 5, FoodType.CHEESE, EvolutionSkill.CHEESE_FRENZY, EvolutionSkill.PLAGUE_BITE, EvolutionSkill.SCAVENGER_FEAST),
        DEFECT("defect", 6, FoodType.ENERGY, EvolutionSkill.OVERLOAD_ARC, EvolutionSkill.HIGH_VOLTAGE, EvolutionSkill.MAGNETIC_LOCK),
        COMPOST("compost", 7, FoodType.VEGETABLE, EvolutionSkill.PRISMATIC_TOXIN, EvolutionSkill.RECYCLE_GUARD, EvolutionSkill.FERTILE_REGROWTH),
        APPLE("apple", 8, FoodType.APPLE, EvolutionSkill.HONEYCRISP_REBIRTH, EvolutionSkill.GOLDEN_PEEL, EvolutionSkill.ORCHARD_STRIKE),
        TIDE("tide", 9, FoodType.FISH, EvolutionSkill.UNDERTOW, EvolutionSkill.BRINE_BITE, EvolutionSkill.TIDAL_RECOVERY),
        BENGER("benger", 10, FoodType.MEAT, EvolutionSkill.SHEPHERD_MIGHT, EvolutionSkill.HUNTING_HOWL, EvolutionSkill.PACK_FEAST);

        private final String id;
        private final int tint;
        private final FoodType food;
        private final EvolutionSkill[] pool;

        Profile(String id, int tint, FoodType food, EvolutionSkill... pool) {
            this.id = id;
            this.tint = tint;
            this.food = food;
            this.pool = pool;
        }

        Component displayName() {
            return Component.translatable("profile.ftboss.evolution." + id);
        }

        static Profile byId(String id) {
            for (Profile profile : values()) if (profile.id.equals(id)) return profile;
            return null;
        }

        static Profile byTint(int tint) {
            for (Profile profile : values()) if (profile.tint == tint) return profile;
            return null;
        }
    }

    private static List<StatueBlessingCatalog.Definition> blessingPool(Profile profile) {
        return StatueBlessingCatalog.pool(profile.id);
    }

    /** One distinct mechanic per blessing; no two entries share an effect kind. */
    private enum FoodBlessingKind {
        EXECUTE, ATTACK_SPEED, ABSORPTION, ATTACK_DAMAGE, LOW_HEALTH_GUARD, LIFESTEAL,
        POISON, REGEN, ROOT, CRIT, PROJECTILE_GUARD, CORK_RUSH, FRENZY, WITHER, CHAIN,
        OVERLOAD, MOVEMENT_SPEED, KNOCKBACK_RESISTANCE, PRISMATIC, MAX_HEALTH, REPAIR,
        EMERGENCY_HEART, MARK, BURN, UNDERTOW, BRINE, WET_GUARD, ISOLATION, HUNT, KILL_FEAST
    }

    private enum FoodBlessing {
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

        private static final java.util.Map<String, FoodBlessing> BY_ID;

        static {
            java.util.Map<String, FoodBlessing> byId = new java.util.HashMap<>();
            for (FoodBlessing blessing : values()) byId.put(blessing.id, blessing);
            BY_ID = java.util.Map.copyOf(byId);
        }

        private final String id;
        private final FoodBlessingKind kind;
        private final UUID modifierId;

        FoodBlessing(String id, FoodBlessingKind kind) {
            this.id = id;
            this.kind = kind;
            this.modifierId = UUID.nameUUIDFromBytes(("ftboss.food_blessing." + id)
                    .getBytes(StandardCharsets.UTF_8));
        }

        Component nameComponent() {
            return Component.translatable("food_blessing.ftboss." + id);
        }

        Component descriptionComponent(int level) {
            return Component.translatable("tooltip.ftboss.food_blessing." + id, level);
        }

        static FoodBlessing byId(String id) {
            return id == null ? null : BY_ID.get(id);
        }
    }

    private enum SocketTier {
        BASIC(1, 1.5D), FINE(2, 1.875D), GOURMET(3, 2.25D);

        private final int level;
        private final double multiplier;

        SocketTier(int level, double multiplier) {
            this.level = level;
            this.multiplier = multiplier;
        }

        static SocketTier byLevel(int level) {
            if (level >= 3) return GOURMET;
            if (level == 2) return FINE;
            return BASIC;
        }

        Component displayName() {
            return Component.translatable("tier.ftboss.socket." + level);
        }
    }

    private record Socket(FoodType type, String item, SocketTier tier) {}

    /** One immutable read of socket NBT for a whole event or tooltip pass. */
    private static final class SocketStats {
        private final List<Socket> installed;
        private final int[] counts = new int[FoodType.values().length];
        private final double[] powers = new double[FoodType.values().length];

        private SocketStats(List<Socket> installed) {
            this.installed = installed;
            for (Socket socket : installed) {
                int index = socket.type.ordinal();
                this.counts[index]++;
                this.powers[index] += socket.tier.multiplier * 1.5D;
            }
        }

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

        int setTier(FoodType type) {
            int first = 0;
            int second = 0;
            int third = 0;
            int matches = 0;
            for (Socket socket : this.installed) {
                boolean matchesType = socket.type == type || ((type == FoodType.SWEET || type == FoodType.FOOD)
                        && (socket.type == FoodType.SWEET || socket.type == FoodType.FOOD));
                if (!matchesType) continue;
                matches++;
                int level = socket.tier.level;
                if (level >= first) {
                    third = second;
                    second = first;
                    first = level;
                } else if (level >= second) {
                    third = second;
                    second = level;
                } else if (level > third) {
                    third = level;
                }
            }
            if (matches == 0) return 1;
            int used = Math.min(3, matches);
            int total = first + (used >= 2 ? second : 0) + (used >= 3 ? third : 0);
            return Math.max(1, Math.min(4, total / used + FoodSocketPowers.setBonus(this.counts, type)));
        }
    }

    /** Maximum level per blessing kind, decoded once for defensive checks. */
    private static final class FoodBlessingStats {
        private final int[] levels = new int[FoodBlessingKind.values().length];

        private FoodBlessingStats(ItemStack stack) {
            ListTag list = foodBlessingList(stack);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                FoodBlessing blessing = FoodBlessing.byId(entry.getString("Blessing"));
                if (blessing == null) continue;
                int index = blessing.kind.ordinal();
                this.levels[index] = Math.max(this.levels[index], foodBlessingLevel(entry));
            }
        }

        int level(FoodBlessingKind kind) {
            return this.levels[kind.ordinal()];
        }
    }
    public record SocketView(ItemStack stack, Component type, Component tier, Component effect,
                             Component synergy, int color, boolean matching) {}
    public record FoodBlessingView(Component name, Component description, String rank) {}
    public record WeaponDetailView(Component category, Component title, Component description,
                                   int gourmetSkillIndex, boolean selected) {
        public WeaponDetailView(Component category, Component title, Component description) {
            this(category, title, description, -1, false);
        }

        public boolean selectable() { return gourmetSkillIndex >= 0; }
    }
    private record AcquiredSkill(Profile profile, EvolutionSkill skill) {}

    private FoodWeaponEvolution() {}

    /** Register from a source-owned setup path; the release merger preserves a legacy FTBossMod class. */
    public static synchronized void register() {
        if (registered) return;
        registered = true;
        MinecraftForge.EVENT_BUS.register(FoodWeaponEvolution.class);
    }

    public static int evolutionLevel(ItemStack stack) {
        if (isEvolutionExcluded(stack)) return 0;
        CompoundTag data = data(stack);
        int max = maxLevel(stack);
        return data == null ? 0 : Math.max(0, Math.min(max, data.getInt(LEVEL)));
    }

    /** Read-only UI bridge; all mutation remains server-side in the statue interaction handler. */
    public static boolean isEvolutionCandidate(ItemStack stack) {
        return weaponCandidate(stack);
    }

    public static int evolutionExperience(ItemStack stack) {
        CompoundTag data = data(stack);
        return data == null ? 0 : FoodWeaponProgression.experience(stack, data.getInt(XP));
    }

    public static int maxEvolutionLevel(ItemStack stack) {
        return maxLevel(stack);
    }

    /** Zero means that the weapon already reached its maximum evolution stage. */
    public static int nextEvolutionRequirement(ItemStack stack) {
        int level = evolutionLevel(stack);
        int max = maxLevel(stack);
        return level >= max ? 0 : threshold(stack, level);
    }

    public static int nativeSkillRank(ItemStack stack) {
        return nativeProfile(stack) == null ? 0 : evolutionLevel(stack) + 1;
    }

    public static float activeSkillMultiplier(ItemStack stack) {
        if (isEvolutionExcluded(stack)) return 1.0F;
        boolean gourmet = isGourmetEvolutionWeapon(stack);
        WeaponRole role = weaponRole(stack);
        float levelBonus = evolutionLevel(stack) * role.skillPowerPerLevel(gourmet);
        float arcaneSocketBonus = 0.0F;
        if (role == WeaponRole.ARCANE) {
            SocketStats socketStats = socketStats(stack);
            arcaneSocketBonus = (float) Math.min(0.40D, (socketStats.effectPower(FoodType.TEA, role)
                    + socketStats.effectPower(FoodType.SWEET, role) + socketStats.effectPower(FoodType.FOOD, role)
                    + socketStats.effectPower(FoodType.ENERGY, role)) * 0.015D);
        }
        float multiplier = 1.0F + levelBonus + arcaneSocketBonus;
        if (role == WeaponRole.ARCANE || role == WeaponRole.RANGED) {
            multiplier *= (float) WeaponMaterialBuild.build(stack).damage();
        }
        return multiplier;
    }

    public static int skillCooldown(ItemStack stack, int baseTicks) {
        if (isEvolutionExcluded(stack)) return baseTicks;
        WeaponRole role = weaponRole(stack);
        double levelReduction = evolutionLevel(stack)
                * role.cooldownReductionPerLevel(isGourmetEvolutionWeapon(stack));
        double reduction = levelReduction + socketStats(stack).effectPower(FoodType.TEA, role) * 0.05D;
        return Math.max(20, (int) Math.round(baseTicks * Math.max(0.55D, 1.0D - reduction)));
    }

    public static boolean isNativeWeapon(ItemStack stack) {
        return nativeProfile(stack) != null;
    }

    public static boolean isGourmetEvolutionWeapon(ItemStack stack) {
        return stack.getItem() instanceof net.farmingtales.ftboss.item.weapons.GourmetEvolutionWeapon;
    }

    /** Item-specific compatibility helper used by diagnostics and old integrations. */
    public static boolean isGourmetBlankBlade(ItemStack stack) {
        return "ftboss:gourmet_blank_blade".equals(itemId(stack));
    }

    /** Stable bridge for the order-independent active skill pool. */
    public static String firstProfileId(ItemStack stack) {
        Profile profile = isGourmetEvolutionWeapon(stack) ? activeProfile(stack) : boundProfile(stack);
        return profile == null ? "" : profile.id;
    }

    public static String repairProfileId(ItemStack stack) {
        Profile profile = boundProfile(stack);
        return profile == null ? "" : profile.id;
    }

    /** Empty until the second evolution is actually completed. */
    public static String secondProfileId(ItemStack stack) {
        if (isEvolutionExcluded(stack)) return "";
        CompoundTag data = data(stack);
        return FoodWeaponProgression.secondProfile(stack, data == null ? "" : data.getString(SECOND_PROFILE));
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.hasTag() ? stack.getTagElement(ROOT) : null;
    }

    private static CompoundTag writableData(ItemStack stack, Profile profile) {
        CompoundTag data = stack.getOrCreateTagElement(ROOT);
        if (profile != null && !data.contains(PROFILE, Tag.TAG_STRING)) data.putString(PROFILE, profile.id);
        return data;
    }

    private static String itemId(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? "" : id.toString();
    }

    /** Create contraptions and tools must remain entirely outside the food-evolution system. */
    private static boolean isEvolutionExcluded(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && CREATE_NAMESPACE.equals(id.getNamespace());
    }

    private static Profile nativeProfile(ItemStack stack) {
        return switch (itemId(stack)) {
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

    private static Profile boundProfile(ItemStack stack) {
        if (isEvolutionExcluded(stack)) return null;
        Profile nativeKind = nativeProfile(stack);
        if (nativeKind != null) return nativeKind;
        CompoundTag data = data(stack);
        return data == null ? null : Profile.byId(data.getString(PROFILE));
    }

    private static boolean externalWeaponCandidate(ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() != 1 || isEvolutionExcluded(stack)
                || nativeProfile(stack) != null) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return false;
        WeaponRole role = weaponRole(stack);
        if (role == WeaponRole.RANGED || role == WeaponRole.ARCANE || role == WeaponRole.TOOL) return true;
        return stack.getItem().getAttributeModifiers(EquipmentSlot.MAINHAND, stack).get(Attributes.ATTACK_DAMAGE).stream()
                .anyMatch(modifier -> modifier.getAmount() > 0.0);
    }

    private static boolean weaponCandidate(ItemStack stack) {
        return EquipmentWeaponBuild.applies(stack) || ManorWeaponCavity.applies(stack) || AdditionalWeaponUiEligibility.applies(stack) || isGourmetEvolutionWeapon(stack) || nativeProfile(stack) != null
                || boundProfile(stack) != null || externalWeaponCandidate(stack);
    }

    private static WeaponRole weaponRole(ItemStack stack) {
        if (stack.isEmpty()) return WeaponRole.BALANCED;
        String id = itemId(stack);
        WeaponRole exact = switch (id) {
            case "ftboss:gourmet_blank_blade", "ftboss:turkey_legs", "ftboss:wine_bottle_mace" -> WeaponRole.VANGUARD;
            case "ftboss:defect_claw", "ftboss:dog_beating_stick" -> WeaponRole.DUELIST;
            case "ftboss:barrel_crossbow", "ftboss:tide_trident" -> WeaponRole.RANGED;
            case "ftboss:compost_crown" -> WeaponRole.ARCANE;
            case "ftboss:cheese_wheel_mace", "ftboss:apple_stem" -> WeaponRole.GUARDIAN;
            case "ftboss:candy_cane", "ftboss:blooming_thorns" -> WeaponRole.BALANCED;
            default -> null;
        };
        if (exact != null) return exact;
        Item item = stack.getItem();
        if (FoodWeaponFamilyBuild.kind(stack) == net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind.STAFF) return WeaponRole.ARCANE;
        if (item instanceof DiggerItem || item instanceof ShearsItem) return WeaponRole.TOOL;
        if (item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem) return WeaponRole.RANGED;
        String path = id.substring(Math.max(0, id.indexOf(':') + 1)).toLowerCase(Locale.ROOT);
        if (containsFoodWord(path, "bow", "crossbow", "trident", "gun", "rifle", "pistol", "musket", "launcher")) {
            return WeaponRole.RANGED;
        }
        if (containsFoodWord(path, "staff", "wand", "scepter", "spellbook", "tome", "focus", "grimoire")) {
            return WeaponRole.ARCANE;
        }
        if (containsFoodWord(path, "pickaxe", "shovel", "hoe", "mattock", "drill", "saw", "wrench", "excavator")) {
            return WeaponRole.TOOL;
        }
        if (containsFoodWord(path, "shield", "bulwark", "tower", "guardian")) return WeaponRole.GUARDIAN;
        if (containsFoodWord(path, "dagger", "rapier", "claw", "knife", "katana", "sabre", "saber", "scimitar")) {
            return WeaponRole.DUELIST;
        }
        if (containsFoodWord(path, "hammer", "greataxe", "greatsword", "cleaver", "mace", "maul")) {
            return WeaponRole.VANGUARD;
        }
        var modifiers = item.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND);
        double damage = modifiers.get(Attributes.ATTACK_DAMAGE).stream().mapToDouble(AttributeModifier::getAmount).sum();
        double speed = modifiers.get(Attributes.ATTACK_SPEED).stream().mapToDouble(AttributeModifier::getAmount).sum();
        if (damage >= 8.0D && speed <= -2.7D) return WeaponRole.VANGUARD;
        if (speed >= -2.2D) return WeaponRole.DUELIST;
        return WeaponRole.BALANCED;
    }

    private static double evolutionDamageBonus(WeaponRole role, int level, boolean gourmet) {
        double each = gourmet ? switch (role) {
            case VANGUARD -> 0.75D; case DUELIST -> 0.30D; case GUARDIAN -> 0.25D;
            case BALANCED -> 0.50D; case RANGED, ARCANE, TOOL -> 0.0D;
        } : switch (role) {
            case VANGUARD -> 3.0D; case DUELIST -> 1.25D; case GUARDIAN -> 1.0D;
            case BALANCED -> 2.0D; case RANGED, ARCANE, TOOL -> 0.0D;
        };
        return level * each;
    }

    private static double evolutionSpeedBonus(WeaponRole role, int level, boolean gourmet) {
        double each = gourmet ? switch (role) {
            case VANGUARD -> 0.01D; case DUELIST -> 0.025D; case BALANCED -> 0.02D;
            default -> 0.0D;
        } : switch (role) {
            case VANGUARD -> 0.04D; case DUELIST -> 0.14D; case BALANCED -> 0.08D;
            default -> 0.0D;
        };
        return level * each;
    }

    private static double evolutionArmorBonus(WeaponRole role, int level, boolean gourmet) {
        return role == WeaponRole.GUARDIAN ? level * (gourmet ? 0.40D : 1.50D) : 0.0D;
    }

    private static double evolutionHealthBonus(WeaponRole role, int level, boolean gourmet) {
        return role == WeaponRole.GUARDIAN ? level * (gourmet ? 0.50D : 2.0D) : 0.0D;
    }

    private static double evolutionMovementBonus(WeaponRole role, int level, boolean gourmet) {
        if (role == WeaponRole.DUELIST) return level * (gourmet ? 0.004D : 0.015D);
        if (role == WeaponRole.RANGED) return level * (gourmet ? 0.003D : 0.010D);
        return 0.0D;
    }

    private static double evolutionLuckBonus(WeaponRole role, int level, boolean gourmet) {
        return role == WeaponRole.ARCANE ? level * (gourmet ? 0.15D : 0.50D) : 0.0D;
    }

    private static int maxLevel(ItemStack stack) {
        if (ManorWeaponCavity.applies(stack)) return net.farmingtales.ftboss.util.ManorWeaponCavityRules.MAX_LEVEL;
        return isGourmetBlankBlade(stack) ? GOURMET_MAX_LEVEL
                : net.farmingtales.ftboss.util.FoodWeaponBuildRules.REGULAR_MAX_LEVEL;
    }

    private static int threshold(ItemStack stack, int level) {
        return net.farmingtales.ftboss.util.FoodWeaponBuildRules.evolutionCost(level);
    }

    private static int storedSocketCount(ItemStack stack) {
        CompoundTag data = data(stack);
        if (data == null || !data.contains(SOCKETS, Tag.TAG_LIST)) return 0;
        int hardCap = isGourmetEvolutionWeapon(stack) ? GOURMET_SOCKET_CAPACITY
                : net.farmingtales.ftboss.util.FoodWeaponBuildRules.REGULAR_SOCKET_CAPACITY;
        return Math.min(hardCap, data.getList(SOCKETS, Tag.TAG_COMPOUND).size());
    }

    private static int unlockedSocketCapacity(ItemStack stack) {
        return isGourmetEvolutionWeapon(stack) ? GOURMET_SOCKET_CAPACITY
                : Math.min(net.farmingtales.ftboss.util.FoodWeaponBuildRules.REGULAR_SOCKET_CAPACITY,
                2 + 2 * evolutionLevel(stack));
    }

    private static int socketCapacityInternal(ItemStack stack) {
        if (isGourmetEvolutionWeapon(stack)) return GOURMET_SOCKET_CAPACITY;
        int unlocked = unlockedSocketCapacity(stack);
        // A grindstone resets progression, not installed food. Existing modules stay visible and removable,
        // while no new module can be inserted until the corresponding capacity is unlocked again.
        return Math.max(unlocked, storedSocketCount(stack));
    }

    private static List<Socket> sockets(ItemStack stack) {
        List<Socket> result = new ArrayList<>();
        CompoundTag data = data(stack);
        if (data == null || !data.contains(SOCKETS, Tag.TAG_LIST)) return result;
        ListTag list = data.getList(SOCKETS, Tag.TAG_COMPOUND);
        int hardCap = isGourmetEvolutionWeapon(stack) ? GOURMET_SOCKET_CAPACITY
                : net.farmingtales.ftboss.util.FoodWeaponBuildRules.REGULAR_SOCKET_CAPACITY;
        for (int i = 0; i < list.size() && i < hardCap; i++) {
            CompoundTag entry = list.getCompound(i);
            try {
                FoodType type = FoodType.valueOf(entry.getString("Type"));
                String item = entry.getString("Item");
                type = FoodSocketFoodCatalog.savedType(type, item);
                int tier = entry.contains("Tier", Tag.TAG_INT) ? entry.getInt("Tier") : foodTierById(item).level;
                result.add(new Socket(type, item, SocketTier.byLevel(tier)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return result;
    }

    private static SocketStats socketStats(ItemStack stack) {
        SocketStats stats = new SocketStats(sockets(stack));
        GourmetAccessoryEffects.scalePowers(stats.powers, stack);
        return stats;
    }

    private static ListTag foodBlessingList(ItemStack stack) {
        CompoundTag data = data(stack);
        return data == null ? new ListTag() : data.getList(FOOD_BLESSINGS, Tag.TAG_COMPOUND);
    }

    private static int foodBlessingLevel(CompoundTag entry) {
        return Math.max(1, Math.min(3, entry.getInt("Level")));
    }

    /** Gives one random statue-specific blessing. It never creates or accepts an enchanted book. */
    private static boolean applyFoodBlessing(ServerPlayer player, Profile statueProfile,
                                              ItemStack weapon, ItemStack food, FoodType type) {
        if (ArmorFoodBlessings.eligible(weapon)) {
            if (type != statueProfile.food) {
                player.displayClientMessage(Component.translatable("message.ftboss.food_blessing.wrong_food",
                        statueProfile.food.displayName()), true);
                return false;
            }
            return ArmorFoodBlessings.apply(player, statueProfile.id, weapon, food, foodTier(food).level);
        }
        Profile weaponProfile = boundProfile(weapon);
        if (weaponProfile == null) {
            player.displayClientMessage(Component.translatable("message.ftboss.food_blessing.attune_first"), true);
            return false;
        }
        if (type != statueProfile.food) {
            player.displayClientMessage(Component.translatable("message.ftboss.food_blessing.wrong_food",
                    statueProfile.food.displayName()), true);
            return false;
        }

        ListTag existing = foodBlessingList(weapon);
        if (existing.size() >= MAX_FOOD_BLESSINGS) {
            player.displayClientMessage(Component.translatable("message.ftboss.food_blessing.full",
                    MAX_FOOD_BLESSINGS), true);
            return false;
        }
        List<StatueBlessingCatalog.Definition> choices = new ArrayList<>();
        for (StatueBlessingCatalog.Definition candidate : blessingPool(statueProfile)) {
            boolean owned = false;
            for (int i = 0; i < existing.size(); i++) {
                if (candidate.id().equals(existing.getCompound(i).getString("Blessing"))) {
                    owned = true;
                    break;
                }
            }
            if (!owned) choices.add(candidate);
        }
        if (choices.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.ftboss.food_blessing.complete",
                    statueProfile.displayName()), true);
            return false;
        }
        StatueBlessingCatalog.Definition selected = choices.get(player.getRandom().nextInt(choices.size()));
        SocketTier tier = foodTier(food);
        int level = tier == SocketTier.BASIC ? 1 : tier == SocketTier.FINE
                ? (player.getRandom().nextBoolean() ? 2 : 1) : (player.getRandom().nextBoolean() ? 3 : 2);
        CompoundTag data = writableData(weapon, weaponProfile);
        ListTag list = data.getList(FOOD_BLESSINGS, Tag.TAG_COMPOUND);
        CompoundTag entry = new CompoundTag();
        entry.putString(PROFILE, statueProfile.id);
        entry.putString("Blessing", selected.id());
        entry.putInt("Level", level);
        entry.putInt("FoodTier", tier.level);
        entry.putString("Food", itemId(food));
        list.add(entry);
        data.put(FOOD_BLESSINGS, list);
        if (!player.getAbilities().instabuild) food.shrink(1);

        player.serverLevel().sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D,
                player.getZ(), 72, 0.7D, 0.8D, 0.7D, 0.15D);
        player.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D,
                player.getZ(), 18, 0.4D, 0.5D, 0.4D, 0.08D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 0.9F, 1.1F + tier.level * 0.12F);
        player.sendSystemMessage(Component.translatable("message.ftboss.food_blessing.applied",
                Component.translatable("food_blessing.ftboss." + selected.id()), toRoman(level), tier.displayName())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.translatable("tooltip.ftboss.food_blessing." + selected.id(), level)
                .withStyle(ChatFormatting.GRAY));
        FTBossMod.LOGGER.info("[FTBoss][食物附魔] {} 的 {} 在 {} 石像获得 {} 等级{}（食物等级{}）",
                player.getName().getString(), itemId(weapon), statueProfile.id, selected.id(), level, tier.level);
        return true;
    }

    private static void addFoodBlessingAttributes(ItemAttributeModifierEvent event, ItemStack stack) {
        FoodBlessingCombat.blessingAttributes(event, stack);
    }

    private static int foodBlessingLevel(ItemStack stack, FoodBlessingKind kind) {
        return new FoodBlessingStats(stack).level(kind);
    }

    private static void applyFoodBlessingHits(LivingHurtEvent event, ServerPlayer player,
                                               LivingEntity target, CompoundTag data, long now, ItemStack weapon) {
        ListTag blessingList = foodBlessingList(weapon);
        String fortuneKey = "FTBossFoodFortune:" + player.getUUID();
        String fortuneLevelKey = fortuneKey + ":Level";
        if (target.getPersistentData().getLong(fortuneKey) >= now) {
            int markLevel = Math.max(1, target.getPersistentData().getInt(fortuneLevelKey));
            event.setAmount(event.getAmount() * (1.12F + markLevel * 0.06F));
            target.getPersistentData().remove(fortuneKey);
            target.getPersistentData().remove(fortuneLevelKey);
        }
        for (int i = 0; i < blessingList.size(); i++) {
            CompoundTag entry = blessingList.getCompound(i);
            FoodBlessing blessing = FoodBlessing.byId(entry.getString("Blessing"));
            if (blessing == null) continue;
            int level = foodBlessingLevel(entry);
            float chance = Math.min(0.36F, 0.12F + level * 0.05F);
            switch (blessing.kind) {
                case CRIT -> {
                    if (player.getRandom().nextFloat() < chance) event.setAmount(event.getAmount() * (1.30F + level * 0.12F));
                }
                case EXECUTE -> {
                    double ratio = target.getHealth() / Math.max(1.0F, target.getMaxHealth());
                    if (ratio <= 0.12D + level * 0.05D) event.setAmount(event.getAmount() * (1.20F + level * 0.12F));
                }
                case ABSORPTION -> {
                    if (player.getRandom().nextFloat() < chance) player.addEffect(new MobEffectInstance(
                            MobEffects.ABSORPTION, 60 + level * 20, level - 1, false, false, true), player);
                }
                case CHAIN -> {
                    if (player.getRandom().nextFloat() < chance) chainArc(player, target, 2.0F + level * 1.5F);
                }
                case OVERLOAD -> {
                    if (player.getRandom().nextFloat() < chance) overloadBurst(player, target, 1.5F + level * 1.25F);
                }
                case BURN -> {
                    if (player.getRandom().nextFloat() < chance) target.setSecondsOnFire(2 + level * 2);
                }
                case MARK -> {
                    if (player.getRandom().nextFloat() < chance) {
                        target.getPersistentData().putLong(fortuneKey, now + 100L + level * 40L);
                        target.getPersistentData().putInt(fortuneLevelKey, level);
                        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100 + level * 40,
                                0, false, false, true), player);
                    }
                }
                case LIFESTEAL -> {
                    String key = LAST_FOOD_BLESSING + blessing.id;
                    if (now - data.getLong(key) >= ON_HIT_LIFESTEAL_COOLDOWN_TICKS) {
                        player.heal(0.35F * level);
                        data.putLong(key, now);
                    }
                }
                case POISON -> {
                    if (player.getRandom().nextFloat() < chance) target.addEffect(new MobEffectInstance(
                            MobEffects.POISON, 40 + level * 20, Math.min(2, level - 1), false, false, true), player);
                }
                case WITHER -> {
                    if (player.getRandom().nextFloat() < chance) target.addEffect(new MobEffectInstance(
                            MobEffects.WITHER, 30 + level * 15, Math.min(1, level - 1), false, false, true), player);
                }
                case ROOT -> {
                    if (player.getRandom().nextFloat() < chance) target.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN, 60 + level * 25, Math.min(3, level), false, false, true), player);
                }
                case REGEN -> {
                    if (player.getRandom().nextFloat() < chance) player.addEffect(new MobEffectInstance(
                            MobEffects.REGENERATION, 40 + level * 20, level - 1, false, false, true), player);
                }
                case CORK_RUSH -> {
                    if (player.getRandom().nextFloat() < chance) {
                        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 80 + level * 20,
                                level - 1, false, false, true), player);
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80 + level * 20,
                                level - 1, false, false, true), player);
                    }
                }
                case FRENZY -> {
                    long last = data.getLong(LAST_FOOD_BLESSING + blessing.id);
                    int stacks = now - last <= 60 ? Math.min(level + 2, data.getInt("CheeseFrenzyStacks") + 1) : 1;
                    data.putInt("CheeseFrenzyStacks", stacks);
                    data.putLong(LAST_FOOD_BLESSING + blessing.id, now);
                    event.setAmount(event.getAmount() * (1.0F + stacks * (0.025F + level * 0.01F)));
                }
                case PRISMATIC -> {
                    if (player.getRandom().nextFloat() < chance) {
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60 + level * 20,
                                Math.min(2, level - 1), false, false, true), player);
                        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80 + level * 20,
                                0, false, false, true), player);
                    }
                }
                case REPAIR -> {
                    String key = LAST_FOOD_BLESSING + blessing.id;
                    if (weapon.isDamaged() && now - data.getLong(key) >= Math.max(20, 70 - level * 15)) {
                        weapon.setDamageValue(Math.max(0, weapon.getDamageValue() - level));
                        data.putLong(key, now);
                    }
                }
                case EMERGENCY_HEART -> {
                    String key = LAST_FOOD_BLESSING + blessing.id;
                    if (player.getHealth() <= player.getMaxHealth() * 0.5F && now - data.getLong(key) >= 100) {
                        player.heal(0.75F * level);
                        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60, 0, false, false, true), player);
                        data.putLong(key, now);
                    }
                }
                case UNDERTOW -> {
                    if (target.isInWaterRainOrBubble()) {
                        event.setAmount(event.getAmount() * (1.10F + level * 0.05F));
                        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60 + level * 20,
                                0, false, false, true), player);
                    }
                }
                case BRINE -> {
                    if (player.getRandom().nextFloat() < chance) target.addEffect(new MobEffectInstance(
                            MobEffects.DIG_SLOWDOWN, 60 + level * 25, Math.min(2, level - 1), false, false, true), player);
                }
                case ISOLATION -> {
                    boolean isolated = target.level().getEntitiesOfClass(LivingEntity.class,
                            target.getBoundingBox().inflate(4.0D), other -> other != target && canTarget(player, other)).isEmpty();
                    if (isolated) event.setAmount(event.getAmount() * (1.10F + level * 0.05F));
                }
                case HUNT -> {
                    String huntKey = "FTBossFoodHunt:" + player.getUUID();
                    if (target.getPersistentData().getLong(huntKey) >= now) {
                        event.setAmount(event.getAmount() * (1.05F + level * 0.035F));
                    } else {
                        target.getPersistentData().putLong(huntKey, now + 160L + level * 40L);
                        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160 + level * 40,
                                0, false, false, true), player);
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 + level * 20,
                                Math.min(1, level - 1), false, false, true), player);
                    }
                }
                default -> { }
            }
        }
    }

    @SubscribeEvent
    public static void foodBlessingDefense(LivingHurtEvent event) {
        if (!Boolean.TRUE.equals(EXTRA_DAMAGE.get())) FoodBlessingCombat.defense(event);
    }

    /** Read-only blessing summary used by the statue UI; duplicate ids are hidden defensively. */
    public static List<FoodBlessingView> foodBlessingViews(ItemStack stack) {
        if (stack.getItem() instanceof net.minecraft.world.item.ArmorItem) return ArmorFoodBlessings.views(stack);
        List<FoodBlessingView> result = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        ListTag list = foodBlessingList(stack);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            StatueBlessingCatalog.Definition blessing = StatueBlessingCatalog.byId(entry.getString("Blessing"));
            if (blessing == null || seen.contains(blessing.id())) continue;
            seen.add(blessing.id());
            int level = foodBlessingLevel(entry);
            result.add(new FoodBlessingView(Component.translatable("food_blessing.ftboss." + blessing.id()),
                    Component.translatable("tooltip.ftboss.food_blessing." + blessing.id(), level), toRoman(level)));
        }
        return List.copyOf(result);
    }

    /** Complete, client-safe weapon dossier rendered only inside the statue UI. */
    public static int selectableSkillCount(ItemStack stack) {
        Profile profile = boundProfile(stack);
        if (profile == null) return 0;
        if (isGourmetEvolutionWeapon(stack)) return nativeProfile(stack) == null ? gourmetSkills(stack).size() : 0;
        return net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Aspect.fromId(profile.id) == null
                ? 0 : evolutionLevel(stack);
    }

    public static List<WeaponDetailView> weaponDetailViews(ItemStack stack) {
        Profile profile = boundProfile(stack);
        if (profile == null) return List.of();
        List<WeaponDetailView> result = new ArrayList<>();
        WeaponRole role = weaponRole(stack);
        result.add(new WeaponDetailView(
                Component.translatable("screen.ftboss.statue_hub.details.category.role"),
                role.displayName(), role.bonusDescription()));

        CompoundTag data = data(stack);
        int level = evolutionLevel(stack);
        boolean gourmet = isGourmetEvolutionWeapon(stack);
        if (nativeProfile(stack) != null) {
            result.add(new WeaponDetailView(
                    Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                    Component.translatable("tooltip.ftboss.evolution.native_skill", level + 1,
                            Math.round((activeSkillMultiplier(stack) - 1.0F) * 100.0F)),
                    Component.translatable("screen.ftboss.statue_hub.details.native_skill_help")));
        } else if (gourmet) {
            List<AcquiredSkill> acquired = gourmetSkills(stack);
            int activeSkillIndex = activeGourmetSkillIndex(stack);
            for (int i = 0; i < acquired.size(); i++) {
                AcquiredSkill entry = acquired.get(i);
                var aspect = net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Aspect.fromId(entry.profile.id);
                Component activeName = aspect == null ? entry.profile.displayName()
                        : Component.translatable(aspect.translationKey());
                boolean selected = i == activeSkillIndex;
                result.add(new WeaponDetailView(
                        Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                        Component.translatable("tooltip.ftboss.gourmet.skill_line", i + 1,
                                entry.profile.displayName(), entry.skill.nameComponent()),
                        Component.translatable(selected
                                        ? "screen.ftboss.statue_hub.details.gourmet_skill_selected"
                                        : "screen.ftboss.statue_hub.details.gourmet_skill_switch",
                                activeName, Component.translatable(entry.skill.effect.key())),
                        i, selected));
            }
        } else if (level > 0 && data != null) {
            EvolutionSkill skill = EvolutionSkill.byId(data.getString(SKILL));
            if (skill != null) result.add(new WeaponDetailView(
                    Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                    Component.translatable("tooltip.ftboss.evolution.drawn_skill", skill.nameComponent()),
                    Component.translatable(skill.effect.key())));
        }

        if (level > 0) {
            var first = net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Aspect.fromId(firstProfileId(stack));
            if (gourmet && first != null) {
                result.add(new WeaponDetailView(
                        Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                        Component.translatable("tooltip.ftboss.gourmet.active",
                                Component.translatable(first.translationKey())),
                        net.farmingtales.ftboss.skill.WeaponTypeSkills.description(stack, false)));
            } else if (first != null && FoodWeaponProgression.activeStage(stack) == 1) {
                result.add(new WeaponDetailView(
                        Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                        Component.translatable("tooltip.ftboss.evolution.active_primary",
                                Component.translatable(first.translationKey())),
                        net.farmingtales.ftboss.skill.WeaponTypeSkills.description(stack, false)));
            } else if (first != null) {
                var second = net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Aspect.fromId(secondProfileId(stack));
                if (second == null) second = first;
                var fusion = net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.fusion(first, second);
                if (fusion != null) {
                    result.add(new WeaponDetailView(
                            Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                            Component.translatable("tooltip.ftboss.evolution.active_primary",
                                    Component.translatable(fusion.primaryTranslationKey())),
                            net.farmingtales.ftboss.skill.WeaponTypeSkills.description(stack, false)));
                    result.add(new WeaponDetailView(
                            Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                            Component.translatable("tooltip.ftboss.evolution.active_secondary",
                                    Component.translatable(fusion.secondaryTranslationKey())),
                            net.farmingtales.ftboss.skill.WeaponTypeSkills.description(stack, true)));
                }
            }
        }

        if (gourmet) {
            for (EvolutionStat stat : EvolutionStat.values()) {
                double amount = gourmetStat(stack, stat);
                if (amount <= 0.0D) continue;
                result.add(new WeaponDetailView(
                        Component.translatable("screen.ftboss.statue_hub.details.category.stat"),
                        stat.displayName(amount),
                        Component.translatable("screen.ftboss.statue_hub.details.stat_help")));
            }
        }
        for (FoodBlessingView blessing : foodBlessingViews(stack)) {
            result.add(new WeaponDetailView(
                    Component.translatable("screen.ftboss.statue_hub.details.category.blessing"),
                    blessing.name().copy().append(Component.literal(" " + blessing.rank())),
                    blessing.description()));
        }
        List<SocketView> installed = socketViews(stack);
        for (int i = 0; i < installed.size(); i++) {
            SocketView socket = installed.get(i);
            result.add(new WeaponDetailView(
                    Component.translatable("screen.ftboss.statue_hub.details.category.socket"),
                    Component.literal("#" + (i + 1) + " ").append(socket.stack().getHoverName())
                            .append(Component.literal(" · ")).append(socket.type()).append(Component.literal(" · "))
                            .append(socket.tier()),
                    socket.effect().copy().append(Component.literal(" · ")).append(socket.synergy())));
        }
        result.addAll(FoodWeaponProgression.details(stack));
        result.addAll(FoodSocketPowers.details(stack));
        return List.copyOf(result);
    }

    private static void addFoodBlessingTooltip(List<Component> tip, ItemStack stack) {
        ListTag list = foodBlessingList(stack);
        if (list.isEmpty()) return;
        tip.add(Component.translatable("tooltip.ftboss.food_blessings.header").withStyle(ChatFormatting.LIGHT_PURPLE));
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            FoodBlessing blessing = FoodBlessing.byId(entry.getString("Blessing"));
            if (blessing == null) continue;
            int level = foodBlessingLevel(entry);
            tip.add(Component.literal("  ").append(blessing.nameComponent())
                    .append(Component.literal(" " + toRoman(level))).withStyle(ChatFormatting.GOLD));
            tip.add(Component.literal("    ").append(blessing.descriptionComponent(level)).withStyle(ChatFormatting.GRAY));
        }
        tip.add(Component.translatable("tooltip.ftboss.food_blessings.grindstone").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static String toRoman(int level) {
        return switch (level) {
            case 3 -> "III";
            case 2 -> "II";
            default -> "I";
        };
    }

    /** Number of installed food modules; exposed for NBT-driven weapon model transformations. */
    public static int socketCount(ItemStack stack) {
        return socketStats(stack).size();
    }

    public static int socketCapacity(ItemStack stack) {
        return socketCapacityInternal(stack);
    }

    public static int socketTypeCount(ItemStack stack, FoodType type) {
        return FoodWeaponLifecycle.isBroken(stack) ? 0 : socketStats(stack).count(type);
    }

    public static int socketSetTier(ItemStack stack, FoodType type) {
        return FoodWeaponLifecycle.isBroken(stack) ? 0 : socketStats(stack).setTier(type);
    }

    public static String profileId(int tint) {
        Profile profile = Profile.byTint(tint);
        return profile == null ? "" : profile.id;
    }

    public static boolean matchesStatueFood(ItemStack food, int tint) {
        Profile profile = Profile.byTint(tint);
        return profile != null && foodType(food) == profile.food;
    }

    public static int foodQualityTier(ItemStack food) {
        return foodTier(food).level;
    }

    static String skillId(EvolutionSkill skill) { return skill.id; }

    static Component skillDescription(EvolutionSkill skill) { return Component.translatable(skill.effect.key()); }

    static EvolutionSkill drawStageSkill(Profile profile, Set<EvolutionSkill> owned, net.minecraft.util.RandomSource random) {
        List<EvolutionSkill> choices = new ArrayList<>();
        for (EvolutionSkill skill : profile.pool) if (!owned.contains(skill)) choices.add(skill);
        if (choices.isEmpty()) {
            for (EvolutionSkill skill : EvolutionSkill.values()) if (!owned.contains(skill)) choices.add(skill);
        }
        return choices.isEmpty() ? null : choices.get(random.nextInt(choices.size()));
    }

    /** Compact, read-only data for the statue accessory grid. */
    public static List<SocketView> socketViews(ItemStack stack) {
        List<SocketView> result = new ArrayList<>();
        Profile profile = activeProfile(stack);
        for (Socket socket : sockets(stack)) {
            ItemStack stored = socketStack(socket);
            int color = socketColor(socket.type);
            result.add(new SocketView(stored, socket.type.displayName(), socket.tier.displayName(),
                    socket.type.effectDescription(), socket.type.setDescription(), color,
                    profile != null && socket.type == profile.food));
        }
        return List.copyOf(result);
    }

    private static ItemStack socketStack(Socket socket) {
        ResourceLocation id = ResourceLocation.tryParse(socket.item);
        Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static int socketColor(FoodType type) {
        return switch (type) {
            case MEAT -> 0xD23A44;
            case CHEESE -> 0xF7CD41;
            case TEA -> 0x40BE6A;
            case WINE -> 0x8F3EB6;
            case SWEET -> 0xFF68BF;
            case APPLE -> 0xE33A3F;
            case FISH -> 0x3ECFF2;
            case VEGETABLE -> 0x59C74B;
            case ENERGY -> 0xFF8E2E;
            case FOOD -> 0xEEEEDE;
        };
    }

    /** RGB tint for the one-based modeled socket fill, or -1 while that slot is empty. */
    public static int socketVisualColor(ItemStack stack, int oneBasedSlot) {
        List<Socket> sockets = sockets(stack);
        if (oneBasedSlot < 1 || oneBasedSlot > sockets.size()) return -1;
        return socketColor(sockets.get(oneBasedSlot - 1).type);
    }

    private static Profile activeProfile(ItemStack stack) {
        if (!isGourmetEvolutionWeapon(stack)) return boundProfile(stack);
        List<AcquiredSkill> acquired = gourmetSkills(stack);
        int index = activeGourmetSkillIndex(stack);
        if (index >= 0 && index < acquired.size()) return acquired.get(index).profile;
        CompoundTag data = data(stack);
        Profile legacy = data == null ? null : Profile.byId(data.getString(GOURMET_ACTIVE_PROFILE));
        return legacy == null ? boundProfile(stack) : legacy;
    }

    /** Selected record from the current run, with migration for blades made before selection existed. */
    public static int activeGourmetSkillIndex(ItemStack stack) {
        if (!isGourmetEvolutionWeapon(stack)) return -1;
        List<AcquiredSkill> acquired = gourmetSkills(stack);
        if (acquired.isEmpty()) return -1;
        CompoundTag data = data(stack);
        if (data != null && data.contains(GOURMET_ACTIVE_SKILL, Tag.TAG_INT)) {
            int stored = data.getInt(GOURMET_ACTIVE_SKILL);
            if (stored >= 0 && stored < acquired.size()) return stored;
        }
        Profile legacy = data == null ? null : Profile.byId(data.getString(GOURMET_ACTIVE_PROFILE));
        if (legacy != null) {
            for (int i = acquired.size() - 1; i >= 0; i--) {
                if (acquired.get(i).profile == legacy) return i;
            }
        }
        return acquired.size() - 1;
    }

    private static List<AcquiredSkill> gourmetSkills(ItemStack stack) {
        List<AcquiredSkill> result = new ArrayList<>();
        CompoundTag data = data(stack);
        if (data == null || !data.contains(GOURMET_SKILLS, Tag.TAG_LIST)) return result;
        ListTag list = data.getList(GOURMET_SKILLS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size() && i < GOURMET_MAX_LEVEL; i++) {
            CompoundTag entry = list.getCompound(i);
            Profile profile = Profile.byId(entry.getString(PROFILE));
            EvolutionSkill skill = EvolutionSkill.byId(entry.getString(SKILL));
            if (profile != null && skill != null) result.add(new AcquiredSkill(profile, skill));
        }
        return result;
    }

    private static EvolutionSkill drawGourmetSkill(ItemStack stack, Profile profile,
                                                    net.minecraft.util.RandomSource random) {
        List<AcquiredSkill> acquired = gourmetSkills(stack);
        List<EvolutionSkill> choices = new ArrayList<>();
        for (EvolutionSkill skill : profile.pool) {
            boolean owned = false;
            for (AcquiredSkill entry : acquired) {
                if (entry.skill == skill) {
                    owned = true;
                    break;
                }
            }
            if (!owned) choices.add(skill);
        }
        return choices.isEmpty() ? null : choices.get(random.nextInt(choices.size()));
    }

    private static double gourmetStat(ItemStack stack, EvolutionStat stat) {
        CompoundTag data = data(stack);
        if (data == null || !data.contains(GOURMET_STATS, Tag.TAG_COMPOUND)) return 0.0;
        return Math.max(0.0, data.getCompound(GOURMET_STATS).getDouble(stat.id));
    }

    public static boolean isSocketIngredient(ItemStack stack) {
        return foodType(stack) != null;
    }

    /** Client-safe mirror of the server's daily submission test. */
    public static boolean isDailySubmissionCandidate(List<StatueDailyView> tasks, ItemStack stack, int statueTint) {
        FoodType type = foodType(stack);
        Profile profile = Profile.byTint(statueTint);
        if (stack.isEmpty() || type == null || profile == null) return false;
        for (StatueDailyView task : tasks) {
            if (task.state() != 0) continue;
            if (task.requestItemId().isBlank() ? type == profile.food
                    : task.requestItemId().equals(itemId(stack))) return true;
        }
        return false;
    }

    /** Only offerings the current statue can actually consume for a blessing are displayed. */
    public static boolean isBlessingCandidate(ItemStack weapon, ItemStack food, int statueTint) {
        Profile statue = Profile.byTint(statueTint);
        Profile bound = boundProfile(weapon);
        FoodType offered = foodType(food);
        if (ArmorFoodBlessings.eligible(weapon)) {
            return statue != null && food != weapon && !food.isEmpty() && offered == statue.food
                    && ArmorFoodBlessings.hasRoom(weapon, statue.id);
        }
        return statue != null && bound != null && !food.isEmpty() && !weapon.is(Items.ENCHANTED_BOOK)
                && food != weapon && !weaponCandidate(food)
                && offered == statue.food && foodBlessingList(weapon).size() < MAX_FOOD_BLESSINGS;
    }

    /** Client-safe mirror of insertSocket's non-mutating preconditions. */
    public static boolean isSocketCandidate(ItemStack weapon, ItemStack food, int statueTint) {
        Profile statue = Profile.byTint(statueTint);
        Profile profile = boundProfile(weapon);
        return statue != null && profile != null && !food.isEmpty() && foodType(food) != null
                && sockets(weapon).size() < unlockedSocketCapacity(weapon);
    }

    public static SocketView ingredientView(ItemStack stack) {
        FoodType type = foodType(stack);
        if (type == null) return null;
        SocketTier tier = foodTier(stack);
        return new SocketView(stack.copy(), type.displayName(), tier.displayName(),
                type.effectDescription(), type.setDescription(), socketColor(type), false);
    }

    /** Cumulative XP at the beginning of the currently displayed evolution level. */
    public static int currentEvolutionFloor(ItemStack stack) {
        return 0;
    }

    private static FoodType foodType(ItemStack stack) {
        if (stack.isEmpty()) return null;
        String id = itemId(stack).toLowerCase(Locale.ROOT);
        FoodType explicit = explicitOfferingType(id);
        return explicit != null ? explicit : FoodSocketFoodCatalog.type(stack);
    }

    /** Boss summoners/relics are intentional non-edible offerings; do not rely on translated display names. */
    private static FoodType explicitOfferingType(String id) {
        return switch (id) {
            case "ftboss:sugar_queen_crystal", "ftboss:frost_crown_charm" -> FoodType.SWEET;
            case "ftboss:meat_heart", "ftboss:butcher_fang_charm", "ftboss:butcher_material",
                    "ftboss:benger_whistle", "ftboss:shepherd_whistle_charm" -> FoodType.MEAT;
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

    private static SocketTier foodTier(ItemStack stack) {
        SocketTier base = baseFoodTier(stack);
        if (stack.hasTag() && stack.getTag().contains("quality_food", Tag.TAG_COMPOUND)
                && stack.getTag().getCompound("quality_food").contains("quality", Tag.TAG_ANY_NUMERIC)) {
            int quality = Math.max(0, Math.min(3, stack.getTag().getCompound("quality_food").getInt("quality")));
            return SocketTier.byLevel(Math.max(base.level, 1 + quality));
        }
        return base;
    }

    private static SocketTier baseFoodTier(ItemStack stack) {
        if (stack.isEmpty()) return SocketTier.BASIC;
        if (stack.hasTag() && stack.getTag().contains("FTBossSocketTier", Tag.TAG_INT)) {
            return SocketTier.byLevel(stack.getTag().getInt("FTBossSocketTier"));
        }
        if (stack.hasTag() && stack.getTag().contains("BrewLevel", Tag.TAG_ANY_NUMERIC)) {
            int quality = stack.getTag().getInt("BrewLevel");
            return quality >= 5 ? SocketTier.GOURMET : quality >= 4 ? SocketTier.FINE : SocketTier.BASIC;
        }
        String id = itemId(stack).toLowerCase(Locale.ROOT);
        String path = id.substring(Math.max(0, id.indexOf(':') + 1));
        if (id.equals("minecraft:golden_apple") || id.equals("minecraft:enchanted_golden_apple")
                || containsFoodWord(path, "deluxe", "supreme", "royal", "gourmet", "legendary", "ultimate",
                "banquet", "feast", "grand", "stuffed", "honey_glazed", "truffle", "lobster")) {
            return SocketTier.GOURMET;
        }
        Rarity rarity = stack.getRarity();
        if (rarity == Rarity.EPIC || rarity == Rarity.RARE) return SocketTier.GOURMET;
        FoodProperties properties = stack.getFoodProperties(null);
        if (properties != null) {
            int nutrition = properties.getNutrition();
            float saturation = properties.getSaturationModifier();
            if ((nutrition >= 8 && saturation >= 0.8F) || nutrition + saturation * 4.0F >= 11.0F) {
                return SocketTier.GOURMET;
            }
            if (nutrition + saturation * 4.0F >= 6.0F) return SocketTier.FINE;
        }
        if (rarity == Rarity.UNCOMMON || containsFoodWord(path, "cooked", "baked", "grilled", "smoked", "fried",
                "roast", "sandwich", "burger", "stew", "soup", "pie", "cake", "tart", "pizza", "sushi",
                "nigiri", "roll", "latte", "cappuccino", "cocktail")) return SocketTier.FINE;
        return SocketTier.BASIC;
    }

    private static SocketTier foodTierById(String identifier) {
        ResourceLocation id = ResourceLocation.tryParse(identifier);
        Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
        return item == null || item == Items.AIR ? SocketTier.BASIC : foodTier(new ItemStack(item));
    }

    private static boolean containsFoodWord(String path, String... words) {
        String normalized = "_" + path.replace('/', '_').replace('-', '_').replace('.', '_') + "_";
        for (String word : words) if (normalized.contains("_" + word + "_")) return true;
        return false;
    }

    private static boolean attune(ItemStack stack, Profile profile) {
        Profile current = boundProfile(stack);
        if (current != null) return true;
        if (!isGourmetEvolutionWeapon(stack) && !externalWeaponCandidate(stack)) return false;
        CompoundTag data = writableData(stack, profile);
        data.putInt(XP, 0);
        data.putInt(LEVEL, 0);
        return true;
    }

    private static int addExperience(ItemStack stack, Profile profile, int amount) {
        if (amount <= 0 || !attune(stack, profile) || evolutionLevel(stack) >= maxLevel(stack)) return 0;
        int cap = threshold(stack, evolutionLevel(stack));
        FoodWeaponProgression.normalizeExperience(stack);
        CompoundTag data = writableData(stack, profile);
        int old = evolutionExperience(stack);
        int next = (int) Math.min(cap, (long) old + amount);
        data.putInt(XP, next);
        return next - old;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void useStatue(PlayerInteractEvent.RightClickBlock event) {
        // All statue operations are now routed through the server-validated hub UI.
        // This no-op prevents duplicate mutation before the hub-opening subscriber runs.
    }

    private static CompoundTag daily(ServerPlayer player, Profile profile) {
        CompoundTag root = player.getPersistentData().getCompound(DAILY_ROOT);
        long day = currentDailyDay(player);
        if (!root.contains(DAILY_DAY, Tag.TAG_LONG) || root.getLong(DAILY_DAY) != day) {
            boolean hadPreviousTasks = !root.isEmpty();
            root = new CompoundTag();
            root.putLong(DAILY_DAY, day);
            player.getPersistentData().put(DAILY_ROOT, root);
            if (hadPreviousTasks) {
                player.sendSystemMessage(Component.translatable("message.ftboss.evolution.daily_refreshed")
                        .withStyle(ChatFormatting.AQUA));
            }
        }
        CompoundTag task = root.getCompound(profile.id);
        if (ensureDailyTasks(player, profile, day, task)) {
            root.put(profile.id, task);
            player.getPersistentData().put(DAILY_ROOT, root);
        }
        return task;
    }

    /** One commission cycle is one overworld Minecraft day; sleeping advances this clock. */
    private static long currentDailyDay(ServerPlayer player) {
        long dayTime = player.serverLevel().getServer().overworld().getDayTime();
        return Math.floorDiv(dayTime, 24_000L);
    }

    private static void saveDaily(ServerPlayer player, Profile profile, CompoundTag task) {
        CompoundTag root = player.getPersistentData().getCompound(DAILY_ROOT);
        root.put(profile.id, task);
        player.getPersistentData().put(DAILY_ROOT, root);
    }

    /** Preserves existing commissions while filling the expanded daily plan. */
    private static boolean ensureDailyTasks(ServerPlayer player, Profile profile, long day, CompoundTag task) {
        boolean changed = false;
        if (task.contains(DAILY_REQUEST, Tag.TAG_STRING) && !task.contains(taskKey(0), Tag.TAG_COMPOUND)) {
            CompoundTag legacy = new CompoundTag();
            legacy.putString(DAILY_REQUEST, task.getString(DAILY_REQUEST));
            legacy.putInt(DAILY_REQUIRED, DAILY_FOOD_COUNT);
            legacy.putString(DAILY_REWARD_TYPE, DailyRewardType.EVOLUTION.id);
            legacy.putInt(DAILY_REWARD_AMOUNT, 40);
            legacy.putInt("Progress", task.getInt("Progress"));
            legacy.putBoolean("Ready", task.getBoolean("Ready"));
            legacy.putBoolean("Claimed", task.getBoolean("Claimed"));
            task.put(taskKey(0), legacy);
            for (String key : List.of(DAILY_REQUEST, "Progress", "Ready", "Claimed")) task.remove(key);
            changed = true;
        }

        List<ResourceLocation> pool = dailyFoodPool(player, profile.food);
        boolean[] valid = new boolean[DAILY_TASK_COUNT];
        int evolutionAssigned = 0;
        List<ResourceLocation> selectedFoods = new ArrayList<>();
        for (int slot = 0; slot < DAILY_TASK_COUNT; slot++) {
            CompoundTag entry = task.getCompound(taskKey(slot));
            valid[slot] = validDailyTask(entry, pool);
            if (!valid[slot]) continue;
            if (DailyRewardType.byId(entry.getString(DAILY_REWARD_TYPE)) == DailyRewardType.EVOLUTION) {
                evolutionAssigned++;
            }
            ResourceLocation selected = dailyRequestId(entry);
            if (selected != null) selectedFoods.add(selected);
        }

        long planSeed = dailySeed(player, profile, day);
        int evolutionSlot = positiveMod(mix64(planSeed ^ 0x632BE59BD9B4E019L), DAILY_TASK_COUNT);
        int evolutionFillSlot = -1;
        if (evolutionAssigned < net.farmingtales.ftboss.util.FoodWeaponBuildRules.DAILY_EVOLUTION_TASKS) {
            if (!valid[evolutionSlot]) evolutionFillSlot = evolutionSlot;
            else for (int slot = DAILY_TASK_COUNT - 1; slot >= 0; slot--) {
                if (!valid[slot]) { evolutionFillSlot = slot; break; }
            }
        }
        int invalidSlots = 0;
        for (boolean present : valid) if (!present) invalidSlots++;
        int resourceOffset = positiveMod(mix64(planSeed ^ 0x94D049BB133111EBL), 2);
        for (int slot = 0; slot < DAILY_TASK_COUNT; slot++) {
            if (valid[slot]) continue;
            int needed = net.farmingtales.ftboss.util.FoodWeaponBuildRules.DAILY_EVOLUTION_TASKS - evolutionAssigned;
            boolean evolution = needed > 0 && (slot == evolutionFillSlot || invalidSlots <= needed);
            DailyRewardType reward = evolution ? DailyRewardType.EVOLUTION
                    : ((slot + resourceOffset) & 1) == 0 ? DailyRewardType.EXPERIENCE : DailyRewardType.COINS;
            if (evolution) evolutionAssigned++;
            invalidSlots--;
            CompoundTag entry = createDailyTask(player, profile, day, slot, reward, pool, selectedFoods);
            task.put(taskKey(slot), entry);
            ResourceLocation selected = dailyRequestId(entry);
            if (selected != null) selectedFoods.add(selected);
            changed = true;
        }
        return changed;
    }

    private static String taskKey(int slot) {
        return "Task" + slot;
    }

    private static boolean validDailyTask(CompoundTag task, List<ResourceLocation> pool) {
        if (task.isEmpty() || task.getInt(DAILY_REQUIRED) <= 0
                || task.getInt(DAILY_REWARD_AMOUNT) <= 0
                || DailyRewardType.byId(task.getString(DAILY_REWARD_TYPE)) == null) return false;
        String stored = task.getString(DAILY_REQUEST);
        if (stored.isBlank()) return pool.isEmpty();
        ResourceLocation id = ResourceLocation.tryParse(stored);
        return id != null && pool.contains(id);
    }

    private static CompoundTag createDailyTask(ServerPlayer player, Profile profile, long day, int slot,
                                                DailyRewardType reward, List<ResourceLocation> pool,
                                                List<ResourceLocation> selectedFoods) {
        CompoundTag task = new CompoundTag();
        if (!pool.isEmpty()) {
            long sequence = day * DAILY_TASK_COUNT + slot;
            ResourceLocation requested = null;
            for (int offset = 0; offset < pool.size(); offset++) {
                ResourceLocation candidate = pool.get(dailyPoolIndex(player, profile, sequence + offset, pool.size()));
                if (!selectedFoods.contains(candidate) || selectedFoods.size() >= pool.size()) {
                    requested = candidate;
                    break;
                }
            }
            if (requested == null) requested = pool.get(dailyPoolIndex(player, profile, sequence, pool.size()));
            task.putString(DAILY_REQUEST, requested.toString());
        }
        long seed = dailySeed(player, profile, day) ^ (slot + 1L) * 0xD1B54A32D192ED03L;
        int required = 4 + positiveMod(mix64(seed), 4) * 2;
        int grade = positiveMod(mix64(seed ^ 0x94D049BB133111EBL), 4);
        int amount = switch (reward) {
            case EVOLUTION -> 25 + grade * 5;
            case EXPERIENCE -> 900 + grade * 300;
            case COINS -> 180 + grade * 60;
        };
        task.putInt(DAILY_REQUIRED, required);
        task.putString(DAILY_REWARD_TYPE, reward.id);
        task.putInt(DAILY_REWARD_AMOUNT, amount);
        return task;
    }

    private static long dailySeed(ServerPlayer player, Profile profile, long day) {
        UUID uuid = player.getUUID();
        return mix64(uuid.getMostSignificantBits() ^ Long.rotateLeft(uuid.getLeastSignificantBits(), 23)
                ^ (profile.ordinal() + 1L) * 0x9E3779B97F4A7C15L ^ day * 0xBF58476D1CE4E5B9L);
    }

    private static synchronized List<ResourceLocation> dailyFoodPool(ServerPlayer player, FoodType type) {
        Set<ResourceLocation> recipeOutputs = dailyRecipeOutputs(player);
        List<ResourceLocation> cached = DAILY_FOOD_POOLS.get(type);
        if (cached != null) return cached;
        List<ResourceLocation> result = new ArrayList<>();
        for (ResourceLocation id : ForgeRegistries.ITEMS.getKeys()) {
            if (isDailyFoodCandidate(id, type, recipeOutputs)) result.add(id);
        }
        result.sort(ResourceLocation::compareTo);
        List<ResourceLocation> pool = List.copyOf(result);
        DAILY_FOOD_POOLS.put(type, pool);
        return pool;
    }

    /**
     * Recipes are the authoritative path for prepared mod foods. Naturally harvested foods are
     * accepted only through conservative Forge crop/fruit/meat/fish tags or a small vanilla list.
     * This deliberately excludes creative/debug foods and registered but unobtainable leftovers.
     */
    private static boolean isDailyFoodCandidate(ResourceLocation id, FoodType type,
                                                Set<ResourceLocation> recipeOutputs) {
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == Items.AIR) return false;
        ItemStack stack = new ItemStack(item);
        if (stack.isEmpty() || foodType(stack) != type) return false;
        boolean consumable = stack.isEdible() || stack.getUseAnimation() == UseAnim.DRINK
                || isNamedDrink(id.getPath(), type);
        return consumable && (recipeOutputs.contains(id) || isNaturallyObtainableDailyFood(stack, id));
    }

    private static boolean isNaturallyObtainableDailyFood(ItemStack stack, ResourceLocation id) {
        if ("minecraft".equals(id.getNamespace()) && VANILLA_NATURAL_DAILY_FOODS.contains(id.getPath())) return true;
        for (TagKey<Item> tag : DAILY_NATURAL_FOOD_TAGS) if (stack.is(tag)) return true;
        return false;
    }

    private static TagKey<Item> dailyFoodTag(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) throw new IllegalArgumentException("Invalid daily food tag: " + id);
        return TagKey.create(Registries.ITEM, location);
    }

    private static synchronized Set<ResourceLocation> dailyRecipeOutputs(ServerPlayer player) {
        RecipeManager manager = player.serverLevel().getServer().getRecipeManager();
        int fingerprint = 31 * manager.getRecipes().size()
                + manager.getRecipeIds().mapToInt(ResourceLocation::hashCode).sum();
        if (manager == dailyRecipeManager && fingerprint == dailyRecipeFingerprint) return dailyRecipeOutputs;

        Set<ResourceLocation> outputs = new HashSet<>();
        for (Recipe<?> recipe : manager.getRecipes()) {
            try {
                ItemStack result = recipe.getResultItem(player.serverLevel().registryAccess());
                if (!result.isEmpty()) outputs.add(ForgeRegistries.ITEMS.getKey(result.getItem()));
            } catch (RuntimeException ignored) {
                // A broken optional recipe must not make its output a supposedly obtainable commission item.
            }
        }
        outputs.remove(null);
        dailyRecipeManager = manager;
        dailyRecipeFingerprint = fingerprint;
        dailyRecipeOutputs = Set.copyOf(outputs);
        DAILY_FOOD_POOLS.clear();
        return dailyRecipeOutputs;
    }

    /** Covers drink mods whose custom consumables do not expose vanilla FoodProperties/UseAnim. */
    private static boolean isNamedDrink(String path, FoodType type) {
        if (containsFoodWord(path, "seed", "seeds", "leaf", "leaves", "blend", "bag", "crop", "block", "crate", "bucket",
                "spawn_egg", "powder", "bean", "mace", "charm")) return false;
        return switch (type) {
            case TEA -> containsFoodWord(path, "tea", "matcha", "oolong", "kombucha", "herbal_infusion");
            case WINE -> containsFoodWord(path, "wine", "beer", "ale", "mead", "cider", "liquor", "vodka", "whiskey",
                    "whisky", "brew", "sake", "rum", "stout", "grog", "champagne", "brandy", "bourbon",
                    "absinthe", "tequila", "sangria", "cocktail", "lager", "kvass", "gin_tonic");
            case ENERGY -> containsFoodWord(path, "coffee", "espresso", "latte", "cappuccino", "mocha", "hot_cocoa",
                    "cola", "energy_drink");
            default -> false;
        };
    }

    /** Player-specific full-cycle rotation: no repeat until every candidate in this pool was offered. */
    private static int dailyPoolIndex(ServerPlayer player, Profile profile, long sequence, int size) {
        if (size <= 1) return 0;
        UUID uuid = player.getUUID();
        long seed = mix64(uuid.getMostSignificantBits() ^ Long.rotateLeft(uuid.getLeastSignificantBits(), 23)
                ^ (profile.ordinal() + 1L) * 0x9E3779B97F4A7C15L);
        int start = positiveMod(seed, size);
        int step = 1 + positiveMod(mix64(seed ^ 0xD1B54A32D192ED03L), size - 1);
        while (greatestCommonDivisor(step, size) != 1) step = step == size - 1 ? 1 : step + 1;
        return positiveMod(start + sequence * step, size);
    }

    private static int greatestCommonDivisor(int first, int second) {
        while (second != 0) {
            int remainder = first % second;
            first = second;
            second = remainder;
        }
        return Math.abs(first);
    }

    private static int positiveMod(long value, int modulus) {
        return (int) Math.floorMod(value, (long) modulus);
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static ResourceLocation dailyRequestId(CompoundTag task) {
        String value = task.getString(DAILY_REQUEST);
        return value.isBlank() ? null : ResourceLocation.tryParse(value);
    }

    private static Component dailyRequestName(CompoundTag task, Profile profile) {
        ResourceLocation id = dailyRequestId(task);
        Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
        return item == null ? profile.food.displayName() : new ItemStack(item).getHoverName();
    }

    private static boolean matchesDailyRequest(CompoundTag task, Profile profile, ItemStack food, FoodType type) {
        ResourceLocation requested = dailyRequestId(task);
        return requested == null ? type == profile.food : requested.toString().equals(itemId(food));
    }

    public record StatueDailyView(Component request, String requestItemId, int progress,
                                  int required, Component reward, int state) {}

    /** Snapshot used by the statue UI: state 0=in progress, 1=ready, 2=claimed. */
    public static List<StatueDailyView> statueDailyViews(ServerPlayer player, int statueTint) {
        Profile profile = Profile.byTint(statueTint);
        if (profile == null) return List.of();
        CompoundTag tasks = daily(player, profile);
        List<StatueDailyView> views = new ArrayList<>(DAILY_TASK_COUNT);
        for (int slot = 0; slot < DAILY_TASK_COUNT; slot++) {
            CompoundTag task = tasks.getCompound(taskKey(slot));
            int required = Math.max(1, task.getInt(DAILY_REQUIRED));
            int progress = Math.min(required, Math.max(0, task.getInt("Progress")));
            int state = task.getBoolean("Claimed") ? 2 : task.getBoolean("Ready") ? 1 : 0;
            ResourceLocation requestId = dailyRequestId(task);
            views.add(new StatueDailyView(dailyRequestName(task, profile),
                    requestId == null ? "" : requestId.toString(), progress, required,
                    dailyRewardName(task), state));
        }
        return List.copyOf(views);
    }

    private static void showDaily(ServerPlayer player, Profile profile) {
        CompoundTag tasks = daily(player, profile);
        player.sendSystemMessage(Component.translatable("message.ftboss.evolution.daily_header", profile.displayName(),
                DAILY_TASK_COUNT).withStyle(ChatFormatting.GOLD));
        for (int slot = 0; slot < DAILY_TASK_COUNT; slot++) {
            CompoundTag task = tasks.getCompound(taskKey(slot));
            int required = task.getInt(DAILY_REQUIRED);
            int progress = Math.min(required, task.getInt("Progress"));
            Component reward = dailyRewardName(task);
            if (task.getBoolean("Claimed")) {
                player.sendSystemMessage(Component.translatable("message.ftboss.evolution.daily_task_claimed", slot + 1,
                        dailyRequestName(task, profile), reward).withStyle(ChatFormatting.DARK_GRAY));
            } else if (task.getBoolean("Ready")) {
                player.sendSystemMessage(Component.translatable("message.ftboss.evolution.daily_task_ready", slot + 1,
                        dailyRequestName(task, profile), reward).withStyle(ChatFormatting.GREEN));
            } else {
                player.sendSystemMessage(Component.translatable("message.ftboss.evolution.daily_task", slot + 1,
                        dailyRequestName(task, profile), progress, required, reward).withStyle(ChatFormatting.YELLOW));
            }
        }
        player.sendSystemMessage(Component.translatable("message.ftboss.evolution.statue_help").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static void submitDailyFood(ServerPlayer player, Profile profile, ItemStack food, FoodType type) {
        CompoundTag tasks = daily(player, profile);
        CompoundTag task = null;
        int selectedSlot = -1;
        boolean matchedCompleted = false;
        for (int slot = 0; slot < DAILY_TASK_COUNT; slot++) {
            CompoundTag candidate = tasks.getCompound(taskKey(slot));
            if (!matchesDailyRequest(candidate, profile, food, type)) continue;
            if (!candidate.getBoolean("Claimed") && !candidate.getBoolean("Ready")) {
                task = candidate;
                selectedSlot = slot;
                break;
            }
            matchedCompleted = true;
        }
        if (task == null) {
            if (matchedCompleted) {
                player.displayClientMessage(Component.translatable("message.ftboss.evolution.daily_item_done",
                        food.getHoverName()), true);
            } else {
                player.displayClientMessage(Component.translatable("message.ftboss.evolution.wrong_food_multi",
                        unfinishedDailyFoods(tasks, profile)), true);
            }
            return;
        }
        int required = task.getInt(DAILY_REQUIRED);
        int old = Math.max(0, task.getInt("Progress"));
        int amount = Math.min(food.getCount(), required - old);
        if (amount <= 0) return;
        if (!player.getAbilities().instabuild) food.shrink(amount);
        int progress = old + amount;
        task.putInt("Progress", progress);
        tasks.put(taskKey(selectedSlot), task);
        if (progress >= required) {
            DailyRewardType reward = DailyRewardType.byId(task.getString(DAILY_REWARD_TYPE));
            if (reward == DailyRewardType.EVOLUTION) {
                task.putBoolean("Ready", true);
                player.sendSystemMessage(Component.translatable("message.ftboss.evolution.daily_evolution_ready",
                        selectedSlot + 1, dailyRewardName(task)).withStyle(ChatFormatting.GOLD));
            } else {
                grantImmediateDailyReward(player, reward, task.getInt(DAILY_REWARD_AMOUNT));
                task.putBoolean("Claimed", true);
                player.sendSystemMessage(Component.translatable("message.ftboss.evolution.daily_rewarded",
                        selectedSlot + 1, dailyRewardName(task)).withStyle(ChatFormatting.GOLD));
            }
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS, 0.65F, 1.35F);
        } else {
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.daily_progress",
                    dailyRequestName(task, profile), progress, required), true);
        }
        saveDaily(player, profile, tasks);
    }

    private static Component dailyRewardName(CompoundTag task) {
        DailyRewardType type = DailyRewardType.byId(task.getString(DAILY_REWARD_TYPE));
        return type == null ? Component.literal("?") : type.displayName(task.getInt(DAILY_REWARD_AMOUNT));
    }

    private static Component unfinishedDailyFoods(CompoundTag tasks, Profile profile) {
        var result = Component.empty();
        boolean first = true;
        for (int slot = 0; slot < DAILY_TASK_COUNT; slot++) {
            CompoundTag task = tasks.getCompound(taskKey(slot));
            if (task.getBoolean("Claimed") || task.getBoolean("Ready")) continue;
            if (!first) result.append(Component.literal(" / "));
            result.append(dailyRequestName(task, profile));
            first = false;
        }
        return first ? Component.translatable("message.ftboss.evolution.daily_all_done") : result;
    }

    private static void grantImmediateDailyReward(ServerPlayer player, DailyRewardType type, int amount) {
        if (type == DailyRewardType.EXPERIENCE) {
            player.giveExperiencePoints(amount);
        } else if (type == DailyRewardType.COINS) {
            Item coin = COIN_ID == null ? null : ForgeRegistries.ITEMS.getValue(COIN_ID);
            if (coin == null || coin == Items.AIR) coin = Items.GOLD_NUGGET;
            int remaining = amount;
            while (remaining > 0) {
                ItemStack reward = new ItemStack(coin, Math.min(remaining, coin.getMaxStackSize()));
                remaining -= reward.getCount();
                if (!player.addItem(reward) && !reward.isEmpty()) player.drop(reward, false);
            }
        }
    }

    private static CompoundTag readyEvolutionTask(CompoundTag tasks) {
        for (int slot = 0; slot < DAILY_TASK_COUNT; slot++) {
            CompoundTag task = tasks.getCompound(taskKey(slot));
            if (task.getBoolean("Ready") && !task.getBoolean("Claimed")
                    && DailyRewardType.byId(task.getString(DAILY_REWARD_TYPE)) == DailyRewardType.EVOLUTION) return task;
        }
        return null;
    }

    private static void handleWeapon(ServerPlayer player, Profile profile, ItemStack weapon, boolean evolve) {
        Profile bound = boundProfile(weapon);
        if (bound == null) {
            if (!attune(weapon, profile)) {
                player.displayClientMessage(Component.translatable("message.ftboss.evolution.not_weapon"), true);
                return;
            }
            player.sendSystemMessage(Component.translatable("message.ftboss.evolution.attuned", weapon.getHoverName(), profile.displayName())
                    .withStyle(ChatFormatting.AQUA));
        }

        CompoundTag tasks = daily(player, profile);
        CompoundTag task = readyEvolutionTask(tasks);
        // Sneak-use must always reach the evolution branch; otherwise a completed daily at the XP cap
        // would intercept the click, add 0 XP and make the weapon impossible to evolve.
        if (!evolve && task != null) {
            int added = addExperience(weapon, profile, task.getInt(DAILY_REWARD_AMOUNT));
            if (added <= 0) {
                player.displayClientMessage(Component.translatable("message.ftboss.evolution.maxed"), true);
                return;
            }
            task.putBoolean("Claimed", true);
            saveDaily(player, profile, tasks);
            player.sendSystemMessage(Component.translatable("message.ftboss.evolution.claimed", weapon.getHoverName(), added,
                    writableData(weapon, profile).getInt(XP)).withStyle(ChatFormatting.GREEN));
            return;
        }
        if (evolve) player.displayClientMessage(Component.translatable("message.ftboss.evolution.offering_required"), true);
        else showWeapon(player, weapon, profile);
    }

    static void completeEvolution(ServerPlayer player, Profile profile, ItemStack weapon) {
        CompoundTag data = writableData(weapon, profile);
        int level = Math.max(0, data.getInt(LEVEL));
        int max = maxLevel(weapon);
        if (level >= max) {
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.maxed"), true);
            return;
        }
        int need = threshold(weapon, level);
        int xp = Math.max(0, data.getInt(XP));
        if (xp < need) {
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.need_xp", xp, need), true);
            return;
        }
        FoodWeaponRepair.invalidateForProgression(weapon);
        level++;
        data.putInt(LEVEL, level);
        data.remove(BLESSING_WASHED);
        boolean gourmet = isGourmetEvolutionWeapon(weapon);
        if (!gourmet && level == 2) data.putString(SECOND_PROFILE, profile.id);
        EvolutionSkill skill = null;
        EvolutionStat stat = null;
        if (gourmet) {
            data.putString(GOURMET_ACTIVE_PROFILE, profile.id);
            skill = drawGourmetSkill(weapon, profile, player.getRandom());
            if (skill != null) {
                ListTag skills = data.getList(GOURMET_SKILLS, Tag.TAG_COMPOUND);
                CompoundTag entry = new CompoundTag();
                entry.putString(PROFILE, profile.id);
                entry.putString(SKILL, skill.id);
                skills.add(entry);
                data.put(GOURMET_SKILLS, skills);
                data.putInt(GOURMET_ACTIVE_SKILL, Math.max(0, gourmetSkills(weapon).size() - 1));
            }
            stat = EvolutionStat.random(weaponRole(weapon), player.getRandom());
            CompoundTag stats = data.getCompound(GOURMET_STATS);
            stats.putDouble(stat.id, Math.max(0.0, stats.getDouble(stat.id)) + stat.gain);
            data.put(GOURMET_STATS, stats);
            net.farmingtales.ftboss.item.weapons.GourmetEvolutionWeapon.animateEvolution(player, weapon);
        }
        playEvolutionSuccessParticles(player, gourmet);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 1.0F, 0.75F + Math.min(level, 5) * 0.12F);
        Component skillName;
        if (skill != null) {
            skillName = skill.nameComponent();
        } else {
            var first = net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Aspect.fromId(boundProfile(weapon).id);
            if (level == 1 || first == null) {
                skillName = first == null ? Component.translatable("message.ftboss.evolution.native_skill_up")
                        : Component.translatable(first.translationKey());
            } else {
                var second = net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Aspect.fromId(profile.id);
                var fusion = net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.fusion(first, second);
                skillName = fusion == null ? Component.translatable("message.ftboss.evolution.native_skill_up")
                        : Component.translatable("message.ftboss.evolution.fusion_unlock",
                        Component.translatable(fusion.primaryTranslationKey()),
                        Component.translatable(fusion.secondaryTranslationKey()));
            }
        }
        player.sendSystemMessage(Component.translatable("message.ftboss.evolution.evolved", weapon.getHoverName(), level,
                skillName, socketCapacityInternal(weapon)).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (gourmet && stat != null) {
            player.sendSystemMessage(Component.translatable("message.ftboss.gourmet.stat_gained",
                    stat.displayName(stat.gain)).withStyle(ChatFormatting.GOLD));
        }
        FTBossMod.LOGGER.info("[FTBoss][食武进化] {} 的 {} 在 {} 石像进化到 {} 级, skill={}, stat={}, slots={}",
                player.getName().getString(), itemId(weapon), profile.id, level,
                skill == null ? "native+1" : skill.id, stat == null ? "none" : stat.id, socketCapacityInternal(weapon));
    }

    /** Emits a restrained, deterministic halo only after the evolution state has been committed. */
    private static void playEvolutionSuccessParticles(ServerPlayer player, boolean gourmet) {
        ServerLevel level = player.serverLevel();
        int points = gourmet ? 16 : 12;
        double radius = gourmet ? 0.74D : 0.62D;
        double centerY = player.getY() + 0.95D;
        for (int point = 0; point < points; point++) {
            double angle = Math.PI * 2.0D * point / points;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            double y = centerY + 0.12D * Math.sin(angle * 2.0D);
            level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1,
                    0.015D, 0.015D, 0.015D, 0.01D);
        }
        level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(),
                gourmet ? 24 : 16, 0.32D, 0.45D, 0.32D, 0.06D);
    }

    private static void showWeapon(ServerPlayer player, ItemStack weapon, Profile profile) {
        CompoundTag data = writableData(weapon, profile);
        int level = evolutionLevel(weapon);
        int max = maxLevel(weapon);
        int xp = Math.max(0, data.getInt(XP));
        int next = level >= max ? xp : threshold(weapon, level);
        player.sendSystemMessage(Component.translatable("message.ftboss.evolution.weapon_status", weapon.getHoverName(),
                level, max, xp, next, sockets(weapon).size(), socketCapacityInternal(weapon)).withStyle(ChatFormatting.AQUA));
        if (level < max && xp >= next) player.sendSystemMessage(Component.translatable("message.ftboss.evolution.can_evolve")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    public static ItemStack heldEvolutionWeapon(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (weaponCandidate(main)) return main;
        ItemStack off = player.getOffhandItem();
        return weaponCandidate(off) ? off : ItemStack.EMPTY;
    }

    /** Server-validated blessing click from the statue's enchantment page. */
    public static boolean applyFoodBlessingFromUi(ServerPlayer player, int statueTint, int inventorySlot) {
        Profile profile = Profile.byTint(statueTint);
        ItemStack armor = ArmorFoodBlessings.heldArmor(player);
        ItemStack weapon = armor.isEmpty() ? heldEvolutionWeapon(player) : armor;
        if (profile == null || weapon.isEmpty() || inventorySlot < 0 || inventorySlot > 40
                || (inventorySlot >= 36 && inventorySlot < 40)) return false;
        if (weapon.is(Items.ENCHANTED_BOOK)) {
            player.displayClientMessage(Component.translatable("message.ftboss.food_blessing.no_books"), true);
            return false;
        }
        ItemStack offered = player.getInventory().getItem(inventorySlot);
        FoodType type = foodType(offered);
        if (offered.isEmpty() || offered == weapon || weaponCandidate(offered) || type == null) {
            player.displayClientMessage(Component.translatable("message.ftboss.food_blessing.wrong_food",
                    profile.food.displayName()), true);
            return false;
        }
        return applyFoodBlessing(player, profile, weapon, offered, type);
    }

    /** Server-validated click from the statue accessory picker. */
    public static boolean insertSocketFromUi(ServerPlayer player, int statueTint, int inventorySlot) {
        Profile statueProfile = Profile.byTint(statueTint);
        ItemStack weapon = heldEvolutionWeapon(player);
        if (statueProfile == null || weapon.isEmpty() || inventorySlot < 0 || inventorySlot > 40
                || (inventorySlot >= 36 && inventorySlot < 40)) return false;
        ItemStack food = player.getInventory().getItem(inventorySlot);
        FoodType type = foodType(food);
        if (food.isEmpty() || food == weapon || type == null) return false;
        int before = socketCount(weapon);
        insertSocket(player, statueProfile, weapon, food, type);
        return socketCount(weapon) > before;
    }

    /** Server-validated food submission from the daily-task inventory grid. */
    public static boolean submitDailyFromUi(ServerPlayer player, int statueTint, int inventorySlot) {
        Profile profile = Profile.byTint(statueTint);
        if (profile == null || inventorySlot < 0 || inventorySlot > 40
                || (inventorySlot >= 36 && inventorySlot < 40)) return false;
        ItemStack food = player.getInventory().getItem(inventorySlot);
        FoodType type = foodType(food);
        if (food.isEmpty() || type == null) {
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.wrong_food"), true);
            return false;
        }
        submitDailyFood(player, profile, food, type);
        return true;
    }

    /** Server-validated evolution click for the weapon held in either hand. */
    public static boolean evolveWeaponFromUi(ServerPlayer player, int statueTint, int inventorySlot) {
        return FoodWeaponProgression.evolve(player, statueTint, inventorySlot);
    }

    public static boolean evolveWeaponFromUi(ServerPlayer player, int statueTint) {
        player.displayClientMessage(Component.translatable("message.ftboss.evolution.offering_required"), true);
        return false;
    }

    /** Switches the blade's J active to one skill record earned in the current eight-evolution run. */
    public static boolean selectGourmetSkillFromUi(ServerPlayer player, int skillIndex) {
        ItemStack weapon = heldEvolutionWeapon(player);
        Profile bound = boundProfile(weapon);
        if (!weapon.isEmpty() && !isGourmetEvolutionWeapon(weapon)) return FoodWeaponProgression.select(player, skillIndex);
        if (weapon.isEmpty() || bound == null) {
            player.displayClientMessage(Component.translatable("message.ftboss.gourmet.switch_wrong_weapon"), true);
            return false;
        }
        List<AcquiredSkill> acquired = gourmetSkills(weapon);
        if (skillIndex < 0 || skillIndex >= acquired.size() || skillIndex >= GOURMET_MAX_LEVEL) {
            player.displayClientMessage(Component.translatable("message.ftboss.gourmet.switch_unavailable"), true);
            return false;
        }
        AcquiredSkill selected = acquired.get(skillIndex);
        CompoundTag data = writableData(weapon, bound);
        data.putInt(GOURMET_ACTIVE_SKILL, skillIndex);
        data.putString(GOURMET_ACTIVE_PROFILE, selected.profile.id);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.75F, 1.15F + skillIndex * 0.04F);
        player.displayClientMessage(Component.translatable("message.ftboss.gourmet.skill_switched",
                skillIndex + 1, selected.profile.displayName()), true);
        FTBossMod.LOGGER.info("[FTBoss][万味胚刃] {} 切换当前进化技能到 #{} ({})",
                player.getName().getString(), skillIndex + 1, selected.profile.id);
        return true;
    }

    /** Claims one completed evolution commission into the weapon held in either hand. */
    public static boolean claimEvolutionFromUi(ServerPlayer player, int statueTint) {
        Profile profile = Profile.byTint(statueTint);
        ItemStack weapon = heldEvolutionWeapon(player);
        if (profile == null || weapon.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.not_weapon"), true);
            return false;
        }
        handleWeapon(player, profile, weapon, false);
        return true;
    }

    /** Removes the clicked accessory slot; shears may be anywhere in the inventory. */
    public static boolean removeSocketFromUi(ServerPlayer player, int statueTint, int socketIndex) {
        Profile statueProfile = Profile.byTint(statueTint);
        ItemStack weapon = heldEvolutionWeapon(player);
        Profile profile = boundProfile(weapon);
        if (statueProfile == null || weapon.isEmpty() || profile == null) return false;
        CompoundTag data = writableData(weapon, profile);
        ListTag list = data.getList(SOCKETS, Tag.TAG_COMPOUND);
        if (socketIndex < 0 || socketIndex >= list.size()) return false;
        ItemStack shears = ItemStack.EMPTY;
        for (int slot = 0; slot <= 40; slot++) {
            if (slot >= 36 && slot < 40) continue;
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.is(Items.SHEARS)) { shears = candidate; break; }
        }
        if (shears.isEmpty() && !player.getAbilities().instabuild) {
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.socket_need_shears"), true);
            return false;
        }
        CompoundTag removed = list.getCompound(socketIndex);
        list.remove(socketIndex);
        data.put(SOCKETS, list);
        String identifier = removed.getString("Item");
        ResourceLocation id = ResourceLocation.tryParse(identifier);
        Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
        ItemStack returned = removed.contains("Stack", Tag.TAG_COMPOUND)
                ? ItemStack.of(removed.getCompound("Stack"))
                : item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        if (!returned.isEmpty() && !player.getInventory().add(returned)) player.drop(returned, false);
        if (!player.getAbilities().instabuild) shears.hurtAndBreak(1, player, holder -> { });
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.GRINDSTONE_USE,
                SoundSource.PLAYERS, 0.7F, 1.25F);
        SocketTier tier = SocketTier.byLevel(removed.contains("Tier", Tag.TAG_INT)
                ? removed.getInt("Tier") : foodTierById(identifier).level);
        player.sendSystemMessage(Component.translatable("message.ftboss.evolution.socket_removed",
                socketItemName(identifier), tier.displayName(), list.size(), socketCapacityInternal(weapon))
                .withStyle(ChatFormatting.GREEN));
        return true;
    }

    private static void insertSocket(ServerPlayer player, Profile statueProfile, ItemStack weapon, ItemStack food, FoodType type) {
        Profile profile = boundProfile(weapon);
        if (profile == null) {
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.socket_attune_first"), true);
            return;
        }
        List<Socket> current = sockets(weapon);
        int capacity = unlockedSocketCapacity(weapon);
        if (current.size() >= capacity) {
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.socket_full", capacity), true);
            return;
        }
        CompoundTag data = writableData(weapon, profile);
        ListTag list = data.getList(SOCKETS, Tag.TAG_COMPOUND);
        Component ingredientName = food.getHoverName().copy();
        SocketTier tier = foodTier(food);
        CompoundTag entry = new CompoundTag();
        entry.putString("Type", type.name());
        entry.putString("Item", itemId(food));
        entry.putInt("Tier", tier.level);
        ItemStack storedFood = food.copy();
        storedFood.setCount(1);
        entry.put("Stack", storedFood.save(new CompoundTag()));
        list.add(entry);
        data.put(SOCKETS, list);
        if (isGourmetEvolutionWeapon(weapon)) {
            net.farmingtales.ftboss.item.weapons.GourmetEvolutionWeapon.animateSocket(player, weapon);
            player.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(),
                    12, 0.35, 0.45, 0.35, 0.06);
        }
        if (!player.getAbilities().instabuild) food.shrink(1);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE,
                SoundSource.PLAYERS, 0.45F, 1.55F);
        player.sendSystemMessage(Component.translatable("message.ftboss.evolution.socketed", ingredientName, type.displayName(),
                tier.displayName(), list.size(), capacity).withStyle(type == profile.food ? ChatFormatting.GOLD : ChatFormatting.GREEN));
        player.sendSystemMessage(Component.translatable("message.ftboss.evolution.socket_effect",
                type.displayName(), tier.displayName(), formatNumber(tier.multiplier
                        * 1.5D * weaponRole(weapon).socketMultiplier(type)), type.effectDescription())
                .withStyle(ChatFormatting.GRAY));
        SocketStats socketStats = socketStats(weapon);
        int setCount = socketStats.setCount(type);
        player.sendSystemMessage(Component.translatable("message.ftboss.evolution.socket_set_progress",
                Math.min(3, setCount), SocketTier.byLevel(socketStats.setTier(type)).displayName(),
                type.setDescription()).withStyle(setCount >= 3 ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
    }

    private static void removeSocket(ServerPlayer player, Profile statueProfile, ItemStack weapon, ItemStack shears,
                                     InteractionHand shearsHand) {
        Profile profile = boundProfile(weapon);
        if (profile == null) {
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.socket_attune_first"), true);
            return;
        }
        CompoundTag data = writableData(weapon, profile);
        ListTag list = data.getList(SOCKETS, Tag.TAG_COMPOUND);
        if (list.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.socket_empty"), true);
            return;
        }
        CompoundTag removed = list.getCompound(list.size() - 1);
        list.remove(list.size() - 1);
        data.put(SOCKETS, list);
        String identifier = removed.getString("Item");
        ResourceLocation id = ResourceLocation.tryParse(identifier);
        Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
        ItemStack returned = removed.contains("Stack", Tag.TAG_COMPOUND)
                ? ItemStack.of(removed.getCompound("Stack"))
                : item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        if (!returned.isEmpty() && !player.getInventory().add(returned)) player.drop(returned, false);
        if (!player.getAbilities().instabuild) {
            shears.hurtAndBreak(1, player, holder -> holder.broadcastBreakEvent(shearsHand));
        }
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.GRINDSTONE_USE,
                SoundSource.PLAYERS, 0.7F, 1.25F);
        SocketTier tier = SocketTier.byLevel(removed.contains("Tier", Tag.TAG_INT)
                ? removed.getInt("Tier") : foodTierById(identifier).level);
        player.sendSystemMessage(Component.translatable("message.ftboss.evolution.socket_removed",
                socketItemName(identifier), tier.displayName(), list.size(), socketCapacityInternal(weapon))
                .withStyle(ChatFormatting.GREEN));
    }

    @SubscribeEvent
    public static void attributes(ItemAttributeModifierEvent event) {
        FoodBlessingCombat.attributes(event);
    }

    public static void growthAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!EquipmentAttributeSlots.growthSlot(event.getSlotType(), stack)) return;
        if (boundProfile(stack) == null || FoodWeaponLifecycle.isBroken(stack)) return;
        addFoodBlessingAttributes(event, stack);
        boolean gourmet = isGourmetEvolutionWeapon(stack);
        int level = evolutionLevel(stack);
        WeaponRole role = weaponRole(stack);
        SocketStats socketStats = socketStats(stack);
        double meat = socketStats.effectPower(FoodType.MEAT, role);
        double wine = socketStats.effectPower(FoodType.WINE, role);
        double energy = socketStats.effectPower(FoodType.ENERGY, role);
        double tea = socketStats.effectPower(FoodType.TEA, role);
        Profile profile = activeProfile(stack);
        double matching = profile == null ? 0.0D : socketStats.effectPower(profile.food, role);
        double cheese = socketStats.effectPower(FoodType.CHEESE, role);
        double vegetable = socketStats.effectPower(FoodType.VEGETABLE, role);
        double apple = socketStats.effectPower(FoodType.APPLE, role);
        double fish = socketStats.effectPower(FoodType.FISH, role);
        double sweet = socketStats.effectPower(FoodType.SWEET, role) + socketStats.effectPower(FoodType.FOOD, role);
        double damage = evolutionDamageBonus(role, level, gourmet)
                + (gourmet ? gourmetStat(stack, EvolutionStat.ATTACK_DAMAGE) : 0.0D)
                + meat + wine * 0.5D + energy * 0.5D + matching * 0.5D;
        double speed = evolutionSpeedBonus(role, level, gourmet)
                + (gourmet ? gourmetStat(stack, EvolutionStat.ATTACK_SPEED) : 0.0D)
                + tea * 0.05D + energy * 0.02D;
        double armor = evolutionArmorBonus(role, level, gourmet) + cheese
                + (gourmet ? gourmetStat(stack, EvolutionStat.ARMOR) : 0.0D);
        double health = evolutionHealthBonus(role, level, gourmet) + apple * 2.0D
                + (gourmet ? gourmetStat(stack, EvolutionStat.MAX_HEALTH) : 0.0D);
        double movement = evolutionMovementBonus(role, level, gourmet) + fish * 0.03D
                + (gourmet ? gourmetStat(stack, EvolutionStat.MOVEMENT_SPEED) : 0.0D);
        double luck = evolutionLuckBonus(role, level, gourmet) + sweet
                + (gourmet ? gourmetStat(stack, EvolutionStat.LUCK) : 0.0D);
        if (damage > 0) event.addModifier(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(EquipmentAttributeSlots.modifierId(EVOLUTION_DAMAGE, stack), "ftboss.food_evolution.damage", damage, AttributeModifier.Operation.ADDITION));
        if (speed > 0) event.addModifier(Attributes.ATTACK_SPEED,
                new AttributeModifier(EquipmentAttributeSlots.modifierId(EVOLUTION_SPEED, stack), "ftboss.food_evolution.speed", speed, AttributeModifier.Operation.ADDITION));
        if (armor > 0) event.addModifier(Attributes.ARMOR,
                new AttributeModifier(EquipmentAttributeSlots.modifierId(SOCKET_ARMOR, stack), "ftboss.food_socket.cheese", armor, AttributeModifier.Operation.ADDITION));
        if (vegetable > 0) event.addModifier(Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(EquipmentAttributeSlots.modifierId(SOCKET_TOUGHNESS, stack), "ftboss.food_socket.vegetable", vegetable * 0.5, AttributeModifier.Operation.ADDITION));
        if (health > 0) event.addModifier(Attributes.MAX_HEALTH,
                new AttributeModifier(EquipmentAttributeSlots.modifierId(SOCKET_HEALTH, stack), "ftboss.food_socket.apple", health, AttributeModifier.Operation.ADDITION));
        if (movement > 0) event.addModifier(Attributes.MOVEMENT_SPEED,
                new AttributeModifier(EquipmentAttributeSlots.modifierId(SOCKET_MOVEMENT, stack), "ftboss.food_socket.fish", movement, AttributeModifier.Operation.MULTIPLY_BASE));
        if (luck > 0) event.addModifier(Attributes.LUCK,
                new AttributeModifier(EquipmentAttributeSlots.modifierId(SOCKET_LUCK, stack), "ftboss.food_socket.sweet", luck, AttributeModifier.Operation.ADDITION));
    }

    @SubscribeEvent
    public static void toolBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack stack = event.getEntity().getMainHandItem();
        WeaponRole role = weaponRole(stack);
        if (boundProfile(stack) == null || FoodWeaponLifecycle.isBroken(stack)
                || role != WeaponRole.TOOL || event.getNewSpeed() <= 0.0F) return;
        boolean gourmet = isGourmetEvolutionWeapon(stack);
        SocketStats socketStats = socketStats(stack);
        double levelBonus = evolutionLevel(stack) * (gourmet ? 0.05D : 0.15D);
        double socketBonus = socketStats.effectPower(FoodType.TEA, role) * 0.10D
                + socketStats.effectPower(FoodType.ENERGY, role) * 0.12D
                + socketStats.effectPower(FoodType.VEGETABLE, role) * 0.06D;
        double multiplier = 1.0D + levelBonus + Math.min(2.0D, socketBonus);
        event.setNewSpeed((float) (event.getNewSpeed() * multiplier));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void weaponHit(LivingHurtEvent event) {
        if (Boolean.TRUE.equals(EXTRA_DAMAGE.get()) || event.getAmount() <= 0
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || event.getEntity() == player) return;
        ItemStack weapon = FoodWeaponRanged.sourceWeapon(event.getSource(), player);
        Profile profile = activeProfile(weapon);
        if (profile == null || FoodWeaponLifecycle.isBroken(weapon)) return;
        LivingEntity target = event.getEntity();
        CompoundTag data = writableData(weapon, profile);
        FoodWeaponRanged.prepareHitTimers(player, event.getSource(), data);
        long now = player.level().getGameTime();
        applyFoodBlessingHits(event, player, target, data, now, weapon);
        WeaponRole role = weaponRole(weapon);
        SocketStats socketStats = socketStats(weapon);
        if (role == WeaponRole.RANGED
                && event.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile) {
            double levelBonus = evolutionLevel(weapon) * (isGourmetEvolutionWeapon(weapon) ? 0.05D : 0.12D);
            double rangedPower = socketStats.effectPower(FoodType.WINE, role)
                    + socketStats.effectPower(FoodType.FISH, role) + socketStats.effectPower(FoodType.ENERGY, role);
            double socketBonus = Math.min(0.60D, rangedPower * 0.04D);
            event.setAmount((float) (event.getAmount() * (1.0D + levelBonus + socketBonus)));
        }

        double wine = socketStats.effectPower(FoodType.WINE, role);
        if (wine > 0 && player.getRandom().nextFloat() < Math.min(0.75D, wine * 0.07D)) {
            event.setAmount(event.getAmount() * 1.5F);
        }
        double vegetable = socketStats.effectPower(FoodType.VEGETABLE, role);
        int vegetableCount = socketStats.count(FoodType.VEGETABLE);
        if (vegetable > 0 && player.getRandom().nextFloat() < Math.min(0.85D, vegetable * 0.15D)) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 40 + (int) Math.round(vegetable * 20.0D),
                    Math.min(1, vegetableCount - 1)), player);
        }
        double meat = socketStats.effectPower(FoodType.MEAT, role);
        if (meat > 0 && now - data.getLong(LAST_SOCKET_PROC) >= ON_HIT_LIFESTEAL_COOLDOWN_TICKS) {
            player.heal((float) (meat * 0.35D));
            data.putLong(LAST_SOCKET_PROC, now);
        }
        if (socketStats.setCount(FoodType.SWEET) >= 3 && now - data.getLong("LastSweetGuard") >= 100) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 80,
                    socketStats.setTier(FoodType.SWEET) - 1), player);
            data.putLong("LastSweetGuard", now);
        }
        if (socketStats.count(FoodType.ENERGY) >= 3 && now - data.getLong("LastEnergyArc") >= 40) {
            chainArc(player, target, 4.0F + 2.0F * (socketStats.setTier(FoodType.ENERGY) - 1));
            data.putLong("LastEnergyArc", now);
        }
        if (profile.food == FoodType.CHEESE && socketStats.count(FoodType.CHEESE) >= 3) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80,
                    socketStats.setTier(FoodType.CHEESE) - 1), player);
        }
        if (profile.food == FoodType.WINE && socketStats.count(FoodType.WINE) >= 3) {
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION,
                    80 + 40 * (socketStats.setTier(FoodType.WINE) - 1), 0), player);
        }

        if (isGourmetEvolutionWeapon(weapon) && evolutionLevel(weapon) > 0) {
            for (AcquiredSkill acquired : gourmetSkills(weapon)) {
                applyEvolutionSkill(event, player, target, data, acquired.skill, now,
                        LAST_SKILL_PROC + "." + acquired.skill.id,
                        (float) FoodWeaponSkillRarity.passiveMultiplier(weapon, acquired.skill.id));
            }
        } else if (evolutionLevel(weapon) > 0) {
            for (EvolutionSkill skill : FoodWeaponProgression.passiveSkills(weapon)) {
                applyEvolutionSkill(event, player, target, data, skill, now, LAST_SKILL_PROC + "." + skill.id,
                        (float) FoodWeaponSkillRarity.passiveMultiplier(weapon, skill.id));
            }
        }
        FoodWeaponRanged.saveHitTimers(player, event.getSource(), data);
    }

    private static void applyEvolutionSkill(LivingHurtEvent event, ServerPlayer player, LivingEntity target,
                                             CompoundTag data, EvolutionSkill skill, long now, String cooldownKey, float skillPower) {
        int cooldownTicks = skill.effect == SkillEffect.LIFESTEAL ? ON_HIT_LIFESTEAL_COOLDOWN_TICKS : 20;
        boolean ready = now - data.getLong(cooldownKey) >= cooldownTicks;
        boolean proc = ready && player.getRandom().nextFloat() < 0.28F;
        switch (skill.effect) {
            case DAMAGE -> event.setAmount(event.getAmount() * (1.0F + 0.18F * skillPower));
            case CRIT -> {
                if (proc) event.setAmount(event.getAmount() * (1.0F + 0.6F * skillPower));
            }
            case LIFESTEAL -> {
                if (ready) { player.heal(1.0F); proc = true; }
            }
            case SLOW -> {
                if (proc) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1), player);
            }
            case POISON -> {
                if (proc) target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), player);
            }
            case FIRE -> {
                if (proc) target.setSecondsOnFire(4);
            }
            case GUARD -> {
                if (proc) player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0), player);
            }
            case HASTE -> {
                if (proc) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0), player);
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 100, 0), player);
                }
            }
            case WEAKEN -> {
                if (proc) target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0), player);
            }
            case KNOCKBACK -> {
                if (proc) knockAway(target, player.position(), 0.85);
            }
            case CHAIN -> {
                if (proc) chainArc(player, target, 5.0F * skillPower);
            }
            case REGEN -> {
                if (proc) player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0), player);
            }
        }
        if (proc) data.putLong(cooldownKey, now);
    }

    private static boolean canTarget(ServerPlayer player, LivingEntity target) {
        if (target == player || !target.isAlive() || target.isRemoved() || player.isAlliedTo(target)
                || target.isAlliedTo(player) || player.isPassengerOfSameVehicle(target)) return false;
        if (target instanceof net.minecraft.world.entity.player.Player other) {
            return !other.isCreative() && !other.isSpectator()
                    && player.serverLevel().getServer().isPvpAllowed() && player.canHarmPlayer(other);
        }
        return true;
    }

    private static void knockAway(LivingEntity target, net.minecraft.world.phys.Vec3 origin, double strength) {
        net.minecraft.world.phys.Vec3 direction = target.position().subtract(origin).multiply(1, 0, 1).normalize();
        double resistance = Math.max(0, Math.min(1, target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
        double magnitude = strength * (1 - resistance);
        if (direction.lengthSqr() < 1.0E-6 || magnitude <= 0) return;
        target.push(direction.x * magnitude, 0.18 * (1 - resistance), direction.z * magnitude);
        target.hurtMarked = true;
    }
    private static void chainArc(ServerPlayer player, LivingEntity origin, float damage) {
        if (!(origin.level() instanceof ServerLevel level)) return;
        LivingEntity next = level.getEntitiesOfClass(LivingEntity.class, origin.getBoundingBox().inflate(4.0),
                        target -> target != origin && canTarget(player, target))
                .stream().min(java.util.Comparator.comparingDouble(origin::distanceToSqr)).orElse(null);
        if (next == null) return;
        EXTRA_DAMAGE.set(true);
        try {
            next.invulnerableTime = 0;
            next.hurt(level.damageSources().magic(), damage);
        } finally {
            EXTRA_DAMAGE.set(false);
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, next.getX(), next.getY() + next.getBbHeight() * 0.5,
                next.getZ(), 12, 0.22, 0.35, 0.22, 0.12);
    }

    private static void overloadBurst(ServerPlayer player, LivingEntity origin, float damage) {
        if (!(origin.level() instanceof ServerLevel level)) return;
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                origin.getBoundingBox().inflate(3.0D), target -> target != origin && canTarget(player, target));
        EXTRA_DAMAGE.set(true);
        try {
            for (LivingEntity target : targets) {
                target.invulnerableTime = 0;
                target.hurt(level.damageSources().magic(), damage);
            }
        } finally {
            EXTRA_DAMAGE.set(false);
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, origin.getX(), origin.getY() + origin.getBbHeight() * 0.5D,
                origin.getZ(), 24 + targets.size() * 4, 1.1D, 0.7D, 1.1D, 0.16D);
    }

    @SubscribeEvent
    public static void heldEffects(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 40 != 0) return;
        ItemStack weapon = player.getMainHandItem();
        if (boundProfile(weapon) == null || FoodWeaponLifecycle.isBroken(weapon)) return;
        SocketStats socketStats = socketStats(weapon);
        if (socketStats.count(FoodType.FISH) >= 3 && player.isInWaterRainOrBubble()) {
            int amplifier = socketStats.setTier(FoodType.FISH) - 1;
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 80, amplifier, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, amplifier, false, false, true));
        }
        if (socketStats.count(FoodType.TEA) >= 3) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60,
                    socketStats.setTier(FoodType.TEA) - 1, false, false, true));
        }
        Profile profile = activeProfile(weapon);
        if (profile != null && socketStats.count(profile.food) >= 3) {
            int amplifier = socketStats.setTier(profile.food) - 1;
            switch (profile.food) {
                case MEAT -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, amplifier, false, false, true));
                case APPLE -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, amplifier, false, false, true));
                case VEGETABLE -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, amplifier, false, false, true));
                default -> { }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void foodBlessingKillFeast(LivingDeathEvent event) {
        FoodBlessingCombat.killFeast(event);
    }
    /** Kill-triggered food-module effects remain; boss kills no longer award evolution experience. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void socketKillEffects(LivingDeathEvent event) {
        if (event.isCanceled()) return;
        ServerPlayer player = resolvePlayer(event.getSource().getEntity());
        if (player == null || LemonSummonRewards.suppressed(event.getEntity())) return;
        ItemStack held = player.getMainHandItem();
        if (boundProfile(held) == null || FoodWeaponLifecycle.isBroken(held)) return;
        WeaponRole role = weaponRole(held);
        SocketStats socketStats = socketStats(held);
        double apples = socketStats.effectPower(FoodType.APPLE, role);
        int meat = socketStats.count(FoodType.MEAT);
        if (apples > 0 || meat >= 3) player.heal((float) (apples * 2.0D
                + (meat >= 3 ? 2.0D * socketStats.setTier(FoodType.MEAT) : 0.0D)));
    }

    private static ServerPlayer resolvePlayer(Entity source) {
        if (source instanceof ServerPlayer player) return player;
        if (source instanceof net.minecraft.world.entity.projectile.Projectile projectile
                && projectile.getOwner() instanceof ServerPlayer player) return player;
        return null;
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        if (!FoodAppraisalMonocleItem.isEquipped(event.getEntity())) return;
        ItemStack stack = event.getItemStack();
        boolean gourmet = isGourmetEvolutionWeapon(stack);
        Profile profile = boundProfile(stack);
        FoodType ingredientType = profile == null && !gourmet ? foodType(stack) : null;
        if (profile == null && !gourmet && ingredientType == null) return;

        // The monocle only identifies ingredients and gives a compact weapon summary. Full skills, never rewrite or interleave
        // rolled stats, blessings and socket effects live in the statue dossier so tooltips stay on-screen.
        List<Component> tip = event.getToolTip();
        tip.add(Component.empty());
        tip.add(Component.translatable("tooltip.ftboss.food_appraisal_monocle.readout")
                .withStyle(ChatFormatting.DARK_AQUA));
        if (profile == null) {
            if (gourmet) {
                tip.add(Component.translatable("tooltip.ftboss.gourmet.unattuned", GOURMET_MAX_LEVEL,
                        GOURMET_SOCKET_CAPACITY).withStyle(ChatFormatting.AQUA));
                tip.add(Component.translatable("tooltip.ftboss.evolution.ui_hint").withStyle(ChatFormatting.DARK_GRAY));
                return;
            }
            addSocketIngredientTooltip(tip, stack, ingredientType);
            return;
        }
        int level = evolutionLevel(stack);
        int max = maxLevel(stack);
        int xp = evolutionExperience(stack);
        int next = level >= max ? xp : threshold(stack, level);
        tip.add(Component.translatable("tooltip.ftboss.evolution.progress", profile.displayName(), level, max, xp, next)
                .withStyle(ChatFormatting.AQUA));
        tip.add(Component.translatable("tooltip.ftboss.evolution.sockets", socketCount(stack), socketCapacityInternal(stack))
                .withStyle(ChatFormatting.GREEN));
        tip.add(Component.translatable("tooltip.ftboss.evolution.ui_hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) return Integer.toString((int) Math.rint(value));
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static void addWeaponRoleTooltip(List<Component> tip, ItemStack stack) {
        WeaponRole role = weaponRole(stack);
        tip.add(Component.translatable("tooltip.ftboss.evolution.role", role.displayName())
                .withStyle(ChatFormatting.GOLD));
        tip.add(role.bonusDescription().copy().withStyle(ChatFormatting.GRAY));
    }

    private static void addSocketIngredientTooltip(List<Component> tip, ItemStack stack, FoodType type) {
        SocketTier tier = foodTier(stack);
        tip.add(Component.translatable("tooltip.ftboss.socket.material", type.displayName())
                .withStyle(ChatFormatting.GOLD));
        tip.add(Component.translatable("tooltip.ftboss.socket.tier", tier.displayName(), formatNumber(tier.multiplier))
                .withStyle(tier == SocketTier.GOURMET ? ChatFormatting.LIGHT_PURPLE
                        : tier == SocketTier.FINE ? ChatFormatting.AQUA : ChatFormatting.GRAY));
        tip.add(type.effectDescription().copy().withStyle(ChatFormatting.GRAY));
        tip.add(Component.translatable("tooltip.ftboss.socket.set_preview", type.setDescription())
                .withStyle(ChatFormatting.DARK_GREEN));
    }

    private static void addCurrentSocketEffects(List<Component> tip, ItemStack stack, Profile profile) {
        tip.add(Component.translatable("tooltip.ftboss.socket.current").withStyle(ChatFormatting.GOLD));
        boolean dessertSetShown = false;
        WeaponRole role = weaponRole(stack);
        SocketStats socketStats = socketStats(stack);
        for (FoodType type : FoodType.values()) {
            int count = socketStats.count(type);
            if (count <= 0) continue;
            double power = socketStats.effectPower(type, role);
            tip.add(Component.translatable("tooltip.ftboss.socket.effect_line", type.displayName(), count,
                    formatNumber(power), type.effectDescription()).withStyle(ChatFormatting.GRAY));
            if (type == profile.food) {
                tip.add(Component.translatable("tooltip.ftboss.socket.matching", profile.displayName(),
                        formatNumber(power * 0.5D), formatNumber(power)).withStyle(ChatFormatting.YELLOW));
            }
            if ((type == FoodType.SWEET || type == FoodType.FOOD) && dessertSetShown) continue;
            if (type == FoodType.SWEET || type == FoodType.FOOD) dessertSetShown = true;
            int setCount = Math.min(3, socketStats.setCount(type));
            SocketTier setTier = SocketTier.byLevel(socketStats.setTier(type));
            tip.add(Component.translatable("tooltip.ftboss.socket.set_progress", setCount, setTier.displayName(),
                    type.setDescription()).withStyle(setCount >= 3 ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
        }
        tip.add(Component.translatable("tooltip.ftboss.socket.remove_hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Component socketItemName(String identifier) {
        try {
            ResourceLocation id = new ResourceLocation(identifier);
            Item item = ForgeRegistries.ITEMS.getValue(id);
            return item == null ? Component.literal(identifier) : item.getDescription();
        } catch (RuntimeException ignored) {
            return Component.literal(identifier);
        }
    }

    private static boolean hasWashableBlessing(ItemStack stack) {
        CompoundTag data = data(stack);
        if (data == null) return false;
        if (data.getInt(XP) > 0 || data.getInt(LEVEL) > 0) return true;
        if (data.contains(SKILL, Tag.TAG_STRING)) return true;
        if (data.contains(GOURMET_SKILLS, Tag.TAG_LIST) && !data.getList(GOURMET_SKILLS, Tag.TAG_COMPOUND).isEmpty()) {
            return true;
        }
        if (data.contains(FOOD_BLESSINGS, Tag.TAG_LIST)
                && !data.getList(FOOD_BLESSINGS, Tag.TAG_COMPOUND).isEmpty()) return true;
        return data.contains(GOURMET_STATS, Tag.TAG_COMPOUND)
                && !data.getCompound(GOURMET_STATS).getAllKeys().isEmpty();
    }

    private static int washBlessings(ItemStack stack) {
        FoodWeaponLifecycle.ensureTracked(stack);
        CompoundTag data = data(stack);
        if (data == null) return 0;
        FoodWeaponRepair.invalidateForProgression(stack);
        int removed = data.contains(SKILL, Tag.TAG_STRING) ? 1 : 0;
        if (data.contains(GOURMET_SKILLS, Tag.TAG_LIST)) {
            removed += data.getList(GOURMET_SKILLS, Tag.TAG_COMPOUND).size();
        }
        if (data.contains(FOOD_BLESSINGS, Tag.TAG_LIST)) {
            removed += data.getList(FOOD_BLESSINGS, Tag.TAG_COMPOUND).size();
        }
        if (data.contains(GOURMET_STATS, Tag.TAG_COMPOUND)) {
            removed += data.getCompound(GOURMET_STATS).getAllKeys().size();
        }
        if (data.getInt(XP) > 0) removed++;
        if (data.getInt(LEVEL) > 0) removed++;
        data.putInt(XP, 0);
        data.putInt(LEVEL, 0);
        data.remove(SECOND_PROFILE);
        data.remove(GOURMET_ACTIVE_PROFILE);
        data.remove(GOURMET_ACTIVE_SKILL);
        data.remove(SKILL);
        data.remove(GOURMET_SKILLS);
        data.remove(GOURMET_STATS);
        data.remove(FOOD_BLESSINGS);
        for (String key : new ArrayList<>(data.getAllKeys())) {
            if (key.startsWith(LAST_SKILL_PROC) || key.startsWith(LAST_FOOD_BLESSING)) data.remove(key);
        }
        data.putBoolean(BLESSING_WASHED, true);
        FoodWeaponProgression.clear(stack);
        return removed;
    }

    /** Creative-only command bridge for deterministic progression testing on the held weapon. */
    public static int setEvolutionExperienceForTesting(ServerPlayer player, int amount, boolean relative) {
        if (player == null || !player.getAbilities().instabuild) {
            if (player != null) player.sendSystemMessage(Component.translatable("command.ftboss.evolution.creative_only")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        ItemStack weapon = heldEvolutionWeapon(player);
        Profile profile = boundProfile(weapon);
        if (weapon.isEmpty() || profile == null) {
            player.sendSystemMessage(Component.translatable("command.ftboss.evolution.no_bound_weapon")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        CompoundTag state = writableData(weapon, profile);
        FoodWeaponProgression.normalizeExperience(weapon);
        int old = evolutionExperience(weapon);
        int cap = evolutionLevel(weapon) >= maxLevel(weapon) ? 0 : threshold(weapon, evolutionLevel(weapon));
        long requested = relative ? (long) old + amount : amount;
        int value = (int) Math.max(0L, Math.min((long) cap, requested));
        state.putInt(XP, value);
        player.getInventory().setChanged();
        player.sendSystemMessage(Component.translatable("command.ftboss.evolution.changed",
                weapon.getHoverName(), old, value, cap).withStyle(ChatFormatting.AQUA));
        return 1;
    }

    @SubscribeEvent
    public static void grindstoneBlessing(GrindstoneEvent.OnPlaceItem event) {
        ItemStack top = event.getTopItem();
        ItemStack bottom = event.getBottomItem();
        ItemStack input = ItemStack.EMPTY;
        if (bottom.isEmpty() && hasWashableBlessing(top)) input = top;
        else if (top.isEmpty() && hasWashableBlessing(bottom)) input = bottom;
        if (input.isEmpty()) return;

        ItemStack output = input.copy();
        output.setCount(1);
        int removed = washBlessings(output);
        int enchantmentXp = 0;
        var enchantments = EnchantmentHelper.getEnchantments(output);
        List<Enchantment> removable = new ArrayList<>();
        for (var entry : enchantments.entrySet()) {
            if (!entry.getKey().isCurse()) {
                removable.add(entry.getKey());
                enchantmentXp += entry.getValue();
            }
        }
        for (Enchantment enchantment : removable) enchantments.remove(enchantment);
        EnchantmentHelper.setEnchantments(enchantments, output);
        output.setRepairCost(0);
        event.setOutput(output);
        event.setXp(Math.max(1, removed * 3 + enchantmentXp));
    }

    @SubscribeEvent
    public static void gourmetBladePickup(EntityItemPickupEvent event) {
        net.farmingtales.ftboss.item.weapons.GourmetEvolutionWeapon.playPickupEffect(event);
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().contains(DAILY_ROOT, Tag.TAG_COMPOUND)) {
            event.getEntity().getPersistentData().put(DAILY_ROOT,
                    event.getOriginal().getPersistentData().getCompound(DAILY_ROOT).copy());
        }
    }
}
