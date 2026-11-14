/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.event.ItemAttributeModifierEvent
 *  net.minecraftforge.event.entity.player.ItemTooltipEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss")
public final class FoodWeaponFamilyBuild {
    private static final UUID SPELL_POWER = UUID.fromString("fb8a9bcc-a8fb-4079-8e58-c0f6fd84b3c7");
    private static final UUID CAST_SPEED = UUID.fromString("b5d8c37c-874c-4194-93b3-99735762c997");
    private static final UUID OFFHAND_SPELL_POWER = UUID.fromString("86ba6bfb-a3e0-4b68-a3ea-68c0bb8cdd9e");
    private static final UUID OFFHAND_CAST_SPEED = UUID.fromString("7c2bead8-fb2f-44e7-ad85-39d92627b119");

    private FoodWeaponFamilyBuild() {
    }

    public static FoodWeaponFamilyRules.Kind kind(ItemStack itemStack) {
        Class<?> clazz;
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            FourWeaponFoundationRules.Profile profile = FourWeaponFoundationAdapter.profile(itemStack);
            if (profile == null) {
                return FoodWeaponFamilyRules.Kind.MELEE;
            }
            return profile.family().equals("staff") ? FoodWeaponFamilyRules.Kind.STAFF : (profile.family().equals("bow") || profile.family().equals("crossbow") ? FoodWeaponFamilyRules.Kind.BOW : FoodWeaponFamilyRules.Kind.MELEE);
        }
        if (itemStack.m_41619_() || itemStack.m_41614_()) {
            return FoodWeaponFamilyRules.Kind.MELEE;
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        if (resourceLocation == null || resourceLocation.m_135827_().equals("create")) {
            return FoodWeaponFamilyRules.Kind.MELEE;
        }
        if (itemStack.m_41720_() instanceof BowItem || itemStack.m_41720_() instanceof CrossbowItem) {
            return FoodWeaponFamilyRules.Kind.BOW;
        }
        String string = resourceLocation.m_135815_().toLowerCase(Locale.ROOT);
        if (string.contains("spellbook") || string.contains("scroll") || string.endsWith("_tome") || string.endsWith("_grimoire")) {
            return FoodWeaponFamilyRules.Kind.MELEE;
        }
        for (clazz = itemStack.m_41720_().getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            for (Class<?> clazz2 : clazz.getInterfaces()) {
                if (!clazz2.getName().equals("io.redspace.ironsspellbooks.api.item.ISpellbook")) continue;
                return FoodWeaponFamilyRules.Kind.MELEE;
            }
        }
        for (clazz = itemStack.m_41720_().getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            if (!clazz.getName().equals("io.redspace.ironsspellbooks.item.CastingItem")) continue;
            return FoodWeaponFamilyRules.Kind.STAFF;
        }
        return FoodWeaponFamilyRules.Kind.MELEE;
    }

    public static int baseDurability(ItemStack itemStack, int n) {
        return n;
    }

    public static FoodWeaponBuildRules.Build build(ItemStack itemStack) {
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            return FourWeaponFoundationAdapter.combat(itemStack);
        }
        return FoodWeaponFamilyRules.build(FoodWeaponFamilyBuild.kind(itemStack), WeaponMaterialBuild.materials(itemStack));
    }

    public static FoodWeaponBuildRules.Build materialPreview(ItemStack itemStack, FoodWeaponBuildRules.Part part, FoodWeaponBuildRules.Material material) {
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            return FourWeaponFoundationAdapter.combat(itemStack);
        }
        ArrayList<FoodWeaponBuildRules.Material> arrayList = new ArrayList<FoodWeaponBuildRules.Material>(WeaponMaterialBuild.materials(itemStack));
        arrayList.set(part.ordinal(), material);
        return FoodWeaponFamilyRules.build(FoodWeaponFamilyBuild.kind(itemStack), arrayList);
    }

    public static Component partName(ItemStack itemStack, FoodWeaponBuildRules.Part part) {
        if (FoodWeaponFamilyBuild.kind(itemStack) == FoodWeaponFamilyRules.Kind.MELEE) {
            return WeaponMaterialBuild.partName(part);
        }
        return Component.m_237115_((String)("part.ftboss.weapon." + FoodWeaponFamilyBuild.kind(itemStack).name().toLowerCase(Locale.ROOT) + "." + part.name().toLowerCase(Locale.ROOT)));
    }

    public static Stats stats(ItemStack itemStack) {
        FoodWeaponBuildRules.Build build = FoodWeaponFamilyBuild.build(itemStack);
        return new Stats(FoodWeaponFamilyBuild.kind(itemStack), FoodWeaponFamilyRules.damageMultiplier(build), FoodWeaponFamilyRules.speedMultiplier(build), FoodWeaponFamilyRules.rangeMultiplier(build), FoodWeaponFamilyRules.manaMultiplier(FoodWeaponFamilyBuild.kind(itemStack), build), itemStack.m_41776_(), build.wear());
    }

    public static Stats preview(ItemStack itemStack, FoodWeaponBuildRules.Part part, FoodWeaponBuildRules.Material material) {
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            return FoodWeaponFamilyBuild.stats(itemStack);
        }
        ItemStack itemStack2 = itemStack.m_41777_();
        itemStack2.m_41698_("FTBossWeaponMaterials").m_128359_(part.name(), material.id());
        return FoodWeaponFamilyBuild.stats(itemStack2);
    }

    public static double damageMultiplier(ItemStack itemStack) {
        return FoodWeaponFamilyRules.damageMultiplier(FoodWeaponFamilyBuild.build(itemStack));
    }

    public static double rangeMultiplier(ItemStack itemStack) {
        return FoodWeaponFamilyRules.rangeMultiplier(FoodWeaponFamilyBuild.build(itemStack));
    }

    public static double manaMultiplier(ItemStack itemStack) {
        return FoodWeaponFamilyRules.manaMultiplier(FoodWeaponFamilyBuild.kind(itemStack), FoodWeaponFamilyBuild.build(itemStack));
    }

    public static int chargeTicks(ItemStack itemStack, int n) {
        return FoodWeaponFamilyRules.chargeTicks(n, FoodWeaponFamilyRules.speedMultiplier(FoodWeaponFamilyBuild.build(itemStack)));
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void staffAttributes(ItemAttributeModifierEvent itemAttributeModifierEvent) {
        ItemStack itemStack = itemAttributeModifierEvent.getItemStack();
        if (itemAttributeModifierEvent.getSlotType() != EquipmentSlot.MAINHAND && itemAttributeModifierEvent.getSlotType() != EquipmentSlot.OFFHAND || FoodWeaponFamilyBuild.kind(itemStack) != FoodWeaponFamilyRules.Kind.STAFF || itemStack.m_41737_("FTBossWeaponMaterials") == null && !FourWeaponFoundationAdapter.applies(itemStack) || FoodWeaponLifecycle.isBroken(itemStack)) {
            return;
        }
    }

    private static void addSpellAttribute(ItemAttributeModifierEvent itemAttributeModifierEvent, String string, UUID uUID, double d) {
        Attribute attribute = (Attribute)ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.fromNamespaceAndPath((String)"irons_spellbooks", (String)string));
        if (attribute != null && Math.abs(d) > 1.0E-5) {
            itemAttributeModifierEvent.addModifier(attribute, new AttributeModifier(uUID, "ftboss.material." + string, d, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent itemTooltipEvent) {
        ItemStack itemStack = itemTooltipEvent.getItemStack();
        FoodWeaponFamilyRules.Kind kind = FoodWeaponFamilyBuild.kind(itemStack);
        if (kind == FoodWeaponFamilyRules.Kind.MELEE || !FoodWeaponEvolution.isEvolutionCandidate(itemStack)) {
            return;
        }
        Stats stats = FoodWeaponFamilyBuild.stats(itemStack);
        itemTooltipEvent.getToolTip().add(Component.m_237110_((String)("tooltip.ftboss.weapon.family." + kind.name().toLowerCase(Locale.ROOT)), (Object[])new Object[]{FoodWeaponFamilyBuild.multiplier(stats.damageMultiplier()), FoodWeaponFamilyBuild.multiplier(stats.speedMultiplier()), FoodWeaponFamilyBuild.multiplier(kind == FoodWeaponFamilyRules.Kind.BOW ? stats.rangeMultiplier() : stats.manaMultiplier())}).m_130940_(ChatFormatting.AQUA));
    }

    private static String multiplier(double d) {
        return String.format(Locale.ROOT, "%.2f", d);
    }

    public record Stats(FoodWeaponFamilyRules.Kind kind, double damageMultiplier, double speedMultiplier, double rangeMultiplier, double manaMultiplier, int durability, double wear) {
    }
}

