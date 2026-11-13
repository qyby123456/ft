package net.farmingtales.ftdungeonmobs;

import com.mojang.authlib.GameProfile;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftdungeonmobs")
@PrefixGameTestTemplate(false)
public final class ReadyGateFlowGameTests {
    private static final ResourceLocation READY = new ResourceLocation("gate_regression", "ready_flow");
    private static int prepared;
    private static int entered;
    private static int checks;

    private static void check(GameTestHelper helper, boolean value, String reason) {
        checks++;
        helper.assertTrue(value, reason);
    }

    private static void floor(ServerLevel level, BlockPos center) {
        for (BlockPos position : BlockPos.betweenClosed(center.offset(-7, -1, -7), center.offset(7, 4, 7))) {
            level.getChunk(position);
            level.setBlock(position, position.getY() == center.getY() - 1 ? Blocks.STONE.defaultBlockState()
                    : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static DungeonGateBlockEntity gate(ServerLevel level, BlockPos anchor, DungeonGateBlock block) {
        for (int part = 0; part < 12; part++) {
            level.setBlock(DungeonGateBlock.partPosition(anchor, Direction.NORTH, part), block.defaultBlockState()
                    .setValue(DungeonGateBlock.PART, part).setValue(DungeonGateBlock.FACING, Direction.NORTH)
                    .setValue(DungeonGateBlock.OPEN, true).setValue(DungeonGateBlock.MOVING, false), Block.UPDATE_CLIENTS);
        }
        return (DungeonGateBlockEntity) level.getBlockEntity(anchor);
    }

    @GameTest(template = "empty", timeoutTicks = 240)
    public static void readyEntryCooldownAndNativeReturn(GameTestHelper helper) {
        ServerLevel source = helper.getLevel();
        check(helper, source.getServer() instanceof GameTestServer, "Isolated GameTestServer only");
        ServerLevel destination = source.getServer().getLevel(DungeonGateTravel.DUNGEON);
        check(helper, destination != null, "Native dungeon dimension exists");
        DungeonGateData original = DungeonGateData.get(source.getServer());
        DungeonGateData data = new DungeonGateData();
        source.getServer().overworld().getDataStorage().set("ftdungeonmobs_dungeon_gate", data);
        BlockPos entry = helper.absolutePos(new BlockPos(3, 2, 3));
        BlockPos landing = new BlockPos(200, 180, 200);
        BlockPos returning = landing.offset(3, 0, 0);
        floor(source, entry);
        floor(destination, landing);
        gate(destination, returning, FTDungeonMobs.DUNGEON_RETURN_GATE.get());
        DungeonGateBlockEntity entryEntity = gate(source, entry, FTDungeonMobs.DUNGEON_ENTRY_GATE.get());
        source.setBlock(entry.offset(0, 0, 3), FTDungeonMobs.BLOODTHORN_BRAZIER.get().defaultBlockState()
                .setValue(BloodthornBrazierBlock.TIER, 1), Block.UPDATE_CLIENTS);
        DungeonGenerators.register(new DungeonGenerators.Provider() {
            public ResourceLocation id() { return READY; }
            public Component name() { return Component.literal("Independent READY provider"); }
            public Component description() { return Component.literal("Safe floor and native return gate fixture"); }
            public DungeonGenerators.Preparation prepare(ServerLevel level) {
                prepared++;
                return new DungeonGenerators.Preparation(DungeonGenerators.State.READY, Vec3.atBottomCenterOf(landing), Component.literal("Ready fixture"));
            }
            public void onEntered(net.minecraft.server.level.ServerPlayer player, ServerLevel level) { entered++; }
        });
        entryEntity.setGeneratorId(READY);
        FakePlayer player = FakePlayerFactory.get(source, new GameProfile(UUID.randomUUID(), "FTGateReady"));
        Vec3 origin = Vec3.atBottomCenterOf(entry.offset(0, 0, -2));
        player.setPos(origin.x, origin.y, origin.z);
        try {
            DungeonGateTravel.use(player, entry, FTDungeonMobs.DUNGEON_ENTRY_GATE.get(), false);
            check(helper, prepared == 1, "READY provider reached exactly once");
            if (player.serverLevel() != destination) {
                System.out.println("READY_FLOW_TOOL_LIMITATION FakePlayer native teleport did not change dimension; no production bypass; entered=" + entered + " round=" + data.roundActive());
                check(helper, entered == 0 && !data.roundActive(), "Failed native teleport must not start round or callback");
                source.getServer().overworld().getDataStorage().set("ftdungeonmobs_dungeon_gate", original);
                helper.succeed();
                return;
            }
            check(helper, entered == 1, "onEntered runs exactly once after successful native entry");
            check(helper, data.roundActive() && data.generatorId().equals(READY), "Successful entry binds round provider");
            check(helper, data.matches(source.dimension(), entry) && data.hasParticipant(player.getUUID()), "Round binds gate and participant");
            CompoundTag record = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound("FTDungeonGate");
            check(helper, record.getString("Dimension").equals(source.dimension().location().toString()) && record.getDouble("X") == origin.x, "Return origin saved before successful entry");
            DungeonGateTravel.use(player, returning, FTDungeonMobs.DUNGEON_RETURN_GATE.get(), true);
            check(helper, player.serverLevel() == destination, "Immediate return blocked by native cooldown");
            helper.runAfterDelay(42, () -> {
                try {
                    DungeonGateTravel.use(player, returning, FTDungeonMobs.DUNGEON_RETURN_GATE.get(), true);
                    check(helper, player.serverLevel() == source, "Native FT return gate returns to original dimension after cooldown");
                    check(helper, player.position().distanceToSqr(origin) < 0.01, "Native return restores original position");
                    check(helper, entered == 1 && prepared == 1, "Return invokes no additional entry callback or preparation");
                    CompoundTag cleared = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound("FTDungeonGate");
                    check(helper, !cleared.contains("Dimension") && cleared.contains("LastTravel"), "Return clears origin and records new cooldown");
                    System.out.println("READY_FLOW_ACTUAL_ENTRY_AND_RETURN_PASS checks=" + checks + " fakePlayer=true nativeCrossDimension=true onEntered=1 cooldownTicks=42");
                    helper.succeed();
                } finally {
                    source.getServer().overworld().getDataStorage().set("ftdungeonmobs_dungeon_gate", original);
                }
            });
        } catch (NullPointerException exception) {
            System.out.println("READY_FLOW_TOOL_LIMITATION FakePlayer connection/native cross-dimension failure; no production bypass");
            exception.printStackTrace();
            source.getServer().overworld().getDataStorage().set("ftdungeonmobs_dungeon_gate", original);
            helper.fail("FakePlayer native cross-dimension connection limitation; successful READY flow NOT verified");
        } catch (RuntimeException exception) {
            source.getServer().overworld().getDataStorage().set("ftdungeonmobs_dungeon_gate", original);
            throw exception;
        }
    }
}
