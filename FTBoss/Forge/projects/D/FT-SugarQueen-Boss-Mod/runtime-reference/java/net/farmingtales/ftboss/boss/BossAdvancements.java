/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.advancements.Advancement
 *  net.minecraft.advancements.AdvancementProgress
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.ServerAdvancementManager
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.RatSwarmEncounter;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class BossAdvancements {
    public static final List<String> NON_MANOR_BOSSES = List.of("sugar_queen", "meat_mountain", "tea_tree", "wine_barrel", "rat_boss", "defect_core", "benger", "tide_fish_boss", "apple_boss", "compost_boss");
    public static final List<String> SIGNATURE_BOSSES = List.of("sugar_queen", "meat_mountain", "tea_tree", "wine_barrel", "rat_boss");
    private static final double NEARBY_RADIUS = 32.0;
    public static final List<String> COMPLETION_BOSSES = NON_MANOR_BOSSES;
    public static final String MANOR_VICTORY = "memories_end";
    public static final String MANOR_IMPOSSIBLE_VICTORY = "manor_impossible_victory";
    private static final Map<String, String> TITLE_GATES = Map.of("title_memories_end", "memories_end", "title_food_boss_slayer_all", "food_boss_slayer_all", "title_manor_impossible", "");

    private BossAdvancements() {
    }

    public static String bossId(Entity entity) {
        Entity entity2;
        if (RatSwarmEncounter.skipVictory(entity)) {
            return null;
        }
        if (entity2 == null) {
            return null;
        }
        if (entity2 instanceof EasterBossEntity) {
            return null;
        }
        EntityType type = entity2.m_6095_();
        if (type == null) {
            return null;
        }
        ResourceLocation key = EntityType.m_20613_((EntityType)type);
        if (key == null || !"ftboss".equals(key.m_135827_())) {
            return null;
        }
        return NON_MANOR_BOSSES.contains(key.m_135815_()) ? key.m_135815_() : null;
    }

    public static String spottedId(String bossId) {
        return "boss_" + bossId + "_spotted";
    }

    public static String slainId(String bossId) {
        return "boss_" + bossId + "_slain";
    }

    public static void onBossSummoned(Entity entity) {
        String id = BossAdvancements.bossId(entity);
        if (id == null) {
            return;
        }
        Level level = entity.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        ServerPlayer owner = BossAdvancements.owner(entity, level2);
        if (owner == null) {
            return;
        }
        BossAdvancements.award(owner, BossAdvancements.spottedId(id));
    }

    public static void onBossDefeated(LivingEntity boss) {
        String id = BossAdvancements.bossId((Entity)boss);
        if (id == null) {
            return;
        }
        Level level = boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        List players = level2.m_45976_(ServerPlayer.class, boss.m_20191_().m_82400_(96.0));
        for (ServerPlayer p : players) {
            BossAdvancements.award(p, BossAdvancements.slainId(id));
            BossAdvancements.awardIfAllSlain(p, SIGNATURE_BOSSES, "boss_slayer_all");
            if (!BossAdvancements.awardIfAllSlain(p, COMPLETION_BOSSES, "food_boss_slayer_all")) continue;
            BossAdvancements.grantTitle(p, "title_food_boss_slayer_all");
        }
    }

    public static void onManorNormalVictory(ServerLevel level, Set<UUID> participants) {
        if (level == null || participants == null || participants.isEmpty()) {
            return;
        }
        for (UUID id : participants) {
            ServerPlayer player;
            ServerPlayer serverPlayer = player = level.m_7654_() == null ? null : level.m_7654_().m_6846_().m_11259_(id);
            if (player == null) continue;
            BossAdvancements.award(player, MANOR_VICTORY);
            BossAdvancements.grantTitle(player, "title_memories_end");
        }
    }

    public static void onManorImpossibleVictory(ServerLevel level, Set<UUID> participants) {
        if (level == null || participants == null || participants.isEmpty()) {
            return;
        }
        for (UUID id : participants) {
            ServerPlayer player;
            ServerPlayer serverPlayer = player = level.m_7654_() == null ? null : level.m_7654_().m_6846_().m_11259_(id);
            if (player == null) continue;
            BossAdvancements.award(player, MANOR_IMPOSSIBLE_VICTORY);
            BossAdvancements.grantTitle(player, "title_manor_impossible");
        }
    }

    private static boolean awardIfAllSlain(ServerPlayer player, List<String> ids, String advancementId) {
        try {
            ServerAdvancementManager advancements = player.f_8924_.m_129889_();
            for (String id : ids) {
                Advancement slain = advancements.m_136041_(new ResourceLocation("ftboss", BossAdvancements.slainId(id)));
                if (slain != null && player.m_8960_().m_135996_(slain).m_8193_()) continue;
                return false;
            }
            BossAdvancements.award(player, advancementId);
            return true;
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][\u8fdb\u5ea6] \u6210\u5c31\u5224\u5b9a\u5931\u8d25({}): {}", (Object)advancementId, (Object)t.toString());
            return false;
        }
    }

    private static ServerPlayer owner(Entity entity, ServerLevel level) {
        try {
            Mob mob;
            Player nearest;
            ServerPlayer online;
            Entity marked = ManorFightMembers.owner(entity);
            if (marked instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)marked;
                return sp;
            }
            if (marked != null && level.m_7654_() != null && (online = level.m_7654_().m_6846_().m_11259_(marked.m_20148_())) != null) {
                return online;
            }
            if (entity instanceof Mob && (nearest = level.m_45930_((Entity)(mob = (Mob)entity), 32.0)) instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)nearest;
                return sp;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    public static void award(ServerPlayer player, String id) {
        if (player == null) {
            return;
        }
        try {
            Advancement advancement = player.f_8924_.m_129889_().m_136041_(new ResourceLocation("ftboss", id));
            if (advancement == null) {
                FTBossMod.LOGGER.warn("[FTBoss][\u8fdb\u5ea6] \u627e\u4e0d\u5230\u8fdb\u5ea6 {}:{}", (Object)"ftboss", (Object)id);
                return;
            }
            AdvancementProgress progress = player.m_8960_().m_135996_(advancement);
            if (progress.m_8193_()) {
                return;
            }
            for (String criterion : advancement.m_138325_().keySet()) {
                player.m_8960_().m_135988_(advancement, criterion);
            }
            String title = advancement.m_138320_() == null ? id : advancement.m_138320_().m_14977_().getString();
            FTBossMod.LOGGER.info("[FTBoss][\u8fdb\u5ea6] {} \u89e3\u9501\u8fdb\u5ea6 {}({})", new Object[]{player.m_7755_().getString(), title, id});
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][\u8fdb\u5ea6] \u53d1\u653e {} \u5931\u8d25: {}", (Object)id, (Object)t.toString());
        }
    }

    public static Set<String> titleAdvancementIds() {
        return TITLE_GATES.keySet();
    }

    public static String titleGate(String titleAdvancementId) {
        return TITLE_GATES.get(titleAdvancementId);
    }

    public static boolean grantTitle(ServerPlayer player, String titleAdvancementId) {
        if (player == null || titleAdvancementId == null) {
            return false;
        }
        try {
            Advancement advancement = player.f_8924_.m_129889_().m_136041_(new ResourceLocation("ftboss", titleAdvancementId));
            if (advancement == null) {
                FTBossMod.LOGGER.warn("[FTBoss][\u5934\u8854] \u627e\u4e0d\u5230\u5934\u8854\u8fdb\u5ea6 {}:{}", (Object)"ftboss", (Object)titleAdvancementId);
                return false;
            }
            if (!player.m_8960_().m_135996_(advancement).m_8193_()) {
                player.m_8960_().m_135988_(advancement, "code_triggered");
            }
            return true;
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][\u5934\u8854] \u53d1\u653e {} \u5931\u8d25: {}", (Object)titleAdvancementId, (Object)t.toString());
            return false;
        }
    }

    public static boolean replayTitle(ServerPlayer player, String titleAdvancementId) {
        if (player == null || titleAdvancementId == null) {
            return false;
        }
        try {
            Advancement advancement = player.f_8924_.m_129889_().m_136041_(new ResourceLocation("ftboss", titleAdvancementId));
            if (advancement == null) {
                FTBossMod.LOGGER.warn("[FTBoss][\u5934\u8854] \u627e\u4e0d\u5230\u5934\u8854\u8fdb\u5ea6 {}:{}", (Object)"ftboss", (Object)titleAdvancementId);
                return false;
            }
            AdvancementProgress progress = player.m_8960_().m_135996_(advancement);
            for (String criterion : advancement.m_138325_().keySet()) {
                if (progress.m_8214_(criterion) == null || !progress.m_8214_(criterion).m_12911_()) continue;
                player.m_8960_().m_135998_(advancement, criterion);
            }
            return BossAdvancements.grantTitle(player, titleAdvancementId);
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][\u5934\u8854] \u91cd\u653e {} \u5931\u8d25: {}", (Object)titleAdvancementId, (Object)t.toString());
            return false;
        }
    }

    public static boolean hasEarned(ServerPlayer player, String advancementId) {
        if (player == null || advancementId == null) {
            return false;
        }
        try {
            Advancement advancement = player.f_8924_.m_129889_().m_136041_(new ResourceLocation("ftboss", advancementId));
            return advancement != null && player.m_8960_().m_135996_(advancement).m_8193_();
        }
        catch (Throwable t) {
            return false;
        }
    }
}

