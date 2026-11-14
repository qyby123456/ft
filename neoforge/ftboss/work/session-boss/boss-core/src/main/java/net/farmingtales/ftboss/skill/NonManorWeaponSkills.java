package net.farmingtales.ftboss.skill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import net.farmingtales.ftboss.api.OptionalWeaponModule;
import net.farmingtales.ftboss.util.NonManorWeaponRules;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.farmingtales.ftboss.util.NonManorWeaponRules.Shape;
import net.farmingtales.ftboss.util.NonManorWeaponRules.Skill;
import net.farmingtales.ftboss.util.WeaponFxRules.Kind;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;

@EventBusSubscriber(
   modid = "ftboss",
   bus = Bus.GAME
)
public final class NonManorWeaponSkills {
   private NonManorWeaponSkills() {
   }

   public static void cast(ServerPlayer var0, ResourceLocation var1) {
      if (var0.isAlive() && !var0.isRemoved() && !var0.isSpectator() && !var0.isSleeping() && !var0.isUsingItem()) {
         ItemStack var2 = var0.getMainHandItem();
         ResourceLocation var3 = BuiltInRegistries.ITEM.getKey(var2.getItem());
         if (!var2.isEmpty() && var3 != null && var3.equals(var1)) {
            if (OptionalWeaponModule.supports(var2)) {
               OptionalWeaponModule.cast(var0, var2);
            } else {
               Skill var4 = NonManorWeaponRules.forItem(var3.toString());
               if (var4 != null) {
                  long var5 = var0.serverLevel().getServer().overworld().getGameTime();
                  CompoundTag var7 = var0.getPersistentData().getCompound("FTBossNonManorWeaponSkills");
                  if (!var7.contains("request") || var5 < var7.getLong("request") || var5 - var7.getLong("request") >= 4L) {
                     var7.putLong("request", var5);
                     var0.getPersistentData().put("FTBossNonManorWeaponSkills", var7);
                     int var8 = NonManorWeaponRules.remaining(var4, var5, var7.getLong(var4.item));
                     if (var8 > 0) {
                        var0.displayClientMessage(
                           Component.translatable(
                              "message.ftboss.non_manor_cooldown", new Object[]{Component.translatable(var4.translationKey()), (var8 + 19) / 20}
                           ),
                           true
                        );
                     } else if (!execute(var0, var4)) {
                        var0.displayClientMessage(Component.translatable("message.ftboss.non_manor_no_target"), true);
                     } else {
                        int var9 = OptionalWeaponModule.skillCooldown(var2, var4.cooldownTicks);
                        var7.putLong(var4.item, var5 + (long)var9);
                        var2.hurtAndBreak(3, var0, EquipmentSlot.MAINHAND);
                        var0.swing(InteractionHand.MAIN_HAND, true);
                        var0.displayClientMessage(
                           Component.translatable("message.ftboss.non_manor_cast", new Object[]{Component.translatable(var4.translationKey()), var9 / 20}),
                           true
                        );
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void clonePlayer(Clone var0) {
      OptionalWeaponModule.copyCooldownData(var0);
      CompoundTag var1 = var0.getOriginal().getPersistentData();
      if (var1.contains("FTBossNonManorWeaponSkills")) {
         var0.getEntity().getPersistentData().put("FTBossNonManorWeaponSkills", var1.getCompound("FTBossNonManorWeaponSkills").copy());
      }
   }

   private static boolean execute(ServerPlayer var0, Skill var1) {
      ServerLevel var2 = var0.serverLevel();
      if (var1 == Skill.ARC) {
         return chain(var0, var1);
      } else if (var1 == Skill.WINE) {
         return WineMaceCombat.begin(var0);
      } else {
         Vec3 var3 = var1.shape == Shape.BURST ? aim(var0, var1.range) : var0.position().add(0.0, 0.4, 0.0);
         int var4 = 0;
         if (var1.shape != Shape.SELF) {
            double var5 = var1.shape == Shape.BURST ? var1.radius : var1.range;
            ArrayList<LivingEntity> var7 = new ArrayList<>(
               var2.getEntitiesOfClass(LivingEntity.class, new AABB(var3, var3).inflate(var5 + 1.0), var1x -> canTarget(var0, var1x))
            );
            var7.sort(Comparator.comparingDouble(var1x -> var1x.distanceToSqr(var3)));

            for (LivingEntity var9 : var7) {
               Vec3 var10 = var9.position().add(0.0, Math.min(0.5, (double)(var9.getBbHeight() / 2.0F)), 0.0);
               double var11 = var10.distanceToSqr(var3);
               if (NonManorWeaponRules.within(var11, var5)) {
                  if (var1.shape == Shape.CONE) {
                     Vec3 var13 = var9.getBoundingBox().getCenter().subtract(var0.getEyePosition()).normalize();
                     if (!NonManorWeaponRules.inCone(var11, var13.dot(var0.getLookAngle()), var5)) {
                        continue;
                     }
                  }

                  if (var1.shape != Shape.BURST || clearLine(var2, var0, var3, var9.getBoundingBox().getCenter())) {
                     if (strike(var0, var9, var1, 0, var3)) {
                        var4++;
                     }

                     if (var4 >= var1.maxTargets) {
                        break;
                     }
                  }
               }
            }
         }

         switch (var1) {
            case MEAT:
               var0.heal(NonManorWeaponRules.meatHealing(var4));
               break;
            case THORNS:
               if (var4 > 0) {
                  effect(var0, MobEffects.REGENERATION, 100, 0, var0);
               }
               break;
            case CHEESE:
               effect(var0, MobEffects.MOVEMENT_SPEED, 120, 1, var0);
               break;
            case APPLE:
               var0.removeEffect(MobEffects.POISON);
               var0.heal(6.0F);
               effect(var0, MobEffects.REGENERATION, 100, 0, var0);
               effect(var0, MobEffects.MOVEMENT_SPEED, 100, 0, var0);
               break;
            case TIDE:
               effect(var0, MobEffects.DAMAGE_RESISTANCE, 120, 0, var0);
               effect(var0, MobEffects.WATER_BREATHING, 240, 0, var0);
         }

         visual(var0, var1, var3);
         return true;
      }
   }

   static boolean canTarget(ServerPlayer var0, LivingEntity var1) {
      if (var1 != var0
         && var1.isAlive()
         && !var1.isRemoved()
         && !var1.isSpectator()
         && !(var1 instanceof ArmorStand)
         && !var0.isAlliedTo(var1)
         && !var1.isAlliedTo(var0)
         && !var0.isPassengerOfSameVehicle(var1)
         && var0.hasLineOfSight(var1)) {
         if (var1 instanceof OwnableEntity var2 && var2.getOwnerUUID() != null) {
            return false;
         }

         if (var1 instanceof AbstractHorse var3 && var3.isTamed()) {
            return false;
         }

         return !(var1 instanceof Player var4)
            ? true
            : !var4.isCreative() && !var4.isSpectator() && var0.serverLevel().getServer().isPvpAllowed() && var0.canHarmPlayer(var4);
      } else {
         return false;
      }
   }

   static boolean clearLine(ServerLevel var0, ServerPlayer var1, Vec3 var2, Vec3 var3) {
      return var0.clip(new ClipContext(var2, var3, Block.COLLIDER, Fluid.NONE, var1)).getType() == Type.MISS;
   }

   static Vec3 aim(ServerPlayer var0, double var1) {
      Vec3 var3 = var0.getEyePosition();
      Vec3 var4 = var3.add(var0.getLookAngle().scale(var1));
      BlockHitResult var5 = var0.serverLevel().clip(new ClipContext(var3, var4, Block.COLLIDER, Fluid.NONE, var0));
      var4 = var5.getLocation();
      double var6 = var3.distanceToSqr(var4);

      for (LivingEntity var9 : var0.serverLevel().getEntitiesOfClass(LivingEntity.class, new AABB(var3, var4).inflate(0.3), var1x -> canTarget(var0, var1x))) {
         Optional var10 = var9.getBoundingBox().inflate(0.15).clip(var3, var4);
         if (var10.isPresent() && var3.distanceToSqr((Vec3)var10.get()) < var6) {
            var4 = (Vec3)var10.get();
            var6 = var3.distanceToSqr(var4);
         }
      }

      return var4.subtract(var0.getLookAngle().scale(0.15));
   }

   private static boolean chain(ServerPlayer var0, Skill var1) {
      Vec3 var2 = aim(var0, var1.range);
      LivingEntity var3 = var0.serverLevel()
         .getEntitiesOfClass(LivingEntity.class, new AABB(var2, var2).inflate(0.65), var1x -> canTarget(var0, var1x))
         .stream()
         .min(Comparator.comparingDouble(var1x -> var1x.getBoundingBox().getCenter().distanceToSqr(var2)))
         .orElse(null);
      if (var3 != null && NonManorWeaponRules.within(var0.getEyePosition().distanceToSqr(var3.getBoundingBox().getCenter()), var1.range)) {
         HashSet var4 = new HashSet();
         Vec3 var5 = var0.getEyePosition();

         for (int var6 = 0; var6 < var1.maxTargets && var3 != null; var6++) {
            var4.add(var3.getId());
            Vec3 var7 = var3.getBoundingBox().getCenter();
            WeaponFx.beam(var0.serverLevel(), var5, var7, Theme.ARC);
            strike(var0, var3, var1, var6, var5);
            var5 = var7;
            var3 = var0.serverLevel()
               .getEntitiesOfClass(
                  LivingEntity.class,
                  new AABB(var7, var7).inflate(var1.radius),
                  var4x -> !var4.contains(var4x.getId())
                        && canTarget(var0, var4x)
                        && NonManorWeaponRules.within(var4x.getBoundingBox().getCenter().distanceToSqr(var7), var1.radius)
                        && NonManorWeaponRules.within(var0.getEyePosition().distanceToSqr(var4x.getBoundingBox().getCenter()), var1.range)
                        && clearLine(var0.serverLevel(), var0, var7, var4x.getBoundingBox().getCenter())
               )
               .stream()
               .min(Comparator.comparingDouble(var1x -> var1x.getBoundingBox().getCenter().distanceToSqr(var7)))
               .orElse(null);
         }

         sound(var0, (SoundEvent)SoundEvents.TRIDENT_THUNDER.value(), 0.45F, 1.6F);
         return true;
      } else {
         return false;
      }
   }

   private static boolean strike(ServerPlayer var0, LivingEntity var1, Skill var2, int var3, Vec3 var4) {
      float var5 = NonManorWeaponRules.damage(var2, var0.getAttributeValue(Attributes.ATTACK_DAMAGE), var3)
         * OptionalWeaponModule.activeSkillMultiplier(var0.getMainHandItem());
      if (!var1.hurt(var0.damageSources().playerAttack(var0), var5)) {
         return false;
      } else if (!var1.isAlive()) {
         return true;
      } else {
         switch (var2) {
            case MEAT:
               knockAway(var1, var4, 0.8);
               break;
            case THORNS:
               effect(var1, MobEffects.MOVEMENT_SLOWDOWN, 60, 2, var0);
               break;
            case CHEESE:
               effect(var1, MobEffects.WEAKNESS, 100, 0, var0);
            case APPLE:
            default:
               break;
            case TIDE:
               knockAway(var1, var4, 0.8);
               break;
            case FROST:
               effect(var1, MobEffects.MOVEMENT_SLOWDOWN, 80, 1, var0);
               break;
            case ARC:
               effect(var1, MobEffects.GLOWING, 60, 0, var0);
               effect(var1, MobEffects.MOVEMENT_SLOWDOWN, 40, 1, var0);
               break;
            case COMPOST:
               effect(var1, MobEffects.POISON, 100, 1, var0);
               break;
            case HOWL:
               effect(var1, MobEffects.WEAKNESS, 100, 0, var0);
               effect(var1, MobEffects.GLOWING, 100, 0, var0);
               knockAway(var1, var4, 0.6);
               break;
            case BARREL:
               effect(var1, MobEffects.MOVEMENT_SLOWDOWN, 60, 1, var0);
               knockAway(var1, var4, 0.65);
         }

         return true;
      }
   }

   private static void effect(LivingEntity var0, Holder<MobEffect> var1, int var2, int var3, ServerPlayer var4) {
      var0.addEffect(new MobEffectInstance(var1, var2, var3), var4);
   }

   static void knockAway(LivingEntity var0, Vec3 var1, double var2) {
      Vec3 var4 = var0.position().subtract(var1).multiply(1.0, 0.0, 1.0).normalize();
      double var5 = Math.max(0.0, Math.min(1.0, var0.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
      double var7 = var2 * (1.0 - var5);
      if (!(var4.lengthSqr() < 1.0E-6) && !(var7 <= 0.0)) {
         var0.push(var4.x * var7, 0.18 * (1.0 - var5), var4.z * var7);
         var0.hurtMarked = true;
      }
   }

   private static void visual(ServerPlayer var0, Skill var1, Vec3 var2) {
      ServerLevel var3 = var0.serverLevel();
      Theme var4 = WeaponFxRules.forItem(var1.item);
      double var5 = var1.shape == Shape.SELF ? 1.2 : (var1.shape == Shape.BURST ? var1.radius : var1.range);
      WeaponFx.send(var3, var1.shape == Shape.CONE ? Kind.SWEEP : Kind.BLAST, var4, var2, var2, var5, var0.getYRot());
      if (var1.shape == Shape.BURST) {
         WeaponFx.beam(var3, var0.getEyePosition(), var2, var4);
      }
      SoundEvent var7 = switch (var1) {
         case MEAT -> (SoundEvent)SoundEvents.GENERIC_EXPLODE.value();
         case THORNS, APPLE -> SoundEvents.AMETHYST_BLOCK_CHIME;
         case CHEESE -> SoundEvents.PLAYER_BURP;
         case TIDE -> (SoundEvent)SoundEvents.TRIDENT_RIPTIDE_1.value();
         case FROST -> SoundEvents.GLASS_BREAK;
         default -> SoundEvents.PLAYER_ATTACK_SWEEP;
         case COMPOST -> SoundEvents.SLIME_SQUISH;
         case HOWL -> SoundEvents.WOLF_GROWL;
         case BARREL, WINE -> SoundEvents.GENERIC_DRINK;
      };
      sound(var0, var7, 0.85F, 1.0F);
   }

   private static void sound(ServerPlayer var0, SoundEvent var1, float var2, float var3) {
      var0.serverLevel().playSound(null, var0.blockPosition(), var1, SoundSource.PLAYERS, var2, var3);
   }
}
