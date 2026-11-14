/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossChallengePreference;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.boss.ai.LemonSupportFinale;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class NonManorFinaleDifficulty {
    public static final double ATTRIBUTE_MULTIPLIER = 2.0;
    public static final int CAST_SPEED_MULTIPLIER = 2;
    private static final String PREPARED = "ftboss:regular_finale_prepared";
    private static final String FINALE = "ftboss:regular_finale";
    private static final String BASE_PREFIX = "ftboss:regular_finale_base/";
    private static final String SCALING_BASE_HP = "ftboss_base_hp";

    private NonManorFinaleDifficulty() {
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onJoin(EntityJoinLevelEvent entityJoinLevelEvent) {
        LivingEntity boss;
        ServerLevel level;
        EntityJoinLevelEvent event;
        block7: {
            block6: {
                Level level2;
                if (LemonSupportFinale.onJoin(entityJoinLevelEvent)) {
                    return;
                }
                if (event.getLevel().f_46443_ || !((level2 = event.getLevel()) instanceof ServerLevel)) break block6;
                level = (ServerLevel)level2;
                level2 = event.getEntity();
                if (level2 instanceof LivingEntity && NonManorFinaleDifficulty.isRegularBoss(boss = (LivingEntity)level2)) break block7;
            }
            return;
        }
        if (event.loadedFromDisk()) {
            NonManorFinaleDifficulty.ensureFinaleMaximumHealth(boss, false);
            return;
        }
        ServerPlayer summoner = null;
        double best = Double.MAX_VALUE;
        for (ServerPlayer candidate : level.m_6443_(ServerPlayer.class, boss.m_20191_().m_82400_(16.0), player -> player.m_6084_() && !player.m_5833_())) {
            double distance = candidate.m_20280_((Entity)boss);
            if (!(distance < best)) continue;
            best = distance;
            summoner = candidate;
        }
        NonManorFinaleDifficulty.prepare(boss, summoner != null && BossChallengePreference.selected(summoner) == ManorDifficulty.IMPOSSIBLE);
    }

    public static void prepare(LivingEntity boss, boolean finale) {
        if (boss == null || boss.m_9236_().f_46443_ || !NonManorFinaleDifficulty.isRegularBoss(boss)) {
            return;
        }
        CompoundTag data = boss.getPersistentData();
        if (data.m_128471_(PREPARED)) {
            return;
        }
        ArrayList<AttributeBase> bases = new ArrayList<AttributeBase>();
        AttributeInstance maximumHealth = boss.m_21051_(Attributes.f_22276_);
        NonManorFinaleDifficulty.rememberBase(data, bases, maximumHealth);
        for (AttributeInstance instance : boss.m_21204_().m_22170_()) {
            if (instance == maximumHealth) continue;
            NonManorFinaleDifficulty.rememberBase(data, bases, instance);
        }
        data.m_128379_(PREPARED, true);
        data.m_128379_(FINALE, finale);
        if (boss instanceof TeaTreeBossEntity) {
            TeaTreeBossEntity saya = (TeaTreeBossEntity)boss;
            saya.setFinaleChallenge(finale);
        }
        if (finale) {
            for (AttributeBase entry : bases) {
                entry.instance().m_22100_(entry.instance().m_22099_().m_6740_(entry.base() * 2.0));
            }
            boss.m_21153_(boss.m_21233_());
            FTBossMod.LOGGER.info("[FTBoss][\u7ec8\u7ed3\u96be\u5ea6] {} \u5c5e\u6027\u00d72\u3001\u65bd\u6cd5\u901f\u5ea6\u00d72", (Object)boss.m_7755_().getString());
        }
    }

    private static void rememberBase(CompoundTag data, List<AttributeBase> bases, AttributeInstance instance) {
        if (instance == null) {
            return;
        }
        ResourceLocation id = ForgeRegistries.ATTRIBUTES.getKey((Object)instance.m_22099_());
        if (id == null) {
            return;
        }
        String key = BASE_PREFIX + id;
        if (!data.m_128441_(key)) {
            data.m_128347_(key, instance.m_22115_());
        }
        bases.add(new AttributeBase(instance, data.m_128459_(key)));
    }

    private static void ensureFinaleMaximumHealth(LivingEntity boss, boolean fullHeal) {
        float ratio;
        String key;
        if (!NonManorFinaleDifficulty.isFinale(boss)) {
            return;
        }
        AttributeInstance maximumHealth = boss.m_21051_(Attributes.f_22276_);
        if (maximumHealth == null) {
            return;
        }
        ResourceLocation id = ForgeRegistries.ATTRIBUTES.getKey((Object)maximumHealth.m_22099_());
        if (id == null) {
            return;
        }
        CompoundTag data = boss.getPersistentData();
        if (!data.m_128441_(key = BASE_PREFIX + id)) {
            double base = data.m_128441_(SCALING_BASE_HP) ? data.m_128459_(SCALING_BASE_HP) : maximumHealth.m_22115_();
            data.m_128347_(key, base);
        }
        double desired = maximumHealth.m_22099_().m_6740_(data.m_128459_(key) * 2.0);
        float oldMaximum = boss.m_21233_();
        float f = ratio = oldMaximum > 0.0f ? boss.m_21223_() / oldMaximum : 1.0f;
        if (Math.abs(maximumHealth.m_22115_() - desired) > 0.01) {
            maximumHealth.m_22100_(desired);
            if (fullHeal) {
                boss.m_21153_(boss.m_21233_());
            } else {
                boss.m_21153_(Math.max(1.0f, Math.min(boss.m_21233_(), ratio * boss.m_21233_())));
            }
        }
    }

    public static boolean isFinale(LivingEntity boss) {
        TeaTreeBossEntity saya;
        return boss != null && (boss.getPersistentData().m_128471_(FINALE) || boss instanceof TeaTreeBossEntity && (saya = (TeaTreeBossEntity)boss).isFinaleChallenge());
    }

    public static int acceleratedTicks(LivingEntity boss, int normalTicks) {
        if (normalTicks <= 0) {
            return normalTicks;
        }
        return NonManorFinaleDifficulty.isFinale(boss) ? Math.max(1, (normalTicks + 2 - 1) / 2) : normalTicks;
    }

    public static boolean isRegularBoss(LivingEntity boss) {
        if (boss instanceof EasterBossEntity) {
            return false;
        }
        if (boss instanceof RatBossEntity) {
            RatBossEntity rat = (RatBossEntity)boss;
            return !rat.isMinion();
        }
        if (boss instanceof TeaTreeBossEntity) {
            TeaTreeBossEntity saya = (TeaTreeBossEntity)boss;
            return !saya.isTeaMinion();
        }
        return boss instanceof SugarQueenEntity || boss instanceof MeatMountainEntity || boss instanceof WineBarrelBossEntity || boss instanceof DefectCoreEntity || boss instanceof CompostBossEntity || boss instanceof AppleBossEntity || boss instanceof TideFishBossEntity || boss instanceof BengerEntity;
    }

    private record AttributeBase(AttributeInstance instance, double base) {
    }
}

