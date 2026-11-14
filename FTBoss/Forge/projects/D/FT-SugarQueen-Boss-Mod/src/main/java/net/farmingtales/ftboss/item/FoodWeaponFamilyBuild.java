package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
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

/** 食武类型适配与只读预览；2026-10-13 为无耐久法杖接入1024基础耐久，不改书、卷轴和万味胚刃。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class FoodWeaponFamilyBuild {
    private static final UUID SPELL_POWER = UUID.fromString("fb8a9bcc-a8fb-4079-8e58-c0f6fd84b3c7");
    private static final UUID CAST_SPEED = UUID.fromString("b5d8c37c-874c-4194-93b3-99735762c997");
    private static final UUID OFFHAND_SPELL_POWER = UUID.fromString("86ba6bfb-a3e0-4b68-a3ea-68c0bb8cdd9e");
    private static final UUID OFFHAND_CAST_SPEED = UUID.fromString("7c2bead8-fb2f-44e7-ad85-39d92627b119");

    public record Stats(Kind kind, double damageMultiplier, double speedMultiplier, double rangeMultiplier,
                        double manaMultiplier, int durability, double wear) {}

    private FoodWeaponFamilyBuild() {}

    public static boolean isEquipment(ItemStack stack) {
        return EquipmentWeaponBuild.applies(stack);
    }

    public static boolean isBuildCandidate(ItemStack stack) {
        return isEquipment(stack) || FourWeaponFoundationAdapter.applies(stack) || SwordCavityBuild.applies(stack)
                || FoodWeaponEvolution.isEvolutionCandidate(stack);
    }

    public static Kind kind(ItemStack stack) {
        if (stack.isEmpty() || stack.isEdible()) return Kind.MELEE;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || id.getNamespace().equals("create")) return Kind.MELEE;
        if (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem) return Kind.BOW;
        String path = id.getPath().toLowerCase(Locale.ROOT);
        if (path.contains("spellbook") || path.contains("scroll") || path.endsWith("_tome") || path.endsWith("_grimoire")) return Kind.MELEE;
        for (Class<?> type = stack.getItem().getClass(); type != null; type = type.getSuperclass()) {
            for (Class<?> contract : type.getInterfaces()) {
                if (contract.getName().equals("io.redspace.ironsspellbooks.api.item.ISpellbook")) return Kind.MELEE;
            }
        }
        for (Class<?> type = stack.getItem().getClass(); type != null; type = type.getSuperclass()) {
            if (type.getName().equals("io.redspace.ironsspellbooks.item.CastingItem")) return Kind.STAFF;
        }
        for (String token : List.of("staff", "wand", "scepter")) {
            if (path.equals(token) || path.startsWith(token + "_") || path.endsWith("_" + token)
                    || path.contains("_" + token + "_")) return Kind.STAFF;
        }
        return Kind.MELEE;
    }

    public static int baseDurability(ItemStack stack, int original) {
        return original > 0 || kind(stack) != Kind.STAFF ? original : FoodWeaponFamilyRules.STAFF_BASE_DURABILITY;
    }

    public static Build build(ItemStack stack) {
        if (FourWeaponFoundationAdapter.applies(stack)) return FourWeaponFoundationAdapter.combat(stack);
        if (SwordCavityBuild.applies(stack)) return SwordCavityBuild.build(stack);
        return FoodWeaponFamilyRules.build(kind(stack), WeaponMaterialBuild.materials(stack));
    }

    public static ItemStack heldBuildItem(net.minecraft.world.entity.player.Player player) {
        ItemStack main = player.getMainHandItem();
        if (isBuildCandidate(main)) return main;
        ItemStack offhand = player.getOffhandItem();
        return isBuildCandidate(offhand) ? offhand : ItemStack.EMPTY;
    }

    public static Build materialPreview(ItemStack stack, Part part, Material material) {
        if (FourWeaponFoundationAdapter.applies(stack)) return FourWeaponFoundationAdapter.combat(stack);
        if (SwordCavityBuild.applies(stack)) {
            for (var slot : net.farmingtales.ftboss.util.SwordCavityRules.Slot.values()) {
                if (slot.legacyPart == part) return SwordCavityBuild.build(SwordCavityBuild.preview(stack, slot, material));
            }
        }
        List<Material> materials = new ArrayList<>(WeaponMaterialBuild.materials(stack));
        materials.set(part.ordinal(), material);
        return FoodWeaponFamilyRules.build(kind(stack), materials);
    }

    public static Component partName(ItemStack stack, Part part) {
        if (kind(stack) == Kind.MELEE) return WeaponMaterialBuild.partName(part);
        return Component.translatable("part.ftboss.weapon." + kind(stack).name().toLowerCase(Locale.ROOT)
                + "." + part.name().toLowerCase(Locale.ROOT));
    }

    public static Stats stats(ItemStack stack) {
        Build build = build(stack);
        return new Stats(kind(stack), FoodWeaponFamilyRules.damageMultiplier(build), FoodWeaponFamilyRules.speedMultiplier(build),
                FoodWeaponFamilyRules.rangeMultiplier(build), FoodWeaponFamilyRules.manaMultiplier(kind(stack), build),
                stack.getMaxDamage(), build.wear());
    }

    public static Stats preview(ItemStack stack, Part part, Material material) {
        ItemStack prospective = stack.copy();
        prospective.getOrCreateTagElement(WeaponMaterialBuild.ROOT).putString(part.name(), material.id());
        return stats(prospective);
    }

    public static double damageMultiplier(ItemStack stack) { return FoodWeaponFamilyRules.damageMultiplier(build(stack)); }
    public static double rangeMultiplier(ItemStack stack) { return FoodWeaponFamilyRules.rangeMultiplier(build(stack)); }
    public static double manaMultiplier(ItemStack stack) { return FoodWeaponFamilyRules.manaMultiplier(kind(stack), build(stack)); }
    public static int chargeTicks(ItemStack stack, int original) {
        if (stack.getItem() instanceof CrossbowItem) return original;
        return FoodWeaponFamilyRules.chargeTicks(original, FoodWeaponFamilyRules.speedMultiplier(build(stack)));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void staffAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!EquipmentAttributeSlots.heldItem(stack)) return;
        if (event.getSlotType() != EquipmentSlot.MAINHAND && event.getSlotType() != EquipmentSlot.OFFHAND
                || kind(stack) != Kind.STAFF
                || stack.getTagElement(WeaponMaterialBuild.ROOT) == null && !FourWeaponFoundationAdapter.applies(stack)
                || FoodWeaponLifecycle.isBroken(stack)) return;
        boolean offhand = event.getSlotType() == EquipmentSlot.OFFHAND;
        addSpellAttribute(event, "spell_power", offhand ? OFFHAND_SPELL_POWER : SPELL_POWER, damageMultiplier(stack) - 1.0);
        addSpellAttribute(event, "cast_time_reduction", offhand ? OFFHAND_CAST_SPEED : CAST_SPEED,
                FoodWeaponFamilyRules.speedMultiplier(build(stack)) - 1.0);
    }

    private static void addSpellAttribute(ItemAttributeModifierEvent event, String path, UUID id, double amount) {
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", path));
        if (attribute != null && Math.abs(amount) > 0.00001) event.addModifier(attribute,
                new AttributeModifier(id, "ftboss.material." + path, amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Kind kind = kind(stack);
        if (kind == Kind.MELEE || !FoodWeaponEvolution.isEvolutionCandidate(stack)) return;
        Stats stats = stats(stack);
        event.getToolTip().add(Component.translatable("tooltip.ftboss.weapon.family." + kind.name().toLowerCase(Locale.ROOT),
                multiplier(stats.damageMultiplier()), multiplier(stats.speedMultiplier()),
                multiplier(kind == Kind.BOW ? stats.rangeMultiplier() : stats.manaMultiplier()))
                .withStyle(ChatFormatting.AQUA));
    }

    private static String multiplier(double value) { return String.format(Locale.ROOT, "%.2f", value); }
}
