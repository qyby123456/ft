package net.farmingtales.ftboss.util;

import java.util.List;
import java.util.Map;
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

   public static Kind forEntity(String var0) {
      List var1 = forBoss(var0);
      return var1.isEmpty() ? null : (Kind)var1.get(0);
   }

   public static List<Kind> forBoss(String var0) {
      return var0 == null ? List.of() : PAIRS.getOrDefault(var0, List.of());
   }

   public static boolean healing(Kind var0) {
      return var0 == Kind.TEA_SANCTUARY || var0 == Kind.TEA_RENEWAL || var0 == Kind.DEFECT_REBOOT || var0 == Kind.COMPOST_MULCH;
   }

   public static boolean shielding(Kind var0) {
      return var0 == Kind.APPLE_AEGIS || var0 == Kind.FROST_PRISM || var0 == Kind.WINE_TOAST || var0 == Kind.TIDE_BUBBLE;
   }

   public static boolean support(Kind var0) {
      return healing(var0) || shielding(var0);
   }

   public static boolean selfCentered(Kind var0) {
      return support(var0) || var0 == Kind.DEFECT_PULSE || var0 == Kind.APPLE_ORBIT;
   }

   public static boolean expanded(Kind var0) {
      return var0.ordinal() >= Kind.FROST_HAIL.ordinal();
   }

   public static int firstPulse(Kind param0) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.NullPointerException: Cannot read field "classStruct" because "classNode" is null
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifyNewEnumSwitch(SwitchHelper.java:319)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplify(SwitchHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:30)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:376)
      //
      // Bytecode:
      // 00: aload 0
      // 01: invokevirtual net/farmingtales/ftboss/util/BossSpellRules$Kind.ordinal ()I
      // 04: lookupswitch 75 6 16 60 20 60 24 60 28 70 32 65 36 60
      // 40: bipush 20
      // 42: goto 50
      // 45: bipush 24
      // 47: goto 50
      // 4a: bipush 16
      // 4c: goto 50
      // 4f: bipush 0
      // 50: ireturn
   }

   public static int pulseCount(Kind param0) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.NullPointerException: Cannot read field "classStruct" because "classNode" is null
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifyNewEnumSwitch(SwitchHelper.java:319)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplify(SwitchHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:30)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:376)
      //
      // Bytecode:
      // 00: aload 0
      // 01: invokevirtual net/farmingtales/ftboss/util/BossSpellRules$Kind.ordinal ()I
      // 04: tableswitch 116 14 36 108 116 116 108 112 108 116 108 108 116 108 116 108 116 116 108 116 108 116 116 108 116 108
      // 70: bipush 3
      // 71: goto 79
      // 74: bipush 4
      // 75: goto 79
      // 78: bipush 1
      // 79: ireturn
   }

   public static int pulseInterval(Kind param0) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.NullPointerException: Cannot read field "classStruct" because "classNode" is null
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifyNewEnumSwitch(SwitchHelper.java:319)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplify(SwitchHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:30)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:376)
      //
      // Bytecode:
      // 00: aload 0
      // 01: invokevirtual net/farmingtales/ftboss/util/BossSpellRules$Kind.ordinal ()I
      // 04: lookupswitch 54 4 17 44 22 49 24 49 36 44
      // 30: bipush 10
      // 32: goto 3c
      // 35: bipush 12
      // 37: goto 3c
      // 3a: bipush 20
      // 3c: ireturn
   }

   public static int lastPulse(Kind var0) {
      return firstPulse(var0) + (pulseCount(var0) - 1) * pulseInterval(var0);
   }

   public static int pulseIndex(Kind var0, long var1) {
      long var3 = var1 - (long)firstPulse(var0);
      return var3 >= 0L && var1 <= (long)lastPulse(var0) && var3 % (long)pulseInterval(var0) == 0L ? (int)(var3 / (long)pulseInterval(var0)) : -1;
   }

   public static int castTicks(Kind var0) {
      return var0 != Kind.FROST_LANCE && var0 != Kind.DEFECT_PULSE ? 32 : 20;
   }

   public static int delay(Kind var0) {
      return var0 == Kind.WINE_BOMB ? 16 : (var0 == Kind.TIDE_GEYSER ? 20 : 0);
   }

   public static float bonusDamage(float var0, boolean var1) {
      return var0 * (var1 ? 1.5F : 1.0F);
   }

   public static int shield(float var0) {
      return Math.max(4, Math.min(40, Math.round(var0 / 4.0F) * 4));
   }

   public static float blastScale(double var0) {
      return (float)Math.max(0.5, Math.min(1.0, 1.0 - var0 / 10.0));
   }

   public static int level(int var0) {
      return 5;
   }

   public static int chainTargets(int var0) {
      return 3 + (level(var0) - 1) / 2;
   }

   public static float chainScale(int var0) {
      return (float)Math.pow(0.8, (double)Math.max(0, Math.min(4, var0)));
   }

   public static boolean cone(double var0, double var2) {
      return Double.isFinite(var0) && Double.isFinite(var2) && var0 >= 0.0 && var0 <= 12.0 && Math.abs(var2) <= 1.2 + var0 * 0.45;
   }

   public static boolean sporePulse(int var0) {
      return var0 == 0 || var0 == 20 || var0 == 40;
   }
}
