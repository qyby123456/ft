package net.farmingtales.teamlag;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(LagQuery.ID)
@PrefixGameTestTemplate(false)
public final class QueryGameTests {
    @GameTest(template = "arena", timeoutTicks = 100, batch = "teamQuery")
    public static void realTeamsQueryCrossDimensionTotalsWithoutRestrictions(GameTestHelper helper) throws Exception {
        Fixture first = new Fixture(helper, "QueryFirst", "f15a47d1-9011-41ae-96ab-4900e38acf01");
        Fixture second = new Fixture(helper, "QuerySecond", "f15a47d1-9011-41ae-96ab-4900e38acf02");
        var firstKey = new ChunkKey("minecraft:overworld", -22000, -22000);
        var netherKey = new ChunkKey("minecraft:the_nether", -22000, -22000);
        var idleKey = new ChunkKey("minecraft:overworld", -22001, -22000);
        var otherKey = new ChunkKey("minecraft:overworld", -22002, -22000);
        boolean onlineOnly = LagQuery.ONLINE_ONLY.get();
        try {
            first.claim(firstKey); first.claim(netherKey); first.claim(idleKey); second.claim(otherKey);
            LagQuery.ONLINE_ONLY.set(false);
            LagQuery.acceptObserved(Map.of(firstKey, 450.0, netherKey, 450.0, otherKey, 200.0,
                    new ChunkKey("minecraft:overworld", -22003, -22000), 99999.0), 100);
            var snapshot = TeamDirectory.snapshot();
            helper.assertTrue(snapshot.chunks(first.id).size() == 3, "all cross-dimension claims included");
            var measured = LagQuery.measurement(snapshot.chunks(first.id));
            helper.assertTrue(measured.micros() == 900 && measured.sampled() == 2, "900us team total, unsampled claims do not dilute");
            helper.assertTrue(LagQuery.measurement(snapshot.chunks(second.id)).micros() == 200, "other team isolated");
            Output output = new Output();
            var dispatcher = helper.getLevel().getServer().getCommands().getDispatcher();
            var source = helper.getLevel().getServer().createCommandSourceStack().withSource(output);
            helper.assertTrue(dispatcher.execute("teamlag " + first.id, source) == 1, "UUID query works from console");
            helper.assertTrue(output.text().contains("900.00") && output.text().contains("人均 900.00"), "actual command displays total and per capita");
            helper.assertTrue(output.text().contains("2/3") && !output.text().contains("99999"), "coverage shown and unclaimed cost excluded");
            helper.assertTrue(dispatcher.execute("teamlag", first.source.withSource(output)) == 1, "no-argument query selects player's own team");
            var here = first.source.withPosition(new net.minecraft.world.phys.Vec3(firstKey.x() * 16.0, 64, firstKey.z() * 16.0)).withSource(output);
            helper.assertTrue(dispatcher.execute("teamlag here", here) == 1, "here selects current claimed team");
            helper.assertTrue(dispatcher.execute("teamlag QueryFirst", source) == 1, "team name resolves: " + output.text());
            helper.assertTrue(dispatcher.execute("teamlag list", source) >= 2, "list queries claimed teams");
            LagQuery.ONLINE_ONLY.set(true); output.lines.clear();
            dispatcher.execute("teamlag " + first.id, source);
            if (TeamDirectory.team(first.id).online() == 0) helper.assertTrue(output.text().contains("不适用"), "offline teams do not divide by zero or invent an online member");
            helper.assertTrue(helper.getLevel().getChunkSource().getChunkNow(firstKey.x(), firstKey.z()) == null, "queries never force-load distant claims");
            helper.assertTrue(dispatcher.execute("teamlag " + UUID.randomUUID(), source) == 0, "unknown team reports missing");
            var root = dispatcher.getRoot().getChild("teamlag");
            helper.assertTrue(!root.canUse(source.withPermission(0)), "queries remain admin-only");
            for (String forbidden : List.of("freeze", "freezeteam", "release", "releaseteam", "limit", "economy"))
                helper.assertTrue(root.getChild(forbidden) == null, "no mutating command: " + forbidden);
            helper.assertTrue(dispatcher.getRoot().getChild("lagcontrol") == null, "legacy restriction commands are not registered");
        } finally {
            first.clear(); second.clear(); LagQuery.ONLINE_ONLY.set(onlineOnly); LagQuery.acceptObserved(Map.of(), 0);
        }
        helper.succeed();
    }
    @GameTest(template = "arena", timeoutTicks = 100, batch = "readOnly")
    public static void blockAndEntityTicksOnlyRecordTime(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        level.setBlockAndUpdate(pos, Blocks.FURNACE.defaultBlockState());
        var furnace = (FurnaceBlockEntity) level.getBlockEntity(pos);
        furnace.setItem(0, new ItemStack(Items.IRON_ORE, 64)); furnace.setItem(1, new ItemStack(Items.COAL, 64));
        double before = LagQuery.measuredMicros(ChunkKey.at(level, pos));
        var chunk = level.getChunkAt(pos);
        var ticker = new net.minecraft.world.level.block.entity.BlockEntityTicker<FurnaceBlockEntity>() {
            @Override public void tick(Level tickingLevel, BlockPos tickingPos, net.minecraft.world.level.block.state.BlockState state, FurnaceBlockEntity entity) {
                FurnaceBlockEntity.serverTick(tickingLevel, tickingPos, state, entity);
            }
        };
        try {
            Class<?> bound = Class.forName("net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity");
            var constructor = bound.getDeclaredConstructors()[0]; constructor.setAccessible(true);
            Object wrapper = constructor.newInstance(chunk, furnace, ticker);
            var tick = bound.getMethod("tick"); tick.setAccessible(true); tick.invoke(wrapper);
        } catch (ReflectiveOperationException error) { throw new IllegalStateException(error); }
        helper.assertTrue(LagQuery.measuredMicros(ChunkKey.at(level, pos)) > before, "actual block-entity ticker instrumented");
        helper.assertTrue(furnace.getItem(0).getCount() == 64 && furnace.getItem(1).getCount() >= 63, "furnace input not confiscated");
        var cow = EntityType.COW.create(level); cow.moveTo(pos.getX(), pos.getY(), pos.getZ()); level.addFreshEntity(cow);
        double entityBefore = LagQuery.measuredMicros(ChunkKey.at(level, pos)); level.tickNonPassenger(cow);
        helper.assertTrue(cow.isAlive() && LagQuery.measuredMicros(ChunkKey.at(level, pos)) > entityBefore, "animal tick measured, not limited or removed");
        var player = FakePlayerFactory.getMinecraft(level);
        double playerBefore = LagQuery.measuredMicros(ChunkKey.at(level, player.blockPosition()));
        MachineTiming.begin(player); MachineTiming.end(player);
        helper.assertTrue(LagQuery.measuredMicros(ChunkKey.at(level, player.blockPosition())) == playerBefore, "players excluded from native totals");
        cow.discard(); level.removeBlock(pos, false); helper.succeed();
    }
    static final class Output implements CommandSource {
        final List<String> lines = new ArrayList<>();
        @Override public void sendSystemMessage(Component text) { lines.add(text.getString()); }
        @Override public boolean acceptsSuccess() { return true; }
        @Override public boolean acceptsFailure() { return true; }
        @Override public boolean shouldInformAdmins() { return false; }
        String text() { return String.join("\n", lines); }
    }
    static final class Fixture {
        final UUID id;
        final CommandSourceStack source;
        final Object data;
        final List<ChunkKey> claims = new ArrayList<>();
        Fixture(GameTestHelper helper, String name, String uuid) throws Exception {
            ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.fromString(uuid), name));
            Object manager = TeamDirectory.manager();
            manager.getClass().getMethod("playerLoggedIn", ServerPlayer.class, UUID.class, String.class).invoke(manager, player, player.getUUID(), name);
            id = TeamDirectory.playerTeam(player.getUUID());
            Object chunks = Class.forName("dev.ftb.mods.ftbchunks.api.FTBChunksAPI").getMethod("api").invoke(null);
            Object chunkManager = chunks.getClass().getMethod("getManager").invoke(chunks);
            var team = ((java.util.Optional<?>) manager.getClass().getMethod("getTeamByID", UUID.class).invoke(manager, id)).orElseThrow();
            data = chunkManager.getClass().getMethod("getOrCreateData", Class.forName("dev.ftb.mods.ftbteams.api.Team")).invoke(chunkManager, team);
            source = player.createCommandSourceStack().withPermission(4);
        }
        Object position(ChunkKey key) throws Exception {
            return Class.forName("dev.ftb.mods.ftblibrary.math.ChunkDimPos").getConstructor(net.minecraft.resources.ResourceKey.class, int.class, int.class)
                    .newInstance(key.dimension().equals("minecraft:the_nether") ? Level.NETHER : Level.OVERWORLD, key.x(), key.z());
        }
        void claim(ChunkKey key) throws Exception {
            Object pos = position(key); data.getClass().getMethod("claim", CommandSourceStack.class, pos.getClass(), boolean.class).invoke(data, source, pos, false); claims.add(key);
            if (!id.equals(TeamDirectory.snapshot().claims().get(key))) throw new IllegalStateException("claim failed: " + key);
        }
        void clear() throws Exception { for (ChunkKey key : claims) { Object pos = position(key); data.getClass().getMethod("unclaim", CommandSourceStack.class, pos.getClass(), boolean.class).invoke(data, source, pos, false); } claims.clear(); }
    }
}
