package net.farmingtales.ftboss.worldevent;

import java.util.Arrays;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/** The ten-entry shuffle bag used by the daily world-event calendar. */
public enum DailyWorldEvent {
    BIG_FISH_MIGRATION("big_fish_migration"),
    KITCHEN_CHAOS("kitchen_chaos"),
    NO_SUCH_CREATURE("no_such_creature"),
    UNDEAD_MIGRATION("undead_migration"),
    PIG_MIGRATION("pig_migration"),
    HEAVY_SNOW("heavy_snow"),
    KOBE_MEMORIAL("kobe_memorial"),
    HARVEST_MOON("harvest_moon"),
    SLIME_RAIN("slime_rain"),
    STARFALL("starfall");

    private final String id;

    DailyWorldEvent(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public Component title() {
        return Component.translatable("event.ftboss." + this.id + ".name");
    }

    public Component description() {
        return Component.translatable("event.ftboss." + this.id + ".description");
    }

    @Nullable
    public static DailyWorldEvent byId(String id) {
        return Arrays.stream(values()).filter(event -> event.id.equals(id)).findFirst().orElse(null);
    }
}