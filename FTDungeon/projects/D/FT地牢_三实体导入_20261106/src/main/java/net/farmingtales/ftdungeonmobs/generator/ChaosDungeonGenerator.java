package net.farmingtales.ftdungeonmobs.generator;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

final class ChaosDungeonGenerator implements DungeonGenerator {
    private static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation("ftmaze", "depths"));
    private static final String RETURN = "FTChaosReturn";

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
        if (stage < 0) return new Status(Phase.FAILED, Component.literal("混沌房网施工失败；保留现场，禁止覆盖重建"));
        if (stage >= 2 && stage <= 4) return new Status(Phase.COMPLETE,
                Component.literal("混沌房网就绪 · R5布局" + saved.data.getInt("Layout") + " · 四层强制连通"));
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
        BlockPos landing = new BlockPos(72, 211, 100);
        destination.getChunk(landing);
        if (!destination.getBlockState(landing.below()).isFaceSturdy(destination, landing.below(), net.minecraft.core.Direction.UP)
                || !destination.getBlockState(landing).getCollisionShape(destination, landing).isEmpty()
                || !destination.getBlockState(landing.above()).getCollisionShape(destination, landing.above()).isEmpty())
            throw new IllegalStateException("混沌地牢落点被阻塞");
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
        if (!ownsDimension(player.serverLevel())) {
            player.getPersistentData().remove(RETURN);
            throw new IllegalStateException("混沌地牢传送失败");
        }
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
        static State load(CompoundTag tag) { State saved = new State(); saved.data = tag.copy(); return saved; }
        public CompoundTag save(CompoundTag tag) { return data.copy(); }
    }
}
