package net.farmingtales.ftboss.entity;

import java.util.ArrayList;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.NonManorFinale;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.BossUnstuck;
import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;
import net.farmingtales.ftboss.boss.ai.ButcherCombat;
import net.farmingtales.ftboss.boss.ai.ButcherPhases;
import net.farmingtales.ftboss.boss.ai.ButcherCombatRules;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.boss.ai.TrapBreaker;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.sound.BossMusic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
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

public class MeatMountainEntity extends Vindicator {
   private static final EntityDataAccessor<CompoundTag> DATA_ACTION = SynchedEntityData.defineId(MeatMountainEntity.class, EntityDataSerializers.COMPOUND_TAG);
   private final BossActionState actionState = new BossActionState(this, DATA_ACTION);
   private final ButcherCombat combat = new ButcherCombat(this, this.actionState);
   private static final double BOSS_BAR_RANGE = 48.0;
   private final ServerBossEvent bossBar;
   private boolean musicStarted = false;
   private boolean cloneButcher = false;
   private boolean cloneGoalsReady = false;

   protected void defineSynchedData(Builder var1) {
      super.defineSynchedData(var1);
      var1.define(DATA_ACTION, new CompoundTag());
   }

   public boolean doHurtTarget(Entity var1) {
      if (var1 instanceof ServerPlayer var2 && BossTargets.valid(var2)) {
         this.setTarget(var2);
      }

      return false;
   }

   public String getButcherAction() {
      return this.actionState.name();
   }

   public float getButcherActionAge(float var1) {
      return this.actionState.elapsed(var1);
   }

   public boolean isButcherActing() {
      return this.actionState.active();
   }

   public void equipMeatLeg() {
      this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack((ItemLike)FTBossItems.TURKEY_LEGS.get()));
      this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
      this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
      this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
      this.setCanPickUpLoot(false);
      this.setLeftHanded(false);
   }

   public boolean displayFireAnimation() {
      return false;
   }

   public MeatMountainEntity(EntityType<? extends MeatMountainEntity> var1, Level var2) {
      super(var1, var2);
      this.xpReward = 120;
      this.goalSelector.addGoal(0, this.combat);
      this.bossBar = new ServerBossEvent(Component.translatable("boss.ftboss.butcher"), BossBarColor.RED, BossBarOverlay.PROGRESS);
      BossBars.track(this, this.bossBar);
   }

   private void tickMusic() {
      if (this.isAlive()) {
         if (this.level() instanceof ServerLevel var1) {
            if (!this.musicStarted) {
               if (var1.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(40.0)).isEmpty()) {
                  return;
               }

               this.musicStarted = true;
            }

            BossMusic.request(var1, this);
         }
      }
   }

   public void stopMusic() {
      if (this.musicStarted) {
         this.musicStarted = false;
         if (this.level() instanceof ServerLevel var1) {
            BossMusic.release(var1, this);
         }
      }
   }

   public void remove(RemovalReason var1) {
      this.stopMusic();
      super.remove(var1);
   }

   public ServerBossEvent getBossBar() {
      return this.bossBar;
   }

   public void hideBossBar() {
      this.bossBar.setVisible(false);
      this.bossBar.removeAllPlayers();
   }

   private void tickBossBar() {
      if (!this.isAlive()) {
         BossBars.untrack(this, this.bossBar);
      } else {
         float var1 = this.getHealth();
         float var2 = this.getMaxHealth();
         this.bossBar.setProgress(var2 <= 0.0F ? 0.0F : Math.max(0.0F, Math.min(1.0F, var1 / var2)));
         Component var3 = this.getCustomName() != null ? this.getCustomName() : Component.translatable("boss.ftboss.butcher");
         var3 = var3.copy()
            .append(
               Component.literal(
                  " §7·阶段 " + this.getButcherPhase() + "/3" + (this.getButcherPhase() == 3 ? " ·剩余 " + ButcherPhases.remainingSeconds(this) + "秒" : "")
               )
            );
         if (!var3.getString().equals(this.bossBar.getName().getString())) {
            this.bossBar.setName((Component)var3);
         }

         boolean var4 = false;

         for (ServerPlayer var6 : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(48.0))) {
            if (this.distanceToSqr(var6) < 2304.0) {
               this.bossBar.addPlayer(var6);
               var4 = true;
            } else {
               this.bossBar.removePlayer(var6);
            }
         }

         if (this.bossBar.isVisible() != var4) {
            this.bossBar.setVisible(var4);
         }
      }
   }

   public boolean hurt(DamageSource var1, float var2) {
      float var3 = BossDamageGate.gate(this, var1, var2);
      if (var3 <= 0.0F) {
         return false;
      } else if (!this.level().isClientSide && ButcherPhases.dodge(this, var1)) {
         return false;
      } else {
         ButcherPhases.beginDamage(this, var1);

         boolean var5;
         try {
            boolean var4 = super.hurt(var1, var3);
            if (var4) {
               BossDamageGate.onHit(this);
            }

            var5 = var4;
         } finally {
            ButcherPhases.endDamage(this);
         }

         return var5;
      }
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         if ((this.tickCount == 1 || this.tickCount % 20 == 0)
            && (!this.getMainHandItem().is((Item)FTBossItems.TURKEY_LEGS.get()) || !this.getOffhandItem().isEmpty() || this.isLeftHanded())) {
            this.equipMeatLeg();
         }

         this.limitChaseSpeed();
         ButcherPhases.tick(this);
         this.combat.update();
         if (this.cloneButcher && !this.cloneGoalsReady) {
            this.initCloneButcher();
         }

         BossDamageGate.tick(this);
         BossRage.tickBoss(this);
         TrapBreaker.tick(this);
         BossUnstuck.tick(this);
         MinionBlessing.tick(this, 40.0);
         this.tickBossBar();
         this.tickMusic();
      }

      if (!this.level().isClientSide && this.tickCount % 20 == 0) {
         for (MobEffectInstance var2 : new ArrayList<>(this.getActiveEffects())) {
            if (!((MobEffect)var2.getEffect().value()).isBeneficial()) {
               this.removeEffect(var2.getEffect());
            }
         }
      }
   }

   public void setCloneButcher(boolean var1) {
      this.cloneButcher = var1;
   }

   public boolean isCloneButcher() {
      return this.cloneButcher;
   }

   private void initCloneButcher() {
      this.cloneGoalsReady = true;

      try {
         boolean var1 = this.getAttribute(Attributes.ATTACK_DAMAGE) != null;
         if (var1) {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, ButcherCombatRules.chaseSpeed(false), true));
         } else {
            FTBossMod.LOGGER.warn("[FTBoss] 屠夫复制体缺少 ATTACK_DAMAGE 属性(老存档实体) -> 不启用近战 AI, 避免崩服");
         }

         this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 24.0F));
         this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
         if (var1) {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
         }

         this.equipMeatLeg();
         FTBossMod.LOGGER.info("[FTBoss] 屠夫复制体 AI 就绪(肉腿技能)");
      } catch (Throwable var2) {
         FTBossMod.LOGGER.warn("[FTBoss] 屠夫复制体 AI 初始化失败: {}", var2.toString());
      }
   }

   private void limitChaseSpeed() {
      AttributeInstance var1 = this.getAttribute(Attributes.MOVEMENT_SPEED);
      if (var1 != null) {
         ResourceLocation var2 = ResourceLocation.fromNamespaceAndPath("ftboss", "uc08dc1b1a4f5447b8d456d801bb09b54");
         var1.setBaseValue(0.1);
         AttributeModifier var3 = var1.getModifier(var2);
         double var4 = var1.getValue() / (var3 == null ? 1.0 : 1.0 + var3.amount());
         double var6 = 0.42 * (NonManorFinale.isFinale(this) ? 2.0 : 1.0);
         double var8 = var4 > var6 ? var6 / var4 - 1.0 : 0.0;
         if (var3 == null || !(Math.abs(var3.amount() - var8) < 1.0E-8)) {
            var1.removeModifier(var2);
            if (var8 < 0.0) {
               var1.addTransientModifier(new AttributeModifier(var2, var8, Operation.ADD_MULTIPLIED_TOTAL));
            }
         }
      }
   }

   public void setSpeed(float var1) {
      super.setSpeed(BossWalkingSpeed.speed(this, var1));
      BossWalkingSpeed.normalizeInput(this);
   }

   public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 380.0)
         .add(Attributes.ARMOR, 3.0)
         .add(Attributes.ATTACK_DAMAGE, 5.5)
         .add(Attributes.ATTACK_SPEED, 1.15)
         .add(Attributes.MOVEMENT_SPEED, 0.1)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.85)
         .add(Attributes.FOLLOW_RANGE, 48.0);
   }
   public int getButcherPhase() {
      return ButcherPhases.phase(this);
   }
   public void resetButcherCombat() {
      this.combat.reset();
   }
}
