package net.farmingtales.ftdungeonmobs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

final class DungeonSpawnData extends SavedData {
    static final String FILE_NAME = "ftdungeonmobs_spawn_points";
    UUID roundId;
    int tier = 1;
    final Map<String, Room> rooms = new LinkedHashMap<>();
    final Set<BlockPos> retired = new LinkedHashSet<>();
    List<Area> catalog;

    static DungeonSpawnData get(ServerLevel level) {
        if (!level.getServer().isSameThread()) {
            throw new IllegalStateException("Dungeon spawn points require the server thread.");
        }
        return level.getDataStorage().computeIfAbsent(DungeonSpawnData::load, DungeonSpawnData::new, FILE_NAME);
    }

    record Area(String id, BlockPos origin, Vec3i size, BlockPos entrance, String fingerprint) {
        AABB bounds() {
            return new AABB(origin, origin.offset(size));
        }

        boolean contains(BlockPos position) {
            return bounds().contains(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        }
    }

    record Room(Area area, List<Point> points) {}

    static final class Point {
        final BlockPos position;
        final List<SkeletonVariant> group;
        int spawned;
        boolean activated;

        Point(BlockPos position, List<SkeletonVariant> group) {
            this.position = position.immutable();
            this.group = List.copyOf(group);
        }

        boolean spent() {
            return spawned >= group.size();
        }
    }

    static DungeonSpawnData load(CompoundTag tag) {
        DungeonSpawnData data = new DungeonSpawnData();
        data.roundId = tag.hasUUID("RoundId") ? tag.getUUID("RoundId") : null;
        data.tier = Math.max(1, Math.min(5, tag.getInt("Tier")));
        for (Tag value : tag.getList("Retired", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) value;
            if (entry.contains("Position", Tag.TAG_LONG)) {
                data.retired.add(BlockPos.of(entry.getLong("Position")));
            }
        }
        for (Tag value : tag.getList("Rooms", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) value;
            String identity = entry.getString("Id");
            Vec3i size = new Vec3i(entry.getInt("SizeX"), entry.getInt("SizeY"), entry.getInt("SizeZ"));
            if (!identity.matches("[A-Z0-9_]{1,40}") || size.getX() < 1 || size.getX() > 96
                    || size.getY() < 1 || size.getY() > 64 || size.getZ() < 1 || size.getZ() > 96
                    || !entry.contains("Origin", Tag.TAG_LONG) || !entry.contains("Entrance", Tag.TAG_LONG)) {
                continue;
            }
            Area area = new Area(identity, BlockPos.of(entry.getLong("Origin")), size,
                    BlockPos.of(entry.getLong("Entrance")), entry.getString("Fingerprint"));
            List<Point> points = new ArrayList<>();
            for (Tag pointValue : entry.getList("Points", Tag.TAG_COMPOUND)) {
                CompoundTag pointTag = (CompoundTag) pointValue;
                BlockPos position = BlockPos.of(pointTag.getLong("Position"));
                List<SkeletonVariant> group = new ArrayList<>();
                try {
                    for (Tag mob : pointTag.getList("Group", Tag.TAG_STRING)) {
                        group.add(DungeonSpawnConfig.variant(mob.getAsString()));
                    }
                } catch (IllegalArgumentException exception) {
                    continue;
                }
                if (!area.contains(position) || group.isEmpty() || group.size() > 8 || points.size() >= 32
                        || points.stream().anyMatch(point -> point.position.equals(position))) {
                    continue;
                }
                Point point = new Point(position, group);
                point.spawned = Math.max(0, Math.min(group.size(), pointTag.getInt("Spawned")));
                point.activated = pointTag.getBoolean("Activated") || point.spawned > 0;
                points.add(point);
            }
            data.rooms.putIfAbsent(identity, new Room(area, List.copyOf(points)));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (roundId != null) {
            tag.putUUID("RoundId", roundId);
        }
        tag.putInt("Tier", tier);
        ListTag oldPoints = new ListTag();
        for (BlockPos position : retired) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Position", position.asLong());
            oldPoints.add(entry);
        }
        tag.put("Retired", oldPoints);
        ListTag entries = new ListTag();
        for (Room room : rooms.values()) {
            Area area = room.area();
            CompoundTag entry = new CompoundTag();
            entry.putString("Id", area.id());
            entry.putLong("Origin", area.origin().asLong());
            entry.putLong("Entrance", area.entrance().asLong());
            entry.putInt("SizeX", area.size().getX());
            entry.putInt("SizeY", area.size().getY());
            entry.putInt("SizeZ", area.size().getZ());
            entry.putString("Fingerprint", area.fingerprint());
            ListTag points = new ListTag();
            for (Point point : room.points()) {
                CompoundTag pointTag = new CompoundTag();
                pointTag.putLong("Position", point.position.asLong());
                pointTag.putInt("Spawned", point.spawned);
                pointTag.putBoolean("Activated", point.activated);
                ListTag group = new ListTag();
                point.group.forEach(variant -> group.add(StringTag.valueOf(variant.id())));
                pointTag.put("Group", group);
                points.add(pointTag);
            }
            entry.put("Points", points);
            entries.add(entry);
        }
        tag.put("Rooms", entries);
        return tag;
    }
}
