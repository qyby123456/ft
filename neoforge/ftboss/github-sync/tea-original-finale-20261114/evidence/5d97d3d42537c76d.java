package net.farmingtales.ftboss.entity;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.NonManorFinale;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.BossMeleeWindup;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.boss.ai.SayaBuds;
import net.farmingtales.ftboss.boss.ai.SummonSweep;
import net.farmingtales.ftboss.boss.ai.TeaTactics;
import net.farmingtales.ftboss.boss.ai.TrapBreaker;
import net.farmingtales.ftboss.integ.WizardSummon;
import net.farmingtales.ftboss.integ.spells.BossSpellPacing;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.RegularBossCastHost;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.Fx;
import net.farmingtales.ftboss.util.BossSpellRules.Kind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

public class TeaTreeBossEntity extends AbstractSpellCastingMob implements RegularBossCastHost {
   private static final EntityDataAccessor<CompoundTag> DATA_ACTION = SynchedEntityData.defineId(TeaTreeBossEntity.class, EntityDataSerializers.COMPOUND_TAG);
   private final BossActionState actionState = new BossActionState(this, DATA_ACTION);
   private static final EntityDataAccessor<CompoundTag> DATA_SIGNATURE_CAST = SynchedEntityData.defineId(
      TeaTreeBossEntity.class, EntityDataSerializers.COMPOUND_TAG
   );
   private final BossActionState signatureCast = new BossActionState(this, DATA_SIGNATURE_CAST);
   private final BossMeleeWindup meleeWindup = new BossMeleeWindup(this, this.actionState, 6, 14, 3.0);
   private UUID nativeTarget;
   private Vec3 nativeAim = Vec3.ZERO;
   private Vec3 rootDirection = Vec3.ZERO;
   private boolean nativePending;
   private static final double BOSS_BAR_RANGE = 48.0;
   private static final double MAX_HP = 340.0;
   private static final int FANG_CD = 100;
   private static final int SPIT_CD = 160;
   private static final int FANG_CD_P2 = 62;
   private static final int SPIT_CD_P2 = 100;
   private static final int HP_BOOST_INTERVAL = 1200;
   private static final int HP_BOOST_MAX = 10;
   private static final float HP_BOOST_PER_LEVEL = 4.0F;
   private final ServerBossEvent bossBar;
   private Holder<MobEffect> teaRenewal;
   private int auraTimer = 0;
   private int fangCd = 100;
   private int spitCd = 160;
   private int contactCd = 0;
   private int castCd = 80;
   private int buffWheelCd = 60;
   private static final String MINION_TAG = "tea_minion";
   private final SayaBuds buds = new SayaBuds(this);
   private boolean musicStarted = false;
   private final TeaTactics tactics = new TeaTactics(this);
   private final List<AbstractSpell> castQueue = new ArrayList<>();
   private boolean phase2 = false;
   private boolean secondLifeUsed = false;
   private int hpBoostLevel = 0;
   private static final String[] SAYA_SELF_SPELL_FIELDS = new String[]{"OAKSKIN_SPELL", "ABSORPTION_SPELL"};
   private List<AbstractSpell> sayaSelfSpells;

   public void initiateCastSpell(AbstractSpell var1, int var2) {
      if (!this.isCasting()) {
         super.initiateCastSpell(var1, var2);
         BossSpellTuning.fasterLegacyCast(this, var1);
      }
   }

   protected void defineSynchedData(Builder var1) {
      super.defineSynchedData(var1);
      var1.define(DATA_ACTION, new CompoundTag());
      var1.define(DATA_SIGNATURE_CAST, new CompoundTag());
   }

   public BossActionState signatureCastAction() {
      return this.signatureCast;
   }

   @Override
   public void startSignatureCast(Kind var1, int var2) {
      this.signatureCast.start(RegularBossCastHost.action(var1), RegularBossCastHost.visualTicks(var2));
   }

   public boolean doHurtTarget(Entity var1) {
      this.meleeWindup.start(var1);
      return false;
   }

   public BossActionState nativeAction() {
      return this.actionState;
   }

   private void startNativeSkill(String var1, LivingEntity var2) {
      this.nativeTarget = var2.getUUID();
      this.nativeAim = var2.getEyePosition();
      this.rootDirection = var2.position().subtract(this.position()).multiply(1.0, 0.0, 1.0).normalize();
      this.nativePending = true;
      this.actionState.start(var1, "roots".equals(var1) ? 26 : 20);
   }

   private void tickNativeSkill() {
      this.meleeWindup.tick(var1x -> super.doHurtTarget(var1x));
      if (this.nativePending && this.level() instanceof ServerLevel var1) {
         if (!this.actionState.active()) {
            this.nativePending = false;
         } else {
            boolean var11 = "roots".equals(this.actionState.name());
            int var3 = var11 ? 12 : 8;
            if (this.actionState.elapsed(0.0F) < (float)var3) {
               if (this.tickCount % 3 == 0) {
                  if (var11) {
                     for (byte var12 = 2; var12 <= 16; var12 += 2) {
                        double var14 = this.getX() + this.rootDirection.x * (double)var12;
                        double var15 = this.getZ() + this.rootDirection.z * (double)var12;
                        double var9 = (double)var1.getHeight(Types.MOTION_BLOCKING, Mth.floor(var14), Mth.floor(var15));
                        var1.sendParticles(ParticleTypes.COMPOSTER, var14, var9 + 0.1, var15, 1, 0.06, 0.02, 0.06, 0.0);
                     }
                  } else {
                     var1.sendParticles(ParticleTypes.SNEEZE, this.getX(), this.getY() + 1.4, this.getZ(), 3, 0.2, 0.12, 0.2, 0.01);
                  }
               }
            } else {
               this.nativePending = false;
               if (var1.getEntity(this.nativeTarget) instanceof LivingEntity var4 && BossTargets.valid(var4)) {
                  if (var11) {
                     this.eruptionRoots(var4);
                  } else {
                     Vec3 var13 = this.getEyePosition();
                     Vec3 var6 = this.nativeAim.subtract(var13);

                     for (int var7 = 0; var7 <= 16; var7++) {
                        Vec3 var8 = var13.add(var6.scale((double)var7 / 16.0));
                        var1.sendParticles(ParticleTypes.SNEEZE, var8.x, var8.y, var8.z, 1, 0.07, 0.07, 0.07, 0.01);
                     }

                     if (var4.getBoundingBox().inflate(0.85).contains(this.nativeAim) && this.hasLineOfSight(var4) && this.distanceToSqr(var4) <= 256.0) {
                        this.spitTeaPoison(var4);
                     }
                  }

                  return;
               }
            }
         }
      }
   }

   public boolean displayFireAnimation() {
      return false;
   }

   public boolean isTeaMinion() {
      return this.getPersistentData().getBoolean("tea_minion");
   }

   public void setTeaMinion(boolean var1) {
      this.getPersistentData().putBoolean("tea_minion", var1);
   }

   public TeaTreeBossEntity(EntityType<? extends TeaTreeBossEntity> var1, Level var2) {
      super(var1, var2);
      this.xpReward = 150;
      this.bossBar = new ServerBossEvent(Component.translatable("boss.ftboss.saya"), BossBarColor.GREEN, BossBarOverlay.PROGRESS);
      BossBars.track(this, this.bossBar);
   }

   public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 340.0)
         .add(Attributes.ARMOR, 5.0)
         .add(Attributes.ATTACK_DAMAGE, 3.5)
         .add(Attributes.MOVEMENT_SPEED, 0.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.85)
         .add(Attributes.FOLLOW_RANGE, 40.0);
   }

   protected void registerGoals() {
   }

   public boolean hurt(DamageSource var1, float var2) {
      if (!this.isTeaMinion() && this.buds.shielded()) {
         this.buds.onBlocked(var1);
         return false;
      } else {
         var2 = BossDamageGate.gate(this, var1, var2);
         if (var2 <= 0.0F) {
            return false;
         } else {
            boolean var3 = super.hurt(var1, var2);
            if (var3) {
               BossDamageGate.onHit(this);
            }

            if (var3) {
               this.bossBar.setVisible(true);
            }

            return var3;
         }
      }
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         BossDamageGate.tick(this);
         BossRage.tickBoss(this);
         this.tickServer();
         if (!this.isTeaMinion()) {
            TrapBreaker.tick(this);
            this.buds.tick();
            this.tactics.tickMinionSummon();
            MinionBlessing.tick(this, 40.0);
         }
      }

      this.tickBossBar();
   }

   private void tickMusic(LivingEntity var1) {
      if (this.isAlive()) {
         if (this.level() instanceof ServerLevel var2) {
            this.musicStarted = true;
            BossMusic.request(var2, this);
         }
      }
   }

   private void stopMusic() {
      if (this.musicStarted) {
         this.musicStarted = false;
         if (this.level() instanceof ServerLevel var1) {
            BossMusic.release(var1, this);
         }
      }
   }

   private void tickServer() {
      if (!this.isAlive()) {
         this.nativePending = false;
      } else {
         this.tickNativeSkill();
         if (this.tickCount % 20 == 0) {
            for (MobEffectInstance var2 : new ArrayList(this.getActiveEffects())) {
               if (!((MobEffect)var2.getEffect().value()).isBeneficial()) {
                  this.removeEffect(var2.getEffect());
               }
            }
         }

         this.tickTeaAura();
         this.tickBuffWheel();
         this.tickLifeGrowth();
         this.tactics.absorb();
         LivingEntity var6 = this.nearestTarget(20.0);
         if (var6 != null) {
            this.setTarget(var6);
            this.faceTarget(var6);
            this.tickMusic(var6);
            if (!this.phase2 && (double)(this.getHealth() / this.getMaxHealth()) < 0.5) {
               this.phase2 = true;
               FTBossMod.LOGGER.info("[FTBoss] 茶树古树 茶心觉醒: 根刺与茶毒提速");
               if (this.level() instanceof ServerLevel var7) {
                  var7.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.0F, 0.6F);
               }

               this.buds.trigger();
            }

            int var8 = this.phase2 ? 62 : 100;
            int var9 = this.phase2 ? 100 : 160;
            double var4 = (double)this.distanceTo(var6);
            this.tickIronMagic(var6);
            if (!this.actionState.active()) {
               this.fangCd--;
               if (this.fangCd <= 0) {
                  this.fangCd = var8;
                  if (var4 < 16.0 && var4 > 1.5) {
                     this.startNativeSkill("roots", var6);
                     return;
                  }
               }

               this.spitCd--;
               if (this.spitCd <= 0) {
                  this.spitCd = var9;
                  if (var4 < 16.0) {
                     this.startNativeSkill("poison", var6);
                     return;
                  }
               }

               this.contactCd--;
               if (this.contactCd <= 0 && var4 < 3.0) {
                  this.contactCd = 30;
                  this.doHurtTarget(var6);
               }
            }
         }
      }
   }

   private void tickIronMagic(LivingEntity var1) {
      if (!this.isCasting()) {
         this.castCd--;
         if (this.castCd <= 0) {
            this.castCd = this.phase2 ? 44 : 62;
            if (this.castQueue.isEmpty()) {
               this.fillCastQueue();
            }

            if (!this.castQueue.isEmpty()) {
               this.tryCastSpell(var1, this.castQueue.remove(0));
            }
         }
      }
   }

   private void fillCastQueue() {
      List var1 = WizardSummon.spellPool();
      if (!var1.isEmpty()) {
         if (BossRage.tierOf(this) >= 1 && this.random.nextFloat() < 0.8F) {
            var1 = WizardSummon.spellPoolRare(8, 3);
         }

         byte var2 = 2;
         ArrayList var3 = new ArrayList(var1);

         while (!var3.isEmpty() && this.castQueue.size() < var2) {
            this.castQueue.add((AbstractSpell)var3.remove(this.random.nextInt(var3.size())));
         }

         if (this.random.nextFloat() < 0.35F) {
            if (this.sayaSelfSpells == null) {
               this.sayaSelfSpells = WizardSummon.spellPoolOf(SAYA_SELF_SPELL_FIELDS);
            }

            if (!this.sayaSelfSpells.isEmpty()) {
               this.castQueue.add(this.sayaSelfSpells.get(this.random.nextInt(this.sayaSelfSpells.size())));
            }
         }
      }
   }

   private void tryCastSpell(LivingEntity var1, AbstractSpell var2) {
      try {
         if (var2 == null) {
            return;
         }

         if (!BossSpellPacing.tryLegacyCast(this, var2)) {
            return;
         }

         int var3 = Math.max(var2.getMinLevel(), Math.min(6 + BossRage.tierOf(this), var2.getMaxLevel()));

         try {
            Class var4 = Class.forName("io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData");

            for (Constructor var8 : var4.getConstructors()) {
               if (var8.getParameterCount() == 1 && var8.getParameterTypes()[0].isInstance(var1)) {
                  Object var9 = var8.newInstance(var1);
                  Method var10 = MagicData.class.getMethod("setAdditionalCastData", ICastData.class);
                  MagicData var11 = MagicData.getPlayerMagicData(this);
                  var10.invoke(var11, var9);
                  break;
               }
            }
         } catch (Throwable var12) {
         }

         this.initiateCastSpell(var2, var3);
         FTBossMod.LOGGER.info("[FTBoss] 茶树古树施法: {} lv{} 目标={}", new Object[]{var2.getSpellId(), var3, var1.getName().getString()});
      } catch (Throwable var13) {
         FTBossMod.LOGGER.warn("[FTBoss] 茶树古树施法异常: {}", var13.toString());
      }
   }

   private void eruptionRoots(LivingEntity var1) {
      if (this.level() instanceof ServerLevel var2) {
         Vec3 var20 = this.rootDirection;
         double var4 = this.getX();
         double var6 = this.getZ();
         double var8 = 1.6;
         int var10 = 9 + this.random.nextInt(3);

         for (int var11 = 1; var11 <= var10; var11++) {
            double var12 = var4 + var20.x * (double)var11 * var8;
            double var14 = var6 + var20.z * (double)var11 * var8;
            BlockPos var16 = BlockPos.containing(var12, this.getY(), var14);
            double var17 = (double)this.level().getHeight(Types.MOTION_BLOCKING, var16.getX(), var16.getZ()) + 0.0;
            EvokerFangs var19 = new EvokerFangs(this.level(), var12, var17, var14, (float)Math.atan2(var20.z, var20.x), (var11 - 1) * 2, this);
            ManorFightMembers.inherit(this, var19);
            var2.addFreshEntity(var19);
         }

         var2.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ROOTED_DIRT_PLACE, SoundSource.HOSTILE, 1.0F, 0.7F);
      }
   }

   private void spitTeaPoison(LivingEntity var1) {
      var1.addEffect(new MobEffectInstance(MobEffects.POISON, 220, 2, false, false, true));
      var1.hurt(this.damageSources().indirectMagic(this, this), 2.5F);
      if (this.level() instanceof ServerLevel var2) {
         var2.playSound(null, var1.getX(), var1.getY(), var1.getZ(), SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.HOSTILE, 1.0F, 1.3F);
      }
   }

   private void tickBuffWheel() {
      if (--this.buffWheelCd <= 0) {
         this.buffWheelCd = 110 + this.random.nextInt(60);
         List var1 = List.of(
            MobEffects.MOVEMENT_SPEED,
            MobEffects.DAMAGE_BOOST,
            MobEffects.DIG_SPEED,
            MobEffects.DAMAGE_RESISTANCE,
            MobEffects.REGENERATION,
            MobEffects.ABSORPTION,
            MobEffects.SATURATION,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.LUCK,
            MobEffects.JUMP,
            MobEffects.WATER_BREATHING
         );

         for (int var2 = 0; var2 < 2; var2++) {
            Holder var3 = (Holder)var1.get(this.random.nextInt(var1.size()));
            int var4 = 100 + this.random.nextInt(120);
            int var5 = this.random.nextInt(3);
            this.addEffect(new MobEffectInstance(var3, var4, var5, false, false, true));
         }
      }
   }

   private void tickLifeGrowth() {
      if (this.hpBoostLevel < 10) {
         if (this.tickCount > 0 && this.tickCount % 1200 == 0) {
            if (this.nearestTarget(48.0) != null) {
               this.hpBoostLevel++;
               this.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 12000, this.hpBoostLevel - 1, false, false, true));
               this.heal(4.0F);
               if (this.level() instanceof ServerLevel var1) {
                  var1.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.0F, 0.8F);
                  Fx.particles(var1, this.getX(), this.getY() + 1.5, this.getZ(), 30, ParticleTypes.HAPPY_VILLAGER, 1.6, 0.08);

                  for (ServerPlayer var3 : var1.players()) {
                     if (var3.distanceToSqr(this) <= 2304.0) {
                        var3.displayClientMessage(
                           Component.literal("§2沙耶 §7汲取茶心: §c生命提升 " + this.hpBoostLevel + " 级 §8(最大生命 +" + (int)(4.0F * (float)this.hpBoostLevel) + ")"), true
                        );
                     }
                  }
               }

               FTBossMod.LOGGER.info("[FTBoss] 茶树古树 生命提升 -> {} 级 (+{} 最大生命)", this.hpBoostLevel, (int)(4.0F * (float)this.hpBoostLevel));
            }
         }
      }
   }

   private void tickTeaAura() {
      if (++this.auraTimer >= 10) {
         this.auraTimer = 0;
         if (this.teaRenewal == null) {
            this.teaRenewal = (Holder<MobEffect>)BuiltInRegistries.MOB_EFFECT
               .getHolder(ResourceLocation.fromNamespaceAndPath("herbalbrews", "renewal"))
               .orElse(null);
         }

         if (this.teaRenewal != null) {
            MobEffectInstance var1 = this.getEffect(this.teaRenewal);
            if (var1 == null || var1.getDuration() < 120) {
               this.addEffect(new MobEffectInstance(this.teaRenewal, 600, 1, false, false, true));
            }
         }
      }
   }

   private LivingEntity nearestTarget(double var1) {
      return BossTargets.combatTarget(this, var1);
   }

   private void faceTarget(LivingEntity var1) {
      double var2 = var1.getX() - this.getX();
      double var4 = var1.getZ() - this.getZ();
      if (!(var2 * var2 + var4 * var4 < 1.0E-4)) {
         this.setYRot((float)(Math.toDegrees(Math.atan2(var4, var2)) - 90.0));
         this.setYBodyRot(this.getYRot());
         this.setYHeadRot(this.getYRot());
      }
   }

   public boolean tryRevive() {
      if (!this.secondLifeUsed && this.isAlive()) {
         this.secondLifeUsed = true;
         this.setHealth((float)((double)this.getMaxHealth() * 0.3));
         this.removeAllEffects();
         this.fangCd = 40;
         this.spitCd = 70;
         this.buds.reset();
         this.buds.trigger();
         if (this.level() instanceof ServerLevel var1) {
            var1.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TOTEM_USE, SoundSource.HOSTILE, 1.0F, 1.0F);
         }

         FTBossMod.LOGGER.info("[FTBoss] 茶树古树 茶根再生, 进入第二阶段");
         return true;
      } else {
         return false;
      }
   }

   public void dropRewards() {
      this.buds.reset();
      SummonSweep.clear(this, 80.0, new Class[]{TeaMinionEntity.class});
      this.stopMusic();
      if (LemonSummonRewards.suppressed(this)) {
         FTBossMod.LOGGER.info("[FTBoss][Lemon] summoned Tea Tree defeated: rewards suppressed");
      } else if (this.level() instanceof ServerLevel var1) {
         double var9 = this.getX();
         double var4 = this.getY();
         double var6 = this.getZ();
         var1.addFreshEntity(new ItemEntity(var1, var9, var4, var6, new ItemStack(Items.OAK_LEAVES, 24)));
         var1.addFreshEntity(new ItemEntity(var1, var9, var4 + 0.4, var6, new ItemStack(Items.OAK_SAPLING, 8)));
         var1.addFreshEntity(new ItemEntity(var1, var9, var4 + 0.8, var6, new ItemStack((ItemLike)FTBossItems.BLOOMING_THORNS.get())));
         var1.addFreshEntity(new ItemEntity(var1, var9, var4 + 1.0, var6, new ItemStack((ItemLike)FTBossItems.THORN_CROWN.get())));
         var1.addFreshEntity(new ItemEntity(var1, var9 + 0.25, var4 + 1.0, var6, new ItemStack((ItemLike)FTBossItems.THORN_CHESTPLATE.get())));
         var1.addFreshEntity(new ItemEntity(var1, var9 - 0.25, var4 + 1.0, var6, new ItemStack((ItemLike)FTBossItems.THORN_LEGGINGS.get())));
         var1.addFreshEntity(new ItemEntity(var1, var9, var4 + 1.0, var6 + 0.25, new ItemStack((ItemLike)FTBossItems.THORN_BOOTS.get())));
         boolean var8 = NonManorFinale.isFinale(this) && this.getKillCredit() instanceof ServerPlayer;
         if (var8) {
            var1.addFreshEntity(new ItemEntity(var1, var9, var4 + 1.35, var6, new ItemStack((ItemLike)FTBossItems.SILVER_KEY.get())));
         }

         var1.addFreshEntity(new ExperienceOrb(var1, var9, var4, var6, var8 ? 300 : 150));
         FTBossMod.LOGGER.info("[FTBoss] 沙耶被击败：荆棘全套已掉落，终结难度银之钥={}", var8);
      }
   }

   private void tickBossBar() {
      if (!this.isAlive()) {
         BossBars.untrack(this, this.bossBar);
      } else if (this.isTeaMinion()) {
         this.hideBossBar();
      } else {
         this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
         boolean var1 = false;

         for (ServerPlayer var3 : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(48.0))) {
            if (this.distanceToSqr(var3) < 2304.0) {
               this.bossBar.addPlayer(var3);
               var1 = true;
            } else {
               this.bossBar.removePlayer(var3);
            }
         }

         if (this.bossBar.isVisible() != var1) {
            this.bossBar.setVisible(var1);
         }
      }
   }

   public void remove(RemovalReason var1) {
      if (!this.level().isClientSide) {
         this.buds.reset();
         SummonSweep.clear(this, 80.0, new Class[]{TeaMinionEntity.class});
      }

      BossBars.untrack(this, this.bossBar);
      this.stopMusic();
      super.remove(var1);
   }

   public void hideBossBar() {
      this.bossBar.setVisible(false);
      this.bossBar.removeAllPlayers();
   }

   public ServerBossEvent getBossBar() {
      return this.bossBar;
   }

   protected boolean isSunBurnTick() {
      return false;
   }
}
