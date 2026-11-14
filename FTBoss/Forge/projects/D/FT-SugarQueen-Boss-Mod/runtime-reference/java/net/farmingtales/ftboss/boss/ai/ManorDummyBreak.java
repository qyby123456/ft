/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import java.lang.reflect.InvocationTargetException;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ManorDummyBreak {
    private ManorDummyBreak() {
    }

    private static boolean manorAttacker(Entity entity) {
        for (int i = 0; entity != null && i < 8; ++i) {
            if (entity instanceof Player) {
                return false;
            }
            if (entity instanceof EasterBossEntity) {
                return true;
            }
            Entity entity2 = ManorFightMembers.owner(entity);
            if (entity2 == null || entity2 == entity) {
                return false;
            }
            entity = entity2;
        }
        return false;
    }

    private static boolean manorDamage(DamageSource damageSource) {
        if (damageSource == null) {
            return false;
        }
        Entity entity = damageSource.m_7639_();
        return ManorDummyBreak.manorAttacker(entity == null ? damageSource.m_7640_() : entity);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onAttack(LivingAttackEvent livingAttackEvent) {
        boolean bl;
        LivingEntity livingEntity = livingAttackEvent.getEntity();
        if (livingEntity.m_9236_().f_46443_ || !livingEntity.m_6084_() || livingEntity.m_213877_() || !(livingAttackEvent.getAmount() > 0.0f)) {
            return;
        }
        ResourceLocation resourceLocation = ForgeRegistries.ENTITY_TYPES.getKey((Object)livingEntity.m_6095_());
        if (resourceLocation == null) {
            return;
        }
        boolean bl2 = resourceLocation.m_135827_().equals("dummmmmmy") && resourceLocation.m_135815_().equals("target_dummy");
        boolean bl3 = bl = resourceLocation.m_135827_().equals("armamentarium") && resourceLocation.m_135815_().equals("test_dummy");
        if (!bl2 && !bl || !ManorDummyBreak.manorDamage(livingAttackEvent.getSource())) {
            return;
        }
        try {
            if (bl2) {
                livingEntity.getClass().getMethod("dismantle", Boolean.TYPE).invoke(livingEntity, true);
            } else {
                livingEntity.m_6074_();
            }
            if (livingEntity.m_213877_() || !livingEntity.m_6084_()) {
                livingAttackEvent.setCanceled(true);
            }
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            Throwable throwable;
            if (reflectiveOperationException instanceof InvocationTargetException) {
                InvocationTargetException invocationTargetException = (InvocationTargetException)reflectiveOperationException;
                throwable = invocationTargetException.getCause();
            } else {
                throwable = reflectiveOperationException;
            }
            ReflectiveOperationException reflectiveOperationException2 = throwable;
            FTBossMod.LOGGER.warn("[FTBoss] Failed to dismantle training dummy {}", (Object)livingEntity.m_20148_(), (Object)reflectiveOperationException2);
        }
    }
}

