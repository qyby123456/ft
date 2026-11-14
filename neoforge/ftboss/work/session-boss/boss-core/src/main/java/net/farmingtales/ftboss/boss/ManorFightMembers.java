package net.farmingtales.ftboss.boss;

import io.redspace.ironsspellbooks.api.events.SetSummonOwnerEvent;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ai.LemonSacrifice;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(
   modid = "ftboss"
)
public final class ManorFightMembers {
   private static final String FIGHT = "ftboss_manor_fight";
   private static final String OWNER = "ftboss_manor_owner";

   private ManorFightMembers() {
   }

   public static UUID fightId(Entity var0) {
      return var0 != null && var0.getPersistentData().hasUUID("ftboss_manor_fight") ? var0.getPersistentData().getUUID("ftboss_manor_fight") : null;
   }

   public static UUID ownerId(Entity var0) {
      return var0 != null && var0.getPersistentData().hasUUID("ftboss_manor_owner") ? var0.getPersistentData().getUUID("ftboss_manor_owner") : null;
   }

   public static void bind(Entity var0, UUID var1) {
      var0.getPersistentData().putUUID("ftboss_manor_fight", var1);
   }

   public static void inherit(Entity var0, Entity var1) {
      UUID var2 = fightId(var0);
      if (var0 != null && var1 != null && var1 != var0) {
         LemonSacrifice.inheritLifetime(var0, var1);
         UUID var3 = ownerId(var0);
         var1.getPersistentData().putUUID("ftboss_manor_owner", var3 == null ? var0.getUUID() : var3);
         if (var2 != null) {
            bind(var1, var2);
            if (var1 instanceof EasterBossEntity var4) {
               var4.setChallengeDifficulty(var0 instanceof EasterBossEntity var5 ? var5.getChallengeDifficulty() : ManorFight.difficulty(var2));
               var4.setManorSummonerId(var0 instanceof EasterBossEntity var6 ? var6.getManorSummonerId() : ManorFight.summoner(var2));
            }
         }
      }
   }

   public static Entity owner(Entity var0) {
      if (var0 == null) {
         return null;
      } else if (var0 instanceof Projectile var1) {
         return var1.getOwner();
      } else if (var0 instanceof TraceableEntity var2) {
         return var2.getOwner();
      } else if (var0 instanceof OwnableEntity var3) {
         return var3.getOwner();
      } else {
         UUID var4 = ownerId(var0);
         return var4 != null && var0.level() instanceof ServerLevel var5 ? var5.getEntity(var4) : null;
      }
   }

   public static Entity resolve(Entity var0) {
      for (int var1 = 0; var0 != null && var1 < 8; var1++) {
         if (fightId(var0) != null) {
            return var0;
         }

         Entity var2 = owner(var0);
         if (var2 == null || var2 == var0) {
            break;
         }

         var0 = var2;
      }

      return var0;
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onJoin(EntityJoinLevelEvent var0) {
      if (var0.getLevel() instanceof ServerLevel var1) {
         Entity var6 = var0.getEntity();
         Entity var3 = owner(var6);
         if (var3 != null) {
            inherit(resolve(var3), var6);
         }

         UUID var4 = fightId(var6);
         ManorFightData var5 = ManorFightData.get(var1.getServer());
         if (var5.retired(var4) || var5.retired(var6.getUUID())) {
            var0.setCanceled(true);
         }
      }
   }

   public static final class IronSummons {
      @SubscribeEvent
      public static void onSummonOwner(SetSummonOwnerEvent var0) {
         if (var0.getSummon() != null && !var0.getSummon().level().isClientSide) {
            ManorFightMembers.inherit(ManorFightMembers.resolve(var0.getOwner()), var0.getSummon());
         }
      }
   }
}
