package net.farmingtales.ftboss.util;

import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.util.BossSpellRules.1;
import net.farmingtales.ftboss.util.BossSpellRules.Kind;

public final class BossSpellRules {
   public static final int MAX_LEVEL = 1;
   public static final int FULL_POWER_LEVEL = 5;
   public static final int CAST_TICKS = 32;
   public static final double RANGE = 24.0;
   public static final double RADIUS = 4.0;
   public static final int MAX_TARGETS = 16;
   private static final Map<String, List<Kind>> PAIRS = Map.of(
      "ftboss:sugar_queen",
      List.of(Kind.FROST, Kind.FROST_LANCE, Kind.FROST_HAIL, Kind.FROST_PRISM, Kind.FROST_SHATTER),
      "ftboss:tea_tree",
      List.of(Kind.TEA, Kind.TEA_SANCTUARY, Kind.TEA_THORNS, Kind.TEA_FLESH, Kind.TEA_POLLEN, Kind.TEA_SUTURE, Kind.TEA_RENEWAL),
      "ftboss:wine_barrel",
      List.of(Kind.WINE, Kind.WINE_BOMB, Kind.WINE_BREATH, Kind.WINE_TOAST, Kind.WINE_FUSE),
      "ftboss:defect_core",
      List.of(Kind.DEFECT, Kind.DEFECT_PULSE, Kind.DEFECT_RAIL, Kind.DEFECT_STORM, Kind.DEFECT_REBOOT),
      "ftboss:compost_boss",
      List.of(Kind.COMPOST, Kind.COMPOST_REAP, Kind.COMPOST_ERUPTION, Kind.COMPOST_ROT, Kind.COMPOST_MULCH),
      "ftboss:apple_boss",
      List.of(Kind.APPLE, Kind.APPLE_AEGIS, Kind.APPLE_ORBIT, Kind.APPLE_SEED, Kind.APPLE_HARVEST),
      "ftboss:tide_fish_boss",
      List.of(Kind.TIDE, Kind.TIDE_GEYSER, Kind.TIDE_RIP, Kind.TIDE_BUBBLE, Kind.TIDE_SURGE)
   );

   private BossSpellRules() {
   }

   public static Kind forEntity(String id) {
      List<Kind> pair = forBoss(id);
      return pair.isEmpty() ? null : pair.get(0);
   }

   public static List<Kind> forBoss(String id) {
      return id == null ? List.of() : PAIRS.getOrDefault(id, List.of());
   }

   public static boolean healing(Kind kind) {
      return kind == Kind.TEA_SANCTUARY || kind == Kind.TEA_RENEWAL || kind == Kind.DEFECT_REBOOT || kind == Kind.COMPOST_MULCH;
   }

   public static boolean shielding(Kind kind) {
      return kind == Kind.APPLE_AEGIS || kind == Kind.FROST_PRISM || kind == Kind.WINE_TOAST || kind == Kind.TIDE_BUBBLE;
   }

   public static boolean support(Kind kind) {
      return healing(kind) || shielding(kind);
   }

   public static boolean selfCentered(Kind kind) {
      return support(kind) || kind == Kind.DEFECT_PULSE || kind == Kind.APPLE_ORBIT;
   }

   public static boolean expanded(Kind kind) {
      return kind.ordinal() >= Kind.FROST_HAIL.ordinal();
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static int firstPulse(Kind kind) {
      return switch (1.$SwitchMap$net$farmingtales$ftboss$util$BossSpellRules$Kind[kind.ordinal()]) {
         case 1, 2, 3, 4 -> 20;
         case 5 -> 24;
         case 6 -> 16;
         default -> 0;
      };
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static int pulseCount(Kind kind) {
      return switch (1.$SwitchMap$net$farmingtales$ftboss$util$BossSpellRules$Kind[kind.ordinal()]) {
         case 3, 4, 7, 8, 9, 10, 11, 12, 13, 14, 15 -> 3;
         default -> 1;
         case 16 -> 4;
      };
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static int pulseInterval(Kind kind) {
      return switch (1.$SwitchMap$net$farmingtales$ftboss$util$BossSpellRules$Kind[kind.ordinal()]) {
         case 3, 11 -> 12;
         case 4, 8 -> 10;
         default -> 20;
      };
   }

   public static int lastPulse(Kind kind) {
      return firstPulse(kind) + (pulseCount(kind) - 1) * pulseInterval(kind);
   }

   public static int pulseIndex(Kind kind, long age) {
      long elapsed = age - (long)firstPulse(kind);
      return elapsed >= 0L && age <= (long)lastPulse(kind) && elapsed % (long)pulseInterval(kind) == 0L ? (int)(elapsed / (long)pulseInterval(kind)) : -1;
   }

   public static int castTicks(Kind kind) {
      return kind != Kind.FROST_LANCE && kind != Kind.DEFECT_PULSE ? 32 : 20;
   }

   public static int delay(Kind kind) {
      return kind == Kind.WINE_BOMB ? 16 : (kind == Kind.TIDE_GEYSER ? 20 : 0);
   }

   public static float bonusDamage(float damage, boolean marked) {
      return damage * (marked ? 1.5F : 1.0F);
   }

   public static int shield(float power) {
      return Math.max(4, Math.min(40, Math.round(power / 4.0F) * 4));
   }

   public static float blastScale(double distance) {
      return (float)Math.max(0.5, Math.min(1.0, 1.0 - distance / 10.0));
   }

   public static int level(int level) {
      return 5;
   }

   public static int chainTargets(int level) {
      return 3 + (level(level) - 1) / 2;
   }

   public static float chainScale(int jump) {
      return (float)Math.pow(0.8, (double)Math.max(0, Math.min(4, jump)));
   }

   public static boolean cone(double forward, double sideways) {
      return Double.isFinite(forward) && Double.isFinite(sideways) && forward >= 0.0 && forward <= 12.0 && Math.abs(sideways) <= 1.2 + forward * 0.45;
   }

   public static boolean sporePulse(int elapsed) {
      return elapsed == 0 || elapsed == 20 || elapsed == 40;
   }
}
