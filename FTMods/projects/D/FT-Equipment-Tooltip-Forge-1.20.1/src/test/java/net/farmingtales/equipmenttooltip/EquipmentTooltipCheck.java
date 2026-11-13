package net.farmingtales.equipmenttooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public final class EquipmentTooltipCheck {
    private static int checks;

    public static void main(String[] arguments) throws Exception {
        checkMath();
        checkSections();
        checkRewriting();
        for (String type : List.of("EquipmentTooltipMod", "EquipmentTooltip", "AttributeTotals", "TooltipSections")) {
            Class.forName("net.farmingtales.equipmenttooltip." + type, false,
                EquipmentTooltipCheck.class.getClassLoader()).getDeclaredMethods();
            check(true, "link " + type);
        }
        System.out.println("PASS " + checks + " checks: real Minecraft attribute math, tooltip sections, rewrite and linkage");
    }

    private static void checkMath() {
        AttributeTotals damage = AttributeTotals.from(List.of(
            modifier(7.0, Operation.ADDITION), modifier(2.0, Operation.ADDITION),
            modifier(0.2, Operation.MULTIPLY_BASE), modifier(-0.1, Operation.MULTIPLY_BASE),
            modifier(0.5, Operation.MULTIPLY_TOTAL), modifier(-0.2, Operation.MULTIPLY_TOTAL)));
        close(damage.apply(1.0), 13.2, "combined damage");
        close(damage.flatContribution(), 11.88, "net flat contribution");
        close(AttributeTotals.from(List.of(modifier(-2.4, Operation.ADDITION))).apply(4.0), 1.6, "attack speed");
        close(AttributeTotals.from(List.of(modifier(0.1, Operation.MULTIPLY_TOTAL),
            modifier(0.2, Operation.MULTIPLY_TOTAL))).multiplier(), 1.32, "total multipliers multiply");
        UUID shared = UUID.randomUUID();
        close(AttributeTotals.from(List.of(new AttributeModifier(shared, "first", 3.0, Operation.ADDITION),
            new AttributeModifier(shared, "second", 5.0, Operation.ADDITION))).apply(0.0), 5.0, "UUID deduplication");
        check(!AttributeTotals.from(List.of(modifier(Double.NaN, Operation.ADDITION))).isFinite(), "reject NaN");
        check(!AttributeTotals.from(List.of(modifier(Double.POSITIVE_INFINITY, Operation.MULTIPLY_TOTAL))).isFinite(), "reject infinity");
        Random random = new Random(20261027L);
        RangedAttribute attribute = new RangedAttribute("test.attribute", 0.0, -1.0E12, 1.0E12);
        for (int sample = 0; sample < 1000; sample++) {
            double base = random.nextDouble() * 50.0;
            AttributeInstance minecraft = new AttributeInstance(attribute, ignored -> { });
            minecraft.setBaseValue(base);
            List<AttributeModifier> modifiers = new ArrayList<>();
            for (int index = 0; index < 12; index++) {
                Operation operation = Operation.values()[index % 3];
                double amount = operation == Operation.ADDITION ? random.nextDouble() * 20.0 - 10.0
                    : random.nextDouble() * 0.8 - 0.4;
                AttributeModifier modifier = modifier(amount, operation);
                modifiers.add(modifier);
                minecraft.addTransientModifier(modifier);
            }
            AttributeTotals totals = AttributeTotals.from(modifiers);
            close(totals.apply(base), minecraft.getValue(), "Minecraft comparison " + sample);
            close(base * totals.multiplier() + totals.flatContribution(), minecraft.getValue(), "affine comparison " + sample);
        }
    }

    private static void checkSections() {
        List<Component> tooltip = original();
        List<TooltipSections.Section> sections = TooltipSections.find(tooltip);
        check(sections.size() == 2, "two slots");
        check(sections.get(0).slot() == EquipmentSlot.MAINHAND, "mainhand separate");
        check(sections.get(1).slot() == EquipmentSlot.OFFHAND, "offhand separate");
        check(sections.get(0).end() == 8, "stop before lore");
        check(TooltipSections.find(List.of(Component.literal("Name"), modifierLine())).isEmpty(), "ignore potion modifier lines");
        check(TooltipSections.find(List.of(Component.literal("Name"), Component.literal("Lore"))).isEmpty(), "hidden attributes stay hidden");
        check(TooltipSections.isEquipmentIconLine(tooltip.get(1)), "recognize equipment icons");
        check(!TooltipSections.isEquipmentIconLine(tooltip.get(2)), "preserve food icons");
        check(!TooltipSections.isEquipmentIconLine(Component.literal("\uEA11")), "do not remove unrelated font");
        check(TooltipSections.hasTranslation(Component.translatable("outer", Component.empty()
            .append(modifierLine())), key -> key.equals("attribute.modifier.plus.0")), "nested translations");
    }

    private static void checkRewriting() {
        List<Component> original = original();
        List<Component> compact = new ArrayList<>(original);
        EquipmentTooltip.rewrite(compact, false, slot -> List.of(Component.literal("Summary " + slot.getName())),
            () -> Component.literal("Durability preserved"));
        check(compact.get(0) == original.get(0), "preserve name identity");
        check(compact.stream().noneMatch(TooltipSections::isEquipmentIconLine), "remove duplicate icons");
        check(compact.contains(original.get(2)), "preserve food icons");
        check(compact.contains(original.get(3)), "preserve enchantment");
        check(compact.contains(original.get(8)), "preserve skill lore");
        check(compact.contains(original.get(12)), "preserve debug info");
        check(TooltipSections.find(compact).isEmpty(), "collapse original attribute blocks");
        check(compact.stream().filter(line -> line.getString().startsWith("Summary ")).count() == 2, "two independent summaries");
        check(compact.stream().anyMatch(line -> line.getString().equals("Durability preserved")), "restore durability");
        int size = compact.size();
        EquipmentTooltip.rewrite(compact, false, slot -> { throw new AssertionError("repeated rewrite"); }, () -> null);
        check(compact.size() == size, "idempotent rewrite");
        List<Component> expanded = new ArrayList<>(original);
        EquipmentTooltip.rewrite(expanded, true, slot -> List.of(Component.literal("Summary")), () -> null);
        check(TooltipSections.find(expanded).size() == 2, "Shift keeps originals");
        check(expanded.contains(original.get(6)) && expanded.contains(original.get(7)), "Shift keeps all modifiers");
        List<Component> failed = new ArrayList<>(original);
        EquipmentTooltip.rewrite(failed, false, slot -> List.of(), () -> null);
        check(failed.equals(original), "unavailable summary leaves original untouched");
        try {
            EquipmentTooltip.rewrite(failed, false, slot -> { throw new IllegalArgumentException("test"); }, () -> null);
            throw new AssertionError("expected calculation failure");
        } catch (IllegalArgumentException expected) {
            check(failed.equals(original), "calculation failure is atomic");
        }
        List<Component> partial = new ArrayList<>(original);
        EquipmentTooltip.rewrite(partial, false, slot -> slot == EquipmentSlot.MAINHAND
            ? List.of(Component.literal("Summary")) : List.of(), () -> null);
        check(TooltipSections.find(partial).size() == 1, "preserve unsupported slot");
        check(TooltipSections.find(partial).get(0).slot() == EquipmentSlot.OFFHAND, "unsupported offhand retained");
    }

    private static List<Component> original() {
        Style icons = Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath("obscure_api", "icons"));
        return List.of(Component.literal("Sword"),
            Component.literal("\uEA11 12 \uEA12 1.6 \uEA0B 90/100").setStyle(icons),
            Component.literal("\uEA19 4 \uEA20 2").setStyle(icons),
            Component.translatable("enchantment.minecraft.sharpness"),
            Component.empty(), Component.translatable("item.modifiers.mainhand"), modifierLine(),
            Component.literal(" ").append(Component.translatable("attribute.modifier.equals.0", "1.6",
                Component.translatable("attribute.name.generic.attack_speed"))),
            Component.literal("Special skill and set bonus"),
            Component.empty(), Component.translatable("item.modifiers.offhand"), modifierLine(),
            Component.literal("minecraft:diamond_sword"));
    }

    private static Component modifierLine() {
        return Component.translatable("attribute.modifier.plus.0", "3", Component.translatable("attribute.name.generic.attack_damage"));
    }

    private static AttributeModifier modifier(double amount, Operation operation) {
        return new AttributeModifier(UUID.randomUUID(), "test", amount, operation);
    }

    private static void close(double actual, double expected, String message) {
        check(Math.abs(actual - expected) <= 1.0E-9 * Math.max(1.0, Math.abs(expected)), message + ": " + actual + " != " + expected);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
        checks++;
    }
}
