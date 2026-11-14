package net.farmingtales.ftboss;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/** 糖霜女皇 Boss mod（与 ftecon 价格 mod 分离后的独立 mod） */
@Mod(FTBossMod.MOD_ID)
public class FTBossMod {
    public static final String MOD_ID = "ftboss";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FTBossMod() {
        MinecraftForge.EVENT_BUS.register(this);
        var modBus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        net.farmingtales.ftboss.integ.tridents.TridentIntegration.register(modBus);
        net.farmingtales.ftboss.item.FTBossItems.register(modBus);
        net.farmingtales.ftboss.item.FTCreativeTabs.register(modBus); // 本模组专属创造模式物品栏
        net.farmingtales.ftboss.effect.FTEffects.register(modBus);   // 感染等状态效果(2026-09-11)
        net.farmingtales.ftboss.block.FTBlocks.register(modBus);
        net.farmingtales.ftboss.block.BlankStatueContent.register(modBus);
        net.farmingtales.ftboss.entity.FTEntities.register(modBus);
        net.farmingtales.ftboss.sound.FTSounds.register(modBus);
        /* 2026-09-16：本模组**第一个自定义网络通道**。为了「暗影镇印」地面魔法阵 ——
           那是世界贴花，粒子/音效之外的东西服务端不会自动同步（详见 FTNetwork 头注释）。 */
        net.farmingtales.ftboss.skill.FTNetwork.register();
        net.farmingtales.ftboss.skill.MaqueFeatherPacket.register();
        MinecraftForge.EVENT_BUS.register(new net.farmingtales.ftboss.boss.BossHandler());
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.boss.ManorFight.Ticker.class);
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.item.ThornCrownEffects.class);
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.skill.BossRelicDrops.class);
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.skill.YouziStatueSkills.class);
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.item.ManorWeaponEnhancements.class);
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.item.weapons.CleaverCombat.class);
        /* 2026-09-18 河豚棒【胀气毒雾】: 毒云的服务端刻表。
           类上也有 @Mod.EventBusSubscriber, 但本工程踩过"注解看着在、实际没注册"的坑
           (见下面 HealBan 的注释), 所以这里照 CleaverCombat 的做法显式再注册一次。 */
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.item.weapons.PufferBatCombat.class);
        net.farmingtales.ftboss.skill.YouziStatuePacket.register();
        /* ★★★ 2026-09-14 v106 按用户反馈「雪黎的禁疗未生效」★★★
           {@code HealBan} 上明明写着 @SubscribeEvent / @Mod.EventBusSubscriber, 但那个注解**只在
           类被当作 mod 主类扫描时才自动注册**, 它是个工具类 —— 结果 {@code onHeal} 从来没被调用过:
           禁疗只挂了个计时表(或 relics:anti_heal 图标), 一点血都没挡住。
           麻雀的 10% 禁疗同样一直是摆设。这里显式注册。 */
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.boss.ai.HealBan.class);
        /* 2026-09-14 v106 按用户反馈「爵士的爆炸会炸毁凋落物」:
           本模组实体的爆炸不再把地上的掉落物一起炸没(原版 ExplosionInteraction.NONE 只挡方块,
           实体伤害照算, 而 ItemEntity 是会被炸掉的)。见 SafeExplosion。 */
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.boss.ai.SafeExplosion.class);
        /* 2026-09-14 v117 按用户要求「折耳根如果连续命中玩家50次则伤害的50%变成虚空伤害」:
           拆伤害要挂在“玩家实际挨打”的那一刻, 所以单独一个监听器(和 HealBan 一样显式注册)。 */
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.boss.ai.ShadowVoidSplit.class);
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage.class);
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.boss.ai.ManorAttackFrames.class);
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.boss.ai.NonManorDamageX2.class);   // v130: 非庄园 Boss 伤害 x2
        /* 2026-09-21 按用户要求「青衣的伤害减半」: 她的出伤路径有六七条(近战/庄园三招/铁魔法闪电/
           激流/投戟/三叉戟落雷与本体补伤), 其中近战与原版三叉戟本体伤害**不走** manorSkillDamage,
           所以和上面那条一样在 LivingHurtEvent 这一处统一乘 —— 以后再加招也不会漏。 */
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.boss.ai.QingyiDamageHalved.class);
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.boss.ai.BossOutgoingDamageNerfs.class);
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.boss.ai.LemonSummonRewards.class);
        /* 2026-09-17 按用户要求「给蛋蛋加一个禁止玩家用末影珍珠传送的效果」:
           蛋蛋周围 48 格内, 玩家扔不出末影珍珠、珍珠落地也不传送(两条拦截见类注释)。 */
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.boss.ai.DandanPearlBan.class);

        /* 2026-09-14 v120 按用户「女仆防御还是有问题」: 雪黎的护法**只吃玩家的伤害**
           (地形窒息 / 别的 Boss 与其召唤物一律取消) —— 见 XueliMaids.onGuardHurt。 */
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.boss.ai.XueliMaids.class);
        MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.boss.ai.XueliCrushDamage.class);
        /* 2026-09-12 v24: 庄园禁术 —— 拦掉黑洞/止心术/橡肤(Iron's Spells)。
           铁魔法是**可选依赖**, 所以只在它已加载时才注册这个监听器:
           否则类里引用的 SpellPreCastEvent 在缺 mod 时会引发类加载错误。 */
        if (net.minecraftforge.fml.ModList.get().isLoaded("irons_spellbooks")) {
            net.farmingtales.ftboss.integ.spells.BossIronSpells.register(modBus);
            net.farmingtales.ftboss.integ.spells.ManorIronSpells.register(modBus);
            net.farmingtales.ftboss.integ.spells.QizaiPlayerSpells.register(modBus);
            MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.boss.ManorFightMembers.IronSummons.class);
            MinecraftForge.EVENT_BUS.register(net.farmingtales.ftboss.boss.ManorSpellBan.class);
            LOGGER.info("[FTBoss] 已挂载庄园禁术拦截(黑洞/止心术/橡肤)");
        }
        LOGGER.info("[FTBoss] 糖霜女皇 Boss 加载完成 (ftboss)");
    }
}
