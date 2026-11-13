package net.farmingtales.ftdungeonchallenges;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.farmingtales.ftdungeonmobs.ArenaTorchBlock;
import net.farmingtales.ftdungeonmobs.DungeonDifficultyConfig;
import net.farmingtales.ftdungeonmobs.FTDungeonMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;

public final class ArenaChallengeApi {
    static final String ENEMY_TAG = "FTArenaChallenge";
    private static final Map<UUID, ArenaChallengeRun> ACTIVE = new HashMap<>();

    private ArenaChallengeApi() {}

    public static Snapshot start(ServerPlayer player, BlockPos position) {
        ServerLevel level = player.serverLevel();
        requireServer(level.getServer());
        ArenaChallengeConfig.Settings settings = ArenaChallengeConfig.settings();
        if (!settings.enabled()) { throw error("disabled"); }
        if (level.getDifficulty() == Difficulty.PEACEFUL) { throw error("peaceful"); }
        if (!eligible(player)) { throw error("player"); }
        BlockPos base = torchBase(level, position);
        if (!level.mayInteract(player, base)) { throw error("protected"); }
        ArenaChallengeRun existing = findRun(level, base);
        if (existing != null) {
            return join(player, base);
        }
        int radius = DungeonDifficultyConfig.settings().arenaRadius();
        if (player.position().distanceToSqr(base.getX() + 0.5, base.getY() + 1, base.getZ() + 0.5) > radius * radius) {
            throw error("range");
        }
        if (ACTIVE.values().stream().filter(run -> run.level.getServer() == level.getServer()).count() >= settings.maxChallenges()) {
            throw error("limit");
        }
        for (ArenaChallengeRun run : ACTIVE.values()) {
            if (run.level.getServer() == level.getServer() && run.participants.contains(player.getUUID())) {
                throw error("participating");
            }
            if (run.level == level && run.center.distanceToSqr(base.getX() + 0.5, base.getY() + 1, base.getZ() + 0.5)
                    <= (run.radius + radius) * (run.radius + radius)) {
                throw error("overlap");
            }
        }
        checkCooldown(player, base, true);
        int tier = level.getBlockState(base).getValue(ArenaTorchBlock.TIER);
        ArenaChallengeRun run = new ArenaChallengeRun(level, base, tier, radius, player, settings);
        ACTIVE.put(run.id, run);
        run.reserveCooldown();
        run.announce("started", tier, run.tierSettings.waves(), settings.preparationSeconds());
        MinecraftForge.EVENT_BUS.post(new ArenaChallengeEvent.Started(level, run.snapshot()));
        return run.snapshot();
    }

    public static Snapshot join(ServerPlayer player, BlockPos position) {
        ServerLevel level = player.serverLevel();
        requireServer(level.getServer());
        ArenaChallengeRun run = findRun(level, basePosition(level, position));
        if (run == null) { throw error("not_active"); }
        if (run.participants.contains(player.getUUID())) { return run.snapshot(); }
        if (!eligible(player)) { throw error("player"); }
        if (!level.mayInteract(player, run.base)) { throw error("protected"); }
        if (!run.inRange(player.position())) { throw error("range"); }
        if (run.wave != 0) { throw error("join_closed"); }
        if (run.participants.size() >= run.settings.maxParticipants()) { throw error("full"); }
        if (ACTIVE.values().stream().anyMatch(other -> other.participants.contains(player.getUUID()))) {
            throw error("participating");
        }
        checkCooldown(player, run.base, false);
        run.participants.add(player.getUUID());
        run.reserveCooldown();
        run.announce("joined", player.getDisplayName(), run.participants.size(), run.settings.maxParticipants());
        return run.snapshot();
    }

    public static Optional<Snapshot> find(ServerLevel level, BlockPos position) {
        requireServer(level.getServer());
        ArenaChallengeRun run = findRun(level, basePosition(level, position));
        return run == null ? Optional.empty() : Optional.of(run.snapshot());
    }

    public static List<Snapshot> active(MinecraftServer server) {
        requireServer(server);
        return ACTIVE.values().stream().filter(run -> run.level.getServer() == server).map(ArenaChallengeRun::snapshot).toList();
    }

    public static boolean isLocked(ServerLevel level, BlockPos position) {
        return find(level, position).isPresent();
    }

    public static boolean stop(ServerPlayer player, BlockPos position) {
        requireServer(player.serverLevel().getServer());
        ArenaChallengeRun run = findRun(player.serverLevel(), basePosition(player.serverLevel(), position));
        if (run == null) { return false; }
        if (!run.owner.equals(player.getUUID()) && !player.hasPermissions(2)) { throw error("not_owner"); }
        run.finish(EndReason.CANCELLED);
        return true;
    }

    public static boolean cancel(ServerLevel level, BlockPos position) {
        requireServer(level.getServer());
        ArenaChallengeRun run = findRun(level, basePosition(level, position));
        if (run == null) { return false; }
        run.finish(EndReason.CANCELLED);
        return true;
    }

    public static Component describe(Snapshot snapshot) {
        return Component.translatable("challenge.ftdungeonmobs.status", snapshot.tier(), snapshot.wave(), snapshot.totalWaves(),
                snapshot.remainingEnemies(), snapshot.pendingSpawns(), snapshot.participants().size(), snapshot.secondsRemaining());
    }

    static void forget(ArenaChallengeRun run) {
        ACTIVE.remove(run.id, run);
    }

    static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % 20 != 0) { return; }
        for (ArenaChallengeRun run : new ArrayList<>(ACTIVE.values())) {
            if (run.level.getServer() == event.getServer()) { run.tick(); }
        }
    }

    static void onDeath(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity().level() instanceof ServerLevel level)) { return; }
        if (event.getEntity() instanceof ServerPlayer player) {
            for (ArenaChallengeRun run : new ArrayList<>(ACTIVE.values())) {
                if (run.level.getServer() == level.getServer() && run.participants.contains(player.getUUID())) {
                    run.recordDeath(event);
                }
            }
        }
        Entity entity = event.getEntity();
        if (entity.getPersistentData().hasUUID(ENEMY_TAG)) {
            ArenaChallengeRun run = ACTIVE.get(entity.getPersistentData().getUUID(ENEMY_TAG));
            if (run != null && run.level == level) { run.recordDeath(event); }
        }
    }

    static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !event.getEntity().getPersistentData().hasUUID(ENEMY_TAG)) { return; }
        Runnable cleanOrphan = () -> {
            Entity entity = event.getEntity();
            ArenaChallengeRun run = ACTIVE.get(entity.getPersistentData().getUUID(ENEMY_TAG));
            if (run == null || run.level != level || !run.enemies.contains(entity.getUUID())) { entity.discard(); }
        };
        if (level.getServer().isSameThread()) { cleanOrphan.run(); }
        else { level.getServer().execute(cleanOrphan); }
    }

    static void onStopping(ServerStoppingEvent event) {
        for (ArenaChallengeRun run : new ArrayList<>(ACTIVE.values())) {
            if (run.level.getServer() == event.getServer()) { run.finish(EndReason.SERVER_STOPPED); }
        }
    }

    static BlockPos torchBase(ServerLevel level, BlockPos position) {
        BlockPos base = basePosition(level, position);
        if (!level.hasChunkAt(base) || !level.hasChunkAt(base.above())) { throw error("torch"); }
        BlockState lower = level.getBlockState(base);
        BlockState upper = level.getBlockState(base.above());
        if (!lower.is(FTDungeonMobs.ARENA_TORCH.get()) || lower.getValue(ArenaTorchBlock.HALF) != DoubleBlockHalf.LOWER
                || !upper.is(FTDungeonMobs.ARENA_TORCH.get()) || upper.getValue(ArenaTorchBlock.HALF) != DoubleBlockHalf.UPPER) {
            throw error("torch");
        }
        return base.immutable();
    }

    private static BlockPos basePosition(ServerLevel level, BlockPos position) {
        if (!level.hasChunkAt(position)) { return position.immutable(); }
        BlockState state = level.getBlockState(position);
        return state.is(FTDungeonMobs.ARENA_TORCH.get()) && state.getValue(ArenaTorchBlock.HALF) == DoubleBlockHalf.UPPER
                ? position.below() : position.immutable();
    }

    private static ArenaChallengeRun findRun(ServerLevel level, BlockPos base) {
        return ACTIVE.values().stream().filter(run -> run.level == level && run.base.equals(base)).findFirst().orElse(null);
    }

    private static boolean eligible(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator();
    }

    private static void checkCooldown(ServerPlayer player, BlockPos base, boolean includeTorch) {
        ArenaChallengeData data = ArenaChallengeData.get(player.serverLevel());
        long remaining = includeTorch ? data.remaining(player.serverLevel(), base, player.getUUID())
                : data.remainingForPlayer(player.serverLevel(), player.getUUID());
        if (remaining > 0) { throw error("cooldown", (remaining + 19) / 20); }
    }

    private static void requireServer(MinecraftServer server) {
        if (!server.isSameThread()) { throw new IllegalStateException("Arena challenges must run on the server thread."); }
    }

    private static ChallengeException error(String key, Object... arguments) {
        return new ChallengeException(Component.translatable("challenge.ftdungeonmobs.error." + key, arguments));
    }

    public enum Phase { PREPARING, WAVE, INTERMISSION }
    public enum EndReason { VICTORY, CANCELLED, DEFEAT, ABSENT, WAVE_TIMEOUT, SPAWN_BLOCKED, TORCH_REMOVED,
        ENEMY_LOST, OUT_OF_BOUNDS, SERVER_STOPPED, DISABLED, PEACEFUL }

    public record Snapshot(UUID id, String dimension, BlockPos torch, int tier, Phase phase, int wave, int totalWaves,
                           int remainingEnemies, int pendingSpawns, int secondsRemaining, List<UUID> participants) {}

    public static final class ChallengeException extends IllegalArgumentException {
        private final Component component;

        private ChallengeException(Component component) {
            super(component.getString());
            this.component = component;
        }

        public Component component() { return component; }
    }
}
