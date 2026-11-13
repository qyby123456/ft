package ftboss.bossprobe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class BossNaturalLoop {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String TAG = "ftboss_split_natural";
    private static Map<String, Object> report;
    private static final Set<UUID> originalIds = new HashSet<>();
    private static int ticks;
    private static int snapshots;
    private static boolean active;

    private BossNaturalLoop() {}

    private static void save(String name, Object value) throws Exception {
        Files.writeString(Path.of(name), GSON.toJson(value));
    }

    static void start(MinecraftServer server, Map<String, Object> startup) throws Exception {
        report = new LinkedHashMap<>(startup);
        report.put("fullBossPortAccepted", false);
        report.put("authenticatedCombatAccepted", false);
        report.put("clientRenderingAccepted", false);
        if (((Number) report.get("failures")).intValue() != 0) {
            save("actual-independent-boss-loop.json", report);
            return;
        }
        try {
            Map<String, Object> registries = new LinkedHashMap<>();
            registries.put("items", BuiltInRegistries.ITEM.keySet().stream().filter(key -> key.getNamespace().equals("ftboss")).map(Object::toString).sorted().toList());
            registries.put("entities", BuiltInRegistries.ENTITY_TYPE.keySet().stream().filter(key -> key.getNamespace().equals("ftboss")).map(Object::toString).sorted().toList());
            registries.put("menus", BuiltInRegistries.MENU.keySet().stream().filter(key -> key.getNamespace().equals("ftboss")).map(Object::toString).sorted().toList());
            save("actual-boss-core-registries.json", registries);
            ServerLevel world = server.overworld();
            int centerX = world.getSharedSpawnPos().getX();
            int centerZ = world.getSharedSpawnPos().getZ();
            List<Map<String, Object>> commands = new ArrayList<>();
            String force = "forceload add " + (centerX - 80) + " " + (centerZ - 80) + " " + (centerX + 80) + " " + (centerZ + 80);
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), force);
            for (int difficultyIndex = 0; difficultyIndex < 2; difficultyIndex++) {
                for (int variant = 0; variant < 12; variant++) {
                    int slot = difficultyIndex * 12 + variant;
                    int coordinateX = centerX - 50 + (slot % 6) * 20;
                    int coordinateZ = centerZ - 30 + (slot / 6) * 20;
                    world.getChunk(coordinateX >> 4, coordinateZ >> 4);
                    int coordinateY = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, coordinateX, coordinateZ);
                    String difficulty = difficultyIndex == 0 ? "normal" : "impossible";
                    String command = "summon ftboss:easter_boss " + coordinateX + " " + coordinateY + " " + coordinateZ
                        + " {variant:" + variant + ",ManorDifficulty:\"" + difficulty + "\",Tags:[\"" + TAG + "\",\"" + difficulty + "-" + variant + "\"]}";
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
                    commands.add(Map.of("command", command, "nativeCommandDispatched", true));
                }
            }
            for (Entity actor : world.getAllEntities()) {
                if (actor.getTags().contains(TAG)) originalIds.add(actor.getUUID());
            }
            if (originalIds.size() != 24) throw new AssertionError("Expected 24 actual original Boss UUIDs");
            save("actual-boss-native-commands.json", commands);
            report.put("bossIndependentJarStartupAccepted", true);
            report.put("actualNaturalBossCount", originalIds.size());
            active = true;
        } catch (Throwable failure) {
            finishFailure(failure);
        }
    }

    public static void tick(ServerTickEvent.Post event) {
        if (!active) return;
        try {
            ticks++;
            if (ticks % 20 != 0) return;
            List<Map<String, Object>> actors = new ArrayList<>();
            Set<UUID> currentIds = new HashSet<>();
            for (ServerLevel world : event.getServer().getAllLevels()) {
                for (Entity actor : world.getAllEntities()) {
                    if (!actor.getTags().contains(TAG)) continue;
                    currentIds.add(actor.getUUID());
                    if (!(actor instanceof LivingEntity living) || !living.isAlive() || actor.isRemoved()) throw new AssertionError("Original Boss not alive");
                    if (!(actor instanceof Mob mob) || mob.isNoAi()) throw new AssertionError("Original Boss AI disabled");
                    CompoundTag saved = actor.saveWithoutId(new CompoundTag());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("uuid", actor.getUUID().toString());
                    row.put("type", BuiltInRegistries.ENTITY_TYPE.getKey(actor.getType()).toString());
                    row.put("age", actor.tickCount);
                    row.put("health", living.getHealth());
                    row.put("maxHealth", living.getMaxHealth());
                    row.put("variant", saved.getInt("variant"));
                    row.put("difficulty", saved.getString("ManorDifficulty"));
                    row.put("noAi", mob.isNoAi());
                    row.put("hasTarget", mob.getTarget() != null);
                    row.put("position", List.of(actor.getX(), actor.getY(), actor.getZ()));
                    row.put("nbt", saved.toString());
                    actors.add(row);
                    if (ticks >= 660 && actor.tickCount < 640) throw new AssertionError("Original Boss age failed to advance naturally");
                }
            }
            if (!currentIds.equals(originalIds)) throw new AssertionError("Original Boss UUID set changed");
            snapshots++;
            Map<String, Object> frame = Map.of("postTicks", ticks, "actors", actors, "players", event.getServer().getPlayerList().getPlayerCount());
            Files.writeString(Path.of("actual-boss-natural-frames.jsonl"), new Gson().toJson(frame) + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            save("actual-boss-loop-progress.json", Map.of("ticks", ticks, "snapshots", snapshots, "originalBosses", actors.size()));
            if (ticks >= 660) {
                active = false;
                report.put("actualPostTicks", ticks);
                report.put("snapshots", snapshots);
                report.put("naturalBossAiLoopAccepted", true);
                report.put("noHealthAgeAiOrUuidReset", true);
                report.put("players", event.getServer().getPlayerList().getPlayerCount());
                save("actual-independent-boss-loop.json", report);
            }
        } catch (Throwable failure) {
            try { finishFailure(failure); }
            catch (Exception recordingFailure) { throw new IllegalStateException(recordingFailure); }
        }
    }

    private static void finishFailure(Throwable failure) throws Exception {
        active = false;
        report.put("failures", 1);
        report.put("failure", failure.toString());
        java.io.StringWriter trace = new java.io.StringWriter();
        failure.printStackTrace(new java.io.PrintWriter(trace));
        report.put("trace", trace.toString());
        report.put("actualPostTicks", ticks);
        save("actual-independent-boss-loop.json", report);
    }
}
