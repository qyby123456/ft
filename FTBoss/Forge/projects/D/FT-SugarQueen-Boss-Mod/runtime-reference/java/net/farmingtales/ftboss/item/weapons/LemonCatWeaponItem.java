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
 *  net.minecraft.world.entity.TamableAnimal
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.FTWeaponTier;
import net.farmingtales.ftboss.skill.LemonCatNetwork;
import net.farmingtales.ftboss.util.LemonCatWeaponRules;
import net.farmingtales.ftboss.util.ManorWeaponBalanceRules;
import net.farmingtales.ftboss.util.ManorWeaponRules;
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
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class LemonCatWeaponItem
extends SwordItem {
    public static final int SKILLET_COOLDOWN = 160;
    public static final int SPATULA_COOLDOWN = 280;
    public static final float SKILLET_DAMAGE = 24.0f;
    public static final float PAIRED_DAMAGE = 32.0f;
    public static final double SKILLET_RANGE = 4.5;
    public static final double PAIRED_RANGE = 5.5;
    public static final float HEAL_AMOUNT = 8.0f;
    public static final float PAIRED_HEAL_AMOUNT = 12.0f;
    public static final int REGEN_TICKS = 100;
    public static final int SPEED_TICKS = 120;
    private final Kind kind;

    public static float skilletDamage(boolean paired) {
        return ManorWeaponBalanceRules.skillDamage(paired ? 32.0f : 24.0f);
    }

    public static float healAmount(boolean paired) {
        return ManorWeaponRules.skillHeal(paired ? 12.0f : 8.0f);
    }

    public LemonCatWeaponItem(Kind kind) {
        super((Tier)FTWeaponTier.INSTANCE, kind == Kind.SKILLET ? 6 : 3, kind == Kind.SKILLET ? -2.6f : -2.0f, new Item.Properties().m_41497_(Rarity.EPIC));
        this.kind = kind;
    }

    public static boolean isPair(Player player) {
        ItemStack main = player.m_21205_();
        ItemStack off = player.m_21206_();
        return main.m_150930_((Item)FTBossItems.LEMONCAT_SKILLET.get()) && off.m_150930_((Item)FTBossItems.LEMONCAT_SPATULA.get()) || main.m_150930_((Item)FTBossItems.LEMONCAT_SPATULA.get()) && off.m_150930_((Item)FTBossItems.LEMONCAT_SKILLET.get());
    }

    public static boolean isCoolingDown(Player player) {
        return player.m_36335_().m_41519_((Item)FTBossItems.LEMONCAT_SKILLET.get()) || player.m_36335_().m_41519_((Item)FTBossItems.LEMONCAT_SPATULA.get());
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.m_21120_(hand);
        if (!player.m_6084_() || player.m_5833_() || LemonCatWeaponItem.isCoolingDown(player)) {
            return InteractionResultHolder.m_19100_((Object)held);
        }
        if (level.f_46443_) {
            return InteractionResultHolder.m_19090_((Object)held);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.m_19098_((Object)held);
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        boolean paired = LemonCatWeaponItem.isPair(player);
        Kind action = LemonCatWeaponRules.useHealing(paired, player.m_6144_(), this.kind == Kind.SPATULA) ? Kind.SPATULA : Kind.SKILLET;
        Item activeItem = action == Kind.SKILLET ? (Item)FTBossItems.LEMONCAT_SKILLET.get() : (Item)FTBossItems.LEMONCAT_SPATULA.get();
        InteractionHand activeHand = player.m_21205_().m_150930_(activeItem) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack activeStack = player.m_21120_(activeHand);
        int cooldown = action == Kind.SKILLET ? 160 : 280;
        player.m_36335_().m_41524_((Item)FTBossItems.LEMONCAT_SKILLET.get(), cooldown);
        player.m_36335_().m_41524_((Item)FTBossItems.LEMONCAT_SPATULA.get(), cooldown);
        if (action == Kind.SKILLET) {
            LemonCatWeaponItem.flip(serverPlayer, paired);
        } else {
            LemonCatWeaponItem.refresh(serverPlayer, paired);
        }
        activeStack.m_41622_(action == Kind.SKILLET ? 4 : 3, (LivingEntity)player, wearer -> wearer.m_21190_(activeHand));
        player.m_21011_(activeHand, true);
        return InteractionResultHolder.m_19096_((Object)held);
    }

    private static boolean canHit(ServerPlayer player, LivingEntity target) {
        Player other;
        TamableAnimal pet;
        if (target == player || !target.m_6084_() || target.m_5833_() || target instanceof ArmorStand || target.m_20147_() || player.m_7307_((Entity)target) || target.m_7307_((Entity)player)) {
            return false;
        }
        if (target instanceof TamableAnimal && (pet = (TamableAnimal)target).m_21824_()) {
            return false;
        }
        if (target instanceof Player && ((other = (Player)target).m_7500_() || !player.f_8924_.m_129799_() || !player.m_7099_(other))) {
            return false;
        }
        return player.m_142582_((Entity)target);
    }

    private static void flip(ServerPlayer player, boolean paired) {
        ServerLevel level = player.m_284548_();
        double radius = paired ? 5.5 : 4.5;
        Vec3 direction = Vec3.m_82498_((float)0.0f, (float)player.m_146908_());
        int hits = 0;
        for (LivingEntity target : level.m_45976_(LivingEntity.class, player.m_20191_().m_82377_(radius, 2.5, radius))) {
            if (!LemonCatWeaponItem.canHit(player, target)) continue;
            Vec3 offset = target.m_20182_().m_82546_(player.m_20182_());
            Vec3 horizontal = new Vec3(offset.f_82479_, 0.0, offset.f_82481_);
            if (!LemonCatWeaponRules.inCone(offset.f_82479_, offset.f_82480_, offset.f_82481_, direction.f_82479_, direction.f_82481_, radius) || !target.m_6469_(level.m_269111_().m_269075_((Player)player), LemonCatWeaponItem.skilletDamage(paired))) continue;
            Vec3 push = horizontal.m_82556_() > 0.01 ? horizontal.m_82541_() : direction;
            target.m_147240_(0.45, -push.f_82479_, -push.f_82481_);
            target.m_5997_(0.0, 0.35, 0.0);
            target.f_19864_ = true;
            target.m_20254_(3);
            if (paired) {
                target.m_147207_(new MobEffectInstance(MobEffects.f_19597_, 60, 1), (Entity)player);
            }
            level.m_8767_((ParticleOptions)ParticleTypes.f_123744_, target.m_20185_(), target.m_20186_() + 0.7, target.m_20189_(), 7, 0.25, 0.2, 0.25, 0.025);
            ++hits;
        }
        level.m_5594_(null, player.m_20183_(), SoundEvents.f_11874_, SoundSource.PLAYERS, 0.8f, 1.2f);
        level.m_5594_(null, player.m_20183_(), SoundEvents.f_12317_, SoundSource.PLAYERS, 0.9f, 0.8f);
        LemonCatNetwork.send(player, paired ? 2 : 0);
        player.m_5661_((Component)Component.m_237110_((String)"message.ftboss.lemoncat.flip", (Object[])new Object[]{hits}), true);
    }

    private static void refresh(ServerPlayer player, boolean paired) {
        player.m_5634_(LemonCatWeaponItem.healAmount(paired));
        player.m_21195_(MobEffects.f_19614_);
        player.m_21195_(MobEffects.f_19597_);
        player.m_7292_(new MobEffectInstance(MobEffects.f_19605_, 100, 0));
        player.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 120, 0));
        if (paired) {
            player.m_20095_();
            player.m_7292_(new MobEffectInstance(MobEffects.f_19607_, 120, 0));
        }
        ServerLevel level = player.m_284548_();
        level.m_8767_((ParticleOptions)ParticleTypes.f_123748_, player.m_20185_(), player.m_20186_() + 1.0, player.m_20189_(), 14, 0.55, 0.6, 0.55, 0.0);
        level.m_8767_((ParticleOptions)ParticleTypes.f_123810_, player.m_20185_(), player.m_20186_() + 0.8, player.m_20189_(), 10, 0.4, 0.5, 0.4, 0.015);
        level.m_5594_(null, player.m_20183_(), SoundEvents.f_144243_, SoundSource.PLAYERS, 0.9f, 1.25f);
        LemonCatNetwork.send(player, paired ? 3 : 1);
        player.m_5661_((Component)Component.m_237115_((String)"message.ftboss.lemoncat.refresh"), true);
    }

    public boolean m_7579_(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        ServerPlayer player;
        boolean hit = super.m_7579_(stack, target, attacker);
        if (hit && attacker instanceof ServerPlayer && LemonCatWeaponItem.canHit(player = (ServerPlayer)attacker, target)) {
            player.m_284548_().m_8767_((ParticleOptions)(this.kind == Kind.SKILLET ? ParticleTypes.f_123797_ : ParticleTypes.f_123810_), target.m_20185_(), target.m_20186_() + (double)target.m_20206_() * 0.5, target.m_20189_(), 5, 0.2, 0.2, 0.2, 0.01);
        }
        return hit;
    }

    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.m_7373_(stack, level, tooltip, flag);
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.lemoncat.origin").m_130940_(ChatFormatting.DARK_PURPLE));
        if (this.kind == Kind.SKILLET) {
            tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.lemoncat.flip", (Object[])new Object[]{(int)LemonCatWeaponItem.skilletDamage(false), 4.5, 8}).m_130940_(ChatFormatting.GOLD));
            tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.lemoncat.flip_pair", (Object[])new Object[]{(int)LemonCatWeaponItem.skilletDamage(true), 5.5}).m_130940_(ChatFormatting.YELLOW));
        } else {
            tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.lemoncat.refresh", (Object[])new Object[]{(int)LemonCatWeaponItem.healAmount(false), 14}).m_130940_(ChatFormatting.GREEN));
            tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.lemoncat.refresh_pair", (Object[])new Object[]{(int)LemonCatWeaponItem.healAmount(true)}).m_130940_(ChatFormatting.AQUA));
        }
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.lemoncat.controls").m_130940_(ChatFormatting.GRAY));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.lemoncat.cooldown").m_130940_(ChatFormatting.DARK_GRAY));
    }

    public static enum Kind {
        SKILLET,
        SPATULA;

    }
}

