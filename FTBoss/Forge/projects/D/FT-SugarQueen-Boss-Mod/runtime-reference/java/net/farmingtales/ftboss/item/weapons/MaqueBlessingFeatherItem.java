/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.TamableAnimal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.farmingtales.ftboss.boss.ai.HealBan;
import net.farmingtales.ftboss.maque.support.MaqueSupportHooks;
import net.farmingtales.ftboss.skill.MaqueFeatherPacket;
import net.farmingtales.ftboss.util.MaqueFeatherRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class MaqueBlessingFeatherItem
extends Item {
    private static final String COOLDOWN = "ftboss.maque_feather.cooldown";
    private static final String LAST_HELD = "ftboss.maque_feather.last_held";
    private static final String NEXT_PULSE = "ftboss.maque_feather.next_pulse";
    private static final List<MobEffect> CLEANSABLE = List.of(MobEffects.f_19614_, MobEffects.f_19615_, MobEffects.f_19613_, MobEffects.f_19597_, MobEffects.f_19610_);

    public MaqueBlessingFeatherItem() {
        super(new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE));
    }

    private static long clock(ServerPlayer player) {
        return player.m_284548_().m_7654_().m_129783_().m_46467_();
    }

    private static ItemStack heldFeather(Player player) {
        if (player.m_21205_().m_41720_() instanceof MaqueBlessingFeatherItem) {
            return player.m_21205_();
        }
        if (player.m_21206_().m_41720_() instanceof MaqueBlessingFeatherItem) {
            return player.m_21206_();
        }
        return ItemStack.f_41583_;
    }

    public void m_6883_(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        CompoundTag data;
        ServerPlayer player;
        if (!(entity instanceof ServerPlayer) || !(player = (ServerPlayer)entity).m_6084_() || player.m_5833_() || stack != MaqueBlessingFeatherItem.heldFeather((Player)player)) {
            return;
        }
        long now = MaqueBlessingFeatherItem.clock(player);
        int remaining = MaqueFeatherRules.remainingCooldown(now, (data = player.getPersistentData()).m_128454_(COOLDOWN));
        if (remaining > 0 && !player.m_36335_().m_41519_((Item)this)) {
            player.m_36335_().m_41524_((Item)this, remaining);
        }
        if (data.m_128441_(LAST_HELD) && data.m_128454_(LAST_HELD) == now) {
            return;
        }
        if (!data.m_128441_(LAST_HELD) || !MaqueFeatherRules.continuousHold(now, data.m_128454_(LAST_HELD))) {
            data.m_128356_(NEXT_PULSE, now + 80L);
        }
        data.m_128356_(LAST_HELD, now);
        if (now < data.m_128454_(NEXT_PULSE)) {
            return;
        }
        data.m_128356_(NEXT_PULSE, now + 80L);
        if (player.m_21223_() < player.m_21233_() && !HealBan.isBanned((LivingEntity)player)) {
            MaqueSupportHooks.heal((LivingEntity)player, MaqueFeatherRules.passiveHeal(), null, 9);
            player.m_284548_().m_8767_((ParticleOptions)ParticleTypes.f_123748_, player.m_20185_(), player.m_20186_() + 1.0, player.m_20189_(), 3, 0.22, 0.28, 0.22, 0.01);
        }
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (!player.m_6084_() || player.m_5833_()) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        if (level.f_46443_) {
            return InteractionResultHolder.m_19092_((Object)stack, (boolean)true);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        ServerPlayer caster = (ServerPlayer)player;
        long now = MaqueBlessingFeatherItem.clock(caster);
        int remaining = MaqueFeatherRules.remainingCooldown(now, caster.getPersistentData().m_128454_(COOLDOWN));
        if (remaining > 0 || player.m_36335_().m_41519_((Item)this)) {
            if (remaining > 0) {
                player.m_36335_().m_41524_((Item)this, remaining);
            }
            caster.m_5661_((Component)Component.m_237110_((String)"message.ftboss.maque_feather.cooldown", (Object[])new Object[]{Math.max(1, (remaining + 19) / 20)}), true);
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        caster.getPersistentData().m_128356_(COOLDOWN, now + 400L);
        player.m_36335_().m_41524_((Item)this, 400);
        boolean wind = player.m_6144_();
        if (wind) {
            caster.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 120, 1));
            caster.m_7292_(new MobEffectInstance(MobEffects.f_19591_, 120, 0));
            caster.f_19789_ = 0.0f;
            caster.m_5661_((Component)Component.m_237115_((String)"message.ftboss.maque_feather.wind"), true);
        } else {
            ServerLevel server = caster.m_284548_();
            List targets = server.m_6443_(LivingEntity.class, caster.m_20191_().m_82400_(6.0), target -> target != caster && MaqueBlessingFeatherItem.canSupport(caster, target));
            targets.sort(Comparator.comparingDouble(arg_0 -> ((ServerPlayer)caster).m_20280_(arg_0)));
            MaqueBlessingFeatherItem.bless(server, (LivingEntity)caster);
            for (LivingEntity target2 : targets.stream().limit(15L).toList()) {
                MaqueBlessingFeatherItem.bless(server, target2);
            }
            caster.m_5661_((Component)Component.m_237110_((String)"message.ftboss.maque_feather.blessing", (Object[])new Object[]{Math.min(targets.size() + 1, 16)}), true);
        }
        MaqueFeatherPacket.send(caster, wind);
        level.m_5594_(null, caster.m_20183_(), wind ? SoundEvents.f_11886_ : SoundEvents.f_144243_, SoundSource.PLAYERS, 0.7f, wind ? 1.3f : 1.15f);
        player.m_21011_(hand, true);
        return InteractionResultHolder.m_19092_((Object)stack, (boolean)false);
    }

    private static boolean canSupport(ServerPlayer caster, LivingEntity target) {
        TamableAnimal pet;
        boolean alliedPlayer = target instanceof Player && caster.m_5647_() != null && caster.m_7307_((Entity)target);
        boolean ownedPet = target instanceof TamableAnimal && (pet = (TamableAnimal)target).m_21824_() && caster.m_20148_().equals(pet.m_21805_());
        return MaqueFeatherRules.canSupport(target == caster, target.m_6084_(), target.m_5833_(), alliedPlayer, ownedPet, caster.m_20280_((Entity)target), target == caster || caster.m_142582_((Entity)target));
    }

    private static void bless(ServerLevel level, LivingEntity target) {
        for (MobEffect effect : CLEANSABLE) {
            MaqueSupportHooks.cleanse(target, effect, null);
        }
        if (!HealBan.isBanned(target)) {
            MaqueSupportHooks.heal(target, MaqueFeatherRules.burstHeal(), null, 2);
        }
        MaqueSupportHooks.empower(target, new MobEffectInstance(MobEffects.f_19605_, 120, 1), null, 3);
        MaqueSupportHooks.empower(target, new MobEffectInstance(MobEffects.f_19606_, 120, 0), null, 3);
        level.m_8767_((ParticleOptions)ParticleTypes.f_123750_, target.m_20185_(), target.m_20186_() + (double)target.m_20206_() + 0.2, target.m_20189_(), 3, 0.3, 0.15, 0.3, 0.01);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.maque_feather.passive", (Object[])new Object[]{4}).m_130940_(ChatFormatting.GREEN));
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.maque_feather.blessing", (Object[])new Object[]{6, (int)(MaqueFeatherRules.burstHeal() / 2.0f), 6}).m_130940_(ChatFormatting.AQUA));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.maque_feather.cleanse").m_130940_(ChatFormatting.GRAY));
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.maque_feather.wind", (Object[])new Object[]{6}).m_130940_(ChatFormatting.WHITE));
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.maque_feather.cooldown", (Object[])new Object[]{20}).m_130940_(ChatFormatting.GOLD));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.maque_feather.allies").m_130940_(ChatFormatting.DARK_GRAY));
    }
}

