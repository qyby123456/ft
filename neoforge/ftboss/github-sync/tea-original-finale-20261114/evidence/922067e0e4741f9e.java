package net.farmingtales.ftboss.integ.spells;

import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.BossSpellRules.Kind;

public interface RegularBossCastHost {
   String ATTACK_ACTION = "signature_cast";
   String SUPPORT_ACTION = "signature_self_cast";
   int MIN_VISUAL_TICKS = 30;
   int RELEASE_TICKS = 8;

   void startSignatureCast(Kind var1, int var2);

   static String action(Kind kind) {
      return BossSpellRules.support(kind) ? "signature_self_cast" : "signature_cast";
   }

   static int visualTicks(int castTicks) {
      return Math.max(30, Math.max(0, castTicks) + 8);
   }
}
