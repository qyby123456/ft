package net.farmingtales.ftdungeonloot;

import java.lang.reflect.Method;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.ModList;

final class DungeonRounds {
    private static Method getData;
    private static Method activeRound;
    private static Method roundId;
    private static boolean reportedFailure;

    static UUID current(ServerLevel level, LootProfile profile) {
        if (profile.roundSource().equals("manual")) return LootRunData.get(level).run();
        if (!ModList.get().isLoaded("ftdungeonmobs")) return null;
        try {
            if (getData == null) {
                Class<?> api = Class.forName("net.farmingtales.ftdungeonmobs.DungeonGateData");
                Method discoveredGet = api.getMethod("get", MinecraftServer.class);
                Method discoveredActive = api.getMethod("roundActive");
                Method discoveredId = api.getMethod("roundId");
                activeRound = discoveredActive;
                roundId = discoveredId;
                getData = discoveredGet;
            }
            Object data = getData.invoke(null, level.getServer());
            return Boolean.TRUE.equals(activeRound.invoke(data)) ? (UUID) roundId.invoke(data) : null;
        } catch (ReflectiveOperationException | LinkageError | ClassCastException exception) {
            if (!reportedFailure) {
                FTDungeonLoot.LOGGER.error("FT dungeon round bridge unavailable; no loot or payment allowed", exception);
                reportedFailure = true;
            }
            return null;
        }
    }
}
