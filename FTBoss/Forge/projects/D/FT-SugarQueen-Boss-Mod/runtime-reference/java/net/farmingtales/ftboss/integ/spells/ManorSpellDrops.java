/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.event.entity.living.LivingDropsEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.integ.spells;

import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ManorSpellDrops {
    private static final String REWARDED = "ftboss:manor_scroll_pair_rewarded";

    private ManorSpellDrops() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void drops(LivingDropsEvent event) {
    }
}

