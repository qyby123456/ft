package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 创造模式物品栏接入：本模组**单独一栏**，全部内容放一起。
 *
 * <p><b>2026-09-10 调整（按用户"单独开一栏放一起，参考其他 mod 的放入法"）</b>：
 * <ol>
 *   <li><b>位置</b>：{@code withTabsBefore(CreativeModeTabs.SPAWN_EGGS)} —— 让本页紧跟在**原版标签之后**。
 *       此前没有任何位置约束，本页会排在 390 个 mod 的标签最末尾（实际被挤出屏幕，用户根本找不到，
 *       于是以为"没有这一栏"）。多数 mod 都是这么定位的。</li>
 *   <li><b>搜索框</b>：{@code withSearchBar()} —— 该页自带搜索栏，找东西不用再去翻原版搜索页。</li>
 *   <li><b>不再往原版页重复投放</b>：删掉了"武器塞进战斗页 / 石像塞进建筑页"的两个
 *       {@code BuildCreativeModeTabContentsEvent} 钩子 —— 既然所有内容都集中在本页，就不必再散落两处。
 *       <br>（注意：日志里的 {@code [zeta/]: Creative tab loop found ...} 警告是 **Quark 自己的方块**
 *       {@code quark:*_bricks} 的排序问题，**与本模组无关**，删钩子只是为了不重复，不是为了修它。）</li>
 * </ol>
 * 内容顺序：召唤物 → 武器 → 石像（共 16 项）。
 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class FTCreativeTabs {

    /** 创造模式物品栏注册器 */
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FTBossMod.MOD_ID);

    /** 本模组专属页：召唤物 / 武器 / 石像 一览 */
    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ftboss.main"))
            .icon(() -> new ItemStack(FTBossItems.SUGAR_QUEEN_CRYSTAL.get()))
            // 紧跟原版标签(排在刷怪蛋之前), 而不是被挤到所有 mod 标签的最后
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .withSearchBar()
            .displayItems((params, out) -> {
                out.accept(FTBossItems.MANOR_STOP_TOKEN.get());
                /* ===== v138 按用户要求重排：召唤物 → 武器 → 石像 → 凋落物 → 刷怪蛋 → 唱片 ===== */

                /* ---------- ① 召唤物 ---------- */
                out.accept(FTBossItems.SUGAR_QUEEN_CRYSTAL.get());   // 姜饼王
                out.accept(FTBossItems.TIDE_BANQUET.get());
                out.accept(FTBossItems.MEAT_HEART.get());            // 屠夫
                out.accept(FTBossItems.TEA_SEED.get());              // 沙耶
                out.accept(FTBossItems.WINE_CHARM.get());            // 酒桶王
                out.accept(FTBossItems.RAT_BAIT.get());              // 鼠群
                out.accept(FTBossItems.DEFECT_CORE.get());           // 故障机器人
                out.accept(FTBossItems.COMPOST_SUMMON.get());        // 炫彩答辩超人
                out.accept(FTBossItems.FORBIDDEN_APPLE.get());       // 苹果爵士
                out.accept(FTBossItems.BENGER_WHISTLE.get());        // 边牧犬王【牧羊哨】

                /* ---------- ② 武器 ---------- */
                out.accept(FTBossItems.GOURMET_BLANK_BLADE.get());   // 创造限定：22槽 / 8次随机食物进化
                out.accept(FTBossItems.DEFECT_CLAW.get());
                out.accept(FTBossItems.TURKEY_LEGS.get());           // 肉系·大肉腿(2026-09-14 用户提供的模型)
                out.accept(FTBossItems.WINE_BOTTLE_MACE.get());      // 酒系·酒瓶锤
                out.accept(FTBossItems.CHEESE_WHEEL_MACE.get());     // 奶酪系·奶酪锤
                out.accept(FTBossItems.COMPOST_CROWN.get());
                out.accept(FTBossItems.CANDY_CANE.get());            // 姜饼王掉落
                out.accept(FTBossItems.BLOOMING_THORNS.get());       // 沙耶掉落【绽放的荆棘】
                out.accept(FTBossItems.THORN_CROWN.get());
                out.accept(FTBossItems.THORN_CHESTPLATE.get());
                out.accept(FTBossItems.THORN_LEGGINGS.get());
                out.accept(FTBossItems.THORN_BOOTS.get());
                out.accept(FTBossItems.SILVER_KEY.get());
                out.accept(FTBossItems.FOOD_APPRAISAL_MONOCLE.get());
                out.accept(FTBossItems.APPLE_STEM.get());            // 苹果爵士掉落
                out.accept(FTBossItems.TIDE_TRIDENT.get());
                BossRelicItems.addTo(out);
                out.accept(FTBossItems.DOG_BEATING_STICK.get());     // benger 掉落【打狗棒】
                ManorWeaponCatalog.addTo(out);
                out.accept(FTBossItems.QINGYI_CONFLUENCE_TRIDENT.get());
                out.accept(net.farmingtales.ftboss.block.FTBlocks.MANOR_LOOT_ITEM.get());
                out.accept(net.farmingtales.ftboss.block.FTBlocks.ORACLE_ITEM.get());
                out.accept(FTBossItems.BARREL_CROSSBOW.get());       // 酒桶弩（空弦）
                out.accept(FTBossItems.BARREL_CROSSBOW_READY.get()); // 酒桶弩（装填完成，外观件）

                /* ---------- ③ 石像 ---------- */
                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_SUGAR_QUEEN.get().asItem());
                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_BUTCHER.get().asItem());
                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_SAYA.get().asItem());
                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_WINE_BARREL.get().asItem());
                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_RAT_SWARM.get().asItem());
                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_DEFECT.get().asItem());
                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_COMPOST.get().asItem());
                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_APPLE.get().asItem());
                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_TIDE_FISH.get().asItem());
                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_BENGER.get().asItem());

                /* ---------- ④ 凋落物（材料类） ---------- */
                /* 苹果核已于 2026-09-18 按用户要求删除（连同物品注册与掉落） */
                out.accept(FTBossItems.TIDE_PEARL.get());

                /* ---------- ⑤ 刷怪蛋 ---------- */
                out.accept(FTBossItems.BENGER_SPAWN_EGG.get());       // 边牧犬王
                for (var egg : FTBossItems.EASTER_EGGS) out.accept(egg.get());   // 隐藏 Boss 十人

                /* ---------- ⑥ 唱片 ---------- */
                out.accept(FTBossItems.MUSIC_DISC_MANOR_CUE.get());   // 庄园·序
                out.accept(FTBossItems.MUSIC_DISC_MANOR_BATTLE.get()); // 庄园·战
                out.accept(FTBossItems.MUSIC_DISC_MANOR_DUAL.get());  // 庄园·双轨
                out.accept(FTBossItems.MUSIC_DISC_MANOR_GUIGNOL.get()); // 庄园·鏖战
                out.accept(FTBossItems.MUSIC_DISC_BENGER.get());      // 牧野犬王
                for (var foodDisc : FTBossItems.FOOD_MUSIC_DISCS) out.accept(foodDisc.get());   // 食物系 10 张
            })
            .build());

    private FTCreativeTabs() {}

    /** 在 mod 构造里调用 */
    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
