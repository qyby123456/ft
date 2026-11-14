package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.config.SpellConfigParameter;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.farmingtales.ftboss.boss.NonManorFinale;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning.1;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.ManorBossCombatRules;
import net.farmingtales.ftboss.util.BossSpellRules.Kind;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class BossSpellTuning {
   public static final double BOSS_CAST_MULT = 0.6666666666666666;
   public static final float PLAYER_POWER_MULT = 2.0F;
   public static final int PLAYER_BUFF_MULT = 2;

   private BossSpellTuning() {
   }

   public static int bossCast(int var0) {
      return var0 <= 0 ? 0 : Math.max(1, (int)Math.ceil((double)var0 * 0.6666666666666666));
   }

   public static int bossCast(LivingEntity var0, int var1) {
      return NonManorFinale.acceleratedTicks(var0, bossCast(var1));
   }

   public static boolean regularBoss(LivingEntity var0) {
      return var0 != null && !BossSpellRules.forBoss(EntityType.getKey(var0.getType()).toString()).isEmpty();
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static float signaturePower(Kind var0) {
      float var1 = switch (1.$SwitchMap$net$farmingtales$ftboss$util$BossSpellRules$Kind[var0.ordinal()]) {
         case 1, 2, 3, 4, 5 -> 34.0F;
         case 6, 7 -> 30.0F;
         case 8, 9, 10 -> 32.0F;
         case 11, 12, 13, 14 -> 24.0F;
         case 15, 16, 17 -> 36.0F;
         case 18, 19, 20 -> 40.0F;
         case 21, 22 -> 42.0F;
         case 23, 24 -> 44.0F;
         case 25 -> 26.0F;
         case 26 -> 21.0F;
         case 27, 28 -> 23.0F;
         case 29 -> 22.0F;
         case 30, 31, 32 -> 10.0F;
         case 33, 34 -> 8.0F;
         case 35, 36 -> 12.0F;
         case 37 -> 9.0F;
         default -> throw new MatchException(null, null);
      };
      return BossSpellRules.support(var0) ? var1 : var1 * 2.0F;
   }

   public static float scaledPower(AbstractSpell var0, LivingEntity var1, float var2) {
      double var3 = (Double)SpellConfigManager.getSpellConfigValue(var0, SpellConfigParameter.POWER_MULTIPLIER);
      double var5 = (double)var2 * var1.getAttributeValue(AttributeRegistry.SPELL_POWER) * var0.getSchoolType().getPowerFor(var1) * var3;
      return Double.isFinite(var5) ? (float)Math.max(0.0, Math.min(Float.MAX_VALUE, var5)) : 0.0F;
   }

   public static void fasterLegacyCast(AbstractSpellCastingMob var0, AbstractSpell var1) {
      if (regularBoss(var0) && !(var1 instanceof BossSignatureSpell) && var1.getCastType() == CastType.LONG && var0.isCasting()) {
         MagicData var2 = var0.getMagicData();
         if (var1.getSpellId().equals(var2.getCastingSpellId())) {
            var2.initiateCast(var1, var2.getCastingSpellLevel(), bossCast(var0, var2.getCastDuration()), var2.getCastSource(), var2.getCastingEquipmentSlot());
         }
      }
   }

   public static boolean manorBoss(LivingEntity var0) {
      if (var0 instanceof EasterBossEntity var1 && !var1.isMini() && !var1.isGiant()) {
         return true;
      }

      return false;
   }

   public static boolean playerCast(LivingEntity var0) {
      return var0 instanceof Player;
   }

   public static float playerPower(LivingEntity var0, float var1) {
      return playerCast(var0) ? var1 * 2.0F : var1;
   }

   public static int buffTicks(LivingEntity var0, Holder<MobEffect> var1, int var2) {
      return playerCast(var0) && var1 != null && ((MobEffect)var1.value()).isBeneficial() ? var2 * 2 : var2;
   }

   public static double radiusScale(LivingEntity var0) {
      return manorBoss(var0) ? ManorBossCombatRules.radiusScale(((EasterBossEntity)var0).getVariant()) : 1.0;
   }

   public static double radius(LivingEntity var0, double var1) {
      return manorBoss(var0) ? ManorBossCombatRules.spellRadius(((EasterBossEntity)var0).getVariant(), var1) : var1;
   }
}
