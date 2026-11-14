/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.SilverKeyItem;
import net.farmingtales.ftboss.item.ThornCrownItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ThornCrownEffects {
    private static final String LAST_REFLECT_TICK = "ftboss_thorn_crown_last_reflect";

    private ThornCrownEffects() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent event) {
        long elapsed;
        LivingEntity wearer = event.getEntity();
        if (wearer.m_9236_().f_46443_ || event.getAmount() <= 0.0f || ThornCrownItem.equippedPieces(wearer) <= 0) {
            return;
        }
        if (event.getSource().m_276093_(DamageTypes.f_268440_)) {
            return;
        }
        if (event.getSource().m_269533_(DamageTypeTags.f_268415_) || event.getSource().m_269533_(DamageTypeTags.f_268524_)) {
            return;
        }
        Entity entity = event.getSource().m_7640_();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity attacker = (LivingEntity)entity;
        if (attacker == wearer || !attacker.m_6084_() || wearer.m_7307_((Entity)attacker)) {
            return;
        }
        if (wearer instanceof Player) {
            Player target;
            Player player = (Player)wearer;
            if (attacker instanceof Player && !player.m_7099_(target = (Player)attacker)) {
                return;
            }
        }
        long currentTick = wearer.m_9236_().m_46467_();
        CompoundTag data = wearer.getPersistentData();
        if (data.m_128441_(LAST_REFLECT_TICK) && (elapsed = currentTick - data.m_128454_(LAST_REFLECT_TICK)) >= 0L && elapsed < 10L) {
            return;
        }
        data.m_128356_(LAST_REFLECT_TICK, currentTick);
        boolean doubled = SilverKeyItem.isEquipped(wearer);
        attacker.m_6469_(wearer.m_269291_().m_269374_((Entity)wearer), ThornCrownItem.reflectedDamage(event.getAmount(), doubled));
    }
}

