/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public final class EnderVortex {
    public static final ResourceLocation VOID_VORTEX = new ResourceLocation("cataclysm", "void_vortex");
    private static Boolean available = null;

    private EnderVortex() {
    }

    public static boolean available() {
        if (available == null) {
            available = ForgeRegistries.ENTITY_TYPES.getValue(VOID_VORTEX) != null;
        }
        return available;
    }

    public static Entity spawn(ServerLevel sl, LivingEntity owner, double x, double y, double z, int ticks) {
        EntityType type = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(VOID_VORTEX);
        if (type == null) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u6ca1\u6709\u627e\u5230 {} \u2014\u2014 \u865a\u7a7a\u6f29\u6da1\u53ea\u653e\u81ea\u7ed8\u7c92\u5b50(\u5438\u5f15\u529b\u7167\u5e38\u751f\u6548)", (Object)VOID_VORTEX);
            return null;
        }
        try {
            Entity e = type.m_20615_((Level)sl);
            if (e == null) {
                return null;
            }
            e.m_7678_(x, y, z, owner != null ? owner.m_146908_() : 0.0f, 0.0f);
            boolean lifespanOk = false;
            boolean ownerOk = false;
            try {
                e.getClass().getMethod("setLifespan", Integer.TYPE).invoke(e, ticks);
                lifespanOk = true;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (owner != null) {
                try {
                    e.getClass().getMethod("setOwner", LivingEntity.class).invoke(e, owner);
                    ownerOk = true;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            sl.m_7967_(e);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u672b\u5f71\u5b88\u536b\u7684\u865a\u7a7a\u6f29\u6da1\u5df2\u53ec\u5524 @({}, {}, {}) \u5bff\u547d {} tick (setLifespan={}, setOwner={})", new Object[]{String.format("%.1f", x), String.format("%.1f", y), String.format("%.1f", z), ticks, lifespanOk, ownerOk});
            return e;
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u865a\u7a7a\u6f29\u6da1\u53ec\u5524\u5931\u8d25: {}", (Object)t.toString());
            return null;
        }
    }
}

