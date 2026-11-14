/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraftforge.event.level.ExplosionEvent$Detonate
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class SafeExplosion {
    private SafeExplosion() {
    }

    @SubscribeEvent
    public static void onDetonate(ExplosionEvent.Detonate e) {
        try {
            Entity src = e.getExplosion().m_253049_();
            if (src == null) {
                src = e.getExplosion().m_252906_();
            }
            if (src == null) {
                return;
            }
            String pkg = src.getClass().getPackageName();
            if (pkg == null || !pkg.startsWith("net.farmingtales.ftboss")) {
                return;
            }
            ArrayList<ItemEntity> items = new ArrayList<ItemEntity>();
            for (Entity en : e.getAffectedEntities()) {
                if (!(en instanceof ItemEntity)) continue;
                ItemEntity ie = (ItemEntity)en;
                items.add(ie);
            }
            if (items.isEmpty()) {
                return;
            }
            e.getAffectedEntities().removeAll(items);
            FTBossMod.LOGGER.info("[FTBoss][\u7206\u70b8] \u4fdd\u4f4f\u4e86 {} \u4ef6\u6389\u843d\u7269({} \u7684\u7206\u70b8)", (Object)items.size(), (Object)src.m_7755_().getString());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

