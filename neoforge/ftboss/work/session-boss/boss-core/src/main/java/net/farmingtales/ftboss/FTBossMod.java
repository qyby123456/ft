package net.farmingtales.ftboss;

import com.mojang.logging.LogUtils;
import net.farmingtales.ftboss.block.FTBlocks;
import net.farmingtales.ftboss.block.BlankStatueContent;
import net.farmingtales.ftboss.boss.BossBarFeed;
import net.farmingtales.ftboss.boss.BossHandler;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorSpellBan;
import net.farmingtales.ftboss.boss.ManorFight.Ticker;
import net.farmingtales.ftboss.boss.ai.BossOutgoingDamageNerfs;
import net.farmingtales.ftboss.boss.ai.DandanPearlBan;
import net.farmingtales.ftboss.boss.ai.HealBan;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage;
import net.farmingtales.ftboss.boss.ai.ManorAttackFrames;
import net.farmingtales.ftboss.boss.ai.ManorChaseTracker;
import net.farmingtales.ftboss.boss.ai.NonManorDamageX2;
import net.farmingtales.ftboss.boss.ai.QingyiDamageHalved;
import net.farmingtales.ftboss.boss.ai.SafeExplosion;
import net.farmingtales.ftboss.boss.ai.ShadowVoidSplit;
import net.farmingtales.ftboss.boss.ai.XueliCrushDamage;
import net.farmingtales.ftboss.client.BossBarOverlay;
import net.farmingtales.ftboss.client.BossBarOverlay.Registration;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.integ.spells.BossIronSpells;
import net.farmingtales.ftboss.integ.spells.ManorIronSpells;
import net.farmingtales.ftboss.integ.spells.QizaiPlayerSpells;
import net.farmingtales.ftboss.integ.tridents.ManorTridentEffects;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.FTCreativeTabs;
import net.farmingtales.ftboss.item.ManorWeaponEnhancements;
import net.farmingtales.ftboss.item.ThornCrownEffects;
import net.farmingtales.ftboss.item.weapons.CleaverCombat;
import net.farmingtales.ftboss.item.weapons.PufferBatCombat;
import net.farmingtales.ftboss.network.BossBarFeedPacket;
import net.farmingtales.ftboss.skill.BossRelicDrops;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.MaqueFeatherPacket;
import net.farmingtales.ftboss.skill.YouziStatuePacket;
import net.farmingtales.ftboss.skill.YouziStatueSkills;
import net.farmingtales.ftboss.sound.FTSounds;
import net.farmingtales.ftboss.trade.TradeHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod("ftboss")
public class FTBossMod {
   public static final String MOD_ID = "ftboss";
   public static final Logger LOGGER = LogUtils.getLogger();

   public FTBossMod(IEventBus var1) {
      FTBossItems.register(var1);
      FTCreativeTabs.register(var1);
      FTEffects.register(var1);
      FTBlocks.register(var1);
      BlankStatueContent.register(var1);
      FTEntities.register(var1);
      FTSounds.register(var1);
      FTNetwork.register(var1);
      MaqueFeatherPacket.register(var1);
      NeoForge.EVENT_BUS.register(new BossHandler());
      NeoForge.EVENT_BUS.register(Ticker.class);
      NeoForge.EVENT_BUS.register(ThornCrownEffects.class);
      NeoForge.EVENT_BUS.register(BossRelicDrops.class);
      NeoForge.EVENT_BUS.register(YouziStatueSkills.class);
      NeoForge.EVENT_BUS.register(ManorWeaponEnhancements.class);
      NeoForge.EVENT_BUS.register(ManorTridentEffects.class);
      NeoForge.EVENT_BUS.register(CleaverCombat.class);
      NeoForge.EVENT_BUS.register(PufferBatCombat.class);
      YouziStatuePacket.register(var1);
      BossBarFeedPacket.register(var1);
      NeoForge.EVENT_BUS.register(BossBarFeed.class);
      if (FMLEnvironment.dist == Dist.CLIENT) {
         var1.addListener(RegisterGuiLayersEvent.class, Registration::registerOverlays);
         NeoForge.EVENT_BUS.register(BossBarOverlay.class);
      }

      NeoForge.EVENT_BUS.register(HealBan.class);
      NeoForge.EVENT_BUS.register(SafeExplosion.class);
      NeoForge.EVENT_BUS.register(ShadowVoidSplit.class);
      NeoForge.EVENT_BUS.register(ManorAdaptiveDamage.class);
      NeoForge.EVENT_BUS.register(ManorAttackFrames.class);
      NeoForge.EVENT_BUS.register(NonManorDamageX2.class);
      NeoForge.EVENT_BUS.register(QingyiDamageHalved.class);
      NeoForge.EVENT_BUS.register(BossOutgoingDamageNerfs.class);
      NeoForge.EVENT_BUS.register(LemonSummonRewards.class);
      NeoForge.EVENT_BUS.register(DandanPearlBan.class);
      NeoForge.EVENT_BUS.register(ManorChaseTracker.class);
      NeoForge.EVENT_BUS.register(XueliCrushDamage.class);
      if (ModList.get().isLoaded("irons_spellbooks")) {
         BossIronSpells.register(var1);
         ManorIronSpells.register(var1);
         QizaiPlayerSpells.register(var1);
         NeoForge.EVENT_BUS.register(ManorFightMembers.IronSummons.class);
         NeoForge.EVENT_BUS.register(ManorSpellBan.class);
         LOGGER.info("[FTBoss] 已挂载庄园禁术拦截(黑洞/止心术/橡肤)");
      }

      TradeHandler.load(FMLPaths.CONFIGDIR.get());
      LOGGER.info("[FTBoss] 糖霜女皇 Boss 加载完成 (ftboss)");
   }
}
