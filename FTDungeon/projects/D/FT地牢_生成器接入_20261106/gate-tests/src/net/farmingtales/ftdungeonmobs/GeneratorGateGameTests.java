package net.farmingtales.ftdungeonmobs;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftdungeonmobs")
@PrefixGameTestTemplate(false)
public final class GeneratorGateGameTests {
    private static final ResourceLocation THIRD = new ResourceLocation("gate_regression", "third");
    private static final ResourceLocation UNKNOWN = new ResourceLocation("gate_regression", "missing");
    private static DungeonGenerators.State simulated = DungeonGenerators.State.BUILDING;
    private static int prepareCalls;
    private static int enteredCalls;
    private static int checks;

    private GeneratorGateGameTests() {}

    private static void check(GameTestHelper helper, boolean condition, String reason) {
        checks++;
        helper.assertTrue(condition, reason);
    }

    private static void rejects(GameTestHelper helper, Runnable action, String reason) {
        try { action.run(); }
        catch (IllegalStateException expected) { checks++; return; }
        helper.fail(reason);
    }

    private static DungeonGateBlockEntity gate(ServerLevel level, BlockPos anchor) {
        DungeonGateBlock block = FTDungeonMobs.DUNGEON_ENTRY_GATE.get();
        for (int part = 0; part < 12; part++) {
            BlockPos position = DungeonGateBlock.partPosition(anchor, Direction.NORTH, part);
            level.getChunk(position);
            level.setBlock(position, block.defaultBlockState().setValue(DungeonGateBlock.PART, part)
                    .setValue(DungeonGateBlock.FACING, Direction.NORTH).setValue(DungeonGateBlock.OPEN, true)
                    .setValue(DungeonGateBlock.MOVING, false), Block.UPDATE_CLIENTS);
        }
        return (DungeonGateBlockEntity) level.getBlockEntity(anchor);
    }

    private static DungeonGeneratorMenu menu(FakePlayer player, BlockPos anchor) throws Exception {
        List<DungeonGeneratorMenu.Entry> entries = DungeonGenerators.entries().stream()
                .map(provider -> new DungeonGeneratorMenu.Entry(provider.id(), provider.name(), provider.description())).toList();
        Constructor<DungeonGeneratorMenu> constructor = DungeonGeneratorMenu.class.getDeclaredConstructor(
                int.class, Inventory.class, BlockPos.class, List.class);
        constructor.setAccessible(true);
        return constructor.newInstance(77, player.getInventory(), anchor, entries);
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void generatorGateContract(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        check(helper, level.getServer() instanceof GameTestServer, "Only an isolated GameTestServer may execute fixtures");
        DungeonGateData original = DungeonGateData.get(level.getServer());
        DungeonGateData data = new DungeonGateData();
        level.getServer().overworld().getDataStorage().set("ftdungeonmobs_dungeon_gate", data);
        try {
            check(helper, DungeonGateData.load(new CompoundTag()).generatorId().equals(DungeonGenerators.ROOMS), "Legacy SavedData defaults to rooms");
            check(helper, DungeonGenerators.get(UNKNOWN) == null, "Unknown provider never falls back");
            if (DungeonGenerators.get(THIRD) == null) {
                DungeonGenerators.register(new DungeonGenerators.Provider() {
                    public ResourceLocation id() { return THIRD; }
                    public Component name() { return Component.literal("Independent third provider fixture"); }
                    public Component description() { return Component.literal("Negative gate regression only"); }
                    public DungeonGenerators.Preparation prepare(ServerLevel destination) {
                        prepareCalls++;
                        return new DungeonGenerators.Preparation(simulated, null, Component.literal("Not ready fixture"));
                    }
                    public void onEntered(net.minecraft.server.level.ServerPlayer player, ServerLevel destination) { enteredCalls++; }
                });
            }
            BlockPos first = helper.absolutePos(new BlockPos(3, 2, 3));
            BlockPos second = first.offset(12, 0, 0);
            DungeonGateBlockEntity firstGate = gate(level, first);
            DungeonGateBlockEntity secondGate = gate(level, second);
            firstGate.setGeneratorId(THIRD);
            check(helper, secondGate.generatorId().equals(DungeonGenerators.ROOMS), "Selections are per gate");
            DungeonGateBlockEntity restored = new DungeonGateBlockEntity(first, firstGate.getBlockState());
            restored.load(firstGate.saveWithoutMetadata());
            check(helper, restored.generatorId().equals(THIRD), "Selected provider survives native BE save/load");
            CompoundTag legacy = secondGate.saveWithoutMetadata();
            legacy.remove("GeneratorType");
            restored.load(legacy);
            check(helper, restored.generatorId().equals(DungeonGenerators.ROOMS), "Legacy BE defaults to rooms");
            CompoundTag unknown = firstGate.saveWithoutMetadata();
            unknown.putString("GeneratorType", UNKNOWN.toString());
            restored.load(unknown);
            check(helper, restored.generatorId().equals(UNKNOWN), "Unknown persisted provider is preserved, not rooms");
            data.bind(level.dimension(), first);
            data.setGeneratorId(THIRD);
            UUID participant = UUID.randomUUID();
            data.startRound(participant, 1);
            rejects(helper, () -> data.setGeneratorId(DungeonGenerators.ROOMS), "Active round must lock provider");
            rejects(helper, () -> firstGate.setGeneratorId(DungeonGenerators.ROOMS), "Active round must lock each gate");
            check(helper, DungeonGateData.load(data.save(new CompoundTag())).generatorId().equals(THIRD), "Round generator persists");
            data.removeParticipant(participant);
            check(helper, data.finishRoundIfEmpty(1L + DungeonGateData.ADMISSION_TICKS), "Empty finished round unlocks");
            data.setGeneratorId(DungeonGenerators.ROOMS);
            firstGate.setGeneratorId(THIRD);
            FakePlayer player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "FTGateContract"));
            player.setPos(first.getX() + 0.5, first.getY(), first.getZ() + 0.5);
            DungeonGeneratorMenu selection = menu(player, first);
            int thirdIndex = -1;
            for (int index = 0; index < selection.entries().size(); index++) {
                if (selection.entries().get(index).id().equals(THIRD)) { thirdIndex = index; }
            }
            check(helper, thirdIndex >= 0, "Third registered provider appears in menu snapshot");
            check(helper, !selection.clickMenuButton(player, -1) && !selection.clickMenuButton(player, selection.entries().size()), "Illegal indices rejected");
            player.setPos(first.getX() + 100, first.getY(), first.getZ());
            check(helper, !selection.clickMenuButton(player, thirdIndex), "Remote menu action rejected");
            player.setPos(first.getX() + 0.5, first.getY(), first.getZ() + 0.5);
            player.setGameMode(GameType.SPECTATOR);
            check(helper, !selection.clickMenuButton(player, thirdIndex), "Spectator menu action rejected");
            player.setGameMode(GameType.SURVIVAL);
            ServerLevel elsewhere = level.getServer().getLevel(Level.NETHER);
            check(helper, elsewhere != null, "Cross-dimension fixture exists");
            FakePlayer crossDimension = FakePlayerFactory.get(elsewhere, new GameProfile(UUID.randomUUID(), "FTGateOther"));
            crossDimension.setPos(player.getX(), player.getY(), player.getZ());
            check(helper, !selection.clickMenuButton(crossDimension, thirdIndex), "Cross-dimension menu action rejected");
            data.startRound(participant, 1);
            check(helper, !selection.clickMenuButton(player, thirdIndex), "Round-locked menu action rejected");
            data.removeParticipant(participant);
            data.finishRoundIfEmpty(1L + DungeonGateData.ADMISSION_TICKS);
            check(helper, selection.clickMenuButton(player, thirdIndex), "Valid third-provider selection succeeds after unlock");
            level.setBlock(first.offset(0, 0, 3), FTDungeonMobs.BLOODTHORN_BRAZIER.get().defaultBlockState()
                    .setValue(BloodthornBrazierBlock.TIER, 1), Block.UPDATE_CLIENTS);
            CompoundTag sentinel = new CompoundTag();
            sentinel.putString("Dimension", "fixture:original_return");
            sentinel.putDouble("X", 321.5);
            CompoundTag persistent = new CompoundTag();
            persistent.put("FTDungeonGate", sentinel.copy());
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);
            Vec3 start = player.position();
            int initialCalls = prepareCalls;
            for (DungeonGenerators.State state : List.of(DungeonGenerators.State.BUILDING, DungeonGenerators.State.FAILED)) {
                simulated = state;
                DungeonGateTravel.use(player, first, FTDungeonMobs.DUNGEON_ENTRY_GATE.get(), false);
                check(helper, player.serverLevel() == level && player.position().equals(start), "Non-ready provider never teleports: " + state);
                check(helper, !data.roundActive(), "Non-ready provider never starts round: " + state);
                check(helper, player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound("FTDungeonGate").equals(sentinel), "Non-ready provider preserves return record: " + state);
            }
            check(helper, prepareCalls == initialCalls + 2, "Both tests reached actual provider preparation, not earlier guards");
            firstGate.load(unknown);
            DungeonGateTravel.use(player, first, FTDungeonMobs.DUNGEON_ENTRY_GATE.get(), false);
            check(helper, prepareCalls == initialCalls + 2 && enteredCalls == 0, "Unknown provider invokes no fallback or entered callback");
            check(helper, !data.roundActive() && player.serverLevel() == level && player.position().equals(start), "Unknown provider leaves player and round unchanged");
            check(helper, player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound("FTDungeonGate").equals(sentinel), "Unknown provider preserves return record");
            System.out.println("INDEPENDENT_GENERATOR_GATE_CONTRACT_PASS checks=" + checks + " fakePlayer=true actualNetworkUI=false");
            helper.succeed();
        } finally {
            level.getServer().overworld().getDataStorage().set("ftdungeonmobs_dungeon_gate", original);
        }
    }
}
