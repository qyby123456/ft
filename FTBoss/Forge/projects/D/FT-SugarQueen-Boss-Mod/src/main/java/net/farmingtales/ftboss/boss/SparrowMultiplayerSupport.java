package net.farmingtales.ftboss.boss;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.SparrowMultiplayerRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class SparrowMultiplayerSupport {
    private static final String ATTACKERS = "ftboss_sparrow_attackers";
    private static final String VISITED = "ftboss_sparrow_visited";
    private static final String STAGE = "ftboss_sparrow_stage";
    private static final String TRIAL_TICK = "ftboss_sparrow_trial_tick";
    private static final String TRIAL_ROLL = "ftboss_sparrow_trial_roll";

    private SparrowMultiplayerSupport() {}

    private static boolean eligible(EasterBossEntity boss) {
        return boss.isAlive() && !boss.isRemoved() && !boss.isMini()
                && EasterBossEntity.isManorVariant(boss.getVariant()) && !boss.isImpossibleChallenge();
    }

    public static int participants(ServerLevel level, EasterBossEntity boss) {
        UUID fight = ManorFightMembers.fightId(boss);
        Set<UUID> members = new HashSet<>();
        boolean managed = ManorFight.currentVariant(fight) >= 0;
        if (managed) {
            for (Tag element : ManorFightData.get(level.getServer()).active()) {
                CompoundTag record = (CompoundTag) element;
                if (!record.hasUUID("Id") || !fight.equals(record.getUUID("Id"))) continue;
                if (record.hasUUID("Summoner")) members.add(record.getUUID("Summoner"));
                for (Tag participant : record.getList("Participants", Tag.TAG_COMPOUND)) {
                    CompoundTag identity = (CompoundTag) participant;
                    if (identity.hasUUID("Id")) members.add(identity.getUUID("Id"));
                }
                break;
            }
        } else {
            long now = level.getGameTime();
            ListTag recent = new ListTag();
            for (Tag element : boss.getPersistentData().getList(ATTACKERS, Tag.TAG_COMPOUND)) {
                CompoundTag identity = (CompoundTag) element;
                long age = now - identity.getLong("At");
                if (identity.hasUUID("Id") && age >= 0 && age <= 1200) {
                    members.add(identity.getUUID("Id"));
                    recent.add(identity.copy());
                }
            }
            boss.getPersistentData().put(ATTACKERS, recent);
            if (boss.getTarget() instanceof ServerPlayer target) members.add(target.getUUID());
        }
        int count = 0;
        var anchor = managed ? ManorFight.anchorOf(fight) : null;
        for (ServerPlayer player : level.players()) {
            if (!members.contains(player.getUUID()) || !player.isAlive() || player.isRemoved()
                    || player.isSpectator() || player.isCreative()) continue;
            boolean nearby = player.distanceToSqr(boss) <= SparrowSupport.FIGHT_RANGE * SparrowSupport.FIGHT_RANGE;
            if (!nearby && (anchor == null || player.distanceToSqr(anchor.x, anchor.y, anchor.z) > 96 * 96)) continue;
            count++;
        }
        return SparrowMultiplayerRules.quota(count);
    }

    private static SparrowAppearanceData.Entry appearance(ServerLevel level, EasterBossEntity boss, boolean visited) {
        UUID fight = ManorFightMembers.fightId(boss);
        int stage = ManorFight.currentVariant(fight);
        UUID owner = stage >= 0 ? fight : boss.getUUID();
        SparrowAppearanceData data = SparrowAppearanceData.get(level.getServer());
        var entry = data.entry(owner, stage, visited);
        if (entry.observe(participants(level, boss))) data.setDirty();
        return entry;
    }

    public static boolean exhausted(boolean original, LivingEntity entity) {
        Entity resolved = ManorFightMembers.resolve(entity);
        if (!(resolved instanceof EasterBossEntity boss) || !(boss.level() instanceof ServerLevel level)
                || !eligible(boss)) return original;
        ManorFightData fights = ManorFightData.get(level.getServer());
        if (fights.retired(ManorFightMembers.fightId(boss))) return true;
        var entry = appearance(level, boss, original);
        if (SparrowAppearanceData.get(level.getServer()).onCooldown(entry, level.getGameTime())) return true;
        return !SparrowMultiplayerRules.canAppear(entry.used, entry.players);
    }

    public static boolean planned(ManorFightData fights, UUID fight, int stage) {
        boolean original = fights.sparrowPlanned(fight, stage);
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || fights.retired(fight)) return original;
        UUID current = ManorFight.currentBoss(fight);
        if (current == null || stage != ManorFight.currentVariant(fight)) return original;
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(current);
            if (entity instanceof EasterBossEntity boss && eligible(boss)) {
                boolean visited = fights.sparrowVisited(fight, stage) || boss.getPersistentData().getBoolean(VISITED);
                var entry = appearance(level, boss, visited);
                return SparrowMultiplayerRules.planned(fight, stage, original, entry.players);
            }
        }
        return original;
    }

    public static double trial(double random, LivingEntity entity) {
        if (!(entity instanceof EasterBossEntity boss) || !(boss.level() instanceof ServerLevel level)
                || !eligible(boss)) return random;
        boolean visited = boss.getPersistentData().getBoolean(VISITED)
                || ManorFightData.get(level.getServer()).sparrowVisited(ManorFightMembers.fightId(boss));
        var entry = appearance(level, boss, visited);
        CompoundTag tag = boss.getPersistentData();
        long now = level.getGameTime();
        if (!tag.contains(TRIAL_TICK) || tag.getLong(TRIAL_TICK) != now) {
            tag.putLong(TRIAL_TICK, now);
            tag.putDouble(TRIAL_ROLL, random);
        }
        return tag.getDouble(TRIAL_ROLL) <= SparrowMultiplayerRules.standaloneChance(entry.players) ? 0 : 1;
    }

    public static void spawned(LivingEntity entity, EasterBossEntity bird) {
        if (!(entity instanceof EasterBossEntity boss) || !(boss.level() instanceof ServerLevel level)
                || !eligible(boss)) return;
        ManorFightData fights = ManorFightData.get(level.getServer());
        UUID fight = ManorFightMembers.fightId(boss);
        int stage = ManorFight.currentVariant(fight);
        boolean visited = stage >= 0 ? fights.sparrowVisited(fight, stage) : fights.sparrowVisited(fight);
        var entry = appearance(level, boss, visited || boss.getPersistentData().getBoolean(VISITED));
        SparrowAppearanceData data = SparrowAppearanceData.get(level.getServer());
        if (data.onCooldown(entry, level.getGameTime())) return;
        if (entry.record(bird.getUUID(), false)) data.markAppearance(entry, level.getGameTime());
    }

    public static void note(Entity member) {
        if (!(member instanceof EasterBossEntity bird) || bird.getVariant() != EasterBossEntity.SPARROW_VARIANT
                || bird.isImpossibleChallenge() || !(bird.level() instanceof ServerLevel level)) return;
        UUID fight = ManorFightMembers.fightId(bird);
        ManorFightData fights = ManorFightData.get(level.getServer());
        if (fights.retired(fight)) return;
        int stage = bird.getPersistentData().contains(STAGE) ? bird.getPersistentData().getInt(STAGE) : ManorFight.currentVariant(fight);
        Entity parent = ManorFightMembers.owner(bird);
        if (parent == null && fight != null && ManorFight.currentBoss(fight) != null) parent = level.getEntity(ManorFight.currentBoss(fight));
        UUID owner = stage >= 0 ? fight : parent != null ? parent.getUUID() : fight;
        if (owner == null) return;
        SparrowAppearanceData data = SparrowAppearanceData.get(level.getServer());
        boolean legacyVisit = stage >= 0 ? fights.sparrowVisited(fight, stage) : fights.sparrowVisited(fight);
        var entry = data.entry(owner, stage, legacyVisit);
        boolean legacyBird = legacyVisit && entry.used == 1 && entry.birds.isEmpty();
        if (parent instanceof EasterBossEntity boss && entry.observe(participants(level, boss))) data.setDirty();
        if (entry.record(bird.getUUID(), legacyBird)) data.setDirty();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent event) {
        if (event.isCanceled() || !(event.getAmount() > 0) || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || player.isSpectator() || player.isCreative() || !(event.getEntity() instanceof EasterBossEntity boss)
                || !(boss.level() instanceof ServerLevel level) || !eligible(boss)
                || ManorFight.currentVariant(ManorFightMembers.fightId(boss)) >= 0) return;
        ListTag previous = boss.getPersistentData().getList(ATTACKERS, Tag.TAG_COMPOUND);
        ListTag recent = new ListTag();
        for (Tag element : previous) {
            CompoundTag identity = (CompoundTag) element;
            long age = level.getGameTime() - identity.getLong("At");
            if (identity.hasUUID("Id") && !identity.getUUID("Id").equals(player.getUUID()) && age >= 0 && age <= 1200) recent.add(identity.copy());
        }
        CompoundTag identity = new CompoundTag();
        identity.putUUID("Id", player.getUUID());
        identity.putLong("At", level.getGameTime());
        recent.add(identity);
        boss.getPersistentData().put(ATTACKERS, recent);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (event.phase != TickEvent.Phase.END || server == null || server.overworld().getGameTime() % 1200 != 0) return;
        SparrowAppearanceData.get(server).prune(ManorFightData.get(server));
    }
}
