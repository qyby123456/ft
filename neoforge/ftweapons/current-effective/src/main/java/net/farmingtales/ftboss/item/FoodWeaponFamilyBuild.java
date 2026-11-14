package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.BuiltInRegistries;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class FoodWeaponFamilyBuild {
    private static final UUID SPELL_POWER = UUID.fromString("fb8a9bcc-a8fb-4079-8e58-c0f6fd84b3c7");
    private static final UUID CAST_SPEED = UUID.fromString("b5d8c37c-874c-4194-93b3-99735762c997");
    private static final UUID OFFHAND_SPELL_POWER = UUID.fromString("86ba6bfb-a3e0-4b68-a3ea-68c0bb8cdd9e");
    private static final UUID OFFHAND_CAST_SPEED = UUID.fromString("7c2bead8-fb2f-44e7-ad85-39d92627b119");

    private FoodWeaponFamilyBuild() {
    }

    public static Kind kind(ItemStack var0) {
        if (FourWeaponFoundationAdapter.applies(var0)) {
            Profile var8 = FourWeaponFoundationAdapter.profile(var0);
            if (var8 == null) {
                return Kind.MELEE;
            } else {
                return var8.family().equals("staff") ? Kind.STAFF : (!var8.family().equals("bow") && !var8.family().equals("crossbow") ? Kind.MELEE : Kind.BOW);
            }
        } else if (!var0.isEmpty() && !var0.has(DataComponents.FOOD)) {
            ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getItem());
            if (var1 == null || var1.getNamespace().equals("create")) {
                return Kind.MELEE;
            } else if (!(var0.getItem() instanceof BowItem) && !(var0.getItem() instanceof CrossbowItem)) {
                String var2 = var1.getPath().toLowerCase(Locale.ROOT);
                if (!var2.contains("spellbook") && !var2.contains("scroll") && !var2.endsWith("_tome") && !var2.endsWith("_grimoire")) {
                    for (Class var3 = var0.getItem().getClass(); var3 != null; var3 = var3.getSuperclass()) {
                        for (Class var7 : var3.getInterfaces()) {
                            if (var7.getName().equals("io.redspace.ironsspellbooks.api.item.ISpellbook")) {
                                return Kind.MELEE;
                            }
                        }
                    }

                    for (Class var9 = var0.getItem().getClass(); var9 != null; var9 = var9.getSuperclass()) {
                        if (var9.getName().equals("io.redspace.ironsspellbooks.item.CastingItem")) {
                            return Kind.STAFF;
                        }
                    }

                    return Kind.MELEE;
                } else {
                    return Kind.MELEE;
                }
            } else {
                return Kind.BOW;
            }
        } else {
            return Kind.MELEE;
        }
    }

    public static int baseDurability(ItemStack var0, int var1) {
        return var1;
    }

    public static Build build(ItemStack var0) {
        return FourWeaponFoundationAdapter.applies(var0)
            ? FourWeaponFoundationAdapter.combat(var0)
            : FoodWeaponFamilyRules.build(kind(var0), WeaponMaterialBuild.materials(var0));
    }

    public static Build materialPreview(ItemStack var0, Part var1, Material var2) {
        if (FourWeaponFoundationAdapter.applies(var0)) {
            return FourWeaponFoundationAdapter.combat(var0);
        } else {
            ArrayList var3 = new ArrayList<>(WeaponMaterialBuild.materials(var0));
            var3.set(var1.ordinal(), var2);
            return FoodWeaponFamilyRules.build(kind(var0), var3);
        }
    }

    public static Component partName(ItemStack var0, Part var1) {
        return (Component)(kind(var0) == Kind.MELEE
            ? WeaponMaterialBuild.partName(var1)
            : Component.translatable("part.ftboss.weapon." + kind(var0).name().toLowerCase(Locale.ROOT) + "." + var1.name().toLowerCase(Locale.ROOT)));
    }

    public static FoodWeaponFamilyBuild.Stats stats(ItemStack var0) {
        Build var1 = build(var0);
        return new FoodWeaponFamilyBuild.Stats(
            kind(var0),
            FoodWeaponFamilyRules.damageMultiplier(var1),
            FoodWeaponFamilyRules.speedMultiplier(var1),
            FoodWeaponFamilyRules.rangeMultiplier(var1),
            FoodWeaponFamilyRules.manaMultiplier(kind(var0), var1),
            var0.getMaxDamage(),
            var1.wear()
        );
    }

    public static FoodWeaponFamilyBuild.Stats preview(ItemStack var0, Part var1, Material var2) {
        if (FourWeaponFoundationAdapter.applies(var0)) {
            return stats(var0);
        } else {
            ItemStack var3 = var0.copy();
            FoodWeaponData.edit(var3, "FTBossWeaponMaterials", data -> data.putString(var1.name(), var2.id()));
            return stats(var3);
        }
    }

    public static double damageMultiplier(ItemStack var0) {
        return FoodWeaponFamilyRules.damageMultiplier(build(var0));
    }

    public static double rangeMultiplier(ItemStack var0) {
        return FoodWeaponFamilyRules.rangeMultiplier(build(var0));
    }

    public static double manaMultiplier(ItemStack var0) {
        return FoodWeaponFamilyRules.manaMultiplier(kind(var0), build(var0));
    }

    public static int chargeTicks(ItemStack var0, int var1) {
        return FoodWeaponFamilyRules.chargeTicks(var1, FoodWeaponFamilyRules.speedMultiplier(build(var0)));
    }

    @SubscribeEvent(
        priority = EventPriority.LOW
    )
    public static void staffAttributes(ItemAttributeModifierEvent var0) {
        ItemStack var1 = var0.getItemStack();
        for (EquipmentSlot equipmentSlot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
            if (kind(var1) != Kind.STAFF
                || FoodWeaponData.read(var1, "FTBossWeaponMaterials") == null && !FourWeaponFoundationAdapter.applies(var1)
                || FoodWeaponLifecycle.isBroken(var1)) {
                ;
            }
        }
    }

    private static void addSpellAttribute(ItemAttributeModifierEvent var0, String var1, UUID var2, double var3, EquipmentSlot equipmentSlot) {
        Holder.Reference<Attribute> var5 = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", var1)).orElse(null);
        if (var5 != null && Math.abs(var3) > 1.0E-5) {
            var0.addModifier(var5, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("ftboss", "material/" + var2), var3, Operation.ADD_MULTIPLIED_TOTAL), EquipmentSlotGroup.bySlot(equipmentSlot));
        }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent var0) {
        ItemStack var1 = var0.getItemStack();
        Kind var2 = kind(var1);
        if (var2 != Kind.MELEE && FoodWeaponEvolution.isEvolutionCandidate(var1)) {
            FoodWeaponFamilyBuild.Stats var3 = stats(var1);
            var0.getToolTip()
                .add(
                    Component.translatable(
                            "tooltip.ftboss.weapon.family." + var2.name().toLowerCase(Locale.ROOT),
                            multiplier(var3.damageMultiplier()),
                            multiplier(var3.speedMultiplier()),
                            multiplier(var2 == Kind.BOW ? var3.rangeMultiplier() : var3.manaMultiplier())
                        )
                        .withStyle(ChatFormatting.AQUA)
                );
        }
    }

    private static String multiplier(double var0) {
        return String.format(Locale.ROOT, "%.2f", var0);
    }

    public static record Stats(
        Kind kind, double damageMultiplier, double speedMultiplier, double rangeMultiplier, double manaMultiplier, int durability, double wear
    ) {
    }
}
