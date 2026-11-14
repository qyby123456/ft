/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.UseAnim
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
 *  software.bernie.geckolib.animatable.GeoItem
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.core.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.core.animation.RawAnimation
 *  software.bernie.geckolib.core.object.PlayState
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.render.ShadowSwordGeoRenderer;
import net.farmingtales.ftboss.item.weapons.BlackCleaverAndShadow;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.ShadowCastManager;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.Fx;
import net.farmingtales.ftboss.util.ManorWeaponBalanceRules;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ShadowSwordItem
extends BlackCleaverAndShadow.ShadowSword
implements GeoItem {
    public static final String TAG_READY = "ftboss_ready";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.ftboss.shadow_sword.idle");
    private static final RawAnimation CAST = RawAnimation.begin().thenPlay("animation.ftboss.shadow_sword.cast");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlay("animation.ftboss.shadow_sword.charge");
    private static final RawAnimation RELEASE = RawAnimation.begin().thenPlay("animation.ftboss.shadow_sword.release");
    private static final RawAnimation RECOVER = RawAnimation.begin().thenPlay("animation.ftboss.shadow_sword.recover");
    private static final RawAnimation CANCEL = RawAnimation.begin().thenPlay("animation.ftboss.shadow_sword.cancel");
    private static final RawAnimation COOLDOWN_READY = RawAnimation.begin().thenPlay("animation.ftboss.shadow_sword.cooldown_ready");
    private static final RawAnimation READY_IDLE = RawAnimation.begin().thenLoop("animation.ftboss.shadow_sword.ready_idle");
    private static final RawAnimation GLOW_FLASH = RawAnimation.begin().thenPlay("animation.ftboss.shadow_sword.glow_flash");
    private static final String CTRL = "skill";
    private static volatile boolean echoPulse;
    private static volatile boolean echoReady;
    private static final Map<UUID, Boolean> WAS_READY;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    public static final float SKY_DAMAGE = 10.0f;
    public static final double SKY_RADIUS = 3.0;
    public static final double SKY_HALF_HEIGHT = 2.5;
    public static final int SKY_BLIND_TICKS = 120;
    private static final double SKY_KNOCKBACK = 1.4;
    private static final int LANDING_SEARCH_DOWN = 3;

    public ShadowSwordItem() {
        GeoItem.registerSyncedAnimatable((GeoAnimatable)this);
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "weapon_controller", 5, state -> {
            if (echoPulse) {
                echoPulse = false;
                return state.setAndContinue(COOLDOWN_READY);
            }
            return state.setAndContinue(echoReady ? READY_IDLE : IDLE);
        })});
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, CTRL, 0, state -> PlayState.STOP).triggerableAnim("cast", CAST).triggerableAnim("charge", CHARGE).triggerableAnim("release", RELEASE).triggerableAnim("recover", RECOVER).triggerableAnim("cancel", CANCEL).triggerableAnim("glow", GLOW_FLASH)});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public void triggerSkillAnim(Entity holder, ItemStack stack, String name) {
        Level level = holder.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            this.triggerAnim(holder, GeoItem.getOrAssignId((ItemStack)stack, (ServerLevel)sl), CTRL, name);
        }
    }

    public void m_6883_(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.m_6883_(stack, level, entity, slot, selected);
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        if (!level.f_46443_) {
            if (player instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)player;
                boolean cooling = sp.m_36335_().m_41519_((Item)this);
                boolean ready = !cooling;
                CompoundTag tag = stack.m_41784_();
                if (tag.m_128471_(TAG_READY) != ready) {
                    tag.m_128379_(TAG_READY, ready);
                }
            }
            return;
        }
        boolean ready = stack.m_41782_() && stack.m_41783_().m_128471_(TAG_READY);
        UUID id = player.m_20148_();
        boolean was = Boolean.TRUE.equals(WAS_READY.get(id));
        if (ready && !was) {
            echoPulse = true;
            ShadowSwordItem.spawnEchoParticles(player, level);
        }
        echoReady = ready;
        WAS_READY.put(id, ready);
    }

    public static float skyDamage() {
        return ManorWeaponBalanceRules.skillDamage(10.0f);
    }

    private static double findLandingY(ServerLevel sl, double x, double y, double z) {
        BlockPos base = BlockPos.m_274561_((double)x, (double)y, (double)z);
        for (int dy = 0; dy <= 3; ++dy) {
            BlockPos feet = base.m_6625_(dy);
            if (!sl.m_46859_(feet) || !sl.m_46859_(feet.m_7494_()) || sl.m_46859_(feet.m_7495_())) continue;
            return feet.m_123342_();
        }
        return Double.NaN;
    }

    public static void teleportStrike(ServerPlayer sp, double x, double y, double z) {
        Level lv = sp.m_9236_();
        ServerLevel sl = sp.m_284548_();
        double landY = ShadowSwordItem.findLandingY(sl, x, y, z);
        if (!Double.isNaN(landY)) {
            sp.m_8999_(sl, x, landY, z, sp.m_146908_(), sp.m_146909_());
            sp.m_183634_();
            sp.f_19864_ = true;
            Fx.sound(lv, x, landY, z, SoundEvents.f_11852_, 1.0f, 0.9f);
            sp.m_5661_((Component)Component.m_237113_((String)"\u00a75\u3010\u6697\u5f71\u4f20\u9001\u3011\u00a77\u5df2\u62b5\u8fbe\u843d\u70b9 \u2014\u2014 \u5468\u56f4\u9020\u6210\u8303\u56f4\u9b54\u6cd5\u4f24\u5bb3"), true);
        }
        FTNetwork.sendSigilNear(sp, x, Double.isNaN(landY) ? y : landY, z, true);
        AABB box = new AABB(x - 3.0, y - 2.5, z - 3.0, x + 3.0, y + 2.5, z + 3.0);
        DamageSource src = lv.m_269111_().m_269104_((Entity)sp, (Entity)sp);
        int hit = 0;
        for (LivingEntity e : lv.m_45976_(LivingEntity.class, box)) {
            Player victim;
            boolean hurtable;
            double dz;
            double dx;
            if (e == sp || (dx = e.m_20185_() - x) * dx + (dz = e.m_20189_() - z) * dz > 9.0) continue;
            boolean bl = hurtable = !(e instanceof Player && !sp.m_7099_(victim = (Player)e));
            if (hurtable && e.m_6469_(src, ShadowSwordItem.skyDamage())) {
                ++hit;
            }
            e.m_7292_(new MobEffectInstance(MobEffects.f_19610_, 120, 0));
            Vec3 away = new Vec3(e.m_20185_() - x, 0.0, e.m_20189_() - z);
            away = away.m_82556_() < 1.0E-4 ? new Vec3(sp.m_217043_().m_188500_() - 0.5, 0.0, sp.m_217043_().m_188500_() - 0.5) : away.m_82541_();
            e.m_5997_(away.f_82479_ * 1.4, 0.35, away.f_82481_ * 1.4);
            e.f_19864_ = true;
        }
        WeaponFx.burst(sl, new Vec3(x, y + 0.1, z), 3.0, WeaponFxRules.Theme.SHADOW);
        Fx.particles(lv, x, y + 0.5, z, 10, (ParticleOptions)ParticleTypes.f_123765_, 0.7, 0.08);
        Fx.particles(lv, x, y + 1.2, z, 12, (ParticleOptions)ParticleTypes.f_123789_, 0.6, 0.12);
        Fx.particles(lv, x, y + 0.2, z, 6, (ParticleOptions)ParticleTypes.f_123796_, 1.0, 0.06);
        lv.m_7106_((ParticleOptions)ParticleTypes.f_123747_, x, y + 0.6, z, 0.0, 0.0, 0.0);
        Fx.sound(lv, x, y, z, SoundEvents.f_11668_, 1.1f, 0.55f);
        Fx.sound(lv, x, y, z, SoundEvents.f_11913_, 0.7f, 1.35f);
        ShadowSwordItem.flashGlow(sp);
        FTBossMod.LOGGER.info("[FTBoss][\u6697\u5f71\u4f20\u9001] {} \u843d\u70b9\u7ed3\u7b97: \u547d\u4e2d {} \u4e2a\u76ee\u6807, \u534a\u5f84 {} \u683c / {} \u70b9\u9b54\u6cd5\u4f24\u5bb3", new Object[]{sp.m_36316_().getName(), hit, 3.0, Float.valueOf(10.0f)});
    }

    public static void flashGlow(ServerPlayer sp) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack st = sp.m_21120_(hand);
            Item item = st.m_41720_();
            if (!(item instanceof ShadowSwordItem)) continue;
            ShadowSwordItem sword = (ShadowSwordItem)item;
            sword.triggerSkillAnim((Entity)sp, st, "glow");
        }
    }

    private static void spawnEchoParticles(Player player, Level level) {
        double x = player.m_20185_();
        double y = player.m_20186_() + 1.1;
        double z = player.m_20189_();
        for (int i = 0; i < 8; ++i) {
            double a = (double)i / 8.0 * Math.PI * 2.0;
            level.m_7106_((ParticleOptions)ParticleTypes.f_123789_, x + Math.cos(a) * 0.28, y, z + Math.sin(a) * 0.28, Math.cos(a) * 0.03, 0.02, Math.sin(a) * 0.03);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (player.m_6117_() || player.m_36335_().m_41519_((Item)this)) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        if (level.f_46443_) {
            player.m_6672_(hand);
            return InteractionResultHolder.m_19098_((Object)stack);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        if (!ShadowCastManager.begin(serverPlayer, hand)) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        player.m_6672_(hand);
        return InteractionResultHolder.m_19096_((Object)stack);
    }

    public int m_8105_(ItemStack stack) {
        return 72000;
    }

    public UseAnim m_6164_(ItemStack stack) {
        return UseAnim.NONE;
    }

    public void m_5551_(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!level.f_46443_ && entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)entity;
            ShadowCastManager.release(player, stack);
        }
    }

    public boolean m_7579_(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player) {
            Player player = (Player)attacker;
            if (!player.m_9236_().f_46443_) {
                this.triggerAnim((Entity)player, GeoItem.getOrAssignId((ItemStack)stack, (ServerLevel)((ServerLevel)player.m_9236_())), CTRL, "release");
            }
        }
        return super.m_7579_(stack, target, attacker);
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private ShadowSwordGeoRenderer renderer;

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ShadowSwordGeoRenderer();
                }
                return this.renderer;
            }
        });
    }

    static {
        WAS_READY = new HashMap<UUID, Boolean>();
    }
}

