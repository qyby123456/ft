package net.farmingtales.ftboss.block;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Boss mod 的装饰方块注册（石像 statue） */
public final class FTBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, FTBossMod.MOD_ID);
    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FTBossMod.MOD_ID);

    public static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FTBossMod.MOD_ID);
    public static final RegistryObject<Block> MANOR_LOOT_CHEST = BLOCKS.register("manor_loot_chest", () ->
            new ManorLootChestBlock(BlockBehaviour.Properties.copy(Blocks.BARREL).strength(2.5F, 1200.0F)
                    .noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
    public static final RegistryObject<Item> MANOR_LOOT_ITEM = BLOCK_ITEMS.register("manor_loot_chest", () ->
            new BlockItem(MANOR_LOOT_CHEST.get(), new Item.Properties().stacksTo(1).fireResistant()));
    public static final RegistryObject<Block> ORACLE = BLOCKS.register("oracle", () ->
            new OracleBlock(BlockBehaviour.Properties.copy(Blocks.ENCHANTING_TABLE)
                    .strength(4.0F, 1200.0F).lightLevel(state -> 7).noOcclusion()));
    public static final RegistryObject<Item> ORACLE_ITEM = BLOCK_ITEMS.register("oracle", () ->
            new BlockItem(ORACLE.get(), new Item.Properties()));
    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<ManorLootChestBlockEntity>> MANOR_LOOT_ENTITY =
            BLOCK_ENTITIES.register("manor_loot_chest", () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder
                    .of(ManorLootChestBlockEntity::new, MANOR_LOOT_CHEST.get()).build(null));

    /* 2026-09-12 晚按用户要求: **通用"交易用石像"(ftboss:statue) 已整体移除** ——
       方块/物品注册 + blockstate + 物品模型 + 掉落表 + 创造栏条目全部删掉,
       右键交易钩子也从 BossHandler 移除。下面只保留 7 座 Boss 石像。 */

    /** 五个 Boss 的「石像」: 1x1宽 2格高双半格。
     *  外观由 tools/model3d/gen_statue_skins.cjs 生成(每个 Boss 一张专属上色贴图 + 一张共用白膜);
     *  第二个参数 = 该石像的上色档位(1..5), 放下时默认就是这个档位, 左键可切回白膜(tint=0)。
     *  交易部分按用户要求**暂时留白**, 不注册任何交易语义。 */
    public static final RegistryObject<Block> STATUE_SUGAR_QUEEN = statue("statue_sugar_queen", 1);
    public static final RegistryObject<Block> STATUE_BUTCHER = statue("statue_butcher", 2);
    public static final RegistryObject<Block> STATUE_SAYA = statue("statue_saya", 3);
    public static final RegistryObject<Block> STATUE_WINE_BARREL = statue("statue_wine_barrel", 4);
    public static final RegistryObject<Block> STATUE_RAT_SWARM = statue("statue_rat_swarm", 5);
    /** 故障机器人石像(2026-09-12 新增) */
    public static final RegistryObject<Block> STATUE_DEFECT = statue("statue_defect", 6);
    /** Compost Superman statue (2026-09-12) */
    public static final RegistryObject<Block> STATUE_COMPOST = statue("statue_compost", 7);
    /** 苹果爵士·蜜脆的石像(第 8 座, 上色档位 8) */
    public static final RegistryObject<Block> STATUE_APPLE = statue("statue_apple", 8);
    public static final RegistryObject<Block> STATUE_TIDE_FISH = statue("statue_tide_fish", 9);
    public static final RegistryObject<Block> STATUE_BENGER = statue("statue_benger", 10);

    /** 注册一个石像方块 + 对应物品(tint = 上色档位) */
    private static RegistryObject<Block> statue(String id, int tint) {
        RegistryObject<Block> block = BLOCKS.register(id, () -> new CandyStatueBlock(
                BlockBehaviour.Properties.copy(Blocks.STONE)
                        .strength(1.5F, 6.0F)
                        .noOcclusion(), tint));
        BLOCK_ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    private FTBlocks() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}
