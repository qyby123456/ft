package cn.farmingtales.qysevent.worldevent;

import java.util.Arrays;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/** Daily events kept separate from seasonal weather, but hosted by qys-event. */
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
    STARFALL("starfall"),
    BLOOD_MOON("blood_moon"),
    GOLD_RUSH("gold_rush"),
    RABBIT_TIDE("rabbit_tide"),
    WITCH_CONVENTION("witch_convention"),
    GRAVITY_GLITCH("gravity_glitch"),
    MUSHROOM_BLOOM("mushroom_bloom"),
    ENDER_ECLIPSE("ender_eclipse"),
    FIREFLY_NIGHT("firefly_night"),
    LUCKY_DAY("lucky_day");

    private final String id;

    DailyWorldEvent(String id) { this.id = id; }
    public String id() { return id; }
    public Component title() { return Component.translatable("event.qys_event." + id + ".name"); }
    public Component description() { return Component.translatable("event.qys_event." + id + ".description"); }

    @Nullable
    public static DailyWorldEvent byId(String id) {
        if (id == null) return null;
        return Arrays.stream(values()).filter(event -> event.id.equals(id)).findFirst().orElse(null);
    }
}


