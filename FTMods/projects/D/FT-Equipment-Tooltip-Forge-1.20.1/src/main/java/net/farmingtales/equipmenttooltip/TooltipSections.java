package net.farmingtales.equipmenttooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EquipmentSlot;

public final class TooltipSections {
    private TooltipSections() {
    }

    public record Section(EquipmentSlot slot, int start, int end) {
    }

    public static List<Section> find(List<Component> tooltip) {
        List<Section> sections = new ArrayList<>();
        for (int index = 1; index < tooltip.size(); index++) {
            EquipmentSlot slot = slotOf(tooltip.get(index));
            if (slot == null) {
                continue;
            }
            int end = index + 1;
            while (end < tooltip.size() && hasTranslation(tooltip.get(end), TooltipSections::isModifierKey)) {
                end++;
            }
            if (end > index + 1) {
                int start = tooltip.get(index - 1).getString().isBlank() ? index - 1 : index;
                sections.add(new Section(slot, start, end));
                index = end - 1;
            }
        }
        return sections;
    }

    public static boolean hasTranslation(Component component, Predicate<String> predicate) {
        if (component.getContents() instanceof TranslatableContents contents) {
            if (predicate.test(contents.getKey())) {
                return true;
            }
            for (Object argument : contents.getArgs()) {
                if (argument instanceof Component nested && hasTranslation(nested, predicate)) {
                    return true;
                }
            }
        }
        for (Component sibling : component.getSiblings()) {
            if (hasTranslation(sibling, predicate)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEquipmentIconLine(Component component) {
        if (!"obscure_api:icons".equals(component.getStyle().getFont().toString())) {
            return false;
        }
        String text = component.getString();
        return text.indexOf('\uEA11') >= 0 || text.indexOf('\uEA12') >= 0
            || text.indexOf('\uEA13') >= 0 || text.indexOf('\uEA14') >= 0
            || text.indexOf('\uEA18') >= 0;
    }

    private static EquipmentSlot slotOf(Component component) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (hasTranslation(component, ("item.modifiers." + slot.getName())::equals)) {
                return slot;
            }
        }
        return null;
    }

    private static boolean isModifierKey(String key) {
        return key.matches("attribute\\.modifier\\.(equals|plus|take)\\.[012]");
    }
}
