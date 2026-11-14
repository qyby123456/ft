package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ImpossibleSparrowSupport;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FlywheelProjectileEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
   modid = "ftboss"
)
public final class XuanlanEncounterLimits {
   private static final int VARIANT = 6;
   private static final int LIMIT = 15;

   private XuanlanEncounterLimits() {
   }

   public static boolean blocksSupport(Entity var0) {
      if (var0 instanceof EasterBossEntity var1 && var1.getVariant() == 6) {
         return true;
      }

      return false;
   }

   public static EasterBossEntity supportTarget(EasterBossEntity var0) {
      return blocksSupport(var0) ? null : var0;
   }

   public static boolean stopSupport(EasterBossEntity var0) {
      if (var0.getVariant() == 9 && var0.level() instanceof ServerLevel var1) {
         UUID var4 = ManorFightMembers.fightId(var0);
         UUID var3 = ManorFightMembers.ownerId(var0);
         if ((var4 == null || ManorFight.currentVariant(var4) != 6) && (var3 == null || !blocksSupport(var1.getEntity(var3)))) {
            return false;
         } else {
            ImpossibleSparrowSupport.failedSpawn(var0);
            var0.discard();
            return true;
         }
      } else {
         return false;
      }
   }

   private static List<FlywheelProjectileEntity> wheels(ServerLevel var0, EasterBossEntity var1, Entity var2) {
      ArrayList<FlywheelProjectileEntity> var3 = new ArrayList<>();

      for (Entity var5 : var0.getAllEntities()) {
         if (var5 != var2 && var5 instanceof FlywheelProjectileEntity) {
            FlywheelProjectileEntity var6 = (FlywheelProjectileEntity)var5;
            if (!var6.isRemoved()) {
               Entity var7 = var6.getOwner();
               if (var7 != null && var7.getUUID().equals(var1.getUUID())) {
                  var3.add(var6);
               }
            }
         }
      }

      return var3;
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onJoin(EntityJoinLevelEvent var0) {
      if (var0.getLevel() instanceof ServerLevel var1) {
         Entity var7 = var0.getEntity();
         if (var7 instanceof EasterBossEntity var3 && stopSupport(var3)) {
            var0.setCanceled(true);
            return;
         }

         if (var7 instanceof FlywheelProjectileEntity var4
            && var4.getOwner() instanceof EasterBossEntity var5
            && blocksSupport(var5)
            && wheels(var1, var5, var4).size() >= 15) {
            var0.setCanceled(true);
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onTick(EntityTickEvent.Post var0) {
      if (!(var0.getEntity() instanceof EasterBossEntity var1) || !(var1.level() instanceof ServerLevel var2)) {
         return;
      }

      if (!stopSupport(var1) && blocksSupport(var1) && var2.getGameTime() % 20L == 0L) {
         List<FlywheelProjectileEntity> var6 = wheels(var2, var1, null);
         if (var6.size() > 15) {
            var6.sort(Comparator.comparingInt(Entity::getId));

            for (int var4 = 15; var4 < var6.size(); var4++) {
               var6.get(var4).discard();
            }
         }
      }
   }
}
