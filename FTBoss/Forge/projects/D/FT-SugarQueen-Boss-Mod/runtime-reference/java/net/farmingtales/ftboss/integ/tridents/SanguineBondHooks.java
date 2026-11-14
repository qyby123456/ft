/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Remove
 *  net.minecraftforge.event.entity.player.AttackEntityEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickItem
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.registries.ForgeRegistries
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.integ.tridents;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.mixin.SanguineBondEntityAccess;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

public final class SanguineBondHooks {
    public static final ResourceLocation ITEM_ID = new ResourceLocation("tridents_n_stuff", "sanguine_bond");
    public static final ResourceLocation BLEEDING_ID = new ResourceLocation("tridents_n_stuff", "bleeding");
    private static final String SKILL_ONE_READY = "ftboss:SanguineSkillOneReady";
    private static final String SKILL_TWO_READY = "ftboss:SanguineSkillTwoReady";
    private static final int MAX_AMPLIFIER = 4;
    private static final int BLEED_TICKS = 400;
    private static final int SKILL_ONE_COOLDOWN = 160;
    private static final int SKILL_TWO_COOLDOWN = 240;
    private static final DustParticleOptions BLOOD = new DustParticleOptions(new Vector3f(0.82f, 0.035f, 0.08f), 1.35f);

    private SanguineBondHooks() {
    }

    public static boolean isSanguine(ItemStack stack) {
        return stack != null && !stack.m_41619_() && ITEM_ID.equals((Object)ForgeRegistries.ITEMS.getKey((Object)stack.m_41720_()));
    }

    public static boolean isBleeding(MobEffect effect) {
        return effect != null && BLEEDING_ID.equals((Object)ForgeRegistries.MOB_EFFECTS.getKey((Object)effect));
    }

    public static ItemStack entityTridentStack(Entity entity) {
        if (entity instanceof SanguineBondEntityAccess) {
            SanguineBondEntityAccess access = (SanguineBondEntityAccess)entity;
            return access.ftboss$getTridentStack();
        }
        return ItemStack.f_41583_;
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onBloodSkillUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().m_5776_() || event.getHand() != InteractionHand.MAIN_HAND || !event.getEntity().m_6144_() || !SanguineBondHooks.isSanguine(event.getItemStack())) {
            return;
        }
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
        long now = player2.m_9236_().m_46467_();
        long ready = player2.getPersistentData().m_128454_(SKILL_ONE_READY);
        if (ready > now) {
            player2.m_5661_((Component)Component.m_237113_((String)("\u00a7c\u8840\u6f6e\u7a81\u523a\u51b7\u5374\u4e2d \u00a77(" + (ready - now + 19L) / 20L + "\u79d2)")), true);
            return;
        }
        player2.getPersistentData().m_128356_(SKILL_ONE_READY, now + 160L);
        SanguineBondHooks.castBloodLance(player2);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onExecution(AttackEntityEvent event) {
        MobEffectInstance mark;
        LivingEntity target;
        Entity entity;
        Player player = event.getEntity();
        if (!(!player.m_9236_().f_46443_ && player.m_6144_() && SanguineBondHooks.isSanguine(player.m_21205_()) && (entity = event.getTarget()) instanceof LivingEntity && SanguineBondHooks.isValidTarget((LivingEntity)player, target = (LivingEntity)entity))) {
            return;
        }
        MobEffect bleeding = SanguineBondHooks.bleedingEffect();
        MobEffectInstance mobEffectInstance = mark = bleeding == null ? null : target.m_21124_(bleeding);
        if (mark == null) {
            return;
        }
        event.setCanceled(true);
        long now = player.m_9236_().m_46467_();
        long ready = player.getPersistentData().m_128454_(SKILL_TWO_READY);
        if (ready > now) {
            player.m_5661_((Component)Component.m_237113_((String)("\u00a7c\u5951\u7ea6\u5904\u5211\u51b7\u5374\u4e2d \u00a77(" + (ready - now + 19L) / 20L + "\u79d2)")), true);
            return;
        }
        player.getPersistentData().m_128356_(SKILL_TWO_READY, now + 240L);
        SanguineBondHooks.execute(player, target, mark.m_19564_() + 1);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void keepBleedingOnCleanse(MobEffectEvent.Remove event) {
        if (SanguineBondHooks.isBleeding(event.getEffect())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onSanguineDamage(LivingDamageEvent event) {
        Level living;
        ItemStack held;
        Entity attacker;
        if (event.getEntity().m_9236_().f_46443_ || event.getAmount() <= 0.0f) {
            return;
        }
        Entity direct = event.getSource().m_7640_();
        ItemStack stack = SanguineBondHooks.entityTridentStack(direct);
        if (!SanguineBondHooks.isSanguine(stack) && (attacker = event.getSource().m_7639_()) instanceof LivingEntity && SanguineBondHooks.isSanguine(held = (living = (LivingEntity)attacker).m_21205_())) {
            stack = held;
        }
        if (!SanguineBondHooks.isSanguine(stack)) {
            return;
        }
        living = event.getEntity().m_9236_();
        if (living instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)living;
            LivingEntity victim = event.getEntity();
            level.m_8767_((ParticleOptions)BLOOD, victim.m_20185_(), victim.m_20227_(0.65), victim.m_20189_(), 10, 0.28, 0.38, 0.28, 0.05);
            level.m_8767_((ParticleOptions)ParticleTypes.f_123798_, victim.m_20185_(), victim.m_20227_(0.65), victim.m_20189_(), 5, 0.22, 0.32, 0.22, 0.1);
            level.m_5594_(null, victim.m_20183_(), SoundEvents.f_12313_, SoundSource.PLAYERS, 0.65f, 0.72f);
        }
    }

    public static void applyBleeding(LivingEntity target, int extraStacks) {
        MobEffect effect = SanguineBondHooks.bleedingEffect();
        if (effect == null || target.m_9236_().f_46443_) {
            return;
        }
        MobEffectInstance old = target.m_21124_(effect);
        int oldAmp = old == null ? -1 : old.m_19564_();
        int amp = Math.min(4, oldAmp + Math.max(1, extraStacks));
        target.m_7292_(new MobEffectInstance(effect, 400, amp));
    }

    private static MobEffect bleedingEffect() {
        return (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(BLEEDING_ID);
    }

    private static void castBloodLance(ServerPlayer player) {
        Vec3 end;
        Vec3 start;
        ServerLevel level = player.m_284548_();
        BlockHitResult clip = level.m_45547_(new ClipContext(start = player.m_146892_(), end = start.m_82549_(player.m_20154_().m_82490_(18.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
        if (clip.m_6662_() != HitResult.Type.MISS) {
            end = clip.m_82450_();
        }
        int hits = 0;
        AABB area = new AABB(start, end).m_82400_(1.25);
        for (LivingEntity target : level.m_6443_(LivingEntity.class, area, candidate -> SanguineBondHooks.isValidTarget((LivingEntity)player, candidate))) {
            if (SanguineBondHooks.distanceToSegment(target.m_20191_().m_82399_(), start, end) > 1.35 || !player.m_142582_((Entity)target)) continue;
            if (target.m_6469_(level.m_269111_().m_269104_((Entity)player, (Entity)player), 8.0f)) {
                SanguineBondHooks.applyBleeding(target, 1);
                ++hits;
                SanguineBondHooks.bloodBurst(level, target.m_20182_().m_82520_(0.0, (double)target.m_20206_() * 0.6, 0.0), 8);
            }
            if (hits < 16) continue;
            break;
        }
        for (int i = 0; i <= 28; ++i) {
            Vec3 point = start.m_165921_(end, (double)i / 28.0);
            level.m_8767_((ParticleOptions)BLOOD, point.f_82479_, point.f_82480_, point.f_82481_, 2, 0.04, 0.04, 0.04, 0.01);
        }
        level.m_8767_((ParticleOptions)ParticleTypes.f_123766_, end.f_82479_, end.f_82480_, end.f_82481_, 3, 0.2, 0.2, 0.2, 0.0);
        level.m_8767_((ParticleOptions)ParticleTypes.f_123798_, end.f_82479_, end.f_82480_, end.f_82481_, 12, 0.45, 0.45, 0.45, 0.08);
        level.m_5594_(null, player.m_20183_(), SoundEvents.f_12520_, SoundSource.PLAYERS, 0.9f, 1.3f);
        FTBossMod.LOGGER.info("[FTBoss][SanguineBond] skill1 blood lance player={} hits={}", (Object)player.m_7755_().getString(), (Object)hits);
    }

    private static void execute(Player player, LivingEntity target, int stacks) {
        float damage = 10.0f + (float)Math.min(5, stacks) * 3.0f;
        if (!target.m_6469_(player.m_9236_().m_269111_().m_269075_(player), damage)) {
            return;
        }
        player.m_5634_(Math.min(8.0f, damage * 0.25f));
        Level level = player.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            Vec3 center = target.m_20182_().m_82520_(0.0, (double)target.m_20206_() * 0.55, 0.0);
            SanguineBondHooks.bloodBurst(level2, center, 28);
            level2.m_8767_((ParticleOptions)ParticleTypes.f_123766_, center.f_82479_, center.f_82480_, center.f_82481_, 8, 0.55, 0.55, 0.55, 0.0);
            level2.m_5594_(null, target.m_20183_(), SoundEvents.f_12514_, SoundSource.PLAYERS, 1.0f, 0.65f);
        }
        FTBossMod.LOGGER.info("[FTBoss][SanguineBond] skill2 execution player={} target={} stacks={}", new Object[]{player.m_7755_().getString(), target.m_7755_().getString(), stacks});
    }

    private static void bloodBurst(ServerLevel level, Vec3 center, int count) {
        level.m_8767_((ParticleOptions)BLOOD, center.f_82479_, center.f_82480_, center.f_82481_, count, 0.45, 0.55, 0.45, 0.12);
        level.m_8767_((ParticleOptions)ParticleTypes.f_123797_, center.f_82479_, center.f_82480_, center.f_82481_, count / 2, 0.36, 0.45, 0.36, 0.08);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static boolean isValidTarget(LivingEntity source, LivingEntity target) {
        if (target == source) return false;
        if (!target.m_6084_()) return false;
        if (target.m_213877_()) return false;
        if (target instanceof ArmorStand) return false;
        if (source.m_7307_((Entity)target)) return false;
        if (target.m_7307_((Entity)source)) {
            return false;
        }
        if (target instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)target;
            if (player.m_7500_()) return false;
            if (player.m_5833_()) {
                return false;
            }
        }
        if (!(source instanceof Player)) return true;
        Player player = (Player)source;
        if (!(target instanceof Player)) return true;
        Player other = (Player)target;
        if (!player.m_7099_(other)) return false;
        return true;
    }

    private static double distanceToSegment(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 delta = end.m_82546_(start);
        double lengthSquared = delta.m_82556_();
        if (lengthSquared < 1.0E-7) {
            return point.m_82554_(start);
        }
        double t = point.m_82546_(start).m_82526_(delta) / lengthSquared;
        t = Math.max(0.0, Math.min(1.0, t));
        return point.m_82554_(start.m_165921_(end, t));
    }
}

