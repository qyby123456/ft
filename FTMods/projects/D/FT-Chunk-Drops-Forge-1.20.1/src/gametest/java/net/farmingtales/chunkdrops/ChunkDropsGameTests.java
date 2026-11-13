package net.farmingtales.chunkdrops;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ChunkDropsMod.ID)
@PrefixGameTestTemplate(false)
public final class ChunkDropsGameTests {
    @GameTest(template = "empty", batch = "counts")
    public static void countsStacksAndSkipsOtherEntitiesWithoutMutation(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos position = helper.absolutePos(new BlockPos(2, 3, 2));
        List<Entity> entities = new ArrayList<>();
        DropScanner.Snapshot before = DropScanner.scan(level.getServer());
        try {
            ItemEntity stone = spawn(level, position.getX() + 0.25, position.getY(), position.getZ() + 0.5, new ItemStack(Items.COBBLESTONE, 64));
            ItemEntity iron = spawn(level, position.getX() + 0.75, position.getY(), position.getZ() + 0.5, new ItemStack(Items.IRON_INGOT, 7));
            ItemEntity dirt = spawn(level, position.getX() + 0.5, position.getY(), position.getZ() + 0.5, new ItemStack(Items.DIRT, 5));
            ItemEntity removed = spawn(level, position.getX(), position.getY(), position.getZ(), new ItemStack(Items.DIAMOND));
            ExperienceOrb orb = new ExperienceOrb(level, position.getX(), position.getY(), position.getZ(), 10);
            level.addFreshEntity(orb);
            entities.addAll(List.of(stone, iron, dirt, removed, orb));
            removed.discard();
            DropScanner.Snapshot after = DropScanner.scan(level.getServer());
            helper.assertTrue(after.entityCount() == before.entityCount() + 3, "three item entities, no XP orb or discarded entity");
            helper.assertTrue(after.itemCount() == before.itemCount() + 76, "64 + 7 + 5 = 76 items");
            helper.assertTrue(after.dimensions() >= 3, "all vanilla dimensions scanned");
            helper.assertTrue(find(after, stone.getUUID()).count() == 64, "one full stack remains one record");
            helper.assertTrue(find(after, iron.getUUID()).itemId().equals("minecraft:iron_ingot"), "registered item ID captured");
            helper.assertTrue(stone.isAlive() && iron.isAlive() && dirt.isAlive() && orb.isAlive(), "query does not remove entities");
            helper.assertTrue(stone.getItem().getCount() == 64 && iron.getItem().getCount() == 7, "query does not change stacks");
            helper.assertTrue(stone.getX() == position.getX() + 0.25, "query does not move entities");
            DropScanner.ChunkDrops chunk = DropScanner.scanChunk(level, stone.chunkPosition().x, stone.chunkPosition().z);
            helper.assertTrue(chunk.drops().stream().anyMatch(drop -> drop.uuid().equals(stone.getUUID())), "chunk detail includes its items");
        } finally {
            entities.forEach(Entity::discard);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "negativeCoordinates", timeoutTicks = 200)
    public static void negativeCoordinatesUseFloorAndActualChunkBoundaries(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<Integer> newlyForced = new ArrayList<>();
        for (int chunkX = -2; chunkX <= 1; chunkX++) {
            if (!level.getForcedChunks().contains(ChunkPos.asLong(chunkX, -1))) {
                level.setChunkForced(chunkX, -1, true);
                newlyForced.add(chunkX);
            }
            level.getChunk(chunkX, -1);
        }
        helper.runAfterDelay(5, () -> {
            List<Entity> entities = new ArrayList<>();
            double[] positions = {-16.001, -16.0, -0.001, 0.0, 15.999, 16.0};
            int[] chunks = {-2, -1, -1, 0, 0, 1};
            try {
                for (int index = 0; index < positions.length; index++) {
                    entities.add(spawn(level, positions[index], 90.25, -0.001, new ItemStack(Items.APPLE, index + 1)));
                }
                DropScanner.Snapshot snapshot = DropScanner.scan(level.getServer());
                for (int index = 0; index < positions.length; index++) {
                    UUID uuid = entities.get(index).getUUID();
                    DropScanner.ChunkDrops chunk = snapshot.chunks().stream()
                            .filter(candidate -> candidate.drops().stream().anyMatch(drop -> drop.uuid().equals(uuid))).findFirst().orElseThrow();
                    helper.assertTrue(chunk.key().x() == chunks[index] && chunk.key().z() == -1, "correct negative / boundary chunk at X=" + positions[index]);
                    helper.assertTrue(find(snapshot, uuid).x() == positions[index], "full coordinate precision retained");
                }
                Output output = new Output();
                CommandSourceStack source = source(level, output).withPosition(new Vec3(-0.001, 90.25, -0.001));
                execute(level, "chunkdrops here", source);
                helper.assertTrue(output.text().contains("区块 [-1, -1]"), "here uses floor, not truncation toward zero");
                helper.assertTrue(output.text().contains("X -16..-1"), "negative block range is correct");
                helper.assertTrue(output.text().contains("-0.001"), "chat reports item position");
            } finally {
                entities.forEach(Entity::discard);
                newlyForced.forEach(chunkX -> level.setChunkForced(chunkX, -1, false));
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = "commands")
    public static void commandsPaginateCheckPermissionsAndDoNotLoadChunks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos position = helper.absolutePos(new BlockPos(2, 3, 2));
        List<Entity> entities = new ArrayList<>();
        Output output = new Output();
        CommandSourceStack source = source(level, output).withPosition(Vec3.atCenterOf(position));
        int chunkX = position.getX() >> 4;
        int chunkZ = position.getZ() >> 4;
        String query = "chunkdrops chunk " + chunkX + " " + chunkZ;
        try {
            for (int index = 1; index <= 12; index++) {
                entities.add(spawn(level, position.getX() + 0.25, position.getY() + index / 100.0,
                        position.getZ() + 0.75, new ItemStack(Items.DIRT, index)));
            }
            helper.assertTrue(execute(level, query, source) == 12, "command returns the chunk's entity count");
            helper.assertTrue(output.itemLines() == 10 && output.text().contains("第 1/2 页"), "first page has ten entries");
            helper.assertTrue(output.text().contains("物品总数 78") && output.text().contains("×12"), "stack count and total are separate");
            helper.assertTrue(output.components.stream().flatMap(component -> component.getSiblings().stream())
                    .filter(component -> component.getStyle().getClickEvent() != null)
                    .allMatch(component -> component.getStyle().getClickEvent().getAction() == ClickEvent.Action.SUGGEST_COMMAND), "coordinates suggest, never execute, teleport");
            output.components.clear();
            execute(level, query + " 2", source);
            helper.assertTrue(output.itemLines() == 2 && output.text().contains("#11") && output.text().contains("#12"), "second page contains the last two entries");
            output.components.clear();
            helper.assertTrue(execute(level, query + " 3", source) == 0 && output.text().contains("页码超出范围"), "invalid page is explained");
            output.components.clear();
            helper.assertTrue(execute(level, "chunkdrops top 1", source) >= 12 && output.itemLines() == 1, "top limit restricts ranking output");
            helper.assertTrue(execute(level, "chunkdrops", source) >= 12, "default command is registered");
            helper.assertTrue(execute(level, "chunkdrops help", source) == 1, "help works from console source");
            helper.assertTrue(rejected(level, "chunkdrops", source.withPermission(1)), "non-OP cannot scan");
            helper.assertTrue(rejected(level, "chunkdrops report", source.withPermission(1)), "non-OP cannot export");
            helper.assertTrue(rejected(level, "chunkdrops top 0", source), "zero top limit rejected");
            helper.assertTrue(rejected(level, "chunkdrops top 51", source), "oversized top limit rejected");
            helper.assertTrue(rejected(level, query + " 0", source), "zero page rejected");
            int loadedBefore = level.getChunkSource().getLoadedChunksCount();
            output.components.clear();
            helper.assertTrue(!level.hasChunk(1_000_000, 1_000_000), "remote test chunk starts unloaded");
            helper.assertTrue(execute(level, "chunkdrops chunk 1000000 1000000", source) == 0, "unloaded chunk query returns zero");
            helper.assertTrue(output.text().contains("未加载") && !level.hasChunk(1_000_000, 1_000_000), "unloaded chunk stays unloaded");
            helper.assertTrue(level.getChunkSource().getLoadedChunksCount() == loadedBefore, "query loads no chunks");
            entities.forEach(Entity::discard);
            output.components.clear();
            helper.assertTrue(execute(level, query, source) == 0 && output.text().contains("实体 0 / 物品总数 0"), "empty loaded chunk reports zero clearly");
        } finally {
            entities.forEach(Entity::discard);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "exportAndDimensions", timeoutTicks = 200)
    public static void crossDimensionCommandsAndCsvKeepEveryEntityAndCoordinate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerLevel nether = level.getServer().getLevel(Level.NETHER);
        boolean alreadyForced = nether.getForcedChunks().contains(ChunkPos.asLong(0, 0));
        nether.setChunkForced(0, 0, true);
        nether.getChunk(0, 0);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                nether.isPositionEntityTicking(new BlockPos(4, 90, 4)) && nether.areEntitiesLoaded(ChunkPos.asLong(0, 0)),
                "Nether test chunk is entity-ticking and its entity storage is loaded")).thenExecute(() -> {
            List<Entity> entities = new ArrayList<>();
            try {
                BlockPos position = helper.absolutePos(new BlockPos(2, 3, 2));
                ItemEntity overworld = spawn(level, position.getX() + 0.125, position.getY(), position.getZ() + 0.25, new ItemStack(Items.EMERALD, 9));
                ItemEntity otherDimension = spawn(nether, 4.123456789, 90.125, 4.75, new ItemStack(Items.GOLD_INGOT, 23));
                entities.addAll(List.of(overworld, otherDimension));
                DropScanner.Snapshot snapshot = DropScanner.scan(level.getServer());
                helper.assertTrue(snapshot.chunks().stream().anyMatch(chunk -> chunk.key().dimension().equals("minecraft:the_nether")
                        && chunk.drops().stream().anyMatch(drop -> drop.uuid().equals(otherDimension.getUUID()))), "server scan includes a real Nether item entity");
                Output output = new Output();
                CommandSourceStack source = source(level, output);
                helper.assertTrue(execute(level, "execute in minecraft:the_nether run chunkdrops chunk 0 0", source) >= 1, "vanilla execute in selects query dimension");
                helper.assertTrue(output.text().contains("minecraft:the_nether") && output.text().contains("minecraft:gold_ingot"), "cross-dimension command lists the correct item");
                output.components.clear();
                execute(level, "chunkdrops top 50", source);
                helper.assertTrue(output.text().contains("minecraft:the_nether"), "ranking includes Nether chunks");
                Path directory = level.getServer().getServerDirectory().toPath().resolve("reports").resolve("chunkdrops");
                Files.createDirectories(directory);
                Set<Path> before;
                try (var files = Files.list(directory)) {
                    before = files.collect(Collectors.toSet());
                }
                output.components.clear();
                helper.assertTrue(execute(level, "chunkdrops report", source) == 1, "actual report command succeeds");
                Path report;
                try (var files = Files.list(directory)) {
                    report = files.filter(file -> !before.contains(file)).findFirst().orElseThrow();
                }
                List<String> lines = Files.readAllLines(report, StandardCharsets.UTF_8);
                helper.assertTrue(lines.get(0).startsWith("\ufeffdimension,chunk_x,chunk_z,"), "UTF-8 BOM and CSV header present");
                helper.assertTrue(lines.size() == snapshot.entityCount() + 1, "CSV includes all entities without chat pagination");
                String row = lines.stream().filter(line -> line.contains(otherDimension.getUUID().toString())).findFirst().orElseThrow();
                String[] fields = row.split(",");
                helper.assertTrue(fields.length == 9 && fields[0].equals("minecraft:the_nether") && fields[4].equals("minecraft:gold_ingot"), "CSV dimension and item ID correct");
                helper.assertTrue(fields[5].equals("23") && Double.parseDouble(fields[6]) == 4.123456789
                        && Double.parseDouble(fields[7]) == 90.125 && Double.parseDouble(fields[8]) == 4.75, "CSV keeps exact XYZ and stack count");
                helper.assertTrue(output.text().contains(report.toAbsolutePath().toString()), "report command returns the actual full path");
                Path empty = DropReport.write(new DropScanner.Snapshot(List.of(), 0, 0, 3, 0), directory);
                helper.assertTrue(!empty.equals(report) && Files.readAllLines(empty, StandardCharsets.UTF_8).size() == 1, "empty report has a header and does not overwrite the prior report");
                helper.assertTrue(overworld.isAlive() && otherDimension.isAlive(), "export leaves both dimension entities alive");
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            } finally {
                entities.forEach(Entity::discard);
                if (!alreadyForced) {
                    nether.setChunkForced(0, 0, false);
                }
            }
            helper.succeed();
        });
    }

    private static ItemEntity spawn(ServerLevel level, double x, double y, double z, ItemStack stack) {
        ItemEntity entity = new ItemEntity(level, x, y, z, stack);
        entity.setNoGravity(true);
        entity.setDeltaMovement(Vec3.ZERO);
        if (!level.addFreshEntity(entity)) {
            throw new IllegalStateException("Could not spawn test item");
        }
        return entity;
    }

    private static DropScanner.Drop find(DropScanner.Snapshot snapshot, UUID uuid) {
        return snapshot.chunks().stream().flatMap(chunk -> chunk.drops().stream())
                .filter(drop -> drop.uuid().equals(uuid)).findFirst().orElseThrow();
    }

    private static CommandSourceStack source(ServerLevel level, Output output) {
        return level.getServer().createCommandSourceStack().withLevel(level).withSource(output).withPermission(4);
    }

    private static int execute(ServerLevel level, String command, CommandSourceStack source) {
        try {
            return level.getServer().getCommands().getDispatcher().execute(command, source);
        } catch (CommandSyntaxException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static boolean rejected(ServerLevel level, String command, CommandSourceStack source) {
        try {
            level.getServer().getCommands().getDispatcher().execute(command, source);
            return false;
        } catch (CommandSyntaxException exception) {
            return true;
        }
    }

    private static final class Output implements CommandSource {
        final List<Component> components = new ArrayList<>();

        @Override
        public void sendSystemMessage(Component component) {
            components.add(component);
        }

        @Override
        public boolean acceptsSuccess() {
            return true;
        }

        @Override
        public boolean acceptsFailure() {
            return true;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }

        String text() {
            return components.stream().map(Component::getString).collect(Collectors.joining("\n"));
        }

        long itemLines() {
            return components.stream().filter(component -> component.getString().startsWith("#")).count();
        }
    }
}
