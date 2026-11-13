package cn.farmingtales.qysevent;

import java.util.HashSet;
import java.util.Set;
import cn.farmingtales.qysevent.worldevent.DailyWorldEvent;
import cn.farmingtales.qysevent.worldevent.DailyWorldEventRules;

public final class QysEventVerifier {
    public static void main(String[] args) {
        SeasonIntegrationVerifier.verify();
        CoreFacingVerifier.verify();
        WishingWellShopVerifier.verify();
        cn.farmingtales.qysevent.wishingwell.GoldPotWishVerifier.verify();
        DailyWorldEvent[] events = DailyWorldEvent.values();
        require(events.length == 19, "expected 19 daily events, got " + events.length);
        Set<String> ids = new HashSet<>();
        for (DailyWorldEvent event : events) {
            require(ids.add(event.id()), "duplicate event id: " + event.id());
            require(DailyWorldEvent.byId(event.id()) == event, "byId failed: " + event.id());
        }
        require(DailyWorldEvent.byId("redstone_storm") == null, "weather duplicate returned to daily events");
        require(DailyWorldEvent.byId(null) == null && DailyWorldEvent.byId("") == null, "empty event ID changed");
        for (DailyWorldEvent event : events) {
            require(DailyWorldEvent.byId(new String(event.id())) == event, "event ID must use value equality");
            require(DailyWorldEvent.byId(event.id().toUpperCase(java.util.Locale.ROOT)) == null, "event IDs became case-insensitive");
        }
        for (long seed : new long[]{0L, 1L, -1L, 9876543212345L}) {
            Set<Integer> cycle = new HashSet<>();
            for (int day = 0; day < events.length; day++) {
                int index = DailyWorldEventRules.eventIndex(seed, day, events.length);
                require(index >= 0 && index < events.length, "candidate out of range");
                require(cycle.add(index), "candidate repeated within deterministic event cycle");
            }
        }
        require(DailyWorldEventRules.kitchenHarvestMultiplier(0.0D) == 1, "kitchen min is not 1x");
        require(DailyWorldEventRules.kitchenHarvestMultiplier(0.34D) == 2, "kitchen middle is not 2x");
        require(DailyWorldEventRules.kitchenHarvestMultiplier(0.99D) == 3, "kitchen max is not 3x");

        double[] heavySnowRolls = {0.0D, .55D, .71D, .84D, .90D, .94D, .96D, .975D, .985D, .995D};
        int[] heavySnowResults = {
                DailyWorldEventRules.HEAVY_SNOW_UNCHANGED,
                DailyWorldEventRules.HEAVY_SNOW_FLY,
                DailyWorldEventRules.HEAVY_SNOW_MAGGOT,
                DailyWorldEventRules.HEAVY_SNOW_POOP,
                DailyWorldEventRules.HEAVY_SNOW_COMPOST_SLIME,
                DailyWorldEventRules.HEAVY_SNOW_SHEEP_DUNG,
                DailyWorldEventRules.HEAVY_SNOW_APPLE_WORM,
                DailyWorldEventRules.HEAVY_SNOW_APPLE_MOTH,
                DailyWorldEventRules.HEAVY_SNOW_ROTTEN_APPLE,
                DailyWorldEventRules.HEAVY_SNOW_GIANT_GRUB
        };
        for (int i = 0; i < heavySnowRolls.length; i++) {
            require(DailyWorldEventRules.heavySnowOutcome(heavySnowRolls[i]) == heavySnowResults[i],
                    "heavy-snow spawn boundary failed at " + heavySnowRolls[i]);
        }
        Set<String> heavySnowEntities = new HashSet<>();
        for (int result = DailyWorldEventRules.HEAVY_SNOW_FLY;
             result <= DailyWorldEventRules.HEAVY_SNOW_GIANT_GRUB; result++) {
            String path = DailyWorldEventRules.heavySnowEntityPath(result);
            require(path != null && heavySnowEntities.add(path), "bad or duplicate heavy-snow entity path");
        }
        require(heavySnowEntities.size() == 9 && heavySnowEntities.contains("giant_grub"),
                "heavy-snow boss-minion pool is incomplete");
        int creatureSpawnEvents = 0;
        for (DailyWorldEvent event : events) {
            if (DailyWorldEventRules.hasCreatureSpawns(event)) creatureSpawnEvents++;
        }
        require(creatureSpawnEvents == 11, "expected 11 creature-spawn events, got " + creatureSpawnEvents);
        require(!DailyWorldEventRules.hasCreatureSpawns(DailyWorldEvent.BLOOD_MOON),
                "blood moon should buff mobs, not create an event-spawn pulse");
        require(DailyWorldEventRules.HEAVY_SNOW_PRECIPITATION_COLOR == 0x6B3E23,
                "heavy-snow rain must stay brown");
        require(DailyWorldEventRules.HEAVY_SNOW_PULSE_INTERVAL_TICKS == 200,
                "heavy-snow creature pulse must remain visible every 10 seconds");
        require(DailyWorldEventRules.HEAVY_SNOW_PREVIEW_SPAWNS_PER_PLAYER == 3,
                "heavy-snow test preview must spawn a visible group");
        require(DailyWorldEventRules.HEAVY_SNOW_PREVIEW_MIN_RADIUS == 4.0D
                        && DailyWorldEventRules.HEAVY_SNOW_PREVIEW_MAX_RADIUS == 12.0D,
                "heavy-snow preview must stay close enough to see indoors");
        require(DailyWorldEventRules.HEAVY_SNOW_PULSE_MIN_RADIUS == 8.0D
                        && DailyWorldEventRules.HEAVY_SNOW_PULSE_MAX_RADIUS == 20.0D,
                "heavy-snow pulse must stay in the nearby loaded area");
        require(!DailyWorldEventRules.HEAVY_SNOW_PULSE_REQUIRES_SKY,
                "heavy-snow pulse must work in caves and indoor test arenas");
        require(DailyWorldEventRules.HEAVY_SNOW_PULSE_INTERVAL_TICKS == 200,
                "heavy-snow creature pulse must remain visible every 10 seconds");
        require(DailyWorldEventRules.HEAVY_SNOW_PREVIEW_SPAWNS_PER_PLAYER == 3,
                "heavy-snow test preview must spawn a visible group");
        require(DailyWorldEventRules.HEAVY_SNOW_PREVIEW_MIN_RADIUS == 4.0D
                        && DailyWorldEventRules.HEAVY_SNOW_PREVIEW_MAX_RADIUS == 12.0D,
                "heavy-snow preview must stay close enough to see indoors");
        require(DailyWorldEventRules.HEAVY_SNOW_PULSE_MIN_RADIUS == 8.0D
                        && DailyWorldEventRules.HEAVY_SNOW_PULSE_MAX_RADIUS == 20.0D,
                "heavy-snow pulse must stay in the nearby loaded area");
        require(!DailyWorldEventRules.HEAVY_SNOW_PULSE_REQUIRES_SKY,
                "heavy-snow pulse must work in caves and indoor test arenas");

        require(WeatherType.values().length == 33, "expected 32 special weather entries plus NONE");
        require(WeatherType.REDSTONE_STORM.precipitationColor() == 0xCF3B35, "redstone rain tint changed");
        require(WeatherType.BLIZZARD.precipitationColor() == 0xFFFFFF, "blizzard must keep vanilla snow color");
        System.out.println("qys-event verified: 19 daily events, 11 capped creature-spawn events, 32 special weathers, brown Heavy Snow rain, 9 boss-minion outcomes and 1-3x kitchen yield.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
