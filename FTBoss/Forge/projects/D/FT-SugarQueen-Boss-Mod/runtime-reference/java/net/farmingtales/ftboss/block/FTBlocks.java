/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.BlockEntityType$Builder
 *  net.minecraft.world.level.block.state.BlockBehaviour
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.material.PushReaction
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package net.farmingtales.ftboss.block;

import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.block.ManorLootChestBlock;
import net.farmingtales.ftboss.block.ManorLootChestBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class FTBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.BLOCKS, (String)"ftboss");
    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.ITEMS, (String)"ftboss");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create((IForgeRegistry)ForgeRegistries.BLOCK_ENTITY_TYPES, (String)"ftboss");
    public static final RegistryObject<Block> MANOR_LOOT_CHEST = BLOCKS.register("manor_loot_chest", () -> new ManorLootChestBlock(BlockBehaviour.Properties.m_60926_((BlockBehaviour)Blocks.f_50618_).m_60913_(2.5f, 1200.0f).m_60955_().m_278166_(PushReaction.BLOCK)));
    public static final RegistryObject<Item> MANOR_LOOT_ITEM = BLOCK_ITEMS.register("manor_loot_chest", () -> new BlockItem((Block)MANOR_LOOT_CHEST.get(), new Item.Properties().m_41487_(1).m_41486_()));
    public static final RegistryObject<BlockEntityType<ManorLootChestBlockEntity>> MANOR_LOOT_ENTITY = BLOCK_ENTITIES.register("manor_loot_chest", () -> BlockEntityType.Builder.m_155273_(ManorLootChestBlockEntity::new, (Block[])new Block[]{(Block)MANOR_LOOT_CHEST.get()}).m_58966_(null));
    public static final RegistryObject<Block> STATUE_SUGAR_QUEEN = FTBlocks.statue("statue_sugar_queen", 1);
    public static final RegistryObject<Block> STATUE_BUTCHER = FTBlocks.statue("statue_butcher", 2);
    public static final RegistryObject<Block> STATUE_SAYA = FTBlocks.statue("statue_saya", 3);
    public static final RegistryObject<Block> STATUE_WINE_BARREL = FTBlocks.statue("statue_wine_barrel", 4);
    public static final RegistryObject<Block> STATUE_RAT_SWARM = FTBlocks.statue("statue_rat_swarm", 5);
    public static final RegistryObject<Block> STATUE_DEFECT = FTBlocks.statue("statue_defect", 6);
    public static final RegistryObject<Block> STATUE_COMPOST = FTBlocks.statue("statue_compost", 7);
    public static final RegistryObject<Block> STATUE_APPLE = FTBlocks.statue("statue_apple", 8);
    public static final RegistryObject<Block> STATUE_TIDE_FISH = FTBlocks.statue("statue_tide_fish", 9);
    public static final RegistryObject<Block> STATUE_BENGER = FTBlocks.statue("statue_benger", 10);

    private static RegistryObject<Block> statue(String id, int tint) {
        RegistryObject block = BLOCKS.register(id, () -> new CandyStatueBlock(BlockBehaviour.Properties.m_60926_((BlockBehaviour)Blocks.f_50069_).m_60913_(1.5f, 6.0f).m_60955_(), tint));
        BLOCK_ITEMS.register(id, () -> new BlockItem((Block)block.get(), new Item.Properties()));
        return block;
    }

    private FTBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}

