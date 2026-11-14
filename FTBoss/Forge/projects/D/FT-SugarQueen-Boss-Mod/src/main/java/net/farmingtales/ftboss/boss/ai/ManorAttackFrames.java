package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorMercy;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ManorAttackFrames {
    private ManorAttackFrames() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttack(LivingAttackEvent event) {
        if (event.isCanceled() || !Float.isFinite(event.getAmount()) || event.getAmount() <= 0.0f
                || !(event.getEntity() instanceof Player player) || player.level().isClientSide
                || !player.isAlive() || player.isCreative() || player.isSpectator()
                || player.isInvulnerableTo(event.getSource())) return;
        if (isManorAttack(event.getSource())) player.invulnerableTime = 0;
    }

    public static boolean isManorAttack(DamageSource source) {
        Entity owner = source.getEntity();
        if (owner == null) owner = source.getDirectEntity();
        if (owner == null) owner = ManorMercy.damageOwner();
        return isManorOwned(owner);
    }

    private static boolean isManorOwned(Entity entity) {
        for (int depth = 0; entity != null && depth < 8; depth++) {
            if (entity instanceof EasterBossEntity) return true;
            Entity parent = ManorFightMembers.owner(entity);
            if (parent == null && entity.level() instanceof ServerLevel level) {
                UUID owner = ManorFightMembers.ownerId(entity);
                if (owner == null) owner = MinionBlessing.ownerId(entity);
                if (owner != null) parent = level.getEntity(owner);
            }
            if (parent == null || parent == entity) return false;
            entity = parent;
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLightning(EntityStruckByLightningEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof Player player)
                || !(player.level() instanceof ServerLevel level) || !isManorOwned(event.getLightning())) return;
        event.setCanceled(true);
        ManorMercy.withOwner(event.getLightning(), () -> {
            player.thunderHit(level, event.getLightning());
            return true;
        });
    }
}
