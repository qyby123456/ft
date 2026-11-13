package net.farmingtales.equipmenttooltip;

import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = EquipmentTooltipMod.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EquipmentTooltip {
    private static final String PREFIX = "ft_equipment_tooltip.";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> WARNED_ITEMS = new HashSet<>();
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private EquipmentTooltip() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {
        try {
            rewrite(event.getItemStack(), event.getToolTip(), Screen.hasShiftDown());
        } catch (RuntimeException exception) {
            String item = event.getItemStack().getDescriptionId();
            if (WARNED_ITEMS.add(item)) {
                LOGGER.warn("Could not summarize equipment tooltip for {}; leaving the original tooltip intact", item, exception);
            }
        }
    }

    public static void rewrite(ItemStack stack, List<Component> tooltip, boolean showDetails) {
        rewrite(tooltip, showDetails, slot -> summarizeSlot(stack, slot), () ->
            stack.isDamageableItem() ? Component.translatable(PREFIX + "durability",
                stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage())
                .withStyle(ChatFormatting.GRAY) : null);
    }

    static void rewrite(List<Component> tooltip, boolean showDetails,
            Function<EquipmentSlot, List<Component>> summarize, Supplier<Component> durability) {
        List<TooltipSections.Section> sections = TooltipSections.find(tooltip);
        if (sections.isEmpty() || tooltip.stream().anyMatch(line ->
                TooltipSections.hasTranslation(line, (PREFIX + "header")::equals))) {
            return;
        }

        List<Component> summary = new ArrayList<>();
        List<TooltipSections.Section> summarized = new ArrayList<>();
        for (TooltipSections.Section section : sections) {
            List<Component> rows = summarize.apply(section.slot());
            if (!rows.isEmpty()) {
                summary.add(Component.translatable(PREFIX + "header",
                    Component.translatable(PREFIX + "slot." + section.slot().getName()))
                    .withStyle(ChatFormatting.GOLD));
                summary.addAll(rows);
                summarized.add(section);
            }
        }
        if (summarized.isEmpty()) {
            return;
        }
        summary.add(Component.translatable(PREFIX + (showDetails ? "details_shown" : "hold_shift"))
            .withStyle(ChatFormatting.DARK_GRAY));

        boolean[] remove = new boolean[tooltip.size()];
        if (!showDetails) {
            for (TooltipSections.Section section : summarized) {
                for (int index = section.start(); index < section.end(); index++) {
                    remove[index] = true;
                }
            }
        }
        boolean restoreDurability = false;
        for (int index = 1; index < tooltip.size(); index++) {
            if (TooltipSections.isEquipmentIconLine(tooltip.get(index))) {
                restoreDurability |= tooltip.get(index).getString().indexOf('\uEA0B') >= 0;
                remove[index] = true;
            }
        }
        if (restoreDurability && tooltip.stream().noneMatch(line ->
                TooltipSections.hasTranslation(line, "item.durability"::equals))) {
            Component durabilityLine = durability.get();
            if (durabilityLine != null) {
                summary.add(durabilityLine);
            }
        }
        List<Component> result = new ArrayList<>();
        result.add(tooltip.get(0));
        result.addAll(summary);
        for (int index = 1; index < tooltip.size(); index++) {
            if (!remove[index]) {
                result.add(tooltip.get(index));
            }
        }
        tooltip.clear();
        tooltip.addAll(result);
    }

    private static List<Component> summarizeSlot(ItemStack stack, EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> modifiers = stack.getAttributeModifiers(slot);
        List<Map.Entry<Attribute, Collection<AttributeModifier>>> attributes = new ArrayList<>(modifiers.asMap().entrySet());
        attributes.sort(Comparator.comparingInt((Map.Entry<Attribute, Collection<AttributeModifier>> entry) ->
            priority(entry.getKey())).thenComparing(entry -> entry.getKey().getDescriptionId()));
        List<Component> entries = new ArrayList<>();
        for (Map.Entry<Attribute, Collection<AttributeModifier>> entry : attributes) {
            AttributeTotals totals = AttributeTotals.from(entry.getValue());
            if (!totals.isFinite()) {
                return List.of();
            }
            Attribute attribute = entry.getKey();
            Component value = valueFor(stack, slot, attribute, totals);
            entries.add(Component.translatable(PREFIX + "entry",
                Component.translatable(attribute.getDescriptionId()), value).withStyle(ChatFormatting.GRAY));
        }
        List<Component> rows = new ArrayList<>();
        MutableComponent row = Component.empty();
        int columns = 0;
        int width = 0;
        for (Component entry : entries) {
            int entryWidth = displayWidth(entry.getString());
            if (columns > 0 && (columns == 2 || width + entryWidth > 48)) {
                rows.add(row);
                row = Component.empty();
                columns = 0;
                width = 0;
            }
            if (columns > 0) {
                row.append(Component.literal("  |  ").withStyle(ChatFormatting.DARK_GRAY));
            }
            row.append(entry);
            columns++;
            width += entryWidth + 5;
        }
        if (columns > 0) {
            rows.add(row);
        }
        return rows;
    }

    public static Component valueFor(ItemStack stack, EquipmentSlot slot, Attribute attribute, AttributeTotals totals) {
        if (slot == EquipmentSlot.MAINHAND && attribute == Attributes.ATTACK_DAMAGE) {
            double damage = attribute.sanitizeValue(totals.apply(1.0));
            damage += EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
            return Component.literal(format(damage)).withStyle(ChatFormatting.WHITE);
        }
        if (slot == EquipmentSlot.MAINHAND && attribute == Attributes.ATTACK_SPEED) {
            return Component.translatable(PREFIX + "per_second",
                format(attribute.sanitizeValue(totals.apply(4.0)))).withStyle(ChatFormatting.WHITE);
        }
        double flat = totals.flatContribution();
        if (Math.abs(totals.multiplier() - 1.0) < 1.0E-10) {
            return Component.literal(formatFlat(attribute, flat)).withStyle(
                flat < 0.0 ? ChatFormatting.RED : ChatFormatting.WHITE);
        }
        if (Math.abs(flat) < 1.0E-10) {
            double percentage = (totals.multiplier() - 1.0) * 100.0;
            return Component.literal((percentage > 0.0 ? "+" : "") + format(percentage) + "%")
                .withStyle(percentage < 0.0 ? ChatFormatting.RED : ChatFormatting.WHITE);
        }
        return Component.translatable(PREFIX + "mixed", format(totals.multiplier()), formatFlat(attribute, flat))
            .withStyle(ChatFormatting.WHITE);
    }

    private static String formatFlat(Attribute attribute, double value) {
        return attribute == Attributes.KNOCKBACK_RESISTANCE ? format(value * 100.0) + "%" : format(value);
    }

    private static String format(double value) {
        return FORMAT.format(Math.abs(value) < 0.005 ? 0.0 : value);
    }

    private static int displayWidth(String text) {
        return text.codePoints().map(character -> character > 255 ? 2 : 1).sum();
    }

    private static int priority(Attribute attribute) {
        if (attribute == Attributes.ATTACK_DAMAGE) return 0;
        if (attribute == Attributes.ATTACK_SPEED) return 1;
        if (attribute == Attributes.ARMOR) return 2;
        if (attribute == Attributes.ARMOR_TOUGHNESS) return 3;
        if (attribute == Attributes.KNOCKBACK_RESISTANCE) return 4;
        return 5;
    }
}
