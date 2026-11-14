/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.server.ServerLifecycleHooks
 */
package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ImpossibleSparrowSupport;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightData;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.SparrowMultiplayerSupport;
import net.farmingtales.ftboss.boss.ai.FightState;
import net.farmingtales.ftboss.boss.ai.XuanlanEncounterLimits;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.maque.support.MaqueSupportHooks;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid="ftboss")
public final class SparrowSupport {
    public static final int CHECK_INTERVAL = 200;
    public static final double CHANCE = 0.5;
    public static final double FIGHT_RANGE = 48.0;
    public static final double DUPLICATE_RANGE = 96.0;
    public static final float SPARROW_HEALTH = 500.0f;
    public static final int MIN_BOSSES = 1;
    private static long lastPump = -1L;
    private static UUID aliveSparrow = null;
    private static final String VISITED = "ftboss_sparrow_visited";
    private static final String STAGE = "ftboss_sparrow_stage";

    private SparrowSupport() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) {
            return;
        }
        SparrowSupport.tick(ServerLifecycleHooks.getCurrentServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        lastPump = -1L;
        aliveSparrow = null;
    }

    public static void tick(MinecraftServer server) {
        if (server == null) {
            return;
        }
        long now = server.m_129783_().m_46467_();
        if (now == lastPump) {
            return;
        }
        lastPump = now;
        if (now % 200L != 0L) {
            return;
        }
        int alive = SparrowSupport.countAliveSparrows(server);
        if (alive > 0) {
            if (alive > 1) {
                FTBossMod.LOGGER.warn("[FTBoss][Easter] \u573a\u4e0a\u540c\u65f6\u6709 {} \u53ea\u9ebb\u96c0(\u5e94\u6309\u89c4\u5219\u53ea\u5141\u8bb8 1 \u53ea) \u2014\u2014 \u5df2\u963b\u6b62\u7ee7\u7eed\u5237\u65b0", (Object)alive);
            }
            return;
        }
        aliveSparrow = null;
        for (ServerLevel sl : server.m_129785_()) {
            for (ServerPlayer p : sl.m_6907_()) {
                List<LivingEntity> bosses;
                if (!p.m_6084_() || p.m_5833_() || (bosses = SparrowSupport.bossesNear(sl, p)).size() < 1) continue;
                LivingEntity candidate = null;
                for (LivingEntity boss : bosses) {
                    UUID fight;
                    int stage;
                    if (SparrowSupport.hasVisited(boss) || (stage = ManorFight.currentVariant(fight = ManorFightMembers.fightId((Entity)boss))) >= 0 && ManorFight.difficulty(fight) != ManorDifficulty.IMPOSSIBLE && !SparrowMultiplayerSupport.planned(ManorFightData.get(server), fight, stage) || (!ManorFight.isManaged(boss) || !ManorFight.isPlayerFighting((Player)p)) && !FightState.isEngagedWith(boss, (Player)p)) continue;
                    candidate = boss;
                    break;
                }
                if (candidate == null || ManorFight.currentVariant(ManorFightMembers.fightId(candidate)) < 0 && SparrowMultiplayerSupport.trial(sl.f_46441_.m_188500_(), candidate) > 0.5) continue;
                SparrowSupport.spawnSparrow(sl, candidate);
                return;
            }
        }
    }

    public static boolean hasVisited(UUID fightId) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null && ManorFightData.get(server).sparrowVisited(fightId);
    }

    public static boolean hasVisited(LivingEntity boss) {
        EasterBossEntity manor;
        Level level;
        Entity member = ManorFightMembers.resolve((Entity)boss);
        if (member == null || !((level = member.m_9236_()) instanceof ServerLevel)) {
            return SparrowMultiplayerSupport.exhausted(false, boss);
        }
        ServerLevel level2 = (ServerLevel)level;
        UUID fight = ManorFightMembers.fightId(member);
        ManorFightData data = ManorFightData.get(level2.m_7654_());
        if (member instanceof EasterBossEntity && (manor = (EasterBossEntity)member).isImpossibleChallenge()) {
            return SparrowMultiplayerSupport.exhausted(data.retired(fight), boss);
        }
        int stage = ManorFight.currentVariant(fight);
        if (stage >= 0) {
            return SparrowMultiplayerSupport.exhausted(data.retired(fight) || data.sparrowVisited(fight, stage) || member.getPersistentData().m_128471_(VISITED), boss);
        }
        return SparrowMultiplayerSupport.exhausted(data.retired(fight) || data.sparrowVisited(fight) || member.getPersistentData().m_128471_(VISITED), boss);
    }

    public static void noteFightMember(Entity entity) {
        EasterBossEntity boss;
        Entity member;
        SparrowMultiplayerSupport.note(entity);
        Level level = member.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        if (member instanceof EasterBossEntity && (boss = (EasterBossEntity)member).getVariant() == 9 || member.getPersistentData().m_128471_(VISITED)) {
            int stage;
            member.getPersistentData().m_128379_(VISITED, true);
            UUID fight = ManorFightMembers.fightId(member);
            ManorFightData data = ManorFightData.get(level2.m_7654_());
            int n = stage = member.getPersistentData().m_128441_(STAGE) ? member.getPersistentData().m_128451_(STAGE) : ManorFight.currentVariant(fight);
            if (stage >= 0) {
                member.getPersistentData().m_128405_(STAGE, stage);
                data.visitSparrow(fight, stage);
            } else {
                data.visitSparrow(fight);
            }
        }
    }

    public static void beginStage(ServerLevel level, EasterBossEntity boss) {
        if (boss.isImpossibleChallenge()) {
            ImpossibleSparrowSupport.ensure(level, boss);
            return;
        }
        UUID fight = ManorFightMembers.fightId((Entity)boss);
        if (fight == null || !boss.m_20148_().equals(ManorFight.currentBoss(fight))) {
            return;
        }
        ManorFightData data = ManorFightData.get(level.m_7654_());
        data.sparrowPlan(fight);
        SparrowSupport.noteFightMember((Entity)boss);
        for (Entity entity : level.m_8583_()) {
            EasterBossEntity sparrow;
            if (!(entity instanceof EasterBossEntity) || !(sparrow = (EasterBossEntity)entity).m_6084_() || sparrow.m_213877_() || sparrow.getVariant() != 9 || !fight.equals(ManorFightMembers.fightId((Entity)sparrow))) continue;
            if (sparrow.getPersistentData().m_128441_(STAGE) && sparrow.getPersistentData().m_128451_(STAGE) != boss.getVariant()) {
                sparrow.m_146870_();
                continue;
            }
            SparrowSupport.noteFightMember((Entity)sparrow);
            boss.getPersistentData().m_128379_(VISITED, true);
        }
        if (SparrowMultiplayerSupport.planned(data, fight, boss.getVariant())) {
            SparrowSupport.spawnSparrow(level, (LivingEntity)boss);
        }
    }

    public static void endStage(ServerLevel level, UUID fight) {
        if (ManorFight.difficulty(fight) == ManorDifficulty.IMPOSSIBLE) {
            return;
        }
        for (Entity entity : level.m_8583_()) {
            EasterBossEntity sparrow;
            if (!(entity instanceof EasterBossEntity) || (sparrow = (EasterBossEntity)entity).getVariant() != 9 || !fight.equals(ManorFightMembers.fightId((Entity)sparrow))) continue;
            sparrow.m_146870_();
        }
    }

    public static int countAliveSparrows(MinecraftServer server) {
        if (server == null) {
            return 0;
        }
        int n = 0;
        for (ServerLevel lv : server.m_129785_()) {
            for (Entity e : lv.m_8583_()) {
                EasterBossEntity eb;
                if (!(e instanceof EasterBossEntity) || (eb = (EasterBossEntity)e).getVariant() != 9 || !eb.m_6084_() || eb.m_213877_()) continue;
                int stage = ManorFight.currentVariant(ManorFightMembers.fightId((Entity)eb));
                if (!eb.isImpossibleChallenge() && stage >= 0 && eb.getPersistentData().m_128441_(STAGE) && eb.getPersistentData().m_128451_(STAGE) != stage) {
                    eb.m_146870_();
                    continue;
                }
                SparrowSupport.noteFightMember((Entity)eb);
                ++n;
            }
        }
        return n;
    }

    private static List<LivingEntity> bossesNear(ServerLevel sl, ServerPlayer p) {
        ArrayList<LivingEntity> out = new ArrayList<LivingEntity>();
        for (EasterBossEntity boss : sl.m_45976_(EasterBossEntity.class, p.m_20191_().m_82400_(48.0))) {
            if (!boss.m_6084_() || boss.isMini() || boss.getVariant() == 9 || HiddenBossIntro.isActing(boss.m_20148_())) continue;
            out.add((LivingEntity)boss);
        }
        return out;
    }

    public static void noteSparrow(EasterBossEntity sparrow) {
        aliveSparrow = sparrow.m_20148_();
        SparrowSupport.noteFightMember((Entity)sparrow);
    }

    public static void forgetSparrow() {
        aliveSparrow = null;
    }

    /*
     * WARNING - void declaration
     */
    public static void spawnSparrow(ServerLevel serverLevel, LivingEntity livingEntity) {
        String string;
        EasterBossEntity manorBoss;
        void boss;
        ServerLevel sl;
        block13: {
            block12: {
                if (XuanlanEncounterLimits.blocksSupport((Entity)livingEntity)) {
                    return;
                }
                if (sl == null || !(boss instanceof EasterBossEntity)) break block12;
                manorBoss = (EasterBossEntity)boss;
                if (boss.m_9236_() == sl && boss.m_6084_() && !boss.m_213877_() && !manorBoss.isMini() && manorBoss.getVariant() != 9 && !HiddenBossIntro.isActing(boss.m_20148_()) && !SparrowSupport.hasVisited((LivingEntity)boss)) break block13;
            }
            return;
        }
        UUID fight = ManorFightMembers.fightId((Entity)boss);
        int stage = ManorFight.currentVariant(fight);
        if (!(stage < 0 || boss.m_20148_().equals(ManorFight.currentBoss(fight)) && (manorBoss.isImpossibleChallenge() || SparrowMultiplayerSupport.planned(ManorFightData.get(sl.m_7654_()), fight, stage)))) {
            return;
        }
        if (ImpossibleSparrowSupport.reserved(sl.m_7654_()) || SparrowSupport.countAliveSparrows(sl.m_7654_()) > 0) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u573a\u4e0a\u5df2\u6709\u9ebb\u96c0, \u8df3\u8fc7\u672c\u6b21\u53ec\u5524");
            return;
        }
        EasterBossEntity s = (EasterBossEntity)((EntityType)FTEntities.EASTER_BOSS.get()).m_20615_((Level)sl);
        if (s == null) {
            return;
        }
        double ang = sl.f_46441_.m_188500_() * Math.PI * 2.0;
        double x = boss.m_20185_() + Math.cos(ang) * 6.0;
        double z = boss.m_20189_() + Math.sin(ang) * 6.0;
        double y = boss.m_20186_() + 6.0;
        s.m_7678_(x, y, z, sl.f_46441_.m_188501_() * 360.0f, 0.0f);
        s.setVariant(9);
        ManorFightMembers.inherit((Entity)boss, (Entity)s);
        s.m_21530_();
        s.m_6593_((Component)Component.m_237113_((String)"\u00a7e\u9ebb\u96c0"));
        s.m_20340_(true);
        AttributeInstance hp = s.m_21051_(Attributes.f_22276_);
        float health = ImpossibleSparrowSupport.health(s);
        if (hp != null) {
            hp.m_22100_((double)health);
        }
        s.m_21153_(health);
        if (!sl.m_7967_((Entity)s)) {
            ImpossibleSparrowSupport.failedSpawn(s);
            return;
        }
        SparrowMultiplayerSupport.spawned((LivingEntity)boss, s);
        Entity member = ManorFightMembers.resolve((Entity)boss);
        member.getPersistentData().m_128379_(VISITED, true);
        SparrowSupport.noteFightMember(member);
        EasterBossEntity easterBossEntity = s;
        MaqueSupportHooks.arrival((Entity)easterBossEntity);
        SparrowSupport.noteSparrow(easterBossEntity);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u9ebb\u96c0\u8fdb\u573a: variant={} \u540d\u5b57={} \u8840\u91cf={} @{},{},{}", new Object[]{s.getVariant(), s.m_7755_().getString(), (int)health, (int)x, (int)y, (int)z});
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123796_, x, y + 0.5, z, 30, 0.5, 0.5, 0.5, 0.08);
        sl.m_6263_(null, x, y, z, SoundEvents.f_12188_, SoundSource.HOSTILE, 1.4f, 1.2f);
        for (ServerPlayer p : sl.m_6907_()) {
            if (!(p.m_20280_((Entity)boss) <= 9216.0)) continue;
            p.m_213846_((Component)Component.m_237113_((String)"\u00a7e\u3010\u9ebb\u96c0\u3011\u00a77\u4e00\u53ea\u9ebb\u96c0\u98de\u8fdb\u4e86\u6218\u573a \u2014\u2014 \u5b83\u5728\u7ed9 Boss \u00a7c\u56de\u8840 / \u4e0a\u589e\u76ca / \u6e05\u51cf\u76ca\u00a77\uff01"));
        }
        if (boss instanceof EasterBossEntity) {
            EasterBossEntity eb = (EasterBossEntity)boss;
            string = EasterBossEntity.NAMES[eb.getVariant()];
        } else {
            string = boss.m_7755_().getString();
        }
        String bossName = string;
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u9ebb\u96c0\u968f\u673a\u52a0\u5165\u6218\u6597, \u652f\u63f4 {}", (Object)bossName);
    }
}

