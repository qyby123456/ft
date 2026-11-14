/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.fml.ModList
 *  net.minecraftforge.fml.common.Mod
 *  net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
 *  net.minecraftforge.fml.loading.FMLPaths
 *  org.slf4j.Logger
 */
package net.farmingtales.ftboss;

import com.mojang.logging.LogUtils;
import net.farmingtales.ftboss.block.BlankStatueContent;
import net.farmingtales.ftboss.block.FTBlocks;
import net.farmingtales.ftboss.boss.BossHandler;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorSpellBan;
import net.farmingtales.ftboss.boss.ai.BossOutgoingDamageNerfs;
import net.farmingtales.ftboss.boss.ai.DandanPearlBan;
import net.farmingtales.ftboss.boss.ai.HealBan;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage;
import net.farmingtales.ftboss.boss.ai.ManorArenaChunks;
import net.farmingtales.ftboss.boss.ai.ManorAttackFrames;
import net.farmingtales.ftboss.boss.ai.ManorFinalDamage;
import net.farmingtales.ftboss.boss.ai.NonManorDamageX2;
import net.farmingtales.ftboss.boss.ai.QingyiDamageHalved;
import net.farmingtales.ftboss.boss.ai.SafeExplosion;
import net.farmingtales.ftboss.boss.ai.ShadowVoidSplit;
import net.farmingtales.ftboss.boss.ai.XueliCrushDamage;
import net.farmingtales.ftboss.boss.ai.XueliMaids;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.integ.spells.BossIronSpells;
import net.farmingtales.ftboss.integ.spells.ManorIronSpells;
import net.farmingtales.ftboss.integ.spells.QizaiPlayerSpells;
import net.farmingtales.ftboss.integ.tridents.SanguineBondHooks;
import net.farmingtales.ftboss.integ.tridents.TridentIntegration;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.FTCreativeTabs;
import net.farmingtales.ftboss.item.ManorWeaponEnhancements;
import net.farmingtales.ftboss.item.ThornCrownEffects;
import net.farmingtales.ftboss.item.weapons.CleaverCombat;
import net.farmingtales.ftboss.item.weapons.PufferBatCombat;
import net.farmingtales.ftboss.skill.BossRelicDrops;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.MaqueFeatherPacket;
import net.farmingtales.ftboss.skill.YouziStatuePacket;
import net.farmingtales.ftboss.skill.YouziStatueSkills;
import net.farmingtales.ftboss.sound.FTSounds;
import net.farmingtales.ftboss.trade.TradeHandler;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionRuntime;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

@Mod(value="ftboss")
public class FTBossMod {
    public static final String MOD_ID = "ftboss";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FTBossMod() {
        MinecraftForge.EVENT_BUS.register((Object)this);
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        TridentIntegration.register(modBus);
        MinecraftForge.EVENT_BUS.register(SanguineBondHooks.class);
        FTBossItems.register(modBus);
        FTCreativeTabs.register(modBus);
        FTEffects.register(modBus);
        FTBlocks.register(modBus);
        BlankStatueContent.register(modBus);
        FTEntities.register(modBus);
        FTSounds.register(modBus);
        FTNetwork.register();
        MaqueFeatherPacket.register();
        FourWeaponTransactionRuntime.register();
        MinecraftForge.EVENT_BUS.register((Object)new BossHandler());
        MinecraftForge.EVENT_BUS.register(ManorFight.Ticker.class);
        MinecraftForge.EVENT_BUS.register(ThornCrownEffects.class);
        MinecraftForge.EVENT_BUS.register(BossRelicDrops.class);
        MinecraftForge.EVENT_BUS.register(YouziStatueSkills.class);
        MinecraftForge.EVENT_BUS.register(ManorWeaponEnhancements.class);
        MinecraftForge.EVENT_BUS.register(CleaverCombat.class);
        MinecraftForge.EVENT_BUS.register(PufferBatCombat.class);
        YouziStatuePacket.register();
        MinecraftForge.EVENT_BUS.register(HealBan.class);
        MinecraftForge.EVENT_BUS.register(SafeExplosion.class);
        MinecraftForge.EVENT_BUS.register(ShadowVoidSplit.class);
        MinecraftForge.EVENT_BUS.register(ManorAdaptiveDamage.class);
        MinecraftForge.EVENT_BUS.register(ManorAttackFrames.class);
        MinecraftForge.EVENT_BUS.register(NonManorDamageX2.class);
        MinecraftForge.EVENT_BUS.register(QingyiDamageHalved.class);
        MinecraftForge.EVENT_BUS.register(BossOutgoingDamageNerfs.class);
        MinecraftForge.EVENT_BUS.register(LemonSummonRewards.class);
        MinecraftForge.EVENT_BUS.register(DandanPearlBan.class);
        MinecraftForge.EVENT_BUS.register(XueliMaids.class);
        MinecraftForge.EVENT_BUS.register(XueliCrushDamage.class);
        if (ModList.get().isLoaded("irons_spellbooks")) {
            BossIronSpells.register(modBus);
            ManorIronSpells.register(modBus);
            QizaiPlayerSpells.register(modBus);
            MinecraftForge.EVENT_BUS.register(ManorFightMembers.IronSummons.class);
            MinecraftForge.EVENT_BUS.register(ManorSpellBan.class);
            LOGGER.info("[FTBoss] \u5df2\u6302\u8f7d\u5e84\u56ed\u7981\u672f\u62e6\u622a(\u9ed1\u6d1e/\u6b62\u5fc3\u672f/\u6a61\u80a4)");
        }
        TradeHandler.load(FMLPaths.CONFIGDIR.get());
        LOGGER.info("[FTBoss] \u7cd6\u971c\u5973\u7687 Boss \u52a0\u8f7d\u5b8c\u6210 (ftboss)");
        MinecraftForge.EVENT_BUS.register(ManorFinalDamage.class);
        ManorArenaChunks.register();
    }
}

