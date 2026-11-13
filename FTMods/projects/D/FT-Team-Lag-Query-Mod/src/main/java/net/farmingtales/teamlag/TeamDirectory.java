package net.farmingtales.teamlag;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class TeamDirectory {
    record Team(UUID id, String name, int members, int online) {}
    record Snapshot(Map<ChunkKey, UUID> claims, Map<UUID, Team> teams) {
        List<ChunkKey> chunks(UUID id) { return claims.entrySet().stream().filter(entry -> id.equals(entry.getValue())).map(Map.Entry::getKey).toList(); }
    }
    private TeamDirectory() {}
    private static Object call(Object target, String method) throws ReflectiveOperationException { return target.getClass().getMethod(method).invoke(target); }
    private static Object api(String name) throws ReflectiveOperationException { return Class.forName(name).getMethod("api").invoke(null); }
    static Object manager() throws ReflectiveOperationException { return call(api("dev.ftb.mods.ftbteams.api.FTBTeamsAPI"), "getManager"); }
    static UUID playerTeam(UUID player) throws ReflectiveOperationException {
        Object manager = manager();
        Optional<?> result = (Optional<?>) manager.getClass().getMethod("getTeamForPlayerID", UUID.class).invoke(manager, player);
        return result.isPresent() ? (UUID) call(result.get(), "getId") : null;
    }
    static Team team(UUID id) throws ReflectiveOperationException {
        Object manager = manager();
        Optional<?> result = (Optional<?>) manager.getClass().getMethod("getTeamByID", UUID.class).invoke(manager, id);
        if (result.isEmpty()) return null;
        Object handle = result.get();
        return new Team(id, ((net.minecraft.network.chat.Component) call(handle, "getName")).getString(),
                ((Collection<?>) call(handle, "getMembers")).size(), ((Collection<?>) call(handle, "getOnlineMembers")).size());
    }
    static Snapshot snapshot() throws ReflectiveOperationException {
        Object manager = call(api("dev.ftb.mods.ftbchunks.api.FTBChunksAPI"), "getManager");
        Collection<?> all = (Collection<?>) call(manager, "getAllClaimedChunks");
        Map<ChunkKey, UUID> claims = new HashMap<>();
        Map<UUID, Team> teams = new HashMap<>();
        for (Object claim : all) {
            Object pos = call(claim, "getPos");
            String dimension = String.valueOf(call(call(pos, "dimension"), "location"));
            UUID id = (UUID) call(call(call(claim, "getTeamData"), "getTeam"), "getId");
            claims.put(new ChunkKey(dimension, (Integer) call(pos, "x"), (Integer) call(pos, "z")), id);
            if (!teams.containsKey(id)) {
                Team team = team(id);
                if (team != null) teams.put(id, team);
            }
        }
        return new Snapshot(Map.copyOf(claims), Map.copyOf(teams));
    }
    static UUID resolve(String target, Snapshot snapshot) throws ReflectiveOperationException {
        try { return UUID.fromString(target); } catch (IllegalArgumentException ignored) { }
        var server = LagQuery.server();
        var player = server == null ? null : server.getPlayerList().getPlayerByName(target);
        if (player != null) return playerTeam(player.getUUID());
        List<Team> matches = snapshot.teams().values().stream().filter(team -> team.name().equalsIgnoreCase(target)).toList();
        if (matches.size() > 1) throw new IllegalArgumentException("存在同名团队，请使用完整团队 UUID");
        return matches.isEmpty() ? null : matches.get(0).id();
    }
}
