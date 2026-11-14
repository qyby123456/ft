/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.TamableAnimal
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.entity.PlayerMoonChakramEntity;
import net.farmingtales.ftboss.skill.XuanlanBladeRoster;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.farmingtales.ftboss.util.XuanlanRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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

    private XuanlanCombat() {
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static boolean canHit(Player owner, Entity target) {
        if (!(target instanceof LivingEntity)) return false;
        LivingEntity living = (LivingEntity)target;
        if (!living.m_6084_()) return false;
        if (living.m_5833_()) return false;
        if (living == owner) return false;
        if (living instanceof ArmorStand) return false;
        if (living.m_20147_()) return false;
        if (owner.m_7307_((Entity)living)) return false;
        if (living.m_7307_((Entity)owner)) {
            return false;
        }
        if (living instanceof Player) {
            Player player = (Player)living;
            if (player.m_7500_()) return false;
            if (!owner.m_7099_(player)) return false;
            if (owner.m_20194_() == null) return false;
            if (!owner.m_20194_().m_129799_()) {
                return false;
            }
        }
        if (!(living instanceof TamableAnimal)) return true;
        TamableAnimal pet = (TamableAnimal)living;
        if (!owner.m_20148_().equals(pet.m_21805_())) return true;
        return false;
    }

    public static Vec3 clip(ServerLevel level, Entity actor, Vec3 from, Vec3 to) {
        return level.m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, actor)).m_82450_();
    }

    public static void fire(ServerPlayer player, int chargeTicks) {
        ServerLevel level = player.m_284548_();
        Vec3 from = player.m_146892_();
        Vec3 endpoint = XuanlanCombat.clip(level, (Entity)player, from, from.m_82549_(player.m_20154_().m_82490_(32.0)));
        LivingEntity target = null;
        double nearest = from.m_82557_(endpoint);
        for (LivingEntity candidate : level.m_6443_(LivingEntity.class, new AABB(from, endpoint).m_82400_(0.3), entity -> XuanlanCombat.canHit((Player)player, (Entity)entity))) {
            Vec3 intersection = candidate.m_20191_().m_82371_(from, endpoint).orElse(null);
            if (candidate.m_20191_().m_82390_(from)) {
                intersection = from;
            }
            if (intersection == null || from.m_82557_(intersection) > nearest) continue;
            nearest = from.m_82557_(intersection);
            target = candidate;
        }
        if (target != null) {
            endpoint = from.m_82549_(player.m_20154_().m_82490_(Math.sqrt(nearest)));
        }
        int energy = XuanlanCombat.readEnergy((Player)player);
        player.getPersistentData().m_128473_(ENERGY);
        if (target != null && target.m_6469_(level.m_269111_().m_269299_((Entity)player, (LivingEntity)player), XuanlanRules.shotDamage(chargeTicks, energy)) && target.m_6084_() && chargeTicks >= 20) {
            CompoundTag mark = new CompoundTag();
            mark.m_128362_("Owner", player.m_20148_());
            mark.m_128356_("Until", level.m_46467_() + 120L);
            mark.m_128359_("Dimension", level.m_46472_().m_135782_().toString());
            target.getPersistentData().m_128365_(MARK, (Tag)mark);
            XuanlanFxPacket.send(level, 2, target.m_19879_(), target.m_20182_(), target.m_20182_());
        }
        XuanlanFxPacket.send(level, energy > 0 ? 1 : 0, player.m_19879_(), from.m_82520_(0.0, -0.12, 0.0), endpoint);
    }

    public static boolean markedBy(LivingEntity target, Player owner) {
        CompoundTag mark = target.getPersistentData().m_128469_(MARK);
        return mark.m_128403_("Owner") && owner.m_20148_().equals(mark.m_128342_("Owner")) && mark.m_128461_("Dimension").equals(owner.m_9236_().m_46472_().m_135782_().toString()) && XuanlanRules.active(owner.m_9236_().m_46467_(), mark.m_128454_("Until"), 120);
    }

    public static void consumeMark(LivingEntity target, ServerPlayer owner) {
        target.getPersistentData().m_128473_(MARK);
        CompoundTag energy = new CompoundTag();
        energy.m_128356_("Until", owner.m_9236_().m_46467_() + 160L);
        energy.m_128359_("Dimension", owner.m_9236_().m_46472_().m_135782_().toString());
        owner.getPersistentData().m_128365_(ENERGY, (Tag)energy);
        XuanlanFxPacket.send(owner.m_284548_(), 3, target.m_19879_(), target.m_20191_().m_82399_(), target.m_20182_());
        owner.m_5661_((Component)Component.m_237115_((String)"message.ftboss.xuanlan.energy"), true);
    }

    private static int readEnergy(Player owner) {
        CompoundTag energy = owner.getPersistentData().m_128469_(ENERGY);
        return energy.m_128461_("Dimension").equals(owner.m_9236_().m_46472_().m_135782_().toString()) && XuanlanRules.active(owner.m_9236_().m_46467_(), energy.m_128454_("Until"), 160) ? 3 : 0;
    }

    public static boolean hasBlade(ServerPlayer owner) {
        return XuanlanBladeRoster.count(owner) > 0;
    }

    public static boolean canLaunchBlade(ServerPlayer owner) {
        return XuanlanBladeRoster.count(owner) < 2;
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
        return XuanlanCombat.bladeSlot(owner, blade) >= 0;
    }
}

