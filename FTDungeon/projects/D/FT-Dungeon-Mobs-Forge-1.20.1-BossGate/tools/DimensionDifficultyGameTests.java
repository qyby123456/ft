package net.farmingtales.ftdungeonmobs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftdungeonmobs")
@PrefixGameTestTemplate(false)
public final class DimensionDifficultyGameTests {
    private static int assertions;

    private DimensionDifficultyGameTests() {}

    @GameTest(template = "empty", batch = "zzz_dimension_difficulty", timeoutTicks = 400)
    public static void dimensionScopedDifficulty(GameTestHelper helper) {
        assertions = 0;
        var server = helper.getLevel().getServer();
        ServerLevel dungeon = server.getLevel(DungeonGateTravel.DUNGEON);
        ServerLevel overworld = server.overworld();
        ServerLevel nether = server.getLevel(Level.NETHER);
        check(helper, server instanceof GameTestServer && dungeon != null && nether != null,
                "Only use the isolated GameTest server with real FT and vanilla dimensions");
        server.setDifficulty(Difficulty.NORMAL, true);
        CompoundTag savedRound = DungeonGateData.get(server).save(new CompoundTag());
        Map<ServerLevel, CompoundTag> savedArenas = new LinkedHashMap<>();
        for (ServerLevel level : List.of(dungeon, overworld, nether)) {
            ArenaDifficultyApi.ArenaData previous = level.getDataStorage().get(ArenaDifficultyApi.ArenaData::load, "ftdungeonmobs_arenas");
            savedArenas.put(level, previous == null ? new CompoundTag() : previous.save(new CompoundTag()));
        }
        List<DungeonSkeletonEntity> entities = new ArrayList<>();
        Map<ServerLevel, Map<BlockPos, BlockState>> blocks = new LinkedHashMap<>();
        BlockPos origin = new BlockPos(-2048, 64, -2048);
        BlockPos gate = origin.offset(16, 0, 0);
        BlockPos outsideBrazier = gate.offset(3, 0, 0);
        DungeonGateData data = new DungeonGateData();
        overworld.getDataStorage().set("ftdungeonmobs_dungeon_gate", data);
        try {
            brazier(blocks, dungeon, origin);
            brazier(blocks, overworld, outsideBrazier);
            brazier(blocks, nether, origin);
            DungeonSkeletonEntity near = spawn(helper, entities, dungeon, origin.offset(2, 0, 0));
            DungeonSkeletonEntity far = spawn(helper, entities, dungeon, origin.offset(512, 0, 0));
            DungeonSkeletonEntity manual = spawn(helper, entities, dungeon, origin.offset(1024, 0, 0));
            DungeonSkeletonEntity outside = spawn(helper, entities, overworld, outsideBrazier.offset(2, 0, 0));
            DungeonSkeletonEntity otherDimension = spawn(helper, entities, nether, origin.offset(2, 0, 0));
            DungeonDifficultyApi.setTier(manual, 2);
            DungeonDifficultyApi.setOverrides(far, new DifficultyProfile(1.2, 1.1, 2, 0.15, 1.25));
            near.setHealth(near.getMaxHealth() * 0.4F);
            far.setHealth(far.getMaxHealth() * 0.4F);
            var outsideBefore = DungeonDifficultyApi.inspect(outside);
            var netherBefore = DungeonDifficultyApi.inspect(otherDimension);
            var manualBefore = DungeonDifficultyApi.inspect(manual);
            DungeonDifficultyApi.setTier(outside, 5);
            outside.storeDifficultyState(5, true, outside.difficultyOverrides(), DungeonDifficultyConfig.settings().revision(),
                    outside.level().getDifficulty());
            check(helper, DungeonDifficultyApi.inspect(outside).equals(outsideBefore),
                    "Previously radius-buffed automatic mobs return to their own world mapping after upgrading");
            check(helper, ArenaDifficultyApi.setTier(overworld, outsideBrazier, 5) == 0,
                    "Unbound overworld brazier does not refresh nearby mobs or another dimension");
            check(helper, ArenaDifficultyApi.setTier(nether, origin, 4) == 0,
                    "Unbound Nether brazier is not a local difficulty source");
            check(helper, ArenaDifficultyApi.findTier(overworld, outside.position()).isEmpty()
                    && ArenaDifficultyApi.findTier(nether, otherDimension.position()).isEmpty(),
                    "No radius influence in either vanilla dimension");
            check(helper, DungeonDifficultyApi.inspect(outside).equals(outsideBefore)
                    && DungeonDifficultyApi.inspect(otherDimension).equals(netherBefore), "Nearby outside attributes stay unchanged");
            check(helper, data.tier() == 1, "Unbound controllers cannot change the FT dungeon");
            CompoundTag oldArena = overworld.getDataStorage().get(ArenaDifficultyApi.ArenaData::load, "ftdungeonmobs_arenas")
                    .save(new CompoundTag());
            overworld.getDataStorage().set("ftdungeonmobs_arenas", ArenaDifficultyApi.ArenaData.load(oldArena));
            check(helper, ArenaDifficultyApi.findTier(overworld, outside.position()).isEmpty(),
                    "Saved legacy torch positions do not restore local difficulty");
            check(helper, ArenaDifficultyApi.findSource(overworld, Vec3.atCenterOf(outsideBrazier), 6).isPresent(),
                    "Position lookup remains available for entrance integration, not mob influence");
            int refreshed = ArenaDifficultyApi.setTier(dungeon, origin, 5);
            check(helper, refreshed >= 2, "Changing the dungeon brazier immediately refreshes loaded automatic mobs");
            BlockPos unloaded = new BlockPos(100000, 64, -100000);
            check(helper, !dungeon.hasChunkAt(unloaded), "Far query chunk starts unloaded");
            check(helper, ArenaDifficultyApi.findTier(dungeon, new Vec3(100000, 64, -100000)).orElseThrow() == 5,
                    "Dimension query is independent of position and does not load the queried chunk");
            check(helper, !dungeon.hasChunkAt(unloaded), "Reading dimension difficulty never forces a distant chunk to load");
            check(helper, data.dimensionTierSelected(), "Explicit interior choice is not overwritten by entrance polling");
            for (int tier = 1; tier <= 5; tier++) {
                ArenaDifficultyApi.setTier(dungeon, origin, tier);
                attributes(helper, near, tier);
                attributes(helper, far, tier);
                close(helper, near.getHealth() / near.getMaxHealth(), 0.4, "Near mob health ratio survives difficulty changes");
                close(helper, far.getHealth() / far.getMaxHealth(), 0.4, "Distant mob health ratio survives difficulty changes");
                check(helper, DungeonDifficultyApi.inspect(manual).equals(manualBefore), "Manual tier retains all attributes");
                check(helper, DungeonDifficultyApi.inspect(outside).equals(outsideBefore)
                        && DungeonDifficultyApi.inspect(otherDimension).equals(netherBefore), "No attributes leak into other dimensions");
            }
            near.setPos(origin.getX() + 768.5, 64, origin.getZ() + 0.5);
            check(helper, DungeonDifficultyApi.getTier(near) == 5, "Moving away from the brazier cannot lower the tier");
            DungeonSkeletonEntity newlySpawned = spawn(helper, entities, dungeon, origin.offset(1536, 0, 0));
            attributes(helper, newlySpawned, 5);
            CompoundTag savedEntity = far.saveWithoutId(new CompoundTag());
            DungeonSkeletonEntity restoredEntity = FTDungeonMobs.WARRIOR.get().create(dungeon);
            check(helper, restoredEntity != null, "A serialized FT mob can be restored");
            restoredEntity.load(savedEntity);
            restoredEntity.setUUID(UUID.randomUUID());
            BlockPos restoredPosition = origin.offset(2048, 0, 0);
            dungeon.getChunk(restoredPosition);
            restoredEntity.setPos(restoredPosition.getX() + 0.5, 64, restoredPosition.getZ() + 0.5);
            entities.add(restoredEntity);
            check(helper, dungeon.addFreshEntity(restoredEntity), "Restored automatic mob joins a distant chunk");
            attributes(helper, restoredEntity, 5);
            close(helper, restoredEntity.getHealth() / restoredEntity.getMaxHealth(), 0.4, "Saved health ratio survives entity NBT restoration");
            check(helper, far.difficultyOverrides().equals(new DifficultyProfile(1.2, 1.1, 2, 0.15, 1.25)),
                    "Per-entity custom bonuses are retained");
            DungeonGateData reloaded = DungeonGateData.load(data.save(new CompoundTag()));
            check(helper, reloaded.tier() == 5 && reloaded.dimensionTierSelected(), "Dimension tier and explicit selection survive save/load");
            overworld.getDataStorage().set("ftdungeonmobs_dungeon_gate", reloaded);
            data = reloaded;
            ArenaDifficultyApi.setTier(dungeon, origin, 0);
            check(helper, data.tier() == 5 && DungeonDifficultyApi.getTier(far) == 5, "Extinguishing retains the last valid dimension tier");
            put(blocks, dungeon, origin, Blocks.AIR.defaultBlockState());
            check(helper, data.tier() == 5, "Removing a controller does not erase the dimension tier");
            brazier(blocks, dungeon, origin);
            check(helper, data.tier() == 5, "Registering an unlit replacement cannot reset difficulty");
            for (int part = 0; part < 12; part++) {
                BlockPos position = DungeonGateBlock.partPosition(gate, Direction.NORTH, part);
                overworld.getChunk(position);
                if (part < 3) put(blocks, overworld, position.below(), Blocks.STONE.defaultBlockState());
                put(blocks, overworld, position, FTDungeonMobs.DUNGEON_ENTRY_GATE.get().defaultBlockState()
                        .setValue(DungeonGateBlock.FACING, Direction.NORTH).setValue(DungeonGateBlock.PART, part));
            }
            check(helper, DungeonGateBlock.complete(overworld, gate, FTDungeonMobs.DUNGEON_ENTRY_GATE.get(), Direction.NORTH),
                    "Use a real complete entrance for integration tests");
            data.bind(Level.OVERWORLD, gate);
            ArenaDifficultyApi.setTier(overworld, outsideBrazier, 3);
            check(helper, data.tier() == 3 && !data.dimensionTierSelected(), "Bound entrance selection targets only the FT dungeon");
            attributes(helper, far, 3);
            check(helper, DungeonDifficultyApi.inspect(outside).equals(outsideBefore), "Entrance neighbors do not receive the dungeon tier");
            ArenaDifficultyApi.setTier(dungeon, origin, 4);
            DungeonGateTravel.onArenaChanged(overworld, outsideBrazier);
            check(helper, data.tier() == 4, "Periodic entrance synchronization cannot undo an explicit dimension choice");
            reloaded = DungeonGateData.load(data.save(new CompoundTag()));
            overworld.getDataStorage().set("ftdungeonmobs_dungeon_gate", reloaded);
            data = reloaded;
            DungeonGateTravel.onArenaChanged(overworld, outsideBrazier);
            check(helper, data.tier() == 4, "An explicit dimension choice still wins after reloading saved data");
            ArenaDifficultyApi.setTier(overworld, outsideBrazier, 2);
            check(helper, data.tier() == 2 && !data.dimensionTierSelected(), "A new explicit entrance choice can select the next dungeon tier");
            UUID participant = UUID.randomUUID();
            long start = server.overworld().getGameTime();
            data.startRound(participant, start);
            locked(helper, dungeon, origin, 5);
            locked(helper, overworld, outsideBrazier, 5);
            check(helper, data.tier() == 2 && data.roundActive(), "Active round tier remains immutable");
            data.removeParticipant(participant);
            check(helper, data.finishRoundIfEmpty(start + DungeonGateData.ADMISSION_TICKS), "Existing round end unlock remains intact");
            BlockPos legacy = origin.offset(4, 0, 0);
            put(blocks, dungeon, legacy.below(), Blocks.STONE.defaultBlockState());
            put(blocks, dungeon, legacy, FTDungeonMobs.ARENA_TORCH.get().defaultBlockState()
                    .setValue(ArenaTorchBlock.HALF, DoubleBlockHalf.LOWER));
            put(blocks, dungeon, legacy.above(), FTDungeonMobs.ARENA_TORCH.get().defaultBlockState()
                    .setValue(ArenaTorchBlock.HALF, DoubleBlockHalf.UPPER));
            ArenaDifficultyApi.setTier(dungeon, legacy.above(), 5);
            check(helper, data.tier() == 5 && DungeonDifficultyApi.getTier(far) == 5,
                    "Legacy upper-half API uses the same dimension-scoped difficulty");
            check(helper, dungeon.getBlockState(legacy).getValue(ArenaTorchBlock.TIER) == 5
                    && dungeon.getBlockState(legacy.above()).getValue(ArenaTorchBlock.TIER) == 5, "Legacy halves remain synchronized");
            check(helper, DungeonDifficultyApi.inspect(manual).equals(manualBefore)
                    && DungeonDifficultyApi.inspect(outside).equals(outsideBefore)
                    && DungeonDifficultyApi.inspect(otherDimension).equals(netherBefore), "Manual and other-dimension mobs remain unchanged at completion");
            CompoundTag legacySave = new CompoundTag();
            legacySave.putInt("Tier", 3);
            check(helper, DungeonGateData.load(legacySave).tier() == 3 && !DungeonGateData.load(legacySave).dimensionTierSelected(),
                    "Existing saves without the new marker retain their tier and entrance behavior");
            System.out.println("DIMENSION_DIFFICULTY_TEST passed with " + assertions
                    + " assertions: remote mobs, all five attributes, health ratios, manual overrides, cross-dimension isolation, entrance link, persistent selection, legacy torch, active-round lock");
        } finally {
            entities.forEach(DungeonSkeletonEntity::discard);
            blocks.forEach((level, changes) -> changes.forEach((position, state) -> level.setBlock(position, state, Block.UPDATE_CLIENTS)));
            savedArenas.forEach((level, tag) -> level.getDataStorage().set("ftdungeonmobs_arenas", ArenaDifficultyApi.ArenaData.load(tag)));
            overworld.getDataStorage().set("ftdungeonmobs_dungeon_gate", DungeonGateData.load(savedRound));
        }
        helper.succeed();
    }

    private static void brazier(Map<ServerLevel, Map<BlockPos, BlockState>> blocks, ServerLevel level, BlockPos position) {
        level.getChunk(position);
        put(blocks, level, position.below(), Blocks.STONE.defaultBlockState());
        put(blocks, level, position, FTDungeonMobs.BLOODTHORN_BRAZIER.get().defaultBlockState());
    }

    private static void put(Map<ServerLevel, Map<BlockPos, BlockState>> blocks, ServerLevel level, BlockPos position, BlockState state) {
        blocks.computeIfAbsent(level, ignored -> new LinkedHashMap<>()).putIfAbsent(position.immutable(), level.getBlockState(position));
        level.setBlock(position, state, Block.UPDATE_CLIENTS);
    }

    private static DungeonSkeletonEntity spawn(GameTestHelper helper, List<DungeonSkeletonEntity> entities, ServerLevel level, BlockPos position) {
        level.getChunk(position);
        DungeonSkeletonEntity entity = FTDungeonMobs.WARRIOR.get().create(level);
        check(helper, entity != null, "Registered FT warrior can be created");
        entity.setPos(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        entity.setNoAi(true);
        entity.setPersistenceRequired();
        check(helper, level.addFreshEntity(entity), "FT warrior joins the actual dimension");
        entities.add(entity);
        return entity;
    }

    private static void attributes(GameTestHelper helper, DungeonSkeletonEntity entity, int tier) {
        check(helper, DungeonDifficultyApi.getTier(entity) == tier, "All automatic FT mobs use the dimension tier");
        DifficultyProfile profile = DungeonDifficultyConfig.settings().profile(entity.variant(), tier).combine(entity.difficultyOverrides());
        close(helper, entity.getMaxHealth(), entity.getAttributeBaseValue(Attributes.MAX_HEALTH) * profile.healthMultiplier(), "Health multiplier");
        close(helper, entity.getAttributeValue(Attributes.ATTACK_DAMAGE), entity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) * profile.attackMultiplier(), "Attack multiplier");
        close(helper, entity.getAttributeValue(Attributes.ARMOR), entity.getAttributeBaseValue(Attributes.ARMOR) + profile.armorBonus(), "Physical armor bonus");
        close(helper, entity.getAttributeValue(AttributeRegistry.SPELL_RESIST.get()), entity.getAttributeBaseValue(AttributeRegistry.SPELL_RESIST.get()) + profile.magicArmorBonus(), "Magic armor bonus");
        close(helper, entity.getAttributeValue(AttributeRegistry.SPELL_POWER.get()), entity.getAttributeBaseValue(AttributeRegistry.SPELL_POWER.get()) * profile.spellPowerMultiplier(), "Spell power multiplier");
    }

    private static void locked(GameTestHelper helper, ServerLevel level, BlockPos position, int tier) {
        BlockState previous = level.getBlockState(position);
        boolean rejected = false;
        try {
            ArenaDifficultyApi.setTier(level, position, tier);
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        check(helper, rejected && level.getBlockState(position).equals(previous), "Active round rejects changes before mutating either controller");
    }

    private static void close(GameTestHelper helper, double actual, double expected, String message) {
        check(helper, Math.abs(actual - expected) < 0.002, message + ": " + actual + " vs " + expected);
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        assertions++;
        helper.assertTrue(condition, message);
    }
}
