/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.living.LivingKnockBackEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.util.BossKnockback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss")
public final class BossKnockbackEvents {
    private static final Map<ServerPlayer, Hit> HITS = new WeakHashMap<ServerPlayer, Hit>();

    private BossKnockbackEvents() {
    }

    public static boolean isBossSource(Entity source) {
        for (int depth = 0; source != null && depth < 8; ++depth) {
            if (source instanceof Player) {
                return false;
            }
            Entity owner = ManorFightMembers.owner(source);
            if (owner == null || owner == source) {
                ResourceLocation identifier = ForgeRegistries.ENTITY_TYPES.getKey((Object)source.m_6095_());
                return identifier != null && identifier.m_135827_().equals("ftboss");
            }
            source = owner;
        }
        return false;
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST, receiveCanceled=true)
    public static void onAttack(LivingAttackEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)livingEntity;
            HITS.remove(player);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST, receiveCanceled=true)
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        HITS.remove(player);
        if (event.isCanceled() || event.getAmount() <= 0.0f) {
            return;
        }
        Entity owner = event.getSource().m_7639_();
        if (owner == null) {
            owner = event.getSource().m_7640_();
        }
        if (BossKnockbackEvents.isBossSource(owner)) {
            HITS.put(player, new Hit(player.m_9236_().m_46467_(), BossKnockback.sourceMultiplier(owner)));
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onKnockback(LivingKnockBackEvent event) {
        ServerPlayer player;
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer) || BossKnockback.isExplicit((LivingEntity)(player = (ServerPlayer)livingEntity))) {
            return;
        }
        Hit hit = HITS.get(player);
        if (hit != null && hit.tick() == player.m_9236_().m_46467_()) {
            event.setStrength((float)((double)event.getStrength() * 0.1 * hit.multiplier()));
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            HITS.clear();
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        HITS.clear();
    }

    private record Hit(long tick, double multiplier) {
    }
}

