package net.farmingtales.ftboss.boss.ai;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.util.BossKnockback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class BossKnockbackEvents {
    private record Hit(long tick, double multiplier) {}
    private static final Map<ServerPlayer, Hit> HITS = new WeakHashMap<>();

    private BossKnockbackEvents() {}

    public static boolean isBossSource(Entity source) {
        for (int depth = 0; source != null && depth < 8; depth++) {
            if (source instanceof Player) return false;
            Entity owner = ManorFightMembers.owner(source);
            if (owner != null && owner != source) {
                source = owner;
                continue;
            }
            ResourceLocation identifier = ForgeRegistries.ENTITY_TYPES.getKey(source.getType());
            return identifier != null && identifier.getNamespace().equals(FTBossMod.MOD_ID);
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) HITS.remove(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        HITS.remove(player);
        if (event.isCanceled() || event.getAmount() <= 0.0F) return;
        Entity owner = event.getSource().getEntity();
        if (owner == null) owner = event.getSource().getDirectEntity();
        if (isBossSource(owner)) HITS.put(player, new Hit(player.level().getGameTime(), BossKnockback.sourceMultiplier(owner)));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onKnockback(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || BossKnockback.isExplicit(player)) return;
        Hit hit = HITS.get(player);
        if (hit != null && hit.tick() == player.level().getGameTime()) {
            event.setStrength((float) (event.getStrength() * BossKnockback.TO_PLAYER_MULT * hit.multiplier()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) HITS.clear();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        HITS.clear();
    }
}
