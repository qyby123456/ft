/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorSupportRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ManorHealthBalance {
    private static final String SUPPORT = "ftboss_sparrow_support_role";
    private static final String APPLIED = "ftboss_manor_health_balance";
    private static final String SCALING = "ftboss_base_hp";
    private static final String IMPOSSIBLE_READY = "ftboss_impossible_sparrow_health";

    private ManorHealthBalance() {
    }

    public static float supportHealth(EasterBossEntity easterBossEntity) {
        easterBossEntity.getPersistentData().m_128379_(SUPPORT, true);
        ManorHealthBalance.normalize((LivingEntity)easterBossEntity);
        return 500.0f;
    }

    public static boolean support(EasterBossEntity easterBossEntity) {
        CompoundTag compoundTag = easterBossEntity.getPersistentData();
        AttributeInstance attributeInstance = easterBossEntity.m_21051_(Attributes.f_22276_);
        return easterBossEntity.getVariant() == 9 && (compoundTag.m_128471_(SUPPORT) || compoundTag.m_128471_(IMPOSSIBLE_READY) || ManorFightMembers.ownerId((Entity)easterBossEntity) != null || attributeInstance != null && attributeInstance.m_22115_() <= 250.0);
    }

    public static void prepareImpossible(EasterBossEntity easterBossEntity) {
        easterBossEntity.setChallengeDifficulty(ManorDifficulty.IMPOSSIBLE);
        ManorHealthBalance.supportHealth(easterBossEntity);
        ManorHealthBalance.normalize((LivingEntity)easterBossEntity);
        easterBossEntity.getPersistentData().m_128379_(IMPOSSIBLE_READY, true);
    }

    public static void normalize(LivingEntity livingEntity) {
        double d;
        EasterBossEntity easterBossEntity;
        block8: {
            block7: {
                if (!(livingEntity instanceof EasterBossEntity)) break block7;
                easterBossEntity = (EasterBossEntity)livingEntity;
                if (!livingEntity.m_9236_().f_46443_ && livingEntity.m_6084_() && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && (EasterBossEntity.isManorVariant(easterBossEntity.getVariant()) || easterBossEntity.getVariant() == 9)) break block8;
            }
            return;
        }
        AttributeInstance attributeInstance = easterBossEntity.m_21051_(Attributes.f_22276_);
        if (attributeInstance == null) {
            return;
        }
        CompoundTag compoundTag = easterBossEntity.getPersistentData();
        boolean bl = ManorHealthBalance.support(easterBossEntity);
        if (bl) {
            compoundTag.m_128379_(SUPPORT, true);
        }
        double d2 = d = bl ? 500.0 : 1500.0;
        if (compoundTag.m_128459_(APPLIED) == d) {
            return;
        }
        double d3 = compoundTag.m_128441_(SCALING) ? compoundTag.m_128459_(SCALING) : attributeInstance.m_22115_();
        float f = easterBossEntity.m_21223_();
        float f2 = easterBossEntity.m_21233_();
        attributeInstance.m_22100_(ManorSupportRules.scaledBase(attributeInstance.m_22115_(), d3, d));
        if (compoundTag.m_128441_(SCALING)) {
            compoundTag.m_128347_(SCALING, d);
        }
        easterBossEntity.m_21153_(ManorSupportRules.health(f, f2, easterBossEntity.m_21233_()));
        compoundTag.m_128347_(APPLIED, d);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onJoin(EntityJoinLevelEvent entityJoinLevelEvent) {
        Entity entity;
        if (!entityJoinLevelEvent.isCanceled() && (entity = entityJoinLevelEvent.getEntity()) instanceof EasterBossEntity) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)entity;
            ManorHealthBalance.normalize((LivingEntity)easterBossEntity);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onTick(LivingEvent.LivingTickEvent livingTickEvent) {
        LivingEntity livingEntity = livingTickEvent.getEntity();
        if (livingEntity instanceof EasterBossEntity) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)livingEntity;
            ManorHealthBalance.normalize((LivingEntity)easterBossEntity);
        }
    }
}

