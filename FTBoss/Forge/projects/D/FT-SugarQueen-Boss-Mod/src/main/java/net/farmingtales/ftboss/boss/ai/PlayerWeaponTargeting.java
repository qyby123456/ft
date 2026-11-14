package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class PlayerWeaponTargeting {
    private PlayerWeaponTargeting() {}

    public static boolean isValidTarget(ServerPlayer player, LivingEntity target) {
        return target != player && target.isAlive() && !target.isSpectator() && !target.isInvulnerable()
                && !player.isAlliedTo(target) && !target.isAlliedTo(player);
    }

    public static boolean canHarmPlayer(ServerPlayer player, LivingEntity target) {
        return !(target instanceof Player other)
                || (!other.isCreative() && player.server.isPvpAllowed() && player.canHarmPlayer(other));
    }

    public static boolean canTarget(ServerPlayer player, LivingEntity target) {
        if (!isValidTarget(player, target) || target instanceof ArmorStand) return false;
        if (target instanceof OwnableEntity owned && owned.getOwnerUUID() != null) return false;
        return canHarmPlayer(player, target);
    }

    public static boolean isHostileTo(ServerPlayer player, LivingEntity target) {
        return target instanceof Enemy || target instanceof EasterBossEntity
                || target instanceof Mob mob && (mob.getTarget() == player || player.getLastHurtMob() == target);
    }

    public static boolean hasClearPath(ServerPlayer player, Vec3 from, Vec3 to) {
        return player.serverLevel().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS;
    }
}
