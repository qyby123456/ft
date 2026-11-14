package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.config.SpellConfigParameter;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.farmingtales.ftboss.boss.NonManorFinaleDifficulty;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning.1;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.ManorBossCombatRules;
import net.farmingtales.ftboss.util.BossSpellRules.Kind;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

public final class BossSpellTuning {
   public static final double BOSS_CAST_MULT = 0.6666666666666666;
   public static final float PLAYER_POWER_MULT = 2.0F;
   public static final int PLAYER_BUFF_MULT = 2;

   private BossSpellTuning() {
   }

   public static int bossCast(int ticks) {
      return ticks <= 0 ? 0 : Math.max(1, (int)Math.ceil((double)ticks * 0.6666666666666666));
   }

   public static int bossCast(LivingEntity caster, int ticks) {
      return NonManorFinaleDifficulty.acceleratedTicks(caster, bossCast(ticks));
   }

   public static boolean regularBoss(LivingEntity caster) {
      return caster != null && !BossSpellRules.forBoss(EntityType.m_20613_(caster.m_6095_()).toString()).isEmpty();
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static float signaturePower(Kind kind) {
      float baseline = switch (1.$SwitchMap$net$farmingtales$ftboss$util$BossSpellRules$Kind[kind.ordinal()]) {
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
         default -> throw new IncompatibleClassChangeError();
      };
      return BossSpellRules.support(kind) ? baseline : baseline * 2.0F;
   }

   public static float scaledPower(AbstractSpell spell, LivingEntity caster, float base) {
      double config = (Double)SpellConfigManager.getSpellConfigValue(spell, SpellConfigParameter.POWER_MULTIPLIER);
      double power = (double)base * caster.m_21133_((Attribute)AttributeRegistry.SPELL_POWER.get()) * spell.getSchoolType().getPowerFor(caster) * config;
      return Double.isFinite(power) ? (float)Math.max(0.0, Math.min(Float.MAX_VALUE, power)) : 0.0F;
   }

   public static void fasterLegacyCast(AbstractSpellCastingMob caster, AbstractSpell spell) {
      if (regularBoss(caster) && !(spell instanceof BossSignatureSpell) && spell.getCastType() == CastType.LONG && caster.isCasting()) {
         MagicData magic = caster.getMagicData();
         if (spell.getSpellId().equals(magic.getCastingSpellId())) {
            magic.initiateCast(
               spell, magic.getCastingSpellLevel(), bossCast(caster, magic.getCastDuration()), magic.getCastSource(), magic.getCastingEquipmentSlot()
            );
         }
      }
   }

   public static boolean manorBoss(LivingEntity caster) {
      if (caster instanceof EasterBossEntity boss && !boss.isMini() && !boss.isGiant()) {
         return true;
      }

      return false;
   }

   public static boolean playerCast(LivingEntity caster) {
      return caster instanceof Player;
   }

   public static float playerPower(LivingEntity caster, float power) {
      return playerCast(caster) ? power * 2.0F : power;
   }

   public static int buffTicks(LivingEntity caster, MobEffect effect, int ticks) {
      return playerCast(caster) && effect != null && effect.m_19486_() ? ticks * 2 : ticks;
   }

   public static double radiusScale(LivingEntity caster) {
      return manorBoss(caster) ? ManorBossCombatRules.radiusScale(((EasterBossEntity)caster).getVariant()) : 1.0;
   }

   public static double radius(LivingEntity caster, double baseRadius) {
      return manorBoss(caster) ? ManorBossCombatRules.spellRadius(((EasterBossEntity)caster).getVariant(), baseRadius) : baseRadius;
   }
}
