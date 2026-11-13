package net.farmingtales.ftdungeonmobs.generator;

import java.util.List;
import java.util.ArrayList;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

final class ChaosDungeonGenerator implements DungeonGenerator {
    private static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation("ftmaze", "depths"));
    private static final String RETURN = "FTChaosReturn";
    private static final TicketType<ChunkPos> CLEANUP_TICKET = TicketType.create("ft_chaos_cleanup",
            java.util.Comparator.comparingLong(ChunkPos::toLong));

    public ResourceLocation id() { return DungeonGenerators.CHAOS; }
    public Component title() { return Component.literal("混沌地牢 · 强制连通房网"); }
    public ItemStack icon() { return new ItemStack(Items.CHISELED_STONE_BRICKS); }
    public boolean ownsDimension(ServerLevel level) { return level.dimension().equals(DIMENSION); }

    private static State state(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(State::load, State::new, "ft_chaos_r5");
    }

    private static int score(MinecraftServer server, String name) {
        var objective = server.getScoreboard().getObjective("ftm_state");
        if (objective == null || !server.getScoreboard().hasPlayerScore(name, objective)) return 0;
        return server.getScoreboard().getOrCreatePlayerScore(name, objective).getScore();
    }

    private static void command(MinecraftServer server, String text) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withPermission(4)
                .withSuppressedOutput(), text);
    }

    private static void function(MinecraftServer server, String path) {
        if (server.getFunctions().get(new ResourceLocation("ftmaze", path)).isEmpty())
            throw new IllegalStateException("混沌地牢资源缺失：" + path);
        command(server, "function ftmaze:" + path);
    }

    public Status status(MinecraftServer server, String instance) {
        if (instance.isEmpty()) return new Status(Phase.UNPLANNED,
                Component.literal("四层混合房网；既有房间和成品走廊强制连通。生成时冻结门旁1～16名参战者。"));
        State saved = state(server);
        if (!instance.equals(saved.data.getString("Instance"))) return new Status(Phase.UNAVAILABLE,
                Component.literal("混沌房网实例记录不匹配，入口保持关闭"));
        int stage = score(server, "#run");
        if (saved.data.getBoolean("Cleaning")) return new Status(Phase.BUILDING,
                Component.literal("正在清理旧混沌地牢：" + saved.data.getInt("CleanupIndex") + "/" + cleanupBoxes(server, saved).size()));
        if (stage < 0) return new Status(Phase.FAILED, Component.literal("混沌房网施工失败；保留现场，禁止覆盖重建"));
        if (stage >= 2 && stage <= 4) {
            try {
                if (saved.data.getInt("SpawnRoomVersion") < 1) {
                    ChaosSpawnRoom.prepare(server.getLevel(DIMENSION), saved.data);
                    saved.setDirty();
                    server.overworld().getDataStorage().save();
                }
            } catch (RuntimeException failure) {
                return new Status(Phase.FAILED, Component.literal(failure.getMessage()));
            }
            return new Status(Phase.COMPLETE,
                    Component.literal("混沌房网就绪 · R5布局" + saved.data.getInt("Layout") + " · 顶层出生房就绪"));
        }
        return new Status(saved.data.getBoolean("Started") ? Phase.BUILDING : Phase.PLANNED,
                Component.literal(saved.data.getBoolean("Started") ? "混沌房网正在分批施工" : "混沌房网已保留，等待生成"));
    }

    public String reserve(Request request) {
        MinecraftServer server = request.source().getServer();
        if (server.getLevel(DIMENSION) == null) throw new IllegalStateException("混沌地牢维度未加载，请重启服务器");
        State saved = state(server);
        String instance = "chaos-r5-" + Long.toUnsignedString(request.seed(), 16);
        if (!saved.data.isEmpty()) {
            if (instance.equals(saved.data.getString("Instance"))
                    && saved.data.getString("Source").equals(request.source().dimension().location().toString())
                    && saved.data.getLong("Gate") == request.gate().asLong()) return instance;
            throw new IllegalStateException("本存档已有混沌房网；使用原入口，禁止覆盖已生成房间");
        }
        if (score(server, "#run") != 0) throw new IllegalStateException("检测到已有房网进度，拒绝覆盖");
        List<ServerPlayer> party = request.source().players().stream()
                .filter(player -> !player.isSpectator() && player.distanceToSqr(Vec3.atCenterOf(request.gate())) <= 64)
                .toList();
        if (party.isEmpty() || party.size() > 16) throw new IllegalStateException("生成时入口八格内须有1～16名参战玩家");
        saved.data.putString("Instance", instance);
        saved.data.putString("Source", request.source().dimension().location().toString());
        saved.data.putLong("Gate", request.gate().asLong());
        saved.data.putInt("Tier", request.tier());
        saved.data.putInt("Layout", Math.floorMod(request.seed(), 4) + 1);
        saved.data.putInt("Players", party.size());
        CompoundTag roster = new CompoundTag();
        party.forEach(player -> roster.putBoolean(player.getStringUUID(), true));
        saved.data.put("Roster", roster);
        saved.setDirty();
        server.overworld().getDataStorage().save();
        return instance;
    }

    public boolean supportsRegeneration() { return true; }

    private static void requireEmpty(MinecraftServer server, ServerLevel destination) {
        if (!destination.players().isEmpty()) throw new IllegalStateException("混沌地牢内仍有玩家，请全部离开后重新生成");
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.getPersistentData().contains(RETURN)) throw new IllegalStateException("仍有玩家保留本局返程记录，请先完成返程");
        }
        var directory = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        try (var paths = Files.list(directory)) {
            for (var file : paths.filter(path -> path.getFileName().toString().endsWith(".dat")).toList()) {
                String uuid = file.getFileName().toString().replace(".dat", "");
                boolean online = server.getPlayerList().getPlayers().stream().anyMatch(player -> player.getStringUUID().equals(uuid));
                if (online) continue;
                CompoundTag player = NbtIo.readCompressed(file.toFile());
                if (player.getString("Dimension").equals(DIMENSION.location().toString())
                        || player.getCompound("ForgeData").contains(RETURN))
                    throw new IllegalStateException("有离线玩家停留在混沌地牢或未完成返程，请先让其上线离开");
            }
        } catch (java.io.IOException failure) {
            throw new IllegalStateException("无法核对离线玩家状态，暂不允许清理旧地牢", failure);
        }
    }

    public String regenerate(Request request, String instance) {
        MinecraftServer server = request.source().getServer();
        State saved = state(server);
        if (!instance.equals(saved.data.getString("Instance"))
                || !request.source().dimension().location().toString().equals(saved.data.getString("Source"))
                || request.gate().asLong() != saved.data.getLong("Gate"))
            throw new IllegalStateException("请使用原混沌地牢入口重新生成");
        int stage = score(server, "#run");
        if (saved.data.getBoolean("Cleaning") || stage == 1 || !saved.data.getBoolean("Started") && stage == 0)
            throw new IllegalStateException("上一轮施工尚未完成，请等待");
        ServerLevel destination = server.getLevel(DIMENSION);
        if (destination == null) throw new IllegalStateException("混沌地牢维度不可用");
        requireEmpty(server, destination);
        List<ServerPlayer> party = request.source().players().stream().filter(player -> !player.isSpectator()
                && player.distanceToSqr(Vec3.atCenterOf(request.gate())) <= 64).toList();
        if (party.isEmpty() || party.size() > 16) throw new IllegalStateException("入口八格内须有1～16名参战玩家");
        resetBoxes(server, saved.data.getInt("Layout"));
        if (server.getFunctions().get(new ResourceLocation("ftmaze", "build/prepare")).isEmpty())
            throw new IllegalStateException("混沌施工函数缺失");
        String replacement = "chaos-r5-" + Long.toUnsignedString(request.seed(), 16) + "-" + java.util.UUID.randomUUID();
        int oldLayout = saved.data.getInt("Layout");
        saved.data = new CompoundTag();
        saved.data.putString("Instance", replacement);
        saved.data.putString("Source", request.source().dimension().location().toString());
        saved.data.putLong("Gate", request.gate().asLong());
        saved.data.putInt("Tier", request.tier());
        saved.data.putInt("Layout", Math.floorMod(request.seed(), 4) + 1);
        saved.data.putInt("Players", party.size());
        CompoundTag roster = new CompoundTag();
        party.forEach(player -> roster.putBoolean(player.getStringUUID(), true));
        saved.data.put("Roster", roster);
        saved.data.putBoolean("Cleaning", true);
        saved.data.putInt("CleanupLayout", oldLayout);
        saved.setDirty();
        server.overworld().getDataStorage().save();
        command(server, "scoreboard players set #run ftm_state -2");
        cancelBuilds(server);
        return replacement;
    }

    private static void cancelBuilds(MinecraftServer server) {
        for (ResourceLocation name : server.getFunctions().getFunctionNames()) {
            if (name.getNamespace().equals("ftmaze") && name.getPath().contains("build/"))
                command(server, "schedule clear " + name);
        }
    }

    private static List<int[]> resetBoxes(MinecraftServer server, int layout) {
        if (layout < 1 || layout > 4) throw new IllegalStateException("旧混沌布局记录无效，拒绝清理");
        try (var resource = server.getResourceManager().getResource(new ResourceLocation("ftmaze", "chaos_reset_boxes.json")).orElseThrow().open();
                var reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            var selected = JsonParser.parseReader(reader).getAsJsonArray().get(layout - 1).getAsJsonArray();
            List<int[]> boxes = new ArrayList<>();
            for (var entry : selected) {
                var values = entry.getAsJsonArray();
                int[] box = new int[6];
                for (int coordinate = 0; coordinate < 6; coordinate++) box[coordinate] = values.get(coordinate).getAsInt();
                boxes.add(box);
            }
            return boxes;
        } catch (java.io.IOException failure) { throw new IllegalStateException("混沌清理范围不可用", failure); }
    }

    private static List<int[]> cleanupBoxes(MinecraftServer server, State saved) {
        if (saved.boxes == null || saved.boxLayout != saved.data.getInt("CleanupLayout")) {
            saved.boxes = resetBoxes(server, saved.data.getInt("CleanupLayout"));
            saved.boxLayout = saved.data.getInt("CleanupLayout");
        }
        return saved.boxes;
    }

    static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        State saved = state(server);
        if (!saved.data.getBoolean("Cleaning")) return;
        ServerLevel destination = server.getLevel(DIMENSION);
        if (destination == null || !destination.players().isEmpty()) return;
        if (server.getTickCount() % 100 == 0) cancelBuilds(server);
        cleanupBoxes(server, saved);
        int index = saved.data.getInt("CleanupIndex");
        long started = System.nanoTime();
        for (int batch = 0; batch < 8 && index < saved.boxes.size()
                && (batch == 0 || System.nanoTime() - started < 4_000_000L); batch++) {
            int offset = saved.data.getInt("CleanupSlice");
            int[] original = saved.boxes.get(index);
            int[] box = ChaosCleanupSlices.slice(original, offset);
            ChunkPos position = new ChunkPos(box[0] >> 4, box[2] >> 4);
            if (saved.cleanupChunk == null) {
                destination.getChunkSource().addRegionTicket(CLEANUP_TICKET, position, 2, position, true);
                saved.cleanupChunk = position;
            }
            if (destination.getChunkSource().getChunkNow(position.x, position.z) == null) return;
            command(server, "execute in ftmaze:depths run fill " + box[0] + " " + box[1] + " " + box[2]
                    + " " + box[3] + " " + box[4] + " " + box[5] + " minecraft:air replace");
            command(server, "execute in ftmaze:depths positioned " + box[0] + " " + box[1] + " " + box[2]
                    + " run kill @e[type=!minecraft:player,dx=" + (box[3] - box[0]) + ",dy=" + (box[4] - box[1]) + ",dz=" + (box[5] - box[2]) + "]");
            destination.getChunkSource().removeRegionTicket(CLEANUP_TICKET, position, 2, position, true);
            saved.cleanupChunk = null;
            offset++;
            if (offset == ChaosCleanupSlices.count(original)) { index++; offset = 0; }
            saved.data.putInt("CleanupSlice", offset);
            saved.data.putInt("CleanupIndex", index);
            saved.setDirty();
        }
        saved.data.putInt("CleanupIndex", index);
        saved.setDirty();
        if (index < saved.boxes.size()) return;
        command(server, "execute in ftmaze:depths run kill @e[type=!minecraft:player]");
        command(server, "execute in ftmaze:depths run forceload remove all");
        for (String objective : List.of("ftm_state", "ftm_mode", "ftm_enter", "ftm_leave", "ftm_join", "ftm_party", "ftm_brick"))
            command(server, "scoreboard objectives remove " + objective);
        command(server, "bossbar remove ftmaze:final");
        saved.data.remove("Cleaning");
        saved.data.remove("CleanupIndex");
        saved.data.remove("CleanupSlice");
        saved.data.remove("CleanupLayout");
        saved.boxes = null;
        server.overworld().getDataStorage().save();
        new ChaosDungeonGenerator().start(server, saved.data.getString("Instance"));
    }

    public void start(MinecraftServer server, String instance) {
        State saved = state(server);
        if (!saved.data.getString("Instance").equals(instance)) throw new IllegalStateException("混沌地牢实例不匹配");
        if (saved.data.getBoolean("Started")) return;
        function(server, "load");
        command(server, "scoreboard players set #layout ftm_state " + saved.data.getInt("Layout"));
        command(server, "scoreboard players set #players ftm_state " + saved.data.getInt("Players"));
        command(server, "scoreboard players set #systems ftm_state " + Math.max(1, saved.data.getInt("Players") / 2));
        command(server, "scoreboard players set #nonce ftm_state 1");
        saved.data.putBoolean("Started", true);
        saved.setDirty();
        server.overworld().getDataStorage().save();
        function(server, "build/prepare");
    }

    public void enter(ServerPlayer player, String instance) {
        MinecraftServer server = player.server;
        if (!status(server, instance).ready()) throw new IllegalStateException("混沌房网尚未生成完成");
        if (!state(server).data.getCompound("Roster").getBoolean(player.getStringUUID()))
            throw new IllegalStateException("本局名单已冻结；仅生成时入口旁的参战者可进入");
        if (player.getPersistentData().contains(RETURN)) throw new IllegalStateException("已有混沌地牢返程记录");
        ServerLevel destination = server.getLevel(DIMENSION);
        BlockPos landing = ChaosSpawnRoom.LANDING;
        destination.getChunk(landing);
        if (!ChaosSpawnRoom.safe(destination, landing)
                || !destination.noCollision(player, player.getBoundingBox().move(ChaosSpawnRoom.TARGET.subtract(player.position()))))
            throw new IllegalStateException("混沌出生房落点被阻塞");
        destination.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, new ChunkPos(landing), 2, player.getId());
        CompoundTag saved = new CompoundTag();
        saved.putString("Dimension", player.serverLevel().dimension().location().toString());
        saved.putDouble("X", player.getX());
        saved.putDouble("Y", player.getY());
        saved.putDouble("Z", player.getZ());
        saved.putFloat("Yaw", player.getYRot());
        saved.putFloat("Pitch", player.getXRot());
        saved.putInt("Mode", player.gameMode.getGameModeForPlayer().getId());
        player.getPersistentData().put(RETURN, saved);
        command(server, "scoreboard players set " + player.getScoreboardName() + " ftm_party 1");
        server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4)
                .withSuppressedOutput(), "function ftmaze:entry/save");
        if (!ownsDimension(player.serverLevel()) || player.position().distanceToSqr(ChaosSpawnRoom.TARGET) > 0.01) {
            player.setGameMode(GameType.byId(saved.getInt("Mode")));
            player.removeTag("ftmaze.inside");
            player.getPersistentData().remove(RETURN);
            throw new IllegalStateException("混沌地牢传送失败");
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0;
    }

    public void leave(ServerPlayer player) {
        if (!ownsDimension(player.serverLevel()) || !player.getPersistentData().contains(RETURN))
            throw new IllegalStateException("没有有效的混沌地牢返程记录");
        if (score(player.server, "#run") != 4) throw new IllegalStateException("最终Boss尚未击败，折返门未开");
        CompoundTag saved = player.getPersistentData().getCompound(RETURN);
        ResourceLocation dimension = ResourceLocation.tryParse(saved.getString("Dimension"));
        ServerLevel destination = dimension == null ? null : player.server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
        if (destination == null) throw new IllegalStateException("原入口维度不可用，保留返程记录");
        Vec3 target = new Vec3(saved.getDouble("X"), saved.getDouble("Y"), saved.getDouble("Z"));
        BlockPos position = BlockPos.containing(target);
        destination.getChunk(position);
        if (!destination.getWorldBorder().isWithinBounds(position)
                || !destination.noCollision(player, player.getBoundingBox().move(target.subtract(player.position()))))
            throw new IllegalStateException("原入口落点被阻塞，保留返程记录");
        player.teleportTo(destination, target.x, target.y, target.z, saved.getFloat("Yaw"), saved.getFloat("Pitch"));
        player.setGameMode(GameType.byId(saved.getInt("Mode")));
        player.removeTag("ftmaze.inside");
        player.getPersistentData().remove(RETURN);
    }

    static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.getTags().contains("ftmaze.exit_requested")) {
            player.removeTag("ftmaze.exit_requested");
            try { new ChaosDungeonGenerator().leave(player); }
            catch (RuntimeException failure) { player.displayClientMessage(Component.literal(failure.getMessage()), true); }
        }
        if (player.getPersistentData().contains(RETURN) && !player.serverLevel().dimension().equals(DIMENSION)) {
            player.setGameMode(GameType.byId(player.getPersistentData().getCompound(RETURN).getInt("Mode")));
            player.removeTag("ftmaze.inside");
            player.getPersistentData().remove(RETURN);
        }
    }

    private static final class State extends SavedData {
        private CompoundTag data = new CompoundTag();
        private List<int[]> boxes;
        private int boxLayout;
        private ChunkPos cleanupChunk;
        static State load(CompoundTag tag) { State saved = new State(); saved.data = tag.copy(); return saved; }
        public CompoundTag save(CompoundTag tag) { return data.copy(); }
    }
}
