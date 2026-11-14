package net.farmingtales.ftboss.item;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.View;
import net.farmingtales.ftboss.util.WeaponDurabilityRules;
import net.farmingtales.ftboss.util.WeaponMaterialBonusRules;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.WeaponMaterialBonusRules.Bonus;
import net.farmingtales.ftboss.util.WeaponMaterialBonusRules.Input;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class WeaponMaterialBonuses {
    private static final List<String> ATTRIBUTES = List.of("spell_power", "max_mana", "attack_damage", "armor", "max_health");
    private static final List<Map<String, UUID>> IDS = List.of(ids("mainhand"), ids("offhand"));

    private WeaponMaterialBonuses() {
    }

    private static Map<String, UUID> ids(String var0) {
        HashMap var1 = new HashMap();

        for (String var3 : ATTRIBUTES) {
            var1.put(var3, UUID.nameUUIDFromBytes(("ftboss.material_rarity." + var0 + "." + var3).getBytes(StandardCharsets.UTF_8)));
        }

        return Map.copyOf(var1);
    }

    private static boolean candidate(CompoundTag var0) {
        if (var0 == null) {
            return false;
        } else {
            if (var0.contains("Cells", 9)) {
                ListTag var1 = var0.getList("Cells", 10);
                if (var1.size() > 27) {
                    return false;
                }

                for (int var2 = 0; var2 < var1.size(); var2++) {
                    if (WeaponMaterialBonusRules.relevant(var1.getCompound(var2).getCompound("Item").getString("id"))) {
                        return true;
                    }
                }
            } else if (var0.contains("Cells", 10)) {
                CompoundTag var6 = var0.getCompound("Cells");

                for (Slot var5 : Slot.values()) {
                    if (WeaponMaterialBonusRules.relevant(var6.getCompound(var5.name()).getCompound("Item").getString("id"))) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public static Input input(ItemStack var0) {
        ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getItem());
        String var2 = var1 == null ? "" : var1.toString();
        if (!var0.isEmpty() && var0.getCount() == 1 && !var0.isDamageableItem() && WeaponMaterialBonusRules.relevant(var2)) {
            ItemStack var3 = new ItemStack(var0.getItem());
            return new Input(var2, var3.getRarity().name());
        } else {
            return null;
        }
    }

    public static Bonus bonuses(ItemStack var0) {
        if (!var0.isEmpty()
            && var0.getCount() == 1
            && var0.has(DataComponents.CUSTOM_DATA)
            && (candidate(FoodWeaponData.read(var0, "FTBossFourWeaponFoundation")) || candidate(FoodWeaponData.read(var0, "FTBossSwordStructure")))) {
            CompoundTag var1 = FoodWeaponData.read(var0, "FTBossWeaponLifecycle");
            if ((var1 == null || !var1.getBoolean("Broken")) && !FoodWeaponLifecycle.durabilityDepleted(var0)) {
                ArrayList<Input> var2 = new ArrayList<>();
                if (FourWeaponFoundationAdapter.applies(var0)) {
                    View var3 = FourWeaponFoundationAdapter.read(var0);
                    if (!var3.valid() || !var3.status().ready()) {
                        return WeaponMaterialBonusRules.ZERO;
                    }

                    var3.entries().forEach((var2x, var3x) -> {
                        if (var3x.paid() && FourWeaponFoundationRules.unlocked(var3.profile(), var2x, var3.level())) {
                            Input var4 = input(var3x.storedItem());
                            if (var4 != null) {
                                var2.add(var4);
                            }
                        }
                    });
                } else if (SwordCavityBuild.applies(var0) && !SwordCavityBuild.missingRequired(var0)) {
                    for (Slot var6 : Slot.values()) {
                        if (SwordCavityBuild.cellUnlocked(var0, var6.ordinal())) {
                            Input var7 = input(SwordCavityBuild.storedItem(var0, var6));
                            if (var7 != null) {
                                var2.add(var7);
                            }
                        }
                    }
                }

                return WeaponMaterialBonusRules.combine(var2);
            } else {
                return WeaponMaterialBonusRules.ZERO;
            }
        } else {
            return WeaponMaterialBonusRules.ZERO;
        }
    }

    @SubscribeEvent
    public static void attributes(ItemAttributeModifierEvent var0) {
        Bonus var1 = bonuses(var0.getItemStack());
        if (!var1.equals(WeaponMaterialBonusRules.ZERO)) {
            for (int var2 = 0; var2 < 2; var2++) {
                add(
                    var0,
                    BuiltInRegistries.ATTRIBUTE.getOptional(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "spell_power")).map(BuiltInRegistries.ATTRIBUTE::wrapAsHolder).orElse(null),
                    var2,
                    "spell_power",
                    var1.spellPower()
                );
                add(
                    var0,
                    BuiltInRegistries.ATTRIBUTE.getOptional(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "max_mana")).map(BuiltInRegistries.ATTRIBUTE::wrapAsHolder).orElse(null),
                    var2,
                    "max_mana",
                    var1.maxMana()
                );
                add(var0, Attributes.ATTACK_DAMAGE, var2, "attack_damage", var1.attackDamage());
                add(var0, Attributes.ARMOR, var2, "armor", var1.armor());
                add(var0, Attributes.MAX_HEALTH, var2, "max_health", var1.maxHealth());
            }
        }
    }

    private static void add(ItemAttributeModifierEvent var0, Holder<Attribute> var1, int var2, String var3, double var4) {
        if (var1 != null && var4 > 0.0) {
            var0.addModifier(var1, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("ftboss", "material_rarity/" + IDS.get(var2).get(var3)), var4, Operation.ADD_VALUE), var2 == 0 ? EquipmentSlotGroup.MAINHAND : EquipmentSlotGroup.OFFHAND);
        }
    }

    @SubscribeEvent
    public static void materialTooltip(ItemTooltipEvent var0) {
        ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getItemStack().getItem());
        if (var1 != null) {
            if (var1.toString().equals("kubejs:ne_ingot")) {
                var0.getToolTip()
                    .add(
                        Component.translatable(
                                "tooltip.ftboss.material.diamond_alloy.durability",
                                new Object[]{
                                    Math.round(10.0),
                                    Math.round(200.0),
                                    String.format(Locale.ROOT, "%.1f", WeaponDurabilityRules.multiplier(Integer.MAX_VALUE))
                                }
                            )
                            .withStyle(ChatFormatting.AQUA)
                    );
                var0.getToolTip().add(Component.translatable("tooltip.ftboss.material.diamond_alloy.requirements").withStyle(ChatFormatting.DARK_GRAY));
            } else if (WeaponMaterialBonusRules.relevant(var1.toString())) {
                ItemStack var2 = new ItemStack(var0.getItemStack().getItem());
                Input var3 = input(var2);
                if (var3 != null) {
                    Bonus var4 = WeaponMaterialBonusRules.material(var3.identifier(), var3.rarity());
                    String var5 = var4.spellPower() > 0.0
                        ? String.format(Locale.ROOT, "配件加成：法强 +%.0f%% / 蓝量 +%.0f", var4.spellPower() * 100.0, var4.maxMana())
                        : String.format(Locale.ROOT, "配件加成：攻击 +%.1f / 护甲 +%.2f / 生命 +%.0f", var4.attackDamage(), var4.armor(), var4.maxHealth());
                    var0.getToolTip().add(Component.literal(var5).withStyle(ChatFormatting.AQUA));
                    var0.getToolTip().add(Component.literal("仅装入可用武器后生效；同种材料只计一次，单件武器加成有上限").withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }
}
