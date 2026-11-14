package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.item.weapons.GourmetEvolutionWeapon;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool;
import net.farmingtales.ftboss.util.FoodWeaponDamageAccounting;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.View;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Aspect;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Fusion;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.GrindstoneEvent.OnPlaceItem;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Pre;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class FoodWeaponEvolution {
    public static final int FIRST_THRESHOLD = 100;
    public static final int SECOND_THRESHOLD = 250;
    public static final int GOURMET_MAX_LEVEL = 8;
    public static final int GOURMET_SOCKET_CAPACITY = 30;
    public static final int ON_HIT_LIFESTEAL_COOLDOWN_TICKS = 60;
    private static final int MAX_FOOD_BLESSINGS = 3;
    private static final int[] GOURMET_THRESHOLDS = new int[]{100, 250, 450, 700, 1000, 1350, 1750, 2200};
    public static final int DAILY_TASK_COUNT = 8;
    public static final int DAILY_FOOD_COUNT = 8;
    private static final String ROOT = "FTBossFoodEvolution";
    private static final String PROFILE = "Profile";
    private static final String XP = "Experience";
    private static final String LEVEL = "Level";
    private static final String SKILL = "EvolutionSkill";
    private static final String GOURMET_SKILLS = "EvolutionSkills";
    private static final String GOURMET_ACTIVE_PROFILE = "ActiveProfile";
    private static final String GOURMET_ACTIVE_SKILL = "ActiveSkillIndex";
    private static final String GOURMET_STATS = "EvolutionStats";
    private static final String BLESSING_WASHED = "BlessingWashed";
    private static final String FOOD_BLESSINGS = "FoodBlessings";
    private static final String LAST_FOOD_BLESSING = "LastFoodBlessing.";
    private static final String SECOND_PROFILE = "SecondProfile";
    private static final String SOCKETS = "Sockets";
    private static final String DAILY_ROOT = "FTBossFoodEvolutionDailies";
    private static final String DAILY_DAY = "MinecraftDay";
    private static final String DAILY_REQUEST = "RequestedItem";
    private static final String DAILY_REQUIRED = "Required";
    private static final String DAILY_REWARD_TYPE = "RewardType";
    private static final String DAILY_REWARD_AMOUNT = "RewardAmount";
    private static final String DAILY_GOLDEN = "GoldenStatue";
    private static final ResourceLocation COIN_ID = ResourceLocation.tryParse("kubejs:coin");
    private static final String LAST_SKILL_PROC = "LastSkillProc";
    private static final String LAST_SOCKET_PROC = "LastSocketProc";
    private static final String CREATE_NAMESPACE = "create";
    private static final ThreadLocal<Boolean> EXTRA_DAMAGE = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> FOOD_BLESSING_CHAIN = ThreadLocal.withInitial(() -> false);
    private static final EnumMap<FoodWeaponEvolution.FoodType, List<ResourceLocation>> DAILY_FOOD_POOLS = new EnumMap<>(FoodWeaponEvolution.FoodType.class);
    private static final List<TagKey<Item>> DAILY_NATURAL_FOOD_TAGS = List.of(
        dailyFoodTag("forge:crops"),
        dailyFoodTag("forge:fruits"),
        dailyFoodTag("forge:berries"),
        dailyFoodTag("forge:vegetables"),
        dailyFoodTag("forge:raw_meats"),
        dailyFoodTag("forge:raw_fishes")
    );
    private static final Set<String> VANILLA_NATURAL_DAILY_FOODS = Set.of(
        "apple",
        "beef",
        "porkchop",
        "chicken",
        "mutton",
        "rabbit",
        "cod",
        "salmon",
        "tropical_fish",
        "pufferfish",
        "carrot",
        "potato",
        "beetroot",
        "melon_slice",
        "sweet_berries",
        "glow_berries",
        "chorus_fruit",
        "honey_bottle"
    );
    private static RecipeManager dailyRecipeManager;
    private static int dailyRecipeFingerprint = Integer.MIN_VALUE;
    private static Set<ResourceLocation> dailyRecipeOutputs = Set.of();
    private static boolean registered;
    private static final ResourceLocation EVOLUTION_DAMAGE = id("food_evolution_damage");
    private static final ResourceLocation EVOLUTION_SPEED = id("food_evolution_speed");
    private static final ResourceLocation SOCKET_ARMOR = id("food_socket_cheese");
    private static final ResourceLocation SOCKET_TOUGHNESS = id("food_socket_vegetable");
    private static final ResourceLocation SOCKET_HEALTH = id("food_socket_apple");
    private static final ResourceLocation SOCKET_MOVEMENT = id("food_socket_fish");
    private static final ResourceLocation SOCKET_LUCK = id("food_socket_sweet");

    private static ResourceLocation id(String var0) {
        return ResourceLocation.fromNamespaceAndPath("ftboss", var0);
    }

    private static List<FoodWeaponEvolution.FoodBlessing> blessingPool(FoodWeaponEvolution.Profile var0) {
        return switch (var0) {
            case SUGAR -> List.of(
            FoodWeaponEvolution.FoodBlessing.CANDIED_EDGE, FoodWeaponEvolution.FoodBlessing.SYRUP_STEP, FoodWeaponEvolution.FoodBlessing.FROSTED_SHELL
        );
            case BUTCHER -> List.of(
            FoodWeaponEvolution.FoodBlessing.CLEAVER_MIGHT, FoodWeaponEvolution.FoodBlessing.BUTCHER_HIDE, FoodWeaponEvolution.FoodBlessing.BLOOD_FEAST
        );
            case SAYA -> List.of(
            FoodWeaponEvolution.FoodBlessing.TEA_THORNS, FoodWeaponEvolution.FoodBlessing.CAMELLIA_GRACE, FoodWeaponEvolution.FoodBlessing.ROOTED_FOCUS
        );
            case WINE -> List.of(
            FoodWeaponEvolution.FoodBlessing.VINTAGE_CRIT, FoodWeaponEvolution.FoodBlessing.BARREL_PLATE, FoodWeaponEvolution.FoodBlessing.CORK_RUSH
        );
            case RAT -> List.of(
            FoodWeaponEvolution.FoodBlessing.CHEESE_FRENZY, FoodWeaponEvolution.FoodBlessing.PLAGUE_TOOTH, FoodWeaponEvolution.FoodBlessing.SCAVENGER_LUCK
        );
            case DEFECT -> List.of(
            FoodWeaponEvolution.FoodBlessing.OVERLOAD_DAMAGE, FoodWeaponEvolution.FoodBlessing.VOLT_STEP, FoodWeaponEvolution.FoodBlessing.MAGNETIC_PLATE
        );
            case COMPOST -> List.of(
            FoodWeaponEvolution.FoodBlessing.PRISMATIC_VENOM, FoodWeaponEvolution.FoodBlessing.RECYCLE_HEART, FoodWeaponEvolution.FoodBlessing.FERTILE_HASTE
        );
            case APPLE -> List.of(
            FoodWeaponEvolution.FoodBlessing.HONEYCRISP_HEART, FoodWeaponEvolution.FoodBlessing.GOLDEN_FORTUNE, FoodWeaponEvolution.FoodBlessing.ORCHARD_EDGE
        );
            case TIDE -> List.of(
            FoodWeaponEvolution.FoodBlessing.UNDERTOW_STEP, FoodWeaponEvolution.FoodBlessing.BRINE_SLOW, FoodWeaponEvolution.FoodBlessing.TIDAL_BULWARK
        );
            case BENGER -> List.of(
            FoodWeaponEvolution.FoodBlessing.SHEPHERD_MIGHT, FoodWeaponEvolution.FoodBlessing.HUNTING_STEP, FoodWeaponEvolution.FoodBlessing.PACK_FEAST
        );
        };
    }

    private FoodWeaponEvolution() {
    }

    public static synchronized void register() {
        if (!registered) {
            registered = true;
            NeoForge.EVENT_BUS.register(FoodWeaponEvolution.class);
        }
    }

    public static int evolutionLevel(ItemStack var0) {
        if (isEvolutionExcluded(var0)) {
            return 0;
        } else {
            CompoundTag var1 = data(var0);
            int var2 = isGourmetEvolutionWeapon(var0) ? 8 : 2;
            return var1 == null ? 0 : Math.max(0, Math.min(var2, var1.getInt("Level")));
        }
    }

    public static boolean isEvolutionCandidate(ItemStack var0) {
        return weaponCandidate(var0);
    }

    public static int evolutionExperience(ItemStack var0) {
        CompoundTag var1 = data(var0);
        return var1 == null ? 0 : Math.max(0, var1.getInt("Experience"));
    }

    public static int maxEvolutionLevel(ItemStack var0) {
        return maxLevel(var0);
    }

    public static int nextEvolutionRequirement(ItemStack var0) {
        int var1 = evolutionLevel(var0);
        int var2 = maxLevel(var0);
        return var1 >= var2 ? 0 : threshold(var0, var1);
    }

    public static int nativeSkillRank(ItemStack var0) {
        return nativeProfile(var0) == null ? 0 : evolutionLevel(var0) + 1;
    }

    public static float activeSkillMultiplier(ItemStack var0) {
        if (isEvolutionExcluded(var0)) {
            return 1.0F;
        } else {
            boolean var1 = isGourmetEvolutionWeapon(var0);
            FoodWeaponEvolution.WeaponRole var2 = weaponRole(var0);
            float var3 = (float)evolutionLevel(var0) * var2.skillPowerPerLevel(var1);
            float var4 = var2 == FoodWeaponEvolution.WeaponRole.ARCANE
                ? (float)Math.min(
                    0.4,
                    (
                            socketEffectPower(var0, FoodWeaponEvolution.FoodType.TEA)
                                + socketEffectPower(var0, FoodWeaponEvolution.FoodType.SWEET)
                                + socketEffectPower(var0, FoodWeaponEvolution.FoodType.FOOD)
                                + socketEffectPower(var0, FoodWeaponEvolution.FoodType.ENERGY)
                        )
                        * 0.015
                )
                : 0.0F;
            return 1.0F + var3 + var4;
        }
    }

    public static int skillCooldown(ItemStack var0, int var1) {
        if (isEvolutionExcluded(var0)) {
            return var1;
        } else {
            FoodWeaponEvolution.WeaponRole var2 = weaponRole(var0);
            double var3 = (double)evolutionLevel(var0) * var2.cooldownReductionPerLevel(isGourmetEvolutionWeapon(var0));
            double var5 = var3 + socketEffectPower(var0, FoodWeaponEvolution.FoodType.TEA) * 0.05;
            return Math.max(20, (int)Math.round((double)var1 * Math.max(0.55, 1.0 - var5)));
        }
    }

    public static boolean isNativeWeapon(ItemStack var0) {
        return nativeProfile(var0) != null;
    }

    public static boolean isGourmetEvolutionWeapon(ItemStack var0) {
        return var0.getItem() instanceof GourmetEvolutionWeapon;
    }

    public static boolean isGourmetBlankBlade(ItemStack var0) {
        return "ftboss:gourmet_blank_blade".equals(itemId(var0));
    }

    public static String firstProfileId(ItemStack var0) {
        FoodWeaponEvolution.Profile var1 = isGourmetEvolutionWeapon(var0) ? activeProfile(var0) : boundProfile(var0);
        return var1 == null ? "" : var1.id;
    }

    public static String secondProfileId(ItemStack var0) {
        if (isEvolutionExcluded(var0)) {
            return "";
        } else {
            CompoundTag var1 = data(var0);
            return var1 == null ? "" : var1.getString("SecondProfile");
        }
    }

    private static CompoundTag data(ItemStack var0) {
        return FoodWeaponData.read(var0, "FTBossFoodEvolution");
    }

    private static CompoundTag writableData(ItemStack var0, FoodWeaponEvolution.Profile var1) {
        CompoundTag var2 = data(var0);
        if (var2 == null) {
            var2 = new CompoundTag();
        }

        if (var1 != null && !var2.contains("Profile", 8)) {
            var2.putString("Profile", var1.id);
        }

        return var2;
    }

    private static void saveData(ItemStack var0, CompoundTag var1) {
        FoodWeaponData.write(var0, "FTBossFoodEvolution", var1);
    }

    private static String itemId(ItemStack var0) {
        ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getItem());
        return var1 == null ? "" : var1.toString();
    }

    private static boolean isEvolutionExcluded(ItemStack var0) {
        if (!WeaponSystemScope410.supports(var0)) return true;
        if (var0.isEmpty()) {
            return false;
        } else {
            ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getItem());
            return var1 != null && "create".equals(var1.getNamespace());
        }
    }

    private static FoodWeaponEvolution.Profile nativeProfile(ItemStack var0) {
        if (!WeaponSystemScope410.supports(var0)) return null;
        String var1 = itemId(var0);

        return switch (var1) {
            case "ftboss:candy_cane" -> FoodWeaponEvolution.Profile.SUGAR;
            case "ftboss:turkey_legs" -> FoodWeaponEvolution.Profile.BUTCHER;
            case "ftboss:blooming_thorns" -> FoodWeaponEvolution.Profile.SAYA;
            case "ftboss:wine_bottle_mace", "ftboss:barrel_crossbow" -> FoodWeaponEvolution.Profile.WINE;
            case "ftboss:cheese_wheel_mace" -> FoodWeaponEvolution.Profile.RAT;
            case "ftboss:defect_claw" -> FoodWeaponEvolution.Profile.DEFECT;
            case "ftboss:compost_crown" -> FoodWeaponEvolution.Profile.COMPOST;
            case "ftboss:apple_stem" -> FoodWeaponEvolution.Profile.APPLE;
            case "ftboss:tide_trident" -> FoodWeaponEvolution.Profile.TIDE;
            case "ftboss:dog_beating_stick" -> FoodWeaponEvolution.Profile.BENGER;
            default -> null;
        };
    }

    private static FoodWeaponEvolution.Profile boundProfile(ItemStack var0) {
        if (isEvolutionExcluded(var0)) {
            return null;
        } else {
            FoodWeaponEvolution.Profile var1 = nativeProfile(var0);
            if (var1 != null) {
                return var1;
            } else {
                CompoundTag var2 = data(var0);
                return var2 == null ? null : FoodWeaponEvolution.Profile.byId(var2.getString("Profile"));
            }
        }
    }

    private static boolean externalWeaponCandidate(ItemStack var0) {
        if (!var0.isEmpty() && var0.getCount() == 1 && !isEvolutionExcluded(var0) && nativeProfile(var0) == null) {
            ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getItem());
            if (var1 != null && !"ftboss".equals(var1.getNamespace())) {
                FoodWeaponEvolution.WeaponRole var2 = weaponRole(var0);
                if (var2 != FoodWeaponEvolution.WeaponRole.RANGED
                    && var2 != FoodWeaponEvolution.WeaponRole.ARCANE
                    && var2 != FoodWeaponEvolution.WeaponRole.TOOL) {
                    boolean[] var3 = new boolean[]{false};
                    var0.forEachModifier(EquipmentSlot.MAINHAND, (var1x, var2x) -> {
                        if (var1x.is(Attributes.ATTACK_DAMAGE) && var2x.amount() > 0.0) {
                            var3[0] = true;
                        }
                    });
                    return var3[0];
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean weaponCandidate(ItemStack var0) {
        if (!WeaponSystemScope410.supports(var0)) return false;
        return !var0.is(Items.ENCHANTED_BOOK)
            && (isGourmetEvolutionWeapon(var0) || nativeProfile(var0) != null || boundProfile(var0) != null || externalWeaponCandidate(var0));
    }

    private static FoodWeaponEvolution.WeaponRole weaponRole(ItemStack var0) {
        if (var0.isEmpty()) {
            return FoodWeaponEvolution.WeaponRole.BALANCED;
        } else {
            String var1 = itemId(var0);

            FoodWeaponEvolution.WeaponRole var2 = switch (var1) {
                case "ftboss:gourmet_blank_blade", "ftboss:turkey_legs", "ftboss:wine_bottle_mace" -> FoodWeaponEvolution.WeaponRole.VANGUARD;
                case "ftboss:defect_claw", "ftboss:dog_beating_stick" -> FoodWeaponEvolution.WeaponRole.DUELIST;
                case "ftboss:barrel_crossbow", "ftboss:tide_trident" -> FoodWeaponEvolution.WeaponRole.RANGED;
                case "ftboss:compost_crown" -> FoodWeaponEvolution.WeaponRole.ARCANE;
                case "ftboss:cheese_wheel_mace", "ftboss:apple_stem" -> FoodWeaponEvolution.WeaponRole.GUARDIAN;
                case "ftboss:candy_cane", "ftboss:blooming_thorns" -> FoodWeaponEvolution.WeaponRole.BALANCED;
                default -> null;
            };
            if (var2 != null) {
                return var2;
            } else {
                Item var3 = var0.getItem();
                if (var3 instanceof DiggerItem || var3 instanceof ShearsItem) {
                    return FoodWeaponEvolution.WeaponRole.TOOL;
                } else if (!(var3 instanceof BowItem) && !(var3 instanceof CrossbowItem) && !(var3 instanceof TridentItem)) {
                    String var10 = var1.substring(Math.max(0, var1.indexOf(58) + 1)).toLowerCase(Locale.ROOT);
                    if (containsFoodWord(var10, "bow", "crossbow", "trident", "gun", "rifle", "pistol", "musket", "launcher")) {
                        return FoodWeaponEvolution.WeaponRole.RANGED;
                    } else if (containsFoodWord(var10, "staff", "wand", "scepter", "spellbook", "tome", "focus", "grimoire")) {
                        return FoodWeaponEvolution.WeaponRole.ARCANE;
                    } else if (containsFoodWord(var10, "pickaxe", "shovel", "hoe", "mattock", "drill", "saw", "wrench", "excavator")) {
                        return FoodWeaponEvolution.WeaponRole.TOOL;
                    } else if (containsFoodWord(var10, "shield", "bulwark", "tower", "guardian")) {
                        return FoodWeaponEvolution.WeaponRole.GUARDIAN;
                    } else if (containsFoodWord(var10, "dagger", "rapier", "claw", "knife", "katana", "sabre", "saber", "scimitar")) {
                        return FoodWeaponEvolution.WeaponRole.DUELIST;
                    } else if (containsFoodWord(var10, "hammer", "greataxe", "greatsword", "cleaver", "mace", "maul")) {
                        return FoodWeaponEvolution.WeaponRole.VANGUARD;
                    } else {
                        double[] var5 = new double[]{0.0, 0.0};
                        var3.getDefaultAttributeModifiers().forEach(EquipmentSlot.MAINHAND, (var1x, var2x) -> {
                            if (var1x.is(Attributes.ATTACK_DAMAGE)) {
                                var5[0] += var2x.amount();
                            }

                            if (var1x.is(Attributes.ATTACK_SPEED)) {
                                var5[1] += var2x.amount();
                            }
                        });
                        double var6 = var5[0];
                        double var8 = var5[1];
                        if (var6 >= 8.0 && var8 <= -2.7) {
                            return FoodWeaponEvolution.WeaponRole.VANGUARD;
                        } else {
                            return var8 >= -2.2 ? FoodWeaponEvolution.WeaponRole.DUELIST : FoodWeaponEvolution.WeaponRole.BALANCED;
                        }
                    }
                } else {
                    return FoodWeaponEvolution.WeaponRole.RANGED;
                }
            }
        }
    }

    private static double evolutionDamageBonus(FoodWeaponEvolution.WeaponRole var0, int var1, boolean var2) {
        double var10000;
        if (var2) {
            switch (var0) {
                case VANGUARD:
                    var10000 = 0.75;
                    break;
                case DUELIST:
                    var10000 = 0.3;
                    break;
                case RANGED:
                case ARCANE:
                case TOOL:
                    var10000 = 0.0;
                    break;
                case GUARDIAN:
                    var10000 = 0.25;
                    break;
                case BALANCED:
                    var10000 = 0.5;
                    break;
                default:
                    throw new MatchException(null, null);
            }
        } else {
            switch (var0) {
                case VANGUARD:
                    var10000 = 3.0;
                    break;
                case DUELIST:
                    var10000 = 1.25;
                    break;
                case RANGED:
                case ARCANE:
                case TOOL:
                    var10000 = 0.0;
                    break;
                case GUARDIAN:
                    var10000 = 1.0;
                    break;
                case BALANCED:
                    var10000 = 2.0;
                    break;
                default:
                    throw new MatchException(null, null);
            }
        }

        double var3 = var10000;
        return (double)var1 * var3;
    }

    private static double evolutionSpeedBonus(FoodWeaponEvolution.WeaponRole var0, int var1, boolean var2) {
        double var10000;
        if (var2) {
            switch (var0) {
                case VANGUARD:
                    var10000 = 0.01;
                    break;
                case DUELIST:
                    var10000 = 0.025;
                    break;
                case BALANCED:
                    var10000 = 0.02;
                    break;
                default:
                    var10000 = 0.0;
            }
        } else {
            switch (var0) {
                case VANGUARD:
                    var10000 = 0.04;
                    break;
                case DUELIST:
                    var10000 = 0.14;
                    break;
                case BALANCED:
                    var10000 = 0.08;
                    break;
                default:
                    var10000 = 0.0;
            }
        }

        double var3 = var10000;
        return (double)var1 * var3;
    }

    private static double evolutionArmorBonus(FoodWeaponEvolution.WeaponRole var0, int var1, boolean var2) {
        return var0 == FoodWeaponEvolution.WeaponRole.GUARDIAN ? (double)var1 * (var2 ? 0.4 : 1.5) : 0.0;
    }

    private static double evolutionHealthBonus(FoodWeaponEvolution.WeaponRole var0, int var1, boolean var2) {
        return var0 == FoodWeaponEvolution.WeaponRole.GUARDIAN ? (double)var1 * (var2 ? 0.5 : 2.0) : 0.0;
    }

    private static double evolutionMovementBonus(FoodWeaponEvolution.WeaponRole var0, int var1, boolean var2) {
        if (var0 == FoodWeaponEvolution.WeaponRole.DUELIST) {
            return (double)var1 * (var2 ? 0.004 : 0.015);
        } else {
            return var0 == FoodWeaponEvolution.WeaponRole.RANGED ? (double)var1 * (var2 ? 0.003 : 0.01) : 0.0;
        }
    }

    private static double evolutionLuckBonus(FoodWeaponEvolution.WeaponRole var0, int var1, boolean var2) {
        return var0 == FoodWeaponEvolution.WeaponRole.ARCANE ? (double)var1 * (var2 ? 0.15 : 0.5) : 0.0;
    }

    private static int maxLevel(ItemStack var0) {
        if (isGourmetEvolutionWeapon(var0)) {
            return 8;
        } else {
            return nativeProfile(var0) != null ? 2 : 1;
        }
    }

    private static int threshold(ItemStack var0, int var1) {
        if (isGourmetEvolutionWeapon(var0)) {
            int var2 = Math.max(0, Math.min(GOURMET_THRESHOLDS.length - 1, var1));
            return GOURMET_THRESHOLDS[var2];
        } else {
            return var1 <= 0 ? 100 : 250;
        }
    }

    private static int storedSocketCount(ItemStack var0) {
        CompoundTag var1 = data(var0);
        if (var1 != null && var1.contains("Sockets", 9)) {
            int var2 = isGourmetEvolutionWeapon(var0) ? 30 : 3;
            return Math.min(var2, var1.getList("Sockets", 10).size());
        } else {
            return 0;
        }
    }

    private static int socketCapacityInternal(ItemStack var0) {
        if (isGourmetEvolutionWeapon(var0)) {
            return 30;
        } else {
            int var1 = Math.min(3, 1 + evolutionLevel(var0));
            return Math.max(var1, storedSocketCount(var0));
        }
    }

    private static List<FoodWeaponEvolution.Socket> sockets(ItemStack var0) {
        ArrayList var1 = new ArrayList();
        CompoundTag var2 = data(var0);
        if (var2 != null && var2.contains("Sockets", 9)) {
            ListTag var3 = var2.getList("Sockets", 10);
            int var4 = isGourmetEvolutionWeapon(var0) ? 30 : 3;

            for (int var5 = 0; var5 < var3.size() && var5 < var4; var5++) {
                CompoundTag var6 = var3.getCompound(var5);

                try {
                    FoodWeaponEvolution.FoodType var7 = FoodWeaponEvolution.FoodType.valueOf(var6.getString("Type"));
                    String var8 = var6.getString("Item");
                    var7 = FoodSocketFoodCatalog.savedType(var7, var8);
                    int var9 = var6.contains("Tier", 3) ? var6.getInt("Tier") : foodTierById(var8).level;
                    var1.add(new FoodWeaponEvolution.Socket(var7, var8, FoodWeaponEvolution.SocketTier.byLevel(var9)));
                } catch (IllegalArgumentException var10) {
                }
            }

            return var1;
        } else {
            return var1;
        }
    }

    private static int socketCount(ItemStack var0, FoodWeaponEvolution.FoodType var1) {
        int var2 = 0;

        for (FoodWeaponEvolution.Socket var4 : sockets(var0)) {
            if (var4.type == var1) {
                var2++;
            }
        }

        return var2;
    }

    private static ListTag foodBlessingList(ItemStack var0) {
        CompoundTag var1 = data(var0);
        return var1 == null ? new ListTag() : var1.getList("FoodBlessings", 10);
    }

    private static int foodBlessingLevel(CompoundTag var0) {
        return Math.max(1, Math.min(3, var0.getInt("Level")));
    }

    private static boolean applyFoodBlessing(
        ServerPlayer var0, FoodWeaponEvolution.Profile var1, ItemStack var2, ItemStack var3, FoodWeaponEvolution.FoodType var4
    ) {
        FoodWeaponEvolution.Profile var5 = boundProfile(var2);
        if (var5 == null) {
            var0.displayClientMessage(Component.translatable("message.ftboss.food_blessing.attune_first"), true);
            return false;
        } else if (!isGourmetEvolutionWeapon(var2) && var5 != var1) {
            var0.displayClientMessage(Component.translatable("message.ftboss.evolution.wrong_statue", new Object[]{var5.displayName()}), true);
            return false;
        } else if (var4 != var1.food) {
            var0.displayClientMessage(Component.translatable("message.ftboss.food_blessing.wrong_food", new Object[]{var1.food.displayName()}), true);
            return false;
        } else {
            ListTag var6 = foodBlessingList(var2);
            if (var6.size() >= 3) {
                var0.displayClientMessage(Component.translatable("message.ftboss.food_blessing.full", new Object[]{3}), true);
                return false;
            } else {
                ArrayList var7 = new ArrayList();

                for (FoodWeaponEvolution.FoodBlessing var9 : blessingPool(var1)) {
                    boolean var10 = false;

                    for (int var11 = 0; var11 < var6.size(); var11++) {
                        if (var9.id.equals(var6.getCompound(var11).getString("Blessing"))) {
                            var10 = true;
                            break;
                        }
                    }

                    if (!var10) {
                        var7.add(var9);
                    }
                }

                if (var7.isEmpty()) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.food_blessing.complete", new Object[]{var1.displayName()}), true);
                    return false;
                } else {
                    FoodWeaponEvolution.FoodBlessing var14 = (FoodWeaponEvolution.FoodBlessing)var7.get(var0.getRandom().nextInt(var7.size()));
                    FoodWeaponEvolution.SocketTier var15 = foodTier(var3);

                    int var16 = switch (var15) {
                        case BASIC -> 1;
                        case FINE -> var0.getRandom().nextBoolean() ? 2 : 1;
                        case GOURMET -> var0.getRandom().nextBoolean() ? 3 : 2;
                    };
                    CompoundTag var17 = writableData(var2, var5);
                    ListTag var12 = var17.getList("FoodBlessings", 10);
                    CompoundTag var13 = new CompoundTag();
                    var13.putString("Profile", var1.id);
                    var13.putString("Blessing", var14.id);
                    var13.putInt("Level", var16);
                    var13.putInt("FoodTier", var15.level);
                    var13.putString("Food", itemId(var3));
                    var12.add(var13);
                    var17.put("FoodBlessings", var12);
                    saveData(var2, var17);
                    var2.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                    if (!var0.getAbilities().instabuild) {
                        var3.shrink(1);
                    }

                    var0.serverLevel().sendParticles(ParticleTypes.ENCHANT, var0.getX(), var0.getY() + 1.0, var0.getZ(), 72, 0.7, 0.8, 0.7, 0.15);
                    var0.serverLevel().sendParticles(ParticleTypes.END_ROD, var0.getX(), var0.getY() + 1.0, var0.getZ(), 18, 0.4, 0.5, 0.4, 0.08);
                    var0.serverLevel()
                        .playSound(null, var0.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.9F, 1.1F + (float)var15.level * 0.12F);
                    var0.sendSystemMessage(
                        Component.translatable("message.ftboss.food_blessing.applied", new Object[]{var14.nameComponent(), toRoman(var16), var15.displayName()})
                            .withStyle(ChatFormatting.LIGHT_PURPLE)
                    );
                    var0.sendSystemMessage(var14.descriptionComponent(var16).copy().withStyle(ChatFormatting.GRAY));
                    FTBossMod.LOGGER
                        .info(
                            "[FTBoss][食物附魔] {} 的 {} 在 {} 石像获得 {} 等级{}（食物等级{}）",
                            new Object[]{var0.getName().getString(), itemId(var2), var1.id, var14.id, var16, var15.level}
                        );
                    return true;
                }
            }
        }
    }

    private static void addFoodBlessingAttributes(ItemAttributeModifierEvent var0, ItemStack var1) {
        ListTag var2 = foodBlessingList(var1);

        for (int var3 = 0; var3 < var2.size(); var3++) {
            CompoundTag var4 = var2.getCompound(var3);
            FoodWeaponEvolution.FoodBlessing var5 = FoodWeaponEvolution.FoodBlessing.byId(var4.getString("Blessing"));
            if (var5 != null) {
                int var6 = foodBlessingLevel(var4);

                double var7 = switch (var5.kind) {
                    case ATTACK_SPEED -> 0.04 * (double)var6;
                    case ATTACK_DAMAGE -> 0.75 * (double)var6;
                    case MOVEMENT_SPEED -> 0.012 * (double)var6;
                    case KNOCKBACK_RESISTANCE -> 0.08 * (double)var6;
                    case MAX_HEALTH -> 1.5 * (double)var6;
                    default -> 0.0;
                };
                if (!(var7 <= 0.0)) {
                    ResourceLocation var9 = id("food_blessing_" + var5.id);
                    Operation var10 = var5.kind != FoodWeaponEvolution.FoodBlessingKind.MOVEMENT_SPEED
                            && var5.kind != FoodWeaponEvolution.FoodBlessingKind.KNOCKBACK_RESISTANCE
                        ? Operation.ADD_VALUE
                        : Operation.ADD_MULTIPLIED_BASE;

                    Holder var11 = switch (var5.kind) {
                        case ATTACK_SPEED -> Attributes.ATTACK_SPEED;
                        case ATTACK_DAMAGE -> Attributes.ATTACK_DAMAGE;
                        case MOVEMENT_SPEED -> Attributes.MOVEMENT_SPEED;
                        case KNOCKBACK_RESISTANCE -> Attributes.KNOCKBACK_RESISTANCE;
                        case MAX_HEALTH -> Attributes.MAX_HEALTH;
                        default -> null;
                    };
                    if (var11 != null) {
                        var0.addModifier(var11, new AttributeModifier(var9, var7, var10), EquipmentSlotGroup.MAINHAND);
                    }
                }
            }
        }
    }

    private static int foodBlessingLevel(ItemStack var0, FoodWeaponEvolution.FoodBlessingKind var1) {
        int var2 = 0;
        ListTag var3 = foodBlessingList(var0);

        for (int var4 = 0; var4 < var3.size(); var4++) {
            CompoundTag var5 = var3.getCompound(var4);
            FoodWeaponEvolution.FoodBlessing var6 = FoodWeaponEvolution.FoodBlessing.byId(var5.getString("Blessing"));
            if (var6 != null && var6.kind == var1) {
                var2 = Math.max(var2, foodBlessingLevel(var5));
            }
        }

        return var2;
    }

    private static void applyFoodBlessingHits(LivingIncomingDamageEvent var0, ServerPlayer var1, LivingEntity var2, CompoundTag var3, long var4) {
        ItemStack var6 = var1.getMainHandItem();
        ListTag var7 = foodBlessingList(var6);
        String var8 = "FTBossFoodFortune:" + var1.getUUID();
        String var9 = var8 + ":Level";
        if (var2.getPersistentData().getLong(var8) >= var4) {
            int var10 = Math.max(1, var2.getPersistentData().getInt(var9));
            var0.setAmount(var0.getAmount() * (1.12F + (float)var10 * 0.06F));
            var2.getPersistentData().remove(var8);
            var2.getPersistentData().remove(var9);
        }

        for (int var18 = 0; var18 < var7.size(); var18++) {
            CompoundTag var11 = var7.getCompound(var18);
            FoodWeaponEvolution.FoodBlessing var12 = FoodWeaponEvolution.FoodBlessing.byId(var11.getString("Blessing"));
            if (var12 != null) {
                int var13 = foodBlessingLevel(var11);
                float var14 = Math.min(0.36F, 0.12F + (float)var13 * 0.05F);
                switch (var12.kind) {
                    case EXECUTE:
                        double var24 = (double)(var2.getHealth() / Math.max(1.0F, var2.getMaxHealth()));
                        if (var24 <= 0.12 + (double)var13 * 0.05) {
                            var0.setAmount(var0.getAmount() * (1.2F + (float)var13 * 0.12F));
                        }
                    case ATTACK_SPEED:
                    case ATTACK_DAMAGE:
                    case LOW_HEALTH_GUARD:
                    case PROJECTILE_GUARD:
                    case MOVEMENT_SPEED:
                    case KNOCKBACK_RESISTANCE:
                    case MAX_HEALTH:
                    case WET_GUARD:
                    default:
                        break;
                    case ABSORPTION:
                        if (var1.getRandom().nextFloat() < var14) {
                            var1.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60 + var13 * 20, var13 - 1, false, false, true), var1);
                        }
                        break;
                    case LIFESTEAL:
                        String var23 = "LastFoodBlessing." + var12.id;
                        if (var4 - var3.getLong(var23) >= 60L) {
                            var1.heal(0.35F * (float)var13);
                            var3.putLong(var23, var4);
                        }
                        break;
                    case POISON:
                        if (var1.getRandom().nextFloat() < var14) {
                            var2.addEffect(new MobEffectInstance(MobEffects.POISON, 40 + var13 * 20, Math.min(2, var13 - 1), false, false, true), var1);
                        }
                        break;
                    case REGEN:
                        if (var1.getRandom().nextFloat() < var14) {
                            var1.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40 + var13 * 20, var13 - 1, false, false, true), var1);
                        }
                        break;
                    case ROOT:
                        if (var1.getRandom().nextFloat() < var14) {
                            var2.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60 + var13 * 25, Math.min(3, var13), false, false, true), var1);
                        }
                        break;
                    case CRIT:
                        if (var1.getRandom().nextFloat() < var14) {
                            var0.setAmount(var0.getAmount() * (1.3F + (float)var13 * 0.12F));
                        }
                        break;
                    case CORK_RUSH:
                        if (var1.getRandom().nextFloat() < var14) {
                            var1.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 80 + var13 * 20, var13 - 1, false, false, true), var1);
                            var1.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80 + var13 * 20, var13 - 1, false, false, true), var1);
                        }
                        break;
                    case FRENZY:
                        long var22 = var3.getLong("LastFoodBlessing." + var12.id);
                        int var17 = var4 - var22 <= 60L ? Math.min(var13 + 2, var3.getInt("CheeseFrenzyStacks") + 1) : 1;
                        var3.putInt("CheeseFrenzyStacks", var17);
                        var3.putLong("LastFoodBlessing." + var12.id, var4);
                        var0.setAmount(var0.getAmount() * (1.0F + (float)var17 * (0.025F + (float)var13 * 0.01F)));
                        break;
                    case WITHER:
                        if (var1.getRandom().nextFloat() < var14) {
                            var2.addEffect(new MobEffectInstance(MobEffects.WITHER, 30 + var13 * 15, Math.min(1, var13 - 1), false, false, true), var1);
                        }
                        break;
                    case CHAIN:
                        if (var1.getRandom().nextFloat() < var14) {
                            chainArc(var1, var2, 2.0F + (float)var13 * 1.5F);
                        }
                        break;
                    case OVERLOAD:
                        if (var1.getRandom().nextFloat() < var14) {
                            overloadBurst(var1, var2, 1.5F + (float)var13 * 1.25F);
                        }
                        break;
                    case PRISMATIC:
                        if (var1.getRandom().nextFloat() < var14) {
                            var2.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60 + var13 * 20, Math.min(2, var13 - 1), false, false, true), var1);
                            var2.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80 + var13 * 20, 0, false, false, true), var1);
                        }
                        break;
                    case REPAIR:
                        String var21 = "LastFoodBlessing." + var12.id;
                        if (var6.isDamaged() && var4 - var3.getLong(var21) >= (long)Math.max(20, 70 - var13 * 15)) {
                            var6.setDamageValue(Math.max(0, var6.getDamageValue() - var13));
                            var3.putLong(var21, var4);
                        }
                        break;
                    case EMERGENCY_HEART:
                        String var20 = "LastFoodBlessing." + var12.id;
                        if (var1.getHealth() <= var1.getMaxHealth() * 0.5F && var4 - var3.getLong(var20) >= 100L) {
                            var1.heal(0.75F * (float)var13);
                            var1.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60, 0, false, false, true), var1);
                            var3.putLong(var20, var4);
                        }
                        break;
                    case MARK:
                        if (var1.getRandom().nextFloat() < var14) {
                            var2.getPersistentData().putLong(var8, var4 + 100L + (long)var13 * 40L);
                            var2.getPersistentData().putInt(var9, var13);
                            var2.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100 + var13 * 40, 0, false, false, true), var1);
                        }
                        break;
                    case BURN:
                        if (var1.getRandom().nextFloat() < var14) {
                            var2.igniteForSeconds(2.0F + (float)var13 * 2.0F);
                        }
                        break;
                    case UNDERTOW:
                        if (var2.isInWaterRainOrBubble()) {
                            var0.setAmount(var0.getAmount() * (1.1F + (float)var13 * 0.05F));
                            var1.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60 + var13 * 20, 0, false, false, true), var1);
                        }
                        break;
                    case BRINE:
                        if (var1.getRandom().nextFloat() < var14) {
                            var2.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60 + var13 * 25, Math.min(2, var13 - 1), false, false, true), var1);
                        }
                        break;
                    case ISOLATION:
                        boolean var19 = var2.level()
                            .getEntitiesOfClass(LivingEntity.class, var2.getBoundingBox().inflate(4.0), var2x -> var2x != var2 && canTarget(var1, var2x))
                            .isEmpty();
                        if (var19) {
                            var0.setAmount(var0.getAmount() * (1.1F + (float)var13 * 0.05F));
                        }
                        break;
                    case HUNT:
                        String var15 = "FTBossFoodHunt:" + var1.getUUID();
                        if (var2.getPersistentData().getLong(var15) >= var4) {
                            var0.setAmount(var0.getAmount() * (1.05F + (float)var13 * 0.035F));
                        } else {
                            var2.getPersistentData().putLong(var15, var4 + 160L + (long)var13 * 40L);
                            var2.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160 + var13 * 40, 0, false, false, true), var1);
                            var1.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 + var13 * 20, Math.min(1, var13 - 1), false, false, true), var1);
                        }
                }
            }
        }
    }

    public static void foodBlessingDefense(LivingIncomingDamageEvent var0) {
        if (!Boolean.TRUE.equals(EXTRA_DAMAGE.get()) && !(var0.getAmount() <= 0.0F) && var0.getEntity() instanceof ServerPlayer var1) {
            ItemStack var7 = var1.getMainHandItem();
            if (boundProfile(var7) != null) {
                int var3 = foodBlessingLevel(var7, FoodWeaponEvolution.FoodBlessingKind.LOW_HEALTH_GUARD);
                int var4 = foodBlessingLevel(var7, FoodWeaponEvolution.FoodBlessingKind.PROJECTILE_GUARD);
                int var5 = foodBlessingLevel(var7, FoodWeaponEvolution.FoodBlessingKind.WET_GUARD);
                float var6 = 1.0F;
                if (var3 > 0 && var1.getHealth() <= var1.getMaxHealth() * 0.5F) {
                    var6 *= Math.max(0.55F, 1.0F - (float)var3 * 0.09F);
                }

                if (var4 > 0 && var0.getSource().getDirectEntity() instanceof Projectile) {
                    var6 *= Math.max(0.55F, 1.0F - (float)var4 * 0.1F);
                }

                if (var5 > 0 && var1.isInWaterRainOrBubble()) {
                    var6 *= Math.max(0.55F, 1.0F - (float)var5 * 0.08F);
                }

                if (var6 < 1.0F) {
                    var0.setAmount(var0.getAmount() * var6);
                }
            }
        }
    }

    public static List<FoodWeaponEvolution.FoodBlessingView> foodBlessingViews(ItemStack var0) {
        ArrayList var1 = new ArrayList();
        ArrayList var2 = new ArrayList();
        ListTag var3 = foodBlessingList(var0);

        for (int var4 = 0; var4 < var3.size(); var4++) {
            CompoundTag var5 = var3.getCompound(var4);
            FoodWeaponEvolution.FoodBlessing var6 = FoodWeaponEvolution.FoodBlessing.byId(var5.getString("Blessing"));
            if (var6 != null && !var2.contains(var6.id)) {
                var2.add(var6.id);
                int var7 = foodBlessingLevel(var5);
                var1.add(new FoodWeaponEvolution.FoodBlessingView(var6.nameComponent(), var6.descriptionComponent(var7), toRoman(var7)));
            }
        }

        return List.copyOf(var1);
    }

    public static List<FoodWeaponEvolution.WeaponDetailView> weaponDetailViews(ItemStack var0) {
        FoodWeaponEvolution.Profile var1 = boundProfile(var0);
        if (var1 == null) {
            return List.of();
        } else {
            ArrayList var2 = new ArrayList();
            FoodWeaponEvolution.WeaponRole var3 = weaponRole(var0);
            var2.add(
                new FoodWeaponEvolution.WeaponDetailView(
                    Component.translatable("screen.ftboss.statue_hub.details.category.role"), var3.displayName(), var3.bonusDescription()
                )
            );
            CompoundTag var4 = data(var0);
            int var5 = evolutionLevel(var0);
            boolean var6 = isGourmetEvolutionWeapon(var0);
            if (nativeProfile(var0) != null) {
                var2.add(
                    new FoodWeaponEvolution.WeaponDetailView(
                        Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                        Component.translatable(
                            "tooltip.ftboss.evolution.native_skill", new Object[]{var5 + 1, Math.round((activeSkillMultiplier(var0) - 1.0F) * 100.0F)}
                        ),
                        Component.translatable("screen.ftboss.statue_hub.details.native_skill_help")
                    )
                );
            } else if (var6) {
                List var7 = gourmetSkills(var0);
                int var8 = activeGourmetSkillIndex(var0);

                for (int var9 = 0; var9 < var7.size(); var9++) {
                    FoodWeaponEvolution.AcquiredSkill var10 = (FoodWeaponEvolution.AcquiredSkill)var7.get(var9);
                    Aspect var11 = Aspect.fromId(var10.profile.id);
                    Object var12 = var11 == null ? var10.profile.displayName() : Component.translatable(var11.translationKey());
                    boolean var13 = var9 == var8;
                    var2.add(
                        new FoodWeaponEvolution.WeaponDetailView(
                            Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                            Component.translatable(
                                "tooltip.ftboss.gourmet.skill_line", new Object[]{var9 + 1, var10.profile.displayName(), var10.skill.nameComponent()}
                            ),
                            Component.translatable(
                                var13 ? "screen.ftboss.statue_hub.details.gourmet_skill_selected" : "screen.ftboss.statue_hub.details.gourmet_skill_switch",
                                new Object[]{var12, Component.translatable(var10.skill.effect.key())}
                            ),
                            var9,
                            var13
                        )
                    );
                }
            } else if (var5 > 0 && var4 != null) {
                FoodWeaponEvolution.EvolutionSkill var14 = FoodWeaponEvolution.EvolutionSkill.byId(var4.getString("EvolutionSkill"));
                if (var14 != null) {
                    var2.add(
                        new FoodWeaponEvolution.WeaponDetailView(
                            Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                            Component.translatable("tooltip.ftboss.evolution.drawn_skill", new Object[]{var14.nameComponent()}),
                            Component.translatable(var14.effect.key())
                        )
                    );
                }
            }

            if (var5 > 0) {
                Aspect var15 = Aspect.fromId(firstProfileId(var0));
                if (var6 && var15 != null) {
                    var2.add(
                        new FoodWeaponEvolution.WeaponDetailView(
                            Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                            Component.translatable("tooltip.ftboss.gourmet.active", new Object[]{Component.translatable(var15.translationKey())}),
                            Component.translatable(var15.descriptionKey())
                        )
                    );
                } else if (var15 != null && var5 == 1) {
                    var2.add(
                        new FoodWeaponEvolution.WeaponDetailView(
                            Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                            Component.translatable("tooltip.ftboss.evolution.active_primary", new Object[]{Component.translatable(var15.translationKey())}),
                            Component.translatable(var15.descriptionKey())
                        )
                    );
                } else if (var15 != null) {
                    Aspect var19 = Aspect.fromId(secondProfileId(var0));
                    if (var19 == null) {
                        var19 = var15;
                    }

                    Fusion var23 = WeaponEvolutionSkillPool.fusion(var15, var19);
                    if (var23 != null) {
                        var2.add(
                            new FoodWeaponEvolution.WeaponDetailView(
                                Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                                Component.translatable(
                                    "tooltip.ftboss.evolution.active_primary", new Object[]{Component.translatable(var23.primaryTranslationKey())}
                                ),
                                Component.translatable(var23.styleDescriptionKey())
                            )
                        );
                        var2.add(
                            new FoodWeaponEvolution.WeaponDetailView(
                                Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                                Component.translatable(
                                    "tooltip.ftboss.evolution.active_secondary", new Object[]{Component.translatable(var23.secondaryTranslationKey())}
                                ),
                                Component.translatable(var23.styleDescriptionKey())
                            )
                        );
                    }
                }
            }

            if (var6) {
                for (FoodWeaponEvolution.EvolutionStat var26 : FoodWeaponEvolution.EvolutionStat.values()) {
                    double var27 = gourmetStat(var0, var26);
                    if (!(var27 <= 0.0)) {
                        var2.add(
                            new FoodWeaponEvolution.WeaponDetailView(
                                Component.translatable("screen.ftboss.statue_hub.details.category.stat"),
                                var26.displayName(var27),
                                Component.translatable("screen.ftboss.statue_hub.details.stat_help")
                            )
                        );
                    }
                }
            }

            for (FoodWeaponEvolution.FoodBlessingView var21 : foodBlessingViews(var0)) {
                var2.add(
                    new FoodWeaponEvolution.WeaponDetailView(
                        Component.translatable("screen.ftboss.statue_hub.details.category.blessing"),
                        var21.name().copy().append(Component.literal(" " + var21.rank())),
                        var21.description()
                    )
                );
            }

            List var18 = socketViews(var0);

            for (int var22 = 0; var22 < var18.size(); var22++) {
                FoodWeaponEvolution.SocketView var25 = (FoodWeaponEvolution.SocketView)var18.get(var22);
                var2.add(
                    new FoodWeaponEvolution.WeaponDetailView(
                        Component.translatable("screen.ftboss.statue_hub.details.category.socket"),
                        Component.literal("#" + (var22 + 1) + " ")
                            .append(var25.stack().getHoverName())
                            .append(Component.literal(" · "))
                            .append(var25.type())
                            .append(Component.literal(" · "))
                            .append(var25.tier()),
                        var25.effect().copy().append(Component.literal(" · ")).append(var25.synergy())
                    )
                );
            }

            var2.addAll(FoodSocketAttributeView.details(var0));
            return List.copyOf(var2);
        }
    }

    private static void addFoodBlessingTooltip(List<Component> var0, ItemStack var1) {
        ListTag var2 = foodBlessingList(var1);
        if (!var2.isEmpty()) {
            var0.add(Component.translatable("tooltip.ftboss.food_blessings.header").withStyle(ChatFormatting.LIGHT_PURPLE));

            for (int var3 = 0; var3 < var2.size(); var3++) {
                CompoundTag var4 = var2.getCompound(var3);
                FoodWeaponEvolution.FoodBlessing var5 = FoodWeaponEvolution.FoodBlessing.byId(var4.getString("Blessing"));
                if (var5 != null) {
                    int var6 = foodBlessingLevel(var4);
                    var0.add(Component.literal("  ").append(var5.nameComponent()).append(Component.literal(" " + toRoman(var6))).withStyle(ChatFormatting.GOLD));
                    var0.add(Component.literal("    ").append(var5.descriptionComponent(var6)).withStyle(ChatFormatting.GRAY));
                }
            }

            var0.add(Component.translatable("tooltip.ftboss.food_blessings.grindstone").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static String toRoman(int var0) {
        return switch (var0) {
            case 2 -> "II";
            case 3 -> "III";
            default -> "I";
        };
    }

    public static int socketCount(ItemStack var0) {
        return sockets(var0).size();
    }

    public static int socketCapacity(ItemStack var0) {
        return socketCapacityInternal(var0);
    }

    public static List<FoodWeaponEvolution.SocketView> socketViews(ItemStack var0) {
        ArrayList var1 = new ArrayList();
        FoodWeaponEvolution.Profile var2 = activeProfile(var0);

        for (FoodWeaponEvolution.Socket var4 : sockets(var0)) {
            ItemStack var5 = socketStack(var4);
            int var6 = socketColor(var4.type);
            var1.add(
                new FoodWeaponEvolution.SocketView(
                    var5,
                    var4.type.displayName(),
                    var4.tier.displayName(),
                    var4.type.effectDescription(),
                    var4.type.setDescription(),
                    var6,
                    var2 != null && var4.type == var2.food
                )
            );
        }

        return List.copyOf(var1);
    }

    private static ItemStack socketStack(FoodWeaponEvolution.Socket var0) {
        ResourceLocation var1 = ResourceLocation.tryParse(var0.item);
        Item var2 = var1 == null ? null : (Item)BuiltInRegistries.ITEM.get(var1);
        return var2 != null && var2 != Items.AIR ? new ItemStack(var2) : ItemStack.EMPTY;
    }

    private static int socketColor(FoodWeaponEvolution.FoodType var0) {
        return switch (var0) {
            case MEAT -> 13777476;
            case CHEESE -> 16239937;
            case TEA -> 4243050;
            case WINE -> 9387702;
            case SWEET -> 16738495;
            case APPLE -> 14891583;
            case FISH -> 4116466;
            case VEGETABLE -> 5883723;
            case ENERGY -> 16748078;
            case FOOD -> 15658718;
        };
    }

    public static int socketVisualColor(ItemStack var0, int var1) {
        List var2 = sockets(var0);
        return var1 >= 1 && var1 <= var2.size() ? socketColor(((FoodWeaponEvolution.Socket)var2.get(var1 - 1)).type) : -1;
    }

    private static double socketPower(ItemStack var0, FoodWeaponEvolution.FoodType var1) {
        double var2 = 0.0;

        for (FoodWeaponEvolution.Socket var5 : sockets(var0)) {
            if (var5.type == var1) {
                var2 += var5.tier.multiplier;
            }
        }

        return var2;
    }

    private static double socketEffectPower(ItemStack var0, FoodWeaponEvolution.FoodType var1) {
        return isEvolutionExcluded(var0) ? 0.0 : socketPower(var0, var1) * weaponRole(var0).socketMultiplier(var1);
    }

    private static FoodWeaponEvolution.Profile activeProfile(ItemStack var0) {
        if (!isGourmetEvolutionWeapon(var0)) {
            return boundProfile(var0);
        } else {
            List var1 = gourmetSkills(var0);
            int var2 = activeGourmetSkillIndex(var0);
            if (var2 >= 0 && var2 < var1.size()) {
                return ((FoodWeaponEvolution.AcquiredSkill)var1.get(var2)).profile;
            } else {
                CompoundTag var3 = data(var0);
                FoodWeaponEvolution.Profile var4 = var3 == null ? null : FoodWeaponEvolution.Profile.byId(var3.getString("ActiveProfile"));
                return var4 == null ? boundProfile(var0) : var4;
            }
        }
    }

    public static int activeGourmetSkillIndex(ItemStack var0) {
        if (!isGourmetEvolutionWeapon(var0)) {
            return -1;
        } else {
            List var1 = gourmetSkills(var0);
            if (var1.isEmpty()) {
                return -1;
            } else {
                CompoundTag var2 = data(var0);
                if (var2 != null && var2.contains("ActiveSkillIndex", 3)) {
                    int var3 = var2.getInt("ActiveSkillIndex");
                    if (var3 >= 0 && var3 < var1.size()) {
                        return var3;
                    }
                }

                FoodWeaponEvolution.Profile var5 = var2 == null ? null : FoodWeaponEvolution.Profile.byId(var2.getString("ActiveProfile"));
                if (var5 != null) {
                    for (int var4 = var1.size() - 1; var4 >= 0; var4--) {
                        if (((FoodWeaponEvolution.AcquiredSkill)var1.get(var4)).profile == var5) {
                            return var4;
                        }
                    }
                }

                return var1.size() - 1;
            }
        }
    }

    private static List<FoodWeaponEvolution.AcquiredSkill> gourmetSkills(ItemStack var0) {
        ArrayList var1 = new ArrayList();
        CompoundTag var2 = data(var0);
        if (var2 != null && var2.contains("EvolutionSkills", 9)) {
            ListTag var3 = var2.getList("EvolutionSkills", 10);

            for (int var4 = 0; var4 < var3.size() && var4 < 8; var4++) {
                CompoundTag var5 = var3.getCompound(var4);
                FoodWeaponEvolution.Profile var6 = FoodWeaponEvolution.Profile.byId(var5.getString("Profile"));
                FoodWeaponEvolution.EvolutionSkill var7 = FoodWeaponEvolution.EvolutionSkill.byId(var5.getString("EvolutionSkill"));
                if (var6 != null && var7 != null) {
                    var1.add(new FoodWeaponEvolution.AcquiredSkill(var6, var7));
                }
            }

            return var1;
        } else {
            return var1;
        }
    }

    private static FoodWeaponEvolution.EvolutionSkill drawGourmetSkill(ItemStack var0, FoodWeaponEvolution.Profile var1, RandomSource var2) {
        List<FoodWeaponEvolution.AcquiredSkill> var3 = gourmetSkills(var0);
        ArrayList var4 = new ArrayList();

        for (FoodWeaponEvolution.EvolutionSkill var8 : var1.pool) {
            if (var3.stream().noneMatch(var1x -> var1x.skill == var8)) {
                var4.add(var8);
            }
        }

        if (var4.isEmpty()) {
            for (FoodWeaponEvolution.EvolutionSkill var12 : FoodWeaponEvolution.EvolutionSkill.values()) {
                if (var3.stream().noneMatch(var1x -> var1x.skill == var12)) {
                    var4.add(var12);
                }
            }
        }

        return var4.isEmpty() ? null : (FoodWeaponEvolution.EvolutionSkill)var4.get(var2.nextInt(var4.size()));
    }

    private static double gourmetStat(ItemStack var0, FoodWeaponEvolution.EvolutionStat var1) {
        CompoundTag var2 = data(var0);
        return var2 != null && var2.contains("EvolutionStats", 10) ? Math.max(0.0, var2.getCompound("EvolutionStats").getDouble(var1.id)) : 0.0;
    }

    private static int socketSetCount(ItemStack var0, FoodWeaponEvolution.FoodType var1) {
        return var1 != FoodWeaponEvolution.FoodType.SWEET && var1 != FoodWeaponEvolution.FoodType.FOOD
            ? socketCount(var0, var1)
            : socketCount(var0, FoodWeaponEvolution.FoodType.SWEET) + socketCount(var0, FoodWeaponEvolution.FoodType.FOOD);
    }

    private static int socketSetTier(ItemStack var0, FoodWeaponEvolution.FoodType var1) {
        ArrayList<Integer> var2 = new ArrayList<>();

        for (FoodWeaponEvolution.Socket var4 : sockets(var0)) {
            boolean var5 = var4.type == var1
                || (var1 == FoodWeaponEvolution.FoodType.SWEET || var1 == FoodWeaponEvolution.FoodType.FOOD)
                    && (var4.type == FoodWeaponEvolution.FoodType.SWEET || var4.type == FoodWeaponEvolution.FoodType.FOOD);
            if (var5) {
                var2.add(var4.tier.level);
            }
        }

        if (var2.isEmpty()) {
            return 1;
        } else {
            var2.sort(Comparator.reverseOrder());
            int var6 = Math.min(3, var2.size());
            int var7 = 0;

            for (int var8 = 0; var8 < var6; var8++) {
                var7 += var2.get(var8);
            }

            return Math.max(1, Math.min(3, var7 / var6));
        }
    }

    public static boolean isSocketIngredient(ItemStack var0) {
        return foodType(var0) != null;
    }

    public static boolean isDailySubmissionCandidate(List<FoodWeaponEvolution.StatueDailyView> var0, ItemStack var1, int var2) {
        FoodWeaponEvolution.FoodType var3 = foodType(var1);
        FoodWeaponEvolution.Profile var4 = FoodWeaponEvolution.Profile.byTint(var2);
        if (!var1.isEmpty() && var3 != null && var4 != null) {
            for (FoodWeaponEvolution.StatueDailyView var6 : var0) {
                if (var6.state() == 0 && (var6.requestItemId().isBlank() ? var3 == var4.food : var6.requestItemId().equals(itemId(var1)))) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public static boolean isBlessingCandidate(ItemStack var0, ItemStack var1, int var2) {
        FoodWeaponEvolution.Profile var3 = FoodWeaponEvolution.Profile.byTint(var2);
        FoodWeaponEvolution.Profile var4 = boundProfile(var0);
        FoodWeaponEvolution.FoodType var5 = foodType(var1);
        return var3 != null
            && var4 != null
            && !var1.isEmpty()
            && !var0.is(Items.ENCHANTED_BOOK)
            && var1 != var0
            && !weaponCandidate(var1)
            && (isGourmetEvolutionWeapon(var0) || var4 == var3)
            && var5 == var3.food
            && foodBlessingList(var0).size() < 3;
    }

    public static boolean isSocketCandidate(ItemStack var0, ItemStack var1, int var2) {
        FoodWeaponEvolution.Profile var3 = FoodWeaponEvolution.Profile.byTint(var2);
        FoodWeaponEvolution.Profile var4 = boundProfile(var0);
        return var3 != null
            && var4 != null
            && !var1.isEmpty()
            && foodType(var1) != null
            && (isGourmetEvolutionWeapon(var0) || var4 == var3)
            && sockets(var0).size() < socketCapacityInternal(var0);
    }

    public static FoodWeaponEvolution.SocketView ingredientView(ItemStack var0) {
        FoodWeaponEvolution.FoodType var1 = foodType(var0);
        if (var1 == null) {
            return null;
        } else {
            FoodWeaponEvolution.SocketTier var2 = foodTier(var0);
            return new FoodWeaponEvolution.SocketView(
                var0.copy(), var1.displayName(), var2.displayName(), var1.effectDescription(), var1.setDescription(), socketColor(var1), false
            );
        }
    }

    public static int currentEvolutionFloor(ItemStack var0) {
        int var1 = evolutionLevel(var0);
        return var1 <= 0 ? 0 : threshold(var0, var1 - 1);
    }

    private static FoodWeaponEvolution.FoodType foodType(ItemStack var0) {
        if (var0.isEmpty()) {
            return null;
        } else {
            FoodWeaponEvolution.FoodType var1 = explicitOfferingType(itemId(var0).toLowerCase(Locale.ROOT));
            return var1 != null ? var1 : FoodSocketFoodCatalog.type(var0);
        }
    }

    private static FoodWeaponEvolution.FoodType explicitOfferingType(String var0) {
        return switch (var0) {
            case "ftboss:sugar_queen_crystal", "ftboss:frost_crown_charm" -> FoodWeaponEvolution.FoodType.SWEET;
            case "ftboss:meat_heart", "ftboss:butcher_fang_charm", "ftboss:benger_whistle", "ftboss:shepherd_whistle_charm" -> FoodWeaponEvolution.FoodType.MEAT;
            case "ftboss:tea_seed", "ftboss:tea_heart_pendant" -> FoodWeaponEvolution.FoodType.TEA;
            case "ftboss:wine_charm", "ftboss:aged_wine_seal" -> FoodWeaponEvolution.FoodType.WINE;
            case "ftboss:rat_bait", "ftboss:cheese_moon_charm" -> FoodWeaponEvolution.FoodType.CHEESE;
            case "ftboss:defect_core", "ftboss:charged_core_pendant" -> FoodWeaponEvolution.FoodType.ENERGY;
            case "ftboss:compost_summon", "ftboss:rainbow_spore_charm" -> FoodWeaponEvolution.FoodType.VEGETABLE;
            case "ftboss:forbidden_apple", "ftboss:golden_apple_brooch" -> FoodWeaponEvolution.FoodType.APPLE;
            case "ftboss:tide_banquet", "ftboss:tide_pearl", "ftboss:tide_pearl_pendant" -> FoodWeaponEvolution.FoodType.FISH;
            default -> null;
        };
    }

    private static FoodWeaponEvolution.SocketTier baseFoodTier(ItemStack var0) {
        int var10000 = FoodSocketQuality.baseOverride(var0);
        if (var10000 >= 0) {
            return FoodWeaponEvolution.SocketTier.byLevel(var10000);
        } else if (var0.isEmpty()) {
            return FoodWeaponEvolution.SocketTier.BASIC;
        } else {
            CompoundTag var1 = ItemData.nbt(var0);
            if (var1 != null && var1.contains("FTBossSocketTier", 3)) {
                return FoodWeaponEvolution.SocketTier.byLevel(var1.getInt("FTBossSocketTier"));
            } else {
                String var2 = itemId(var0).toLowerCase(Locale.ROOT);
                String var3 = var2.substring(Math.max(0, var2.indexOf(58) + 1));
                if (!var2.equals("minecraft:golden_apple")
                    && !var2.equals("minecraft:enchanted_golden_apple")
                    && !containsFoodWord(
                        var3,
                        "deluxe",
                        "supreme",
                        "royal",
                        "gourmet",
                        "legendary",
                        "ultimate",
                        "banquet",
                        "feast",
                        "grand",
                        "stuffed",
                        "honey_glazed",
                        "truffle",
                        "lobster"
                    )) {
                    Rarity var4 = var0.getRarity();
                    if (var4 != Rarity.EPIC && var4 != Rarity.RARE) {
                        FoodProperties var5 = var0.getFoodProperties(null);
                        if (var5 != null) {
                            int var6 = var5.nutrition();
                            float var7 = var5.saturation();
                            if (var6 >= 8 && var7 >= 0.8F || (float)var6 + var7 * 4.0F >= 11.0F) {
                                return FoodWeaponEvolution.SocketTier.GOURMET;
                            }

                            if ((float)var6 + var7 * 4.0F >= 6.0F) {
                                return FoodWeaponEvolution.SocketTier.FINE;
                            }
                        }

                        return var4 != Rarity.UNCOMMON
                                && !containsFoodWord(
                                    var3,
                                    "cooked",
                                    "baked",
                                    "grilled",
                                    "smoked",
                                    "fried",
                                    "roast",
                                    "sandwich",
                                    "burger",
                                    "stew",
                                    "soup",
                                    "pie",
                                    "cake",
                                    "tart",
                                    "pizza",
                                    "sushi",
                                    "nigiri",
                                    "roll",
                                    "latte",
                                    "cappuccino",
                                    "cocktail"
                                )
                            ? FoodWeaponEvolution.SocketTier.BASIC
                            : FoodWeaponEvolution.SocketTier.FINE;
                    } else {
                        return FoodWeaponEvolution.SocketTier.GOURMET;
                    }
                } else {
                    return FoodWeaponEvolution.SocketTier.GOURMET;
                }
            }
        }
    }

    private static FoodWeaponEvolution.SocketTier foodTierById(String var0) {
        ResourceLocation var1 = ResourceLocation.tryParse(var0);
        Item var2 = var1 == null ? null : (Item)BuiltInRegistries.ITEM.get(var1);
        return var2 != null && var2 != Items.AIR ? foodTier(new ItemStack(var2)) : FoodWeaponEvolution.SocketTier.BASIC;
    }

    private static boolean containsFoodWord(String var0, String... var1) {
        String var2 = "_" + var0.replace('/', '_').replace('-', '_').replace('.', '_') + "_";

        for (String var6 : var1) {
            if (var2.contains("_" + var6 + "_")) {
                return true;
            }
        }

        return false;
    }

    private static boolean attune(ItemStack var0, FoodWeaponEvolution.Profile var1) {
        FoodWeaponEvolution.Profile var2 = boundProfile(var0);
        if (var2 == null) {
            if (!isGourmetEvolutionWeapon(var0) && !externalWeaponCandidate(var0)) {
                return false;
            } else {
                CompoundTag var3 = writableData(var0, var1);
                var3.putInt("Experience", 0);
                var3.putInt("Level", 0);
                saveData(var0, var3);
                return true;
            }
        } else {
            return isGourmetEvolutionWeapon(var0) || var2 == var1;
        }
    }

    private static int addExperience(ItemStack var0, FoodWeaponEvolution.Profile var1, int var2) {
        if (var2 > 0 && attune(var0, var1) && (isGourmetEvolutionWeapon(var0) || boundProfile(var0) == var1)) {
            int var3 = threshold(var0, maxLevel(var0) - 1);
            CompoundTag var4 = writableData(var0, var1);
            int var5 = Math.max(0, var4.getInt("Experience"));
            int var6 = Math.min(var3, var5 + var2);
            var4.putInt("Experience", var6);
            saveData(var0, var4);
            return var6 - var5;
        } else {
            return 0;
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGH
    )
    public static void useStatue(RightClickBlock var0) {
    }

    private static CompoundTag daily(ServerPlayer var0, FoodWeaponEvolution.Profile var1, boolean var2) {
        CompoundTag var3 = var0.getPersistentData().getCompound("FTBossFoodEvolutionDailies");
        long var4 = currentDailyDay(var0);
        if (!var3.contains("MinecraftDay", 4) || var3.getLong("MinecraftDay") != var4) {
            boolean var6 = !var3.isEmpty();
            var3 = new CompoundTag();
            var3.putLong("MinecraftDay", var4);
            var0.getPersistentData().put("FTBossFoodEvolutionDailies", var3);
            if (var6) {
                var0.sendSystemMessage(Component.translatable("message.ftboss.evolution.daily_refreshed").withStyle(ChatFormatting.AQUA));
            }
        }

        CompoundTag var7 = var3.getCompound(var1.id);
        if (ensureDailyTasks(var0, var1, var4, var7, var2)) {
            var3.put(var1.id, var7);
            var0.getPersistentData().put("FTBossFoodEvolutionDailies", var3);
        }

        return var7;
    }

    private static CompoundTag daily(ServerPlayer var0, FoodWeaponEvolution.Profile var1) {
        return daily(var0, var1, false);
    }

    private static long currentDailyDay(ServerPlayer var0) {
        long var1 = var0.serverLevel().getServer().overworld().getDayTime();
        return Math.floorDiv(var1, 24000L);
    }

    private static void saveDaily(ServerPlayer var0, FoodWeaponEvolution.Profile var1, CompoundTag var2) {
        CompoundTag var3 = var0.getPersistentData().getCompound("FTBossFoodEvolutionDailies");
        var3.put(var1.id, var2);
        var0.getPersistentData().put("FTBossFoodEvolutionDailies", var3);
    }

    private static boolean ensureDailyTasks(ServerPlayer var0, FoodWeaponEvolution.Profile var1, long var2, CompoundTag var4, boolean var5) {
        boolean var6 = false;
        if (var4.contains("RequestedItem", 8) && !var4.contains(taskKey(0), 10)) {
            CompoundTag var7 = new CompoundTag();
            var7.putString("RequestedItem", var4.getString("RequestedItem"));
            var7.putInt("Required", 8);
            var7.putString("RewardType", FoodWeaponEvolution.DailyRewardType.EVOLUTION.id);
            var7.putInt("RewardAmount", 40);
            var7.putInt("Progress", var4.getInt("Progress"));
            var7.putBoolean("Ready", var4.getBoolean("Ready"));
            var7.putBoolean("Claimed", var4.getBoolean("Claimed"));
            var4.put(taskKey(0), var7);

            for (String var9 : List.of("RequestedItem", "Progress", "Ready", "Claimed")) {
                var4.remove(var9);
            }

            var6 = true;
        }

        List var20 = dailyFoodPool(var0, var1.food);
        boolean[] var21 = new boolean[8];
        boolean var22 = false;
        ArrayList var10 = new ArrayList();

        for (int var11 = 0; var11 < 8; var11++) {
            CompoundTag var12 = var4.getCompound(taskKey(var11));
            var21[var11] = validDailyTask(var12, var20);
            if (var21[var11]) {
                if (var5 && !var12.getBoolean("GoldenStatue")) {
                    upgradeGoldenDailyTask(var12);
                    var4.put(taskKey(var11), var12);
                    var6 = true;
                }

                if (FoodWeaponEvolution.DailyRewardType.byId(var12.getString("RewardType")) == FoodWeaponEvolution.DailyRewardType.EVOLUTION) {
                    var22 = true;
                }

                ResourceLocation var13 = dailyRequestId(var12);
                if (var13 != null) {
                    var10.add(var13);
                }
            }
        }

        long var23 = dailySeed(var0, var1, var2);
        int var24 = positiveMod(mix64(var23 ^ 7146057691288625177L), 8);
        int var14 = -1;
        if (!var22) {
            if (!var21[var24]) {
                var14 = var24;
            } else {
                for (int var15 = 7; var15 >= 0; var15--) {
                    if (!var21[var15]) {
                        var14 = var15;
                        break;
                    }
                }
            }
        }

        int var25 = positiveMod(mix64(var23 ^ -7723592293110705685L), 2);

        for (int var16 = 0; var16 < 8; var16++) {
            if (!var21[var16]) {
                FoodWeaponEvolution.DailyRewardType var17 = var16 == var14
                    ? FoodWeaponEvolution.DailyRewardType.EVOLUTION
                    : ((var16 + var25 & 1) == 0 ? FoodWeaponEvolution.DailyRewardType.EXPERIENCE : FoodWeaponEvolution.DailyRewardType.COINS);
                CompoundTag var18 = createDailyTask(var0, var1, var2, var16, var17, var20, var10, var5);
                var4.put(taskKey(var16), var18);
                ResourceLocation var19 = dailyRequestId(var18);
                if (var19 != null) {
                    var10.add(var19);
                }

                var6 = true;
            }
        }

        return var6;
    }

    private static String taskKey(int var0) {
        return "Task" + var0;
    }

    private static boolean validDailyTask(CompoundTag var0, List<ResourceLocation> var1) {
        if (!var0.isEmpty()
            && var0.getInt("Required") > 0
            && var0.getInt("RewardAmount") > 0
            && FoodWeaponEvolution.DailyRewardType.byId(var0.getString("RewardType")) != null) {
            String var2 = var0.getString("RequestedItem");
            if (var2.isBlank()) {
                return var1.isEmpty();
            } else {
                ResourceLocation var3 = ResourceLocation.tryParse(var2);
                return var3 != null && var1.contains(var3);
            }
        } else {
            return false;
        }
    }

    private static CompoundTag createDailyTask(
        ServerPlayer var0,
        FoodWeaponEvolution.Profile var1,
        long var2,
        int var4,
        FoodWeaponEvolution.DailyRewardType var5,
        List<ResourceLocation> var6,
        List<ResourceLocation> var7,
        boolean var8
    ) {
        CompoundTag var9 = new CompoundTag();
        if (!var6.isEmpty()) {
            long var10 = var2 * 8L + (long)var4;
            ResourceLocation var12 = null;

            for (int var13 = 0; var13 < var6.size(); var13++) {
                ResourceLocation var14 = (ResourceLocation)var6.get(dailyPoolIndex(var0, var1, var10 + (long)var13, var6.size()));
                if (!var7.contains(var14) || var7.size() >= var6.size()) {
                    var12 = var14;
                    break;
                }
            }

            if (var12 == null) {
                var12 = (ResourceLocation)var6.get(dailyPoolIndex(var0, var1, var10, var6.size()));
            }

            var9.putString("RequestedItem", var12.toString());
        }

        long var15 = dailySeed(var0, var1, var2) ^ ((long)var4 + 1L) * -3335678366873096957L;
        int var16 = 4 + positiveMod(mix64(var15), 4) * 2;
        int var17 = positiveMod(mix64(var15 ^ -7723592293110705685L), 4);

        int var18 = switch (var5) {
            case EVOLUTION -> 25 + var17 * 5;
            case EXPERIENCE -> 900 + var17 * 300;
            case COINS -> 180 + var17 * 60;
        };
        var9.putInt("Required", var16);
        var9.putString("RewardType", var5.id);
        var9.putInt("RewardAmount", var18);
        if (var8) {
            upgradeGoldenDailyTask(var9);
        }

        return var9;
    }

    private static void upgradeGoldenDailyTask(CompoundTag var0) {
        if (var0 != null && !var0.getBoolean("GoldenStatue")) {
            int var1 = Math.max(1, var0.getInt("Required"));
            int var2 = Math.max(1, var0.getInt("RewardAmount"));
            int var3 = (var1 * 5 + 3) / 4;
            var0.putInt("Required", var3);
            var0.putInt("RewardAmount", var2 * 2);
            if (!var0.getBoolean("Claimed") && var0.getInt("Progress") < var3) {
                var0.putBoolean("Ready", false);
            }

            var0.putBoolean("GoldenStatue", true);
        }
    }

    private static long dailySeed(ServerPlayer var0, FoodWeaponEvolution.Profile var1, long var2) {
        UUID var4 = var0.getUUID();
        return mix64(
            var4.getMostSignificantBits()
                ^ Long.rotateLeft(var4.getLeastSignificantBits(), 23)
                ^ ((long)var1.ordinal() + 1L) * -7046029254386353131L
                ^ var2 * -4658895280553007687L
        );
    }

    private static synchronized List<ResourceLocation> dailyFoodPool(ServerPlayer var0, FoodWeaponEvolution.FoodType var1) {
        Set var2 = dailyRecipeOutputs(var0);
        List var3 = DAILY_FOOD_POOLS.get(var1);
        if (var3 != null) {
            return var3;
        } else {
            ArrayList<ResourceLocation> var4 = new ArrayList<>();

            for (ResourceLocation var6 : BuiltInRegistries.ITEM.keySet()) {
                if (isDailyFoodCandidate(var6, var1, var2)) {
                    var4.add(var6);
                }
            }

            var4.sort(ResourceLocation::compareTo);
            List var7 = List.copyOf(var4);
            DAILY_FOOD_POOLS.put(var1, var7);
            return var7;
        }
    }

    private static boolean isDailyFoodCandidate(ResourceLocation var0, FoodWeaponEvolution.FoodType var1, Set<ResourceLocation> var2) {
        Item var3 = (Item)BuiltInRegistries.ITEM.get(var0);
        if (var3 != null && var3 != Items.AIR) {
            ItemStack var4 = new ItemStack(var3);
            if (!var4.isEmpty() && foodType(var4) == var1) {
                boolean var5 = var4.has(DataComponents.FOOD) || var4.getUseAnimation() == UseAnim.DRINK || isNamedDrink(var0.getPath(), var1);
                return var5 && (var2.contains(var0) || isNaturallyObtainableDailyFood(var4, var0));
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean isNaturallyObtainableDailyFood(ItemStack var0, ResourceLocation var1) {
        if ("minecraft".equals(var1.getNamespace()) && VANILLA_NATURAL_DAILY_FOODS.contains(var1.getPath())) {
            return true;
        } else {
            for (TagKey var3 : DAILY_NATURAL_FOOD_TAGS) {
                if (var0.is(var3)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static TagKey<Item> dailyFoodTag(String var0) {
        ResourceLocation var1 = ResourceLocation.tryParse(var0);
        if (var1 == null) {
            throw new IllegalArgumentException("Invalid daily food tag: " + var0);
        } else {
            return TagKey.create(Registries.ITEM, var1);
        }
    }

    private static synchronized Set<ResourceLocation> dailyRecipeOutputs(ServerPlayer var0) {
        RecipeManager var1 = var0.serverLevel().getServer().getRecipeManager();
        int var2 = 31 * var1.getRecipes().size() + var1.getRecipeIds().mapToInt(ResourceLocation::hashCode).sum();
        if (var1 == dailyRecipeManager && var2 == dailyRecipeFingerprint) {
            return dailyRecipeOutputs;
        } else {
            HashSet var3 = new HashSet();

            for (RecipeHolder var5 : var1.getRecipes()) {
                Recipe var6 = var5.value();

                try {
                    ItemStack var7 = var6.getResultItem(var0.serverLevel().registryAccess());
                    if (!var7.isEmpty()) {
                        var3.add(BuiltInRegistries.ITEM.getKey(var7.getItem()));
                    }
                } catch (RuntimeException var8) {
                }
            }

            var3.remove(null);
            dailyRecipeManager = var1;
            dailyRecipeFingerprint = var2;
            dailyRecipeOutputs = Set.copyOf(var3);
            DAILY_FOOD_POOLS.clear();
            return dailyRecipeOutputs;
        }
    }

    private static boolean isNamedDrink(String var0, FoodWeaponEvolution.FoodType var1) {
        if (containsFoodWord(
            var0, "seed", "seeds", "leaf", "leaves", "blend", "bag", "crop", "block", "crate", "bucket", "spawn_egg", "powder", "bean", "mace", "charm"
        )) {
            return false;
        } else {
            return switch (var1) {
                case TEA -> containsFoodWord(var0, "tea", "matcha", "oolong", "kombucha", "herbal_infusion");
                case WINE -> containsFoodWord(
                var0,
                "wine",
                "beer",
                "ale",
                "mead",
                "cider",
                "liquor",
                "vodka",
                "whiskey",
                "whisky",
                "brew",
                "sake",
                "rum",
                "stout",
                "grog",
                "champagne",
                "brandy",
                "bourbon",
                "absinthe",
                "tequila",
                "sangria",
                "cocktail",
                "lager",
                "kvass",
                "gin_tonic"
            );
                case ENERGY -> containsFoodWord(var0, "coffee", "espresso", "latte", "cappuccino", "mocha", "hot_cocoa", "cola", "energy_drink");
                default -> false;
            };
        }
    }

    private static int dailyPoolIndex(ServerPlayer var0, FoodWeaponEvolution.Profile var1, long var2, int var4) {
        if (var4 <= 1) {
            return 0;
        } else {
            UUID var5 = var0.getUUID();
            long var6 = mix64(
                var5.getMostSignificantBits() ^ Long.rotateLeft(var5.getLeastSignificantBits(), 23) ^ ((long)var1.ordinal() + 1L) * -7046029254386353131L
            );
            int var8 = positiveMod(var6, var4);
            int var9 = 1 + positiveMod(mix64(var6 ^ -3335678366873096957L), var4 - 1);

            while (greatestCommonDivisor(var9, var4) != 1) {
                var9 = var9 == var4 - 1 ? 1 : var9 + 1;
            }

            return positiveMod((long)var8 + var2 * (long)var9, var4);
        }
    }

    private static int greatestCommonDivisor(int var0, int var1) {
        while (var1 != 0) {
            int var2 = var0 % var1;
            var0 = var1;
            var1 = var2;
        }

        return Math.abs(var0);
    }

    private static int positiveMod(long var0, int var2) {
        return (int)Math.floorMod(var0, (long)var2);
    }

    private static long mix64(long var0) {
        var0 = (var0 ^ var0 >>> 30) * -4658895280553007687L;
        var0 = (var0 ^ var0 >>> 27) * -7723592293110705685L;
        return var0 ^ var0 >>> 31;
    }

    private static ResourceLocation dailyRequestId(CompoundTag var0) {
        String var1 = var0.getString("RequestedItem");
        return var1.isBlank() ? null : ResourceLocation.tryParse(var1);
    }

    private static Component dailyRequestName(CompoundTag var0, FoodWeaponEvolution.Profile var1) {
        ResourceLocation var2 = dailyRequestId(var0);
        Item var3 = var2 == null ? null : (Item)BuiltInRegistries.ITEM.get(var2);
        return var3 == null ? var1.food.displayName() : new ItemStack(var3).getHoverName();
    }

    private static boolean matchesDailyRequest(CompoundTag var0, FoodWeaponEvolution.Profile var1, ItemStack var2, FoodWeaponEvolution.FoodType var3) {
        ResourceLocation var4 = dailyRequestId(var0);
        return var4 == null ? var3 == var1.food : var4.toString().equals(itemId(var2));
    }

    public static List<FoodWeaponEvolution.StatueDailyView> statueDailyViews(ServerPlayer var0, int var1) {
        return statueDailyViews(var0, var1, false);
    }

    public static List<FoodWeaponEvolution.StatueDailyView> statueDailyViews(ServerPlayer var0, int var1, boolean var2) {
        FoodWeaponEvolution.Profile var3 = FoodWeaponEvolution.Profile.byTint(var1);
        if (var3 == null) {
            return List.of();
        } else {
            CompoundTag var4 = daily(var0, var3, var2);
            ArrayList var5 = new ArrayList(8);

            for (int var6 = 0; var6 < 8; var6++) {
                CompoundTag var7 = var4.getCompound(taskKey(var6));
                int var8 = Math.max(1, var7.getInt("Required"));
                int var9 = Math.min(var8, Math.max(0, var7.getInt("Progress")));
                int var10 = var7.getBoolean("Claimed") ? 2 : (var7.getBoolean("Ready") ? 1 : 0);
                ResourceLocation var11 = dailyRequestId(var7);
                var5.add(
                    new FoodWeaponEvolution.StatueDailyView(
                        dailyRequestName(var7, var3), var11 == null ? "" : var11.toString(), var9, var8, dailyRewardName(var7), var10
                    )
                );
            }

            return List.copyOf(var5);
        }
    }

    private static void showDaily(ServerPlayer var0, FoodWeaponEvolution.Profile var1) {
        boolean var2 = false;
        CompoundTag var3 = daily(var0, var1, var2);
        var0.sendSystemMessage(
            Component.translatable("message.ftboss.evolution.daily_header", new Object[]{var1.displayName(), 8}).withStyle(ChatFormatting.GOLD)
        );
        if (var2) {
            var0.sendSystemMessage(
                Component.translatable("message.ftboss.evolution.golden_daily").withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD})
            );
        }

        for (int var4 = 0; var4 < 8; var4++) {
            CompoundTag var5 = var3.getCompound(taskKey(var4));
            int var6 = var5.getInt("Required");
            int var7 = Math.min(var6, var5.getInt("Progress"));
            Component var8 = dailyRewardName(var5);
            if (var5.getBoolean("Claimed")) {
                var0.sendSystemMessage(
                    Component.translatable("message.ftboss.evolution.daily_task_claimed", new Object[]{var4 + 1, dailyRequestName(var5, var1), var8})
                        .withStyle(ChatFormatting.DARK_GRAY)
                );
            } else if (var5.getBoolean("Ready")) {
                var0.sendSystemMessage(
                    Component.translatable("message.ftboss.evolution.daily_task_ready", new Object[]{var4 + 1, dailyRequestName(var5, var1), var8})
                        .withStyle(ChatFormatting.GREEN)
                );
            } else {
                var0.sendSystemMessage(
                    Component.translatable("message.ftboss.evolution.daily_task", new Object[]{var4 + 1, dailyRequestName(var5, var1), var7, var6, var8})
                        .withStyle(ChatFormatting.YELLOW)
                );
            }
        }

        var0.sendSystemMessage(Component.translatable("message.ftboss.evolution.statue_help").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static void submitDailyFood(ServerPlayer var0, FoodWeaponEvolution.Profile var1, ItemStack var2, FoodWeaponEvolution.FoodType var3) {
        submitDailyFood(var0, var1, var2, var3, false);
    }

    private static void submitDailyFood(ServerPlayer var0, FoodWeaponEvolution.Profile var1, ItemStack var2, FoodWeaponEvolution.FoodType var3, boolean var4) {
        CompoundTag var5 = daily(var0, var1, var4);
        CompoundTag var6 = null;
        int var7 = -1;
        boolean var8 = false;

        for (int var9 = 0; var9 < 8; var9++) {
            CompoundTag var10 = var5.getCompound(taskKey(var9));
            if (matchesDailyRequest(var10, var1, var2, var3)) {
                if (!var10.getBoolean("Claimed") && !var10.getBoolean("Ready")) {
                    var6 = var10;
                    var7 = var9;
                    break;
                }

                var8 = true;
            }
        }

        if (var6 == null) {
            if (var8) {
                var0.displayClientMessage(Component.translatable("message.ftboss.evolution.daily_item_done", new Object[]{var2.getHoverName()}), true);
            } else {
                var0.displayClientMessage(
                    Component.translatable("message.ftboss.evolution.wrong_food_multi", new Object[]{unfinishedDailyFoods(var5, var1)}), true
                );
            }
        } else {
            int var14 = var6.getInt("Required");
            int var15 = Math.max(0, var6.getInt("Progress"));
            int var11 = Math.min(var2.getCount(), var14 - var15);
            if (var11 > 0) {
                if (!var0.getAbilities().instabuild) {
                    var2.shrink(var11);
                }

                int var12 = var15 + var11;
                var6.putInt("Progress", var12);
                var5.put(taskKey(var7), var6);
                if (var12 >= var14) {
                    FoodWeaponEvolution.DailyRewardType var13 = FoodWeaponEvolution.DailyRewardType.byId(var6.getString("RewardType"));
                    if (var13 == FoodWeaponEvolution.DailyRewardType.EVOLUTION) {
                        var6.putBoolean("Ready", true);
                        var0.sendSystemMessage(
                            Component.translatable("message.ftboss.evolution.daily_evolution_ready", new Object[]{var7 + 1, dailyRewardName(var6)})
                                .withStyle(ChatFormatting.GOLD)
                        );
                    } else {
                        grantImmediateDailyReward(var0, var13, var6.getInt("RewardAmount"));
                        var6.putBoolean("Claimed", true);
                        var0.sendSystemMessage(
                            Component.translatable("message.ftboss.evolution.daily_rewarded", new Object[]{var7 + 1, dailyRewardName(var6)})
                                .withStyle(ChatFormatting.GOLD)
                        );
                    }

                    var0.serverLevel().playSound(null, var0.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.65F, 1.35F);
                } else {
                    var0.displayClientMessage(
                        Component.translatable("message.ftboss.evolution.daily_progress", new Object[]{dailyRequestName(var6, var1), var12, var14}), true
                    );
                }

                saveDaily(var0, var1, var5);
            }
        }
    }

    private static Component dailyRewardName(CompoundTag var0) {
        FoodWeaponEvolution.DailyRewardType var1 = FoodWeaponEvolution.DailyRewardType.byId(var0.getString("RewardType"));
        return (Component)(var1 == null ? Component.literal("?") : var1.displayName(var0.getInt("RewardAmount")));
    }

    private static Component unfinishedDailyFoods(CompoundTag var0, FoodWeaponEvolution.Profile var1) {
        MutableComponent var2 = Component.empty();
        boolean var3 = true;

        for (int var4 = 0; var4 < 8; var4++) {
            CompoundTag var5 = var0.getCompound(taskKey(var4));
            if (!var5.getBoolean("Claimed") && !var5.getBoolean("Ready")) {
                if (!var3) {
                    var2.append(Component.literal(" / "));
                }

                var2.append(dailyRequestName(var5, var1));
                var3 = false;
            }
        }

        return var3 ? Component.translatable("message.ftboss.evolution.daily_all_done") : var2;
    }

    private static void grantImmediateDailyReward(ServerPlayer var0, FoodWeaponEvolution.DailyRewardType var1, int var2) {
        if (var1 == FoodWeaponEvolution.DailyRewardType.EXPERIENCE) {
            var0.giveExperiencePoints(var2);
        } else if (var1 == FoodWeaponEvolution.DailyRewardType.COINS) {
            Item var3 = COIN_ID == null ? null : (Item)BuiltInRegistries.ITEM.get(COIN_ID);
            if (var3 == null || var3 == Items.AIR) {
                var3 = Items.GOLD_NUGGET;
            }

            int var4 = var2;

            while (var4 > 0) {
                ItemStack var5 = new ItemStack(var3, Math.min(var4, new ItemStack(var3).getMaxStackSize()));
                var4 -= var5.getCount();
                if (!var0.addItem(var5) && !var5.isEmpty()) {
                    var0.drop(var5, false);
                }
            }
        }
    }

    private static CompoundTag readyEvolutionTask(CompoundTag var0) {
        for (int var1 = 0; var1 < 8; var1++) {
            CompoundTag var2 = var0.getCompound(taskKey(var1));
            if (var2.getBoolean("Ready")
                && !var2.getBoolean("Claimed")
                && FoodWeaponEvolution.DailyRewardType.byId(var2.getString("RewardType")) == FoodWeaponEvolution.DailyRewardType.EVOLUTION) {
                return var2;
            }
        }

        return null;
    }

    private static void handleWeapon(ServerPlayer var0, FoodWeaponEvolution.Profile var1, ItemStack var2, boolean var3) {
        handleWeapon(var0, var1, var2, var3, false);
    }

    private static void handleWeapon(ServerPlayer var0, FoodWeaponEvolution.Profile var1, ItemStack var2, boolean var3, boolean var4) {
        FoodWeaponEvolution.Profile var5 = nativeProfile(var2);
        FoodWeaponEvolution.Profile var6 = boundProfile(var2);
        boolean var7 = isGourmetEvolutionWeapon(var2);
        boolean var8 = var5 != null && evolutionLevel(var2) == 1 && var3;
        if (var7 || var8 || (var5 == null || var5 == var1) && (var6 == null || var6 == var1)) {
            if (var6 == null) {
                if (!attune(var2, var1)) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.evolution.not_weapon"), true);
                    return;
                }

                var0.sendSystemMessage(
                    Component.translatable("message.ftboss.evolution.attuned", new Object[]{var2.getHoverName(), var1.displayName()})
                        .withStyle(ChatFormatting.AQUA)
                );
            }

            CompoundTag var12 = daily(var0, var1, var4);
            CompoundTag var10 = readyEvolutionTask(var12);
            if (!var8 && !var3 && var10 != null) {
                int var11 = addExperience(var2, var1, var10.getInt("RewardAmount"));
                if (var11 <= 0) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.evolution.maxed"), true);
                } else {
                    var10.putBoolean("Claimed", true);
                    saveDaily(var0, var1, var12);
                    var0.sendSystemMessage(
                        Component.translatable(
                                "message.ftboss.evolution.claimed", new Object[]{var2.getHoverName(), var11, writableData(var2, var1).getInt("Experience")}
                            )
                            .withStyle(ChatFormatting.GREEN)
                    );
                }
            } else {
                if (var3) {
                    evolve(var0, var1, var2);
                } else {
                    showWeapon(var0, var2, var1);
                }
            }
        } else {
            FoodWeaponEvolution.Profile var9 = var6 == null ? var5 : var6;
            var0.displayClientMessage(Component.translatable("message.ftboss.evolution.wrong_statue", new Object[]{var9.displayName()}), true);
        }
    }

    private static void evolve(ServerPlayer var0, FoodWeaponEvolution.Profile var1, ItemStack var2) {
        CompoundTag var3 = writableData(var2, var1);
        int var4 = Math.max(0, var3.getInt("Level"));
        int var5 = maxLevel(var2);
        if (var4 >= var5) {
            var0.displayClientMessage(Component.translatable("message.ftboss.evolution.maxed"), true);
        } else {
            int var6 = threshold(var2, var4);
            int var7 = Math.max(0, var3.getInt("Experience"));
            if (var7 < var6) {
                var0.displayClientMessage(Component.translatable("message.ftboss.evolution.need_xp", new Object[]{var7, var6}), true);
            } else {
                var3.putInt("Level", ++var4);
                var3.remove("BlessingWashed");
                boolean var8 = isGourmetEvolutionWeapon(var2);
                if (!var8 && var4 == 2) {
                    var3.putString("SecondProfile", var1.id);
                }

                FoodWeaponEvolution.EvolutionSkill var9 = null;
                FoodWeaponEvolution.EvolutionStat var10 = null;
                if (var8) {
                    var3.putString("ActiveProfile", var1.id);
                    var9 = drawGourmetSkill(var2, var1, var0.getRandom());
                    if (var9 != null) {
                        ListTag var11 = var3.getList("EvolutionSkills", 10);
                        CompoundTag var12 = new CompoundTag();
                        var12.putString("Profile", var1.id);
                        var12.putString("EvolutionSkill", var9.id);
                        var11.add(var12);
                        var3.put("EvolutionSkills", var11);
                        var3.putInt("ActiveSkillIndex", Math.max(0, var11.size() - 1));
                    }

                    var10 = FoodWeaponEvolution.EvolutionStat.random(weaponRole(var2), var0.getRandom());
                    CompoundTag var16 = var3.getCompound("EvolutionStats");
                    var16.putDouble(var10.id, Math.max(0.0, var16.getDouble(var10.id)) + var10.gain);
                    var3.put("EvolutionStats", var16);
                    GourmetEvolutionWeapon.animateEvolution(var0, var2);
                } else if (nativeProfile(var2) == null && var4 == 1) {
                    var9 = var1.pool[var0.getRandom().nextInt(var1.pool.length)];
                    var3.putString("EvolutionSkill", var9.id);
                }

                saveData(var2, var3);
                playEvolutionSuccessParticles(var0, var8);
                var0.serverLevel()
                    .playSound(
                        null, var0.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 0.75F + (float)Math.min(var4, 5) * 0.12F
                    );
                Object var17;
                if (var9 != null) {
                    var17 = var9.nameComponent();
                } else {
                    Aspect var18 = Aspect.fromId(boundProfile(var2).id);
                    if (var4 != 1 && var18 != null) {
                        Aspect var13 = Aspect.fromId(var1.id);
                        Fusion var14 = WeaponEvolutionSkillPool.fusion(var18, var13);
                        var17 = var14 == null
                            ? Component.translatable("message.ftboss.evolution.native_skill_up")
                            : Component.translatable(
                                "message.ftboss.evolution.fusion_unlock",
                                new Object[]{Component.translatable(var14.primaryTranslationKey()), Component.translatable(var14.secondaryTranslationKey())}
                            );
                    } else {
                        var17 = var18 == null
                            ? Component.translatable("message.ftboss.evolution.native_skill_up")
                            : Component.translatable(var18.translationKey());
                    }
                }

                var0.sendSystemMessage(
                    Component.translatable("message.ftboss.evolution.evolved", new Object[]{var2.getHoverName(), var4, var17, socketCapacityInternal(var2)})
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                );
                if (var8 && var10 != null) {
                    var0.sendSystemMessage(
                        Component.translatable("message.ftboss.gourmet.stat_gained", new Object[]{var10.displayName(var10.gain)})
                            .withStyle(ChatFormatting.GOLD)
                    );
                }

                FTBossMod.LOGGER
                    .info(
                        "[FTBoss][食武进化] {} 的 {} 在 {} 石像进化到 {} 级, skill={}, stat={}, slots={}",
                        new Object[]{
                            var0.getName().getString(),
                            itemId(var2),
                            var1.id,
                            var4,
                            var9 == null ? "native+1" : var9.id,
                            var10 == null ? "none" : var10.id,
                            socketCapacityInternal(var2)
                        }
                    );
            }
        }
    }

    private static void playEvolutionSuccessParticles(ServerPlayer var0, boolean var1) {
        ServerLevel var2 = var0.serverLevel();
        int var3 = var1 ? 16 : 12;
        double var4 = var1 ? 0.74 : 0.62;
        double var6 = var0.getY() + 0.95;

        for (int var8 = 0; var8 < var3; var8++) {
            double var9 = (Math.PI * 2) * (double)var8 / (double)var3;
            double var11 = var0.getX() + Math.cos(var9) * var4;
            double var13 = var0.getZ() + Math.sin(var9) * var4;
            double var15 = var6 + 0.12 * Math.sin(var9 * 2.0);
            var2.sendParticles(ParticleTypes.END_ROD, var11, var15, var13, 1, 0.015, 0.015, 0.015, 0.01);
        }

        var2.sendParticles(ParticleTypes.ENCHANT, var0.getX(), var0.getY() + 1.0, var0.getZ(), var1 ? 24 : 16, 0.32, 0.45, 0.32, 0.06);
    }

    private static void showWeapon(ServerPlayer var0, ItemStack var1, FoodWeaponEvolution.Profile var2) {
        CompoundTag var3 = writableData(var1, var2);
        int var4 = evolutionLevel(var1);
        int var5 = maxLevel(var1);
        int var6 = Math.max(0, var3.getInt("Experience"));
        int var7 = var4 >= var5 ? var6 : threshold(var1, var4);
        var0.sendSystemMessage(
            Component.translatable(
                    "message.ftboss.evolution.weapon_status",
                    new Object[]{var1.getHoverName(), var4, var5, var6, var7, sockets(var1).size(), socketCapacityInternal(var1)}
                )
                .withStyle(ChatFormatting.AQUA)
        );
        if (var4 < var5 && var6 >= var7) {
            var0.sendSystemMessage(Component.translatable("message.ftboss.evolution.can_evolve").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    private static ItemStack heldEvolutionWeapon(ServerPlayer var0) {
        ItemStack var1 = var0.getMainHandItem();
        if (weaponCandidate(var1)) {
            return var1;
        } else {
            ItemStack var2 = var0.getOffhandItem();
            return weaponCandidate(var2) ? var2 : ItemStack.EMPTY;
        }
    }

    public static boolean applyFoodBlessingFromUi(ServerPlayer var0, int var1, int var2) {
        FoodWeaponEvolution.Profile var3 = FoodWeaponEvolution.Profile.byTint(var1);
        ItemStack var4 = heldEvolutionWeapon(var0);
        if (var3 != null && !var4.isEmpty() && var2 >= 0 && var2 <= 40 && (var2 < 36 || var2 >= 40)) {
            if (var4.is(Items.ENCHANTED_BOOK)) {
                var0.displayClientMessage(Component.translatable("message.ftboss.food_blessing.no_books"), true);
                return false;
            } else {
                ItemStack var5 = var0.getInventory().getItem(var2);
                FoodWeaponEvolution.FoodType var6 = foodType(var5);
                if (!var5.isEmpty() && var5 != var4 && !weaponCandidate(var5) && var6 != null) {
                    return applyFoodBlessing(var0, var3, var4, var5, var6);
                } else {
                    var0.displayClientMessage(Component.translatable("message.ftboss.food_blessing.wrong_food", new Object[]{var3.food.displayName()}), true);
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public static boolean insertSocketFromUi(ServerPlayer var0, int var1, int var2) {
        FoodWeaponEvolution.Profile var3 = FoodWeaponEvolution.Profile.byTint(var1);
        ItemStack var4 = heldEvolutionWeapon(var0);
        if (var3 != null && !var4.isEmpty() && var2 >= 0 && var2 <= 40 && (var2 < 36 || var2 >= 40)) {
            ItemStack var5 = var0.getInventory().getItem(var2);
            FoodWeaponEvolution.FoodType var6 = foodType(var5);
            if (!var5.isEmpty() && var5 != var4 && var6 != null) {
                int var7 = socketCount(var4);
                insertSocket(var0, var3, var4, var5, var6);
                return socketCount(var4) > var7;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean submitDailyFromUi(ServerPlayer var0, int var1, int var2) {
        return submitDailyFromUi(var0, var1, var2, false);
    }

    public static boolean submitDailyFromUi(ServerPlayer var0, int var1, int var2, boolean var3) {
        FoodWeaponEvolution.Profile var4 = FoodWeaponEvolution.Profile.byTint(var1);
        if (var4 != null && var2 >= 0 && var2 <= 40 && (var2 < 36 || var2 >= 40)) {
            ItemStack var5 = var0.getInventory().getItem(var2);
            FoodWeaponEvolution.FoodType var6 = foodType(var5);
            if (!var5.isEmpty() && var6 != null) {
                submitDailyFood(var0, var4, var5, var6, var3);
                return true;
            } else {
                var0.displayClientMessage(Component.translatable("message.ftboss.evolution.wrong_food"), true);
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean evolveWeaponFromUi(ServerPlayer var0, int var1) {
        return evolveWeaponFromUi(var0, var1, false);
    }

    public static boolean evolveWeaponFromUi(ServerPlayer var0, int var1, boolean var2) {
        FoodWeaponEvolution.Profile var3 = FoodWeaponEvolution.Profile.byTint(var1);
        ItemStack var4 = heldEvolutionWeapon(var0);
        if (var3 != null && !var4.isEmpty()) {
            handleWeapon(var0, var3, var4, true, var2);
            return true;
        } else {
            var0.displayClientMessage(Component.translatable("message.ftboss.evolution.not_weapon"), true);
            return false;
        }
    }

    public static boolean selectGourmetSkillFromUi(ServerPlayer var0, int var1) {
        ItemStack var2 = heldEvolutionWeapon(var0);
        FoodWeaponEvolution.Profile var3 = boundProfile(var2);
        if (!var2.isEmpty() && var3 != null && isGourmetEvolutionWeapon(var2)) {
            List var4 = gourmetSkills(var2);
            if (var1 >= 0 && var1 < var4.size() && var1 < 8) {
                FoodWeaponEvolution.AcquiredSkill var5 = (FoodWeaponEvolution.AcquiredSkill)var4.get(var1);
                CompoundTag var6 = writableData(var2, var3);
                var6.putInt("ActiveSkillIndex", var1);
                var6.putString("ActiveProfile", var5.profile.id);
                saveData(var2, var6);
                var0.serverLevel()
                    .playSound(null, var0.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.75F, 1.15F + (float)var1 * 0.04F);
                var0.displayClientMessage(
                    Component.translatable("message.ftboss.gourmet.skill_switched", new Object[]{var1 + 1, var5.profile.displayName()}), true
                );
                FTBossMod.LOGGER.info("[FTBoss][万味胚刃] {} 切换当前进化技能到 #{} ({})", new Object[]{var0.getName().getString(), var1 + 1, var5.profile.id});
                return true;
            } else {
                var0.displayClientMessage(Component.translatable("message.ftboss.gourmet.switch_unavailable"), true);
                return false;
            }
        } else {
            var0.displayClientMessage(Component.translatable("message.ftboss.gourmet.switch_wrong_weapon"), true);
            return false;
        }
    }

    public static boolean claimEvolutionFromUi(ServerPlayer var0, int var1) {
        return claimEvolutionFromUi(var0, var1, false);
    }

    public static boolean claimEvolutionFromUi(ServerPlayer var0, int var1, boolean var2) {
        FoodWeaponEvolution.Profile var3 = FoodWeaponEvolution.Profile.byTint(var1);
        ItemStack var4 = heldEvolutionWeapon(var0);
        if (var3 != null && !var4.isEmpty()) {
            handleWeapon(var0, var3, var4, false, var2);
            return true;
        } else {
            var0.displayClientMessage(Component.translatable("message.ftboss.evolution.not_weapon"), true);
            return false;
        }
    }

    public static boolean removeSocketFromUi(ServerPlayer var0, int var1, int var2) {
        FoodWeaponEvolution.Profile var3 = FoodWeaponEvolution.Profile.byTint(var1);
        ItemStack var4 = heldEvolutionWeapon(var0);
        FoodWeaponEvolution.Profile var5 = boundProfile(var4);
        if (var3 == null || var4.isEmpty() || var5 == null) {
            return false;
        } else if (!isGourmetEvolutionWeapon(var4) && var5 != var3) {
            var0.displayClientMessage(Component.translatable("message.ftboss.evolution.wrong_statue", new Object[]{var5.displayName()}), true);
            return false;
        } else {
            CompoundTag var6 = writableData(var4, var5);
            ListTag var7 = var6.getList("Sockets", 10);
            if (var2 >= 0 && var2 < var7.size()) {
                ItemStack var8 = ItemStack.EMPTY;

                for (int var9 = 0; var9 <= 40; var9++) {
                    if (var9 < 36 || var9 >= 40) {
                        ItemStack var10 = var0.getInventory().getItem(var9);
                        if (var10.is(Items.SHEARS)) {
                            var8 = var10;
                            break;
                        }
                    }
                }

                if (var8.isEmpty() && !var0.getAbilities().instabuild) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.evolution.socket_need_shears"), true);
                    return false;
                } else {
                    CompoundTag var15 = var7.getCompound(var2);
                    var7.remove(var2);
                    var6.put("Sockets", var7);
                    saveData(var4, var6);
                    String var16 = var15.getString("Item");
                    ResourceLocation var11 = ResourceLocation.tryParse(var16);
                    Item var12 = var11 == null ? null : (Item)BuiltInRegistries.ITEM.get(var11);
                    ItemStack var13 = var15.contains("Stack", 10)
                        ? ItemStack.parseOptional(var0.registryAccess(), var15.getCompound("Stack"))
                        : (var12 != null && var12 != Items.AIR ? new ItemStack(var12) : ItemStack.EMPTY);
                    if (!var13.isEmpty() && !var0.getInventory().add(var13)) {
                        var0.drop(var13, false);
                    }

                    if (!var0.getAbilities().instabuild) {
                        var8.hurtAndBreak(1, var0, EquipmentSlot.MAINHAND);
                    }

                    var0.serverLevel().playSound(null, var0.blockPosition(), SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 0.7F, 1.25F);
                    FoodWeaponEvolution.SocketTier var14 = FoodWeaponEvolution.SocketTier.byLevel(
                        var15.contains("Tier", 3) ? var15.getInt("Tier") : foodTierById(var16).level
                    );
                    var0.sendSystemMessage(
                        Component.translatable(
                                "message.ftboss.evolution.socket_removed",
                                new Object[]{socketItemName(var16), var14.displayName(), var7.size(), socketCapacityInternal(var4)}
                            )
                            .withStyle(ChatFormatting.GREEN)
                    );
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    private static void insertSocket(ServerPlayer var0, FoodWeaponEvolution.Profile var1, ItemStack var2, ItemStack var3, FoodWeaponEvolution.FoodType var4) {
        FoodWeaponEvolution.Profile var5 = boundProfile(var2);
        if (var5 == null) {
            var0.displayClientMessage(Component.translatable("message.ftboss.evolution.socket_attune_first"), true);
        } else if (!isGourmetEvolutionWeapon(var2) && var5 != var1) {
            var0.displayClientMessage(Component.translatable("message.ftboss.evolution.wrong_statue", new Object[]{var5.displayName()}), true);
        } else {
            List var6 = sockets(var2);
            int var7 = socketCapacityInternal(var2);
            if (var6.size() >= var7) {
                var0.displayClientMessage(Component.translatable("message.ftboss.evolution.socket_full", new Object[]{var7}), true);
            } else {
                CompoundTag var8 = writableData(var2, var5);
                ListTag var9 = var8.getList("Sockets", 10);
                MutableComponent var10 = var3.getHoverName().copy();
                FoodWeaponEvolution.SocketTier var11 = foodTier(var3);
                CompoundTag var12 = new CompoundTag();
                var12.putString("Type", var4.name());
                var12.putString("Item", itemId(var3));
                var12.putInt("Tier", var11.level);
                ItemStack var13 = var3.copy();
                var13.setCount(1);
                var12.put("Stack", var13.save(var0.registryAccess()));
                var9.add(var12);
                var8.put("Sockets", var9);
                saveData(var2, var8);
                if (isGourmetEvolutionWeapon(var2)) {
                    GourmetEvolutionWeapon.animateSocket(var0, var2);
                    var0.serverLevel().sendParticles(ParticleTypes.END_ROD, var0.getX(), var0.getY() + 1.0, var0.getZ(), 12, 0.35, 0.45, 0.35, 0.06);
                }

                if (!var0.getAbilities().instabuild) {
                    var3.shrink(1);
                }

                var0.serverLevel().playSound(null, var0.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.45F, 1.55F);
                var0.sendSystemMessage(
                    Component.translatable("message.ftboss.evolution.socketed", new Object[]{var10, var4.displayName(), var11.displayName(), var9.size(), var7})
                        .withStyle(var4 == var5.food ? ChatFormatting.GOLD : ChatFormatting.GREEN)
                );
                var0.sendSystemMessage(
                    Component.translatable(
                            "message.ftboss.evolution.socket_effect",
                            new Object[]{
                                var4.displayName(),
                                var11.displayName(),
                                formatNumber(var11.multiplier * weaponRole(var2).socketMultiplier(var4)),
                                var4.effectDescription()
                            }
                        )
                        .withStyle(ChatFormatting.GRAY)
                );
                var0.sendSystemMessage(
                    Component.translatable(
                            "message.ftboss.evolution.socket_set_progress",
                            new Object[]{
                                Math.min(3, socketSetCount(var2, var4)),
                                FoodWeaponEvolution.SocketTier.byLevel(socketSetTier(var2, var4)).displayName(),
                                var4.setDescription()
                            }
                        )
                        .withStyle(socketSetCount(var2, var4) >= 3 ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY)
                );
            }
        }
    }

    private static void removeSocket(ServerPlayer var0, FoodWeaponEvolution.Profile var1, ItemStack var2, ItemStack var3, InteractionHand var4) {
        FoodWeaponEvolution.Profile var5 = boundProfile(var2);
        if (var5 == null) {
            var0.displayClientMessage(Component.translatable("message.ftboss.evolution.socket_attune_first"), true);
        } else if (!isGourmetEvolutionWeapon(var2) && var5 != var1) {
            var0.displayClientMessage(Component.translatable("message.ftboss.evolution.wrong_statue", new Object[]{var5.displayName()}), true);
        } else {
            CompoundTag var6 = writableData(var2, var5);
            ListTag var7 = var6.getList("Sockets", 10);
            if (var7.isEmpty()) {
                var0.displayClientMessage(Component.translatable("message.ftboss.evolution.socket_empty"), true);
            } else {
                CompoundTag var8 = var7.getCompound(var7.size() - 1);
                var7.remove(var7.size() - 1);
                var6.put("Sockets", var7);
                saveData(var2, var6);
                String var9 = var8.getString("Item");
                ResourceLocation var10 = ResourceLocation.tryParse(var9);
                Item var11 = var10 == null ? null : (Item)BuiltInRegistries.ITEM.get(var10);
                ItemStack var12 = var8.contains("Stack", 10)
                    ? ItemStack.parseOptional(var0.registryAccess(), var8.getCompound("Stack"))
                    : (var11 != null && var11 != Items.AIR ? new ItemStack(var11) : ItemStack.EMPTY);
                if (!var12.isEmpty() && !var0.getInventory().add(var12)) {
                    var0.drop(var12, false);
                }

                if (!var0.getAbilities().instabuild) {
                    var3.hurtAndBreak(1, var0, var4 == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }

                var0.serverLevel().playSound(null, var0.blockPosition(), SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 0.7F, 1.25F);
                FoodWeaponEvolution.SocketTier var13 = FoodWeaponEvolution.SocketTier.byLevel(
                    var8.contains("Tier", 3) ? var8.getInt("Tier") : foodTierById(var9).level
                );
                var0.sendSystemMessage(
                    Component.translatable(
                            "message.ftboss.evolution.socket_removed",
                            new Object[]{socketItemName(var9), var13.displayName(), var7.size(), socketCapacityInternal(var2)}
                        )
                        .withStyle(ChatFormatting.GREEN)
                );
            }
        }
    }

    @SubscribeEvent
    public static void attributes(ItemAttributeModifierEvent var0) {
        ItemStack var1 = var0.getItemStack();
        if (boundProfile(var1) != null) {
            addFoodBlessingAttributes(var0, var1);
            boolean var2 = isGourmetEvolutionWeapon(var1);
            int var3 = evolutionLevel(var1);
            FoodWeaponEvolution.WeaponRole var4 = weaponRole(var1);
            double var5 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.MEAT);
            double var7 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.WINE);
            double var9 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.ENERGY);
            double var11 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.TEA);
            FoodWeaponEvolution.Profile var13 = activeProfile(var1);
            double var14 = var13 == null ? 0.0 : socketEffectPower(var1, var13.food);
            double var16 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.CHEESE);
            double var18 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.VEGETABLE);
            double var20 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.APPLE);
            double var22 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.FISH);
            double var24 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.SWEET) + socketEffectPower(var1, FoodWeaponEvolution.FoodType.FOOD);
            double var26 = evolutionDamageBonus(var4, var3, var2)
                + (var2 ? gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.ATTACK_DAMAGE) : 0.0)
                + var5
                + var7 * 0.5
                + var9 * 0.5
                + var14 * 0.5;
            double var28 = evolutionSpeedBonus(var4, var3, var2)
                + (var2 ? gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.ATTACK_SPEED) : 0.0)
                + var11 * 0.05
                + var9 * 0.02;
            double var30 = evolutionArmorBonus(var4, var3, var2) + var16 + (var2 ? gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.ARMOR) : 0.0);
            double var32 = evolutionHealthBonus(var4, var3, var2)
                + var20 * 2.0
                + (var2 ? gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.MAX_HEALTH) : 0.0);
            double var34 = evolutionMovementBonus(var4, var3, var2)
                + var22 * 0.03
                + (var2 ? gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.MOVEMENT_SPEED) : 0.0);
            double var36 = evolutionLuckBonus(var4, var3, var2) + var24 + (var2 ? gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.LUCK) : 0.0);
            if (var26 > 0.0) {
                var0.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(EVOLUTION_DAMAGE, var26, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            }

            if (var28 > 0.0) {
                var0.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(EVOLUTION_SPEED, var28, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            }

            if (var30 > 0.0) {
                var0.addModifier(Attributes.ARMOR, new AttributeModifier(SOCKET_ARMOR, var30, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            }

            if (var18 > 0.0) {
                var0.addModifier(
                    Attributes.ARMOR_TOUGHNESS, new AttributeModifier(SOCKET_TOUGHNESS, var18 * 0.5, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
                );
            }

            if (var32 > 0.0) {
                var0.addModifier(Attributes.MAX_HEALTH, new AttributeModifier(SOCKET_HEALTH, var32, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            }

            if (var34 > 0.0) {
                var0.addModifier(
                    Attributes.MOVEMENT_SPEED, new AttributeModifier(SOCKET_MOVEMENT, var34, Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND
                );
            }

            if (var36 > 0.0) {
                var0.addModifier(Attributes.LUCK, new AttributeModifier(SOCKET_LUCK, var36, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            }
        }
    }

    @SubscribeEvent
    public static void toolBreakSpeed(BreakSpeed var0) {
        ItemStack var1 = var0.getEntity().getMainHandItem();
        if (boundProfile(var1) != null && weaponRole(var1) == FoodWeaponEvolution.WeaponRole.TOOL && !(var0.getNewSpeed() <= 0.0F)) {
            boolean var2 = isGourmetEvolutionWeapon(var1);
            double var3 = (double)evolutionLevel(var1) * (var2 ? 0.05 : 0.15);
            double var5 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.TEA) * 0.1
                + socketEffectPower(var1, FoodWeaponEvolution.FoodType.ENERGY) * 0.12
                + socketEffectPower(var1, FoodWeaponEvolution.FoodType.VEGETABLE) * 0.06;
            double var7 = 1.0 + var3 + Math.min(2.0, var5);
            var0.setNewSpeed((float)((double)var0.getNewSpeed() * var7));
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGH
    )
    public static void weaponHit(LivingIncomingDamageEvent var0) {
        if (FoodWeaponFinalDamage.nativeOriginalInteractionActive(var0.getSource(), var0.getEntity())) {
            nativeIncomingRangedScalar202(var0);
            return;
        }
        if (!Boolean.TRUE.equals(EXTRA_DAMAGE.get())
            && !(var0.getAmount() <= 0.0F)
            && var0.getSource().getEntity() instanceof ServerPlayer var1
            && var0.getEntity() != var1) {
            ItemStack var18 = var1.getMainHandItem();
            FoodWeaponEvolution.Profile var3 = activeProfile(var18);
            if (var3 != null) {
                LivingEntity var4 = var0.getEntity();
                CompoundTag var5 = writableData(var18, var3);
                long var6 = var1.level().getGameTime();
                FoodWeaponEvolution.WeaponRole var8 = weaponRole(var18);
                if (var8 == FoodWeaponEvolution.WeaponRole.RANGED && var0.getSource().getDirectEntity() instanceof Projectile) {
                    double var9 = (double)evolutionLevel(var18) * (isGourmetEvolutionWeapon(var18) ? 0.05 : 0.12);
                    double var11 = socketEffectPower(var18, FoodWeaponEvolution.FoodType.WINE)
                        + socketEffectPower(var18, FoodWeaponEvolution.FoodType.FISH)
                        + socketEffectPower(var18, FoodWeaponEvolution.FoodType.ENERGY);
                    double var13 = Math.min(0.6, var11 * 0.04);
                    var0.setAmount((float)((double)var0.getAmount() * (1.0 + var9 + var13)));
                }

                applyFoodBlessingHits(var0, var1, var4, var5, var6);
                double var19 = socketEffectPower(var18, FoodWeaponEvolution.FoodType.WINE);
                if (var19 > 0.0 && (double)var1.getRandom().nextFloat() < Math.min(0.75, var19 * 0.07)) {
                    var0.setAmount(var0.getAmount() * 1.5F);
                }

                double var20 = socketEffectPower(var18, FoodWeaponEvolution.FoodType.VEGETABLE);
                int var21 = socketCount(var18, FoodWeaponEvolution.FoodType.VEGETABLE);
                if (var20 > 0.0 && (double)var1.getRandom().nextFloat() < Math.min(0.85, var20 * 0.15)) {
                    var4.addEffect(new MobEffectInstance(MobEffects.POISON, 40 + (int)Math.round(var20 * 20.0), Math.min(1, var21 - 1)), var1);
                }

                double var14 = socketEffectPower(var18, FoodWeaponEvolution.FoodType.MEAT);
                if (var14 > 0.0 && var6 - var5.getLong("LastSocketProc") >= 60L) {
                    var1.heal((float)(var14 * 0.35));
                    var5.putLong("LastSocketProc", var6);
                }

                if (socketSetCount(var18, FoodWeaponEvolution.FoodType.SWEET) >= 3 && var6 - var5.getLong("LastSweetGuard") >= 100L) {
                    var1.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 80, socketSetTier(var18, FoodWeaponEvolution.FoodType.SWEET) - 1), var1);
                    var5.putLong("LastSweetGuard", var6);
                }

                if (socketCount(var18, FoodWeaponEvolution.FoodType.ENERGY) >= 3 && var6 - var5.getLong("LastEnergyArc") >= 40L) {
                    chainArc(var1, var4, 4.0F + 2.0F * (float)(socketSetTier(var18, FoodWeaponEvolution.FoodType.ENERGY) - 1));
                    var5.putLong("LastEnergyArc", var6);
                }

                if (var3.food == FoodWeaponEvolution.FoodType.CHEESE && socketCount(var18, FoodWeaponEvolution.FoodType.CHEESE) >= 3) {
                    var4.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, socketSetTier(var18, FoodWeaponEvolution.FoodType.CHEESE) - 1), var1);
                }

                if (var3.food == FoodWeaponEvolution.FoodType.WINE && socketCount(var18, FoodWeaponEvolution.FoodType.WINE) >= 3) {
                    var4.addEffect(
                        new MobEffectInstance(MobEffects.CONFUSION, 80 + 40 * (socketSetTier(var18, FoodWeaponEvolution.FoodType.WINE) - 1), 0), var1
                    );
                }

                if (isGourmetEvolutionWeapon(var18) && evolutionLevel(var18) > 0) {
                    for (FoodWeaponEvolution.AcquiredSkill var17 : gourmetSkills(var18)) {
                        applyEvolutionSkill(var0, var1, var4, var5, var17.skill, var6, "LastSkillProc." + var17.skill.id);
                    }
                } else if (nativeProfile(var18) == null && evolutionLevel(var18) > 0) {
                    FoodWeaponEvolution.EvolutionSkill var16 = FoodWeaponEvolution.EvolutionSkill.byId(var5.getString("EvolutionSkill"));
                    if (var16 != null) {
                        applyEvolutionSkill(var0, var1, var4, var5, var16, var6, "LastSkillProc");
                    }
                }

                saveData(var18, var5);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void weaponHit(net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation event) {
        if (FoodWeaponFinalDamage.nativeOriginalInteractionActive(event.getSource(), event.getEntity())) {
            FoodWeaponFinalDamage.captureEvolution(event);
        }
    }

    private static void nativeIncomingRangedScalar202(LivingIncomingDamageEvent event) {
        if (!Boolean.TRUE.equals(EXTRA_DAMAGE.get())
            && !(event.getAmount() <= 0.0F)
            && event.getSource().getEntity() instanceof ServerPlayer player
            && event.getEntity() != player) {
            ItemStack weapon = player.getMainHandItem();
            FoodWeaponEvolution.Profile profile = activeProfile(weapon);
            if (profile != null && weaponRole(weapon) == FoodWeaponEvolution.WeaponRole.RANGED
                && event.getSource().getDirectEntity() instanceof Projectile) {
                double evolution = (double)evolutionLevel(weapon) * (isGourmetEvolutionWeapon(weapon) ? 0.05 : 0.12);
                double sockets = socketEffectPower(weapon, FoodWeaponEvolution.FoodType.WINE)
                    + socketEffectPower(weapon, FoodWeaponEvolution.FoodType.FISH)
                    + socketEffectPower(weapon, FoodWeaponEvolution.FoodType.ENERGY);
                double bonus = Math.min(0.6, sockets * 0.04);
                event.setAmount((float)((double)event.getAmount() * (1.0 + evolution + bonus)));
            }
        }
    }

    private static void applyEvolutionSkill(
        LivingIncomingDamageEvent var0, ServerPlayer var1, LivingEntity var2, CompoundTag var3, FoodWeaponEvolution.EvolutionSkill var4, long var5, String var7
    ) {
        int var8 = var4.effect == FoodWeaponEvolution.SkillEffect.LIFESTEAL ? 60 : 20;
        boolean var9 = var5 - var3.getLong(var7) >= (long)var8;
        boolean var10 = var9 && var1.getRandom().nextFloat() < 0.28F;
        switch (var4.effect) {
            case DAMAGE:
                var0.setAmount(var0.getAmount() * 1.18F);
                break;
            case LIFESTEAL:
                if (var9) {
                    var1.heal(1.0F);
                    var10 = true;
                }
                break;
            case SLOW:
                if (var10) {
                    var2.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1), var1);
                }
                break;
            case POISON:
                if (var10) {
                    var2.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), var1);
                }
                break;
            case FIRE:
                if (var10) {
                    var2.igniteForSeconds(4.0F);
                }
                break;
            case CRIT:
                if (var10) {
                    var0.setAmount(var0.getAmount() * 1.6F);
                }
                break;
            case GUARD:
                if (var10) {
                    var1.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0), var1);
                }
                break;
            case HASTE:
                if (var10) {
                    var1.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0), var1);
                    var1.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 100, 0), var1);
                }
                break;
            case WEAKEN:
                if (var10) {
                    var2.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0), var1);
                }
                break;
            case KNOCKBACK:
                if (var10) {
                    knockAway(var2, var1.position(), 0.85);
                }
                break;
            case CHAIN:
                if (var10) {
                    chainArc(var1, var2, 5.0F);
                }
                break;
            case REGEN:
                if (var10) {
                    var1.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0), var1);
                }
        }

        if (var10) {
            var3.putLong(var7, var5);
        }
    }

    private static boolean canTarget(ServerPlayer var0, LivingEntity var1) {
        if (var1 == var0 || !var1.isAlive() || var1.isRemoved() || var0.isAlliedTo(var1) || var1.isAlliedTo(var0) || var0.isPassengerOfSameVehicle(var1)) {
            return false;
        } else {
            return !(var1 instanceof Player var2)
                ? true
                : !var2.isCreative() && !var2.isSpectator() && var0.serverLevel().getServer().isPvpAllowed() && var0.canHarmPlayer(var2);
        }
    }

    private static void knockAway(LivingEntity var0, Vec3 var1, double var2) {
        Vec3 var4 = var0.position().subtract(var1).multiply(1.0, 0.0, 1.0).normalize();
        double var5 = Math.max(0.0, Math.min(1.0, var0.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
        double var7 = var2 * (1.0 - var5);
        if (!(var4.lengthSqr() < 1.0E-6) && !(var7 <= 0.0)) {
            var0.push(var4.x * var7, 0.18 * (1.0 - var5), var4.z * var7);
            var0.hurtMarked = true;
        }
    }

    private static void chainArc(ServerPlayer var0, LivingEntity var1, float var2) {
        if (var1.level() instanceof ServerLevel var3) {
            LivingEntity var8 = var3.getEntitiesOfClass(
                    LivingEntity.class, var1.getBoundingBox().inflate(4.0), var2x -> var2x != var1 && canTarget(var0, var2x)
                )
                .stream()
                .min(Comparator.comparingDouble(var1::distanceToSqr))
                .orElse(null);
            if (var8 != null) {
                EXTRA_DAMAGE.set(true);

                try {
                    var8.invulnerableTime = 0;
                    var8.hurt(var3.damageSources().magic(), var2);
                } finally {
                    EXTRA_DAMAGE.set(false);
                }

                var3.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK, var8.getX(), var8.getY() + (double)var8.getBbHeight() * 0.5, var8.getZ(), 12, 0.22, 0.35, 0.22, 0.12
                );
            }
        }
    }

    private static void overloadBurst(ServerPlayer var0, LivingEntity var1, float var2) {
        if (var1.level() instanceof ServerLevel var3) {
            List<LivingEntity> var10 = var3.getEntitiesOfClass(LivingEntity.class, var1.getBoundingBox().inflate(3.0), var2x -> var2x != var1 && canTarget(var0, var2x));
            EXTRA_DAMAGE.set(true);

            try {
                for (LivingEntity var6 : var10) {
                    var6.invulnerableTime = 0;
                    var6.hurt(var3.damageSources().magic(), var2);
                }
            } finally {
                EXTRA_DAMAGE.set(false);
            }

            var3.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                var1.getX(),
                var1.getY() + (double)var1.getBbHeight() * 0.5,
                var1.getZ(),
                24 + var10.size() * 4,
                1.1,
                0.7,
                1.1,
                0.16
            );
        }
    }

    @SubscribeEvent
    public static void heldEffects(Post var0) {
        if (var0.getEntity() instanceof ServerPlayer var1 && var1.tickCount % 40 == 0) {
            ItemStack var5 = var1.getMainHandItem();
            if (boundProfile(var5) == null) {
                return;
            }

            if (socketCount(var5, FoodWeaponEvolution.FoodType.FISH) >= 3 && var1.isInWaterRainOrBubble()) {
                int var3 = socketSetTier(var5, FoodWeaponEvolution.FoodType.FISH) - 1;
                var1.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 80, var3, false, false, true));
                var1.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, var3, false, false, true));
            }

            if (socketCount(var5, FoodWeaponEvolution.FoodType.TEA) >= 3) {
                var1.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, socketSetTier(var5, FoodWeaponEvolution.FoodType.TEA) - 1, false, false, true));
            }

            FoodWeaponEvolution.Profile var6 = activeProfile(var5);
            if (var6 != null && socketCount(var5, var6.food) >= 3) {
                int var4 = socketSetTier(var5, var6.food) - 1;
                switch (var6.food) {
                    case MEAT:
                        var1.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, var4, false, false, true));
                        break;
                    case APPLE:
                        var1.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, var4, false, false, true));
                        break;
                    case VEGETABLE:
                        var1.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, var4, false, false, true));
                }
            }

            return;
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void foodBlessingKillFeast(LivingDeathEvent var0) {
        if (!var0.isCanceled()) {
            ServerPlayer var1 = resolvePlayer(var0.getSource().getEntity());
            if (var1 != null && !LemonSummonRewards.suppressed(var0.getEntity())) {
                ItemStack var2 = var1.getMainHandItem();
                if (boundProfile(var2) != null) {
                    int var3 = foodBlessingLevel(var2, FoodWeaponEvolution.FoodBlessingKind.KILL_FEAST);
                    if (var3 > 0) {
                        var1.heal(0.75F + (float)var3 * 0.5F);
                        var1.getFoodData().eat(1 + var3, 0.2F + (float)var3 * 0.1F);
                    }
                }
            }
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void socketKillEffects(LivingDeathEvent var0) {
        if (!var0.isCanceled()) {
            ServerPlayer var1 = resolvePlayer(var0.getSource().getEntity());
            if (var1 != null && !LemonSummonRewards.suppressed(var0.getEntity())) {
                ItemStack var2 = var1.getMainHandItem();
                if (boundProfile(var2) != null) {
                    double var3 = socketEffectPower(var2, FoodWeaponEvolution.FoodType.APPLE);
                    int var5 = socketCount(var2, FoodWeaponEvolution.FoodType.MEAT);
                    if (var3 > 0.0 || var5 >= 3) {
                        var1.heal((float)(var3 * 2.0 + (var5 >= 3 ? 2.0 * (double)socketSetTier(var2, FoodWeaponEvolution.FoodType.MEAT) : 0.0)));
                    }
                }
            }
        }
    }

    private static ServerPlayer resolvePlayer(Entity var0) {
        if (var0 instanceof ServerPlayer) {
            return (ServerPlayer)var0;
        } else {
            if (var0 instanceof Projectile var1) {
                Entity var3 = var1.getOwner();
                if (var3 instanceof ServerPlayer) {
                    return (ServerPlayer)var3;
                }
            }

            return null;
        }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent var0) {
        ItemStack var1 = var0.getItemStack();
        boolean var2 = isGourmetEvolutionWeapon(var1);
        FoodWeaponEvolution.Profile var3 = boundProfile(var1);
        FoodWeaponEvolution.FoodType var4 = var3 == null && !var2 ? foodType(var1) : null;
        if (var3 != null || var2 || var4 != null) {
            if (FoodAppraisalMonocleItem.isEquipped(var0.getEntity())) {
                List var5 = var0.getToolTip();
                var5.add(Component.empty());
                var5.add(Component.translatable("tooltip.ftboss.food_appraisal_monocle.readout").withStyle(ChatFormatting.DARK_AQUA));
                if (var3 == null) {
                    if (var2) {
                        var5.add(Component.translatable("tooltip.ftboss.gourmet.unattuned", new Object[]{8, 30}).withStyle(ChatFormatting.AQUA));
                        var5.add(Component.translatable("tooltip.ftboss.evolution.ui_hint").withStyle(ChatFormatting.DARK_GRAY));
                    } else {
                        addSocketIngredientTooltip(var5, var1, var4);
                    }
                } else {
                    int var6 = evolutionLevel(var1);
                    int var7 = maxLevel(var1);
                    int var8 = evolutionExperience(var1);
                    int var9 = var6 >= var7 ? var8 : threshold(var1, var6);
                    var5.add(
                        Component.translatable("tooltip.ftboss.evolution.progress", new Object[]{var3.displayName(), var6, var7, var8, var9})
                            .withStyle(ChatFormatting.AQUA)
                    );
                    var5.add(
                        Component.translatable("tooltip.ftboss.evolution.sockets", new Object[]{socketCount(var1), socketCapacityInternal(var1)})
                            .withStyle(ChatFormatting.GREEN)
                    );
                    var5.add(Component.translatable("tooltip.ftboss.evolution.ui_hint").withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }

    private static String formatNumber(double var0) {
        return Math.abs(var0 - Math.rint(var0)) < 1.0E-4
            ? Integer.toString((int)Math.rint(var0))
            : String.format(Locale.ROOT, "%.2f", var0).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static void addWeaponRoleTooltip(List<Component> var0, ItemStack var1) {
        FoodWeaponEvolution.WeaponRole var2 = weaponRole(var1);
        var0.add(Component.translatable("tooltip.ftboss.evolution.role", new Object[]{var2.displayName()}).withStyle(ChatFormatting.GOLD));
        var0.add(var2.bonusDescription().copy().withStyle(ChatFormatting.GRAY));
    }

    private static void addSocketIngredientTooltip(List<Component> var0, ItemStack var1, FoodWeaponEvolution.FoodType var2) {
        FoodWeaponEvolution.SocketTier var3 = foodTier(var1);
        var0.add(Component.translatable("tooltip.ftboss.socket.material", new Object[]{var2.displayName()}).withStyle(ChatFormatting.GOLD));
        var0.add(
            Component.translatable("tooltip.ftboss.socket.tier", new Object[]{var3.displayName(), formatNumber(var3.multiplier)})
                .withStyle(
                    var3 == FoodWeaponEvolution.SocketTier.GOURMET
                        ? ChatFormatting.LIGHT_PURPLE
                        : (var3 == FoodWeaponEvolution.SocketTier.FINE ? ChatFormatting.AQUA : ChatFormatting.GRAY)
                )
        );
        var0.add(var2.effectDescription().copy().withStyle(ChatFormatting.GRAY));
        var0.add(Component.translatable("tooltip.ftboss.socket.set_preview", new Object[]{var2.setDescription()}).withStyle(ChatFormatting.DARK_GREEN));
    }

    private static void addCurrentSocketEffects(List<Component> var0, ItemStack var1, FoodWeaponEvolution.Profile var2) {
        var0.add(Component.translatable("tooltip.ftboss.socket.current").withStyle(ChatFormatting.GOLD));
        boolean var3 = false;

        for (FoodWeaponEvolution.FoodType var7 : FoodWeaponEvolution.FoodType.values()) {
            int var8 = socketCount(var1, var7);
            if (var8 > 0) {
                double var9 = socketEffectPower(var1, var7);
                var0.add(
                    Component.translatable(
                            "tooltip.ftboss.socket.effect_line", new Object[]{var7.displayName(), var8, formatNumber(var9), var7.effectDescription()}
                        )
                        .withStyle(ChatFormatting.GRAY)
                );
                if (var7 == var2.food) {
                    var0.add(
                        Component.translatable("tooltip.ftboss.socket.matching", new Object[]{var2.displayName(), formatNumber(var9 * 0.5), formatNumber(var9)})
                            .withStyle(ChatFormatting.YELLOW)
                    );
                }

                if (var7 != FoodWeaponEvolution.FoodType.SWEET && var7 != FoodWeaponEvolution.FoodType.FOOD || !var3) {
                    if (var7 == FoodWeaponEvolution.FoodType.SWEET || var7 == FoodWeaponEvolution.FoodType.FOOD) {
                        var3 = true;
                    }

                    int var11 = Math.min(3, socketSetCount(var1, var7));
                    FoodWeaponEvolution.SocketTier var12 = FoodWeaponEvolution.SocketTier.byLevel(socketSetTier(var1, var7));
                    var0.add(
                        Component.translatable("tooltip.ftboss.socket.set_progress", new Object[]{var11, var12.displayName(), var7.setDescription()})
                            .withStyle(var11 >= 3 ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY)
                    );
                }
            }
        }

        var0.add(Component.translatable("tooltip.ftboss.socket.remove_hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Component socketItemName(String var0) {
        try {
            ResourceLocation var1 = ResourceLocation.parse(var0);
            Item var2 = (Item)BuiltInRegistries.ITEM.get(var1);
            return (Component)(var2 == null ? Component.literal(var0) : var2.getDescription());
        } catch (RuntimeException var3) {
            return Component.literal(var0);
        }
    }

    private static boolean hasWashableBlessing(ItemStack var0) {
        CompoundTag var1 = data(var0);
        if (var1 == null) {
            return false;
        } else if (var1.getInt("Experience") > 0 || var1.getInt("Level") > 0) {
            return true;
        } else if (var1.contains("EvolutionSkill", 8)) {
            return true;
        } else if (var1.contains("EvolutionSkills", 9) && !var1.getList("EvolutionSkills", 10).isEmpty()) {
            return true;
        } else {
            return var1.contains("FoodBlessings", 9) && !var1.getList("FoodBlessings", 10).isEmpty()
                ? true
                : var1.contains("EvolutionStats", 10) && !var1.getCompound("EvolutionStats").getAllKeys().isEmpty();
        }
    }

    private static int washBlessings(ItemStack var0) {
        CompoundTag var1 = data(var0);
        if (var1 == null) {
            return 0;
        } else {
            int var2 = var1.contains("EvolutionSkill", 8) ? 1 : 0;
            if (var1.contains("EvolutionSkills", 9)) {
                var2 += var1.getList("EvolutionSkills", 10).size();
            }

            if (var1.contains("FoodBlessings", 9)) {
                var2 += var1.getList("FoodBlessings", 10).size();
            }

            if (var1.contains("EvolutionStats", 10)) {
                var2 += var1.getCompound("EvolutionStats").getAllKeys().size();
            }

            if (var1.getInt("Experience") > 0) {
                var2++;
            }

            if (var1.getInt("Level") > 0) {
                var2++;
            }

            var1.putInt("Experience", 0);
            var1.putInt("Level", 0);
            var1.remove("SecondProfile");
            var1.remove("ActiveProfile");
            var1.remove("ActiveSkillIndex");
            var1.remove("EvolutionSkill");
            var1.remove("EvolutionSkills");
            var1.remove("EvolutionStats");
            var1.remove("FoodBlessings");

            for (String var4 : new ArrayList<String>(var1.getAllKeys())) {
                if (var4.startsWith("LastSkillProc") || var4.startsWith("LastFoodBlessing.")) {
                    var1.remove(var4);
                }
            }

            var1.putBoolean("BlessingWashed", true);
            saveData(var0, var1);
            return var2;
        }
    }

    public static int setEvolutionExperienceForTesting(ServerPlayer var0, int var1, boolean var2) {
        if (var0 != null && var0.getAbilities().instabuild) {
            ItemStack var3 = heldEvolutionWeapon(var0);
            FoodWeaponEvolution.Profile var4 = boundProfile(var3);
            if (!var3.isEmpty() && var4 != null) {
                CompoundTag var5 = writableData(var3, var4);
                int var6 = Math.max(0, var5.getInt("Experience"));
                int var7 = threshold(var3, maxLevel(var3) - 1);
                long var8 = var2 ? (long)var6 + (long)var1 : (long)var1;
                int var10 = (int)Math.max(0L, Math.min((long)var7, var8));
                var5.putInt("Experience", var10);
                saveData(var3, var5);
                var0.getInventory().setChanged();
                var0.sendSystemMessage(
                    Component.translatable("command.ftboss.evolution.changed", new Object[]{var3.getHoverName(), var6, var10, var7})
                        .withStyle(ChatFormatting.AQUA)
                );
                return 1;
            } else {
                var0.sendSystemMessage(Component.translatable("command.ftboss.evolution.no_bound_weapon").withStyle(ChatFormatting.RED));
                return 0;
            }
        } else {
            if (var0 != null) {
                var0.sendSystemMessage(Component.translatable("command.ftboss.evolution.creative_only").withStyle(ChatFormatting.RED));
            }

            return 0;
        }
    }

    @SubscribeEvent
    public static void grindstoneBlessing(OnPlaceItem var0) {
        ItemStack var1 = var0.getTopItem();
        ItemStack var2 = var0.getBottomItem();
        ItemStack var3 = ItemStack.EMPTY;
        if (var2.isEmpty() && hasWashableBlessing(var1)) {
            var3 = var1;
        } else if (var1.isEmpty() && hasWashableBlessing(var2)) {
            var3 = var2;
        }

        if (!var3.isEmpty()) {
            ItemStack var4 = var3.copy();
            var4.setCount(1);
            int var5 = washBlessings(var4);
            int[] var6 = new int[]{0};
            EnchantmentHelper.updateEnchantments(var4, var1x -> {
                for (Holder var3x : List.copyOf(var1x.keySet())) {
                    if (!var3x.is(EnchantmentTags.CURSE)) {
                        var6[0] += var1x.getLevel(var3x);
                        var1x.set(var3x, 0);
                    }
                }
            });
            var4.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
            var4.set(DataComponents.REPAIR_COST, 0);
            var0.setOutput(var4);
            var0.setXp(Math.max(1, var5 * 3 + var6[0]));
        }
    }

    @SubscribeEvent
    public static void gourmetBladePickup(Pre var0) {
        GourmetEvolutionWeapon.playPickupEffect(var0);
    }

    @SubscribeEvent
    public static void clonePlayer(Clone var0) {
        if (var0.getOriginal().getPersistentData().contains("FTBossFoodEvolutionDailies", 10)) {
            var0.getEntity()
                .getPersistentData()
                .put("FTBossFoodEvolutionDailies", var0.getOriginal().getPersistentData().getCompound("FTBossFoodEvolutionDailies").copy());
        }
    }

    private static FoodWeaponEvolution.SocketTier foodTier(ItemStack var0) {
        return FoodWeaponEvolution.SocketTier.byLevel(FoodSocketQuality.tier(var0, baseFoodTier(var0).level));
    }

    public static String profileId(int var0) {
        FoodWeaponEvolution.Profile var1 = FoodWeaponEvolution.Profile.byTint(var0);
        return var1 == null ? "" : var1.id;
    }

    static FoodWeaponEvolution.EvolutionSkill drawStageSkill(FoodWeaponEvolution.Profile var0, Set<FoodWeaponEvolution.EvolutionSkill> var1, RandomSource var2) {
        ArrayList var3 = new ArrayList();

        for (FoodWeaponEvolution.EvolutionSkill var7 : var0.pool) {
            if (!var1.contains(var7)) {
                var3.add(var7);
            }
        }

        if (var3.isEmpty()) {
            for (FoodWeaponEvolution.EvolutionSkill var11 : FoodWeaponEvolution.EvolutionSkill.values()) {
                if (!var1.contains(var11)) {
                    var3.add(var11);
                }
            }
        }

        return var3.isEmpty() ? null : (FoodWeaponEvolution.EvolutionSkill)var3.get(var2.nextInt(var3.size()));
    }

    public static int foodQualityTier(ItemStack var0) {
        return foodTier(var0).level;
    }

    public static boolean matchesStatueFood(ItemStack var0, int var1) {
        FoodWeaponEvolution.Profile var2 = FoodWeaponEvolution.Profile.byTint(var1);
        return var2 != null && foodType(var0) == var2.food;
    }

    static String planHitTimerProfile(FoodWeaponEvolution.Profile var0) {
        return var0 == null ? null : var0.id;
    }

    public static String repairProfileId(ItemStack var0) {
        FoodWeaponEvolution.Profile var1 = boundProfile(var0);
        return var1 == null ? "" : var1.id;
    }

    static String skillId(FoodWeaponEvolution.EvolutionSkill var0) {
        return var0.id;
    }

    public static ItemStack heldEvolutionWeaponForProgression(ServerPlayer var0) {
        ItemStack var1 = var0.getMainHandItem();
        if (weaponCandidate(var1)) {
            return var1;
        } else {
            ItemStack var2 = var0.getOffhandItem();
            return weaponCandidate(var2) ? var2 : ItemStack.EMPTY;
        }
    }

    static Component skillDescription(FoodWeaponEvolution.EvolutionSkill var0) {
        return Component.translatable(var0.effect.key());
    }

    private static record AcquiredSkill(FoodWeaponEvolution.Profile profile, FoodWeaponEvolution.EvolutionSkill skill) {
    }

    private static enum DailyRewardType {
        EVOLUTION("evolution"),
        EXPERIENCE("experience"),
        COINS("coins");

        private final String id;

        private DailyRewardType(String nullxx) {
            this.id = nullxx;
        }

        static FoodWeaponEvolution.DailyRewardType byId(String var0) {
            for (FoodWeaponEvolution.DailyRewardType var4 : values()) {
                if (var4.id.equals(var0)) {
                    return var4;
                }
            }

            return null;
        }

        Component displayName(int var1) {
            return Component.translatable("reward.ftboss.statue." + this.id, new Object[]{var1});
        }
    }

    public static enum EvolutionSkill {
        FROST_BITE("frost_bite", FoodWeaponEvolution.SkillEffect.SLOW),
        SUGAR_RUSH("sugar_rush", FoodWeaponEvolution.SkillEffect.HASTE),
        CANDY_GUARD("candy_guard", FoodWeaponEvolution.SkillEffect.GUARD),
        BLOOD_FEAST("blood_feast", FoodWeaponEvolution.SkillEffect.LIFESTEAL),
        CLEAVER_FORCE("cleaver_force", FoodWeaponEvolution.SkillEffect.DAMAGE),
        HEAVY_IMPACT("heavy_impact", FoodWeaponEvolution.SkillEffect.KNOCKBACK),
        THORN_VENOM("thorn_venom", FoodWeaponEvolution.SkillEffect.POISON),
        TEA_RECOVERY("tea_recovery", FoodWeaponEvolution.SkillEffect.REGEN),
        ROOTED_GRIP("rooted_grip", FoodWeaponEvolution.SkillEffect.SLOW),
        VINTAGE_FURY("vintage_fury", FoodWeaponEvolution.SkillEffect.CRIT),
        TIPSY_GUARD("tipsy_guard", FoodWeaponEvolution.SkillEffect.GUARD),
        CORK_BURST("cork_burst", FoodWeaponEvolution.SkillEffect.KNOCKBACK),
        CHEESE_FRENZY("cheese_frenzy", FoodWeaponEvolution.SkillEffect.HASTE),
        PLAGUE_BITE("plague_bite", FoodWeaponEvolution.SkillEffect.POISON),
        SCAVENGER_FEAST("scavenger_feast", FoodWeaponEvolution.SkillEffect.LIFESTEAL),
        OVERLOAD_ARC("overload_arc", FoodWeaponEvolution.SkillEffect.CHAIN),
        HIGH_VOLTAGE("high_voltage", FoodWeaponEvolution.SkillEffect.CRIT),
        MAGNETIC_LOCK("magnetic_lock", FoodWeaponEvolution.SkillEffect.SLOW),
        PRISMATIC_TOXIN("prismatic_toxin", FoodWeaponEvolution.SkillEffect.POISON),
        RECYCLE_GUARD("recycle_guard", FoodWeaponEvolution.SkillEffect.GUARD),
        FERTILE_REGROWTH("fertile_regrowth", FoodWeaponEvolution.SkillEffect.REGEN),
        HONEYCRISP_REBIRTH("honeycrisp_rebirth", FoodWeaponEvolution.SkillEffect.REGEN),
        GOLDEN_PEEL("golden_peel", FoodWeaponEvolution.SkillEffect.GUARD),
        ORCHARD_STRIKE("orchard_strike", FoodWeaponEvolution.SkillEffect.DAMAGE),
        UNDERTOW("undertow", FoodWeaponEvolution.SkillEffect.KNOCKBACK),
        BRINE_BITE("brine_bite", FoodWeaponEvolution.SkillEffect.SLOW),
        TIDAL_RECOVERY("tidal_recovery", FoodWeaponEvolution.SkillEffect.REGEN),
        SHEPHERD_MIGHT("shepherd_might", FoodWeaponEvolution.SkillEffect.DAMAGE),
        HUNTING_HOWL("hunting_howl", FoodWeaponEvolution.SkillEffect.WEAKEN),
        PACK_FEAST("pack_feast", FoodWeaponEvolution.SkillEffect.LIFESTEAL);

        private final String id;
        private final FoodWeaponEvolution.SkillEffect effect;

        private EvolutionSkill(String nullxx, FoodWeaponEvolution.SkillEffect nullxxx) {
            this.id = nullxx;
            this.effect = nullxxx;
        }

        Component nameComponent() {
            return Component.translatable("skill.ftboss.evolution." + this.id);
        }

        static FoodWeaponEvolution.EvolutionSkill byId(String var0) {
            for (FoodWeaponEvolution.EvolutionSkill var4 : values()) {
                if (var4.id.equals(var0)) {
                    return var4;
                }
            }

            return null;
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

        private EvolutionStat(String nullxx, double nullxxx, int nullxxxx) {
            this.id = nullxx;
            this.gain = nullxxx;
            this.weight = nullxxxx;
        }

        Component displayName(double var1) {
            return Component.translatable("tooltip.ftboss.gourmet.stat." + this.id, new Object[]{FoodWeaponEvolution.formatNumber(var1)});
        }

        static FoodWeaponEvolution.EvolutionStat random(FoodWeaponEvolution.WeaponRole var0, RandomSource var1) {
            int var2 = 0;

            for (FoodWeaponEvolution.EvolutionStat var6 : values()) {
                var2 += var0.statWeight(var6);
            }

            int var8 = var1.nextInt(var2);

            for (FoodWeaponEvolution.EvolutionStat var7 : values()) {
                var8 -= var0.statWeight(var7);
                if (var8 < 0) {
                    return var7;
                }
            }

            return LUCK;
        }
    }

    private static enum FoodBlessing {
        CANDIED_EDGE("candied_edge", FoodWeaponEvolution.FoodBlessingKind.EXECUTE),
        SYRUP_STEP("syrup_step", FoodWeaponEvolution.FoodBlessingKind.ATTACK_SPEED),
        FROSTED_SHELL("frosted_shell", FoodWeaponEvolution.FoodBlessingKind.ABSORPTION),
        CLEAVER_MIGHT("cleaver_might", FoodWeaponEvolution.FoodBlessingKind.ATTACK_DAMAGE),
        BUTCHER_HIDE("butcher_hide", FoodWeaponEvolution.FoodBlessingKind.LOW_HEALTH_GUARD),
        BLOOD_FEAST("blood_feast", FoodWeaponEvolution.FoodBlessingKind.LIFESTEAL),
        TEA_THORNS("tea_thorns", FoodWeaponEvolution.FoodBlessingKind.POISON),
        CAMELLIA_GRACE("camellia_grace", FoodWeaponEvolution.FoodBlessingKind.REGEN),
        ROOTED_FOCUS("rooted_focus", FoodWeaponEvolution.FoodBlessingKind.ROOT),
        VINTAGE_CRIT("vintage_crit", FoodWeaponEvolution.FoodBlessingKind.CRIT),
        BARREL_PLATE("barrel_plate", FoodWeaponEvolution.FoodBlessingKind.PROJECTILE_GUARD),
        CORK_RUSH("cork_rush", FoodWeaponEvolution.FoodBlessingKind.CORK_RUSH),
        CHEESE_FRENZY("cheese_frenzy", FoodWeaponEvolution.FoodBlessingKind.FRENZY),
        PLAGUE_TOOTH("plague_tooth", FoodWeaponEvolution.FoodBlessingKind.WITHER),
        SCAVENGER_LUCK("scavenger_luck", FoodWeaponEvolution.FoodBlessingKind.CHAIN),
        OVERLOAD_DAMAGE("overload_damage", FoodWeaponEvolution.FoodBlessingKind.OVERLOAD),
        VOLT_STEP("volt_step", FoodWeaponEvolution.FoodBlessingKind.MOVEMENT_SPEED),
        MAGNETIC_PLATE("magnetic_plate", FoodWeaponEvolution.FoodBlessingKind.KNOCKBACK_RESISTANCE),
        PRISMATIC_VENOM("prismatic_venom", FoodWeaponEvolution.FoodBlessingKind.PRISMATIC),
        RECYCLE_HEART("recycle_heart", FoodWeaponEvolution.FoodBlessingKind.MAX_HEALTH),
        FERTILE_HASTE("fertile_haste", FoodWeaponEvolution.FoodBlessingKind.REPAIR),
        HONEYCRISP_HEART("honeycrisp_heart", FoodWeaponEvolution.FoodBlessingKind.EMERGENCY_HEART),
        GOLDEN_FORTUNE("golden_fortune", FoodWeaponEvolution.FoodBlessingKind.MARK),
        ORCHARD_EDGE("orchard_edge", FoodWeaponEvolution.FoodBlessingKind.BURN),
        UNDERTOW_STEP("undertow_step", FoodWeaponEvolution.FoodBlessingKind.UNDERTOW),
        BRINE_SLOW("brine_slow", FoodWeaponEvolution.FoodBlessingKind.BRINE),
        TIDAL_BULWARK("tidal_bulwark", FoodWeaponEvolution.FoodBlessingKind.WET_GUARD),
        SHEPHERD_MIGHT("shepherd_might", FoodWeaponEvolution.FoodBlessingKind.ISOLATION),
        HUNTING_STEP("hunting_step", FoodWeaponEvolution.FoodBlessingKind.HUNT),
        PACK_FEAST("pack_feast", FoodWeaponEvolution.FoodBlessingKind.KILL_FEAST);

        private final String id;
        private final FoodWeaponEvolution.FoodBlessingKind kind;
        private final UUID modifierId;

        private FoodBlessing(String nullxx, FoodWeaponEvolution.FoodBlessingKind nullxxx) {
            this.id = nullxx;
            this.kind = nullxxx;
            this.modifierId = UUID.nameUUIDFromBytes(("ftboss.food_blessing." + nullxx).getBytes(StandardCharsets.UTF_8));
        }

        Component nameComponent() {
            return Component.translatable("food_blessing.ftboss." + this.id);
        }

        Component descriptionComponent(int var1) {
            return Component.translatable("tooltip.ftboss.food_blessing." + this.id, new Object[]{var1});
        }

        static FoodWeaponEvolution.FoodBlessing byId(String var0) {
            for (FoodWeaponEvolution.FoodBlessing var4 : values()) {
                if (var4.id.equals(var0)) {
                    return var4;
                }
            }

            return null;
        }
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

    public static record FoodBlessingView(Component name, Component description, String rank) {
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
            return Component.translatable("food.ftboss.socket." + this.id());
        }

        Component effectDescription() {
            return Component.translatable("tooltip.ftboss.socket.effect." + this.id());
        }

        Component setDescription() {
            String var1 = this == FOOD ? SWEET.id() : this.id();
            return Component.translatable("tooltip.ftboss.socket.set." + var1);
        }
    }

    public static enum Profile {
        SUGAR(
            "sugar",
            1,
            FoodWeaponEvolution.FoodType.SWEET,
            FoodWeaponEvolution.EvolutionSkill.FROST_BITE,
            FoodWeaponEvolution.EvolutionSkill.SUGAR_RUSH,
            FoodWeaponEvolution.EvolutionSkill.CANDY_GUARD
        ),
        BUTCHER(
            "butcher",
            2,
            FoodWeaponEvolution.FoodType.MEAT,
            FoodWeaponEvolution.EvolutionSkill.BLOOD_FEAST,
            FoodWeaponEvolution.EvolutionSkill.CLEAVER_FORCE,
            FoodWeaponEvolution.EvolutionSkill.HEAVY_IMPACT
        ),
        SAYA(
            "saya",
            3,
            FoodWeaponEvolution.FoodType.TEA,
            FoodWeaponEvolution.EvolutionSkill.THORN_VENOM,
            FoodWeaponEvolution.EvolutionSkill.TEA_RECOVERY,
            FoodWeaponEvolution.EvolutionSkill.ROOTED_GRIP
        ),
        WINE(
            "wine",
            4,
            FoodWeaponEvolution.FoodType.WINE,
            FoodWeaponEvolution.EvolutionSkill.VINTAGE_FURY,
            FoodWeaponEvolution.EvolutionSkill.TIPSY_GUARD,
            FoodWeaponEvolution.EvolutionSkill.CORK_BURST
        ),
        RAT(
            "rat",
            5,
            FoodWeaponEvolution.FoodType.CHEESE,
            FoodWeaponEvolution.EvolutionSkill.CHEESE_FRENZY,
            FoodWeaponEvolution.EvolutionSkill.PLAGUE_BITE,
            FoodWeaponEvolution.EvolutionSkill.SCAVENGER_FEAST
        ),
        DEFECT(
            "defect",
            6,
            FoodWeaponEvolution.FoodType.ENERGY,
            FoodWeaponEvolution.EvolutionSkill.OVERLOAD_ARC,
            FoodWeaponEvolution.EvolutionSkill.HIGH_VOLTAGE,
            FoodWeaponEvolution.EvolutionSkill.MAGNETIC_LOCK
        ),
        COMPOST(
            "compost",
            7,
            FoodWeaponEvolution.FoodType.VEGETABLE,
            FoodWeaponEvolution.EvolutionSkill.PRISMATIC_TOXIN,
            FoodWeaponEvolution.EvolutionSkill.RECYCLE_GUARD,
            FoodWeaponEvolution.EvolutionSkill.FERTILE_REGROWTH
        ),
        APPLE(
            "apple",
            8,
            FoodWeaponEvolution.FoodType.APPLE,
            FoodWeaponEvolution.EvolutionSkill.HONEYCRISP_REBIRTH,
            FoodWeaponEvolution.EvolutionSkill.GOLDEN_PEEL,
            FoodWeaponEvolution.EvolutionSkill.ORCHARD_STRIKE
        ),
        TIDE(
            "tide",
            9,
            FoodWeaponEvolution.FoodType.FISH,
            FoodWeaponEvolution.EvolutionSkill.UNDERTOW,
            FoodWeaponEvolution.EvolutionSkill.BRINE_BITE,
            FoodWeaponEvolution.EvolutionSkill.TIDAL_RECOVERY
        ),
        BENGER(
            "benger",
            10,
            FoodWeaponEvolution.FoodType.MEAT,
            FoodWeaponEvolution.EvolutionSkill.SHEPHERD_MIGHT,
            FoodWeaponEvolution.EvolutionSkill.HUNTING_HOWL,
            FoodWeaponEvolution.EvolutionSkill.PACK_FEAST
        );

        private final String id;
        private final int tint;
        private final FoodWeaponEvolution.FoodType food;
        private final FoodWeaponEvolution.EvolutionSkill[] pool;

        private Profile(String nullxx, int nullxxx, FoodWeaponEvolution.FoodType nullxxxx, FoodWeaponEvolution.EvolutionSkill... nullxxxxx) {
            this.id = nullxx;
            this.tint = nullxxx;
            this.food = nullxxxx;
            this.pool = nullxxxxx;
        }

        Component displayName() {
            return Component.translatable("profile.ftboss.evolution." + this.id);
        }

        static FoodWeaponEvolution.Profile byId(String var0) {
            for (FoodWeaponEvolution.Profile var4 : values()) {
                if (var4.id.equals(var0)) {
                    return var4;
                }
            }

            return null;
        }

        static FoodWeaponEvolution.Profile byTint(int var0) {
            for (FoodWeaponEvolution.Profile var4 : values()) {
                if (var4.tint == var0) {
                    return var4;
                }
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

    private static record Socket(FoodWeaponEvolution.FoodType type, String item, FoodWeaponEvolution.SocketTier tier) {
    }

    private static enum SocketTier {
        BASIC(1, 1.0),
        FINE(2, 1.25),
        GOURMET(3, 1.5);

        private final int level;
        private final double multiplier;

        private SocketTier(int nullxx, double nullxxx) {
            this.level = nullxx;
            this.multiplier = nullxxx;
        }

        static FoodWeaponEvolution.SocketTier byLevel(int var0) {
            if (var0 >= 3) {
                return GOURMET;
            } else {
                return var0 == 2 ? FINE : BASIC;
            }
        }

        Component displayName() {
            return Component.translatable("tier.ftboss.socket." + this.level);
        }
    }

    public static record SocketView(ItemStack stack, Component type, Component tier, Component effect, Component synergy, int color, boolean matching) {
    }

    public static record StatueDailyView(Component request, String requestItemId, int progress, int required, Component reward, int state) {
    }

    public static record WeaponDetailView(Component category, Component title, Component description, int gourmetSkillIndex, boolean selected) {
        public WeaponDetailView(Component var1, Component var2, Component var3) {
            this(var1, var2, var3, -1, false);
        }

        public boolean selectable() {
            return this.gourmetSkillIndex >= 0;
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

        private WeaponRole(String nullxx) {
            this.id = nullxx;
        }

        Component displayName() {
            return Component.translatable("role.ftboss.weapon." + this.id);
        }

        Component bonusDescription() {
            return Component.translatable("tooltip.ftboss.evolution.role_bonus." + this.id);
        }

        boolean specializes(FoodWeaponEvolution.FoodType var1) {
            return switch (this) {
                case VANGUARD -> var1 == FoodWeaponEvolution.FoodType.MEAT
                || var1 == FoodWeaponEvolution.FoodType.WINE
                || var1 == FoodWeaponEvolution.FoodType.ENERGY;
                case DUELIST -> var1 == FoodWeaponEvolution.FoodType.TEA
                || var1 == FoodWeaponEvolution.FoodType.FISH
                || var1 == FoodWeaponEvolution.FoodType.ENERGY;
                case RANGED -> var1 == FoodWeaponEvolution.FoodType.WINE
                || var1 == FoodWeaponEvolution.FoodType.FISH
                || var1 == FoodWeaponEvolution.FoodType.ENERGY;
                case ARCANE -> var1 == FoodWeaponEvolution.FoodType.TEA
                || var1 == FoodWeaponEvolution.FoodType.SWEET
                || var1 == FoodWeaponEvolution.FoodType.FOOD
                || var1 == FoodWeaponEvolution.FoodType.ENERGY;
                case GUARDIAN -> var1 == FoodWeaponEvolution.FoodType.CHEESE
                || var1 == FoodWeaponEvolution.FoodType.VEGETABLE
                || var1 == FoodWeaponEvolution.FoodType.APPLE;
                case TOOL -> var1 == FoodWeaponEvolution.FoodType.TEA
                || var1 == FoodWeaponEvolution.FoodType.ENERGY
                || var1 == FoodWeaponEvolution.FoodType.VEGETABLE;
                case BALANCED -> true;
            };
        }

        double socketMultiplier(FoodWeaponEvolution.FoodType var1) {
            return this == BALANCED ? 1.1 : (this.specializes(var1) ? 1.25 : 1.0);
        }

        float skillPowerPerLevel(boolean var1) {
            if (var1) {
                return switch (this) {
                    case VANGUARD -> 0.06F;
                    case DUELIST -> 0.07F;
                    case RANGED -> 0.08F;
                    case ARCANE -> 0.14F;
                    case GUARDIAN -> 0.06F;
                    case TOOL -> 0.08F;
                    case BALANCED -> 0.1F;
                };
            } else {
                return switch (this) {
                    case VANGUARD -> 0.18F;
                    case DUELIST -> 0.18F;
                    case RANGED -> 0.2F;
                    case ARCANE -> 0.25F;
                    case GUARDIAN -> 0.16F;
                    case TOOL -> 0.18F;
                    case BALANCED -> 0.2F;
                };
            }
        }

        double cooldownReductionPerLevel(boolean var1) {
            if (var1) {
                return switch (this) {
                    case VANGUARD, GUARDIAN -> 0.03;
                    case DUELIST, RANGED, TOOL, BALANCED -> 0.04;
                    case ARCANE -> 0.045;
                };
            } else {
                return switch (this) {
                    case VANGUARD, GUARDIAN -> 0.08;
                    case DUELIST -> 0.12;
                    case RANGED, TOOL, BALANCED -> 0.1;
                    case ARCANE -> 0.14;
                };
            }
        }

        int statWeight(FoodWeaponEvolution.EvolutionStat var1) {
            return switch (this) {
                case VANGUARD -> {
                    switch (var1) {
                        case ATTACK_DAMAGE:
                            yield 6;
                        case ATTACK_SPEED:
                            yield 2;
                        case ARMOR:
                            yield 1;
                        case MAX_HEALTH:
                            yield 2;
                        case MOVEMENT_SPEED:
                        case LUCK:
                            yield 1;
                        default:
                            throw new MatchException(null, null);
                    }
                }
                case DUELIST -> {
                    switch (var1) {
                        case ATTACK_DAMAGE:
                            yield 3;
                        case ATTACK_SPEED:
                            yield 6;
                        case ARMOR:
                        case MAX_HEALTH:
                        case LUCK:
                            yield 1;
                        case MOVEMENT_SPEED:
                            yield 4;
                        default:
                            throw new MatchException(null, null);
                    }
                }
                case RANGED -> {
                    switch (var1) {
                        case ATTACK_DAMAGE:
                        case ATTACK_SPEED:
                            yield 0;
                        case ARMOR:
                            yield 1;
                        case MAX_HEALTH:
                            yield 2;
                        case MOVEMENT_SPEED:
                        case LUCK:
                            yield 4;
                        default:
                            throw new MatchException(null, null);
                    }
                }
                case ARCANE -> {
                    switch (var1) {
                        case ATTACK_DAMAGE:
                        case ATTACK_SPEED:
                            yield 0;
                        case ARMOR:
                        case MOVEMENT_SPEED:
                            yield 2;
                        case MAX_HEALTH:
                            yield 3;
                        case LUCK:
                            yield 5;
                        default:
                            throw new MatchException(null, null);
                    }
                }
                case GUARDIAN -> {
                    switch (var1) {
                        case ATTACK_DAMAGE:
                        case MOVEMENT_SPEED:
                        case LUCK:
                            yield 1;
                        case ATTACK_SPEED:
                            yield 0;
                        case ARMOR:
                        case MAX_HEALTH:
                            yield 6;
                        default:
                            throw new MatchException(null, null);
                    }
                }
                case TOOL -> {
                    switch (var1) {
                        case ATTACK_DAMAGE:
                        case ATTACK_SPEED:
                            yield 0;
                        case ARMOR:
                        case MAX_HEALTH:
                            yield 2;
                        case MOVEMENT_SPEED:
                            yield 5;
                        case LUCK:
                            yield 4;
                        default:
                            throw new MatchException(null, null);
                    }
                }
                case BALANCED -> var1.weight;
            };
        }
    }

    static void completeEvolution(ServerPlayer var0, FoodWeaponEvolution.Profile var1, ItemStack var2) {
        CompoundTag var3 = writableData(var2, var1);
        int var4 = Math.max(0, var3.getInt("Level"));
        int var5 = maxLevel(var2);
        if (var4 >= var5) {
            var0.displayClientMessage(Component.translatable("message.ftboss.evolution.maxed"), true);
        } else {
            int var6 = threshold(var2, var4);
            int var7 = Math.max(0, var3.getInt("Experience"));
            if (var7 < var6) {
                var0.displayClientMessage(Component.translatable("message.ftboss.evolution.need_xp", new Object[]{var7, var6}), true);
            } else {
                FoodWeaponRepair.invalidateForProgression(var2);
                var3.putInt("Level", ++var4);
                var3.remove("BlessingWashed");
                FoodWeaponData.write(var2, "FTBossFoodEvolution", var3);
                boolean var8 = isGourmetEvolutionWeapon(var2);
                if (!var8 && var4 == 2) {
                    var3.putString("SecondProfile", var1.id);
                }

                FoodWeaponEvolution.EvolutionSkill var9 = null;
                FoodWeaponEvolution.EvolutionStat var10 = null;
                if (var8) {
                    var3.putString("ActiveProfile", var1.id);
                    var9 = drawGourmetSkill(var2, var1, var0.getRandom());
                    if (var9 != null) {
                        ListTag var11 = var3.getList("EvolutionSkills", 10);
                        CompoundTag var12 = new CompoundTag();
                        var12.putString("Profile", var1.id);
                        var12.putString("EvolutionSkill", var9.id);
                        var11.add(var12);
                        var3.put("EvolutionSkills", var11);
                        FoodWeaponData.write(var2, "FTBossFoodEvolution", var3);
                        var3.putInt("ActiveSkillIndex", Math.max(0, gourmetSkills(var2).size() - 1));
                    }

                    var10 = FoodWeaponEvolution.EvolutionStat.random(weaponRole(var2), var0.getRandom());
                    CompoundTag var16 = var3.getCompound("EvolutionStats");
                    var16.putDouble(var10.id, Math.max(0.0, var16.getDouble(var10.id)) + var10.gain);
                    var3.put("EvolutionStats", var16);
                    FoodWeaponData.write(var2, "FTBossFoodEvolution", var3);
                    GourmetEvolutionWeapon.animateEvolution(var0, var2);
                }

                FoodWeaponData.write(var2, "FTBossFoodEvolution", var3);
                playEvolutionSuccessParticles(var0, var8);
                var0.serverLevel()
                    .playSound(
                        null, var0.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 0.75F + (float)Math.min(var4, 5) * 0.12F
                    );
                Object var17;
                if (var9 != null) {
                    var17 = var9.nameComponent();
                } else {
                    Aspect var18 = Aspect.fromId(boundProfile(var2).id);
                    if (var4 != 1 && var18 != null) {
                        Aspect var13 = Aspect.fromId(var1.id);
                        Fusion var14 = WeaponEvolutionSkillPool.fusion(var18, var13);
                        var17 = var14 == null
                            ? Component.translatable("message.ftboss.evolution.native_skill_up")
                            : Component.translatable(
                                "message.ftboss.evolution.fusion_unlock",
                                new Object[]{Component.translatable(var14.primaryTranslationKey()), Component.translatable(var14.secondaryTranslationKey())}
                            );
                    } else {
                        var17 = var18 == null
                            ? Component.translatable("message.ftboss.evolution.native_skill_up")
                            : Component.translatable(var18.translationKey());
                    }
                }

                var0.sendSystemMessage(
                    Component.translatable("message.ftboss.evolution.evolved", new Object[]{var2.getHoverName(), var4, var17, socketCapacityInternal(var2)})
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                );
                if (var8 && var10 != null) {
                    var0.sendSystemMessage(
                        Component.translatable("message.ftboss.gourmet.stat_gained", new Object[]{var10.displayName(var10.gain)})
                            .withStyle(ChatFormatting.GOLD)
                    );
                }

                FTBossMod.LOGGER
                    .info(
                        "[FTBoss][食武进化] {} 的 {} 在 {} 石像进化到 {} 级, skill={}, stat={}, slots={}",
                        new Object[]{
                            var0.getName().getString(),
                            itemId(var2),
                            var1.id,
                            var4,
                            var9 == null ? "native+1" : var9.id,
                            var10 == null ? "none" : var10.id,
                            socketCapacityInternal(var2)
                        }
                    );
            }
        }
    }

    public static boolean selectGourmetSkillFromUi(ServerPlayer var0, ItemStack var1, int var2) {
        if (var1 != var0.getMainHandItem() && var1 != var0.getOffhandItem()) {
            return false;
        } else {
            FoodWeaponEvolution.Profile var3 = boundProfile(var1);
            if (!var1.isEmpty() && !isGourmetEvolutionWeapon(var1)) {
                return FoodWeaponProgression.select(var0, var1, var2);
            } else if (!var1.isEmpty() && var3 != null) {
                List var4 = gourmetSkills(var1);
                if (var2 >= 0 && var2 < var4.size() && var2 < 8) {
                    FoodWeaponEvolution.AcquiredSkill var5 = (FoodWeaponEvolution.AcquiredSkill)var4.get(var2);
                    CompoundTag var6 = writableData(var1, var3);
                    var6.putInt("ActiveSkillIndex", var2);
                    var6.putString("ActiveProfile", var5.profile.id);
                    FoodWeaponData.write(var1, "FTBossFoodEvolution", var6);
                    var0.serverLevel()
                        .playSound(null, var0.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.75F, 1.15F + (float)var2 * 0.04F);
                    var0.displayClientMessage(
                        Component.translatable("message.ftboss.gourmet.skill_switched", new Object[]{var2 + 1, var5.profile.displayName()}), true
                    );
                    FTBossMod.LOGGER.info("[FTBoss][万味胚刃] {} 切换当前进化技能到 #{} ({})", new Object[]{var0.getName().getString(), var2 + 1, var5.profile.id});
                    return true;
                } else {
                    var0.displayClientMessage(Component.translatable("message.ftboss.gourmet.switch_unavailable"), true);
                    return false;
                }
            } else {
                var0.displayClientMessage(Component.translatable("message.ftboss.gourmet.switch_wrong_weapon"), true);
                return false;
            }
        }
    }

    public static double damageEvolutionMultiplier(ItemStack var0) {
        if (var0 == null || var0.isEmpty() || var0.getCount() != 1) {
            return 0.0;
        } else if (isEvolutionExcluded(var0)) {
            return 1.0;
        } else if (FoodWeaponLifecycle.isBroken(var0)) {
            return 0.0;
        } else if (FourWeaponFoundationAdapter.applies(var0)) {
            View var7 = FourWeaponFoundationAdapter.read(var0);
            if (var7.valid()
                && var7.persisted()
                && var7.level() >= 0
                && var7.level() <= 4
                && var7.status().ready()
                && !FourWeaponUseGuard.blocksUse(var0)
                && !FoodWeaponMaterialFingerprint.foundationSnapshot(var0).isEmpty()) {
                String var10 = var7.profile().family();

                return switch (var10) {
                    case "bow", "crossbow", "trident" -> 1.0 + (double)var7.level() * 0.12;
                    case "staff" -> (double)(1.0F + (float)var7.level() * 0.25F);
                    case "axe" -> (double)(1.0F + (float)var7.level() * 0.18F);
                    default -> 0.0;
                };
            } else {
                return 0.0;
            }
        } else if (isEvolutionCandidate(var0)
            && (var0.get(DataComponents.CUSTOM_DATA) == null || !var0.get(DataComponents.CUSTOM_DATA).contains("FTBossFoodEvolution") || var0.get(DataComponents.CUSTOM_DATA).copyTag().contains("FTBossFoodEvolution", 10))) {
            CompoundTag var1 = data(var0);
            if (var1 != null) {
                if (var1.contains("Schema")
                    || var1.contains("Version")
                    || var1.contains("ProgressionVersion")
                        && (!var1.contains("ProgressionVersion", 3) || var1.getInt("ProgressionVersion") < 0 || var1.getInt("ProgressionVersion") > 2)
                    || var1.contains("Level") && (!var1.contains("Level", 3) || var1.getInt("Level") < 0 || var1.getInt("Level") > maxLevel(var0))
                    || var1.contains("Profile") && (!var1.contains("Profile", 8) || FoodWeaponEvolution.Profile.byId(var1.getString("Profile")) == null)
                    || var1.contains("Sockets") && !var1.contains("Sockets", 9)) {
                    return 0.0;
                }

                if (var1.get("Sockets") instanceof ListTag var2 && !var2.isEmpty() && var2.getElementType() != 10) {
                    return 0.0;
                }

                ListTag var8 = var1.getList("Sockets", 10);
                if (var8.size() > unlockedSocketCapacity(var0)) {
                    return 0.0;
                }

                for (int var11 = 0; var11 < var8.size(); var11++) {
                    CompoundTag var4 = var8.getCompound(var11);
                    if (!var4.contains("Type", 8)
                        || !var4.contains("Item", 8)
                        || ResourceLocation.tryParse(var4.getString("Item")) == null
                        || var4.contains("Tier") && (!var4.contains("Tier", 3) || var4.getInt("Tier") < 1 || var4.getInt("Tier") > 3)) {
                        return 0.0;
                    }

                    try {
                        FoodWeaponEvolution.FoodType.valueOf(var4.getString("Type"));
                    } catch (IllegalArgumentException var6) {
                        return 0.0;
                    }
                }
            }

            FoodWeaponEvolution.WeaponRole var9 = weaponRole(var0);
            if (var9 == FoodWeaponEvolution.WeaponRole.RANGED) {
                FoodWeaponEvolution.SocketStats var13 = socketStats(var0);
                double var15 = var13.effectPower(FoodWeaponEvolution.FoodType.WINE, var9)
                    + var13.effectPower(FoodWeaponEvolution.FoodType.FISH, var9)
                    + var13.effectPower(FoodWeaponEvolution.FoodType.ENERGY, var9);
                return 1.0 + (double)evolutionLevel(var0) * (isGourmetEvolutionWeapon(var0) ? 0.05 : 0.12) + Math.min(0.6, var15 * 0.04);
            } else {
                double var12 = (double)activeSkillMultiplier(var0);
                if (var9 == FoodWeaponEvolution.WeaponRole.ARCANE) {
                    var12 /= WeaponMaterialBuild.build(var0).damage();
                }

                return FoodWeaponDamageAccounting.positive(var12) ? var12 : 0.0;
            }
        } else {
            return 0.0;
        }
    }

    private static FoodWeaponEvolution.SocketStats socketStats(ItemStack var0) {
        return new FoodWeaponEvolution.SocketStats(sockets(var0));
    }

    private static int unlockedSocketCapacity(ItemStack var0) {
        return isGourmetEvolutionWeapon(var0) ? 30 : Math.min(10, 2 + 2 * evolutionLevel(var0));
    }

    private static final class SocketStats {
        private final List<FoodWeaponEvolution.Socket> installed;
        private final int[] counts = new int[FoodWeaponEvolution.FoodType.values().length];
        private final double[] powers = new double[FoodWeaponEvolution.FoodType.values().length];

        int size() {
            return this.installed.size();
        }

        int count(FoodWeaponEvolution.FoodType var1) {
            return this.counts[var1.ordinal()];
        }

        double rawPower(FoodWeaponEvolution.FoodType var1) {
            return this.powers[var1.ordinal()];
        }

        double effectPower(FoodWeaponEvolution.FoodType var1, FoodWeaponEvolution.WeaponRole var2) {
            return this.rawPower(var1) * var2.socketMultiplier(var1);
        }

        int setCount(FoodWeaponEvolution.FoodType var1) {
            return var1 != FoodWeaponEvolution.FoodType.SWEET && var1 != FoodWeaponEvolution.FoodType.FOOD
                ? this.count(var1)
                : this.count(FoodWeaponEvolution.FoodType.SWEET) + this.count(FoodWeaponEvolution.FoodType.FOOD);
        }

        private SocketStats(List<FoodWeaponEvolution.Socket> var1) {
            this.installed = var1;

            for (FoodWeaponEvolution.Socket var3 : var1) {
                int var4 = var3.type.ordinal();
                this.counts[var4]++;
                this.powers[var4] = this.powers[var4] + var3.tier.multiplier * 1.5;
            }
        }

        int setTier(FoodWeaponEvolution.FoodType var1) {
            int var2 = 0;
            int var3 = 0;
            int var4 = 0;
            int var5 = 0;

            for (FoodWeaponEvolution.Socket var7 : this.installed) {
                boolean var8 = var7.type == var1
                    || (var1 == FoodWeaponEvolution.FoodType.SWEET || var1 == FoodWeaponEvolution.FoodType.FOOD)
                        && (var7.type == FoodWeaponEvolution.FoodType.SWEET || var7.type == FoodWeaponEvolution.FoodType.FOOD);
                if (var8) {
                    var5++;
                    int var9 = var7.tier.level;
                    if (var9 >= var2) {
                        var4 = var3;
                        var3 = var2;
                        var2 = var9;
                    } else if (var9 >= var3) {
                        var4 = var3;
                        var3 = var9;
                    } else if (var9 > var4) {
                        var4 = var9;
                    }
                }
            }

            if (var5 == 0) {
                return 1;
            } else {
                int var10 = Math.min(3, var5);
                int var11 = var2 + (var10 >= 2 ? var3 : 0) + (var10 >= 3 ? var4 : 0);
                return Math.max(1, Math.min(4, var11 / var10 + FoodSocketPowers.setBonus(this.counts, var1)));
            }
        }
    }


    public static void growthAttributes(ItemAttributeModifierEvent var0) {
        ItemStack var1 = var0.getItemStack();
        if (boundProfile(var1) != null && !FoodWeaponLifecycle.isBroken(var1)
            && EquipmentAttributeSlots.growthSlot(NativeEquipmentAttributes.primarySlot(var1), var1)) {
            EquipmentSlotGroup growthSlot = EquipmentSlotGroup.bySlot(NativeEquipmentAttributes.primarySlot(var1));
            boolean var2 = isGourmetEvolutionWeapon(var1);
            int var3 = evolutionLevel(var1);
            FoodWeaponEvolution.WeaponRole var4 = weaponRole(var1);
            double var5 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.MEAT);
            double var7 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.WINE);
            double var9 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.ENERGY);
            double var11 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.TEA);
            FoodWeaponEvolution.Profile var13 = activeProfile(var1);
            double var14 = var13 == null ? 0.0 : socketEffectPower(var1, var13.food);
            double var16 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.CHEESE);
            double var18 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.VEGETABLE);
            double var20 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.APPLE);
            double var22 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.FISH);
            double var24 = socketEffectPower(var1, FoodWeaponEvolution.FoodType.SWEET) + socketEffectPower(var1, FoodWeaponEvolution.FoodType.FOOD);
            double var26 = evolutionDamageBonus(var4, var3, var2)
                + (var2 ? gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.ATTACK_DAMAGE) : 0.0)
                + var5
                + var7 * 0.5
                + var9 * 0.5
                + var14 * 0.5;
            double var28 = evolutionSpeedBonus(var4, var3, var2)
                + (var2 ? gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.ATTACK_SPEED) : 0.0)
                + var11 * 0.05
                + var9 * 0.02;
            double var30 = evolutionArmorBonus(var4, var3, var2) + var16 + (var2 ? gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.ARMOR) : 0.0);
            double var32 = evolutionHealthBonus(var4, var3, var2)
                + var20 * 2.0
                + (var2 ? gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.MAX_HEALTH) : 0.0);
            double var34 = evolutionMovementBonus(var4, var3, var2)
                + var22 * 0.03
                + (var2 ? gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.MOVEMENT_SPEED) : 0.0);
            double var36 = evolutionLuckBonus(var4, var3, var2) + var24 + (var2 ? gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.LUCK) : 0.0);
            if (var26 > 0.0) {
                var0.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(growthModifierId(EVOLUTION_DAMAGE, var1), var26, Operation.ADD_VALUE), growthSlot);
            }

            if (var28 > 0.0) {
                var0.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(growthModifierId(EVOLUTION_SPEED, var1), var28, Operation.ADD_VALUE), growthSlot);
            }

            if (var30 > 0.0) {
                var0.addModifier(Attributes.ARMOR, new AttributeModifier(growthModifierId(SOCKET_ARMOR, var1), var30, Operation.ADD_VALUE), growthSlot);
            }

            if (var18 > 0.0) {
                var0.addModifier(
                    Attributes.ARMOR_TOUGHNESS, new AttributeModifier(growthModifierId(SOCKET_TOUGHNESS, var1), var18 * 0.5, Operation.ADD_VALUE), growthSlot
                );
            }

            if (var32 > 0.0) {
                var0.addModifier(Attributes.MAX_HEALTH, new AttributeModifier(growthModifierId(SOCKET_HEALTH, var1), var32, Operation.ADD_VALUE), growthSlot);
            }

            if (var34 > 0.0) {
                var0.addModifier(
                    Attributes.MOVEMENT_SPEED, new AttributeModifier(growthModifierId(SOCKET_MOVEMENT, var1), var34, Operation.ADD_MULTIPLIED_BASE), growthSlot
                );
            }

            if (var36 > 0.0) {
                var0.addModifier(Attributes.LUCK, new AttributeModifier(growthModifierId(SOCKET_LUCK, var1), var36, Operation.ADD_VALUE), growthSlot);
            }
        }
    }

    private static ResourceLocation growthModifierId(ResourceLocation identifier, ItemStack stack) {
        if (stack.getItem() instanceof net.minecraft.world.item.ArmorItem armor) {
            return ResourceLocation.fromNamespaceAndPath(identifier.getNamespace(),
                "armor_growth/" + armor.getEquipmentSlot().getName() + "/" + identifier.getPath());
        }
        return identifier;
    }

    public static FoodWeaponHitPlan quoteFinalWeaponHit(net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation var0, FoodWeaponBondHits.Hit var1, ItemStack var2, FoodWeaponHitPlan.RandomQuotes var3) {
        FoodWeaponHitPlan.Builder var4 = new FoodWeaponHitPlan.Builder(var0.getAmount());
        var4.ftboss$meleeEnhancement = var0.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK);
        if (var1 != null
            && !var0.isCanceled()
            && !FoodWeaponBondHits.secondary(var0.getSource())
            && Float.isFinite(var0.getAmount())
            && !(var0.getAmount() <= 0.0F)
            && !Boolean.TRUE.equals(EXTRA_DAMAGE.get())
            && var0.getSource().getEntity() instanceof ServerPlayer var5
            && var5.isAlive()
            && !var5.isSpectator()
            && var0.getEntity() != var5) {
            ItemStack var27 = var2 != null && !var2.isEmpty() ? var2 : var1.weapon();
            FoodWeaponEvolution.Profile var7 = activeProfile(var27);
            LivingEntity var8 = var0.getEntity();
            if (var7 != null && !FoodWeaponLifecycle.isBroken(var27) && canTarget(var5, var8)) {
                CompoundTag var9 = data(var27);
                CompoundTag var10 = var9 == null ? new CompoundTag() : var9.copy();
                CompoundTag var11 = var10.copy();
                CompoundTag var12 = var5.getPersistentData().getCompound("FTBossRangedProcTimers");

                for (String var14 : detachedTimerKeys(var12)) {
                    if (var14.startsWith("Last") && var12.contains(var14, 4)) {
                        var11.putLong(var14, Math.max(var11.getLong(var14), var12.getLong(var14)));
                    }
                }

                CompoundTag var28 = var11.copy();
                long var29 = var5.serverLevel().getServer().overworld().getGameTime();
                quoteFoodBlessingHits(var4, var5, var8, var11, var29, var27, var3);
                FoodWeaponEvolution.WeaponRole var16 = weaponRole(var27);
                FoodWeaponEvolution.SocketStats var17 = socketStats(var27);
                double var18 = var17.effectPower(FoodWeaponEvolution.FoodType.WINE, var16);
                if (var18 > 0.0 && var4.roll(FoodWeaponSourceLedger.AttemptDomain.SOCKET_WINE_CRIT, Math.min(0.75, var18 * 0.07), var3)) {
                    var4.multiply(1.5F);
                }

                double var20 = var17.effectPower(FoodWeaponEvolution.FoodType.VEGETABLE, var16);
                int var22 = var17.count(FoodWeaponEvolution.FoodType.VEGETABLE);
                if (var20 > 0.0 && var4.roll(FoodWeaponSourceLedger.AttemptDomain.SOCKET_VEGETABLE_POISON, Math.min(0.85, var20 * 0.15), var3)) {
                    var4.effect(
                        FoodWeaponSourceLedger.AttemptDomain.SOCKET_VEGETABLE_POISON,
                        FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_POISON,
                        var8,
                        new MobEffectInstance(MobEffects.POISON, 40 + (int)Math.round(var20 * 20.0), Math.min(1, var22 - 1)),
                        var5
                    );
                }

                double var23 = var17.effectPower(FoodWeaponEvolution.FoodType.MEAT, var16);
                if (var23 > 0.0 && var29 - var11.getLong("LastSocketProc") >= 60L && var4.roll(FoodWeaponSourceLedger.AttemptDomain.SOCKET_MEAT_HEAL, 1.0, var3)) {
                    var4.heal(FoodWeaponSourceLedger.AttemptDomain.SOCKET_MEAT_HEAL, var5, (float)(var23 * 0.35));
                    var4.writeAfterWorld(FoodWeaponSourceLedger.AttemptDomain.SOCKET_MEAT_HEAL, var11, "LastSocketProc", net.minecraft.nbt.LongTag.valueOf(var29));
                }

                if (var17.setCount(FoodWeaponEvolution.FoodType.SWEET) >= 3
                    && var29 - var11.getLong("LastSweetGuard") >= 100L
                    && var4.roll(FoodWeaponSourceLedger.AttemptDomain.SOCKET_SWEET_GUARD, 1.0, var3)) {
                    var4.effect(
                        FoodWeaponSourceLedger.AttemptDomain.SOCKET_SWEET_GUARD,
                        null,
                        var5,
                        new MobEffectInstance(MobEffects.ABSORPTION, 80, var17.setTier(FoodWeaponEvolution.FoodType.SWEET) - 1),
                        var5
                    );
                    var4.writeAfterWorld(FoodWeaponSourceLedger.AttemptDomain.SOCKET_SWEET_GUARD, var11, "LastSweetGuard", net.minecraft.nbt.LongTag.valueOf(var29));
                }

                if (var17.count(FoodWeaponEvolution.FoodType.ENERGY) >= 3 && var29 - var11.getLong("LastEnergyArc") >= 40L) {
                    quoteChainArc(
                        var4, var5, var8, 4.0F + 2.0F * (float)(var17.setTier(FoodWeaponEvolution.FoodType.ENERGY) - 1), null, var11, "LastEnergyArc", var29
                    );
                }

                if (var7.food == FoodWeaponEvolution.FoodType.CHEESE && var17.count(FoodWeaponEvolution.FoodType.CHEESE) >= 3) {
                    var4.effect(
                        null,
                        FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_WEAKEN,
                        var8,
                        new MobEffectInstance(MobEffects.WEAKNESS, 80, var17.setTier(FoodWeaponEvolution.FoodType.CHEESE) - 1),
                        var5
                    );
                }

                if (var7.food == FoodWeaponEvolution.FoodType.WINE && var17.count(FoodWeaponEvolution.FoodType.WINE) >= 3) {
                    var4.effect(
                        null,
                        FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_WEAKEN,
                        var8,
                        new MobEffectInstance(MobEffects.CONFUSION, 80 + 40 * (var17.setTier(FoodWeaponEvolution.FoodType.WINE) - 1), 0),
                        var5
                    );
                }

                if (isGourmetEvolutionWeapon(var27) && evolutionLevel(var27) > 0) {
                    for (FoodWeaponEvolution.AcquiredSkill var31 : gourmetSkills(var27)) {
                        quoteEvolutionSkill(
                            var4,
                            var5,
                            var8,
                            var11,
                            var31.skill,
                            var29,
                            "LastSkillProc." + var31.skill.id,
                            (float)FoodWeaponSkillRarity.passiveMultiplier(var27, var31.skill.id),
                            var3
                        );
                    }
                } else if (evolutionLevel(var27) > 0) {
                    for (FoodWeaponEvolution.EvolutionSkill var26 : FoodWeaponProgression.passiveSkills(var27)) {
                        quoteEvolutionSkill(
                            var4,
                            var5,
                            var8,
                            var11,
                            var26,
                            var29,
                            "LastSkillProc." + var26.id,
                            (float)FoodWeaponSkillRarity.passiveMultiplier(var27, var26.id),
                            var3
                        );
                    }
                }

                var4.saveHitTimers(var27, var7, var5, var0.getSource(), var28, var11);
                return var4.build();
            } else {
                return var4.build();
            }
        } else {
            return var4.build();
        }
    }

    static void writePlanHitTimer(
        ItemStack var0,
        FoodWeaponEvolution.Profile var1,
        String var2,
        net.minecraft.nbt.Tag var3,
        FoodWeaponSourceLedger.OwnerWriterStep var4,
        FoodWeaponHitPlan.FinalEffectsCommit var5,
        int var6,
        FoodWeaponSourceLedger.OwnerWriterPublication var7,
        Object var8
    ) {
        FoodWeaponOriginalSequence.Session var9 = FoodWeaponHitPlan.originalSequence(var5);
        FoodWeaponOriginalSequence.Step var10 = FoodWeaponHitPlan.beginOriginalTimer(var5, var8, var2, planHitTimerProfile(var1));

        try {
            CompoundTag var11 = writableData(var0, var1);
            CompoundTag nativeBefore = data(var0);
            if (var9 != null) {
                try {
                    var9.originalCopyBegins(var10, var11, var3);
                } catch (Throwable var29) {
                    var9.observationFailed(var10, 0, var29);
                }
            }

            net.minecraft.nbt.Tag var12 = var3.copy();
            if (var9 != null) {
                try {
                    var9.originalCopyReturned(var10, var11, var3, var12);
                } catch (Throwable var28) {
                    var9.observationFailed(var10, 0, var28);
                }

                try {
                    var9.entryReturned(var10, var11, var3, var12);
                } catch (Throwable var27) {
                    var9.observationFailed(var10, 0, var27);
                }
            }

            net.minecraft.nbt.Tag var13 = var11.put(var2, var12);
            NativeLifecycleItemData.commit(var0, "FTBossFoodEvolution", nativeBefore, var11);
            FoodWeaponHitPlan.timerOriginalPutReturned(var5);
            if (var9 != null) {
                try {
                    var9.originalReturned(var10, var13);
                } catch (Throwable var26) {
                    var9.observationFailed(var10, 1, var26);
                }
            }
        } catch (Throwable var30) {
            if (var9 != null) {
                var9.originalThrown(var10, var30);
            }

            FoodWeaponHitPlan.retainOriginalTimerThrowable(var5, var30);
            throw var30;
        } finally {
            if (var9 != null) {
                try {
                    var9.close(var10);
                } catch (Throwable var25) {
                    var9.observationFailed(var10, 3, var25);
                }
            }
        }
    }

    static void applyPlanOverload(FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1, ServerPlayer var2, LivingEntity var3, List<LivingEntity> var4, float var5) {
        EXTRA_DAMAGE.set(true);

        try {
            for (LivingEntity var7 : var4) {
                if (!FoodWeaponHitPlan.leafLive(var0, var1)) {
                    return;
                }

                FoodWeaponDamageSources.hurtListedSecondary(var2, var7, var5, var1);
                if (!FoodWeaponHitPlan.leafLive(var0, var1)) {
                    return;
                }
            }
        } finally {
            EXTRA_DAMAGE.set(false);
        }

        if (FoodWeaponHitPlan.leafLive(var0, var1)) {
            ((ServerLevel)var3.level())
                .sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    var3.getX(),
                    var3.getY() + (double)var3.getBbHeight() * 0.5,
                    var3.getZ(),
                    24 + var4.size() * 4,
                    1.1,
                    0.7,
                    1.1,
                    0.16
                );
        }
    }

    static boolean hurtPlanSecondary(ServerPlayer var0, LivingEntity var1, float var2, FoodWeaponFinalDamage.ChildHurtBridge var3) {
        EXTRA_DAMAGE.set(true);

        boolean var4;
        try {
            var4 = FoodWeaponDamageSources.hurtListedSecondary(var0, var1, var2, var3);
        } finally {
            EXTRA_DAMAGE.set(false);
        }

        return var4;
    }

    public static int socketTypeCount(ItemStack var0, FoodWeaponEvolution.FoodType var1) {
        if (var0 == null || var1 == null || var0.isEmpty()) {
            return 0;
        } else if (!FourWeaponFoundationAdapter.applies(var0) && NativeFoundationItemData.element(var0, "FTBossFourWeaponFoundation") == null) {
            return FoodWeaponLifecycle.isBroken(var0) ? 0 : socketStats(var0).count(var1);
        } else {
            View var2 = FourWeaponFoundationAdapter.read(var0);
            if (var0.getCount() == 1 && var2.valid() && var2.persisted() && var2.level() >= 0 && var2.level() <= 4) {
                int var3 = 0;

                for (java.util.Map.Entry var5 : var2.entries().entrySet()) {
                    net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.Entry var6 = (net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.Entry)var5.getValue();
                    if (var6.paid() && net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.unlocked(var2.profile(), (Integer)var5.getKey(), var2.level())) {
                        ItemStack var7 = var6.storedItem();
                        if (!var7.isEmpty() && var7.getCount() == 1) {
                            java.util.Optional var8 = net.farmingtales.ftboss.api.SwordMaterialExtensions.resolve(var7);
                            if (var8.isPresent() && ((net.farmingtales.ftboss.api.SwordMaterialDefinition)var8.get()).categories().contains(net.farmingtales.ftboss.api.SwordMaterialDefinition.Category.FOOD) && foodType(var7) == var1) {
                                var3++;
                            }
                        }
                    }
                }

                if (var3 != 0) {
                    if (!var2.status().ready() || FourWeaponUseGuard.blocksUse(var0) || FoodWeaponLifecycle.isBroken(var0)) {
                        return 0;
                    }

                    if (FoodWeaponMaterialFingerprint.foundationSnapshot(var0).isEmpty()) {
                        return 0;
                    }
                }

                return var3;
            } else {
                return 0;
            }
        }
    }

    private static void quoteChainArc(
        FoodWeaponHitPlan.Builder var0, ServerPlayer var1, LivingEntity var2, float var3, FoodWeaponSourceLedger.AttemptDomain var4, CompoundTag var5, String var6, long var7
    ) {
        if (var2.level() instanceof ServerLevel var9 && FoodWeaponDamageSources.attributeSecondary(var1) != null) {
            LivingEntity var11 = var9.getEntitiesOfClass(
                    LivingEntity.class, var2.getBoundingBox().inflate(4.0), var2x -> var2x != var2 && canTarget(var1, var2x)
                )
                .stream()
                .min(Comparator.comparingDouble(var2::distanceToSqr))
                .orElse(null);
            if (var11 == null) {
                return;
            }

            var0.chain(var4, var1, var11, var3, var5, var6, var7);
            return;
        }
    }

    private static void quoteEvolutionSkill(
        FoodWeaponHitPlan.Builder var0,
        ServerPlayer var1,
        LivingEntity var2,
        CompoundTag var3,
        FoodWeaponEvolution.EvolutionSkill var4,
        long var5,
        String var7,
        float var8,
        FoodWeaponHitPlan.RandomQuotes var9
    ) {
        int var10 = var4.effect == FoodWeaponEvolution.SkillEffect.LIFESTEAL ? 60 : 20;
        boolean var11 = var5 - var3.getLong(var7) >= (long)var10;
        FoodWeaponSourceLedger.AttemptDomain var12 = FoodWeaponSourceLedger.AttemptDomain.valueOf("EVOLUTION_" + var4.name());
        if (var4.effect == FoodWeaponEvolution.SkillEffect.DAMAGE) {
            if (var0.roll(var12, 1.0, var9)) {
                var0.multiply(1.0F + 0.18F * var8);
            }
        } else if (var4.effect == FoodWeaponEvolution.SkillEffect.LIFESTEAL) {
            if (var11 && var0.roll(var12, 1.0, var9)) {
                var0.heal(var12, var1, 1.0F);
                var0.writeAfterWorld(var12, var3, var7, net.minecraft.nbt.LongTag.valueOf(var5));
            }
        } else if (var11 && (var4.effect != FoodWeaponEvolution.SkillEffect.CHAIN || FoodWeaponDamageSources.attributeSecondary(var1) != null)) {
            if (var0.roll(var12, 0.28, var9)) {
                if (var4.effect == FoodWeaponEvolution.SkillEffect.CHAIN) {
                    quoteChainArc(var0, var1, var2, 5.0F * var8, var12, var3, var7, var5);
                } else {
                    boolean var13 = true;
                    switch (var4.effect) {
                        case DAMAGE:
                            var0.multiply(1.0F + 0.6F * var8);
                            break;
                        case CRIT:
                            var0.effect(var12, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_SLOW, var2, new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1), var1);
                            break;
                        case LIFESTEAL:
                            var0.effect(var12, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_POISON, var2, new MobEffectInstance(MobEffects.POISON, 80, 0), var1);
                            break;
                        case SLOW:
                            var0.fire(var12, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_FIRE, var2, 4);
                            break;
                        case POISON:
                            var0.effect(var12, null, var1, new MobEffectInstance(MobEffects.ABSORPTION, 100, 0), var1);
                            break;
                        case FIRE:
                            var0.effect(var12, null, var1, new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0), var1);
                            var0.effect(var12, null, var1, new MobEffectInstance(MobEffects.DIG_SPEED, 100, 0), var1);
                            break;
                        case GUARD:
                            var0.effect(var12, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_WEAKEN, var2, new MobEffectInstance(MobEffects.WEAKNESS, 100, 0), var1);
                            break;
                        case HASTE:
                            var0.pushAway(var12, var2, var1.position(), 0.85);
                            break;
                        case WEAKEN:
                            var0.effect(var12, null, var1, new MobEffectInstance(MobEffects.REGENERATION, 80, 0), var1);
                            break;
                        default:
                            var13 = false;
                    }

                    if (var13) {
                        var0.writeAfterWorld(var12, var3, var7, net.minecraft.nbt.LongTag.valueOf(var5));
                    }
                }
            }
        }
    }

    private static void quoteFoodBlessingHits(
        FoodWeaponHitPlan.Builder var0, ServerPlayer var1, LivingEntity var2, CompoundTag var3, long var4, ItemStack var6, FoodWeaponHitPlan.RandomQuotes var7
    ) {
        ListTag var8 = foodBlessingList(var6);
        String var9 = "FTBossFoodFortune:" + var1.getUUID();
        String var10 = var9 + ":Level";
        if (var2.getPersistentData().getLong(var9) >= var4) {
            int var11 = Math.max(1, var2.getPersistentData().getInt(var10));
            var0.multiply(1.12F + (float)var11 * 0.06F);
            var0.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_FORTUNE, var2);
            var0.write(null, var2.getPersistentData(), var9, null);
            var0.write(null, var2.getPersistentData(), var10, null);
        }

        for (int var22 = 0; var22 < var8.size(); var22++) {
            CompoundTag var12 = var8.getCompound(var22);
            FoodWeaponEvolution.FoodBlessing var13 = FoodWeaponEvolution.FoodBlessing.byId(var12.getString("Blessing"));
            if (var13 != null) {
                int var14 = foodBlessingLevel(var12);
                float var15 = Math.min(0.36F, 0.12F + (float)var14 * 0.05F);

                FoodWeaponSourceLedger.AttemptDomain var16 = switch (var13.kind) {
                    case CRIT, EXECUTE, ABSORPTION, CHAIN, OVERLOAD, BURN, MARK, LIFESTEAL, POISON, WITHER, ROOT, REGEN, CORK_RUSH -> FoodWeaponSourceLedger.AttemptDomain.valueOf(
                    "BLESSING_" + var13.kind.name()
                );
                    default -> null;
                };
                if (var13.kind != FoodWeaponEvolution.FoodBlessingKind.CHAIN && var13.kind != FoodWeaponEvolution.FoodBlessingKind.OVERLOAD
                    || FoodWeaponDamageSources.attributeSecondary(var1) != null) {
                    boolean var17 = var16 != null && var0.roll(var16, (double)var15, var7);
                    switch (var13.kind) {
                        case CRIT:
                            if (var17) {
                                var0.multiply(1.3F + (float)var14 * 0.12F);
                            }
                            break;
                        case EXECUTE:
                            if (var17) {
                                var0.effect(
                                    var16, null, var1, new MobEffectInstance(MobEffects.ABSORPTION, 60 + var14 * 20, var14 - 1, false, false, true), var1
                                );
                            }
                            break;
                        case ABSORPTION:
                            if (var17) {
                                quoteChainArc(var0, var1, var2, 2.0F + (float)var14 * 1.5F, var16, null, null, 0L);
                            }
                            break;
                        case CHAIN:
                            if (var17) {
                                quoteOverloadBurst(var0, var1, var2, 1.5F + (float)var14 * 1.25F, var16);
                            }
                            break;
                        case OVERLOAD:
                            if (var17) {
                                var0.fire(var16, FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_FIRE, var2, 2 + var14 * 2);
                            }
                            break;
                        case BURN:
                            if (var17) {
                                var0.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_FORTUNE, var2);
                                var0.write(var16, var2.getPersistentData(), var9, net.minecraft.nbt.LongTag.valueOf(var4 + 100L + (long)var14 * 40L));
                                var0.write(var16, var2.getPersistentData(), var10, net.minecraft.nbt.IntTag.valueOf(var14));
                                var0.effect(
                                    var16,
                                    FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_FORTUNE,
                                    var2,
                                    new MobEffectInstance(MobEffects.GLOWING, 100 + var14 * 40, 0, false, false, true),
                                    var1
                                );
                            }
                            break;
                        case MARK:
                            if (var17) {
                                var0.effect(
                                    var16,
                                    FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_POISON,
                                    var2,
                                    new MobEffectInstance(MobEffects.POISON, 40 + var14 * 20, Math.min(2, var14 - 1), false, false, true),
                                    var1
                                );
                            }
                            break;
                        case LIFESTEAL:
                            if (var17) {
                                var0.effect(
                                    var16,
                                    FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_WITHER,
                                    var2,
                                    new MobEffectInstance(MobEffects.WITHER, 30 + var14 * 15, Math.min(1, var14 - 1), false, false, true),
                                    var1
                                );
                            }
                            break;
                        case POISON:
                            if (var17) {
                                var0.effect(
                                    var16,
                                    FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_SLOW,
                                    var2,
                                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60 + var14 * 25, Math.min(3, var14), false, false, true),
                                    var1
                                );
                            }
                            break;
                        case WITHER:
                            if (var17) {
                                var0.effect(
                                    var16, null, var1, new MobEffectInstance(MobEffects.REGENERATION, 40 + var14 * 20, var14 - 1, false, false, true), var1
                                );
                            }
                            break;
                        case ROOT:
                            if (var17) {
                                var0.effect(
                                    var16, null, var1, new MobEffectInstance(MobEffects.DIG_SPEED, 80 + var14 * 20, var14 - 1, false, false, true), var1
                                );
                                var0.effect(
                                    var16, null, var1, new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80 + var14 * 20, var14 - 1, false, false, true), var1
                                );
                            }
                            break;
                        case REGEN:
                            if (var17) {
                                var0.effect(
                                    var16,
                                    FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_WEAKEN,
                                    var2,
                                    new MobEffectInstance(MobEffects.WEAKNESS, 60 + var14 * 20, Math.min(2, var14 - 1), false, false, true),
                                    var1
                                );
                                var0.effect(
                                    var16,
                                    FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_WEAKEN,
                                    var2,
                                    new MobEffectInstance(MobEffects.CONFUSION, 80 + var14 * 20, 0, false, false, true),
                                    var1
                                );
                            }
                            break;
                        case CORK_RUSH:
                            if (var17) {
                                var0.effect(
                                    var16,
                                    FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_SLOW,
                                    var2,
                                    new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60 + var14 * 25, Math.min(2, var14 - 1), false, false, true),
                                    var1
                                );
                            }
                            break;
                        case FRENZY:
                            double var28 = (double)(var2.getHealth() / Math.max(1.0F, var2.getMaxHealth()));
                            if (var28 <= 0.12 + (double)var14 * 0.05) {
                                var0.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_EXECUTE, var2);
                                var0.multiply(1.2F + (float)var14 * 0.12F);
                            }
                            break;
                        case PRISMATIC:
                            String var27 = "LastFoodBlessing." + var13.id;
                            if (var4 - var3.getLong(var27) >= 60L && var0.roll(FoodWeaponSourceLedger.AttemptDomain.BLESSING_LIFESTEAL, 1.0, var7)) {
                                var0.heal(FoodWeaponSourceLedger.AttemptDomain.BLESSING_LIFESTEAL, var1, 0.35F * (float)var14);
                                var0.writeAfterWorld(FoodWeaponSourceLedger.AttemptDomain.BLESSING_LIFESTEAL, var3, var27, net.minecraft.nbt.LongTag.valueOf(var4));
                            }
                            break;
                        case REPAIR:
                            long var26 = var3.getLong("LastFoodBlessing." + var13.id);
                            boolean var20 = var0.roll(FoodWeaponSourceLedger.AttemptDomain.BLESSING_FRENZY, 1.0, var7);
                            if (var20) {
                                int var21 = var20
                                    ? (var4 - var26 <= 60L ? Math.min(var14 + 2, var3.getInt("CheeseFrenzyStacks") + 1) : 1)
                                    : (var4 - var26 <= 60L ? Math.max(1, var3.getInt("CheeseFrenzyStacks")) : 1);
                                var0.multiply(1.0F + (float)var21 * (0.025F + (float)var14 * 0.01F));
                                if (var20) {
                                    var0.write(FoodWeaponSourceLedger.AttemptDomain.BLESSING_FRENZY, var3, "CheeseFrenzyStacks", net.minecraft.nbt.IntTag.valueOf(var21));
                                    var0.write(FoodWeaponSourceLedger.AttemptDomain.BLESSING_FRENZY, var3, "LastFoodBlessing." + var13.id, net.minecraft.nbt.LongTag.valueOf(var4));
                                }
                            }
                            break;
                        case EMERGENCY_HEART:
                            String var25 = "LastFoodBlessing." + var13.id;
                            if (var6.isDamaged()
                                && var4 - var3.getLong(var25) >= (long)Math.max(20, 70 - var14 * 15)
                                && var0.roll(FoodWeaponSourceLedger.AttemptDomain.BLESSING_REPAIR, 1.0, var7)) {
                                var0.repair(FoodWeaponSourceLedger.AttemptDomain.BLESSING_REPAIR, var6, var14);
                                var0.writeAfterWorld(FoodWeaponSourceLedger.AttemptDomain.BLESSING_REPAIR, var3, var25, net.minecraft.nbt.LongTag.valueOf(var4));
                            }
                            break;
                        case UNDERTOW:
                            String var24 = "LastFoodBlessing." + var13.id;
                            if (var1.getHealth() <= var1.getMaxHealth() * 0.5F
                                && var4 - var3.getLong(var24) >= 100L
                                && var0.roll(FoodWeaponSourceLedger.AttemptDomain.BLESSING_EMERGENCY_HEART, 1.0, var7)) {
                                var0.heal(FoodWeaponSourceLedger.AttemptDomain.BLESSING_EMERGENCY_HEART, var1, 0.75F * (float)var14);
                                var0.effect(
                                    FoodWeaponSourceLedger.AttemptDomain.BLESSING_EMERGENCY_HEART,
                                    null,
                                    var1,
                                    new MobEffectInstance(MobEffects.ABSORPTION, 60, 0, false, false, true),
                                    var1
                                );
                                var0.writeAfterWorld(FoodWeaponSourceLedger.AttemptDomain.BLESSING_EMERGENCY_HEART, var3, var24, net.minecraft.nbt.LongTag.valueOf(var4));
                            }
                            break;
                        case BRINE:
                            if (var2.isInWaterRainOrBubble()) {
                                var0.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_UNDERTOW, var2);
                                var0.multiply(1.1F + (float)var14 * 0.05F);
                                var0.effect(null, null, var1, new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60 + var14 * 20, 0, false, false, true), var1);
                            }
                            break;
                        case ISOLATION:
                            boolean var23 = var2.level()
                                .getEntitiesOfClass(LivingEntity.class, var2.getBoundingBox().inflate(4.0), var2x -> var2x != var2 && canTarget(var1, var2x))
                                .isEmpty();
                            if (var23) {
                                var0.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_ISOLATION, var2);
                                var0.multiply(1.1F + (float)var14 * 0.05F);
                            }
                            break;
                        case HUNT:
                            String var18 = "FTBossFoodHunt:" + var1.getUUID();
                            if (var2.getPersistentData().getLong(var18) >= var4) {
                                var0.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_HUNT, var2);
                                var0.multiply(1.05F + (float)var14 * 0.035F);
                            } else {
                                var0.requireTarget(FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_HUNT, var2);
                                var0.write(null, var2.getPersistentData(), var18, net.minecraft.nbt.LongTag.valueOf(var4 + 160L + (long)var14 * 40L));
                                var0.effect(
                                    null,
                                    FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_HUNT,
                                    var2,
                                    new MobEffectInstance(MobEffects.GLOWING, 160 + var14 * 40, 0, false, false, true),
                                    var1
                                );
                                var0.effect(
                                    null,
                                    null,
                                    var1,
                                    new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 + var14 * 20, Math.min(1, var14 - 1), false, false, true),
                                    var1
                                );
                            }
                    }
                }
            }
        }
    }

    private static void quoteOverloadBurst(FoodWeaponHitPlan.Builder var0, ServerPlayer var1, LivingEntity var2, float var3, FoodWeaponSourceLedger.AttemptDomain var4) {
        if (var2.level() instanceof ServerLevel var5 && FoodWeaponDamageSources.attributeSecondary(var1) != null) {
            List var7 = List.copyOf(
                var5.getEntitiesOfClass(LivingEntity.class, var2.getBoundingBox().inflate(3.0), var2x -> var2x != var2 && canTarget(var1, var2x))
            );
            if (var7.isEmpty()) {
                return;
            }

            var0.overload(var4, var1, var2, var7, var3);
            return;
        }
    }

    private static final EnumMap<FoodWeaponEvolution.FoodType, List<ResourceLocation>> ftboss$legacyOrdinary$DAILY_FOOD_POOLS = new EnumMap<>(FoodWeaponEvolution.FoodType.class);
    private static RecipeManager ftboss$legacyOrdinary$dailyRecipeManager;
    private static int ftboss$legacyOrdinary$dailyRecipeFingerprint = Integer.MIN_VALUE;
    private static Set<ResourceLocation> ftboss$legacyOrdinary$dailyRecipeOutputs = Set.of();

    private static int ftboss$legacyOrdinary$addExperience(ItemStack var0, FoodWeaponEvolution.Profile var1, int var2) {
            if (var2 > 0 && ftboss$legacyOrdinary$attune(var0, var1) && ftboss$legacyOrdinary$evolutionLevel(var0) < ftboss$legacyOrdinary$maxLevel(var0)) {
                int var3 = ftboss$legacyOrdinary$threshold(var0, ftboss$legacyOrdinary$evolutionLevel(var0));
                FoodWeaponProgression.ftboss$legacyOrdinary$normalizeExperience(var0);
                CompoundTag var4 = ftboss$legacyOrdinary$writableData(var0, var1);
                int var5 = ftboss$legacyOrdinary$evolutionExperience(var0);
                int var6 = (int)Math.min((long)var3, (long)var5 + (long)var2);
                var4.putInt("Experience", var6);
                NativeLegacyEvolutionItemData.write(var0, "FTBossFoodEvolution", var4);
                return var6 - var5;
            } else {
                return 0;
            }
        }

    private static boolean ftboss$legacyOrdinary$applyFoodBlessing(
            ServerPlayer var0, FoodWeaponEvolution.Profile var1, ItemStack var2, ItemStack var3, FoodWeaponEvolution.FoodType var4
        ) {
            FoodWeaponEvolution.Profile var5 = ftboss$legacyOrdinary$boundProfile(var2);
            if (var5 == null) {
                var0.displayClientMessage(Component.translatable("message.ftboss.food_blessing.attune_first"), true);
                return false;
            } else if (var4 != var1.food) {
                var0.displayClientMessage(Component.translatable("message.ftboss.food_blessing.wrong_food", new Object[]{var1.food.displayName()}), true);
                return false;
            } else {
                ListTag var6 = ftboss$legacyOrdinary$foodBlessingList(var2);
                if (var6.size() >= 6) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.food_blessing.full", new Object[]{6}), true);
                    return false;
                } else {
                    ArrayList var7 = new ArrayList();
    
                    for (FoodWeaponEvolution.FoodBlessing var9 : ftboss$legacyOrdinary$blessingPool(var1)) {
                        boolean var10 = false;
    
                        for (int var11 = 0; var11 < var6.size(); var11++) {
                            if (var9.id.equals(var6.getCompound(var11).getString("Blessing"))) {
                                var10 = true;
                                break;
                            }
                        }
    
                        if (!var10) {
                            var7.add(var9);
                        }
                    }
    
                    if (var7.isEmpty()) {
                        var0.displayClientMessage(Component.translatable("message.ftboss.food_blessing.complete", new Object[]{var1.displayName()}), true);
                        return false;
                    } else {
                        FoodWeaponEvolution.FoodBlessing var14 = (FoodWeaponEvolution.FoodBlessing)var7.get(var0.getRandom().nextInt(var7.size()));
                        FoodWeaponEvolution.SocketTier var15 = ftboss$legacyOrdinary$foodTier(var3);
    
                        int var16 = switch (var15) {
                            case BASIC -> 1;
                            case FINE -> var0.getRandom().nextBoolean() ? 2 : 1;
                            case GOURMET -> var0.getRandom().nextBoolean() ? 3 : 2;
                        };
                        CompoundTag var17 = ftboss$legacyOrdinary$writableData(var2, var5);
                        ListTag var12 = var17.getList("FoodBlessings", 10);
                        CompoundTag var13 = new CompoundTag();
                        var13.putString("Profile", var1.id);
                        var13.putString("Blessing", var14.id);
                        var13.putInt("Level", var16);
                        var13.putInt("FoodTier", var15.level);
                        var13.putString("Food", ftboss$legacyOrdinary$itemId(var3));
                        var12.add(var13);
                        var17.put("FoodBlessings", var12);
                        NativeLegacyEvolutionItemData.write(var2, "FTBossFoodEvolution", var17);
                        if (!var0.getAbilities().instabuild) {
                            var3.shrink(1);
                        }
    
                        var0.serverLevel().sendParticles(ParticleTypes.ENCHANT, var0.getX(), var0.getY() + 1.0, var0.getZ(), 72, 0.7, 0.8, 0.7, 0.15);
                        var0.serverLevel().sendParticles(ParticleTypes.END_ROD, var0.getX(), var0.getY() + 1.0, var0.getZ(), 18, 0.4, 0.5, 0.4, 0.08);
                        var0.serverLevel()
                            .playSound(null, var0.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.9F, 1.1F + (float)var15.level * 0.12F);
                        var0.sendSystemMessage(
                            Component.translatable("message.ftboss.food_blessing.applied", new Object[]{var14.nameComponent(), ftboss$legacyOrdinary$toRoman(var16), var15.displayName()})
                                .withStyle(ChatFormatting.LIGHT_PURPLE)
                        );
                        var0.sendSystemMessage(var14.descriptionComponent(var16).copy().withStyle(ChatFormatting.GRAY));
                        FTBossMod.LOGGER
                            .info(
                                "[FTBoss][食物附魔] {} 的 {} 在 {} 石像获得 {} 等级{}（食物等级{}）",
                                new Object[]{var0.getName().getString(), ftboss$legacyOrdinary$itemId(var2), var1.id, var14.id, var16, var15.level}
                            );
                        return true;
                    }
                }
            }
        }

    public static boolean ftboss$legacyOrdinary$applyFoodBlessingFromUi(ServerPlayer var0, int var1, int var2) {
            FoodWeaponEvolution.Profile var3 = FoodWeaponEvolution.Profile.byTint(var1);
            ItemStack var4 = ftboss$legacyOrdinary$heldEvolutionWeapon(var0);
            if (var3 != null && !var4.isEmpty() && var2 >= 0 && var2 <= 40 && (var2 < 36 || var2 >= 40)) {
                if (var4.is(Items.ENCHANTED_BOOK)) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.food_blessing.no_books"), true);
                    return false;
                } else {
                    ItemStack var5 = var0.getInventory().getItem(var2);
                    FoodWeaponEvolution.FoodType var6 = ftboss$legacyOrdinary$foodType(var5);
                    if (!var5.isEmpty() && var5 != var4 && !ftboss$legacyOrdinary$weaponCandidate(var5) && var6 != null) {
                        return ftboss$legacyOrdinary$applyFoodBlessing(var0, var3, var4, var5, var6);
                    } else {
                        var0.displayClientMessage(Component.translatable("message.ftboss.food_blessing.wrong_food", new Object[]{var3.food.displayName()}), true);
                        return false;
                    }
                }
            } else {
                return false;
            }
        }

    private static boolean ftboss$legacyOrdinary$attune(ItemStack var0, FoodWeaponEvolution.Profile var1) {
            FoodWeaponEvolution.Profile var2 = ftboss$legacyOrdinary$boundProfile(var0);
            if (var2 != null) {
                return true;
            } else if (!ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0) && !ftboss$legacyOrdinary$externalWeaponCandidate(var0)) {
                return false;
            } else {
                CompoundTag var3 = ftboss$legacyOrdinary$writableData(var0, var1);
                var3.putInt("Experience", 0);
                var3.putInt("Level", 0);
                NativeLegacyEvolutionItemData.write(var0, "FTBossFoodEvolution", var3);
                NativeFoodWeaponWear.manageBound(var0);
                return true;
            }
        }

    private static FoodWeaponEvolution.SocketTier ftboss$legacyOrdinary$baseFoodTier(ItemStack var0) {
            if (var0.isEmpty()) {
                return FoodWeaponEvolution.SocketTier.BASIC;
            } else if ((NativeLegacyEvolutionItemData.root(var0) != null) && NativeLegacyEvolutionItemData.root(var0).contains("FTBossSocketTier", 3)) {
                return FoodWeaponEvolution.SocketTier.byLevel(NativeLegacyEvolutionItemData.root(var0).getInt("FTBossSocketTier"));
            } else if ((NativeLegacyEvolutionItemData.root(var0) != null) && NativeLegacyEvolutionItemData.root(var0).contains("BrewLevel", 99)) {
                int var7 = NativeLegacyEvolutionItemData.root(var0).getInt("BrewLevel");
                return var7 >= 5
                    ? FoodWeaponEvolution.SocketTier.GOURMET
                    : (var7 >= 4 ? FoodWeaponEvolution.SocketTier.FINE : FoodWeaponEvolution.SocketTier.BASIC);
            } else {
                String var1 = ftboss$legacyOrdinary$itemId(var0).toLowerCase(Locale.ROOT);
                String var2 = var1.substring(Math.max(0, var1.indexOf(58) + 1));
                if (!var1.equals("minecraft:golden_apple")
                    && !var1.equals("minecraft:enchanted_golden_apple")
                    && !ftboss$legacyOrdinary$containsFoodWord(
                        var2,
                        "deluxe",
                        "supreme",
                        "royal",
                        "gourmet",
                        "legendary",
                        "ultimate",
                        "banquet",
                        "feast",
                        "grand",
                        "stuffed",
                        "honey_glazed",
                        "truffle",
                        "lobster"
                    )) {
                    Rarity var3 = var0.getRarity();
                    if (var3 != Rarity.EPIC && var3 != Rarity.RARE) {
                        FoodProperties var4 = var0.getFoodProperties(null);
                        if (var4 != null) {
                            int var5 = var4.nutrition();
                            float var6 = var4.saturation();
                            if (var5 >= 8 && var6 >= 0.8F || (float)var5 + var6 * 4.0F >= 11.0F) {
                                return FoodWeaponEvolution.SocketTier.GOURMET;
                            }
    
                            if ((float)var5 + var6 * 4.0F >= 6.0F) {
                                return FoodWeaponEvolution.SocketTier.FINE;
                            }
                        }
    
                        return var3 != Rarity.UNCOMMON
                                && !ftboss$legacyOrdinary$containsFoodWord(
                                    var2,
                                    "cooked",
                                    "baked",
                                    "grilled",
                                    "smoked",
                                    "fried",
                                    "roast",
                                    "sandwich",
                                    "burger",
                                    "stew",
                                    "soup",
                                    "pie",
                                    "cake",
                                    "tart",
                                    "pizza",
                                    "sushi",
                                    "nigiri",
                                    "roll",
                                    "latte",
                                    "cappuccino",
                                    "cocktail"
                                )
                            ? FoodWeaponEvolution.SocketTier.BASIC
                            : FoodWeaponEvolution.SocketTier.FINE;
                    } else {
                        return FoodWeaponEvolution.SocketTier.GOURMET;
                    }
                } else {
                    return FoodWeaponEvolution.SocketTier.GOURMET;
                }
            }
        }

    private static List<FoodWeaponEvolution.FoodBlessing> ftboss$legacyOrdinary$blessingPool(FoodWeaponEvolution.Profile var0) {
            return switch (var0) {
                case SUGAR -> List.of(
                FoodWeaponEvolution.FoodBlessing.CANDIED_EDGE,
                FoodWeaponEvolution.FoodBlessing.SYRUP_STEP,
                FoodWeaponEvolution.FoodBlessing.FROSTED_SHELL,
                FoodWeaponEvolution.FoodBlessing.GOLDEN_FORTUNE,
                FoodWeaponEvolution.FoodBlessing.ROOTED_FOCUS,
                FoodWeaponEvolution.FoodBlessing.CORK_RUSH
            );
                case BUTCHER -> List.of(
                FoodWeaponEvolution.FoodBlessing.CLEAVER_MIGHT,
                FoodWeaponEvolution.FoodBlessing.BUTCHER_HIDE,
                FoodWeaponEvolution.FoodBlessing.BLOOD_FEAST,
                FoodWeaponEvolution.FoodBlessing.SHEPHERD_MIGHT,
                FoodWeaponEvolution.FoodBlessing.PLAGUE_TOOTH,
                FoodWeaponEvolution.FoodBlessing.BARREL_PLATE
            );
                case SAYA -> List.of(
                FoodWeaponEvolution.FoodBlessing.TEA_THORNS,
                FoodWeaponEvolution.FoodBlessing.CAMELLIA_GRACE,
                FoodWeaponEvolution.FoodBlessing.ROOTED_FOCUS,
                FoodWeaponEvolution.FoodBlessing.FERTILE_HASTE,
                FoodWeaponEvolution.FoodBlessing.PRISMATIC_VENOM,
                FoodWeaponEvolution.FoodBlessing.HONEYCRISP_HEART
            );
                case WINE -> List.of(
                FoodWeaponEvolution.FoodBlessing.VINTAGE_CRIT,
                FoodWeaponEvolution.FoodBlessing.BARREL_PLATE,
                FoodWeaponEvolution.FoodBlessing.CORK_RUSH,
                FoodWeaponEvolution.FoodBlessing.OVERLOAD_DAMAGE,
                FoodWeaponEvolution.FoodBlessing.SCAVENGER_LUCK,
                FoodWeaponEvolution.FoodBlessing.HUNTING_STEP
            );
                case RAT -> List.of(
                FoodWeaponEvolution.FoodBlessing.CHEESE_FRENZY,
                FoodWeaponEvolution.FoodBlessing.PLAGUE_TOOTH,
                FoodWeaponEvolution.FoodBlessing.SCAVENGER_LUCK,
                FoodWeaponEvolution.FoodBlessing.BLOOD_FEAST,
                FoodWeaponEvolution.FoodBlessing.MAGNETIC_PLATE,
                FoodWeaponEvolution.FoodBlessing.ORCHARD_EDGE
            );
                case DEFECT -> List.of(
                FoodWeaponEvolution.FoodBlessing.OVERLOAD_DAMAGE,
                FoodWeaponEvolution.FoodBlessing.VOLT_STEP,
                FoodWeaponEvolution.FoodBlessing.MAGNETIC_PLATE,
                FoodWeaponEvolution.FoodBlessing.VINTAGE_CRIT,
                FoodWeaponEvolution.FoodBlessing.CHEESE_FRENZY,
                FoodWeaponEvolution.FoodBlessing.ROOTED_FOCUS
            );
                case COMPOST -> List.of(
                FoodWeaponEvolution.FoodBlessing.PRISMATIC_VENOM,
                FoodWeaponEvolution.FoodBlessing.RECYCLE_HEART,
                FoodWeaponEvolution.FoodBlessing.FERTILE_HASTE,
                FoodWeaponEvolution.FoodBlessing.CAMELLIA_GRACE,
                FoodWeaponEvolution.FoodBlessing.GOLDEN_FORTUNE,
                FoodWeaponEvolution.FoodBlessing.PLAGUE_TOOTH
            );
                case APPLE -> List.of(
                FoodWeaponEvolution.FoodBlessing.HONEYCRISP_HEART,
                FoodWeaponEvolution.FoodBlessing.GOLDEN_FORTUNE,
                FoodWeaponEvolution.FoodBlessing.ORCHARD_EDGE,
                FoodWeaponEvolution.FoodBlessing.FERTILE_HASTE,
                FoodWeaponEvolution.FoodBlessing.FROSTED_SHELL,
                FoodWeaponEvolution.FoodBlessing.CLEAVER_MIGHT
            );
                case TIDE -> List.of(
                FoodWeaponEvolution.FoodBlessing.UNDERTOW_STEP,
                FoodWeaponEvolution.FoodBlessing.BRINE_SLOW,
                FoodWeaponEvolution.FoodBlessing.TIDAL_BULWARK,
                FoodWeaponEvolution.FoodBlessing.ROOTED_FOCUS,
                FoodWeaponEvolution.FoodBlessing.CORK_RUSH,
                FoodWeaponEvolution.FoodBlessing.PACK_FEAST
            );
                case BENGER -> List.of(
                FoodWeaponEvolution.FoodBlessing.SHEPHERD_MIGHT,
                FoodWeaponEvolution.FoodBlessing.HUNTING_STEP,
                FoodWeaponEvolution.FoodBlessing.PACK_FEAST,
                FoodWeaponEvolution.FoodBlessing.BLOOD_FEAST,
                FoodWeaponEvolution.FoodBlessing.ORCHARD_EDGE,
                FoodWeaponEvolution.FoodBlessing.TIDAL_BULWARK
            );
            };
        }

    private static FoodWeaponEvolution.Profile ftboss$legacyOrdinary$boundProfile(ItemStack var0) {
            if (ftboss$legacyOrdinary$isEvolutionExcluded(var0)) {
                return null;
            } else {
                FoodWeaponEvolution.Profile var1 = ftboss$legacyOrdinary$nativeProfile(var0);
                if (var1 != null) {
                    return var1;
                } else {
                    CompoundTag var2 = ftboss$legacyOrdinary$data(var0);
                    return var2 == null ? null : FoodWeaponEvolution.Profile.byId(var2.getString("Profile"));
                }
            }
        }

    public static boolean ftboss$legacyOrdinary$claimEvolutionFromUi(ServerPlayer var0, int var1) {
            FoodWeaponEvolution.Profile var2 = FoodWeaponEvolution.Profile.byTint(var1);
            ItemStack var3 = ftboss$legacyOrdinary$heldEvolutionWeapon(var0);
            if (var2 != null && !var3.isEmpty()) {
                ftboss$legacyOrdinary$handleWeapon(var0, var2, var3, false);
                return true;
            } else {
                var0.displayClientMessage(Component.translatable("message.ftboss.evolution.not_weapon"), true);
                return false;
            }
        }

    private static boolean ftboss$legacyOrdinary$containsFoodWord(String var0, String... var1) {
            String var2 = "_" + var0.replace('/', '_').replace('-', '_').replace('.', '_') + "_";
    
            for (String var6 : var1) {
                if (var2.contains("_" + var6 + "_")) {
                    return true;
                }
            }
    
            return false;
        }

    private static CompoundTag ftboss$legacyOrdinary$createDailyTask(
            ServerPlayer var0,
            FoodWeaponEvolution.Profile var1,
            long var2,
            int var4,
            FoodWeaponEvolution.DailyRewardType var5,
            List<ResourceLocation> var6,
            List<ResourceLocation> var7
        ) {
            CompoundTag var8 = new CompoundTag();
            if (!var6.isEmpty()) {
                long var9 = var2 * 12L + (long)var4;
                ResourceLocation var11 = null;
    
                for (int var12 = 0; var12 < var6.size(); var12++) {
                    ResourceLocation var13 = (ResourceLocation)var6.get(ftboss$legacyOrdinary$dailyPoolIndex(var0, var1, var9 + (long)var12, var6.size()));
                    if (!var7.contains(var13) || var7.size() >= var6.size()) {
                        var11 = var13;
                        break;
                    }
                }
    
                if (var11 == null) {
                    var11 = (ResourceLocation)var6.get(ftboss$legacyOrdinary$dailyPoolIndex(var0, var1, var9, var6.size()));
                }
    
                var8.putString("RequestedItem", var11.toString());
            }
    
            long var14 = ftboss$legacyOrdinary$dailySeed(var0, var1, var2) ^ ((long)var4 + 1L) * -3335678366873096957L;
            int var15 = 4 + ftboss$legacyOrdinary$positiveMod(ftboss$legacyOrdinary$mix64(var14), 4) * 2;
            int var16 = ftboss$legacyOrdinary$positiveMod(ftboss$legacyOrdinary$mix64(var14 ^ -7723592293110705685L), 4);
    
            int var17 = switch (var5) {
                case EVOLUTION -> 25 + var16 * 5;
                case EXPERIENCE -> 900 + var16 * 300;
                case COINS -> 180 + var16 * 60;
            };
            var8.putInt("Required", var15);
            var8.putString("RewardType", var5.id);
            var8.putInt("RewardAmount", var17);
            return var8;
        }

    private static long ftboss$legacyOrdinary$currentDailyDay(ServerPlayer var0) {
            long var1 = var0.serverLevel().getServer().overworld().getDayTime();
            return Math.floorDiv(var1, 24000L);
        }

    private static CompoundTag ftboss$legacyOrdinary$daily(ServerPlayer var0, FoodWeaponEvolution.Profile var1) {
            CompoundTag var2 = var0.getPersistentData().getCompound("FTBossFoodEvolutionDailies");
            long var3 = ftboss$legacyOrdinary$currentDailyDay(var0);
            if (!var2.contains("MinecraftDay", 4) || var2.getLong("MinecraftDay") != var3) {
                boolean var5 = !var2.isEmpty();
                var2 = new CompoundTag();
                var2.putLong("MinecraftDay", var3);
                var0.getPersistentData().put("FTBossFoodEvolutionDailies", var2);
                if (var5) {
                    var0.sendSystemMessage(Component.translatable("message.ftboss.evolution.daily_refreshed").withStyle(ChatFormatting.AQUA));
                }
            }
    
            CompoundTag var6 = var2.getCompound(var1.id);
            if (ftboss$legacyOrdinary$ensureDailyTasks(var0, var1, var3, var6)) {
                var2.put(var1.id, var6);
                var0.getPersistentData().put("FTBossFoodEvolutionDailies", var2);
            }
    
            return var6;
        }

    private static synchronized List<ResourceLocation> ftboss$legacyOrdinary$dailyFoodPool(ServerPlayer var0, FoodWeaponEvolution.FoodType var1) {
            Set var2 = ftboss$legacyOrdinary$dailyRecipeOutputs(var0);
            List var3 = ftboss$legacyOrdinary$DAILY_FOOD_POOLS.get(var1);
            if (var3 != null) {
                return var3;
            } else {
                ArrayList<ResourceLocation> var4 = new ArrayList<>();
    
                for (ResourceLocation var6 : BuiltInRegistries.ITEM.keySet()) {
                    if (ftboss$legacyOrdinary$isDailyFoodCandidate(var6, var1, var2)) {
                        var4.add(var6);
                    }
                }
    
                var4.sort(ResourceLocation::compareTo);
                List var7 = List.copyOf(var4);
                ftboss$legacyOrdinary$DAILY_FOOD_POOLS.put(var1, var7);
                return var7;
            }
        }

    private static int ftboss$legacyOrdinary$dailyPoolIndex(ServerPlayer var0, FoodWeaponEvolution.Profile var1, long var2, int var4) {
            if (var4 <= 1) {
                return 0;
            } else {
                UUID var5 = var0.getUUID();
                long var6 = ftboss$legacyOrdinary$mix64(
                    var5.getMostSignificantBits() ^ Long.rotateLeft(var5.getLeastSignificantBits(), 23) ^ ((long)var1.ordinal() + 1L) * -7046029254386353131L
                );
                int var8 = ftboss$legacyOrdinary$positiveMod(var6, var4);
                int var9 = 1 + ftboss$legacyOrdinary$positiveMod(ftboss$legacyOrdinary$mix64(var6 ^ -3335678366873096957L), var4 - 1);
    
                while (ftboss$legacyOrdinary$greatestCommonDivisor(var9, var4) != 1) {
                    var9 = var9 == var4 - 1 ? 1 : var9 + 1;
                }
    
                return ftboss$legacyOrdinary$positiveMod((long)var8 + var2 * (long)var9, var4);
            }
        }

    private static synchronized Set<ResourceLocation> ftboss$legacyOrdinary$dailyRecipeOutputs(ServerPlayer var0) {
            RecipeManager var1 = var0.serverLevel().getServer().getRecipeManager();
            int var2 = 31 * var1.getRecipes().size() + var1.getRecipeIds().mapToInt(ResourceLocation::hashCode).sum();
            if (var1 == ftboss$legacyOrdinary$dailyRecipeManager && var2 == ftboss$legacyOrdinary$dailyRecipeFingerprint) {
                return ftboss$legacyOrdinary$dailyRecipeOutputs;
            } else {
                HashSet var3 = new HashSet();
    
                for (var holder : var1.getRecipes()) {
                    Recipe<?> var5 = holder.value();
                    try {
                        ItemStack var6 = var5.getResultItem(var0.serverLevel().registryAccess());
                        if (!var6.isEmpty()) {
                            var3.add(BuiltInRegistries.ITEM.getKey(var6.getItem()));
                        }
                    } catch (RuntimeException var7) {
                    }
                }
    
                var3.remove(null);
                ftboss$legacyOrdinary$dailyRecipeManager = var1;
                ftboss$legacyOrdinary$dailyRecipeFingerprint = var2;
                ftboss$legacyOrdinary$dailyRecipeOutputs = Set.copyOf(var3);
                ftboss$legacyOrdinary$DAILY_FOOD_POOLS.clear();
                return ftboss$legacyOrdinary$dailyRecipeOutputs;
            }
        }

    private static ResourceLocation ftboss$legacyOrdinary$dailyRequestId(CompoundTag var0) {
            String var1 = var0.getString("RequestedItem");
            return var1.isBlank() ? null : ResourceLocation.tryParse(var1);
        }

    private static Component ftboss$legacyOrdinary$dailyRequestName(CompoundTag var0, FoodWeaponEvolution.Profile var1) {
            ResourceLocation var2 = ftboss$legacyOrdinary$dailyRequestId(var0);
            Item var3 = var2 == null ? null : (Item)BuiltInRegistries.ITEM.get(var2);
            return var3 == null ? var1.food.displayName() : new ItemStack(var3).getHoverName();
        }

    private static Component ftboss$legacyOrdinary$dailyRewardName(CompoundTag var0) {
            FoodWeaponEvolution.DailyRewardType var1 = FoodWeaponEvolution.DailyRewardType.byId(var0.getString("RewardType"));
            return (Component)(var1 == null ? Component.literal("?") : var1.displayName(var0.getInt("RewardAmount")));
        }

    private static long ftboss$legacyOrdinary$dailySeed(ServerPlayer var0, FoodWeaponEvolution.Profile var1, long var2) {
            UUID var4 = var0.getUUID();
            return ftboss$legacyOrdinary$mix64(
                var4.getMostSignificantBits()
                    ^ Long.rotateLeft(var4.getLeastSignificantBits(), 23)
                    ^ ((long)var1.ordinal() + 1L) * -7046029254386353131L
                    ^ var2 * -4658895280553007687L
            );
        }

    private static CompoundTag ftboss$legacyOrdinary$data(ItemStack var0) {
            return (NativeLegacyEvolutionItemData.root(var0) != null) ? NativeLegacyEvolutionItemData.element(var0, "FTBossFoodEvolution") : null;
        }

    private static boolean ftboss$legacyOrdinary$ensureDailyTasks(ServerPlayer var0, FoodWeaponEvolution.Profile var1, long var2, CompoundTag var4) {
            boolean var5 = false;
            if (var4.contains("RequestedItem", 8) && !var4.contains(ftboss$legacyOrdinary$taskKey(0), 10)) {
                CompoundTag var6 = new CompoundTag();
                var6.putString("RequestedItem", var4.getString("RequestedItem"));
                var6.putInt("Required", 8);
                var6.putString("RewardType", FoodWeaponEvolution.DailyRewardType.EVOLUTION.id);
                var6.putInt("RewardAmount", 40);
                var6.putInt("Progress", var4.getInt("Progress"));
                var6.putBoolean("Ready", var4.getBoolean("Ready"));
                var6.putBoolean("Claimed", var4.getBoolean("Claimed"));
                var4.put(ftboss$legacyOrdinary$taskKey(0), var6);
    
                for (String var8 : List.of("RequestedItem", "Progress", "Ready", "Claimed")) {
                    var4.remove(var8);
                }
    
                var5 = true;
            }
    
            List var22 = ftboss$legacyOrdinary$dailyFoodPool(var0, var1.food);
            boolean[] var23 = new boolean[12];
            int var24 = 0;
            ArrayList var9 = new ArrayList();
    
            for (int var10 = 0; var10 < 12; var10++) {
                CompoundTag var11 = var4.getCompound(ftboss$legacyOrdinary$taskKey(var10));
                var23[var10] = ftboss$legacyOrdinary$validDailyTask(var11, var22);
                if (var23[var10]) {
                    if (FoodWeaponEvolution.DailyRewardType.byId(var11.getString("RewardType")) == FoodWeaponEvolution.DailyRewardType.EVOLUTION) {
                        var24++;
                    }
    
                    ResourceLocation var12 = ftboss$legacyOrdinary$dailyRequestId(var11);
                    if (var12 != null) {
                        var9.add(var12);
                    }
                }
            }
    
            long var25 = ftboss$legacyOrdinary$dailySeed(var0, var1, var2);
            int var26 = ftboss$legacyOrdinary$positiveMod(ftboss$legacyOrdinary$mix64(var25 ^ 7146057691288625177L), 12);
            int var13 = -1;
            if (var24 < 3) {
                if (!var23[var26]) {
                    var13 = var26;
                } else {
                    for (int var14 = 11; var14 >= 0; var14--) {
                        if (!var23[var14]) {
                            var13 = var14;
                            break;
                        }
                    }
                }
            }
    
            int var27 = 0;
    
            for (boolean var18 : var23) {
                if (!var18) {
                    var27++;
                }
            }
    
            int var28 = ftboss$legacyOrdinary$positiveMod(ftboss$legacyOrdinary$mix64(var25 ^ -7723592293110705685L), 2);
    
            for (int var29 = 0; var29 < 12; var29++) {
                if (!var23[var29]) {
                    int var30 = 3 - var24;
                    boolean var31 = var30 > 0 && (var29 == var13 || var27 <= var30);
                    FoodWeaponEvolution.DailyRewardType var19 = var31
                        ? FoodWeaponEvolution.DailyRewardType.EVOLUTION
                        : ((var29 + var28 & 1) == 0 ? FoodWeaponEvolution.DailyRewardType.EXPERIENCE : FoodWeaponEvolution.DailyRewardType.COINS);
                    if (var31) {
                        var24++;
                    }
    
                    var27--;
                    CompoundTag var20 = ftboss$legacyOrdinary$createDailyTask(var0, var1, var2, var29, var19, var22, var9);
                    var4.put(ftboss$legacyOrdinary$taskKey(var29), var20);
                    ResourceLocation var21 = ftboss$legacyOrdinary$dailyRequestId(var20);
                    if (var21 != null) {
                        var9.add(var21);
                    }
    
                    var5 = true;
                }
            }
    
            return var5;
        }

    public static int ftboss$legacyOrdinary$evolutionExperience(ItemStack var0) {
            if (FourWeaponFoundationAdapter.applies(var0)) {
                return FourWeaponFoundationAdapter.read(var0).experience();
            } else {
                CompoundTag var1 = ftboss$legacyOrdinary$data(var0);
                return var1 == null ? 0 : FoodWeaponProgression.ftboss$legacyOrdinary$experience(var0, var1.getInt("Experience"));
            }
        }

    public static int ftboss$legacyOrdinary$evolutionLevel(ItemStack var0) {
            if (FourWeaponFoundationAdapter.applies(var0)) {
                return FourWeaponFoundationAdapter.read(var0).level();
            } else if (ftboss$legacyOrdinary$isEvolutionExcluded(var0)) {
                return 0;
            } else {
                CompoundTag var1 = ftboss$legacyOrdinary$data(var0);
                int var2 = ftboss$legacyOrdinary$maxLevel(var0);
                return var1 == null ? 0 : Math.max(0, Math.min(var2, var1.getInt("Level")));
            }
        }

    public static boolean evolveWeaponFromUi(ServerPlayer var0, int var1, int var2) {
            return FoodWeaponProgression.ftboss$legacyOrdinary$evolve(var0, var1, var2);
        }

    private static FoodWeaponEvolution.FoodType ftboss$legacyOrdinary$explicitOfferingType(String var0) {
            return switch (var0) {
                case "ftboss:sugar_queen_crystal", "ftboss:frost_crown_charm" -> FoodWeaponEvolution.FoodType.SWEET;
                case "ftboss:meat_heart", "ftboss:butcher_fang_charm", "ftboss:benger_whistle", "ftboss:shepherd_whistle_charm" -> FoodWeaponEvolution.FoodType.MEAT;
                case "ftboss:tea_seed", "ftboss:tea_heart_pendant" -> FoodWeaponEvolution.FoodType.TEA;
                case "ftboss:wine_charm", "ftboss:aged_wine_seal" -> FoodWeaponEvolution.FoodType.WINE;
                case "ftboss:rat_bait", "ftboss:cheese_moon_charm" -> FoodWeaponEvolution.FoodType.CHEESE;
                case "ftboss:defect_core", "ftboss:charged_core_pendant" -> FoodWeaponEvolution.FoodType.ENERGY;
                case "ftboss:compost_summon", "ftboss:rainbow_spore_charm" -> FoodWeaponEvolution.FoodType.VEGETABLE;
                case "ftboss:forbidden_apple", "ftboss:golden_apple_brooch" -> FoodWeaponEvolution.FoodType.APPLE;
                case "ftboss:tide_banquet", "ftboss:tide_pearl", "ftboss:tide_pearl_pendant" -> FoodWeaponEvolution.FoodType.FISH;
                default -> null;
            };
        }

    private static boolean ftboss$legacyOrdinary$externalWeaponCandidate(ItemStack var0) {
            if (!var0.isEmpty() && var0.getCount() == 1 && !ftboss$legacyOrdinary$isEvolutionExcluded(var0) && ftboss$legacyOrdinary$nativeProfile(var0) == null) {
                ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getItem());
                if (var1 == null) {
                    return false;
                } else {
                    FoodWeaponEvolution.WeaponRole var2 = ftboss$legacyOrdinary$weaponRole(var0);
                    return var2 != FoodWeaponEvolution.WeaponRole.RANGED
                            && var2 != FoodWeaponEvolution.WeaponRole.ARCANE
                            && var2 != FoodWeaponEvolution.WeaponRole.TOOL
                        ? NativeLegacyEvolutionItemData.hasPositiveMainHandDamage(var0)
                        : true;
                }
            } else {
                return false;
            }
        }

    private static ListTag ftboss$legacyOrdinary$foodBlessingList(ItemStack var0) {
            CompoundTag var1 = ftboss$legacyOrdinary$data(var0);
            return var1 == null ? new ListTag() : var1.getList("FoodBlessings", 10);
        }

    private static FoodWeaponEvolution.SocketTier ftboss$legacyOrdinary$foodTier(ItemStack var0) {
            FoodWeaponEvolution.SocketTier var1 = ftboss$legacyOrdinary$baseFoodTier(var0);
            if ((NativeLegacyEvolutionItemData.root(var0) != null) && NativeLegacyEvolutionItemData.root(var0).contains("quality_food", 10) && NativeLegacyEvolutionItemData.root(var0).getCompound("quality_food").contains("quality", 99)) {
                int var2 = Math.max(0, Math.min(3, NativeLegacyEvolutionItemData.root(var0).getCompound("quality_food").getInt("quality")));
                return FoodWeaponEvolution.SocketTier.byLevel(Math.max(var1.level, 1 + var2));
            } else {
                return var1;
            }
        }

    private static FoodWeaponEvolution.SocketTier ftboss$legacyOrdinary$foodTierById(String var0) {
            ResourceLocation var1 = ResourceLocation.tryParse(var0);
            Item var2 = var1 == null ? null : (Item)BuiltInRegistries.ITEM.get(var1);
            return var2 != null && var2 != Items.AIR ? ftboss$legacyOrdinary$foodTier(new ItemStack(var2)) : FoodWeaponEvolution.SocketTier.BASIC;
        }

    private static FoodWeaponEvolution.FoodType ftboss$legacyOrdinary$foodType(ItemStack var0) {
            if (var0.isEmpty()) {
                return null;
            } else {
                String var1 = ftboss$legacyOrdinary$itemId(var0).toLowerCase(Locale.ROOT);
                FoodWeaponEvolution.FoodType var2 = ftboss$legacyOrdinary$explicitOfferingType(var1);
                return var2 != null ? var2 : FoodSocketFoodCatalog.type(var0);
            }
        }

    private static String ftboss$legacyOrdinary$formatNumber(double var0) {
            return Math.abs(var0 - Math.rint(var0)) < 1.0E-4
                ? Integer.toString((int)Math.rint(var0))
                : String.format(Locale.ROOT, "%.2f", var0).replaceAll("0+$", "").replaceAll("\\.$", "");
        }

    private static void ftboss$legacyOrdinary$grantImmediateDailyReward(ServerPlayer var0, FoodWeaponEvolution.DailyRewardType var1, int var2) {
            if (var1 == FoodWeaponEvolution.DailyRewardType.EXPERIENCE) {
                var0.giveExperiencePoints(var2);
            } else if (var1 == FoodWeaponEvolution.DailyRewardType.COINS) {
                Item var3 = COIN_ID == null ? null : (Item)BuiltInRegistries.ITEM.get(COIN_ID);
                if (var3 == null || var3 == Items.AIR) {
                    var3 = Items.GOLD_NUGGET;
                }
    
                int var4 = var2;
    
                while (var4 > 0) {
                    ItemStack var5 = new ItemStack(var3, Math.min(var4, new ItemStack(var3).getMaxStackSize()));
                    var4 -= var5.getCount();
                    if (!var0.addItem(var5) && !var5.isEmpty()) {
                        var0.drop(var5, false);
                    }
                }
            }
        }

    private static int ftboss$legacyOrdinary$greatestCommonDivisor(int var0, int var1) {
            while (var1 != 0) {
                int var2 = var0 % var1;
                var0 = var1;
                var1 = var2;
            }
    
            return Math.abs(var0);
        }

    private static void ftboss$legacyOrdinary$handleWeapon(ServerPlayer var0, FoodWeaponEvolution.Profile var1, ItemStack var2, boolean var3) {
            NativeFoodWeaponWear.manage(var0, var2);
            FoodWeaponEvolution.Profile var4 = ftboss$legacyOrdinary$boundProfile(var2);
            if (var4 == null) {
                if (!ftboss$legacyOrdinary$attune(var2, var1)) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.evolution.not_weapon"), true);
                    return;
                }
    
                var0.sendSystemMessage(
                    Component.translatable("message.ftboss.evolution.attuned", new Object[]{var2.getHoverName(), var1.displayName()})
                        .withStyle(ChatFormatting.AQUA)
                );
            }
    
            CompoundTag var5 = ftboss$legacyOrdinary$daily(var0, var1);
            CompoundTag var6 = ftboss$legacyOrdinary$readyEvolutionTask(var5);
            if (!var3 && var6 != null) {
                int var7 = ftboss$legacyOrdinary$addExperience(var2, var1, var6.getInt("RewardAmount"));
                if (var7 <= 0) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.evolution.maxed"), true);
                } else {
                    var6.putBoolean("Claimed", true);
                    ftboss$legacyOrdinary$saveDaily(var0, var1, var5);
                    var0.sendSystemMessage(
                        Component.translatable(
                                "message.ftboss.evolution.claimed", new Object[]{var2.getHoverName(), var7, ftboss$legacyOrdinary$writableData(var2, var1).getInt("Experience")}
                            )
                            .withStyle(ChatFormatting.GREEN)
                    );
                }
            } else {
                if (var3) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.evolution.offering_required"), true);
                } else {
                    ftboss$legacyOrdinary$showWeapon(var0, var2, var1);
                }
            }
        }

    public static ItemStack ftboss$legacyOrdinary$heldEvolutionWeapon(ServerPlayer var0) {
            ItemStack var1 = var0.getMainHandItem();
            if (ftboss$legacyOrdinary$isEvolutionCandidate(var1)) {
                return var1;
            } else {
                ItemStack var2 = var0.getOffhandItem();
                return ftboss$legacyOrdinary$isEvolutionCandidate(var2) ? var2 : ItemStack.EMPTY;
            }
        }

    private static void ftboss$legacyOrdinary$insertSocket(ServerPlayer var0, FoodWeaponEvolution.Profile var1, ItemStack var2, ItemStack var3, FoodWeaponEvolution.FoodType var4) {
            FoodWeaponEvolution.Profile var5 = ftboss$legacyOrdinary$boundProfile(var2);
            if (var5 == null) {
                var0.displayClientMessage(Component.translatable("message.ftboss.evolution.socket_attune_first"), true);
            } else {
                List var6 = ftboss$legacyOrdinary$sockets(var2);
                int var7 = ftboss$legacyOrdinary$unlockedSocketCapacity(var2);
                if (var6.size() >= var7) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.evolution.socket_full", new Object[]{var7}), true);
                } else {
                    CompoundTag var8 = ftboss$legacyOrdinary$writableData(var2, var5);
                    ListTag var9 = var8.getList("Sockets", 10);
                    MutableComponent var10 = var3.getHoverName().copy();
                    FoodWeaponEvolution.SocketTier var11 = ftboss$legacyOrdinary$foodTier(var3);
                    CompoundTag var12 = new CompoundTag();
                    var12.putString("Type", var4.name());
                    var12.putString("Item", ftboss$legacyOrdinary$itemId(var3));
                    var12.putInt("Tier", var11.level);
                    ItemStack var13 = var3.copy();
                    var13.setCount(1);
                    var12.put("Stack", var13.save(var0.registryAccess()));
                    var9.add(var12);
                    var8.put("Sockets", var9);
                    NativeLegacyEvolutionItemData.write(var2, "FTBossFoodEvolution", var8);
                    if (ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var2)) {
                        GourmetEvolutionWeapon.animateSocket(var0, var2);
                        var0.serverLevel().sendParticles(ParticleTypes.END_ROD, var0.getX(), var0.getY() + 1.0, var0.getZ(), 12, 0.35, 0.45, 0.35, 0.06);
                    }
    
                    if (!var0.getAbilities().instabuild) {
                        var3.shrink(1);
                    }
    
                    var0.serverLevel().playSound(null, var0.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.45F, 1.55F);
                    var0.sendSystemMessage(
                        Component.translatable("message.ftboss.evolution.socketed", new Object[]{var10, var4.displayName(), var11.displayName(), var9.size(), var7})
                            .withStyle(var4 == var5.food ? ChatFormatting.GOLD : ChatFormatting.GREEN)
                    );
                    var0.sendSystemMessage(
                        Component.translatable(
                                "message.ftboss.evolution.socket_effect",
                                new Object[]{
                                    var4.displayName(),
                                    var11.displayName(),
                                    ftboss$legacyOrdinary$formatNumber(var11.multiplier * 1.5 * ftboss$legacyOrdinary$weaponRole(var2).socketMultiplier(var4)),
                                    var4.effectDescription()
                                }
                            )
                            .withStyle(ChatFormatting.GRAY)
                    );
                    FoodWeaponEvolution.SocketStats var14 = ftboss$legacyOrdinary$socketStats(var2);
                    int var15 = var14.setCount(var4);
                    var0.sendSystemMessage(
                        Component.translatable(
                                "message.ftboss.evolution.socket_set_progress",
                                new Object[]{Math.min(3, var15), FoodWeaponEvolution.SocketTier.byLevel(var14.setTier(var4)).displayName(), var4.setDescription()}
                            )
                            .withStyle(var15 >= 3 ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY)
                    );
                }
            }
        }

    public static boolean ftboss$legacyOrdinary$insertSocketFromUi(ServerPlayer var0, int var1, int var2) {
            FoodWeaponEvolution.Profile var3 = FoodWeaponEvolution.Profile.byTint(var1);
            ItemStack var4 = ftboss$legacyOrdinary$heldEvolutionWeapon(var0);
            if (var3 != null && !var4.isEmpty() && var2 >= 0 && var2 <= 40 && (var2 < 36 || var2 >= 40)) {
                ItemStack var5 = var0.getInventory().getItem(var2);
                FoodWeaponEvolution.FoodType var6 = ftboss$legacyOrdinary$foodType(var5);
                if (!var5.isEmpty() && var5 != var4 && var6 != null) {
                    int var7 = ftboss$legacyOrdinary$socketCount(var4);
                    ftboss$legacyOrdinary$insertSocket(var0, var3, var4, var5, var6);
                    return ftboss$legacyOrdinary$socketCount(var4) > var7;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

    private static boolean ftboss$legacyOrdinary$isDailyFoodCandidate(ResourceLocation var0, FoodWeaponEvolution.FoodType var1, Set<ResourceLocation> var2) {
            Item var3 = (Item)BuiltInRegistries.ITEM.get(var0);
            if (var3 != null && var3 != Items.AIR) {
                ItemStack var4 = new ItemStack(var3);
                if (!var4.isEmpty() && ftboss$legacyOrdinary$foodType(var4) == var1) {
                    boolean var5 = var4.has(DataComponents.FOOD) || var4.getUseAnimation() == UseAnim.DRINK || ftboss$legacyOrdinary$isNamedDrink(var0.getPath(), var1);
                    return var5 && (var2.contains(var0) || ftboss$legacyOrdinary$isNaturallyObtainableDailyFood(var4, var0));
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

    public static boolean ftboss$legacyOrdinary$isEvolutionCandidate(ItemStack var0) {
            return FourWeaponFoundationAdapter.applies(var0) ? FourWeaponFoundationAdapter.read(var0).valid() : ftboss$legacyOrdinary$weaponCandidate(var0);
        }

    private static boolean ftboss$legacyOrdinary$isEvolutionExcluded(ItemStack var0) {
        if (!WeaponSystemScope410.supports(var0)) return true;
            if (var0.isEmpty()) {
                return false;
            } else {
                ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getItem());
                return var1 != null && "create".equals(var1.getNamespace());
            }
        }

    public static boolean ftboss$legacyOrdinary$isGourmetBlankBlade(ItemStack var0) {
            return "ftboss:gourmet_blank_blade".equals(ftboss$legacyOrdinary$itemId(var0));
        }

    public static boolean ftboss$legacyOrdinary$isGourmetEvolutionWeapon(ItemStack var0) {
            return var0.getItem() instanceof GourmetEvolutionWeapon;
        }

    private static boolean ftboss$legacyOrdinary$isNamedDrink(String var0, FoodWeaponEvolution.FoodType var1) {
            if (ftboss$legacyOrdinary$containsFoodWord(
                var0, "seed", "seeds", "leaf", "leaves", "blend", "bag", "crop", "block", "crate", "bucket", "spawn_egg", "powder", "bean", "mace", "charm"
            )) {
                return false;
            } else {
                return switch (var1) {
                    case TEA -> ftboss$legacyOrdinary$containsFoodWord(var0, "tea", "matcha", "oolong", "kombucha", "herbal_infusion");
                    case WINE -> ftboss$legacyOrdinary$containsFoodWord(
                    var0,
                    "wine",
                    "beer",
                    "ale",
                    "mead",
                    "cider",
                    "liquor",
                    "vodka",
                    "whiskey",
                    "whisky",
                    "brew",
                    "sake",
                    "rum",
                    "stout",
                    "grog",
                    "champagne",
                    "brandy",
                    "bourbon",
                    "absinthe",
                    "tequila",
                    "sangria",
                    "cocktail",
                    "lager",
                    "kvass",
                    "gin_tonic"
                );
                    case ENERGY -> ftboss$legacyOrdinary$containsFoodWord(var0, "coffee", "espresso", "latte", "cappuccino", "mocha", "hot_cocoa", "cola", "energy_drink");
                    default -> false;
                };
            }
        }

    private static boolean ftboss$legacyOrdinary$isNaturallyObtainableDailyFood(ItemStack var0, ResourceLocation var1) {
            if ("minecraft".equals(var1.getNamespace()) && VANILLA_NATURAL_DAILY_FOODS.contains(var1.getPath())) {
                return true;
            } else {
                for (TagKey var3 : DAILY_NATURAL_FOOD_TAGS) {
                    if (var0.is(var3)) {
                        return true;
                    }
                }
    
                return false;
            }
        }

    private static String ftboss$legacyOrdinary$itemId(ItemStack var0) {
            ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getItem());
            return var1 == null ? "" : var1.toString();
        }

    private static boolean ftboss$legacyOrdinary$matchesDailyRequest(CompoundTag var0, FoodWeaponEvolution.Profile var1, ItemStack var2, FoodWeaponEvolution.FoodType var3) {
            ResourceLocation var4 = ftboss$legacyOrdinary$dailyRequestId(var0);
            return var4 == null ? var3 == var1.food : var4.toString().equals(ftboss$legacyOrdinary$itemId(var2));
        }

    private static int ftboss$legacyOrdinary$maxLevel(ItemStack var0) {
            return ftboss$legacyOrdinary$isGourmetBlankBlade(var0) ? 8 : 4;
        }

    private static long ftboss$legacyOrdinary$mix64(long var0) {
            var0 = (var0 ^ var0 >>> 30) * -4658895280553007687L;
            var0 = (var0 ^ var0 >>> 27) * -7723592293110705685L;
            return var0 ^ var0 >>> 31;
        }

    private static FoodWeaponEvolution.Profile ftboss$legacyOrdinary$nativeProfile(ItemStack var0) {
        if (!WeaponSystemScope410.supports(var0)) return null;
            String var1 = ftboss$legacyOrdinary$itemId(var0);
    
            return switch (var1) {
                case "ftboss:candy_cane" -> FoodWeaponEvolution.Profile.SUGAR;
                case "ftboss:turkey_legs" -> FoodWeaponEvolution.Profile.BUTCHER;
                case "ftboss:blooming_thorns" -> FoodWeaponEvolution.Profile.SAYA;
                case "ftboss:wine_bottle_mace", "ftboss:barrel_crossbow" -> FoodWeaponEvolution.Profile.WINE;
                case "ftboss:cheese_wheel_mace" -> FoodWeaponEvolution.Profile.RAT;
                case "ftboss:defect_claw" -> FoodWeaponEvolution.Profile.DEFECT;
                case "ftboss:compost_crown" -> FoodWeaponEvolution.Profile.COMPOST;
                case "ftboss:apple_stem" -> FoodWeaponEvolution.Profile.APPLE;
                case "ftboss:tide_trident" -> FoodWeaponEvolution.Profile.TIDE;
                case "ftboss:dog_beating_stick" -> FoodWeaponEvolution.Profile.BENGER;
                default -> null;
            };
        }

    private static int ftboss$legacyOrdinary$positiveMod(long var0, int var2) {
            return (int)Math.floorMod(var0, (long)var2);
        }

    private static CompoundTag ftboss$legacyOrdinary$readyEvolutionTask(CompoundTag var0) {
            for (int var1 = 0; var1 < 12; var1++) {
                CompoundTag var2 = var0.getCompound(ftboss$legacyOrdinary$taskKey(var1));
                if (var2.getBoolean("Ready")
                    && !var2.getBoolean("Claimed")
                    && FoodWeaponEvolution.DailyRewardType.byId(var2.getString("RewardType")) == FoodWeaponEvolution.DailyRewardType.EVOLUTION) {
                    return var2;
                }
            }
    
            return null;
        }

    public static boolean ftboss$legacyOrdinary$removeSocketFromUi(ServerPlayer var0, int var1, int var2) {
            FoodWeaponEvolution.Profile var3 = FoodWeaponEvolution.Profile.byTint(var1);
            ItemStack var4 = ftboss$legacyOrdinary$heldEvolutionWeapon(var0);
            FoodWeaponEvolution.Profile var5 = ftboss$legacyOrdinary$boundProfile(var4);
            if (var3 != null && !var4.isEmpty() && var5 != null) {
                CompoundTag var6 = ftboss$legacyOrdinary$writableData(var4, var5);
                ListTag var7 = var6.getList("Sockets", 10);
                if (var2 >= 0 && var2 < var7.size()) {
                    ItemStack var8 = ItemStack.EMPTY;
    
                    for (int var9 = 0; var9 <= 40; var9++) {
                        if (var9 < 36 || var9 >= 40) {
                            ItemStack var10 = var0.getInventory().getItem(var9);
                            if (var10.is(Items.SHEARS)) {
                                var8 = var10;
                                break;
                            }
                        }
                    }
    
                    if (var8.isEmpty() && !var0.getAbilities().instabuild) {
                        var0.displayClientMessage(Component.translatable("message.ftboss.evolution.socket_need_shears"), true);
                        return false;
                    } else {
                        CompoundTag var15 = var7.getCompound(var2);
                        var7.remove(var2);
                        var6.put("Sockets", var7);
                        NativeLegacyEvolutionItemData.write(var4, "FTBossFoodEvolution", var6);
                        String var16 = var15.getString("Item");
                        ResourceLocation var11 = ResourceLocation.tryParse(var16);
                        Item var12 = var11 == null ? null : (Item)BuiltInRegistries.ITEM.get(var11);
                        ItemStack var13 = var15.contains("Stack", 10)
                            ? ItemStack.parseOptional(var0.registryAccess(), var15.getCompound("Stack"))
                            : (var12 != null && var12 != Items.AIR ? new ItemStack(var12) : ItemStack.EMPTY);
                        if (!var13.isEmpty() && !var0.getInventory().add(var13)) {
                            var0.drop(var13, false);
                        }
    
                        if (!var0.getAbilities().instabuild) {
                            var8.hurtAndBreak(1, var0.serverLevel(), var0, var0x -> {
                            });
                        }
    
                        var0.serverLevel().playSound(null, var0.blockPosition(), SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 0.7F, 1.25F);
                        FoodWeaponEvolution.SocketTier var14 = FoodWeaponEvolution.SocketTier.byLevel(
                            var15.contains("Tier", 3) ? var15.getInt("Tier") : ftboss$legacyOrdinary$foodTierById(var16).level
                        );
                        var0.sendSystemMessage(
                            Component.translatable(
                                    "message.ftboss.evolution.socket_removed",
                                    new Object[]{ftboss$legacyOrdinary$socketItemName(var16), var14.displayName(), var7.size(), ftboss$legacyOrdinary$socketCapacityInternal(var4)}
                                )
                                .withStyle(ChatFormatting.GREEN)
                        );
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

    private static void ftboss$legacyOrdinary$saveDaily(ServerPlayer var0, FoodWeaponEvolution.Profile var1, CompoundTag var2) {
            CompoundTag var3 = var0.getPersistentData().getCompound("FTBossFoodEvolutionDailies");
            var3.put(var1.id, var2);
            var0.getPersistentData().put("FTBossFoodEvolutionDailies", var3);
        }

    private static void ftboss$legacyOrdinary$showWeapon(ServerPlayer var0, ItemStack var1, FoodWeaponEvolution.Profile var2) {
            CompoundTag var3 = ftboss$legacyOrdinary$writableData(var1, var2);
            int var4 = ftboss$legacyOrdinary$evolutionLevel(var1);
            int var5 = ftboss$legacyOrdinary$maxLevel(var1);
            int var6 = Math.max(0, var3.getInt("Experience"));
            int var7 = var4 >= var5 ? var6 : ftboss$legacyOrdinary$threshold(var1, var4);
            var0.sendSystemMessage(
                Component.translatable(
                        "message.ftboss.evolution.weapon_status",
                        new Object[]{var1.getHoverName(), var4, var5, var6, var7, ftboss$legacyOrdinary$sockets(var1).size(), ftboss$legacyOrdinary$socketCapacityInternal(var1)}
                    )
                    .withStyle(ChatFormatting.AQUA)
            );
            if (var4 < var5 && var6 >= var7) {
                var0.sendSystemMessage(Component.translatable("message.ftboss.evolution.can_evolve").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }

    private static int ftboss$legacyOrdinary$socketCapacityInternal(ItemStack var0) {
            if (ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0)) {
                return 30;
            } else {
                int var1 = ftboss$legacyOrdinary$unlockedSocketCapacity(var0);
                return Math.max(var1, ftboss$legacyOrdinary$storedSocketCount(var0));
            }
        }

    public static int ftboss$legacyOrdinary$socketCount(ItemStack var0) {
            return ftboss$legacyOrdinary$socketStats(var0).size();
        }

    private static Component ftboss$legacyOrdinary$socketItemName(String var0) {
            try {
                ResourceLocation var1 = ResourceLocation.parse(var0);
                Item var2 = (Item)BuiltInRegistries.ITEM.get(var1);
                return (Component)(var2 == null ? Component.literal(var0) : var2.getDescription());
            } catch (RuntimeException var3) {
                return Component.literal(var0);
            }
        }

    private static FoodWeaponEvolution.SocketStats ftboss$legacyOrdinary$socketStats(ItemStack var0) {
            return new FoodWeaponEvolution.SocketStats(ftboss$legacyOrdinary$sockets(var0));
        }

    private static List<FoodWeaponEvolution.Socket> ftboss$legacyOrdinary$sockets(ItemStack var0) {
            ArrayList var1 = new ArrayList();
            CompoundTag var2 = ftboss$legacyOrdinary$data(var0);
            if (var2 != null && var2.contains("Sockets", 9)) {
                ListTag var3 = var2.getList("Sockets", 10);
                int var4 = ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0) ? 30 : 10;
    
                for (int var5 = 0; var5 < var3.size() && var5 < var4; var5++) {
                    CompoundTag var6 = var3.getCompound(var5);
    
                    try {
                        FoodWeaponEvolution.FoodType var7 = FoodWeaponEvolution.FoodType.valueOf(var6.getString("Type"));
                        String var8 = var6.getString("Item");
                        var7 = FoodSocketFoodCatalog.savedType(var7, var8);
                        int var9 = var6.contains("Tier", 3) ? var6.getInt("Tier") : ftboss$legacyOrdinary$foodTierById(var8).level;
                        var1.add(new FoodWeaponEvolution.Socket(var7, var8, FoodWeaponEvolution.SocketTier.byLevel(var9)));
                    } catch (IllegalArgumentException var10) {
                    }
                }
    
                return var1;
            } else {
                return var1;
            }
        }

    private static int ftboss$legacyOrdinary$storedSocketCount(ItemStack var0) {
            CompoundTag var1 = ftboss$legacyOrdinary$data(var0);
            if (var1 != null && var1.contains("Sockets", 9)) {
                int var2 = ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0) ? 30 : 10;
                return Math.min(var2, var1.getList("Sockets", 10).size());
            } else {
                return 0;
            }
        }

    private static void ftboss$legacyOrdinary$submitDailyFood(ServerPlayer var0, FoodWeaponEvolution.Profile var1, ItemStack var2, FoodWeaponEvolution.FoodType var3) {
            CompoundTag var4 = ftboss$legacyOrdinary$daily(var0, var1);
            CompoundTag var5 = null;
            int var6 = -1;
            boolean var7 = false;
    
            for (int var8 = 0; var8 < 12; var8++) {
                CompoundTag var9 = var4.getCompound(ftboss$legacyOrdinary$taskKey(var8));
                if (ftboss$legacyOrdinary$matchesDailyRequest(var9, var1, var2, var3)) {
                    if (!var9.getBoolean("Claimed") && !var9.getBoolean("Ready")) {
                        var5 = var9;
                        var6 = var8;
                        break;
                    }
    
                    var7 = true;
                }
            }
    
            if (var5 == null) {
                if (var7) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.evolution.daily_item_done", new Object[]{var2.getHoverName()}), true);
                } else {
                    var0.displayClientMessage(
                        Component.translatable("message.ftboss.evolution.wrong_food_multi", new Object[]{ftboss$legacyOrdinary$unfinishedDailyFoods(var4, var1)}), true
                    );
                }
            } else {
                int var13 = var5.getInt("Required");
                int var14 = Math.max(0, var5.getInt("Progress"));
                int var10 = Math.min(var2.getCount(), var13 - var14);
                if (var10 > 0) {
                    if (!var0.getAbilities().instabuild) {
                        var2.shrink(var10);
                    }
    
                    int var11 = var14 + var10;
                    var5.putInt("Progress", var11);
                    var4.put(ftboss$legacyOrdinary$taskKey(var6), var5);
                    if (var11 >= var13) {
                        FoodWeaponEvolution.DailyRewardType var12 = FoodWeaponEvolution.DailyRewardType.byId(var5.getString("RewardType"));
                        if (var12 == FoodWeaponEvolution.DailyRewardType.EVOLUTION) {
                            var5.putBoolean("Ready", true);
                            var0.sendSystemMessage(
                                Component.translatable("message.ftboss.evolution.daily_evolution_ready", new Object[]{var6 + 1, ftboss$legacyOrdinary$dailyRewardName(var5)})
                                    .withStyle(ChatFormatting.GOLD)
                            );
                        } else {
                            ftboss$legacyOrdinary$grantImmediateDailyReward(var0, var12, var5.getInt("RewardAmount"));
                            var5.putBoolean("Claimed", true);
                            var0.sendSystemMessage(
                                Component.translatable("message.ftboss.evolution.daily_rewarded", new Object[]{var6 + 1, ftboss$legacyOrdinary$dailyRewardName(var5)})
                                    .withStyle(ChatFormatting.GOLD)
                            );
                        }
    
                        var0.serverLevel().playSound(null, var0.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.65F, 1.35F);
                    } else {
                        var0.displayClientMessage(
                            Component.translatable("message.ftboss.evolution.daily_progress", new Object[]{ftboss$legacyOrdinary$dailyRequestName(var5, var1), var11, var13}), true
                        );
                    }
    
                    ftboss$legacyOrdinary$saveDaily(var0, var1, var4);
                }
            }
        }

    public static boolean ftboss$legacyOrdinary$submitDailyFromUi(ServerPlayer var0, int var1, int var2) {
            FoodWeaponEvolution.Profile var3 = FoodWeaponEvolution.Profile.byTint(var1);
            if (var3 != null && var2 >= 0 && var2 <= 40 && (var2 < 36 || var2 >= 40)) {
                ItemStack var4 = var0.getInventory().getItem(var2);
                FoodWeaponEvolution.FoodType var5 = ftboss$legacyOrdinary$foodType(var4);
                if (!var4.isEmpty() && var5 != null) {
                    ftboss$legacyOrdinary$submitDailyFood(var0, var3, var4, var5);
                    return true;
                } else {
                    var0.displayClientMessage(Component.translatable("message.ftboss.evolution.wrong_food"), true);
                    return false;
                }
            } else {
                return false;
            }
        }

    private static String ftboss$legacyOrdinary$taskKey(int var0) {
            return "Task" + var0;
        }

    private static int ftboss$legacyOrdinary$threshold(ItemStack var0, int var1) {
            return FoodWeaponBuildRules.evolutionCost(var1);
        }

    private static String ftboss$legacyOrdinary$toRoman(int var0) {
            return switch (var0) {
                case 2 -> "II";
                case 3 -> "III";
                default -> "I";
            };
        }

    private static Component ftboss$legacyOrdinary$unfinishedDailyFoods(CompoundTag var0, FoodWeaponEvolution.Profile var1) {
            MutableComponent var2 = Component.empty();
            boolean var3 = true;
    
            for (int var4 = 0; var4 < 12; var4++) {
                CompoundTag var5 = var0.getCompound(ftboss$legacyOrdinary$taskKey(var4));
                if (!var5.getBoolean("Claimed") && !var5.getBoolean("Ready")) {
                    if (!var3) {
                        var2.append(Component.literal(" / "));
                    }
    
                    var2.append(ftboss$legacyOrdinary$dailyRequestName(var5, var1));
                    var3 = false;
                }
            }
    
            return var3 ? Component.translatable("message.ftboss.evolution.daily_all_done") : var2;
        }

    private static int ftboss$legacyOrdinary$unlockedSocketCapacity(ItemStack var0) {
            return ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0) ? 30 : Math.min(10, 2 + 2 * ftboss$legacyOrdinary$evolutionLevel(var0));
        }

    private static boolean ftboss$legacyOrdinary$validDailyTask(CompoundTag var0, List<ResourceLocation> var1) {
            if (!var0.isEmpty()
                && var0.getInt("Required") > 0
                && var0.getInt("RewardAmount") > 0
                && FoodWeaponEvolution.DailyRewardType.byId(var0.getString("RewardType")) != null) {
                String var2 = var0.getString("RequestedItem");
                if (var2.isBlank()) {
                    return var1.isEmpty();
                } else {
                    ResourceLocation var3 = ResourceLocation.tryParse(var2);
                    return var3 != null && var1.contains(var3);
                }
            } else {
                return false;
            }
        }

    private static boolean ftboss$legacyOrdinary$weaponCandidate(ItemStack var0) {
        if (!WeaponSystemScope410.supports(var0)) return false;
            return AdditionalWeaponUiEligibility.applies(var0)
                ? true
                : ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0) || ftboss$legacyOrdinary$nativeProfile(var0) != null || ftboss$legacyOrdinary$boundProfile(var0) != null || ftboss$legacyOrdinary$externalWeaponCandidate(var0);
        }

    private static FoodWeaponEvolution.WeaponRole ftboss$legacyOrdinary$weaponRole(ItemStack var0) {
            if (var0.isEmpty()) {
                return FoodWeaponEvolution.WeaponRole.BALANCED;
            } else {
                String var1 = ftboss$legacyOrdinary$itemId(var0);
    
                FoodWeaponEvolution.WeaponRole var2 = switch (var1) {
                    case "ftboss:gourmet_blank_blade", "ftboss:turkey_legs", "ftboss:wine_bottle_mace" -> FoodWeaponEvolution.WeaponRole.VANGUARD;
                    case "ftboss:defect_claw", "ftboss:dog_beating_stick" -> FoodWeaponEvolution.WeaponRole.DUELIST;
                    case "ftboss:barrel_crossbow", "ftboss:tide_trident" -> FoodWeaponEvolution.WeaponRole.RANGED;
                    case "ftboss:compost_crown" -> FoodWeaponEvolution.WeaponRole.ARCANE;
                    case "ftboss:cheese_wheel_mace", "ftboss:apple_stem" -> FoodWeaponEvolution.WeaponRole.GUARDIAN;
                    case "ftboss:candy_cane", "ftboss:blooming_thorns" -> FoodWeaponEvolution.WeaponRole.BALANCED;
                    default -> null;
                };
                if (var2 != null) {
                    return var2;
                } else {
                    Item var3 = var0.getItem();
                    if (FoodWeaponFamilyBuild.kind(var0) == Kind.STAFF) {
                        return FoodWeaponEvolution.WeaponRole.ARCANE;
                    } else if (var3 instanceof DiggerItem || var3 instanceof ShearsItem) {
                        return FoodWeaponEvolution.WeaponRole.TOOL;
                    } else if (!(var3 instanceof BowItem) && !(var3 instanceof CrossbowItem) && !(var3 instanceof TridentItem)) {
                        String var10 = var1.substring(Math.max(0, var1.indexOf(58) + 1)).toLowerCase(Locale.ROOT);
                        if (ftboss$legacyOrdinary$containsFoodWord(var10, "bow", "crossbow", "trident", "gun", "rifle", "pistol", "musket", "launcher")) {
                            return FoodWeaponEvolution.WeaponRole.RANGED;
                        } else if (ftboss$legacyOrdinary$containsFoodWord(var10, "staff", "wand", "scepter", "spellbook", "tome", "focus", "grimoire")) {
                            return FoodWeaponEvolution.WeaponRole.ARCANE;
                        } else if (ftboss$legacyOrdinary$containsFoodWord(var10, "pickaxe", "shovel", "hoe", "mattock", "drill", "saw", "wrench", "excavator")) {
                            return FoodWeaponEvolution.WeaponRole.TOOL;
                        } else if (ftboss$legacyOrdinary$containsFoodWord(var10, "shield", "bulwark", "tower", "guardian")) {
                            return FoodWeaponEvolution.WeaponRole.GUARDIAN;
                        } else if (ftboss$legacyOrdinary$containsFoodWord(var10, "dagger", "rapier", "claw", "knife", "katana", "sabre", "saber", "scimitar")) {
                            return FoodWeaponEvolution.WeaponRole.DUELIST;
                        } else if (ftboss$legacyOrdinary$containsFoodWord(var10, "hammer", "greataxe", "greatsword", "cleaver", "mace", "maul")) {
                            return FoodWeaponEvolution.WeaponRole.VANGUARD;
                        } else {
                            var var5 = var3.getDefaultAttributeModifiers(var0);
                            double var6 = NativeLegacyEvolutionItemData.defaultMainHandAmount(var5, Attributes.ATTACK_DAMAGE);
                            double var8 = NativeLegacyEvolutionItemData.defaultMainHandAmount(var5, Attributes.ATTACK_SPEED);
                            if (var6 >= 8.0 && var8 <= -2.7) {
                                return FoodWeaponEvolution.WeaponRole.VANGUARD;
                            } else {
                                return var8 >= -2.2 ? FoodWeaponEvolution.WeaponRole.DUELIST : FoodWeaponEvolution.WeaponRole.BALANCED;
                            }
                        }
                    } else {
                        return FoodWeaponEvolution.WeaponRole.RANGED;
                    }
                }
            }
        }

    private static CompoundTag ftboss$legacyOrdinary$writableData(ItemStack var0, FoodWeaponEvolution.Profile var1) {
            CompoundTag var2 = NativeLegacyEvolutionItemData.writable(var0, "FTBossFoodEvolution");
            if (var1 != null && !var2.contains("Profile", 8)) {
                var2.putString("Profile", var1.id);
                NativeLegacyEvolutionItemData.write(var0, "FTBossFoodEvolution", var2);
            }
    
            return var2;
        }

    public static int ftboss$legacyOrdinary$activeGourmetSkillIndex(ItemStack var0) {
            if (!ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0)) {
                return -1;
            } else {
                List var1 = ftboss$legacyOrdinary$gourmetSkills(var0);
                if (var1.isEmpty()) {
                    return -1;
                } else {
                    CompoundTag var2 = ftboss$legacyOrdinary$data(var0);
                    if (var2 != null && var2.contains("ActiveSkillIndex", 3)) {
                        int var3 = var2.getInt("ActiveSkillIndex");
                        if (var3 >= 0 && var3 < var1.size()) {
                            return var3;
                        }
                    }
    
                    FoodWeaponEvolution.Profile var5 = var2 == null ? null : FoodWeaponEvolution.Profile.byId(var2.getString("ActiveProfile"));
                    if (var5 != null) {
                        for (int var4 = var1.size() - 1; var4 >= 0; var4--) {
                            if (((FoodWeaponEvolution.AcquiredSkill)var1.get(var4)).profile == var5) {
                                return var4;
                            }
                        }
                    }
    
                    return var1.size() - 1;
                }
            }
        }

    private static FoodWeaponEvolution.Profile ftboss$legacyOrdinary$activeProfile(ItemStack var0) {
            if (!ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0)) {
                return ftboss$legacyOrdinary$boundProfile(var0);
            } else {
                List var1 = ftboss$legacyOrdinary$gourmetSkills(var0);
                int var2 = ftboss$legacyOrdinary$activeGourmetSkillIndex(var0);
                if (var2 >= 0 && var2 < var1.size()) {
                    return ((FoodWeaponEvolution.AcquiredSkill)var1.get(var2)).profile;
                } else {
                    CompoundTag var3 = ftboss$legacyOrdinary$data(var0);
                    FoodWeaponEvolution.Profile var4 = var3 == null ? null : FoodWeaponEvolution.Profile.byId(var3.getString("ActiveProfile"));
                    return var4 == null ? ftboss$legacyOrdinary$boundProfile(var0) : var4;
                }
            }
        }

    static void ftboss$legacyOrdinary$completeEvolution(ServerPlayer var0, FoodWeaponEvolution.Profile var1, ItemStack var2) {
            CompoundTag var3 = ftboss$legacyOrdinary$writableData(var2, var1);
            int var4 = Math.max(0, var3.getInt("Level"));
            int var5 = ftboss$legacyOrdinary$maxLevel(var2);
            if (var4 >= var5) {
                var0.displayClientMessage(Component.translatable("message.ftboss.evolution.maxed"), true);
            } else {
                int var6 = ftboss$legacyOrdinary$threshold(var2, var4);
                int var7 = Math.max(0, var3.getInt("Experience"));
                if (var7 < var6) {
                    var0.displayClientMessage(Component.translatable("message.ftboss.evolution.need_xp", new Object[]{var7, var6}), true);
                } else {
                    FoodWeaponRepair.invalidateForProgression(var2);
                    var3.putInt("Level", ++var4);
                    var3.remove("BlessingWashed");
                    NativeLegacyEvolutionItemData.write(var2, "FTBossFoodEvolution", var3);
                    boolean var8 = ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var2);
                    if (!var8 && var4 == 2) {
                        var3.putString("SecondProfile", var1.id);
                        NativeLegacyEvolutionItemData.write(var2, "FTBossFoodEvolution", var3);
                    }
    
                    FoodWeaponEvolution.EvolutionSkill var9 = null;
                    FoodWeaponEvolution.EvolutionStat var10 = null;
                    if (var8) {
                        var3.putString("ActiveProfile", var1.id);
                        NativeLegacyEvolutionItemData.write(var2, "FTBossFoodEvolution", var3);
                        var9 = ftboss$legacyOrdinary$drawGourmetSkill(var2, var1, var0.getRandom());
                        if (var9 != null) {
                            ListTag var11 = var3.getList("EvolutionSkills", 10);
                            CompoundTag var12 = new CompoundTag();
                            var12.putString("Profile", var1.id);
                            var12.putString("EvolutionSkill", var9.id);
                            var11.add(var12);
                            var3.put("EvolutionSkills", var11);
                            NativeLegacyEvolutionItemData.write(var2, "FTBossFoodEvolution", var3);
                            var3.putInt("ActiveSkillIndex", Math.max(0, ftboss$legacyOrdinary$gourmetSkills(var2).size() - 1));
                            NativeLegacyEvolutionItemData.write(var2, "FTBossFoodEvolution", var3);
                        }
    
                        var10 = FoodWeaponEvolution.EvolutionStat.random(ftboss$legacyOrdinary$weaponRole(var2), var0.getRandom());
                        CompoundTag var16 = var3.getCompound("EvolutionStats");
                        var16.putDouble(var10.id, Math.max(0.0, var16.getDouble(var10.id)) + var10.gain);
                        var3.put("EvolutionStats", var16);
                        NativeLegacyEvolutionItemData.write(var2, "FTBossFoodEvolution", var3);
                        GourmetEvolutionWeapon.animateEvolution(var0, var2);
                    }
    
                    ftboss$legacyOrdinary$playEvolutionSuccessParticles(var0, var8);
                    var0.serverLevel()
                        .playSound(
                            null, var0.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 0.75F + (float)Math.min(var4, 5) * 0.12F
                        );
                    Object var17;
                    if (var9 != null) {
                        var17 = var9.nameComponent();
                    } else {
                        Aspect var18 = Aspect.fromId(ftboss$legacyOrdinary$boundProfile(var2).id);
                        if (var4 != 1 && var18 != null) {
                            Aspect var13 = Aspect.fromId(var1.id);
                            Fusion var14 = WeaponEvolutionSkillPool.fusion(var18, var13);
                            var17 = var14 == null
                                ? Component.translatable("message.ftboss.evolution.native_skill_up")
                                : Component.translatable(
                                    "message.ftboss.evolution.fusion_unlock",
                                    new Object[]{Component.translatable(var14.primaryTranslationKey()), Component.translatable(var14.secondaryTranslationKey())}
                                );
                        } else {
                            var17 = var18 == null
                                ? Component.translatable("message.ftboss.evolution.native_skill_up")
                                : Component.translatable(var18.translationKey());
                        }
                    }
    
                    var0.sendSystemMessage(
                        Component.translatable("message.ftboss.evolution.evolved", new Object[]{var2.getHoverName(), var4, var17, ftboss$legacyOrdinary$socketCapacityInternal(var2)})
                            .withStyle(ChatFormatting.LIGHT_PURPLE)
                    );
                    if (var8 && var10 != null) {
                        var0.sendSystemMessage(
                            Component.translatable("message.ftboss.gourmet.stat_gained", new Object[]{var10.displayName(var10.gain)})
                                .withStyle(ChatFormatting.GOLD)
                        );
                    }
    
                    FTBossMod.LOGGER
                        .info(
                            "[FTBoss][食武进化] {} 的 {} 在 {} 石像进化到 {} 级, skill={}, stat={}, slots={}",
                            new Object[]{
                                var0.getName().getString(),
                                ftboss$legacyOrdinary$itemId(var2),
                                var1.id,
                                var4,
                                var9 == null ? "native+1" : var9.id,
                                var10 == null ? "none" : var10.id,
                                ftboss$legacyOrdinary$socketCapacityInternal(var2)
                            }
                        );
                }
            }
        }

    private static FoodWeaponEvolution.EvolutionSkill ftboss$legacyOrdinary$drawGourmetSkill(ItemStack var0, FoodWeaponEvolution.Profile var1, RandomSource var2) {
            List<FoodWeaponEvolution.AcquiredSkill> var3 = ftboss$legacyOrdinary$gourmetSkills(var0);
            ArrayList var4 = new ArrayList();
    
            for (FoodWeaponEvolution.EvolutionSkill var8 : FoodWeaponEvolution.LegacyOrdinaryProfile.byTint(var1.tint).pool) {
                if (var3.stream().noneMatch(var1x -> var1x.skill == var8)) {
                    var4.add(var8);
                }
            }
    
            if (var4.isEmpty()) {
                for (FoodWeaponEvolution.EvolutionSkill var12 : FoodWeaponEvolution.EvolutionSkill.values()) {
                    if (var3.stream().noneMatch(var1x -> var1x.skill == var12)) {
                        var4.add(var12);
                    }
                }
            }
    
            return var4.isEmpty() ? null : (FoodWeaponEvolution.EvolutionSkill)var4.get(var2.nextInt(var4.size()));
        }

    static FoodWeaponEvolution.EvolutionSkill ftboss$legacyOrdinary$drawStageSkill(FoodWeaponEvolution.Profile var0, Set<FoodWeaponEvolution.EvolutionSkill> var1, RandomSource var2) {
            ArrayList var3 = new ArrayList();
    
            for (FoodWeaponEvolution.EvolutionSkill var7 : FoodWeaponEvolution.LegacyOrdinaryProfile.byTint(var0.tint).pool) {
                if (!var1.contains(var7)) {
                    var3.add(var7);
                }
            }
    
            if (var3.isEmpty()) {
                for (FoodWeaponEvolution.EvolutionSkill var11 : FoodWeaponEvolution.EvolutionSkill.values()) {
                    if (!var1.contains(var11)) {
                        var3.add(var11);
                    }
                }
            }
    
            return var3.isEmpty() ? null : (FoodWeaponEvolution.EvolutionSkill)var3.get(var2.nextInt(var3.size()));
        }

    public static String ftboss$legacyOrdinary$firstProfileId(ItemStack var0) {
            FoodWeaponEvolution.Profile var1 = ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0) ? ftboss$legacyOrdinary$activeProfile(var0) : ftboss$legacyOrdinary$boundProfile(var0);
            return var1 == null ? "" : var1.id;
        }

    public static int ftboss$legacyOrdinary$foodQualityTier(ItemStack var0) {
            return ftboss$legacyOrdinary$foodTier(var0).level;
        }

    private static List<FoodWeaponEvolution.AcquiredSkill> ftboss$legacyOrdinary$gourmetSkills(ItemStack var0) {
            ArrayList var1 = new ArrayList();
            CompoundTag var2 = ftboss$legacyOrdinary$data(var0);
            if (var2 != null && var2.contains("EvolutionSkills", 9)) {
                ListTag var3 = var2.getList("EvolutionSkills", 10);
    
                for (int var4 = 0; var4 < var3.size() && var4 < 8; var4++) {
                    CompoundTag var5 = var3.getCompound(var4);
                    FoodWeaponEvolution.Profile var6 = FoodWeaponEvolution.Profile.byId(var5.getString("Profile"));
                    FoodWeaponEvolution.EvolutionSkill var7 = FoodWeaponEvolution.EvolutionSkill.byId(var5.getString("EvolutionSkill"));
                    if (var6 != null && var7 != null) {
                        var1.add(new FoodWeaponEvolution.AcquiredSkill(var6, var7));
                    }
                }
    
                return var1;
            } else {
                return var1;
            }
        }

    public static boolean ftboss$legacyOrdinary$matchesStatueFood(ItemStack var0, int var1) {
            FoodWeaponEvolution.Profile var2 = FoodWeaponEvolution.Profile.byTint(var1);
            return var2 != null && ftboss$legacyOrdinary$foodType(var0) == var2.food;
        }

    public static int ftboss$legacyOrdinary$maxEvolutionLevel(ItemStack var0) {
            return FourWeaponFoundationAdapter.applies(var0) ? 4 : ftboss$legacyOrdinary$maxLevel(var0);
        }

    public static int ftboss$legacyOrdinary$nextEvolutionRequirement(ItemStack var0) {
            int var1 = ftboss$legacyOrdinary$evolutionLevel(var0);
            int var2 = ftboss$legacyOrdinary$maxLevel(var0);
            return var1 >= var2 ? 0 : ftboss$legacyOrdinary$threshold(var0, var1);
        }

    private static void ftboss$legacyOrdinary$playEvolutionSuccessParticles(ServerPlayer var0, boolean var1) {
            ServerLevel var2 = var0.serverLevel();
            int var3 = var1 ? 16 : 12;
            double var4 = var1 ? 0.74 : 0.62;
            double var6 = var0.getY() + 0.95;
    
            for (int var8 = 0; var8 < var3; var8++) {
                double var9 = (Math.PI * 2) * (double)var8 / (double)var3;
                double var11 = var0.getX() + Math.cos(var9) * var4;
                double var13 = var0.getZ() + Math.sin(var9) * var4;
                double var15 = var6 + 0.12 * Math.sin(var9 * 2.0);
                var2.sendParticles(ParticleTypes.END_ROD, var11, var15, var13, 1, 0.015, 0.015, 0.015, 0.01);
            }
    
            var2.sendParticles(ParticleTypes.ENCHANT, var0.getX(), var0.getY() + 1.0, var0.getZ(), var1 ? 24 : 16, 0.32, 0.45, 0.32, 0.06);
        }

    public static String ftboss$legacyOrdinary$profileId(int var0) {
            FoodWeaponEvolution.Profile var1 = FoodWeaponEvolution.Profile.byTint(var0);
            return var1 == null ? "" : var1.id;
        }

    public static String ftboss$legacyOrdinary$repairProfileId(ItemStack var0) {
            FoodWeaponEvolution.Profile var1 = ftboss$legacyOrdinary$boundProfile(var0);
            return var1 == null ? "" : var1.id;
        }

    static String ftboss$legacyOrdinary$skillId(FoodWeaponEvolution.EvolutionSkill var0) {
            return var0.id;
        }

    private static enum LegacyOrdinaryProfile {
            SUGAR(
                "sugar",
                1,
                FoodWeaponEvolution.FoodType.SWEET,
                FoodWeaponEvolution.EvolutionSkill.FROST_BITE,
                FoodWeaponEvolution.EvolutionSkill.SUGAR_RUSH,
                FoodWeaponEvolution.EvolutionSkill.CANDY_GUARD,
                FoodWeaponEvolution.EvolutionSkill.GOLDEN_PEEL,
                FoodWeaponEvolution.EvolutionSkill.TEA_RECOVERY,
                FoodWeaponEvolution.EvolutionSkill.HIGH_VOLTAGE
            ),
            BUTCHER(
                "butcher",
                2,
                FoodWeaponEvolution.FoodType.MEAT,
                FoodWeaponEvolution.EvolutionSkill.BLOOD_FEAST,
                FoodWeaponEvolution.EvolutionSkill.CLEAVER_FORCE,
                FoodWeaponEvolution.EvolutionSkill.HEAVY_IMPACT,
                FoodWeaponEvolution.EvolutionSkill.SHEPHERD_MIGHT,
                FoodWeaponEvolution.EvolutionSkill.PLAGUE_BITE,
                FoodWeaponEvolution.EvolutionSkill.CORK_BURST
            ),
            SAYA(
                "saya",
                3,
                FoodWeaponEvolution.FoodType.TEA,
                FoodWeaponEvolution.EvolutionSkill.THORN_VENOM,
                FoodWeaponEvolution.EvolutionSkill.TEA_RECOVERY,
                FoodWeaponEvolution.EvolutionSkill.ROOTED_GRIP,
                FoodWeaponEvolution.EvolutionSkill.FERTILE_REGROWTH,
                FoodWeaponEvolution.EvolutionSkill.PRISMATIC_TOXIN,
                FoodWeaponEvolution.EvolutionSkill.BRINE_BITE
            ),
            WINE(
                "wine",
                4,
                FoodWeaponEvolution.FoodType.WINE,
                FoodWeaponEvolution.EvolutionSkill.VINTAGE_FURY,
                FoodWeaponEvolution.EvolutionSkill.TIPSY_GUARD,
                FoodWeaponEvolution.EvolutionSkill.CORK_BURST,
                FoodWeaponEvolution.EvolutionSkill.HIGH_VOLTAGE,
                FoodWeaponEvolution.EvolutionSkill.SCAVENGER_FEAST,
                FoodWeaponEvolution.EvolutionSkill.HEAVY_IMPACT
            ),
            RAT(
                "rat",
                5,
                FoodWeaponEvolution.FoodType.CHEESE,
                FoodWeaponEvolution.EvolutionSkill.CHEESE_FRENZY,
                FoodWeaponEvolution.EvolutionSkill.PLAGUE_BITE,
                FoodWeaponEvolution.EvolutionSkill.SCAVENGER_FEAST,
                FoodWeaponEvolution.EvolutionSkill.BLOOD_FEAST,
                FoodWeaponEvolution.EvolutionSkill.PRISMATIC_TOXIN,
                FoodWeaponEvolution.EvolutionSkill.GOLDEN_PEEL
            ),
            DEFECT(
                "defect",
                6,
                FoodWeaponEvolution.FoodType.ENERGY,
                FoodWeaponEvolution.EvolutionSkill.OVERLOAD_ARC,
                FoodWeaponEvolution.EvolutionSkill.HIGH_VOLTAGE,
                FoodWeaponEvolution.EvolutionSkill.MAGNETIC_LOCK,
                FoodWeaponEvolution.EvolutionSkill.VINTAGE_FURY,
                FoodWeaponEvolution.EvolutionSkill.CHEESE_FRENZY,
                FoodWeaponEvolution.EvolutionSkill.ROOTED_GRIP
            ),
            COMPOST(
                "compost",
                7,
                FoodWeaponEvolution.FoodType.VEGETABLE,
                FoodWeaponEvolution.EvolutionSkill.PRISMATIC_TOXIN,
                FoodWeaponEvolution.EvolutionSkill.RECYCLE_GUARD,
                FoodWeaponEvolution.EvolutionSkill.FERTILE_REGROWTH,
                FoodWeaponEvolution.EvolutionSkill.TEA_RECOVERY,
                FoodWeaponEvolution.EvolutionSkill.HONEYCRISP_REBIRTH,
                FoodWeaponEvolution.EvolutionSkill.PLAGUE_BITE
            ),
            APPLE(
                "apple",
                8,
                FoodWeaponEvolution.FoodType.APPLE,
                FoodWeaponEvolution.EvolutionSkill.HONEYCRISP_REBIRTH,
                FoodWeaponEvolution.EvolutionSkill.GOLDEN_PEEL,
                FoodWeaponEvolution.EvolutionSkill.ORCHARD_STRIKE,
                FoodWeaponEvolution.EvolutionSkill.FERTILE_REGROWTH,
                FoodWeaponEvolution.EvolutionSkill.CANDY_GUARD,
                FoodWeaponEvolution.EvolutionSkill.CLEAVER_FORCE
            ),
            TIDE(
                "tide",
                9,
                FoodWeaponEvolution.FoodType.FISH,
                FoodWeaponEvolution.EvolutionSkill.UNDERTOW,
                FoodWeaponEvolution.EvolutionSkill.BRINE_BITE,
                FoodWeaponEvolution.EvolutionSkill.TIDAL_RECOVERY,
                FoodWeaponEvolution.EvolutionSkill.ROOTED_GRIP,
                FoodWeaponEvolution.EvolutionSkill.TIPSY_GUARD,
                FoodWeaponEvolution.EvolutionSkill.PACK_FEAST
            ),
            BENGER(
                "benger",
                10,
                FoodWeaponEvolution.FoodType.MEAT,
                FoodWeaponEvolution.EvolutionSkill.SHEPHERD_MIGHT,
                FoodWeaponEvolution.EvolutionSkill.HUNTING_HOWL,
                FoodWeaponEvolution.EvolutionSkill.PACK_FEAST,
                FoodWeaponEvolution.EvolutionSkill.BLOOD_FEAST,
                FoodWeaponEvolution.EvolutionSkill.ORCHARD_STRIKE,
                FoodWeaponEvolution.EvolutionSkill.UNDERTOW
            );
    
            private final String id;
            private final int tint;
            private final FoodWeaponEvolution.FoodType food;
            private final FoodWeaponEvolution.EvolutionSkill[] pool;
    
            private LegacyOrdinaryProfile(String var3, int var4, FoodWeaponEvolution.FoodType var5, FoodWeaponEvolution.EvolutionSkill... var6) {
                this.id = var3;
                this.tint = var4;
                this.food = var5;
                this.pool = var6;
            }
    
            Component displayName() {
                return Component.translatable("profile.ftboss.evolution." + this.id);
            }
    
            static FoodWeaponEvolution.LegacyOrdinaryProfile byId(String var0) {
                for (FoodWeaponEvolution.LegacyOrdinaryProfile profile : values()) {
                    if (profile.id.equals(var0)) {
                        return profile;
                    }
                }
    
                return null;
            }
    
            static FoodWeaponEvolution.LegacyOrdinaryProfile byTint(int var0) {
                for (FoodWeaponEvolution.LegacyOrdinaryProfile profile : values()) {
                    if (profile.tint == var0) {
                        return profile;
                    }
                }
    
                return null;
            }
        }

    private static final ResourceLocation ftboss$legacyOrdinary$EVOLUTION_DAMAGE = ResourceLocation.withDefaultNamespace("4c678802-b15b-4a2a-b399-d6559081bd1f");
    private static final ResourceLocation ftboss$legacyOrdinary$EVOLUTION_SPEED = ResourceLocation.withDefaultNamespace("2ae62943-4400-4296-ad06-efec0d0e98a9");
    private static final ResourceLocation ftboss$legacyOrdinary$SOCKET_ARMOR = ResourceLocation.withDefaultNamespace("be787aed-adb4-46ec-a466-f64dd529a11c");
    private static final ResourceLocation ftboss$legacyOrdinary$SOCKET_TOUGHNESS = ResourceLocation.withDefaultNamespace("c5ddd11c-b0db-4503-ab92-b53f0302809f");
    private static final ResourceLocation ftboss$legacyOrdinary$SOCKET_HEALTH = ResourceLocation.withDefaultNamespace("b14aec63-216e-46d9-8c31-b66495b2f087");
    private static final ResourceLocation ftboss$legacyOrdinary$SOCKET_MOVEMENT = ResourceLocation.withDefaultNamespace("80a6b7ea-22cd-4c99-a32e-e825d83f3fe7");
    private static final ResourceLocation ftboss$legacyOrdinary$SOCKET_LUCK = ResourceLocation.withDefaultNamespace("13fb758d-c241-4071-a7d4-a8e0f74e6258");

    public static void ftboss$legacyOrdinary$attributes(ItemAttributeModifierEvent var0) {
            {
                ItemStack var1 = var0.getItemStack();
                if (ftboss$legacyOrdinary$boundProfile(var1) != null && !FoodWeaponLifecycle.isBroken(var1)) {
                    ftboss$legacyOrdinary$addFoodBlessingAttributes(var0, var1);
                    boolean var2 = ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var1);
                    int var3 = ftboss$legacyOrdinary$evolutionLevel(var1);
                    FoodWeaponEvolution.WeaponRole var4 = ftboss$legacyOrdinary$weaponRole(var1);
                    FoodWeaponEvolution.SocketStats var5 = ftboss$legacyOrdinary$socketStats(var1);
                    double var6 = var5.effectPower(FoodWeaponEvolution.FoodType.MEAT, var4);
                    double var8 = var5.effectPower(FoodWeaponEvolution.FoodType.WINE, var4);
                    double var10 = var5.effectPower(FoodWeaponEvolution.FoodType.ENERGY, var4);
                    double var12 = var5.effectPower(FoodWeaponEvolution.FoodType.TEA, var4);
                    FoodWeaponEvolution.Profile var14 = ftboss$legacyOrdinary$activeProfile(var1);
                    double var15 = var14 == null ? 0.0 : var5.effectPower(var14.food, var4);
                    double var17 = var5.effectPower(FoodWeaponEvolution.FoodType.CHEESE, var4);
                    double var19 = var5.effectPower(FoodWeaponEvolution.FoodType.VEGETABLE, var4);
                    double var21 = var5.effectPower(FoodWeaponEvolution.FoodType.APPLE, var4);
                    double var23 = var5.effectPower(FoodWeaponEvolution.FoodType.FISH, var4);
                    double var25 = var5.effectPower(FoodWeaponEvolution.FoodType.SWEET, var4) + var5.effectPower(FoodWeaponEvolution.FoodType.FOOD, var4);
                    double var27 = ftboss$legacyOrdinary$evolutionDamageBonus(var4, var3, var2)
                        + (var2 ? ftboss$legacyOrdinary$gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.ATTACK_DAMAGE) : 0.0)
                        + var6
                        + var8 * 0.5
                        + var10 * 0.5
                        + var15 * 0.5;
                    double var29 = ftboss$legacyOrdinary$evolutionSpeedBonus(var4, var3, var2)
                        + (var2 ? ftboss$legacyOrdinary$gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.ATTACK_SPEED) : 0.0)
                        + var12 * 0.05
                        + var10 * 0.02;
                    double var31 = ftboss$legacyOrdinary$evolutionArmorBonus(var4, var3, var2) + var17 + (var2 ? ftboss$legacyOrdinary$gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.ARMOR) : 0.0);
                    double var33 = ftboss$legacyOrdinary$evolutionHealthBonus(var4, var3, var2)
                        + var21 * 2.0
                        + (var2 ? ftboss$legacyOrdinary$gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.MAX_HEALTH) : 0.0);
                    double var35 = ftboss$legacyOrdinary$evolutionMovementBonus(var4, var3, var2)
                        + var23 * 0.03
                        + (var2 ? ftboss$legacyOrdinary$gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.MOVEMENT_SPEED) : 0.0);
                    double var37 = ftboss$legacyOrdinary$evolutionLuckBonus(var4, var3, var2) + var25 + (var2 ? ftboss$legacyOrdinary$gourmetStat(var1, FoodWeaponEvolution.EvolutionStat.LUCK) : 0.0);
                    if (var27 > 0.0) {
                        var0.addModifier(
                            Attributes.ATTACK_DAMAGE, new AttributeModifier(ftboss$legacyOrdinary$EVOLUTION_DAMAGE, var27, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
                        );
                    }
    
                    if (var29 > 0.0) {
                        var0.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(ftboss$legacyOrdinary$EVOLUTION_SPEED, var29, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
                    }
    
                    if (var31 > 0.0) {
                        var0.addModifier(Attributes.ARMOR, new AttributeModifier(ftboss$legacyOrdinary$SOCKET_ARMOR, var31, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
                    }
    
                    if (var19 > 0.0) {
                        var0.addModifier(
                            Attributes.ARMOR_TOUGHNESS, new AttributeModifier(ftboss$legacyOrdinary$SOCKET_TOUGHNESS, var19 * 0.5, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
                        );
                    }
    
                    if (var33 > 0.0) {
                        var0.addModifier(Attributes.MAX_HEALTH, new AttributeModifier(ftboss$legacyOrdinary$SOCKET_HEALTH, var33, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
                    }
    
                    if (var35 > 0.0) {
                        var0.addModifier(
                            Attributes.MOVEMENT_SPEED, new AttributeModifier(ftboss$legacyOrdinary$SOCKET_MOVEMENT, var35, Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND
                        );
                    }
    
                    if (var37 > 0.0) {
                        var0.addModifier(Attributes.LUCK, new AttributeModifier(ftboss$legacyOrdinary$SOCKET_LUCK, var37, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
                    }
                }
            }
        }

    private static void ftboss$legacyOrdinary$addFoodBlessingAttributes(ItemAttributeModifierEvent var0, ItemStack var1) {
            ListTag var2 = ftboss$legacyOrdinary$foodBlessingList(var1);
    
            for (int var3 = 0; var3 < var2.size(); var3++) {
                CompoundTag var4 = var2.getCompound(var3);
                FoodWeaponEvolution.FoodBlessing var5 = FoodWeaponEvolution.FoodBlessing.byId(var4.getString("Blessing"));
                if (var5 != null) {
                    int var6 = ftboss$legacyOrdinary$foodBlessingLevel(var4);
    
                    double var7 = switch (var5.kind) {
                        case ATTACK_DAMAGE -> 0.75 * (double)var6;
                        case ATTACK_SPEED -> 0.04 * (double)var6;
                        case MAX_HEALTH -> 1.5 * (double)var6;
                        case MOVEMENT_SPEED -> 0.012 * (double)var6;
                        case KNOCKBACK_RESISTANCE -> 0.08 * (double)var6;
                        default -> 0.0;
                    };
                    if (!(var7 <= 0.0)) {
                        Operation var9 = var5.kind != FoodWeaponEvolution.FoodBlessingKind.MOVEMENT_SPEED
                                && var5.kind != FoodWeaponEvolution.FoodBlessingKind.KNOCKBACK_RESISTANCE
                            ? Operation.ADD_VALUE
                            : Operation.ADD_MULTIPLIED_BASE;
    
                        Holder<net.minecraft.world.entity.ai.attributes.Attribute> var10 = switch (var5.kind) {
                            case ATTACK_DAMAGE -> Attributes.ATTACK_DAMAGE;
                            case ATTACK_SPEED -> Attributes.ATTACK_SPEED;
                            case MAX_HEALTH -> Attributes.MAX_HEALTH;
                            case MOVEMENT_SPEED -> Attributes.MOVEMENT_SPEED;
                            case KNOCKBACK_RESISTANCE -> Attributes.KNOCKBACK_RESISTANCE;
                            default -> null;
                        };
                        if (var10 != null) {
                            var0.addModifier(var10, new AttributeModifier(ResourceLocation.withDefaultNamespace(var5.modifierId.toString()), var7, var9), EquipmentSlotGroup.MAINHAND);
                        }
                    }
                }
            }
        }

    private static int ftboss$legacyOrdinary$foodBlessingLevel(CompoundTag var0) {
            return Math.max(1, Math.min(3, var0.getInt("Level")));
        }

    private static double ftboss$legacyOrdinary$gourmetStat(ItemStack var0, FoodWeaponEvolution.EvolutionStat var1) {
            CompoundTag var2 = ftboss$legacyOrdinary$data(var0);
            return var2 != null && var2.contains("EvolutionStats", 10) ? Math.max(0.0, var2.getCompound("EvolutionStats").getDouble(var1.id)) : 0.0;
        }

    private static double ftboss$legacyOrdinary$evolutionDamageBonus(FoodWeaponEvolution.WeaponRole var0, int var1, boolean var2) {
            double var10000;
            if (var2) {
                switch (var0) {
                    case VANGUARD:
                        var10000 = 0.75;
                        break;
                    case DUELIST:
                        var10000 = 0.3;
                        break;
                    case RANGED:
                    case ARCANE:
                    case TOOL:
                        var10000 = 0.0;
                        break;
                    case GUARDIAN:
                        var10000 = 0.25;
                        break;
                    case BALANCED:
                        var10000 = 0.5;
                        break;
                    default:
                        throw new IncompatibleClassChangeError();
                }
            } else {
                switch (var0) {
                    case VANGUARD:
                        var10000 = 3.0;
                        break;
                    case DUELIST:
                        var10000 = 1.25;
                        break;
                    case RANGED:
                    case ARCANE:
                    case TOOL:
                        var10000 = 0.0;
                        break;
                    case GUARDIAN:
                        var10000 = 1.0;
                        break;
                    case BALANCED:
                        var10000 = 2.0;
                        break;
                    default:
                        throw new IncompatibleClassChangeError();
                }
            }
    
            double var3 = var10000;
            return (double)var1 * var3;
        }

    private static double ftboss$legacyOrdinary$evolutionSpeedBonus(FoodWeaponEvolution.WeaponRole var0, int var1, boolean var2) {
            double var10000;
            if (var2) {
                switch (var0) {
                    case VANGUARD:
                        var10000 = 0.01;
                        break;
                    case DUELIST:
                        var10000 = 0.025;
                        break;
                    case BALANCED:
                        var10000 = 0.02;
                        break;
                    default:
                        var10000 = 0.0;
                }
            } else {
                switch (var0) {
                    case VANGUARD:
                        var10000 = 0.04;
                        break;
                    case DUELIST:
                        var10000 = 0.14;
                        break;
                    case BALANCED:
                        var10000 = 0.08;
                        break;
                    default:
                        var10000 = 0.0;
                }
            }
    
            double var3 = var10000;
            return (double)var1 * var3;
        }

    private static double ftboss$legacyOrdinary$evolutionArmorBonus(FoodWeaponEvolution.WeaponRole var0, int var1, boolean var2) {
            return var0 == FoodWeaponEvolution.WeaponRole.GUARDIAN ? (double)var1 * (var2 ? 0.4 : 1.5) : 0.0;
        }

    private static double ftboss$legacyOrdinary$evolutionHealthBonus(FoodWeaponEvolution.WeaponRole var0, int var1, boolean var2) {
            return var0 == FoodWeaponEvolution.WeaponRole.GUARDIAN ? (double)var1 * (var2 ? 0.5 : 2.0) : 0.0;
        }

    private static double ftboss$legacyOrdinary$evolutionMovementBonus(FoodWeaponEvolution.WeaponRole var0, int var1, boolean var2) {
            if (var0 == FoodWeaponEvolution.WeaponRole.DUELIST) {
                return (double)var1 * (var2 ? 0.004 : 0.015);
            } else {
                return var0 == FoodWeaponEvolution.WeaponRole.RANGED ? (double)var1 * (var2 ? 0.003 : 0.01) : 0.0;
            }
        }

    private static double ftboss$legacyOrdinary$evolutionLuckBonus(FoodWeaponEvolution.WeaponRole var0, int var1, boolean var2) {
            return var0 == FoodWeaponEvolution.WeaponRole.ARCANE ? (double)var1 * (var2 ? 0.15 : 0.5) : 0.0;
        }

    public static float ftboss$legacyOrdinary$activeSkillMultiplier(ItemStack var0) {
            if (ftboss$legacyOrdinary$isEvolutionExcluded(var0)) {
                return 1.0F;
            } else {
                boolean var1 = ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0);
                FoodWeaponEvolution.WeaponRole var2 = ftboss$legacyOrdinary$weaponRole(var0);
                float var3 = (float)ftboss$legacyOrdinary$evolutionLevel(var0) * var2.skillPowerPerLevel(var1);
                float var4 = 0.0F;
                if (var2 == FoodWeaponEvolution.WeaponRole.ARCANE) {
                    FoodWeaponEvolution.SocketStats var5 = ftboss$legacyOrdinary$socketStats(var0);
                    var4 = (float)Math.min(
                        0.4,
                        (
                                var5.effectPower(FoodWeaponEvolution.FoodType.TEA, var2)
                                    + var5.effectPower(FoodWeaponEvolution.FoodType.SWEET, var2)
                                    + var5.effectPower(FoodWeaponEvolution.FoodType.FOOD, var2)
                                    + var5.effectPower(FoodWeaponEvolution.FoodType.ENERGY, var2)
                            )
                            * 0.015
                    );
                }
    
                float var6 = 1.0F + var3 + var4;
                if (var2 == FoodWeaponEvolution.WeaponRole.ARCANE || var2 == FoodWeaponEvolution.WeaponRole.RANGED) {
                    var6 *= (float)WeaponMaterialBuild.build(var0).damage();
                }
    
                return var6;
            }
        }

    public static List<FoodWeaponEvolution.FoodBlessingView> ftboss$legacyOrdinary$foodBlessingViews(ItemStack var0) {
            ArrayList var1 = new ArrayList();
            ArrayList var2 = new ArrayList();
            ListTag var3 = ftboss$legacyOrdinary$foodBlessingList(var0);
    
            for (int var4 = 0; var4 < var3.size(); var4++) {
                CompoundTag var5 = var3.getCompound(var4);
                FoodWeaponEvolution.FoodBlessing var6 = FoodWeaponEvolution.FoodBlessing.byId(var5.getString("Blessing"));
                if (var6 != null && !var2.contains(var6.id)) {
                    var2.add(var6.id);
                    int var7 = ftboss$legacyOrdinary$foodBlessingLevel(var5);
                    var1.add(new FoodWeaponEvolution.FoodBlessingView(var6.nameComponent(), var6.descriptionComponent(var7), ftboss$legacyOrdinary$toRoman(var7)));
                }
            }
    
            return List.copyOf(var1);
        }

    public static FoodWeaponEvolution.SocketView ftboss$legacyOrdinary$ingredientView(ItemStack var0) {
            FoodWeaponEvolution.FoodType var1 = ftboss$legacyOrdinary$foodType(var0);
            if (var1 == null) {
                return null;
            } else {
                FoodWeaponEvolution.SocketTier var2 = ftboss$legacyOrdinary$foodTier(var0);
                return new FoodWeaponEvolution.SocketView(
                    var0.copy(), var1.displayName(), var2.displayName(), var1.effectDescription(), var1.setDescription(), ftboss$legacyOrdinary$socketColor(var1), false
                );
            }
        }

    public static boolean ftboss$legacyOrdinary$isBlessingCandidate(ItemStack var0, ItemStack var1, int var2) {
            FoodWeaponEvolution.Profile var3 = FoodWeaponEvolution.Profile.byTint(var2);
            FoodWeaponEvolution.Profile var4 = ftboss$legacyOrdinary$boundProfile(var0);
            FoodWeaponEvolution.FoodType var5 = ftboss$legacyOrdinary$foodType(var1);
            return var3 != null
                && var4 != null
                && !var1.isEmpty()
                && !var0.is(Items.ENCHANTED_BOOK)
                && var1 != var0
                && !ftboss$legacyOrdinary$weaponCandidate(var1)
                && var5 == var3.food
                && ftboss$legacyOrdinary$foodBlessingList(var0).size() < 6;
        }

    public static boolean ftboss$legacyOrdinary$isDailySubmissionCandidate(List<FoodWeaponEvolution.StatueDailyView> var0, ItemStack var1, int var2) {
            FoodWeaponEvolution.FoodType var3 = ftboss$legacyOrdinary$foodType(var1);
            FoodWeaponEvolution.Profile var4 = FoodWeaponEvolution.Profile.byTint(var2);
            if (!var1.isEmpty() && var3 != null && var4 != null) {
                for (FoodWeaponEvolution.StatueDailyView var6 : var0) {
                    if (var6.state() == 0 && (var6.requestItemId().isBlank() ? var3 == var4.food : var6.requestItemId().equals(ftboss$legacyOrdinary$itemId(var1)))) {
                        return true;
                    }
                }
    
                return false;
            } else {
                return false;
            }
        }

    public static boolean ftboss$legacyOrdinary$isSocketCandidate(ItemStack var0, ItemStack var1, int var2) {
            FoodWeaponEvolution.Profile var3 = FoodWeaponEvolution.Profile.byTint(var2);
            FoodWeaponEvolution.Profile var4 = ftboss$legacyOrdinary$boundProfile(var0);
            return var3 != null && var4 != null && !var1.isEmpty() && ftboss$legacyOrdinary$foodType(var1) != null && ftboss$legacyOrdinary$sockets(var0).size() < ftboss$legacyOrdinary$unlockedSocketCapacity(var0);
        }

    public static String ftboss$legacyOrdinary$secondProfileId(ItemStack var0) {
            if (ftboss$legacyOrdinary$isEvolutionExcluded(var0)) {
                return "";
            } else {
                CompoundTag var1 = ftboss$legacyOrdinary$data(var0);
                return FoodWeaponProgression.ftboss$legacyOrdinary$secondProfile(var0, var1 == null ? "" : var1.getString("SecondProfile"));
            }
        }

    private static int ftboss$legacyOrdinary$socketColor(FoodWeaponEvolution.FoodType var0) {
            return switch (var0) {
                case MEAT -> 13777476;
                case CHEESE -> 16239937;
                case TEA -> 4243050;
                case WINE -> 9387702;
                case SWEET -> 16738495;
                case APPLE -> 14891583;
                case FISH -> 4116466;
                case VEGETABLE -> 5883723;
                case ENERGY -> 16748078;
                case FOOD -> 15658718;
            };
        }

    private static ItemStack ftboss$legacyOrdinary$socketStack(FoodWeaponEvolution.Socket var0) {
            ResourceLocation var1 = ResourceLocation.tryParse(var0.item);
            Item var2 = var1 == null ? null : (Item)BuiltInRegistries.ITEM.get(var1);
            return var2 != null && var2 != Items.AIR ? new ItemStack(var2) : ItemStack.EMPTY;
        }

    public static List<FoodWeaponEvolution.SocketView> ftboss$legacyOrdinary$socketViews(ItemStack var0) {
            ArrayList var1 = new ArrayList();
            FoodWeaponEvolution.Profile var2 = ftboss$legacyOrdinary$activeProfile(var0);
    
            for (FoodWeaponEvolution.Socket var4 : ftboss$legacyOrdinary$sockets(var0)) {
                ItemStack var5 = ftboss$legacyOrdinary$socketStack(var4);
                int var6 = ftboss$legacyOrdinary$socketColor(var4.type);
                var1.add(
                    new FoodWeaponEvolution.SocketView(
                        var5,
                        var4.type.displayName(),
                        var4.tier.displayName(),
                        var4.type.effectDescription(),
                        var4.type.setDescription(),
                        var6,
                        var2 != null && var4.type == var2.food
                    )
                );
            }
    
            return List.copyOf(var1);
        }

    public static List<FoodWeaponEvolution.StatueDailyView> ftboss$legacyOrdinary$statueDailyViews(ServerPlayer var0, int var1) {
            FoodWeaponEvolution.Profile var2 = FoodWeaponEvolution.Profile.byTint(var1);
            if (var2 == null) {
                return List.of();
            } else {
                CompoundTag var3 = ftboss$legacyOrdinary$daily(var0, var2);
                ArrayList var4 = new ArrayList(12);
    
                for (int var5 = 0; var5 < 12; var5++) {
                    CompoundTag var6 = var3.getCompound(ftboss$legacyOrdinary$taskKey(var5));
                    int var7 = Math.max(1, var6.getInt("Required"));
                    int var8 = Math.min(var7, Math.max(0, var6.getInt("Progress")));
                    int var9 = var6.getBoolean("Claimed") ? 2 : (var6.getBoolean("Ready") ? 1 : 0);
                    ResourceLocation var10 = ftboss$legacyOrdinary$dailyRequestId(var6);
                    var4.add(
                        new FoodWeaponEvolution.StatueDailyView(
                            ftboss$legacyOrdinary$dailyRequestName(var6, var2), var10 == null ? "" : var10.toString(), var8, var7, ftboss$legacyOrdinary$dailyRewardName(var6), var9
                        )
                    );
                }
    
                return List.copyOf(var4);
            }
        }

    public static List<FoodWeaponEvolution.WeaponDetailView> ftboss$legacyOrdinary$weaponDetailViews(ItemStack var0) {
            FoodWeaponEvolution.Profile var1 = ftboss$legacyOrdinary$boundProfile(var0);
            if (var1 == null) {
                return List.of();
            } else {
                ArrayList var2 = new ArrayList();
                FoodWeaponEvolution.WeaponRole var3 = ftboss$legacyOrdinary$weaponRole(var0);
                var2.add(
                    new FoodWeaponEvolution.WeaponDetailView(
                        Component.translatable("screen.ftboss.statue_hub.details.category.role"), var3.displayName(), var3.bonusDescription()
                    )
                );
                CompoundTag var4 = ftboss$legacyOrdinary$data(var0);
                int var5 = ftboss$legacyOrdinary$evolutionLevel(var0);
                boolean var6 = ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0);
                if (ftboss$legacyOrdinary$nativeProfile(var0) != null) {
                    var2.add(
                        new FoodWeaponEvolution.WeaponDetailView(
                            Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                            Component.translatable(
                                "tooltip.ftboss.evolution.native_skill", new Object[]{var5 + 1, Math.round((ftboss$legacyOrdinary$activeSkillMultiplier(var0) - 1.0F) * 100.0F)}
                            ),
                            Component.translatable("screen.ftboss.statue_hub.details.native_skill_help")
                        )
                    );
                } else if (var6) {
                    List var7 = ftboss$legacyOrdinary$gourmetSkills(var0);
                    int var8 = ftboss$legacyOrdinary$activeGourmetSkillIndex(var0);
    
                    for (int var9 = 0; var9 < var7.size(); var9++) {
                        FoodWeaponEvolution.AcquiredSkill var10 = (FoodWeaponEvolution.AcquiredSkill)var7.get(var9);
                        Aspect var11 = Aspect.fromId(var10.profile.id);
                        Object var12 = var11 == null ? var10.profile.displayName() : Component.translatable(var11.translationKey());
                        boolean var13 = var9 == var8;
                        var2.add(
                            new FoodWeaponEvolution.WeaponDetailView(
                                Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                                Component.translatable(
                                    "tooltip.ftboss.gourmet.skill_line", new Object[]{var9 + 1, var10.profile.displayName(), var10.skill.nameComponent()}
                                ),
                                Component.translatable(
                                    var13 ? "screen.ftboss.statue_hub.details.gourmet_skill_selected" : "screen.ftboss.statue_hub.details.gourmet_skill_switch",
                                    new Object[]{var12, Component.translatable(var10.skill.effect.key())}
                                ),
                                var9,
                                var13
                            )
                        );
                    }
                } else if (var5 > 0 && var4 != null) {
                    FoodWeaponEvolution.EvolutionSkill var14 = FoodWeaponEvolution.EvolutionSkill.byId(var4.getString("EvolutionSkill"));
                    if (var14 != null) {
                        var2.add(
                            new FoodWeaponEvolution.WeaponDetailView(
                                Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                                Component.translatable("tooltip.ftboss.evolution.drawn_skill", new Object[]{var14.nameComponent()}),
                                Component.translatable(var14.effect.key())
                            )
                        );
                    }
                }
    
                if (var5 > 0) {
                    Aspect var15 = Aspect.fromId(ftboss$legacyOrdinary$firstProfileId(var0));
                    if (var6 && var15 != null) {
                        var2.add(
                            new FoodWeaponEvolution.WeaponDetailView(
                                Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                                Component.translatable("tooltip.ftboss.gourmet.active", new Object[]{Component.translatable(var15.translationKey())}),
                                Component.translatable(var15.descriptionKey())
                            )
                        );
                    } else if (var15 != null && FoodWeaponProgression.ftboss$legacyOrdinary$activeStage(var0) == 1) {
                        var2.add(
                            new FoodWeaponEvolution.WeaponDetailView(
                                Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                                Component.translatable("tooltip.ftboss.evolution.active_primary", new Object[]{Component.translatable(var15.translationKey())}),
                                Component.translatable(var15.descriptionKey())
                            )
                        );
                    } else if (var15 != null) {
                        Aspect var19 = Aspect.fromId(ftboss$legacyOrdinary$secondProfileId(var0));
                        if (var19 == null) {
                            var19 = var15;
                        }
    
                        Fusion var23 = WeaponEvolutionSkillPool.fusion(var15, var19);
                        if (var23 != null) {
                            var2.add(
                                new FoodWeaponEvolution.WeaponDetailView(
                                    Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                                    Component.translatable(
                                        "tooltip.ftboss.evolution.active_primary", new Object[]{Component.translatable(var23.primaryTranslationKey())}
                                    ),
                                    Component.translatable(var23.styleDescriptionKey())
                                )
                            );
                            var2.add(
                                new FoodWeaponEvolution.WeaponDetailView(
                                    Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                                    Component.translatable(
                                        "tooltip.ftboss.evolution.active_secondary", new Object[]{Component.translatable(var23.secondaryTranslationKey())}
                                    ),
                                    Component.translatable(var23.styleDescriptionKey())
                                )
                            );
                        }
                    }
                }
    
                if (var6) {
                    for (FoodWeaponEvolution.EvolutionStat var26 : FoodWeaponEvolution.EvolutionStat.values()) {
                        double var27 = ftboss$legacyOrdinary$gourmetStat(var0, var26);
                        if (!(var27 <= 0.0)) {
                            var2.add(
                                new FoodWeaponEvolution.WeaponDetailView(
                                    Component.translatable("screen.ftboss.statue_hub.details.category.stat"),
                                    var26.displayName(var27),
                                    Component.translatable("screen.ftboss.statue_hub.details.stat_help")
                                )
                            );
                        }
                    }
                }
    
                for (FoodWeaponEvolution.FoodBlessingView var21 : ftboss$legacyOrdinary$foodBlessingViews(var0)) {
                    var2.add(
                        new FoodWeaponEvolution.WeaponDetailView(
                            Component.translatable("screen.ftboss.statue_hub.details.category.blessing"),
                            var21.name().copy().append(Component.literal(" " + var21.rank())),
                            var21.description()
                        )
                    );
                }
    
                List var18 = ftboss$legacyOrdinary$socketViews(var0);
    
                for (int var22 = 0; var22 < var18.size(); var22++) {
                    FoodWeaponEvolution.SocketView var25 = (FoodWeaponEvolution.SocketView)var18.get(var22);
                    var2.add(
                        new FoodWeaponEvolution.WeaponDetailView(
                            Component.translatable("screen.ftboss.statue_hub.details.category.socket"),
                            Component.literal("#" + (var22 + 1) + " ")
                                .append(var25.stack().getHoverName())
                                .append(Component.literal(" · "))
                                .append(var25.type())
                                .append(Component.literal(" · "))
                                .append(var25.tier()),
                            var25.effect().copy().append(Component.literal(" · ")).append(var25.synergy())
                        )
                    );
                }
    
                var2.addAll(FoodWeaponProgression.ftboss$legacyOrdinary$details(var0));
                var2.addAll(FoodSocketPowers.details(var0));
                return List.copyOf(var2);
            }
        }

    static Component ftboss$legacyOrdinary$skillDescription(FoodWeaponEvolution.EvolutionSkill var0) {
            return Component.translatable(var0.effect.key());
        }
    private static java.util.Set<String> detachedTimerKeys(CompoundTag timers) {
        return timers.copy().getAllKeys();
    }
}
