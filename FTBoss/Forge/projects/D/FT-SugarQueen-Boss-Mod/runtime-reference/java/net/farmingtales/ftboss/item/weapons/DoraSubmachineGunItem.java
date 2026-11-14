/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
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
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import net.farmingtales.ftboss.item.FTWeaponTier;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.DoraSmgRules;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
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
import net.minecraft.world.phys.Vec3;

public final class DoraSubmachineGunItem
extends SwordItem {
    private static final int USE_TICKS = 72000;
    private static final String ROUNDS = "FTBossDoraSmgRounds";
    private static final String RELOAD = "FTBossDoraSmgReload";
    private static final ResourceKey<DamageType> ROUND_DAMAGE = ResourceKey.m_135785_((ResourceKey)Registries.f_268580_, (ResourceLocation)new ResourceLocation("ftboss", "dora_smg_round"));

    public DoraSubmachineGunItem() {
        super((Tier)FTWeaponTier.INSTANCE, 1, -3.0f, new Item.Properties().m_41487_(1).m_41497_(Rarity.EPIC).m_41486_());
    }

    private static int rounds(ItemStack stack) {
        return stack.m_41782_() && stack.m_41783_().m_128441_(ROUNDS) ? DoraSmgRules.rounds(stack.m_41783_().m_128451_(ROUNDS)) : 30;
    }

    private static int reloadTicks(ItemStack stack) {
        return stack.m_41782_() ? DoraSmgRules.reloadTicks(stack.m_41783_().m_128451_(RELOAD)) : 0;
    }

    public int m_8105_(ItemStack stack) {
        return 72000;
    }

    public UseAnim m_6164_(ItemStack stack) {
        return UseAnim.NONE;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.f_44986_ || enchantment == Enchantments.f_44962_ || enchantment == Enchantments.f_44963_;
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.m_41720_() != newStack.m_41720_();
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.m_6084_() || player.m_5833_() || player.m_36335_().m_41519_((Item)this)) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        if (player.m_6144_()) {
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                DoraSubmachineGunItem.startReload(serverPlayer, stack);
            }
            return InteractionResultHolder.m_19092_((Object)stack, (boolean)level.f_46443_);
        }
        player.m_6672_(hand);
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            this.fireOrReload(serverPlayer, stack);
        }
        return InteractionResultHolder.m_19096_((Object)stack);
    }

    public void m_5929_(Level level, LivingEntity user, ItemStack stack, int remaining) {
        ServerPlayer player;
        if (user instanceof ServerPlayer && (player = (ServerPlayer)user).m_6084_() && !player.m_5833_() && player.m_21205_() == stack && player.m_7655_() == InteractionHand.MAIN_HAND) {
            this.fireOrReload(player, stack);
        }
    }

    private void fireOrReload(ServerPlayer player, ItemStack stack) {
        if (DoraSubmachineGunItem.reloadTicks(stack) > 0 || player.m_36335_().m_41519_((Item)this)) {
            return;
        }
        int available = DoraSubmachineGunItem.rounds(stack);
        if (available == 0) {
            DoraSubmachineGunItem.startReload(player, stack);
            return;
        }
        player.m_36335_().m_41524_((Item)this, 2);
        stack.m_41784_().m_128405_(ROUNDS, available - 1);
        DoraSubmachineGunItem.shoot(player);
        stack.m_41622_(1, (LivingEntity)player, owner -> owner.m_21190_(InteractionHand.MAIN_HAND));
        player.m_36399_(0.015f);
        if (stack.m_41619_()) {
            player.m_5810_();
        } else if (available == 1) {
            DoraSubmachineGunItem.startReload(player, stack);
        } else {
            DoraSubmachineGunItem.showStatus(player, stack);
        }
    }

    private static void startReload(ServerPlayer player, ItemStack stack) {
        if (DoraSubmachineGunItem.reloadTicks(stack) > 0 || DoraSubmachineGunItem.rounds(stack) == 30) {
            return;
        }
        stack.m_41784_().m_128405_(RELOAD, 36);
        player.m_284548_().m_5594_(null, player.m_20183_(), SoundEvents.f_11843_, SoundSource.PLAYERS, 0.65f, 1.3f);
        DoraSubmachineGunItem.showStatus(player, stack);
    }

    public void m_6883_(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        ServerPlayer player;
        super.m_6883_(stack, level, entity, slot, selected);
        if (!(entity instanceof ServerPlayer) || !(player = (ServerPlayer)entity).m_6084_() || player.m_5833_() || player.m_21205_() != stack) {
            return;
        }
        int remaining = DoraSubmachineGunItem.reloadTicks(stack);
        if (remaining == 0) {
            return;
        }
        if (remaining == 1) {
            stack.m_41784_().m_128473_(RELOAD);
            stack.m_41784_().m_128405_(ROUNDS, 30);
            level.m_5594_(null, player.m_20183_(), SoundEvents.f_11841_, SoundSource.PLAYERS, 0.7f, 1.5f);
        } else {
            stack.m_41784_().m_128405_(RELOAD, remaining - 1);
        }
        if (remaining == 1 || remaining % 4 == 0) {
            DoraSubmachineGunItem.showStatus(player, stack);
        }
    }

    private static void showStatus(ServerPlayer player, ItemStack stack) {
        int reload = DoraSubmachineGunItem.reloadTicks(stack);
        MutableComponent status = reload > 0 ? Component.m_237110_((String)"message.ftboss.dora_smg.reload", (Object[])new Object[]{(double)((reload + 1) / 2) / 10.0}) : Component.m_237110_((String)"message.ftboss.dora_smg.ammo", (Object[])new Object[]{DoraSubmachineGunItem.rounds(stack), 30});
        player.m_5661_((Component)status.m_6881_().m_130940_(reload > 0 ? ChatFormatting.GOLD : ChatFormatting.AQUA), true);
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

    private static void shoot(ServerPlayer player) {
        ServerLevel level = player.m_284548_();
        Vec3 start = player.m_146892_();
        double spread = player.m_6144_() ? 0.003 : 0.009;
        Vec3 direction = player.m_20154_().m_82520_(player.m_217043_().m_188583_() * spread, player.m_217043_().m_188583_() * spread, player.m_217043_().m_188583_() * spread).m_82541_();
        Vec3 end = level.m_45547_(new ClipContext(start, start.m_82549_(direction.m_82490_(28.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player)).m_82450_();
        LivingEntity hit = null;
        double nearest = start.m_82557_(end);
        for (LivingEntity candidate : level.m_6443_(LivingEntity.class, new AABB(start, end).m_82400_(1.0), target -> target != player && target.m_6084_() && !target.m_5833_())) {
            AABB bounds = candidate.m_20191_().m_82400_(0.08);
            Vec3 point = bounds.m_82390_(start) ? start : (Vec3)bounds.m_82371_(start, end).orElse(null);
            if (point == null || !(start.m_82557_(point) < nearest)) continue;
            nearest = start.m_82557_(point);
            end = point;
            hit = candidate;
        }
        if (hit != null && DoraSubmachineGunItem.canTarget(player, hit)) {
            DamageSource source = new DamageSource((Holder)level.m_9598_().m_175515_(Registries.f_268580_).m_246971_(ROUND_DAMAGE), (Entity)player);
            hit.m_6469_(source, DoraSmgRules.damage(Math.sqrt(nearest)));
        }
        WeaponFx.send(level, WeaponFxRules.Kind.DORA_TRACER, WeaponFxRules.Theme.ARC, start, end, 0.12, player.m_146908_());
        level.m_5594_(null, player.m_20183_(), SoundEvents.f_11928_, SoundSource.PLAYERS, 0.45f, 1.8f + player.m_217043_().m_188501_() * 0.15f);
    }

    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.dora_nova_launcher.use").m_130940_(ChatFormatting.AQUA));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.dora_nova_launcher.shift").m_130940_(ChatFormatting.GOLD));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.dora_smg.stats").m_130940_(ChatFormatting.GRAY));
        tooltip.add((Component)Component.m_237110_((String)"message.ftboss.dora_smg.ammo", (Object[])new Object[]{DoraSubmachineGunItem.rounds(stack), 30}).m_130940_(ChatFormatting.DARK_AQUA));
        if (DoraSubmachineGunItem.reloadTicks(stack) > 0) {
            tooltip.add((Component)Component.m_237110_((String)"message.ftboss.dora_smg.reload", (Object[])new Object[]{(double)((DoraSubmachineGunItem.reloadTicks(stack) + 1) / 2) / 10.0}).m_130940_(ChatFormatting.GOLD));
        }
    }
}

