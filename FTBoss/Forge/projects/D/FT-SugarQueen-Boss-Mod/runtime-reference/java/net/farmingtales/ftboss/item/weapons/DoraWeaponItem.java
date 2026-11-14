/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.OwnableEntity
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.UseAnim
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import net.farmingtales.ftboss.item.FTWeaponTier;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.DoraWeaponRules;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class DoraWeaponItem
extends SwordItem {
    private static final int USE_TICKS = 72000;
    private final boolean lance;

    public DoraWeaponItem(boolean lance) {
        super((Tier)FTWeaponTier.INSTANCE, lance ? 4 : 1, lance ? -2.6f : -3.0f, new Item.Properties().m_41487_(1).m_41497_(Rarity.EPIC).m_41486_());
        this.lance = lance;
    }

    public int m_8105_(ItemStack stack) {
        return 72000;
    }

    public UseAnim m_6164_(ItemStack stack) {
        return this.lance ? UseAnim.SPEAR : UseAnim.BOW;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (this.lance) {
            return super.canApplyAtEnchantingTable(stack, enchantment);
        }
        return enchantment == Enchantments.f_44986_ || enchantment == Enchantments.f_44962_ || enchantment == Enchantments.f_44963_;
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.m_6084_() || player.m_5833_() || player.m_36335_().m_41519_((Item)this)) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        player.m_6672_(hand);
        return InteractionResultHolder.m_19096_((Object)stack);
    }

    public void m_5929_(Level level, LivingEntity user, ItemStack stack, int remaining) {
        ServerPlayer player;
        block3: {
            block2: {
                if (this.lance || !(user instanceof ServerPlayer)) break block2;
                player = (ServerPlayer)user;
                if (remaining % 4 == 0) break block3;
            }
            return;
        }
        Vec3 direction = player.m_20154_();
        Vec3 chargePoint = player.m_146892_().m_82549_(direction.m_82490_(0.8)).m_82520_(0.0, -0.15, 0.0);
        WeaponFx.send(player.m_284548_(), WeaponFxRules.Kind.DORA_CHARGE, WeaponFxRules.Theme.ARC, chargePoint, chargePoint.m_82549_(direction.m_82490_(0.36)), 0.32, player.m_146908_());
    }

    public void m_5551_(ItemStack stack, Level level, LivingEntity user, int remaining) {
        ServerPlayer player;
        if (!(user instanceof ServerPlayer) || !(player = (ServerPlayer)user).m_6084_() || player.m_5833_() || player.m_21205_() != stack || player.m_36335_().m_41519_((Item)this)) {
            return;
        }
        double charge = DoraWeaponRules.charge(72000 - remaining);
        if (charge == 0.0) {
            return;
        }
        DoraWeaponRules.Mode mode = DoraWeaponRules.mode(this.lance, player.m_6144_());
        player.m_36335_().m_41524_((Item)this, mode.cooldownTicks);
        if (this.lance) {
            DoraWeaponItem.strike(player, mode, charge);
        } else {
            DoraWeaponItem.shoot(player, mode, charge);
        }
        stack.m_41622_(mode.durabilityCost, (LivingEntity)player, owner -> owner.m_21190_(InteractionHand.MAIN_HAND));
        player.m_36399_(0.15f);
        player.m_21011_(InteractionHand.MAIN_HAND, true);
    }

    private static boolean canTarget(ServerPlayer player, LivingEntity target) {
        Player other;
        OwnableEntity owned;
        if (target == player || !target.m_6084_() || target.m_5833_() || target.m_20147_() || target instanceof ArmorStand || player.m_7307_((Entity)target) || target.m_7307_((Entity)player)) {
            return false;
        }
        if (target instanceof OwnableEntity && (owned = (OwnableEntity)target).m_21805_() != null) {
            return false;
        }
        return !(target instanceof Player) || !(other = (Player)target).m_7500_() && player.f_8924_.m_129799_() && player.m_7099_(other);
    }

    private static boolean visible(ServerPlayer player, Vec3 from, Vec3 to) {
        return player.m_284548_().m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player)).m_6662_() == HitResult.Type.MISS;
    }

    private static void shoot(ServerPlayer player, DoraWeaponRules.Mode mode, double charge) {
        ServerLevel level = player.m_284548_();
        Vec3 start = player.m_146892_();
        Vec3 direction = player.m_20154_();
        Vec3 reach = start.m_82549_(direction.m_82490_(32.0));
        Vec3 end = level.m_45547_(new ClipContext(start, reach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player)).m_82450_();
        LivingEntity hit = null;
        double nearest = start.m_82557_(end);
        for (LivingEntity candidate : level.m_6443_(LivingEntity.class, new AABB(start, end).m_82400_(1.0), entity -> entity != player && entity.m_6084_() && !entity.m_5833_())) {
            AABB bounds = candidate.m_20191_().m_82400_(0.18);
            Vec3 point = bounds.m_82390_(start) ? start : (Vec3)bounds.m_82371_(start, end).orElse(null);
            if (point == null || !(start.m_82557_(point) < nearest)) continue;
            nearest = start.m_82557_(point);
            end = point;
            hit = candidate;
        }
        WeaponFx.send(level, mode == DoraWeaponRules.Mode.NOVA ? WeaponFxRules.Kind.DORA_NOVA : WeaponFxRules.Kind.DORA_PULSE, mode == DoraWeaponRules.Mode.NOVA ? WeaponFxRules.Theme.GOLD : WeaponFxRules.Theme.ARC, start, end, 0.18, player.m_146908_());
        if (mode == DoraWeaponRules.Mode.NOVA) {
            Vec3 impact = end.m_82546_(direction.m_82490_(Math.min(0.08, start.m_82554_(end))));
            double radius = 2.75;
            for (LivingEntity target : level.m_45976_(LivingEntity.class, new AABB(impact, impact).m_82400_(radius))) {
                if (!DoraWeaponItem.canTarget(player, target) || target.m_20191_().m_82399_().m_82557_(impact) > radius * radius || !DoraWeaponItem.visible(player, impact, target.m_20191_().m_82399_())) continue;
                target.m_6469_(player.m_269291_().m_269075_((Player)player), (float)((double)mode.damage() * charge));
            }
            WeaponFx.send(level, WeaponFxRules.Kind.DORA_IMPACT, WeaponFxRules.Theme.GOLD, impact, impact, radius, player.m_146908_());
            level.m_6263_(null, impact.f_82479_, impact.f_82480_, impact.f_82481_, SoundEvents.f_11913_, SoundSource.PLAYERS, 0.65f, 1.65f);
        } else if (hit != null && DoraWeaponItem.canTarget(player, hit)) {
            hit.m_6469_(player.m_269291_().m_269075_((Player)player), (float)((double)mode.damage() * charge));
        }
        level.m_5594_(null, player.m_20183_(), SoundEvents.f_11738_, SoundSource.PLAYERS, 0.65f, 1.6f);
    }

    private static void strike(ServerPlayer player, DoraWeaponRules.Mode mode, double charge) {
        ServerLevel level = player.m_284548_();
        Vec3 start = player.m_146892_();
        Vec3 forward = player.m_20154_();
        double yaw = Math.toRadians(player.m_146908_());
        Vec3 horizontal = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
        double range = mode == DoraWeaponRules.Mode.THRUST ? 6.5 : 4.5;
        for (LivingEntity target : level.m_45976_(LivingEntity.class, player.m_20191_().m_82400_(range))) {
            if (!DoraWeaponItem.canTarget(player, target)) continue;
            Vec3 center = target.m_20191_().m_82399_();
            Vec3 offset = center.m_82546_(start);
            double along = offset.m_82526_(forward);
            Vec3 planar = offset.m_82542_(1.0, 0.0, 1.0).m_82541_();
            boolean inside = mode == DoraWeaponRules.Mode.THRUST ? DoraWeaponRules.inThrust(along, Math.max(0.0, offset.m_82556_() - along * along)) : DoraWeaponRules.inSweep(offset.m_82556_(), planar.m_82526_(horizontal));
            if (!inside || !DoraWeaponItem.visible(player, start, center) || !target.m_6469_(player.m_269291_().m_269075_((Player)player), (float)((double)mode.damage() * charge))) continue;
            target.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 50, 0));
            target.m_5997_(planar.f_82479_ * 0.3, 0.08, planar.f_82481_ * 0.3);
            target.f_19864_ = true;
        }
        if (mode == DoraWeaponRules.Mode.THRUST) {
            Vec3 end = level.m_45547_(new ClipContext(start, start.m_82549_(forward.m_82490_(range)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player)).m_82450_();
            WeaponFx.beam(level, start, end, WeaponFxRules.Theme.ARC);
        } else {
            Vec3 origin = player.m_20182_().m_82520_(0.0, 1.0, 0.0);
            WeaponFx.send(level, WeaponFxRules.Kind.SWEEP, WeaponFxRules.Theme.GOLD, origin, origin, range, player.m_146908_());
        }
        level.m_5594_(null, player.m_20183_(), SoundEvents.f_12317_, SoundSource.PLAYERS, 0.9f, 0.85f);
    }

    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        String prefix = this.lance ? "tooltip.ftboss.dora_lance" : "tooltip.ftboss.dora_pulse_gun";
        tooltip.add((Component)Component.m_237115_((String)(prefix + ".use")).m_130940_(ChatFormatting.AQUA));
        tooltip.add((Component)Component.m_237115_((String)(prefix + ".shift")).m_130940_(ChatFormatting.GOLD));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.dora_weapon.charge").m_130940_(ChatFormatting.GRAY));
    }
}

