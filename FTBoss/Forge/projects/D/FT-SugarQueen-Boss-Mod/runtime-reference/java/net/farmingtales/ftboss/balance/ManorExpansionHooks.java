/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cn.farmingtales.qysevent.worldevent.DailyWorldEvent
 *  cn.farmingtales.qysevent.worldevent.DailyWorldEventManager
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.network.SyncManaPacket
 *  io.redspace.ironsspellbooks.setup.Messages
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.balance;

import cn.farmingtales.qysevent.worldevent.DailyWorldEvent;
import cn.farmingtales.qysevent.worldevent.DailyWorldEventManager;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.setup.Messages;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.balance.ManorExpansionRules;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.turret.XuanlanTurretEntity;
import net.farmingtales.ftboss.turret.XuanlanTurrets;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ManorExpansionHooks {
    private static final Map<EasterBossEntity, Map<UUID, Long>> DRAINS = new WeakHashMap<EasterBossEntity, Map<UUID, Long>>();
    private static final Map<EasterBossEntity, Integer> BULLETS = new WeakHashMap<EasterBossEntity, Integer>();
    private static final Map<EasterBossEntity, Long> TURRETS = new WeakHashMap<EasterBossEntity, Long>();
    private static boolean snowFailureLogged;

    private ManorExpansionHooks() {
    }

    public static void nova(EasterBossEntity boss, ServerLevel level, Vec3 origin, int phase) {
        if (!ManorExpansionRules.warningPhase(phase) || !boss.m_6084_() || boss.m_213877_()) {
            DRAINS.remove(boss);
            return;
        }
        Map inside = DRAINS.computeIfAbsent(boss, ignored -> new HashMap());
        long now = level.m_46467_();
        List<ServerPlayer> players = ManorBalanceHooks.players(boss, level);
        inside.keySet().removeIf(id -> players.stream().noneMatch(player -> player.m_20148_().equals(id) && ManorExpansionHooks.inNova(player, origin)));
        for (ServerPlayer player : players) {
            long previous;
            if (!ManorExpansionHooks.inNova(player, origin) || !ManorExpansionRules.drainDue(now, previous = inside.computeIfAbsent(player.m_20148_(), ignored -> now).longValue())) continue;
            inside.put(player.m_20148_(), now);
            MagicData data = MagicData.getPlayerMagicData((LivingEntity)player);
            data.setMana(ManorExpansionRules.drainedMana(data.getMana()));
            Messages.sendToPlayer((Object)new SyncManaPacket(data), (ServerPlayer)player);
            level.m_8767_((ParticleOptions)ParticleTypes.f_123771_, player.m_20185_(), player.m_20186_() + 0.5, player.m_20189_(), 3, 0.25, 0.2, 0.25, 0.01);
        }
    }

    private static boolean inNova(ServerPlayer player, Vec3 origin) {
        Vec3 offset = player.m_20182_().m_82546_(origin);
        return offset.f_82479_ * offset.f_82479_ + offset.f_82481_ * offset.f_82481_ <= 2304.0 && Math.abs(offset.f_82480_) <= 32.0;
    }

    public static void bullet(EasterBossEntity boss, ServerLevel level, LivingEntity original) {
        if (boss.getVariant() != 2 || !ManorBalanceHooks.mainBoss(boss)) {
            return;
        }
        List<ServerPlayer> players = ManorBalanceHooks.players(boss, level);
        if (players.size() < 2) {
            BULLETS.remove(boss);
            return;
        }
        int count = BULLETS.getOrDefault(boss, 0) + 1;
        BULLETS.put(boss, count % 10);
        if (!ManorExpansionRules.tenthBullet(count)) {
            return;
        }
        ServerPlayer far = players.stream().filter(player -> player != original && boss.m_20280_((Entity)player) >= 144.0 && boss.m_20280_((Entity)player) <= 4096.0).filter(player -> ManorExpansionHooks.clear(level, boss.m_146892_(), player.m_20191_().m_82399_(), (Entity)boss)).max(Comparator.comparingDouble(arg_0 -> ((EasterBossEntity)boss).m_20280_(arg_0))).orElse(null);
        if (far == null) {
            return;
        }
        Vec3 from = boss.m_146892_().m_82520_(0.0, -0.15, 0.0);
        Vec3 end = far.m_20191_().m_82399_();
        WeaponFx.send(level, WeaponFxRules.Kind.DORA_TRACER, WeaponFxRules.Theme.ARC, from, end, 0.12, boss.m_146908_());
        far.f_19802_ = 0;
        far.m_6469_(boss.m_269291_().m_269333_((LivingEntity)boss), 6.666667f);
        far.f_19802_ = 0;
        far.m_6469_(boss.m_269291_().m_269104_((Entity)boss, (Entity)boss), 6.666667f);
        WeaponFx.send(level, WeaponFxRules.Kind.DORA_IMPACT, WeaponFxRules.Theme.GOLD, end, end, 0.34, boss.m_146908_());
    }

    public static boolean clear(ServerLevel level, Vec3 from, Vec3 to, Entity source) {
        return level.m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source)).m_6662_() == HitResult.Type.MISS;
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event) {
        ServerLevel level;
        EasterBossEntity boss;
        block10: {
            block9: {
                LivingEntity livingEntity = event.getEntity();
                if (!(livingEntity instanceof EasterBossEntity) || !((livingEntity = (boss = (EasterBossEntity)livingEntity).m_9236_()) instanceof ServerLevel)) break block9;
                level = (ServerLevel)livingEntity;
                if (ManorBalanceHooks.mainBoss(boss) && boss.m_6084_()) break block10;
            }
            return;
        }
        if (boss.m_21525_() || boss.m_20145_() || boss.isManorIntroHidden() || HiddenBossIntro.isActing(boss.m_20148_())) {
            return;
        }
        if (boss.getVariant() == 8 && !boss.getPersistentData().m_128471_("ftboss.zicaiSnowStarted")) {
            try {
                if (DailyWorldEventManager.active((MinecraftServer)level.m_7654_()) != DailyWorldEvent.HEAVY_SNOW) {
                    DailyWorldEventManager.setActiveForTesting((MinecraftServer)level.m_7654_(), (DailyWorldEvent)DailyWorldEvent.HEAVY_SNOW);
                }
                boss.getPersistentData().m_128379_("ftboss.zicaiSnowStarted", true);
            }
            catch (LinkageError failure) {
                if (!snowFailureLogged) {
                    FTBossMod.LOGGER.error("[FTBoss] \u5927\u96ea\u4e8b\u4ef6\u63a5\u53e3\u4e0d\u53ef\u7528\uff0c\u672a\u66ff\u6362\u6210\u666e\u901a\u5929\u6c14", (Throwable)failure);
                }
                snowFailureLogged = true;
            }
        }
    }

    public static boolean dropTurret(EasterBossEntity boss, ServerLevel level) {
        long previous;
        long now = level.m_46467_();
        if (now < (previous = TURRETS.computeIfAbsent(boss, ignored -> now - 100L).longValue()) || now - previous < 100L) {
            return false;
        }
        for (int attempt = 0; attempt < 8; ++attempt) {
            BlockPos candidate;
            double angle = (double)attempt * Math.PI / 4.0 + (double)boss.m_146908_() * Math.PI / 180.0;
            BlockPos floor = null;
            for (int down = 0; down < 48 && level.m_46805_(candidate = BlockPos.m_274561_((double)(boss.m_20185_() + Math.cos(angle) * 3.0), (double)(boss.m_20186_() - 1.0 - (double)down), (double)(boss.m_20189_() + Math.sin(angle) * 3.0))); ++down) {
                if (!level.m_8055_(candidate).m_60804_((BlockGetter)level, candidate)) continue;
                floor = candidate;
                break;
            }
            if (floor == null) continue;
            XuanlanTurretEntity turret = new XuanlanTurretEntity(XuanlanTurrets.TYPE, (Level)level);
            turret.setOwner(boss);
            turret.m_7678_((double)floor.m_123341_() + 0.5, (double)floor.m_123342_() + 1.0, (double)floor.m_123343_() + 0.5, boss.m_146908_(), 0.0f);
            ManorFightMembers.inherit((Entity)boss, (Entity)turret);
            if (!level.m_45786_((Entity)turret) || !level.m_7967_((Entity)turret)) continue;
            TURRETS.put(boss, now);
            return true;
        }
        TURRETS.put(boss, now - 100L + 20L);
        return false;
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        DRAINS.clear();
        BULLETS.clear();
        TURRETS.clear();
        snowFailureLogged = false;
    }
}

