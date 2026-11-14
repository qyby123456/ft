/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.BlockTags
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
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.entity.EntityTeleportEvent
 *  net.minecraftforge.eventbus.api.Event
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import net.farmingtales.ftboss.client.render.DandanScytheRenderer;
import net.farmingtales.ftboss.item.FTWeaponTier;
import net.farmingtales.ftboss.skill.DandanScytheFxPacket;
import net.farmingtales.ftboss.util.DandanScytheRules;
import net.farmingtales.ftboss.util.ManorWeaponBalanceRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.Event;

public final class DandanScytheItem
extends SwordItem {
    public static final double SWEEP_RADIUS = 4.5;
    public static final double BLINK_DISTANCE = 6.0;
    public static final double BLINK_RADIUS = 2.5;
    public static final float SWEEP_DAMAGE = 20.0f;
    public static final float BLINK_DAMAGE = 16.0f;
    public static final float MARK_BONUS = 8.0f;
    public static final int SWEEP_COOLDOWN = 120;
    public static final int BLINK_COOLDOWN = 200;
    public static final int MARK_TICKS = 100;
    private static final int SLOW_TICKS = 40;
    private static final int MAX_TARGETS = 16;
    private static final String MARK_KEY = "ftboss:DandanCandleMark";

    public static float sweepDamage() {
        return ManorWeaponBalanceRules.skillDamage(20.0f);
    }

    public static float blinkDamage() {
        return ManorWeaponBalanceRules.skillDamage(16.0f);
    }

    public static float markBonus() {
        return ManorWeaponBalanceRules.skillBonusDamage(8.0f);
    }

    public DandanScytheItem() {
        super((Tier)FTWeaponTier.INSTANCE, 8, -2.8f, new Item.Properties());
    }

    public boolean m_7579_(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        ServerPlayer player;
        boolean result = super.m_7579_(stack, target, attacker);
        if (result && attacker instanceof ServerPlayer && DandanScytheItem.canTarget(player = (ServerPlayer)attacker, target)) {
            CompoundTag mark = new CompoundTag();
            mark.m_128362_("owner", player.m_20148_());
            mark.m_128356_("expires", target.m_9236_().m_46467_() + 100L);
            target.getPersistentData().m_128365_(MARK_KEY, (Tag)mark);
            target.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 40, 0));
            DandanScytheFxPacket.send(player, target.m_20182_(), 0.0f, 0.65f, 2, target.m_19879_());
        }
        return result;
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.m_6084_() || player.m_5833_() || player.m_36335_().m_41519_((Item)this)) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        player.m_6672_(hand);
        return InteractionResultHolder.m_19096_((Object)stack);
    }

    public int m_8105_(ItemStack stack) {
        return 12;
    }

    public ItemStack m_5922_(ItemStack stack, Level level, LivingEntity user) {
        int action;
        ServerPlayer player;
        if (!(user instanceof ServerPlayer) || !(player = (ServerPlayer)user).m_6084_() || player.m_5833_() || player.m_21205_() != stack || player.m_36335_().m_41519_((Item)this)) {
            return stack;
        }
        ServerPlayer serverPlayer = player;
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (player.m_6144_()) {
            if (!DandanScytheItem.blink(serverPlayer)) {
                return stack;
            }
            action = 5;
            player.m_36335_().m_41524_((Item)this, 200);
            stack.m_41622_(5, (LivingEntity)player, holder -> holder.m_21190_(hand));
        } else {
            action = 4;
            DandanScytheItem.strike(serverPlayer, 4.5, DandanScytheItem.sweepDamage(), false);
            DandanScytheFxPacket.send(serverPlayer, player.m_20182_().m_82520_(0.0, 0.9, 0.0), player.m_146908_(), 4.5f, 0, -1);
            player.m_36335_().m_41524_((Item)this, 120);
            stack.m_41622_(3, (LivingEntity)player, holder -> holder.m_21190_(hand));
            level.m_5594_(null, player.m_20183_(), SoundEvents.f_12317_, SoundSource.PLAYERS, 1.0f, 0.8f);
        }
        player.m_21011_(hand, true);
        player.m_36399_(0.15f);
        DandanScytheFxPacket.send(player, player.m_20182_(), player.m_146908_(), 1.0f, action, player.m_19879_());
        return stack;
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private BlockEntityWithoutLevelRenderer renderer;

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new DandanScytheRenderer();
                }
                return this.renderer;
            }
        });
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

    private static Vec3 horizontalForward(Player player) {
        double radians = Math.toRadians(player.m_146908_());
        return new Vec3(-Math.sin(radians), 0.0, Math.cos(radians));
    }

    private static boolean visible(ServerPlayer player, LivingEntity target) {
        Vec3 center = target.m_20191_().m_82399_();
        return player.m_284548_().m_45547_(new ClipContext(player.m_146892_(), center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player)).m_6662_() == HitResult.Type.MISS;
    }

    private static void strike(ServerPlayer player, double radius, float damage, boolean fullCircle) {
        ServerLevel level = player.m_284548_();
        List targets = level.m_6443_(LivingEntity.class, player.m_20191_().m_82377_(radius, 1.25, radius), target -> DandanScytheItem.canTarget(player, target));
        targets.sort(Comparator.comparingDouble(arg_0 -> ((ServerPlayer)player).m_20280_(arg_0)));
        int affected = 0;
        for (LivingEntity target2 : targets) {
            Vec3 delta = target2.m_20182_().m_82546_(player.m_20182_()).m_82542_(1.0, 0.0, 1.0);
            if (!DandanScytheRules.withinSweep(delta.f_82479_, delta.f_82481_, player.m_146908_(), radius, fullCircle) || !DandanScytheItem.visible(player, target2)) continue;
            CompoundTag mark = target2.getPersistentData().m_128469_(MARK_KEY);
            boolean marked = mark.m_128403_("owner") && mark.m_128342_("owner").equals(player.m_20148_()) && mark.m_128454_("expires") > level.m_46467_();
            if (!target2.m_6469_(player.m_269291_().m_269075_((Player)player), damage + (marked ? DandanScytheItem.markBonus() : 0.0f))) continue;
            if (marked) {
                target2.getPersistentData().m_128473_(MARK_KEY);
                DandanScytheFxPacket.send(player, target2.m_20191_().m_82399_(), 0.0f, 1.0f, 3, target2.m_19879_());
                level.m_8767_((ParticleOptions)ParticleTypes.f_123810_, target2.m_20185_(), target2.m_20186_() + 1.0, target2.m_20189_(), 9, 0.3, 0.4, 0.3, 0.03);
            }
            target2.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 40, 0));
            target2.m_147240_(0.45, player.m_20185_() - target2.m_20185_(), player.m_20189_() - target2.m_20189_());
            if (++affected < 16) continue;
            break;
        }
    }

    private static boolean blink(ServerPlayer player) {
        Vec3 candidate;
        if (player.m_20159_() || !player.m_20096_() || player.m_21255_() || player.m_6069_()) {
            return DandanScytheItem.blocked(player);
        }
        Vec3 start = player.m_20182_();
        Vec3 forward = DandanScytheItem.horizontalForward((Player)player);
        Vec3 destination = null;
        for (double distance = 0.25; distance <= 6.0 && DandanScytheItem.clearAt(player, candidate = start.m_82549_(forward.m_82490_(distance))); distance += 0.25) {
            if (!(distance >= 1.25) || !DandanScytheItem.supportedAt(player, candidate)) continue;
            destination = candidate;
        }
        if (destination == null) {
            return DandanScytheItem.blocked(player);
        }
        EntityTeleportEvent event = new EntityTeleportEvent((Entity)player, destination.f_82479_, destination.f_82480_, destination.f_82481_);
        if (MinecraftForge.EVENT_BUS.post((Event)event)) {
            return DandanScytheItem.blocked(player);
        }
        Vec3 approved = event.getTarget();
        if (!DandanScytheItem.safePath(player, start, approved)) {
            return DandanScytheItem.blocked(player);
        }
        player.f_8906_.m_9774_(approved.f_82479_, approved.f_82480_, approved.f_82481_, player.m_146908_(), player.m_146909_());
        player.m_20256_(Vec3.f_82478_);
        player.f_19789_ = 0.0f;
        DandanScytheFxPacket.send(player, start.m_82520_(0.0, 0.08, 0.0), 0.0f, 1.1f, 1, -1);
        DandanScytheFxPacket.send(player, approved.m_82520_(0.0, 0.08, 0.0), 0.0f, 2.5f, 1, -1);
        DandanScytheItem.strike(player, 2.5, DandanScytheItem.blinkDamage(), true);
        ServerLevel level = player.m_284548_();
        for (Vec3 position : List.of(start, approved)) {
            level.m_6263_(null, position.f_82479_, position.f_82480_, position.f_82481_, SoundEvents.f_11852_, SoundSource.PLAYERS, 0.65f, 1.25f);
            level.m_8767_((ParticleOptions)ParticleTypes.f_123810_, position.f_82479_, position.f_82480_ + 0.8, position.f_82481_, 14, 0.3, 0.65, 0.3, 0.015);
        }
        return true;
    }

    private static boolean safePath(ServerPlayer player, Vec3 start, Vec3 destination) {
        Vec3 delta = destination.m_82546_(start);
        if (!DandanScytheRules.validBlinkDelta(delta.f_82479_, delta.f_82480_, delta.f_82481_)) {
            return false;
        }
        int steps = (int)Math.ceil(delta.m_82553_() / 0.25);
        for (int step = 1; step <= steps; ++step) {
            if (DandanScytheItem.clearAt(player, start.m_82549_(delta.m_82490_((double)step / (double)steps)))) continue;
            return false;
        }
        return DandanScytheItem.supportedAt(player, destination);
    }

    private static AABB boxAt(ServerPlayer player, Vec3 position) {
        return player.m_20191_().m_82383_(position.m_82546_(player.m_20182_()));
    }

    private static boolean clearAt(ServerPlayer player, Vec3 position) {
        ServerLevel level = player.m_284548_();
        AABB box = DandanScytheItem.boxAt(player, position);
        if (box.f_82289_ < (double)level.m_141937_() || box.f_82292_ >= (double)level.m_151558_() || !level.m_6857_().m_61935_(box)) {
            return false;
        }
        for (BlockPos block : BlockPos.m_121940_((BlockPos)BlockPos.m_274561_((double)box.f_82288_, (double)(box.f_82289_ - 0.2), (double)box.f_82290_), (BlockPos)BlockPos.m_274561_((double)box.f_82291_, (double)box.f_82292_, (double)box.f_82293_))) {
            if (level.m_46805_(block) && !DandanScytheItem.dangerous(level.m_8055_(block))) continue;
            return false;
        }
        return level.m_45756_((Entity)player, box) && !level.m_46855_(box);
    }

    private static boolean supportedAt(ServerPlayer player, Vec3 position) {
        AABB box = DandanScytheItem.boxAt(player, position);
        return !player.m_284548_().m_45756_((Entity)player, new AABB(box.f_82288_ + 0.05, box.f_82289_ - 0.15, box.f_82290_ + 0.05, box.f_82291_ - 0.05, box.f_82289_, box.f_82293_ - 0.05));
    }

    private static boolean dangerous(BlockState state) {
        return state.m_204336_(BlockTags.f_13076_) || state.m_60713_(Blocks.f_50450_) || state.m_60713_(Blocks.f_50128_) || state.m_60713_(Blocks.f_50683_) || state.m_60713_(Blocks.f_50684_) || state.m_60713_(Blocks.f_152499_) || state.m_60713_(Blocks.f_50685_) || state.m_60713_(Blocks.f_50070_) || !state.m_60819_().m_76178_();
    }

    private static boolean blocked(ServerPlayer player) {
        player.m_5661_((Component)Component.m_237115_((String)"message.ftboss.dandan_scythe.blocked").m_130940_(ChatFormatting.GRAY), true);
        return false;
    }

    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.dandan_scythe.mark", (Object[])new Object[]{5, (int)DandanScytheItem.markBonus()}).m_130940_(ChatFormatting.LIGHT_PURPLE));
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.dandan_scythe.sweep", (Object[])new Object[]{4.5, (int)DandanScytheItem.sweepDamage(), 6}).m_130940_(ChatFormatting.GOLD));
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.dandan_scythe.blink", (Object[])new Object[]{6.0, 2.5, (int)DandanScytheItem.blinkDamage(), 10}).m_130940_(ChatFormatting.AQUA));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.dandan_scythe.rules").m_130940_(ChatFormatting.GRAY));
    }
}

