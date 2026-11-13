package cn.ft.biomedungeon.runtime;

import cn.ft.biomedungeon.core.DungeonApi;
import cn.ft.biomedungeon.core.DungeonGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class Placement {
    record Operation(BlockPos position, BlockState state, net.minecraft.nbt.CompoundTag payload) {}
    final DungeonApi.Box bounds;
    final List<Operation> operations;
    final String digest;

    Placement(DungeonApi.Plan plan, TemplateCatalog catalog) {
        TemplateCatalog.require(!plan.rooms().isEmpty() && plan.rooms().size() <= 9, "Room count outside runtime cap");
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        Map<BlockPos, BlockState> cells = new HashMap<>();
        Map<BlockPos, net.minecraft.nbt.CompoundTag> payloads = new HashMap<>();
        Map<String, DungeonApi.PlacedRoom> nodes = new HashMap<>();
        for (DungeonApi.PlacedRoom room : plan.rooms()) {
            TemplateCatalog.require(nodes.putIfAbsent(room.nodeId(), room) == null, "Duplicate room node");
            TemplateCatalog.Loaded template = catalog.templates.get(room.templateId());
            TemplateCatalog.require(template != null, "Missing template");
            DungeonApi.Box actual = rotatedBounds(room, template.metadata().size());
            TemplateCatalog.require(actual.equals(room.bounds()), "Planner/runtime bounds mismatch");
            minX = Math.min(minX, actual.min().x()); minY = Math.min(minY, actual.min().y()); minZ = Math.min(minZ, actual.min().z());
            maxX = Math.max(maxX, actual.maxExclusive().x()); maxY = Math.max(maxY, actual.maxExclusive().y()); maxZ = Math.max(maxZ, actual.maxExclusive().z());
            Rotation rotation = Rotation.values()[room.quarterTurns()];
            for (TemplateCatalog.Cell cell : template.cells()) {
                BlockPos position = transformCell(cell.local(), room);
                TemplateCatalog.require(contains(actual, position), "Rotated payload outside declared bounds");
                TemplateCatalog.require(cells.putIfAbsent(position, cell.state().rotate(rotation)) == null, "Overlapping room payload");
                if (cell.payload() != null) {
                    payloads.put(position, PayloadPolicy.canonical(position, cell.state().rotate(rotation), cell.payload(), rotation));
                }
            }
        }
        Set<String> usedPorts = new HashSet<>();
        for (DungeonApi.Link link : plan.links()) {
            TemplateCatalog.require(link.width() <= 7 && link.height() <= 8, "Oversized corridor");
            DungeonApi.PlacedRoom from = nodes.get(link.fromNode()), to = nodes.get(link.toNode());
            TemplateCatalog.require(from != null && to != null, "Unknown corridor node");
            TemplateCatalog.require(usedPorts.add(link.fromNode() + ":" + link.fromPort())
                    && usedPorts.add(link.toNode() + ":" + link.toPort()), "Reused port");
            validateEndpoint(link, from, link.fromPort(), link.waypoints().get(0), catalog);
            validateEndpoint(link, to, link.toPort(), link.waypoints().get(link.waypoints().size() - 1), catalog);
            for (DungeonApi.Vec3i point : link.waypoints()) {
                minX = Math.min(minX, point.x() - 8); minY = Math.min(minY, point.y() - 2); minZ = Math.min(minZ, point.z() - 8);
                maxX = Math.max(maxX, point.x() + 9); maxY = Math.max(maxY, point.y() + 10); maxZ = Math.max(maxZ, point.z() + 9);
            }
        }
        TemplateCatalog.require(plan.links().size() == plan.rooms().size() - 1, "Only connected tree plans supported");
        Set<String> reached = new HashSet<>();
        reached.add(plan.rooms().get(0).nodeId());
        for (int pass = 0; pass < plan.rooms().size(); pass++) {
            for (DungeonApi.Link link : plan.links()) {
                if (reached.contains(link.fromNode())) { reached.add(link.toNode()); }
                if (reached.contains(link.toNode())) { reached.add(link.fromNode()); }
            }
        }
        TemplateCatalog.require(reached.size() == nodes.size(), "Disconnected plan");
        bounds = new DungeonApi.Box(new DungeonApi.Vec3i(minX - 3, minY - 3, minZ - 3),
                new DungeonApi.Vec3i(maxX + 3, maxY + 3, maxZ + 3));
        long volume = (long) (maxX - minX + 6) * (maxY - minY + 6) * (maxZ - minZ + 6);
        TemplateCatalog.require(maxX - minX <= 256 && maxY - minY <= 96 && maxZ - minZ <= 256
                && volume <= 1_500_000, "Envelope exceeds isolated runtime budget");
        for (DungeonApi.Link link : plan.links()) {
            corridor(cells, plan.rooms(), link);
        }
        List<Operation> result = new ArrayList<>((int) volume);
        for (int y = bounds.min().y(); y < bounds.maxExclusive().y(); y++) {
            for (int z = bounds.min().z(); z < bounds.maxExclusive().z(); z++) {
                for (int x = bounds.min().x(); x < bounds.maxExclusive().x(); x++) {
                    BlockPos position = new BlockPos(x, y, z);
                    BlockState state = shell(position) ? Blocks.BARRIER.defaultBlockState()
                            : cells.getOrDefault(position, Blocks.AIR.defaultBlockState());
                    net.minecraft.nbt.CompoundTag payload = payloads.get(position);
                    TemplateCatalog.require(payload == null || state.hasBlockEntity(), "Connector/sealing would delete source furniture BE");
                    result.add(new Operation(position, state, payload));
                }
            }
        }
        result.sort(Comparator.comparingInt((Operation operation) -> operation.state().is(Blocks.LAVA) ? 3 : operation.state().is(Blocks.VINE) ? 2 : shell(operation.position()) ? 0 : 1)
                .thenComparingInt(operation -> operation.position().getY())
                .thenComparingInt(operation -> operation.position().getZ())
                .thenComparingInt(operation -> operation.position().getX()));
        operations = List.copyOf(result);
        StringBuilder fingerprint = new StringBuilder();
        for (Operation operation : operations) {
            fingerprint.append(operation.position().asLong()).append(':')
                    .append(net.minecraft.nbt.NbtUtils.writeBlockState(operation.state())).append(':')
                    .append(operation.payload() == null ? "-" : AtomicFiles.sha256(payloadBytes(operation.payload()))).append('\n');
        }
        digest = AtomicFiles.sha256(fingerprint.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] payloadBytes(net.minecraft.nbt.CompoundTag tag) {
        try {
            java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
            net.minecraft.nbt.NbtIo.write(tag, new java.io.DataOutputStream(output));
            return output.toByteArray();
        } catch (java.io.IOException exception) { throw new IllegalStateException(exception); }
    }

    private static void validateEndpoint(DungeonApi.Link link, DungeonApi.PlacedRoom room,
                                         String portId, DungeonApi.Vec3i endpoint, TemplateCatalog catalog) {
        DungeonApi.Port port = catalog.templates.get(room.templateId()).metadata().ports().stream()
                .filter(candidate -> candidate.id().equals(portId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown port"));
        TemplateCatalog.require(port.medium().equals("dry") && port.width() == link.width()
                && port.height() == link.height(), "Corridor/port aperture mismatch");
        DungeonApi.Vec3i exterior = DungeonGeometry.exteriorFeet(DungeonGeometry.worldPort(
                catalog.templates.get(room.templateId()).metadata(), port, room.origin(), room.quarterTurns()));
        TemplateCatalog.require(exterior.equals(endpoint), "Endpoint not at core exterior port feet");
    }

    private static void sealPort(Map<BlockPos, BlockState> cells, DungeonApi.PlacedRoom room, DungeonApi.Port port) {
        boolean alongX = port.facing().equals("north") || port.facing().equals("south");
        for (int width = 0; width < port.width(); width++) {
            for (int height = 0; height < port.height(); height++) {
                BlockPos local = new BlockPos(port.feet().x() + (alongX ? width - (port.width() - 1) / 2 : 0),
                        port.feet().y() + height, port.feet().z() + (alongX ? 0 : width - (port.width() - 1) / 2));
                cells.put(transformCell(local, room), Blocks.STONE_BRICKS.defaultBlockState());
            }
        }
    }

    private static void openPort(Map<BlockPos, BlockState> cells, DungeonApi.PlacedRoom room, String portId, TemplateCatalog catalog) {
        DungeonApi.Template template = catalog.templates.get(room.templateId()).metadata();
        DungeonApi.Port port = template.ports().stream().filter(candidate -> candidate.id().equals(portId)).findFirst().orElseThrow();
        DungeonGeometry.WorldPort world = DungeonGeometry.worldPort(template, port, room.origin(), room.quarterTurns());
        boolean alongX = world.facing().equals("north") || world.facing().equals("south");
        int inside = world.surface() - (world.facing().equals("east") || world.facing().equals("south") ? 1 : 0);
        for (int transverse = world.transverseMin(); transverse < world.transverseMaxExclusive(); transverse++) {
            for (int height = -1; height <= world.height(); height++) {
                BlockPos position = new BlockPos(alongX ? transverse : inside, world.feet().y() + height, alongX ? inside : transverse);
                cells.put(position, height == -1 || height == world.height() ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static void corridor(Map<BlockPos, BlockState> cells, List<DungeonApi.PlacedRoom> rooms, DungeonApi.Link link) {
        Set<BlockPos> interior = new HashSet<>();
        Set<BlockPos> casing = new HashSet<>();
        for (int segment = 1; segment < link.waypoints().size(); segment++) {
            DungeonApi.Vec3i start = link.waypoints().get(segment - 1), end = link.waypoints().get(segment);
            TemplateCatalog.require(start.y() == end.y() && (start.x() == end.x() || start.z() == end.z()), "Only level orthogonal corridors supported");
            int deltaX = Integer.compare(end.x(), start.x()), deltaZ = Integer.compare(end.z(), start.z());
            int length = Math.abs(end.x() - start.x()) + Math.abs(end.z() - start.z());
            TemplateCatalog.require(length > 0 && length <= 256, "Invalid corridor segment");
            for (int step = 0; step <= length; step++) {
                int centerX = start.x() + deltaX * step, centerZ = start.z() + deltaZ * step;
                for (int offset = -(link.width() - 1) / 2 - 1; offset <= link.width() / 2 + 1; offset++) {
                    for (int height = -1; height <= link.height(); height++) {
                        BlockPos position = new BlockPos(centerX + (deltaZ != 0 ? offset : 0), start.y() + height,
                                centerZ + (deltaX != 0 ? offset : 0));
                        boolean inside = offset >= -(link.width() - 1) / 2 && offset <= link.width() / 2
                                && height >= 0 && height < link.height();
                        if (inside) {
                            TemplateCatalog.require(rooms.stream().noneMatch(room -> contains(room.bounds(), position)), "External connector would mutate original room");
                            interior.add(position);
                        }
                        else if (rooms.stream().noneMatch(room -> contains(room.bounds(), position))) { casing.add(position); }
                    }
                }
            }
        }
        for (BlockPos position : casing) { cells.put(position, Blocks.STONE_BRICKS.defaultBlockState()); }
        for (BlockPos position : interior) { cells.put(position, Blocks.AIR.defaultBlockState()); }
    }

    boolean shell(BlockPos position) {
        return position.getX() == bounds.min().x() || position.getX() == bounds.maxExclusive().x() - 1
                || position.getY() == bounds.min().y() || position.getY() == bounds.maxExclusive().y() - 1
                || position.getZ() == bounds.min().z() || position.getZ() == bounds.maxExclusive().z() - 1;
    }

    static boolean contains(DungeonApi.Box box, BlockPos position) {
        return position.getX() >= box.min().x() && position.getX() < box.maxExclusive().x()
                && position.getY() >= box.min().y() && position.getY() < box.maxExclusive().y()
                && position.getZ() >= box.min().z() && position.getZ() < box.maxExclusive().z();
    }

    static BlockPos transform(BlockPos local, DungeonApi.PlacedRoom room) {
        int rotatedX = switch (room.quarterTurns()) { case 1 -> -local.getZ(); case 2 -> -local.getX(); case 3 -> local.getZ(); default -> local.getX(); };
        int rotatedZ = switch (room.quarterTurns()) { case 1 -> local.getX(); case 2 -> -local.getZ(); case 3 -> -local.getX(); default -> local.getZ(); };
        return new BlockPos(room.origin().x() + rotatedX, room.origin().y() + local.getY(), room.origin().z() + rotatedZ);
    }

    static BlockPos transformCell(BlockPos local, DungeonApi.PlacedRoom room) {
        DungeonApi.Vec3i point = DungeonGeometry.blockPosition(new DungeonApi.Vec3i(local.getX(), local.getY(), local.getZ()),
                room.origin(), room.quarterTurns());
        return new BlockPos(point.x(), point.y(), point.z());
    }

    private static DungeonApi.Box rotatedBounds(DungeonApi.PlacedRoom room, DungeonApi.Vec3i size) {
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (int cornerX : new int[]{0, size.x()}) {
            for (int cornerZ : new int[]{0, size.z()}) {
                BlockPos corner = transform(new BlockPos(cornerX, 0, cornerZ), room);
                minX = Math.min(minX, corner.getX()); maxX = Math.max(maxX, corner.getX());
                minZ = Math.min(minZ, corner.getZ()); maxZ = Math.max(maxZ, corner.getZ());
            }
        }
        return new DungeonApi.Box(new DungeonApi.Vec3i(minX, room.origin().y(), minZ),
                new DungeonApi.Vec3i(maxX, room.origin().y() + size.y(), maxZ));
    }
}
