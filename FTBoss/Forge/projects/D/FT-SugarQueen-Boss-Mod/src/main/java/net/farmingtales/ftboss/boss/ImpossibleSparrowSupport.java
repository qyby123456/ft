package net.farmingtales.ftboss.boss;

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ImpossibleSparrowSupport extends SavedData {
    private UUID sparrow;
    private UUID fight;
    private ResourceKey<Level> dimension;
    private BlockPos position = BlockPos.ZERO;

    public static ImpossibleSparrowSupport get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(ImpossibleSparrowSupport::load,
                ImpossibleSparrowSupport::new, "ftboss_impossible_sparrow");
    }

    public static ImpossibleSparrowSupport load(CompoundTag tag) {
        var data = new ImpossibleSparrowSupport();
        if (tag.hasUUID("Sparrow") && tag.hasUUID("Fight")) {
            var location = ResourceLocation.tryParse(tag.getString("Dimension"));
            if (location != null) {
                data.sparrow = tag.getUUID("Sparrow");
                data.fight = tag.getUUID("Fight");
                data.dimension = ResourceKey.create(Registries.DIMENSION, location);
                data.position = BlockPos.of(tag.getLong("Position"));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (this.sparrow != null) {
            tag.putUUID("Sparrow", this.sparrow);
            tag.putUUID("Fight", this.fight);
            tag.putString("Dimension", this.dimension.location().toString());
            tag.putLong("Position", this.position.asLong());
        }
        return tag;
    }

    public static float health(EasterBossEntity sparrow) {
        return net.farmingtales.ftboss.boss.ai.ManorHealthBalance.supportHealth(sparrow);
    }

    private void clear() {
        this.sparrow = null;
        this.fight = null;
        this.dimension = null;
        this.setDirty();
    }

    public static void finish(MinecraftServer server, UUID fight) {
        var data = get(server);
        if (fight != null && fight.equals(data.fight)) data.clear();
    }

    public static void failedSpawn(EasterBossEntity bird) {
        if (!(bird.level() instanceof ServerLevel level)) return;
        var data = get(level.getServer());
        if (bird.getUUID().equals(data.sparrow)) data.clear();
    }

    public static boolean reserved(MinecraftServer server) {
        var data = get(server);
        if (data.sparrow != null && ManorFightData.get(server).retired(data.fight)) data.clear();
        return data.sparrow != null;
    }

    private void remember(EasterBossEntity bird) {
        this.sparrow = bird.getUUID();
        this.fight = ManorFightMembers.fightId(bird);
        this.dimension = bird.level().dimension();
        this.position = bird.blockPosition();
        this.setDirty();
    }

    private static void prepare(EasterBossEntity bird) {
        net.farmingtales.ftboss.boss.ai.ManorHealthBalance.prepareImpossible(bird);
    }

    public static void ensure(ServerLevel level, EasterBossEntity boss) {
        if (net.farmingtales.ftboss.boss.ai.XuanlanEncounterLimits.blocksSupport(boss)) return;
        UUID fight = ManorFightMembers.fightId(boss);
        if (!boss.isImpossibleChallenge() || !boss.isAlive() || boss.isRemoved() || boss.isMini()
                || boss.getVariant() == EasterBossEntity.SPARROW_VARIANT || fight == null
                || !boss.getUUID().equals(ManorFight.currentBoss(fight)) || HiddenBossIntro.isActing(boss.getUUID())
                || ManorFightData.get(level.getServer()).retired(fight)) return;
        var data = get(level.getServer());
        if (reserved(level.getServer())) {
            ServerLevel storedLevel = level.getServer().getLevel(data.dimension);
            if (storedLevel == null) return;
            Entity stored = storedLevel.getEntity(data.sparrow);
            if (stored instanceof EasterBossEntity bird && bird.isAlive() && !bird.isRemoved()) {
                prepare(bird);
                data.remember(bird);
                return;
            }
            if (stored == null) {
                storedLevel.getChunk(data.position);
                return;
            }
            data.clear();
        }
        for (ServerLevel candidateLevel : level.getServer().getAllLevels()) {
            for (Entity entity : candidateLevel.getAllEntities()) {
                if (!(entity instanceof EasterBossEntity bird) || bird.getVariant() != EasterBossEntity.SPARROW_VARIANT
                        || !bird.isAlive() || bird.isRemoved()) continue;
                if (fight.equals(ManorFightMembers.fightId(bird))) {
                    prepare(bird);
                    data.remember(bird);
                }
                return;
            }
        }
        SparrowSupport.spawnSparrow(level, boss);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof EasterBossEntity bird)
                || bird.getVariant() != EasterBossEntity.SPARROW_VARIANT || event.isCanceled()) return;
        var data = get(level.getServer());
        if (reserved(level.getServer()) && !bird.getUUID().equals(data.sparrow)) {
            event.setCanceled(true);
            return;
        }
        UUID fight = ManorFightMembers.fightId(bird);
        if (!bird.isImpossibleChallenge() || fight == null || ManorFightData.get(level.getServer()).retired(fight)) return;
        for (ServerLevel candidateLevel : level.getServer().getAllLevels()) {
            for (Entity entity : candidateLevel.getAllEntities()) {
                if (entity instanceof EasterBossEntity existing && existing.getVariant() == EasterBossEntity.SPARROW_VARIANT
                        && existing.isAlive() && !existing.isRemoved() && !existing.getUUID().equals(bird.getUUID())) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
        prepare(bird);
        data.remember(bird);
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof EasterBossEntity bird) || bird.getVariant() != EasterBossEntity.SPARROW_VARIANT) return;
        var data = get(level.getServer());
        if (!entity.getUUID().equals(data.sparrow)) return;
        if (entity.getRemovalReason() == Entity.RemovalReason.UNLOADED_TO_CHUNK
                || entity.getRemovalReason() == Entity.RemovalReason.UNLOADED_WITH_PLAYER) data.remember(bird);
        else data.clear();
    }

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof EasterBossEntity bird) || !(bird.level() instanceof ServerLevel level)
                || !bird.isImpossibleChallenge() || bird.getVariant() != EasterBossEntity.SPARROW_VARIANT
                || bird.tickCount % 20 != 0 || !bird.isAlive()) return;
        var data = get(level.getServer());
        if (bird.getUUID().equals(data.sparrow)) data.remember(bird);
    }
}
