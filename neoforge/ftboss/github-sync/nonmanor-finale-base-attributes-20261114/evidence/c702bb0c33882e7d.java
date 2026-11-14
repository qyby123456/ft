package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.LemonSupportFinale;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(
   modid = "ftboss",
   bus = Bus.FORGE
)
public final class NonManorFinaleDifficulty {
   public static final double ATTRIBUTE_MULTIPLIER = 2.0;
   public static final int CAST_SPEED_MULTIPLIER = 2;
   private static final String PREPARED = "ftboss:regular_finale_prepared";
   private static final String FINALE = "ftboss:regular_finale";
   private static final String BASE_PREFIX = "ftboss:regular_finale_base/";
   private static final String SCALING_BASE_HP = "ftboss_base_hp";

   private NonManorFinaleDifficulty() {
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onJoin(EntityJoinLevelEvent event) {
      if (!LemonSupportFinale.onJoin(event)) {
         if (!event.getLevel().f_46443_
            && event.getLevel() instanceof ServerLevel level
            && event.getEntity() instanceof LivingEntity boss
            && isRegularBoss(boss)) {
            if (event.loadedFromDisk()) {
               ensureFinaleMaximumHealth(boss, false);
            } else {
               ServerPlayer summoner = null;
               double best = Double.MAX_VALUE;

               for (ServerPlayer candidate : level.m_6443_(ServerPlayer.class, boss.m_20191_().m_82400_(16.0), player -> player.m_6084_() && !player.m_5833_())) {
                  double distance = candidate.m_20280_(boss);
                  if (distance < best) {
                     best = distance;
                     summoner = candidate;
                  }
               }

               prepare(boss, summoner != null && BossChallengePreference.selected(summoner) == ManorDifficulty.IMPOSSIBLE);
            }
         }
      }
   }

   public static void prepare(LivingEntity boss, boolean finale) {
      if (boss != null && !boss.m_9236_().f_46443_ && isRegularBoss(boss)) {
         CompoundTag data = boss.getPersistentData();
         if (!data.m_128471_("ftboss:regular_finale_prepared")) {
            List<NonManorFinaleDifficulty.AttributeBase> bases = new ArrayList<>();
            AttributeInstance maximumHealth = boss.m_21051_(Attributes.f_22276_);
            rememberBase(data, bases, maximumHealth);

            for (AttributeInstance instance : boss.m_21204_().m_22170_()) {
               if (instance != maximumHealth) {
                  rememberBase(data, bases, instance);
               }
            }

            data.m_128379_("ftboss:regular_finale_prepared", true);
            data.m_128379_("ftboss:regular_finale", finale);
            if (boss instanceof TeaTreeBossEntity saya) {
               saya.setFinaleChallenge(finale);
            }

            if (finale) {
               for (NonManorFinaleDifficulty.AttributeBase entry : bases) {
                  entry.instance().m_22100_(entry.instance().m_22099_().m_6740_(entry.base() * 2.0));
               }

               boss.m_21153_(boss.m_21233_());
               FTBossMod.LOGGER.info("[FTBoss][终结难度] {} 属性×2、施法速度×2", boss.m_7755_().getString());
            }
         }
      }
   }

   private static void rememberBase(CompoundTag data, List<NonManorFinaleDifficulty.AttributeBase> bases, AttributeInstance instance) {
      if (instance != null) {
         ResourceLocation id = ForgeRegistries.ATTRIBUTES.getKey(instance.m_22099_());
         if (id != null) {
            String key = "ftboss:regular_finale_base/" + id;
            if (!data.m_128441_(key)) {
               data.m_128347_(key, instance.m_22115_());
            }

            bases.add(new NonManorFinaleDifficulty.AttributeBase(instance, data.m_128459_(key)));
         }
      }
   }

   private static void ensureFinaleMaximumHealth(LivingEntity boss, boolean fullHeal) {
      if (isFinale(boss)) {
         AttributeInstance maximumHealth = boss.m_21051_(Attributes.f_22276_);
         if (maximumHealth != null) {
            ResourceLocation id = ForgeRegistries.ATTRIBUTES.getKey(maximumHealth.m_22099_());
            if (id != null) {
               CompoundTag data = boss.getPersistentData();
               String key = "ftboss:regular_finale_base/" + id;
               if (!data.m_128441_(key)) {
                  double base = data.m_128441_("ftboss_base_hp") ? data.m_128459_("ftboss_base_hp") : maximumHealth.m_22115_();
                  data.m_128347_(key, base);
               }

               double desired = maximumHealth.m_22099_().m_6740_(data.m_128459_(key) * 2.0);
               float oldMaximum = boss.m_21233_();
               float ratio = oldMaximum > 0.0F ? boss.m_21223_() / oldMaximum : 1.0F;
               if (Math.abs(maximumHealth.m_22115_() - desired) > 0.01) {
                  maximumHealth.m_22100_(desired);
                  if (fullHeal) {
                     boss.m_21153_(boss.m_21233_());
                  } else {
                     boss.m_21153_(Math.max(1.0F, Math.min(boss.m_21233_(), ratio * boss.m_21233_())));
                  }
               }
            }
         }
      }
   }

   public static boolean isFinale(LivingEntity boss) {
      return boss != null
         && (boss.getPersistentData().m_128471_("ftboss:regular_finale") || boss instanceof TeaTreeBossEntity saya && saya.isFinaleChallenge());
   }

   public static int acceleratedTicks(LivingEntity boss, int normalTicks) {
      if (normalTicks <= 0) {
         return normalTicks;
      } else {
         return isFinale(boss) ? Math.max(1, (normalTicks + 2 - 1) / 2) : normalTicks;
      }
   }

   public static boolean isRegularBoss(LivingEntity boss) {
      if (boss instanceof EasterBossEntity) {
         return false;
      } else if (boss instanceof RatBossEntity rat) {
         return !rat.isMinion();
      } else {
         return boss instanceof TeaTreeBossEntity saya
            ? !saya.isTeaMinion()
            : boss instanceof SugarQueenEntity
               || boss instanceof MeatMountainEntity
               || boss instanceof WineBarrelBossEntity
               || boss instanceof DefectCoreEntity
               || boss instanceof CompostBossEntity
               || boss instanceof AppleBossEntity
               || boss instanceof TideFishBossEntity
               || boss instanceof BengerEntity;
      }
   }

   private static record AttributeBase(AttributeInstance instance, double base) {
   }
}
