package net.farmingtales.ftboss.item;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.util.WeaponMaterialBonusRules;
import net.farmingtales.ftboss.util.WeaponMaterialBonusRules.Bonus;
import net.farmingtales.ftboss.util.WeaponMaterialBonusRules.Input;
import net.farmingtales.ftboss.util.WeaponDurabilityRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** 已支付原料的稀有度属性；2026-10-20 只读取可用配件，不缓存权限、不扫描背包或逐tick刷新。 */
@Mod.EventBusSubscriber(modid = "ftboss")
public final class WeaponMaterialBonuses {
    private static final List<String> ATTRIBUTES = List.of("spell_power", "max_mana", "attack_damage", "armor", "max_health");
    private static final List<Map<String, UUID>> IDS = List.of(ids("mainhand"), ids("offhand"));

    private WeaponMaterialBonuses() {}

    private static Map<String, UUID> ids(String hand) {
        var identifiers = new java.util.HashMap<String, UUID>();
        for (String attribute : ATTRIBUTES) identifiers.put(attribute,
                UUID.nameUUIDFromBytes(("ftboss.material_rarity." + hand + "." + attribute).getBytes(StandardCharsets.UTF_8)));
        return Map.copyOf(identifiers);
    }

    private static boolean candidate(CompoundTag structure) {
        if (structure == null) return false;
        if (structure.contains("Cells", Tag.TAG_LIST)) {
            var cells = structure.getList("Cells", Tag.TAG_COMPOUND);
            if (cells.size() > 27) return false;
            for (int index = 0; index < cells.size(); index++) {
                if (WeaponMaterialBonusRules.relevant(cells.getCompound(index).getCompound("Item").getString("id"))) return true;
            }
        } else if (structure.contains("Cells", Tag.TAG_COMPOUND)) {
            var cells = structure.getCompound("Cells");
            for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
                if (WeaponMaterialBonusRules.relevant(cells.getCompound(slot.name()).getCompound("Item").getString("id"))) return true;
            }
        }
        return false;
    }

    public static Input input(ItemStack material) {
        ResourceLocation identifier = ForgeRegistries.ITEMS.getKey(material.getItem());
        String id = identifier == null ? "" : identifier.toString();
        if (material.isEmpty() || material.getCount() != 1 || material.isDamageableItem() || !WeaponMaterialBonusRules.relevant(id)) return null;
        ItemStack plain = new ItemStack(material.getItem());
        return new Input(id, plain.getRarity().name());
    }

    public static Bonus bonuses(ItemStack weapon) {
        if (weapon.isEmpty() || weapon.getCount() != 1 || weapon.getTag() == null
                || !candidate(weapon.getTagElement(FourWeaponFoundationAdapter.ROOT)) && !candidate(weapon.getTagElement(SwordCavityBuild.ROOT))) {
            return WeaponMaterialBonusRules.ZERO;
        }
        CompoundTag lifecycle = weapon.getTagElement(FoodWeaponLifecycle.ROOT);
        if (lifecycle != null && lifecycle.getBoolean("Broken") || FoodWeaponLifecycle.durabilityDepleted(weapon)) return WeaponMaterialBonusRules.ZERO;
        List<Input> paid = new ArrayList<>();
        if (FourWeaponFoundationAdapter.applies(weapon)) {
            var view = FourWeaponFoundationAdapter.read(weapon);
            if (!view.valid() || !view.status().ready()) return WeaponMaterialBonusRules.ZERO;
            view.entries().forEach((cell, entry) -> {
                if (entry.paid() && FourWeaponFoundationRules.unlocked(view.profile(), cell, view.level())) {
                    Input input = input(entry.storedItem());
                    if (input != null) paid.add(input);
                }
            });
        } else if (SwordCavityBuild.applies(weapon) && !SwordCavityBuild.missingRequired(weapon)) {
            for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
                if (!SwordCavityBuild.cellUnlocked(weapon, slot.ordinal())) continue;
                Input input = input(SwordCavityBuild.storedItem(weapon, slot));
                if (input != null) paid.add(input);
            }
        }
        return EquipmentWeaponBuild.scale(weapon, WeaponMaterialBonusRules.combine(paid));
    }

    @SubscribeEvent
    public static void attributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!EquipmentAttributeSlots.heldItem(stack)) {
            EquipmentWeaponBuild.attributes(event);
            return;
        }
        if (event.getSlotType() != EquipmentSlot.MAINHAND && event.getSlotType() != EquipmentSlot.OFFHAND) return;
        Bonus bonus = bonuses(event.getItemStack());
        if (bonus.equals(WeaponMaterialBonusRules.ZERO)) return;
        int hand = event.getSlotType() == EquipmentSlot.MAINHAND ? 0 : 1;
        add(event, ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", "spell_power")), hand, "spell_power", bonus.spellPower());
        add(event, ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", "max_mana")), hand, "max_mana", bonus.maxMana());
        if (FoodWeaponFamilyBuild.kind(event.getItemStack()) == net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind.MELEE) {
            add(event, Attributes.ATTACK_DAMAGE, hand, "attack_damage", bonus.attackDamage());
        }
        add(event, Attributes.ARMOR, hand, "armor", bonus.armor());
        add(event, Attributes.MAX_HEALTH, hand, "max_health", bonus.maxHealth());
    }

    private static void add(ItemAttributeModifierEvent event, Attribute attribute, int hand, String name, double amount) {
        if (attribute != null && amount > 0.0) event.addModifier(attribute,
                new AttributeModifier(IDS.get(hand).get(name), "ftboss.material_rarity." + name, amount, AttributeModifier.Operation.ADDITION));
    }

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void unavailableArmorAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!EquipmentWeaponBuild.applies(stack)
                || ((net.minecraft.world.item.ArmorItem) stack.getItem()).getEquipmentSlot() != event.getSlotType()
                || !FoodWeaponLifecycle.isBroken(stack)) return;
        for (Attribute attribute : List.copyOf(event.getModifiers().keySet())) event.removeAttribute(attribute);
    }

    @SubscribeEvent
    public static void rangedDamage(LivingHurtEvent event) {
        if (event.getAmount() <= 0.0F || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !(event.getSource().getDirectEntity() instanceof AbstractArrow)
                || event.getEntity() == player || event.getEntity().isAlliedTo(player)) return;
        CompoundTag snapshot = FoodWeaponRanged.materialSnapshot(event.getSource(), player);
        if (!snapshot.getString("Family").equals("BOW")) return;
        ItemStack weapon = ItemStack.of(snapshot.getCompound("Weapon"));
        if (!(weapon.getItem() instanceof net.minecraft.world.item.BowItem)) return;
        double damage = bonuses(weapon).attackDamage() + BowGrowthCombat.bonuses(weapon).damage();
        if (damage > 0.0) event.setAmount(event.getAmount() + (float) damage);
    }

    @SubscribeEvent
    public static void materialTooltip(ItemTooltipEvent event) {
        ResourceLocation identifier = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        if (identifier == null) return;
        if (identifier.toString().equals(WeaponDurabilityRules.ALLOY_ITEM)) {
            event.getToolTip().add(Component.translatable("tooltip.ftboss.material.diamond_alloy.durability",
                    Math.round(WeaponDurabilityRules.BONUS_PER_PART * 100), Math.round(WeaponDurabilityRules.MAX_BONUS * 100),
                    String.format(Locale.ROOT, "%.1f", WeaponDurabilityRules.multiplier(Integer.MAX_VALUE))).withStyle(ChatFormatting.AQUA));
            event.getToolTip().add(Component.translatable("tooltip.ftboss.material.diamond_alloy.requirements")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        if (!WeaponMaterialBonusRules.relevant(identifier.toString())) return;
        ItemStack material = new ItemStack(event.getItemStack().getItem());
        Input input = input(material);
        if (input == null) return;
        Bonus bonus = WeaponMaterialBonusRules.material(input.identifier(), input.rarity());
        String text = bonus.spellPower() > 0.0
                ? String.format(Locale.ROOT, "配件加成：法强 +%.0f%% / 蓝量 +%.0f", bonus.spellPower() * 100.0, bonus.maxMana())
                : String.format(Locale.ROOT, "配件加成：近战/箭矢/投掷/法术伤害 +%.1f / 护甲 +%.2f / 生命 +%.0f", bonus.attackDamage(), bonus.armor(), bonus.maxHealth());
        event.getToolTip().add(Component.literal(text).withStyle(ChatFormatting.AQUA));
        event.getToolTip().add(Component.literal("仅装入可用武器后生效；同种材料只计一次，单件武器加成有上限").withStyle(ChatFormatting.DARK_GRAY));
    }
}
