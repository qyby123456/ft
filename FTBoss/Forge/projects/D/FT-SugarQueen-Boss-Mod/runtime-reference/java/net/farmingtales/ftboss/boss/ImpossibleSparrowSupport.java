/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.saveddata.SavedData
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.EntityLeaveLevelEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss;

import java.util.UUID;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightData;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.SparrowSupport;
import net.farmingtales.ftboss.boss.ai.ManorHealthBalance;
import net.farmingtales.ftboss.boss.ai.XuanlanEncounterLimits;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ImpossibleSparrowSupport
extends SavedData {
    private static final String HEALTH_READY = "ftboss_impossible_sparrow_health";
    private UUID sparrow;
    private UUID fight;
    private ResourceKey<Level> dimension;
    private BlockPos position = BlockPos.f_121853_;

    public static ImpossibleSparrowSupport get(MinecraftServer server) {
        return (ImpossibleSparrowSupport)server.m_129783_().m_8895_().m_164861_(ImpossibleSparrowSupport::load, ImpossibleSparrowSupport::new, "ftboss_impossible_sparrow");
    }

    public static ImpossibleSparrowSupport load(CompoundTag tag) {
        ResourceLocation location;
        ImpossibleSparrowSupport data = new ImpossibleSparrowSupport();
        if (tag.m_128403_("Sparrow") && tag.m_128403_("Fight") && (location = ResourceLocation.m_135820_((String)tag.m_128461_("Dimension"))) != null) {
            data.sparrow = tag.m_128342_("Sparrow");
            data.fight = tag.m_128342_("Fight");
            data.dimension = ResourceKey.m_135785_((ResourceKey)Registries.f_256858_, (ResourceLocation)location);
            data.position = BlockPos.m_122022_((long)tag.m_128454_("Position"));
        }
        return data;
    }

    public CompoundTag m_7176_(CompoundTag tag) {
        if (this.sparrow != null) {
            tag.m_128362_("Sparrow", this.sparrow);
            tag.m_128362_("Fight", this.fight);
            tag.m_128359_("Dimension", this.dimension.m_135782_().toString());
            tag.m_128356_("Position", this.position.m_121878_());
        }
        return tag;
    }

    public static float health(EasterBossEntity easterBossEntity) {
        return ManorHealthBalance.supportHealth(easterBossEntity);
    }

    private void clear() {
        this.sparrow = null;
        this.fight = null;
        this.dimension = null;
        this.m_77762_();
    }

    public static void finish(MinecraftServer server, UUID fight) {
        ImpossibleSparrowSupport data = ImpossibleSparrowSupport.get(server);
        if (fight != null && fight.equals(data.fight)) {
            data.clear();
        }
    }

    public static void failedSpawn(EasterBossEntity bird) {
        Level level = bird.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        ImpossibleSparrowSupport data = ImpossibleSparrowSupport.get(level2.m_7654_());
        if (bird.m_20148_().equals(data.sparrow)) {
            data.clear();
        }
    }

    public static boolean reserved(MinecraftServer server) {
        ImpossibleSparrowSupport data = ImpossibleSparrowSupport.get(server);
        if (data.sparrow != null && ManorFightData.get(server).retired(data.fight)) {
            data.clear();
        }
        return data.sparrow != null;
    }

    private void remember(EasterBossEntity bird) {
        this.sparrow = bird.m_20148_();
        this.fight = ManorFightMembers.fightId((Entity)bird);
        this.dimension = bird.m_9236_().m_46472_();
        this.position = bird.m_20183_();
        this.m_77762_();
    }

    private static void prepare(EasterBossEntity easterBossEntity) {
        ManorHealthBalance.prepareImpossible(easterBossEntity);
    }

    /*
     * WARNING - void declaration
     */
    public static void ensure(ServerLevel serverLevel, EasterBossEntity easterBossEntity) {
        ServerLevel level;
        void boss;
        if (XuanlanEncounterLimits.blocksSupport((Entity)easterBossEntity)) {
            return;
        }
        UUID fight = ManorFightMembers.fightId((Entity)boss);
        if (!boss.isImpossibleChallenge() || !boss.m_6084_() || boss.m_213877_() || boss.isMini() || boss.getVariant() == 9 || fight == null || !boss.m_20148_().equals(ManorFight.currentBoss(fight)) || HiddenBossIntro.isActing(boss.m_20148_()) || ManorFightData.get(level.m_7654_()).retired(fight)) {
            return;
        }
        ImpossibleSparrowSupport data = ImpossibleSparrowSupport.get(level.m_7654_());
        if (ImpossibleSparrowSupport.reserved(level.m_7654_())) {
            EasterBossEntity bird;
            ServerLevel storedLevel = level.m_7654_().m_129880_(data.dimension);
            if (storedLevel == null) {
                return;
            }
            Entity stored = storedLevel.m_8791_(data.sparrow);
            if (stored instanceof EasterBossEntity && (bird = (EasterBossEntity)stored).m_6084_() && !bird.m_213877_()) {
                ImpossibleSparrowSupport.prepare(bird);
                data.remember(bird);
                return;
            }
            if (stored == null) {
                storedLevel.m_46865_(data.position);
                return;
            }
            data.clear();
        }
        for (ServerLevel candidateLevel : level.m_7654_().m_129785_()) {
            for (Entity entity : candidateLevel.m_8583_()) {
                EasterBossEntity bird;
                if (!(entity instanceof EasterBossEntity) || (bird = (EasterBossEntity)entity).getVariant() != 9 || !bird.m_6084_() || bird.m_213877_()) continue;
                if (fight.equals(ManorFightMembers.fightId((Entity)bird))) {
                    ImpossibleSparrowSupport.prepare(bird);
                    data.remember(bird);
                }
                return;
            }
        }
        SparrowSupport.spawnSparrow(level, (LivingEntity)boss);
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void onJoin(EntityJoinLevelEvent event) {
        EasterBossEntity bird;
        ServerLevel level;
        block8: {
            block7: {
                Level level2 = event.getLevel();
                if (!(level2 instanceof ServerLevel)) break block7;
                level = (ServerLevel)level2;
                level2 = event.getEntity();
                if (level2 instanceof EasterBossEntity && (bird = (EasterBossEntity)level2).getVariant() == 9 && !event.isCanceled()) break block8;
            }
            return;
        }
        ImpossibleSparrowSupport data = ImpossibleSparrowSupport.get(level.m_7654_());
        if (ImpossibleSparrowSupport.reserved(level.m_7654_()) && !bird.m_20148_().equals(data.sparrow)) {
            event.setCanceled(true);
            return;
        }
        UUID fight = ManorFightMembers.fightId((Entity)bird);
        if (!bird.isImpossibleChallenge() || fight == null || ManorFightData.get(level.m_7654_()).retired(fight)) {
            return;
        }
        for (ServerLevel candidateLevel : level.m_7654_().m_129785_()) {
            for (Entity entity : candidateLevel.m_8583_()) {
                EasterBossEntity existing;
                if (!(entity instanceof EasterBossEntity) || (existing = (EasterBossEntity)entity).getVariant() != 9 || !existing.m_6084_() || existing.m_213877_() || existing.m_20148_().equals(bird.m_20148_())) continue;
                event.setCanceled(true);
                return;
            }
        }
        ImpossibleSparrowSupport.prepare(bird);
        data.remember(bird);
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        EasterBossEntity bird;
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        Entity entity = event.getEntity();
        if (!(entity instanceof EasterBossEntity) || (bird = (EasterBossEntity)entity).getVariant() != 9) {
            return;
        }
        ImpossibleSparrowSupport data = ImpossibleSparrowSupport.get(level2.m_7654_());
        if (!entity.m_20148_().equals(data.sparrow)) {
            return;
        }
        if (entity.m_146911_() == Entity.RemovalReason.UNLOADED_TO_CHUNK || entity.m_146911_() == Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            data.remember(bird);
        } else {
            data.clear();
        }
    }

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        ServerLevel level;
        EasterBossEntity bird;
        block5: {
            block4: {
                LivingEntity livingEntity = event.getEntity();
                if (!(livingEntity instanceof EasterBossEntity) || !((livingEntity = (bird = (EasterBossEntity)livingEntity).m_9236_()) instanceof ServerLevel)) break block4;
                level = (ServerLevel)livingEntity;
                if (bird.isImpossibleChallenge() && bird.getVariant() == 9 && bird.f_19797_ % 20 == 0 && bird.m_6084_()) break block5;
            }
            return;
        }
        ImpossibleSparrowSupport data = ImpossibleSparrowSupport.get(level.m_7654_());
        if (bird.m_20148_().equals(data.sparrow)) {
            data.remember(bird);
        }
    }
}

