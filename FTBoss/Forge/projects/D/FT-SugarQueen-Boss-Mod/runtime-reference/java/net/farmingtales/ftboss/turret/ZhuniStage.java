/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.turret;

import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.turret.XuanlanTurrets;
import net.farmingtales.ftboss.turret.ZhuniEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ZhuniStage {
    private static final String STATE = "ftboss.zhuniStage";
    private static final String BIRD = "ftboss.zhuniId";

    private ZhuniStage() {
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void joined(EntityJoinLevelEvent entityJoinLevelEvent) {
        EasterBossEntity easterBossEntity;
        Entity entity;
        block6: {
            block5: {
                entity = entityJoinLevelEvent.getEntity();
                if (!(entity instanceof EasterBossEntity)) break block5;
                easterBossEntity = (EasterBossEntity)entity;
                if (!entityJoinLevelEvent.getLevel().m_5776_() && easterBossEntity.getVariant() == 6 && ManorBalanceHooks.mainBoss(easterBossEntity) && ManorFightMembers.fightId((Entity)easterBossEntity) != null && ManorFightMembers.ownerId((Entity)easterBossEntity) == null) break block6;
            }
            return;
        }
        entity = easterBossEntity.getPersistentData();
        if (!entity.m_128441_(STATE)) {
            entity.m_128405_(STATE, 1);
            entity.m_128379_("ftboss.zhuniOriginalAi", easterBossEntity.m_21525_());
            entity.m_128379_("ftboss.zhuniOriginalInvulnerable", easterBossEntity.m_20147_());
            entity.m_128379_("ftboss.zhuniOriginalInvisible", easterBossEntity.m_20145_());
            entity.m_128379_("ftboss.zhuniOriginalHidden", easterBossEntity.isManorIntroHidden());
        }
        if (entity.m_128451_(STATE) == 1) {
            ZhuniStage.hide(easterBossEntity);
        }
    }

    private static void hide(EasterBossEntity easterBossEntity) {
        easterBossEntity.m_21557_(true);
        easterBossEntity.m_20331_(true);
        easterBossEntity.m_6842_(true);
        easterBossEntity.setManorIntroHidden(true);
        ZhuniStage.syncBossBar(easterBossEntity);
    }

    public static boolean waiting(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getPersistentData().m_128451_(STATE) == 1;
    }

    public static void reveal(EasterBossEntity easterBossEntity, ZhuniEntity zhuniEntity) {
        if (!ZhuniStage.waiting(easterBossEntity) || !easterBossEntity.m_6084_()) {
            return;
        }
        CompoundTag compoundTag = easterBossEntity.getPersistentData();
        compoundTag.m_128405_(STATE, 2);
        easterBossEntity.m_21557_(compoundTag.m_128471_("ftboss.zhuniOriginalAi"));
        easterBossEntity.m_20331_(compoundTag.m_128471_("ftboss.zhuniOriginalInvulnerable"));
        easterBossEntity.m_6842_(compoundTag.m_128471_("ftboss.zhuniOriginalInvisible"));
        easterBossEntity.setManorIntroHidden(compoundTag.m_128471_("ftboss.zhuniOriginalHidden"));
        easterBossEntity.getBossBar().m_8321_(true);
        zhuniEntity.revealedAt(easterBossEntity.m_9236_().m_46467_());
        Level level = easterBossEntity.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            for (ServerPlayer serverPlayer : ManorBalanceHooks.players(easterBossEntity, serverLevel)) {
                serverPlayer.m_213846_((Component)Component.m_237113_((String)"\u00a7a\u7af9\u9e1f\u632f\u7fc5\uff01\u00a7b\u7384\u84dd\u767b\u573a\u3002"));
            }
        }
    }

    public static boolean hold(EasterBossEntity easterBossEntity) {
        Entity entity;
        ServerLevel serverLevel;
        Level level;
        block10: {
            block9: {
                level = easterBossEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break block9;
                serverLevel = (ServerLevel)level;
                if (easterBossEntity.getVariant() == 6 && ManorBalanceHooks.mainBoss(easterBossEntity) && easterBossEntity.m_6084_() && ManorFightMembers.ownerId((Entity)easterBossEntity) == null) break block10;
            }
            return false;
        }
        level = easterBossEntity.getPersistentData();
        int n = level.m_128451_(STATE);
        if (n == 0 && ManorFightMembers.fightId((Entity)easterBossEntity) != null && ManorFight.hasActive(ManorFightMembers.fightId((Entity)easterBossEntity)) && !HiddenBossIntro.isActing(easterBossEntity.m_20148_())) {
            level.m_128405_(STATE, 1);
            level.m_128379_("ftboss.zhuniOriginalAi", easterBossEntity.m_21525_());
            level.m_128379_("ftboss.zhuniOriginalInvulnerable", easterBossEntity.m_20147_());
            level.m_128379_("ftboss.zhuniOriginalInvisible", easterBossEntity.m_20145_());
            level.m_128379_("ftboss.zhuniOriginalHidden", easterBossEntity.isManorIntroHidden());
            n = 1;
        }
        if (n != 1 && n != 2) {
            return false;
        }
        UUID uUID = ManorFightMembers.fightId((Entity)easterBossEntity);
        if (uUID == null || !ManorFight.hasActive(uUID)) {
            return false;
        }
        Entity entity2 = entity = level.m_128403_(BIRD) ? serverLevel.m_8791_(level.m_128342_(BIRD)) : null;
        if (!(entity instanceof ZhuniEntity) || !easterBossEntity.m_20148_().equals(((ZhuniEntity)entity).ownerId())) {
            ZhuniEntity zhuniEntity = new ZhuniEntity(XuanlanTurrets.BIRD, (Level)serverLevel);
            zhuniEntity.setOwner(easterBossEntity);
            zhuniEntity.m_7678_(easterBossEntity.m_20185_(), easterBossEntity.m_20186_() + 0.65, easterBossEntity.m_20189_(), easterBossEntity.m_146908_(), 0.0f);
            zhuniEntity.revealedAt(serverLevel.m_46467_());
            ManorFightMembers.inherit((Entity)easterBossEntity, (Entity)zhuniEntity);
            if (serverLevel.m_7967_((Entity)zhuniEntity)) {
                level.m_128362_(BIRD, zhuniEntity.m_20148_());
            }
        }
        if (n != 1) {
            return false;
        }
        ZhuniStage.hide(easterBossEntity);
        return true;
    }

    public static void syncBossBar(EasterBossEntity easterBossEntity) {
        ServerLevel serverLevel;
        Level level;
        block5: {
            block4: {
                level = easterBossEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break block4;
                serverLevel = (ServerLevel)level;
                if (easterBossEntity.m_6084_() && !HiddenBossIntro.isActing(easterBossEntity.m_20148_())) break block5;
            }
            return;
        }
        level = easterBossEntity.getBossBar();
        List<ServerPlayer> list = ManorBalanceHooks.players(easterBossEntity, serverLevel);
        level.m_6456_((Component)Component.m_237113_((String)"\u7af9\u9e1f \u00b7 \u7384\u84dd"));
        float f = easterBossEntity.m_21233_();
        level.m_142711_(f > 0.0f ? Math.max(0.0f, Math.min(1.0f, easterBossEntity.m_21223_() / f)) : 0.0f);
        for (ServerPlayer serverPlayer : List.copyOf(level.m_8324_())) {
            if (list.contains(serverPlayer)) continue;
            level.m_6539_(serverPlayer);
        }
        for (ServerPlayer serverPlayer : list) {
            level.m_6543_(serverPlayer);
        }
        level.m_8321_(true);
    }
}

