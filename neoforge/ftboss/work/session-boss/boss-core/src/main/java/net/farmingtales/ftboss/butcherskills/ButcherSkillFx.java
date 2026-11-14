package net.farmingtales.ftboss.butcherskills;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class ButcherSkillFx {
   private static final DustParticleOptions CRIMSON = new DustParticleOptions(new Vector3f(0.85F, 0.06F, 0.12F), 0.75F);
   private static final DustParticleOptions BONE = new DustParticleOptions(new Vector3f(1.0F, 0.72F, 0.38F), 0.75F);
   private static final Map<ServerLevel, ButcherSkillRules.ParticleBudget> BUDGETS = new WeakHashMap<>();

   private ButcherSkillFx() {
   }

   private static void emit(ServerLevel var0, Vec3 var1, Vec3 var2, List<ButcherFxPlan.Point> var3, boolean var4) {
      ButcherSkillRules.ParticleBudget var5 = BUDGETS.computeIfAbsent(var0, var0x -> new ButcherSkillRules.ParticleBudget());
      long var6 = var0.getGameTime();

      for (ButcherFxPlan.Point var9 : var3) {
         if (!var5.take(var6, var4)) {
            break;
         }

         var0.sendParticles(
            var9.bone() ? BONE : CRIMSON,
            var1.x + var2.x * var9.offsetX() - var2.z * var9.offsetZ(),
            var1.y + var9.height(),
            var1.z + var2.z * var9.offsetX() + var2.x * var9.offsetZ(),
            1,
            0.018,
            0.018,
            0.018,
            0.0
         );
      }
   }

   public static void telegraph(ServerLevel var0, ButcherSkillRules.Skill var1, Vec3 var2, Vec3 var3, int var4) {
      if (var4 % 4 == 0) {
         emit(var0, var2, var3, ButcherFxPlan.spell(var1, var4), false);
      }
   }

   public static void release(ServerLevel var0, ButcherSkillRules.Skill var1, Vec3 var2, Vec3 var3) {
      emit(var0, var2, var3, ButcherFxPlan.spell(var1, var1.windup), false);
   }

   public static void summon(ServerLevel var0, Vec3 var1, int var2, boolean var3) {
      emit(var0, var1, new Vec3(1.0, 0.0, 0.0), ButcherFxPlan.summon(var3, var2), true);
   }
}
