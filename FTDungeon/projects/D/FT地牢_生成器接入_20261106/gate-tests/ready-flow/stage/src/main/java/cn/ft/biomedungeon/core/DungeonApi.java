package cn.ft.biomedungeon.core;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class DungeonApi {
    private DungeonApi() {}
    public record Vec3i(int x, int y, int z) {}
    public record Box(Vec3i min, Vec3i maxExclusive) {
        public Box {
            Objects.requireNonNull(min, "min");
            Objects.requireNonNull(maxExclusive, "maxExclusive");
            if (min.x() >= maxExclusive.x() || min.y() >= maxExclusive.y() || min.z() >= maxExclusive.z()) {
                throw new IllegalArgumentException("Box requires min < maxExclusive on every axis");
            }
        }
    }
    public record Port(String id, Vec3i feet, String facing, int width, int height, String medium) {
        public Port {
            text(id, "port id");
            Objects.requireNonNull(feet, "feet");
            if (!List.of("north", "east", "south", "west").contains(facing)) {
                throw new IllegalArgumentException("Invalid port facing: " + facing);
            }
            if (width < 1 || width > 32 || height < 1 || height > 32) {
                throw new IllegalArgumentException("Port width/height must be 1..32");
            }
            if (!List.of("dry", "wet").contains(medium)) {
                throw new IllegalArgumentException("Invalid port medium: " + medium);
            }
        }
    }
    public record Template(String id, String biome, String resourceId, String sha256,
                           Vec3i size, List<Port> ports, String role) {
        public Template {
            text(id, "template id");
            text(biome, "biome");
            text(role, "role");
            if (resourceId == null || !resourceId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")
                    || resourceId.contains("..") || resourceId.contains(":/") || resourceId.endsWith("/")) {
                throw new IllegalArgumentException("Invalid resource location: " + resourceId);
            }
            if (sha256 == null || !sha256.matches("[0-9a-fA-F]{64}")) {
                throw new IllegalArgumentException("Template SHA-256 must be 64 hex characters");
            }
            Objects.requireNonNull(size, "size");
            if (size.x() < 1 || size.y() < 1 || size.z() < 1
                    || size.x() > 512 || size.y() > 512 || size.z() > 512) {
                throw new IllegalArgumentException("Template size must be 1..512 per axis");
            }
            ports = List.copyOf(ports);
            if (ports.size() > 64) {
                throw new IllegalArgumentException("At most 64 ports per template");
            }
            var ids = new HashSet<String>();
            for (Port port : ports) {
                if (!ids.add(port.id())) {
                    throw new IllegalArgumentException("Duplicate port id: " + port.id());
                }
            }
        }
    }
    public record Request(long worldSeed, String dimension, Vec3i anchor, long instanceOrdinal,
                          String generatorVersion, String biome, int roomCount, int maxAttempts) {
        public Request {
            text(dimension, "dimension");
            Objects.requireNonNull(anchor, "anchor");
            text(generatorVersion, "generatorVersion");
            text(biome, "biome");
            if (instanceOrdinal < 0 || roomCount < 1 || roomCount > 64
                    || maxAttempts < 1 || maxAttempts > 100_000) {
                throw new IllegalArgumentException("ordinal >= 0, roomCount 1..64, maxAttempts 1..100000 required");
            }
        }
    }
    public record PlacedRoom(String nodeId, String templateId, Vec3i origin, int quarterTurns, Box bounds) {
        public PlacedRoom {
            text(nodeId, "nodeId");
            text(templateId, "templateId");
            Objects.requireNonNull(origin, "origin");
            Objects.requireNonNull(bounds, "bounds");
            if (quarterTurns < 0 || quarterTurns > 3) {
                throw new IllegalArgumentException("quarterTurns must be 0..3");
            }
        }
    }
    public record Link(String fromNode, String fromPort, String toNode, String toPort,
                       List<Vec3i> waypoints, int width, int height) {
        public Link {
            text(fromNode, "fromNode");
            text(fromPort, "fromPort");
            text(toNode, "toNode");
            text(toPort, "toPort");
            waypoints = List.copyOf(waypoints);
            if (waypoints.size() < 2 || waypoints.size() > 3 || width < 1 || width > 32
                    || height < 1 || height > 32) {
                throw new IllegalArgumentException("Link requires 2..3 waypoints and dimensions 1..32");
            }
        }
    }
    public record Plan(String instanceId, long seed, String generatorVersion,
                       List<PlacedRoom> rooms, List<Link> links) {
        public Plan {
            text(instanceId, "instanceId");
            text(generatorVersion, "generatorVersion");
            rooms = List.copyOf(rooms);
            links = List.copyOf(links);
        }
    }
    public enum Phase { PLANNED, RESERVED, BUILDING, VERIFYING, COMMITTED, FAILED, ROLLBACK_REQUIRED }
    private static void text(String value, String name) {
        if (value == null || value.isBlank() || value.length() > 1024) {
            throw new IllegalArgumentException(name + " must be nonblank and at most 1024 characters");
        }
    }
}
