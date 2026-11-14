package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.FoodBlessingView;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable.Result;
import net.neoforged.fml.common.EventBusSubscriber;

public final class ArmorFoodBlessings {
    private static final String ROOT = "FTBossArmorBlessings";
    private static final String LIST = "FoodBlessings";
    private static final String TIMER = "FTBossArmorBlessing_";
    private static final int CAPACITY = 6;
    private static final TagKey<DamageType> MAGIC_DAMAGE = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("ftboss", "is_magic"));
    private static final TagKey<DamageType> POISON_DAMAGE = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("ftboss", "is_poison"));

    private ArmorFoodBlessings() {
    }

    public static boolean eligible(ItemStack var0) {
        return false;
    }

    public static ItemStack heldArmor(Player var0) {
        if (eligible(var0.getMainHandItem())) {
            return var0.getMainHandItem();
        } else {
            return eligible(var0.getOffhandItem()) ? var0.getOffhandItem() : ItemStack.EMPTY;
        }
    }

    private static ListTag entries(ItemStack var0) {
        return new ListTag();
    }

    private static Map<String, ArmorFoodBlessings.Active> snapshot(ItemStack var0) {
        LinkedHashMap<String, ArmorFoodBlessings.Active> var1 = new LinkedHashMap<>();
        if (!eligible(var0)) {
            return var1;
        } else {
            ListTag var2 = entries(var0);

            for (int var3 = 0; var3 < Math.min(6, var2.size()); var3++) {
                CompoundTag var4 = var2.getCompound(var3);
                ArmorBlessingCatalog.Definition var5 = ArmorBlessingCatalog.byId(var4.getString("Blessing"));
                if (var5 != null && var5.profile().equals(var4.getString("Profile"))) {
                    int var6 = ArmorBlessingCatalog.clampLevel(var4.getInt("Level"));
                    ArmorFoodBlessings.Active var7 = (ArmorFoodBlessings.Active)var1.get(var5.id());
                    if (var7 == null || var7.level() < var6) {
                        var1.put(var5.id(), new ArmorFoodBlessings.Active(var5, var6));
                    }
                }
            }

            return var1;
        }
    }

    public static boolean hasRoom(ItemStack var0, String var1) {
        return false;
    }

    public static boolean apply(ServerPlayer var0, String var1, ItemStack var2, ItemStack var3, int var4) {
        return false;
    }

    private static String roman(int var0) {
        return var0 == 1 ? "I" : (var0 == 2 ? "II" : "III");
    }

    public static List<FoodBlessingView> views(ItemStack var0) {
        return List.of();
    }

    private static Map<String, ArmorFoodBlessings.Active> worn(ServerPlayer var0) {
        LinkedHashMap<String, ArmorFoodBlessings.Active> var1 = new LinkedHashMap<>();

        for (EquipmentSlot var5 : EquipmentSlot.values()) {
            if (var5.getType() == Type.HUMANOID_ARMOR) {
                ItemStack var6 = var0.getItemBySlot(var5);
                if (eligible(var6) && ((ArmorItem)var6.getItem()).getEquipmentSlot() == var5) {
                    for (ArmorFoodBlessings.Active var8 : snapshot(var6).values()) {
                        var1.merge(var8.definition().id(), var8, (var0x, var1x) -> (ArmorFoodBlessings.Active)(var0x.level() >= var1x.level() ? var0x : var1x));
                    }
                }
            }
        }

        return var1;
    }

    @SubscribeEvent
    public static void attributes(ItemAttributeModifierEvent var0) {
        
    }

    private static ArmorBlessingCatalog.Context context(ServerPlayer var0, DamageSource var1, float var2) {
        return new ArmorBlessingCatalog.Context(
            (double)var2 / Math.max(1.0, (double)var0.getMaxHealth()),
            var0.getFoodData().getFoodLevel(),
            var0.isCrouching(),
            var0.isOnFire(),
            var1 != null && var1.is(DamageTypeTags.IS_PROJECTILE),
            var0.onGround(),
            var0.isInWaterRainOrBubble(),
            var0.isSprinting(),
            var0.isUnderWater(),
            var0.level().isNight(),
            var1 != null && var1.getDirectEntity() instanceof LivingEntity var3 && var3.isAlive() && var3 != var0,
            var1 != null && (var1.is(DamageTypes.MAGIC) || var1.is(DamageTypes.INDIRECT_MAGIC) || var1.is(MAGIC_DAMAGE)),
            var1 != null && var1.is(DamageTypeTags.IS_FALL),
            var1 != null && var1.is(DamageTypeTags.IS_FIRE),
            var1 != null && var1.is(DamageTypeTags.IS_EXPLOSION),
            var0.hasEffect(MobEffects.POISON),
            var1 != null && var1.is(DamageTypeTags.IS_FREEZING),
            var1 != null && var1.is(POISON_DAMAGE)
        );
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void defend(NativeWeaponDamageEvent.BeforeMitigation var0) {
        
    }

    private static boolean immune(ServerPlayer var0, ArmorBlessingCatalog.Condition var1) {
        return worn(var0)
            .values()
            .stream()
            .anyMatch(var1x -> var1x.definition().mechanic() == ArmorBlessingCatalog.Mechanic.IMMUNE && var1x.definition().condition() == var1);
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void poison(Applicable var0) {
        
    }

    private static Holder<MobEffect> effect(String var0) {
        return switch (var0) {
            case "absorption" -> MobEffects.ABSORPTION;
            case "regeneration" -> MobEffects.REGENERATION;
            case "speed" -> MobEffects.MOVEMENT_SPEED;
            case "resistance" -> MobEffects.DAMAGE_RESISTANCE;
            case "strength" -> MobEffects.DAMAGE_BOOST;
            case "jump" -> MobEffects.JUMP;
            case "slow_falling" -> MobEffects.SLOW_FALLING;
            case "haste" -> MobEffects.DIG_SPEED;
            case "night_vision" -> MobEffects.NIGHT_VISION;
            case "fire_resistance" -> MobEffects.FIRE_RESISTANCE;
            case "nausea" -> MobEffects.CONFUSION;
            case "invisibility" -> MobEffects.INVISIBILITY;
            case "luck" -> MobEffects.LUCK;
            case "glowing" -> MobEffects.GLOWING;
            case "water_breathing" -> MobEffects.WATER_BREATHING;
            case "dolphins_grace" -> MobEffects.DOLPHINS_GRACE;
            case "slowness" -> MobEffects.MOVEMENT_SLOWDOWN;
            case "weakness" -> MobEffects.WEAKNESS;
            case "poison" -> MobEffects.POISON;
            case "wither" -> MobEffects.WITHER;
            case "hunger" -> MobEffects.HUNGER;
            case "mining_fatigue" -> MobEffects.DIG_SLOWDOWN;
            default -> throw new IllegalArgumentException(var0);
        };
    }

    private static boolean repair(ServerPlayer var0, int var1) {
        ItemStack var2 = ItemStack.EMPTY;

        for (EquipmentSlot var6 : EquipmentSlot.values()) {
            if (var6.getType() == Type.HUMANOID_ARMOR) {
                ItemStack var7 = var0.getItemBySlot(var6);
                if (eligible(var7)
                    && ((ArmorItem)var7.getItem()).getEquipmentSlot() == var6
                    && var7.isDamageableItem()
                    && var7.getDamageValue() > var2.getDamageValue()) {
                    var2 = var7;
                }
            }
        }

        if (var2.isEmpty()) {
            return false;
        } else {
            var2.setDamageValue(Math.max(0, var2.getDamageValue() - var1));
            var0.getInventory().setChanged();
            var0.containerMenu.broadcastChanges();
            return true;
        }
    }

    private static boolean activate(ServerPlayer var0, ArmorFoodBlessings.Active var1, DamageSource var2) {
        ArmorBlessingCatalog.Definition var3 = var1.definition();
        int var4 = var1.level();
        LivingEntity var5 = var2 != null && var2.getEntity() instanceof LivingEntity var6 && var6 != var0 && var6.isAlive() && !var6.isAlliedTo(var0)
            ? var6
            : null;

        return switch (var3.mechanic()) {
            case POTION -> var0.addEffect(new MobEffectInstance(effect(var3.payload()), var3.ticks(var4), var3.payload().equals("absorption") ? var4 - 1 : 0));
            case COUNTER -> var5 != null && var5.addEffect(new MobEffectInstance(effect(var3.payload()), var3.ticks(var4), 0));
            case CLEANSE -> var0.removeEffect(effect(var3.payload()));
            case EXTINGUISH -> {
                boolean var10 = var0.isOnFire();
                if (var10) {
                    var0.clearFire();
                }

                yield var10;
            }
            case FEED -> {
                boolean var9 = var0.getFoodData().getFoodLevel() < 20;
                if (var9) {
                    var0.getFoodData().eat((int)var3.value(var4), 0.1F);
                }

                yield var9;
            }
            case AIR -> {
                int var8 = var0.getAirSupply();
                if (var8 < var0.getMaxAirSupply()) {
                    var0.setAirSupply(Math.min(var0.getMaxAirSupply(), var8 + (int)var3.value(var4)));
                }

                yield var8 < var0.getMaxAirSupply();
            }
            case REPAIR -> repair(var0, (int)var3.value(var4));
            case REPEL -> {
                if (var5 != null) {
                    var5.knockback(var3.value(var4), var0.getX() - var5.getX(), var0.getZ() - var5.getZ());
                }

                yield var5 != null;
            }
            default -> false;
        };
    }

    private static boolean ready(ServerPlayer var0, ArmorBlessingCatalog.Definition var1, long var2) {
        CompoundTag var4 = var0.getPersistentData();
        String var5 = "FTBossArmorBlessing_" + var1.id();
        return ArmorBlessingCatalog.ready(var4.contains(var5), var4.getLong(var5), var2, var1.cooldown());
    }

    private static void trigger(ServerPlayer var0, ArmorBlessingCatalog.Phase var1, DamageSource var2, float var3) {
        if (var3 > 0.0F) {
            long var4 = var0.level().getGameTime();
            ArmorBlessingCatalog.Context var6 = context(var0, var2, var3);

            for (ArmorFoodBlessings.Active var8 : worn(var0).values()) {
                ArmorBlessingCatalog.Definition var9 = var8.definition();
                if (var9.phase() == var1 && ArmorBlessingCatalog.matches(var9.condition(), var6) && ready(var0, var9, var4) && activate(var0, var8, var2)) {
                    var0.getPersistentData().putLong("FTBossArmorBlessing_" + var9.id(), var4);
                }
            }
        }
    }

    @SubscribeEvent
    public static void tickStart(PlayerTickEvent.Pre var0) {
        
    }

    @SubscribeEvent
    public static void tickEnd(PlayerTickEvent.Post event) {
        
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void damaged(NativeWeaponDamageEvent.BeforeHealth var0) {
        
    }

    private static record Active(ArmorBlessingCatalog.Definition definition, int level) {
    }
}
