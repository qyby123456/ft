/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
 *  software.bernie.geckolib.animatable.GeoItem
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.core.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.core.animation.RawAnimation
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.render.BlackCleaverGeoRenderer;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.item.weapons.BlackCleaverAndShadow;
import net.farmingtales.ftboss.item.weapons.CleaverCombat;
import net.farmingtales.ftboss.item.weapons.CleaverSkillController;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BlackCleaverItem
extends BlackCleaverAndShadow.BlackCleaver
implements GeoItem {
    public static final String TAG_BLOOD = "ftboss_blood";
    public static final String TAG_BLOOD_LV = "ftboss_blood_lv";
    public static final int PARTICLE_INTERVAL = 5;
    private static final String CTRL = "cleaver_skill";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.black_cleaver.idle");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlay("animation.black_cleaver.charge");
    private static final RawAnimation SLAM = RawAnimation.begin().thenPlay("animation.black_cleaver.slam").thenPlay("animation.black_cleaver.recover");
    private static final RawAnimation BLOOD_SLAM = RawAnimation.begin().thenPlay("animation.black_cleaver.blood_slam").thenPlay("animation.black_cleaver.blood_recover");
    private static final RawAnimation BLOOD_RECOVER = RawAnimation.begin().thenPlay("animation.black_cleaver.blood_recover");
    private static final RawAnimation RECOVER = RawAnimation.begin().thenPlay("animation.black_cleaver.recover");
    private static final RawAnimation READY = RawAnimation.begin().thenPlay("animation.black_cleaver.ready");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public BlackCleaverItem() {
        GeoItem.registerSyncedAnimatable((GeoAnimatable)this);
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "weapon_controller", 5, state -> state.setAndContinue(IDLE))});
        controllers.add(new AnimationController[]{new CleaverSkillController(this).triggerableAnim("charge", CHARGE).triggerableAnim("slam", SLAM).triggerableAnim("recover", RECOVER).triggerableAnim("blood_slam", BLOOD_SLAM).triggerableAnim("blood_recover", BLOOD_RECOVER).triggerableAnim("ready", READY)});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public void triggerCleaverAnim(Entity holder, ItemStack stack, String name) {
        Level level = holder.m_9236_();
        if (level instanceof ServerLevel) {
            LivingEntity living;
            ServerLevel sl = (ServerLevel)level;
            boolean blood = holder instanceof LivingEntity && (living = (LivingEntity)holder).m_21023_((MobEffect)FTEffects.BLOOD_RAGE_FAKE.get());
            String action = blood && (name.equals("slam") || name.equals("recover")) ? "blood_" + name : name;
            this.triggerAnim(holder, GeoItem.getOrAssignId((ItemStack)stack, (ServerLevel)sl), CTRL, action);
        }
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        return CleaverCombat.use(level, player, hand);
    }

    public void m_6883_(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        boolean held;
        CompoundTag tag;
        MobEffectInstance inst;
        Player player;
        block8: {
            block7: {
                super.m_6883_(stack, level, entity, slot, selected);
                if (level.f_46443_ || !(entity instanceof Player)) break block7;
                player = (Player)entity;
                if (level instanceof ServerLevel) break block8;
            }
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        boolean blood = false;
        int stacks = 0;
        MobEffect eff = (MobEffect)FTEffects.BLOOD_RAGE_FAKE.get();
        if (eff != null && (inst = player.m_21124_(eff)) != null) {
            blood = true;
            stacks = inst.m_19564_() + 1;
        }
        if ((tag = stack.m_41784_()).m_128471_(TAG_BLOOD) != blood) {
            tag.m_128379_(TAG_BLOOD, blood);
            FTBossMod.LOGGER.info("[FTBoss][\u9ed1\u5207] {} \u7684\u8840\u6012\u6807\u8bb0 {} -> {}\uff08\u5c42\u6570 {}\uff09", new Object[]{player.m_7755_().getString(), !blood, blood, stacks});
        }
        if (tag.m_128451_(TAG_BLOOD_LV) != stacks) {
            tag.m_128405_(TAG_BLOOD_LV, stacks);
        }
        GeoItem.getOrAssignId((ItemStack)stack, (ServerLevel)sl);
        boolean bl = held = selected || player.m_21206_() == stack;
        if (blood && held && sl.m_46467_() % 5L == 0L) {
            BlackCleaverItem.spawnBloodParticle(sl, player, stacks);
        }
    }

    private static void spawnBloodParticle(ServerLevel sl, Player player, int stacks) {
        Vec3 look = player.m_20154_();
        double x = player.m_20185_() + look.f_82479_ * 0.45;
        double y = player.m_20186_() + 1.05 + (player.m_217043_().m_188500_() - 0.5) * 0.35;
        double z = player.m_20189_() + look.f_82481_ * 0.45;
        int count = stacks >= 5 ? 2 : 1;
        sl.m_8767_((ParticleOptions)ParticleTypes.f_123784_, x, y, z, count, 0.12, 0.12, 0.12, 0.0);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int stacks;
        super.m_7373_(stack, level, tooltip, flag);
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.black_cleaver.slam").m_130940_(ChatFormatting.GOLD));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.black_cleaver.rage_slam").m_130940_(ChatFormatting.DARK_RED));
        int n = stacks = stack.m_41782_() ? stack.m_41783_().m_128451_(TAG_BLOOD_LV) : 0;
        if (stacks > 0) {
            tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.black_cleaver.blood", (Object[])new Object[]{stacks}).m_130940_(ChatFormatting.DARK_RED));
        }
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        FTBossMod.LOGGER.info("[FTBoss][\u9ed1\u5207] \u8c03\u7528\u94fe\u2460 Item#initializeClient \u88ab Forge \u8c03\u7528\u4e86");
        consumer.accept(new IClientItemExtensions(){
            private BlackCleaverGeoRenderer renderer;

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    FTBossMod.LOGGER.info("[FTBoss][\u9ed1\u5207] \u8c03\u7528\u94fe\u2461 getCustomRenderer \u88ab\u8c03\u7528 -> \u6784\u9020 GeoItemRenderer");
                    this.renderer = new BlackCleaverGeoRenderer();
                }
                return this.renderer;
            }
        });
    }
}

