package net.farmingtales.ftdungeonchallenges;

import com.mojang.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftdungeonmobs.ArenaTorchBlock;
import net.farmingtales.ftdungeonmobs.DungeonDifficultyApi;
import net.farmingtales.ftdungeonmobs.DungeonSkeletonEntity;
import net.farmingtales.ftdungeonmobs.FTDungeonMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.slf4j.Logger;

final class ArenaChallengeRun {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final BossEvent.BossBarColor[] COLORS = {BossEvent.BossBarColor.BLUE, BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarColor.YELLOW, BossEvent.BossBarColor.PURPLE, BossEvent.BossBarColor.RED};
    final UUID id = UUID.randomUUID();
    final ServerLevel level;
    final BlockPos base;
    final Vec3 center;
    final int tier;
    final int radius;
    final UUID owner;
    final ArenaChallengeConfig.Settings settings;
    final ArenaChallengeConfig.Tier tierSettings;
    final Set<UUID> participants = new LinkedHashSet<>();
    final Set<UUID> eliminated = new LinkedHashSet<>();
    final Set<UUID> enemies = new LinkedHashSet<>();
    private final Queue<EntityType<DungeonSkeletonEntity>> pending = new ArrayDeque<>();
    private final Map<UUID, LivingDeathEvent> deaths = new HashMap<>();
    private final ServerBossEvent bossBar;
    private ArenaChallengeApi.Phase phase = ArenaChallengeApi.Phase.PREPARING;
    private long phaseUntil;
    private long spawnUntil;
    private long absentSince = -1;
    private long waveStarted;
    private long nextCooldownRefresh;
    private int waveSize;
    private boolean finished;
    int wave;

    ArenaChallengeRun(ServerLevel level, BlockPos base, int tier, int radius, ServerPlayer owner,
                      ArenaChallengeConfig.Settings settings) {
        this.level = level;
        this.base = base.immutable();
        this.center = new Vec3(base.getX() + 0.5, base.getY() + 1, base.getZ() + 0.5);
        this.tier = tier;
        this.radius = radius;
        this.owner = owner.getUUID();
        this.settings = settings;
        this.tierSettings = settings.tier(tier);
        participants.add(owner.getUUID());
        phaseUntil = level.getGameTime() + settings.preparationSeconds() * 20L;
        bossBar = new ServerBossEvent(Component.empty(), COLORS[tier - 1], BossEvent.BossBarOverlay.PROGRESS);
        updateBossBar(List.of(owner));
    }

    ArenaChallengeApi.Snapshot snapshot() {
        return new ArenaChallengeApi.Snapshot(id, level.dimension().location().toString(), base, tier, phase, wave,
                tierSettings.waves(), enemies.size(), pending.size(),
                (int) Math.max(0, (phaseUntil - level.getGameTime() + 19) / 20), List.copyOf(participants));
    }

    boolean inRange(Vec3 position) { return center.distanceToSqr(position) <= radius * radius; }

    void reserveCooldown() {
        ArenaChallengeData.get(level).reserve(level, base, participants, settings.cooldownSeconds());
    }

    void recordDeath(LivingDeathEvent event) { deaths.put(event.getEntity().getUUID(), event); }

    private void confirmDeaths() {
        for (LivingDeathEvent event : deaths.values()) {
            if (event.isCanceled() || event.getEntity().getHealth() > 0) { continue; }
            UUID identifier = event.getEntity().getUUID();
            if (participants.contains(identifier) && eliminated.add(identifier)) {
                announce("eliminated", event.getEntity().getDisplayName());
            }
            enemies.remove(identifier);
        }
        deaths.clear();
    }

    void tick() {
        if (finished) { return; }
        confirmDeaths();
        if (!ArenaChallengeConfig.settings().enabled()) { finish(ArenaChallengeApi.EndReason.DISABLED); return; }
        if (level.getDifficulty() == Difficulty.PEACEFUL) { finish(ArenaChallengeApi.EndReason.PEACEFUL); return; }
        if (!level.hasChunkAt(base)) { finish(ArenaChallengeApi.EndReason.TORCH_REMOVED); return; }
        try {
            ArenaChallengeApi.torchBase(level, base);
            if (level.getBlockState(base).getValue(ArenaTorchBlock.TIER) != tier) {
                finish(ArenaChallengeApi.EndReason.TORCH_REMOVED);
                return;
            }
        } catch (ArenaChallengeApi.ChallengeException exception) {
            finish(ArenaChallengeApi.EndReason.TORCH_REMOVED);
            return;
        }
        if (eliminated.containsAll(participants)) { finish(ArenaChallengeApi.EndReason.DEFEAT); return; }
        List<ServerPlayer> present = presentPlayers();
        long now = level.getGameTime();
        if (now >= nextCooldownRefresh) {
            reserveCooldown();
            nextCooldownRefresh = now + 200;
        }
        if (present.isEmpty()) {
            if (absentSince < 0) { absentSince = now; }
            updateBossBar(present);
            if (now - absentSince >= settings.absenceSeconds() * 20L) { finish(ArenaChallengeApi.EndReason.ABSENT); }
            return;
        }
        absentSince = -1;
        if (phase != ArenaChallengeApi.Phase.WAVE) {
            if (now < phaseUntil) { updateBossBar(present); return; }
            beginWave();
            if (finished) { return; }
        }
        if (now >= phaseUntil) { finish(ArenaChallengeApi.EndReason.WAVE_TIMEOUT); return; }
        if (!pending.isEmpty() && now >= spawnUntil) { finish(ArenaChallengeApi.EndReason.SPAWN_BLOCKED); return; }
        for (int attempt = 0; attempt < settings.spawnBatch() && !pending.isEmpty(); attempt++) {
            if (spawn(pending.peek(), present)) { pending.remove(); }
        }
        for (UUID enemy : new ArrayList<>(enemies)) {
            Entity entity = level.getEntity(enemy);
            if (entity == null || entity.isRemoved() || !entity.isAlive()) {
                finish(ArenaChallengeApi.EndReason.ENEMY_LOST);
                return;
            }
            if (center.distanceToSqr(entity.position()) > (radius + 8) * (radius + 8)) {
                finish(ArenaChallengeApi.EndReason.OUT_OF_BOUNDS);
                return;
            }
            if (entity instanceof DungeonSkeletonEntity skeleton) {
                if (!(skeleton.getTarget() instanceof ServerPlayer target) || !present.contains(target)) {
                    skeleton.setTarget(present.get(level.random.nextInt(present.size())));
                }
                if (pending.isEmpty() && enemies.size() <= 2 && now - waveStarted >= 1200) {
                    skeleton.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false));
                }
            }
        }
        if (pending.isEmpty() && enemies.isEmpty()) {
            if (wave >= tierSettings.waves()) { finish(ArenaChallengeApi.EndReason.VICTORY); return; }
            phase = ArenaChallengeApi.Phase.INTERMISSION;
            phaseUntil = now + settings.intermissionSeconds() * 20L;
            announce("wave_cleared", wave, settings.intermissionSeconds());
        }
        updateBossBar(present);
    }

    private void beginWave() {
        wave++;
        phase = ArenaChallengeApi.Phase.WAVE;
        waveStarted = level.getGameTime();
        phaseUntil = waveStarted + settings.waveSeconds() * 20L;
        spawnUntil = waveStarted + settings.spawnSeconds() * 20L;
        waveSize = tierSettings.mobCount(wave, participants.size(), settings);
        if (tierSettings.finalBoss() && wave == tierSettings.waves()) { pending.add(FTDungeonMobs.KING.get()); }
        while (pending.size() < waveSize) { pending.add(randomEnemy()); }
        announce("wave_started", wave, tierSettings.waves(), waveSize);
        level.playSound(null, base, SoundEvents.RAID_HORN.get(), SoundSource.HOSTILE, 1, 0.85F + tier * 0.03F);
        MinecraftForge.EVENT_BUS.post(new ArenaChallengeEvent.WaveStarted(level, snapshot()));
    }

    private EntityType<DungeonSkeletonEntity> randomEnemy() {
        int roll = level.random.nextInt(tierSettings.warriorWeight() + tierSettings.archerWeight() + tierSettings.mageWeight());
        if (roll < tierSettings.warriorWeight()) { return FTDungeonMobs.WARRIOR.get(); }
        if (roll < tierSettings.warriorWeight() + tierSettings.archerWeight()) { return FTDungeonMobs.ARCHER.get(); }
        return FTDungeonMobs.MAGE.get();
    }

    private boolean spawn(EntityType<DungeonSkeletonEntity> type, List<ServerPlayer> present) {
        DungeonSkeletonEntity entity = type.create(level);
        if (entity == null) { return false; }
        double minimum = Math.min(8, radius * 0.4);
        double maximum = Math.min(18, radius - 2);
        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = minimum + level.random.nextDouble() * (maximum - minimum);
            int columnX = Mth.floor(center.x + Math.cos(angle) * distance);
            int columnZ = Mth.floor(center.z + Math.sin(angle) * distance);
            if (!level.hasChunkAt(new BlockPos(columnX, base.getY(), columnZ))) { continue; }
            for (int offset = 0; offset <= 12; offset++) {
                int height = base.getY() + (offset % 2 == 0 ? offset / 2 : -(offset + 1) / 2);
                BlockPos feet = new BlockPos(columnX, height, columnZ);
                if (height <= level.getMinBuildHeight() || height + Math.ceil(type.getHeight()) >= level.getMaxBuildHeight()) { continue; }
                BlockState floor = level.getBlockState(feet.below());
                if (!floor.isFaceSturdy(level, feet.below(), Direction.UP) || floor.is(Blocks.MAGMA_BLOCK)
                        || floor.is(Blocks.CAMPFIRE) || floor.is(Blocks.SOUL_CAMPFIRE) || floor.is(Blocks.CACTUS)
                        || level.getBlockState(feet).is(BlockTags.FIRE)) { continue; }
                entity.moveTo(columnX + 0.5, height, columnZ + 0.5, level.random.nextFloat() * 360, 0);
                if (!level.hasChunksAt(BlockPos.containing(entity.getBoundingBox().minX, height, entity.getBoundingBox().minZ),
                        BlockPos.containing(entity.getBoundingBox().maxX, height, entity.getBoundingBox().maxZ))
                        || !inRange(entity.position()) || present.stream().anyMatch(player -> player.distanceToSqr(entity) < 9)
                        || !level.getWorldBorder().isWithinBounds(entity.getBoundingBox()) || !level.noCollision(entity)
                        || level.containsAnyLiquid(entity.getBoundingBox())) { continue; }
                entity.setPersistenceRequired();
                entity.setCanPickUpLoot(false);
                entity.getPersistentData().putUUID(ArenaChallengeApi.ENEMY_TAG, id);
                DungeonDifficultyApi.setTier(entity, tier);
                entity.setTarget(present.get(level.random.nextInt(present.size())));
                enemies.add(entity.getUUID());
                if (!level.addFreshEntity(entity)) {
                    enemies.remove(entity.getUUID());
                    entity.discard();
                    return false;
                }
                level.sendParticles(ParticleTypes.POOF, entity.getX(), entity.getY() + 0.5, entity.getZ(), 6, 0.3, 0.3, 0.3, 0.02);
                return true;
            }
        }
        entity.discard();
        return false;
    }

    private List<ServerPlayer> presentPlayers() {
        List<ServerPlayer> present = new ArrayList<>();
        for (UUID participant : participants) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(participant);
            if (player != null && player.level() == level && player.isAlive() && !player.isSpectator()
                    && !eliminated.contains(participant) && inRange(player.position())) { present.add(player); }
        }
        return present;
    }

    private void updateBossBar(List<ServerPlayer> present) {
        for (ServerPlayer previous : new ArrayList<>(bossBar.getPlayers())) {
            if (!present.contains(previous)) { bossBar.removePlayer(previous); }
        }
        present.forEach(bossBar::addPlayer);
        if (phase == ArenaChallengeApi.Phase.WAVE) {
            bossBar.setName(Component.translatable("challenge.ftdungeonmobs.bar.wave", tier, wave,
                    tierSettings.waves(), enemies.size() + pending.size(), snapshot().secondsRemaining()));
            bossBar.setProgress(Mth.clamp((float) (enemies.size() + pending.size()) / Math.max(1, waveSize), 0, 1));
        } else {
            bossBar.setName(Component.translatable("challenge.ftdungeonmobs.bar.prepare", tier, wave + 1,
                    tierSettings.waves(), snapshot().secondsRemaining()));
            int duration = phase == ArenaChallengeApi.Phase.PREPARING ? settings.preparationSeconds() : settings.intermissionSeconds();
            bossBar.setProgress(Mth.clamp(1 - (phaseUntil - level.getGameTime()) / (duration * 20F), 0, 1));
        }
    }

    void announce(String key, Object... arguments) {
        Component message = Component.translatable("challenge.ftdungeonmobs." + key, arguments);
        for (UUID participant : participants) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(participant);
            if (player != null) { player.sendSystemMessage(message); }
        }
    }

    void finish(ArenaChallengeApi.EndReason reason) {
        if (finished) { return; }
        finished = true;
        ArenaChallengeApi.forget(this);
        reserveCooldown();
        bossBar.removeAllPlayers();
        for (UUID enemy : enemies) {
            Entity entity = level.getEntity(enemy);
            if (entity != null) { entity.discard(); }
        }
        enemies.clear();
        pending.clear();
        deaths.clear();
        announce("finished", Component.translatable("challenge.ftdungeonmobs.reason." + reason.name().toLowerCase(java.util.Locale.ROOT)));
        if (reason == ArenaChallengeApi.EndReason.VICTORY) {
            for (ServerPlayer player : presentPlayers()) { reward(player); }
        }
        LOGGER.info("FT arena challenge {} ended: {}, tier {}, wave {}/{}", id, reason, tier, wave, tierSettings.waves());
        MinecraftForge.EVENT_BUS.post(new ArenaChallengeEvent.Finished(level, snapshot(), reason));
    }

    private void reward(ServerPlayer player) {
        LootParams loot = new LootParams.Builder(level).withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.ORIGIN, player.position()).create(LootContextParamSets.GIFT);
        List<ItemStack> rewards = level.getServer().getLootData().getLootTable(FTDungeonChallenges.resource("challenges/tier_" + tier))
                .getRandomItems(loot);
        for (ItemStack stack : rewards) {
            if (!player.getInventory().add(stack)) { player.drop(stack, false); }
        }
        player.giveExperiencePoints(settings.experiencePerTier() * tier);
        player.sendSystemMessage(Component.translatable("challenge.ftdungeonmobs.reward", tier, settings.experiencePerTier() * tier));
    }
}
