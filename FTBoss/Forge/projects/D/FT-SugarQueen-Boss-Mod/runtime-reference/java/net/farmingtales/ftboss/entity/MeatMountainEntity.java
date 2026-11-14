/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.level.ServerBossEvent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.BossEvent$BossBarColor
 *  net.minecraft.world.BossEvent$BossBarOverlay
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Vindicator
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.entity;

import java.util.ArrayList;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.BossUnstuck;
import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;
import net.farmingtales.ftboss.boss.ai.ButcherCombat;
import net.farmingtales.ftboss.boss.ai.ButcherCombatRules;
import net.farmingtales.ftboss.boss.ai.ButcherPhases;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.boss.ai.TrapBreaker;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.sound.BossMusic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class MeatMountainEntity
extends Vindicator {
    private static final EntityDataAccessor<CompoundTag> DATA_ACTION = SynchedEntityData.m_135353_(MeatMountainEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private final BossActionState actionState = new BossActionState((LivingEntity)this, DATA_ACTION);
    private final ButcherCombat combat = new ButcherCombat(this, this.actionState);
    private static final double BOSS_BAR_RANGE = 48.0;
    private final ServerBossEvent bossBar;
    private boolean musicStarted = false;
    private boolean cloneButcher = false;
    private boolean cloneGoalsReady = false;

    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(DATA_ACTION, (Object)new CompoundTag());
    }

    public boolean m_7327_(Entity target) {
        ServerPlayer player;
        if (target instanceof ServerPlayer && BossTargets.valid((Player)(player = (ServerPlayer)target))) {
            this.m_6710_((LivingEntity)player);
        }
        return false;
    }

    public String getButcherAction() {
        return this.actionState.name();
    }

    public float getButcherActionAge(float partialTick) {
        return this.actionState.elapsed(partialTick);
    }

    public boolean isButcherActing() {
        return this.actionState.active();
    }

    public void equipMeatLeg() {
        this.m_8061_(EquipmentSlot.MAINHAND, new ItemStack((ItemLike)FTBossItems.TURKEY_LEGS.get()));
        this.m_8061_(EquipmentSlot.OFFHAND, ItemStack.f_41583_);
        this.m_21409_(EquipmentSlot.MAINHAND, 0.0f);
        this.m_21409_(EquipmentSlot.OFFHAND, 0.0f);
        this.m_21553_(false);
        this.m_21559_(false);
    }

    public boolean m_6051_() {
        return false;
    }

    public MeatMountainEntity(EntityType<? extends MeatMountainEntity> type, Level level) {
        super(type, level);
        this.f_21364_ = 120;
        this.f_21345_.m_25352_(0, (Goal)this.combat);
        this.bossBar = new ServerBossEvent((Component)Component.m_237115_((String)"boss.ftboss.butcher"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        BossBars.track((Entity)this, this.bossBar);
    }

    private void tickMusic() {
        if (!this.m_6084_()) {
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        if (!this.musicStarted) {
            if (sl.m_45976_(ServerPlayer.class, this.m_20191_().m_82400_(40.0)).isEmpty()) {
                return;
            }
            this.musicStarted = true;
        }
        BossMusic.request(sl, (LivingEntity)this);
    }

    public void stopMusic() {
        if (!this.musicStarted) {
            return;
        }
        this.musicStarted = false;
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            BossMusic.release(sl, (LivingEntity)this);
        }
    }

    public void m_142687_(Entity.RemovalReason reason) {
        this.stopMusic();
        super.m_142687_(reason);
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    public void hideBossBar() {
        BossBarVisibility.visible(this.bossBar, false);
        this.bossBar.m_7706_();
    }

    public void setCloneButcher(boolean v) {
        this.cloneButcher = v;
    }

    public boolean isCloneButcher() {
        return this.cloneButcher;
    }

    private void initCloneButcher() {
        this.cloneGoalsReady = true;
        try {
            boolean canMelee;
            boolean bl = canMelee = this.m_21051_(Attributes.f_22281_) != null;
            if (canMelee) {
                this.f_21345_.m_25352_(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, ButcherCombatRules.chaseSpeed(false), true));
            } else {
                FTBossMod.LOGGER.warn("[FTBoss] \u5c60\u592b\u590d\u5236\u4f53\u7f3a\u5c11 ATTACK_DAMAGE \u5c5e\u6027(\u8001\u5b58\u6863\u5b9e\u4f53) -> \u4e0d\u542f\u7528\u8fd1\u6218 AI, \u907f\u514d\u5d29\u670d");
            }
            this.f_21345_.m_25352_(7, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 24.0f));
            this.f_21345_.m_25352_(8, (Goal)new RandomLookAroundGoal((Mob)this));
            if (canMelee) {
                this.f_21346_.m_25352_(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
            }
            this.equipMeatLeg();
            FTBossMod.LOGGER.info("[FTBoss] \u5c60\u592b\u590d\u5236\u4f53 AI \u5c31\u7eea(\u8089\u817f\u6280\u80fd)");
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] \u5c60\u592b\u590d\u5236\u4f53 AI \u521d\u59cb\u5316\u5931\u8d25: {}", (Object)t.toString());
        }
    }

    private void limitChaseSpeed() {
        double adjustment;
        AttributeInstance speed = this.m_21051_(Attributes.f_22279_);
        if (speed == null) {
            return;
        }
        UUID limitId = UUID.fromString("c08dc1b1-a4f5-447b-8d45-6d801bb09b54");
        speed.m_22100_(0.1);
        AttributeModifier previous = speed.m_22111_(limitId);
        double current = speed.m_22135_() / (previous == null ? 1.0 : 1.0 + previous.m_22218_());
        double maximum = 0.42;
        double d = adjustment = current > maximum ? maximum / current - 1.0 : 0.0;
        if (previous != null && Math.abs(previous.m_22218_() - adjustment) < 1.0E-8) {
            return;
        }
        speed.m_22120_(limitId);
        if (adjustment < 0.0) {
            speed.m_22118_(new AttributeModifier(limitId, "ftboss_butcher_speed_limit", adjustment, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    public void m_7910_(float speed) {
        super.m_7910_(BossWalkingSpeed.speed((Mob)this, speed));
        BossWalkingSpeed.normalizeInput((Mob)this);
    }

    public static AttributeSupplier.Builder m_34104_() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 380.0).m_22268_(Attributes.f_22284_, 3.0).m_22268_(Attributes.f_22281_, 5.5).m_22268_(Attributes.f_22283_, 1.15).m_22268_(Attributes.f_22279_, 0.1).m_22268_(Attributes.f_22278_, 0.85).m_22268_(Attributes.f_22277_, 48.0);
    }

    public int getButcherPhase() {
        return ButcherPhases.phase(this);
    }

    public void resetButcherCombat() {
        this.combat.reset();
    }

    private void tickBossBar() {
        if (!this.m_6084_()) {
            BossBars.untrack((Entity)this, this.bossBar);
            return;
        }
        float f = this.m_21223_();
        float f2 = this.m_21233_();
        this.bossBar.m_142711_(f2 <= 0.0f ? 0.0f : Math.max(0.0f, Math.min(1.0f, f / f2)));
        Component component = this.m_7770_() != null ? this.m_7770_() : Component.m_237115_((String)"boss.ftboss.butcher");
        component = component.m_6881_().m_7220_((Component)Component.m_237113_((String)(" \u00a77\u00b7\u9636\u6bb5 " + this.getButcherPhase() + "/3" + (String)(this.getButcherPhase() == 3 ? " \u00b7\u5269\u4f59 " + ButcherPhases.remainingSeconds(this) + "\u79d2" : ""))));
        if (!component.getString().equals(this.bossBar.m_18861_().getString())) {
            this.bossBar.m_6456_(component);
        }
        boolean bl = false;
        for (ServerPlayer serverPlayer : this.m_9236_().m_45976_(ServerPlayer.class, this.m_20191_().m_82400_(48.0))) {
            if (this.m_20280_((Entity)serverPlayer) < 2304.0) {
                BossBarVisibility.addPlayer(this.bossBar, serverPlayer);
                bl = true;
                continue;
            }
            this.bossBar.m_6539_(serverPlayer);
        }
        if (this.bossBar.m_8323_() != bl) {
            BossBarVisibility.visible(this.bossBar, bl);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean m_6469_(DamageSource damageSource, float f) {
        float f2 = BossDamageGate.gate((LivingEntity)this, damageSource, f);
        if (f2 <= 0.0f) {
            return false;
        }
        if (!this.m_9236_().f_46443_ && ButcherPhases.dodge(this, damageSource)) {
            return false;
        }
        ButcherPhases.beginDamage(this, damageSource);
        try {
            boolean bl = super.m_6469_(damageSource, f2);
            if (bl) {
                BossDamageGate.onHit((LivingEntity)this);
            }
            boolean bl2 = bl;
            return bl2;
        }
        finally {
            ButcherPhases.endDamage(this);
        }
    }

    public void m_8119_() {
        super.m_8119_();
        if (!this.m_9236_().f_46443_) {
            if (!(this.f_19797_ != 1 && this.f_19797_ % 20 != 0 || this.m_21205_().m_150930_((Item)FTBossItems.TURKEY_LEGS.get()) && this.m_21206_().m_41619_() && !this.m_21526_())) {
                this.equipMeatLeg();
            }
            this.limitChaseSpeed();
            ButcherPhases.tick(this);
            this.combat.update();
            if (this.cloneButcher && !this.cloneGoalsReady) {
                this.initCloneButcher();
            }
            BossDamageGate.tick((LivingEntity)this);
            TrapBreaker.tick((LivingEntity)this);
            BossUnstuck.tick((LivingEntity)this);
            MinionBlessing.tick((LivingEntity)this, 40.0);
            this.tickBossBar();
            this.tickMusic();
        }
        if (!this.m_9236_().f_46443_ && this.f_19797_ % 20 == 0) {
            for (MobEffectInstance mobEffectInstance : new ArrayList(this.m_21220_())) {
                if (mobEffectInstance.m_19544_().m_19486_()) continue;
                this.m_21195_(mobEffectInstance.m_19544_());
            }
        }
    }
}

