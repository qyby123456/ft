/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.server.ServerLifecycleHooks
 */
package net.farmingtales.ftboss.boss;

import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightData;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.SparrowAppearanceData;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.SparrowMultiplayerRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid="ftboss")
public final class SparrowMultiplayerSupport {
    private static final String ATTACKERS = "ftboss_sparrow_attackers";
    private static final String VISITED = "ftboss_sparrow_visited";
    private static final String STAGE = "ftboss_sparrow_stage";
    private static final String TRIAL_TICK = "ftboss_sparrow_trial_tick";
    private static final String TRIAL_ROLL = "ftboss_sparrow_trial_roll";

    private SparrowMultiplayerSupport() {
    }

    private static boolean eligible(EasterBossEntity easterBossEntity) {
        return easterBossEntity.m_6084_() && !easterBossEntity.m_213877_() && !easterBossEntity.isMini() && EasterBossEntity.isManorVariant(easterBossEntity.getVariant()) && !easterBossEntity.isImpossibleChallenge();
    }

    public static int participants(ServerLevel serverLevel, EasterBossEntity easterBossEntity) {
        Tag tag2;
        boolean bl;
        UUID uUID = ManorFightMembers.fightId((Entity)easterBossEntity);
        HashSet<UUID> hashSet = new HashSet<UUID>();
        boolean bl2 = bl = ManorFight.currentVariant(uUID) >= 0;
        if (bl) {
            for (Tag tag2 : ManorFightData.get(serverLevel.m_7654_()).active()) {
                var7_9 = (CompoundTag)tag2;
                if (!var7_9.m_128403_("Id") || !uUID.equals(var7_9.m_128342_("Id"))) continue;
                if (var7_9.m_128403_("Summoner")) {
                    hashSet.add(var7_9.m_128342_("Summoner"));
                }
                for (Tag tag32 : var7_9.m_128437_("Participants", 10)) {
                    var10_13 = (CompoundTag)tag32;
                    if (!var10_13.m_128403_("Id")) continue;
                    hashSet.add(var10_13.m_128342_("Id"));
                }
                break;
            }
        } else {
            Tag tag32;
            long l = serverLevel.m_46467_();
            var7_9 = new ListTag();
            for (Tag tag32 : easterBossEntity.getPersistentData().m_128437_(ATTACKERS, 10)) {
                var10_13 = (CompoundTag)tag32;
                long l2 = l - var10_13.m_128454_("At");
                if (!var10_13.m_128403_("Id") || l2 < 0L || l2 > 1200L) continue;
                hashSet.add(var10_13.m_128342_("Id"));
                var7_9.add((Object)var10_13.m_6426_());
            }
            easterBossEntity.getPersistentData().m_128365_(ATTACKERS, (Tag)var7_9);
            tag32 = easterBossEntity.m_5448_();
            if (tag32 instanceof ServerPlayer) {
                Iterator iterator = (ServerPlayer)tag32;
                hashSet.add(iterator.m_20148_());
            }
        }
        int n = 0;
        tag2 = bl ? ManorFight.anchorOf(uUID) : null;
        for (Iterator iterator : serverLevel.m_6907_()) {
            boolean bl3;
            if (!hashSet.contains(iterator.m_20148_()) || !iterator.m_6084_() || iterator.m_213877_() || iterator.m_5833_() || iterator.m_7500_()) continue;
            boolean bl4 = bl3 = iterator.m_20280_((Entity)easterBossEntity) <= 2304.0;
            if (!bl3 && (tag2 == null || iterator.m_20275_(tag2.f_82479_, tag2.f_82480_, tag2.f_82481_) > 9216.0)) continue;
            ++n;
        }
        return SparrowMultiplayerRules.quota(n);
    }

    private static SparrowAppearanceData.Entry appearance(ServerLevel serverLevel, EasterBossEntity easterBossEntity, boolean bl) {
        UUID uUID = ManorFightMembers.fightId((Entity)easterBossEntity);
        int n = ManorFight.currentVariant(uUID);
        UUID uUID2 = n >= 0 ? uUID : easterBossEntity.m_20148_();
        SparrowAppearanceData sparrowAppearanceData = SparrowAppearanceData.get(serverLevel.m_7654_());
        SparrowAppearanceData.Entry entry = sparrowAppearanceData.entry(uUID2, n, bl);
        if (entry.observe(SparrowMultiplayerSupport.participants(serverLevel, easterBossEntity))) {
            sparrowAppearanceData.m_77762_();
        }
        return entry;
    }

    public static boolean exhausted(boolean bl, LivingEntity livingEntity) {
        ServerLevel serverLevel;
        EasterBossEntity easterBossEntity;
        Object object;
        block6: {
            block5: {
                Entity entity = ManorFightMembers.resolve((Entity)livingEntity);
                if (!(entity instanceof EasterBossEntity) || !((object = (easterBossEntity = (EasterBossEntity)entity).m_9236_()) instanceof ServerLevel)) break block5;
                serverLevel = (ServerLevel)object;
                if (SparrowMultiplayerSupport.eligible(easterBossEntity)) break block6;
            }
            return bl;
        }
        object = ManorFightData.get(serverLevel.m_7654_());
        if (object.retired(ManorFightMembers.fightId((Entity)easterBossEntity))) {
            return true;
        }
        SparrowAppearanceData.Entry entry = SparrowMultiplayerSupport.appearance(serverLevel, easterBossEntity, bl);
        if (SparrowAppearanceData.get(serverLevel.m_7654_()).onCooldown(entry, serverLevel.m_46467_())) {
            return true;
        }
        return !SparrowMultiplayerRules.canAppear(entry.used, entry.players);
    }

    public static boolean planned(ManorFightData manorFightData, UUID uUID, int n) {
        boolean bl = manorFightData.sparrowPlanned(uUID, n);
        MinecraftServer minecraftServer = ServerLifecycleHooks.getCurrentServer();
        if (minecraftServer == null || manorFightData.retired(uUID)) {
            return bl;
        }
        UUID uUID2 = ManorFight.currentBoss(uUID);
        if (uUID2 == null || n != ManorFight.currentVariant(uUID)) {
            return bl;
        }
        for (ServerLevel serverLevel : minecraftServer.m_129785_()) {
            EasterBossEntity easterBossEntity;
            Entity entity = serverLevel.m_8791_(uUID2);
            if (!(entity instanceof EasterBossEntity) || !SparrowMultiplayerSupport.eligible(easterBossEntity = (EasterBossEntity)entity)) continue;
            boolean bl2 = manorFightData.sparrowVisited(uUID, n) || easterBossEntity.getPersistentData().m_128471_(VISITED);
            SparrowAppearanceData.Entry entry = SparrowMultiplayerSupport.appearance(serverLevel, easterBossEntity, bl2);
            return SparrowMultiplayerRules.planned(uUID, n, bl, entry.players);
        }
        return bl;
    }

    public static double trial(double d, LivingEntity livingEntity) {
        ServerLevel serverLevel;
        EasterBossEntity easterBossEntity;
        block5: {
            block4: {
                Level level;
                if (!(livingEntity instanceof EasterBossEntity) || !((level = (easterBossEntity = (EasterBossEntity)livingEntity).m_9236_()) instanceof ServerLevel)) break block4;
                serverLevel = (ServerLevel)level;
                if (SparrowMultiplayerSupport.eligible(easterBossEntity)) break block5;
            }
            return d;
        }
        boolean bl = easterBossEntity.getPersistentData().m_128471_(VISITED) || ManorFightData.get(serverLevel.m_7654_()).sparrowVisited(ManorFightMembers.fightId((Entity)easterBossEntity));
        SparrowAppearanceData.Entry entry = SparrowMultiplayerSupport.appearance(serverLevel, easterBossEntity, bl);
        CompoundTag compoundTag = easterBossEntity.getPersistentData();
        long l = serverLevel.m_46467_();
        if (!compoundTag.m_128441_(TRIAL_TICK) || compoundTag.m_128454_(TRIAL_TICK) != l) {
            compoundTag.m_128356_(TRIAL_TICK, l);
            compoundTag.m_128347_(TRIAL_ROLL, d);
        }
        return compoundTag.m_128459_(TRIAL_ROLL) <= SparrowMultiplayerRules.standaloneChance(entry.players) ? 0.0 : 1.0;
    }

    public static void spawned(LivingEntity livingEntity, EasterBossEntity easterBossEntity) {
        ServerLevel serverLevel;
        EasterBossEntity easterBossEntity2;
        Object object;
        block6: {
            block5: {
                if (!(livingEntity instanceof EasterBossEntity) || !((object = (easterBossEntity2 = (EasterBossEntity)livingEntity).m_9236_()) instanceof ServerLevel)) break block5;
                serverLevel = (ServerLevel)object;
                if (SparrowMultiplayerSupport.eligible(easterBossEntity2)) break block6;
            }
            return;
        }
        object = ManorFightData.get(serverLevel.m_7654_());
        UUID uUID = ManorFightMembers.fightId((Entity)easterBossEntity2);
        int n = ManorFight.currentVariant(uUID);
        boolean bl = n >= 0 ? ((ManorFightData)((Object)object)).sparrowVisited(uUID, n) : ((ManorFightData)((Object)object)).sparrowVisited(uUID);
        SparrowAppearanceData.Entry entry = SparrowMultiplayerSupport.appearance(serverLevel, easterBossEntity2, bl || easterBossEntity2.getPersistentData().m_128471_(VISITED));
        SparrowAppearanceData sparrowAppearanceData = SparrowAppearanceData.get(serverLevel.m_7654_());
        if (sparrowAppearanceData.onCooldown(entry, serverLevel.m_46467_())) {
            return;
        }
        if (entry.record(easterBossEntity.m_20148_(), false)) {
            sparrowAppearanceData.markAppearance(entry, serverLevel.m_46467_());
        }
    }

    public static void note(Entity entity) {
        EasterBossEntity easterBossEntity;
        boolean bl;
        Object object;
        Object object2;
        EasterBossEntity easterBossEntity2;
        if (!(entity instanceof EasterBossEntity) || (easterBossEntity2 = (EasterBossEntity)entity).getVariant() != 9 || easterBossEntity2.isImpossibleChallenge() || !((object2 = easterBossEntity2.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object2;
        object2 = ManorFightMembers.fightId((Entity)easterBossEntity2);
        ManorFightData manorFightData = ManorFightData.get(serverLevel.m_7654_());
        if (manorFightData.retired((UUID)object2)) {
            return;
        }
        int n = easterBossEntity2.getPersistentData().m_128441_(STAGE) ? easterBossEntity2.getPersistentData().m_128451_(STAGE) : ManorFight.currentVariant((UUID)object2);
        Entity entity2 = ManorFightMembers.owner((Entity)easterBossEntity2);
        if (entity2 == null && object2 != null && ManorFight.currentBoss((UUID)object2) != null) {
            entity2 = serverLevel.m_8791_(ManorFight.currentBoss((UUID)object2));
        }
        Object object3 = n >= 0 ? object2 : (object = entity2 != null ? entity2.m_20148_() : object2);
        if (object == null) {
            return;
        }
        SparrowAppearanceData sparrowAppearanceData = SparrowAppearanceData.get(serverLevel.m_7654_());
        boolean bl2 = n >= 0 ? manorFightData.sparrowVisited((UUID)object2, n) : manorFightData.sparrowVisited((UUID)object2);
        SparrowAppearanceData.Entry entry = sparrowAppearanceData.entry((UUID)object, n, bl2);
        boolean bl3 = bl = bl2 && entry.used == 1 && entry.birds.isEmpty();
        if (entity2 instanceof EasterBossEntity && entry.observe(SparrowMultiplayerSupport.participants(serverLevel, easterBossEntity = (EasterBossEntity)entity2))) {
            sparrowAppearanceData.m_77762_();
        }
        if (entry.record(easterBossEntity2.m_20148_(), bl)) {
            sparrowAppearanceData.m_77762_();
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent livingDamageEvent) {
        ServerLevel serverLevel;
        EasterBossEntity easterBossEntity;
        ServerPlayer serverPlayer;
        Entity entity;
        block4: {
            block3: {
                if (livingDamageEvent.isCanceled() || !(livingDamageEvent.getAmount() > 0.0f) || !((entity = livingDamageEvent.getSource().m_7639_()) instanceof ServerPlayer) || (serverPlayer = (ServerPlayer)entity).m_5833_() || serverPlayer.m_7500_() || !((entity = livingDamageEvent.getEntity()) instanceof EasterBossEntity) || !((entity = (easterBossEntity = (EasterBossEntity)entity).m_9236_()) instanceof ServerLevel)) break block3;
                serverLevel = (ServerLevel)entity;
                if (SparrowMultiplayerSupport.eligible(easterBossEntity) && ManorFight.currentVariant(ManorFightMembers.fightId((Entity)easterBossEntity)) < 0) break block4;
            }
            return;
        }
        entity = easterBossEntity.getPersistentData().m_128437_(ATTACKERS, 10);
        ListTag listTag = new ListTag();
        for (Tag tag : entity) {
            CompoundTag compoundTag = (CompoundTag)tag;
            long l = serverLevel.m_46467_() - compoundTag.m_128454_("At");
            if (!compoundTag.m_128403_("Id") || compoundTag.m_128342_("Id").equals(serverPlayer.m_20148_()) || l < 0L || l > 1200L) continue;
            listTag.add((Object)compoundTag.m_6426_());
        }
        Iterator iterator = new CompoundTag();
        iterator.m_128362_("Id", serverPlayer.m_20148_());
        iterator.m_128356_("At", serverLevel.m_46467_());
        listTag.add((Object)iterator);
        easterBossEntity.getPersistentData().m_128365_(ATTACKERS, (Tag)listTag);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent serverTickEvent) {
        MinecraftServer minecraftServer = ServerLifecycleHooks.getCurrentServer();
        if (serverTickEvent.phase != TickEvent.Phase.END || minecraftServer == null || minecraftServer.m_129783_().m_46467_() % 1200L != 0L) {
            return;
        }
        SparrowAppearanceData.get(minecraftServer).prune(ManorFightData.get(minecraftServer));
    }
}

