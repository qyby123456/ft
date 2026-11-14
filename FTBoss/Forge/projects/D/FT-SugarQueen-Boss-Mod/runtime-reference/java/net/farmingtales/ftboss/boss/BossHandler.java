/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.ThrownPotion
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.DoublePlantBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.DoubleBlockHalf
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.EntityLeaveLevelEvent
 *  net.minecraftforge.event.entity.living.LivingChangeTargetEvent
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingEntityUseItemEvent$Finish
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Applicable
 *  net.minecraftforge.event.entity.player.PlayerEvent$Clone
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$LeftClickBlock
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.boss.BossAdvancements;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.HardcoreBossSafety;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.MeatBoss;
import net.farmingtales.ftboss.boss.SparrowSupport;
import net.farmingtales.ftboss.boss.SugarQueenBoss;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.boss.ai.BossEmpower;
import net.farmingtales.ftboss.boss.ai.BossMercy;
import net.farmingtales.ftboss.boss.ai.BossScaling;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.LemonSacrifice;
import net.farmingtales.ftboss.boss.ai.ManorChaseTracker;
import net.farmingtales.ftboss.boss.ai.ManorFlyBan;
import net.farmingtales.ftboss.boss.ai.RatSwarmEncounter;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.sound.BossMusic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class BossHandler {
    private static final float MINION_DAMAGE_TAKEN = 0.6f;
    private static MobEffect[] ratDebuffPool;
    public static final boolean IMMUNE_TO_DEBUFFS = false;
    private static final Set<String> armamentariumBlockLogged;
    private static final Map<UUID, Integer> FLY_BAN_CD;
    public static final boolean FLY_BAN_ENABLED = false;
    public static final MobEffect[] BEACON_EFFECTS;
    private static final String[] TENDON_HINTS;
    public static final String BUILD_TAG = "easter-v155-sword-flat-face-turkey-display-java";
    public static final String[] BLOODY_CONTRACT;

    public BossHandler() {
        MinecraftForge.EVENT_BUS.register(ManorChaseTracker.class);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            int n;
            SugarQueenBoss.tick();
            MeatBoss.tick();
            MinecraftServer server = e.getServer();
            HardcoreBossSafety.tickGrace(server);
            if (server != null && server.m_129921_() % 40 == 0) {
                BossBars.sweep(server);
                BossMusic.sweep(server);
                ManorFight.recoverNear(server);
            }
            if (server != null && server.m_129921_() % 20 == 0) {
                BossBars.forEach(server, le -> {
                    BossScaling.tick(le);
                    BossEmpower.tick(le);
                });
            }
            if (server != null && server.m_129921_() % 10 == 0) {
                BossAggro.tick(server);
                BossBars.forEach(server, BossTargets::dropInvalidTarget);
            }
            if (server != null && server.m_129921_() % 200 == 0 && (n = MeatBoss.purgeStrays(server)) > 0) {
                FTBossMod.LOGGER.info("[FTBoss] \u5b9a\u671f\u6e05\u7406\u65e0\u4e3b\u5c60\u592b\u5b9e\u4f53 {} \u53ea", (Object)n);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerAttackGrubCurse(LivingHurtEvent e) {
        if (e.getEntity().m_9236_().f_46443_) {
            return;
        }
        Entity source = BossHandler.sourceCaster(e.getSource());
        if (!(source instanceof LivingEntity)) {
            return;
        }
        LivingEntity attacker = (LivingEntity)source;
        if (e.getEntity() == attacker) {
            return;
        }
        MobEffectInstance inst = attacker.m_21124_((MobEffect)FTEffects.INFECTION.get());
        if (inst == null) {
            return;
        }
        float dmg = inst.m_19564_() + 1;
        attacker.f_19802_ = 0;
        attacker.m_6469_(attacker.m_269291_().m_269425_(), dmg);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onBossHurtRetaliate(LivingDamageEvent e) {
        if (e.getEntity().m_9236_().f_46443_) {
            return;
        }
        BossAggro.record(e.getEntity(), BossHandler.sourceCaster(e.getSource()), e.getAmount(), e.getEntity().m_9236_().m_46467_());
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onBossChangeTarget(LivingChangeTargetEvent event) {
        Mob boss;
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Mob) || (boss = (Mob)livingEntity).m_21525_()) {
            return;
        }
        LivingEntity target = BossAggro.highest((LivingEntity)boss);
        if (target != null) {
            event.setNewTarget(target);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onConsumeForThreat(LivingEntityUseItemEvent.Finish event) {
        BossAggro.consume(event.getEntity(), event.getItem());
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onPotionForThreat(EntityJoinLevelEvent event) {
        ThrownPotion potion;
        if (event.getLevel().f_46443_ || event.loadedFromDisk()) {
            return;
        }
        BossAdvancements.onBossSummoned(event.getEntity());
        Entity entity = event.getEntity();
        if (entity instanceof ThrownPotion && (entity = (potion = (ThrownPotion)entity).m_19749_()) instanceof LivingEntity) {
            LivingEntity owner = (LivingEntity)entity;
            BossAggro.consume(owner, potion.m_7846_());
        }
    }

    @SubscribeEvent
    public void onThreatEntityLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().f_46443_) {
            BossAggro.forgetEntity(event.getEntity().m_20148_());
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onThreatDeath(LivingDeathEvent event) {
        if (!event.getEntity().m_9236_().f_46443_) {
            BossAggro.forgetEntity(event.getEntity().m_20148_());
        }
    }

    @SubscribeEvent
    public void onThreatServerStopped(ServerStoppedEvent event) {
        BossAggro.clear();
        HardcoreBossSafety.clearGrace();
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock e) {
        if (e.getLevel().f_46443_) {
            return;
        }
        Player player = e.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer sp = (ServerPlayer)player;
        Level level = e.getLevel();
        BlockPos pos = e.getPos();
        BlockState st = level.m_8055_(pos);
        Block block = st.m_60734_();
        if (!(block instanceof CandyStatueBlock)) {
            return;
        }
        CandyStatueBlock statue = (CandyStatueBlock)block;
        if (statue.ownTint() <= 0) {
            return;
        }
        int tint = BossHandler.tintFor(statue, e.getItemStack());
        if (tint <= 0) {
            if (BossHandler.isSummonItem(e.getItemStack())) {
                sp.m_5661_((Component)Component.m_237113_((String)"\u00a7c\u8fd9\u4e2a\u53ec\u5524\u7269\u548c\u8fd9\u5ea7\u77f3\u50cf\u4e0d\u5339\u914d"), true);
                e.setCanceled(true);
            }
            return;
        }
        boolean toWhite = !CandyStatueBlock.isWhite(st) && !CandyStatueBlock.isGold(st);
        DoubleBlockHalf half = (DoubleBlockHalf)st.m_61143_((Property)DoublePlantBlock.f_52858_);
        BlockPos other = half == DoubleBlockHalf.LOWER ? pos.m_7494_() : pos.m_7495_();
        UnaryOperator apply = s -> (BlockState)((BlockState)((BlockState)s.m_61124_((Property)CandyStatueBlock.WHITE, (Comparable)Boolean.valueOf(toWhite))).m_61124_((Property)CandyStatueBlock.GOLD, (Comparable)Boolean.valueOf(false))).m_61124_((Property)CandyStatueBlock.TINT, (Comparable)Integer.valueOf(toWhite ? 0 : tint));
        level.m_7731_(pos, (BlockState)apply.apply(st), 3);
        BlockState os = level.m_8055_(other);
        if (os.m_60734_() == st.m_60734_()) {
            level.m_7731_(other, (BlockState)apply.apply(os), 3);
        }
        e.setCanceled(true);
        level.m_5594_(null, pos, SoundEvents.f_144243_, SoundSource.BLOCKS, 0.9f, toWhite ? 0.9f : 1.3f);
        sp.m_5661_((Component)Component.m_237113_((String)(toWhite ? "\u00a77\u77f3\u50cf\u5df2\u6062\u590d\u767d\u819c\uff08\u518d\u5de6\u952e\u4e00\u6b21\u53ef\u91cd\u65b0\u4e0a\u8272\uff09" : "\u00a7a\u77f3\u50cf\u5df2\u6309\u5bf9\u5e94 Boss \u7684\u914d\u8272\u4e0a\u8272")), true);
    }

    private static int tintFor(CandyStatueBlock statue, ItemStack held) {
        int own = statue.ownTint();
        Item want = switch (own) {
            case 1 -> (Item)FTBossItems.SUGAR_QUEEN_CRYSTAL.get();
            case 2 -> (Item)FTBossItems.MEAT_HEART.get();
            case 3 -> (Item)FTBossItems.TEA_SEED.get();
            case 4 -> (Item)FTBossItems.WINE_CHARM.get();
            case 5 -> (Item)FTBossItems.RAT_BAIT.get();
            case 6 -> (Item)FTBossItems.DEFECT_CORE.get();
            case 7 -> (Item)FTBossItems.COMPOST_SUMMON.get();
            case 8 -> (Item)FTBossItems.FORBIDDEN_APPLE.get();
            case 9 -> (Item)FTBossItems.TIDE_BANQUET.get();
            case 10 -> (Item)FTBossItems.BENGER_WHISTLE.get();
            default -> null;
        };
        return want != null && held.m_150930_(want) ? own : 0;
    }

    private static boolean isSummonItem(ItemStack held) {
        if (held.m_150930_((Item)FTBossItems.BENGER_WHISTLE.get())) {
            return true;
        }
        if (held.m_150930_((Item)FTBossItems.TIDE_BANQUET.get())) {
            return true;
        }
        return held.m_150930_((Item)FTBossItems.SUGAR_QUEEN_CRYSTAL.get()) || held.m_150930_((Item)FTBossItems.MEAT_HEART.get()) || held.m_150930_((Item)FTBossItems.TEA_SEED.get()) || held.m_150930_((Item)FTBossItems.WINE_CHARM.get()) || held.m_150930_((Item)FTBossItems.RAT_BAIT.get()) || held.m_150930_((Item)FTBossItems.DEFECT_CORE.get()) || held.m_150930_((Item)FTBossItems.COMPOST_SUMMON.get()) || held.m_150930_((Item)FTBossItems.FORBIDDEN_APPLE.get());
    }

    /*
     * WARNING - void declaration
     */
    @SubscribeEvent
    public void onRatSwarmHurt(LivingHurtEvent livingHurtEvent) {
        void e;
        if (RatSwarmEncounter.skipSharedHealth(livingHurtEvent)) {
            return;
        }
        if (e.getEntity().m_9236_().f_46443_) {
            return;
        }
        LivingEntity livingEntity = e.getEntity();
        if (!(livingEntity instanceof RatBossEntity)) {
            return;
        }
        RatBossEntity victim = (RatBossEntity)livingEntity;
        if (!victim.isMinion()) {
            return;
        }
        EasterBossEntity lemon = LemonSacrifice.ratLeader((Entity)victim);
        if (lemon != null) {
            e.setCanceled(true);
            victim.m_21153_(victim.m_21233_());
            lemon.m_6469_(e.getSource(), e.getAmount() * 0.6f);
            return;
        }
        RatBossEntity leader = null;
        for (RatBossEntity r : victim.m_9236_().m_45976_(RatBossEntity.class, victim.m_20191_().m_82400_(160.0))) {
            if (r.isMinion() || !r.m_6084_()) continue;
            leader = r;
            break;
        }
        if (leader == null) {
            return;
        }
        e.setCanceled(true);
        victim.m_21153_(victim.m_21233_());
        leader.m_6469_(e.getSource(), e.getAmount() * 0.6f);
    }

    @SubscribeEvent
    public void onAllyFriendlyFire(LivingHurtEvent e) {
        if (e.getEntity().m_9236_().f_46443_) {
            return;
        }
        if (!BossAggro.isBossSide((Entity)e.getEntity())) {
            return;
        }
        Entity attacker = BossHandler.sourceCaster(e.getSource());
        if (attacker == null || attacker instanceof Player) {
            return;
        }
        if (attacker == e.getEntity()) {
            e.setCanceled(true);
            return;
        }
        if (!BossAggro.isBossSide(attacker)) {
            return;
        }
        e.setCanceled(true);
    }

    private static Entity sourceCaster(DamageSource src) {
        if (src == null) {
            return null;
        }
        Entity e = src.m_7639_();
        if (e == null) {
            e = src.m_7640_();
        }
        return BossHandler.rootOwner(e, 0);
    }

    private static Entity rootOwner(Entity e, int depth) {
        Projectile p;
        if (e == null || depth > 4) {
            return e;
        }
        if (e instanceof Projectile && (p = (Projectile)e).m_19749_() != null) {
            return BossHandler.rootOwner(p.m_19749_(), depth + 1);
        }
        try {
            Entity owner;
            Method m = e.getClass().getMethod("getSummoner", new Class[0]);
            Object o = m.invoke(e, new Object[0]);
            if (o instanceof Entity && (owner = (Entity)o) != e) {
                return BossHandler.rootOwner(owner, depth + 1);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return e;
    }

    private static boolean isBossSide(Entity e) {
        return BossAggro.isBossSide(e);
    }

    private static MobEffect[] ratDebuffPool() {
        String[][] ARM;
        if (ratDebuffPool != null) {
            return ratDebuffPool;
        }
        ArrayList<MobEffect> pool = new ArrayList<MobEffect>();
        pool.add(MobEffects.f_19614_);
        pool.add(MobEffects.f_19613_);
        pool.add(MobEffects.f_19597_);
        pool.add(MobEffects.f_19599_);
        pool.add(MobEffects.f_19612_);
        pool.add(MobEffects.f_19590_);
        pool.add(MobEffects.f_19615_);
        for (String[] a : ARM = new String[][]{{"vulnerable"}, {"fatigue"}, {"sluggish"}, {"fragile"}, {"impotent"}, {"lethargy"}, {"pure_venom"}, {"blood_loss"}, {"clipped"}, {"damnified"}, {"sedated"}, {"restrained"}, {"malnourished"}, {"unsatiated"}, {"hypothermia"}, {"vulnerable"}}) {
            MobEffect fx = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("armamentarium", a[0]));
            if (fx == null) continue;
            pool.add(fx);
        }
        ratDebuffPool = pool.toArray(new MobEffect[0]);
        FTBossMod.LOGGER.info("[FTBoss] \u9f20\u7fa4\u968f\u673adebuff\u6c60\u5c31\u7eea: {} \u79cd", (Object)ratDebuffPool.length);
        return ratDebuffPool;
    }

    public static void applyRandomRatDebuff(ServerPlayer p) {
        MobEffect[] pool = BossHandler.ratDebuffPool();
        if (pool.length == 0) {
            return;
        }
        MobEffect fx = pool[p.m_217043_().m_188503_(pool.length)];
        int amp = p.m_217043_().m_188503_(2);
        int dur = 140 + p.m_217043_().m_188503_(120);
        p.m_7292_(new MobEffectInstance(fx, dur, amp, false, true, true));
    }

    /*
     * WARNING - void declaration
     */
    @SubscribeEvent
    public void onBossHitPlayer(LivingHurtEvent livingHurtEvent) {
        void e;
        if (RatSwarmEncounter.handlePlayerHit(livingHurtEvent)) {
            return;
        }
        LivingEntity livingEntity = e.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer)livingEntity;
            if (!p.m_9236_().f_46443_) {
                Entity src = e.getSource().m_7639_();
                BossMercy.noteThreat(p, src != null ? src : e.getSource().m_7640_(), p.m_9236_().m_46467_());
                if (SugarQueenBoss.isBoss(src)) {
                    p.m_7292_(new MobEffectInstance(MobEffects.f_19612_, 80, 1));
                    p.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 60, 0));
                } else if (MeatBoss.isBoss(src)) {
                    e.setAmount(MeatBoss.onMeatHitPlayer(p, e.getAmount()));
                } else if (src instanceof RatBossEntity) {
                    BossHandler.applyRandomRatDebuff(p);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEffectApplicable(MobEffectEvent.Applicable e) {
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onHardcoreManorGraceDamage(LivingHurtEvent event) {
        HardcoreBossSafety.cancelManorGraceDamage(event);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onHardcorePlayerFatal(LivingDeathEvent event) {
        HardcoreBossSafety.beforePlayerDeath(event);
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent e) {
        LivingEntity livingEntity = e.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)livingEntity;
            if (!sp.m_9236_().f_46443_) {
                BossBars.forgetPlayer(sp);
                if (!sp.f_8924_.m_7035_()) {
                    BossMercy.onPlayerDeath(sp);
                }
            }
        }
    }

    @SubscribeEvent
    public void onBossDeath(LivingDeathEvent e) {
        LivingEntity livingEntity = e.getEntity();
        if (livingEntity instanceof Mob) {
            Mob m = (Mob)livingEntity;
            if (!e.getEntity().m_9236_().f_46443_) {
                if (m instanceof TeaTreeBossEntity) {
                    TeaTreeBossEntity tt = (TeaTreeBossEntity)m;
                    if (tt.tryRevive()) {
                        e.setCanceled(true);
                        return;
                    }
                    BossMercy.onBossDefeated((LivingEntity)m);
                    tt.dropRewards();
                    return;
                }
                if (SugarQueenBoss.tryPopTotem(m)) {
                    e.setCanceled(true);
                    return;
                }
                if (MeatBoss.tryPopTotem(m)) {
                    e.setCanceled(true);
                    return;
                }
                BossMercy.onBossDefeated((LivingEntity)m);
                SugarQueenBoss.onBossDeath(m);
                MeatBoss.onBossDeath(m);
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent e) {
        MeatMountainEntity m;
        Entity ent = e.getEntity();
        if (e.getLevel().m_5776_()) {
            if (ent instanceof MeatMountainEntity) {
                MeatMountainEntity m2 = (MeatMountainEntity)ent;
                FTBossMod.LOGGER.info("[FTBoss][\u5c60\u592b\u8bca\u65ad] \u5ba2\u6237\u7aef\u6536\u5230\u5c60\u592b\u5b9e\ufffd? id={} \u4f4d\u7f6e=({}, {}, {}) \u8840\ufffd?{}/{}", new Object[]{m2.m_19879_(), String.format("%.1f", m2.m_20185_()), String.format("%.1f", m2.m_20186_()), String.format("%.1f", m2.m_20189_()), Float.valueOf(m2.m_21223_()), Float.valueOf(m2.m_21233_())});
            }
            return;
        }
        if (ent instanceof MeatMountainEntity && !MeatBoss.isBoss((Entity)(m = (MeatMountainEntity)ent)) && !m.isCloneButcher()) {
            m.hideBossBar();
            m.m_146870_();
            FTBossMod.LOGGER.info("[FTBoss] \u5df2\u6e05\u7406\u65e0\u4e3b\u5c60\u592b\u5b9e\ufffd? \u4f4d\u7f6e=({}, {}, {}) \u8840\ufffd?{}/{}", new Object[]{String.format("%.1f", m.m_20185_()), String.format("%.1f", m.m_20186_()), String.format("%.1f", m.m_20189_()), Float.valueOf(m.m_21223_()), Float.valueOf(m.m_21233_())});
            return;
        }
        if (!ent.getClass().getName().endsWith("spectral_hammer.SpectralHammer")) {
            return;
        }
        try {
            Field f = ent.getClass().getDeclaredField("owner");
            f.setAccessible(true);
            Object owner = f.get(ent);
            if (owner == null) {
                ent.m_146870_();
                FTBossMod.LOGGER.info("[FTBoss] \ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\u9d38(\ufffd\ufffd\u05b9\ufffd\ufffd\ufffd\ufffd)");
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @SubscribeEvent
    public void onLoggedIn(PlayerEvent.PlayerLoggedInEvent e) {
        Player player = e.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            ManorFight.reattach(sp);
        }
    }

    @SubscribeEvent
    public void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent e) {
        Player player = e.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            BossBars.forgetPlayer(sp);
            if (SugarQueenBoss.isActive() && sp.m_20148_().equals(SugarQueenBoss.ownerIdOf())) {
                SugarQueenBoss.cleanup();
                FTBossMod.LOGGER.info("[FTBoss] \u4e3b\u4eba\u9000\u51fa\uff0c\u7cd6\u971c\u5973\u7687\u6d88\u6563");
            }
            if (MeatBoss.isActive() && sp.m_20148_().equals(MeatBoss.ownerIdOf())) {
                MeatBoss.cleanup();
                FTBossMod.LOGGER.info("[FTBoss] \u4e3b\u4eba\u9000\u51fa\uff0c\u5c60\u592b\u6d88\u6563");
            }
        }
    }

    @SubscribeEvent
    public void onEntityLeaveLevel(EntityLeaveLevelEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof MeatMountainEntity) {
            MeatMountainEntity m = (MeatMountainEntity)entity;
            BossBars.untrack((Entity)m, m.getBossBar());
        } else {
            entity = e.getEntity();
            if (entity instanceof RatBossEntity) {
                RatBossEntity r = (RatBossEntity)entity;
                BossBars.untrack((Entity)r, r.getBossBar());
            } else {
                entity = e.getEntity();
                if (entity instanceof TeaTreeBossEntity) {
                    TeaTreeBossEntity t = (TeaTreeBossEntity)entity;
                    BossBars.untrack((Entity)t, t.getBossBar());
                } else {
                    entity = e.getEntity();
                    if (entity instanceof WineBarrelBossEntity) {
                        WineBarrelBossEntity w = (WineBarrelBossEntity)entity;
                        BossBars.untrack((Entity)w, w.getBossBar());
                    } else {
                        entity = e.getEntity();
                        if (entity instanceof CompostBossEntity) {
                            CompostBossEntity c = (CompostBossEntity)entity;
                            BossBars.untrack((Entity)c, c.getBossBar());
                        } else {
                            entity = e.getEntity();
                            if (entity instanceof AppleBossEntity) {
                                AppleBossEntity a = (AppleBossEntity)entity;
                                BossBars.untrack((Entity)a, a.getBossBar());
                            } else {
                                entity = e.getEntity();
                                if (entity instanceof DefectCoreEntity) {
                                    DefectCoreEntity d = (DefectCoreEntity)entity;
                                    BossBars.untrack((Entity)d, d.getBossBar());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone e) {
        Player player = e.getOriginal();
        if (player instanceof ServerPlayer) {
            ServerPlayer old = (ServerPlayer)player;
            BossBars.forgetPlayer(old);
        }
    }

    private static boolean isBossEntity(Entity e) {
        if (e instanceof TideFishBossEntity) {
            return true;
        }
        return e instanceof SugarQueenEntity || e instanceof MeatMountainEntity || e instanceof TeaTreeBossEntity || e instanceof WineBarrelBossEntity || e instanceof RatBossEntity || e instanceof DefectCoreEntity || e instanceof CompostBossEntity || e instanceof AppleBossEntity || e instanceof EasterBossEntity;
    }

    private static boolean hiddenBossNear(ServerLevel sl, ServerPlayer sp) {
        for (EasterBossEntity boss : sl.m_45976_(EasterBossEntity.class, sp.m_20191_().m_82400_(64.0))) {
            if (boss.isMini()) continue;
            return true;
        }
        return false;
    }

    private void applyBeaconBuffs(ServerPlayer sp) {
        for (MobEffect eff : BEACON_EFFECTS) {
            MobEffectInstance cur = sp.m_21124_(eff);
            if (cur != null && cur.m_19564_() >= 1 && cur.m_19557_() >= 60) continue;
            sp.m_7292_(new MobEffectInstance(eff, 240, 1, true, false, false));
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onDeadPlayerStopEating(TickEvent.PlayerTickEvent e) {
        ServerPlayer sp;
        block8: {
            block7: {
                if (e.phase != TickEvent.Phase.START) {
                    return;
                }
                Player player = e.player;
                if (!(player instanceof ServerPlayer)) break block7;
                sp = (ServerPlayer)player;
                if (!sp.m_9236_().f_46443_) break block8;
            }
            return;
        }
        if (!sp.m_6117_()) {
            return;
        }
        if (sp.m_6084_() && !sp.m_21224_()) {
            return;
        }
        ItemStack stack = sp.m_21211_();
        if (!stack.m_41614_()) {
            return;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey((Object)stack.m_41720_());
        FTBossMod.LOGGER.info("[FTBoss] \u6b7b\u4ea1\u77ac\u95f4\u505c\u6b62\u8fdb\u98df(\u9632 solcarrot \u6b7b\u5403\u5d29\u6e83): {} \u505c\u4e0b {}", (Object)sp.m_7755_().getString(), (Object)id);
        sp.m_5810_();
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        boolean hiddenNear;
        boolean bossNear;
        ServerPlayer sp;
        block12: {
            block11: {
                if (e.phase != TickEvent.Phase.END) {
                    return;
                }
                Player player = e.player;
                if (!(player instanceof ServerPlayer)) break block11;
                sp = (ServerPlayer)player;
                if (!sp.m_9236_().f_46443_) break block12;
            }
            return;
        }
        if (sp.f_19797_ % 10 != 0) {
            return;
        }
        Level level = sp.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        if (ManorFlyBan.active(sp) && ManorFlyBan.stripFlight(sp)) {
            ManorFlyBan.notifyPlayer(sp);
        }
        if (!(bossNear = (hiddenNear = BossHandler.hiddenBossNear(sl, sp)))) {
            return;
        }
        try {
            SparrowSupport.tick(sl.m_7654_());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (hiddenNear) {
            this.applyBeaconBuffs(sp);
        }
    }

    private static MobEffect findAnyEffect(String[][] ids) {
        for (String[] id : ids) {
            MobEffect fx = BossHandler.findEffect(id[0], id[1]);
            if (fx == null) continue;
            return fx;
        }
        MobEffect hit = BossHandler.scanForTendon("armamentarium");
        if (hit != null) {
            return hit;
        }
        return BossHandler.scanForTendon(null);
    }

    private static MobEffect scanForTendon(String namespace) {
        try {
            for (Map.Entry entry : ForgeRegistries.MOB_EFFECTS.getEntries()) {
                ResourceLocation key = ((ResourceKey)entry.getKey()).m_135782_();
                if (namespace != null && !namespace.equals(key.m_135827_())) continue;
                String p = key.m_135815_().toLowerCase(Locale.ROOT);
                for (String hint : TENDON_HINTS) {
                    if (!p.contains(hint)) continue;
                    FTBossMod.LOGGER.info("[FTBoss] \u7b4b\u7bad\u65ad\u88c2\u547d\u4e2d: {}", (Object)key);
                    return (MobEffect)entry.getValue();
                }
            }
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] \u626b\u63cf\u7b4b\u7bad\u65ad\u88c2\u5931\u8d25: {}", (Object)t.toString());
        }
        return null;
    }

    private static MobEffect findEffect(String namespace, String path) {
        try {
            return (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(namespace, path));
        }
        catch (Throwable t) {
            return null;
        }
    }

    @SubscribeEvent
    public void onAppleFriendlyFire(LivingHurtEvent e) {
        LivingEntity le = e.getEntity();
        if (!AppleBossEntity.isAppleSide((Entity)le)) {
            return;
        }
        DamageSource src = e.getSource();
        if (src.m_269533_(DamageTypeTags.f_268725_) || src.m_269533_(DamageTypeTags.f_268415_)) {
            e.setCanceled(true);
        }
    }

    public static String playerTitle(Player p) {
        try {
            Object cap;
            Class<?> tm = Class.forName("aurilux.titles.common.core.TitleManager");
            Object lazy = tm.getMethod("getCapability", Player.class).invoke(null, p);
            if (lazy == null) {
                return null;
            }
            Object opt = lazy.getClass().getMethod("resolve", new Class[0]).invoke(lazy, new Object[0]);
            if (opt instanceof Optional) {
                Optional o = (Optional)opt;
                v0 = o.orElse(null);
            } else {
                v0 = cap = null;
            }
            if (cap == null) {
                return null;
            }
            Object title = cap.getClass().getMethod("getDisplayTitle", new Class[0]).invoke(cap, new Object[0]);
            Object disp = title == null ? null : title.getClass().getMethod("getDefaultDisplay", new Class[0]).invoke(title, new Object[0]);
            Object id = title == null ? null : title.getClass().getMethod("getID", new Class[0]).invoke(title, new Object[0]);
            Object nbt = null;
            try {
                nbt = cap.getClass().getMethod("serializeNBT", new Class[0]).invoke(cap, new Object[0]);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return (disp == null ? "" : disp.toString()) + "||" + (id == null ? "" : id.toString()) + "||" + (nbt == null ? "" : nbt.toString());
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static boolean looseContains(String hay, String needle) {
        if (hay == null) {
            return false;
        }
        return hay.replaceAll("\\s+", "").contains(needle.replaceAll("\\s+", ""));
    }

    public static boolean hasTitle(Player p, String ... keywords) {
        String[] sources = new String[]{BossHandler.playerTitle(p), p.m_5446_().getString(), p.m_7770_() != null ? p.m_7770_().getString() : null, p.m_7755_().getString(), p.getPersistentData().toString()};
        for (String kw : keywords) {
            for (String s : sources) {
                if (!BossHandler.looseContains(s, kw)) continue;
                return true;
            }
        }
        return false;
    }

    public static boolean hasItem(Player p, String ... keywords) {
        return BossHandler.matchedItem(p, keywords) != null;
    }

    public static String matchedItem(Player p, String ... exact) {
        try {
            for (int i = 0; i < p.m_150109_().m_6643_(); ++i) {
                ItemStack st = p.m_150109_().m_8020_(i);
                if (st.m_41619_()) continue;
                ResourceLocation key = ForgeRegistries.ITEMS.getKey((Object)st.m_41720_());
                String idStr = key == null ? "" : key.toString();
                String nameStr = st.m_41786_().getString();
                for (String kw : exact) {
                    if (!idStr.equalsIgnoreCase(kw) && !BossHandler.looseEquals(nameStr, kw)) continue;
                    return idStr + " (" + nameStr + ") x" + st.m_41613_();
                }
            }
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] \u80cc\u5305\u68c0\u6d4b\u5931\u8d25? {}", (Object)t.toString());
        }
        return null;
    }

    private static boolean looseEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return a.replaceAll("\\s+", "").equalsIgnoreCase(b.replaceAll("\\s+", ""));
    }

    public static String matchedContract(Player p) {
        try {
            String[] hands = new String[]{"mainhand", "offhand"};
            for (int h = 0; h < 2; ++h) {
                boolean hit;
                ItemStack st;
                ItemStack itemStack = st = h == 0 ? p.m_21205_() : p.m_21206_();
                if (st == null || st.m_41619_()) continue;
                ResourceLocation key = ForgeRegistries.ITEMS.getKey((Object)st.m_41720_());
                String idStr = key == null ? "" : key.toString();
                String nameStr = st.m_41786_().getString();
                boolean bl = hit = idStr.equalsIgnoreCase("tridents_n_stuff:sanguine_bond") || BossHandler.looseEquals(nameStr, "\u8840\u8165\u5951\u7ea6");
                if (!hit) continue;
                return hands[h] + "=" + idStr + " (" + nameStr + ") x" + st.m_41613_();
            }
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] \u5951\u7ea6\u68c0\u6d4b\u5931\u8d25? {}", (Object)t.toString());
        }
        return null;
    }

    public static String handsDump(Player p) {
        try {
            ItemStack m = p.m_21205_();
            ItemStack o = p.m_21206_();
            ResourceLocation km = m.m_41619_() ? null : ForgeRegistries.ITEMS.getKey((Object)m.m_41720_());
            ResourceLocation ko = o.m_41619_() ? null : ForgeRegistries.ITEMS.getKey((Object)o.m_41720_());
            return "main=" + (Comparable)((Object)(km == null ? "empty" : km)) + ", off=" + (Comparable)((Object)(ko == null ? "empty" : ko));
        }
        catch (Throwable t) {
            return "hands=?";
        }
    }

    public static String inventoryDump(Player p) {
        StringBuilder sb = new StringBuilder("[");
        try {
            int n = 0;
            for (int i = 0; i < p.m_150109_().m_6643_(); ++i) {
                ItemStack st = p.m_150109_().m_8020_(i);
                if (st.m_41619_()) continue;
                ResourceLocation key = ForgeRegistries.ITEMS.getKey((Object)st.m_41720_());
                if (n++ > 0) {
                    sb.append(", ");
                }
                sb.append(i).append(':').append((Object)(key == null ? "?" : key));
                if (n < 14) continue;
                sb.append(", ...");
                break;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return sb.append(']').toString();
    }

    public static boolean hasBloodyContract(Player p) {
        return BossHandler.hasItem(p, BLOODY_CONTRACT);
    }

    public static String debugLine(Player p) {
        return p.m_7755_().getString() + " | title=" + BossHandler.playerTitle(p) + " | contract=" + BossHandler.matchedContract(p) + " | hands=" + BossHandler.handsDump(p) + " | inv=" + BossHandler.inventoryDump(p);
    }

    static {
        armamentariumBlockLogged = ConcurrentHashMap.newKeySet();
        FLY_BAN_CD = new ConcurrentHashMap<UUID, Integer>();
        BEACON_EFFECTS = new MobEffect[]{MobEffects.f_19596_, MobEffects.f_19598_, MobEffects.f_19606_, MobEffects.f_19603_, MobEffects.f_19600_, MobEffects.f_19605_};
        TENDON_HINTS = new String[]{"tendon", "rupture", "cripple", "broken", "lame", "snap"};
        FTBossMod.LOGGER.info("[FTBoss] ===== \u8f7d\u5165\u6784\u5efa\u7248\u672c\ufffd? {} =====", (Object)BUILD_TAG);
        BLOODY_CONTRACT = new String[]{"tridents_n_stuff:sanguine_bond", "\u8840\u8165\u5951\u7ea6"};
    }
}

