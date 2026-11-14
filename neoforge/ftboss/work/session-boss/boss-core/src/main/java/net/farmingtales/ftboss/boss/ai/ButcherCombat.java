package net.farmingtales.ftboss.boss.ai;

import java.util.EnumSet;
import java.util.UUID;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.butcherskills.ButcherBloodBrand;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

public final class ButcherCombat extends Goal {
   private static final ResourceKey<DamageType> MELEE_DAMAGE = ResourceKey.create(
      Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("ftboss", "butcher_melee")
   );
   private final MeatMountainEntity owner;
   private final BossActionState action;
   private UUID swingTarget;
   private long nextAttack;
   private final BossApproach approach = new BossApproach();
   private int successfulHits;
   private int swings;
   private boolean resolved;

   public ButcherCombat(MeatMountainEntity var1, BossActionState var2) {
      this.owner = var1;
      this.action = var2;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public boolean canUse() {
      return this.owner.isAlive() && !this.owner.isNoAi() && this.eligible(this.owner.getTarget());
   }

   public boolean canContinueToUse() {
      return this.canUse();
   }

   public void update() {
      if (ButcherBloodBrand.tick(this.owner, this.action)) {
         return;
      }
      if (this.owner.level() instanceof ServerLevel var1) {
         if (this.owner.isAlive() && !this.owner.isNoAi()) {
            long var11 = var1.getGameTime();
            LivingEntity var4 = BossAggro.highest(this.owner);
            if (var4 == null && this.eligible(this.owner.getTarget())) {
               var4 = this.owner.getTarget();
            }

            if (var4 == null) {
               double var5 = 2304.0;

               for (ServerPlayer var8 : var1.players()) {
                  double var9 = this.owner.distanceToSqr(var8);
                  if (this.eligible(var8) && var9 < var5) {
                     var4 = var8;
                     var5 = var9;
                  }
               }

               this.owner.setTarget((LivingEntity)var4);
            }

            if (var4 == null) {
               this.cancelSwing();
               this.approach.reset(this.owner);
            } else {
               boolean var12 = this.owner.getHealth() < this.owner.getMaxHealth() * 0.5F;
               this.owner.getLookControl().setLookAt((Entity)var4, 30.0F, 30.0F);
               double var6 = Math.sqrt((double)(this.owner.getBbWidth() * this.owner.getBbWidth() * 4.0F + var4.getBbWidth()));
               this.approach
                  .tick(this.owner, (LivingEntity)var4, ButcherCombatRules.chaseSpeed(var12), Math.max(0.5, var6 - 0.8), Math.max(0.6, var6 - 0.3), 0.0, 0.0);
               if (this.action.active()) {
                  if (!this.resolved && this.action.elapsed(0.0F) >= 1.0F) {
                     this.resolved = true;
                     if (this.swingTarget != null && var1.getEntity(this.swingTarget) instanceof LivingEntity var13 && this.canHit(var13)) {
                        this.hit(var1, var13);
                     }
                  }
               } else if (var11 >= this.nextAttack && this.canHit((LivingEntity)var4)) {
                  this.swingTarget = var4.getUUID();
                  this.resolved = false;
                  this.action.start(this.swings++ % 2 == 0 ? "chop" : "backhand", 4);
                  this.nextAttack = var11 + (long)ButcherPhaseRules.interval(this.owner.getButcherPhase());
                  this.owner.swing(InteractionHand.MAIN_HAND);
                  var1.playSound(null, this.owner.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 0.75F, var12 ? 1.3F : 1.1F);
               }
            }
         } else {
            this.cancelSwing();
            this.approach.reset(this.owner);
         }
      }
   }

   private boolean eligible(LivingEntity var1) {
      return BossTargets.valid(var1)
         && var1.level() == this.owner.level()
         && !this.owner.isAlliedTo(var1)
         && !BossAggro.isBossSide(var1)
         && this.owner.distanceToSqr(var1) < 2304.0;
   }

   private boolean canHit(LivingEntity var1) {
      return this.eligible(var1)
         && this.owner.hasLineOfSight(var1)
         && ButcherCombatRules.withinReach(
            this.owner.distanceToSqr(var1), Math.abs(this.owner.getY() - var1.getY()), (double)this.owner.getBbWidth(), (double)var1.getBbWidth()
         );
   }

   private void hit(ServerLevel var1, LivingEntity var2) {
      float var3 = ButcherCombatRules.damage((float)this.owner.getAttributeValue(Attributes.ATTACK_DAMAGE), this.successfulHits);
      float var4 = var2.getHealth() + var2.getAbsorptionAmount();
      DamageSource var5 = new DamageSource(var1.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(MELEE_DAMAGE), this.owner);
      if (var2.hurt(var5, var3)) {
         float var6 = Math.max(0.0F, var4 - var2.getHealth() - var2.getAbsorptionAmount());
         if (!(var6 <= 0.0F)) {
            this.owner.heal(ButcherCombatRules.lifesteal(var6, this.owner.getMaxHealth() - this.owner.getHealth()));
            ButcherHitEffects.apply(this.owner, var2, ButcherCombatRules.empowered(this.successfulHits));
            this.successfulHits++;
            var1.sendParticles(ParticleTypes.DAMAGE_INDICATOR, var2.getX(), var2.getY() + 1.0, var2.getZ(), 5, 0.25, 0.3, 0.25, 0.05);
            var1.playSound(null, var2.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.HOSTILE, 0.8F, 0.8F);
         }
      }
   }

   private void cancelSwing() {
      if (this.action.active()) {
         this.action.start("", 0);
      }

      this.swingTarget = null;
      this.resolved = true;
   }
   public void reset() {
      this.cancelSwing();
      this.successfulHits = 0;
      this.nextAttack = this.owner.level().getGameTime() + (long)ButcherPhaseRules.interval(this.owner.getButcherPhase());
   }
}
