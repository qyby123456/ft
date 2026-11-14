package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.entity.PlayerMoonChakramEntity;
import net.farmingtales.ftboss.util.XuanlanRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class XuanlanCombat {
    private static final String MARK = "ftboss:XuanlanMark";
    private static final String ENERGY = "ftboss:XuanlanEnergy";

    private XuanlanCombat() {}

    public static boolean canHit(Player owner, Entity target) {
        if (!(target instanceof LivingEntity living) || !living.isAlive() || living.isSpectator()
                || living == owner || living instanceof ArmorStand || living.isInvulnerable()
                || owner.isAlliedTo(living) || living.isAlliedTo(owner)) return false;
        if (living instanceof Player player && (player.isCreative() || !owner.canHarmPlayer(player)
                || owner.getServer() == null || !owner.getServer().isPvpAllowed())) return false;
        return !(living instanceof TamableAnimal pet && owner.getUUID().equals(pet.getOwnerUUID()));
    }

    public static Vec3 clip(ServerLevel level, Entity actor, Vec3 from, Vec3 to) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, actor)).getLocation();
    }

    public static void fire(ServerPlayer player, int chargeTicks) {
        ServerLevel level = player.serverLevel();
        Vec3 from = player.getEyePosition();
        Vec3 endpoint = clip(level, player, from, from.add(player.getLookAngle().scale(XuanlanRules.RANGE)));
        LivingEntity target = null;
        double nearest = from.distanceToSqr(endpoint);
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(from, endpoint).inflate(0.3), entity -> canHit(player, entity))) {
            Vec3 intersection = candidate.getBoundingBox().clip(from, endpoint).orElse(null);
            if (candidate.getBoundingBox().contains(from)) intersection = from;
            if (intersection == null || from.distanceToSqr(intersection) > nearest) continue;
            nearest = from.distanceToSqr(intersection);
            target = candidate;
        }
        if (target != null) endpoint = from.add(player.getLookAngle().scale(Math.sqrt(nearest)));
        int energy = readEnergy(player);
        player.getPersistentData().remove(ENERGY);
        if (target != null && target.hurt(level.damageSources().mobProjectile(player, player),
                XuanlanRules.shotDamage(chargeTicks, energy)) && target.isAlive() && chargeTicks >= XuanlanRules.CHARGE_TICKS) {
            CompoundTag mark = new CompoundTag();
            mark.putUUID("Owner", player.getUUID());
            mark.putLong("Until", level.getGameTime() + XuanlanRules.MARK_TICKS);
            mark.putString("Dimension", level.dimension().location().toString());
            target.getPersistentData().put(MARK, mark);
            XuanlanFxPacket.send(level, XuanlanFxPacket.MARK, target.getId(), target.position(), target.position());
        }
        XuanlanFxPacket.send(level, energy > 0 ? XuanlanFxPacket.EMPOWERED_BEAM : XuanlanFxPacket.BEAM,
                player.getId(), from.add(0, -0.12, 0), endpoint);
    }

    public static boolean markedBy(LivingEntity target, Player owner) {
        CompoundTag mark = target.getPersistentData().getCompound(MARK);
        return mark.hasUUID("Owner") && owner.getUUID().equals(mark.getUUID("Owner"))
                && mark.getString("Dimension").equals(owner.level().dimension().location().toString())
                && XuanlanRules.active(owner.level().getGameTime(), mark.getLong("Until"), XuanlanRules.MARK_TICKS);
    }

    public static void consumeMark(LivingEntity target, ServerPlayer owner) {
        target.getPersistentData().remove(MARK);
        CompoundTag energy = new CompoundTag();
        energy.putLong("Until", owner.level().getGameTime() + XuanlanRules.ENERGY_TICKS);
        energy.putString("Dimension", owner.level().dimension().location().toString());
        owner.getPersistentData().put(ENERGY, energy);
        XuanlanFxPacket.send(owner.serverLevel(), XuanlanFxPacket.BURST, target.getId(), target.getBoundingBox().getCenter(), target.position());
        owner.displayClientMessage(Component.translatable("message.ftboss.xuanlan.energy"), true);
    }

    private static int readEnergy(Player owner) {
        CompoundTag energy = owner.getPersistentData().getCompound(ENERGY);
        return energy.getString("Dimension").equals(owner.level().dimension().location().toString())
                && XuanlanRules.active(owner.level().getGameTime(), energy.getLong("Until"), XuanlanRules.ENERGY_TICKS) ? 3 : 0;
    }

    public static boolean hasBlade(ServerPlayer owner) {
        return XuanlanBladeRoster.count(owner) > 0;
    }

    public static boolean canLaunchBlade(ServerPlayer owner) {
        return XuanlanBladeRoster.count(owner) <= XuanlanRules.MAX_ACTIVE_BLADES - XuanlanRules.BLADES_PER_VOLLEY;
    }

    public static int trackBlade(ServerPlayer owner, PlayerMoonChakramEntity blade) {
        return XuanlanBladeRoster.track(owner, blade);
    }

    public static int bladeSlot(ServerPlayer owner, PlayerMoonChakramEntity blade) {
        return XuanlanBladeRoster.slot(owner, blade);
    }

    public static int forgetBlade(ServerPlayer owner, PlayerMoonChakramEntity blade) {
        return XuanlanBladeRoster.forget(owner, blade);
    }

    public static boolean isTrackedBlade(ServerPlayer owner, PlayerMoonChakramEntity blade) {
        return bladeSlot(owner, blade) >= 0;
    }
}
