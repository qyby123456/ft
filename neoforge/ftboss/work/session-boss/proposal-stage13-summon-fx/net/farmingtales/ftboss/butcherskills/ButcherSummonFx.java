package net.farmingtales.ftboss.butcherskills;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class ButcherSummonFx {
   private static final Map<ServerLevel, Pulses> SUMMONS = new WeakHashMap<>();

   private ButcherSummonFx() {
   }

   public static boolean addWithFx(ServerLevel var0, Entity var1) {
      boolean var2 = var0.addFreshEntity(var1);
      if (var2 && var1 instanceof LivingEntity) {
         ButcherSummonFx.Pulses var3 = SUMMONS.computeIfAbsent(var0, var0x -> new ButcherSummonFx.Pulses());
         if (var3.entries.size() >= 24) {
            var3.entries.remove(0);
         }

         int var4 = Math.floorMod(var1.getUUID().hashCode(), 4);
         var3.entries.add(new ButcherSummonFx.Pulse(var1.position(), var0.getGameTime(), var1 instanceof MeatMountainEntity, var4));
         ButcherSkillFx.summon(var0, var1.position(), 0, var1 instanceof MeatMountainEntity);
      }

      return var2;
   }

   public static void tickSummons(ServerLevel var0, long var1) {
      ButcherSummonFx.Pulses var3 = SUMMONS.get(var0);
      if (var3 != null && var3.lastTick != var1) {
         var3.lastTick = var1;
         var3.entries.removeIf(var2 -> var1 - var2.start() > 32L);

         for (ButcherSummonFx.Pulse var5 : var3.entries) {
            int var6 = (int)(var1 - var5.start());
            if (var6 > 0 && var6 % 4 == var5.phase()) {
               ButcherSkillFx.summon(var0, var5.center(), var6, var5.boss());
            }
         }

         if (var3.entries.isEmpty()) {
            SUMMONS.remove(var0);
         }
      }
   }

   private static record Pulse(Vec3 center, long start, boolean boss, int phase) {
   }

   private static final class Pulses {
      final List<ButcherSummonFx.Pulse> entries = new ArrayList<>();
      long lastTick = Long.MIN_VALUE;
   }
}
