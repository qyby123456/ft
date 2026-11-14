package net.farmingtales.ftboss.item;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.item.ArmorBlessingCatalog.Mechanic;
import net.farmingtales.ftboss.item.ArmorBlessingCatalog.Phase;
import net.farmingtales.ftboss.item.ArmorBlessingCatalog.Context;
import net.farmingtales.ftboss.item.ArmorBlessingCatalog.Definition;
import net.farmingtales.ftboss.item.ArmorBlessingCatalog.Condition;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ftboss")
public final class ArmorFoodBlessings {
    private static final String ROOT = "FTBossArmorBlessings";
    private static final String LIST = "FoodBlessings";
    private static final String TIMER = "FTBossArmorBlessing_";
    private static final int CAPACITY = 6;
    private static final TagKey<DamageType> MAGIC_DAMAGE = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("ftboss", "is_magic"));
    private static final TagKey<DamageType> POISON_DAMAGE = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("ftboss", "is_poison"));
    private record Active(Definition definition, int level) {}

    private ArmorFoodBlessings() {}

    public static boolean eligible(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ArmorItem
                && (!stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage());
    }

    public static ItemStack heldArmor(Player player) {
        if (eligible(player.getMainHandItem())) return player.getMainHandItem();
        return eligible(player.getOffhandItem()) ? player.getOffhandItem() : ItemStack.EMPTY;
    }

    private static ListTag entries(ItemStack stack) {
        CompoundTag root = stack.getTagElement(ROOT);
        return root == null ? new ListTag() : root.getList(LIST, Tag.TAG_COMPOUND);
    }

    private static Map<String, Active> snapshot(ItemStack stack) {
        Map<String, Active> result = new LinkedHashMap<>();
        if (!eligible(stack)) return result;
        ListTag list = entries(stack);
        for (int index = 0; index < Math.min(CAPACITY, list.size()); index++) {
            CompoundTag entry = list.getCompound(index);
            Definition definition = ArmorBlessingCatalog.byId(entry.getString("Blessing"));
            if (definition == null || !definition.profile().equals(entry.getString("Profile"))) continue;
            int level = ArmorBlessingCatalog.clampLevel(entry.getInt("Level"));
            Active old = result.get(definition.id());
            if (old == null || old.level() < level) result.put(definition.id(), new Active(definition, level));
        }
        return result;
    }

    public static boolean hasRoom(ItemStack armor, String profile) {
        if (!eligible(armor) || entries(armor).size() >= CAPACITY) return false;
        Map<String, Active> current = snapshot(armor);
        return ArmorBlessingCatalog.pool(profile).stream().anyMatch(entry -> !current.containsKey(entry.id()));
    }

    public static boolean apply(ServerPlayer player, String profile, ItemStack armor, ItemStack food, int tier) {
        if (food.isEmpty() || food == armor || !hasRoom(armor, profile)) {
            player.displayClientMessage(Component.translatable("message.ftboss.food_blessing.full", CAPACITY), true);
            return false;
        }
        Map<String, Active> current = snapshot(armor);
        List<Definition> choices = ArmorBlessingCatalog.pool(profile).stream().filter(entry -> !current.containsKey(entry.id())).toList();
        Definition selected = choices.get(player.getRandom().nextInt(choices.size()));
        int foodTier = ArmorBlessingCatalog.clampLevel(tier);
        int level = foodTier == 1 ? 1 : foodTier == 2 ? (player.getRandom().nextBoolean() ? 2 : 1)
                : (player.getRandom().nextBoolean() ? 3 : 2);
        CompoundTag entry = new CompoundTag();
        entry.putString("Blessing", selected.id());
        entry.putString("Profile", selected.profile());
        entry.putInt("Level", level);
        entry.putInt("FoodTier", foodTier);
        ListTag list = entries(armor);
        list.add(entry);
        armor.getOrCreateTagElement(ROOT).put(LIST, list);
        if (!player.getAbilities().instabuild) food.shrink(1);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        player.sendSystemMessage(Component.translatable("message.ftboss.armor_blessing.applied",
                Component.translatable("food_blessing.ftboss." + selected.id()), roman(level)).withStyle(ChatFormatting.LIGHT_PURPLE));
        return true;
    }

    private static String roman(int level) { return level == 1 ? "I" : level == 2 ? "II" : "III"; }

    public static List<FoodWeaponEvolution.FoodBlessingView> views(ItemStack stack) {
        List<FoodWeaponEvolution.FoodBlessingView> result = new ArrayList<>();
        for (Active active : snapshot(stack).values()) {
            String id = active.definition().id();
            result.add(new FoodWeaponEvolution.FoodBlessingView(Component.translatable("food_blessing.ftboss." + id),
                    Component.translatable("tooltip.ftboss.food_blessing." + id), roman(active.level())));
        }
        return List.copyOf(result);
    }

    private static Map<String, Active> worn(ServerPlayer player) {
        Map<String, Active> result = new LinkedHashMap<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack armor = player.getItemBySlot(slot);
            if (!eligible(armor) || ((ArmorItem) armor.getItem()).getEquipmentSlot() != slot) continue;
            for (Active active : snapshot(armor).values()) {
                result.merge(active.definition().id(), active, (old, added) -> old.level() >= added.level() ? old : added);
            }
        }
        return result;
    }

    @SubscribeEvent
    public static void attributes(ItemAttributeModifierEvent event) {
        ItemStack armor = event.getItemStack();
        if (!eligible(armor) || ((ArmorItem) armor.getItem()).getEquipmentSlot() != event.getSlotType()) return;
        for (Active active : snapshot(armor).values()) {
            if (active.definition().phase() != Phase.ATTRIBUTE) continue;
            Attribute attribute = switch (active.definition().payload()) {
                case "armor" -> Attributes.ARMOR;
                case "toughness" -> Attributes.ARMOR_TOUGHNESS;
                case "health" -> Attributes.MAX_HEALTH;
                case "knockback" -> Attributes.KNOCKBACK_RESISTANCE;
                case "attack_damage" -> Attributes.ATTACK_DAMAGE;
                case "luck" -> Attributes.LUCK;
                case "speed" -> Attributes.MOVEMENT_SPEED;
                default -> null;
            };
            if (attribute == null) continue;
            String key = "ftboss:armor_blessing/" + active.definition().id() + "/" + event.getSlotType().getName();
            event.addModifier(attribute, new AttributeModifier(UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)),
                    key, active.definition().value(active.level()), active.definition().payload().equals("speed")
                            ? AttributeModifier.Operation.MULTIPLY_BASE : AttributeModifier.Operation.ADDITION));
        }
    }

    private static Context context(ServerPlayer player, DamageSource source, float health) {
        return new Context(health / Math.max(1.0, player.getMaxHealth()), player.getFoodData().getFoodLevel(),
                player.isCrouching(), player.isOnFire(), source != null && source.is(DamageTypeTags.IS_PROJECTILE), player.onGround(),
                player.isInWaterRainOrBubble(), player.isSprinting(), player.isUnderWater(), player.level().isNight(),
                source != null && source.getDirectEntity() instanceof LivingEntity attacker && attacker.isAlive() && attacker != player,
                source != null && (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC) || source.is(MAGIC_DAMAGE)),
                source != null && source.is(DamageTypeTags.IS_FALL), source != null && source.is(DamageTypeTags.IS_FIRE),
                source != null && source.is(DamageTypeTags.IS_EXPLOSION), player.hasEffect(MobEffects.POISON),
                source != null && source.is(DamageTypeTags.IS_FREEZING), source != null && source.is(POISON_DAMAGE));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void defend(LivingHurtEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player) || !(event.getAmount() > 0)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        Context context = context(player, event.getSource(), player.getHealth());
        double reduction = 0;
        for (Active active : worn(player).values()) {
            Definition definition = active.definition();
            if (definition.phase() != Phase.HURT || !ArmorBlessingCatalog.matches(definition.condition(), context)) continue;
            if (definition.mechanic() == Mechanic.IMMUNE) {
                event.setAmount(0);
                event.setCanceled(true);
                return;
            }
            if (definition.mechanic() == Mechanic.REDUCE) {
                reduction += definition.value(active.level());
            }
        }
        event.setAmount((float) (event.getAmount() * ArmorBlessingCatalog.multiplier(reduction)));
    }

    private static boolean immune(ServerPlayer player, Condition condition) {
        return worn(player).values().stream().anyMatch(active -> active.definition().mechanic() == Mechanic.IMMUNE
                && active.definition().condition() == condition);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void poison(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getEffectInstance().getEffect() == MobEffects.POISON
                && immune(player, Condition.POISON)) event.setResult(Event.Result.DENY);
    }

    private static MobEffect effect(String id) {
        return switch (id) {
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
            default -> throw new IllegalArgumentException(id);
        };
    }

    private static boolean repair(ServerPlayer player, int amount) {
        ItemStack selected = ItemStack.EMPTY;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack armor = player.getItemBySlot(slot);
            if (!eligible(armor) || ((ArmorItem) armor.getItem()).getEquipmentSlot() != slot || !armor.isDamageableItem()) continue;
            if (armor.getDamageValue() > selected.getDamageValue()) selected = armor;
        }
        if (selected.isEmpty()) return false;
        selected.setDamageValue(Math.max(0, selected.getDamageValue() - amount));
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        return true;
    }

    private static boolean activate(ServerPlayer player, Active active, DamageSource source) {
        Definition definition = active.definition();
        int level = active.level();
        LivingEntity attacker = source != null && source.getEntity() instanceof LivingEntity living
                && living != player && living.isAlive() && !living.isAlliedTo(player) ? living : null;
        return switch (definition.mechanic()) {
            case POTION -> player.addEffect(new MobEffectInstance(effect(definition.payload()), definition.ticks(level),
                    definition.payload().equals("absorption") ? level - 1 : 0));
            case COUNTER -> attacker != null && attacker.addEffect(new MobEffectInstance(effect(definition.payload()), definition.ticks(level), 0));
            case CLEANSE -> player.removeEffect(effect(definition.payload()));
            case EXTINGUISH -> {
                boolean burning = player.isOnFire();
                if (burning) player.clearFire();
                yield burning;
            }
            case FEED -> {
                boolean hungry = player.getFoodData().getFoodLevel() < 20;
                if (hungry) player.getFoodData().eat((int) definition.value(level), .1F);
                yield hungry;
            }
            case AIR -> {
                int previous = player.getAirSupply();
                if (previous < player.getMaxAirSupply()) player.setAirSupply(Math.min(player.getMaxAirSupply(), previous + (int) definition.value(level)));
                yield previous < player.getMaxAirSupply();
            }
            case REPAIR -> repair(player, (int) definition.value(level));
            case REPEL -> {
                if (attacker != null) attacker.knockback(definition.value(level), player.getX() - attacker.getX(), player.getZ() - attacker.getZ());
                yield attacker != null;
            }
            default -> false;
        };
    }

    private static boolean ready(ServerPlayer player, Definition definition, long now) {
        CompoundTag timers = player.getPersistentData();
        String key = TIMER + definition.id();
        return ArmorBlessingCatalog.ready(timers.contains(key), timers.getLong(key), now, definition.cooldown());
    }

    private static void trigger(ServerPlayer player, Phase phase, DamageSource source, float health) {
        if (!(health > 0)) return;
        long now = player.level().getGameTime();
        Context snapshot = context(player, source, health);
        for (Active active : worn(player).values()) {
            Definition definition = active.definition();
            if (definition.phase() != phase || !ArmorBlessingCatalog.matches(definition.condition(), snapshot)
                    || !ready(player, definition, now)) continue;
            if (activate(player, active, source)) player.getPersistentData().putLong(TIMER + definition.id(), now);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player) || !player.isAlive()) return;
        if (event.phase == TickEvent.Phase.START) {
            if (player.hasEffect(MobEffects.POISON) && immune(player, Condition.POISON)) player.removeEffect(MobEffects.POISON);
            if (player.isOnFire() && immune(player, Condition.FIRE)) player.clearFire();
            if (player.getTicksFrozen() > 0 && immune(player, Condition.FREEZING)) player.setTicksFrozen(0);
            return;
        }
        if (player.level().getGameTime() % 20 != 0) return;
        trigger(player, Phase.TICK, null, player.getHealth());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void damaged(LivingDamageEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player) || !(event.getAmount() > 0)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        float remainingHealth = player.getHealth() - event.getAmount();
        long now = player.level().getGameTime();
        if (remainingHealth <= 0 && player.getHealth() > 1) {
            for (Active active : worn(player).values()) {
                Definition definition = active.definition();
                int cost = Math.max(1, 4 - active.level());
                if (definition.mechanic() != Mechanic.RESCUE || !ready(player, definition, now) || player.experienceLevel < cost) continue;
                player.giveExperienceLevels(-cost);
                event.setAmount(player.getHealth() - 1);
                remainingHealth = 1;
                player.getPersistentData().putLong(TIMER + definition.id(), now);
                break;
            }
        }
        trigger(player, Phase.DAMAGE, event.getSource(), remainingHealth);
    }

}
