/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
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
 *  net.minecraft.world.entity.animal.horse.AbstractHorse
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.item;

import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class FrostQueenStaffItem
extends Item {
    public static final double RAY_RANGE = 24.0;
    public static final double NOVA_RADIUS = 5.0;
    public static final float RAY_DAMAGE = 14.0f;
    public static final float NOVA_DAMAGE = 10.0f;
    public static final int RAY_COOLDOWN = 30;
    public static final int NOVA_COOLDOWN = 160;
    private static final int MAX_NOVA_TARGETS = 16;

    public FrostQueenStaffItem() {
        super(new Item.Properties().m_41503_(1024).m_41497_(Rarity.EPIC).m_41486_());
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.m_6084_() || player.m_5833_() || player.m_36335_().m_41519_((Item)this)) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.m_19092_((Object)stack, (boolean)true);
        }
        ServerPlayer caster = (ServerPlayer)player;
        boolean nova = player.m_6144_();
        if (nova) {
            FrostQueenStaffItem.nova(caster);
        } else {
            FrostQueenStaffItem.ray(caster);
        }
        player.m_36335_().m_41524_((Item)this, nova ? 160 : 30);
        stack.m_41622_(nova ? 4 : 1, (LivingEntity)player, holder -> holder.m_21190_(hand));
        player.m_21011_(hand, true);
        player.m_36399_(nova ? 0.2f : 0.05f);
        return InteractionResultHolder.m_19092_((Object)stack, (boolean)false);
    }

    private static boolean canTarget(ServerPlayer player, LivingEntity target) {
        AbstractHorse horse;
        OwnableEntity owned;
        if (target == player || !target.m_6084_() || target.m_213877_() || target.m_5833_() || target.m_20147_() || target instanceof ArmorStand || player.m_7307_((Entity)target) || target.m_7307_((Entity)player) || player.m_20365_((Entity)target)) {
            return false;
        }
        if (target instanceof OwnableEntity && (owned = (OwnableEntity)target).m_21805_() != null) {
            return false;
        }
        if (target instanceof AbstractHorse && (horse = (AbstractHorse)target).m_30614_()) {
            return false;
        }
        if (target instanceof Player) {
            Player other = (Player)target;
            return !other.m_7500_() && player.m_284548_().m_7654_().m_129799_() && player.m_7099_(other);
        }
        return true;
    }

    private static void ray(ServerPlayer player) {
        ServerLevel level = player.m_284548_();
        Vec3 start = player.m_146892_();
        Vec3 end = level.m_45547_(new ClipContext(start, start.m_82549_(player.m_20154_().m_82490_(24.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player)).m_82450_();
        LivingEntity nearest = null;
        Vec3 impact = end;
        double nearestDistance = start.m_82557_(end);
        for (LivingEntity target : level.m_6443_(LivingEntity.class, new AABB(start, end).m_82400_(0.3), candidate -> FrostQueenStaffItem.canTarget(player, candidate))) {
            AABB hitbox = target.m_20191_().m_82400_(0.15);
            Vec3 hit = hitbox.m_82390_(start) ? start : (Vec3)hitbox.m_82371_(start, end).orElse(null);
            if (hit == null || !(start.m_82557_(hit) < nearestDistance)) continue;
            nearestDistance = start.m_82557_(hit);
            nearest = target;
            impact = hit;
        }
        if (nearest != null) {
            FrostQueenStaffItem.strike(player, nearest, 14.0f, 60, 2);
        }
        Vec3 direction = impact.m_82546_(start);
        int steps = Math.max(1, (int)Math.ceil(direction.m_82553_() / 0.35));
        for (int step = 0; step <= steps; ++step) {
            Vec3 position = start.m_82549_(direction.m_82490_((double)step / (double)steps));
            level.m_8767_((ParticleOptions)ParticleTypes.f_175821_, position.f_82479_, position.f_82480_, position.f_82481_, 1, 0.03, 0.03, 0.03, 0.0);
            if (step % 3 != 0) continue;
            level.m_8767_((ParticleOptions)ParticleTypes.f_123810_, position.f_82479_, position.f_82480_, position.f_82481_, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.m_8767_((ParticleOptions)ParticleTypes.f_175821_, impact.f_82479_, impact.f_82480_, impact.f_82481_, 18, 0.25, 0.25, 0.25, 0.025);
        level.m_5594_(null, player.m_20183_(), SoundEvents.f_144243_, SoundSource.PLAYERS, 1.0f, 1.4f);
    }

    private static void nova(ServerPlayer player) {
        ServerLevel level = player.m_284548_();
        Vec3 center = player.m_20182_().m_82520_(0.0, 0.2, 0.0);
        List targets = level.m_6443_(LivingEntity.class, player.m_20191_().m_82400_(5.0), target -> FrostQueenStaffItem.canTarget(player, target) && player.m_20280_((Entity)target) <= 25.0);
        targets.sort(Comparator.comparingDouble(arg_0 -> ((ServerPlayer)player).m_20280_(arg_0)));
        int hits = 0;
        for (LivingEntity target2 : targets) {
            Vec3 targetCenter = target2.m_20191_().m_82399_();
            if (level.m_45547_(new ClipContext(player.m_146892_(), targetCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player)).m_6662_() != HitResult.Type.MISS) continue;
            FrostQueenStaffItem.strike(player, target2, 10.0f, 100, 1);
            if (++hits < 16) continue;
            break;
        }
        for (int ring = 1; ring <= 3; ++ring) {
            double radius = 5.0 * (double)ring / 3.0;
            for (int point = 0; point < 48; ++point) {
                double angle = Math.PI * 2 * (double)point / 48.0;
                level.m_8767_((ParticleOptions)ParticleTypes.f_175821_, center.f_82479_ + Math.cos(angle) * radius, center.f_82480_, center.f_82481_ + Math.sin(angle) * radius, 1, 0.04, 0.15, 0.04, 0.015);
            }
        }
        level.m_5594_(null, player.m_20183_(), SoundEvents.f_11983_, SoundSource.PLAYERS, 0.8f, 1.3f);
    }

    private static void strike(ServerPlayer player, LivingEntity target, float damage, int duration, int amplifier) {
        if (target.m_6469_(player.m_269291_().m_269104_((Entity)player, (Entity)player), damage)) {
            target.m_7292_(new MobEffectInstance(MobEffects.f_19597_, duration, amplifier));
        }
    }

    public boolean m_6832_(ItemStack stack, ItemStack ingredient) {
        return ingredient.m_150930_(Items.f_151049_) || super.m_6832_(stack, ingredient);
    }

    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.frost_queen_staff.ray", (Object[])new Object[]{24, 14, Float.valueOf(1.5f)}).m_130940_(ChatFormatting.AQUA));
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.frost_queen_staff.nova", (Object[])new Object[]{5, 10, 8}).m_130940_(ChatFormatting.LIGHT_PURPLE));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.frost_queen_staff.rules").m_130940_(ChatFormatting.GRAY));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.frost_queen_staff.source").m_130940_(ChatFormatting.DARK_GRAY));
    }
}

